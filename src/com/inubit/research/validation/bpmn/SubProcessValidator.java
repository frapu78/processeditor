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
import java.util.LinkedList;
import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import java.util.List;
import net.frapu.code.visualization.bpmn.SubProcess;

/**
 *
 * @author tmi
 */
class SubProcessValidator {

    private ClusterAdaptor subProcess;
    private ModelAdaptor model;
    private BPMNValidator validator;

    public SubProcessValidator(ClusterAdaptor subPrcoess, ModelAdaptor model,
            BPMNValidator validator) {
        this.subProcess = subPrcoess;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        if (isAdHoc()) {
            validateAdHocProcess();
        } else {
            validateStartEvents();
        }
    }

    private void validateAdHocProcess() {
        if (isTransaction()) {
            validator.addMessage("adHocTransaction", subProcess);
            return;
        }
        if (isTriggeredByEvent()) {
            validator.addMessage("adHocEventSubProcess", subProcess);
            return;
        }
        for (NodeAdaptor node : subProcess.getProcessNodes()) {
            if (node.isStartEvent()) {
                List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                related.add(subProcess);
                validator.addMessage("adHocSubProcessWithStartEvent",
                        node, related);
            } else if (node.isEndEvent()) {
                List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                related.add(subProcess);
                validator.addMessage("adHocSubProcessWithEndEvent",
                        node, related);
            }
        }
    }

    private void validateStartEvents() {
        if (isTriggeredByEvent()) {
            validateStartEventsForEventSubProcess();
        } else {
            validateStartEventsForNotEventTriggeredSubProcess();
        }
    }

    private void validateStartEventsForNotEventTriggeredSubProcess() {
        List<NodeAdaptor> startEvents = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : subProcess.getProcessNodes()) {
            if (node.isStartEvent()) {
                startEvents.add(node);
                if (!node.isNoneStartEvent()) {
                    List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
                    related.add(subProcess);
                    validator.addMessage("subProcessWithTriggeredStartEvent",
                            node, related);
                }
            }
        }
        if (startEvents.size() > 1) {
            validator.addMessage("subProcessWithMultipleStartEvents",
                    subProcess, startEvents);
        }
    }

    private void validateStartEventsForEventSubProcess() {
        List<NodeAdaptor> startEvents = new LinkedList<NodeAdaptor>();
        for (NodeAdaptor node : subProcess.getProcessNodes()) {
            if (node.isStartEvent()) {
                startEvents.add(node);
                validateStartEventForEventSubProcess((EventAdaptor)node);
            }
        }
        if (startEvents.isEmpty()) {
            validator.addMessage("eventSubProcessWithoutStartEvent", subProcess);
        } else if (startEvents.size() > 1) {
            validator.addMessage("eventSubProcessWithMultipleStartEvents",
                    subProcess, startEvents);
        }
    }

    private void validateStartEventForEventSubProcess(EventAdaptor node) {
        if (node.isNoneStartEvent()) {
            List<NodeAdaptor> related = new LinkedList<NodeAdaptor>();
            related.add(subProcess);
            validator.addMessage("eventSubProcessWithNoneStartEvent",
                    node, related);
        }
    }

    private boolean isAdHoc() {
        return subProcess.getProperty(SubProcess.PROP_AD_HOC).
                equals(SubProcess.TRUE);
    }

    private boolean isTransaction() {
        return subProcess.getProperty(SubProcess.PROP_TRANSACTION).
                equals(SubProcess.TRUE);
    }

    private boolean isTriggeredByEvent() {
        return subProcess.getProperty(SubProcess.PROP_EVENT_SUBPROCESS).
                equals(SubProcess.TRUE);
    }
}
