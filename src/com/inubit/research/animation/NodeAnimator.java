/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessObject;

/**
 * @author ff
 *
 */
public abstract class NodeAnimator {

	private ProcessObject f_object;
	private int f_delaySteps = 0;//steps to wait until animation starts
	private int f_animationSteps = 1;
	
	private int f_animationTime = 0;
	private int f_delay = 0; 
	private Animator f_animator;
	
	private boolean f_firstAnimationStep = true;
	
	private List<IAnimationListener> f_listeners = new ArrayList<IAnimationListener>();
	
	/**
	 * the Animator is needed when setting the animation and delay times.
	 * passing in null will cause an exception when calling those methods!
	 */
	public NodeAnimator(ProcessObject toAnimate,Animator parent) {
		f_object = toAnimate;
		f_animator = parent;
	}
	
	public void addListener(IAnimationListener listener) {
		f_listeners.add(listener);
	}
	
	public void removeListener(IAnimationListener listener) {
		f_listeners.remove(listener);
	}
	
	public ProcessObject getProcessObject() {
		return f_object;
	}

	private int f_currentTick = 0;


        public synchronized void setTick(int tick) {
            f_currentTick = tick;
            updateTick(0);
        }
	
	public synchronized void updateTick(int ticks) {
                if(f_currentTick > getSteps()) {
                    //updateTick too late, already finished
                    return;
                }
		f_currentTick+=ticks;
		if(f_currentTick < f_delaySteps) {
			//delayed, wait!
		}else {
			if(f_firstAnimationStep) {
				f_firstAnimationStep = false;
				firstStep();
			}
			setNewValues();
		}
		if(f_currentTick > getSteps()) {
			notifyListeners();
		}
	}
	
	/**
	 * is called after the delay finishes, 
	 * which means that the first animation step occurred.
	 * To make overlapping animations possible the current values of 
	 * the given node should be taken here, and not earlier!
	 */
	protected abstract void firstStep();

	/**
	 * 
	 */
	private void notifyListeners() {
		//copying so listeners can remove themselvs upon notification
		ArrayList<IAnimationListener> _copy = new ArrayList<IAnimationListener>();
		_copy.addAll(f_listeners);
		for(IAnimationListener l:_copy) {
			l.animationFinished(this);
		}
	}

	/**
	 * 
	 */
	protected abstract void setNewValues();
	
	
	/**
	 * takes delay into account!
	 * @param tick
	 * @return
	 */
	protected double getHyperbolicProgress() {
		double _progress = getLinearProgress();
		double _x = -3 + _progress*6;
		double _movementValue = Math.tanh(_x);
		_movementValue +=1;
		_movementValue /=2;
		return _movementValue;
	}
	
	/**
	 * takes delay into account!
	 * @param tick
	 * @return
	 */
	protected double getLinearProgress() {
		double _progress = (double)(getCurrentTick()-f_delaySteps)
							/(double)(getSteps()-f_delaySteps);
		return _progress;
	}

	/**
	 * used by the animator to control speed
	 */
	public void setAnimationTime(int time) {
		f_animationTime = time;
		f_animationSteps =  f_animationTime / f_animator.getSleepTime();
	}
	
	public int getAnimationTime() {
		return f_animationTime;
	}
	
	protected int getSteps() {
		return f_animationSteps + f_delaySteps;
	}

	public void setDelay(int delay) {
		this.f_delay = delay;
		f_delaySteps = getDelay()/f_animator.getSleepTime();
	}

	public int getDelay() {
		return f_delay;
	}
	
	protected Animator getAnimator() {
		return f_animator;
	}
	
	protected void setAnimator(Animator animator) {
		f_animator = animator;
	}
	
	protected int getCurrentTick() {
    	return f_currentTick;
    }
    
    protected void resetCurrentTick() {
    	f_currentTick = 0;
    }

}
