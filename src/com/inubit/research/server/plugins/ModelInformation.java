/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.manager.ModelManager;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Data object containing information about the current model and the selection within that model.
 * @author fel
 */
public class ModelInformation {
    private ProcessModel pm = null;
    private Set<String> selNodeIDs = new HashSet<String>();
    private Set<String> selEdgeIDs = new HashSet<String>();

    private ModelInformation( ProcessModel pm, Set<String> selNodeIDs, Set<String> selEdgeIDs ) {
        this.pm = pm;
        this.selEdgeIDs = selEdgeIDs;
        this.selNodeIDs = selNodeIDs;
    }

    public ProcessModel getProcessModel() {
        return pm;
    }

    public Set<String> getSelEdgeIDs() {
        return selEdgeIDs;
    }

    public Set<String> getSelNodeIDs() {
        return selNodeIDs;
    }

    public static ModelInformation forDocument( Document doc ) {
        String id = doc.getDocumentElement().getAttribute("id");

        if (id == null)
            return null;
        
        ProcessModel pm = ModelManager.getInstance().getTemporaryModel(id);
        Set<String> selNodeIds = getIdSet("node", doc);
        Set<String> selEdgeIds = getIdSet("edge", doc);

        return new ModelInformation(pm, selNodeIds, selEdgeIds);
    }

    public static ModelInformation forJSON( JSONObject json ) throws JSONException {
        String id = json.getString("id");
        ProcessModel pm = ModelManager.getInstance().getTemporaryModel(id);
        
        Set<String> nodeIds = getIdSet(json.getJSONArray("nodes"));
        Set<String> edgeIds = getIdSet(json.getJSONArray("edges"));

        return new ModelInformation(pm, nodeIds, edgeIds);
    }

    private static Set<String> getIdSet( String tagName , Document doc ) {
        Set<String> ids = new HashSet<String>();

        if ( tagName != null && doc != null ) {
            NodeList nodes = doc.getElementsByTagName(tagName);
            //the ID is assumed to be set via the "id"-attribute of the node
            for (int i = 0; i < nodes.getLength(); i++)
                ids.add(nodes.item(i).getAttributes().getNamedItem("id").getNodeValue());
        }

        return ids;
    }

    private static Set<String> getIdSet( JSONArray array ) throws JSONException {
        Set<String> ids = new HashSet<String>();

        for ( int i = 0; i < array.length(); i++ )
            ids.add( array.getString(i) );

        return ids;
    }

}
