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
public class ModelAlreadyExistsException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -446433397487279577L;

	public ModelAlreadyExistsException(String message) {
        super(message);
    }

    public ModelAlreadyExistsException() {
    }   

}
