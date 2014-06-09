/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.twf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class ToolDocker extends ProcessNode {

	public final double DEFAULT_RADIUS =8.0;
	private Tool f_parent;
	
	public static String PROP_LEFT_DOCKER = "#DockerIsLeft";
	public static String PROP_PARENT_ID = "#ParentToolID";
	
	
	
	/**
	 * for serialization
	 */
	public ToolDocker() {
		f_parent = null;
		setLeft(false);
	}
	
	@Override
	public void addContext(ProcessModel context) {
		super.addContext(context);
		if(f_parent == null) {
			f_parent = (Tool) context.getNodeById(getProperty(PROP_PARENT_ID));
			if(f_parent != null) // can happen with legacy models
				f_parent.setToolDocker(this, getLeft());
		}
	}
	
	/**
	 * 
	 */
	public ToolDocker(Tool parent,boolean left) {
		f_parent = parent;
		setProperty(PROP_PARENT_ID, f_parent.getId());
		setLeft(left);
		setProperty(PROP_WIDTH, ""+(int)(2*DEFAULT_RADIUS));
		setProperty(PROP_HEIGHT,""+ (int)(2*DEFAULT_RADIUS));
	}
	
	/**
	 * @param left
	 */
	public void setLeft(boolean left) {
		setProperty(PROP_LEFT_DOCKER, left ? TRUE : FALSE);
	}
	
	public boolean getLeft() {
		return getProperty(PROP_LEFT_DOCKER).equals(TRUE);
	}
	
	@Override
	protected Shape getOutlineShape() {
		Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
	}

	@Override
	protected void paintInternal(Graphics g) {
		drawDocker((Graphics2D)g,DEFAULT_RADIUS);
	}
	
	private void drawDocker(Graphics2D g2,double radius) {
		updatePos();
        g2.setStroke(ProcessUtils.defaultStroke);
        Point pos = getPos();
        Shape outline = new Ellipse2D.Double(pos.x-radius, pos.y-radius, radius*2, radius*2);

        g2.setPaint(Color.GRAY);
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);
    }
	
	/**
	 * 
	 */
	private void updatePos() {
		if(f_parent != null) {
			Point _pos = f_parent.getPos();
			if(getLeft())
				_pos.x -= f_parent.getSize().width/2;
			else
				_pos.x += f_parent.getSize().width/2;
			setProperty(PROP_XPOS,""+_pos.x);
			setProperty(PROP_YPOS,""+_pos.y);
		}
	}

	@Override
	public void setPos(int x, int y) {
		//not possible
		return;
	}
	
	@Override
	public void setSize(int w, int h) {
		//not possible
	}
	
	@Override
	public Set<Point> getDefaultConnectionPoints() {
		 HashSet<Point> cp = new HashSet<Point>();
        // Calculate default connection points
        cp.add(new Point(0-(getSize().width/2), 0));
        cp.add(new Point((getSize().width/2), 0));
        return cp;
	}

	public Tool getParent() {
		return f_parent;
	}
	
}
