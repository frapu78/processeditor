/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2015 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

import de.erichseifert.vectorgraphics2d.SVGGraphics2D;


/**
 *
 * Exports a model to SVG format.
 *
 * @author fpu
 */
public class SVGExporter implements Exporter {

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {
        Graphics2D g2;

        // Create new ProcessEditor
        ProcessEditor editor = new ProcessEditor(m);
        editor.setEditable(false);
        // Set preferred size
        editor.setSize(editor.getPreferredSize().width,
                editor.getPreferredSize().height);

        /** SOURCE CODE FOR VECTORGRAPHICS2D FOLLOWS */
        SVGGraphics2D pdfOutput = new SVGGraphics2D(
                editor.getBounds().x,
                editor.getBounds().y,
                editor.getBounds().width,
                editor.getBounds().height);

        pdfOutput.setFontRendering(VectorGraphics2D.FontRendering.VECTORS);

        editor.paint(pdfOutput);

        byte[] output = pdfOutput.getBytes();

        FileOutputStream fo = new FileOutputStream(f);
        fo.write(output);
        fo.close();
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ProcessModel.class);
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Scalable Vector Graphics";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"svg"};
        return types;
    }
}
