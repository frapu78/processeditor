/**
 *
 * Process Editor - Use Case Package
 *
 * (C) 2015 the authors
 *
 * http://frapu.de
 *
 */
package net.frapu.code.visualization.usecase;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import java.util.LinkedList;
import java.util.List;

/**
 * This class provides support for UML Use Case Diagrams.
 *
 * @author fpu
 */
public class UseCaseModel extends ProcessModel {

    public UseCaseModel() {
        processUtils = new UseCaseUtils();
    }

    @Override
    public String getDescription() {
        return "Use Case (UML)";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(Actor.class);
        result.add(UseCase.class);
        result.add(SystemBoundary.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        /**
         * The edge are in order of their "strength" as defined by the spec
         */
        result.add(Association.class);
        result.add(Dependency.class);
        result.add(Inheritance.class);
        return result;
    }

}
