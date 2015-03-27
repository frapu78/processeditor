/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler.util;

import com.inubit.research.server.request.XMLHelper;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.List;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.*;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.RoutingPointLayouter;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author fel
 */
public class ProcessNodeUtils extends ProcessObjectUtils {
    public static final String NODE_UPDATE_RESPONSE_TAG = "nodeupdate-response";
    private static final String METADATA_TAG_NAME = "metadata";
    private static final String PROP_IMAGE_WIDTH = "imagewidth";
    private static final String PROP_IMAGE_HEIGHT = "imageheight";
    private static final String PROP_OFFSET_X = "offsetx";
    private static final String PROP_OFFSET_Y = "offsety";
    private static final String PROP_ATTACHED_TO = "attachedTo";
    private static final String PROP_ATTACH_POSSIBLE = "attachmentPossible";
    private static final String PROP_ATTACHABLE = "attachable";
    private static final String PROP_IS_CLUSTER = "isCluster";

    private ProcessNode node;

    public ProcessNodeUtils(ProcessNode node) {
        super(node);
        this.node = node;
    }

    public Document toXML() {
        Document doc = XMLHelper.newDocument();
        this.toXML(doc, null);
        return doc;
    }

    public Element toXML( Document doc, Element parentEl ) {
        Element metaEl;
        Rectangle bBox = this.node.getBoundingBox();

        boolean attachmentPossible = false;
        boolean attachable = this.isNodeAttachable();
        String attached = this.getAttachedTo();
        attachmentPossible = isAttachmentPossible();

        //information on image size
        if ( parentEl == null )
            metaEl = XMLHelper.addDocumentElement(doc, METADATA_TAG_NAME);
        else
            metaEl = XMLHelper.addElement(doc, parentEl, METADATA_TAG_NAME);
        
        Properties props = new Properties();

        props.setProperty( PROP_IMAGE_WIDTH, String.valueOf( (int) bBox.getWidth() ));
        props.setProperty( PROP_IMAGE_HEIGHT, String.valueOf( (int) bBox.getHeight() ));

        //information on selection offset
        Point offset = this.node.getSelectionOffset();
        props.setProperty( PROP_OFFSET_X, String.valueOf( offset.x ));
        props.setProperty( PROP_OFFSET_Y, String.valueOf( offset.y ));

        //if this node is attached to another node, add attachment information
        if (attached != null && !attached.equals("")) {
            props.setProperty( PROP_ATTACHED_TO, attached );
        }

        //information on attachability and related stuff
        props.setProperty( PROP_ATTACH_POSSIBLE, String.valueOf(attachmentPossible) );
        props.setProperty( PROP_ATTACHABLE, String.valueOf( attachable ) );
        props.setProperty( PROP_IS_CLUSTER, String.valueOf( this.node.isCluster() ));

        XMLHelper.addPropertyList(doc, metaEl, props);

        return metaEl;
    }

    private boolean isAttachmentPossible() {
        boolean attachmentPossible  = false;
        //define if attachable nodes can be attached to this node
        if (this.node instanceof Task || this.node instanceof SubProcess) {
            attachmentPossible = true;
        }
        return attachmentPossible;
    }

    public JSONObject toJSON() throws JSONException {
        Rectangle bBox = this.node.getBoundingBox();

        boolean attachmentPossible = this.isAttachmentPossible();
        boolean attachable = this.isNodeAttachable();
        String attached = getAttachedTo();

        JSONObject metaData = new JSONObject();

        metaData.put( PROP_IMAGE_WIDTH, bBox.width );
        metaData.put( PROP_IMAGE_HEIGHT, bBox.height );

        //information on selection offset
        Point offset = this.node.getSelectionOffset();
        metaData.put( PROP_OFFSET_X, offset.x );
        metaData.put( PROP_OFFSET_Y, offset.y );

        //if this node is attached to another node, add attachment information
        if (attached != null && !attached.equals("")) {
            metaData.put( PROP_ATTACHED_TO, attached );
        }

        //information on attachability and related stuff
        metaData.put( PROP_ATTACH_POSSIBLE, attachmentPossible );
        metaData.put( PROP_ATTACHABLE, attachable );
        metaData.put( PROP_IS_CLUSTER, this.node.isCluster() );

        return metaData;
    }

    private String getAttachedTo() {
        String attached = null;
        if (this.node instanceof AttachedNode) {
            AttachedNode a = (AttachedNode) this.node;
            if (a.getParentNodeId() != null && !a.getParentNodeId().equals("")) {
                attached = ((AttachedNode) this.node).getParentNodeId();
            }
        }
        return attached;
    }

