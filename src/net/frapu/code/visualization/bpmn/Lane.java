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
import java.util.LinkedList;
import net.frapu.code.visualization.Linkable;
import net.frapu.code.visualization.editors.ReferenceChooserRestriction;
import net.frapu.code.visualization.orgChart.ManagerialRole;
import net.frapu.code.visualization.orgChart.Person;
import net.frapu.code.visualization.orgChart.Role;

/**
 * @author ff, frapu
 *
 */
public class Lane extends LaneableCluster implements Linkable {

    private LaneableCluster parent;

    private static ReferenceChooserRestriction restrictions;

    /**
     * needed for deserialization
     */
    public Lane() {
        this("", 100, null);
    }

    /**
     * @param string
     * @param integer
     */
    public Lane(String name, Integer size, LaneableCluster parent) {
    	init();
        setText(name);
        if (size != null) {
            setSize(100, size);
        } else {
            setSize(100, 100);
        }
        this.parent = parent;
    }

    /**
     * @param g2
     * @param lane
     * @param lane2
     * @param remainingSpace
     */
    public void draw(Graphics2D g2, int x, int y, int remainingSpace) {
        //enough size left?
        if (remainingSpace >= 0) {
            int drawSize = isVertical() ? this.getSize().width : this.getSize().height;
            if (remainingSpace < drawSize) {
                drawSize = remainingSpace;
            }
            drawLane(g2, x, y, drawSize);
        }
    }

    private void drawLane(Graphics2D g2, int x, int y, int drawSize) {
        g2.setColor(getBackground());
        if(isVertical()) {
        	g2.fillRect(x+2, y+2, drawSize-4,getSize().height-4);	
        }else {
        	g2.fillRect(x+2, y+2, getSize().width-4, drawSize-4);
        }
        g2.setColor(Color.BLACK);
    	
    	if (getLanes().size() > 0) {
    		if(isVertical()) {
	            g2.drawLine(x,
	                    y + Pool.POOL_LABEL_WIDTH,
	                    x + drawSize,
	                    y + Pool.POOL_LABEL_WIDTH);
    		}else {
    			g2.drawLine(x + Pool.POOL_LABEL_WIDTH,
	                    y,
	                    x + Pool.POOL_LABEL_WIDTH,
	                    y + drawSize);
    		}
        }
    	if(isVertical()) {
	        g2.drawLine(x+ drawSize,
	                y ,
	                x + drawSize,
	                y + (int) (parent.getSize().getHeight()) - Pool.POOL_LABEL_WIDTH);
    	}else {
    	    g2.drawLine(x,
	                y + drawSize,
	                x + (int) (parent.getSize().getWidth()) - Pool.POOL_LABEL_WIDTH,
	                y + drawSize);
    	}
        //setting properties directly so that setSize (below) does not influence the position of contained nodes
        if(isVertical()) {
	    	setProperty(PROP_YPOS, ""+((int) (y + ((parent.getSize().getHeight()) - Pool.POOL_LABEL_WIDTH) / 2)));
	        setProperty(PROP_XPOS, ""+(x + drawSize / 2));
	        super.setSize(drawSize, (int) (parent.getSize().getHeight()) - Pool.POOL_LABEL_WIDTH);
        }else {
        	setProperty(PROP_XPOS, ""+((int) (x + ((parent.getSize().getWidth()) - Pool.POOL_LABEL_WIDTH) / 2)));
            setProperty(PROP_YPOS, ""+(y + drawSize / 2));
            super.setSize((int) (parent.getSize().getWidth()) - Pool.POOL_LABEL_WIDTH, drawSize);
        }
        if(isVertical()) {
        	BPMNUtils.drawText(g2, 
        			x + drawSize / 2,	
        			y ,
	                drawSize,
	                getText(),
	                BPMNUtils.Orientation.TOP);
        	
        }else {
	        BPMNUtils.drawTextVertical(g2, 
	        		x + Pool.POOL_LABEL_WIDTH - 3,
	                y + drawSize / 2,
	                drawSize,
	                getText(),
	                BPMNUtils.Orientation.TOP);
        }
        super.drawLanes(g2);
    }

    @Override
    protected void paintInternal(Graphics g) {
       //do nothing here, someone else paints us!
    }

    /**
     * @param g2
     * @param lane
     * @param lane2
     * @param remainingSpace
     */
    public void drawLast(Graphics2D g2, int movingCoord, int fixed, int remainingSpace) {
        if (remainingSpace >= 0) {
            int drawSize = remainingSpace;
            drawLane(g2, movingCoord, fixed, drawSize);
        }
    }

    /**
     * returns the height of the lane
     * @return
     */
    public int getLaneSize() {
        return isVertical() ? super.getSize().width : super.getSize().height;
    }

    @Override
    public void setSize(int w, int h) {
        //lanes have to be resized differently
    	if(isVertical()) {
    		int neww = this.getSize().width + (w - this.getSize().width) / 2;
            super.setSize(neww,this.getSize().height);	
    	}else {
	        int newh = this.getSize().height + (h - this.getSize().height) / 2;
	        super.setSize(this.getSize().width, newh);
    	}
    }
    
    @Override
    public void setProperty(String key, String value) {
    	if(PROP_SHADOW.equals(key)) {
    		return; //Lanes cannot have shadows as the are not graphical objects
    	}
    	super.setProperty(key, value);
    }

    @Override
    public synchronized void setPos(int x, int y) {
        //not possible
    }

    /**
     *
     */
    public LaneableCluster getParent() {
        return parent;
    }

    public Pool getSurroundingPool() {
        if ( this.getParent() != null ) {
            if ( this.getParent() instanceof Pool )
                return (Pool) this.getParent();
            else if ( this.getParent() instanceof Lane )
                return ((Lane) this.getParent()).getSurroundingPool();
        }
        
        return null;
    }

    /**
     * switches the parent of this Lane.
     * The old and new parent will get notified of the change
     * @param parent
     */
    public void setParent(LaneableCluster parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean isVertical() {
    	//always the same as the parent
    	if(parent != null) {
    		return parent.isVertical();
    	}
    	return super.isVertical();
    }

    public ReferenceChooserRestriction getReferenceRestrictions() {
        if ( restrictions == null ) {
            LinkedList<Class> classes = new LinkedList<Class>();
            classes.add(Role.class);
            classes.add(ManagerialRole.class);
            classes.add(Person.class);
            restrictions = new ReferenceChooserRestriction(null, classes);
        }

        return restrictions;
    }
}
