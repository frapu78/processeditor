/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.frapu.code.visualization.DefaultRoutingPointLayouter;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 * @author fel
 */
public class TemporaryModelManager {
    static final String TMP_URI_PREFIX = "/models/tmp/";
    private Map<String, ProcessModel> models = new HashMap<String, ProcessModel>();

    /**
     * package private constructor, to avoid instantiation from outside this package
     */
    TemporaryModelManager() { }

    /**
     * Add model to this manager
     * @param model the model
     * @param baseId the base ID that is used for generating the temporary ID
     * @return the generated ID
     */
    public String addModel(ProcessModel model, String baseId) {
        String id = this.getUnusedId(baseId);

        model.setProcessModelURI(TMP_URI_PREFIX + id);

        model.addListener( model.getUtils().getRoutingPointLayouter() );
        this.models.put(id, model);

        return id;
    }

    /**
     * Get temporary model by ID
     * @param id the ID
     * @return <ul>
     *  <li> null, if no model is referred by this ID </li>
     *  <li> the model, otherwise </li>
     * </ul>
     */
    public ProcessModel getModel(String id) {
        return this.models.get(id);
    }

    /**
     * Remove model with given ID from this manager
     * @param id the ID
     */
    public void removeModel(String id) {
        this.models.remove(id);
    }


    /**
     * Generate new temporary ID
     * @param baseId the base ID that is extended to create temporary ID
     * @return the temporary ID
     */
    private String getUnusedId(String baseId) {
        Random random = new Random(System.currentTimeMillis());
        String newId = baseId + "_" + Math.abs(random.nextLong());

        while (models.containsKey(newId))
            newId = baseId + "_" + Math.abs(random.nextLong());

        return newId;
    }
}
