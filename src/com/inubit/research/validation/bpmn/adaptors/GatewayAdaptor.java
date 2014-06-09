/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.ComplexGateway;
import net.frapu.code.visualization.bpmn.EventBasedGateway;
import net.frapu.code.visualization.bpmn.ExclusiveGateway;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.InclusiveGateway;
import net.frapu.code.visualization.bpmn.ParallelGateway;

/**
 *
 * @author tmi
 */
public class GatewayAdaptor extends NodeAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof Gateway;
    }

    protected GatewayAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public GatewayAdaptor(Gateway adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return GatewayAdaptor.canAdapt(node);
    }

    @Override
    public Gateway getAdaptee() {
        return (Gateway) super.getAdaptee();
    }

    @Override
    public boolean isGateway() {
        return true;
    }

    @Override
    public boolean isDecisionGateway() {
        return isPlainGateway() || isExclusiveGateway() || isInclusiveGateway();
    }

    public boolean isPlainGateway() {
        return getAdaptee().getClass().equals(Gateway.class);
    }

    public boolean isExclusiveGateway() {
        return getAdaptee() instanceof ExclusiveGateway;
    }

    public boolean isInclusiveGateway() {
        return getAdaptee() instanceof InclusiveGateway;
    }

    @Override
    public boolean isParallelGateway() {
        return getAdaptee() instanceof ParallelGateway;
    }

    public boolean isComplexGateway() {
        return getAdaptee() instanceof ComplexGateway;
    }

    @Override
    public boolean isEventBasedGateway() {
        return getAdaptee() instanceof EventBasedGateway;
    }

    @Override
    public boolean isInstantiatingGateway() {
        return isEventBasedGateway() &&
                !getProperty(EventBasedGateway.PROP_INSTANTIATE).
                equals(EventBasedGateway.TYPE_INSTANTIATE_NONE);
    }

    @Override
    public boolean isExclusiveInstantiatingGateway() {
        return isInstantiatingGateway() &&
                getProperty(EventBasedGateway.PROP_INSTANTIATE).
                equals(EventBasedGateway.TYPE_INSTANTIATE_EXCLUSIVE);
    }

    @Override
    public boolean shouldHaveIncommingSequenceFlow() {
        return ! isInstantiatingGateway();
    }

    @Override
    public boolean shouldHaveOutgoingSequenceFlow() {
        return true;
    }

    @Override
    public boolean mayHavIncommingSequenceFlow() {
        return ! isInstantiatingGateway();
    }

    @Override
    public boolean mayHaveOutgoingSequenceFlow() {
        return true;
    }

    @Override
    public boolean isAllowedInChoreography() {
        return true;
    }

    @Override
    public boolean isExclusiveJoin() {
        return isExclusiveGateway() || isEventBasedGateway() || isPlainGateway();
    }
}
