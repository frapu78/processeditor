/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashSet;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.reporting.BarChart;
import net.frapu.code.visualization.reporting.PieChart;

/**
 * The animation Facade hides animation details.
 * It can be used to easily animate objects and state changes
 * within a processEditor.
 * If necessary the underlying animator can be accessed directly
 * e.g. to perform large animations.
 * @author ff
 *
 */
public class AnimationFacade implements IAnimationListener {

    public enum Type {

        /**
         * uses the alpha value to fade in an object
         */
        TYPE_FADE_IN,
        /**
         * uses the size of an object to make it appear
         */
        TYPE_GROW
    }
    private Animator f_animator;
    private ProcessEditor f_parent;

    /**
     * provides an animation facade which is able to
     * handle animations for the given ProcessEditor.
     * @param parent
     */
    public AnimationFacade(ProcessEditor parent) {
        f_parent = parent;
    }

    /**
     * creates a new Animator and makes it ready for animation
     */
    public void start() {
        if (f_animator == null) {
            f_animator = new Animator(f_parent, 60); //60 fps
            f_animator.setPaused(true);
            f_animator.start();
        }
    }

    /**
     * ends the Thread and make this Animator useless
     */
    public void end() {
        if (f_animator != null) {
            f_animator.setPaused(true);
            f_animator.setRunning(false);
            f_animator = null;
        }
    }

    public void finishAnimationSequence() {
        getAnimator().finishAnimations();
    }

    /**
     * adds the given ProcessObject to the model by fading it in
     * using the alpha property
     * @param object
     * @param animationTime
     */
    public void addProcessObject(ProcessObject object, int animationTime) {
        addProcessObject(object, animationTime, 0);
    }

    /**
     * adds the given ProcessObject to the model by fading it in
     * using the alpha property
     * @param object
     * @param animationTime
     */
    public void addProcessObject(ProcessObject object, int animationTime, int delay) {
        if (object==null) {
            throw new NullPointerException();
        }
        Float oldAlpha = object.getAlpha();
        object.setAlpha(0.0f);
        //adding object
        if (object instanceof ProcessHelper) {
            synchronized (f_parent.getProcessHelpers()) {
                f_parent.addProcessHelper((ProcessHelper) object);
            }
        } else {
            synchronized (f_parent.getModel().getNodes()) {
                synchronized (f_parent.getModel().getEdges()) {
                    f_parent.getModel().addObject(object);
                }
            }
        }
        if (f_animator != null) {
            DefaultAlphaAnimator _daa = new DefaultAlphaAnimator(object, f_animator);
            _daa.setTargetAlpha(oldAlpha);
            animateSingleObject(animationTime, delay, _daa);
        } else {
            object.setAlpha(oldAlpha);
        }
    }

    /**
     * adds the given ProcessNode to the model.
     * @param object
     * @param animationTime
     */
    public void addProcessNode(ProcessNode node, int animationTime, int delay, Type type) {
        if (node==null) {
            throw new NullPointerException();
        }
        if (type == Type.TYPE_FADE_IN) {
            addProcessObject(node, animationTime, delay);
        } else if (type == Type.TYPE_GROW) {
            //adding object
            f_parent.getModel().addNode(node);
            if (f_animator != null) {
                //using the grow type
                Dimension _d = node.getSize();
                node.setSize(10, 10); //given minimum size (see Task.setProperty(...))
                DefaultNodeAnimator _dna = new DefaultNodeAnimator(node, f_animator);
                _dna.setNewSize(_d);
                animateSingleObject(animationTime, delay, _dna);
            }            
        }
    }

    /**
     * adds the given ProcessObject to the model by fading it out
     * using the alpha property
     * @param object
     * @param animationTime
     */
    public void removeProcessObject(ProcessObject object, int animationTime) {
        removeProcessObject(object, animationTime, 0, true);
    }

    public void removeProcessObject(ProcessObject object, int animationTime, int delay) {
        removeProcessObject(object, animationTime, delay, true);
    }

    /**
     * adds the given ProcessObject to the model by fading it out
     * using the alpha property
     * @param object
     * @param animationTime
     */
    public void removeProcessObject(ProcessObject object, int animationTime, int delay, boolean includeEdges) {
        if (object==null) {
            throw new NullPointerException();
        }
        f_parent.getOnSelectMenu(null).setNode(null);
        if (f_animator != null) {
            DefaultAlphaAnimator _daa = new DefaultAlphaAnimator(object, f_animator, includeEdges, 0.0f);
            _daa.addListener(this);
            animateSingleObject(animationTime, delay, _daa);
        } else {
            object.setAlpha(0.0f);
            handleDelete(object, includeEdges);
        }
    }

