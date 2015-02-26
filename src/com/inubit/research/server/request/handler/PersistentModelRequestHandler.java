/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.frapu.code.converter.ConverterHelper;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelPreview;
import net.frapu.code.visualization.ProcessNode;

import org.w3c.dom.Document;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.errors.ModelAlreadyExistsException;
import com.inubit.research.server.errors.ModelNotFoundException;
import com.inubit.research.server.manager.Location;
import com.inubit.research.server.manager.TemporaryKeyManager;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.meta.VersionMetaData;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.request.handler.util.ProcessEdgeUtils;
import com.inubit.research.server.request.handler.util.ProcessNodeUtils;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;
import com.inubit.research.server.user.User;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.frapu.code.visualization.ProcessUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handler class for persistent models. This class defines the REST API for accessing
 * the process model repository.
 *
 * The offered functionality requires an authenticated user.
 *
 * @author fel
 */
public class PersistentModelRequestHandler extends ModelRequestHandler {

    private static ProcessEditorExporter exporter = new ProcessEditorExporter();

    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        String response = "";
        Document responseDoc = null;
        int version = parseVersionFromRequestUri(requestUri);
        String id = parseModelIdFromRequestUri(requestUri);

        ServerModel currentLM = modelManager.getPersistentModel(id, version );

        ProcessModel currentModel = null;
        ProcessNode currentNode = null;
        ProcessEdge currentEdge = null;
        AccessType access = null;

        if (currentLM != null) {
            access = modelManager.getAccessForModel(id, version, lu);
            if (access.equals(AccessType.NONE)) {
                throw new AccessViolationException("Access denied for user " + lu.getName() + " at URI "
                        + requestUri);
            }

            String modelUri = "/models/" + id;
            if (version == -1) {
                version = modelManager.getPersistentVersionCount(id) - 1;
            }

            modelUri += "/versions/" + version;

            currentModel = currentLM.getModel();
            currentNode = retrieveProcessNode(requestUri, currentModel);
            currentEdge = retrieveProcessEdge(requestUri, currentModel);

            /*
             * set this model's URI so that all URIs  contained in any kind of list
             * start with the correct path.
             */
            currentModel.setProcessModelURI(modelUri);
        }

