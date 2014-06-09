/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.server.merger.ProcessModelMerger;
import com.inubit.research.server.merger.ProcessObjectMerger;
import com.inubit.research.server.merger.gui.ConflictResolverEditor;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author uha
 *
 * Allows asynchronus calls of whole animation sequences which will be executed one after the other.
 * Abstracts from synchronization problems with animator/editor
 *
 */
public class ProcessMergeAnimator {

    public static final boolean animateLayout = false;
    private ProcessEditor editor;
    private ProcessModelMerger currentMerger = null;
    private AnimationSequenceQueue animationQueue;

    /*
     *  only one mergeAnimator per editor is allowed
     */
    public ProcessMergeAnimator(ProcessEditor editor) {
        this.editor = editor;
        animationQueue = new AnimationSequenceQueue();
    }

    public ProcessModelMerger getCurrentMerger() {
        return currentMerger;
    }

    public void setCurrentMerger(ProcessModelMerger currentMerger) {
        this.currentMerger = currentMerger;
    }


    /*
     * Takes the model currently displayed by the editor and transforms it with animations into the given model
     */
    public void animateModelTransition(final ProcessModel newModel) {
        animateModelTransition(newModel, false, false);
    }

    public void animateModelTransition(final ProcessModel newModel, final boolean doLayout, final boolean withMarkings) {
        try {
            animateModelTransitionWithoutResetingModel(newModel, doLayout, withMarkings);
            animationQueue.queue(new AnimationAssertionChecker(editor, newModel));
        } finally {
            animationQueue.queue(new ModelSetter(editor, newModel));
        }

    }

    /*
     * Do not use, only for testing purposes
     */
    public void animateModelTransitionWithoutResetingModel(final ProcessModel newModel, final boolean doLayout, final boolean withMarkings) {
        animationQueue.queue(new AnimationSequence(editor) {

            public void run() {
                ProcessModelMerger newMerger = new ProcessModelMerger(editor.getModel(), editor.getModel(), newModel);
                animateModelTransitionNow(newMerger, doLayout, withMarkings);
            }
        });

    }
    /*
     * Uses the given merger to transform the merger's source model (which should be displayed in the editor)
     * to the merged model, including markings
     */

    public void animateModelTransition(final ProcessModelMerger merger, final boolean doLayout) {
        animateModelTransition(merger, doLayout, true);
    }

    public void animateModelTransition(final ProcessModelMerger merger, final boolean doLayout, final boolean withMarkings) {

        animationQueue.queue(new AnimationSequence(editor) {

            public void run() {
                animateModelTransitionNow(merger, doLayout, withMarkings);
            }
        });

    }

    private void animateModelTransitionNow(final ProcessModelMerger merger, final boolean doLayout, final boolean withMarkings) {
        try {
            if (doLayout && !animateLayout && !merger.isLayouted()) {
                partialLayout(merger);
                merger.setLayouted(true);
            }
            ProcessModel backupModel = merger.getMergedModel().clone();
            animationQueue.queue(new ModelSetter(editor, merger.getModelFrom()));
            ModelSwitcher switcher = new ModelSwitcher(editor, merger);
            switcher.setActuallyRemoveDeletedObjects(!withMarkings);
            switcher.setMarkProcessObjects(withMarkings);
            animationQueue.queue(switcher);
            if (doLayout && animateLayout && !merger.isLayouted()) {
                animationQueue.queue(new PartialLayouter(editor, merger));
                merger.setLayouted(true);
            }
            animationQueue.queue(new AnimationAssertionChecker(editor, merger.getMergedModel()));
        } finally {
            animationQueue.queue(new ModelSetter(editor, merger.getMergedModel()));
        }
    }

    public void resetMergeMarkings(ProcessModelMerger merger) {
        animationQueue.queue(new MarkingRemover(editor));
    }

    public void setMergeMarkings(ProcessModelMerger merger) {
        animationQueue.queue(new MarkingSetter(editor));
    }

    public void partialLayout(ProcessModelMerger newMerger) {
        ConflictResolverEditor newEditor = new ConflictResolverEditor(null);
        newEditor.setModel(new BPMNModel());
        for (ProcessObjectMerger m : newMerger.getMergeRelations()) {
            if (m.getAnimateTo() == null) {
                continue;
            }
            newEditor.getModel().addObject(m.getAnimateTo());
        }
        newEditor.setAnimationEnabled(true);
        PartialLayouter parLay = new PartialLayouter(newEditor, newMerger);
        parLay.setAnimationTime(1);
        animationQueue.queue(parLay);
        newEditor.setModel(new BPMNModel());
    }

    public AnimationSequenceQueue getAnimationQueue() {
        return animationQueue;
    }
}







