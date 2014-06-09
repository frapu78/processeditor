/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.CancelEndEvent;
import net.frapu.code.visualization.bpmn.CancelIntermediateEvent;
import net.frapu.code.visualization.bpmn.CompensationEndEvent;
import net.frapu.code.visualization.bpmn.CompensationIntermediateEvent;
import net.frapu.code.visualization.bpmn.CompensationStartEvent;
import net.frapu.code.visualization.bpmn.ConditionalIntermediateEvent;
import net.frapu.code.visualization.bpmn.ConditionalStartEvent;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.ErrorEndEvent;
import net.frapu.code.visualization.bpmn.ErrorIntermediateEvent;
import net.frapu.code.visualization.bpmn.ErrorStartEvent;
import net.frapu.code.visualization.bpmn.EscalationEndEvent;
import net.frapu.code.visualization.bpmn.EscalationIntermediateEvent;
import net.frapu.code.visualization.bpmn.EscalationStartEvent;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.LinkIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageEndEvent;
import net.frapu.code.visualization.bpmn.MessageIntermediateEvent;
import net.frapu.code.visualization.bpmn.MessageStartEvent;
import net.frapu.code.visualization.bpmn.MultipleEndEvent;
import net.frapu.code.visualization.bpmn.MultipleIntermediateEvent;
import net.frapu.code.visualization.bpmn.MultipleStartEvent;
import net.frapu.code.visualization.bpmn.ParallelMultipleIntermediateEvent;
import net.frapu.code.visualization.bpmn.ParallelMultipleStartEvent;
import net.frapu.code.visualization.bpmn.SignalEndEvent;
import net.frapu.code.visualization.bpmn.SignalIntermediateEvent;
import net.frapu.code.visualization.bpmn.SignalStartEvent;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.TerminateEndEvent;
import net.frapu.code.visualization.bpmn.TimerIntermediateEvent;
import net.frapu.code.visualization.bpmn.TimerStartEvent;

/**
 *
 * @author tmi
 */
