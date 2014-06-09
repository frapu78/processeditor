/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.WorkBenchSpecific.WorkbenchHandler;
import com.inubit.research.layouter.adapter.ProcessNodeAdapter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;

/**
 * @author ff
 *
 */
public class LayoutingAnimator implements IAnimationListener {

    /**
     * Configuration Key values
     */
    public static final String CONF_ANIMATION_SPEED = "LayouterAnimationSpeed";
    private long start;
    private ProcessLayouter f_layouter;
    private int f_animationTime = -1;
    private Animator animator;
	private ProcessEditor f_editor;
	private boolean f_layoutEdgesValue;

    /**
     *
     */
    public LayoutingAnimator(ProcessLayouter layouter) {
        f_layouter = layouter;
    }

    public ProcessLayouter getLayouter() {
        return f_layouter;
    }

    /**
     * Animates the layout of the model.
     * @param model
     * @param xstart
     * @param ystart
     * @param direction
     * @throws Exception
     */
    public void layoutModelWithAnimation(ProcessEditor editor, List<NodeAnimator> animList, int xstart, int ystart, int direction)
            throws Exception {
//        Animator orgAnimator = editor.getAnimator().getAnimator();
//        if (orgAnimator != null) {
//            orgAnimator.setRunning(false);
//        }
        // animator = new Animator(null, 60);
        // animator.start();
        // animator.setParent(editor);

    	f_editor = editor;
        animator = editor.getAnimator().getAnimator();

        ProcessModel model = editor.getModel();
        ProcessModel copy = model.clone();
        ProcessNode _selNode = findNode(editor.getSelectionHandler().getLastSelectedNode(), copy);
        if (_selNode != null) {
            ProcessNodeAdapter selectedNode = new ProcessNodeAdapter(_selNode);
            f_layouter.setSelectedNode(selectedNode);
        } else {
            f_layouter.setSelectedNode(null);
        }
        // Fix all sizes to final size
        if (animList != null) {
            for (NodeAnimator a : animList) {
                if (a instanceof DefaultNodeAnimator) {
                    DefaultNodeAnimator defA = (DefaultNodeAnimator) a;
                    // Check if node is contained in copy
                    if (model.getNodes().contains(defA.getNode())) {
                        // If found, set target size for layouting
                        findNode(defA.getNode(), copy).setSize(defA.getNewSize().width, defA.getNewSize().height);
                    }
                }
            }
        }

        Point _offset = determinePartialLayoutingRegion(editor, copy);

        AbstractModelAdapter modelI = LayoutUtils.getAdapter(copy);

        f_layouter.layoutModel(modelI, xstart, ystart, 0);

        WorkbenchHandler.postProcess(f_layouter, copy);

        int _animationTime = f_animationTime;
        if (_animationTime == -1) {
            _animationTime = LayoutHelper.toInt(Configuration.getInstance().getProperty(CONF_ANIMATION_SPEED, "6000"), 6000);
        }
        //writing back coords to wrappers
        ArrayList<NodeAnimator> wrappers = new ArrayList<NodeAnimator>();

        for (ProcessNode n : editor.getModel().getNodes()) {
            DefaultNodeAnimator w = new DefaultNodeAnimator(n, animator);
            w.setAnimationTime(_animationTime);
            ProcessNode dup = findNode(n, copy);
            if (dup != null) {
                Point _pos = applyPartialLayoutingOffsetToNode(_offset, dup);
                w.setNewCoords(_pos);
                w.setNewSize(dup.getSize());
                wrappers.add(w);
            }
        }

        for (ProcessEdge edge : editor.getModel().getEdges()) {
            DefaultEdgeAnimator w = new DefaultEdgeAnimator(edge, animator);
            w.setAnimationTime(_animationTime);
            ProcessEdge _e = (ProcessEdge) copy.getObjectById(edge.getId());
            if (copy.getEdges().contains(_e)) {
                applyPartialLayoutingOffsetToEdge(_offset, _e);
                w.transformTo(_e);
                wrappers.add(w);
            }
        }

        // Check if additional animation list @todo Refactor :-)
        if (animList != null) {
            for (NodeAnimator a : animList) {
                if (wrappers.contains(a)) {
                    //Already contained, modify
                    NodeAnimator org = wrappers.get(wrappers.indexOf(a));
                    if (org instanceof DefaultNodeAnimator) {
                        DefaultNodeAnimator defOrg = (DefaultNodeAnimator) org;
                        defOrg.setNewSize(((DefaultNodeAnimator) a).getNewSize());
                    }
                }
            }
        }
        if (wrappers.size() > 0) {
            wrappers.get(0).addListener(this);
            start = System.nanoTime();
        }
        f_layoutEdgesValue = editor.isLayoutEdges();
        editor.setLayoutEdges(false);
        animator.setAnimationObjects(wrappers);
    }

