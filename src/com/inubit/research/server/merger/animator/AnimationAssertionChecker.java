/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.ModelComparator;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author uha
 */
public class AnimationAssertionChecker extends AnimationSequence {

    ProcessModel desiredModel;

    public AnimationAssertionChecker(ProcessEditor editor, ProcessModel desiredModel) {
        super(editor);
        this.desiredModel = desiredModel.clone();
    }



    @Override
    public void run() {
        ModelComparator comp = new ModelComparator();
        assert comp.modelEquals(getEditor().getModel(), desiredModel) : comp.getReason();
    }

}
