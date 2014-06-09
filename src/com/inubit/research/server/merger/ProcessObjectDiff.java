/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author FSC
 */
public class ProcessObjectDiff implements Tupel {

    public enum ProcessObjectState {New, Changed, Removed, Equal};



        public static final HashSet<String> ignoreProperties= new HashSet<String>();
        
        static {
            ignoreProperties.add(ProcessNode.PROP_XPOS);
            ignoreProperties.add(ProcessNode.PROP_YPOS);
            ignoreProperties.add(ProcessNode.PROP_HEIGHT);
            ignoreProperties.add(ProcessNode.PROP_WIDTH);
            ignoreProperties.add(ProcessEdge.PROP_POINTS);
            ignoreProperties.add(Cluster.PROP_CONTAINED_NODES);
            ignoreProperties.add(ProcessEdge.PROP_COLOR_ARC);
            ignoreProperties.add(ProcessNode.PROP_BACKGROUND);
            ignoreProperties.add(ProcessEdge.PROP_SOURCE_DOCKPOINT);
            ignoreProperties.add(ProcessEdge.PROP_TARGET_DOCKPOINT);
        }
         
    
   
    private boolean useIgnoreProperties = true;

    private ProcessObject object1;
    private ProcessObject object2;

    private ProcessObjectState state;

    private ProcessModel object1Origin;
    private ProcessModel object2Origin;



    public ProcessModel getObject1Origin() {
        return object1Origin;
    }

    public ProcessObjectState getState() {
        return state;
    }

    public void setStatus(ProcessObjectState status) {
        this.state = status;
    }

    public ProcessModel getObject2Origin() {
        return object2Origin;
    }

    public boolean isUseIgnoreProperties() {
        return useIgnoreProperties;
    }

    public void setUseIgnoreProperties(boolean useIgnoreProperties) {
        this.useIgnoreProperties = useIgnoreProperties;
    }



    


    @Override
    public String getKey1() {
        if (getObject1()==null) return null;
        return getObject1().getId();
    }

    @Override
    public String getKey2() {
        if (getObject2()==null) return null;
        return  getObject2().getId();
    }


    public ProcessObjectDiff(ProcessObject object, ProcessModel origin1, ProcessModel origin2, ProcessObjectState processObjectStatus) {
        if (object==null || origin1==null || origin2==null) throw new IllegalArgumentException("null is not permited");
        switch (processObjectStatus) {
            case New: init(null, object, origin1, origin2,processObjectStatus);
                      break;
            case Removed: init(object, null, origin1, origin2,processObjectStatus);
                          break;
            default: throw new IllegalArgumentException("This constructor only works with new and removed objects");
        }
    }

    public ProcessObjectDiff(ProcessObject object1, ProcessObject object2,
            ProcessModel object1Origin, ProcessModel object2Origin, ProcessObjectState status) {
        init(object1, object2, object1Origin, object2Origin, status);
    }

    private void init(ProcessObject object1, ProcessObject object2,
            ProcessModel object1Origin, ProcessModel object2Origin, ProcessObjectState status) {
        this.object1 = object1;
        this.object2 = object2;
        this.object1Origin = object1Origin;
        this.object2Origin = object2Origin;
        this.state = status;
}



    /**
     * @return the object1
     */
    public ProcessObject getObject1() {
        return object1;
    }

    /**
     * @return the object2
     */
    public ProcessObject getObject2() {
        return object2;
    }

    public boolean hasId(String ID) {
        return object1.getId().equals(ID) || object2.getId().equals(ID);
    }
    /*
     * @return returns true, if both objects have the attribute and the attributes are not equal()
     */

    public boolean propertiesEqual() {
        return getChangedProperties().isEmpty();
    }

    public String[] getPropertyPair(String propertyKey) {
        String[] result = new String[2];
        result[0] = getObject1()==null ? null : getObject1().getProperty(propertyKey);
        result[1] = getObject2()==null ? null : getObject2().getProperty(propertyKey);
        return result;
    }

    public HashMap<String, String[]> getChangedProperties() {
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        String[] s = new String[2];
        Set<String> propKeys = new HashSet<String>();
        if (getObject1()!=null) propKeys.addAll(getObject1().getPropertyKeys());
        if (getObject2()!=null) propKeys.addAll(getObject2().getPropertyKeys());
        for (String key: propKeys) {
            //do not consider semantically irrelevant properties
            if (useIgnoreProperties && ignoreProperties.contains(key)) {
                continue;
            }

            String v2Value = getObject2()==null ? null : getObject2().getProperty(key);
            String v1Value = getObject1()==null ? null : getObject1().getProperty(key);
            if (v1Value==null && v2Value==null) continue;
            if (v1Value==null && v2Value!=null) {
                s[0] = "";
                s[1] = v2Value;
                map.put(key, s);
            } else if (v1Value!=null && v2Value==null) {
                s[0] = v1Value;
                s[1] = "";
                map.put(key, s);
            } else if (!v1Value.equals(v2Value)) {
                s[0] = v1Value;
                s[1] = v2Value;
                map.put(key, s);
            }
        }
        if (!map.isEmpty() && this.object1.getId().equals("31512035")){
            System.out.println("bla");
        }
        return map;
    }
 





}
