/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.animation.AnimationListener;
import com.inubit.research.animation.AnimationSequenceFinishedEvent;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public class AnimationSequenceQueue implements AnimationListener {

    private LinkedList<AnimationSequence> animationQueue;
    private LinkedList<ProcessEditor> listeningTo = new LinkedList<ProcessEditor>();
    private long runningAnimationSequence = -1;
    private boolean isClearing = false;
    private LinkedList<LinkedList<AnimationSequence>> hyperHiberQueue = new LinkedList<LinkedList<AnimationSequence>>();

    public AnimationSequenceQueue() {
        animationQueue = new LinkedList<AnimationSequence>();
    }

    public synchronized void queue(AnimationSequence seq) {
        insert(seq, null);
    }

    public synchronized void insert(List<AnimationSequence> seqQueue, AnimationSequence after) {
        AnimationSequence last = after;
        AnimationSequence current;
        for (int i = 0; i < seqQueue.size(); i++) {
            current = seqQueue.get(i);
            insert(current, last);
            last = current;
        }
    }

    public synchronized void insert(AnimationSequence seq, AnimationSequence after) {
        if (seq == null) {
            return;
        }
        if (!listeningTo.contains(seq.getEditor())) {
            seq.getEditor().getAnimator().addAnimationListener(this);
        }
        if (after != null && animationQueue.contains(after)) {
            int index = animationQueue.indexOf(after);
            animationQueue.add(index + 1, seq);
        } else {
            animationQueue.addLast(seq);
        }
        if (animationQueue.size() == 1) {
            execute();
        }

    }

    public synchronized void clear() {
        //softClear();
        if (animationQueue.isEmpty()) {
            return;
        }
        isClearing = true;
        //softClear();
        //editor.getAnimator().finishAnimationSequence();
//        if (isClearing) {
//            try {
//                wait();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(AnimationSequenceQueue.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }


    }

    public synchronized void softClear() {
        while (animationQueue.size() > 1) {
            animationQueue.removeLast();
        }
    }
    /*
     * postpones the execution of all actions except the currently executed one
     * postponed actions will be requeued when executing wake()
     * make sure wake is called if you use this methods recursively
     */

    public synchronized void sleep() {
        hyperHiberQueue.addFirst(new LinkedList<AnimationSequence>());
        LinkedList<AnimationSequence> hiberQueue = hyperHiberQueue.getFirst();
        while (animationQueue.size() > 1) {
            hiberQueue.add(animationQueue.get(1));
            animationQueue.remove(1);
        }

    }
    /*
     * wakes the queue from its sleep state and puts all postponed actions at the end of the queue
     *
     */

    public synchronized void wake() {
        LinkedList<AnimationSequence> hiberQueue = hyperHiberQueue.pollFirst();
        if (hiberQueue == null) {
            return;
        }
        animationQueue.addAll(hiberQueue);
    }

    private synchronized void execute() {
        if (animationQueue.isEmpty()) {
            return;
        }
        AnimationSequence exe = animationQueue.getFirst();
        if (isClearing) {
            exe.setAnimationTime(150);
            exe.setDelay(0);
        }
        //System.out.println("Executing and waiting " + exe.getClass().getSimpleName());
        sleep();
        long lastSequenceID = exe.getEditor().getAnimator().getAnimationSequenceID();
        try {
            exe.run();
        } finally {
            runningAnimationSequence = exe.getEditor().getAnimator().getAnimationSequenceID();
            wake();
            if (lastSequenceID == runningAnimationSequence) {
                //no animations were done, do not wait
                //System.out.println("no animation, continue");
                animationQueue.removeFirst();
                execute();
            }
        }
    }

    @Override
    public synchronized void onAnimationSequenceFinished(AnimationSequenceFinishedEvent e) {
        if (e.getAnimationSequenceID() != runningAnimationSequence) {
            return;
        }
        if (!animationQueue.isEmpty()) {
            animationQueue.removeFirst();
        }
        if (animationQueue.isEmpty() && isClearing) {
            isClearing = false;
            notifyAll();
        }
        //System.out.println("Animation finnished " + animationQueue.size() + " animations remain");
        execute();
    }
}
