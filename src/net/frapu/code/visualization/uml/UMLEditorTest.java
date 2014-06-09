/**
 *
 * Process Editor - UML Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.uml;

import net.frapu.code.visualization.*;

import javax.swing.*;

/**
 *
 * @author frank
 */
public class UMLEditorTest {

    private ProcessEditor editor;
    
    public UMLEditorTest() {
        // Show editor
        JFrame f = new JFrame("UML Editor Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editor = new ProcessEditor(createExample());
        
        // Add pane to frame and show
        f.add(new JScrollPane(editor));
        f.pack();
        f.setVisible(true);
    }
    
    private ClassModel createExample() {

        // Prepare model
        ClassModel model = new ClassModel("UML Class model");

        // Create sample Class
        UMLClass c1 = new UMLClass("Class 1");
        c1.setPos(100, 100);

        model.addNode(c1);
        
        return model;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new UMLEditorTest();
            }
        });
    }
}
