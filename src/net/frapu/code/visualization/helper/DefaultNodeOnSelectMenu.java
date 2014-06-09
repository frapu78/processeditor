/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

import com.inubit.research.animation.DefaultAlphaAnimator;

/**
 * @author ff
 *
 */
public class DefaultNodeOnSelectMenu extends NodeOnSelectMenuBasis implements PEButtonListener {

    /**
     *
     */
    protected static final int BUTTON_HEIGHT = 20;
    /**
     *
     */
    protected static final int BUTTON_WIDTH = 20;
    protected PEButton f_arrowButton;
    protected PEButton f_refactorButton;
    protected PEButton f_deleteButton;
    protected List<PEButton> f_recommendations = new ArrayList<PEButton>();
    protected List<Class<?>> f_recommendationClasses = new ArrayList<Class<?>>();
    protected List<PEButton> f_refactorings = new ArrayList<PEButton>();
    protected List<Class<?>> f_refactoringClasses = new ArrayList<Class<?>>();

    /**
     * @param editor
     * @param selectedObject
     */
    public DefaultNodeOnSelectMenu(ProcessEditor editor) {
        super(editor);
    }

    @Override
    protected void buildMenu(ProcessNode node, ProcessEditor editor) {
        clearButtons();
        f_recommendations.clear();
        f_recommendationClasses.clear();
        f_refactoringClasses.clear();
        f_refactorings.clear();
        //next node menu
        if (getEditor().getModel().getUtils().getNextNodesRecommendation(editor.getModel(), node) != null) {
            if (getEditor().getModel().getUtils().getNextNodesRecommendation(editor.getModel(), node).size() > 0) {
                f_arrowButton = new PEButton(getEditor(), getImage("/menu/mini_arrow_top_right.gif"));
                f_arrowButton.addListener(this);
                f_arrowButton.setSize(20, 20);
                //building all buttons
                List<Class<? extends ProcessNode>> _recomms = getEditor().getModel().getUtils().getNextNodesRecommendation(editor.getModel(), node);
                for (Class<? extends ProcessNode> c : _recomms) {
                    Image _img = ProcessUtils.createPreviewImage(c, BUTTON_HEIGHT - 6);
                    PEButton _butt = new PEButton(getEditor(), _img);
                    _butt.addListener(this);
                    _butt.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
                    _butt.setVisible(false);
                    f_recommendations.add(_butt);
                    f_recommendationClasses.add(c);
                }//position will be automatically set when painting

            }
        }
        //refactoring menu
        if (node.getVariants().size() > 0) {
            f_refactorButton = new PEButton(getEditor(), getImage("/menu/refactoring.gif"));
            f_refactorButton.addListener(this);
            f_refactorButton.setSize(20, 20);

            List<Class<? extends ProcessNode>> _variants = getNode().getVariants();
            Point _pos = f_refactorButton.getPosition();
            _pos.y += BUTTON_HEIGHT;
            for (Class<?> c : _variants) {
                Image _img = ProcessUtils.createPreviewImage(c, BUTTON_HEIGHT - 6);
                PEButton _butt = new PEButton(getEditor(), _img);
                _butt.addListener(this);
                _butt.setCenterImage(false);
                _butt.setText(c.getSimpleName());
                _butt.setSize(150, BUTTON_HEIGHT);
                _butt.setVisible(false);
                f_refactorings.add(_butt);
                f_refactoringClasses.add(c);
            }
        }
        f_deleteButton = new PEButton(getEditor(), getImage("/menu/delete_small.gif"));
        f_deleteButton.addListener(this);
        f_deleteButton.setSize(20, 20);

        // Setting the positions for all objects now
        Point _pos = new Point();
        _pos.x = (getNode().getSize().width / 2) - BUTTON_WIDTH;
        _pos.y = -(getNode().getSize().height / 2) - BUTTON_HEIGHT;
        this.addButton(f_deleteButton, _pos);

        if (f_arrowButton != null) {
            _pos.x += BUTTON_HEIGHT;
            _pos.y += BUTTON_WIDTH;
            this.addButton(f_arrowButton, _pos);
            _pos.x += BUTTON_WIDTH;
            int _counter = 0;
            for (PEButton b : f_recommendations) {
                this.addButton(b, _pos);
                _counter++;
                if (_counter % 3 == 0) {
                    _pos.x -= 2 * BUTTON_WIDTH;
                    _pos.y += BUTTON_HEIGHT;
                } else {
                    _pos.x += BUTTON_WIDTH;
                }
            }
        }
        //f_refactor button and its predecessors
        if (f_refactorButton != null) {
            _pos = new Point();
            _pos.y = getNode().getSize().height / 2;
            _pos.x = -getNode().getSize().width / 2;
            this.addButton(f_refactorButton, _pos);
            _pos.y += BUTTON_HEIGHT;
            for (PEButton b : f_refactorings) {
                this.addButton(b, _pos);
                _pos.y += BUTTON_HEIGHT;
            }
        }
    }

