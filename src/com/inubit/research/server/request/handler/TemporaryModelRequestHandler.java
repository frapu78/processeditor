/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEdgeDragHelper;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.RoutingPointLayouter;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.LaneableCluster;
import net.frapu.code.visualization.bpmn.Pool;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ImageStore;
import com.inubit.research.server.InvitationMailer;
import com.inubit.research.server.MonitoringUtils;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.errors.AccessViolationException;
import com.inubit.research.server.manager.TemporaryKeyManager;
import com.inubit.research.server.meta.ProcessObjectComment;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.plugins.ModelDifferenceTracker;
import com.inubit.research.server.request.RequestFacade;
import com.inubit.research.server.request.RequestUtils;
import com.inubit.research.server.request.ResponseFacade;
import com.inubit.research.server.request.ResponseUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.request.handler.util.ProcessEdgeUtils;
import com.inubit.research.server.request.handler.util.ProcessModelUtils;
import com.inubit.research.server.request.handler.util.ProcessNodeUtils;
import com.inubit.research.server.user.Group;
import com.inubit.research.server.user.LoginableUser;
import com.inubit.research.server.user.SingleUser;
import com.inubit.research.server.user.TemporaryUser;

/**
 * Handler class for temporary models. In general, all request for editing a model are
 * processed by this handler.
 * 
 * @author fel
 */
public class TemporaryModelRequestHandler extends ModelRequestHandler {
    @Override
    public void handleGetRequest( RequestFacade req, ResponseFacade resp ) throws IOException, AccessViolationException {
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        
        if (!lu.isSingleUser())
            throw new AccessViolationException("Unregistered users are not allowed to access temporary models");
        SingleUser user = (SingleUser) lu;

        String requestUri = req.getRequestURI();
        Document responseDoc = null;
        ProcessModel currentModel = retrieveProcessModel(requestUri);
        ProcessNode currentNode = retrieveProcessNode(requestUri, currentModel);
        ProcessEdge currentEdge = retrieveProcessEdge(requestUri, currentModel);

        if (requestUri.matches("/models/tmp/\\d+_\\d+")) {
            retrieveCorrectRepresentation(currentModel, AccessType.WRITE, requestUri, req, resp );
            return;
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+\\..+")) {
            retrieveCorrectRepresentation(currentModel, AccessType.NONE, requestUri, req, resp );
            return;
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/meta")) {
            //Show meta data for model
            String baseId = parseModelIdFromRequestUri(requestUri);
            AccessType at = null;

            if (modelManager.persistentModelExists(baseId))
                at = modelManager.getAccessForModel(baseId, -1, user);
            else
                at = AccessType.OWNER;

            responseDoc = createModelMetaData(currentModel, requestUri, at);
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/comments\\?version=.+")) {
            int version = 0;

            try {
                version = Integer.parseInt(RequestUtils.getQueryParameters(req).get("version"));
            } catch ( NumberFormatException ex ) {
                version = 0;
            }
            Map<String, String> params = RequestUtils.getQueryParameters(req);
            String baseId;
            if ( params.get("baseId") != null )
                baseId = params.get("baseId");
            else
                baseId = parseModelIdFromRequestUri(requestUri);
            
            JSONObject jo = new JSONObject();
            try {
                jo.put("success", true);
                jo.put("comments", createCommentList(baseId, version, "model", lu));
            } catch ( JSONException ex ) {
                ex.printStackTrace();
            }
            ResponseUtils.respondWithJSON(resp, jo, 200);
            return;
//            responseDoc = createCommentList(baseId, version, "model", user);
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+/comments\\?version=.+")) {
            try {
                Map<String, String> params = RequestUtils.getQueryParameters(req);
                int version = Integer.parseInt(params.get("version"));
                String baseId;
                if ( params.get("baseId") != null )
                    baseId = params.get("baseId");
                else
                    baseId = parseModelIdFromRequestUri(requestUri);
                String id = getNodeIdFromUri(requestUri);
                JSONObject jo = new JSONObject();
                try {
                    jo.put("success", true);
                    jo.put("comments", createCommentList(baseId, version, id, lu));
                } catch ( JSONException ex ) {
                    ex.printStackTrace();
                }
                ResponseUtils.respondWithJSON(resp, jo, 200);
                return;
//                responseDoc = createCommentList(baseId, version, id, user);
            } catch (NumberFormatException e) {
                responseDoc = XMLHelper.newDocument();
                XMLHelper.addDocumentElement(responseDoc, "comments");
            }
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes")) {
            //Show list of all nodes contained in the current model
            responseDoc = createNodeList(currentModel, getAbsoluteAddressPrefix(req));
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+")) {
            String firstAccept = req.getHeader(HttpConstants.HEADER_KEY_ACCEPT);
            if (firstAccept.startsWith("image")) {
                createNodePNGGraphics(currentNode.getId(), currentModel, resp);
                return;
            }
            //Return node as XML
            responseDoc = serializeObject(currentNode);
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+\\.png(\\?time=.+)?")) {
            // Return node as PNG graphics
            String nodeId = getNodeIdFromUri(requestUri);
            createNodePNGGraphics(nodeId, currentModel, resp);
            mu.addResponseTime(MonitoringUtils.RESPONSE_TEMP_NODE_IMAGES,System.nanoTime()-startTime);
            return;
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+/meta")) {
            //Return node meta data
            ProcessNodeUtils utils = new ProcessNodeUtils(currentNode);
            responseDoc = utils.toXML();
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges")) {
            //Show list of all edges
            responseDoc = createEdgeList(currentModel, getAbsoluteAddressPrefix( req ));
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges/\\d+")) {
            //return edge XML
            responseDoc = serializeObject(currentEdge);
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges/\\d+/meta")) {
            //return edge meta data
            ProcessEdgeUtils utils = new ProcessEdgeUtils(currentEdge);
//            response = utils.getMetaXMLDocument();
            ResponseUtils.respondWithXML(resp, utils.getMetaXMLDocument( getAbsoluteAddressPrefix(req) ), 200);
            return;
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges/\\d+/label\\?.*")) {
            createEdgeLabelPNGGraphics(currentEdge, resp);
            mu.addResponseTime(MonitoringUtils.RESPONSE_TEMP_NODE_IMAGES,System.nanoTime()-startTime);
            return;
        }

        if ( responseDoc == null ) {
            // Unknown path, return 404
            resp.setContentType(HttpConstants.CONTENT_TYPE_TEXT_PLAIN);
            ResponseUtils.respondWithStatus(404, "Unknown path (" + requestUri + ")", resp, true);
        } else {
            ResponseUtils.respondWithXML(resp, responseDoc, 200);
            mu.addResponseTime(MonitoringUtils.RESPONSE_TEMP_MISC,System.nanoTime()-startTime);
        }


        return;
    }

