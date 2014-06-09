/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.model.DatabaseServerModel;
import com.inubit.research.server.model.ServerModel;
import com.inubit.research.server.persistence.DatabaseConnector;
import java.util.LinkedList;

/**
 *
 * @author fel
 */
public class DatabaseModelListProxy extends LinkedList<ServerModel> {
    private DatabaseConnector db;
    private String modelId;

    public DatabaseModelListProxy( DatabaseConnector dc, String modelId ) {
        super();
        this.db = dc;
        this.modelId = modelId;

        this.update();
    }

    private void update() {
        int size = db.getModelVersionCount(modelId);
        for( int i = 0; i < size; i++ ) {
            //auto increment for SQL databases starts with 1
            int version = i + 1;
            if ( version > super.size() ) {
                ServerModel m = new DatabaseServerModel(db, modelId, version);
                add(i, m);
            }
        }
    }

    @Override
    public int size() {
        return db.getModelVersionCount(modelId);
    }

    @Override
    public ServerModel get(int i) {
        int realSize = size();
        if ( i > super.size() && i + 1 <= realSize ) {
            for ( int k = super.size(); k + 1< realSize; k++ ) {
                ServerModel m = new DatabaseServerModel(db, modelId, k);
                add(k, m);
            }
        }
        return super.get(i);
    }

    @Override
    public ServerModel getLast() {
        int s = size();

        return this.get(s - 1);
    }
}
