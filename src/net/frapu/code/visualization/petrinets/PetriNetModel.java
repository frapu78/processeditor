/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 * 
 * This class provides a model for Workflow nets used within the ProcessEditor.
 * 
 * @author frank
 */
public class PetriNetModel extends ProcessModel {
    
    /**
     * Creates a new WorkflowNetModel.
     */
    public PetriNetModel() {
        super();
        processUtils = new PetriNetUtils();
    }
    
    /** 
     * Creates a new WorkflowNetModel with a name.
     * @param name
     */
    public PetriNetModel(String name) {
        super(name);
        processUtils = new PetriNetUtils();
    }

    public String getDescription() {
       return "Petri net";
    }
    
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();        
        result.add(Transition.class);        
        result.add(Place.class);
        result.add(SubNet.class);
        result.add(ResourcePlace.class);
        result.add(LaborPlace.class);
        result.add(Lane.class);
        result.add(Comment.class);
        result.add(Clock.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(Edge.class);
        result.add(ResetEdge.class);
        result.add(InhibitorEdge.class);
        result.add(CommentEdge.class);
        return result;
    }
    
}
