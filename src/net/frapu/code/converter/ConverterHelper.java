/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package net.frapu.code.converter;

import javax.swing.filechooser.FileFilter;

import com.inubit.research.ISConverter.exporter.ISBPDExporter;
import com.inubit.research.ISConverter.importer.ISDiagramImporter;
import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.server.ProcessEditorServerUtils;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessModelPreview;
import org.w3c.dom.Document;

/**
 *
 * @author fpu
 */
public class ConverterHelper {

	
	public static List<Exporter> getExporters() {
		List<Exporter> result = new LinkedList<Exporter>();
        // Add all default Exporters (for all model types)
        result.add(new ProcessEditorExporter());
        result.add(new PNGExporter());
        result.add(new PDFExporter());
        result.add(new XPDLExporter());
        result.add(new ISBPDExporter());        
        result.add(new LoLAExporter());
        result.add(new XSDExporter());
        result.add(new XSDCreator());
        return result;
	}
	
    /**
     * Returns all registered Exporters for a certain model type.
     * @param modelType
     * @return
     */
    public static List<Exporter> getExportersFor(Class<? extends ProcessModel> modelType) {
        List<Exporter> result = new LinkedList<Exporter>();
        for(Exporter ex:getExporters()) {
        	for(Class<? extends ProcessModel> cl:ex.getSupportedModels()) {
        		if(cl.isAssignableFrom(modelType)) {
        			result.add(ex);
        			break;
        		}        		
        	}
        }        
        return result;
    }

    /**
     * Returns FileFilters for all supported Exporters.
     * @param modelType
     * @return
     */
    public static List<FileFilter> getExporterFileFilters(Class<? extends ProcessModel> modelType) {
        List<FileFilter> result = new LinkedList<FileFilter>();

        for (final Exporter e : getExportersFor(modelType)) {
            javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {

                public String getDescription() {
                    String prefixes = "";
                    for (String ext2 : e.getFileTypes()) {
                        prefixes += "*." + ext2 + ";";
                    }
                    if (prefixes.length() > 0) {
                        prefixes = prefixes.substring(0, prefixes.length() - 1);
                    }

                    return e.getDisplayName() + " (" + prefixes + ")";
                }

                public boolean accept(java.io.File f) {
                    if (f.isDirectory()) {
                        return true;
                    }

                    for (String ext2 : e.getFileTypes()) {
                    	if(f.getName().endsWith(ext2)) {	                       
                                return true;                           
                        }
                    }
                    return false;
                }
            };
            result.add(ff);
        }

        return result;
    }

    /**
     * Returns all registered importers.
     * @param modelType
     * @return
     */
    public static List<Importer> getImporters() {
        List<Importer> result = new LinkedList<Importer>();
        // Add all Importers
        result.add(new PNMLImporter());
        // Add IS BPMN Importer
        result.add(new ISDiagramImporter());
        // Add XPDL Importer
        result.add(new XPDLImporter());
        // Add XML-Schema Importer
        result.add(new XSDImporter());

        // Add default Importer
        result.add(new ProcessEditorImporter());

        return result;
    }

    /**
     * Returns FileFilters for all Importers.
     * @param modelType
     * @return
     */
    public static List<FileFilter> getImporterFileFilters() {
        List<FileFilter> result = new LinkedList<FileFilter>();

        for (final Importer imp : getImporters()) {
            javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {

                public String getDescription() {
                    String prefixes = "";
                    for (String ext2 : imp.getFileTypes()) {
                        prefixes += "*." + ext2 + ";";
                    }
                    if (prefixes.length() > 0) {
                        prefixes = prefixes.substring(0, prefixes.length() - 1);
                    }

                    return imp.getDisplayName() + " (" + prefixes + ")";
                }

                public boolean accept(java.io.File f) {
                    if (f.isDirectory()) {
                        return true;
                    }

                    String ext = null;
                    String s = f.getName();
                    int i = s.lastIndexOf('.');
                    if (i > 0 && i < s.length() - 1) {
                        ext = s.substring(i + 1).toLowerCase();
                    }

                    for (String ext2 : imp.getFileTypes()) {
                        if (ext != null) {
                            if (ext.equals(ext2)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
            result.add(ff);
        }

        return result;
    }

    public static List<ProcessModel> importModels(URI uri) throws Exception {
        // Create temp file from URI
        File tmpFile = File.createTempFile("pe_", ".import");
        // Get content from URI and write in tmpFile
        XmlHttpRequest req = new XmlHttpRequest(uri);
        Document doc = req.executeGetRequest();
        FileWriter fw = new FileWriter(tmpFile);
        ProcessEditorServerUtils.writeXMLtoStream(fw, doc);
        fw.close();
        // Fetch result from file
        List<ProcessModel> result = importModels(tmpFile);
        // Delete temp file
        tmpFile.delete();
        // Return result
        return result;
    }
    
    /**
     * opens a FileOpenDialog and lets the user pick a file which
     * is supported by any of the available importers
	 * @return
	 */
	public static File pickFileForImport(File currentDirectory) {
		// Get file name
        String filename = java.io.File.separator + "model";
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(new java.io.File(filename));
        fc.setDialogTitle("Open/Import File");
        if(currentDirectory!=null){
            fc.setCurrentDirectory(currentDirectory);
        }

        // Add preview
        ProcessModelPreview previewer = new ProcessModelPreview(fc);
        fc.setAccessory(previewer);

        for (FileFilter ff : ConverterHelper.getImporterFileFilters()) {
            fc.addChoosableFileFilter(ff);
        }

        if (fc.showOpenDialog(null) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return null;
        }
        java.io.File selFile = fc.getSelectedFile();
        // Check if file has been selected
        return selFile;
	}
    
    public static List<ProcessModel> importModels(File selFile) throws Exception {
		List<ProcessModel> models = null;
		// Iterate over until matching file filter is found
		for (Importer importer : ConverterHelper.getImporters()) {
		    try {
		        // Try to import file
		        // Currently, we import only the first model found
                        System.out.println("Trying importer "+importer);

		        long start = System.currentTimeMillis();
		        models = importer.parseSource(selFile);
		        System.out.println("Model imported in: "+(System.currentTimeMillis()-start)+" ms.");
		        break;
		    } catch (UnsupportedFileTypeException e) {
		    }
		}
		return models;
	}
}
