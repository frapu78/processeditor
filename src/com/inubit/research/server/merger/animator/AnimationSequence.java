/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.gui.ConflictResolverEditor;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public abstract class AnimationSequence implements Runnable {
    
    //static
    public static int DEFAULT_ANIMATION_TIME = 500;
    public static int DEFAULT_DELAY = 0;
    
    //members
    private int animationTime = DEFAULT_ANIMATION_TIME;
    private int delay = DEFAULT_DELAY;

    private ProcessEditor editor;


    //methods
    public AnimationSequence(ProcessEditor editor) {
        this.editor = editor;
    }

    public ProcessEditor getEditor() {
        return editor;
    }

    public final int getAnimationTime() {
        return animationTime;
    }

    public final void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setEditor(ConflictResolverEditor editor) {
        this.editor = editor;
    }

    public AnimationSequenceQueue getLocalAnimationQueue() {
        return getEditor().getMergeAnimator().getAnimationQueue();
    }
 
    
    
    



}