    private void applyPartialLayoutingOffsetToEdge(Point _offset, ProcessEdge _e) {
        if (_offset.x != Integer.MAX_VALUE) {
            List<Point> _rps = _e.getRoutingPoints();
            if (_rps.size() > 2) {
                _rps.remove(0);
                _rps.remove(_rps.size() - 1);
                for (Point p : _rps) {
                    p.x += _offset.x;
                    p.y += _offset.y;
                }
                //setting new routing points
                _e.clearRoutingPoints();
                for (int i = 0; i < _rps.size(); i++) {
                    _e.addRoutingPoint(i, _rps.get(i));
                }
            }
        }
    }

    private Point applyPartialLayoutingOffsetToNode(Point _offset, ProcessNode dup) {
        Point _pos = dup.getPos();
        if (_offset.x != Integer.MAX_VALUE) {
            _pos.x += _offset.x;
            _pos.y += _offset.y;
        }
        return _pos;
    }

    /**
     * used for partial layouting (if just some node are selected)
     * @param editor
     * @param copy
     * @return
     */
    private Point determinePartialLayoutingRegion(ProcessEditor editor,
            ProcessModel copy) {
        List<ProcessObject> _selectedNodes = editor.getSelectionHandler().getSelection();
        Point _offset = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        if (_selectedNodes.size() > 1) {
            for (ProcessObject o : _selectedNodes) {
                if (o instanceof ProcessNode) {
                    ProcessNode _n = (ProcessNode) o;
                    _offset.x = Math.min(_offset.x, _n.getPos().x - _n.getSize().width / 2);
                    _offset.y = Math.min(_offset.y, _n.getPos().y - _n.getSize().height / 2);
                }
            }
            for (ProcessNode n : new ArrayList<ProcessNode>(copy.getNodes())) {
                if (!_selectedNodes.contains(n)) {
                    copy.removeNode(n);
                }
            }
            for (ProcessEdge e : new ArrayList<ProcessEdge>(copy.getEdges())) {
                if (!_selectedNodes.contains(e)) {
                    copy.removeEdge(e);
                }
            }
        }
        return _offset;
    }

    @Override
    public void animationFinished(NodeAnimator node) {
        node.removeListener(this);
        System.out.println("Animation took: " + (System.nanoTime() - start) / 1000000 + " ms");
        f_editor.setLayoutEdges(f_layoutEdgesValue);
        
        // Kill Animator thread
        //animator.setRunning(false);
    }

    private ProcessNode findNode(ProcessNode original, ProcessModel copy) {
        if (original != null) {
            String _id = original.getProperty(ProcessNode.PROP_ID);
            for (ProcessNode n : copy.getNodes()) {
                if (n.getProperty(ProcessNode.PROP_ID).equals(_id)) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * can be used to override the user set animation time for special occassions
     * @param time
     */
    public void setCustomAnimationTime(int time) {
        f_animationTime = time;
    }
}
