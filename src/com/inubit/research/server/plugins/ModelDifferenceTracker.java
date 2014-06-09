/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.Pool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.inubit.research.server.ImageStore;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.XMLHelper;
import com.inubit.research.server.request.handler.util.ProcessEdgeUtils;
import com.inubit.research.server.request.handler.util.ProcessModelUtils;
import com.inubit.research.server.request.handler.util.ProcessNodeUtils;

/**
 *
 * @author fel
 */
public class ModelDifferenceTracker implements ProcessModelListener {
    private static final String[] imageIndependent = {
        ProcessNode.PROP_XPOS, ProcessNode.PROP_YPOS
    };

    private ProcessModel model;

    private Set<String> removedEdges = new HashSet<String>();
    private Set<String> removedNodes = new HashSet<String>();

    private Set<String> addedEdges = new HashSet<String>();
    private Set<String> addedNodes = new HashSet<String>();

    private Set<ProcessEdge> updatedEdges = new HashSet<ProcessEdge>();
    private Set<ProcessNode> updatedNodes = new HashSet<ProcessNode>();

    private Set<String> updateImage = new HashSet<String>();

    public ModelDifferenceTracker( ProcessModel model ) {
        this.model = model;
    }

    public void processNodeAdded(ProcessNode newNode) {
        addedNodes.add( newNode.getId() );
    }

    public void processNodeRemoved(ProcessNode remNode) {
        removedNodes.add( remNode.getId() );
    }

    public void processEdgeAdded(ProcessEdge edge) {
        addedEdges.add( edge.getId() );
    }

    public void processEdgeRemoved(ProcessEdge edge) {
        removedEdges.add( edge.getId() );
    }

    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        if ( obj instanceof ProcessEdge )
            updatedEdges.add( (ProcessEdge) obj );
        else if ( obj instanceof ProcessNode ) {
            updatedNodes.add( (ProcessNode) obj );
            
            if ( obj instanceof Lane ) {
            	Lane l = (Lane) obj;
            	Pool p = l.getSurroundingPool();
            	
            	updatedNodes.add( p );
            	updateImage.add( p.getId() );
            } else if ( Arrays.binarySearch(imageIndependent, name) < 0 ) {
                updateImage.add( obj.getId() );
            }
            
            
        }
    }

    public JSONObject toJSON( String addressPrefix ) throws JSONException {
        JSONObject json = new JSONObject();

        JSONObject bounds = new ProcessModelUtils(model).getBoundsJSON();
        json.put("bounds", bounds);

        JSONObject added = new JSONObject();
        JSONObject removed = new JSONObject();
        JSONObject updated = new JSONObject();

        //added elements
        added.put("edges", this.addedEdges);
        added.put("nodes", this.addedNodes);

        //removed elements
        removed.put("edges", this.removedEdges);
        removed.put("nodes", this.removedNodes);

        JSONArray updatedE = new JSONArray();

        for ( ProcessEdge e : updatedEdges ) {
            JSONObject jo = new JSONObject();
            jo.put("id", e.getId());
            jo.put("properties", this.propertiesToJSON(e));
            jo.put("metadata", new ProcessEdgeUtils( e ).getMetaJSON(addressPrefix));
            updatedE.put(jo);
        }

        JSONArray updatedN = new JSONArray();
        for ( ProcessNode n : updatedNodes ) {
            JSONObject jo = new JSONObject();
            jo.put("id", n.getId());

            if ( updateImage.contains( n.getId() ) ) {
                BufferedImage img = ProcessEditorServerUtils.createNodeImage(n);
                String newId = ImageStore.add( this.model, n, img);
                String newUri =  addressPrefix + model.getProcessModelURI() + "/nodes/" + newId;
                jo.put("image", newUri);
                jo.put("metadata", new ProcessNodeUtils(n).toJSON());
            }

            jo.put("properties", this.propertiesToJSON(n));
            updatedN.put(jo);
        }

        updated.put("edges", updatedE);
        updated.put("nodes", updatedN);

        json.put("add", added);
        json.put("remove", removed);
        json.put("update", updated);

        return json;
    }

    public Document toXML( String addressPrefix ) {
        Document doc = XMLHelper.newDocument();
        
        this.toXML(doc, null, addressPrefix);

        return doc;
    }

    public Element toXML( Document doc, Element parentEl, String addressPrefix ) {
        Element docEl;

        if ( parentEl == null )
            docEl = XMLHelper.addDocumentElement(doc, "difference");
        else
            docEl = XMLHelper.addElement(doc, parentEl, "difference");

        new ProcessModelUtils(model).addBoundsElement(doc, docEl);
        Element addEl = XMLHelper.addElement(doc, docEl, "add");
        for ( String id : addedNodes )
            XMLHelper.addElement(doc, addEl, "node").setAttribute("id", id);
        for ( String id : addedEdges )
            XMLHelper.addElement(doc, addEl, "edge").setAttribute("id", id);

        Element remEl = XMLHelper.addElement(doc, docEl, "remove");
        for ( String id : removedNodes )
            XMLHelper.addElement(doc, remEl, "node").setAttribute("id", id);
        for ( String id : removedEdges )
            XMLHelper.addElement(doc, remEl, "edge").setAttribute("id", id);

        Element upEl = XMLHelper.addElement(doc, docEl, "update");
        for ( ProcessEdge e : updatedEdges ) {
            Element eEl = XMLHelper.addElement(doc, upEl, "edge");
            eEl.setAttribute("id", e.getId());

            Element propEl = XMLHelper.addElement(doc, eEl, "properties");
            for ( String  key : e.getPropertyKeys() ) {
                Element pEl = XMLHelper.addElement(doc, propEl, "property");
                pEl.setAttribute("name", key);
                pEl.setAttribute("value", e.getProperty(key));
            }

            new ProcessEdgeUtils(e).addMetaXMLElement(doc, eEl, addressPrefix);
        }

        for ( ProcessNode n : updatedNodes ) {
            Element nEl = XMLHelper.addElement(doc, upEl, "node");
            nEl.setAttribute("id", n.getId());

            if ( updateImage.contains( n.getId() ) ) {
                BufferedImage img = ProcessEditorServerUtils.createNodeImage(n);
                String newId = ImageStore.add( this.model, n, img);
                String newUri = addressPrefix + "/nodes/" + newId;
                nEl.setAttribute("image", newUri);

                new ProcessNodeUtils(n).toXML(doc, nEl);
            }

            Element propEl = XMLHelper.addElement(doc, nEl, "properties");
            for ( String  key : n.getPropertyKeys() ) {
                Element pEl = XMLHelper.addElement(doc, propEl, "property");
                pEl.setAttribute("name", key);
                pEl.setAttribute("value", n.getProperty(key));
            }
        }

        return docEl;
    }

    private JSONArray propertiesToJSON( ProcessObject o ) throws JSONException {
        JSONArray a = new JSONArray();

        for ( String key : o.getPropertyKeys() ) {
            JSONObject jo = new JSONObject();
            jo.put("name", key);
            jo.put("value", o.getProperty(key));
            a.put(jo);
        }

        return a;
    }

}
