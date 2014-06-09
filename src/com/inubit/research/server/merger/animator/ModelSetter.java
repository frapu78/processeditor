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
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author Uwe
 */
public class ModelSetter extends AnimationSequence {

    private ProcessModel toSet;

    public ModelSetter(ProcessEditor editor, ProcessModel toSet) {
        super(editor);
        this.toSet = toSet;
    }




    public void run() {
        getEditor().setModel(toSet.clone());
    }

}
