/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.helper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * Provides a Ruler to show the size of a ProcessNode.
 *
 * @author fpu
 */
public class Ruler extends ProcessHelper {

    protected ProcessNode node;
    protected boolean showHorizontal = true;
    protected boolean showVertical = true;
    /** The property for the background color */
    public String PROP_COLOR = "color";
    /** The distance of the Ruler from the ProcessObject */
    public int DISTANCE = 25;

    /**
     * Creates a new Ruler for a ProcessNode.
     * @param node
     */
    public Ruler(ProcessNode node) {
        this.node = node;
        setProperty(PROP_COLOR, ""+ProcessUtils.RULERCOLOR.getRGB());
    }

    @Override
    public Object clone() {
        // @todo: Implement clone()
       return null;
    }

    public boolean isShowHorizontal() {
        return showHorizontal;
    }

    public void setShowHorizontal(boolean showHorizontal) {
        this.showHorizontal = showHorizontal;
    }

    public boolean isShowVertical() {
        return showVertical;
    }

    public void setShowVertical(boolean showVertical) {
        this.showVertical = showVertical;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(getColor());
        g2.setStroke(ProcessUtils.defaultStroke);
        g2.setFont(new Font("Arial Bold", Font.BOLD, 12));
        int x1 = node.getPos().x-node.getSize().width/2;
        int x2 = node.getPos().x+node.getSize().width/2;
        int y1 = node.getPos().y-node.getSize().height/2;
        int y2 = node.getPos().y+node.getSize().height/2;

        final int ARROWSIZE = 5;

        if (isShowHorizontal()) {
            // Draw horizontal ruler on ProcessObject            
            g2.drawLine(x1,y2,x1,y2+DISTANCE);
            g2.drawLine(x2,y2,x2,y2+DISTANCE);
            g2.drawLine(x1,y2+DISTANCE,x2,y2+DISTANCE);            
            g2.drawLine(x1,y2+DISTANCE,x1+ARROWSIZE,y2+DISTANCE-ARROWSIZE);
            g2.drawLine(x1,y2+DISTANCE,x1+ARROWSIZE,y2+DISTANCE+ARROWSIZE);
            g2.drawLine(x2,y2+DISTANCE,x2-ARROWSIZE,y2+DISTANCE-ARROWSIZE);
            g2.drawLine(x2,y2+DISTANCE,x2-ARROWSIZE,y2+DISTANCE+ARROWSIZE);
            ProcessUtils.drawText(g2, node.getPos().x, y2+DISTANCE-g2.getFont().getSize(),
                    node.getSize().width, ""+node.getSize().width+"px", ProcessUtils.Orientation.CENTER);
        }

        if (isShowVertical()) {
            // Draw vertical ruler on ProcessObject
            g2.drawLine(x2,y1,x2+DISTANCE,y1);
            g2.drawLine(x2,y2,x2+DISTANCE,y2);
            g2.drawLine(x2+DISTANCE,y1,x2+DISTANCE,y2);
            g2.drawLine(x2+DISTANCE,y1,x2+DISTANCE+ARROWSIZE,y1+ARROWSIZE);
            g2.drawLine(x2+DISTANCE,y1,x2+DISTANCE-ARROWSIZE,y1+ARROWSIZE);
            g2.drawLine(x2+DISTANCE,y2,x2+DISTANCE+ARROWSIZE,y2-ARROWSIZE);
            g2.drawLine(x2+DISTANCE,y2,x2+DISTANCE-ARROWSIZE,y2-ARROWSIZE);
            ProcessUtils.drawTextVertical(g2, x2+DISTANCE-g2.getFont().getSize()/2, node.getPos().y,
                    node.getSize().width, ""+node.getSize().height+"px", ProcessUtils.Orientation.CENTER);
        }
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
