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

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 *
 * Exports a model to PDF format.
 *
 * @author fpu
 */
public class PDFExporter implements Exporter {

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {
        Graphics2D g2;

        // Create new ProcessEditor
        ProcessEditor editor = new ProcessEditor(m);
        editor.setEditable(false);
        // Set preferred size
        editor.setSize(editor.getPreferredSize().width,
                editor.getPreferredSize().height);

        Document document = new Document(new Rectangle(
                editor.getBounds().x,
                editor.getBounds().y,
                editor.getBounds().width,
                editor.getBounds().height
                ));
        PdfWriter writer;
        writer = PdfWriter.getInstance(document, new FileOutputStream(f));

        document.open();
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(editor.getSize().width, editor.getSize().height);

        g2 = tp.createGraphicsShapes(editor.getSize().width, editor.getSize().height);

        editor.paintComponent(g2);
        g2.dispose();

        cb.addTemplate(tp, 0, 0);

        document.close();


    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
    	Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ProcessModel.class);
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Portable Document Format";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"pdf"};
        return types;
    }
}
