/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.server.merger.ProcessObjectPropertyMerger;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 *
 * @author Uwe
 */
public class AttributeNameDisplay extends AttributeSelectionButton {

    public AttributeNameDisplay(ProcessEditor editor, String Attribute, ProcessObjectPropertyMerger parent) {
        super(editor, Attribute, parent);
        f_textOrientation = Orientation.CENTER;
        setText(parent.getPropertyName());
    }



    @Override
    public boolean isHighlighted() {
        return false;
    }

    @Override
    public boolean isChosen() {
        return false;
    }

    @Override
    public boolean isConflicting() {
        return false;
    }






}
