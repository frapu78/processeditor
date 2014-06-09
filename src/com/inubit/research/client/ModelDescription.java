/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.request.handler.UserRequestHandler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * Describes a ProcessModel with all its variants.
 *
 * @author fpu
 */
public class ModelDescription implements ModelDirectoryEntry {

    private String name;
    private URI uri;
    private String folder;
    private List<ModelVersionDescription> modelVersionDescriptions = null;
    private static final String predecessorsKey = "predecessors";
    private UserCredentials credentials;

    public ModelDescription(URI uri, String name, String folder, UserCredentials credentials) {
        this.name = name;
        this.uri = uri;
        this.folder = folder;
        this.credentials = credentials;
    }

    public ImageIcon getPreview() throws MalformedURLException, ParserConfigurationException, IOException, XMLHttpRequestException, XPathExpressionException, InvalidUserCredentialsException {
        return new ImageIcon(new URL(uri.toASCIIString() + "/preview" + "?"+UserRequestHandler.SESSION_ATTRIBUTE+"=" + credentials.getSessionId()));
    }

    public static List<String> decodePredecessors(String versionString) {
        String[] versions = versionString.split(", ");
        LinkedList<String> result = new LinkedList<String>();
        for (String s : versions) {
            if (!versionString.equals("")) {
                result.add(s);
            }
        }
        return result;
    }

    public static String encodePredecessors(List<String> versionStrings) {
        if (versionStrings==null) {
            return "";
        }
        if (versionStrings.isEmpty()) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        for (String s : versionStrings) {
            result.append(s);
            result.append(", ");
        }
        result.delete(result.length() - 2, result.length() - 1);
        return result.toString();
    }

    public List<ModelVersionDescription> getModelVersionDescriptions() throws IOException, ParserConfigurationException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        List<ModelVersionDescription> models = new LinkedList<ModelVersionDescription>();
        // Retrieve versions

        // Prepare XPath
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // Request top-level directory
        XmlHttpRequest req = new XmlHttpRequest(URI.create(uri.toASCIIString() + "/versions"));
        if (credentials!=null) {
            req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE+"="+credentials.getSessionId());
        }
        Document versions = req.executeGetRequest();
        String query = "//version";
        Object res = xpath.evaluate(query, versions, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;

        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            String version = elem.getAttribute("id");
            String versionUri = elem.getElementsByTagName("uri").item(0).getTextContent();
            String comment = "-";
            //getting preceeding/succeeding versions
            List<String> predecessors;
            try {
                predecessors = decodePredecessors(elem.getAttribute(predecessorsKey));
                if (predecessors.isEmpty()) {
                    throw new IllegalArgumentException("loaded model in outdated format");
                }
            } catch (IllegalArgumentException e) {
                //fallback for older models
                predecessors = new LinkedList<String>();
                //not first version
                if (Integer.parseInt(version) > 0) {
                    Integer prevVersion = Integer.parseInt(version) - 1;
                    predecessors.add(prevVersion.toString());
                }
            }
            if (elem.getElementsByTagName("comment").getLength() > 0) {
                comment = elem.getElementsByTagName("comment").item(0).getTextContent();
            }
            // Create ModelVersionDescription
            ModelVersionDescription descr = new ModelVersionDescription(this, URI.create(versionUri), version, comment, predecessors, credentials);
            models.add(0, descr);
        }
        modelVersionDescriptions = models;
        return models;
    }

    public void publishModelVersionDescription(ModelVersionDescription commit) throws IOException, Exception {
        ModelDirectory.publishToServer(commit.getProcessModel(), false, commit.modelUri.toString(), null, commit.getComment(),
                this.folder, commit.getProcessModel().getProcessName(), commit.getPredecessors());
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public String getFolder() {
        return folder;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public ModelVersionDescription getVersionDescription(String version) throws IOException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        if (modelVersionDescriptions == null) {
            getModelVersionDescriptions();
        }
        for (ModelVersionDescription d : modelVersionDescriptions) {
            if (d.getVersion().equals(version)) {
                return d;
            }
        }
        return null;
    }

    public ModelVersionDescription getVersionDescription(URI modelURI) throws IOException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        if (modelVersionDescriptions == null) {
            getModelVersionDescriptions();
        }
        for (ModelVersionDescription d : modelVersionDescriptions) {
            if (d.getModelUri().equals(modelURI)) {
                return d;
            }
        }
        return null;
    }

    public Set<ModelVersionDescription> getPredecessors(ModelVersionDescription from) throws IOException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        if (modelVersionDescriptions == null) {
            getModelVersionDescriptions();
        }
        List<String> versionNumbers = from.getPredecessors();
        Set<ModelVersionDescription> result = new HashSet<ModelVersionDescription>();
        for (String v : versionNumbers) {
            result.add(getVersionDescription(v));
        }
        return result;
    }

    public Set<ModelVersionDescription> getSuccessors(ModelVersionDescription from) throws IOException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, MalformedURLException, InvalidUserCredentialsException {
        if (modelVersionDescriptions == null) {
            getModelVersionDescriptions();
        }
        Set<ModelVersionDescription> result = new HashSet<ModelVersionDescription>();
        for (ModelVersionDescription d : modelVersionDescriptions) {
            if (d.getPredecessors().contains(from.getVersion())) {
                result.add(d);
            }
        }
        return result;
    }

    public String getHeadVersion() throws IOException, ParserConfigurationException, XPathExpressionException, IOException, XMLHttpRequestException, IOException, IOException, XMLHttpRequestException, InvalidUserCredentialsException {
        Integer v = getModelVersionDescriptions().size() - 1;
        return v.toString();
    }

    public ModelVersionDescription getHead() throws IOException, ParserConfigurationException, XPathExpressionException, XMLHttpRequestException, InvalidUserCredentialsException {
        return getVersionDescription(getHeadVersion());
    }
}
