/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;
import net.frapu.code.visualization.ProcessObject;

/**
 * enables the Animation of NodeAnimationWrappers
 * @author ff
 *
 */
public class Animator extends Thread implements IAnimationListener {

    private int f_fps; // frames per second
    private int f_sleepTime;
    private volatile List<NodeAnimator> f_objects = new LinkedList<NodeAnimator>();
    private volatile List<ProcessObject> animationSequence = new LinkedList<ProcessObject>();
    private volatile boolean f_running = true;
    private volatile JPanel f_parent;
    private volatile boolean f_paused = true;
    private volatile int f_sleptFor;
    private EventListenerList AnimationListeners = new EventListenerList();
    private long animationSequenceID;
    /* an animation sequence is the largest set of animations 
     * whose animation periods intersect
     */

    //private static int f_number = 0;
    //private int f_myNumber = 0;
    /**
     *
     */
    public Animator(JPanel parent, int fps) {
        //f_myNumber = f_number++;
        f_parent = parent;
        animationSequenceID = 0;
        setFPS(fps);
    }

    public void addObjectToAnimate(NodeAnimator node) {
        if (isFinished()) {
            animationSequenceID++;
        }
        f_objects.add(node);
        animationSequence.add(node.getProcessObject());
        node.addListener(this);
        if (f_objects.size() > 0) {
            this.setPaused(false);
        }
    }

    /**
     * is called from the NodeAnimator when it has finished its animation.
     * the object will then be removed and the thread will go into a less
     * resource consuming mode, if no object to animate is left
     */
    @Override
    public synchronized void animationFinished(NodeAnimator node) {
        f_objects.remove(node);
        node.removeListener(this);
        if (isFinished()) {
            //nothing to animate anymore
            this.setPaused(true);
            this.notifyAllAnimationsFinished(new AnimationSequenceFinishedEvent(animationSequence, animationSequenceID));


        }
    }

    /**
     * @return
     */
    public int getSleepTime() {
        return f_sleepTime;
    }

    @Override
    public void run() {
        int overSleepTime = 0;
        int excess = 0;
        long updateTime = 0;
        while (f_running) {
            try {
                if (!f_paused) {
                    //System.out.println("------------------------------------------------");
                    long start = System.nanoTime();
                    updateTick(1);
                    if (f_parent != null) {
                        synchronized (f_parent) {
                            f_parent.repaint();
                        }
                    }
                    updateTime += (System.nanoTime() - start) / 1000000;
                    //System.out.println("time to update: "+updateTime);
                    int sleep = (int) (f_sleepTime - overSleepTime - updateTime);
                    overSleepTime = 0;
                    //System.out.println("sleeping "+sleep);
                    if (sleep > 0) {
                        //usual handling
                        start = System.nanoTime();
                        sleep(sleep);
                        long realSleep = (System.nanoTime() - start) / 1000000;
                        overSleepTime = (int) (sleep - realSleep); //to ms
                        //System.out.println("real sleep "+realSleep);
                        //System.out.println("oversleep "+overSleepTime);
                        f_sleptFor += f_sleepTime;
                        updateTime = 0;
                    } else {
                        excess += -sleep;
                        //updating took longer than a frame is!
                        if (excess > f_sleepTime) {
                            //do more update to recover speed....
                            //System.out.println("skipping "+(excess/f_sleepTime));
                            start = System.nanoTime();
                            updateTick(excess / f_sleepTime);
                            excess %= f_sleepTime;
                            //measuring this update too and take it into account later
                            updateTime = (System.nanoTime() - start) / 1000000;
                        }
                        f_sleptFor += f_sleepTime - sleep;
                    }
                } else {
                    //costing less CPU Time
                    sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * sets a whole new list of objects to animate and starts the animation
     * @param value
     */
    public synchronized void setAnimationObjects(List<NodeAnimator> value) {
        animationSequenceID++;
        f_objects = value;
        for (NodeAnimator n : f_objects) {
            n.addListener(this);
        }
        if (f_objects.size() > 0) {
            this.setPaused(false);
        }
    }

    /**
     * @param fps
     */
    private void setFPS(int fps) {
        f_fps = fps;
        f_sleepTime = 1000 / f_fps;
    }

    public synchronized void setParent(JPanel parent) {
        f_parent = parent;
    }

    protected void setPaused(boolean value) {
        //restarting again
        //System.out.println("Animator"+f_myNumber+": paused-"+value);
        f_paused = value;
        f_sleptFor = 0;
    }

    public void setRunning(boolean value) {
        if (value == false) {
            f_running = false;
            //kills this Thread
        }
    }

    public int toSteps(int time) {
        return (int) ((f_fps * time) / 1000);
    }

    /**
     * sends a tick update to all NodeAnimators
     */
    private synchronized void updateTick(int ticks) {
        if (f_objects != null) {
            //working on copy to avoid concurrent modification
            synchronized (f_objects) {
                LinkedList<NodeAnimator> _copy = new LinkedList<NodeAnimator>(f_objects);
                for (NodeAnimator a : _copy) {
                    a.updateTick(ticks);
                }
            }
        }
    }

    public boolean isFinished() {
        return this.f_objects.size() == 0;
    }

    public synchronized void finishAnimations() {
        if (f_objects != null) {
            //working on copy to avoid concurrent modification
            LinkedList<NodeAnimator> _copy = new LinkedList<NodeAnimator>();
            _copy.addAll(f_objects);
            for (NodeAnimator a : _copy) {
                a.setTick(Integer.MAX_VALUE / 8);
            }
        }
    }

    public synchronized void waitForAnimationsToFinish() {
        if (isFinished()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(Animator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public long getAnimationSequenceID() {
        return animationSequenceID;
    }

    /*
     *
     *
     *  Event listening
     *
     */
    public void addAnimationListener(AnimationListener listener) {
        AnimationListeners.add(AnimationListener.class, listener);
    }

    public void removeAnimationListener(AnimationListener listener) {
        AnimationListeners.remove(AnimationListener.class, listener);
    }

    public synchronized void notifyAllAnimationsFinished(AnimationSequenceFinishedEvent event) {
        for (AnimationListener l : AnimationListeners.getListeners(AnimationListener.class)) {
            String eventName = event.getClass().getName();
            if (eventName.equals(AnimationSequenceFinishedEvent.class.getName())) {
                l.onAnimationSequenceFinished(event);
            } else {
                throw new IllegalArgumentException("does not notify event type");
            }
        }
        notifyAll();
    }
}
