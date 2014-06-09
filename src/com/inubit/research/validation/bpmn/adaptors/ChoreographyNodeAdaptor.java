/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import java.util.List;

/**
 *
 * @author tmi
 */
public interface ChoreographyNodeAdaptor {

    public String getActiveParticipant();
    public List<String> getPassiveParticipants();
    public List<String> getParticipants();
    public List<String> getUpperParticipants();
    public List<String> getLowerParticipants();
    public boolean hasParticipant(String participant);
    public boolean isMultipleParticipant(String participant);
    public NodeAdaptor asNodeAdaptor();
    //will actually only return this in all intentional subclasses, but avoids casts
}
