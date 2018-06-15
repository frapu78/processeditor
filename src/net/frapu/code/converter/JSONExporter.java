/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2018 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.Transition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * This class exports a ProcessModel to the JSON format.
 *
 * @author frank
 */
public class JSONExporter implements Exporter {

    /**
     * Serializes the ProcessModel als JSON file.
     * @param f
     * @param m
     * @throws Exception
     */
    public void serialize(File f, ProcessModel m) throws Exception {

        JSONObject result = new JSONObject();

        // Id, Name, Type
        result.put("id", m.getId());
        result.put("type", m.getClass().getName());
        result.put("name", m.getProcessName());

        // Add ProcessNodes
        JSONArray nodeArray = new JSONArray();
        result.put("nodes", nodeArray);
        for (ProcessNode n: m.getNodes()) {
            JSONObject node = new JSONObject();
            for (String key: n.getPropertyKeys()) {
                node.put(key, n.getProperty(key));
            }
            nodeArray.put(node);
        }

        // Add ProcessEdges
        JSONArray edgeArray = new JSONArray();
        result.put("edges", edgeArray);
        for (ProcessEdge e: m.getEdges()) {
            JSONObject edge = new JSONObject();
            for (String key: e.getPropertyKeys()) {
                edge.put(key, e.getProperty(key));
            }
            edgeArray.put(edge);
        }

        // Add Properties
        JSONObject propsObject = new JSONObject();
        result.put("properties", propsObject);
        for (String p: m.getPropertyKeys()) {
            propsObject.put(p, m.getProperty(p));
        }

        // Write file to disk
        FileOutputStream fos = new FileOutputStream(f);
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        Writer w = new BufferedWriter(osw);
        w.write(result.toString());
        w.flush();
        fos.close();

    }

    @Override
    public String getDisplayName() {
        return "JSON (JavaScript Object Notation";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"json"};
        return types;
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ProcessModel.class);
        return result;
    }
}
