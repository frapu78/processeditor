/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.model;

import net.frapu.code.visualization.ProcessModel;

/**
 * Interface for all models that can be controlled by this server.
 * @author fel
 */
public interface ServerModel {

    /**
     * Delete this model
     */
    public void delete();

    /**
     * Returns the model. Fetches it from its persistence layer if requested.
     * @return
     */
    public ProcessModel getModel();

    /**
     * Persists the model.
     * @return
     */
    public ServerModel save(ProcessModel m, int version, String modelId, String comment, String folder);

    /**
     * Refreshes the model from its source.
     * @return
     */
    public boolean refresh();

    /**
     * Returns the name of the loaded model.
     * @return
     */
    public String getModelName();

    /**
     * Returns the recent checksum checksum of the model.
     * @return
     */
    public String getChecksum();

    /**
     * Returns the recent comment of this model.      
     * @return
     */
    public String getComment();

    /**
     * Returns the author of this model.
     * @return
     */
    public String getAuthor();

    /**
     * Returns the creation date of this model.
     * @return
     */
    public String getCreationDate();

    /**
     * Returns the last update of this model.
     * @return
     */
    public String getLastUpdateDate();
}
