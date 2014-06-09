/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.util.List;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author FSC
 */
public class AnimationSequenceFinishedEvent {

private List<ProcessObject> animatedNodes;
private long animationSequenceID;

    public AnimationSequenceFinishedEvent(List<ProcessObject> animatedNodes, long animationSequenceID) {
        this.animatedNodes = animatedNodes;
        this.animationSequenceID = animationSequenceID;
    }

    public List<ProcessObject> getAnimatedNodes() {
        return animatedNodes;
    }

    public long getAnimationSequenceID() {
        return animationSequenceID;
    }




}