    private void animateSingleObject(int animationTime, int delay, NodeAnimator obj) {
        obj.setAnimationTime(animationTime);
        obj.setDelay(delay);
        f_animator.addObjectToAnimate(obj);
        f_animator.setParent(f_parent);
    }

    /**
     * The newStateObject will not be added to the model and just serves as a container
     * for the animation information
     *
     * Supported Properties:
     * - Position
     * - Size
     * - Color
     *
     * @param node
     * @param newState
     * @param animationTime
     * @param delay
     */
    public void animateNode(ProcessNode node, ProcessNode newState, int animationTime, int delay) {
        if (node==null || newState==null) {
            throw new NullPointerException();
        }
        if (f_animator != null) {
            DefaultNodeAnimator _node = new DefaultNodeAnimator(node, f_animator);
            setTargetValues(newState, _node);
            animateSingleObject(animationTime, delay, _node);
        } else {
            node.setPos(newState.getPos());
            node.setSize(newState.getSize().width, newState.getSize().height);
            node.setBackground(newState.getBackground());
        }
    }

    private boolean equalsExceptAnimatedValues(ProcessObject o1, ProcessObject o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        HashSet<String> animatedValues = new HashSet<String>();
        animatedValues.add(ProcessNode.PROP_BACKGROUND);
        animatedValues.add(ProcessNode.PROP_XPOS);
        animatedValues.add(ProcessNode.PROP_YPOS);
        HashSet<String> o1Values = new HashSet<String>(o1.getPropertyKeys());
        o1Values.removeAll(animatedValues);
        HashSet<String> o2Values = new HashSet<String>(o2.getPropertyKeys());
        o2Values.removeAll(animatedValues);
        return o1Values.equals(o2Values);

    }

    public void animateSubstitution(ProcessObject original, ProcessObject substituteBy, int animationTime, int delay, boolean adaptEnvironment) {
        if (original == substituteBy) {
            return;
        }
        ProcessObject originalCopy = null;
        if (original != null) {
            originalCopy = (ProcessObject) original.clone();
            this.f_parent.getModel().substitute(original, originalCopy);
        }
        if (original == null && substituteBy instanceof ProcessNode) {
            //new node
            addProcessNode((ProcessNode) substituteBy, animationTime, delay, Type.TYPE_GROW);
        } else if (original == null && substituteBy instanceof ProcessEdge) {
            //new edges
            addProcessObject(substituteBy, animationTime, delay);
        } else if (substituteBy == null) {
            //removed Objects
            removeProcessObject(originalCopy, animationTime, delay, false);
        } else if (original instanceof ProcessNode && substituteBy instanceof ProcessNode) {
            // nodes
            animateNode((ProcessNode) originalCopy, (ProcessNode) substituteBy, animationTime / 2, delay);
            if (!equalsExceptAnimatedValues(originalCopy, substituteBy)) {
                removeProcessObject(originalCopy, animationTime / 2, delay + animationTime / 2, false);
                addProcessNode((ProcessNode) substituteBy, animationTime / 2, delay + animationTime / 2, Type.TYPE_FADE_IN);
            } else {
                removeProcessObject(originalCopy, 0, delay + animationTime / 2, false);
                addProcessNode((ProcessNode) substituteBy, 0, delay + animationTime / 2, Type.TYPE_FADE_IN);
            }
        } else if (original instanceof ProcessEdge && substituteBy instanceof ProcessEdge) {
            // edges
            animateEdge((ProcessEdge) originalCopy, (ProcessEdge) substituteBy, animationTime / 2, delay);
            if (!equalsExceptAnimatedValues(originalCopy, substituteBy)) {
                removeProcessObject(originalCopy, animationTime / 2, delay + animationTime / 2, false);
                addProcessObject(substituteBy, animationTime / 2, delay + animationTime / 2);
            } else {
                removeProcessObject(originalCopy, 0, delay + animationTime / 2, false);
                addProcessObject(substituteBy, 0, delay + animationTime / 2);
            }
        } else {
            throw new UnsupportedOperationException("substitution not implemented");
        }
        if (adaptEnvironment) {
            this.f_parent.getModel().substitute(originalCopy, substituteBy, true);
        }
    }

    public void animateSubstitution(ProcessObject original, ProcessObject substituteBy, int animationTime, int delay) {
        animateSubstitution(original, substituteBy, animationTime, delay, true);
    }

