/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import com.inubit.research.server.merger.ProcessObjectDiff.ProcessObjectState;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * This class provides support for creating a diff between two versions of a
 * ProcessModels. The difference detection is id-based.
 *
 * @author fpu
 * @author uha
 */
public class IDbasedModelDiff extends AbstractModelDiff {

    public IDbasedModelDiff() {
    }

    /**
     * Compares v2 against v1 (additions, removals, changes). The comparison
     * is only done at construction time!
     * @param v1
     * @param v2
     */



    public IDbasedModelDiff(ProcessModel v1, ProcessModel v2) {
        compare(v1, v2);
    }

    public void compare(ProcessModel m1, ProcessModel m2) {
        this.model1 = m1;
        this.model2 = m2;
        compareModels();
    }



    private void compareModels() {
        // Iterate over old model, search for removed objects / changed props
        for (ProcessObject o1: model1.getObjects()) {
            ProcessObject o2 = model2.getObjectById(o1.getId());
            if (o2==null) {
                removedObjects.put(o1.getId(), o1);
            } else {
                ProcessObjectDiff tmp = new ProcessObjectDiff(o1, o2, model1, model2, ProcessObjectState.Changed);
                // Check if properties are the same                
                    if (!tmp.propertiesEqual()) {
                        changedObjects.put(tmp);
                    } else {
                        tmp.setStatus(ProcessObjectState.Equal);
                        equalObjects.put(tmp);
                    }
                   }

        }
        // Iterate over new model, search for added objects
        for (ProcessObject o: model2.getObjects()) {
            ProcessObject p = model1.getObjectById(o.getId());
            if (p==null) {
                addedObjects.put(o.getId(), o);
            }
        }
    } 

}
