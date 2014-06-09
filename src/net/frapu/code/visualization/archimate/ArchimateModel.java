/**
 *
 * Process Editor - Archimate Package
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization.archimate;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

/**
 * Implements a Process Model for the Archimate specification.
 *
 * @see http://www.opengroup.org/archimate
 * @author fpu
 */
public class ArchimateModel extends ProcessModel {

    public ArchimateModel() {
        processUtils = new ArchimateUtils();
    }


    @Override
    public String getDescription() {
        return "ArchiMate";
    }

    @Override
    public List<Class<? extends ProcessNode>> getSupportedNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(BusinessActor.class);
        result.add(BusinessRole.class);
        result.add(BusinessProcess.class);
        result.add(BusinessService.class);
        result.add(Group.class);
        return result;
    }

    @Override
    public List<Class<? extends ProcessEdge>> getSupportedEdgeClasses() {
        List<Class<? extends ProcessEdge>> result = new LinkedList<Class<? extends ProcessEdge>>();
        /**
         * The edge are in order of their "strength" as defined by the spec
         */
        result.add(Association.class);
        result.add(UsedBy.class);
        result.add(Realization.class);
        result.add(Assignment.class);
        return result;
    }

}
