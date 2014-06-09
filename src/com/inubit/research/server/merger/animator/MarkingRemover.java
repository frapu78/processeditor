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
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */


public class MarkingRemover extends AnimationSequence {

    public static int ANIMATION_TIME = 500;
    public static int DELAY = 0;




    public MarkingRemover(ProcessEditor editor) {
        super(editor);
        setAnimationTime(ANIMATION_TIME);
        setDelay(DELAY);
    }

    public synchronized void resetMergeMarkings(ProcessModelMerger merger) {
        for (ProcessObjectMerger m : merger.getMergeRelations()) {
            if (m.getMergedObject()==null) continue;
            if (m.isDestinySolved()) continue;
            ProcessObject objectInModel = getEditor().getModel().getObjectById(m.getMergedObject().getId());
            if (objectInModel != null) {
                if (m.isDestinyRemove()) {
                    getEditor().getAnimator().removeProcessObject(objectInModel, getAnimationTime(), 0, false);
                } else {
                    getEditor().getAnimator().animateObject(objectInModel, m.getUnmarkedMergedObject(), getAnimationTime(), 0);
                }
            } else System.err.println("Object to reset not in Model");
        }
        System.out.println("reset Markings");
    }

    public void run() {
        if (getEditor().getMergeAnimator().getCurrentMerger()!=null) resetMergeMarkings(getEditor().getMergeAnimator().getCurrentMerger());
    }
}
