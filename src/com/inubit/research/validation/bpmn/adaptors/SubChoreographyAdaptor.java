/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;

/**
 *
 * @author tmi
 */
public class SubChoreographyAdaptor extends ClusterAdaptor
        implements ChoreographyNodeAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node instanceof ChoreographySubProcess;
    }

    protected SubChoreographyAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public SubChoreographyAdaptor(ChoreographySubProcess adaptee,
            ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return SubChoreographyAdaptor.canAdapt(node);
    }

    @Override
    public ChoreographySubProcess getAdaptee() {
        return (ChoreographySubProcess) super.getAdaptee();
    }
    
    @Override
    public boolean isChoreographyActivity() {
        return true;
    }

    @Override
    public boolean isSubChoreography() {
        return true;
    }

    @Override
    public boolean isWhiteboxSubChoreography() {
        return !isCollapsed();
    }

    public boolean isCollapsed() {
        return getProperty(ChoreographySubProcess.PROP_COLLAPSED).
                equals(ChoreographySubProcess.TRUE);
    }

    @Override
    public String getActiveParticipant() {
        return getAdaptee().getProperty(
                ChoreographySubProcess.PROP_ACTIVE_PARTICIPANTS);
        //actually only one ACTIVE_PARTICIPANT
    }

    @Override
    public List<String> getPassiveParticipants() {
        List<String> passive = getParticipants();
        passive.remove(getActiveParticipant());
        return passive;
    }

    @Override
    public List<String> getParticipants() {
        List<String> participants = getUpperParticipants();
        participants.addAll(getLowerParticipants());
        return participants;
    }

    @Override
    public List<String> getUpperParticipants() {
        return new LinkedList<String>(Arrays.asList(
                getAdaptee().
                getProperty(ChoreographySubProcess.PROP_UPPER_PARTICIPANTS).
                split(";+")));
    }

    @Override
    public List<String> getLowerParticipants() {
        return new LinkedList<String>(Arrays.asList(
                getAdaptee().
                getProperty(ChoreographySubProcess.PROP_LOWER_PARTICIPANTS).
                split(";+")));
    }

    public List<String> collectParticipants() {
        List<String> participants = new LinkedList<String>();
        for (NodeAdaptor node : getProcessNodes()) {
            if (node.isChoreographyActivity()) {
                participants.addAll(
                        ((ChoreographyNodeAdaptor)node).getParticipants());
            }
        }
        return participants;
    }

    @Override
    public boolean hasParticipant(String participant) {
        return getParticipants().contains(participant);
    }

    @Override
    public NodeAdaptor asNodeAdaptor() {
        return this;
    }

    @Override
    public boolean isMultipleParticipant(String participant) {
        if (getLowerParticipants().contains(participant)) {
            return isMultipleInList(participant,
                    getLowerParticipants(),
                    new LinkedList<String>(Arrays.asList(
                    getProperty(ChoreographySubProcess.
                    PROP_LOWER_PARTICIPANTS_MULTI).split(";"))));
        } else {
            return isMultipleInList(participant,
                    getUpperParticipants(),
                    new LinkedList<String>(Arrays.asList(getProperty(
                    ChoreographySubProcess.
                    PROP_UPPER_PARTICIPANTS_MULTI).split(";"))));
        }
    }

    private boolean isMultipleInList(String participant,
            List<String> participants, List<String> participantsMI) {
        for (int i = 0; i < participants.size(); ++i) {
            if (participants.get(i).equals(participant)) {
                if (i < participantsMI.size()) {
                    return participantsMI.get(i).
                            equals(ChoreographySubProcess.TRUE);
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAllowedInChoreography() {
        return true;
    }
}
