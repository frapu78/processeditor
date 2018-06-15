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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.frapu.code.converter.*;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

import org.w3c.dom.Document;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.MonitoringUtils;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.config.DirectoryConfig;
import com.inubit.research.server.manager.Location;
import com.inubit.research.server.manager.Location.LocationType;
import com.inubit.research.server.manager.MetaCache;
import com.inubit.research.server.manager.ModelManager;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.request.handler.util.ProcessEdgeUtils;
import com.inubit.research.server.request.handler.util.ProcessModelUtils;
import com.inubit.research.server.request.handler.util.ProcessObjectUtils;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import java.util.Map;
import java.util.Properties;

import net.frapu.code.visualization.domainModel.DomainModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

/**
 * @author fpu
 * @author fel
 *
 * Abstract base class for all handlers that handle model requests
 */
public abstract class ModelRequestHandler extends AbstractRequestHandler {
    private static Set<String> types;
    static {
        String[] _types = {"pdf", "json", "png", "xpdl", "pm", "xsd", "edit", "view", "edit_version", "view_version", "edit_tmp"};
        File dir = new File(ProcessEditorServerHelper.TMP_DIR);
        if (!dir.exists())
            dir.mkdirs();

        types = new HashSet<String>(Arrays.asList(_types));
    }

    //model manager
    protected static ModelManager modelManager = ModelManager.getInstance();

    protected long startTime = 0;
    protected static MonitoringUtils mu = MonitoringUtils.getInstance();

//    public ModelHandler(InetSocketAddress ad) {
//        super(true);
//        address = ad;
//    }
//
//    protected  ModelHandler() {
//        super(true);
//    }

//    @Override
//    public void handle(HttpExchange t) {
////        String requestUri = t.getRequestURI().toASCIIString();
//        startTime = System.nanoTime();
//        super.handle(t);
//    }

    /**
     * Parse model id from request URI
     * @param requestUri the request URI
     * @return  if found, the id, else null
     */
    public String parseModelIdFromRequestUri(String requestUri) {
        Pattern p = Pattern.compile("/models/(tmp/)?(\\d+)");
        Matcher m = p.matcher(requestUri);

        if (m.find()) {
            return m.group(2);
        }

        return null;
    }

    /**
     * Get the requested version from the request-URI
     * @param requestUri requested URI
     * @return the version. If no version was given or it was a negative number -1 is returned
     */
    protected int parseVersionFromRequestUri(String requestUri) {
        Pattern p = Pattern.compile("/models/\\d+/versions/(\\d+)");
        Matcher m = p.matcher(requestUri);

        if (m.find()) {
            int version = Integer.parseInt(m.group(1));

            return version < 0 ? -1 : version;
        }

        return -1;
    }

    /**
     * Create a list of all nodes belonging to the current model
     * @param currentModel the current process model
     * @param prefix the base address of server
     * @return XML-list of nodes
     */
    protected Document createNodeList(ProcessModel currentModel, String prefix) {
        ProcessModelUtils utils = new ProcessModelUtils(currentModel);
        return utils.getNodeList(prefix);
    }

     /**
     * Create an XML-list of all edges contained in the given model
     * @param currentModel the current model
     * @param prefix the base address of server
     * @return XML-list of all edges
     */
    protected Document createEdgeList(ProcessModel currentModel, String prefix) {
         ProcessModelUtils utils = new ProcessModelUtils(currentModel);
         return utils.getEdgeList(prefix);
    }

    /**
     * Get the node ID from the given uri
     * @param requestUri the uri
     * @return if found, return the node ID, else return null
     */
    protected String getNodeIdFromUri(String requestUri) {
        final Pattern nodeRequest = Pattern.compile("/nodes/(\\d+)");
        final Matcher m = nodeRequest.matcher(requestUri);

        if (m.find()) {
           return m.group(1);
        }

        return null;
    }

    protected String getCommentIdFromUri( String requestUri ) {
        final Pattern commentRequest = Pattern.compile("/comments/(\\d+)");
        final Matcher m = commentRequest.matcher(requestUri);

        if (m.find()) {
           return m.group(1);
        }

        return null;
    }

    /**
     * Retrieve the requested node by model and node ID given by the URI
     * @param requestUri URI path
     * @return if exists/found, returns the node, else returns null
     */
    protected ProcessNode retrieveProcessNode(String requestUri, ProcessModel currentModel) {
        String nodeId = getNodeIdFromUri(requestUri);
        return nodeId != null ? currentModel.getNodeById(nodeId) : null;
    }

