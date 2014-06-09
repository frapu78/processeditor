/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.model;

import com.inubit.research.server.manager.FileSystemLocation;
import java.io.File;
import net.frapu.code.converter.ProcessEditorImporter;

import net.frapu.code.visualization.ProcessModel;


/**
 *
 * @author fel
 */
public class FileServerModel implements ServerModel{

    private ProcessModel  model;
    private FileSystemLocation fsl;
    private File file;
    
    public FileServerModel( File file, FileSystemLocation fsl ) {
        this.file = file;
        this.fsl = fsl;
    }

    public ProcessModel getModel() {
        if ( this.model == null ) {
            try {
                this.model = new ProcessEditorImporter().parseSource(this.file).get(0);
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }

        return this.model;
    }

    public ServerModel save(ProcessModel newModel, int version, String modelId, String comment, String folder) {
        return this.fsl.saveProcessModel(newModel, modelId, version);
    }

    @Override
    public boolean refresh() {
        // n/a
        return false;
    }

    @Override
    public String getModelName() {
        return this.getModel().getProcessName();
    }

    @Override
    public String getChecksum() {
        // n/a
        return "STATIC";
    }

    @Override
    public String getComment() {
        return this.getModel().getProperty(ProcessModel.PROP_COMMENT);
    }

    @Override
    public String getAuthor() {
        return this.getModel().getProperty(ProcessModel.PROP_AUTHOR);
    }

    @Override
    public String getCreationDate() {
        return this.getModel().getProperty(ProcessModel.PROP_CREATE_DATE);
    }

    @Override
    public String getLastUpdateDate() {
        return this.getModel().getProperty(ProcessModel.PROP_LASTCHECKIN);
    }

    @Override
    public void delete() {
        this.file.delete();
    }

}
