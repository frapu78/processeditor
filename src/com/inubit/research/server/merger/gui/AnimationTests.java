/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author Uwe
 */
public class AnimationTests {


    ConflictResolverEditor editor;
    ProcessModel model1;
    ProcessModel model2;

    public AnimationTests(ConflictResolverEditor editor, ProcessModel model1, ProcessModel model2) {
        this.editor = editor;
        this.model1 = model1;
        this.model2 = model2;
    }

    public void testTransformModel() {
        editor.setModel(model1);
        editor.getMergeAnimator().animateModelTransition(model2);
    }

}
