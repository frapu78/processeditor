/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import java.net.URI;
import java.net.URL;

/**
 *
 * @author fel
 */
public class HttpConstants {

    public final static String ERROR_404 = "Not Found";

    public final static String HEADER_KEY_ACCEPT = "Accept";
    public final static String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public final static String HEADER_KEY_ACCEPT_ENCODING = "Accept-Encoding";
    public final static String HEADER_KEY_LOCATION = "Location";
    public final static String HEADER_KEY_CONTENT_ENCODING = "Content-Encoding";
    public final static String HEADER_KEY_COOKIE = "Cookie";
    public final static String HEADER_KEY_CREATE_TEMP_MODEL = "Model-Source";

    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";
    public final static String CONTENT_TYPE_TEXT_XML = "text/xml";
    public final static String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public final static String CONTENT_TYPE_TEXT_JAVASCRIPT = "text/javascript";
    public final static String CONTENT_TYPE_TEXT_CSS = "text/css";
    public final static String CONTENT_TYPE_APPLICATION_XSD = "application/x-xsd+xml";
    public final static String CONTENT_TYPE_APPLICATION_PDF = "application/pdf";
    public final static String CONTENT_TYPE_APPLICATION_XPDL = "application/xpdl";
    public final static String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public final static String CONTENT_TYPE_APPLICATION_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public final static String CONTENT_TYPE_APPLICATION_PROCESSMODEL = "application/xml+model";
    public final static String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public final static String CONTENT_TYPE_IMAGE_PNG = "image/png";
    public final static String CONTENT_TYPE_IMAGE_GIF = "image/gif";
    public final static String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
    public final static String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    public final static String CONTENT_CODING_GZIP = "x-gzip";

    public final static String REQUEST_POST = "POST";
    public final static String REQUEST_PUT = "PUT";
    public final static String REQUEST_GET = "GET";
    public final static String REQUEST_DELETE = "DELETE";

    

    public static final String ATTRIBUTE_KEY_NAME = "name";
    public static final String ELEMENT_PROPERTY = "property";
    public static final String ATTRIBUTE_KEY_VALUE = "value";
    public static final String ATTRIBUTE_KEY_UPDATE_TYPE = "type";
    public static final String ELEMENT_UPDATE = "update";
    public final static String ELEMENT_UPDATE_METHOD_PROPERTY = "property";
    public final static String ELEMENT_UPDATE_METHOD_RESIZE = "resize";
    public final static String ELEMENT_UPDATE_METHOD_POSITION = "position";
    public final static String ELEMENT_UPDATE_METHOD_TYPE = "type";
    public final static String ELEMENT_UPDATE_METHOD_CLUSTER = "cluster";
    public final static String ELEMENT_UPDATE_METHOD_ATTACH = "attach";
    public final static String ELEMENT_UPDATE_METHOD_DETACH = "detach";
    public final static String ELEMENT_UPDATE_METHOD_LANE = "lane";
    public final static String ELEMENT_UPDATE_METHOD_ADDLANE = "addLane";

    public static final String TAG_GET_TEMP_MODEL_URI_RESPONSE = "uri";

    public final static String FOLDER_MODELS_ALIAS = "/models";
    public final static String FOLDER_TEMP_ALIAS = "/tmp";
    public final static String FOLDER_NODES_ALIAS = "/nodes";
    public final static String FOLDER_EDGES_ALIAS = "/edges";




   
    /**
     * "http://novplex:1205/models"
     * @param server
     * @return
     */
    public static String getModelsDirectory(String server) {
        return server + FOLDER_MODELS_ALIAS;
    }

    /**
     * "http://novplex:1205/models/tmp"
     * @param server
     * @return
     */
    public static String getTempModelsDirectory(String server) {
        return getModelsDirectory(server) + FOLDER_TEMP_ALIAS;
    }

    /**
     * "http://novplex:1205/models/tmp/123/nodes"
     * @param server
     * @param modelID
     * @return
     */
    public static String getTempNodesDirectory(String server, String modelID) {
        return getTempModelsDirectory(server) + FOLDER_NODES_ALIAS;
    }

    /**
     * "http://novplex:1205/models/tmp/123/edges"
     * @param server
     * @param modelID
     * @return
     */
    public static String getTempEdgesDirectory(String server, String modelID) {
        return getModelsDirectory(server) + FOLDER_EDGES_ALIAS;
    }

    /**
     * "http://novplex:1205/models/tmp/123"
     * @param server
     * @param modelID
     * @return
     */
    public static String getTempModelURL(String server, String modelID) {
        return getTempModelsDirectory(server) + "/" + modelID;
    }

    /**
     * "http://novplex:1205/models/tmp/123/nodes/123"
     * @param server
     * @param modelID
     * @param objectID
     * @return
     */
    public static String getTempNodeURL(String server, String modelID, String objectID) {
        return getTempNodesDirectory(server, modelID) + "/" + objectID;
    }

    /**
     * "http://novplex:1205/models/tmp/123/edges/123"
     * @param server
     * @param modelID
     * @param objectID
     * @return
     */
    public static String getTempEdgeURL(String server, String modelID, String objectID) {
        return getTempEdgesDirectory(server, modelID) + "/" + objectID;
    }

    /**
     * "http://novplex:1205/models/tmp/123/nodes/123/png"
     * @param server
     * @param modelID
     * @param objectID
     * @return
     */
    public static String getTempNodeImageURL(String server, String modelID, String objectID) {
        return getTempNodeURL(server, modelID, objectID) + "/png";
    }

    public static String getBaseDirectory(URI modelURI) {
            String uriString = modelURI.toString();
            String path = modelURI.getPath();
            if (path.equals("")) return uriString;
            int delIndex = uriString.lastIndexOf(FOLDER_MODELS_ALIAS);
            StringBuilder b = new StringBuilder(uriString);
            b.delete(delIndex, uriString.length());
            return b.toString();
    }

}
