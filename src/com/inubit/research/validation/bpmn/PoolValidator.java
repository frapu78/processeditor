/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.LinkedList;
import com.inubit.research.validation.bpmn.adaptors.LaneableClusterAdaptor;
import java.util.List;

/**
 *
 * @author tmi
 */
class PoolValidator {

    private LaneableClusterAdaptor pool;
    private ModelAdaptor model;
    private BPMNValidator validator;

    public PoolValidator(LaneableClusterAdaptor pool, ModelAdaptor model,
            BPMNValidator validator) {
        this.pool = pool;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        List<LaneableClusterAdaptor> lanes = pool.getLanes();
        if(lanes.isEmpty()) return;
        List<NodeAdaptor> containedNodes = pool.getProcessNodes();
        if (containedNodes.size() != lanes.size() ||
                !(containedNodes.containsAll(lanes))) {
            List<ProcessObjectAdaptor> related =
                    new LinkedList<ProcessObjectAdaptor>(pool.getLanes());
            related.addAll(pool.getProcessNodes());
            validator.addMessage("poolContainingLanesAndOtherNodes",pool, related);
        }
    }
}
