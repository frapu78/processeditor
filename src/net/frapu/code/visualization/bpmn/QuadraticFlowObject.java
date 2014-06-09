/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.bpmn;

import java.awt.Dimension;

/**
 * represents a flow object whose width and height cannot be changed
 * independently. It will be ensured, that width and height are always equal.
 * @author ff
 *
 */
public abstract class QuadraticFlowObject extends FlowObject{
	
	public void setSize(int w, int h) {
    	Dimension old = this.getSize();
    	if(old.width - w != 0) {
    		//width was changed
    		super.setSize(w, w);
    	}else {
    		//height was changed
    		super.setSize(h, h);
    	}
    }

}
