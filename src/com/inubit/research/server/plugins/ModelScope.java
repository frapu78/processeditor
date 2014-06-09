/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import java.util.Set;
import net.frapu.code.visualization.ProcessModel;

/**
 * @author fel
 */
public interface ModelScope {
    public Set<Class<? extends ProcessModel>> getSupportedModels();
}
