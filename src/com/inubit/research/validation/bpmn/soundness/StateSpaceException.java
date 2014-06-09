/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.soundness;

/**
 *
 * @author tmi
 */
public class StateSpaceException extends Exception {

    public StateSpaceException() {
        super ("too many states found");
    }
}
