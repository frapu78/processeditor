/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.EventAdaptor;
import com.inubit.research.validation.bpmn.adaptors.LaneableClusterAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tmi
 */
class LinkCorrelationValidator {

    private LaneableClusterAdaptor pool;
    private ModelAdaptor model;
    private BPMNValidator validator;
    private Map<String, List<EventAdaptor>> throwingLinkEvents,
            catchingLinkEvents;

    public LinkCorrelationValidator(LaneableClusterAdaptor pool,
            ModelAdaptor model, BPMNValidator validator) {
        this.pool = pool;
        this.model = model;
        this.validator = validator;
    }

    public void doValidation() {
        buildMaps();
        validateEveryThrowingLinkHasTarget();
        validateEveryLabelHasOnlyOneCatchingLink();
        validateEveryCatchingLinkHasThrowingPartner();
    }

    private void validateEveryThrowingLinkHasTarget() {
        for (Map.Entry<String, List<EventAdaptor>> entry :
                throwingLinkEvents.entrySet()) {
            if (!catchingLinkEvents.containsKey(entry.getKey())) {
                validator.addMessage("noCatchingLink", entry.getValue());
            }
        }
    }

    private void validateEveryLabelHasOnlyOneCatchingLink() {
        for (Map.Entry<String, List<EventAdaptor>> entry :
                catchingLinkEvents.entrySet()) {
            if (entry.getValue().size() > 1) {
                validator.addMessage("multipleCatchingLinksForOneLabel",
                        entry.getValue());
            }
        }
    }

    private void validateEveryCatchingLinkHasThrowingPartner() {
        for (Map.Entry<String, List<EventAdaptor>> entry :
                catchingLinkEvents.entrySet()) {
            if (!throwingLinkEvents.containsKey(entry.getKey())) {
                validator.addMessage("noThrowingLink", entry.getValue());
            }
        }
    }

    private void buildMaps() {
        throwingLinkEvents = new HashMap<String, List<EventAdaptor>>();
        catchingLinkEvents = new HashMap<String, List<EventAdaptor>>();
        for (NodeAdaptor node : pool.getNodesOfContainedProcess()) {
            if (node.isEvent()) {
                addMapEntryIfIsLink((EventAdaptor) node);
            }
        }
    }

    private void addMapEntryIfIsLink(EventAdaptor event) {
        if (event.isLinkIntermediateEvent()) {
            String linkName = event.getText();
            if (linkName == null) {
                linkName = "";
            }
            if (event.isCatching()) {
                addToMapAt(catchingLinkEvents, linkName, event);
            } else {
                addToMapAt(throwingLinkEvents, linkName, event);
            }
        }
    }

    private void addToMapAt(Map<String, List<EventAdaptor>> map,
            String key, EventAdaptor value) {
        if (!map.containsKey(key)) {
            map.put(key, new LinkedList<EventAdaptor>());
        }
        map.get(key).add(value);
    }
}
