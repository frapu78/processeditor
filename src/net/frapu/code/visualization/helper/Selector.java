package net.frapu.code.visualization.helper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author fpu
 */
public class Selector extends ProcessHelper {

    private ProcessNode node;
    /** The property for the background color */
    public String PROP_COLOR = "color";

    public Selector(ProcessNode node) {
        this.node = node;
        setProperty(PROP_COLOR, ""+new Color(255,100,100).getRGB());
    }

    public ProcessNode getNode() {
        return node;
    }

    public void setNode(ProcessNode node) {
        this.node = node;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setStroke(ProcessUtils.defaultStroke);
        g2.setPaint(getColor());

        Point p = node.getTopLeftPos();
        Dimension d = node.getSize();

        g2.drawRect(p.x-10,p.y-10, d.width+20, d.height+20);

    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    public Color getColor() {
        return getColor(getProperty(PROP_COLOR));
    }

    protected Color getColor(String colorPropertyValue) {
        Color c = Color.BLACK;
        try {
            c = new Color(Integer.parseInt(colorPropertyValue));
        } catch (Exception e) {}

        return c;
    }

}
