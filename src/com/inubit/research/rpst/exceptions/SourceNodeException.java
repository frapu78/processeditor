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
public class SourceNodeException extends Exception {
    public SourceNodeException(String key) {
        super( "This graph has " + key + " source nodes!" );
    }
}
