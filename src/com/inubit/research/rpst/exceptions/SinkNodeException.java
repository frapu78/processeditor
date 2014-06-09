/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.rpst.exceptions;

/**
 *
 * @author fel
 */
public class SinkNodeException extends Exception {
    public SinkNodeException(String key) {
        super( "This graph has " + key + " sink nodes!" );
    }
}
