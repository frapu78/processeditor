/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import net.frapu.code.visualization.Cluster;

import net.frapu.code.visualization.ProcessNode;

/**
 * animates position changes as a linear
 * interpolation
 * @author ff
 *
 */
public class DefaultNodeAnimator extends DefaultAlphaAnimator {
		
	//for movement
	private Point f_coords;
	private Point f_newCoords;
	private Point f_difference;
	
	//for size
	private Dimension f_size;
	private Dimension f_newSize;
	private Dimension f_dimDifference;
	
	//for color
	private Color f_color;
	private Color f_newColor;
	//color differences, cannot be saved to a color object as negative values are needed
	private int f_diffRed = 0;
	private int f_diffGreen = 0;
	private int f_diffBlue = 0;	

	/**
	 * 
	 */
	public DefaultNodeAnimator(ProcessNode node,Animator animator) {
		super(node,animator);
		
	}
	
	public void setNewCoords(Point p) {
		f_newCoords = p;
		resetCurrentTick();
	}
	
	public void setNewSize(Dimension size) {
		f_newSize = size;
		resetCurrentTick();
	}
	
	public void setNewColor(Color c) {
		f_newColor = c;
	}

	@Override
	protected void firstStep() {
		super.firstStep();
		if(f_newCoords != null) {
			f_coords = getNode().getPos();
			f_difference = new Point(f_newCoords);
			f_difference.x -= f_coords.x;
			f_difference.y -= f_coords.y;
		}
		if(f_newSize != null) {
			f_size = getNode().getSize();
			f_dimDifference = new Dimension(f_newSize);
			f_dimDifference.width -= f_size.width;
			f_dimDifference.height -= f_size.height;
		}
		if(f_newColor != null) {
			f_color = getNode().getBackground();
                        if (f_newColor==null) f_newColor = new Color(f_color.getRGB());
			f_diffRed = f_newColor.getRed() - f_color.getRed();
			f_diffGreen = f_newColor.getGreen() - f_color.getGreen();
			f_diffBlue = f_newColor.getBlue() - f_color.getBlue();
		}
	}
	
	/**
	 * performs the main animation.
	 * sets
	 */
    @Override
	protected void setNewValues() {
		super.setNewValues();
		double _percentage = getHyperbolicProgress();
		if(processRunning()) {
			if(f_difference != null) {
				Point _newPos = new Point(f_coords);
				_newPos.x += (int)(f_difference.x*_percentage);
				_newPos.y += (int)(f_difference.y*_percentage);
                                if (getNode() instanceof Cluster) {
                                   ((Cluster) getNode()).setPosIgnoreContainedNodes(_newPos.x, _newPos.y);
                                } else getNode().setPos(_newPos);
				
			}
			
			if(f_dimDifference != null) {
				Dimension _newDim = new Dimension(f_size);
				_newDim.width  += (int)(f_dimDifference.width*_percentage);
				_newDim.height += (int)(f_dimDifference.height*_percentage);
				getNode().setSize(_newDim.width, _newDim.height);
			}
			
			if(f_newColor != null) {
				int _r = (int) (f_color.getRed() + f_diffRed*_percentage);
				int _g = (int) (f_color.getGreen() + f_diffGreen*_percentage);
				int _b = (int) (f_color.getBlue() + f_diffBlue*_percentage);
				getNode().setBackground(new Color(_r,_g,_b));
			}
			
		}else {
			if(f_newCoords != null) {
                            if (getNode() instanceof Cluster) {
                                ((Cluster) getNode()).setPosIgnoreContainedNodes(f_newCoords.x, f_newCoords.y);
                            } getNode().setPos(f_newCoords);
                        }
			if(f_newSize != null) getNode().setSize(f_newSize.width, f_newSize.height);
			if(f_newColor != null) getNode().setBackground(f_newColor);
		}
	} 

	protected boolean processRunning() {
		return getCurrentTick() < getSteps();
	}

	public Dimension getNewSize() {
        return f_newSize;
    }

    public ProcessNode getNode() {
        return (ProcessNode)getProcessObject();
    }



    /**
     * compares the underlying node objects for equality
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultNodeAnimator) {
            DefaultNodeAnimator c = (DefaultNodeAnimator)obj;
            return this.getNode().equals(c.getNode());
        }
        return super.equals(obj);
    }
}
