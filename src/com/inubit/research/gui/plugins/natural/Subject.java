/**
 *
 * Process Editor - inubit Workbench Natural Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.natural;

/**
 *
 * Represents the subject of a sentence.
 *
 * @author fpu
 */
public class Subject {

    private String subject;
    private Location location;

    public Subject(String subject, Location location) {
        this.subject = subject;
        this.location = location;
    }

    public String getSubject() {
        return subject;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return subject;
    }

}
