/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.ChoreographyActivity;
import net.frapu.code.visualization.bpmn.ChoreographyTask;

/**
 *
 * @author tmi
 */
public class ChoreographyActivityAdaptor extends NodeAdaptor
        implements ChoreographyNodeAdaptor {
    
    public static boolean canAdapt(ProcessNode node) {
        return node instanceof ChoreographyActivity;
    }

    protected ChoreographyActivityAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public ChoreographyActivityAdaptor(ChoreographyActivity adaptee,
            ModelAdaptor model) {
        super(adaptee, model);
    }

    @Override
    public ChoreographyActivity getAdaptee() {
        return (ChoreographyActivity) super.getAdaptee();
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return ChoreographyActivityAdaptor.canAdapt(node);
    }

    @Override
    public boolean isChoreographyActivity() {
        return true;
    }

   @Override
   public boolean isChoreographyTask() {
       return getAdaptee() instanceof ChoreographyTask;
   }

    @Override
    public String getActiveParticipant() {
        return getAdaptee().getProperty(ChoreographyTask.PROP_ACTIVE_PARTICIPANT);
    }

    public boolean upperParticipantIsActive() {
        return getActiveParticipant().equals(getUpperParticipant());
    }

    @Override
    public List<String> getPassiveParticipants() {
        if (upperParticipantIsActive()) {
            return getLowerParticipants();
        } else {
            return getUpperParticipants();
        }
    }

    @Override
    public List<String> getParticipants() {
        List<String> participants = new LinkedList<String>(getUpperParticipants());
        participants.addAll(getLowerParticipants());
        return participants;
    }

    @Override
    public List<String> getUpperParticipants() {
        List<String> upperParticipants = new LinkedList<String>();
        upperParticipants.add(getUpperParticipant());
        return upperParticipants;
    }

    public String getUpperParticipant() {
        return getAdaptee().getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT);
    }

    @Override
    public List<String> getLowerParticipants() {
        List<String> lowerParticipants = new LinkedList<String>();
        lowerParticipants.add(getLowerParticipant());
        return lowerParticipants;
    }

    public String getLowerParticipant() {
        return getAdaptee().getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT);
    }

    @Override
    public boolean hasParticipant(String participant) {
        return getLowerParticipant().equals(participant) ||
                getUpperParticipant().equals(participant);
    }

    @Override
    public boolean isMultipleParticipant(String participant) {
        if (getUpperParticipant().equals(participant)) {
            return upperParticipantIsMultiple();
        } else if (getLowerParticipant().equals(participant)) {
            return lowerParticipantIsMultiple();
        } else {
            return false;
        }
    }

    public boolean upperParticipantIsMultiple() {
        return getProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT_MULTI).
                equals(ChoreographyTask.TRUE);
    }

    public boolean lowerParticipantIsMultiple() {
        return getProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT_MULTI).
                equals(ChoreographyTask.TRUE);
    }

    @Override
    public NodeAdaptor asNodeAdaptor() {
        return this;
    }

    @Override
    public boolean isAllowedInChoreography() {
        return true;
    }
}
