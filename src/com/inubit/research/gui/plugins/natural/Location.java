/**
 *
 * Process Editor - inubit Workbench Natural Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.natural;

import java.util.List;

/**
 *
 * @author fpu
 */
public class Location {

    protected String modifier;
    protected List<Subject> subjects;

    public Location(String modifier, List<Subject> subjects) {
        this.modifier = modifier;
        this.subjects = subjects;
    }

    public String getModifier() {
        return modifier;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

}
