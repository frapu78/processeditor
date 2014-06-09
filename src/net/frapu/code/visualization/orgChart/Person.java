/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.orgChart;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/**
 * @author ff
 *
 */
public class Person extends OrgChartElement {

	
	/**
	 * 
	 */
	public Person() {
		setBackground(new Color(255,230,156)); //inspired by inubit orgChart
	}

	@Override
	protected Shape getOutlineShape() {
		RoundRectangle2D outline = new RoundRectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height, 10, 10);
        return outline;
	}

}
