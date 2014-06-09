/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

/**
 * @author ff
 *
 */
public interface IAnimationListener {
	
	/**
	 * Informs the listener, that a state change/animation
	 * has been completed.
	 * @param node
	 */
	public void animationFinished(NodeAnimator node);

}
