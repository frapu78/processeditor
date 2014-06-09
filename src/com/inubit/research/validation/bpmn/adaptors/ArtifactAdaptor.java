/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.adaptors;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Artifact;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.Group;
import net.frapu.code.visualization.bpmn.TextAnnotation;

/**
 *
 * @author tmi
 */
public class ArtifactAdaptor extends NodeAdaptor {

    public static boolean canAdapt(ProcessNode node) {
        return node == null || node instanceof Artifact;
    }

    protected ArtifactAdaptor(ProcessNode adaptee, ModelAdaptor model) {
        super(adaptee, model);
    }

    public ArtifactAdaptor(Artifact adaptee, ModelAdaptor model) {
        super (adaptee, model);
    }

    @Override
    public boolean isAdaptable(ProcessNode node) {
        return ArtifactAdaptor.canAdapt(node);
    }

    @Override
    public Artifact getAdaptee() {
        return (Artifact) super.getAdaptee();
    }

    @Override
    public boolean isArtifact() {
        return true;
    }

    @Override
    public boolean isData() {
        return isDataObject() || isDataStore();
    }

    public boolean isDataObject() {
        return getAdaptee() instanceof DataObject;
    }

    public boolean isDataStore() {
        return getAdaptee() instanceof DataStore;
    }

    @Override
    public boolean isTextAnnotation() {
        return getAdaptee() instanceof TextAnnotation;
    }

    public boolean isGroup() {
        return getAdaptee() instanceof Group;
    }

    @Override
    public boolean isAllowedInChoreography() {
        return isTextAnnotation() || isGroup();
    }
}
