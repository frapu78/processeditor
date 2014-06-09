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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import static com.inubit.research.server.HttpConstants.*;
/**
 *
 * @author uha
 */
public class ServerModel extends ServerEntity {



    public ServerModel(URI uri, UserCredentials credentials) {
        this.uri = uri;
        this.credentials = credentials;
    }


    

     /**
     * Gets the URL of the nodes contained in a model given by the URL of this Model.
     *
     * @return the list of the URI of the nodes in the given model
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws Exception
     */
    public List<URI> getNodeURIs() throws IOException, ParserConfigurationException, Exception {
        return getURIs(getUri(), getCredentials(), FOLDER_NODES_ALIAS);
    }

    /**
     * Gets the URL of the edges contained in a model given by the URL of this Model.
     *
     * @return the list of the URI of the edges in the given model
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws Exception
     */
    public List<URI> getEdgeURIs() throws IOException, ParserConfigurationException, URISyntaxException, InvalidUserCredentialsException, XMLHttpRequestException, MalformedURLException, XPathExpressionException {
        return getURIs(getUri(), getCredentials(), FOLDER_EDGES_ALIAS);
    }

    private List<URI> getURIs(URI modelURI, UserCredentials credentials, String pathAddition) throws IOException, ParserConfigurationException, URISyntaxException, InvalidUserCredentialsException, XMLHttpRequestException, MalformedURLException, XPathExpressionException {
        String path = modelURI.toString();
        path = path + pathAddition;
        URI uri = new URI(path);
        XmlHttpRequest req = new XmlHttpRequest(uri);
        req.addCredentials(credentials);
        Document doc = req.executeGetRequest();
        NodeList elementsByTagName = doc.getElementsByTagName("uri");
        int l = elementsByTagName.getLength();
        ArrayList<URI> result = new ArrayList<URI>(l);
        for (int i=0; i<l;i++) {
            result.add(new URI(elementsByTagName.item(i).getTextContent()));
        }
        return result;
    }

    public URI getNodeURI(String id) throws URISyntaxException {
        return new URI(getUri() + HttpConstants.FOLDER_NODES_ALIAS + "/" + id);
    }

    public URI getEdgeURI(String id) throws URISyntaxException {
        return new URI(getUri() + HttpConstants.FOLDER_EDGES_ALIAS + "/" + id);
    }


}
