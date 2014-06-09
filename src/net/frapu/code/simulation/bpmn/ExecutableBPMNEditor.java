/**
 *
 * Process Editor - Executable BPMN Package
 *
 * (C) 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.simulation.bpmn;

import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.SwingUtils;
import net.frapu.code.visualization.bpmn.Pool;

/**
 *
 * Provides a BPMN 2.0 based Process Editor with execution capabilities
 * according to the Draft specification v0.9.14
 *
 * @author frank
 */
public class ExecutableBPMNEditor extends ProcessEditor {

	private static final long serialVersionUID = 7550165125851525653L;

	public ExecutableBPMNEditor() {
        initialize();
    }

    public ExecutableBPMNEditor(ProcessModel m) {
        initialize();
        setModel(m);
    }

    private void initialize() {

        //
        // Add context menus
        //
        final JMenuItem contextItem1 = new JMenuItem("Start execution...");
        contextItem1.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // Do something useful here...
            }
        });
        this.addCustomContextMenuItem(Pool.class, contextItem1);

    }

    public static void main(String[] args) {

        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("Unable to load native look and feel");
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                // Create new editor
                ExecutableBPMNEditor pnEditor = new ExecutableBPMNEditor();

                // Show editor
                JFrame f = new JFrame("Executable BPMN 2.0 Editor");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                // Add pane to frame and show
                f.add(new JScrollPane(pnEditor));
                f.setSize(600, 400);
                SwingUtils.center(f);
                f.setVisible(true);

            }
        });
    }
    

}
