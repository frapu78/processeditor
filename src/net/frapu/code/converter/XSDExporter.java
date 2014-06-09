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
import net.frapu.code.visualization.uml.ClassModel;

/**
 *
 * @author fpu
 */
public class XSDExporter implements Exporter {

    public final static String ERROR_NO_DATA = "No XSD data found!";

    @Override
    public void serialize(File f, ProcessModel m) throws Exception {
        if (!(m instanceof ClassModel)) throw new Exception("Unsupported model type for XSD export");
        if (m.getProperty(ClassModel.PROP_DATA)==null) throw new Exception(ERROR_NO_DATA);
         if (m.getProperty(ClassModel.PROP_DATA).isEmpty()) throw new Exception(ERROR_NO_DATA);

        FileOutputStream fos = new FileOutputStream(f);
        // create a Writer that converts Java character stream to UTF-8 stream
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
        Writer w = new BufferedWriter(osw);
        w.write(m.getProperty(ClassModel.PROP_DATA));
        w.flush();
        fos.close();
    }

    @Override
    public Set<Class<? extends ProcessModel>> getSupportedModels() {
        Set<Class<? extends ProcessModel>> result = new HashSet<Class<? extends ProcessModel>>();
        result.add(ClassModel.class);
        return result;
    }

    @Override
    public String getDisplayName() {
        return "XML Schema";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"xsd"};
        return types;    }

}
