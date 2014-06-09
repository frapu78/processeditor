/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

import com.inubit.research.animation.DefaultNodeAnimator;
import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.animation.NodeAnimator;
import com.inubit.research.layouter.adapter.BPMNNodeAdapter;
import com.inubit.research.layouter.freeSpace.FreeSpaceLayouter;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import net.frapu.code.visualization.layouter.LayoutMenuitemActionListener;

/**
 *
 * Provides a ProcessEditor with special "goodies" for BPMN.
 *
 * @author fpu
 */
public class BPMNEditor extends ProcessEditor {

    /**
     *
     */
    private static final int NEW_TASK_WIDTH = 100;
    private static final long serialVersionUID = -6660784360605974595L;

    public BPMNEditor() {
        super();
        init();
    }

    public BPMNEditor(ProcessModel model) {
        super(model);
        init();
    }

    private void init() {
        System.out.println("BPMN Editor additions loaded.");
        addDynamicTaskAddRemove();
        addLaneContextMenu();
        addArtifactContextMenu();
        addFreeSpaceLayouterContextMenu();        
    }

    private void addFreeSpaceLayouterContextMenu() {
        //Also add the layouting menu here
        FreeSpaceLayouter l = new FreeSpaceLayouter();
        JMenuItem menuItem = new JMenuItem(l.getDisplayName());
        menuItem.addActionListener(new LayoutMenuitemActionListener(this, l));
        addCustomContextMenuItem(ProcessObject.class, menuItem);
    }

