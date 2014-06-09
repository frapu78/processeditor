/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import com.inubit.research.server.HttpConstants;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 *
 * @author fel
 */
public class ExchangeResponseFacade extends ResponseFacade {
    private HttpExchange ex;

    public ExchangeResponseFacade( HttpExchange ex ) {
        this.ex = ex;
    }

    @Override
    public void setContentType(String contentType) {
        this.setHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE, contentType);
    }

    @Override
    public void setStatus(int statusCode) throws IOException {
        this.ex.sendResponseHeaders(statusCode, 0);
    }

    @Override
    public void setHeader(String key, String value) {
        this.ex.getResponseHeaders().set(key, value);
    }

    @Override
    public void sendRedirect(String redirectPath) throws IOException {
        this.ex.getResponseHeaders().set(HttpConstants.HEADER_KEY_LOCATION, "/?redirect=" + redirectPath);
        ResponseUtils.respondWithStatus(307, "", this, false);
    }

    @Override
    public void addCookie(String key, String value, String path) {
        this.ex.getResponseHeaders().set("Set-Cookie",
                            key + "=" + value +
                            ";Path=" + path);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.ex.getResponseBody();
    }

    @Override
    public URL getResource(String path) throws IOException {
        return this.getClass().getResource(path);
    }
}
