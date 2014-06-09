/**
 *
 * Process Editor - Editor Package
 *
 * (C) 2008-2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.editors;

import net.frapu.code.visualization.*;

/**
 *
 * @author fpu
 */
public interface PropertyEditorListener {

    public void propertyChanged(ProcessObject obj, String key, String newValue);

}
