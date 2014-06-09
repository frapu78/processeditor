/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.user.SingleUser;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;


import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.meta.ISMetaDataHandler;
import com.inubit.research.server.meta.MetaDataHandler;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ISServerModel;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.persistence.PersistenceConnector;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.ProcessModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author ff
 *
 */
public class ISLocation implements Location {

    private URI f_uri;
    private String f_pw;
    private String f_user;
    private String owner = "unknown";
    private Random f_random = new Random(System.currentTimeMillis());
    private ISMetaDataHandler f_metaHandler = null;
    private Map<String, List<ServerModel>> f_myIndex;
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    private Document index;

    /**
     * this hashcode ensures that no ISLocation with the same user/URL combination can be added
     */
    @Override
    public int hashCode() {
        return f_user.hashCode() + f_uri.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ISLocation)) {
            return false;
        }

        ISLocation ism = (ISLocation) o;
        
        if (f_uri == null || !f_uri.equals(ism.f_uri)) {
            return false;
        }
        if (f_user == null || !f_user.equals(ism.f_user)) {
            return false;
        }
        if (f_pw == null || !f_pw.equals(ism.f_pw)) {
            return false;
        }

        return true;
    }

    /**
     *
     */
    public ISLocation(URI serverURL, String username, String password) {
        f_uri = serverURL;
        f_user = username;
        f_pw = password;
    }

    private String encodeCredentials(String uri) {
        // Check if user or password is already appended
        if (uri.contains("user=")) {
            return uri;
        }
        return uri + "?user=" + f_user + "&password=" + f_pw;
    }

    public boolean checkConnection() {
        try {
            XmlHttpRequest req = new XmlHttpRequest(URI.create(encodeCredentials(f_uri.toString())));
            index = req.executeGetRequest();
        } catch (Exception ex) {
            System.err.println("Could not connect to iS!");
            System.err.println(this);
            //ex.printStackTrace();
            return false;
        }
        return true;
    }

    public ServerModel saveProcessModel(ProcessModel pm, String id, int version) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeModel(String id, SingleUser user) {
        String path = this.getMetaDataHandler().getFolderAlias(id);
        if ( path.endsWith( MetaCache.ATTIC_FOLDER_NAME ) ) {
            this.getMetaDataHandler().remove(id);
            return true;
        }

        return false;
    }

    public String getAtticPath(SingleUser user) {
        return this.f_uri.toString() + MetaCache.ATTIC_FOLDER_NAME;
    }

    @Override
    public Map<String, List<ServerModel>> getIndex(Set<String> usedIDs, boolean forceRefresh) {
        if (f_myIndex == null || forceRefresh) {
            f_myIndex = buildIndex(usedIDs);
        }

        return f_myIndex;
    }

    private Map<String, List<ServerModel>> buildIndex(Set<String> usedIDs) {

        HashMap<String, List<ServerModel>> _result = new HashMap<String, List<ServerModel>>();
        // Check connection and update model index
        if (!checkConnection()) {
            return _result;
        }

        // Fetch all models
        String query = "/models/model";
        Object res;
        try {
            res = xpath.evaluate(query, index, XPathConstants.NODESET);
            NodeList modelNodes = (NodeList) res;

            PersistenceConnector pc = ProcessEditorServerHelper.getPersistenceConnector();
            Map<String, String> mapping = pc.getIDMapping( this.f_uri.toString() );
            Map<String, String> toAdd = new HashMap<String, String>();
            boolean create = false;
            if ( mapping.isEmpty() )
                create = true;

            // Iterate over all models
            for (int i = 0; i < modelNodes.getLength(); i++) {
                assert modelNodes.item(i) instanceof Element;
                Element modelElement = (Element) modelNodes.item(i);
                // Check supported types
                String type = modelElement.getElementsByTagName("type").item(0).getTextContent();
                String name = modelElement.getElementsByTagName("name").item(0).getTextContent();
                String group = modelElement.getElementsByTagName("group").item(0).getTextContent();
                String uri = modelElement.getElementsByTagName("uri").item(0).getTextContent().replace(" ", "%20");
                String md5 = modelElement.getElementsByTagName("md5").item(0).getTextContent();
                String comment = modelElement.getElementsByTagName("comment").item(0).getTextContent();
                // Currently supported: BPD and Constrainsdiagram
                if (type.startsWith("bpd") || type.startsWith("constraintsdiagram")) {
                    ArrayList<ServerModel> _addMe = new ArrayList<ServerModel>();
                    _addMe.add(new ISServerModel(URI.create(encodeCredentials(uri)), name, group, type, f_user, f_pw, md5, comment));

                    if ( mapping.get(uri) != null ) {
                        _result.put( mapping.get(uri), _addMe );
                    } else {
                        String newId = createID(usedIDs, md5);
                        toAdd.put( newId, uri );
                        _result.put( newId, _addMe );
                    }
                }
            }

            if ( create )
                pc.storeIDMapping( this.f_uri.toString(), toAdd );
            else
                pc.addToIDMapping( this.f_uri.toString(), toAdd );

        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
            return _result;
        }

        return _result;
    }

    /**
     * @param usedIDs
     * @return
     */
    private String createID(Set<String> usedIDs, String md5) {
        String _id;
        // Check if already contained in f_index
        if (f_myIndex != null) {
            for (String key : f_myIndex.keySet()) {
                for (ServerModel h: f_myIndex.get(key)) {
                    if (h.getChecksum().equals(md5)) {
                        // found
                        return key;
                    }
                }
            }
        }

        do {
            _id = "" + Math.abs(f_random.nextLong());
        } while (usedIDs.contains(_id));
        return _id;
    }

    public ServerModel getModel(String id) {
        return f_myIndex.get(id).get(0);
    }

    @Override
    public MetaDataHandler getMetaDataHandler() {
        if (f_metaHandler == null) {
            try {
                f_metaHandler = new ISMetaDataHandler(f_uri.toURL(), f_user, this);
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        return f_metaHandler;
    }

    @Override
    public LocationType getType() {
        return LocationType.IS;
    }

    @Override
    public String getName() {
        return this.getMetaDataHandler().getFolderAlias(null);
    }

    @Override
    public Set<String> listPaths() {
        Set<String> paths = new HashSet<String>();
        paths.add( this.getMetaDataHandler().getFolderAlias(null) );

        for (String id : this.f_myIndex.keySet()) {
            String p = this.getMetaDataHandler().getFolderAlias(id);

            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            paths.add(p);
        }
        return paths;
    }

    @Override
    public Set<String> listPaths(SingleUser user) {
        if (user.getISConnections().contains(this)) {
            return listPaths();
        } else {
            return new HashSet<String>();
        }
    }

    @Override
    public Map<String, AccessType> getModelsForUser(SingleUser user) {
        Map<String, AccessType> models = new HashMap<String, AccessType>();

        if (user.getISConnections().contains(this)) {
            Set<String> ids = this.getIndex(new HashSet<String>(),false).keySet();

            for (String id : ids) {
                models.put(id, AccessType.OWNER);
            }
        }

        return models;
    }

    @Override
    public ServerModel createNewModel( File model, String path, String id, SingleUser user, String comment ) {
        try {
            ProcessModel m = new ProcessEditorImporter().parseSource(model).get(0);
            String group = path.replace(this.getMetaDataHandler().getFolderAlias(null), "");
            if ( group.isEmpty() )
                group = "ProcessEditorExport";
            else 
                group = group.substring( group.lastIndexOf("/") + 1 );

//            m.setProperty( ISDiagramImporter.PROP_GROUP, group);
            ISServerModel hm = new ISServerModel(f_uri, m.getProcessName(), group, "NEW", f_user, f_pw, ""+(Math.random()*1000000000),"");
//            ISServerModel lm = ( ISServerModel) hm.load();

            hm.save(m, 0, id, comment, path);

            List<ServerModel> l = new LinkedList<ServerModel>();
            l.add(hm);

            Map<String, String> newIdMap = new HashMap<String, String>(1);
            newIdMap.put( id, hm.getServerURI().toString().substring(0, hm.getServerURI().toString().indexOf("?")) );

            ProcessEditorServerHelper.getPersistenceConnector().addToIDMapping( this.f_uri.toString(), newIdMap);

            this.f_myIndex.put(id, l);

            return hm;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public AccessType getAccessability(String id, int version, LoginableUser user) {
        if (user.getName().equals(this.owner)) {
            return AccessType.OWNER;
        } else if (user.isAdmin()) {
            return AccessType.ADMIN;
        } else {
            return AccessType.NONE;
        }
    }

    public String getOwner(String id) {
        //not supported...maybe ;-)
        return this.owner;
    }

    public Set<User> getViewers(String id) {
        //not supported...maybe ;-)
        return new HashSet<User>();
    }

    public Set<User> getEditors(String id) {
        //not supported...maybe ;-)
        return new HashSet<User>();
    }

    public Set<User> getAnnotators(String id) {
        //not supported...maybe ;-)
        return new HashSet<User>();
    }

    public boolean setOwner(String id, SingleUser owner, SingleUser admin) {
        this.owner = owner.getName();
        //not supported...maybe ;-)
        return false;
    }

    public String getURL() throws Exception {
        return this.f_uri.toASCIIString();
    }

    public String getUser() {
        return this.f_user;
    }

    public String getPwd() {
        return this.f_pw;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(100);

        b.append("\tURL: " + this.f_uri);
        b.append("\n\tName: " + this.f_user);
        b.append("\n\tPwd: " + this.f_pw);

        return b.toString();
    }
//        public void persistData(File baseDir, Set<String> usedIds) throws Exception {
//            File f = new File(baseDir, f_url.hashCode() + ".isconn");
//
//            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
//            xmlFactory.setNamespaceAware(false);
//            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
//
//            Document doc = builder.newDocument();
//            Element rootElem = doc.createElement("isconnection");
//            rootElem.setAttribute("version", "1.0");
//            doc.appendChild(rootElem);
//
//            Element urlElem = doc.createElement("url");
//            urlElem.setTextContent(f_url.toString());
//            rootElem.appendChild(urlElem);
//
//            Element userElem = doc.createElement("user");
//            userElem.setTextContent(f_user);
//            rootElem.appendChild(userElem);
//
//            Element pwElem = doc.createElement("pwd");
//            pwElem.setTextContent(f_pw);
//            rootElem.appendChild(pwElem);
//
//            FileOutputStream fos = new FileOutputStream(f);
//            OutputStreamWriter osw = new OutputStreamWriter(fos);
//
//            ProcessEditorServerUtils.writeXMLtoStream(osw, doc);
//            fos.close();
//        }
//
//        public static ISLocation fromConnectionFile(File f, Set<String> usedIds) throws Exception {
//            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
//            xmlFactory.setNamespaceAware(false);
//            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
//
//            Document doc = builder.parse(f);
//
//            String url = doc.getElementsByTagName("url").item(0).getTextContent();
//            String user = doc.getElementsByTagName("user").item(0).getTextContent();
//            String pwd = doc.getElementsByTagName("pwd").item(0).getTextContent();
//
//            ISLocation ism = new ISLocation(new URL(url), user, pwd);
//
//            if (ism.checkConnection()) {
//                ism.getIndex(usedIds);
//
//                return ism;
//            }
//
//            return null;
//        }
}
