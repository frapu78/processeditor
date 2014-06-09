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
public class ToolErrorConnector extends ProcessNode {

	private static final int DIST_X = 4;
    private static final int DIST_Y = 3;
    private static final int BUTTON_WIDTH = 25;
    public static final int AREA_HEIGHT = 20;
	public static String PROP_PARENT_ID = "#ParentToolID";
	private Tool f_parent;
	private String PROP_NUMBER = "#ConnectorNumber";
	
	/**
	 * for serialization
	 */
	public ToolErrorConnector() {
		f_parent = null;
		setNumber(0);
	}
	
	public Tool getParent() {
		return f_parent;
	}
	
	/**
	 * @param tool
	 */
	public ToolErrorConnector(Tool tool, int number) {
		f_parent = tool;
		setProperty(PROP_PARENT_ID, f_parent.getId());
		setNumber(number);
	}
	
	@Override
	public void addContext(ProcessModel context) {
		super.addContext(context);
		if(f_parent == null) {
			f_parent = (Tool) context.getNodeById(getProperty(PROP_PARENT_ID));
			if(f_parent != null)//can happen with legacy models
					f_parent.setErrorConnector(this,getNumber());
		}
	}

	@Override
	protected Shape getOutlineShape() {
		Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
	}

	@Override
	protected void paintInternal(Graphics g) {
		updatePosAndSize();
		Graphics2D g2 = (Graphics2D) g;		
		g2.setStroke(ProcessUtils.defaultStroke);
	    g2.setColor(Color.WHITE);
	    g2.fillRect(getPos().x-getSize().width/2, getPos().y-getSize().height/2, getSize().width, getSize().height);
	    g2.setColor(Color.BLACK);
	    g2.drawRect(getPos().x-getSize().width/2, getPos().y-getSize().height/2, getSize().width, getSize().height);
	    
	}

	
	/**
	 * @param left
	 */
	public void setNumber(int number) {
		setProperty(PROP_NUMBER , ""+number);
	}
	
	public int getNumber() {
		try {
			return Integer.parseInt(getProperty(PROP_NUMBER));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}
	/**
	 * 
	 */
	private void updatePosAndSize() {
		if(f_parent != null) {
			Point _tlPos = new Point(f_parent.getPos().x-f_parent.getSize().width/2,
					f_parent.getPos().y+f_parent.getSize().height/2-AREA_HEIGHT);
			_tlPos.x += ((getNumber()+0.5)*BUTTON_WIDTH) + (getNumber()+1)*DIST_X;
			_tlPos.y += AREA_HEIGHT/2;
			setPos(_tlPos);
			setSize(BUTTON_WIDTH, AREA_HEIGHT-2*DIST_Y);
		}
	}
	
	@Override
	public Set<Point> getDefaultConnectionPoints() {
		HashSet<Point> cp = new HashSet<Point>();
        cp.add(new Point(0, (getSize().height/2)));
        return cp;
	}

}
