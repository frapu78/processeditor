/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author tmi
 */
public interface ProcessObjectAdaptor {

    public ProcessObject getAdaptee();
    public String getProperty(String key);
    public boolean isNode();
    public boolean isEdge();
}
