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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author fpu
 */
public class BPMN20DIImporter implements Importer {

    public BPMN20DIImporter() {
        // Do nothing
    }

    public List<ProcessModel> parseSource(File f) throws Exception {
        System.out.println("Parsing "+f);

        


        List<ProcessModel> models = new LinkedList<ProcessModel>();

        // Parse all contained diagrams

        return models;
    }

    public String getDisplayName() {
        return "BPMN 2.0 DI";
    }

    public String[] getFileTypes() {
        String[] types = {"bpmn"};
        return types;
    }

    public static void main(String[] argv) throws Exception {
        File testData = new File("models/referenzmodelle.bpmn");
        BPMN20DIImporter imp = new BPMN20DIImporter();
        imp.parseSource(testData);
    }

}
