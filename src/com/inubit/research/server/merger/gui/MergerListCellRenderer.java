/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger.gui;

import com.inubit.research.server.merger.ProcessObjectMerger;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Uwe
 */
public class MergerListCellRenderer extends JLabel implements ListCellRenderer {

    public MergerListCellRenderer() {
        setOpaque(true);
    }

    @SuppressWarnings("empty-statement")
    public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        setText(value.toString());

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            background = Color.BLUE;
            foreground = Color.WHITE;

            // check if this cell is selected
        } else if (isSelected) {
            background = Color.BLUE;
            foreground = Color.WHITE;

            // unselected, and not the DnD drop location
        } else if (value instanceof ProcessObjectMerger){
            ProcessObjectMerger merger = (ProcessObjectMerger) value;
            background = merger.getStateColor();
            foreground = Color.BLACK;
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }

        setBackground(background);
        setForeground(foreground);

        return this;
    }
}


