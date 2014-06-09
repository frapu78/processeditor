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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public abstract class AbstractModelDiff implements ProcessModelDiff {
    protected  ProcessModel model1;
    protected ProcessModel model2;

    protected HashMap<String, ProcessObject> addedObjects =
            new HashMap<String, ProcessObject>();
    protected HashMap<String, ProcessObject> removedObjects =
            new HashMap<String, ProcessObject>();
    protected Relation<String, ProcessObjectDiff> changedObjects =
            new Relation<String, ProcessObjectDiff>();
    protected Relation<String, ProcessObjectDiff> equalObjects =
            new Relation<String, ProcessObjectDiff>();

    public Collection<ProcessObject> getAddedObjects() {
        return addedObjects.values();
    }

    public Collection<ProcessObject> getChangedObjects() {
        ArrayList<ProcessObject> result = new ArrayList<ProcessObject>(changedObjects.size());
        for (ProcessObjectDiff d : changedObjects.values()) {
            result.add(d.getObject2());
        }
        return result;
    }

    public Collection<ProcessObject> getRemovedObjects() {
        return removedObjects.values();
    }


    public Relation<String, ProcessObjectDiff> getChangedObjectDiffs() {
        return changedObjects;
    }

    public Relation<String, ProcessObjectDiff> getEqualObjectDiffs() {
        return equalObjects;
    }

    public ProcessModel getModel1() {
        return model1;
    }

    public ProcessModel getModel2() {
        return model2;
    }

        public ProcessObjectState getStatus(String objectID) {
        if(equalObjects.containsKey1(objectID) || equalObjects.containsKey2(objectID)) return ProcessObjectState.Equal;
        if(addedObjects.containsKey(objectID)) return ProcessObjectState.New;
        if(changedObjects.containsKey1(objectID) || changedObjects.containsKey2(objectID)) return ProcessObjectState.Changed;
        if(removedObjects.containsKey(objectID)) return ProcessObjectState.Removed;
        return null;
    }

    public ProcessObject getPartnerProcessObject(String objectID, ProcessModel origin) {
        ProcessObjectDiff res = getProcessObjectRelation(objectID, origin);
        if (res==null) return null;
        return (origin==model1) ? res.getObject2() : res.getObject1();
    }

   public ProcessObjectDiff getProcessObjectRelation(String objectID, ProcessModel origin) {
        if (origin!=model1 && origin!=model2) throw new IllegalArgumentException("ProcessModel does not participate in diff");
        ProcessObjectDiff result = null;
        Relation[] couples = {changedObjects,equalObjects};
        for (Relation<String,ProcessObjectDiff> h : couples) {
            result =(origin==model1) ? h.getWithKey1(objectID) : h.getWithKey2(objectID);
            if (result!=null) return result;
        }
        ProcessObject res = null;
        if (origin==model1) {
            res = removedObjects.get(objectID);
            if (res!=null) return new ProcessObjectDiff(res, model1,model2, ProcessObjectState.Removed);
        }
        if (origin==model2) {
            res = addedObjects.get(objectID);
            if (res!=null) return new ProcessObjectDiff(res, model1,model2, ProcessObjectState.New);
        }
        throw new IllegalArgumentException("Object ID not in diff");

    }

    public void dump() {
        for (ProcessObject o: getAddedObjects()) {
            System.out.println("ADDED "+o);
        }
        for (ProcessObject o: getRemovedObjects()) {
            System.out.println("REM "+o);
        }
        for (ProcessObject o: getChangedObjects()) {
            System.out.print("CHG "+o);
        }
    }
}
