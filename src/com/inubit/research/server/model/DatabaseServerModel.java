/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.model;

import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.persistence.DatabaseConnector;
import com.inubit.research.server.persistence.DatabaseSchema;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author fel
 */
public class DatabaseServerModel implements ServerModel {
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ProcessModel model;
    private DatabaseConnector db;
    private String id;
    private int version;
    private String author;
    private String name;
    private String creationComment;
    private String creationDate;

    public DatabaseServerModel( DatabaseConnector dc, String id, int version ) {
        this.db = dc;
        this.id = id;
        this.version = version;
    }

    public void delete() {
        db.deleteModel(id);
    }

    public ProcessModel getModel() {
        if ( model == null ) {
            model = db.getModel(id, version);
        }

        return model;
    }

    public ServerModel save(ProcessModel m, int version, String modelId, String comment, String folder) {
        try {
            File tmpFile = new File(ProcessEditorServerHelper.TMP_DIR + "/" + (Math.random() * 100000000) + ".model");
            new ProcessEditorExporter().serialize(tmpFile, m);
            String currentDate = SDF.format(Calendar.getInstance().getTime());
            int newVersion = db.addModelVersion(tmpFile, id, null, comment, currentDate);
            return new DatabaseServerModel(db, id, newVersion);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean refresh() {
        return false;
    }

    public String getModelName() {
        if ( name == null )
            this.fetchModelAttributes();

        return this.name;
    }

    public String getChecksum() {
         return "STATIC";
    }

    public String getComment() {
         if ( creationComment == null )
             this.fetchModelAttributes();

        return this.creationComment;
    }

    public String getAuthor() {
         if ( author == null )
             this.fetchModelAttributes();

        return this.author;
    }

    public String getCreationDate() {
        if ( creationDate == null )
             this.fetchModelAttributes();

        return this.creationDate;
    }

    public String getLastUpdateDate() {
        int c = db.getModelVersionCount(id);
        Object o = db.selectSingleAttribute( DatabaseSchema.Attribute.VERSION_CREATED, DatabaseConnector.EntityType.VERSION, new Object[] {id, Integer.valueOf(version)});

        if ( o != null )
            return SDF.format(o);

        return null;
    }

    private void fetchModelAttributes() {
        DatabaseSchema.Attribute[] atts = { DatabaseSchema.Attribute.MODEL_AUTHOR, DatabaseSchema.Attribute.MODEL_NAME, DatabaseSchema.Attribute.MODEL_COMMENT, DatabaseSchema.Attribute.MODEL_CREATION_DATE };
        Object[] values = db.selectAttributes(atts, DatabaseConnector.EntityType.MODEL, new String[] { id });

        this.author = (String) values[0];
        this.name = (String) values[1];
        this.creationComment = (String) values[2];
        this.creationDate = SDF.format( values[3] );
    }

}