    private void addArtifactContextMenu() {
        JMenu menu = new JMenu("Symbols");
        final String symbols[][] = { {"Man", "pics/symbols/man.png"},
        {"Woman", "pics/symbols/woman.png"},
        {"Ok","pics/symbols/ok.png"},
        {"Question", "pics/symbols/question.png"},
        {"Stop", "pics/symbols/stop.png"},
        {"Computer", "pics/symbols/computer.png"},
        {"Laptop", "pics/symbols/laptop.png"}};
        for (int i=0; i<symbols.length; i++) {
            final int pos = i;
            BufferedImage dst = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = dst.createGraphics();
            g2.drawImage(new ImageIcon(symbols[i][1]).getImage(), 0, 0, 16, 16, this);
            g2.dispose();
            JMenuItem item = new JMenuItem(symbols[i][0], new ImageIcon(dst));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getLastSelectedNode().setProperty(UserArtifact.PROP_IMAGE_LOCATION, symbols[pos][1]);
                    repaint();
                }
            });
            menu.add(item);
        }
        addCustomContextMenuItem(UserArtifact.class, menu);
    }

    /**
     *
     */
    private void addLaneContextMenu() {
        final ProcessEditor outer = this;

        JMenuItem menuItem = new JMenuItem("Add Lane");
        menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/add_lane_below.gif")));
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                LaneableCluster _lc = (LaneableCluster) getSelectionHandler().getLastSelectedNode();
                Lane _l = new Lane("Lane", 100, _lc);
                _l.setProperty(Lane.PROP_XPOS,""+Integer.MAX_VALUE); //so it is always added to the right/bottom
                _l.setProperty(Lane.PROP_YPOS,""+Integer.MAX_VALUE); //so it is always added to the right/bottom
                outer.getModel().addNode(_l);
                _lc.addLane(_l);
            }
        });
        addCustomContextMenuItem(LaneableCluster.class, menuItem);

        menuItem = new JMenuItem("Remove Lane");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Lane _l = (Lane) getSelectionHandler().getLastSelectedNode();
                _l.getParent().removeLane(_l);
                outer.getModel().removeNode(_l);
            }
        });
        addCustomContextMenuItem(Lane.class, menuItem);
    }

    private void addDynamicTaskAddRemove() {
        final ProcessEditor outer = this;

        // Add context entry for "SequenceFlow"
        JMenuItem menuItem = new JMenuItem("Add Task here");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Get recent selection
                ProcessObject lastSel = getSelectionHandler().getLastSelection();
                if (lastSel instanceof SequenceFlow) {
                    SequenceFlow orgFlow = (SequenceFlow) lastSel;

                    int x1 = orgFlow.getRoutingPoints().get(0).x;
                    int y1 = orgFlow.getRoutingPoints().get(0).y;
                    int x2 = orgFlow.getRoutingPoints().get(orgFlow.getRoutingPoints().size() - 1).x;
                    int y2 = orgFlow.getRoutingPoints().get(orgFlow.getRoutingPoints().size() - 1).y;

                    int x = x1 + ((x2 - x1) / 2);
                    int y = y1 + ((y2 - y1) / 2);
                    x += NEW_TASK_WIDTH/2 + FreeSpaceLayouter.PADDING_RIGHT_AFTER_EXTENSION ;

                    Task t = new Task(x, y, "New Task");
                    t.setSize(15, 15);

                    DefaultNodeAnimator anim = new DefaultNodeAnimator(t, null);
                    anim.setNewSize(new Dimension(NEW_TASK_WIDTH, 60));

                    List<NodeAnimator> animList = new LinkedList<NodeAnimator>();
                    animList.add(anim);

                    SequenceFlow addFlow = new SequenceFlow();
                    addFlow.setTarget(orgFlow.getTarget());
                    addFlow.setSource(t);
                    orgFlow.setTarget(t);
                    // Remove dock points
                    addFlow.setProperty(SequenceFlow.PROP_TARGET_DOCKPOINT, orgFlow.getProperty(SequenceFlow.PROP_TARGET_DOCKPOINT));
                    orgFlow.setProperty(SequenceFlow.PROP_TARGET_DOCKPOINT, "");
                    getModel().addNode(t);
                    getModel().addEdge(addFlow);

                    // Check if node needs to be added to Cluster
                    for (int i = getModel().getNodes().size() - 1; i >= 0; i--) {
                        ProcessNode node = getModel().getNodes().get(i);
                        if (node instanceof Cluster) {
                            Cluster c = (Cluster) node;
                            if (c.isContainedGraphically(getModel().getNodes(), t, true)) {
                                c.addProcessNode(t);
                                break; // Skip out of loop
                            }
                        }
                    }

                    selectionHandler.clearSelection();
                    selectionHandler.addSelectedObject(t);

                    // Layout model
                    FreeSpaceLayouter layouter = new FreeSpaceLayouter();
                    LayoutingAnimator anlayouter = new LayoutingAnimator(layouter);
                    layouter.setSelectedNode(new BPMNNodeAdapter(orgFlow.getSource()));
                    layouter.setSpaceToFree(NEW_TASK_WIDTH + 20); //+20 to have some free space
                    try {
                    	anlayouter.layoutModelWithAnimation(outer, animList, 50, 100, 0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    repaint();
                }

            }
        });
        addCustomContextMenuItem(SequenceFlow.class, menuItem);

        // Add context entry for "Task"
        menuItem = new JMenuItem("Remove this Task");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Get recent selection
                ProcessObject lastSel = getSelectionHandler().getLastSelection();
                if (lastSel instanceof Task) {
                    Task task = (Task) lastSel;
                    List<NodeAnimator> animList = new LinkedList<NodeAnimator>();
                    //it would be nice to have this animation, but as the node gets deleted
                    //immediately this is not working 
                    //-> then use editor.getanimtor.deletenode instad of model.deleteNode
                    //DefaultNodeAnimator anim = new DefaultNodeAnimator(task,null);
                    //anim.setNewSize(new Dimension(15,15));
                    //animList.add(anim);
                    //contracting the model so unused space is consumed
                    FreeSpaceLayouter layouter = new FreeSpaceLayouter();
                    LayoutingAnimator anlayouter = new LayoutingAnimator(layouter);
                    layouter.setSelectedNode(new BPMNNodeAdapter(task));
                    layouter.setSpaceToFree(-(task.getSize().width)); //+20 to have some free space
                    try {
                        anlayouter.layoutModelWithAnimation(outer, animList, 50, 100, 0);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // Check very simple case: no incoming or outgoing edge
                    if (getModel().getIncomingEdges(SequenceFlow.class, task).size() == 0 |
                            getModel().getOutgoingEdges(SequenceFlow.class, task).size() == 0) {
                        // Simply remove node
                        getModel().removeNode(task);
                    } else {
                        // Check simple case: 1 in, 1 out-edge
                        if (getModel().getIncomingEdges(SequenceFlow.class, task).size() == 1 &
                                getModel().getOutgoingEdges(SequenceFlow.class, task).size() == 1) {
                            // Remove node and point incoming edge to target of outgoing edge
                            ProcessEdge sourceEdge = getModel().getIncomingEdges(SequenceFlow.class, task).get(0);
                            ProcessEdge targetEdge = getModel().getOutgoingEdges(SequenceFlow.class, task).get(0);
                            sourceEdge.setTarget(targetEdge.getTarget());
                            getModel().removeEdge(targetEdge);
                            getModel().removeNode(task);
                        }
                    }

                    selectionHandler.clearSelection();
                    repaint();
                }

            }
        });
        addCustomContextMenuItem(Task.class, menuItem);
    }
}