    @Override
    public void handlePostRequest( RequestFacade req , ResponseFacade resp ) throws IOException, AccessViolationException {
        LoginableUser lu = RequestUtils.getCurrentUser(req);
        String requestUri = req.getRequestURI();
        if (!lu.isSingleUser())
            throw new AccessViolationException("Unregistered users are not allowed to access temporary models");

        SingleUser user = (SingleUser) lu;
        ProcessModel currentModel = retrieveProcessModel(requestUri);
        Document responseDoc = null;
        int status = 200;

        try {
            if (requestUri.matches("/models/tmp")) {
                //create a temporary model according to request headers
                //either from an existing persistent, or as complete new model
                String sourceHeader = req.getHeader("Model-Source");
                String modelTypeHeader = req.getHeader("Model-Type");
                if (sourceHeader != null) {
                    String id = this.parseModelIdFromRequestUri(sourceHeader);

                    if (!lu.isSingleUser())
                        throw new AccessViolationException("Access denied for unregistered user");

                    AccessType access = modelManager.getAccessForModel(id, -1, lu);
                    if (access.compareTo(AccessType.WRITE) < 0)
                        throw new AccessViolationException("User " + lu.getName() + " is not allowed to change this model!");

                    //Create new temporary version of the model and return its temporary URI
                    responseDoc = createNewTemporaryModel( sourceHeader, this.getAbsoluteAddressPrefix(req) );
                } else if (modelTypeHeader != null) {
                    try {
                        Class<? extends ProcessModel> c = (Class<? extends ProcessModel>) Class.forName(modelTypeHeader);
                        String tmpUri = modelManager.createNewModel(c);
                        responseDoc = XMLHelper.newDocument();
                        XMLHelper.addElement(responseDoc, XMLHelper.addDocumentElement(responseDoc, "model"), "uri")
                                .setAttribute("value", req.getContext() + tmpUri);
                        status = 201;
                    } catch (Exception e) {
                        e.printStackTrace();
                        status = 500;
                        responseDoc = XMLHelper.newDocument();
                        XMLHelper.addDocumentElement(responseDoc, "error").setTextContent(e.getMessage());
                    }
                } else {
                    ResponseUtils.respondWithStatus(400, "No type or source specified!", resp, true);
                    return;
                }
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes")) {
               Document doc = RequestUtils.getXML(req);
               //create new node(s)
               responseDoc = XMLHelper.newDocument();
               Element docEl = XMLHelper.addDocumentElement(responseDoc, "nodes");
               NodeList nodes = doc.getElementsByTagName("node");
               for (int i = 0; i < nodes.getLength(); i++) {
                   Node node = nodes.item(i);
                   Map<String, String> props = XMLHelper.parseProperties(node);
                   String className = props.get("#type");

                   ProcessNode newNode;

                   //be aware of edge docker creation
                   if (className.equals(EdgeDocker.class.getName())) {
                       ProcessEdge dockedEdge = (ProcessEdge) currentModel.getObjectById(props.get("#docked_edge"));
                       newNode = new EdgeDocker(dockedEdge);
                   } else {
                       newNode = (ProcessNode) Class.forName(className).newInstance();
                   }

                   for (Map.Entry<String, String> e : props.entrySet()) {
                       if (e.getKey().startsWith("#"))
                           continue;
                       newNode.setProperty(e.getKey(), e.getValue());
                   }

                   if ( props.get("#id") != null && currentModel.getNodeById( props.get("#id") ) == null )
                       newNode.setId( props.get("#id") );

                   currentModel.addNode(newNode);
                   //keep reference to old node for web editor
                   Element nodeEl = XMLHelper.addElement(responseDoc, docEl, "node");
                   if (props.get("#id") != null)
                       nodeEl.setAttribute("oldId", props.get("#id"));

                   nodeEl.setAttribute("newId",newNode.getId());
               }
               status = 200;
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges")) {
               //create new edge(s)
               Document doc = RequestUtils.getXML(req);
               responseDoc = XMLHelper.newDocument();
               Element docEl = XMLHelper.addDocumentElement(responseDoc, "edges");
               NodeList edges = doc.getElementsByTagName("edge");

               for (int i = 0; i < edges.getLength(); i++) {
                   Node edge = edges.item(i);
                   Map<String, String> props = XMLHelper.parseProperties(edge);
                   String className = props.get("#type");

                   ProcessEdge newEdge = null;

                   //create specified type, or take default type
                   if (className != null) {
                       newEdge = (ProcessEdge) Class.forName(className).newInstance();
                   } else {
                       //create default edge using source and target node
                       String sourceId = props.get("#sourceNode");
                       String targetId = props.get("#targetNode");

                       ProcessNode sourceNode = currentModel.getNodeById(sourceId);
                       ProcessNode targetNode = currentModel.getNodeById(targetId);

                       newEdge = currentModel.getUtils().createDefaultEdge(sourceNode, targetNode);
                   }

                   //update properties
                   for (Map.Entry<String, String> e : props.entrySet()) {
                       if (e.getKey().equals("#id")) {
                           ProcessEdge edgeWithId = null;
                           for ( ProcessEdge ed : currentModel.getEdges() )
                                if ( ed.getId().equals( e.getValue() )) {
                                    edgeWithId = ed;
                                    break;
                                }

                           if ( edgeWithId != null )
                                continue;
                       }
                       
                       else if (e.getKey().equals("#sourceNode")) {
                           ProcessNode source = currentModel.getNodeById(e.getValue());
                           newEdge.setSource(source);
                       }
                       else if (e.getKey().equals("#targetNode")) {
                           ProcessNode target = currentModel.getNodeById(e.getValue());
                           newEdge.setTarget(target);
                       }
                       newEdge.setProperty(e.getKey(), e.getValue());
                   }

                   currentModel.addEdge(newEdge);
                   XMLHelper.addElement(responseDoc, docEl, "edge").setAttribute("id", newEdge.getId());
               }
               status = 200;
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/invite")) {
                Document doc = RequestUtils.getXML(req);
                //Create new invitations to comment on this model
                NodeList children = doc.getDocumentElement().getChildNodes();

                //Get invitation data
                String expireString = doc.getElementsByTagName("expire").item(0).getTextContent();
                String modelId = doc.getElementsByTagName("id").item(0).getTextContent();
                String mailText = ProcessEditorServerUtils.unEscapeString(doc.getElementsByTagName("text").item(0).getTextContent());
                int version = Integer.parseInt(doc.getElementsByTagName("version").item(0).getTextContent());

                Date expireDate = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").parse(expireString);
                Set<TemporaryUser> invitees = new HashSet<TemporaryUser>();

                for ( int i = 0; i < children.getLength(); i++ ) {
                    Node node = children.item(i);

                    if (node.getNodeName().equals("user")) {
                        //invite existing user
                        SingleUser u = ProcessEditorServerHelper.getUserManager().getUserForName(node.getTextContent());

                        if (u != null) {
                            TemporaryUser tu = new TemporaryUser(u.getName(), u.getMail(), u.getRealName(), expireDate, modelId, version);
                            invitees.add(tu);
                        }

                    } else if (node.getNodeName().equals("guest")) {
                        //create a new temporary user and invite him/her
                        NodeList nodeChildren = node.getChildNodes();
                        String uAlias = null;
                        String uMail = null;
                        String uReal = null;
                        for ( int j = 0; j < nodeChildren.getLength(); j++) {
                            Node childNode = nodeChildren.item(j);
                            if (childNode.getNodeName().equals("name"))
                                uAlias = childNode.getTextContent();
                            else if (childNode.getNodeName().equals("mail"))
                                uMail = childNode.getTextContent();
                            else if (childNode.getNodeName().equals("real"))
                                uReal = childNode.getTextContent();
                        }

                        TemporaryUser tu = new TemporaryUser(uAlias, uMail, uReal, expireDate, modelId, version);
                        invitees.add(tu);
                    } else if (node.getNodeName().equals("group")) {
                        //invite all users contained in the given group
                        Group g = ProcessEditorServerHelper.getUserManager().getGroupForName(node.getTextContent());

                        Set<SingleUser> groupUsers = ProcessEditorServerHelper.getUserManager().getRecursiveUsersForGroup(g);
                        for (SingleUser u : groupUsers) {
                            TemporaryUser tu = new TemporaryUser(u.getName(), u.getMail(), u.getRealName(), expireDate, modelId, version);
                            invitees.add(tu);
                        }
                    }
                }

                //send mails
                for (TemporaryUser tu : invitees) {
                    String key = TemporaryKeyManager.addUser(tu);
                    InvitationMailer.inviteUser(key, tu, mailText, user, currentModel.getProcessName(), req);
                }
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/comments\\?version=.+")) {
                Map<String, String> params = RequestUtils.getQueryParameters(req);
                int version = Integer.parseInt(params.get("version"));
                String baseId;
                if ( params.get("baseId") != null )
                    baseId = params.get("baseId");
                else
                    baseId = parseModelIdFromRequestUri(requestUri);
                
                status = 201;
                try {
                    JSONObject jo = new JSONObject();
                    ProcessObjectComment poc = this.createComment(baseId, version, "model", req, lu);
                    jo.put("success", true);
                    jo.put("comments", poc.toJSON(lu));

                    ResponseUtils.respondWithJSON(resp, jo, status);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                responseDoc = this.createComment( baseId, version, "model", req, user);
                
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+/comments\\?version=.+")) {
                Map<String, String> params = RequestUtils.getQueryParameters(req);
                int version = Integer.parseInt(params.get("version"));
                String baseId;
                if ( params.get("baseId") != null )
                    baseId = params.get("baseId");
                else
                    baseId = parseModelIdFromRequestUri(requestUri);
                status = 201;
                try {
                    JSONObject jo = new JSONObject();
                    ProcessObjectComment poc = this.createComment(baseId, version, this.getNodeIdFromUri(requestUri), req, lu);
                    jo.put("success", true);
                    jo.put("comments", poc.toJSON(lu));

                    ResponseUtils.respondWithJSON(resp, jo, status);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                responseDoc = this.createComment( baseId, version, this.getNodeIdFromUri(requestUri), req, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtils.respondWithStatus(500, "An error occured while processing your request" + e.toString(), resp, true);
            return;
        }

        ResponseUtils.respondWithXML(resp, responseDoc, status);
    }

    @Override
    public void handlePutRequest( RequestFacade req, ResponseFacade resp ) throws IOException {
        String requestUri = req.getRequestURI();
        ProcessModel currentModel = retrieveProcessModel(requestUri);
        String response = "";
        int status = 404;

        try {
            //START REQUEST DISPATCH
            if (requestUri.matches("/models/tmp/\\d+_\\d+")) {
                    Document doc = RequestUtils.getXML(req);
                    String type = doc.getElementsByTagName("update").item(0).
                                    getAttributes().getNamedItem("type").getNodeValue();

                    ProcessModelUtils utils = new ProcessModelUtils(currentModel);
                    //update model property
                    if (type.equals("property") )
                        utils.applyPropertyChange(doc);
                    else if (type.equals("layout")) {
                        Document respDoc = XMLHelper.newDocument();
                        ModelDifferenceTracker mdt = new ModelDifferenceTracker( currentModel );
                        currentModel.addListener(mdt);
                        utils.layout();
                        currentModel.removeListener(mdt);
                        mdt.toXML(respDoc, null, getAbsoluteAddressPrefix(req));
                        ResponseUtils.respondWithXML(resp, respDoc, 200);
                        return;
                    }  else if (type.equals("position")) {
                        NodeList children = doc.getDocumentElement().getChildNodes();
                        Map<ProcessNode, Point> nodes = new HashMap<ProcessNode, Point>();
                        Set<String> ignoreEdges = new HashSet<String>();
                        for ( int i = 0; i < children.getLength(); i++ ) {
                            if ( children.item(i).getNodeName().equals("update") ) {
                                Node node = children.item(i);
                                Map<String, String> properties = XMLHelper.parseProperties( node );
                                int x = Integer.parseInt(properties.get("x"));
                                int y = Integer.parseInt(properties.get("y"));
                                ProcessNode n = currentModel.getNodeById( node.getAttributes().getNamedItem("id").getNodeValue() );
                                nodes.put( n, new Point(x, y) );
                            }
                            if ( children.item(i).getNodeName().equals("ignore-edge") )
                                ignoreEdges.add( children.item(i).getAttributes().getNamedItem("id").getNodeValue() );
                        }

                        Document responseDoc = this.moveMultiSelection(currentModel, nodes, ignoreEdges, getAbsoluteAddressPrefix(req));
                        ResponseUtils.respondWithXML(resp, responseDoc, 200);
                        return;
                    }
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+")) {
                //change node object and return URI to new image
                ProcessNode currentNode = retrieveProcessNode(requestUri, currentModel);
                ProcessNodeUtils utils = new ProcessNodeUtils(currentNode);
                Document doc = RequestUtils.getXML(req);
                String type = doc.getElementsByTagName("update").item(0).
                                getAttributes().getNamedItem("type").getNodeValue();

                status = 200;

                if (type.equals("property")) {
                    //simple property update
                    NodeList ups = doc.getElementsByTagName("property-update");
                    if ( ups.getLength() > 0 ) {
                        for  ( int i = 0; i < ups.getLength(); i++ ) {
                            utils.applyPropertyChange( (Element) ups.item(i) );
                        }
                    } else
                        utils.applyPropertyChange(doc.getDocumentElement());

                    String newUri = createNewNodeImage(currentNode, currentModel, req);
                    ResponseUtils.respondWithXML(resp, utils.createPropertyUpdateResponse( currentModel, getAbsoluteAddressPrefix(req), newUri), status);
                    return;
                } else if (type.equals("resize")) {
                    //apply new size and position
                    utils.applyResizeRequest(doc, currentModel, getAbsoluteAddressPrefix(req) );
                    String newUri = this.createNewNodeImage(currentNode, currentModel, req);
                    Document responseDoc = utils.createResizeResponse(currentModel, getAbsoluteAddressPrefix(req), newUri);
                    if ( responseDoc != null ) {
                        ResponseUtils.respondWithXML(resp, responseDoc, status);
                        return;
                    }
                } else if (type.equals("position")) {
                    //set position
                    Document responseDoc = utils.applyPositionRequest(doc, currentModel, getAbsoluteAddressPrefix(req));
                    ResponseUtils.respondWithXML(resp, responseDoc, status);
                    return;
                } else if (type.equals("type")) {
                    //create new node with new type and copy all properties from the old one
                    String className = XMLHelper.parseProperties(doc.getDocumentElement()).get("newtype");
                    String imageId = currentModel.getId() + "_" + currentNode.getId();

                    Cluster c = currentModel.getClusterForNode(currentNode);

                    ImageStore.removeAllRelatedImages(imageId);
                    ProcessNode newNode = (ProcessNode) Class.forName(className).newInstance();
                    this.copyProperties(newNode, currentNode, currentModel);

                    if ( c != null )
                        c.addProcessNode(newNode);

                    String newUri = getAbsoluteAddressPrefix(req) + requestUri.replaceAll("nodes/\\d+", "nodes/" + newNode.getId());
                    response = "<nodeupdate-response><uri value='" + newUri +"'/></nodeupdate-response>";
                } else if (type.equals("cluster")) {
                    //remove node from old and add it to new cluster
                    updateCluster(currentModel, doc, currentNode);
                } else if (type.equals("attach")) {
                    //attach node
                    String target = XMLHelper.parseProperties(doc.getDocumentElement()).get("target");

                    ProcessNode parentNode = currentModel.getNodeById(target);

                    ((AttachedNode) currentNode).setParentNode(parentNode);
                } else if (type.equals("detach")) {
                    //detach node by setting parent to null
                    ((AttachedNode) currentNode).setParentNode(null);
                } else if (type.equals("lane")) {
                    //find lane and update its property
                    Map<String, String> properties = XMLHelper.parseProperties(doc.getDocumentElement());
                    ProcessNode lane = currentModel.getNodeById(properties.get("laneId"));
                    lane.setProperty(properties.get("key"), ProcessEditorServerUtils.unEscapeString(properties.get("value")));

                    //in case of a lane update the current node is the surrounding pool
                    String newUri = this.createNewNodeImage(currentNode, currentModel, req);

                    Document responseDoc = new ProcessNodeUtils(currentNode).createPoolUpdateResponse(newUri);
                    if ( responseDoc != null ) {
                        ResponseUtils.respondWithXML(resp, responseDoc, status);
                        return;
                    }
                } else if (type.equals("addLane")) {
                    //get pool and lane object by their IDs
                    Map<String, String> properties = XMLHelper.parseProperties(doc.getDocumentElement());
                    ProcessNode lane = currentModel.getNodeById(properties.get("laneId"));
                    ProcessNode cluster = currentModel.getNodeById(properties.get("clusterId"));

                    //check if adding a lane is possible
                    if (cluster instanceof LaneableCluster && lane instanceof Lane) {
                        ((LaneableCluster) cluster).addLane((Lane) lane);

                        String newUri = this.createNewNodeImage(currentNode, currentModel, req);

                        Document responseDoc = new ProcessNodeUtils(currentNode).createPoolUpdateResponse(newUri);
                        if ( responseDoc != null ) {
                            ResponseUtils.respondWithXML(resp, responseDoc, status);
                            return;
                        }
                    } else {
                        status = 500;
                        response = "<error>Object types do not support this operation</error>";
                    }
                }
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges/\\d+")) {
                ProcessEdge currentEdge = retrieveProcessEdge(requestUri, currentModel);
                ProcessEdgeUtils utils = new ProcessEdgeUtils(currentEdge);
                Document doc = RequestUtils.getXML(req);
                //determine action type
                NamedNodeMap attr = doc.getElementsByTagName("update").item(0).getAttributes();
                String type = attr.getNamedItem("type").getNodeValue();

                status = 200;

                if (type.equals("property")) {
                    //update property
                    utils.applyPropertyChange(doc.getDocumentElement());
                } else if (type.equals("type")) {
                    try {
                    //create edge according to new type and copy properties
                        String className = XMLHelper.parseProperties(doc.getDocumentElement()).get("newtype");

                        ProcessEdge newEdge = (ProcessEdge) Class.forName(className).newInstance();
                        newEdge.setSource(currentEdge.getSource());
                        newEdge.setTarget(currentEdge.getTarget());
                        // Copy routing points
                        for (int i = 1; i < currentEdge.getRoutingPoints().size() - 1; i++) {
                            newEdge.addRoutingPoint(currentEdge.getRoutingPoints().get(i));
                        }

                        //copy all other properties
                        newEdge.copyPropertiesFrom(currentEdge);
                        //update model
                        currentModel.removeEdge(currentEdge);
                        currentModel.addEdge(newEdge);

                        String newUri = getAbsoluteAddressPrefix(req) + requestUri.replaceAll("edges/\\d+", "edges/" + newEdge.getId());
                        response = "<edgeupdate-response><uri value='" + newUri +"'/></edgeupdate-response>";
                    } catch ( Exception ex ) {
                        ex.printStackTrace();
                    }
                } else if (type.equals("segment")) {
                    Map<String, String> props = XMLHelper.parseProperties(doc.getDocumentElement());

                    double startX = Double.parseDouble( props.get("fromX") );
                    double startY = Double.parseDouble( props.get("fromY") );
                    double toX = Double.parseDouble( props.get("toX") );
                    double toY = Double.parseDouble( props.get("toY") );

                    Point p1 = new Point( (int) startX , (int) startY );
                    Point p2 = new Point( (int) toX, (int) toY );

                    ProcessEdgeDragHelper edh = new ProcessEdgeDragHelper(currentEdge, p1);
                    edh.setPos(p2);

                    Document responseDoc = new ProcessEdgeUtils(currentEdge).getMetaXMLDocument( getAbsoluteAddressPrefix(req) );
                    ResponseUtils.respondWithXML(resp, responseDoc, status);
                    return;
                }
            } else if (requestUri.matches("/models/tmp/\\d+_\\d+(/nodes/\\d+)?/comments/\\d+(\\?.*?baseId=.+)?")) {
                //change a comment
                status = 200;
                Map<String, String> params = RequestUtils.getQueryParameters(req);
                String id;
                if ( params.get("baseId") != null )
                    id = params.get("baseId");
                else
                    id = parseModelIdFromRequestUri(requestUri);
                
                if ( id.contains("/") ) 
                    id = id.substring(0, id.indexOf("/"));

                String commentId = this.getCommentIdFromUri(requestUri);
                JSONObject jo = RequestUtils.getJSON(req);
                ProcessObjectComment poc = modelManager.updateComment(id, commentId, jo.getString("text"), jo.getInt("validuntil"));
                
                JSONObject responseJSON = new JSONObject();
                if ( poc != null ) {
                    responseJSON.put("success", true);
                    responseJSON.put("comments", poc.toJSON(RequestUtils.getCurrentUser(req)));
                } else
                    responseJSON.put("success", false);
                ResponseUtils.respondWithJSON(resp, responseJSON, status);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.setContentType( HttpConstants.CONTENT_TYPE_TEXT_XML );
        ResponseUtils.respondWithStatus(status, response, resp, false);
        return;
    }

    @Override
    public void handleDeleteRequest( RequestFacade req, ResponseFacade resp ) throws IOException {
        String requestUri = req.getRequestURI();
        ProcessModel currentModel = retrieveProcessModel(requestUri);
        
        if (requestUri.matches("/models/tmp/\\d+_\\d+")) {
            ImageStore.removeAllRelatedImages(currentModel.getId());
            modelManager.removeTemporaryModel(requestUri.substring(requestUri.lastIndexOf("/") + 1));
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/nodes/\\d+")) {
            ProcessNode currentNode = retrieveProcessNode(requestUri, currentModel);
            Document responseDoc = null;

            currentModel.removeNode(currentNode);

            if ( currentNode instanceof Lane ) {
                Pool p = ((Lane) currentNode).getSurroundingPool();
                if ( p != null ) {
                    String newUri = this.createNewNodeImage(p, currentModel, req);
                    responseDoc = new ProcessNodeUtils(p).createPoolUpdateResponse(newUri);
                }
            }

            if ( responseDoc != null ) {
                ResponseUtils.respondWithXML(resp, responseDoc, 200);
                return;
            }
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+/edges/\\d+")) {
            ProcessEdge currentEdge = retrieveProcessEdge(requestUri, currentModel);
            currentModel.removeEdge(currentEdge);
        } else if (requestUri.matches("/models/tmp/\\d+_\\d+(/nodes/\\d+)?/comments/\\d+(\\?.*?baseId=.+)?")) {
            Map<String, String> params = RequestUtils.getQueryParameters(req);
            String mid;
            if ( params.get("baseId") != null )
                mid = params.get("baseId");
            else
                mid = parseModelIdFromRequestUri(requestUri);
            
            if ( mid.contains("/") ) 
                mid = mid.substring(0, mid.indexOf("/"));
            
            String cid = this.getCommentIdFromUri(requestUri);
            
            modelManager.deleteComment( mid, cid );
        }

        ResponseUtils.respondWithStatus(200, "", resp, false);

        return;
    }

    @Override
    protected void createNodePNGGraphics(String nodeId, ProcessModel model, ResponseFacade resp ) {
        try {
            BufferedImage img = ImageStore.getImage(nodeId, model);
            ResponseUtils.respondWithImage(resp, img);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
     }

    private void copyProperties(ProcessNode newNode, ProcessNode oldNode, ProcessModel model) {
        List<ProcessEdge> inEdges = model.getIncomingEdges(ProcessEdge.class, oldNode);
        List<ProcessEdge> outEdges = model.getOutgoingEdges(ProcessEdge.class, oldNode);
        newNode.setText(oldNode.getText());
        newNode.setPos(oldNode.getPos());
        newNode.setStereotype(oldNode.getStereotype());
        newNode.setSize(oldNode.getSize().width, oldNode.getSize().height);
        newNode.setId(oldNode.getId());
        model.addNode(newNode);
        if (oldNode instanceof AttachedNode) {
            ((AttachedNode) newNode).setParentNode(((AttachedNode) oldNode).getParentNode(model));
        }
        for (ProcessEdge edge : inEdges) {
            edge.setTarget(newNode);
        }
        for (ProcessEdge edge : outEdges) {
            edge.setSource(newNode);
        }
        List<Cluster> cluster = model.getClusters();
        for (Cluster c : cluster) {
            if (c.isContained(oldNode)) {
                c.removeProcessNode(oldNode);
                c.addProcessNode(newNode);
            }
        }
        model.removeNode(oldNode);
    }

    private String createNewNodeImage(ProcessNode currentNode, ProcessModel currentModel, RequestFacade req ) {
        BufferedImage img = ProcessEditorServerUtils.createNodeImage(currentNode);
        String newId = ImageStore.add(currentModel, currentNode, img);
        String newUri = getAbsoluteAddressPrefix(req) + req.getRequestURI().replaceAll("/nodes/\\d+", "/nodes/" + newId);
        return newUri;
    }

    private ProcessModel retrieveProcessModel(String requestUri) {
        final Pattern modelRequest = Pattern.compile("/models/tmp/(\\d+_\\d+)");
        final Matcher m = modelRequest.matcher(requestUri);

        if (m.find()) {
             return modelManager.getTemporaryModel(m.group(1));
        }
        
        return null;
    }

    private Document createNewTemporaryModel( String modelUri, String prefix ) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, "model");
        Element uriEl = XMLHelper.addElement(doc, docEl, "uri");

        int version = parseVersionFromRequestUri( modelUri );
        String id = parseModelIdFromRequestUri( modelUri );

        String tmpUri = modelManager.createTemporaryModel(id, version);
        uriEl.setAttribute("value", prefix + tmpUri);

        return doc;
    }

    private void updateCluster(ProcessModel currentModel, Document doc, ProcessNode currentNode) throws DOMException {
        //remove node from old and add it to new cluster
        List<Cluster> clusters = new ArrayList<Cluster>(currentModel.getClusters());
        String clusterID = doc.getElementsByTagName("new").item(0).getAttributes().getNamedItem("value").getNodeValue();
        for (Cluster c : clusters) {
            if (c.isContained(currentNode)) {
                c.removeProcessNode(currentNode);
            }
            if (c.getId().equals(clusterID)) {
                c.addProcessNode(currentNode);
                currentModel.moveAfter(currentNode, c);
            }
        }
    }

    private Document moveMultiSelection( ProcessModel model, Map<ProcessNode, Point> nodes, Set<String> ignoreEdges, String prefix ) {
        Document doc = XMLHelper.newDocument();
        Element pEl = XMLHelper.addDocumentElement(doc, "update-edges");

        // Check if we have a routing point layouter listener
        boolean foundLayouter = model.getListeners().contains(model.getUtils().getRoutingPointLayouter());
        model.removeListener( model.getUtils().getRoutingPointLayouter() );

        Set<ProcessEdge> edges = new HashSet<ProcessEdge>();
        Set<ProcessEdge> movingEdges = new HashSet<ProcessEdge>();

        int xdiff = 0;
        int ydiff = 0;

        for ( Map.Entry<ProcessNode, Point> e : nodes.entrySet() ) {
            xdiff = e.getValue().x - e.getKey().getPos().x;
            ydiff = e.getValue().y - e.getKey().getPos().y;

            e.getKey().setPos( e.getValue() );

            for ( ProcessEdge edge : model.getEdges() ) {
                if ( ( edge.getTarget() == e.getKey() || edge.getSource() == e.getKey() )) {
                    if ( !ignoreEdges.contains(edge.getId()) )
                        edges.add( edge );
                    else
                        movingEdges.add(edge);
                }
            }
        }

        RoutingPointLayouter rpl = model.getUtils().getRoutingPointLayouter();
        // Only add initialy contained
        if (foundLayouter) {
            model.addListener(rpl);
        }

        for ( ProcessNode node : nodes.keySet() ) {
            Iterator<ProcessEdge> it = edges.iterator();
            while ( it.hasNext() ) {
                ProcessEdge edge = it.next();
                if ( edge.getTarget() == node || edge.getSource() == node ) {
                    rpl.optimizeRoutingPoints(edge, node);
                    new ProcessNodeUtils(node).addEdgeUpdateXMLElement(model, prefix, ignoreEdges, doc, pEl);
                    it.remove();
                }
            }
        }

        Iterator<ProcessEdge> it = movingEdges.iterator();
        while ( it.hasNext() ) {
            ProcessEdge edge = it.next();

            for (int i = 1; i < edge.getRoutingPoints().size() - 1; i++) {
                Point p = edge.getRoutingPoints().get(i);
                edge.moveRoutingPoint(i, new Point(p.x + xdiff, p.y + ydiff));
            }

            it.remove();
        }
        
        return doc;
    }
}
