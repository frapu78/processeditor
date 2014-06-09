/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.helper.DefaultNodeOnSelectMenu;
import net.frapu.code.visualization.helper.PEButton;

import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.merger.ProcessObjectMerger;
import com.inubit.research.server.merger.ProcessObjectPropertyMerger;
import com.inubit.research.server.merger.animator.ConflictResolvedChecker;

/**
 *
 * @author uha
 */
public class ConflictResolverOnSelectMenu extends DefaultNodeOnSelectMenu {

    private static int CONFLICTRESOLVER_WIDTH = 60;
    private static int CONFLICTRESOLVER_HEIGHT = 60;
    private PEButton conflictResolverButton1 = null;
    private PEButton conflictResolverButton2 = null;
    private ProcessNode sourceAlternative = null;
    private ProcessNode targetAlternative = null;
    private List<PEButton> refactoringButtons = new ArrayList<PEButton>();
    private List<Class<?>> refactoringButtonClasses = new ArrayList<Class<?>>();
    private List<List<PEButton>> smallButtonCollumns = new ArrayList(3);
    private ProcessObjectMerger relation;

    public ConflictResolverOnSelectMenu(ProcessObjectMerger relation, ConflictResolverEditor editor) {
        super(editor);
        this.relation = relation;
    }

    @Override
    protected void buildMenu(ProcessNode node, ProcessEditor editor) {
        //super.buildMenu(node, editor);
        if (!(relation.getAnimateTo() instanceof ProcessNode)) {
            return;
        }
        this.sourceAlternative = (ProcessNode) relation.getSourceAlternative();
        this.targetAlternative = (ProcessNode) relation.getTargetAlternative();

        //refactoring button

        if (getNode().getVariants().size() > 0) {
            List<Class<? extends ProcessNode>> _variants = getNode().getVariants();
            Point _pos = getNode().getPos();
            _pos.y += BUTTON_HEIGHT;
            for (Class<?> c : _variants) {
                Image _img = ProcessUtils.createPreviewImage(c, BUTTON_HEIGHT - 6);
                PEButton _butt = new PEButton(getEditor(), _img);
                _butt.addListener(this);
                _butt.setCenterImage(false);
                _butt.setText(c.getSimpleName());
                _butt.setSize(100, BUTTON_HEIGHT);
                _butt.setVisible(false);
                refactoringButtons.add(_butt);
                refactoringButtonClasses.add(c);
            }//position will be automatically set when painting
        }


        Point _pos = new Point();

        //change position of refactoring in order to free space for conflict resolver buttons
        if (f_refactorButton != null) {
            _pos = new Point();
            _pos.y = getNode().getSize().height / 2 - BUTTON_HEIGHT;
            _pos.x = -getNode().getSize().width / 2 - BUTTON_WIDTH;
            this.moveButton(f_refactorButton, _pos);
            _pos.y += BUTTON_HEIGHT;
            for (PEButton b : f_refactorings) {
                this.moveButton(b, _pos);
                _pos.y += BUTTON_HEIGHT;
            }
        }

        //create menus depending on destiny of object

        if (relation.isDestinyChanged()) {
            createChangeMenu(relation, editor);
        }

        if (!relation.isDestinyEqual()) {
            conflictResolverButton1 = createBigButton(sourceAlternative);
            conflictResolverButton2 = createBigButton(targetAlternative);
        }

        setButtonPosition(conflictResolverButton1, true);
        setButtonPosition(conflictResolverButton2, false);



        //AttributeButtons
        if (relation.isDestinyChanged()) {
            for (int i = 0; i <= 2; i++) {
                smallButtonCollumns.add(new LinkedList<PEButton>());
            }
            createAttributeButtons(relation);
        }
    }

    private void createAttributeButtons(ProcessObjectMerger merger) {
        AttributeSelectionButton newButton;
        String attrValue;
        for (ProcessObjectPropertyMerger propMerge : merger.getResolvedPropertyConflicts().values()) {
            attrValue = propMerge.getSourceValue();
            newButton = new AttributeSelectionButton(getEditor(), attrValue, propMerge);
            setAttributeButtonPosition(newButton, 0);
            attrValue = propMerge.getMergedValue();
            newButton = new AttributeNameDisplay(getEditor(), attrValue, propMerge);
            setAttributeButtonPosition(newButton, 1);
            attrValue = propMerge.getTargetValue();
            newButton = new AttributeSelectionButton(getEditor(), attrValue, propMerge);
            setAttributeButtonPosition(newButton, 2);
        }

    }

    private BPMNModel getPreviewModel(ProcessNode node, ProcessEditor editor) {
        BPMNModel result = new BPMNModel();
        result.addNode(node);
        return result;
    }

