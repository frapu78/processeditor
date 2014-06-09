/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.validationPlugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.domainModel.DomainModel;

import com.inubit.research.gui.Workbench;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import com.inubit.research.validation.ModelValidator;
import com.inubit.research.validation.Validator;
import com.inubit.research.validation.bpmn.BPMNValidator;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.soundness.ReachabilityGraph;
import com.inubit.research.validation.bpmn.soundness.StateSpaceException;
import com.inubit.research.validation.domainModel.DomainModelValidator;

/**
 *
 * @author tmi
 */
public class ValidationPlugin extends WorkbenchPlugin {

    public static Map<Class<? extends ProcessModel>, ModelValidator> supportedModels = new HashMap<Class<? extends ProcessModel>, ModelValidator>();
    private static ValidationPlugin instance;
    
    static {
        addSupportedModel(DomainModel.class, DomainModelValidator.getInstance());
        addSupportedModel(BPMNModel.class, BPMNValidator.getInstance());
    }
    
    public ValidationPlugin(Workbench workbench) {
        super(workbench);
    }
    
    @Override
    public Component getMenuEntry() {
        JMenuItem validateButton = new JMenuItem("Validate");
        validateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                performCheck(workbench.getSelectedModel());
            }
        });

        return validateButton;

        /*JMenuItem reachabilityGraphButton = new JMenuItem("Reachability graph");
        reachabilityGraphButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showReachabilityGraph();
            }
        });

        JMenu subMenu = new JMenu("Validation");
        subMenu.add(validateButton);
        subMenu.add(reachabilityGraphButton);
        return subMenu;*/
    }

    private void performCheck(ProcessModel model) {
        Validator validator = new Validator(model, supportedModels);
        if(validator.getAllMessages().isEmpty()) {
            JOptionPane.showMessageDialog(workbench, "No problems found",
                    "Validation successfull", JOptionPane.OK_OPTION,
                    UIManager.getIcon("OptionPane.informationIcon"));
        } else {
            CheckingResultDialog dialog = new CheckingResultDialog(workbench, validator);
            dialog.setVisible(true);
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void showReachabilityGraph() {
        try {
            ReachabilityGraph graph = new ReachabilityGraph(
                    new ModelAdaptor((BPMNModel) workbench.getSelectedModel()),
                    getStartEvent(), getEndEvent(), new LinkedList<NodeAdaptor>());
            System.out.println(graph.toString());
            workbench.addModel("reachability-graph", graph.toModel());
        } catch (StateSpaceException ex) {
            ex.printStackTrace();
        }
    }

    private NodeAdaptor getStartEvent() {
        for (ProcessNode node : workbench.getSelectedModel().getNodes()) {
            if (/*workbench.getSelectedModel().
                    getIncomingEdges(SequenceFlow.class, node).isEmpty()*/
                    node instanceof StartEvent) {
                return NodeAdaptor.adapt(node,
                        new ModelAdaptor((BPMNModel)workbench.getSelectedModel()));
            }
        }
        return null;
    }

    private NodeAdaptor getEndEvent() {
        for (ProcessNode node : workbench.getSelectedModel().getNodes()) {
            if (workbench.getSelectedModel().
                    getOutgoingEdges(SequenceFlow.class, node).isEmpty()) {
                return NodeAdaptor.adapt(node,
                        new ModelAdaptor((BPMNModel)workbench.getSelectedModel()));
            }
        }
        return null;
    }
    
    
    private static void addSupportedModel(Class<? extends ProcessModel> cls, ModelValidator val) {
        supportedModels.put(cls, val);
    }
    
    public Map<Class<? extends ProcessModel>, ModelValidator> getSupportedModels() {
        return supportedModels;
    }
    
    public static ValidationPlugin getInstance(Workbench workbench){
        if(instance == null)
            instance = new ValidationPlugin(workbench);
        return instance;
    }

}
