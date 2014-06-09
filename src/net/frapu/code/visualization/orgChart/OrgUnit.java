/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.orgChart;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * @author ff
 *
 */
public class OrgUnit extends OrgChartElement {

	/**
	 * 
	 */
	public OrgUnit() {
		super();
	}
	
	@Override
	protected Shape getOutlineShape() {
		Ellipse2D outline = new Ellipse2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
	}

}
