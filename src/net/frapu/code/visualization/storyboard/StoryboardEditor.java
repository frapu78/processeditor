/**
 *
 * Process Editor - Storyboard Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.storyboard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ExtendedProcessEditorListener;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.SwingUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.uml.UMLUtils;
import net.frapu.code.visualization.xforms.XFormsWizard;

/**
 *
 * @author fpu
 */
public class StoryboardEditor extends ProcessEditor {

    /**
     *
     */
    private static final long serialVersionUID = -7845383456980527855L;

    public StoryboardEditor() {
    }

    public StoryboardEditor(ProcessModel model) {
        super(model);
        init();
    }

    private void init() {
        System.out.println("Storyboard Editor additions loaded.");
        addGenerateBPMNMenu();
        addImplementFormMenu();
    }

    private void addGenerateBPMNMenu() {
        final StoryboardEditor outer = this;

        JMenuItem item = new JMenuItem("Generate BPMN model...");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Convert current model to BPMNModel
                BPMNModel bpmn = Storyboard2BPMNGenerator.convertToBPMN((StoryboardModel) getModel());
                Set<ProcessEditorListener> listener = getListeners();
                try {
                    startProcessEditorListenerUpdate();
                    for (ProcessEditorListener l : listener) {
                        if (l instanceof ExtendedProcessEditorListener) {
                            ExtendedProcessEditorListener el = (ExtendedProcessEditorListener) l;
                            el.requestNewProcessEditor(bpmn);
                        }
                    }
                    endProcessEditorListenerUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        addCustomPopUpMenuItem(item);

        JMenuItem item2 = new JMenuItem("Reduce Story");
        item2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Storyboard2BPMNGenerator.reduceStory((StoryboardModel) getModel());
                outer.repaint();
            }
        });
        addCustomPopUpMenuItem(item2);

    }

    private void addImplementFormMenu() {
        JMenuItem item = new JMenuItem("Design form...");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Detect first Business Object
                try {
                    ProcessModel bo = null;
                    Cluster cluster = null;
                    for (Cluster c : getModel().getClusters()) {
                        if (c.isContained(getSelectionHandler().getLastSelectedNode())) {
                            cluster = c;
                        }
                    }
                    if (cluster == null) {
                        throw new Exception("No surrounding Scene found!");
                    }

                    for (ProcessNode n : getModel().getNeighbourNodes(Association.class, cluster)) {
                        if (n instanceof BusinessObject) {
                            String schemaUri = n.getProperty(BusinessObject.PROP_ELEMENT_REF);
                            if (schemaUri == null) {
                                continue;
                            }
                            if (schemaUri.isEmpty()) {
                                continue;
                            }
                            String schemaLocation = schemaUri.substring(0, schemaUri.indexOf("#"));
                            String schemaElement = schemaUri.substring(schemaUri.indexOf("#") + 1);
                            // Fetch model
                            ProcessModel model = UMLUtils.fetchSchemaModel(schemaLocation);
                            // Find root node by name matching (first occurrence)
                            ProcessNode rootNode = null;
                            for (ProcessNode n2 : model.getNodes()) {
                                if (n2.getText().equals(schemaElement)) {
                                    rootNode = n2;
                                    break;
                                }
                            }
                            if (rootNode == null) {
                                throw new Exception("Referenced top level Schema Element not found!");
                            }
                            // Detect actions
                            List<String> actions = new LinkedList<String>();
                            for (ProcessEdge edge : getModel().getOutgoingEdges(Sequence.class, getSelectionHandler().getLastSelectedNode())) {
                                if (!edge.getLabel().isEmpty()) {
                                    actions.add(edge.getLabel());
                                }
                            }
                            // Show Wizard
                            XFormsWizard wizard = new XFormsWizard(null, true);
                            SwingUtils.center(wizard);
                            wizard.setData(getSelectionHandler().getLastSelectedNode().getText(), model, rootNode, actions);
                            wizard.setVisible(true);
                            // Capture results
                            ProcessModel result = wizard.getResult();
                            if (result != null) {
                                result.setProcessName("Form for "+getSelectionHandler().getLastSelectedNode().getText());
                                startProcessEditorListenerUpdate();
                                for (ProcessEditorListener l : getListeners()) {
                                    if (l instanceof ExtendedProcessEditorListener) {
                                        ExtendedProcessEditorListener el = (ExtendedProcessEditorListener) l;
                                        el.requestNewProcessEditor(result);
                                    }
                                }
                                endProcessEditorListenerUpdate();
                            }
                            return; // Only consider first BO for now...
                        }
                    }
                    throw new Exception("No BusinessObject with Schema reference attached to Scene!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        addCustomContextMenuItem(Action.class, item);
    }
}
