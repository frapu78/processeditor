/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author fel
 */
public class ProcessEditorPool {
    private static final int SIZE = 50;

    private LinkedList<ProcessEditor> editors;
    private Semaphore semaphore;

    private static ProcessEditorPool instance;

    private ProcessEditorPool() {
        editors = new LinkedList<ProcessEditor>();
        
        for (int i = 0; i < SIZE; i++) {
            editors.add(new ProcessEditor());
        }

        semaphore = new Semaphore(SIZE);
    }

    public static ProcessEditorPool getPool() {
        if (instance == null) {
            instance = new ProcessEditorPool();
        }

        return instance;
    }

    public ProcessEditor getEditor() {
        try {
            semaphore.acquire();
            ProcessEditor e = editors.pop();
            return e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void returnEditor(ProcessEditor e) {
        editors.push(e);
        semaphore.release();
    }
}
