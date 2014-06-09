/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.ConversationLink;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.SequenceFlow;

/**
 * Adapts a BPMN-Edge for the specific needs of Validation.
 * @author tmi
 */
public class EdgeAdaptor extends AbstractAdaptor implements ProcessObjectAdaptor {

    private ProcessEdge adaptee;
    private ModelAdaptor model;

    public EdgeAdaptor(ProcessEdge adaptee, ModelAdaptor model) {
        this.adaptee = adaptee;
        this.model = model;
    }

    @Override
    public boolean isEdge() {
        return true;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public ProcessEdge getAdaptee() {
        return adaptee;
    }

    public boolean isSequenceFlow() {
        return adaptee instanceof SequenceFlow;
    }

    public boolean isMessageFlow() {
        return adaptee instanceof MessageFlow;
    }

    public boolean isAssociation() {
        return adaptee instanceof Association;
    }

    public boolean isConversationLink() {
        return adaptee instanceof ConversationLink;
    }

    public boolean isAllowedInBPD() {
        return isSequenceFlow() ||
                isMessageFlow() ||
                isAssociation() ||
                isConversationLink();
    }

    public NodeAdaptor getSource() {
        return NodeAdaptor.adapt(adaptee.getSource(), model);
    }

    public NodeAdaptor getTarget() {
        return NodeAdaptor.adapt(adaptee.getTarget(), model);
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || ! (otherObject instanceof EdgeAdaptor)) {
            return false;
        }
        if (adaptee == null) {
            return ((EdgeAdaptor) otherObject).getAdaptee() == null;
        }
        return  adaptee.equals(((EdgeAdaptor) otherObject).getAdaptee());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.adaptee != null ? this.adaptee.hashCode() : 0);
        return hash;
    }

    @Override
    public String getProperty(String key) {
        return adaptee.getProperty(key);
    }

    public String getLabel() {
        return adaptee.getLabel();
    }

    public boolean isConditionalSequenceFlow() {
        return isSequenceFlow() &&
                getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                    equals(SequenceFlow.TYPE_CONDITIONAL);
    }

    public boolean isDefaultSequenceFlow() {
        return isSequenceFlow() &&
                getProperty(SequenceFlow.PROP_SEQUENCETYPE).
                    equals(SequenceFlow.TYPE_DEFAULT);
    }

    public boolean isStandardSequenceFlow() {
        return isSequenceFlow() &&
                ! (isDefaultSequenceFlow() || isConditionalSequenceFlow());
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }
}
