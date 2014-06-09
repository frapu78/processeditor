/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.client.ModelVersionDescription;
import com.inubit.research.server.merger.ProcessModelMerger;
import com.inubit.research.server.merger.gui.VersionTreeManager;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public class VersionChangeFinished extends AnimationSequence{

    ProcessModelMerger merger;
    VersionTreeManager versionTreeManager;
    ModelVersionDescription oldVersion;
    ModelVersionDescription newVersion;

    public VersionChangeFinished(ProcessEditor editor, ProcessModelMerger merger, VersionTreeManager versionTreeManager, ModelVersionDescription oldVersion, ModelVersionDescription newVersion) {
        super(editor);
        this.merger = merger;
        this.versionTreeManager = versionTreeManager;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }




    public void run() {
        getEditor().getMergeAnimator().setCurrentMerger(merger);
        versionTreeManager.versionChanged(oldVersion, newVersion);
    }









}