    @Override
    public Object clone() {
        // @todo: Implement clone()
        return null;
    }

    /**
     * @param string
     * @return
     */
    protected Image getImage(String path) {
        return Toolkit.getDefaultToolkit().createImage(getClass().getResource(path));
    }

    private void showNextNodesMenu() {
        for (PEButton b : f_recommendations) {
            if (!b.isVisible()) {
                fadeIn(b);
            }
        }
    }

    private void fadeIn(PEButton b) {
        b.setAlpha(0.0f);
        b.setVisible(true);
        if (getEditor().isAnimationEnabled()) {
            DefaultAlphaAnimator _daa = new DefaultAlphaAnimator(b, getEditor().getAnimator().getAnimator());
            _daa.setAnimationTime(ProcessUtils.RULER_FADE_TIME);
            _daa.setTargetAlpha(1.0f);
            getEditor().getAnimator().getAnimator().addObjectToAnimate(_daa);
        } else {
            b.setAlpha(1.0f);
        }

    }

    /**
     *
     */
    private void showRefactorMenu() {
        for (PEButton b : f_refactorings) {
            if (!b.isVisible()) {
                fadeIn(b);
            }
        }
    }

    /**
     * hides all recommendet next node buttons
     */
    private void hideNextNodesMenu() {
        for (PEButton b : f_recommendations) {
            b.setVisible(false);
        }
    }

    /**
     * hides all refactoring buttons
     */
    private void hideRefactoringMenu() {
        for (PEButton b : f_refactorings) {
            b.setVisible(false);
        }
    }

    /**
     * changes the class of the node
     * @param idx
     */
    private void refactorNode(int idx) {
        Class<?> _toRefac = f_refactoringClasses.get(idx);
        //using already present helper function :-)
        ProcessUtils.refactorNode(getEditor(), getNode(), _toRefac);
    }

    /**
     * creates a new node at the location p
     * @param idx
     * @param p
     */
    private void createNextNode(int idx, Point p) {
        try {
            //else the arrow button was hit
            Class<?> _c = f_recommendationClasses.get(idx);
            ProcessNode _node = (ProcessNode) _c.newInstance();
            _node.setPos(p);
            getEditor().getModel().addNode(_node);
            // now we have to check if the new node has to be added to a Cluster
            // (so edge creation works) --> Reverse iteration required (fpu)
            List<ProcessNode> _allNodes = getEditor().getModel().getNodes();
            for (int i = _allNodes.size() - 1; i >= 0; i--) {
                ProcessNode n = _allNodes.get(i);
                if (n instanceof Cluster) {
                    Cluster _clus = (Cluster) n;
                    if (_clus.isContainedGraphically(_allNodes, _node, true)) {
                        _clus.addProcessNode(_node);
                        break;
                    }
                }
            }
            //now create the edge
            ProcessEdge _edge = getEditor().getModel().getUtils().createDefaultEdge(getNode(), _node);
            if (_edge != null) {
                getEditor().getModel().addEdge(_edge);
            }
            getEditor().getSelectionHandler().clearSelection();
            getEditor().getSelectionHandler().addSelectedObject(_node);
            getEditor().setDragableObject(_node);
            getEditor().requestFocus();
            destroy();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /*
     * ----------------------------------------------------------------------------------------------
     * PEBUTTON LISTENER METHODS
     * ----------------------------------------------------------------------------------------------
     */
    @Override
    public void buttonClicked(PEButton button) {
        int idx = f_recommendations.indexOf(button);
        if (idx >= 0) {
            createNextNode(idx, button.getPosition());
        } else {
            idx = f_refactorings.indexOf(button);
            if (idx >= 0) {
                refactorNode(idx);
            } else {
                if (button.equals(f_deleteButton)) {
                    getEditor().getAnimator().removeProcessObject(getNode(), ProcessEditor.DELETE_FADE_TIME);
                }
            }
        }

    }

    @Override
    public void buttonMouseIn(PEButton button) {
        if (button.equals(f_arrowButton) || f_recommendations.contains(button)) {
            showNextNodesMenu();
            hideRefactoringMenu();
        } else if (button.equals(f_refactorButton) || f_refactorings.contains(button)) {
            showRefactorMenu();
            hideNextNodesMenu();
        } else if (button.equals(f_deleteButton)) {
            hideNextNodesMenu();
            hideRefactoringMenu();
        }
    }

    @Override
    public void buttonMouseOut(PEButton button) {
    }
}
