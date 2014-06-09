/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.manager;

import com.inubit.research.server.model.ServerModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the cache for models.
 * @author fel
 */
public class ModelCache {

    private Map<String, ServerModel> models = new HashMap<String, ServerModel>();
//    private Map<String, ServerModel> transientModels = new HashMap<String, ServerModel>();

    /**
     * Load the given model into cache
     * @param hm the model
     * @param id the id this model is referred by
     */
    public void loadModel(ServerModel sm, String id) {
        if (sm == null || id == null) {
            return;
        }

        // Check if already in models, in this case remove
        if (this.models.containsKey(id)) {
            this.models.remove(id);
        }
        //lazy loading
        this.models.put(id, sm);
    }

    /**
     * Get model from cache
     * @param id the id
     * @return <ul>
     *  <li> null, if this id does not refer to a model </li>
     *  <li> the model, otherwise </li>
     * </ul>
     */
    public ServerModel getModel(String id) {
        ServerModel result = this.models.get(id);
//        if (result == null) {
//            ServerModel hm = this.transientModels.remove(id);
//            if (hm == null) {
//                return null;
//            }
//            result = hm.load();
//            this.models.put(id, result);
//        }
        return result;
    }

    public void remove(String id) {
        this.models.remove(id);
//        this.transientModels.remove(id);
    }
}
