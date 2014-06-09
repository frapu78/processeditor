/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

/**
 *
 * @author fpu
 */
public class XMLHttpRequestException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5085711334772836587L;
	private int statusCode;

    public XMLHttpRequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
