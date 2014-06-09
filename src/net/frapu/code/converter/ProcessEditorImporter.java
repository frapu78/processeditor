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
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * Reads a model (*.model) in the ProcessEditor format.
 *
 * @author fpu
 */
public class ProcessEditorImporter implements Importer {
    
    private static ProcessEditorImporter instance = null;

    @Override
    public List<ProcessModel> parseSource(File f) throws Exception {
        try {
            List<ProcessModel> result = new LinkedList<ProcessModel>();
            ProcessModel model = ProcessUtils.parseProcessModelSerialization(new FileInputStream(f));
            if (model.getProcessName().isEmpty()) {
                model.setProcessName(f.getName());
            }
            result.add(model);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new UnsupportedFileTypeException("Not a ProcessEditor File!");
        }
    }

    @Override
    public String getDisplayName() {
        return "Process Editor files";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"model"};
        return types;
    }

    public static ProcessEditorImporter getInstance() {
        if (instance==null){
            instance = new ProcessEditorImporter();
        }
        return instance;
    }
    
    public ProcessModel load(String path) throws Exception {        
        File file = new File(path);
        return this.parseSource(file).get(0);
    }
}
