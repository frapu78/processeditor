/**
 *
 * Process Editor - Grails Generator Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.grailsgen;

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.uml.ClassModel;
import net.frapu.code.visualization.uml.UMLClass;

/**
 *
 * @author fpu
 */
public class GrailsGenerator {

    private ClassModel model;
    private String packageName;

    public GrailsGenerator(ClassModel model, String packageName) {
        this.model = model;
        this.packageName = packageName;
    }

    /**
     * Creates a Grails project for the given class model at the given
     * directory.
     * @param directory
     * @param packageName
     */
    public void createGrailsProject(File directory, String packageName) {
        
    }


    /**
     * Generates the implementation class (grails) for a graphical UML class.
     */
    public String generateClassImplementation(UMLClass umlClass) {

        System.out.println("Generating implementation for Class "+umlClass.getName());

        String result="";

        result += "package "+packageName+"\n\n";
        result += "class "+getValidIdentifier(umlClass.getName())+" { \n";

        // Generate attributes
        String constraints = "\n   static constraint = {\n";
        StringTokenizer st = new StringTokenizer(umlClass.getProperty(UMLClass.PROP_ATTRIBUTES), ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            // Split type
            String name = token.substring(0, token.indexOf(":"));
            String type = token.substring(token.indexOf(":")+1, token.length());
            result+="   "+type+" "+getValidIdentifier(name)+"\n";

            constraints += "      "+getValidIdentifier(name)+"()\n";
        }
        constraints +="   }\n";

        // Generate references



        result += constraints+"}";

        return result;
    }

    private String getValidIdentifier(String in) {
        String result="";
        
        for (int i=0; i<in.length(); i++) {
            String ch = in.substring(i, i+1);
            if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".contains(ch)) result += ch;
        }
        
        return result;
    }

    public static void main(String args[]) throws Exception {
        ProcessModel model = ProcessUtils.parseProcessModelSerialization(new FileInputStream("test.model"));
        GrailsGenerator gen = new GrailsGenerator((ClassModel)model, "com.inubit.examples");
     
        for (ProcessNode node: model.getNodes()) {
            if (node instanceof UMLClass) {
                String result = gen.generateClassImplementation((UMLClass)node);
                System.out.println(result);
            }
        }

    }

}
