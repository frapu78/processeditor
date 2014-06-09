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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import net.frapu.code.visualization.ProcessModel;

import org.w3c.dom.Document;

import com.inubit.research.server.ProcessEditorServerUtils;

/**
 *
 * Exports a model to the ProcessEditor (*.model) format.
 *
 * @author fpu
 */
public class ProcessEditorExporter implements Exporter {

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {
        FileOutputStream fos = new FileOutputStream(f);
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        Document doc = (m.getSerialization());
        ProcessEditorServerUtils.writeXMLtoStream(osw,doc);
        fos.close();
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
    	Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ProcessModel.class);
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Process Editor Files";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"model"};
        return types;    }

}
