/**
 *
 * Process Editor - inubit Workbench Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 * 
 */
package com.inubit.research.gui;

import com.inubit.research.client.InvalidUserCredentialsException;
import com.inubit.research.client.UserCredentials;
import com.inubit.research.client.XMLHttpRequestException;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.frapu.code.converter.Exporter;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.ExtendedProcessEditorListener;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorClipboard;
import net.frapu.code.visualization.ProcessEditorInterface;
import net.frapu.code.visualization.ProcessEditorOpenAction;
import net.frapu.code.visualization.ProcessEditorSaveAction;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.SwingUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.layouter.LayoutMenuitemActionListener;
import net.frapu.code.visualization.tracking.ProcessEditorActionTracker;

import com.inubit.research.gui.plugins.PluginHelper;
import com.inubit.research.gui.plugins.WorkbenchPlugin;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.server.ProcessEditorServer;
import com.inubit.research.server.ProcessEditorServerHelper;
import com.inubit.research.server.merger.ClientFascade;
import com.inubit.research.server.merger.gui.VersionExplorer;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import net.frapu.code.visualization.ProcessEditorExternalizeableActionHandler;

/**
 *
 * @author fpu
 */
public class Workbench extends javax.swing.JFrame implements ProcessEditorInterface, ExtendedProcessEditorListener, ProcessEditorExternalizeableActionHandler {

    private static final long serialVersionUID = 3488875420298876738L;
    public static String TITLE = "Workbench";
    public static String VERSION = "0.2014.06.10-frapu-fork";
    //global custom menu items, which will be added to all ProcessEditor instances
    protected LinkedList<WorkbenchEditorListener> editorListeners = new LinkedList<>();
    // The ProcessEditorTrackers
    Map<ProcessEditor, ProcessEditorActionTracker> actionTrackers =
            new HashMap<>();
    private ProcessEditor currentEditor = null;
    public final static String CONF_ANIMATION_ENABLED = "animation_enabled";
    public final static String CONF_SHOW_TOOLBAR = "show_toolbar";
    private Configuration conf = Configuration.getInstance();
    private static SplashScreen splashScreen = new SplashScreen(null, false);
    private int pluginCount = 0;
    static int[] FUNC_KEYS = {
                java.awt.event.KeyEvent.VK_F1,
                java.awt.event.KeyEvent.VK_F2,
                java.awt.event.KeyEvent.VK_F3,
                java.awt.event.KeyEvent.VK_F4,
                java.awt.event.KeyEvent.VK_F5,
                java.awt.event.KeyEvent.VK_F6,
                java.awt.event.KeyEvent.VK_F7,
                java.awt.event.KeyEvent.VK_F8,
                java.awt.event.KeyEvent.VK_F9,
                java.awt.event.KeyEvent.VK_F10,
                java.awt.event.KeyEvent.VK_F11,
                java.awt.event.KeyEvent.VK_F12
            };

    public Workbench() {
        this(true, null);
    }

    public Workbench(boolean createDefaultModel) {
        this(createDefaultModel, null);
    }

    /** Creates new form WorkbenchFrame */
    public Workbench(boolean createDefaultModel, List<WorkbenchPlugin> toLoad) {

        System.out.println("Starting "+TITLE+" " + VERSION);
        try {
            if (conf.getProperty(Configuration.PROP_USE_SYSTEM_LOOK_AND_FEEL, "0").equals("1")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }
        } catch (Exception ex) {
            System.out.println("Unable to set look and feel");
        }

        // Show splash screen
        SwingUtils.center(splashScreen);
        splashScreen.setVisible(true);

        splashScreen.setStatus("Initializing Components...", 10);
        initComponents();
        initCustomComponents();

        if (conf.getProperty(ProcessEditorServer.CONF_SERVER_START_AT_WB_STARTUP).equals("1")) {
            splashScreen.setStatus("Starting Server...", 30);
            initServer();
        }

        if (toLoad != null) {
            for (WorkbenchPlugin plugin : toLoad) {
                plugin.init(splashScreen);
            }
        }

        if (createDefaultModel) {
            createDefaultModel();
            updateTitle();
        }

        splashScreen.setStatus("Opening window...", 80);

        splashScreen.setVisible(false);
        splashScreen.dispose();

    }

    private void initCustomComponents() {
        // Initialize newMenu
        initNewModelMenu();
        // Add resize listener
        layeredPane.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
                //logoPane.setLocation(layeredPane.getWidth()-logoPane.getWidth(),0);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                //
            }

