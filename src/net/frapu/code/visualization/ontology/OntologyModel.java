/**
 *
 * Process Editor - Ontology Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.ontology;

import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author frank
 */
public class OntologyModel extends ProcessModel {

    public OntologyModel() {
        this(null);
    }

    public OntologyModel(String name) {
        super(name);
        processUtils = new OntologyUtils();
    }

   public String getDescription() {
       return "Ontology diagram";
   }
   
	@Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Concept.class);
        result.add(Individual.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        result.add(ContainmentEdge.class);
        result.add(SubClassEdge.class);
        result.add(EquivalentClassEdge.class);
        return result;
    }

    @Override
    public String toString() {
        return this.getProcessName()+" (Ontology Diagram)";
    }

}
