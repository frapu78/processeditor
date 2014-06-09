/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class ModelCheckUtils {

    public static final boolean testing = false;

    static boolean IDsAreUnique(ProcessModel model) {
        if (testing) {
            for (ProcessObject o1 : model.getObjects()) {
                for (ProcessObject o2 : model.getObjects()) {
                    if (o1.getId().equals(o2.getId()) && o1 != o2) {
                        System.err.println("ID" + o1.getId() + " not unique " +o1 + " / " + o2);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }
    
    static boolean containsEdgesToNull (ProcessModel model) {
        if (testing) {
            for (ProcessEdge e :model.getEdges()) {
                if (e.getSource()==null) {
                    System.err.println(e + " getSource()==null");
                    return false;
                }
                if (e.getTarget()==null) {
                    System.err.println(e + " getTarget()==null");
                    return false;
                }
            }
        }
        return false;
    }


}
