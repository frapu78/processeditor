/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.util.EventListener;

/**
 *
 * @author FSC
 */
public interface AnimationListener extends EventListener {

    void onAnimationSequenceFinished( AnimationSequenceFinishedEvent e );

}
