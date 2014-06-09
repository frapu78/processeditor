/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.errors;

/**
 *
 * @author fpu
 */
public class ModelNotFoundException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7898170114328844038L;

	public ModelNotFoundException(String message) {
        super(message);
    }

    public ModelNotFoundException() {
    }   

}
