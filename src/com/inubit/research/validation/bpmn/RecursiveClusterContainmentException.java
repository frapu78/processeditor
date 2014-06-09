/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn;

import com.inubit.research.validation.bpmn.adaptors.ClusterAdaptor;
import java.util.List;

/**
 *
 * @author tmi
 */
class RecursiveClusterContainmentException extends Exception {
    
    private List<ClusterAdaptor> cluster;

    public RecursiveClusterContainmentException(List<ClusterAdaptor> clusters) {
        super("self-containing cluster");
        this.cluster = clusters;
    }

    public List<ClusterAdaptor> getClusters() {
        return cluster;
    }
}
