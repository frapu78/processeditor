/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author uha
 */
public class ModelComparator {

    public ProcessObjectComparator comp = new ProcessObjectComparator();

    //compares two models returns 0 if both models are equal in essentail elements

    private ProcessObject failedObject1 = null;
    private ProcessObject failedObject2 = null;

    private ProcessModel m1;
    private ProcessModel m2;

    public boolean modelEquals(ProcessModel m1, ProcessModel m2) {
        this.m1=m1;
        this.m2=m2;
        for (ProcessObject object : m1.getObjects()) {
           ProcessObject objectM2 = m2.getObjectById(object.getId());
           if (objectM2==null || !comp.equals(object, objectM2)) {
               failedObject1 = object;
               failedObject2 = objectM2;
               return false;
           }
        }
        return true;
    }

    public String getReason() {
        ProcessObjectDiff diff = new ProcessObjectDiff(failedObject1, failedObject2, m1, m2, ProcessObjectDiff.ProcessObjectState.Changed);
        diff.setUseIgnoreProperties(false);
        return failedObject1.toString() + "//" + failedObject1.toString() + "//" + diff.getChangedProperties();
    }



}
