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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.helper.PEButton;
import net.frapu.code.visualization.helper.PEButtonListener;

/**
 *
 * @author Uwe
 */
public class AttributeSelectionButton extends PEButton implements PEButtonListener {

    private String AttributeName;
    private String Attribute;
    public static int ATTRIBUTE_HEIGHT = 20;
    public static int ATTRIBUTE_WIDTH = 60;
    public static Color selectionColor = Color.YELLOW;
    public static Color conflictColor = Color.RED;
    private boolean chosen = false;
    private ProcessObjectPropertyMerger parent;

    public AttributeSelectionButton(ProcessEditor editor, String Attribute, ProcessObjectPropertyMerger parent) {
        super(editor);
        this.Attribute = Attribute;
        this.AttributeName = parent.getPropertyName();
        this.parent = parent;
        this.setSize(ATTRIBUTE_WIDTH, ATTRIBUTE_HEIGHT);
        this.setText(Attribute);
        if (isConflicting()) {
            chosen=false;
        } else {
            chosen=parent.getMergedValue().equals(Attribute);
        }
        this.addListener(this);
    }

    public String getAttribute() {
        return Attribute;
    }

    public String getAttributeName() {
        return AttributeName;
    }

    public boolean isConflicting() {
        return parent.isConflict();
    }

    public void setConflicting(boolean conflicting) {
        this.parent.setConflict(conflicting);


    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean choosen) {
        this.chosen = choosen;
        if (choosen) {
            parent.setMergedValue(Attribute);
        } else {
            if (parent.getSourceValue().equals(Attribute)) {
                parent.setMergedValue(Attribute);
            } else {
                parent.setMergedValue(parent.getTargetValue());
            }
        }
    }

    @Override
    public void paint(Graphics gra) {
        Graphics2D g = (Graphics2D) gra;
        if (!isVisible()) {
            return;
        }

        g.setStroke(f_lineStroke);
        //inner rectangle------------------------------------------------------
        g.setColor(new Color(240, 240, 240));
        if (isChosen()) {
            g.setColor(selectionColor);
        }
        if (isConflicting()) {
            g.setColor(conflictColor);
        }
        if (isHighlighted()) {
            g.setColor(new Color(230, 210, 140));
        }
        g.fillRoundRect(f_position.x, f_position.y,
                f_size.width, f_size.height, 5, 5);
        //smaller inner rectangle (for nice 3d-like effects)-------------------

        g.setColor(new Color(224, 224, 244));
        if (isChosen()) {
            g.setColor(selectionColor);
        }
        if (isConflicting()) {
            g.setColor(conflictColor);
        }
        if (isHighlighted()) {
            g.setColor(new Color(246, 226, 136));
        }
        
        final int BORDER = 2;
        g.fillRect(f_position.x + BORDER, f_position.y + f_size.height / 2, f_size.width - 2 * BORDER, f_size.height / 2 - BORDER);

        //drawing a border-----------------------------------------------------
        g.setColor(new Color(154, 154, 154));
        g.drawRoundRect(f_position.x, f_position.y,
                f_size.width, f_size.height, 5, 5);

        //drawing image and/or text--------------------------------------------
        g.setColor(new Color(100, 100, 100));
        if (f_image != null) {
            //used to center image
            int _w = f_centerImage ? (f_size.width - f_image.getWidth(null)) / 2 : 1;
            int _h = (f_size.height - f_image.getHeight(null)) / 2;
            g.drawImage(f_image, f_position.x + _w, f_position.y + _h, null);
            if (f_text != null) {
                int _offsetX = f_textOrientation == Orientation.CENTER ? ((f_size.width - f_image.getWidth(null) - 2) / 2) : 0;
                int _offsetY = f_textOrientation == Orientation.CENTER ? (f_size.height / 2) : 0;
                ProcessUtils.drawText(g,
                        f_position.x + f_image.getWidth(null) + _offsetX,
                        f_position.y + _offsetY, (f_size.width - f_image.getWidth(null)) - 2, f_text, f_textOrientation);
            }
        } else {
            if (f_text != null) {
                int _offsetX = f_textOrientation == Orientation.CENTER ? (f_size.width) / 2 : 0;
                int _offsetY = f_textOrientation == Orientation.CENTER ? (f_size.height / 2) : 0;
                ProcessUtils.drawText(g, f_position.x + _offsetX, f_position.y + _offsetY, f_size.width - 2, f_text, f_textOrientation);
            }
        }
    }

    public void buttonMouseIn(PEButton button) {

    }

    public void buttonMouseOut(PEButton button) {

    }

    public void buttonClicked(PEButton button) {
        setChosen(true);

    }
}
