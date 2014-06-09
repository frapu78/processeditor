/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import java.util.LinkedList;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author uha
 */
public class ProcessObjectComparator {
    
    private LinkedList<String> excludedProps = new LinkedList<String>();

    //compares all properties returns true, if they are all equal
    public boolean equals(ProcessObject processObject1, ProcessObject processObject2) {
       for (String propKey : processObject1.getPropertyKeys()) {
           if (excludedProps.contains(propKey)) {
               continue;
           }
           //TODO Solve
           if (propKey.equals(Cluster.PROP_CONTAINED_NODES)) continue;
           if (!processObject1.getProperty(propKey).equals(processObject2.getProperty(propKey))) {
               return false;
           }
       }
       if (processObject1.getAlpha()!=processObject2.getAlpha())
           return false;
       return true;
    }

    public void exclude(String property) {
        excludedProps.add(property);
    }

    public void include(String property) {
        excludedProps.remove(property);
    }

}
