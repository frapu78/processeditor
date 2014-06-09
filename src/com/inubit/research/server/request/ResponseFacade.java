/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 *
 * @author fel
 */
public abstract class ResponseFacade {

    public abstract void setContentType( String contentType );

    public abstract void setStatus( int statusCode ) throws IOException ;

    public abstract void setHeader( String key, String value );

    public abstract void sendRedirect( String redirectPath ) throws IOException;

    public abstract void addCookie( String key, String value , String path );

    public abstract OutputStream getOutputStream() throws IOException ;

    public abstract URL getResource( String path ) throws IOException ;

}
