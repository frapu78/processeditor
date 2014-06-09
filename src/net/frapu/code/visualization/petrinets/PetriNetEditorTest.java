/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import net.frapu.code.visualization.*;

import javax.swing.*;

/**
 *
 * @author frank
 */
public class PetriNetEditorTest {

    private ProcessEditor editor;
    
    public PetriNetEditorTest() {
        // Show editor
        JFrame f = new JFrame("Process Editor Petri Net Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editor = new ProcessEditor(createExample());
        
        // Add pane to frame and show
        f.add(new JScrollPane(editor));
        f.pack();
        f.setVisible(true);
    }
    
    private PetriNetModel createExample() {

        // Prepare model
        PetriNetModel model = new PetriNetModel("Sample Workflow net");

        // Create nodes
        Place i = new Place(50, 70, "i");
        model.addNode(i);

        Transition t1 = new Transition(130, 70, "t1");
        model.addNode(t1);

        Place p1 = new Place(210, 30, "p1");
        model.addNode(p1);

        Place p2 = new Place(210, 100, "p2");
        model.addNode(p2);

        Transition t2 = new Transition(290, 70, "t2");
        model.addNode(t2);

        Place o = new Place(370, 70, "o");
        model.addNode(o);

        // Create edges
        Edge e1 = new Edge(i, t1);
        model.addEdge(e1);

        Edge e2 = new Edge(t1, p1);
        model.addEdge(e2);

        Edge e3 = new Edge(t1, p2);
        model.addEdge(e3);

        Edge e4 = new Edge(p1, t2);
        model.addEdge(e4);

        Edge e5 = new Edge(p2, t2);
        model.addEdge(e5);

        Edge e6 = new Edge(t2, o);
        model.addEdge(e6);
        
        return model;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new PetriNetEditorTest();
            }
        });
    }
}
