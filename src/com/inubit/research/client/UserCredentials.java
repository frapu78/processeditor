/**
 *
 * Process Editor - inubit Client Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.client;

import com.inubit.research.gui.WorkbenchConnectToServerDialog;
import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.handler.PersistentModelRequestHandler;
import com.inubit.research.server.request.handler.UserRequestHandler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.frapu.code.visualization.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * This class provides credentials for a ProcessEditorServer user.
 *
 * @author fpu
 */
public class UserCredentials {

    private URI server;
    private String user, password, sessionId;
    private String cookie;

    public UserCredentials(URI server, String user, String password) {
        this.server = server;
        this.user = user;
        this.password = password;
    }

    /**
     * Creates user credentials from a request.
     * @param req
     */
    public UserCredentials(RequestFacade req) {
        this.server = URI.create(PersistentModelRequestHandler.getAbsoluteAddressPrefix(req));
        this.sessionId = req.getCookieByName(UserRequestHandler.SESSION_ATTRIBUTE);
        this.cookie = req.getHeader(HttpConstants.HEADER_KEY_COOKIE);
    }

    public UserCredentials(String server, String user, String password) {
        try {
            this.server = new URI(server);
        } catch (URISyntaxException ex) {
            Logger.getLogger(UserCredentials.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.user = user;
        this.password = password;
    }

    /**
     * Returns the cookies of this user (if exist).
     * @return
     */
    public String getCookies() {
        return cookie;
    }

    public String getUser() {
        return user;
    }

    public URI getServer() {
        return server;
    }

    /**
     * Returns the current session id (if not already existing, the
     * user is logged in).
     * 
     * @return
     */
    public String getSessionId() throws ParserConfigurationException, MalformedURLException, IOException, XMLHttpRequestException, XPathExpressionException, InvalidUserCredentialsException {
        if (sessionId==null) loginUser();
        return sessionId;
    }

    /**
     * Refreshes the user's login (session).
     */
    public void refresh() throws XMLHttpRequestException, InvalidUserCredentialsException, IOException, ParserConfigurationException, MalformedURLException, XPathExpressionException {
        try {
            logout();
        } catch (Exception ex) {};
        loginUser();
    }

    /**
     * Closes the user's session at the server.
     */
    public void logout() {
        // tbd.
    }

    private void loginUser() throws ParserConfigurationException, MalformedURLException, IOException, XMLHttpRequestException, XPathExpressionException, InvalidUserCredentialsException {

        // Prepare XPath
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        XmlHttpRequest login = new XmlHttpRequest(URI.create(server.toASCIIString() + "/users/login"));
        Document loginDoc = getServerLoginXML();
        Document loginResponse = login.executePostRequest(loginDoc);

        String query = "/login/id";
        Object res = xpath.evaluate(query, loginResponse, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;

        // Check if result size > 0
        if (nodes.getLength() > 0) {
            // Use first to fetch session id
            sessionId = nodes.item(0).getTextContent();
            System.out.println("USER " + getUser() + " LOGGED IN (SESSIONID=" + sessionId + ").");
        } else {
            throw new InvalidUserCredentialsException("User Credentials for USER " + getUser() + " ON SERVER " + server + " invalid.");
        }
    }

    /**
     * Returns a document that can be used to pass the credentials to the
     * server for login (/user/login).
     * 
     * @return
     * @throws ParserConfigurationException
     */
    private Document getServerLoginXML() throws ParserConfigurationException {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document xmlDoc = builder.newDocument();

//            var xml = "<user><property name='name' value='";
//            xml += name;
//            xml += "'/><property name='pwd' value='";
//            xml += pwd;
//            xml += "'/></user>";

        // Create root element
        Element rootNode = xmlDoc.createElement("user");
        xmlDoc.appendChild(rootNode);
        Element prop01 = xmlDoc.createElement("property");
        prop01.setAttribute("name", "name");
        prop01.setAttribute("value", user);
        rootNode.appendChild(prop01);

        Element prop02 = xmlDoc.createElement("property");
        prop02.setAttribute("name", "pwd");
        prop02.setAttribute("value", password);
        rootNode.appendChild(prop02);

        return xmlDoc;
    }

    /**
     * Returns the default crendentials from the configuration file.
     * @return
     */
    public static UserCredentials getDefaultCredentials() {
        Configuration conf = Configuration.getInstance();
        UserCredentials result = new UserCredentials(
                URI.create(conf.getProperty(WorkbenchConnectToServerDialog.CONF_SERVER_URI)),
                conf.getProperty(WorkbenchConnectToServerDialog.CONF_SERVER_USER),
                conf.getProperty(WorkbenchConnectToServerDialog.CONF_SERVER_PASSWORD));
        return result;
    }
}
