/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * Describes a ProcessModel version.
 *
 * @author fpu
 */
public class ModelVersionDescription {

    protected URI modelUri;
    protected String version;
    protected String comment;
    protected ModelDescription parent;
    protected List<String> predecessors;
    private UserCredentials credentials;

    public ModelVersionDescription(ModelDescription parent, URI modelUri, String version, String comment, List<String> predecessors, UserCredentials credentials) {
        this.modelUri = modelUri;
        this.version = version;
        this.comment = comment;
        this.parent = parent;
        this.predecessors = predecessors;
        this.credentials = credentials;
    }

    public UserCredentials getCredentials() {
        return this.credentials;
    }

    public ImageIcon getPreview() throws MalformedURLException, ParserConfigurationException, IOException, XMLHttpRequestException, XPathExpressionException, InvalidUserCredentialsException {
        URL url = new URL(modelUri.toASCIIString() + "/preview" +"?PES_SESSION_ID=" + credentials.getSessionId());
        return new ImageIcon(url);
    }

    public ProcessModel getProcessModel() throws IOException, Exception {
        ProcessModel model = ProcessUtils.parseProcessModelSerialization(modelUri, credentials);
        // Set recent URI
        model.setProcessModelURI(modelUri.toASCIIString());
        return model;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelVersionDescription other = (ModelVersionDescription) obj;
        if (this.modelUri != other.modelUri && (this.modelUri == null || !this.modelUri.equals(other.modelUri))) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        if ((this.comment == null) ? (other.comment != null) : !this.comment.equals(other.comment)) {
            return false;
        }
        if (this.predecessors != other.predecessors && (this.predecessors == null || !this.predecessors.equals(other.predecessors))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.modelUri != null ? this.modelUri.hashCode() : 0);
        hash = 59 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 59 * hash + (this.comment != null ? this.comment.hashCode() : 0);
        hash = 59 * hash + (this.predecessors != null ? this.predecessors.hashCode() : 0);
        return hash;
    }

    
    public ModelDescription getParentModelDescription() {
        return parent;
    }

    public String getComment() {
        return comment;
    }

    public URI getModelUri() {
        return modelUri;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return version;
    }

    public List<String> getPredecessors() {
        return predecessors;
    }



}
