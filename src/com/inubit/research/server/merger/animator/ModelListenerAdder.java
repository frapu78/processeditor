/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModelListener;

 /**
 *
 * @author Uwe
 */
public class ModelListenerAdder extends AnimationSequence{

    private ProcessModelListener listener;

    public ModelListenerAdder(ProcessEditor editor, ProcessModelListener listener) {
        super(editor);
        this.listener = listener;
    }



    public void run() {
        getEditor().getModel().addListener(listener);
    }





}
