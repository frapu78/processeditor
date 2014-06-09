/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.layouter;

/**
 * 
 * This is thrown if a method comes across a model type that is not supported.
 *
 * @author frank
 */
public class UnsupportedModelTypeException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2319116094003022026L;

	public UnsupportedModelTypeException(String message) {
        super(message);
    }

    public UnsupportedModelTypeException() {
    }

    
}