      /**
     * Retrieve the edge queried by the URI path
     * @param requestUri the URI path
     * @param currentModel the current model
     * @return if found/exists, return the edge, else return null
     */
    protected ProcessEdge retrieveProcessEdge(String requestUri, ProcessModel currentModel) {
        final Pattern edgeRequest = Pattern.compile("/edges/(\\d+)");
        final Matcher m = edgeRequest.matcher(requestUri);

        if (m.find()) {
            for (ProcessEdge edge : currentModel.getEdges()) {
                if (edge.getId().equals(m.group(1))) {
                    return edge;
                }
            }
        }

        return null;
    }

    protected Document serializeObject(ProcessObject object) {
        if (object != null) {
            ProcessObjectUtils utils = new ProcessObjectUtils(object);

            try {
                return utils.serialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Create XML representation of the given model's meta data
     * @param currentModel the current model
     * @return meta data as XML
     */
    protected Document createModelMetaData(ProcessModel currentModel, String requestUri, AccessType access) {
        String id = this.parseModelIdFromRequestUri(requestUri);
        ProcessModelUtils utils = new ProcessModelUtils(currentModel);

        String folder = modelManager.getFolderAlias(id);
        return utils.getMetaXML(id, folder, access);
    }

    protected void retrieveCorrectRepresentation( ProcessModel model, AccessType access, String requestUri, RequestFacade req, ResponseFacade resp ) throws IOException {
        String type = null;

        if (requestUri.matches(".+\\.(png|json|pdf|xpdl|xsd|pm)\\z")) {
            type = this.getTypeFromRequestUri( requestUri, model );
        } else {
            type = this.getTypeFromRequestHeaders( req, access );
        }

        if ( (type.equals("edit") || type.equals("view")) ) {
            if ( requestUri.contains("/versions/") )
                type += "_version";
            else if ( requestUri.contains("/tmp/") )
                type += "_tmp";
        }

        this.respondWithRepresentation(type, model, resp);
    }

    protected void respondWithRepresentation(String type, ProcessModel model, ResponseFacade resp) throws IOException {
        String response = "";
        int status = 404;
        
        if (type==null) {
            // Set type to pm
            type = "pm";
        }

        if (types.contains(type)) {
            if (type.equals("png")) {
                createModelPNGGraphics(model, resp);
                return;
            } else if (type.equals("json")) {
                createModelJSON(model, resp);
                return;
            } else if (type.equals("pdf")) {
                createModelPDF(model, resp);
                return;
            } else if (type.equals("xpdl")) {
                createModelXPDL(model, resp);
                return;
            } else if (type.equals("xsd")) {
                createModelXSD(model, resp);
                return;
            } else if (type.equals("pm")) {
                this.respondWithModel(model, resp);
                return;
            } else if (type.equals("edit")) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessEditorRaphael.html", resp);
                return;
            } else if (type.equals("view")) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessViewer.html", resp);
                return;
            } else if (type.equals("edit_version")) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessEditorRaphael_version.html", resp);
                return;
            } else if (type.equals("view_version")) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessViewer_version.html", resp);
                return;
            } else if ( type.equals("edit_tmp") ) {
                ResponseUtils.respondWithNegotiatedServerResource(HttpConstants.CONTENT_TYPE_TEXT_HTML, "/html/ProcessEditorRaphael_tmp.html", resp);
                return;
            }

        } else {
            response = "Requested type not found";
        }         
        ResponseUtils.respondWithStatus(status, response, resp, false);
     }

     protected void createModelPNGGraphics(ProcessModel model, ResponseFacade resp) throws IOException {
        try {
            BufferedImage img = ProcessEditorServerUtils.createModelImage(model);
            ResponseUtils.respondWithImage(resp, img);
            return;
        } catch (Exception e) {
            ResponseUtils.respondWithStatus(500, e.getMessage(), resp, true);
        }
     }

     protected void createNodePNGGraphics(String nodeId, ProcessModel model, ResponseFacade resp ) {
        try {
            ProcessNode node = model.getNodeById(nodeId);

            BufferedImage img = ProcessEditorServerUtils.createNodeImage(node);

            ResponseUtils.respondWithImage(resp, img);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createModelJSON(ProcessModel model, ResponseFacade resp) throws IOException {
        try {
            File f = new File(ProcessEditorServerHelper.TMP_DIR + File.separator + model.getId() + ".json");
            JSONExporter ex = new JSONExporter();

            ex.serialize(f, model);
            ResponseUtils.respondWithFile(HttpConstants.CONTENT_TYPE_APPLICATION_JSON, f, resp);
        } catch (Exception e) {
            ResponseUtils.respondWithStatus(500, e.getMessage(), resp, true);
        }
    }

     protected void createModelPDF(ProcessModel model, ResponseFacade resp) throws IOException {
         try {
             File f = new File(ProcessEditorServerHelper.TMP_DIR + File.separator + model.getId() + ".pdf");
             PDFExporter ex = new PDFExporter();

             ex.serialize(f, model);
             ResponseUtils.respondWithFile(HttpConstants.CONTENT_TYPE_APPLICATION_PDF, f, resp);
         } catch (Exception e) {
             ResponseUtils.respondWithStatus(500, e.getMessage(), resp, true);
         }
     }

     protected void createModelXPDL( ProcessModel model, ResponseFacade resp ) throws IOException {
        try {
            File f = new File(ProcessEditorServerHelper.TMP_DIR + File.separator + model.getId() + ".xpdl");
            XPDLExporter ex = new XPDLExporter();

            ex.serialize(f, model);
            ResponseUtils.respondWithFile(HttpConstants.CONTENT_TYPE_TEXT_XML, f, resp);
        } catch (Exception e) {
            ResponseUtils.respondWithStatus(500, e.getMessage(), resp, true);
        }
     }

     protected void createModelXSD( ProcessModel model, ResponseFacade resp ) throws IOException {
        try {
            File f = new File(ProcessEditorServerHelper.TMP_DIR + "/temp/temp" + model.getId() + ".xsd");
            Exporter ex;
            if ( model instanceof DomainModel )
                ex = new XSDCreator();
            else
                ex = new XSDExporter();

            ex.serialize(f, model);

            ResponseUtils.respondWithFile(HttpConstants.CONTENT_TYPE_TEXT_XML, f, resp );
        } catch (Exception e) {
            if (e.getMessage().equals( XSDExporter.ERROR_NO_DATA)) {
                ResponseUtils.respondWithStatus(406, "Requested content type not available for resource.", resp, true);
            } else {
                e.printStackTrace();
                ResponseUtils.respondWithStatus(500, e.getMessage(), resp, true);
            }
        }
     }
     
     protected JSONArray createCommentList(String id, int version, String elementId , LoginableUser user) {
        Set<ProcessObjectComment> comments = modelManager.getComments(id, version, elementId);
        ProcessObjectComment[] sortedComments = comments.toArray(new ProcessObjectComment[0]);
        Arrays.sort(sortedComments);
        JSONArray ja = new JSONArray();
        
        for ( ProcessObjectComment comment : sortedComments ) 
            ja.put(comment.toJSON(user));

        return ja;
     }
     
//     protected Document createCommentList(String id, int version, String elementId , LoginableUser user ) {
//        Set<ProcessObjectComment> comments = modelManager.getComments(id, version, elementId);
//        ProcessObjectComment[] sortedComments = comments.toArray(new ProcessObjectComment[0]);
//        Arrays.sort(sortedComments);
//
//        Document doc = XMLHelper.newDocument();
//        Element docEl = XMLHelper.addDocumentElement(doc, "comments");
//        for ( ProcessObjectComment comment : sortedComments ) {
//            comment.addToXML(user, doc, docEl);
//        }
//
//        return doc;
//    }
     
    protected ProcessObjectComment createComment( String modelId, int version, String elementId, RequestFacade req, LoginableUser lu ) throws Exception {
        JSONObject jo = RequestUtils.getJSON(req);
        String text = ProcessEditorServerUtils.unEscapeString( jo.getString("text") );
        
        if (version == -1)
            version = modelManager.getPersistentVersionCount(modelId) - 1;
        
        return this.createComment(modelId, version, elementId, lu, text);
    }
    
    protected ProcessObjectComment createComment( String id, int version, String elementId, LoginableUser user, String text) {
        ProcessObjectComment poc = new ProcessObjectComment(elementId, user.getName(), version, text);
        modelManager.addComment( id, poc );
        return poc;
     }
     
//     protected Document createComment( String modelId, int version, String elementId, RequestFacade req, LoginableUser lu ) throws Exception {
//        Document doc = RequestUtils.getXML(req);
//
//        Node textNode = doc.getElementsByTagName("text").item(0);
//
//        if (version == -1)
//            version = modelManager.getPersistentVersionCount(modelId) - 1;
//
//        String text = ProcessEditorServerUtils.unEscapeString(textNode.getTextContent());
//        return this.createComment(modelId, version, elementId, lu, text);
//    }
//
//     protected Document createComment( String id, int version, String elementId, LoginableUser user, String text) {
//        ProcessObjectComment poc = new ProcessObjectComment(elementId, user.getName(), version, text);
//
//        modelManager.addComment( id, poc );
//        Document doc = XMLHelper.newDocument();
//        poc.addToXML( user, doc, null );
//        return doc;
//     }

     protected void createEdgeLabelPNGGraphics(ProcessEdge currentEdge, ResponseFacade resp) throws IOException {
        ProcessEdgeUtils utils = new ProcessEdgeUtils(currentEdge);
        BufferedImage img = utils.getLabelPNGGraphics(); 

        ResponseUtils.respondWithImage(resp, img);
    }

     protected void addFolderStructure( SingleUser user , Document doc, Element parent ) {
        Element structureEl = XMLHelper.addElement(doc, parent, "structure");

        Map<String, LocationType> structure = modelManager.listLocations(user);
        String home = modelManager.getHomePath(user);

        Element homeEl = XMLHelper.addElement( doc, structureEl, "home");
        homeEl.setTextContent(home);

        Element isEl = XMLHelper.addElement( doc, structureEl, "isroot");
        isEl.setTextContent("/is");

        Element sharedEl = XMLHelper.addElement( doc, structureEl, "shared");
        sharedEl.setTextContent(Location.SHARED_PATH_PREFIX + "/" + DirectoryConfig.USER_HOME_ROOT_PATH);

        Element trashEl = XMLHelper.addElement( doc, structureEl, "trash");
        trashEl.setTextContent(home + MetaCache.ATTIC_FOLDER_NAME);

        for (Map.Entry<String, LocationType> e : structure.entrySet()) {
            Element folderEl = XMLHelper.addElement(doc, structureEl, "folder");
            Properties props = new Properties();
            props.setProperty("path", e.getKey());
            props.setProperty("type", e.getValue()!=null?e.getValue().toString():"");

            XMLHelper.addPropertyList(doc, folderEl, props);
        }
     }

     protected void respondWithModel( ProcessModel model, ResponseFacade resp ) {
        try {
            Document doc = model.getSerialization();
            ResponseUtils.respondWithXML(resp, doc, 200);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

     private String getTypeFromRequestHeaders( RequestFacade req , AccessType access ) {
        String type = null;

        String primaryAccept =
                    req.getHeader(HttpConstants.HEADER_KEY_ACCEPT).split(",")[0];

        if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_APPLICATION_PROCESSMODEL))
            type = "pm";
        else if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_APPLICATION_JSON))
            type = "json";
        else if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_APPLICATION_XSD))
            type = "xsd";
        else if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_APPLICATION_PDF)) 
            type = "pdf";
        else if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_APPLICATION_XPDL))
            type = "xpdl";
        else if (primaryAccept.equals(HttpConstants.CONTENT_TYPE_IMAGE_PNG))
            type = "png";
        else {
            if (access.compareTo(AccessType.WRITE) >= 0)
                type = "edit";
            else if (access.compareTo(AccessType.NONE) > 0)
                type = "view";
        }

        return type;
     }

     private String getTypeFromRequestUri( String requestUri , ProcessModel model ) {
        String type = null;
        Pattern p = Pattern.compile("\\.(.+?)\\z");
        Matcher m = p.matcher(requestUri);

        if (m.find() && model != null) 
            type = m.group(1);
        else if ("edit".equals(type) || "view".equals(type))
            type = "pm";

        return type;
     }

    public static String getAbsoluteAddressPrefix(RequestFacade req) {
        //the second "/" is contained in t.getLocalAddress()
        String prefix = req.getProtocol() + "://" + req.getLocalAddress();
        return prefix;
    }

    static ModelManager getManager() {
        return modelManager;
    }
}