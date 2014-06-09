/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import com.inubit.research.gui.WorkbenchHelper;
import com.inubit.research.server.*;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;

import net.frapu.code.visualization.*;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;
import net.frapu.code.visualization.editors.PropertyEditor;
import net.frapu.code.visualization.editors.PropertyEditorType;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handler for utility requests
 * @author fel
 */
public class UtilsRequestHandler extends AbstractRequestHandler{
    private static ImageStore edgeShapes = new ImageStore();

    @Override
    public void handleGetRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        String response = "";
        String requestUri = req.getRequestURI();
        int status = 200;

        if (requestUri.matches("/utils/modeltypes")) {
            //list all supported model types
            StringBuilder b = new StringBuilder(300);
            b.append("<modelclasses>");
            List<Class<? extends ProcessModel>> modelClasses = WorkbenchHelper.getSupportedProcessModels();

            for (Class<? extends ProcessModel> c : modelClasses) {
                    ProcessModel p = null;
                    try {
                        p = c.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    b.append("<modelclass>");
                    b.append("<name>");
                    b.append(p.getDescription());
                    b.append("</name>");
                    b.append("<class>");
                    b.append(c.getName());
                    b.append("</class>");
                    b.append("</modelclass>");
            }

            b.append("</modelclasses>");
            response = b.toString();
        } else if (requestUri.matches("/utils/dummy\\?name=.+(&preview=.+)?")) {
            //return dummy image
            Map<String, String> qParams = RequestUtils.getQueryParameters(req);

            String name = qParams.get("name");

            if (name != null) {
                try {
                    boolean preview = false;
                    if (qParams.get("preview") != null && qParams.get("preview").equals("true"))
                        preview = true;

                    BufferedImage img = UtilsRequestHandler.edgeShapes.getDummyNodeImage(name, preview);
                    ResponseUtils.respondWithImage(resp, img);

                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestUri.matches("/utils/nodetypes\\?modeltype=.+")) {
            //list detailed node information on a certain type of process model
            String query = req.getQuery();

            if (query.matches("modeltype=([^=]+)")) {

                try {
                    ProcessModel newModel = (ProcessModel) Class.forName(query.split("=")[1]).newInstance();
                    StringBuilder builder = new StringBuilder(300);
                    //list hierarchy/grouping of node types
                    builder.append("<nodetypes>");

                    List<Class<? extends ProcessNode>> supportedNodes = newModel.getSupportedNodeClasses();
                    for (Class<? extends ProcessNode> nodeClass : supportedNodes) {
                        builder.append("<nodeclass name='");
                        builder.append(nodeClass.getName());
                        builder.append("'>");
                        builder.append(createSuccessorXMLFragment(newModel, nodeClass));
                        builder.append(createPropertyXMLFragment(nodeClass));
                        try {
                            List<Class<? extends ProcessNode>> variants = nodeClass.newInstance().getVariants();
                            for (Class<? extends ProcessNode> c : variants) {
                                builder.append("        <nodetype name='");
                                builder.append(c.getName());
                                builder.append("'>");
                                builder.append(createSuccessorXMLFragment(newModel, c));
                                builder.append(createPropertyXMLFragment(c));
                                builder.append(createDefaultSizeXMLFragment(newModel, c));
                                builder.append("</nodetype>");
                            }

                            if (variants.size() == 0)
                                builder.append(createDefaultSizeXMLFragment(newModel, nodeClass));

                        } catch (Exception e) {
                            e.printStackTrace();
                            builder.append(createDefaultSizeXMLFragment(newModel, nodeClass));
                            builder.append("</nodeclass>");
                            continue;
                        }
                        builder.append("</nodeclass>");
                    }

                    builder.append("</nodetypes>");

                    response = builder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestUri.matches("/utils/edgetypes\\?modeltype=.+")) {
            //list detailed edge information on a certain type of process model
            String query = req.getQuery();
            if (query.matches("modeltype=([^=]+)")) {
                try {
                    ProcessModel newModel = (ProcessModel) Class.forName(query.split("=")[1]).newInstance();
                    StringBuilder builder = new StringBuilder(300);

                    builder.append("<edgetypes>");

                    List<Class<? extends ProcessEdge>> edgeClasses = newModel.getSupportedEdgeClasses();
                    for (Class<? extends ProcessEdge> edgeClass : edgeClasses) {
                        builder.append("<edgeclass name='");
                        builder.append(edgeClass.getName());
                        builder.append("'>");
                        builder.append(createPropertyXMLFragment(edgeClass));
                        builder.append("</edgeclass>");
                    }

                    builder.append("</edgetypes>");
                    response = builder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestUri.matches("/utils/propertyeditortypes\\?objecttype=.+")) {
            //get property editor information
            String className = RequestUtils.getQueryParameters(req).get("objecttype");

            try {
                response = this.createPropertyXMLFragment((Class<? extends ProcessObject>) Class.forName(className));
            } catch (Exception e) {
                e.printStackTrace();
                status = 500;
                response = "<error>Class not found</error>";
            }
        } else if (requestUri.matches("/utils/propertylabels\\?lang=.+class=.+")) {
            Map<String, String> params = RequestUtils.getQueryParameters(req);

            Document doc = XMLHelper.newDocument();
            Element docEl = XMLHelper.addDocumentElement(doc, "property-labels");

            try {
                Class<? extends ProcessObject> c = (Class<? extends ProcessObject>) Class.forName( params.get("class") );
                ProcessObject o = c.newInstance();

                for ( String key : o.getPropertyKeys() ) {
                    String label = PropertyConfig.getPropertyLabel(o, key , params.get("lang"));

                    //to minimize the amount of transferred data check if there is any difference
                    if ( !key.equals(label) ) {
                        Element labelEl = XMLHelper.addElement(doc, docEl, "label");
                        labelEl.setAttribute("property", key);
                        labelEl.setTextContent(label);
                    }
                }

            } catch ( Exception e ) {
                if ( !( e instanceof ClassNotFoundException ) )
                    e.printStackTrace();
            }

            ResponseUtils.respondWithXML(resp, doc, status);
            return;
        } else if ( requestUri.matches("/utils/propertytypes\\?class=.+") ) {
            Map<String, String> params = RequestUtils.getQueryParameters(req);

            Document doc = XMLHelper.newDocument();
            Element docEl = XMLHelper.addDocumentElement(doc, "property-types");

            String className = params.get("class");
            try {
                Class<? extends ProcessObject> c = (Class<? extends ProcessObject>) Class.forName( params.get("class") );
                ProcessObject o = c.newInstance();

                for ( String key : o.getPropertyKeys() ) {
                    String type = PropertyConfig.getPropertyType(o, key );
                    Element propEl = XMLHelper.addElement(doc, docEl, "property");
                    propEl.setAttribute("name", key);
                    propEl.setAttribute("type", type);
                }

            } catch ( Exception e ) {
                if ( !(e instanceof ClassNotFoundException ) )
                    e.printStackTrace();
            }

            ResponseUtils.respondWithXML(resp, doc, status);
            return;
        } else if (requestUri.matches("/utils/edgeshape\\?key=.+")) {
            String query = req.getQuery();
            Map<String, String> params = RequestUtils.getQueryParameters(req);

            BufferedImage img = edgeShapes.getEdgeShapeImage(params.get("key"));

            ResponseUtils.respondWithImage(resp, img);

            return;
        }

        resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_XML);
        ResponseUtils.respondWithStatus(status, response, resp, false);
    }

    @Override
    public void handlePostRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handlePutRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleDeleteRequest(RequestFacade req, ResponseFacade resp) throws IOException, AccessViolationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String createSuccessorXMLFragment(ProcessModel model, Class<? extends ProcessNode> nodeClass) {
        StringBuilder builder = new StringBuilder(300);

        builder.append("<successors>");
        if (model.getUtils() != null) {
            // Create instance of nodeClass @todo: Refactor to use real node!
            try {
                ProcessNode node = nodeClass.newInstance();
                for (Class<? extends ProcessNode> c : model.getUtils().getNextNodesRecommendation(model, node)) {
                    builder.append("<successor class='" + c.getName() + "'/>");
                }
            } catch (Exception e) {
                // Do nothing here
            }
        }

        builder.append("</successors>");

        return builder.toString();
    }

    /**
     * 
     * @param objectClass
     * @return
     */
    private String createPropertyXMLFragment( Class<? extends ProcessObject> objectClass ) {
        StringBuilder builder = new StringBuilder(300);
        
        try {
            ProcessObject node = (ProcessObject) objectClass.newInstance();
            builder.append("<property-editor-types>");

            PropertyEditor editor;
            for (String key : node.getPropertyKeys()) {
                builder.append("<property name='" + key + "' type='");
                editor = node.getPropertyEditor(key);
                builder.append(editor.getType().name());

                if (editor.getType().equals(PropertyEditorType.LIST)) {
                    ListSelectionPropertyEditor lpe = (ListSelectionPropertyEditor) editor;
                    builder.append("' values='" + Arrays.toString(lpe.getValues()));
                }
                builder.append("'/>");
            }

            builder.append("</property-editor-types>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }

    /**
     * @param model
     */
    private String createDefaultSizeXMLFragment(ProcessModel model, Class<? extends ProcessNode> nodeClass) {
        StringBuilder b = new StringBuilder(300);
        try {
            ProcessNode currentNode = (ProcessNode) nodeClass.newInstance();
            //take new BPMNModel as default since all ProcessNodes can be added to all model types
            Rectangle bBox = currentNode.getBoundingBox();

            b.append("<default-size>");
            b.append("  <property name='width' value='" + bBox.width + "'/>");
            b.append("  <property name='height' value='" + bBox.height + "'/>");
            b.append("</default-size>");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return b.toString();
    }

    public static ImageStore getEdgeShapeStore() {
        return edgeShapes;
    }

}