            @Override
            public void componentShown(ComponentEvent e) {
                //
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                //
            }
        });

        // Add action listener for openMenuItem
        ProcessEditorOpenAction openAction = new ProcessEditorOpenAction(this);
        openMenuItem.addActionListener(openAction);
        openToolbarIcon.addActionListener(openAction);
        // Initialize recent menu
        updateOpenRecentMenu();
        // Add action listener for saveMenuItem
        ProcessEditorSaveAction saveAction = new ProcessEditorSaveAction(this);
        saveAsMenuItem.addActionListener(saveAction);
        saveToolbarIcon.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveMenuItemActionPerformed(e);
            }
        });
        // Add action listener for closeMenuItem
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Remove currently selected model
                removeModel(getSelectedModel());
            }
        });
        // Add action listener for Tab Change
        final Workbench outer = this;
        modelPane.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes

            @Override
            public void stateChanged(ChangeEvent evt) {
                // Add listener
                if (currentEditor != null) {
                    currentEditor.removeListener(outer);
                }
                currentEditor = getSelectedProcessEditor();
                currentEditor.addListener(outer);
                // Change title
                updateTitle();
                // Inform listeners
                for (WorkbenchEditorListener l : editorListeners) {
                    l.selectedProcessEditorChanged(getSelectedProcessEditor());
                }
            }
        });
        // Add all default plugins
        for (Class<?> c : PluginHelper.getPlugins()) {
            try {
                Constructor<?> con = c.getConstructor(Workbench.class);
                Object o = con.newInstance(this);
                WorkbenchPlugin plugin = (WorkbenchPlugin) o;
                addPlugin(plugin);
            } catch (Exception e) {
                System.out.println("Failed to instantiate plugin " + c + ": " + e.getMessage());
            }

        }
        // Check animation menu        
        animationEnabledMenuItem.setSelected(conf.getProperty(CONF_ANIMATION_ENABLED, "1").equals("1"));
        animationEnabledMenuItemActionPerformed(null);
        // Check toolbar menu
        showToolbarMenuItem.setSelected(conf.getProperty(CONF_SHOW_TOOLBAR, "1").equals("1"));
        showToolbarMenuItemActionPerformed(null);

        // Resize all components to current size
        setSize(800, 600);
        resizeComponents();

        // Set icon
        Toolkit tk = getToolkit();
        Image image = tk.getImage(getClass().getResource("/_logo/logo_1024.png"));
        while (!tk.prepareImage(image, -1, -1, this)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        setIconImage(image);

    }

    @SuppressWarnings("unchecked")
    private void initNewModelMenu() {
        JMenuItem wizardItem = new JMenuItem("Templates...");
        wizardItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                newToolbarIconActionPerformed(e);
            }
        });

        newModelMenu.add(wizardItem);
        newModelMenu.add(new JSeparator());

        for (Class c : WorkbenchHelper.getSupportedProcessModels()) {
            try {
                // Try to instantiate class
                Object o = c.newInstance();
                // Check if ProcessModel
                if (o instanceof ProcessModel) {
                    // Cast to ProcessModel
                    ProcessModel m = (ProcessModel) o;
                    // Create menu item
                    JMenuItem item = new JMenuItem(m.getDescription());
                    item.addActionListener(new WorkbenchModelFactory(this, c));
                    // Add menu item
                    newModelMenu.add(item);
                }
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Resizes all contained components of the Workbench
     */
    public void resizeComponents() {

        // Set new size of tool bar and editor
        toolBar.setSize(600, 50);
        if (toolBar.isVisible()) {
            skinLabel.setVisible(true);
            /* Disable logo by now
            logoLabel.setVisible(true);
            int pos = layeredPane.getWidth() - logoLabel.getWidth();
            if (pos < 600) {
                pos = 600;
            }
            logoLabel.setLocation(pos, 0);            
            */
        } else {
            // logoLabel.setVisible(false);
            skinLabel.setVisible(false);
        }
    }

    private void createDefaultModel() {
        processModelOpened(new BPMNModel());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        layeredPane = new javax.swing.JLayeredPane();
        toolBar = new javax.swing.JToolBar();
        newToolbarIcon = new javax.swing.JButton();
        openToolbarIcon = new javax.swing.JButton();
        saveToolbarIcon = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        publishToolbarIcon = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        undoToolbarIcon = new javax.swing.JButton();
        cutToolbarIcon = new javax.swing.JButton();
        copyToolbarIcon = new javax.swing.JButton();
        PasteToolbarIcon = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        ZoomInToolbarIcon = new javax.swing.JButton();
        zoomOriginalToolbarIcon = new javax.swing.JButton();
        zoomOutToolbarIcon = new javax.swing.JButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        jButton1 = new javax.swing.JButton();
        skinLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        modelPane = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newModelMenu = new javax.swing.JMenu();
        jSeparator5 = new javax.swing.JSeparator();
        openMenuItem = new javax.swing.JMenuItem();
        openRecentMenu = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JSeparator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        fetchFromServerMenuItem = new javax.swing.JMenuItem();
        publishToServerMenuItem = new javax.swing.JMenuItem();
        ShowVersionsMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        quitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenu = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        layoutMenuItem = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        configurationMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        zoomMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        showToolbarMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        animationEnabledMenuItem = new javax.swing.JCheckBoxMenuItem();
        pluginMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("inubit Workbench");
        setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        setMinimumSize(new java.awt.Dimension(35, 30));
        setName("frame"); // NOI18N

        layeredPane.setBackground(new java.awt.Color(255, 255, 255));
        layeredPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        layeredPane.setMinimumSize(new java.awt.Dimension(0, 30));
        layeredPane.setOpaque(true);
        layeredPane.setPreferredSize(new java.awt.Dimension(400, 50));

        toolBar.setBackground(new java.awt.Color(255, 255, 255));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setOpaque(false);

        newToolbarIcon.setBackground(new java.awt.Color(255, 255, 255));
        newToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/new.gif"))); // NOI18N
        newToolbarIcon.setText("New");
        newToolbarIcon.setToolTipText("New");
        newToolbarIcon.setFocusable(false);
        newToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(newToolbarIcon);

        openToolbarIcon.setBackground(new java.awt.Color(255, 255, 255));
        openToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/open.gif"))); // NOI18N
        openToolbarIcon.setText("Open");
        openToolbarIcon.setToolTipText("Open...");
        openToolbarIcon.setFocusable(false);
        openToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(openToolbarIcon);

        saveToolbarIcon.setBackground(new java.awt.Color(255, 255, 255));
        saveToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/save.gif"))); // NOI18N
        saveToolbarIcon.setText("Save");
        saveToolbarIcon.setToolTipText("Save");
        saveToolbarIcon.setFocusable(false);
        saveToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveToolbarIcon);
        toolBar.add(jSeparator7);

        publishToolbarIcon.setBackground(new java.awt.Color(255, 255, 255));
        publishToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/publish.gif"))); // NOI18N
        publishToolbarIcon.setText("Publish");
        publishToolbarIcon.setToolTipText("Publish to Server...");
        publishToolbarIcon.setFocusable(false);
        publishToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        publishToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        publishToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publishToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(publishToolbarIcon);
        toolBar.add(jSeparator12);

        undoToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/undo.gif"))); // NOI18N
        undoToolbarIcon.setText("Undo");
        undoToolbarIcon.setToolTipText("Undo");
        undoToolbarIcon.setFocusable(false);
        undoToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        undoToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        undoToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(undoToolbarIcon);

        cutToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/cut.gif"))); // NOI18N
        cutToolbarIcon.setText("Cut");
        cutToolbarIcon.setToolTipText("Cut");
        cutToolbarIcon.setFocusable(false);
        cutToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cutToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cutToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(cutToolbarIcon);

        copyToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/copy.gif"))); // NOI18N
        copyToolbarIcon.setText("Copy");
        copyToolbarIcon.setToolTipText("Copy");
        copyToolbarIcon.setFocusable(false);
        copyToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        copyToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        copyToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(copyToolbarIcon);

        PasteToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/paste.gif"))); // NOI18N
        PasteToolbarIcon.setText("Paste");
        PasteToolbarIcon.setToolTipText("Paste");
        PasteToolbarIcon.setFocusable(false);
        PasteToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PasteToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        PasteToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasteToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(PasteToolbarIcon);
        toolBar.add(jSeparator8);

        ZoomInToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/zoomin.gif"))); // NOI18N
        ZoomInToolbarIcon.setText("Zoom in");
        ZoomInToolbarIcon.setToolTipText("Zoom in");
        ZoomInToolbarIcon.setFocusable(false);
        ZoomInToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ZoomInToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        ZoomInToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZoomInToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(ZoomInToolbarIcon);

        zoomOriginalToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/zoom1x1.gif"))); // NOI18N
        zoomOriginalToolbarIcon.setText("Zoom 1:1");
        zoomOriginalToolbarIcon.setToolTipText("Zoom 1:1");
        zoomOriginalToolbarIcon.setFocusable(false);
        zoomOriginalToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOriginalToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOriginalToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOriginalToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(zoomOriginalToolbarIcon);

        zoomOutToolbarIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/zoomout.gif"))); // NOI18N
        zoomOutToolbarIcon.setText("Zoom out");
        zoomOutToolbarIcon.setToolTipText("Zoom out");
        zoomOutToolbarIcon.setFocusable(false);
        zoomOutToolbarIcon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOutToolbarIcon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOutToolbarIcon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutToolbarIconActionPerformed(evt);
            }
        });
        toolBar.add(zoomOutToolbarIcon);
        toolBar.add(jSeparator13);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar/icon_22x22_auto-ausrichten.gif"))); // NOI18N
        jButton1.setText("Layout");
        jButton1.setToolTipText("Auto Layout");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        toolBar.add(jButton1);

        layeredPane.add(toolBar);
        toolBar.setBounds(0, 0, 600, 48);

        skinLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        skinLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbar_skin.png"))); // NOI18N
        layeredPane.add(skinLabel);
        skinLabel.setBounds(0, 0, 550, 50);

        getContentPane().add(layeredPane, java.awt.BorderLayout.NORTH);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        modelPane.setBackground(new java.awt.Color(255, 255, 255));
        modelPane.setMinimumSize(new java.awt.Dimension(100, 100));
        modelPane.setPreferredSize(new java.awt.Dimension(800, 400));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(modelPane, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        newModelMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/new_small.gif"))); // NOI18N
        newModelMenu.setText("New");
        fileMenu.add(newModelMenu);
        fileMenu.add(jSeparator5);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/open_small.gif"))); // NOI18N
        openMenuItem.setText("Open...");
        fileMenu.add(openMenuItem);

        openRecentMenu.setText("Open Recent");
        fileMenu.add(openRecentMenu);
        fileMenu.add(jSeparator4);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/save_small.gif"))); // NOI18N
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setText("Save as...");
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator11);

        fetchFromServerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        fetchFromServerMenuItem.setText("Fetch from Server...");
        fetchFromServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fetchFromServerMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(fetchFromServerMenuItem);

        publishToServerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        publishToServerMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/publish_small.gif"))); // NOI18N
        publishToServerMenuItem.setText("Publish to Server...");
        publishToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publishToServerMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(publishToServerMenuItem);

        ShowVersionsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        ShowVersionsMenuItem.setText("Show Versions");
        ShowVersionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowVersionsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(ShowVersionsMenuItem);
        fileMenu.add(jSeparator3);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setText("Close ");
        fileMenu.add(closeMenuItem);
        fileMenu.add(jSeparator2);

        quitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitApplication(evt);
            }
        });
        fileMenu.add(quitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editMenuMouseClicked(evt);
            }
        });

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setText("Undo (exp.)");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);
        editMenu.add(jSeparator1);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/cut_small.gif"))); // NOI18N
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/copy_small.gif"))); // NOI18N
        copyMenuItem.setText("Copy");
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(copyMenuItem);

        pasteMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/paste_small.gif"))); // NOI18N
        pasteMenu.setText("Paste");
        pasteMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteMenuActionPerformed(evt);
            }
        });
        editMenu.add(pasteMenu);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/delete_small.gif"))); // NOI18N
        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(deleteMenuItem);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setText("Select All");
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(selectAllMenuItem);
        editMenu.add(jSeparator9);

        layoutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        layoutMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/icon_16x16_auto-ausrichten.gif"))); // NOI18N
        layoutMenuItem.setText("Layout model");
        layoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(layoutMenuItem);

        jCheckBoxMenuItem1.setText("Layout Edges automatically");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        editMenu.add(jCheckBoxMenuItem1);
        editMenu.add(jSeparator6);

        configurationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_MASK));
        configurationMenuItem.setText("Preferences");
        configurationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(configurationMenuItem);

        jMenuBar1.add(editMenu);

        viewMenu.setText("View");

        zoomMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/lens_small.gif"))); // NOI18N
        zoomMenu.setText("Zoom");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("50%");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem1);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setText("75%");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem6);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("100%");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("125%");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("150%");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("200%");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setZoom(evt);
            }
        });
        zoomMenu.add(jMenuItem5);

        viewMenu.add(zoomMenu);

        showToolbarMenuItem.setSelected(true);
        showToolbarMenuItem.setText("Show Toolbar");
        showToolbarMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showToolbarMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(showToolbarMenuItem);
        viewMenu.add(jSeparator10);

        animationEnabledMenuItem.setSelected(true);
        animationEnabledMenuItem.setText("Animation enabled");
        animationEnabledMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animationEnabledMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(animationEnabledMenuItem);

        jMenuBar1.add(viewMenu);

        pluginMenu.setText("Plug-Ins");
        jMenuBar1.add(pluginMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAboutDialog(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     * @param evt
     */
    private void quitApplication(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitApplication
        System.exit(0);
    }//GEN-LAST:event_quitApplication
    /**
     * 
     * @param evt
     */
    private void showAboutDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAboutDialog
        WorkbenchAboutDialog aboutDialog = new WorkbenchAboutDialog(this, true);
        SwingUtils.center(aboutDialog);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_showAboutDialog

    private void setZoom(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setZoom
        // Parse zoom level from evt
        double zoom = 1.0;
        try {
            String zoomStr = evt.getActionCommand();
            zoom = Double.parseDouble(zoomStr.substring(0, zoomStr.indexOf("%"))) / 100;
        } catch (Exception e) {
        }
        getSelectedProcessEditor().setScale(zoom);
        Component c = (JScrollPane) modelPane.getComponent(modelPane.getSelectedIndex());
        c.invalidate();
        updateScrollPaneViewport();
        this.invalidate();
        this.repaint();
    }//GEN-LAST:event_setZoom

    /**
     * Copy selection to clipboard.
     * @param evt
     */
    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
        ProcessEditor editor = getSelectedProcessEditor();
        clipboard.copy(editor, editor.getSelectionHandler());
    }//GEN-LAST:event_copyMenuItemActionPerformed

    /**
     * Inserts the selection from the clipboard.
     * @param evt
     */
    private void pasteMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuActionPerformed
        ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
        ProcessEditor editor = getSelectedProcessEditor();
        clipboard.paste(editor, editor.getSelectionHandler());
        editor.repaint();
    }//GEN-LAST:event_pasteMenuActionPerformed
    /**
     * @param evt
     */
    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
        ProcessEditor editor = getSelectedProcessEditor();
        clipboard.delete(editor, editor.getSelectionHandler());
        editor.repaint();
    }//GEN-LAST:event_deleteMenuItemActionPerformed
    /**
     * @param evt
     */
    private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemActionPerformed
        ProcessEditorClipboard clipboard = ProcessEditorClipboard.getInstance();
        ProcessEditor editor = getSelectedProcessEditor();
        clipboard.cut(editor, editor.getSelectionHandler());
        editor.repaint();
    }//GEN-LAST:event_cutMenuItemActionPerformed
    /**
     * @param evt
     */
    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        // Get current ActionTracker
        ProcessEditorActionTracker tracker = actionTrackers.get(getSelectedProcessEditor());
        if (tracker != null) {
            tracker.undoLastAction();
            getSelectedProcessEditor().repaint();
        }
    }//GEN-LAST:event_undoMenuItemActionPerformed
    /**
     * @param evt
     */
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed

        if (getSelectedModel().getProcessModelURI().endsWith(">")
                || (!getSelectedModel().getProcessModelURI().endsWith(".model"))) {
            // Show default dialog
            ProcessEditorSaveAction a = new ProcessEditorSaveAction(this);
            a.actionPerformed(new ActionEvent(this, 0, "Save as..."));
            return;
        }

        try {
            File selFile = new File(getSelectedModel().getProcessModelURI());

            Exporter exporter = new ProcessEditorExporter();
            exporter.serialize(selFile, getSelectedModel());

            processModelSaved(getSelectedModel(), selFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Model could not be saved.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_saveMenuItemActionPerformed
    /**
     * @param evt
     */
    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        // Select all
        for (ProcessNode n : getSelectedProcessEditor().getModel().getNodes()) {
            getSelectedProcessEditor().getSelectionHandler().addSelectedObject(n);
        }
        for (ProcessEdge e : getSelectedProcessEditor().getModel().getEdges()) {
            getSelectedProcessEditor().getSelectionHandler().addSelectedObject(e);
        }
        getSelectedProcessEditor().repaint();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed
    /**
     * @param evt
     */
    private void configurationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationMenuItemActionPerformed
        WorkbenchConfigurationDialog confDialog = new WorkbenchConfigurationDialog(this, true);
        SwingUtils.center(confDialog);
        confDialog.setVisible(true);
    }//GEN-LAST:event_configurationMenuItemActionPerformed
    /**
     * @param evt
     */
    private void showToolbarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showToolbarMenuItemActionPerformed
        // Status toggled
        if (showToolbarMenuItem.isSelected()) {
            // Show toolbar
            toolBar.setVisible(true);
            layeredPane.setVisible(true);
        } else {
            // Hide toolbar
            toolBar.setVisible(false);
            layeredPane.setVisible(false);
        }
        resizeComponents();
        // Write properties
        conf.setProperty(CONF_SHOW_TOOLBAR, showToolbarMenuItem.isSelected() ? "1" : "0");
    }//GEN-LAST:event_showToolbarMenuItemActionPerformed
    /**
     * @param evt
     */
    private void newToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newToolbarIconActionPerformed
        // Show NewModelWizard
        NewModelWizard wizard = new NewModelWizard(this, true, this);
        SwingUtils.center(wizard);
        wizard.setVisible(true);
        if (wizard.getResult() != null) {
            processModelOpened(wizard.getResult());
        }
    }//GEN-LAST:event_newToolbarIconActionPerformed

    private void undoToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoToolbarIconActionPerformed
        undoMenuItemActionPerformed(evt);
    }//GEN-LAST:event_undoToolbarIconActionPerformed

    private void cutToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutToolbarIconActionPerformed
        cutMenuItemActionPerformed(evt);
    }//GEN-LAST:event_cutToolbarIconActionPerformed

    private void copyToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyToolbarIconActionPerformed
        copyMenuItemActionPerformed(evt);
    }//GEN-LAST:event_copyToolbarIconActionPerformed

    private void PasteToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasteToolbarIconActionPerformed
        pasteMenuActionPerformed(evt);
    }//GEN-LAST:event_PasteToolbarIconActionPerformed
    /**
     * @param evt
     */
    private void ZoomInToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZoomInToolbarIconActionPerformed
        // Zoom 10% in
        double currentZoom = this.getProcessEditor(this.getActiveTab()).getScale();
        currentZoom = currentZoom * 1.1;
        if (currentZoom > 5.0) {
            currentZoom = 5.0;
        }
        this.getProcessEditor(this.getActiveTab()).setScale(currentZoom);
        updateScrollPaneViewport();
        this.repaint();
    }//GEN-LAST:event_ZoomInToolbarIconActionPerformed

    private void zoomOutToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutToolbarIconActionPerformed
        double currentZoom = this.getProcessEditor(this.getActiveTab()).getScale();
        // Check modifier keys
        if ((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
            currentZoom = 0.1;
        } else {
            // Zoom 10% out
            currentZoom = currentZoom * 0.9;
            if (currentZoom < .1) {
                currentZoom = 0.1;
            }
        }
        this.getProcessEditor(this.getActiveTab()).setScale(currentZoom);
        updateScrollPaneViewport();
        this.repaint();
    }//GEN-LAST:event_zoomOutToolbarIconActionPerformed

    /**
     * @param evt
     */
    private void zoomOriginalToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOriginalToolbarIconActionPerformed
        this.getProcessEditor(this.getActiveTab()).setScale(1.0);
        updateScrollPaneViewport();
        this.repaint();
    }//GEN-LAST:event_zoomOriginalToolbarIconActionPerformed

    private void layoutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutMenuItemActionPerformed
        // Layout model with default Layouter    	
        if (getSelectedModel().getUtils() != null) {
            if (getSelectedModel().getUtils().getLayouters().size() > 0) {
                // Get first layouter
                ProcessLayouter layouter = getSelectedModel().getUtils().getLayouters().get(0);
                new LayoutMenuitemActionListener(getSelectedProcessEditor(), layouter).actionPerformed(evt);
            }
        }
    }//GEN-LAST:event_layoutMenuItemActionPerformed
    /**
     * @param evt
     */
    private void animationEnabledMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animationEnabledMenuItemActionPerformed
        // Propagate status to all Editors
        for (int i = 0; i < getNumOfProcessEditors(); i++) {
            ProcessEditor e = getProcessEditor(i);
            e.setAnimationEnabled(animationEnabledMenuItem.isSelected());
        }
        conf.setProperty(CONF_ANIMATION_ENABLED, animationEnabledMenuItem.isSelected() ? "1" : "0");
    }//GEN-LAST:event_animationEnabledMenuItemActionPerformed
    /**
     * @param evt
     */
    private void fetchFromServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fetchFromServerMenuItemActionPerformed
        // Fetch model from server
        WorkbenchFetchModelDialog fetchModelDialog = new WorkbenchFetchModelDialog(this, true);
        SwingUtils.center(fetchModelDialog);
        fetchModelDialog.setVisible(true);
        ProcessModel model = fetchModelDialog.getSelectedProcessModel();
        if (model != null) {
            processModelOpened(model);
        }
    }//GEN-LAST:event_fetchFromServerMenuItemActionPerformed
    /**
     * @param evt
     */
    private void publishToServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishToServerMenuItemActionPerformed
        publishToServer();
    }//GEN-LAST:event_publishToServerMenuItemActionPerformed

    private void publishToolbarIconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishToolbarIconActionPerformed
        publishToServerMenuItemActionPerformed(evt);
    }//GEN-LAST:event_publishToolbarIconActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        layoutMenuItemActionPerformed(evt);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
	 * @param evt  
	 */
    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItem1ActionPerformed
        if (getSelectedProcessEditor() != null) {
            getSelectedProcessEditor().setLayoutEdges(jCheckBoxMenuItem1.isSelected());
            jCheckBoxMenuItem1.setSelected(getSelectedProcessEditor().isLayoutEdges());
        }
    }//GEN-LAST:event_jCheckBoxMenuItem1ActionPerformed

    /**
	 * @param evt  
	 */
    private void editMenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editMenuMouseClicked
        if (getSelectedProcessEditor() != null) {
            jCheckBoxMenuItem1.setSelected(getSelectedProcessEditor().isLayoutEdges());
        }
    }//GEN-LAST:event_editMenuMouseClicked

