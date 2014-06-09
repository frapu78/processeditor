/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * This class exports a PetriNetModel to the LoLA (Low Level Petri net Analyzer)
 * format. (see http://www2.informatik.hu-berlin.de/~kschmidt/lola.html)
 *
 * @author frank
 */
public class LoLAExporter implements Exporter {

    /**
     * Serializes the ProcessModel als LoLA file.
     * @param f
     * @param m
     * @throws java.lang.Exception
     */
    public void serialize(File f, ProcessModel m) throws Exception {

        if (!(m instanceof PetriNetModel)) throw new Exception("Unsupported Process Model type");

        String result = "";

        // 1. Export all places with comments for the ids
        String places = "";
        String markings = "";
        for (ProcessNode n : m.getNodes()) {
            if (n instanceof Place) {
                Place p = (Place) n;
                result += "{" + p.getProperty(Place.PROP_ID) + "->" + p.getText() + "}\n";
                places += p.getProperty(Place.PROP_ID) + ", ";
                if (p.getTokenCount() > 0) {
                    markings += "MARKING " + p.getProperty(Place.PROP_ID) + ": " + p.getTokenCount() + ";\n";
                }
            }
        }
        if (places.length() > 0) {
            places = places.substring(0, places.length() - 2);
        }
        result += "PLACE " + places + ";";

        // 2. Export all markings
        result += "\n\n" + markings;

        // 3. Export all transitions
        for (ProcessNode n : m.getNodes()) {
            if (n instanceof Transition) {
                Transition t = (Transition) n;

                result += "\n{" + t.getProperty(Transition.PROP_ID) + "->" + t.getText() + "}\n";
                result += "TRANSITION " + t.getProperty(Transition.PROP_ID) + "\n";
                // Process all incoming edges
                String consume = "CONSUME ";
                for (ProcessNode n2 : m.getPredecessors(n)) {
                    if (n2 instanceof Place) {
                        Place p2 = (Place) n2;
                        consume += p2.getProperty(Place.PROP_ID) + ": 1, ";
                    }
                }
                if (consume.length() > 0) {
                    consume = consume.substring(0, consume.length() - 2) + ";\n";
                }
                // Process all outgoing edges
                String produce = "PRODUCE ";
                for (ProcessNode n2 : m.getSuccessors(n)) {
                    if (n2 instanceof Place) {
                        Place p2 = (Place) n2;
                        produce += p2.getProperty(Place.PROP_ID) + ": 1, ";
                    }
                }
                if (produce.length() > 0) {
                    produce = produce.substring(0, produce.length() - 2) + ";\n";
                }

                result += consume + produce;
            }
        }

        // Write file to disk
        FileOutputStream fos = new FileOutputStream(f);
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        Writer w = new BufferedWriter(osw);
        w.write(result);
        w.flush();
        fos.close();
    }

    @Override
    public String getDisplayName() {
        return "Low Level Petri net Analyzer";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"lola"};
        return types;
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(PetriNetModel.class);
        return result;
    }
}
