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
public class MarkingSetter extends AnimationSequence {

    public static int ANIMATION_TIME = 500;
    public static int DELAY = 0;



    public MarkingSetter(ProcessEditor editor) {
        super(editor);
        setAnimationTime(ANIMATION_TIME);
        setDelay(DELAY);
    }



    public synchronized void setMergeMarkings(ProcessModelMerger merger) {
        for (ProcessObjectMerger m: merger.getMergeRelations()) {
            if (m.getMergedObject()==null) continue;
            ProcessObject objectInModel = getEditor().getModel().getObjectById(m.getMergedObject().getId());
            if (m.getAnimateTo()==null) continue;
            if (m.isDestinyRemove()) {
                getEditor().getAnimator().addProcessObject(m.getAnimateTo(), getAnimationTime());
            } else {
                getEditor().getAnimator().animateObject(objectInModel, m.getAnimateTo(), getAnimationTime(), 0);
            }
        }
        System.out.println("set Markings");
    }

    public void run() {
        setMergeMarkings(getEditor().getMergeAnimator().getCurrentMerger());
    }



}
