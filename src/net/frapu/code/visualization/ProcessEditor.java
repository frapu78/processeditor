/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 *
 * http://frapu.net
 */
package net.frapu.code.visualization;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import net.frapu.code.visualization.editors.PropertyEditor;
import net.frapu.code.visualization.helper.DefaultNodeOnSelectMenu;
import net.frapu.code.visualization.helper.NodeOnSelectMenuBasis;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.File;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.filechooser.FileFilter;

import com.inubit.research.animation.AnimationFacade;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.server.merger.animator.ProcessMergeAnimator;
import java.awt.Image;

import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.layouter.LayoutMenuitemActionListener;
import net.frapu.code.converter.ConverterHelper;
import net.frapu.code.converter.Exporter;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.Task;

/**
 * This class provides a graphical process editor.
 * 
 * @author fpu
 */
public class ProcessEditor extends JPanel
        implements
        ProcessModelListener,
        ProcessEditorInterface,
        Printable {

    /**
     * Amount of pixels that the ProcessEditor will get extended by
     * when asked for its preferred size
     */
    private static final int PREFERRED_SIZE_MARGIN = 10;
    /** Serialization id */
    private static final long serialVersionUID = 7108753016924859656L;
    /** The data model */
    private ProcessModel model;
    /** Set of listeners */
    private Set<ProcessEditorListener> listeners = new HashSet<ProcessEditorListener>();
    private boolean workingOnListeners = false;
    private List<ProcessEditorListener> removeLaterListener = new ArrayList<ProcessEditorListener>();
    private List<ProcessEditorListener> addLaterListener = new ArrayList<ProcessEditorListener>();
    /** States if the process can be edited */
    private boolean editable = true;
    /** States if the data objects and associations are visible */
    // private boolean dataObjectsVisible = true;
    /** Notes if anything has been added (in this case, the example is removed */
    private boolean virginEditor = true;
    /** The pop-up menu */
    protected JPopupMenu popupMenu;
    /** The context pop-up menu */
    protected JPopupMenu contextMenu;
    /** Additional (external) pop-up menu entries */
    protected LinkedList<JMenuItem> customMenuItems = new LinkedList<JMenuItem>();
    /** Additional (external) context pop-up menu entries */
    protected Map<Class<?>, LinkedList<JMenuItem>> customContextItems =
            new HashMap<Class<?>, LinkedList<JMenuItem>>();
    /** The selection handler */
    protected SelectionHandler selectionHandler = null;
    /** The last selected node */
    //protected ProcessNode selectedNode = null;
    /** The last selected edge */
    protected ProcessEdge selectedEdge = null;
    /** A runner thread */
    protected AnimationFacade animator = new AnimationFacade(this);
    /** Animation enabled */
    protected boolean animationEnabled = false;
    /** layout edges when moving nodes or creating edges */
    private boolean layoutEdges = true;
    /** The scale of the painted diagram */
    private double scale = 1.0;
    private AffineTransform scaleTransform = new AffineTransform();
    /** The minimum diagram scalle */
    public static final double MIN_DIAGRAM_SCALE = 0.5;
    /** offers advanced, synchronized animations transforming the whole model*/
    protected ProcessMergeAnimator mergeAnimator = null;
    /** The last selected Dragable */
    protected Dragable dragableObject = null;
    /** The mouse listener */
    protected ProcessEditorMouseListener mouseListener = null;
    /** The vertical alignment ruler */
    protected int verticalAlignmentRuler = Integer.MAX_VALUE;
    /** The horizontal alignment ruler */
    protected int horizontalAlignmentRuler = Integer.MAX_VALUE;
    /** The alpha value for invisible objects */
    protected float invisibleAlpha = 0.1f;
    /** A flag that denotes if the background should be drawn by the editor */
    protected boolean drawBackground = true;
    /** A list of ProcessHelper, transient ProcessObjects */
    protected HashSet<ProcessHelper> processHelpers = new HashSet<ProcessHelper>();
    /** The fade-in time for new Elements */
    public final static int NEW_FADE_TIME = 250;
    /** The fade-out time for deletion of Elements */
    public final static int DELETE_FADE_TIME = 500;
    /** An external action handler */
    protected ProcessEditorExternalizeableActionHandler extHandler = null;
    /** A flag if topological rendering should be used (default = false) */
    protected boolean topologicalRendering = false;
    
    private NodeOnSelectMenuBasis f_onSelectMenu = new DefaultNodeOnSelectMenu(this);

    /** 
     * Creates a new ProcessEditor with a sample model.
     */
    public ProcessEditor() {
        super();
        // Perfom custom initialization
        customInitialization();
        // Insert example
        setModel(createExample());
    }

    /**
     * Creates a new ProcessEditor with a given model.
     * @param model
     */
    public ProcessEditor(ProcessModel model) {
        super();
        // Perfom custom initialization
        customInitialization();
        // Insert model
        setModel(model);
    }

    /**
     * Initializes a custom context menu
     */
    protected void customInitialization() {
        // Add mouse listener
        mouseListener = new ProcessEditorMouseListener(this);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.setTransferHandler(ProcessEditorFileDropHandler.getInstance());

        // Do initialization here...     
        initializePopup();
        this.setBackground(Color.white);
    }

    public boolean isTopologicalRendering() {
        return topologicalRendering;
    }

    /**
     * Sets topological rendering. If enabled (default), the nodes are
     * drawn by their topological ordering. The content nodes of
     * collapsed clusters are not drawn.
     * @param topologicalRendering
     */
    public void setTopologicalRendering(boolean topologicalRendering) {
        this.topologicalRendering = topologicalRendering;
    }

    /**
     * Adds a custome pop-up menu item.
     * @param item
     */
    public void addCustomPopUpMenuItem(JMenuItem item) {
        customMenuItems.add(item);
        initializePopup();
    }

    /**
     * Adds a customer context-menu for a specific node type.
     * @param nodeType
     * @param item
     */
    public void addCustomContextMenuItem(Class<?> nodeType, JMenuItem item) {
        LinkedList<JMenuItem> cl = customContextItems.get(nodeType);
        if (cl == null) {
            // Create new list
            cl = new LinkedList<JMenuItem>();
            customContextItems.put(nodeType, cl);
        }
        cl.add(item);
    }

    public Map<Class<?>, LinkedList<JMenuItem>> getCustomContextMenuItems() {
        return customContextItems;
    }

    /**
     * returns all custom menu items for the given class or its subclasses!
     * @param class1
     * @return
     */
    public LinkedList<JMenuItem> getCustomContextMenuItems(
            Class<? extends ProcessObject> class1) {
        LinkedList<JMenuItem> _result = new LinkedList<JMenuItem>();
        for (Class<?> cl : customContextItems.keySet()) {
            if (cl.isAssignableFrom(class1)) {
                _result.addAll(customContextItems.get(cl));
            }
        }
        return _result;
    }

    private void initializePopup() {
        try {

            final ProcessEditor outer = this;

            if (isEditable()) {
                initializeEditorPopup();
            } else {
                initializeViewPopup();
                // Insert generic items
            }
            popupMenu.add(new JSeparator());
            JMenuItem menuItem = new JMenuItem("Open model...");
            menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/open_small.gif")));
            menuItem.addActionListener(new ProcessEditorOpenAction(this));
            popupMenu.add(menuItem);

            menuItem = new JMenuItem("Save model...");
            menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/save_small.gif")));
            menuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // Get file name
                    String filename = java.io.File.separator + "model";
                    javax.swing.JFileChooser fc = new javax.swing.JFileChooser(new java.io.File(filename));
                    fc.setDialogTitle("Save Model");

                    for (FileFilter ff : ConverterHelper.getExporterFileFilters(getModel().getClass())) {
                        fc.addChoosableFileFilter(ff);
                    }

                    if (fc.showSaveDialog(null) != javax.swing.JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    java.io.File selFile = fc.getSelectedFile();
                    // Check if file has been selected
                    if (selFile == null) {
                        return;
                    }
                    // Abort otherwise
                    try {
                        // Get selected file filter
                        FileFilter selectedFF = fc.getFileFilter();
                        // Iterate over until matching file filter is found
                        int pos = 0;
                        for (FileFilter ff : ConverterHelper.getExporterFileFilters(getModel().getClass())) {
                            if (selectedFF.getDescription().equals(ff.getDescription())) {
                                // Save file
                                Exporter exporter = ConverterHelper.getExportersFor(getModel().getClass()).get(pos);
                                exporter.serialize(selFile, getModel());
                            }
                            pos++;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            popupMenu.add(menuItem);

            menuItem = new JMenuItem("Print model...");
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Get a PrinterJob
                    PrinterJob job = PrinterJob.getPrinterJob();
                    // Specify the Printable is an instance of SimplePrint
                    job.setPrintable(outer);
                    repaint();
                    // Show print options first
                    ProcessEditorPrintConfigurationDialog printConf =
                            new ProcessEditorPrintConfigurationDialog(null, true);
                    SwingUtils.center(printConf);
                    printConf.setVisible(true);
                    if (printConf.getStatus() != ProcessEditorPrintConfigurationDialog.Status.OK) {
                        return;
                    }
                    // Put up the dialog box
                    if (job.printDialog()) {
                        // Print the job if the user didn't cancel printing
                        try {
                            job.print();
                        } catch (Exception ex) {
                            JOptionPane.showConfirmDialog(outer, ex.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    repaint();
                }
            });
            popupMenu.add(menuItem);


            menuItem = new JMenuItem("Properties...");
            menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/edit_small.gif")));
            menuItem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    final ProcessObject bgObj = new ProcessModelMetaNode(getModel());

                    // @todo Store in list for re-use!
                    final PropertiesPanel propPanel = new PropertiesPanel(bgObj, isEditable());

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
                                bgObj.setProperty(key, propEditor.getValue());
                                propDialog.dispose();
                            }
                        }
                    });
                    c.weighty = 0;
                    c.gridy = 1;
                    propDialog.add(okButton, c);

                    propDialog.pack();
                    SwingUtils.center(propDialog);
                    propDialog.setModal(true);
                    propDialog.setVisible(true);
                    // Release listener!
                    propPanel.dispose();
                }
            });
            popupMenu.add(menuItem);

            // Check if plug-in menu items are available
            if (customMenuItems.size() > 0) {
                popupMenu.add(new JSeparator());
                JMenu plugins = new JMenu("Plug-ins");
                popupMenu.add(plugins);
                for (JMenuItem i : customMenuItems) {
                    plugins.add(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeViewPopup() {
        //Create the popupMenu menu.
        popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Enable editor");
        menuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setEditable(true);
            }
        });
        // Only add Enable editor menü if isEnabled();
        if (!isEnabled()) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        // Initialize context menu
        contextMenu = new JPopupMenu();
    }

    @SuppressWarnings("unchecked")
    private void initializeEditorPopup() {
        //Create the popupMenu menu.
        popupMenu = new JPopupMenu();

        if (getModel() != null) {
            // Add menu
            JMenu editMenu = new JMenu("Add");
            // Add all supported classes
            for (final Class nodeClass : getModel().getSupportedNodeClasses()) {
                // Check if variants exist
                ProcessNode newNode = null;
                try {
                    newNode = (ProcessNode) nodeClass.newInstance();
                } catch (InstantiationException inex) {
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (newNode.getVariants().size() == 0) {
                    // Add default entry
                    JMenuItem classEntry = new JMenuItem(nodeClass.getSimpleName());
                    classEntry.setIcon(new ImageIcon(ProcessUtils.createPreviewImage(nodeClass, 16)));
                    editMenu.add(classEntry);
                    classEntry.addActionListener(new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            // Create new element at last mouse location
                            ProcessNode n;
                            try {
                                n = (ProcessNode) nodeClass.newInstance();
                                Point mouseClickLocation = mouseListener.getMouseClickLocation();
                                n.setPos(mouseClickLocation.x, mouseClickLocation.y);
                                //getModel().addNode(n);
                                getAnimator().addProcessObject(n, NEW_FADE_TIME);
                                repaint();
                            } catch (Exception ex) {
                            }
                        }
                    });
                } else {
                    // Add submenu
                    JMenu variantMenu = new JMenu(nodeClass.getSimpleName());
                    editMenu.add(variantMenu);
                    int counter = 0;
                    // Iterate over all variants
                    for (final Class nodeVariant : newNode.getVariants()) {
                        JMenuItem classEntry = new JMenuItem(nodeVariant.getSimpleName());
                        Image preview = ProcessUtils.createPreviewImage(nodeVariant, 16);
                        if (preview != null) {
                            classEntry.setIcon(new ImageIcon(preview));
                            if (counter == 0) {
                                variantMenu.setIcon(new ImageIcon(preview));
                            }
                        }
                        variantMenu.add(classEntry);
                        classEntry.addActionListener(new java.awt.event.ActionListener() {

                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                // Create new element at last mouse location
                                ProcessNode n;
                                try {
                                    Point mouseClickLocation = mouseListener.getMouseClickLocation();
                                    n = (ProcessNode) nodeVariant.newInstance();
                                    n.setPos(mouseClickLocation.x, mouseClickLocation.y);
                                    //getModel().addNode(n);
                                    getAnimator().addProcessObject(n, NEW_FADE_TIME);
                                    repaint();
                                } catch (Exception ex) {
                                }
                            }
                        });
                        counter++;
                    }
                }
            }
            popupMenu.add(editMenu);

            popupMenu.add(new JSeparator());

            final ProcessEditor outer = this;

            // Paste menu
            JMenuItem pasteMenuItem = new JMenuItem("Paste");
            pasteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/paste_small.gif")));
            pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
                    clipboard.paste(outer, getSelectionHandler());
                    repaint();
                    // Inform listeners
                    for (ProcessEditorListener l : getListeners()) {
                        l.processObjectClicked(getSelectionHandler().getLastSelectedNode());
                    }
                }
            });
            popupMenu.add(pasteMenuItem);

            // Clear menu
            JMenuItem clearMenuItem = new JMenuItem("Clear model");
            clearMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/new_small.gif")));
            clearMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // Double check
                    int result = JOptionPane.showConfirmDialog(null, "Are you really sure?\nThe current model will be erased!", "Clear model", JOptionPane.OK_CANCEL_OPTION);
                    if (result != JOptionPane.OK_OPTION) {
                        return;
                        // Create new process model of the current type
                    }
                    try {
                        ProcessModel newModel = model.getClass().newInstance();
                        newModel.setProcessName("New process model");
                        setModel(newModel);
                    } catch (Exception ex) {
                    }
                }
            });
            popupMenu.add(clearMenuItem);
        }

        popupMenu.add(new JSeparator());

        //layouting menu---------------------------------------------------------
        if (addLayoutMenu(popupMenu)) {
            popupMenu.add(new JSeparator());
        }

        //Disable editor menu----------------------------------------------------

        JMenuItem menuItem;
        menuItem = new JMenuItem("Disable editor");
        menuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setEditable(false);
            }
        });
        popupMenu.add(menuItem);

        // Initialize context menu
        contextMenu = new JPopupMenu();
    }

    public boolean addLayoutMenu(JPopupMenu pmenu) {
        JMenu layoutmenu = new JMenu("Layout");
        layoutmenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/icon_16x16_auto-ausrichten.gif")));
        int count = 0;
        if (getModel() != null) {
            ProcessUtils utils = getModel().getUtils();
            if (utils != null) {
                if (utils.getLayouters() != null) {
                    for (ProcessLayouter l : utils.getLayouters()) {
                        JMenuItem menuItem = new JMenuItem(l.getDisplayName());
                        menuItem.addActionListener(new LayoutMenuitemActionListener(this, l));
                        layoutmenu.add(menuItem);
                        count++;
                    }
                }
            }
            //Disable menuItem if no layouters were specified
            //menuItem.setEnabled(model instanceof BPMNModel);
            if (getModel() != null) {
                utils = getModel().getUtils();
                if (utils != null) {
                    if (utils.getLayouters() != null) {
                        if (utils.getLayouters().size() > 0) {
                            pmenu.add(layoutmenu);
                        }
                    }
                    // end layouting menu ----------------------------------------------------
                }
            }
        }
        return !(count == 0);
    }

    /**
     * Adds a transient ProcessHelper to the ProcessEditor.
     * @param helper
     */
    public void addProcessHelper(ProcessHelper helper) {
        if (!processHelpers.contains(helper)) {
            processHelpers.add(helper);
        }
    }

    /**
     * Removes a ProcessHelper from the ProcessEditor.
     * @param helper
     */
    public void removeProcessHelper(ProcessHelper helper) {
        processHelpers.remove(helper);
    }

    public ProcessEditorMouseListener getProcessEditorMouseListener() {
        return mouseListener;
    }

    public HashSet<ProcessHelper> getProcessHelpers() {
        return processHelpers;
    }

    /**
     * Removes all ProcessHelpers of a certain kind.
     * @param cl
     */
    public void removeProcessHelperClass(Class<? extends ProcessHelper> cl) {
        List<ProcessHelper> remList = new LinkedList<ProcessHelper>();
        for (ProcessHelper h : processHelpers) {
            if (h.getClass() == cl) {
                remList.add(h);
            }
        }
        for (ProcessHelper r : remList) {
            removeProcessHelper(r);
        }
    }

    public ProcessObject getLastSelectedNode() {
        return selectionHandler.getLastSelection();
    }

    @Override
    public Dimension getPreferredSize() {
        int maxX = 0;
        int maxY = 0;

        ProcessModel model = this.getModel();
        if (model != null) {
            Dimension d = model.getSize();
            maxX = d.width;
            maxY = d.height;
        }

        return new Dimension((int) ((maxX + PREFERRED_SIZE_MARGIN) * scale), (int) ((maxY + PREFERRED_SIZE_MARGIN) * scale));
    }

    @Override
    public synchronized void paintComponent(Graphics g) {
        if (isDrawBackground()) {
            super.paintComponent(g);
        }
        Graphics2D g2d = (Graphics2D) g;
        applyScaling(g2d);
        // Set anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background Gatter if visible
        if (isDrawBackground()) {
            g2d.setColor(getModel().getBackgroundColor());
            g2d.fillRect(0, 0, this.getSize().width, this.getSize().height);
            if (isEditable()) {
                ProcessUtils.drawGatter(g2d, 20, 20);
            }
        }

        if (!topologicalRendering) {
            // Draw all visible process nodes (fifo)
            for (ProcessNode f : new ArrayList<ProcessNode>(getModel().getNodes())) {
                float alpha = f.getAlpha();
                if (!f.isVisible()) {
                    alpha = invisibleAlpha;
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                f.paint(g2d);
            }

            // Draw all process edges (fifo)
            for (ProcessEdge e : new ArrayList<ProcessEdge>(getModel().getEdges())) {
                float alpha = e.getAlpha();//invisibleAlpha;
                if (e.getSource() == null || e.getTarget() == null) {
                    continue; //continue so the other edges still get painted!
                }
                if (!(e.getSource().isVisible() || e.getTarget().isVisible())) {
                    alpha = invisibleAlpha;
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                e.paint(g2d);               
            }
        } else {
            // Topological rendering
            LinkedList<ProcessNode> topNodes = getModel().getTopLevelNodes();
            // Set of already painted nodes
            Set<ProcessNode> paintedNodes = new HashSet<ProcessNode>();
            // HashMap with nodes with deferred edges
            Map<ProcessNode, List<ProcessEdge>> deferredEdgeMap =
                    new HashMap<ProcessNode, List<ProcessEdge>>();
            // Draw all visible process nodes (topological)
            for (ProcessNode f : new ArrayList<ProcessNode>(topNodes)) {
                float alpha = f.getAlpha();
                if (!f.isVisible()) {
                    alpha = invisibleAlpha;
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                f.paint(g2d);
                // Add node to set of painted nodes
                paintedNodes.add(f);
                // Draw incoming edges
                drawEdge(f, g2d, paintedNodes, deferredEdgeMap);
            }
            // Draw sub-clusters
            for (ProcessNode f : new ArrayList<ProcessNode>(topNodes)) {
                if (f instanceof Cluster) {
                    Cluster c = (Cluster) f;
                    if (!c.isCollapsed()) {
                        // Draw cluster
                        drawCluster(g2d, c, paintedNodes, deferredEdgeMap);
                    }
                }
            }
        }

        // Draw process helpers
        for (ProcessHelper h : new HashSet<ProcessHelper>(processHelpers)) {
            float alpha = h.getAlpha();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            h.paint(g2d);
        }

        // Draw alignment rulers
        if (horizontalAlignmentRuler != Integer.MAX_VALUE) {
            ProcessUtils.drawHorizontalAligmentRuler(g2d, horizontalAlignmentRuler);
        }
        if (verticalAlignmentRuler != Integer.MAX_VALUE) {
            ProcessUtils.drawVerticalAlignmentRuler(g2d, verticalAlignmentRuler);
        }

        // Draw rubber band
        if (mouseListener.getRubberBand() != null) {
            ProcessUtils.drawRubberBand(g2d, mouseListener.getRubberBand());
        }
    }

    private void drawCluster(Graphics2D g2d, Cluster c, Set<ProcessNode> done,
            Map<ProcessNode,List<ProcessEdge>> deferredEdgeMap) {
        List<ProcessNode> clusterNodes = c.getProcessNodes();
        for (ProcessNode f : new ArrayList<ProcessNode>(clusterNodes)) {
            if (done.contains(f)) {
                continue;
            }
            float alpha = f.getAlpha();
            if (!f.isVisible()) {
                alpha = invisibleAlpha;
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            f.paint(g2d);
            done.add(f);
            drawEdge(f, g2d, done, deferredEdgeMap);

        }
        // Draw sub-clusters
        for (ProcessNode f : new ArrayList<ProcessNode>(clusterNodes)) {
            if (f instanceof Cluster) {
                Cluster c1 = (Cluster) f;
                if (!c1.isCollapsed()) {
                    // Draw cluster recursivly
                    drawCluster(g2d, c1, done, deferredEdgeMap);
                }
            }
        }
    }

    private void drawEdge(ProcessNode f, Graphics2D g2d,
            Set<ProcessNode> paintedNodes,
            Map<ProcessNode,List<ProcessEdge>> deferredEdgeMap) {
        for (ProcessEdge e : getModel().getPreceedingEdges(f)) {
            // Check if source is visible
            if (!getModel().getVisibleNodes().contains(e.getTarget())) {
                continue;
            }
            // Check if preceeding node has already been drawn
            if (!paintedNodes.contains(e.getSource())) {
                // Defer, continue
                List<ProcessEdge> edgeList = deferredEdgeMap.get(e.getSource());
                if (edgeList==null) edgeList = new LinkedList<ProcessEdge>();
                edgeList.add(e);
                deferredEdgeMap.put(e.getSource(), edgeList);
                continue;
            }
            // Yes, move on
            float alpha = e.getAlpha(); //invisibleAlpha;
            if (e.getSource() == null || e.getTarget() == null) {
                continue; //continue so the other edges still get painted!
            }
            if (!(e.getSource().isVisible() || e.getTarget().isVisible())) {
                alpha = invisibleAlpha;
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            e.paint(g2d);            
        }
        // @todo: Process deferred nodes here!
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
        repaint();
    }

    public float getInvisibleAlpha() {
        return invisibleAlpha;
    }

    public void setInvisibleAlpha(float invisibleAlpha) {
        this.invisibleAlpha = invisibleAlpha;
    }

    public ProcessObject detectProcessObject(Point p) {
        ProcessObject _edge = detectProcessEdge(p);
        if(_edge != null)
        	return _edge;
        //check Helpers first, otherwise e.g. a Pool will always be found first
        //check for intersection with selectable and Dragable ProcessHelpers
        for(ProcessHelper h:getProcessHelpers()) {
        	if(h.isSelectable() && h instanceof Dragable) {
        		if(h.contains(p)) {
        			dragableObject = (Dragable) h;
        			return h;
        		}
        	}
        }
        // Check for intersection with the nodes
        for (int i = getModel().getVisibleNodes().size() - 1; i >= 0; i--) {
            ProcessNode f = getModel().getVisibleNodes().get(i);
            if (f.getOutlineShape() != null) {
                if (f.contains(p)) {
                    dragableObject = f;
                    return f;
                }
            }
        }
        
        return null;
    }

    /**
     * part of detectProcessObject, but only checks for edges (no nodes, no helpers)
     * @param p
     * @return
     */
	public ProcessObject detectProcessEdge(Point p) {
		// Check for intersection with the edges
        for (int i = getModel().getEdges().size() - 1; i >= 0; i--) {
            ProcessEdge e = getModel().getEdges().get(i);
            // Check for routing points
            List<Shape> shapes = e.getRoutingPointShapes();
            for (int i1 = 0; i1 < shapes.size(); i1++) {
                Shape s = shapes.get(i1);
                if (s.contains(p)) {
                    RoutingPointDragable rpd = new RoutingPointDragable(e, i1);
                    dragableObject = rpd;
                    return rpd;
                }
            }
            if (e.distanceToEdge(p) < 3) {  
                ProcessEdgeDragHelper helper = new ProcessEdgeDragHelper(e, p);
                setDragableObject(helper);
                return e;
            }            
        }
        return null;
	}

    public Dragable getDragableObject() {
        return dragableObject;
    }

    public void setDragableObject(Dragable dragableObject) {
        this.dragableObject = dragableObject;
        mouseListener.setDragOffset(new Point(0, 0));
    }

    /**
     * Returns a sample ProcessModel in BPMN (might be overwritten).
     * @return
     */
    public ProcessModel createExample() {

        BPMNModel sampleModel = new BPMNModel("Example Process");

        // Create Pool
        Pool pool1 = new Pool();
        pool1.setPos(180, 80);
        pool1.setSize(340, 140);
        Lane lane = new Lane("Lane", 150, pool1);
        sampleModel.addNode(lane);
        pool1.addLane(lane);
        sampleModel.addNode(pool1);

        // Create Start Event
        Event ev1 = new StartEvent(70, 80, "Start\\nEvent");
        sampleModel.addFlowObject(ev1);

        // Create first Task
        Activity ta1 = new Task(190, 80, "Task");
        sampleModel.addFlowObject(ta1);

        // Create End Event
        Event ev2 = new EndEvent(320, 80, "End\\nEvent");
        sampleModel.addFlowObject(ev2);

        // Sequence Flows
        SequenceFlow sf1 = new SequenceFlow(ev1, ta1);
        sampleModel.addEdge(sf1);

        SequenceFlow sf2 = new SequenceFlow(ta1, ev2);
        sampleModel.addEdge(sf2);

        // Add nodes to Pool
        pool1.addProcessNode(ev1);
        pool1.addProcessNode(ta1);
        pool1.addProcessNode(ev2);

        return sampleModel;
    }

    /**
     * Returns whether this editor is editable or not.
     * @return
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Defines if this editor is editable or not.
     * @param edit
     */
    public void setEditable(boolean edit) {
        this.editable = edit;
        initializePopup();
        this.repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setEditable(false);
        }
    }

    public SelectionHandler getSelectionHandler() {
        return selectionHandler;
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
        if (animationEnabled) {
            // Initialize background working thread
            animator.start();
        } else {
            animator.end();
        }
    }

    /**
     * Adds a listener to this ProcessEditor.
     * @param l
     */
    public void addListener(ProcessEditorListener l) {
        if (!workingOnListeners) {
            //all Fine
            listeners.add(l);
        } else {
            addLaterListener.add(l);
        }
    }

    /**
     * Returns the currently registered ProcessEditorListeners.
     * @return
     */
    public Set<ProcessEditorListener> getListeners() {
        return listeners;
    }

    private void applyScaling(Graphics2D g2d) {
        //it is not sure that this Graphics Object
        //is the same as last draw cycle (double buffering)
        //Thus the scaling always has to be applied!
        // if (scaleTransform.getScaleX()!=scale) {
        scaleTransform.setTransform(g2d.getTransform());
        scaleTransform.scale(scale, scale);
        g2d.setTransform(scaleTransform);
        // }
    }

    /** 
     * Removes a listener from this ProcessEditor.
     * @param l
     */
    public void removeListener(ProcessEditorListener l) {
        if (!workingOnListeners) {
            //all fine
            listeners.remove(l);
        } else {
            //all listeners are currently notified.
            //remember this listener and remove it later so no Concurrent Modification Exception occurs
            //@see endProcessEditorListenerUpdate()
            removeLaterListener.add(l);
        }
    }

    /**
     * applies scaling and transformations to the 
     * Point so it represents the "real" click location
     * in the editor
     * @param p
     * @return
     */
    public Point getMouseClickLocation(Point p) {
        return new Point(
                (int) (p.x * (1 / this.getScale())),
                (int) (p.y * (1 / this.getScale())));
    }

    public ProcessModel getModel() {
        return model;
    }

    public void setModel(ProcessModel model) {
    	if (model == null) {
            return;
        }    	 	
        //remove edge layouter
        boolean layouterEnabled = isLayoutEdges();
        setLayoutEdges(false);
        // Remove ProessModelListener for old model (if existing)
        if (this.model != null) {
            this.model.removeListener(this);
        }
        this.model = model;
        // Create new SelectionHandler
        selectionHandler = new SelectionHandler(this);
        setVirginEditor(true);
        this.model.addListener(this);
        //reset old layout state
        setLayoutEdges(layouterEnabled);
        initializePopup();
        //clearing all ProcessHelpers
        processHelpers.clear();   
        //rebuilding edge labels
        for(ProcessEdge edge:getModel().getEdges()) {
        	addProcessHelper(edge.getLabelHelper());
        }
       
        this.repaint();
        // Inform listeners
        for (ProcessEditorListener e : getListeners()) {
            e.modelChanged(model);
        }
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public AnimationFacade getAnimator() {
        return animator;
    }

    public ProcessMergeAnimator getMergeAnimator() {
        if (mergeAnimator == null) {
            mergeAnimator = new ProcessMergeAnimator(this);
        }
        return mergeAnimator;
    }

    public JPopupMenu getContextMenu() {
        return contextMenu;
    }

    public boolean isVirginEditor() {
        return virginEditor;
    }

    public void setVirginEditor(boolean virginEditor) {
        this.virginEditor = virginEditor;
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }

    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }

    /**
     * Sets the position of the alignment rulers. Set to Integer.MAX_VALUE to
     * dismiss.
     * @param x
     * @param y
     */
    public void setAlignmentRuler(int x, int y) {
        verticalAlignmentRuler = x;
        horizontalAlignmentRuler = y;
    }

    /**
     * needed to avoid concurrent modifications exceptions when someone wants to remove
     * a listener while being notified
     */
    public void endProcessEditorListenerUpdate() {
        workingOnListeners = false;
        for (ProcessEditorListener l : removeLaterListener) {
            this.removeListener(l);
        }
        removeLaterListener.clear();
        for (ProcessEditorListener l : addLaterListener) {
            this.addListener(l);
        }
        addLaterListener.clear();
    }

    /**
     * needed to avoid concurrent modifications exceptions when someone wants to remove
     * a listener when being notified
     */
    public void startProcessEditorListenerUpdate() {
        workingOnListeners = true;
    }

    /**
     * sets the scale in the ProcessEditor so that the whole model is visible
     */
    public void zoomToFit() {
        zoomToFit(this.model);
    }

    /**
     * sets the scale in the ProcessEditor so that the whole model is visible.
     */
    public void zoomToFit(ProcessModel model) {
        Dimension d = model.getSize();
        d.height += PREFERRED_SIZE_MARGIN;
        d.width += PREFERRED_SIZE_MARGIN;
        Dimension d2 = this.getSize();
        double hratio = d2.getHeight() / d.getHeight();
        double wratio = d2.getWidth() / d.getWidth();
        double ratio = Math.min(hratio, wratio);
        if (ratio < 1.0) {
            this.setScale(ratio);
        } else {
            this.setScale(1.0);
        }
    }

    /**
     * Stops all Threads belonging to this Editor. Should be called when the editor
     * is dismissed.
     */
    public void dispose() {
        setAnimationEnabled(false);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "
                + SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Process Editor Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane jScrollPane = new JScrollPane(new ProcessEditor());
        jScrollPane.setWheelScrollingEnabled(true);
        f.add(jScrollPane);
        f.pack();
        f.setVisible(true);
    }

    /**
     * 
     * ProcessModelListener for updating the UI corresponding to model changes.
     * 
     */
    @Override
    public void processNodeAdded(ProcessNode newNode) {
        setVirginEditor(false);
        this.repaint();
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
        setVirginEditor(false);
        this.repaint();
    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
        setVirginEditor(false);
        this.addProcessHelper(edge.getLabelHelper());
        this.repaint();
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
        setVirginEditor(false);
        this.removeProcessHelper(edge.getLabelHelper());
        this.repaint();
    }

    @Override
    public void processObjectPropertyChange(ProcessObject obj, String name, String oldValue, String newValue) {
        // ignore
    }

    @Override
    public void processModelOpened(ProcessModel model) {
        setModel(model);
        setSize(getPreferredSize());
        repaint();
    }

    @Override
    public ProcessModel getSelectedModel() {
        return getModel();
    }

    @Override
    public void processModelSaved(ProcessModel model, File f) {
        // Do nothing here...
    }

    /**
     * 
     * Printable Interface implementation follows.
     *
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g2 = (Graphics2D) graphics;
        // We fit everything on one page
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        }

        // Create copy of this Editor @todo: Figure out why it won't work
        ProcessEditor editor = this;

        // Figure out pageFormat
        Rectangle2D rect = new Rectangle2D.Double(
                pageFormat.getImageableX(),
                pageFormat.getImageableY(),
                pageFormat.getImageableWidth(),
                pageFormat.getImageableHeight());

        // Reduce 1inch at top
        rect.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

        // Check if width or height is larger than rect's width or size
        Dimension currentSize = editor.getPreferredSize();
        double xScale = 1.0;
        if (currentSize.width > rect.getWidth()) {
            xScale = rect.getWidth() / currentSize.width;
        }
        double yScale = 1.0;
        if (currentSize.height > rect.getHeight()) {
            yScale = rect.getHeight() / currentSize.height;
        }
        if (xScale < yScale) {
            yScale = xScale;
        }
        if (yScale < xScale) {
            xScale = yScale;
        }
        // Scale
        AffineTransform oldTrans = g2.getTransform();
        AffineTransform trans = new AffineTransform();
        trans.setTransform(oldTrans);
        System.out.println("Setting scale to " + xScale + ":" + yScale);
        trans.scale(xScale, yScale);
        // Center at page origin
        double tx = rect.getWidth() / 2 - (currentSize.width * xScale) / 2;
        double ty = rect.getHeight() / 2 - (currentSize.height * yScale) / 2;
        System.out.println("Trans: " + tx + "," + ty);
        trans.translate(tx, ty);
        // Paint graphics
        g2.setTransform(trans);
        editor.setEnabled(true);
        editor.paint(g2);

        // Set default transform
        g2.setTransform(oldTrans);

        g2.setPaint(Color.WHITE);
        g2.setStroke(ProcessUtils.boldStroke);
        g2.setFont(ProcessUtils.defaultFont);
//        g2.fillRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)g2.getFont().getSize()*2);
        g2.setPaint(Color.BLACK);
//        g2.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)g2.getFont().getSize()*2);
        ProcessUtils.drawText(g2, (int) (rect.getWidth() / 2), (int) (g2.getFont().getSize()), (int) rect.getWidth(),
                getModel().getProcessName(), ProcessUtils.Orientation.CENTER);


        return Printable.PAGE_EXISTS;
    }

    /**
     * returns an instance on an NodeOnSelectMenu.
     * Can be overwritten by subclasses of ProcessEditor to
     * display other menus than the default one.
     * @param processNode 
     *
     * @param po
     * @param editor
     * @return
     */
    public NodeOnSelectMenuBasis getOnSelectMenu(ProcessNode processNode) {
       return f_onSelectMenu;
    }
    
    public void setNodeOnSelectMenu(NodeOnSelectMenuBasis menu) {
    	f_onSelectMenu = menu;
    }

    public ProcessEditorExternalizeableActionHandler getExtHandler() {
        return extHandler;
    }

    public void setExtHandler(ProcessEditorExternalizeableActionHandler extHandler) {
        this.extHandler = extHandler;
    }

    /**
     * Opens a new model. Default implementation is a new ProcessEditor window.
     * @param model
     */
    public ProcessEditor openNewModel(ProcessModel model) {
        if (getExtHandler() != null) {
            return getExtHandler().openNewModel(model);
        }
		// Default action
		ProcessEditor editor = new ProcessEditor();
		editor.setModel(model);
		JFrame f = new JFrame("ProcessEditor - " + model.getProcessName());
		JScrollPane jScrollPane = new JScrollPane(editor);
		jScrollPane.setWheelScrollingEnabled(true);
		f.add(jScrollPane);
		f.pack();
		SwingUtils.center(f);
		f.setVisible(true);
		return editor;
    }

    public boolean isLayoutEdges() {
        return layoutEdges;
    }



    public void setLayoutEdges(boolean layoutEdges) {
        this.layoutEdges = layoutEdges;
        if (layoutEdges) {
            if (getModel()!=null) {
                if (getModel().getUtils()!=null) {
                    if (getModel().getUtils().getRoutingPointLayouter() instanceof ProcessModelListener) {
                        getModel().addListener((ProcessModelListener) getModel().getUtils().getRoutingPointLayouter());
                    }
                }
            }
        } else {
            if (getModel()!=null) {
                if (getModel().getUtils()!=null) {
                    if (getModel().getUtils().getRoutingPointLayouter() instanceof ProcessModelListener) {
                        getModel().removeListener((ProcessModelListener) getModel().getUtils().getRoutingPointLayouter());
                    }
                }
            }
        }
    }

    public void pauseLayoutEdges() {
        boolean layout = isLayoutEdges();
        setLayoutEdges(false);
        this.layoutEdges = layout;
    }

    public void continueLayoutEdges() {
        if (isLayoutEdges())
            setLayoutEdges(true);

    }

}
