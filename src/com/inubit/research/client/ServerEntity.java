/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author uha
 */
public abstract class ServerEntity {

    protected URI uri;
    protected UserCredentials credentials;

    public UserCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(UserCredentials credentials) {
        this.credentials = credentials;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    protected XmlHttpRequest getRequest() {
        try {
            XmlHttpRequest req = new XmlHttpRequest(getUri());
            req.addCredentials(getCredentials());
            return req;
        } catch (Exception ex) {
            Logger.getLogger(ServerEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected XmlHttpRequest getRequest(URI uri) {
        try {
            XmlHttpRequest req = new XmlHttpRequest(uri);
            req.addCredentials(getCredentials());
            return req;
        } catch (Exception ex) {
            Logger.getLogger(ServerEntity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }



}
