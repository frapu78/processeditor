/**
 *
 * Process Editor - Domain Package
 *
 * (C) 2010 inubit AG
 * (C) 2014 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.domainModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Set;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 *
 * @author frank
 */
public class Comment extends ProcessNode {

	private static final int EDGE_SIZE = 15;
     
	
	public Comment() {
        super();
        initializeProperties();
    }

    public Comment(String name) {
        super();
        initializeProperties();
        setText(name);
    }

    protected void initializeProperties() {
        setSize(100, 66);
        setText("Comment");
        setBackground(ProcessUtils.commentColor);
    }

    @Override
    protected Shape getOutlineShape() {
    	// Clone dataObject
    	Point _pos = getPos();
    	Dimension _d = getSize();
    	int _up = _pos.y - _d.height/2;
    	int _down = _pos.y + _d.height/2;
    	int _left = _pos.x - _d.width/2;
    	int _right = _pos.x + _d.width/2;
    	
    	int[] _yObjectPoints = {_down,_down,_up,_up,_up+EDGE_SIZE};
        int[] _xObjectPoints = {_right,_left,_left,_right-EDGE_SIZE,_right};
       
        Polygon _shape = new Polygon(_xObjectPoints, _yObjectPoints, 5);
        return _shape;
    }

    @Override
    public void setSize(int w, int h) {
        if (w < 50 | h < 50) {
            return;
        }
        super.setSize(w, h);
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(ProcessUtils.extraBoldStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(new Color(200, 200, 200));
        g2.draw(outline);
        
        Point _pos = getPos();
    	Dimension _d = getSize();
        int _up = _pos.y - _d.height/2;
    	int _right = _pos.x + _d.width/2;
    	
        g2.drawLine(_right-EDGE_SIZE, _up, _right-EDGE_SIZE, _up+EDGE_SIZE);
        g2.drawLine(_right, _up+EDGE_SIZE, _right-EDGE_SIZE, _up+EDGE_SIZE);    
        
        // Text
        ProcessUtils.drawText(g2, getPos().x, getPos().y, getSize().width - 8, getText(),
                ProcessUtils.Orientation.CENTER,false,true);
    }

    @Override
    public Set<Point> getDefaultConnectionPoints() {
    	Set<Point> points = super.getDefaultConnectionPoints();    	
    	return points;
    }
}
