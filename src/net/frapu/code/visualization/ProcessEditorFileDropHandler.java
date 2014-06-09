/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import net.frapu.code.converter.ConverterHelper;

/**
 * so file can just be dropped into a ProcessEditor
 * could also be used for a workbench, to add new models via drag and drop
 * @author ff
 *
 */
public class ProcessEditorFileDropHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4514371928243021920L;
	private static ProcessEditorFileDropHandler f_instance = new ProcessEditorFileDropHandler();
	
	private ProcessEditorFileDropHandler() {
		//make constructor private!		
	}
	
	public static ProcessEditorFileDropHandler getInstance() {
		return f_instance;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		// Make sure we have the right starting points
		if (!(comp instanceof ProcessEditor)) {
			return false;
		}
		if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		// Grab the tree, its model and the root node
		ProcessEditor _editor = (ProcessEditor) comp;
		try {
			List<File> _data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
			for(File f:_data) {
				try {
					List<ProcessModel> _models =  ConverterHelper.importModels(f);
					if(_models.size() > 0) {
						_editor.setModel(_models.get(0));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		} catch (UnsupportedFlavorException ufe) {
			System.err.println("Ack! we should not be here.\nBad Flavor.");
		} catch (IOException ioe) {
			System.out.println("Something failed during import:\n" + ioe);
		}
		return false;
	}
	
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		 if (comp instanceof ProcessEditor) {
	        for (int i = 0; i < transferFlavors.length; i++) {
	          if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
	            return false;
	          }
	        }
	        return true;
	      }
	      return false;
	}
}
