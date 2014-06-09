/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.orgChart;

import java.awt.Shape;
import java.awt.Stroke;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;

/**
 * @author ff
 *
 */
public class Connection extends ProcessEdge {

	
	/**
	 * 
	 */
	public Connection() {
		super();
	}
	
	/**
	 * @param source
	 * @param target
	 */
	public Connection(ProcessNode source, ProcessNode target) {
		super(source,target);
	}

	@Override
	public Stroke getLineStroke() {
		return ProcessUtils.defaultStroke;
	}

	@Override
	public Shape getSourceShape() {
		return null;
	}

	@Override
	public Shape getTargetShape() {
		return ProcessUtils.standardArrowFilled;
	}

}
