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
import java.awt.Point;

import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public class ErrorConnection extends Connection {
	
	
	public ErrorConnection() {
		super();
	}
	
	/**
	 * 
	 */
	public ErrorConnection(ProcessNode source, ProcessNode target) {
		super(source,target);
	}
	
	@Override
	protected synchronized void paintInternal(Graphics g) {
		setColor(Color.RED);
		super.paintInternal(g);
	}
	
	@Override
	protected synchronized void updateCache() {		
		super.updateCache();
		routingPointCache.remove(0);
		Point _p = getSource().getPos();
		_p.y += getSource().getSize().height/2;
		routingPointCache.add(0,_p);	
	}	
}
