/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler.util;

import com.inubit.research.server.ImageStore;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.request.handler.UtilsRequestHandler;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author fel
 */
public class ProcessEdgeUtils extends ProcessObjectUtils {
    public static final String EDGE_SHAPE_URI = "/utils/edgeshape?key=";
    
    private ProcessEdge edge;

    public ProcessEdgeUtils(ProcessEdge edge) {
        super(edge);
        this.edge = edge;
    }

    public Document getMetaXMLDocument( String prefix ) {
        Document doc = XMLHelper.newDocument();
        Element edgeEl = XMLHelper.addDocumentElement(doc, "edge");

        this.addMetaXMLElement(doc, edgeEl, prefix);
        
        return doc;
    }

    public JSONArray getPointsJSON() throws JSONException {
        JSONArray a = new JSONArray();

        for ( Point p : edge.getRoutingPoints() ) {
            JSONObject o = new JSONObject();
            o.put("x", p.x);
            o.put("y", p.y);
            a.put(o);
        }

        return a;
    }

    public void addPointsXML( Document doc , Element parent ) {
        Element pointsElement = XMLHelper.addElement(doc, parent, "points");
        List<Point> points = edge.getRoutingPoints();
        for (Point p : points) {
            Element pElement = XMLHelper.addElement(doc, pointsElement, "point");
            pElement.setAttribute("x", String.valueOf(p.x));
            pElement.setAttribute("y", String.valueOf(p.y));
        }
    }

    public BufferedImage getLabelPNGGraphics() {
        Rectangle textSize = this.getTextSize();

        int width = ( textSize.width <= 0 ? 1 : textSize.width);
        int height = ( textSize.height <= 0 ? 1 : textSize.height);

        BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setBackground(new Color(255,255,255,255));
        g2.clearRect(0, 0, textSize.width, textSize.height);
        g2.setColor(Color.BLACK);
        
        g2.setFont(ProcessUtils.defaultFont);
        ProcessUtils.drawText(g2, textSize.width / 2 + 2, textSize.height / 2 - 1, textSize.width, this.edge.getLabel(), ProcessUtils.Orientation.CENTER, false);
        return img;
    }

    public Element addMetaXMLElement( Document doc, Element parent, String prefix ) {
        if (edge == null) return null;

        String sourceShapeKey = UtilsRequestHandler.getEdgeShapeStore().
                                    addEdgeShapeImage(edge, ImageStore.DIR_SOURCE);

        String sourceUri = sourceShapeKey == null ? null : prefix + EDGE_SHAPE_URI + sourceShapeKey;

        String targetShapeKey = UtilsRequestHandler.getEdgeShapeStore().
                                    addEdgeShapeImage(edge, ImageStore.DIR_TARGET);

        String targetUri = targetShapeKey == null ? null : prefix + EDGE_SHAPE_URI + targetShapeKey;

        Element metaEl = XMLHelper.addElement(doc, parent, "metadata");

        //information on routing points
        this.addPointsXML(doc, metaEl);

        // (fpu): Add label position to edge metadata
        Point labelP = edge.getLabelPosition();
        Element labelPos = XMLHelper.addElement(doc, metaEl, "labelpos");
        Element labelXPos = XMLHelper.addElement(doc, labelPos, "property");
        labelXPos.setAttribute("name", "x");
        labelXPos.setAttribute("value", ""+labelP.x);
        Element labelYPos = XMLHelper.addElement(doc, labelPos, "property");
        labelYPos.setAttribute("name", "y");
        labelYPos.setAttribute("value", ""+labelP.y);

        // (fpu): Add edge style information (to be extended)
        Stroke edgeStroke = edge.getLineStroke();
        float strokeWidthValue = ProcessUtils.defaultStroke.getLineWidth();
        if (edgeStroke instanceof BasicStroke) {
            BasicStroke bStroke = (BasicStroke)edgeStroke;
            // Parse values from BasicStroke
            strokeWidthValue = bStroke.getLineWidth();
        }
        Element strokeMeta = XMLHelper.addElement(doc, metaEl, "stroke");
        Element strokeWidth = XMLHelper.addElement(doc, strokeMeta, "property");
        strokeWidth.setAttribute("name", "width");
        strokeWidth.setAttribute("value",""+((double)strokeWidthValue));

        //if exists, information on source shape
        if (sourceUri != null) {
            BufferedImage img = UtilsRequestHandler.getEdgeShapeStore().getEdgeShapeImage(sourceShapeKey);
            Element sShape = XMLHelper.addElement(doc, metaEl, "sourceshape");
            Properties props = new Properties();
            props.setProperty("uri", sourceUri);
            props.setProperty("width", String.valueOf( img.getWidth() ));
            props.setProperty("height", String.valueOf( img.getHeight() ));
            props.setProperty("outline", String.valueOf( edge.isOutlineSourceArrow() ));
            XMLHelper.addPropertyList(doc, sShape, props);
        }

        //if exists, information on target shape
        if (targetUri != null) {
            BufferedImage img = UtilsRequestHandler.getEdgeShapeStore().getEdgeShapeImage(targetShapeKey);
            Element tShape = XMLHelper.addElement(doc, metaEl, "targetshape");
            Properties props = new Properties();
            props.setProperty("uri", targetUri);
            props.setProperty("width", String.valueOf( img.getWidth() ));
            props.setProperty("height", String.valueOf( img.getHeight() ));
            props.setProperty("outline", String.valueOf( edge.isOutlineTargetArrow() ));
            XMLHelper.addPropertyList(doc, tShape, props);
        }

        //information on edge label size
        Rectangle textBounds = this.getTextSize();
        int width = (textBounds.width < 0 ? 0 : textBounds.width);
        int height = (textBounds.height < 0 ? 0 : textBounds.height);
        Element tbElement = XMLHelper.addElement(doc, metaEl, "textbounds");
        Properties props = new Properties();
        props.setProperty("width", String.valueOf( width ));
        props.setProperty("height", String.valueOf( height ));
        XMLHelper.addPropertyList(doc, tbElement, props);

        return metaEl;
    }

