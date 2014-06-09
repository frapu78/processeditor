/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import java.io.File;
import java.util.List;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author fpu
 */
public interface Importer {
   
    /**
     * Imports a set of ProcessModels from a source.
     * @param uri
     * @return
     */
    public List<ProcessModel> parseSource(File f) throws Exception;

    /**
     * Returns the display name of this Importer.
     * @return
     */
    public String getDisplayName();

    /**
     * Returns the set of file types supported by the Importer.
     * @return
     */
    public String[] getFileTypes();

}
