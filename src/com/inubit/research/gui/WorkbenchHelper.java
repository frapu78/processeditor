/**
 *
 * Process Editor - inubit Workbench Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 * 
 */
package com.inubit.research.gui;

import java.util.LinkedList;
import java.util.List;

import com.inubit.research.ontologyEditor.OntologyEditor;
import java.util.HashMap;
import java.util.Map;

import net.frapu.code.simulation.petrinets.PetriNetSimulationEditor;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.archimate.ArchimateModel;
import net.frapu.code.visualization.bpmn.BPMNEditor;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.domainModel.DomainModel;
import net.frapu.code.visualization.epk.EPKModel;
import net.frapu.code.visualization.gantt.GanttModel;
import net.frapu.code.visualization.lifecycle.LifecycleModel;
import net.frapu.code.visualization.ontology.OntologyModel;
import net.frapu.code.visualization.orgChart.OrgChartModel;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.processmap.ProcessMapModel;
import net.frapu.code.visualization.reporting.ReportingModel;
import net.frapu.code.visualization.storyboard.StoryboardEditor;
import net.frapu.code.visualization.storyboard.StoryboardModel;
import net.frapu.code.visualization.uml.ClassModel;
import net.frapu.code.visualization.twf.TWFModel;
import net.frapu.code.visualization.xforms.XFormsModel;

/**
 *
 * @author fpu
 */
public class WorkbenchHelper {

    /** List of supported ProcessModels */
    private static List<Class<? extends ProcessModel>> modelList = new LinkedList<Class<? extends ProcessModel>>();
    /** List of specific Editors */
    private static Map<Class<? extends ProcessModel>, Class<? extends ProcessEditor>> editorMap = 
            new HashMap<Class<? extends ProcessModel>, Class<? extends ProcessEditor>>();

    /**
     * Initialize list of supported ProcessModels.
     */
    static {
        modelList.add(ArchimateModel.class);
        modelList.add(BPMNModel.class);
        modelList.add(ClassModel.class);
        modelList.add(DomainModel.class);
        modelList.add(EPKModel.class);
        modelList.add(GanttModel.class);
        modelList.add(LifecycleModel.class);
        modelList.add(OntologyModel.class);
        modelList.add(OrgChartModel.class);
        modelList.add(PetriNetModel.class);
        modelList.add(ProcessMapModel.class);
        modelList.add(ReportingModel.class);
        modelList.add(StoryboardModel.class);
        modelList.add(TWFModel.class);
        modelList.add(XFormsModel.class);

        editorMap.put(BPMNModel.class, BPMNEditor.class);
        editorMap.put(PetriNetModel.class, PetriNetSimulationEditor.class);
        editorMap.put(OntologyModel.class, OntologyEditor.class);
        editorMap.put(StoryboardModel.class, StoryboardEditor.class);
    }

    /**
     * Adds a new supported ProcessModel.
     * @param cl
     */
    public static void addSupportedProcessModel(Class<? extends ProcessModel> cl) {
        modelList.add(cl);
    }
    
    /**
     * Adds a new supported ProcessModel together with a corresponding editor.
     * @param cl
     */
    public static void addSupportedProcessModel(Class<? extends ProcessModel> cl, 
            Class<? extends ProcessEditor> editor) {
        modelList.add(cl);
        editorMap.put(cl, editor);
    }    

    /**
     * Removes as supported ProcessModels.
    */
    public static void removeSupportedProcessModel(Class<? extends ProcessModel> cl) {
        modelList.remove(cl);
    }
    
    /**
     * Clears all supported ProcessModels.
     */
    public static void removeAllProcessModels() {
        modelList.clear();
    }

    /**
     * Returns all supported model types.<br>
     * New types need to be declared here!
     * @return
     */
    public static List<Class<? extends ProcessModel>> getSupportedProcessModels() {
        return modelList;
    }

    /**
     * Returns an appropriate editor for a certain model.
     * @param m
     * @return
     */
    public static ProcessEditor getEditor(ProcessModel m) {
//        if (m instanceof BPMNModel) return new BPMNEditor(m);
//        if (m instanceof PetriNetModel) return new PetriNetSimulationEditor(m);
//        if (m instanceof OntologyModel) return new OntologyEditor((OntologyModel) m);
//        if (m instanceof StoryboardModel) return new StoryboardEditor(m);
        if (editorMap.containsKey(m.getClass())) {
            // Try to instantiate editor
            try {
                Object o = editorMap.get(m.getClass()).getConstructor(ProcessModel.class).newInstance(m);
                ProcessEditor editor = (ProcessEditor)o;
                return editor;
            } catch (Exception e) {
                e.printStackTrace();
            }          
        }       
        // else
        return new ProcessEditor(m);
    }

}
