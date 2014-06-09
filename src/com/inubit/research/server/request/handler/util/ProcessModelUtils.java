/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.request.handler.util;


import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.model.AccessType;
import com.inubit.research.server.request.XMLHelper;
import java.util.Properties;

import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author fel
 *
 * Class for handling request actions that are related to ProcessModel instances
 */
public class ProcessModelUtils {
    private static final String PROP_WIDTH = "width";
    private static final String PROP_HEIGHT = "height";
    private static final String PROP_TYPE = "type";
    private static final String PROP_FOLDER = "folder";
    private static final String PROP_ACCESS = "access";
    
    private ProcessModel model;

    public ProcessModelUtils(ProcessModel model) {
        this.model = model;
    }

    public void applyPropertyChange(Document doc) throws DOMException {
        Node propertyNode = doc.getElementsByTagName("property").item(0);

        String property = propertyNode.getAttributes().getNamedItem("name").getNodeValue();
        String value = propertyNode.getAttributes().getNamedItem("value").getNodeValue();

        value = ProcessEditorServerUtils.unEscapeString(value);

        this.model.setProperty(property, value);
    }

    public Document getMetaXML(String id, String folder, AccessType access) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, "metadata");
        Properties props = new Properties();

        props.setProperty(PROP_WIDTH, String.valueOf( this.model.getSize().width ));
        props.setProperty(PROP_HEIGHT, String.valueOf( this.model.getSize().height ));
        props.setProperty(PROP_TYPE, this.model.getClass().getName());
        props.setProperty(PROP_FOLDER, folder);
        props.setProperty(PROP_ACCESS, access.toString());

        XMLHelper.addPropertyList(doc, docEl, props);

        return doc;
    }

    /**
     * Create a list of all nodes belonging to the model
     * @param prefix server base address
     * @return XML-list of nodes
     */
    public Document getNodeList(String prefix) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, "nodes");
        
        for (ProcessNode node : this.model.getNodes()) {
            Element nodeEl = XMLHelper.addElement(doc, docEl, "node");
            XMLHelper.addElement(doc, nodeEl, "uri").setTextContent(
                    prefix + this.model.getProcessModelURI() +
                    "/nodes/" + node.getId());

        }
        return doc;
    }

     /**
     * Create an XML-list of all edges contained in the given model
     * @param prefix server base address
     * @return XML-list of all edges
     */
    public Document getEdgeList(String prefix) {
        Document doc = XMLHelper.newDocument();
        Element docEl = XMLHelper.addDocumentElement(doc, "edges");
        for (ProcessEdge edge : this.model.getEdges()) {
            Element nodeEl = XMLHelper.addElement(doc, docEl, "node");
            XMLHelper.addElement(doc, nodeEl, "uri").setTextContent(
                    prefix + this.model.getProcessModelURI() +
                    "/edges/" + edge.getId());

        }
        return doc;
    }

    public void layout() {
        ProcessLayouter layouter = this.model.getUtils().getLayouters().get(0);

        try {
            layouter.layoutModel(LayoutUtils.getAdapter(this.model));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Element addBoundsElement( Document doc, Element parentEl ) {
        Element el;
        if ( parentEl == null ) 
            el = XMLHelper.addDocumentElement(doc, "bounds");
        else
            el = XMLHelper.addElement(doc, parentEl, "bounds");

        Properties props = new Properties();
        props.setProperty(PROP_WIDTH, String.valueOf(this.model.getSize().width));
        props.setProperty(PROP_HEIGHT, String.valueOf(this.model.getSize().width));

        XMLHelper.addPropertyList(doc, el, props);

        return el;
    }

    public JSONObject getBoundsJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put(PROP_WIDTH, this.model.getSize().width);
        o.put(PROP_HEIGHT, this.model.getSize().width);
        return o;
    }
}
