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
 * @author fpu
 */
public class Sentence {

    protected Predicate predicate;
    protected Subject subject;

    public Sentence(Predicate predicate, Subject subject) {
        this.predicate = predicate;
        this.subject = subject;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "P{"+predicate+"}+S{"+subject+"}";
    }

}
