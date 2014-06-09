/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ActivityAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import net.frapu.code.visualization.bpmn.Association;
import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;

/**
 *
 * @author tmi
 */
public class AssociationValidator extends EdgeValidator {

    public AssociationValidator(EdgeAdaptor edge, ModelAdaptor model,
            BPMNValidator validator) {
        super(edge, model, validator);
    }

    @Override
    public void doValidation() {
        super.doValidation();
        checkClusterCrossingRules();
        checkSourceAndTargetAreAllowed();
        checkDirection();
    }

    private void checkClusterCrossingRules() {
        if( ! (edge.getSource().isTextAnnotation() ||
                    edge.getTarget().isTextAnnotation() ||
                    edge.getSource().isEdgeDocker() ||
                    edge.getTarget().isEdgeDocker()) &&
                ! model.getPoolForNode(edge.getSource()).equals(
                    model.getPoolForNode(edge.getTarget()))) {
            validator.addMessage("associationCrossingPool", edge);
        }
    }

    private void checkSourceAndTargetAreAllowed() {
        if (! isAllowedSourceTargetCombination()) {
            validator.addMessage("illegalAssociationSourceTargetCombination", edge);
        }
    }

    private boolean isAllowedSourceTargetCombination() {
        NodeAdaptor source, target;
        if(edge.getProperty(Association.PROP_DIRECTION).
                equals(Association.DIRECTION_SOURCE)) {
            source = edge.getTarget();
            target = edge.getSource();
        } else {
            source = edge.getSource();
            target = edge.getTarget();
        }
        return isAllowedFromTo(source, target);
    }

    private boolean isAllowedFromTo(NodeAdaptor source, NodeAdaptor target) {
        if (source.isTextAnnotation()) return ! target.isTextAnnotation();
        if (target.isTextAnnotation()) return ! source.isTextAnnotation();
        if (source.isData()) return target.isActivity() || target.isEdgeDocker();
        if (source.isEdgeDocker()) {
            return target.isData() ||
                    target.isMessage() ||
                    target.isChoreographyActivity();
        }
        if (target.isData()) return source.isActivity();
        if (target.isEdgeDocker()) {
            return source.isData() ||
                    source.isMessage() ||
                    source.isChoreographyActivity();
        }
        if (source.isMessage()) {
            return (target.isMessageEvent() &&
                        (target.isStartEvent() ||
                            ((EventAdaptor)target).isCatching())) ||
                    target.isReceiveTask() ||
                    target.isServiceTask() ||
                    target.isChoreographyActivity();
        }
        if (target.isMessage()) {
            return (source.isMessageEvent() &&
                    (source.isStartEvent() || ((EventAdaptor)source).isCatching())) ||
                    source.isSendTask() ||
                    source.isServiceTask() ||
                    source.isChoreographyActivity();
        }
        if (source.isEvent() &&
                ((EventAdaptor)source).isCompensationIntermediateEvent()) {
            return target.isActivity() &&
                    ((ActivityAdaptor)target).isCompensationTask();
        }
        return false;
    }

    private void checkDirection() {
        if((edge.getSource().isData() || edge.getTarget().isData()) &&
                (edge.getSource().isActivity() || edge.getTarget().isActivity())) {
            if (edge.getProperty(Association.PROP_DIRECTION).
                        equals(Association.DIRECTION_BOTH) ||
                    edge.getProperty(Association.PROP_DIRECTION).
                        equals(Association.DIRECTION_NONE)) {
                validator.addMessage(
                        "associationShouldBeDirected", edge);
            }
        }
    }

    
}
