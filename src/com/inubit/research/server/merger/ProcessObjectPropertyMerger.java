/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class ProcessObjectPropertyMerger {

    private String propertyName;
    private String originalValue;
    private String sourceValue;
    private String targetValue;
    private String mergedValue;
    private ProcessObject mergedObject;
    private boolean conflict;
    private boolean changed;

    public ProcessObjectPropertyMerger(String propertyName, String[] sourcePropDiff, String[] targetPropDiff, ProcessObject mergedObject) {
        this.propertyName = propertyName;
        originalValue = nullToEmptyString(sourcePropDiff[0]);
        if (!originalValue.equals(nullToEmptyString(targetPropDiff[0]))) {
            System.err.println("Property origins do not match. Source is " + sourcePropDiff[0] + " whereas target is " + targetPropDiff[0]);
        }
        sourceValue = nullToEmptyString(sourcePropDiff[1]);
        targetValue = nullToEmptyString(targetPropDiff[1]);
        this.mergedObject = mergedObject;
        createMergedProp();
        if (mergedObject == null && !isConflict()) {
            throw new AssertionError();
        }
    }

    private String nullToEmptyString(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    public ProcessObject getMergedObject() {
        return mergedObject;
    }

    public void setMergedObject(ProcessObject mergedObject) {
        this.mergedObject = mergedObject;
        if (isSettableAttribute(propertyName)) {
            conflict = false;
        } else {
            conflict = true;
        }
    }

    public String getMergedValue() {
        return mergedValue;
    }

    public void setMergedValue(String mergedValue) {
        this.mergedValue = mergedValue;
        getMergedObject().setProperty(propertyName, mergedValue);
        if (isSettableAttribute(propertyName)) {
            conflict = false;
        } else {
            conflict = true;
        }
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getSourceValue() {
        return sourceValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    private Set<String> getUnsettableProperties(Set<String> props) {
        Set<String> result = new HashSet<String>(getMergedObject().getPropertyKeys());
        result.retainAll(props);
        return result;
    }

    private boolean areSettableAttributes(Set<String> props) {
        return getUnsettableProperties(props).isEmpty();
    }

    private boolean isSettableAttribute(String prop) {
        HashSet set = new HashSet();
        set.add(prop);
        //return areSettableAttributes(set);
        return true;
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public void createMergedProp() {
        boolean sourceChanged = !originalValue.equals(sourceValue);
        boolean targetChanged = !originalValue.equals(targetValue);

        this.changed = true;
        mergedValue = null;

        if (sourceValue.equals(targetValue)) {
            this.changed = false;
            this.conflict = false;
            mergedValue = mergedObject.getProperty(propertyName);
        } else if (sourceChanged && !targetChanged) {
            if (isSettableAttribute(propertyName)) {
                this.conflict = false;
                mergedValue = sourceValue;
            } else {
                this.conflict = true;
            }
        } else if (!sourceChanged && targetChanged) {
            if (isSettableAttribute(propertyName)) {
                this.conflict = false;
                mergedValue = targetValue;
            } else {
                this.conflict = true;
            }
        } else if (sourceChanged && targetChanged) {
            this.conflict = true;
        }

    }
}
