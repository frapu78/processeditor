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
import java.io.InputStream;


/**
 *
 * @author fel
 */
public abstract class RequestFacade {

    public abstract String getRequestMethod();

    public abstract String getRequestURI();

    public abstract String getQuery();

    public abstract String getCookieByName( String name );

    public abstract String getHeader( String key );

    public abstract InputStream getInputStream() throws IOException;

    public abstract String getProtocol();

    public abstract String getPort();

    public abstract String getContext();

    public abstract String getLocalAddress();

    public abstract String getRemoteHost();
}
