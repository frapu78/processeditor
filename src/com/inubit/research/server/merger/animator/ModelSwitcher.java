/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.ProcessModelMerger;
import com.inubit.research.server.merger.ProcessObjectMerger;
import com.inubit.research.server.merger.gui.ConflictResolverEditor;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class ModelSwitcher extends AnimationSequence {

    private ProcessModelMerger merger;
    private ProcessModel from;
    private ProcessModel to;
    public static int ANIMATION_TIME = 1000;
    public static int DELAY = 0;
    private boolean actuallyRemoveDeletedObjects = false;
    private boolean markProcessObjects = true;
    private boolean removeRoutingPoints = true;

    public ModelSwitcher(ProcessEditor editor, ProcessModelMerger merger) {
        super(editor);
        if (merger == null) {
            throw new NullPointerException();
        }        
        this.merger = merger;
        setAnimationTime(ANIMATION_TIME);
        setDelay(DELAY);
    }

    public ModelSwitcher(ConflictResolverEditor editor, ProcessModel to) {
        super(editor);
        this.from = editor.getModel();
        this.to = to;        
        setAnimationTime(ANIMATION_TIME);
        setDelay(DELAY);
    }

    public boolean isActuallyRemoveDeletedObjects() {
        return actuallyRemoveDeletedObjects;
    }

    public void setActuallyRemoveDeletedObjects(boolean actuallyRemoveDeletedObjects) {
        this.actuallyRemoveDeletedObjects = actuallyRemoveDeletedObjects;
    }

    public boolean isMarkProcessObjects() {
        return markProcessObjects;
    }

    public void setMarkProcessObjects(boolean markProcessObjects) {
        this.markProcessObjects = markProcessObjects;
    }

    public boolean isRemoveRoutingPoints() {
        return removeRoutingPoints;
    }

    public void setRemoveRoutingPoints(boolean removeRoutingPoints) {
        this.removeRoutingPoints = removeRoutingPoints;
    }



    private void animateMerger(ProcessModelMerger merger) {
        getEditor().setModel(getEditor().getModel().clone());
        for (ProcessObjectMerger r : merger.getMergeRelations()) {
            ProcessObject animateFrom = r.getAnimateFrom();
            if (r.getAnimateFrom() != null) {
                animateFrom = getEditor().getModel().getObjectById(animateFrom.getId());
                if (animateFrom == null) {
//                        animateFrom = r.getAnimateFrom();
//                        animateFrom = getEditor().getModel().getObjectById(animateFrom.getId());
                    System.err.println("Warning: The source model in the merger is not the model in the editor");
                }
            } else if (!r.isDestinyNew()) {
                System.err.println("Warning: " + r + " is not marked as new and has no source. Override. Using Fallback. Check whether you supplied the correct merger.");
            }
            ProcessObject animateTo;
            if (isMarkProcessObjects()) {
                if (r.getAnimateTo()==null) continue;
                animateTo = r.getAnimateTo();
            } else {
                animateTo = r.getMergedObject();
                if (animateTo==null) continue;
            }
            if (animateFrom == null && animateTo == null) {
                throw new IllegalStateException("Merger is inconsistent");
            }
            if (actuallyRemoveDeletedObjects && r.isDestinyRemove()) {
                animateTo = null;
            }
            // remove routing points of deleted edges to avoid strange routing locations
//            if (animateTo != null && r.isDestinyRemove() && r.getMergedObject() instanceof ProcessEdge) {
//                ProcessEdge e = (ProcessEdge) animateTo;
//                e.clearRoutingPoints();
//            }
            getEditor().getAnimator().animateSubstitution(animateFrom, animateTo, getAnimationTime(), 0, false);
        }
    }

    public void run() {
//        if (!getEditor().getModel().equals(from)) {
//            System.err.println("Warning merger model does not match editor model");
//            //getEditor().setModel(merger.getModelFrom());
//        }
        this.from = merger.getModelFrom();
        this.to = merger.getModelTo();
        getEditor().getMergeAnimator().setCurrentMerger(merger);
        animateMerger(merger);
    }
}