        /*
         * START OF REQUEST DISPATCH (without recent model!)
         */
        if (requestUri.matches("/models")) {
            if (!lu.isSingleUser()) {
                throw new AccessViolationException("Access denied for unregistered user");
            }
            SingleUser user = (SingleUser) lu;
            String detailHeader = req.getHeader("Detailed");
            //check if detailed information is requested (e.g. for root page)
            Document doc = null;
            if (detailHeader != null && detailHeader.equals("true")) {
                doc = createOverviewXML( req.getContext(), user );
            } else //Show list of all registered models
            {
                doc = createModelList(getAbsoluteAddressPrefix( req), user);
            }
            ResponseUtils.respondWithXML(resp, doc, 200);
            return;
        } else if (requestUri.matches("/models/rss")) {
            if (!lu.isSingleUser()) {
                throw new AccessViolationException("Access denied for unregistered user");
            }
            SingleUser user = (SingleUser) lu;
            //Generate RSS-feed for models
            response = createRSSFeed(user);
        } else if (requestUri.matches("/models/\\d+/versions")) {
            //create a list of all versions
            response = createVersionList(getAbsoluteAddressPrefix(req), requestUri);
        } else if (requestUri.matches("/models/\\d+/access")) {
            //create overview of accessors
            response = createAccessList(id);
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/comments(\\?.*)?")) {
            //create list of comments for this model
            JSONObject jo = new JSONObject();
            try {
                jo.put("success", true);
                jo.put("comments", createCommentList(id, version, "model", lu));
            } catch ( JSONException ex ) {
                ex.printStackTrace();
            }
            ResponseUtils.respondWithJSON(resp, jo, 200);
            return;
//            responseDoc = createCommentList(id, version, "model", lu);
        } else if (requestUri.matches("/models/\\d+/rss")) {
            //ProcessModelUtils utils = new ProcessModelUtils(currentModel);
            //modelManager.g
            //response = utils.createRSSFeed();
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?")) {
            //Show model as XML or return editor/viewer if first accept is html
            retrieveCorrectRepresentation(currentModel, access, requestUri, req, resp);
            return;
        } else if (requestUri.matches("/models/\\d+/versions/\\d+\\?key=.+?")) {
            String key = RequestUtils.getQueryParameters(req).get("key");
            TemporaryUser tu = TemporaryKeyManager.checkKey(key, id, String.valueOf(version));
            if (tu != null) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessViewer_version.html", resp);
                return;
            }
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?\\..+")) {
            //respond with representation according to type
            retrieveCorrectRepresentation(currentModel, access, requestUri, req, resp);
            return;
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/preview(\\?(size=(\\d+)))?(.*)?")) {
            // Return preview as PNG graphics
            createPreviewPNGGraphics(currentModel, req, resp);
            return;
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/meta")) {
            //Show meta data for model
            AccessType at = modelManager.getAccessForModel(id, version, lu);
            responseDoc = createModelMetaData(currentModel, requestUri, at);
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/nodes")) {
            //Show list of all nodes contained in the current model
            responseDoc = createNodeList(currentModel, getAbsoluteAddressPrefix( req ));
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/nodes/\\d+\\.png.*")) {
            // Return node as PNG graphics
            createNodePNGGraphics(currentNode.getId(), currentModel, resp);
            return;
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/nodes/\\d+/meta")) {
            //Return node meta data
            ProcessNodeUtils utils = new ProcessNodeUtils(currentNode);
            responseDoc = utils.toXML();
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/nodes/\\d+/comments(\\?.*)?")) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("success", true);
                jo.put("comments", createCommentList(id, version, currentNode.getId(), lu));
            } catch ( JSONException ex ) {
                ex.printStackTrace();
            }
            ResponseUtils.respondWithJSON(resp, jo, 200);
            return;
