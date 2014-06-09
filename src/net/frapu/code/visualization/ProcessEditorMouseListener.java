/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import com.inubit.research.client.UserCredentials;
import net.frapu.code.visualization.editors.PropertyEditor;
import net.frapu.code.visualization.helper.NameNodeOverlay;
import net.frapu.code.visualization.helper.NodeOnSelectMenuBasis;
import net.frapu.code.visualization.helper.Ruler;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.helper.Selector;

/**
 *
 * @author frank
 */
public class ProcessEditorMouseListener implements
        MouseListener, MouseMotionListener, MouseWheelListener {

    private ProcessEditor editor;
    /** The last mouse click position */
    protected Point mouseClickLocation = new Point(0, 0);
    /** The rubber band */
    private Rectangle rubberBand = null;
    /** The origin of the rubber band */
    private Point rubberBandOrigin = null;
    /** The attachment source node */
    private ProcessNode attachementSource = null;
    /** The recent map of ProcessNodes that have Rulers attached */
    Map<ProcessNode, Ruler> rulerMap = new HashMap<ProcessNode, Ruler>();
    protected Cluster clusterUnderMouse = null;

    public ProcessEditorMouseListener(ProcessEditor editor) {
        super();
        this.editor = editor;
    }
    /**
     *
     * MOUSE (MOTION) LISTENER FOLLOWS
     *
     */
    protected ProcessNode dragSource;
    protected ProcessObject dragTarget;
    protected Point dragOffset = new Point(0, 0);
    protected int dragButton = 0;
    protected Point dragStart = new Point(0, 0);
    protected Selector selector = null;

    @Override
    public void mouseClicked(final MouseEvent e) {

        //this.requestFocusInWindow();
        // Save position
        updateMouseClickPoint(e);
        // Reset dragableObject to zero
        editor.setDragableObject(null);
        final ProcessObject selectedObject = editor.detectProcessObject(getMouseClickLocation());

        // Check right button
        if ((e.getButton() == MouseEvent.BUTTON3)
                | // Check Control Mask for Apple
                (e.getButton() == MouseEvent.BUTTON1 & (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK))) {

            // Check selection
            if (selectedObject == null) {
                // Show pop-up menu
                editor.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            } else {

                //
                // Initialize context menu for edges/nodes
                //
                editor.getContextMenu().removeAll();
                JMenuItem menuItem = new JMenuItem("Properties...");
                menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/edit_small.gif")));
                // Add action listener
                menuItem.addActionListener(new java.awt.event.ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Create properties panel
                        // @todo Store in list for re-use!
                        final PropertiesPanel propPanel = new PropertiesPanel(selectedObject, editor.isEditable());
                        //propPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
                        final JDialog propDialog = new JDialog();
                        propDialog.setTitle("Properties");
                        JScrollPane scrollPane = new JScrollPane(propPanel);
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        propDialog.setLayout(new GridBagLayout());
                        GridBagConstraints c = new GridBagConstraints();
                        c.gridx = 0;
                        c.gridy = 0;
                        c.weightx = 100;
                        c.weighty = 100;
                        c.fill = GridBagConstraints.BOTH;
                        propDialog.add(scrollPane, c);
                        // Create button
                        JButton okButton = new JButton("Ok");
                        okButton.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Ok Button pressed, update changes
                                for (String key : propPanel.getData().keySet()) {
                                    PropertyEditor propEditor = propPanel.getData().get(key);
                                    selectedObject.setProperty(key, propEditor.getValue());
                                    propDialog.dispose();
                                    editor.repaint();
                                }
                            }
                        });
                        c.weighty = 0;
                        c.gridy = 1;
                        propDialog.add(okButton, c);
                        propDialog.pack();
                        propDialog.setResizable(false);
                        SwingUtils.center(propDialog);
                        propDialog.setModal(true);
                        propDialog.setVisible(true);
                        // Release listener!
                        propPanel.dispose();
                        editor.repaint();
                    }
                });
                editor.getContextMenu().add(menuItem);

                // Add editing elements if isEditable

                if (editor.isEditable()) {

                    editor.getContextMenu().add(new JSeparator());

                    menuItem = new JMenuItem("Cut");
                    menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/cut_small.gif")));
                    menuItem.addActionListener(new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
                            clipboard.cut(editor, editor.getSelectionHandler());
                            editor.repaint();
                        }
                    });
                    editor.getContextMenu().add(menuItem);

                    menuItem = new JMenuItem("Copy");
                    menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/copy_small.gif")));
                    menuItem.addActionListener(new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
                            clipboard.copy(editor, editor.getSelectionHandler());
                        }
                    });
                    editor.getContextMenu().add(menuItem);

                    menuItem = new JMenuItem("Delete");
                    menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/delete_small.gif")));
                    menuItem.addActionListener(new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
                            clipboard.delete(editor, editor.getSelectionHandler());
                            editor.repaint();
                            // Inform listeners
                            for (ProcessEditorListener l : editor.getListeners()) {
                                l.processObjectClicked(null);
                            }
                        }
                    });
                    editor.getContextMenu().add(menuItem);

                    editor.getContextMenu().add(new JSeparator());

                    // Front/Back is only available for nodes
                    if (selectedObject instanceof ProcessNode) {

                        final ProcessNode selectedNode = ((ProcessNode) selectedObject);

                        if (editor.getSelectionHandler().getSelectionSize() > 1) {
                            // Add vertical align menu
                            JMenu hAlignMenu = new JMenu("Align horizontally");
                            menuItem = new JMenuItem("Top");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int yPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (yPos != Integer.MAX_VALUE) {
                                                int diff = yPos - n.getTopLeftPos().y;
                                                n.setPos(n.getPos().x, n.getPos().y + diff);
                                            } else {
                                                yPos = n.getTopLeftPos().y;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            hAlignMenu.add(menuItem);
                            menuItem = new JMenuItem("Center");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int yPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (yPos != Integer.MAX_VALUE) {
                                                n.setPos(n.getPos().x, yPos);
                                            } else {
                                                yPos = n.getPos().y;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            hAlignMenu.add(menuItem);
                            menuItem = new JMenuItem("Bottom");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int yPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (yPos != Integer.MAX_VALUE) {
                                                int diff = yPos - (n.getPos().y + n.getSize().height / 2);
                                                n.setPos(n.getPos().x, n.getPos().y + diff);
                                            } else {
                                                yPos = n.getPos().y + n.getSize().height / 2;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            hAlignMenu.add(menuItem);
                            editor.getContextMenu().add(hAlignMenu);

                            // Add vertical align menu
                            JMenu vAlignMenu = new JMenu("Align vertically");
                            menuItem = new JMenuItem("Left");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int xPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (xPos != Integer.MAX_VALUE) {
                                                int diff = xPos - (n.getPos().x - n.getSize().width / 2);
                                                n.setPos(n.getPos().x + diff, n.getPos().y);
                                            } else {
                                                xPos = n.getPos().x - n.getSize().width / 2;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            vAlignMenu.add(menuItem);
                            menuItem = new JMenuItem("Center");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int xPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (xPos != Integer.MAX_VALUE) {
                                                n.setPos(xPos, n.getPos().y);
                                            } else {
                                                xPos = n.getPos().x;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            vAlignMenu.add(menuItem);
                            menuItem = new JMenuItem("Right");
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    int xPos = Integer.MAX_VALUE;
                                    for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                        if (o instanceof ProcessNode) {
                                            ProcessNode n = (ProcessNode) o;
                                            if (xPos != Integer.MAX_VALUE) {
                                                int diff = xPos - (n.getPos().x + n.getSize().width / 2);
                                                n.setPos(n.getPos().x + diff, n.getPos().y);
                                            } else {
                                                xPos = n.getPos().x + n.getSize().width / 2;
                                            }
                                        }
                                    }
                                    editor.repaint();
                                }
                            });
                            vAlignMenu.add(menuItem);
                            editor.getContextMenu().add(vAlignMenu);
                            editor.getContextMenu().add(new JSeparator());
                        }
                        //Also add the layouting menu here this is needed for the partial layouting
                        //to work! If clicking in the background is the only possibility to activate
                        //the layouting menu the selection would get lost!
                        editor.addLayoutMenu(editor.getContextMenu());
                        editor.getContextMenu().add(new JSeparator());


                        menuItem = new JMenuItem("Bring to front");
                        menuItem.addActionListener(new java.awt.event.ActionListener() {

                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                    if (o instanceof ProcessNode) {
                                        editor.getModel().moveToFront((ProcessNode) o);
                                    }
                                }
                                editor.repaint();
                            }
                        });
                        editor.getContextMenu().add(menuItem);

                        menuItem = new JMenuItem("Send to back");
                        menuItem.addActionListener(new java.awt.event.ActionListener() {

                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
                                    if (o instanceof ProcessNode) {
                                        editor.getModel().moveToBack((ProcessNode) o);
                                    }
                                }
                                editor.repaint();
                            }
                        });
                        editor.getContextMenu().add(menuItem);

                        // Add refactoring menu
                        JMenu typeMenu = new JMenu("Type");

                        if (selectedNode.getVariants().size() > 0) {
                            editor.getContextMenu().add(typeMenu);
                        }
                        for (final Class<?> nodeType : selectedNode.getVariants()) {
                            menuItem = new JCheckBoxMenuItem(nodeType.getSimpleName());
                            menuItem.setSelected(selectedNode.getClass() == nodeType);
                            menuItem.setIcon(new ImageIcon(ProcessUtils.createPreviewImage(nodeType, 16)));
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {
                                    ProcessUtils.refactorNode(editor, selectedNode, nodeType);
                                }
                            });
                            typeMenu.add(menuItem);
                        }


                    }

                    // Process Edges
                    if (selectedObject instanceof ProcessEdge) {
                        menuItem = new JMenuItem("Add routing point here");
                        menuItem.addActionListener(new java.awt.event.ActionListener() {

                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                ProcessEdge edge = (ProcessEdge) selectedObject;
                                edge.addRoutingPoint(getMouseClickLocation());
                                editor.repaint();
                            }
                        });
                        editor.getContextMenu().add(menuItem);

                        JMenu typeMenu = new JMenu("Type");
                        editor.getContextMenu().add(typeMenu);
                        for (final Class<?> edgeType : editor.getModel().getSupportedEdgeClasses()) {
                            menuItem = new JMenuItem(edgeType.getSimpleName());
                            menuItem.addActionListener(new java.awt.event.ActionListener() {

                                @Override
                                public void actionPerformed(java.awt.event.ActionEvent evt) {

                                    ProcessEdge edge = (ProcessEdge) selectedObject;

                                    ProcessNode sourceNode = edge.getSource();
                                    ProcessNode targetNode = edge.getTarget();
                                    try {
                                        ProcessEdge newEdge = (ProcessEdge) edgeType.newInstance();
                                        newEdge.setSource(sourceNode);
                                        newEdge.setTarget(targetNode);
                                        // Copy routing points
                                        for (int i = 1; i < edge.getRoutingPoints().size() - 1; i++) {
                                            newEdge.addRoutingPoint(edge.getRoutingPoints().get(i));
                                        }
                                        newEdge.copyPropertiesFrom(edge);
                                        editor.getModel().removeEdge(edge);
                                        editor.getModel().addEdge(newEdge);

                                    } catch (Exception ex) {
                                    }

                                    editor.repaint();
                                }
                            });
                            typeMenu.add(menuItem);
                        }

                    }

                    // Process Routing point
                    if (selectedObject instanceof RoutingPointDragable) {
                        menuItem = new JMenuItem("Remove routing point");
                        menuItem.addActionListener(new java.awt.event.ActionListener() {

                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {

                                RoutingPointDragable rpd = (RoutingPointDragable) selectedObject;
                                ProcessEdge edge = rpd.getEdge();
                                edge.removeRoutingPoint(rpd.getOrderPosition());

                                editor.repaint();
                            }
                        });
                        editor.getContextMenu().add(menuItem);
                    }
                }

                // Check if ref is available
                List<ProcessObject> selection = editor.getSelectionHandler().getSelection();
                if ((selection.size() == 1)) {
                    if (selection.get(0).getProperty(ProcessNode.PROP_REF) != null) {
                        if (selection.get(0).getProperty(ProcessNode.PROP_REF).length() > 0) {
                            List<Reference> refHolders = new LinkedList<Reference>();
                            String ref = selection.get(0).getProperty(ProcessNode.PROP_REF);
                            JMenu references = new JMenu("References");
                            // Iterate over references
                            StringTokenizer st = new StringTokenizer(ref, ",");
                            while (st.hasMoreTokens()) {
                                String reference = st.nextToken();
                                String displayText = reference;
                                String id = "";
                                ProcessModel model = null;
                                // Try to resolve reference name
                                try {
                                    String modelUri = reference;
                                    if (modelUri.contains("#")) {
                                        modelUri = reference.substring(0, reference.indexOf("#"));
                                        id = reference.substring(reference.indexOf("#") + 1);
                                    }
                                    // Open model with credentials of the current model!
                                    UserCredentials credentials = (UserCredentials) editor.getSelectedModel().getTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS);
                                    //System.out.println(URI.create(modelUri));
                                    model = ProcessUtils.parseProcessModelSerialization(URI.create(modelUri), credentials);
                                    displayText = model.getProcessName();
                                    if (model.getObjectById(id) != null) {
                                        Object o = model.getObjectById(id);
                                        String label = "";
                                        if (o instanceof ProcessNode) {
                                            label = ((ProcessNode) o).getText();
                                        }
                                        if (o instanceof ProcessEdge) {
                                            label = ((ProcessEdge) o).getLabel();
                                        }
                                        displayText = label + " (" + displayText + ")";
                                        refHolders.add(new Reference(URI.create(reference), (ProcessObject) o, model));
                                    }
                                    displayText += " - " + model.getDescription();
                                } catch (Exception ex) {
                                    //ex.printStackTrace();
                                    displayText = displayText + " (n/a)";
                                }
                                JMenuItem refItem = new JMenuItem(displayText);
                                final ProcessModel finModel = model;
                                // Add uri of upper process model
                                finModel.setProperty(ProcessModel.ATTR_PARENT_REF, editor.getModel().getProcessModelURI()+"#"+selection.get(0).getId());
                                final String nodeId = id;
                                refItem.addActionListener(new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent ae) {
                                        if (finModel != null) {
                                            // Open reference in new window!
                                            ProcessEditor newEditor = editor.openNewModel(finModel);
                                            newEditor.getSelectionHandler().addSelectedObject(finModel.getObjectById(nodeId));
                                            newEditor.repaint();
                                        }
                                    }
                                });
                                references.add(refItem);
                            }

                            // Call update references on recent node
                            if (selection.get(0) instanceof ProcessNode) {
                                ProcessNode node = (ProcessNode) selection.get(0);
                                node.updateReferences(refHolders);
                                editor.repaint();
                            }

                            editor.getContextMenu().add(new JPopupMenu.Separator());
                            editor.getContextMenu().add(references);
                        }
                    }
                }

                // check if custom menu is available
                LinkedList<JMenuItem> cl = editor.getCustomContextMenuItems(selectedObject.getClass());
                if (cl != null && cl.size() > 0) {
                    editor.getContextMenu().add(new JPopupMenu.Separator());
                    for (JMenuItem i : cl) {
                        editor.getContextMenu().add(i);
                    }
                }

                // Show context menu
                editor.getContextMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }

        updateCursor(getMouseClickLocation());
    }

    /**
     * @todo: Refactor all to use Dragable only!!!
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {

        editor.requestFocusInWindow();
        updateMouseClickPoint(e);
        // Reset dragableObject to zero
        editor.setDragableObject(null);
        // Update selected object
        ProcessObject po = editor.detectProcessObject(getMouseClickLocation());
        // Notify listeners about selection - if po is null -> background is selected
        //editor.startProcessEditorListenerUpdate();
        //for (ProcessEditorListener l : editor.getListeners()) {
        //    l.processObjectClicked(po);
        //}
        //editor.endProcessEditorListenerUpdate();
        // Check if editor is enabled
        if (!editor.isEditable()) {
            // Allow toggle selection
            //editor.getSelectionHandler().clearSelection();
            if (po != null) {
                editor.getSelectionHandler().toggleSelectedObject(po);
            }
            editor.repaint();            
        }
        if (!editor.isEnabled()) {
            return;
        }
        // Remove all Selectors
        if (selector != null) {
            editor.getAnimator().removeProcessObject(selector, ProcessUtils.SELECTOR_FADE_TIME);
            selector = null;
        }
        // Add next node helper (or remove it if po == null)
       /* 
            // Check if left mouse button is pressed
            if (e.getButton() == MouseEvent.BUTTON1) {
                showNextNodeHelper(po);
            } else {
                // Remove next node helper
                showNextNodeHelper(null);
            }
        */

        // Check if ALT is pressed
        if (!((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)) {
            // Clear selection if selected object is not already in selection
            if (!editor.getSelectionHandler().contains(po)) {
                editor.getSelectionHandler().clearSelection();
            }
        }

        // Object selection detection
        if (po != null) {
            if (po instanceof RoutingPointDragable) {
                // If routing point, add corresponding edge to selection
                RoutingPointDragable rpd = (RoutingPointDragable) po;
                editor.getSelectionHandler().addSelectedObject(rpd.getEdge());
            }

            editor.getSelectionHandler().addSelectedObject(po);
            setRubberBand(null);
            editor.repaint();

        } else {
            editor.getSelectionHandler().clearSelection();

            // Start rubber band selection
            startRubberBandSelection();
        }

        // (de)select nodes
        for (ProcessNode node : editor.getModel().getNodes()) {
            node.setSelected(editor.getSelectionHandler().contains(node));
        }
        // (de)select edges
        for (ProcessEdge edge : editor.getModel().getEdges()) {
            edge.setSelected(editor.getSelectionHandler().contains(edge));
        }

        // Refresh
        editor.repaint();

        // Notify listeners about node selection
        if (po instanceof ProcessNode | po instanceof ProcessEdge) {
            editor.startProcessEditorListenerUpdate();
            for (ProcessEditorListener l : editor.getListeners()) {
                if (e.getClickCount() == 1) {
                    l.processObjectClicked(po);
                } else {
                    l.processObjectDoubleClicked(po);
                }
            }
            editor.endProcessEditorListenerUpdate();
        }

        // Notify listeners about background selection
        /*if (po==null) {
        editor.startProcessEditorListenerUpdate();
        for (ProcessEditorListener l : editor.getListeners()) {

        }
        editor.endProcessEditorListenerUpdate();
        }*/


        if (po != null) {
            if (po instanceof ProcessNode) {
                if (e.getClickCount() == 2) {
                    NameNodeOverlay _n = new NameNodeOverlay(editor, (ProcessNode) po);
                    //notify Listeners about Text editing
                    for (ProcessEditorListener l : new ArrayList<ProcessEditorListener>(editor.getListeners())) {
                        l.processNodeEditingStarted((ProcessNode) po, _n.getTextField());
                    }
                    dragSource = null;
                } else {
                    dragSource = (ProcessNode) po;
                }
            } else {
                dragSource = null;
            }
        } else {
            dragSource = null;
        }
        dragButton = e.getButton();

        // Calculate dragOffset & save start position
        if (editor.getDragableObject() != null) {
            dragOffset.x = editor.getDragableObject().getPos().x - mouseClickLocation.x;
            dragOffset.y = editor.getDragableObject().getPos().y - mouseClickLocation.y;
            dragStart = new Point(editor.getDragableObject().getPos().x, editor.getDragableObject().getPos().y);
        }

        // (de)select edges
        for (ProcessEdge edge : editor.getModel().getEdges()) {
            edge.setSelected(editor.getSelectionHandler().contains(edge));
        }


        // Refresh
        editor.repaint();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!editor.isEditable()) {
            return;
        }

        // Update rubber band
        updateRubberBandSelection();
        // Delete rubber band
        setRubberBand(null);
        // Delete alignment rulers
        editor.setAlignmentRuler(Integer.MAX_VALUE, Integer.MAX_VALUE);
        // Delete rulers
        clearRulers();
        ProcessObject po = editor.getSelectionHandler().getLastSelection();
       // Check if left mouse button is pressed
        if (e.getButton() == MouseEvent.BUTTON1) {
            showNextNodeHelper(po);
        } else {
            // Remove next node helper
            showNextNodeHelper(null);
        }
        

        if (e.getButton() == MouseEvent.BUTTON1) {

            // 1. Update attached nodes
            if (attachementSource != null) {
                if (dragSource instanceof AttachedNode) {
                    AttachedNode nodeToAttach = (AttachedNode) dragSource;
                    editor.getModel().getAttachedNodeHandler().attach(attachementSource, nodeToAttach);
                    attachementSource.setHighlighted(false);
                }
            } else {
                if (dragSource instanceof AttachedNode) {
                    AttachedNode nodeToAttach = (AttachedNode) dragSource;
                    editor.getModel().getAttachedNodeHandler().detach(nodeToAttach);
                }
            }


            // Remove current selection from all Clusters
            for (ProcessObject dragObject : editor.getSelectionHandler().getSelection()) {
                if (!(dragObject instanceof ProcessNode)) {
                    continue;
                }
                ProcessNode dragNode = (ProcessNode) dragObject;
                // @todo: Generalize handling of lanes
                if (dragNode instanceof Lane) {
                    continue;
                }
                List<Cluster> clusterList = editor.getModel().getClusters();
                for (Cluster c : clusterList) {
                    c.removeProcessNode(dragNode);
                }
            }

            // 2. Update containments of all Clusters if selection moved
            List<ProcessNode> allreadyAddedSomewhere = new LinkedList<ProcessNode>();
            if (clusterUnderMouse != null) {
                clusterUnderMouse.setHighlighted(false);

                if (!clusterUnderMouse.isCollapsed()) {
                    for (ProcessObject dragObject : editor.getSelectionHandler().getSelection()) {
                        if (!(dragObject instanceof ProcessNode)) {
                            continue;
                        }
                        ProcessNode dragNode = (ProcessNode) dragObject;

                        // Add to cluster under mouse if contained
                        if (clusterUnderMouse.isContainedGraphically(editor.getModel().getNodes(), dragNode, false)
                                && !allreadyAddedSomewhere.contains(dragNode)) {
                            clusterUnderMouse.addProcessNode(dragNode);
                            allreadyAddedSomewhere.add(dragNode);
                            editor.getModel().moveAfter(dragNode, clusterUnderMouse);
                        }
                    }
                }
            }
//            List<ProcessNode> addedNodes = new LinkedList<ProcessNode>();
//            for (int i = editor.getModel().getNodes().size() - 1; i >= 0; i--) {
//                ProcessNode n = editor.getModel().getNodes().get(i);
//                if (n instanceof Cluster) {
//                    Cluster c = (Cluster) n;
//                    if (!c.isCollapsed()) {
//                        for (ProcessObject dragObject : editor.getSelectionHandler().getSelection()) {
//                            if (!(dragObject instanceof ProcessNode)) {
//                                continue;
//                            }
//                            ProcessNode dragNode = (ProcessNode) dragObject;
//                            // Try to remove
//                            c.removeProcessNode(dragNode);
//                            // Add if contained
//                            if (c.isContainedGraphically(editor.getModel().getNodes(), dragNode, true) && !addedNodes.contains(dragNode)) {
//                                //editor.getModel().moveAfter(dragNode, c);
//                                //System.out.println("Moved "+dragNode+" after "+c);
//                                c.addProcessNode(dragNode);
//                                addedNodes.add(dragNode);
//                            }
//                        }
//                        if (addedNodes.size() > 0) {
//                            break;
//                        }
//                    }
//                }
//            }

            // Inform listeners
            if (editor.getDragableObject() != null) {
                // Check if dragged
                Point dragPos = editor.getDragableObject().getPos();

                // @todo: FIX to support multiple selections!!!‚
                if (!(dragPos.x == dragStart.x && dragPos.y == dragStart.y)) {
                    for (ProcessEditorListener listener : editor.getListeners()) {
                        listener.processObjectDragged(editor.getDragableObject(), dragStart.x, dragStart.y);
                    }
                }

                // Unset virgin editor
                editor.setVirginEditor(false);
            }

        }
        // Check button 2 (create connection)
        if (e.getButton() == MouseEvent.BUTTON3) {
            // If dragSource and dragTarget exist, create corresponding flow
            if (dragSource != null & dragTarget != null) {
                // Check if dragTarget is ProcessNode
                if (dragTarget instanceof ProcessNode) {
                    // Check if there's already a flow betwenn dragSource and dragTarget
                    ProcessEdge edge = editor.getModel().getUtils().createDefaultEdge(dragSource, (ProcessNode) dragTarget);
                    if (edge != null) {
                        // Add only if dragSource != dragTarget
                        if (dragSource != dragTarget) {
                            editor.getModel().addEdge(edge);
                        }
                    }
                }
                // Check if dragTarget is ProcessEdge
                if (dragTarget instanceof ProcessEdge) {
                    EdgeDocker docker = new EdgeDocker();
                    docker.setDockedEdge((ProcessEdge) dragTarget);
                    ProcessEdge edge = editor.getModel().getUtils().createDefaultEdge(dragSource, docker);
                    if (edge != null) {
                        editor.getModel().addNode(docker);
                        editor.getModel().addEdge(edge);
                    }
                }

                dragSource.setHighlighted(false);
                dragTarget.setHighlighted(false);

                dragSource = null;
                dragTarget = null;
                editor.repaint();
            }
        }

        editor.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            // make it a reasonable amount of zoom
            // .025 gives a nice slow transition
            double newScale = editor.getScale() + (.025 * e.getWheelRotation());
            // do not cross negative threshold.
            // also, setting scale to 0 has bad effects
            editor.setScale(Math.max(ProcessEditor.MIN_DIAGRAM_SCALE, newScale));
            editor.repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (!editor.isEditable()) {
            return;
        }

        updateMouseClickPoint(evt);

        if (editor.getDragableObject() == null) {
            // Update rubber band
            updateRubberBandSelection();
            editor.repaint();
        }

        if (editor.getDragableObject() != null) {
            // Hide all node helpers
            showNextNodeHelper(null); //why really remove them? It looks nice that they move with the node :-)

            // Check button 1 (move selection)
            if (dragButton == MouseEvent.BUTTON1) {
                //Handle special case when draging an edge
                if (editor.getDragableObject() instanceof ProcessEdgeDragHelper) {
                    //ProcessEdgeDragHelper helper = (ProcessEdgeDragHelper) editor.getDragableObject();
                    if (editor.getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
                        editor.getDragableObject().setPos(mouseClickLocation);
                    } else if (editor.getCursor().getType() == Cursor.W_RESIZE_CURSOR) {
                        editor.getDragableObject().setPos(mouseClickLocation);
                    }

                    updateClusters();
                } else {
                    // Default behavior (move)
                    if (editor.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
                        // Check all Dragables from selection
                        int xDiff = mouseClickLocation.x + dragOffset.x - editor.getDragableObject().getPos().x;
                        int yDiff = mouseClickLocation.y + dragOffset.y - editor.getDragableObject().getPos().y;
                        editor.getSelectionHandler().moveSelection(xDiff, yDiff);
                        updateAlignmentRulers();
                        updateAttachedNodes(xDiff, yDiff);
                        updateClusters();
                    }
                    if (dragSource != null) {
                        // North resize
                        if (editor.getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
                            ProcessNode oldSource = dragSource.clone();
                            if ((evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK) {
                                dragSource.setSize(dragSource.getSize().width,
                                        (dragSource.getPos().y - mouseClickLocation.y) * 2);
                            } else {
                                int sizeDiff = dragSource.getTopLeftPos().y - mouseClickLocation.y;
                                sizeDiff = sizeDiff / 2 * 2;
                                int newSize = dragSource.getSize().height + sizeDiff;
                                dragSource.setSize(dragSource.getSize().width, newSize);
                                // Check if setSize worked
                                if (dragSource.getSize().height == newSize) {
                                    // Move position directly (skipping setPos to avoid moving nodes)
                                    dragSource.setProperty(ProcessNode.PROP_YPOS, "" + (dragSource.getPos().y - sizeDiff / 2));
                                }
                            }
                            if (editor.getModel().getAttachedNodeHandler() != null) {
                                editor.getModel().getAttachedNodeHandler().sourceResized(
                                        oldSource, dragSource, getAttachedNodes(),
                                        AttachedNodeHandler.RESIZE_NORTH);
                            }
                            updateVerticalRulers();
                        }
                        // South resize
                        if (editor.getCursor().getType() == Cursor.S_RESIZE_CURSOR) {
                            ProcessNode oldSource = dragSource.clone();
                            if ((evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK) {
                                dragSource.setSize(dragSource.getSize().width,
                                        (mouseClickLocation.y - dragSource.getPos().y) * 2);
                            } else {
                                int sizeDiff = mouseClickLocation.y - (dragSource.getTopLeftPos().y + dragSource.getSize().height);
                                sizeDiff = sizeDiff / 2 * 2;
                                int newSize = dragSource.getSize().height + sizeDiff;
                                dragSource.setSize(dragSource.getSize().width, newSize);
                                // Check if setSize worked
                                if (dragSource.getSize().height == newSize) {
                                    // Move position directly (skipping setPos to avoid moving nodes)
                                    dragSource.setProperty(ProcessNode.PROP_YPOS, "" + (dragSource.getPos().y + sizeDiff / 2));
                                }
                            }
                            if (editor.getModel().getAttachedNodeHandler() != null) {
                                editor.getModel().getAttachedNodeHandler().sourceResized(
                                        oldSource, dragSource, getAttachedNodes(),
                                        AttachedNodeHandler.RESIZE_SOUTH);
                            }
                            updateVerticalRulers();
                        }
                        // West resize
                        if (editor.getCursor().getType() == Cursor.W_RESIZE_CURSOR) {
                            ProcessNode oldSource = dragSource.clone();
                            if ((evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK) {
                                dragSource.setSize((dragSource.getPos().x - mouseClickLocation.x) * 2,
                                        dragSource.getSize().height);
                            } else {
                                int sizeDiff = dragSource.getTopLeftPos().x - mouseClickLocation.x;
                                sizeDiff = sizeDiff / 2 * 2; // makes sense! it evens the value (5/2*2 = 4)
                                int newSize = dragSource.getSize().width + sizeDiff;
                                dragSource.setSize(newSize, dragSource.getSize().height);
                                // Check if setSize worked
                                if (dragSource.getSize().width == newSize) {
                                    // Move position directly (skipping setPos to avoid moving nodes)
                                    dragSource.setProperty(ProcessNode.PROP_XPOS, "" + (dragSource.getPos().x - sizeDiff / 2));
                                }
                            }

                            if (editor.getModel().getAttachedNodeHandler() != null) {
                                editor.getModel().getAttachedNodeHandler().sourceResized(
                                        oldSource, dragSource, getAttachedNodes(),
                                        AttachedNodeHandler.RESIZE_WEST);
                            }
                            updateHorizontalRulers();
                        }
                        // East resize
                        if (editor.getCursor().getType() == Cursor.E_RESIZE_CURSOR) {
                            if (dragSource != null) {
                            	ProcessNode oldSource = null;
                            	if (editor.getModel().getAttachedNodeHandler() != null) {
                            		oldSource = dragSource.clone();
                            	}
                                if ((evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == MouseEvent.ALT_DOWN_MASK) {
                                    dragSource.setSize((mouseClickLocation.x - dragSource.getPos().x) * 2,
                                            dragSource.getSize().height);
                                } else {
                                    int sizeDiff = mouseClickLocation.x - (dragSource.getTopLeftPos().x + dragSource.getSize().width);
                                    sizeDiff = sizeDiff / 2 * 2;
                                    int newSize = dragSource.getSize().width + sizeDiff;
                                    dragSource.setSize(newSize, dragSource.getSize().height);
                                    // Check if setSize worked
                                    if (dragSource.getSize().width == newSize) {
                                        // Move position directly (skipping setPos to avoid moving nodes)
                                        dragSource.setProperty(ProcessNode.PROP_XPOS, "" + (dragSource.getPos().x + sizeDiff / 2));
                                    }
                                }
                                if (editor.getModel().getAttachedNodeHandler() != null) {
                                    editor.getModel().getAttachedNodeHandler().sourceResized(
                                            oldSource, dragSource, getAttachedNodes(),
                                            AttachedNodeHandler.RESIZE_EAST);
                                }
                            }
                            updateHorizontalRulers();
                        }
                    }
                }
                editor.repaint();
            }

            // Check button 2 (create connection)
            if (dragButton == MouseEvent.BUTTON3) {

                // Check if dragTarget needs to be deselected!
                if (dragTarget != null) {
                    dragTarget.setHighlighted(false);
                }
                ProcessObject po = editor.detectProcessObject(mouseClickLocation);
                if (po != null) {
                    // Just take ProcessNodes
                    if (po instanceof ProcessNode) {
                        dragTarget = (ProcessNode) po;
                    }
                    // Check if Edge
                    if (po instanceof ProcessEdge) {
                        // Check if edge support docking
                        ProcessEdge edge = (ProcessEdge) po;
                        if (edge.isDockingSupported()) {
                            dragTarget = po;
                        }
                    }
                }
                // Return if target = null
                if (dragTarget == null) {
                    return;
                    // Return if source=target
                }
                if (dragTarget == dragSource) {
                    return;
                    // Ok, possible target found, highlight
                }
                // Detect if Connection is possible
                if (dragTarget instanceof ProcessNode) {
                    ProcessEdge edge = editor.getModel().getUtils().createDefaultEdge(dragSource, (ProcessNode) dragTarget);
                    if (edge != null) {
                        dragTarget.setHighlighted(true);
                    }
                } else {
                    dragTarget.setHighlighted(true);
                }
                editor.repaint();
            }

        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMouseClickPoint(e);
        updateCursor(mouseClickLocation);
        // Check if no button is pressed
        if (e.getButton() == 0 && editor.isEditable() == false) {
            ProcessObject po = editor.detectProcessObject(mouseClickLocation);
            if (po != null) {
                if (po instanceof ProcessNode) {
                    if (selector != null) {
                        if (selector.getNode() != po) {
                            editor.getAnimator().removeProcessObject(selector, ProcessUtils.SELECTOR_FADE_TIME);
                            selector = new Selector((ProcessNode) po);
                            editor.getAnimator().addProcessObject(selector, ProcessUtils.SELECTOR_FADE_TIME);
                            return;
                        }
                        return;
                    }
                    selector = new Selector((ProcessNode) po);
                    editor.getAnimator().addProcessObject(selector, ProcessUtils.SELECTOR_FADE_TIME);
                    return;
                }
            }
        }
        // Remove all Selectors
        if (selector != null) {
            editor.getAnimator().removeProcessObject(selector, ProcessUtils.SELECTOR_FADE_TIME);
            selector = null;
        }

    }

    protected void updateCursor(Point position) {
        // Set default cursor
        editor.setCursor(Cursor.getDefaultCursor());

        // Check if selection and editable
        if (editor.isEditable() && (editor.getSelectionHandler().getLastSelection() != null)
                && editor.getSelectionHandler().getLastSelection() instanceof ProcessNode) {
            ProcessNode selectedNode = (ProcessNode) editor.getSelectionHandler().getLastSelection();
            // Show resize cursors at the borders
            int mouseX = position.x;
            int mouseY = position.y;
            int x1 = selectedNode.getPos().x - (selectedNode.getSize().width / 2);
            int y1 = selectedNode.getPos().y - (selectedNode.getSize().height / 2);
            int x2 = selectedNode.getPos().x + (selectedNode.getSize().width / 2);
            int y2 = selectedNode.getPos().y + (selectedNode.getSize().height / 2);

            // Top border
            if (mouseX > (x1 + 2) && mouseX < (x2 - 2)
                    && mouseY > (y1) && mouseY < (y1 + 3)) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            }
            // Bottom border
            if (mouseX > (x1 + 2) && mouseX < (x2 - 2)
                    && mouseY > (y2 - 3) && mouseY < (y2)) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
            }
            // Left border
            if (mouseX > (x1) && mouseX < (x1 + 3)
                    && mouseY > (y1 + 2) && mouseY < (y2)) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
            // Right border
            if (mouseX > (x2 - 3) && mouseX < (x2)
                    && mouseY > (y1 + 2) && mouseY < (y2 - 2)) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            }
        }
        //Check if hovering over edge
        ProcessObject closeObject = editor.detectProcessEdge(position);
        if (editor.isEditable() && (closeObject instanceof ProcessEdge) && editor.getDragableObject() instanceof ProcessEdgeDragHelper) {
            ProcessEdgeDragHelper helper = (ProcessEdgeDragHelper) editor.getDragableObject();
            //is horizontal
            if (helper.isHorizontal()) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                //is vertical
            } else if (helper.isVertical()) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
        }
    }

    public Point getMouseClickLocation() {
        return mouseClickLocation;
    }

    public void setMouseClickLocation(Point mouseClickLocation) {
        this.mouseClickLocation = mouseClickLocation;
    }

    private void updateMouseClickPoint(MouseEvent evt) {
        // Scale mouse-click location
        setMouseClickLocation(editor.getMouseClickLocation(evt.getPoint()));
    }

    private void startRubberBandSelection() {
        rubberBand = new Rectangle(getMouseClickLocation(), new Dimension(0, 0));
        rubberBandOrigin = rubberBand.getLocation();
    }

    private int updateRubberBandSelection() {
        int elementsContained = 0;

        if (getRubberBand() == null) {
            return 0;
        }

        // Clear selection
        editor.getSelectionHandler().clearSelection();

        // Get current location and size
        //Point oldStart = getRubberBand().getLocation();
        //Dimension oldSize = getRubberBand().getSize();

        Point newStart = new Point(0, 0);
        Dimension newSize = new Dimension(0, 0);

        // Check mouse movements
        if (mouseClickLocation.x < rubberBandOrigin.x) {
            newStart.x = mouseClickLocation.x;
            newSize.width = rubberBandOrigin.x - mouseClickLocation.x;
        } else {
            newStart.x = rubberBandOrigin.x;
            newSize.width = mouseClickLocation.x - rubberBandOrigin.x;
        }
        if (mouseClickLocation.y < rubberBandOrigin.y) {
            newStart.y = mouseClickLocation.y;
            newSize.height = rubberBandOrigin.y - mouseClickLocation.y;
        } else {
            newStart.y = rubberBandOrigin.y;
            newSize.height = mouseClickLocation.y - rubberBandOrigin.y;
        }

        // Update size
        rubberBand = new Rectangle(newStart, newSize);

        // Iterate over all visible ProcessNodes and check if inside rubber band
        for (ProcessNode n : new LinkedList<ProcessNode>(editor.getModel().getVisibleNodes())) {
            if (getRubberBand().contains(n.getPos())) {
                // Add node to selection (since inside rubber band)
                editor.getSelectionHandler().addSelectedObject(n);
                // Increase count
                elementsContained++;
            }
        }
        // Iterate over all ProcessEdges and check if inside rubber band
        for (ProcessEdge e : editor.getModel().getEdges()) {
            if (e.getRoutingPoints().size() > 0) {
                if (getRubberBand().contains(e.getRoutingPoints().get(0))
                        & getRubberBand().contains(e.getRoutingPoints().get(e.getRoutingPoints().size() - 1))) {
                    // Add node to selection (since inside rubber band)
                    editor.getSelectionHandler().addSelectedObject(e);
                    // Increase count
                    elementsContained++;
                }
            }
        }


        return elementsContained;
    }

    public Rectangle getRubberBand() {
        return rubberBand;
    }

    public void setRubberBand(Rectangle rubberBand) {
        this.rubberBand = rubberBand;
    }

    private void updateHorizontalRulers() {
        updateRulers(true);
    }

    private void updateVerticalRulers() {
        updateRulers(false);
    }

    private void updateRulers(boolean horizontal) {
        // Attach Ruler to DragSource
        Ruler r = rulerMap.get(dragSource);
        if (r == null) {
            // Create new Ruler
            r = new Ruler(dragSource);
            if (horizontal) {
                r.setShowVertical(false);
                r.setShowHorizontal(true);
            } else {
                r.setShowHorizontal(false);
                r.setShowVertical(true);
            }
            rulerMap.put(dragSource, r);
            editor.getAnimator().addProcessObject(r, ProcessUtils.RULER_FADE_TIME);
        }
        // Iterate over all ProcessNodes of the model an check if the size is the same
        for (ProcessNode n : editor.getModel().getNodes()) {
            if (n != dragSource) {

                if (horizontal ? n.getSize().width == dragSource.getSize().width
                        : n.getSize().height == dragSource.getSize().height) {
                    // Add to RulerMap
                    Ruler rx = rulerMap.get(n);
                    if (rx == null) {
                        rx = new Ruler(n);
                        if (horizontal) {
                            rx.setShowVertical(false);
                        } else {
                            rx.setShowHorizontal(false);
                        }
                        rulerMap.put(n, rx);
                        editor.getAnimator().addProcessObject(rx, ProcessUtils.RULER_FADE_TIME);
                    }
                } else {
                    // Remove from RulerMap
                    if (rulerMap.containsKey(n)) {
                        editor.getAnimator().removeProcessObject(rulerMap.get(n), ProcessUtils.RULER_FADE_TIME);
                        rulerMap.remove(n);
                    }
                }
            }
        }
    }

    private void clearRulers() {
        for (ProcessNode n : rulerMap.keySet()) {
            editor.getAnimator().removeProcessObject(rulerMap.get(n), ProcessUtils.RULER_FADE_TIME);
        }
        rulerMap.clear();
    }

    private void updateAlignmentRulers() {
        // Calculate average horizontal/vertical position
        int vPos = 0;
        int hPos = 0;
        int count = 0;
        for (ProcessObject o : editor.getSelectionHandler().getSelection()) {
            if (o instanceof ProcessNode) {
                ProcessNode n = (ProcessNode) o;
                hPos += n.getPos().x;
                vPos += n.getPos().y;
                count++;
            }
        }

        boolean showV = false, showH = false;

        if (count > 0) {
            vPos /= count;
            hPos /= count;

            //final int RASTER = 5;

            for (ProcessNode n : editor.getModel().getNodes()) {
                if (editor.getSelectionHandler().contains(n)) {
                    continue;
                }
                if (n.getPos().y == vPos) {
                    showV = true;
                }
                if (n.getPos().x == hPos) {
                    showH = true;
                }
            }

        }

        editor.setAlignmentRuler(showH ? hPos : Integer.MAX_VALUE, showV ? vPos : Integer.MAX_VALUE);

    }

    /**
     * Adds the next node ProcessHelper.
     * @param po
     */
    public void showNextNodeHelper(ProcessObject po) {
        // Find all next node helpers
        List<NodeOnSelectMenuBasis> remList = new LinkedList<NodeOnSelectMenuBasis>();
        for (ProcessHelper ph : this.editor.getProcessHelpers()) {
            if (ph instanceof NodeOnSelectMenuBasis) {
                remList.add((NodeOnSelectMenuBasis) ph);
            }
        }
        
        if (po instanceof ProcessNode) {
            //checking if this helper is already contained to avoid double adding
            boolean contained = false;
            for (NodeOnSelectMenuBasis rem : remList) {
                if ((po.equals(rem.getNode()))) {
                    contained = true;                        
                } else {
                	rem.destroy();
                }
            }
            
            //adding a NextNodeHelper
            if (!contained) {
                NodeOnSelectMenuBasis menu = editor.getOnSelectMenu((ProcessNode) po);
                menu.setNode((ProcessNode) po);
                this.editor.getAnimator().addProcessObject(menu, ProcessUtils.RULER_FADE_TIME);
            }
        } else {
	        // Remove all other helpers
	        for (NodeOnSelectMenuBasis rem : remList) {
	            rem.destroy();
	        }
        }
    }

    private void updateClusters() {
        if (clusterUnderMouse != null) {
            clusterUnderMouse.setHighlighted(false);
            clusterUnderMouse = null;
        }
        // Check if any Cluster is under current mouse pos (top to bottom!)
        List<ProcessNode> nodeList = new LinkedList<ProcessNode>(editor.getModel().getNodes());
        for (int pos = nodeList.size() - 1; pos >= 0; pos--) {
            if (nodeList.get(pos) instanceof Cluster) {
                Cluster c = (Cluster) nodeList.get(pos);
                if (c.isCollapsed()) {
                    continue;
                }
                if (c.isContainedGraphically(nodeList, dragSource, false)) {
                    clusterUnderMouse = c;
                    clusterUnderMouse.setHighlighted(true);
                    break;
                }
            }
        }
    }

    /**
     * Highlights the source node if an AttachedNode is moved.
     */
    private void updateAttachedNodes(int xDiff, int yDiff) {
        if (attachementSource != null) {
            attachementSource.setHighlighted(false);
            attachementSource = null;
        }
        if (editor.getModel().getAttachedNodeHandler() == null) {
            return;
        }
        if (editor.getSelectionHandler().getSelectionSize() == 1) {
            if (!(editor.getSelectionHandler().getLastSelection() instanceof AttachedNode)) {
                // 1. detect nodes attached to all nodes of the selection
                List<AttachedNode> attachedNodes = getAttachedNodes();
                // 2. Move attachedNodes
                editor.getModel().getAttachedNodeHandler().sourceMoved(dragSource, attachedNodes, xDiff, yDiff);
                return;
            }
        }
        if (!(editor.getSelectionHandler().getLastSelectedNode() instanceof AttachedNode)) {
            return;
        }
        AttachedNode nodeToAttach = (AttachedNode) editor.getSelectionHandler().getLastSelection();
        AttachedNodeHandler attachedNodeHandler = editor.getModel().getAttachedNodeHandler();
        // Check all nodes if an attachement is possible (from top to bottom)
        for (int i = editor.getModel().getNodes().size() - 1; i >= 0; i--) {
            ProcessNode sourceNode = editor.getModel().getNodes().get(i);
            if (attachedNodeHandler.isAttachable(sourceNode, nodeToAttach)) {
                sourceNode.setHighlighted(true);
                attachementSource = sourceNode;
                break;
            }
        }
    }

    private List<AttachedNode> getAttachedNodes() {
        LinkedList<AttachedNode> attachedNodes = new LinkedList<AttachedNode>();
        for (ProcessNode node : editor.getModel().getNodes()) {
            if (node instanceof AttachedNode) {
                AttachedNode aNode = (AttachedNode) node;
                for (ProcessObject selNode : editor.getSelectionHandler().getSelection()) {
                    if (!aNode.getParentNodeId().equals("")) {
                        if (aNode.getParentNodeId().equals(selNode.getProperty(ProcessObject.PROP_ID))) {
                            attachedNodes.add(aNode);
                        }
                    }
                }
            }
        }
        return attachedNodes;
    }

    /**
     * Sets the drag offset.
     * @param point
     */
    void setDragOffset(Point point) {
        dragOffset = point;
    }
}
