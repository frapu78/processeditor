/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.gui.ConflictResolverEditor;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author uha
 */
public class ConflictResolvedChecker extends AnimationSequence {

    public ConflictResolvedChecker(ProcessEditor editor) {
        super(editor);
    }



    public void run() {
        try {
            ((ConflictResolverEditor) getEditor()).checkConflictsSolved();
        } catch (Exception e) {
            System.err.print("model not checkt for conflicts");
        }

    }



}
