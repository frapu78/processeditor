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

import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.frapu.code.converter.ConverterHelper;
import net.frapu.code.converter.Exporter;
import net.frapu.code.converter.ProcessEditorExporter;
import net.frapu.code.converter.XPDLExportDialog;
import net.frapu.code.converter.XPDLExporter;

/**
 *
 * @author fpu
 */
public class ProcessEditorSaveAction implements ActionListener {

    private ProcessEditorInterface pei;
    private File currentDirectory;

    public ProcessEditorSaveAction(ProcessEditorInterface i) {
        this.pei = i;
        currentDirectory = null;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        // Get file name
        String filename = java.io.File.separator + "model";
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(new java.io.File(filename));
        fc.setDialogTitle("Save/Export Model");

        if (currentDirectory != null) {
            fc.setCurrentDirectory(currentDirectory);
        }

        for (FileFilter ff : ConverterHelper.getExporterFileFilters(pei.getSelectedModel().getClass())) {
            fc.addChoosableFileFilter(ff);
        }

       
       

        if (fc.showSaveDialog(null) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        FileFilter selectedFF;        
       //if using AcceptAllFileFilter we have to select a default file filter ourselves 
       if (fc.isAcceptAllFileFilterUsed()) {
           //we just select the first one after the selectAllFilter
           if (fc.getChoosableFileFilters().length<2) {
               throw new UnsupportedOperationException("No FileFilter for model found");
           }
           selectedFF = fc.getChoosableFileFilters()[1];       
       } else {
           // Get selected file filter
           selectedFF = fc.getFileFilter();
       }
           
        

        
        java.io.File selFile = fc.getSelectedFile();
        // Check if extension contained
        if (!selFile.getName().contains(".")) {
            // No extension
            try {
                String type = selectedFF.getDescription();
                type = type.substring(type.indexOf("*.") + 2);
                if (type.indexOf(";") >= 0) {
                    type = type.substring(0, type.indexOf(";"));
                } else {
                    type = type.substring(0, type.indexOf(")"));
                }
                selFile = new File(selFile.getPath() + "." + type);
            } catch (Exception e) {
            }
        }

        // Check if file has been selected, abort otherwise
        if (selFile == null) {
            return;
        }

        // Check if file exists
        if (selFile.exists()) {
            // Show confirm dialog
            if (JOptionPane.showConfirmDialog(null, "File exists, overwrite?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
        }        


        try {
            // Iterate over until matching file filter is found
            int pos = 0;
            for (FileFilter ff : ConverterHelper.getExporterFileFilters(pei.getSelectedModel().getClass())) {
                if (selectedFF.getDescription().equals(ff.getDescription())) {
                    // Save file
                    Exporter exporter = ConverterHelper.getExportersFor(pei.getSelectedModel().getClass()).get(pos);
                    if (exporter instanceof XPDLExporter) {
                        XPDLExportDialog dialog = new XPDLExportDialog(null,true);
                        SwingUtils.center(dialog);
                        dialog.setVisible(true);                    
                        XPDLExporter ex= (XPDLExporter)exporter;
                        ex.setDto(dialog.con);
                    }
                    exporter.serialize(selFile, pei.getSelectedModel());

                    if (exporter instanceof ProcessEditorExporter) {
                        try {
                            pei.getSelectedModel().setProcessModelURI(selFile.getAbsolutePath());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    break;
                }
                pos++;
            }
            pei.getSelectedModel().markAsDirty(false);
            pei.processModelSaved(pei.getSelectedModel(), selFile);
            currentDirectory = fc.getCurrentDirectory();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
