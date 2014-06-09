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
public class Predicate {

    protected String verb;
    protected String object_type;
    protected String object_label;

    public Predicate(String verb, String object_type, String object_label) {
        this.verb = verb;
        this.object_type = object_type;
        this.object_label = object_label;
    }

    public String getObject_label() {
        return object_label;
    }


    public String getObject_type() {
        return object_type;
    }

    public String getVerb() {
        return verb;
    }

    @Override
    public String toString() {
        return "V{"+verb+"}+O{"+object_type+"}"+(object_label==null?"":"+L{"+object_label+"}");
    }

}
