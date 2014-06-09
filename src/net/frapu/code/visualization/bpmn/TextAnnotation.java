/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.editors.ColorPropertyEditor;

/**
 * @author ff
 *
 */
public class TextAnnotation extends Artifact {

	
    /** The font size */
    public final static String PROP_FONTSIZE = "font_size";
    /** The font style (see java.awt.Font) for values */
    public final static String PROP_FONTSTYLE = "font_style";

    public TextAnnotation() {
        super();
        initializeProperties();
    }

    private void initializeProperties() {
        setSize(120,70);
        setProperty(PROP_FONTSIZE, "10");
        setProperty(PROP_FONTSTYLE, ""+Font.BOLD);
        setProperty(PROP_BACKGROUND, ""+ProcessUtils.commentColor.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), this.getSize().width, this.getSize().height);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        Color backgroundColor = Color.YELLOW;
        try {
            backgroundColor =
                new Color(Integer.parseInt(this.getProperty(PROP_BACKGROUND)));
            } catch (NumberFormatException e) {}

        g2.setPaint(backgroundColor);
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        Point corner = this.getPos();
        corner.x -= (this.getSize().width / 2);
        corner.y -= (this.getSize().height / 2);
        g2.drawLine(corner.x, corner.y, corner.x, corner.y + this.getSize().height);
        g2.drawLine(corner.x, corner.y, corner.x + (int) (0.2 * this.getSize().width), corner.y);
        g2.drawLine(corner.x, corner.y + this.getSize().height, corner.x + (int) (0.2 * this.getSize().width), corner.y + this.getSize().height);


        // Set font size
        int fontSize = BPMNUtils.defaultFont.getSize();
        try {
            fontSize = Integer.parseInt(this.getProperty(PROP_FONTSIZE));
        } catch (NumberFormatException e) {}

        int fontStyle = Font.PLAIN;
        try {
            fontStyle = Integer.parseInt(this.getProperty(PROP_FONTSTYLE));
        } catch (NumberFormatException e) {}

        // Set font
        g2.setFont(BPMNUtils.defaultFont.deriveFont(fontStyle, (float)fontSize));
        
        BPMNUtils.drawFitText(g2, getPos().x, getPos().y - getSize().height / 2 + 4, getSize().width - 8, getSize().height - 8, getText());
    }
}
