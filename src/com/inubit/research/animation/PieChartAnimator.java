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
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.reporting.PieChart;

/**
 * @author ff
 *
 */
public class PieChartAnimator extends DefaultNodeAnimator{

	private PieChart f_chart = null;

	private List<Integer> f_oldData;
	private List<Integer> f_diffs;
	private List<Integer> f_newData;

	private int f_removedDummyCount = 0;

	private int f_colorResetIndex = -1;
	
	/**
	 * @param toAnimate
	 * @param parent
	 */
	public PieChartAnimator(ProcessNode toAnimate, Animator parent) {
		super(toAnimate, parent);
		if((toAnimate instanceof PieChart)) {
			f_chart = (PieChart) toAnimate;
		}
	}
	
	public void setNewData(List<Integer> newData) {
		f_newData = newData;
	}

	@Override
	protected void firstStep() {
		super.firstStep();
		if(f_chart != null) {
			f_oldData = f_chart.getData();
			if(f_newData != null) {
				f_diffs = new ArrayList<Integer>();
				//extending f_oldData if necessary
				for(int i=0;i<f_newData.size();i++) {
					if(f_oldData.size()<=i) {
						f_oldData.add(new Integer(0));
					}
				}
				//extending f_newData if necessary
				for(int i=0;i<f_oldData.size();i++) {
					if(f_newData.size()<=i) {
						f_newData.add(new Integer(0));
						f_removedDummyCount++;
					}
				}
				int _sum = sum(f_oldData);
				if(_sum == 0) { //chart is supposed to build up, add a dummy value
					f_colorResetIndex  = f_newData.size();
					f_removedDummyCount++;
					f_oldData.add(new Integer(sum(f_newData))); //last value as dummy element
					f_newData.add(new Integer(0));
					//setting the last color to "white"
					List<Color> _colors = f_chart.getColors();
					_colors.add(f_newData.size()-1, Color.WHITE);
					f_chart.setColors(_colors);
				}
				//building differenceList				
				for(int i=0;i<f_newData.size();i++) {
					f_diffs.add(f_newData.get(i)-f_oldData.get(i));
				}
			}
		}		
	}

	/**
	 * @param data
	 * @return
	 */
	private int sum(List<Integer> data) {
		int result = 0;
		for(Integer i:data) {
			result += i;
		}
		return result;
	}

	@Override
	protected void setNewValues() {
		super.setNewValues();
		if(getCurrentTick() < getSteps()) {
			if(f_newData != null) {
				List<Integer> _data = getNewValues();
				f_chart.setData(_data);
			}
		}else {
			while(f_removedDummyCount > 0) {
				f_newData.remove(f_newData.size()-1);
				f_removedDummyCount--;
			}
			if(f_colorResetIndex != -1) {
				//removing the white color
				List<Color> _colors = f_chart.getColors();
				_colors.remove(f_colorResetIndex);
				f_colorResetIndex = -1;
				f_chart.setColors(_colors);
			}
			if(f_newData != null) f_chart.setData(f_newData);
		}
	}

	/**
	 * @return
	 */
	private List<Integer> getNewValues() {
		double _prog = getLinearProgress();
		List<Integer> _newData = new ArrayList<Integer>();
		if(f_diffs != null) {
			for(int i=0;i<f_diffs.size();i++) {		
				_newData.add((int)(f_oldData.get(i) + _prog * f_diffs.get(i)));
			}
		}
		return _newData;
	}
	

}
