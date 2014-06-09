/**
 *
 * Process Editor - Choreography Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.choreography.enforceabilityChecker;

import com.inubit.research.animation.AnimationFacade.Type;
import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.choreography.AbstractChoreographyPlugin;
import com.inubit.research.gui.plugins.choreography.Utils;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.TextAnnotation;

/**
 * A workbench plugin for checking the Enforceability of a BPMN 2.0 Choreography
 * diagram. This plugin is able to tell, wheter a choreography is enforceable and
 * if there are errors ("non-enforceabilities") it will show, where they are and
 * briefly describe the problem.
 * @author tmi
 */
public class EnforceabilityPlugin extends AbstractChoreographyPlugin {

    /**
     * maps from the ID of a choreography-diagram to a map, that maps from a
     * ProcessNode, that was added to the diagram (in order to show a fault) to
     * a Collection of ProcessObjects, that were modified in order to show some
     * fault.
     */
    private Map<String, Map<ProcessObject, Collection<ProcessObject>>> modifications =
            new HashMap<String, Map<ProcessObject, Collection<ProcessObject>>>();
    /**
     * maps from the id of a modified ProcessObject to a map, that maps from the
     * name of a modified property to its original value.
     */
    private Map<String, Map<String, String>> modifiedProperties =
            new HashMap<String, Map<String, String>>();
    /**
     * maps from the id of a ProcessModel to the ProcessModel Listener, that has
     * been added to this model.
     */
    private Map<String, ProcessModelListener> modelListeners =
            new HashMap<String, ProcessModelListener>();
    /**
     * enables the following function: When a TextAnnotation, that was added to
     * the model by this plugin, is selected, all nodes that are connected to this
     * problem, are highlighted
     */
    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseReleased(MouseEvent e) {
            updateHighlighting();
        }
    };

    /**
     * states, wich level of enforceability is achieved by the choreography.
     */
    public enum ErrorLevel {

        /**
         * no Errors - the choreography is fully enforceable
         */
        None,
        /**
         * warning(s) occured - the choreography is enforceable only with some restrictions
         */
        Warning,
        /**
         * error(s) occured - the choreography is not enforceable
         */
        Error;

        public static ErrorLevel levelFor(Collection<EnforceabilityProblem> problems) {
            if (problems.isEmpty()) {
                return None;
            }
            for (EnforceabilityProblem problem : problems) {
                if (problem.isError()) {
                    return Error;
                }
            }
            return Warning;
        }
    }

    /**
     *
     * @param workbench the workbench, on which to operate
     */
    public EnforceabilityPlugin(Workbench workbench) {
        this(workbench, true);
    }

    public EnforceabilityPlugin(Workbench workbench, boolean addContextMenu) {
        super(workbench, addContextMenu);
    }

    @Override
    public List<Component> getMenuEntries() {
        List<Component> entries = new LinkedList<Component>();
        JMenuItem startCheck = new JMenuItem("Check Enforceability");
        startCheck.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        startCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                performCheck(true);
            }
        });
        registerComponent(startCheck);
        entries.add(startCheck);

        JMenuItem removeMarkers = new JMenuItem("Remove Markers");
        removeMarkers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        removeMarkers.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeMarkers();
            }
        });
        entries.add(removeMarkers);
        registerComponent(removeMarkers);
        return entries;
    }

    private void recordAddedNode(BPMNModel model, ProcessNode node) {
        if (!modifications.containsKey(model.getId())) {
            modifications.put(model.getId(),
                    new HashMap<ProcessObject, Collection<ProcessObject>>());
        }
        modifications.get(model.getId()).put(node, new HashSet<ProcessObject>());
    }

    /**Remove failure markers from the model*/
    public void removeMarkers() {
        final int animationTime = 500;
        BPMNModel model = (BPMNModel) workbench.getSelectedModel();
        Collection<ProcessObject> toBeRemoved = new HashSet<ProcessObject>();
        if (modifications.containsKey(model.getId())) {
            for (Map.Entry<ProcessObject, Collection<ProcessObject>> entry :
                    modifications.get(model.getId()).entrySet()) {
                toBeRemoved.add(entry.getKey());
                for (ProcessObject relatedObject : entry.getValue()) {
                    relatedObject.setHighlighted(false);
                    for (Map.Entry<String, String> property :
                            modifiedProperties.get(relatedObject.getId()).entrySet()) {
                        relatedObject.setProperty(property.getKey(), property.getValue());
                    }
                }
            }
            modifications.remove(model.getId());
        }
        for (ProcessObject removee : toBeRemoved) {
            workbench.getSelectedProcessEditor().getAnimator().
                    removeProcessObject(removee, animationTime);
        }
        if (modelListeners.containsKey(model.getId())) {
            model.removeListener(modelListeners.get(model.getId()));
        }
        workbench.getSelectedProcessEditor().removeMouseListener(mouseListener);
    }

    /**
     * performs the enforcability check
     * @return true, if the selected BPMN diagram is a valid and enforcable choreography diagram
     */
    public ErrorLevel performCheck(boolean showResult) throws HeadlessException {
        if (!(workbench.getSelectedModel() instanceof BPMNModel)) {
            showOnlyBPMNMessage();
            return ErrorLevel.Error;
        }
        CombinedEnforceabilityCheck checker =
                new CombinedEnforceabilityCheck(
                (BPMNModel) workbench.getSelectedModel());
        long time = System.currentTimeMillis();
        Collection<EnforceabilityProblem> problems = checker.checkModel();
        time = System.currentTimeMillis() - time;
        System.out.println("Checking enforceability took " + time + " ms.");
        return evaluate(problems, showResult);
    }

    /**
     * displays the result of the check (marks problems and, if desired, show a
     * message box stating the result).
     * @param problems the collection of EnforceabilityProblems, that resulted from
     * the check
     * @param showResult true, if a MessageBox should be displayed, that states the
     * result of the check
     * @return the errorLevel, that resulted from analyzing the problems
     */
    private ErrorLevel evaluate(
            Collection<EnforceabilityProblem> problems, boolean showResult) {
        ErrorLevel level = ErrorLevel.levelFor(problems);
        if (showResult) {
            showFeedback(level);
        }
        if (level.equals(ErrorLevel.Error) || level.equals(ErrorLevel.Warning)) {
            markProblems(problems);
        }
        return level;
    }

    /**
     * shows a messagebox stating the result of the check
     * @param level the check´s result
     */
    private void showFeedback(ErrorLevel level) {
        if (level.equals(ErrorLevel.None)) {
            showPositiveFeedback();
        } else if (level.equals(ErrorLevel.Warning)) {
            showWarning();
        } else {
            showNegativeFeedback();
        }
    }

    /**
     * Notifies the user about the fact, that only BPMN diagrams can be checked
     */
    private void showOnlyBPMNMessage() throws HeadlessException {
        JOptionPane.showMessageDialog(
                workbench,
                "Only applicable for BPMN-diagrams",
                "Error",
                JOptionPane.OK_OPTION);
    }

    /**
     * Notifies the user, that the diagram is _not_ a valid and enforceable
     * choreography diagram
     */
    private void showNegativeFeedback() throws HeadlessException {
        JOptionPane.showMessageDialog(
                workbench,
                "This choreography is NOT enforceable",
                "Enforcability-cecking-result",
                JOptionPane.OK_OPTION,
                UIManager.getIcon("OptionPane.errorIcon"));
    }

    /**
     * Notifies t
     */
    private void showWarning() {
        JOptionPane.showMessageDialog(
                workbench,
                "This choreography is enforceable with RESTRICTIONS",
                "Enforcability-cecking-result",
                JOptionPane.OK_OPTION,
                UIManager.getIcon("OptionPane.warningIcon"));
    }

    /**
     * Notifies the user, that the diagram _is_ valid and enforceable
     * choreography diagram
     */
    private void showPositiveFeedback() throws HeadlessException {
        JOptionPane.showMessageDialog(
                workbench,
                "This choreography is enforceable",
                "Enforcability-cecking-result",
                JOptionPane.OK_OPTION,
                UIManager.getIcon("OptionPane.informationIcon"));
    }

    /**
     * Marks problems in the diagram by adding text annotations and highlighting
     * related objects
     * @param problems a list of problems to be marked
     */
    private void markProblems(Collection<EnforceabilityProblem> problems) {
        if (!problems.isEmpty()) {
            ProcessModelListener modelListener = getModelListener();
            modelListeners.put(workbench.getSelectedModel().getId(), modelListener);
            workbench.getSelectedModel().addListener(modelListener);
            workbench.getSelectedProcessEditor().addMouseListener(mouseListener);
        }
        for (EnforceabilityProblem problem : problems) {
            ProcessNode annotation =
                    showProblemAt(problem.getDescription(), problem.getMainObject());
            for (ProcessObject additional : problem.getRelatedObjects()) {
                highlight(additional, annotation);
            }
        }
    }

    private ProcessModelListener getModelListener() {
        return new ProcessModelListener() {

            private BPMNModel model = (BPMNModel) workbench.getSelectedModel();

            @Override
            public void processNodeAdded(ProcessNode newNode) {
            }

            public void processNodeRemoved(ProcessNode remNode) {
                nodeRemoved(remNode, model);
            }

            @Override
            public void processEdgeAdded(ProcessEdge edge) {
            }

            @Override
            public void processEdgeRemoved(ProcessEdge edge) {
            }

            @Override
            public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
                // ignore
            }
        };
    }

    /**
     * highlights an object and registers the object as altered
     * @param object the object, that should be highlighted
     * @param problemMainObject the main-object of the problem (needed for registration)
     */
    private void highlight(ProcessObject object, ProcessObject problemMainObject) {
        BPMNModel model = (BPMNModel) workbench.getSelectedModel();
        object.setHighlighted(true);
        if (!modifications.containsKey(model.getId())) {
            modifications.put(model.getId(),
                    new HashMap<ProcessObject, Collection<ProcessObject>>());
        }
        if (!modifications.get(model.getId()).containsKey(problemMainObject)) {
            modifications.get(model.getId()).put(problemMainObject,
                    new HashSet<ProcessObject>());
        }
        modifications.get(model.getId()).get(problemMainObject).add(object);
        if (!modifiedProperties.containsKey(object.getId())) {
            modifiedProperties.put(object.getId(), new HashMap<String, String>());
        }
    }

    /**
     * Adds an annotation to an object right beneath the object stating a given text
     * @param errorText the text to be displayed in the annotation
     * @param object the object, which the annotation should be added to
     */
    private TextAnnotation showProblemAt(String errorText, ProcessObject object) {
        final int width = 190, height = 80;
        TextAnnotation annotation = new TextAnnotation();
        annotation.setText(errorText);
        annotation.setSize(width, height);
        ProcessNode dockingNode;
        if (object instanceof ProcessNode) {
            dockingNode = (ProcessNode) object;
        } else if (object instanceof ProcessEdge) {
            dockingNode = new EdgeDocker((ProcessEdge) object);
            workbench.getSelectedModel().addNode(dockingNode);
        } else {
            return null;
        }
        annotation.setPos(dockingNode.getPos().x,
                dockingNode.getPos().y
                + dockingNode.getSize().height / 2
                + annotation.getSize().height);
        annotation.setShadowEnabled(true);
        addAnnotationTo(annotation, dockingNode);
        highlight(object, annotation);
        return annotation;
    }

    private void addAnnotationTo(TextAnnotation annotation, ProcessNode dockingNode) {
        final int animTime = 1000, animDelay = 0;
        Association association = new Association(dockingNode, annotation);
        BPMNModel model = (BPMNModel) workbench.getSelectedModel();
        association.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
        workbench.getSelectedProcessEditor().getAnimator().
                addProcessNode(annotation, animTime, animDelay, Type.TYPE_FADE_IN);
        recordAddedNode(model, annotation);
        workbench.getSelectedProcessEditor().getAnimator().
                addProcessObject(association, animTime);
    }

    /**
     * highlights the objects belonging to a problem, if the TextAnnotation
     * stating this problem is selected
     */
    private void updateHighlighting() {
        if (!modifications.containsKey(workbench.getSelectedModel().getId())) {
            return;
        }
        resetAlteredObjectProperties();
        doHighlighting();
    }

    private void resetAlteredObjectProperties() {
        for (ProcessObject object : workbench.getSelectedModel().getObjects()) {
            if (!object.isSelected() && modifiedProperties.containsKey(object.getId())) {
                for (Map.Entry<String, String> properties :
                        modifiedProperties.get(object.getId()).entrySet()) {
                    object.setProperty(properties.getKey(), properties.getValue());
                }
                modifiedProperties.get(object.getId()).clear();
            }
        }
    }

    private void doHighlighting() {
        for (Map.Entry<ProcessObject, Collection<ProcessObject>> annotation :
                modifications.get(workbench.getSelectedModel().getId()).entrySet()) {
            for (ProcessObject related : annotation.getValue()) {
                related.setHighlighted(true);
                if (annotation.getKey().isSelected()) {
                    highlightSpecial(related, modifiedProperties.get(related.getId()));
                }
            }
        }
    }

    /**
     * highlights a ProcessObject by changing its color and records the change
     * @param processObject the object, which´s color should be changed
     * @param properties the map, where to record the change
     */
    private void highlightSpecial(ProcessObject processObject,
            Map<String, String> properties) {
        final Color highlightColor = new Color(255, 227, 102);
        processObject.setHighlighted(true);
        if (Utils.isNode(processObject)) {
            if (!properties.containsKey(ProcessNode.PROP_BACKGROUND)) {
                properties.put(ProcessNode.PROP_BACKGROUND,
                        processObject.getProperty(ProcessNode.PROP_BACKGROUND));
                ((ProcessNode) processObject).setBackground(highlightColor);
            }
        } else if (Utils.isEdge(processObject)) {
            if (!properties.containsKey(ProcessEdge.PROP_COLOR_ARC)) {
                properties.put(ProcessEdge.PROP_COLOR_ARC,
                        processObject.getProperty(ProcessEdge.PROP_COLOR_ARC));
                ((ProcessEdge) processObject).setColor(highlightColor);
                processObject.setHighlighted(false);
            }
        }
    }

    /**
     * checks, wheter the removedNode was an anotation added by this plugin and,
     * if so, unhighlights all of its related objects. At the end, it updates
     * the highlighting of all nodes, that are still referenced.
     */
    private void nodeRemoved(ProcessNode removedNode, BPMNModel fromModel) {
        if (modifications.containsKey(fromModel.getId())) {
            if (modifications.get(fromModel.getId()).containsKey(removedNode)) {
                for (ProcessObject object :
                        modifications.get(fromModel.getId()).get(removedNode)) {
                    object.setHighlighted(false);
                }
                modifications.get(fromModel.getId()).remove(removedNode);
                updateHighlighting();
            }
        }
    }
}