private void ShowVersionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowVersionsMenuItemActionPerformed
        try {
            ClientFascade repository = new ClientFascade(getSelectedModel());
            VersionExplorer versionExplorer = new VersionExplorer(repository);
            versionExplorer.setVisible(true);
        } catch (XMLHttpRequestException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error finding the model on the server. Probably there are no Versions of the current model on the server",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error finding the model on the server. Probably there are no Versions of the current model on the server",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        } catch (InvalidUserCredentialsException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error finding the model on the server. Probably there are no Versions of the current model on the server",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_ShowVersionsMenuItemActionPerformed

    /**
     * @param evt
     */
    /**
     * Adds a listener.
     * @param item
     */
    public void addWorkbenchEditorListener(WorkbenchEditorListener listener) {
        editorListeners.add(listener);
    }

    /**
     * Removes a listener.
     * @param listener
     */
    public void removeWorkbenchEditorListener(WorkbenchEditorListener listener) {
        editorListeners.remove(listener);
    }

    // Adds a (custom) plugin.
    public void addPlugin(WorkbenchPlugin plugin) {
        Component comp = plugin.getMenuEntry();
        System.out.println("Adding plugin... "+plugin);
        if ((comp instanceof JMenuItem) & (!(comp instanceof JMenu)))  {
            // Add shortcut to all top level entries
            JMenuItem menu = (JMenuItem)comp;
            if (pluginCount<FUNC_KEYS.length) {
                menu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(FUNC_KEYS[pluginCount],java.awt.event.InputEvent.CTRL_MASK));
            }
        }
        pluginCount++;
        pluginMenu.add(comp);
    }

    /**
     * Adds a ProcessModel in a separate tab pane.
     * @param m
     */
    public void addModel(String name, ProcessModel m) {
        ProcessEditor editor = WorkbenchHelper.getEditor(m);
        editor.setExtHandler(this);
        editor.setAnimationEnabled(animationEnabledMenuItem.isSelected());
        // Attach ProcessEditorTracker
        ProcessEditorActionTracker tracker = new ProcessEditorActionTracker(editor);
        actionTrackers.put(editor, tracker);

        // Inform Listeners
        for (WorkbenchEditorListener listener : editorListeners) {
            listener.newEditorCreated(editor);
        }
        modelPane.addTab(name, new JScrollPane(editor));
        modelPane.setTabComponentAt(modelPane.getTabCount() - 1, new WorkbenchTabPanel(this, editor, name));
        modelPane.setSelectedIndex(modelPane.getTabCount() - 1);
    }

    /**
     * Removes a tab containing a certain ProcessEditor.
     * @param e
     */
    public void removeTab(ProcessEditor e) {
        if (e == null) {
            return;
        }
        // Only remove model if another model is contained
        if (modelPane.getTabCount() < 2) {
            return;
        }

        Component removeComponent = null;
        for (Component c : modelPane.getComponents()) {
            Component comp = c;
            if (c instanceof JScrollPane) {
                comp = ((JScrollPane) c).getViewport().getView();
            }
            if (comp instanceof ProcessEditor) {
                ProcessEditor e2 = (ProcessEditor) comp;
                if (e == e2) {
                    removeComponent = c;
                }

                // Remove Tracker
                e.removeListener(actionTrackers.get(e));
            }
        }
        if (removeComponent != null) {
            modelPane.remove(removeComponent);
        }

        // Call Garbage Collector
        System.gc();
    }

    /**
     * Removes a tab containing a certain ProcessModel.
     * @param m
     */
    public void removeModel(ProcessModel m) {
        if (m == null) {
            return;
        }
        // Only remove model if another model is contained
        if (modelPane.getTabCount() < 2) {
            return;
        }

        Component removeComponent = null;
        for (Component c : modelPane.getComponents()) {
            Component comp = c;
            if (c instanceof JScrollPane) {
                comp = ((JScrollPane) c).getViewport().getView();
            }
            if (comp instanceof ProcessEditor) {
                ProcessEditor e = (ProcessEditor) comp;
                if (e.getModel() == m) {
                    removeComponent = c;
                }

                // Remove Tracker
                e.removeListener(actionTrackers.get(e));
            }
        }
        if (removeComponent != null) {
            modelPane.remove(removeComponent);
        }

        // Call Garbage Collector
        System.gc();
    }

    /**
     * Scrolls the JScrollPane to the given point. The scale of the current
     * editor is considered internally.
     * @param p
     */
    public void setViewportToPoint(Point p) {
        JScrollPane pane = (JScrollPane) modelPane.getSelectedComponent();
        double scale = getSelectedProcessEditor().getScale();
        p.x = (int) (p.x * scale - pane.getSize().width / 2);
        p.y = (int) (p.y * scale - pane.getSize().height / 2);
        pane.getViewport().setViewPosition(p);
        pane.invalidate();
        pane.repaint();
    }
    
    public static void setWorkbenchTitle(String title) {
        TITLE = title;
    }
    
    public static void setWorkbenchVersion(String version) {
        VERSION = version;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        JFrame f = new Workbench();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtils.center(f);
        f.setVisible(true);

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton PasteToolbarIcon;
    private javax.swing.JMenuItem ShowVersionsMenuItem;
    private javax.swing.JButton ZoomInToolbarIcon;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JCheckBoxMenuItem animationEnabledMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem configurationMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JButton copyToolbarIcon;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton cutToolbarIcon;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem fetchFromServerMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JMenuItem layoutMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTabbedPane modelPane;
    private javax.swing.JMenu newModelMenu;
    private javax.swing.JButton newToolbarIcon;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu openRecentMenu;
    private javax.swing.JButton openToolbarIcon;
    private javax.swing.JMenuItem pasteMenu;
    private javax.swing.JMenu pluginMenu;
    private javax.swing.JMenuItem publishToServerMenuItem;
    private javax.swing.JButton publishToolbarIcon;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton saveToolbarIcon;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JCheckBoxMenuItem showToolbarMenuItem;
    private javax.swing.JLabel skinLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JButton undoToolbarIcon;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenu zoomMenu;
    private javax.swing.JButton zoomOriginalToolbarIcon;
    private javax.swing.JButton zoomOutToolbarIcon;
    // End of variables declaration//GEN-END:variables

    @Override
    public void processModelOpened(ProcessModel model) {
        if (model.getProcessModelURI().isEmpty()) {
            // Set default URI
            model.setProcessModelURI("<" + model.getDescription() + ".model" + ">");
        }
        String name = model.getProcessName();
        if (name == null) {
            name = "";
        }
        String uri = model.getProcessModelURI();
        if (name.isEmpty()) {
            name = uri;
        }
        if (!name.startsWith("http")) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);            
        }
        addModel(name, model);
        // Check if credentials are attached
        if (model.getProperty(ProcessModel.PROP_EDITOR)!=null) {
            uri = model.getProperty(ProcessModel.PROP_EDITOR)+"@"+uri;
        }
        //updates Version Menu Item
        ShowVersionsMenuItem.setEnabled(model.isOnlineModel());
        addRecentModel(uri);
        updateScrollPaneViewport();
        model.markAsDirty(false);
    }

    @Override
    public void processModelSaved(ProcessModel model, File f) {
        // Change the title of the tab
        Component c = modelPane.getTabComponentAt(getActiveTab());
        String name = model.getProcessName();
        String uri = model.getProcessModelURI();
        if (name == null) {
            name = "";
        }
        if (name.isEmpty()) {
            name = uri;
        }
        if (!name.startsWith("http")) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }
        if (c instanceof WorkbenchTabPanel) {
            WorkbenchTabPanel wtp = (WorkbenchTabPanel) c;
            wtp.setText(name);
        } else {
            modelPane.setTitleAt(getActiveTab(), name);
        }
        updateTitle();
        modelPane.repaint();
        // Check if credentials are attached
        if (model.getProperty(ProcessModel.PROP_EDITOR)!=null) {
            uri = model.getProperty(ProcessModel.PROP_EDITOR)+"@"+uri;
        }
        addRecentModel(uri);
    }

    private void updateTitle() {
        String uri = "";
        Component c = modelPane.getTabComponentAt(getActiveTab());
        if (c instanceof WorkbenchTabPanel) {
            WorkbenchTabPanel wtp = (WorkbenchTabPanel) c;
            uri = " - " + wtp.getText();
        }
        // Change the title of the Workbench
        setTitle(TITLE + uri);
    }

    public void addRecentModel(String uri) {
        if (uri.endsWith(">")) {
            return;
        }
        Configuration conf = Configuration.getInstance();
        String recentFiles = conf.getProperty(Configuration.PROP_RECENT_FILES);
        if (recentFiles == null) {
            recentFiles = "";
        }
        String newRecentFiles = uri.replace("\\", "\\\\");
        newRecentFiles = newRecentFiles.replace(" ", "%20");
        uri = newRecentFiles;
        // Keep last 19 entries
        int count = 0;
        StringTokenizer entries = new StringTokenizer(recentFiles, ";");
        while (entries.hasMoreElements() && count < 20) {
            // Remove if already contained
            String entry = entries.nextToken();
            if (!entry.equals(uri)) {
                newRecentFiles += ";" + entry;
            }
            count++;
        }
        conf.setProperty(Configuration.PROP_RECENT_FILES, newRecentFiles);
        updateOpenRecentMenu();
    }

    public void updateOpenRecentMenu() {
        // @todo Fix Recent Files for Windows systems
        Configuration conf = Configuration.getInstance();
        String recentFiles = conf.getProperty(Configuration.PROP_RECENT_FILES);
        StringTokenizer entries = new StringTokenizer(recentFiles, ";");

        // No recent files...
        if (recentFiles == null) {
            openRecentMenu.setEnabled(false);
            return;
        }
        // Clear recent file menu
        openRecentMenu.removeAll();

        int count = 0;

        final ProcessEditorInterface outer = this;
        final Workbench outerWb = this;

        while (entries.hasMoreElements() && count < 20) {
            // Remove if already contained
            String entry = entries.nextToken();
            String compl_entry = entry;
            // Check if user is given
            String user="";
            if (entry.indexOf("@")>0) {
                user = entry.substring(0, entry.indexOf("@"));
                entry = entry.substring(entry.indexOf("@")+1);
            }

            // Replace %20 by " "
            if (!entry.startsWith("http")) {
                entry = entry.replace("%20", " ");
                entry = entry.replace("\\\\", "\\");
            }
            final String entry2 = entry;
            final String user2 = user;
            //System.out.println("USER "+user+" URL "+entry);

            count++;
            // Create menu entry
            JMenuItem recentFileMenu = new JMenuItem(compl_entry);
            try {

                recentFileMenu.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Check if starts with "http"
                        if (entry2.startsWith("http")) {
                            String serverUri = (URI.create(entry2).getScheme()+"://"+URI.create(entry2).getAuthority());
                            try {
                                Configuration conf = Configuration.getInstance();
                                UserCredentials credentials = new UserCredentials(
                                        URI.create(serverUri),
                                        conf.getProperty(WorkbenchConnectToServerDialog.CONF_SERVER_USER, "root"),
                                        conf.getProperty(WorkbenchConnectToServerDialog.CONF_SERVER_PASSWORD, "inubit"));

                                ProcessModel model = ProcessUtils.parseProcessModelSerialization(URI.create(entry2), credentials);
                                outer.processModelOpened(model);
                            } catch (Exception ex) {
                                if (ex instanceof InvalidUserCredentialsException || ex instanceof XMLHttpRequestException) {
                                    WorkbenchConnectToServerDialog dialog = new WorkbenchConnectToServerDialog(outerWb, outerWb, true);
                                    SwingUtils.center(dialog);
                                    dialog.setServerURI(serverUri);
                                    dialog.setPassword("");
                                    // Check if user is found
                                    dialog.setUser(user2);
                                    dialog.setVisible(true);
                                    if (dialog.isLoggedIn()) {
                                        try {
                                            // Try with new credentials
                                            ProcessModel model = ProcessUtils.parseProcessModelSerialization(URI.create(entry2), dialog.getCredentials());
                                            outer.processModelOpened(model);
                                            return;
                                        } catch (Exception ex2) {
                                            ex = ex2;
                                        }
                                    } else {
                                        return;
                                    }
                                }
                                ex.printStackTrace();
                                JOptionPane.showConfirmDialog(null, ex.getMessage(), "Error fetching file", JOptionPane.WARNING_MESSAGE);
                            }
                        } else {
                            // Open model from file                            
                            File openFile = new File(entry2);
                            ProcessEditorOpenAction action = new ProcessEditorOpenAction(outer);
                            action.openModel(openFile);
                        }
                    }
                });
                openRecentMenu.add(recentFileMenu);
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * returns a reference to the Panel that holds all processEditors in the center
     * and the toolbar in the north, so subclasses
     * can make use of the eastern, western or southern space
     * @return
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    @Override
    public ProcessModel getSelectedModel() {
        if (modelPane.getSelectedIndex() < 0) {
            return null;
        }

        Component c = modelPane.getSelectedComponent();
        // Has to be inside a JScrollPane
        c = ((JScrollPane) c).getViewport().getView();
        if (c instanceof ProcessEditor) {
            return ((ProcessEditor) c).getModel();
        }
        return null;
    }

    public ProcessEditor getSelectedProcessEditor() {
        return getProcessEditor(modelPane.getSelectedIndex());
    }

    /**
     * @param i
     */
    public ProcessEditor getProcessEditor(int i) {
        return ((ProcessEditor) ((JScrollPane) modelPane.getComponentAt(i)).getViewport().getComponent(0));
    }

    /**
     * returns the number of open tabs.
     * each ProcessEditor can be accessed through getProcessEditor(int)
     * @return
     */
    public int getNumOfProcessEditors() {
        return modelPane.getTabCount();
    }

    public int getActiveTab() {
        return modelPane.getSelectedIndex();
    }

    public void updateScrollPaneViewport() {
        JScrollPane scrollPane = (JScrollPane) modelPane.getComponentAt(getActiveTab());
        scrollPane.updateUI();
    }

    //
    // ProcessEditorListener follows
    //
    @Override
    public void processObjectClicked(ProcessObject o) {
        // Ignore
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
        // Ignore
    }

    @Override
    public void modelChanged(ProcessModel model) {
        if (model.getProcessModelURI() == null) {
            // Set default URI
            model.setProcessModelURI("<" + model.getDescription() + ".model" + ">");
        }
        String uri = model.getProcessModelURI();
        modelPane.setTitleAt(getActiveTab(), uri.substring(uri.lastIndexOf(File.separator) + 1));
        updateScrollPaneViewport();
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        updateScrollPaneViewport();
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    }

    @Override
    public void requestNewProcessEditor(ProcessModel model) {
        // Open a new model in a new tab
        addModel(model.getProcessName(), model);
    }

    private void initServer() {
        try {
            //try to start  server with port from configuration
            ProcessEditorServer.startForWorkbench("1".equals(Configuration.getInstance().getProperty(ProcessEditorServerHelper.CONF_SERVER_SECURE)), -1);
        } catch (Exception e) {
            System.err.println("Error on starting server!");
            e.printStackTrace();
        }
    }

    private void publishToServer() {
        try {
            WorkbenchPublishModelDialog publishModelDialog = new WorkbenchPublishModelDialog(this, true);
            SwingUtils.center(publishModelDialog);
            publishModelDialog.setTitleText(getSelectedModel().getProperty(ProcessModel.PROP_PROCESS_NAME));
            publishModelDialog.setFolder(getSelectedModel().getProperty(ProcessModel.PROP_FOLDERALIAS));
            ClientFascade repository = new ClientFascade(getSelectedModel());
            boolean publishAsNewModel = repository.shouldBePublishedAsNewModel();
            publishModelDialog.setPublishAsNewModel(publishAsNewModel);
            publishModelDialog.setVisible(true);
            // Fetch credentials
            if (publishModelDialog.isPublishAsNewModel()) {
                // Get credentials
                getSelectedModel().setTransientProperty(ProcessUtils.TRANS_PROP_CREDENTIALS, publishModelDialog.getCredentials());
            }
            //call dialog
            boolean canceled = false;
            if (!repository.commitIfPossible(publishModelDialog.getAnswer())) {
                VersionExplorer versionExplorer = new VersionExplorer(repository);
                versionExplorer.setVisible(true);
                canceled = versionExplorer.canceled;
            }
            if (publishModelDialog.getAnswer().publish && !canceled) {
                processModelSaved(repository.LoadHeadModel().getProcessModel(), null);
                processModelOpened(repository.LoadHeadModel().getProcessModel());
                modelPane.remove(getActiveTab() - 1);
                getSelectedModel().markAsDirty(false);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    @Override
    public ProcessEditor openNewModel(ProcessModel model) {
        this.addModel(model.getProcessName(), model);
        return getSelectedProcessEditor();
    }
}
