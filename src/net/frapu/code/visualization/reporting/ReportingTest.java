/**
 *
 * Process Editor - Reporting Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.reporting;

import java.util.LinkedList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.frapu.code.visualization.ProcessEditor;

/**
 *
 * @author fpu
 */
public class ReportingTest {

        private ProcessEditor editor;

    public ReportingTest() {
        // Show editor
        JFrame f = new JFrame("Reporting Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editor = new ProcessEditor(createExample());

        // Add pane to frame and show
        f.add(new JScrollPane(editor));
        f.pack();
        f.setVisible(true);
    }

    private ReportingModel createExample() {
        // Prepare model
        ReportingModel model = new ReportingModel("Reporting model");
        
        // Create sample
        BarChart b1 = new BarChart();
        b1.setText("Waiting Times per Instance");
        b1.setSize(300, 200);
        b1.setPos(200,150);
        b1.setYLabel("Waiting Time (min)");
        b1.setXLabel("Instances");

        LinkedList<Integer> data = new LinkedList<Integer>();
        Random rand = new Random();
        for (int i=0; i<100; i++) {
            data.add(rand.nextInt(501));
        }

        b1.setData(data);

        PieChart p1 = new PieChart();
        p1.setText("I'm a PieChart");
        p1.setSize(200,200);
        p1.setPos(200, 400);

        p1.setProperty(PieChart.PROP_VALUES, "10");
        p1.setProperty(PieChart.PROP_LABELS, "A");
        p1.setHighlightIndex(-1);

        model.addNode(p1);
        
        model.addNode(b1);

        return model;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new ReportingTest();
            }
        });
    }

}
