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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * @author fpu
 */
public class ModelServer {

    /** Foldername, ModelDirectory    */
    private Map<String, ModelDirectory> dirMap = new HashMap<String, ModelDirectory>();

    private URI uri;
    private String name;
    private UserCredentials credentials;

    private static final String DefaultLocalURI = Configuration.getInstance().getProperty("server_uri", "http://localhost:49152");//"http://localhost:8080/Processeditor";
    private static final UserCredentials DefaultCredentials = new UserCredentials(DefaultLocalURI, Configuration.getInstance().getProperty("server_user", "root"), Configuration.getInstance().getProperty("server_password", "inubit"));


    /**
     * Connects to a ProcessEditorServer without credentials. Should not be
     * used anymore, since the MAGICTOKEN might be removed at anytime.
     *
     * @deprecated
     * @param uri
     * @param name
     * @throws URISyntaxException
     */
    public ModelServer(URI uri, String name) throws URISyntaxException {
        this.uri = new URI(uri.getScheme()+"://"+uri.getAuthority());
        this.name = name;
    }

    /**
     * Connects to a ProcessEditorServer with the given credentials but
     * a different server than in the credentials.
     *
     * @param uri
     * @param name
     * @param credentials
     * @throws URISyntaxException
     */
    public ModelServer(URI uri, String name, UserCredentials credentials) throws URISyntaxException {
        this.uri = new URI(uri.getScheme()+"://"+uri.getAuthority()); //fails with new base uri http://localhost:8080/Processeditor/
        this.name = name;
        this.credentials = credentials;
    }

    /**
     * Connects to a ProcessEditorServer with the given credentials.
     * @param name
     * @param credentials
     */
    public ModelServer(String name, UserCredentials credentials) {
        this.uri = credentials.getServer();
        this.name = name;
        this.credentials = credentials;
    }

    public ModelServer() throws URISyntaxException {
        this(getDefaultLocalURI(), "ModelServer", getDefaultCredentials());
    }

    public ModelDescription findModel(String modelUri) throws Exception {
        return findModel(getDirectory(), modelUri);
    }

    private ModelDescription findModel(ModelDirectory dir, String modelUri) throws Exception {
        //System.out.println("Searching in "+dir+" for "+modelUri);
        for (ModelDirectoryEntry e: dir.getEntries()) {
            //System.out.println("  -> Entry: "+e);
            if (e instanceof ModelDirectory) {
                ModelDescription result = findModel((ModelDirectory)e, modelUri);
                if (result!=null) return result; // Found result
            }
            if (e instanceof ModelDescription) {
                ModelDescription md = (ModelDescription)e;
                if (modelUri.contains(md.getUri().toASCIIString())) return md;
            }
        }
        return null;
    }

    public ModelDirectory getDirectory() throws IOException, ParserConfigurationException, ParserConfigurationException, XPathExpressionException, InvalidUserCredentialsException, XMLHttpRequestException {
        ModelDirectory rootDirectory = new ModelDirectory(name);

        dirMap.put("/", rootDirectory);

        // Prepare XPath
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // Request top-level directories for user
        XmlHttpRequest req = new XmlHttpRequest(URI.create(uri.toASCIIString() + "/models"));
        if (credentials!=null) {
            req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE+"="+credentials.getSessionId());
        }
        req.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, HttpConstants.CONTENT_TYPE_APPLICATION_XML);
        Document models = req.executeGetRequest();

        String query = "//model";
        Object res = xpath.evaluate(query, models, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;

        for (int i = 0; i < nodes.getLength(); i++) {
            Element model = (Element) nodes.item(i);
            String modelName = model.getElementsByTagName("name").item(0).getTextContent();
            String modelUri = model.getElementsByTagName("uri").item(0).getTextContent();
            String returnedHost = URI.create(modelUri).getHost();
            // Check if modelURI has the connected server (and not some internal name!)
            String requestedHost = uri.getHost();
            if (!returnedHost.equals(requestedHost)) {
                // Put in requestedHost
                URI ru = URI.create(modelUri);
                modelUri = uri.getScheme()+"://"+requestedHost+":"+uri.getPort()+ru.getPath();
            }
            String folderAlias = model.getElementsByTagName("folderalias").item(0).getTextContent();

            // Create ModelDescription
            ModelDescription descr = new ModelDescription(URI.create(modelUri), modelName, folderAlias, credentials );
            // Get corresponding directory
            ModelDirectory currDirectory = getDirectory(folderAlias);
            currDirectory.addEntry(descr);
        }

        // Free resources
        credentials.logout();

        return rootDirectory;
    }

    private ModelDirectory getDirectory(String folderAlias) {
        // Check if can be found
        if (dirMap.containsKey(folderAlias)) return dirMap.get(folderAlias);
        // Create all needed folders
        StringTokenizer st = new StringTokenizer(folderAlias,"/");
        ModelDirectory currDir = dirMap.get("/");
        String currPath = "";
        while (st.hasMoreTokens()) {
            String nextFolderName = st.nextToken();
            currPath += "/"+nextFolderName;
            // Check if contained
            if (dirMap.containsKey(currPath)) {
                currDir = dirMap.get(currPath);
            } else {
                // Create
                ModelDirectory newDir = new ModelDirectory(nextFolderName);
                currDir.addEntry(newDir);
                dirMap.put(currPath, newDir);
                currDir=newDir;
            }
        }
        return currDir;
    }

    public static void main(String args[]) throws Exception {
        ModelServer server = new ModelServer(new URI("http://127.0.0.1:1205"), "Test", new UserCredentials(new URI("http://127.0.0.1:1205"),"fpu","inubit"));
        server.getDirectory();
        //System.out.println(server.findModel("http://127.0.0.1:1205/models/3229061/versions/1.pm"));
    }

    public static UserCredentials getDefaultCredentials() {
        return DefaultCredentials;
    }

    public static URI getDefaultLocalURI() {
        try {
            return new URI(DefaultLocalURI);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ModelServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }



}
