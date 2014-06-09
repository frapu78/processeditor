/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author fpu
 */
public class ProcessEditorBlockingQueue extends ArrayBlockingQueue {

    public ProcessEditorBlockingQueue(int capacity, boolean fair, Collection c) {
        super(capacity, fair, c);
    }

    public ProcessEditorBlockingQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean add(Object e) {
        // Add to hashmap
        ProcessEditorServer.startTimes.put(e, System.nanoTime());
        return super.add(e);
    }

    @Override
    public void put(Object e) throws InterruptedException {
        // Add to hashmap
        ProcessEditorServer.startTimes.put(e, System.nanoTime());
        super.put(e);
    }

    @Override
    public boolean offer(Object e) {
        // Add to hashmap
        ProcessEditorServer.startTimes.put(e, System.nanoTime());
        return super.offer(e);
    }

    @Override
    public boolean offer(Object e, long timeout, TimeUnit unit) throws InterruptedException {
        // Add to hashmap
        ProcessEditorServer.startTimes.put(e, System.nanoTime());
        return super.offer(e, timeout, unit);
    }


    @Override
    public Object take() throws InterruptedException {
        Object e = super.take();
        return e;
    }



}
