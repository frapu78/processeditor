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
import com.inubit.research.server.merger.VersionTreeViewer.MergedModelVersionDescription;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author Uwe
 */
public class MergedModelBackup extends AnimationSequence {

    private ModelVersionDescription lastDisplayedVersionDescription;

    public MergedModelBackup(ProcessEditor editor, ModelVersionDescription lastDisplayedVersionDescription) {
        super(editor);
        this.lastDisplayedVersionDescription = lastDisplayedVersionDescription;
    }



    public void run() {
        if (lastDisplayedVersionDescription instanceof MergedModelVersionDescription) {
            MergedModelVersionDescription mergedVersion = (MergedModelVersionDescription) lastDisplayedVersionDescription;
            mergedVersion.getMerger().setMergedModel(getEditor().getModel());
        }
    }
}
