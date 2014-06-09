/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.orgChart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public abstract class OrgChartElement extends ProcessNode {

	
	private Stroke f_linestroke = OrgChartUtils.defaultStroke;
	private boolean f_hasLine = false;
	private static final int LINE_DISTANCE_LEFT = 10;
	
	/**
	 * @return the f_linestroke
	 */
	public Stroke getLinestroke() {
		return f_linestroke;
	}
	/**
	 * @return the f_hasLine
	 */
	public boolean hasLine() {
		return f_hasLine;
	}
	/**
	 * @param f_linestroke the f_linestroke to set
	 */
	public void setLinestroke(Stroke linestroke) {
		this.f_linestroke = linestroke;
	}

	/**
	 * @param line the f_hasLine to set
	 */
	public void setHasLine(boolean line) {
		f_hasLine = line;
	}

	
	/**
	 * 
	 */
	public OrgChartElement() {
		setBackground(new Color(255,204,51)); //IS color
		setSize(100, 30);
	}
	
	
	
	@Override
	protected Shape getOutlineShape() {
		Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
	}
	
	@Override
	protected void paintInternal(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(OrgChartUtils.defaultStroke);
        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);
        
        if(f_hasLine) {
        	g2.setStroke(f_linestroke);
	        Dimension _size = getSize();
	        Point _start = getPos();
	        _start.y -= _size.height/2;
	        _start.x -= _size.getWidth()/2 - LINE_DISTANCE_LEFT;
	        g2.drawLine(_start.x, _start.y, _start.x, _start.y + (int)_size.getHeight());
	        g2.setStroke(OrgChartUtils.defaultStroke);
        }       
        
        g2.setFont(OrgChartUtils.defaultFont);
        g2.setPaint(Color.BLACK);
        int _offset = (f_hasLine ? LINE_DISTANCE_LEFT/2:0);
        OrgChartUtils.drawText(g2, getPos().x+ _offset, getPos().y, 
        		getSize().width - 8 - _offset, getText(), OrgChartUtils.Orientation.CENTER);
        
	}

}
