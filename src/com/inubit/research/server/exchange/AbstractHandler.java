/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.exchange;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

/**
 *
 * @author fel
 */
public abstract class AbstractHandler implements HttpHandler {
    protected String contextUri = "/";


    public String getContextUri() {
        return this.contextUri;
    }

    public abstract void handle(HttpExchange t) throws IOException;
}
