/**
 *
 * Process Editor - Process Map Package
 *
 * (C) 2010 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.processmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.editors.ColorPropertyEditor;

/**
 *
 * @author fpu
 */
public class ProcessOwner extends ProcessNode {

    public static String PROP_HEADCOLOR = "head_color";
    public static String PROP_ISMANAGER = "is_manager";
    public static int DEFAULTHEADCOLOR = (new Color(255, 242, 242)).getRGB();

    public ProcessOwner() {
        super();
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(60, 60);
        setBackground(new Color(204, 204, 255));
        setProperty(PROP_HEADCOLOR, "" + DEFAULTHEADCOLOR);
        setPropertyEditor(PROP_HEADCOLOR, new ColorPropertyEditor());
        setProperty(PROP_ISMANAGER, TRUE);
        setPropertyEditor(PROP_ISMANAGER, new BooleanPropertyEditor());

    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.defaultStroke);

        Point p = getTopLeftPos();
        Dimension d = getSize();

        // Fill and draw body
        g2.setColor(getBackground());
        g2.fillArc(p.x, p.y + d.height / 2, d.width, d.height, 0, 180);
        g2.setPaint(Color.BLACK);
        g2.drawArc(p.x, p.y + d.height / 2, d.width, d.height, 0, 180);
        g2.drawLine(p.x, p.y + d.height, p.x + d.width, p.y + d.height);

        // Fill and draw head
        final int OFFSET = 10;
        try {
            g2.setColor(new Color(Integer.parseInt(getProperty(PROP_HEADCOLOR))));
        } catch (NumberFormatException e) {
            g2.setColor(new Color(DEFAULTHEADCOLOR));
        }
        g2.fillOval(p.x + OFFSET, p.y, d.width - 2 * OFFSET, d.height - 2 * OFFSET);
        g2.setPaint(Color.BLACK);
        g2.drawOval(p.x + OFFSET, p.y, d.width - 2 * OFFSET, d.height - 2 * OFFSET);

        // Draw tie if isManager
        if (isManager()) {
            final int TIEWIDTH = 5;
            g2.setColor(Color.BLACK);
            Path2D path = new Path2D.Double();
            path.moveTo(p.x+d.width/2, p.y+d.height - 2 * OFFSET);
            path.lineTo(p.x+d.width/2+TIEWIDTH, p.y+d.height - OFFSET);
            path.lineTo(p.x+d.width/2, p.y+d.height);
            path.lineTo(p.x+d.width/2-TIEWIDTH, p.y+d.height - OFFSET);
            path.closePath();
            g2.fill(path);
        }

        g2.setFont(ProcessMapUtils.defaultFont);
        // Draw title
        ProcessMapUtils.drawText(g2, getPos().x, p.y + d.height, (d.width) * 4, getText(), Orientation.TOP);

    }

    public boolean isManager() {
        return getProperty(PROP_ISMANAGER).equalsIgnoreCase(TRUE);
    }

    @Override
    public Rectangle getBoundingBox() {
        // Get bounds of text
        int x1 = getPos().x - getSize().width / 2;
        int x2 = getPos().x + getSize().width / 2;
        int y2 = getPos().y + getSize().height / 2;
        BufferedImage dummyImg = new BufferedImage(100, 50, BufferedImage.BITMASK);
        Graphics2D g2 = dummyImg.createGraphics();
        g2.setFont(ProcessMapUtils.defaultFont);
        Rectangle gfxBounds = super.getBoundingBox();
        Rectangle textBounds = new Rectangle(gfxBounds);
        if (getText() != null) {
            textBounds = ProcessMapUtils.drawText(g2, getPos().x, y2 + g2.getFont().getSize(), (x2 - x1) * 4, getText(), Orientation.TOP);
        }
        // Merge bounds
        gfxBounds.add(textBounds);
        return gfxBounds;
    }

    @Override
    protected Shape getOutlineShape() {
        return new Rectangle2D.Double(getTopLeftPos().x, getTopLeftPos().y, getSize().width, getSize().height);
    }
}
