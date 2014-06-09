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
import java.util.Set;
import net.frapu.code.visualization.ProcessModel;

/**
 *
 * @author fpu
 */
public interface Exporter {

    /**
     * Serializes a ProcessModel to a file. 
     * @param f
     * @param m
     * @throws java.lang.Exception
     */
    public void serialize(File f, ProcessModel m) throws Exception;

    /**
     * Returns a set of supported ProcessModell classes.
     * @return
     */
    public Set<Class<? extends ProcessModel>>getSupportedModels();

    /**
     * Returns the display name of this Exporter.
     * @return
     */
    public String getDisplayName();

    /**
     * Returns the set of file types supported by the Exporter.
     * @return
     */
    public String[] getFileTypes();

}