    private void setAttributeButtonPosition(AttributeSelectionButton button, int column) {
        if (button != null) {
            Point pos = new Point();
            pos.y = getNode().getSize().height / 2 + CONFLICTRESOLVER_HEIGHT;
            pos.y += smallButtonCollumns.get(column).size() * AttributeSelectionButton.ATTRIBUTE_HEIGHT;
            pos.x = (int) (-1.5 * AttributeSelectionButton.ATTRIBUTE_WIDTH + column * AttributeSelectionButton.ATTRIBUTE_WIDTH);
            this.smallButtonCollumns.get(column).add(button);
            this.addButton(button, pos);

        }
    }

    private void setButtonPosition(PEButton button, boolean left) {
        if (button != null) {
            Point pos = new Point();
            pos.y = getNode().getSize().height / 2;
            if (left) {
                pos.x = -button.getBounds().width;
            } else {
                pos.x = 0;
            }
            this.addButton(button, pos);
            for (PEButton b : refactoringButtons) {
                pos.y += b.getBounds().getHeight();
                this.addButton(b, pos);
            }
        }
    }

    private PEButton createBigButton(ProcessNode node) {
        PEButton result;
        if (node != null) {
            Image image = ProcessEditorServerUtils.createNodeImage(node);
            image = image.getScaledInstance(CONFLICTRESOLVER_WIDTH, -1, Image.SCALE_SMOOTH);
            result = new PEButton(getEditor(), image);
        } else {
            Image image = getImage("/symbols/stop.png");
            image = image.getScaledInstance(CONFLICTRESOLVER_WIDTH, CONFLICTRESOLVER_HEIGHT, Image.SCALE_DEFAULT);
            result = new PEButton(getEditor(), image);
        }
        result.addListener(this);
        result.setSize(CONFLICTRESOLVER_WIDTH, CONFLICTRESOLVER_HEIGHT);
        return result;

    }
    
    private void createChangeMenu(ProcessObjectMerger relation, ProcessEditor editor) {
        //position of conflict resolver buttons
    }

    @Override
    public void buttonClicked(PEButton button) {
        super.buttonClicked(button);
        ProcessNode n = getEditor().getModel().getNodeById(getNode().getId());
        if (button == conflictResolverButton1) {
            replace(n, sourceAlternative);
        }
        if (button == conflictResolverButton2) {
            replace(n, targetAlternative);
        }
        ((ConflictResolverEditor)getEditor()).checkConflictsSolved();
        getEditor().getMergeAnimator().getAnimationQueue().queue(new ConflictResolvedChecker(getEditor()));
    }

    private void replace(ProcessNode node, ProcessNode by) {
        if (by != null) {
            ((ConflictResolverEditor)getEditor()).getMerger().getObjectMerger(node).setSolved();
            dontDeleteSurroundingEdges(getEditor().getModel().getNodeById(node.getId()));
            getEditor().getAnimator().animateSubstitution(node, by.clone(), 250, 0, true);

        } else {
            getEditor().getAnimator().removeProcessObject(node, 150);
            ((ConflictResolverEditor)getEditor()).getMerger().removeObjectMerger(node);
            //removeConnectedEdges
            for (ProcessObjectMerger r : new ArrayList<ProcessObjectMerger>(((ConflictResolverEditor)getEditor()).getMerger().getMergeRelations())) {
                if (r.getMergedObject() == null) {
                    continue;
                }
                if (r.getMergedObject() instanceof ProcessEdge) {
                    if ((((ProcessEdge) r.getMergedObject()).getSource().getId().equals(node.getId())) || (((ProcessEdge) r.getMergedObject()).getTarget().getId().equals(node.getId()))) {
                        ((ConflictResolverEditor)getEditor()).getMerger().removeObjectMerger(r.getMergedObject());
                    }
                }
            }
        }
    }

    private void dontDeleteSurroundingEdges(ProcessNode node) {
        List<ProcessNode> surroundings = getEditor().getModel().getNeighbourNodes(ProcessEdge.class, node);
        ProcessObjectMerger merger;
        ProcessEdge edge;
        for (ProcessNode surround : surroundings) {
            merger = ((ConflictResolverEditor)getEditor()).getMerger().getObjectMerger(surround);
            if (merger != null && !merger.isDestinyRemove()) {
                edge = getEditor().getModel().getConnectingEdge(node, surround);
                ((ConflictResolverEditor)getEditor()).getMerger().getObjectMerger(edge).setSolved();
                ProcessEdge unmarkedEdge = (ProcessEdge) ((ConflictResolverEditor)getEditor()).getMerger().getObjectMerger(edge).getUnmarkedMergedObject();
                getEditor().getAnimator().animateSubstitution(edge, unmarkedEdge, 250, 0, true);
            }
        }
    }

    @Override
    public void buttonMouseIn(PEButton button) {
        super.buttonMouseIn(button);
    }

    @Override
    public void buttonMouseOut(PEButton button) {
        super.buttonMouseOut(button);
    }
    
    
}
