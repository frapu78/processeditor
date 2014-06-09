/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModelListener;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 *
 * @author Uwe
 */
public class MergedModelListener implements ProcessModelListener {

    private ConflictResolverEditor editor;
    private VersionTreeManager manager;

    public MergedModelListener(ConflictResolverEditor editor, VersionTreeManager manager) {
        this.editor = editor;
        this.manager = manager;
    }

    @Override
    public void processNodeAdded(ProcessNode newNode) {
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        // ignore
    }
}