    private void setTargetValues(ProcessNode newState, DefaultNodeAnimator dna) {
        dna.setNewColor(newState.getBackground());
        dna.setNewCoords(newState.getPos());
        dna.setNewSize(newState.getSize());
        dna.setTargetAlpha(newState.getAlpha());
    }

    private void setTargetValues(ProcessEdge newState, DefaultEdgeAnimator dea) {
        dea.setNewColor(newState.getColor());
        dea.setTargetAlpha(newState.getAlpha());
    }

    /**
     * works just as animateNode, but is also able to adjust the value of the data
     * of charts
     */
    public void animateChart(ProcessNode node, ProcessNode newState, int animationTime, int delay) {
        if (f_animator != null) {
            DefaultNodeAnimator _node;
            if (node instanceof BarChart) {
                _node = new BarChartAnimator(node, f_animator);
            } else if (node instanceof PieChart) {
                _node = new PieChartAnimator(node, f_animator);
            } else {
                _node = new DefaultNodeAnimator(node, f_animator);
            }
            setTargetValues(newState, _node);
            if (node instanceof BarChart) {
                ((BarChartAnimator) _node).setNewData(((BarChart) newState).getData(), ((BarChart) newState).getMaxHeight());
            } else if (node instanceof PieChart) {
                ((PieChartAnimator) _node).setNewData(((PieChart) newState).getData());
            }
            animateSingleObject(animationTime, delay, _node);
        } else {
            node.setPos(newState.getPos());
            node.setSize(newState.getSize().width, newState.getSize().height);
            node.setBackground(newState.getBackground());
            if (node instanceof BarChart) {
                ((BarChart) node).setStackedData(((BarChart) newState).getData());
            } else if (node instanceof PieChart) {
                ((PieChart) node).setData(((PieChart) newState).getData());
            }
        }
    }

    /**
     * The newState Object will not be added to the model and just serves as a container
     * for the animation information.
     *
     * Supported Properties:
     * - routing points
     *
     * @param node
     * @param newState
     * @param animationTime
     * @param delay
     */
    public void animateEdge(ProcessEdge edge, ProcessEdge newEdge, int animationTime, int delay) {
        if (f_animator != null) {
            DefaultEdgeAnimator _edge = new DefaultEdgeAnimator(edge, f_animator);
            setTargetValues(newEdge, _edge);
            _edge.transformTo(newEdge);
            animateSingleObject(animationTime, delay, _edge);
        } else {
            edge.clearRoutingPoints();
            List<Point> _rps = newEdge.getRoutingPoints();
            for (int i = 0; i < _rps.size(); i++) {
                edge.addRoutingPoint(i, _rps.get(i));
            }
        }
    }

    public void animateObject(ProcessObject object, ProcessObject newObject, int animationTime, int delay) {
        if (object instanceof ProcessNode && newObject instanceof ProcessNode) {
            animateNode((ProcessNode) object, (ProcessNode) newObject, animationTime, delay);
        } else if (object instanceof ProcessEdge && newObject instanceof ProcessEdge) {
            animateEdge((ProcessEdge) object, (ProcessEdge) newObject, animationTime, delay);
        }
    }

    /**
     * retrieves the underlying animator which can be used for
     * more complicated handling issues
     * @return the animator
     */
    public Animator getAnimator() {
        return f_animator;
    }

    /**
     * internal method called when a NodeAnimator is finished, so that
     * a delete operation can remove the corresponding node from the model.
     *
     */
    @Override
    public void animationFinished(NodeAnimator node) {
        if (node instanceof DefaultAlphaAnimator) {
            DefaultAlphaAnimator _ani = (DefaultAlphaAnimator) node;
            //removing object
            handleDelete(_ani.getProcessObject(), _ani.isIncludeEdges());
            node.removeListener(this);
        }
    }

    private void handleDelete(ProcessObject animatedObject, boolean includeEdges) {
        if (animatedObject instanceof ProcessHelper) {
            synchronized (f_parent.getProcessHelpers()) {
                f_parent.removeProcessHelper((ProcessHelper) animatedObject);
            }
        } else {
            synchronized (f_parent.getModel().getNodes()) {
                synchronized (f_parent.getModel().getEdges()) {
                    f_parent.getModel().removeObject(animatedObject, includeEdges);
                }
            }
        }
    }

    public long getAnimationSequenceID() {
        return getAnimator().getAnimationSequenceID();
    }

    /**
     *
     * events
     */
    public void addAnimationListener(AnimationListener listener) {
        this.getAnimator().addAnimationListener(listener);
    }

    public void removeAnimationListener(AnimationListener listener) {
        this.getAnimator().addAnimationListener(listener);
    }
}