    public void applyResizeRequest(Document doc, ProcessModel model, String prefix) {
        Map<String, String> properties = XMLHelper.parseProperties(doc.getDocumentElement());

        String width = properties.get("width");
        if (width != null)
            this.node.setProperty(ProcessNode.PROP_WIDTH, width);
        
        String height = properties.get("height");
        if (height != null)
            this.node.setProperty(ProcessNode.PROP_HEIGHT, height);

        String x = properties.get("x");
        if (x != null)
            this.node.setProperty(ProcessNode.PROP_XPOS, x);

        String y = properties.get("y");
        if (y != null)
            this.node.setProperty(ProcessNode.PROP_YPOS, y);
    }

    public Document applyPositionRequest(Document doc, ProcessModel model, String prefix) {
        Map<String, String> properties = XMLHelper.parseProperties(doc.getDocumentElement());
        
        int x = Integer.parseInt(properties.get("x"));
        int y = Integer.parseInt(properties.get("y"));

        // Check if we have a routing point layouter listener
        boolean foundLayouter = model.getListeners().contains(model.getUtils().getRoutingPointLayouter());

        if ( this.node.isCluster() ) {
            model.removeListener( model.getUtils().getRoutingPointLayouter() );
        }

        this.node.setPos(x, y);

        if ( this.node.isCluster() ) {
            RoutingPointLayouter rpl = model.getUtils().getRoutingPointLayouter();
            if (foundLayouter) model.addListener( rpl );
            for ( ProcessEdge e : model.getEdges() )
                rpl.optimizeRoutingPoints(e, this.node);
        }

        return this.getEdgeUpdateXML(model, prefix, new HashSet<String>());
    }

