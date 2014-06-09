/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.testUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.frapu.code.converter.ProcessEditorImporter;
import net.frapu.code.visualization.DefaultRoutingPointLayouter;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author uha
 */
public class TestUtils {

    private static TestUtils instance = null;

    private TestUtils() {};

    public static TestUtils getInstance() {
        if (instance==null) {
            instance = new TestUtils();
        }
        return instance;
    }


    public static String getLayoutDebugMessage(ProcessEdge edge) {
        return "Edge: "+ edge + "Routing Points: " + edge.getRoutingPoints() +
                "Source: " + edge.getSource() + "Source Pos: " + edge.getSource().getPos() + "Source Dimension: " + edge.getSource().getBounds() +
                "Target: " + edge.getTarget() + "Target Pos: " + edge.getTarget().getPos() + "Target Dimension: " + edge.getTarget().getBounds() +
                " Delta Intersect?" + (new DefaultRoutingPointLayouter()).deltaIntersect(edge) + " isStraight: " + (new DefaultRoutingPointLayouter()).isStraight(edge,0,0) + "nodesEmpty: " + (new DefaultRoutingPointLayouter()).nodesEmpty(edge) + " endDirectionsCorrect: " + (new DefaultRoutingPointLayouter()).endDirectionsCorrect(edge, true);
    }

    public static void getModelCode(ProcessModel model) throws UnsupportedEncodingException, DataFormatException, Exception {
         // Encode a String into bytes
         String inputString = model.getSerialization().toString();
         System.out.println(inputString);
         byte[] input = inputString.getBytes("UTF-8");

         // Compress the bytes
         byte[] output = new byte[100];
         Deflater compresser = new Deflater();
         compresser.setInput(input);
         compresser.finish();
         int compressedDataLength = compresser.deflate(output);

         String codeString = new String(output, 0, output.length, "ASCII");
         System.out.println(codeString);

         // Decompress the bytes
         Inflater decompresser = new Inflater();
         decompresser.setInput(output, 0, compressedDataLength);
         byte[] result = new byte[100];
         int resultLength = decompresser.inflate(result);
         decompresser.end();

         // Decode the bytes into a String
         String outputString = new String(result, 0, resultLength, "UTF-8");
         System.out.println(outputString);
    }

    public static ProcessModel loadTestModel(String filename) {
        try {
            ProcessEditorImporter importer = new ProcessEditorImporter();
            File file = new File("models\\" + filename);
            return importer.parseSource(file).get(0);
        } catch (Exception ex) {
            Logger.getLogger(TestUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
