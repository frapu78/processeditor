/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author fel
 */
public class ExchangeRequestFacade extends RequestFacade {

    private HttpExchange ex;
    private String localAddress = null;

    public ExchangeRequestFacade(HttpExchange ex) {
        this.ex = ex;
    }

    @Override
    public String getRequestMethod() {
        return this.ex.getRequestMethod();
    }

    @Override
    public String getRequestURI() {
        return ex.getRequestURI().toASCIIString();
    }

    @Override
    public String getQuery() {
        return ex.getRequestURI().getQuery();
    }

    @Override
    public String getCookieByName(String name) {
        Map<String, String> pars = new HashMap<String, String>();
        String cookie = ex.getRequestHeaders().getFirst("Cookie");

        if (cookie != null) {
            String[] parts = cookie.split("; ");
            for (String part : parts) {
                String[] c = part.split("=");

                if (c[0].trim().equals(name)) {
                    return c[1].trim();
                }
            }
        }

        return null;
    }

    @Override
    public String getHeader(String key) {
        return ex.getRequestHeaders().getFirst(key);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return ex.getRequestBody();
    }

    @Override
    public String getProtocol() {
        boolean isSecure = this.ex instanceof HttpsExchange;

        return ex.getProtocol().split("/")[0].toLowerCase() + (isSecure ? "s" : "");
    }

    @Override
    public String getPort() {
        return String.valueOf(this.ex.getLocalAddress().getPort());
    }

    @Override
    public String getContext() {
        return "";
    }

    @Override
    public String getLocalAddress() {
        if (localAddress == null) {
            localAddress = ex.getLocalAddress().getHostName() + ":" + ex.getLocalAddress().getPort();

        }
        return localAddress;
    }
    @Override
    public String getRemoteHost() {
        return this.ex.getRemoteAddress().getHostName();
    }
}
