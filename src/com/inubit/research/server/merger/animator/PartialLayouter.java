/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.animator;

import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.adapter.BPMNModelAdapter;
import com.inubit.research.server.merger.ProcessModelMerger;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.BPMNModel;

/**
 *
 * @author Uwe
 */
public class PartialLayouter extends AnimationSequence {

    ProcessModelMerger merger;
    private boolean animate = true;
    private static int ANIMATION_TIME = 500;

    public PartialLayouter(ProcessEditor editor, ProcessModelMerger merger) {
        super(editor);
        this.merger = merger;
        setAnimationTime(ANIMATION_TIME);
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    private Rectangle getBoundingBox(ProcessNode n, int extension) {
        Point p = n.getPos();
        Dimension d = n.getSize();
        d.height += extension * 2;
        d.width += extension * 2;
        p.x -= d.width / 2;
        p.y -= d.height / 2;
        return new Rectangle(p, d);
    }

    private Set<ProcessNode> getOverlappingNodes(ProcessModel model) {
        HashSet<ProcessNode> result = new HashSet<ProcessNode>();
        for (ProcessNode n1 : model.getNodes()) {
            for (ProcessNode n2 : model.getNodes()) {
                if (n1 == n2) {
                    continue;
                }
                if (n2 instanceof Cluster) {
                    for (ProcessObject affectedObject : merger.getAffectedObjects()) {
                        if (affectedObject.getId().equals(n2.getId())) {
                            result.add(n2);
                        }
                    }
                }
                //only consider conflicts where an affected (changed) object is involved
                if (getBoundingBox(n1, 0).intersects(getBoundingBox(n2, 0))) {
                    for (ProcessObject affectedObject : merger.getAffectedObjects()) {
                        if (affectedObject.getId().equals(n1.getId()) || affectedObject.getId().equals(n2.getId())) {
                            result.add(n1);
                            result.add(n2);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void selectObjectsToLayout() {
        for (ProcessObject n : merger.getAffectedObjects()/*getOverlappingNodes(getEditor().getModel())*/) {
            if (!(n instanceof ProcessNode)) continue;
            ProcessObject objectInModel = getEditor().getModel().getObjectById(n.getId());
            if (objectInModel != null) {
                getEditor().getSelectionHandler().addSelectedObject(objectInModel);
            }
        }


    }

    public void run() {
        try {
            selectObjectsToLayout();
            if (getEditor().getSelectionHandler().isEmpty()) {
                return;
            }
            getEditor().getSelectionHandler().clearSelection();
            ProcessLayouter layouter = getEditor().getModel().getUtils().getLayouters().get(0);
            if (animate) {
                LayoutingAnimator anim = new LayoutingAnimator(layouter);
                anim.setCustomAnimationTime(getAnimationTime());
                anim.layoutModelWithAnimation(getEditor(), null, 0, 0, ProcessLayouter.LAYOUT_HORIZONTAL);
            } else {
                layouter.layoutModel(new BPMNModelAdapter((BPMNModel) getEditor().getModel()),0,0,ProcessLayouter.LAYOUT_HORIZONTAL);
            }
            getEditor().getSelectionHandler().clearSelection();
        } catch (Exception ex) {
            Logger.getLogger(PartialLayouter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
