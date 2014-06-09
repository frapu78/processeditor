/**
 *
 * Process Editor - Core Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

/**
 *
 *  Provides a DataSource with a single String.
 *
 * @author fpu
 */
public interface DataSource {

    /**
     * Returns the data contained in this DataSource.
     * 
     * @return
     */
    public String getData();

}
