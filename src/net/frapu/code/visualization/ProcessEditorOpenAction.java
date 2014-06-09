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

import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import net.frapu.code.converter.ConverterHelper;

/**
 *
 * @author fpu
 */
public class ProcessEditorOpenAction implements ActionListener {

    private ProcessEditorInterface pei;
    private File currentDirectory;

    public ProcessEditorOpenAction(ProcessEditorInterface i) {
        this.pei = i;
        currentDirectory=null;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        File selFile = ConverterHelper.pickFileForImport(currentDirectory);
        if(selFile != null) {
	        openModel(selFile);
	        currentDirectory = selFile.getParentFile();
        }
    }

    

	public void openModel(File selFile) throws HeadlessException {
        // Abort otherwise
        try {
            List<ProcessModel> models = ConverterHelper.importModels(selFile);
            if (models==null) throw new Exception("Model type not recognized");
            for(ProcessModel p:models) {
	            p.setProcessModelURI(selFile.getAbsolutePath());
	            pei.processModelOpened(p);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error importing file:\n"+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

	
}
