/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.frapu.code.visualization.twf;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import net.frapu.code.visualization.ProcessNode;

/**
 *
 * @author fpu
 */
public abstract class Module extends ProcessNode {

	
	/**
	 * 
	 */
	public Module() {
		this.setSize(60,40);
	}
    @Override
    public Set<Point> getDefaultConnectionPoints() {
        HashSet<Point> cp = new HashSet<Point>();
        // Calculate default connection points
        cp.add(new Point(0-(getSize().width/2), 0));
        cp.add(new Point((getSize().width/2), 0));
        return cp;
    }

}
