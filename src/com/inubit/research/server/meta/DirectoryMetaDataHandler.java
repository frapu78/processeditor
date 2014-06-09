/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.meta;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.config.DirectoryConfig;
import com.inubit.research.server.config.ModelConfig;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;
import com.inubit.research.server.user.User;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Taken from MetaDataUtils.java of earlier versions <br>
 * Provides read/write access to meta-data information for files.
 * @author fel
 * @author fpu
 */
public class DirectoryMetaDataHandler implements MetaDataHandler {
    private static final String META_SUFFIX = ".meta";
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    private File dir;
    private DirectoryConfig config;

//    private Map<String, Set<ProcessObjectComment>> comments = new HashMap<String, Set<ProcessObjectComment>>();

    public DirectoryMetaDataHandler( File dir) {
        this.dir = dir;
        try {
            this.config = DirectoryConfig.forDirectory(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVersionComment(String id, String version, String comment) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/comments/comment[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element commentElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                commentElem = (Element) nodes.item(0);
            } else {
                commentElem = doc.createElement("comment");
                commentElem.setAttribute("version", version);
                // Append
                doc.getElementsByTagName("comments").item(0).appendChild(commentElem);
            }
            if (comment == null) {
                comment = "";
            }
            commentElem.appendChild(doc.createTextNode(comment));

            // Write back
            writeMetaDataFile(id, doc);
        } catch (Exception ex) {
        }
    }

    @Override
    public String getVersionComment(String id, String version) {
       try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/comments/comment[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element commentElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                commentElem = (Element) nodes.item(0);
                return commentElem.getTextContent();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setVersionUser( String id, String version, String user ) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/users/user[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element commentElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                commentElem = (Element) nodes.item(0);
            } else {
                commentElem = doc.createElement("user");
                commentElem.setAttribute("version", version);
                // Append
                doc.getElementsByTagName("users").item(0).appendChild(commentElem);
            }
            if (user == null) {
                user = "unknown";
            }
            commentElem.setTextContent(user);

            // Write back
            writeMetaDataFile(id, doc);
        } catch (Exception ex) {
        }
    }

    @Override
    public String getVersionUser( String id, String version ) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a user exists
            String query = "/metadata/users/user[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node userElem = (Node) res;
            if (userElem != null) {
                // Entry found, use
                return userElem.getTextContent();
            } else {
                return "unknown";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void setSucceedingVersions( String id, String version, Set<String> versions ) {
        if (version == null || versions.isEmpty())
            return;
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if successors exists
            String query = "/metadata/dependencies/successors[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element succElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                succElem = (Element) nodes.item(0);
            } else {
                succElem = doc.createElement("successors");
                succElem.setAttribute("version", version);
                // Append
                doc.getElementsByTagName("dependencies").item(0).appendChild(succElem);
            }
            
            succElem.setTextContent(versions.toString().replace("[", "").replace("]", ""));

            // Write back
            writeMetaDataFile(id, doc);
        } catch (Exception ex) {
        }
    }