    public JSONObject getMetaJSON( String prefix ) throws JSONException {
        if ( edge == null ) return null;

        JSONObject json = new JSONObject();
        String sourceShapeKey = UtilsRequestHandler.getEdgeShapeStore().
                                    addEdgeShapeImage(edge, ImageStore.DIR_SOURCE);

        String sourceUri = sourceShapeKey == null ? null : prefix + EDGE_SHAPE_URI + sourceShapeKey;

        String targetShapeKey = UtilsRequestHandler.getEdgeShapeStore().
                                    addEdgeShapeImage(edge, ImageStore.DIR_TARGET);

        String targetUri = targetShapeKey == null ? null : prefix + EDGE_SHAPE_URI + targetShapeKey;
        json.put("points", this.getPointsJSON());
        
        JSONObject labelPosition = new JSONObject();
        Point labelP = edge.getLabelPosition();
        labelPosition.put("x", labelP.x);
        labelPosition.put("y", labelP.y);
        json.put("labelpos", labelPosition);

        JSONObject stroke = new JSONObject();
        Stroke edgeStroke = edge.getLineStroke();
        float strokeWidthValue = ProcessUtils.defaultStroke.getLineWidth();
        if (edgeStroke instanceof BasicStroke) {
            BasicStroke bStroke = (BasicStroke)edgeStroke;
            // Parse values from BasicStroke
            strokeWidthValue = bStroke.getLineWidth();
        }
        stroke.put("width", strokeWidthValue);
        json.put("stroke", stroke);

        //if exists, information on source shape
        if (sourceUri != null) {
            BufferedImage img = UtilsRequestHandler.getEdgeShapeStore().getEdgeShapeImage(sourceShapeKey);
            JSONObject sourceShape = new JSONObject();
            sourceShape.put("uri", sourceUri);
            sourceShape.put("width", img.getWidth());
            sourceShape.put("height", img.getHeight());
            sourceShape.put("outline", edge.isOutlineSourceArrow());
            json.put("sourceshape", sourceShape);
        }

        //if exists, information on target shape
        if (targetUri != null) {
            BufferedImage img = UtilsRequestHandler.getEdgeShapeStore().getEdgeShapeImage(targetShapeKey);
            JSONObject targetShape = new JSONObject();
            targetShape.put("uri", targetUri);
            targetShape.put("width", img.getWidth() );
            targetShape.put("height", img.getHeight() );
            targetShape.put("outline", edge.isOutlineTargetArrow() );
            json.put("targetshape", targetShape);
        }

        //information on edge label size
        Rectangle tB = this.getTextSize();
        int width = (tB.width < 0 ? 0 : tB.width);
        int height = (tB.height < 0 ? 0 : tB.height);
        JSONObject textBounds = new JSONObject();
        textBounds.put("width", width );
        textBounds.put("height",  height );
        json.put("textbounds", textBounds);
        
        return json;
    }

    private Rectangle getTextSize() {
        BufferedImage dummyImg = new BufferedImage(200, 200, BufferedImage.BITMASK);
        Graphics2D g = dummyImg.createGraphics();
        g.setFont(ProcessUtils.defaultFont);

        Rectangle textBounds = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE);

        textBounds = BPMNUtils.drawText(g, 100, 100, 100, this.edge.getLabel(), ProcessUtils.Orientation.CENTER);

        textBounds.x -= 2;
        textBounds.y -= 2;
        textBounds.width += 8;
        textBounds.height += 4;

        return textBounds;
    }
}
