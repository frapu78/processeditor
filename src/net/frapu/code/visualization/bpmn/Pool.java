/**
 *
 * Process Editor - BPMN Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public class Pool extends LaneableCluster {

    /** Property if the Pool is a Black Box Pool */
    public final static String PROP_BLACKBOX_POOL = "blackbox_pool";
    private static final int LOOP_SPACE_TO_BORDER = 3;
    private static final int LOOP_ICON_SIZE = 10;

    /** Property if this Pool has multiple instances (0=FALSE, 1=TRUE) */
    public final static String PROP_MULTI_INSTANCE = "multi_instance";
   
    public Pool() {
        super();
        init();
        setText("Pool");
    }

    public Pool(int x, int y, String label) {
        super();
        init();
        setPos(x, y);
        setText(label);
    }

    @Override
    protected void init() {
    	super.init();
        int w = 400;
        int h = 100;
        setSize(w, h);
        setProperty(PROP_BLACKBOX_POOL, FALSE);
        setPropertyEditor(PROP_BLACKBOX_POOL, new BooleanPropertyEditor());
        setProperty(PROP_MULTI_INSTANCE, FALSE);
        setPropertyEditor(PROP_MULTI_INSTANCE, new BooleanPropertyEditor());
    }

    @Override
    public void setProperty(String key, String value) {
        if (key.equals(PROP_HEIGHT)) {
            try {
                int height = Integer.parseInt(value);
                if (height<30) value = "30";
            } catch (Exception e) {
                value = "150";
            }
        }
        if (key.equals(PROP_WIDTH)) {
            try {
                int width = Integer.parseInt(value);
                if (width<50) value = "50";
            } catch (Exception e) {
                value = "300";
            }
        }
        super.setProperty(key, value);
    }

    @Override
    public Point getConnectionPoint(Point to) {
        // Check if toX is with pool size
        if (to.x >= (getPos().x - getSize().width / 2) && to.x <= (getPos().x + getSize().width / 2)) {
            int newY = getPos().y - getSize().height / 2;
            // Detect upper or lower y
            if (to.y > getPos().y) {
                newY = getPos().y + getSize().height / 2;
            }

            return new Point(to.x, newY);
        }

        return super.getConnectionPoint(to);
    }

    public void paintInternal(Graphics g) {
        drawPool(g);
    }

    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }

    /**
     * Returns whether a certain ProcessNode is graphically contained or not.
     */
    @Override
    public boolean isContainedGraphically(List<ProcessNode> nodes, ProcessNode node, boolean onTopRequired) {
        if (node == null) {
            return false;
        }
        // A black box pool can never contain nodes
        if (getProperty(PROP_BLACKBOX_POOL).equals(TRUE)) {
            return false;
        }
        return super.isContainedGraphically(nodes, node,onTopRequired);
    }

    private void drawPool(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        
        g2.setStroke(BPMNUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);
        
        // Draw Lanes
        if (!isCollapsed())
            super.drawLanes(g2);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);
     

        // Draw Black Box Pool
        if (isCollapsed()) {
            g2.setFont(BPMNUtils.defaultFont.deriveFont(14.0f));
            g2.setPaint(Color.BLACK);
            BPMNUtils.drawText(g2, getPos().x, getPos().y - 4, getSize().width - 8, getText(), BPMNUtils.Orientation.CENTER);
        } else {
            // Draw default pool        	
        	if(isVertical()) {
        		int _x1 = getPos().x - (getSize().width / 2);
            	int _y1 = getPos().y - (getSize().height) / 2 + POOL_LABEL_WIDTH;
            	int _x2 = getPos().x + (getSize().width / 2);
            	int _y2 = getPos().y - (getSize().height) / 2  + POOL_LABEL_WIDTH;
        		g2.drawLine(_x1,_y1,_x2,_y2);	
        	}else {
        		int _x1 = getPos().x - (getSize().width / 2) + POOL_LABEL_WIDTH;
            	int _y1 = getPos().y - (getSize().height) / 2;
            	int _x2 = getPos().x - (getSize().width / 2) + POOL_LABEL_WIDTH;
            	int _y2 = getPos().y + (getSize().height) / 2;
        		g2.drawLine(_x1,_y1,_x2,_y2);	
        	} 
            // Set font
            g2.setFont(BPMNUtils.defaultFont.deriveFont(14.0f));

            // Draw text
            if (getText() != null) {
            	if(isVertical()) {
            		BPMNUtils.drawText(g2,            				
            				getPos().x,
            				getPos().y - (getSize().height / 2) -6,
                            getSize().height,
                            getText(),
                            BPMNUtils.Orientation.TOP);
            	}else {
            		BPMNUtils.drawTextVertical(g2, 
            				getPos().x - (getSize().width / 2) + POOL_LABEL_WIDTH-3,
                            getPos().y,
                            getSize().height,
                            getText(),
                            BPMNUtils.Orientation.TOP);
            	}
                
            }            
        }

        // Check Multi-Instance
        if (getProperty(PROP_MULTI_INSTANCE).equals(TRUE))
                drawMultiInstance(g2);
        
    }

    /**
     * @param g2
     */
    protected void drawMultiInstance(Graphics2D g2) {
        g2.setStroke(BPMNUtils.defaultStroke);
        Point pos = this.getPos();
        pos.y += this.getSize().height / 2 - LOOP_SPACE_TO_BORDER - LOOP_ICON_SIZE;
        pos.x -= LOOP_ICON_SIZE / 2;
        //drawing 3 small lines
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
        pos.x += LOOP_ICON_SIZE / 2;
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
        pos.x += LOOP_ICON_SIZE / 2;
        g2.drawLine(pos.x, pos.y, pos.x, pos.y + LOOP_ICON_SIZE);
    }

    @Override
    public boolean isCollapsed() {
        return TRUE.equalsIgnoreCase(getProperty(PROP_BLACKBOX_POOL));
    }

    public String toString() {
        return "BPMN Pool ("+getName()+")";
    }
}