    @Override
    public Set<String> getSucceedingVersions( String id, String version ) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/dependencies/successors[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node succElem = (Node) res;
            if (succElem != null) {
                // Entry found, use
                String versionString = succElem.getTextContent();
                String[] vs = versionString.split(",");
                Set<String> versions = new HashSet<String>();

                for (String v : vs)
                    versions.add(v.trim());

                return versions;
            } else {
                return new HashSet<String>();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<String>();
    }

    @Override
    public void setPreceedingVersions( String id, String version, Set<String> versions ) {
        if (versions == null || versions.isEmpty())
            return;
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if successors exists
            String query = "/metadata/dependencies/predecessors[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element precElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                precElem = (Element) nodes.item(0);
            } else {
                precElem = doc.createElement("predecessors");
                precElem.setAttribute("version", version);
                // Append
                doc.getElementsByTagName("dependencies").item(0).appendChild(precElem);
            }
            precElem.setTextContent(versions.toString().replace("[", "").replace("]", ""));

            // Write back
            writeMetaDataFile(id, doc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Set<String> getPreceedingVersions( String id, String version ) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/dependencies/predecessors[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node precElem = (Node) res;
            if (precElem != null) {
                // Entry found, use
                String versionString = precElem.getTextContent();
                String[] vs = versionString.split(",");
                Set<String> versions = new HashSet<String>();

                for (String v : vs)
                    versions.add(v.trim());

                return versions;
            } else {
                return new HashSet<String>();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<String>();
    }

    @Override
    public void setFolderAlias(String id, String alias, SingleUser user) {
        this.config.setPath(id, alias, user);
    }

    @Override
    public String getFolderAlias(String id) {
        String path = this.config.getPathForModel(id);

        if (path != null) {
            if (!path.startsWith("/"))
                path = "/" + path;
            return path;
        }

        return "/"; // Fallback
    }

    @Override
    public VersionMetaData getVersionMetaData(String id, String version) {
        String comment = this.getVersionComment(id, version);
        String folder = this.getFolderAlias(id);
        String date = this.getVersionDate(id, version);
        String user = this.getVersionUser(id, version);
        return new VersionMetaData(folder, comment, user, date, this);
    }

    @Override
    public void remove( String id ) {
        File f = this.getMetaDataFile(id);

        if (f != null)
            f.delete();
        this.config.remove(id);
    }

    @Override
    public Set<ProcessObjectComment> getComments( String modelId, String version, String elementId ) {
        if (elementId == null)
            return this.getComments(modelId, version);
        else
            return this.getElementComments(modelId, version, elementId);
    }

    public DirectoryConfig getConfig() {
        return this.config;
    }

    
    public AccessType getAccessability(String id, int version, LoginableUser user) {
        if(user.isTemporaryUser()) {
            TemporaryUser tu = (TemporaryUser) user;
            if (tu.getModelId().equals(id) && tu.getModelVersion() == version)
                return AccessType.COMMENT;
        }

        return this.getAccessType(user, id, new HashSet<Group>());
    }

    public String getOwner( String id ) {
        ModelConfig mc = this.config.getModelConfig(id);
        if (mc != null)
            return mc.getOwner();

        return null;
    }

    public Set<User> getViewers( String id ) {
        ModelConfig mc = this.config.getModelConfig(id);
        if (mc != null)
            return mc.getViewers();

        return new HashSet<User>();
    }

    public Set<User> getAnnotators( String id ) {
        ModelConfig mc = this.config.getModelConfig(id);
        if (mc != null)
            return mc.getAnnotators();

        return new HashSet<User>();
    }

    public Set<User> getEditors( String id ) {
        ModelConfig mc = this.config.getModelConfig(id);
        if (mc != null)
            return mc.getEditors();

        return new HashSet<User>();
    }

    public boolean setOwner( String id, SingleUser owner, SingleUser admin ) {
        return this.config.setOwner(id, owner, admin);
    }

    @Override
    public void grantRight( String id, AccessType at, Set<User> users ) {
        this.config.grantRight( id, at, users );
    }

    @Override
    public void divestRight( String id, AccessType at, Set<User> users ) {
        this.config.divestRight( id, at, users );
    }

    private AccessType getAccessType( User user, String id, Set<Group> visitedGroups ) {
         if (user.isSingleUser() && ((SingleUser) user).isAdmin())
            return AccessType.ADMIN;

        ModelConfig mc = this.config.getModelConfig(id);

        if (mc != null) {
            if (user.isSingleUser() && mc.isOwner((SingleUser) user))
                return AccessType.OWNER;

            if (mc.isWriteableByUser(user))
                return AccessType.WRITE;

            if (mc.isReadableByUser(user))
                return AccessType.VIEW;

            if (mc.isAnnotatableByUser(user))
                return AccessType.COMMENT;
        }

        Set<Group> groups = ProcessEditorServerHelper.getUserManager().getGroupsForUser(user);

        AccessType maxAccess = AccessType.NONE;
        for (Group g : groups) {
            if (visitedGroups.contains(g))
                continue;

            visitedGroups.add(g);
            AccessType at = this.getAccessType(g, id, visitedGroups);
            if (at.compareTo(maxAccess) > 0)
                maxAccess = at;
        }

        return maxAccess;
    }

    public void addComment( String id, ProcessObjectComment comment ) {
        try {
            Document doc = this.readMetaDataFile(id);

            String query = "/metadata/elementcomment";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList commentIdAttrs = (NodeList) res;

            Set<String> commentIds = new HashSet<String>();

            for (int i = 0; i < commentIdAttrs.getLength(); i++)
                commentIds.add(commentIdAttrs.item(i).getAttributes().getNamedItem("id").getNodeValue());

            Random ran = new Random(System.currentTimeMillis());
            String newId = String.valueOf(Math.abs(ran.nextInt()));
            while (commentIds.contains(newId))
                newId = String.valueOf(Math.abs(ran.nextInt()));

            comment.setCommentId(newId);
            comment.serialize(doc, doc.getDocumentElement());
            writeMetaDataFile(id, doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProcessObjectComment updateComment( String id, String commentId, String newText, int validUntil ) {
        try {
            Document doc = this.readMetaDataFile(id);

            String query = "/metadata/elementcomment[@id='" + commentId + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node commentNode = (Node) res;

            if (commentNode != null) {
                ((Element) commentNode).setAttribute("validuntil", String.valueOf(validUntil));
                NodeList children = commentNode.getChildNodes();
                Node textNode = null;
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeName().equals("text")) {
                        textNode = children.item(i);
                        break;
                    }
                }
                if (textNode != null)
                    textNode.setTextContent(newText);

            }

            this.writeMetaDataFile(id, doc);
            return ProcessObjectComment.forNode(commentNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Deprecated
    public void resolveComment( String id, String commentId, String version ) {
        try {
            Document doc = this.readMetaDataFile(id);

            String query = "/metadata/elementcomment[@id='" + commentId + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node commentNode = (Node) res;

            if (commentNode != null) {
                ((Element) commentNode).setAttribute("validuntil", version);
//                if (textNode != null)
//                    textNode.setTextContent(newText);

            }

            this.writeMetaDataFile(id, doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeComment( String id, String commentId ) {
        try {
            Document doc = this.readMetaDataFile(id);

            String query = "/metadata/elementcomment[@id='" + commentId + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODE);
            Node commentNode = (Node) res;

            if (commentNode != null)
                commentNode.getParentNode().removeChild(commentNode);

            this.writeMetaDataFile(id, doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read meta data file for the given ID
     * @param id the ID
     * @return the meta file document
     * @throws Exception 
     */
    private Document readMetaDataFile(String id) throws Exception {
        // Check if file exists
        File testFile = new File(this.dir, id + ".v0");
        if (!testFile.exists()) {
            System.err.println(testFile.getPath());
            throw new FileNotFoundException("ID not found");
        }

        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document xmlDoc = null;
        try {
            xmlDoc = builder.parse(getMetaDataFile(id));
        } catch (Exception ex) {
            // Create new metadata file
            xmlDoc = builder.newDocument();
            Element rootElem = xmlDoc.createElement("metadata");
            rootElem.setAttribute("version", "1.0");
            xmlDoc.appendChild(rootElem);
            Element aliasElem = xmlDoc.createElement("alias");
            rootElem.appendChild(aliasElem);
            Element commentsElem = xmlDoc.createElement("comments");
            rootElem.appendChild(commentsElem);
            Element usersElem = xmlDoc.createElement("users");
            rootElem.appendChild(usersElem);
            Element datesElem = xmlDoc.createElement("dates");
            rootElem.appendChild(datesElem);
            Element depsElem = xmlDoc.createElement("dependencies");
            rootElem.appendChild(depsElem);
        }
        return xmlDoc;
    }

    /**
     * Write meta data file for the given ID
     * @param id the ID
     * @param doc the document to be written
     * @throws Exception 
     */
    private void writeMetaDataFile(String id, Document doc) throws Exception {
        FileOutputStream fos = new FileOutputStream(getMetaDataFile(id));
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        ProcessEditorServerUtils.writeXMLtoStream(osw,doc);
        fos.close();
    }

    /**
     * Fetch meta data file for ID from disc
     * @param id the ID
     * @return the meta data file
     */
    private File getMetaDataFile(String id) {
        return new File(this.dir, id + META_SUFFIX);
    }

    private Set<ProcessObjectComment> getElementComments( String id,  String version, String elementId ) {
        try {
            Document doc = this.readMetaDataFile(id);

            String query = "/metadata/elementcomment[@elementid='" + elementId + "' and @validfrom <= " + version +
                    " and @validuntil >= " + version + "]";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList commentElems = (NodeList) res;

            Set<ProcessObjectComment> elementComments = new HashSet<ProcessObjectComment>(commentElems.getLength());
            for (int i = 0; i < commentElems.getLength(); i++) {
                elementComments.add(ProcessObjectComment.forNode(commentElems.item(i)));
            }

            return elementComments;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashSet<ProcessObjectComment>();
    }

    private Set<ProcessObjectComment>  getComments( String id , String version ) {
        try {
            Document doc = this.readMetaDataFile(id);
            String query = "/metadata/elementcomment[@validfrom <= " + version +
                    " and @validuntil >= " + version + "]";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList commentElems = (NodeList) res;

            Set<ProcessObjectComment> elementComments = new HashSet<ProcessObjectComment>(commentElems.getLength());
            for (int i = 0; i < commentElems.getLength(); i++) 
                elementComments.add(ProcessObjectComment.forNode(commentElems.item(i)));

            return elementComments;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashSet<ProcessObjectComment>();
    }

    @Override
    public void setVersionDate(String id, String version, Date date) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a comment exists
            String query = "/metadata/dates/date[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element dateElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                dateElem = (Element) nodes.item(0);
            } else {
                dateElem = doc.createElement("date");
                dateElem.setAttribute("version", version);
                // Append
                doc.getElementsByTagName("dates").item(0).appendChild(dateElem);
            }

            dateElem.appendChild(doc.createTextNode(DateFormat.getInstance().format(date)));

            // Write back
            writeMetaDataFile(id, doc);
        } catch (Exception ex) {
        }
    }

    @Override
    public String getVersionDate(String id, String version) {
        try {
            // Get meta data
            Document doc = readMetaDataFile(id);
            // Check if a date exists
            String query = "/metadata/dates/date[@version='" + version + "']";
            Object res = xpath.evaluate(query, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) res;
            Element dateElem = null;
            if (nodes.getLength() > 0) {
                // Entry found, use
                dateElem = (Element) nodes.item(0);
                return dateElem.getTextContent();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //else return current date
        return DateFormat.getInstance().format(new Date());
    }
}