//            responseDoc = this.createCommentList(id, version, currentNode.getId(), lu);
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/nodes/\\d+(.*)?")) {
            //Return node as png image or xml depending on requested content type
            String firstAccept = req.getHeader(HttpConstants.HEADER_KEY_ACCEPT);
            if (firstAccept.startsWith("image")) {
                createNodePNGGraphics(currentNode.getId(), currentModel, resp);
                return;
            }

            responseDoc = serializeObject(currentNode);
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/edges")) {
            //Show list of all edges
            responseDoc = createEdgeList(currentModel, getAbsoluteAddressPrefix(req));
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/edges/\\d+")) {
            //return edge XML
            responseDoc = serializeObject(currentEdge);
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/edges/\\d+/meta")) {
            //return edge meta data
            ProcessEdgeUtils utils = new ProcessEdgeUtils(currentEdge);
            ResponseUtils.respondWithXML(resp, utils.getMetaXMLDocument( getAbsoluteAddressPrefix(req) ), 200);
            return;
        } else if (requestUri.matches("/models/\\d+(/versions/(\\d+))?/edges/\\d+/label\\?.*")) {
            //return edge meta data
            createEdgeLabelPNGGraphics(currentEdge, resp);
            return;
        }

        if ( responseDoc != null ) {
            ResponseUtils.respondWithXML(resp, responseDoc, 200);
        } else if (response.isEmpty()) {
            // Unknown path, return 404
            resp.setContentType( HttpConstants.CONTENT_TYPE_TEXT_PLAIN );
            ResponseUtils.respondWithStatus(404, "Not found (" + requestUri + ")", resp, true);
        } else {
            resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);
            ResponseUtils.respondWithStatus(200, response, resp, false);
        }

        return;
    }

    @Override
    public void handlePostRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String requestUri = req.getRequestURI();
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        String response = "";
        int statusCode = 200;

        //Parse request headers
        String comment = ProcessEditorServerUtils.unEscapeString(req.getHeader("Comment"));
        String folder = ProcessEditorServerUtils.unEscapeString(req.getHeader("Folder-Alias"));
        String sourceRefHeader = req.getHeader("Commit-SourceRef");
        String newName = ProcessEditorServerUtils.unEscapeString(req.getHeader("Commit-Name"));
        String sourceVersion = req.getHeader("Source-Version");

        Set<String> precVersions = new HashSet<String>();

        if (sourceVersion != null) {
            String[] versions = sourceVersion.split(",");
            for (String v : versions) {
                precVersions.add(v.trim());
            }
        }

        if (folder == null) {
            // Use root folder as default
            folder = "/";
        }

        if (requestUri.matches("/models")) {
            //Import model from multipart or create it according to the given request headers
            try {
                // Set status to 201 (CREATED)
                statusCode = 201;
                if (!lu.isSingleUser()) {
                    throw new AccessViolationException("Access denied for unregistered user");
                }

                String contentHeader = req.getHeader(HttpConstants.HEADER_KEY_CONTENT_TYPE);
                if (contentHeader != null && contentHeader.contains(HttpConstants.CONTENT_TYPE_MULTIPART)) {
                    //parse model from form data
                    response = importFromMultiPart(req, (SingleUser) lu);
                    ResponseUtils.respondWithStatus(statusCode, response, HttpConstants.CONTENT_TYPE_TEXT_HTML, resp, false);
                    return;
                } else {
                    //create model according to header values
                    response = createNewModel(sourceRefHeader, newName, comment, folder, req, resp, (SingleUser) lu);
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof ModelAlreadyExistsException) {
                    // Set status to 403 (FORBIDDEN)
                    statusCode = 403;
                } else {
                    // Set status to 500 (INTERNAL SERVER ERROR)
                    statusCode = 500;
                }
                if (req.getHeader(HttpConstants.HEADER_KEY_ACCEPT).contains(HttpConstants.CONTENT_TYPE_TEXT_XML)) {
                    resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);
                    response = "<error>" + e.getMessage() + "</error>";
                } else {
                    ResponseUtils.respondWithStatus( statusCode, "{success:false}", HttpConstants.CONTENT_TYPE_TEXT_HTML, resp, false);
                    return;
                }
            }

        } else if (requestUri.matches("/models/\\d+")) {
            //create a new model version
            try {
                ProcessModel newModel = null;

                String id = parseModelIdFromRequestUri(requestUri);
                if (!lu.isSingleUser()) {
                    throw new AccessViolationException("Access denied for unregistered user");
                }

                AccessType access = modelManager.getAccessForModel(id, -1, (SingleUser) lu);
                if (access.compareTo(AccessType.WRITE) < 0) {
                    throw new AccessViolationException("User " + lu.getName() + " is not allowed to change this model!");
                }

                if (sourceRefHeader != null) {
                    newModel = modelManager.getTemporaryModel(sourceRefHeader).clone();
                } else {
                    newModel = ProcessUtils.parseProcessModelSerialization( RequestUtils.getXML(req) );
                }

                if (newModel != null) {
                    String modelAddr = createNewVersion(requestUri, req, resp, newModel, comment, folder, (SingleUser) lu, precVersions);

                    if (modelAddr == null) {
                        statusCode = 500;
                        response = "<error>Could not create new version</error>";
                    } else {
                        // Set status to 201 (CREATED)
                        statusCode = 201;
                        response = "<url>" + modelAddr + "</url>";
                    }

                    resp.setHeader(HttpConstants.HEADER_KEY_LOCATION, modelAddr);
                } else {
                    throw new Exception("Unable to create model.");
                }
            } catch (Exception e) {
                if (e instanceof ModelNotFoundException) {
                    // Set status code to 404 (NOT FOUND)
                    statusCode = 404;
                } else if (e instanceof AccessViolationException) {
                    statusCode = 403;
                } else {
                    // Set status to 500 (INTERNAL SERVER ERROR)
                    statusCode = 500;
                }
                resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);
                response = "<error>" + e.getMessage() + "</error>";
            }
        } else if (requestUri.matches("/models/\\d+(/versions/\\d+)?/comments(\\?.*)?")) {
            //Create a comment for the specified model
            String id = this.parseModelIdFromRequestUri(requestUri);
            int version = this.parseVersionFromRequestUri(requestUri);
            statusCode = 201;
            try {
                JSONObject jo = new JSONObject();
                ProcessObjectComment poc = this.createComment(id, version, "model", req, lu);
                jo.put("success", true);
                jo.put("comments", poc.toJSON(lu));
                
                ResponseUtils.respondWithJSON(resp, jo, statusCode);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestUri.matches("/models/\\d+(/versions/\\d+)?/nodes/\\d+/comments(\\?.*)?")) {
            //create a comment for the specified node
            String id = this.parseModelIdFromRequestUri(requestUri);
            int version = this.parseVersionFromRequestUri(requestUri);
            statusCode = 201;

            try {
                JSONObject jo = new JSONObject();
                ProcessObjectComment poc = this.createComment(id, version, this.getNodeIdFromUri(requestUri), req, lu);
                jo.put("success", true);
                jo.put("comments", poc.toJSON(lu));
                
                ResponseUtils.respondWithJSON(resp, jo, statusCode);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ResponseUtils.respondWithStatus(statusCode, response, resp, false);
        return;
    }

    @Override
    public void handlePutRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {

        int statusCode = 200;

        String requestUri = req.getRequestURI();
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        String response = "";

        //Parse request headers
        String sourceVersion = req.getHeader("Source-Version");

        Set<String> precVersions = new HashSet<String>();

        if (sourceVersion != null) {
            String[] versions = sourceVersion.split(",");
            for (String v : versions) {
                precVersions.add(v.trim());
            }
        }

        //START REQUEST DISPATCH
        if (requestUri.matches("/models/\\d+(/versions/\\d+)?(/nodes/\\d+)?/comments/\\d+(\\?.*)?")) {
            //Change a comment
            String id = this.parseModelIdFromRequestUri(requestUri);
            String commentId = this.getCommentIdFromUri(requestUri);

            try {
                JSONObject jo = RequestUtils.getJSON(req);
                ProcessObjectComment poc = modelManager.updateComment(id, commentId, jo.getString("text"), jo.getInt("validuntil"));
                
                JSONObject responseJSON = new JSONObject();
                if ( poc != null ) {
                    responseJSON.put("success", true);
                    responseJSON.put("comments", poc.toJSON(lu));
                } else 
                    responseJSON.put("success", false);
                ResponseUtils.respondWithJSON(resp, responseJSON, 200);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestUri.matches("/models/\\d+/access")) {
            //update the access list of this process model
            String id = parseModelIdFromRequestUri(requestUri);

            if (!lu.isSingleUser()) {
                throw new AccessViolationException("Access denied for unregistered user");
            }

            AccessType access = modelManager.getAccessForModel(id, -1, (SingleUser) lu);
            if (access.compareTo(AccessType.OWNER) < 0) {
                throw new AccessViolationException("User " + lu.getName() + " is not allowed to change rights for this model!");
            }

            try {
                Document doc = RequestUtils.getXML(req);

                String method = doc.getDocumentElement().getAttribute("method");
                String type = doc.getDocumentElement().getAttribute("type");

                AccessType at = null;

                if (type.equals("viewers")) {
                    at = AccessType.VIEW;
                } else if (type.equals("editors")) {
                    at = AccessType.WRITE;
                } else if (type.equals("annotators")) {
                    at = AccessType.COMMENT;
                }

                Set<User> users = new HashSet<User>();

                NodeList children = doc.getDocumentElement().getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);

                    Map<String, String> props = XMLHelper.parseProperties(child);

                    if (props.get("type").equals(User.UserType.GROUP.toString())) {
                        users.add(ProcessEditorServerHelper.getUserManager().getGroupForName(props.get("name")));
                    } else if (props.get("type").equals(User.UserType.SINGLE_USER.toString())) {
                        users.add(ProcessEditorServerHelper.getUserManager().getUserForName(props.get("name")));
                    }
                }

                if (method.equals("add")) {
                    modelManager.grantRight(id, at, users);
                } else if (method.equals("delete")) {
                    modelManager.divestRight(id, at, users);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestUri.matches("/models/(\\d+)/meta")) {
            //change meta datum
            if (!lu.isSingleUser()) {
                throw new AccessViolationException("Access denied for unregistered user");
            }

            String id = this.parseModelIdFromRequestUri(requestUri);
            AccessType access = modelManager.getAccessForModel(id, -1, lu);

            if (access.compareTo(AccessType.WRITE) < 0) {
                throw new AccessViolationException("User " + lu.getName() + " is not allowed to change this model!");
            }

            try {
                Document doc = RequestUtils.getXML(req);
                String type = doc.getDocumentElement().getAttribute("type");
                Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                //update folder or owner
                if (type.equals("folder")) {
                    String newFolder = props.get("folder") != null ? props.get("folder") : "/";
                    modelManager.setFolderAlias(id, newFolder, (SingleUser) lu);
                } else if (type.equals("owner")) {
                    if (!lu.isAdmin()) {
                        throw new AccessViolationException("User " + lu.getName() + " is not allowed to change the owner of this model!");
                    }

                    String newOwner = props.get("owner") != null ? props.get("owner") : "root";
                    SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(newOwner);

                    if (u == null) {
                        statusCode = 500;
                    } else {
                        modelManager.setOwner(id, u, (SingleUser) lu);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);

        ResponseUtils.respondWithStatus(statusCode, response, resp, false);
        return;
    }

    @Override
    public void handleDeleteRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        String response = "";
        String requestUri = req.getRequestURI();
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        //no deletion of single versions
        if (requestUri.matches("/models/\\d+")) {
            if (!lu.isSingleUser()) {
                throw new AccessViolationException("Access denied for unregistered user");
            }
            ServerModel currentLM = retrieveProcessModel(requestUri, -1);
            if (currentLM == null) {
                ResponseUtils.respondWithStatus(404, "Model not found", resp, true);
                return;
            }
            /*
             * move model to 'attic' folder
             * if model is already in 'attic' remove it physically
             */
            String id = parseModelIdFromRequestUri(requestUri);

            AccessType access = modelManager.getAccessForModel(id, -1, lu);
            if (access.compareTo(AccessType.OWNER) < 0) {
                throw new AccessViolationException("User " + lu.getName() + " is not allowed to delete this model!");
            }

            response = "<delete>" + modelManager.removePersistentModel(id, (SingleUser) lu) + "</delete>";
        } else if (requestUri.matches("/models/\\d+(/versions/\\d+)?(/nodes/\\d+)?/comments/\\d+(\\?.*)?")) {
            //delete a comment
            String mid = this.parseModelIdFromRequestUri(requestUri);
            String cid = this.getCommentIdFromUri(requestUri);
            modelManager.deleteComment(mid, cid);
        }

        ResponseUtils.respondWithStatus(200, response, resp, false);
    }

    private ProcessModel createModelFromRequest(String sourceRefHeader, String newName, RequestFacade req ) throws Exception {
        ProcessModel newModel = null;
        if (sourceRefHeader != null) {
            newModel = modelManager.getTemporaryModel(sourceRefHeader).clone();

            newModel.setProcessName(newName);
        } else {
            newModel = ProcessUtils.parseProcessModelSerialization( RequestUtils.getXML(req) );
        }
        return newModel;
    }

    private String createNewModel(String sourceRefHeader, String newName, String comment, String folder, RequestFacade req, ResponseFacade resp, SingleUser user) throws Exception {
        String response;
        ProcessModel newModel = createModelFromRequest(sourceRefHeader, newName, req);

        if (newModel == null)
            throw new Exception("No model send or referred in header");

        //import model to working directory
        File f = new File(ProcessEditorServerHelper.TMP_DIR + "/" + newModel.getId() + ".model");
        exporter.serialize(f, newModel);
        String key = modelManager.addPersistentModel(f, comment, folder, user);
        f.delete();

        // Write meta data
        String modelId = key.substring(key.lastIndexOf("/") + 1);

        //create response
        String editorAddr = getAbsoluteAddressPrefix( req ) + "/editor?id=" + modelId;
        String modelAddr = getAbsoluteAddressPrefix( req ) + "/models/" + modelId + "/versions/0";

        if (req.getHeader(HttpConstants.HEADER_KEY_ACCEPT).contains(HttpConstants.CONTENT_TYPE_TEXT_XML)) {
            resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);
            resp.setHeader(HttpConstants.HEADER_KEY_LOCATION, modelAddr);
            response = "<url>" + modelAddr + "</url>";
        } else {
            resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_HTML);
            resp.setHeader(HttpConstants.HEADER_KEY_LOCATION, editorAddr);
            response = "{success:true, url:'" + editorAddr + "'}";
        }

        return response;
    }

    /**
     * Create a list of all registered models with their names und URIs
     * @return XML-list of models
     */
    protected Document createModelList(String prefix, SingleUser user) {
        Document doc = XMLHelper.newDocument();
        Element modelsEl = XMLHelper.addDocumentElement(doc, "models");

        // Force reload of index
        //modelManager.reloadIndex();

        Map<String, AccessType> recentVersions = modelManager.getRecentVersions(user);
        for (String id : recentVersions.keySet()) {
            VersionMetaData vmd = modelManager.getRecentMetaData(id);

            Element modelEl = XMLHelper.addElement(doc, modelsEl, "model");
            Element nameEl = XMLHelper.addElement(doc, modelEl, "name");
            nameEl.setTextContent(vmd.getProcessName());
            Element uriEl = XMLHelper.addElement(doc, modelEl, "uri");
            uriEl.setTextContent(prefix + "/models/" + id);
            Element folderEl = XMLHelper.addElement(doc, modelEl, "folderalias");
            folderEl.setTextContent(vmd.getFolder());
        }

        return doc;
    }

    /**
     * @todo currently broken
     * Create an RSS feed with the last 10 process models.
     * @return XML-list of models
     */
    protected String createRSSFeed(SingleUser user) {
        //(XML-Hack!!!) will be fixed, when it works again
        StringBuffer builder = new StringBuffer(300);
        builder.append("<rss version='2.0'>");
        builder.append("<channel>\n");
        builder.append("<title>ProcessModel_Server Feed</title>");
        builder.append("<link>/models/rss</link>");
        builder.append("<description>List of ProcessModels</description>");
        builder.append("<copyright>Copyright 2009-2010, inubit AG</copyright>");
        builder.append("<generator>ProcessEditor_Server</generator>");
        try {
            //ProcessEditorImporter importer = new ProcessEditorImporter();
            for (Map.Entry<String, AccessType> modelVersion : modelManager.getRecentVersions(user).entrySet()) {
                //take first because it definitely exists and all versions have same name and base URI
                ProcessModel model = modelManager.getRecentVersion(modelVersion.getKey()).getModel();
                builder.append("<item>\n");
                builder.append("<title>" + model.getProcessName() + "</title>\n");
                builder.append("<link>/editor?id=" + modelVersion.getKey() + "</link>\n");
                builder.append("<description><table border='0'><tr><td valign='top'><a href='/editor?id=");
                builder.append(modelVersion.getKey());
                builder.append("'><img src='/models/");
                builder.append(modelVersion.getKey());
                builder.append("/preview'/></a></td><td valign='top'>Comment: ");
                builder.append(model.getProperty(ProcessModel.PROP_COMMENT));
                builder.append("</td></tr></table></description>");
                builder.append("<author>" + model.getProperty(ProcessModel.PROP_AUTHOR) + "</author>");
                builder.append("<pubDate>" + model.getCreationDate() + "</pubDate>");
                builder.append("</item>\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.append("</channel>");
        builder.append("</rss>\n");
        return builder.toString();
    }

    private String createNewVersion(String requestUri, RequestFacade req, ResponseFacade resp, ProcessModel newModel,
            String comment, String folder, SingleUser user, Set<String> precVersions ) throws Exception, ModelNotFoundException {
        

        String modelId = parseModelIdFromRequestUri(requestUri);
        int version = parseVersionFromRequestUri(requestUri);

        //check if model exists
        int count = modelManager.getPersistentVersionCount(modelId);
        if (count == 0) {
            throw new ModelNotFoundException("Model " + requestUri + " not found on server.");
        }

        String newName = ProcessEditorServerUtils.unEscapeString(req.getHeader("Commit-Name"));

        newModel.setProcessName(newName);
        newModel.setProcessModelURI(requestUri);

        int newVersionNr = modelManager.saveModel(newModel, modelId, version, comment, folder, precVersions, user) - 1;


        resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);

        if (newVersionNr < 0) {
            return null;
        }

        String modelAddr = getAbsoluteAddressPrefix(req) + "/models/" + modelId + "/versions/" + newVersionNr;
        resp.setHeader(HttpConstants.HEADER_KEY_LOCATION, modelAddr);


        return modelAddr;
    }

    private void createPreviewPNGGraphics(ProcessModel model, RequestFacade req, ResponseFacade resp) {
        int size = 128;
        try {
            Map<String, String> queryPar = RequestUtils.getQueryParameters(req);
            if (queryPar.get("size") != null) {
                size = Integer.parseInt(queryPar.get("size"));
            }
        } catch (Exception ex) {
        }
        try {
            BufferedImage img = ProcessModelPreview.createStyledPreview(model, size);
            ResponseUtils.respondWithImage(resp, img);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the requested process model by its ID given by the URI
     * @param requestUri URI path
     *
     * @return if exists/found, return the model, else return null
     */
    private ServerModel retrieveProcessModel(String requestUri, int version) {
        final Pattern modelRequest = Pattern.compile("/models/(\\d+)");
        final Matcher m = modelRequest.matcher(requestUri);
        if (m.find()) {
            ServerModel model = modelManager.getPersistentModel(m.group(1), version);

            if (model != null && version > -1) {
                model.getModel().setProcessModelURI("/models/" + m.group(1) + "/versions/" + version);
            }

            return model;
        }

        return null;
    }

    public static File importModel(File m) throws Exception {
        ProcessModel model = null;
        model = ConverterHelper.importModels(m).get(0);
        if (model != null) {
            File newModel = new File(ProcessEditorServerHelper.TMP_DIR + "/" + model.getProcessName() + ".model");
            exporter.serialize(newModel, model);

            return newModel;
        }

        return null;
    }

    private String createVersionList(String prefix, String requestUri) {
        requestUri = requestUri.replaceAll("/versions.*", "");

        StringBuffer b = new StringBuffer(300);
        String addr = prefix + requestUri;
        String modelId = parseModelIdFromRequestUri(requestUri);

        int count = modelManager.getPersistentVersionCount(modelId);
        b.append("<versions>\n");

        for (int i = 0; i < count; i++) {
            // Retrieve comment
            String comment = modelManager.getVersionComment(modelId, i);
            String user = modelManager.getVersionUser(modelId, i);

            Set<String> prec = modelManager.getPreceedingVersions(modelId, i);
            Set<String> succ = modelManager.getSucceedingVersions(modelId, i);
            b.append("<version id='" + i + "'>\n");
            b.append("      <uri>" + addr + "/versions/" + i + "</uri>");
            if (comment != null) {
                b.append("<comment>" + comment + "</comment>");
            }
            if (user != null) {
                b.append("<user>" + user + "</user>");
            }
            if (prec != null) {
                b.append("<predecessors>");
                b.append(prec.toString().replace("[", "").replace("]", ""));
                b.append("</predecessors>");
            }

            if (succ != null) {
                b.append("<successors>");
                b.append(succ.toString().replace("[", "").replace("]", ""));
                b.append("</successors>");
            }


            b.append("</version>\n");
        }

        b.append("</versions>");

        return b.toString();
    }

    private String createAccessList(String id) {
        String owner = modelManager.getOwner(id);
        Set<User> viewers = modelManager.getViewers(id);
        Set<User> annotators = modelManager.getAnnotators(id);
        Set<User> editors = modelManager.getEditors(id);

        StringBuffer b = new StringBuffer(300);

        b.append("<access>");
        b.append("<owner>" + owner + "</owner>");
        b.append("<viewers>");
        for (User u : viewers) {
            b.append("<viewer>");
            b.append("<property name='name' value='" + u.getName() + "'/>");
            b.append("<property name='type' value='" + u.getUserType() + "'/>");
            b.append("</viewer>");
        }
        b.append("</viewers>");

        b.append("<annotators>");
        for (User u : annotators) {
            b.append("<annotator>");
            b.append("<property name='name' value='" + u.getName() + "'/>");
            b.append("<property name='type' value='" + u.getUserType() + "'/>");
            b.append("</annotator>");
        }
        b.append("</annotators>");

        b.append("<editors>");
        for (User u : editors) {
            b.append("<editor>");
            b.append("<property name='name' value='" + u.getName() + "'/>");
            b.append("<property name='type' value='" + u.getUserType() + "'/>");
            b.append("</editor>");
        }
        b.append("</editors>");

        b.append("</access>");

        return b.toString();
    }

    private String importFromMultiPart(RequestFacade req, SingleUser user) throws IOException {
        File newModel = RequestUtils.parseMultiPartItemIntoTmpFile(req, "uploadfile");
        
        try {
            File f = importModel(newModel);
            String id = modelManager.addPersistentModel(f, "imported", "/", user);
            String addr = getAbsoluteAddressPrefix(req) + "/models/" + id;

            return "{success:true, url:'" + addr + "'}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{success:false}";
        }
    }

    private Document createOverviewXML( String context , SingleUser user) {
        try {
            Document doc = XMLHelper.newDocument();
            XMLHelper.addDocumentElement(doc, "modeloverview");
            // Force reload of index
            //modelManager.reloadIndex();

            Map<String, AccessType> models = modelManager.getRecentVersions(user);
            for (Map.Entry<String, AccessType> e : models.entrySet()) {
                Properties props = new Properties();
                String addr = (String) e.getKey();
                String number = addr.substring(addr.lastIndexOf("/") + 1);
                String folderAlias = modelManager.getFolderAlias(number);
                if (e.getValue().compareTo(AccessType.OWNER) < 0) {
                    folderAlias = Location.SHARED_PATH_PREFIX + folderAlias;
                }

                //ProcessModel model = modelManager.getRecentVersion(e.getKey()).getModel();
//                ServerModel lm = modelManager.getRecentVersion(e.getKey());
                VersionMetaData vmd = modelManager.getRecentMetaData(e.getKey());

                // Check if model is acutally loaded
                if (vmd==null) continue;

                props.setProperty("editor", context + "/models/" + number);
                props.setProperty("image", context + "/models/" + number + "/preview");
                
                props.setProperty("name", vmd.getProcessName());

                if (vmd.getAuthor() != null) {
                    props.setProperty("autor", vmd.getAuthor());
                }
                if (vmd.getCreationDate() != null) {
                    props.setProperty("created", vmd.getCreationDate());
                }
                if (vmd.getLastUpdateDate() != null) {
                    props.setProperty("modified", vmd.getLastUpdateDate());
                }

                props.setProperty( "model", context + "/models/" + number );
                props.setProperty( "folder", folderAlias );
                props.setProperty( "owner", modelManager.getOwner(number) == null ? "root" : modelManager.getOwner(number) );
                props.setProperty( "access", e.getValue().toString() );

                Element modelElement = XMLHelper.addElement(doc, doc.getDocumentElement(), "model");
                XMLHelper.addPropertyList(doc, modelElement, props);
            }

            addFolderStructure(user, doc, doc.getDocumentElement());
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}