public class EventAdaptor extends NodeAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof Event;
    }

    protected EventAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public EventAdaptor(Event adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return EventAdaptor.canAdapt(node);
    }

    @Override
    public Event getAdaptee() {
        return (Event) super.getAdaptee();
    }

    @Override
    public boolean isEvent() {
        return true;
    }

    public boolean isThrowingMessageEvent() {
        if (isMessageIntermediateEvent()) {
            return isThrowing();
        } else {
            return isMessageEndEvent();
        }
    }

    public boolean isThrowing() {
        return IntermediateEvent.EVENT_SUBTYPE_THROWING.equals(getAdaptee().
                getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE));
    }

    public boolean isCatching() {
        return IntermediateEvent.EVENT_SUBTYPE_CATCHING.equals(getAdaptee().
                getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE));
    }

    public boolean isInterrupting() {
        if (isStartEvent()) {
            return getProperty(StartEvent.PROP_NON_INTERRUPTING).
                    equals(StartEvent.EVENT_NON_INTERRUPTING_FALSE);
        } else if (isIntermediateEvent()) {
            return getProperty(IntermediateEvent.PROP_NON_INTERRUPTING).
                    equals(IntermediateEvent.EVENT_NON_INTERRUPTING_FALSE);
        } else {
            return true;
        }
    }

    @Override
    public boolean isMessageIntermediateEvent() {
        return getAdaptee() instanceof MessageIntermediateEvent;
    }

    public boolean isCatchingMessageIntermediateEvent() {
        return isMessageIntermediateEvent() && isCatching();
    }

    public boolean isMessageEndEvent() {
        return getAdaptee() instanceof MessageEndEvent;
    }

    public boolean isCatchingMessageEvent() {
        if (isMessageIntermediateEvent()) {
            return !isThrowing();
        } else {
            return isMessageStartEvent();
        }
    }

    public boolean isMessageStartEvent() {
        return getAdaptee() instanceof MessageStartEvent;
    }

    @Override
    public boolean isMessageEvent() {
        return isMessageStartEvent() || isMessageEndEvent() || isMessageIntermediateEvent();
    }

    public boolean isCompensationIntermediateEvent() {
        return getAdaptee() instanceof CompensationIntermediateEvent;
    }

    public boolean isCompensationStartEvent() {
        return getAdaptee() instanceof CompensationStartEvent;
    }

    public boolean isErrorStartEvent() {
        return getAdaptee() instanceof ErrorStartEvent;
    }

    public boolean isErrorEndEvent() {
        return getAdaptee() instanceof ErrorEndEvent;
    }

    public boolean isCancelEvent() {
        return isCancelEndEvent() || isCancelIntermediateEvent();
    }

    public boolean isCancelEndEvent() {
        return getAdaptee() instanceof CancelEndEvent;
    }

    public boolean isCancelIntermediateEvent() {
        return getAdaptee() instanceof CancelIntermediateEvent;
    }

    @Override
    public boolean isStartEvent() {
        return getAdaptee() instanceof StartEvent;
    }

    @Override
    public boolean isNoneStartEvent() {
        return getAdaptee().getClass() == StartEvent.class;
    }

    @Override
    public boolean isEndEvent() {
        return getAdaptee() instanceof EndEvent;
    }

    public boolean isNoneEndEvent() {
        return getAdaptee().getClass().equals(EndEvent.class);
    }

    public boolean isTerminateEndEvent() {
        return getAdaptee() instanceof TerminateEndEvent;
    }

    public boolean isEscalationEndEvent() {
        return getAdaptee() instanceof EscalationEndEvent;
    }

    public boolean isCompensationEndEvent() {
        return getAdaptee() instanceof CompensationEndEvent;
    }

    public boolean isIntermediateEvent() {
        return getAdaptee() instanceof IntermediateEvent;
    }

    public boolean isNoneIntermediateEvent() {
        return getAdaptee().getClass().equals(IntermediateEvent.class);
    }

    public boolean isErrorIntermediateEvent() {
        return getAdaptee() instanceof ErrorIntermediateEvent;
    }

    public boolean isEscalationStartEvent() {
        return getAdaptee() instanceof EscalationStartEvent;
    }

    public boolean isEscalationIntermediateEvent() {
        return getAdaptee() instanceof EscalationIntermediateEvent;
    }

    public boolean isTimerStartEvent() {
        return getAdaptee() instanceof TimerStartEvent;
    }

    public boolean isTimerIntermediateEvent() {
        return getAdaptee() instanceof TimerIntermediateEvent;
    }

    public boolean isSignalStartEvent() {
        return getAdaptee() instanceof SignalStartEvent;
    }

    public boolean isSignalIntermediateEvent() {
        return getAdaptee() instanceof SignalIntermediateEvent;
    }

    public boolean isSignalEndEvent() {
        return getAdaptee() instanceof SignalEndEvent;
    }

    public boolean isCatchingSignalIntermediateEvent() {
        return isSignalIntermediateEvent() && isCatching();
    }

    public boolean isConditionalStartEvent() {
        return getAdaptee() instanceof ConditionalStartEvent;
    }

    public boolean isConditionalIntermediateEvent() {
        return getAdaptee() instanceof ConditionalIntermediateEvent;
    }

    public boolean isMultipleIntermediateEvent() {
        return getAdaptee() instanceof MultipleIntermediateEvent;
    }

    public boolean isCatchingMultipleIntermediateEvent() {
        return isMultipleIntermediateEvent() && isCatching();
    }

    public boolean isThrowingMultipleIntermediateEvent() {
        return isMultipleIntermediateEvent() && isThrowing();
    }

    public boolean isMultipleStartEvent() {
        return getAdaptee() instanceof MultipleStartEvent;
    }

    public boolean isMultipleEndEvent() {
        return getAdaptee() instanceof MultipleEndEvent;
    }

    public boolean isParallelMultipleIntermediateEvent() {
        return getAdaptee() instanceof ParallelMultipleIntermediateEvent;
    }

    public boolean isCatchingParallelMultipleIntermediateEvent() {
        return isParallelMultipleIntermediateEvent() && isCatching();
    }

    public boolean isThrowingParallelMultipleIntermediateEvent() {
        return isParallelMultipleIntermediateEvent() && isThrowing();
    }

    public boolean isParallelMultipleStartEvent() {
        return getAdaptee() instanceof ParallelMultipleStartEvent;
    }

    public boolean isLinkIntermediateEvent() {
        return getAdaptee() instanceof LinkIntermediateEvent;
    }

    public boolean isCatchingLinkIntermediateEvent() {
        return isLinkIntermediateEvent() && isCatching();
    }

    public boolean isThrowingLinkIntermediateEvent() {
        return isLinkIntermediateEvent() && isThrowing();
    }

    public boolean isAttachable() {
        return getAdaptee() instanceof AttachedNode;
    }

    public boolean isAttached() {
        return ! getParentNode().isNull();
    }

    public NodeAdaptor getParentNode() {
        if (! isAttachable()) return new NodeAdaptor(null, model);
        return NodeAdaptor.adapt(((AttachedNode)getAdaptee()).
                getParentNode(model.getAdaptee()),
                model);
    }

    @Override
    public boolean shouldHaveIncommingSequenceFlow() {
        return ! (isAttached() ||
                  isCatchingLinkIntermediateEvent() ||
                  isStartEvent());
    }

    @Override
    public boolean shouldHaveOutgoingSequenceFlow() {
        return ! (isThrowingLinkIntermediateEvent() ||
                  isEndEvent() ||
                  isCompensationIntermediateEvent());
    }

    @Override
    public boolean mayHavIncommingSequenceFlow() {
        return !(isAttached() ||
                 isCatchingLinkIntermediateEvent() ||
                 isStartEvent());
    }

    @Override
    public boolean mayHaveOutgoingSequenceFlow() {
        return !(isThrowingLinkIntermediateEvent() ||
                 isEndEvent());
    }

    @Override
    public boolean mayHaveIncommingMessageFlow() {
        return isCatchingMessageEvent() ||
                isCatchingMessageIntermediateEvent() ||
                isCatchingMultipleIntermediateEvent() ||
                isMultipleStartEvent() ||
                isCatchingParallelMultipleIntermediateEvent() ||
                isParallelMultipleStartEvent();
    }

    @Override
    public boolean mayHaveOutgoingMessageFlow() {
        return isThrowingMessageEvent() ||
                isThrowingMultipleIntermediateEvent() ||
                isMultipleEndEvent();
    }

    @Override
    public boolean isAllowedInChoreography() {
        if (isStartEvent()) {
            return isNoneStartEvent() ||
                    isTimerStartEvent() ||
                    isConditionalStartEvent() ||
                    isSignalStartEvent() ||
                    isMultipleStartEvent() ||
                    isParallelMultipleStartEvent();
        } else if (isIntermediateEvent()) {
            return isNoneIntermediateEvent() ||
                    isMessageIntermediateEvent() ||
                    isTimerIntermediateEvent() ||
                    isCancelIntermediateEvent() ||
                    isCompensationIntermediateEvent() ||
                    isConditionalIntermediateEvent() ||
                    isLinkIntermediateEvent() ||
                    isSignalIntermediateEvent() ||
                    isMultipleIntermediateEvent() ||
                    isParallelMultipleIntermediateEvent();
        } else {
            return isNoneEndEvent() ||
                    isTerminateEndEvent();
        }
    }

    @Override
    public boolean isForCompensation() {
        return isCompensationIntermediateEvent() && isCatching();
    }

    public EventAdaptor getIntermediateEventWithSameTrigger() {
        EventAdaptor event;
        if (isIntermediateEvent()) {
            return this;
        } else if (isMessageStartEvent() || isMessageEndEvent()) {
            event = new EventAdaptor(new MessageIntermediateEvent(), model);
        } else if (isTimerStartEvent()) {
            event = new EventAdaptor(new TimerIntermediateEvent(), model);
        } else if (isSignalStartEvent() || isSignalEndEvent()) {
            event = new EventAdaptor(new SignalIntermediateEvent(), model);
        } else if (isEscalationStartEvent() || isEscalationEndEvent()) {
            event = new EventAdaptor(new EscalationIntermediateEvent(), model);
        } else if (isConditionalStartEvent()) {
            event = new EventAdaptor(new ConditionalIntermediateEvent(), model);
        } else if (isErrorStartEvent() || isErrorEndEvent()) {
            event = new EventAdaptor(new ErrorIntermediateEvent(), model);
        } else if (isCancelEndEvent()) {
            event = new EventAdaptor(new CancelIntermediateEvent(), model);
        } else if (isConditionalStartEvent() || isCompensationEndEvent()) {
            event = new EventAdaptor(new CompensationIntermediateEvent(), model);
        } else if (isMultipleStartEvent() || isMultipleEndEvent()) {
            event = new EventAdaptor(new MultipleIntermediateEvent(), model);
        } else if (isParallelMultipleStartEvent()) {
            event = new EventAdaptor(new ParallelMultipleIntermediateEvent(), model);
        } else  {
            event = new EventAdaptor(new IntermediateEvent(), model);
        }
        if (isEndEvent()) {
            event.getAdaptee().setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE,
                    IntermediateEvent.EVENT_SUBTYPE_THROWING);
        }
        event.getAdaptee().setText(getText());
        return event;
    }
}
