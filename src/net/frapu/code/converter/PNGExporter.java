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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * This class exports a ProcessModel to a PNG file.
 *
 * @author frank
 */
public class PNGExporter implements Exporter {

    /**
     * Serializes the ProcessModel als PNG file.
     * @param f
     * @param m
     * @throws java.lang.Exception
     */
    public void serialize(File f, ProcessModel m) throws Exception {
        // Create new ProcessEditor
        ProcessEditor editor = new ProcessEditor(m);
        editor.setEditable(false);
        // Set prefered size
        editor.setSize(editor.getPreferredSize().width+50,
               editor.getPreferredSize().height+50);
        // Create buffered image
        BufferedImage img = new BufferedImage(editor.getSize().width,
                editor.getSize().height,BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        editor.paintComponent(g);
        // Write image to file
        ImageIO.write(img, "png", f);
    }

    /**
     * The PNG exporter can serialize all ProcessModels to a PNG file.
     * @return
     */
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ProcessModel.class);
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Portable Network Graphics";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"png"};
        return types;
    }

}