    /**
     * Creates the necessary information after lanes of a pool have been added, removed or updated
     * @param node the pool
     * @param newUri the URI of the new node image
     * @return the XML
     */
    public Document createPoolUpdateResponse( String newUri ) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, NODE_UPDATE_RESPONSE_TAG);

        this.addBoundsXMLFragment(doc, docEl);
        XMLHelper.addElement(doc, docEl, "imageuri").setAttribute("value", newUri);
        this.addLaneBoundsXMLFragment(doc, docEl);

        return doc;
    }

    public Document createResizeResponse( ProcessModel model, String prefix, String newUri ) {
        Document responseDoc = this.createPropertyUpdateResponse(model, prefix, newUri);

        this.addLaneBoundsXMLFragment(responseDoc, responseDoc.getDocumentElement());

        return responseDoc;
    }

    public Document getEdgeUpdateXML( ProcessModel model , String prefix, Set<String> ignoreEdges ) {
        Document responseDoc = XMLHelper.newDocument();

        this.addEdgeUpdateXMLElement(model, prefix, ignoreEdges, responseDoc, null);

        return responseDoc;
    }

    public void addEdgeUpdateXMLElement( ProcessModel model , String prefix, Set<String> ignoreEdges, Document responseDoc, Element parent ) {
        Element edgeElement;
        if ( parent == null )
            edgeElement = XMLHelper.addDocumentElement(responseDoc, "edges");
        else
            edgeElement = XMLHelper.addElement(responseDoc, parent, "edges");

        List<ProcessNode> dockers = model.getNodesByClass( EdgeDocker.class );
        Set<EdgeDocker> affectedDockers = new HashSet<EdgeDocker>();

        for ( ProcessEdge e : model.getEdges() ) {
            if ( !ignoreEdges.contains(e.getId()) && (e.getSource() == this.node || e.getTarget() == this.node) ) {
                Element edgeEl = XMLHelper.addElement(responseDoc, edgeElement, "edge");
                edgeEl.setAttribute("id", e.getId());

                new ProcessEdgeUtils(e).addMetaXMLElement(responseDoc, edgeEl, prefix);
                collectEdgeDockers(dockers, affectedDockers, e, model);
            }
        }

        Element dockersEl = XMLHelper.addElement(responseDoc, edgeElement, "dockers");
        for ( EdgeDocker d : affectedDockers ) {
            Element dEl = XMLHelper.addElement(responseDoc, dockersEl, "docker");
            dEl.setAttribute("id", d.getId());
            Properties props = new Properties();
            props.setProperty("x", String.valueOf(d.getPos().x));
            props.setProperty("y", String.valueOf(d.getPos().y));

            XMLHelper.addPropertyList(responseDoc, XMLHelper.addElement(responseDoc, dEl, "position"), props);

            for ( ProcessEdge dockedEdge : model.getEdges() ) {
                if ( !ignoreEdges.contains(dockedEdge.getId()) && (dockedEdge.getTarget().equals( d ) || dockedEdge.getSource().equals( d ))) {
                    Element edgeEl = XMLHelper.addElement(responseDoc, dEl, "edge");
                    edgeEl.setAttribute("id", dockedEdge.getId());
                    new ProcessEdgeUtils(dockedEdge).addMetaXMLElement(responseDoc, edgeEl, prefix);
                }
            }
        }

        Element ceEl = XMLHelper.addElement(responseDoc, edgeElement, "cross-edges");
        if ( this.node.isCluster() ) {
            List<ProcessNode> containedNodes = (( Cluster ) this.node).getProcessNodesRecursivly();

            for ( ProcessEdge edge : model.getEdges() ) {
                if ( !ignoreEdges.contains(edge.getId()) && (containedNodes.contains(edge.getSource()) ^ containedNodes.contains(edge.getTarget())) ) {
                    Element edgeEl = XMLHelper.addElement(responseDoc, ceEl, "edge");
                    edgeEl.setAttribute("id", edge.getId());
                    new ProcessEdgeUtils(edge).addMetaXMLElement(responseDoc, edgeEl, prefix);
                }
            }
        }
    }

    public Document createPropertyUpdateResponse( ProcessModel model, String prefix, String newUri ) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, NODE_UPDATE_RESPONSE_TAG);
        this.addBoundsXMLFragment(doc, docEl);
        XMLHelper.addElement(doc, docEl, "imageuri").setAttribute("value", newUri);

        this.toXML(doc, docEl);

        this.addEdgeUpdateXMLElement(model, prefix, new HashSet<String>(), doc, docEl);
        return doc;
    }

    private Element addBoundsXMLFragment( Document doc, Element parentEl ) {
        Element boundsEl = XMLHelper.addElement(doc, parentEl, "bounds");

        Properties props = new Properties();
        props.setProperty( ProcessNode.PROP_XPOS, String.valueOf( node.getPos().x ));
        props.setProperty( ProcessNode.PROP_YPOS, String.valueOf( node.getPos().y ));
        props.setProperty( ProcessNode.PROP_WIDTH, String.valueOf( node.getSize().width ));
        props.setProperty( ProcessNode.PROP_HEIGHT, String.valueOf( node.getSize().height ));
        XMLHelper.addPropertyList(doc, boundsEl, props);

        return boundsEl;
    }

    private Element addLaneBoundsXMLFragment( Document doc, Element parentEl) {
        if ( !(this.node instanceof Pool) )
            return null;

        Pool pool = ( Pool ) this.node;

        Element lanesEl = XMLHelper.addElement(doc, parentEl, "lanes");
        Set<Lane> lanes = pool.getLanesRecursively();

        for ( Lane lane : lanes ) {
            Element laneEl = XMLHelper.addElement(doc, lanesEl, "lane");
            laneEl.setAttribute("id", lane.getId());
            new ProcessNodeUtils(lane).addBoundsXMLFragment(doc, laneEl);
        }

        return lanesEl;
    }

    private void collectEdgeDockers( List<ProcessNode> dockers, Set<EdgeDocker> collected, ProcessEdge e, ProcessModel model  ) {
        for ( ProcessNode n : dockers ) {
            EdgeDocker d = ( EdgeDocker ) n;

            if ( e.equals( d.getDockedEdge() )) {
                if ( !collected.contains(d) ) {
                    collected.add(d);
                    for ( ProcessEdge pe : model.getEdges() ) {
                        if ( pe.getSource().equals( d ) || pe.getTarget().equals(d) )
                            this.collectEdgeDockers(dockers, collected, pe, model);
                    }
                }
            }
        }
    }

    private boolean isNodeAttachable() {
        boolean attachable = false;

        //define if this node is attachable to other nodes
        if (this.node instanceof MessageIntermediateEvent ||
            this.node instanceof TimerIntermediateEvent ||
            this.node instanceof EscalationIntermediateEvent ||
            this.node instanceof ConditionalIntermediateEvent ||
            this.node instanceof CancelIntermediateEvent ||
            this.node instanceof SignalIntermediateEvent ||
            this.node instanceof MultipleIntermediateEvent ||
            this.node instanceof ParallelMultipleIntermediateEvent ||
            this.node instanceof CompensationIntermediateEvent ||
            this.node instanceof ErrorIntermediateEvent)

                  attachable = true;
        return attachable;
    }
}
