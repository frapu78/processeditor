/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public class EmptyNodeOnSelectMenu extends NodeOnSelectMenuBasis {

	/**
	 * @param editor
	 */
	public EmptyNodeOnSelectMenu(ProcessEditor editor) {
		super(editor);		
	}

	@Override
	protected void buildMenu(ProcessNode node, ProcessEditor editor) {
		//nothing to do!
	}

}
