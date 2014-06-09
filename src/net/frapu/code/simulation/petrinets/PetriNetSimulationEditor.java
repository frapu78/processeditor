/**
 *
 * Process Editor - Petri net Simulation Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.petrinets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import net.frapu.code.visualization.petrinets.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import net.frapu.code.visualization.*;

import javax.swing.*;

import net.frapu.code.simulation.petrinets.reporting.SimulationReportingFrame;

/**
 *
 * Provides a Petri net-based Process Editor with simulation and analysis
 * capabilities.
 *
 * @author frank
 */
public class PetriNetSimulationEditor extends ProcessEditor implements
        ProcessEditorListener,
        ProcessModelListener,
        PetriNetSimulationListener {

    private static final long serialVersionUID = -8809409716479298591L;
    private int simulationSpeed = 25;
    private int tokenCount = 100;
    private int processInstance;
    private PetriNetEngine engine;
    private PetriNetSimulationEditor outer;
    private PetriNetSimulation simulation;
    private SimulationConfigurationFrame configurationFrame;
    private SimulationReportingFrame reportingFrame;
    private JMenuItem simulationMenuItem = null;
    private JCheckBoxMenuItem steppingCheckBox = null;
    private JCheckBoxMenuItem timeTicksCheckBox = null;

    public PetriNetSimulationEditor() {
        super();
        initialize();
        this.setModel(createExample());
    }

    public PetriNetSimulationEditor(ProcessModel m) {
        super();
        initialize();
        this.setModel(m);
    }

    private void initialize() {

        // Initialize Engine
        engine = new PetriNetEngine((PetriNetModel) this.getModel());

        // Initialize simulation properties
        simulation = new PetriNetSimulation();
        simulation.addListener(this);

        // Initialize reporting frame
        if (reportingFrame == null) {
            reportingFrame = new SimulationReportingFrame(simulation);
            reportingFrame.pack();
            SwingUtils.center(reportingFrame);
        }

        // Initialize configuration frame
        if (configurationFrame == null) {
            configurationFrame = new SimulationConfigurationFrame(this);
            configurationFrame.pack();
            SwingUtils.center(configurationFrame);
        }

        this.addListener(this);
        this.getModel().addListener(this);
        outer = this;

        //
        // Add Petri net custom menus
        //

        final JMenuItem menuItem4 = new JMenuItem("Check Soundness...");
        menuItem4.setEnabled(false);
        this.addCustomPopUpMenuItem(menuItem4);

        final JMenuItem hotPathMenuItem = new JMenuItem("Highlight hot path");
        hotPathMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getModel() instanceof PetriNetModel) {
                    (new HotPathAnalysis()).highlightHottestPath((PetriNetModel) getModel());
                }
                updateUI();
            }
        });
        this.addCustomPopUpMenuItem(hotPathMenuItem);


        steppingCheckBox = new JCheckBoxMenuItem("Enable stepping");
        steppingCheckBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/icon_16x16_scuttle.gif")));
        steppingCheckBox.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Call engine
                if (steppingCheckBox.isSelected()) {
                    // Disable timeTicksCheckBox
                    timeTicksCheckBox.setSelected(false);
                    // Disable time-ticks
                    engine.setTimingEnabled(false);
                    // Highlight enabled transitions
                    engine.calculateEnabledTransitions(processInstance);
                    outer.repaint();
                    return;
                }
                engine.clearEnabledTransitionHighlights();
                outer.repaint();
            }
        });
        this.addCustomPopUpMenuItem(steppingCheckBox);

        timeTicksCheckBox = new JCheckBoxMenuItem("Use time-ticks");
        timeTicksCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Disable stepping
                steppingCheckBox.setSelected(false);
                engine.clearEnabledTransitionHighlights();
                engine.setTimingEnabled(timeTicksCheckBox.isSelected());
                outer.repaint();
            }
        });
        this.addCustomPopUpMenuItem(timeTicksCheckBox);

        final JMenuItem menuItem2 = new JMenuItem("Reset net");
        menuItem2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Remove all tokens from the net
                for (ProcessNode n : outer.getModel().getNodes()) {
                    if (n instanceof Place) {
                        Place p = (Place) n;
                        p.removeAllTokens();
                    }
                }
                if (steppingCheckBox.isSelected()) {
                    engine.calculateEnabledTransitions(processInstance);
                }
                if (timeTicksCheckBox.isSelected()) {
                    simulation.stopSimulation();
                    // Enable simulation dialog
                    simulationMenuItem.setEnabled(true);
                }
                // Reset calculated instance costs
                engine.resetEngine();
                outer.repaint();
            }
        });
        this.addCustomPopUpMenuItem(menuItem2);

        final JMenuItem menuItem8 = new JMenuItem("Show reporting...");
        menuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/icon_16x16_barchart.gif")));
        menuItem8.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Show reporting frame
                reportingFrame.setVisible(true);
            }
        });
        this.addCustomPopUpMenuItem(menuItem8);

        final JMenuItem menuItem3 = new JMenuItem("Show configuration...");
        menuItem3.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Show configuration frame
                configurationFrame.setVisible(true);
            }
        });
        this.addCustomPopUpMenuItem(menuItem3);

        //
        // Add context menu
        //
        final JMenuItem contextItem1 = new JMenuItem("Add token");
        contextItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Add token to this place
                if (outer.getSelectionHandler().getLastSelection() instanceof Place) {
                    Place p = (Place) outer.getSelectionHandler().getLastSelection();
                    p.addToken(new Token(processInstance));
                    updateSimulation();
                }
                outer.repaint();
            }
        });
        this.addCustomContextMenuItem(Place.class, contextItem1);
        this.addCustomContextMenuItem(ResourcePlace.class, contextItem1);

        final JMenuItem contextItem2 = new JMenuItem("Remove token");
        contextItem2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Add token to this place
                if (outer.getSelectionHandler().getLastSelection() instanceof Place) {
                    Place p = (Place) outer.getSelectionHandler().getLastSelection();
                    p.removeToken(processInstance);
                    updateSimulation();
                }
                outer.repaint();
            }
        });
        this.addCustomContextMenuItem(Place.class, contextItem2);
        this.addCustomContextMenuItem(ResourcePlace.class, contextItem2);

        simulationMenuItem = new JMenuItem("Start simulation here");
        simulationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu/icon_16x16_play.gif")));
        simulationMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (outer.getSelectionHandler().getLastSelection() instanceof Place) {
                    Place p = (Place) outer.getSelectionHandler().getLastSelection();

                    simulation.setModel((PetriNetModel) outer.getModel());
                    simulation.setStartPlace(p);
                    simulation.setTokenCount(tokenCount);
                    simulation.setSimulationSpeed(simulationSpeed);
                    simulation.setEngine(engine);

                    simulation.startSimulation();
                    simulationMenuItem.setEnabled(false);
                }
            }
        });
        this.addCustomContextMenuItem(Place.class, simulationMenuItem);

        final JMenu inspectMenu = new JMenu("Inspect");
        this.addCustomContextMenuItem(Place.class, inspectMenu);

        final JMenuItem contextItem4 = new JMenuItem("Contained Tokens");
        // Add action listener
        contextItem4.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Create properties panel
                Map<String, String> values = new HashMap<String, String>();

                // Collect relevant values from all Token contained in this Place
                Place p = (Place) outer.getSelectionHandler().getLastSelection();
                Token shortestWaitingToken = engine.getShortestWaitingToken(p);
                Token longestWaitingToken = engine.getLongestWaitingToken(p);

                if (shortestWaitingToken != null) {
                    values.put("Shortest waiting time (id)",
                            shortestWaitingToken.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME)
                            + " (" + shortestWaitingToken.getProcessInstance() + ")");
                }

                if (longestWaitingToken != null) {
                    values.put("Longest waiting time (id)",
                            longestWaitingToken.getProperty(PetriNetEngine.PROP_TOKEN_WAIT_TIME)
                            + " (" + longestWaitingToken.getProcessInstance() + ")");
                }


                final InspectorPanel propPanel = new InspectorPanel(values);
                //propPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
                final JDialog propDialog = new JDialog();
                propDialog.setLayout(
                        new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 0;

                propDialog.add(propPanel, c);

                // Create button
                JButton okButton = new JButton("Ok");
                okButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        propDialog.dispose();
                    }
                });
                c.gridy = 1;

                propDialog.add(okButton, c);

                propDialog.pack();
                propDialog.setVisible(true);
            }
        });
        inspectMenu.add(contextItem4);

        final JMenuItem contextItem5 = new JMenuItem("Path of Token with shortest duration");
        contextItem5.setEnabled(false);
        inspectMenu.add(contextItem5);

        final JMenuItem contextItem6 = new JMenuItem("Open subnet...");
        contextItem6.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ProcessModel subModel = ((SubNet) outer.getSelectionHandler().getLastSelection()).getSubNet();
                outer.repaint();

                if (subModel != null) {
                    // Create new editor
                    PetriNetSimulationEditor pnEditor = new PetriNetSimulationEditor();
                    pnEditor.setModel(subModel);

                    // Show editor
                    JFrame f = new JFrame(subModel.getProcessName());
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    // Add pane to frame and show
                    f.add(new JScrollPane(pnEditor));
                    f.pack();
                    SwingUtils.center(f);
                    f.setVisible(true);


                }
            }
        });
        this.addCustomContextMenuItem(SubNet.class, contextItem6);
    }

    @Override
    public void setModel(ProcessModel model) {
        super.setModel(model);
        this.engine = new PetriNetEngine((PetriNetModel) model);
    }

    @Override
    public ProcessModel createExample() {

        processInstance = 1;

        // Prepare localModel
        PetriNetModel localModel = new PetriNetModel("Sample Petri net");

        // Create nodes
        Place i = new Place(50, 70, "i");
        localModel.addNode(i);

        Transition t1 = new Transition(130, 70, "t1");
        t1.setProperty(Transition.PROP_DURATION, "10");
        t1.setProperty(Transition.PROP_COST, "5");

        localModel.addNode(t1);

        Place p1 = new Place(210, 30, "p1");
        localModel.addNode(p1);

        Place p2 = new Place(210, 100, "p2");
        localModel.addNode(p2);

        Transition t2 = new Transition(290, 70, "t2");
        localModel.addNode(t2);
        t2.setProperty(Transition.PROP_DURATION, "5");
        t2.setProperty(Transition.PROP_COST, "2");

        Place o = new Place(370, 70, "o");
        localModel.addNode(o);

        // Create edges
        Edge e1 = new Edge(i, t1);
        localModel.addEdge(e1);

        Edge e2 = new Edge(t1, p1);
        localModel.addEdge(e2);

        Edge e3 = new Edge(t1, p2);
        localModel.addEdge(e3);

        Edge e4 = new Edge(p1, t2);
        localModel.addEdge(e4);

        Edge e5 = new Edge(p2, t2);
        localModel.addEdge(e5);

        Edge e6 = new Edge(t2, o);
        localModel.addEdge(e6);

        // Create and add Tokens
        Token tok = new Token(processInstance);
        i.addToken(tok);

        return localModel;
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setSimulationSpeed(int simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
        if (simulation != null) {
            simulation.setSimulationSpeed(simulationSpeed);
        }
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
        // Inform simulation
        if (simulation != null) {
            simulation.setTokenCount(tokenCount);
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        simulation.kill();
    }

    public static void main(String[] args) {

        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Unable to load native look and feel");
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                // Create new editor
                PetriNetSimulationEditor pnEditor = new PetriNetSimulationEditor();

                // Show editor
                JFrame f = new JFrame("PetriNetSimulationEditor");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // Add pane to frame and show
                f.add(new JScrollPane(pnEditor));
                f.setSize(600, 400);
                SwingUtils.center(f);
                f.setVisible(true);

            }
        });
    }

    public void updateSimulation() {
        if (steppingCheckBox.isSelected()) {
            engine.calculateEnabledTransitions(processInstance);
            this.repaint();
        }
    }

    /**
     * Determines the actions if a ProcessNode is clicked.
     * @param o
     */
    @Override
    public void processObjectClicked(ProcessObject o) {

        if (!steppingCheckBox.isSelected()) {
            return;
        }

        if (o instanceof Transition) {
            Transition t = (Transition) o;
            if (t.isEnabled(engine.getPredecessors(t), processInstance)) {
                // @todo: refactor to work with time-ticks!!!
                engine.fireTransition(t, processInstance);
                engine.calculateEnabledTransitions(processInstance);
                this.repaint();
                // Debug current instance costs
                System.out.println("Current instance costs: "
                        + engine.getInstanceCost(processInstance));
            }
        }
    }

    @Override
    public void processObjectDoubleClicked(ProcessObject o) {
    }

    public void processNodeSelected(ProcessNode o) {
        // Ignore event
        return;
    }

    public void processNodeDeselected(ProcessNode o) {
        // Ignore event
        return;
    }

    @Override
    public void modelChanged(ProcessModel m) {
        // Update engine
        if (m instanceof PetriNetModel) {
            this.getModel().addListener(this);
            engine = new PetriNetEngine((PetriNetModel) m);
            steppingCheckBox.setSelected(false);
            timeTicksCheckBox.setSelected(false);
        } else {
            JOptionPane.showMessageDialog(null, "Unsupported model loaded.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void processNodeAdded(ProcessNode newNode) {
        updateSimulation();
    }

    @Override
    public void processNodeRemoved(ProcessNode remNode) {
        updateSimulation();
    }

    @Override
    public void processEdgeAdded(ProcessEdge edge) {
        updateSimulation();
    }

    @Override
    public void processEdgeRemoved(ProcessEdge edge) {
        updateSimulation();
    }

    @Override
    public void processObjectDragged(Dragable o, int oldX, int oldY) {
        // Do nothing here...
    }

    @Override
    public void refreshDisplay() {
        this.repaint();
    }

    @Override
    public void simulationFinished() {
        // Enable simulation menu
        simulationMenuItem.setEnabled(true);
    }

    @Override
    public void simulationStarted() {
        // skip
    }

    @Override
    public void processNodeEditingFinished(ProcessNode o) {
    }

    @Override
    public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
    }
}
