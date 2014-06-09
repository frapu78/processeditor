/**
 *
 * Process Editor - inubit Workbench Plugin Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins;

import com.inubit.research.gui.Workbench;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import javax.swing.JMenuItem;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.uml.ClassModel;
import net.frapu.code.visualization.uml.UMLClass;

/**
 *
 * @author fpu
 */
public class SourceVisualizerPlugin extends WorkbenchPlugin {

    public SourceVisualizerPlugin(Workbench workbench) {
        super(workbench);
    }

    @Override
    public Component getMenuEntry() {
        JMenuItem item = new JMenuItem("Visualize Source Code...");

        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ProcessModel model = parseSourceDirectory(new File("C:\\Users\\fpu.INUBIT\\Documents\\NetBeansProjects\\Processeditor\\src\\net\\frapu\\code\\visualization"));

                if (model!=null) {
                    workbench.openNewModel(model);
                }

                System.out.println("DONE");

            }
        });

        return item;
    }

    public ProcessModel parseSourceDirectory(File dir) {
        if (!dir.isDirectory()) return null;

        ClassModel model = new ClassModel("Visualized Source");
        model.setProcessName("Source Visualization");
        model.setProperty("#source", dir.toString());

        // Find all *.java files in current dir
        for (File file: dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".java");
            }
        })) {

            System.out.println(file);

            ProcessNode uc = parseJavaFile(file);
            if (uc!=null) {
                model.addNode(uc);
            }
        }

        System.out.println("MODEL CREATED");

        return model;
    }

    private ProcessNode parseJavaFile(File file) {
        if (!file.exists()) return null;

        UMLClass uc = new UMLClass();

        //System.out.print("Parsing file "+file);

        // Read file
        String data = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            boolean commentMode = false;
            while (reader.ready()) {
                String currLine = reader.readLine();
                // Trim comments
                if (currLine.indexOf("//")>=0) {
                    currLine.substring(0, currLine.indexOf("//"));
                }
                data += currLine;
            }
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        // Remove multi-line comments
        //data.replaceAll("/\\*", "")

        // Parse class name
        if (data.indexOf(" class ")<0) return null;
        String className = data.substring(data.indexOf(" class ")+7);
        className = className.trim();
        className = className.substring(0, className.indexOf(" "));

        System.out.print("FOUND "+className);

        uc.setText(className);

        System.out.println(" DONE");

        return uc;
    }



}
