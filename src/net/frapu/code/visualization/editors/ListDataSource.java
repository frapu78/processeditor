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

import java.util.List;

/**
 *
 * Provides a DataSource with a list of Strings
 *
 * @author fpu
 */
public interface ListDataSource extends DataSource {

    /**
     * Returns the current data values
     * @return
     */
    public List<String> getListData();

}
