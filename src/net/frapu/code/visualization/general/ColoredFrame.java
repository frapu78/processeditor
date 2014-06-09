/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.general;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;

/**
 * @author ff
 *
 */
public class ColoredFrame extends Cluster{
	
	public final static String PROP_HEADING_LEFT = "heading_left";

	/**
	 * 
	 */
	public ColoredFrame() {
		setBackground(new Color(180,200,255)); // looks similar to default IS frame color
		setSize(250, 150);
		setProperty(PROP_HEADING_LEFT, "1");
		setPropertyEditor(PROP_HEADING_LEFT, new BooleanPropertyEditor());
	}
	
	@Override
	protected Shape getOutlineShape() {
		  Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
	                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
		  return outline;
	}

	@Override
	protected void paintInternal(Graphics g) {
		drawFrame((Graphics2D)g);
	}

	/**
	 * @param g
	 */
	private void drawFrame(Graphics2D g) {
		Shape _s = getOutlineShape();
                g.setStroke(ProcessUtils.defaultStroke);
		g.setColor(this.getBackground());
		g.fill(_s);
		g.setColor(Color.BLACK);
		g.draw(_s);
		Orientation _o = getProperty(PROP_HEADING_LEFT).equals("1") ? Orientation.LEFT : Orientation.TOP;
		int _offset = getProperty(PROP_HEADING_LEFT).equals("1") ? this.getSize().width/2 : 0;
		ProcessUtils.drawText(g, this.getPos().x -_offset,this.getPos().y- this.getSize().height/2,
				this.getSize().width, this.getText(), _o);
		
	}

}
