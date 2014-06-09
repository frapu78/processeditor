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
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.CallActivity;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * @author tmi
 */
public class ActivityAdaptor extends NodeAdaptor {
    
    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof Activity;
    }

    protected ActivityAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public ActivityAdaptor(Activity adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return ActivityAdaptor.canAdapt(node);
    }

    @Override
    public Activity getAdaptee() {
        return (Activity) super.getAdaptee();
    }

    @Override
    public boolean isActivity() {
        return true;
    }

    @Override
    public boolean isTask() {
        return getAdaptee() instanceof Task;
    }

    public boolean isCompensationTask() {
        return isTask() &&
                getProperty(Task.PROP_COMPENSATION).equals(Task.TRUE);
    }

    @Override
    public boolean isReceiveTask() {
        return isTask() && getProperty(Task.PROP_STEREOTYPE).
                equals(Task.TYPE_RECEIVE);
    }

    @Override
    public boolean isSendTask() {
        return isTask() && getProperty(Task.PROP_STEREOTYPE).
                equals(Task.TYPE_SEND);
    }

    @Override
    public boolean isServiceTask() {
        return isTask() && getProperty(Task.PROP_STEREOTYPE).
                equals(Task.TYPE_SERVICE);
    }

    public boolean isCallActivity() {
        return getAdaptee() instanceof CallActivity;
    }

    @Override
    public boolean shouldHaveIncommingSequenceFlow() {
        return !isCompensationTask();
    }

    @Override
    public boolean shouldHaveOutgoingSequenceFlow() {
        return !isCompensationTask();
    }

    @Override
    public boolean mayHavIncommingSequenceFlow() {
        return true;
    }

    @Override
    public boolean mayHaveOutgoingSequenceFlow() {
        return ! isCompensationTask();
    }

    @Override
    public boolean mayHaveIncommingMessageFlow() {
        return isTask() || isCallActivity();
    }

    @Override
    public boolean mayHaveOutgoingMessageFlow() {
        return isTask() || isCallActivity();
    }

    @Override
    public boolean isForCompensation() {
        return isCompensationTask();
    }
}
