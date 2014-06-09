/**
 *
 * Process Editor - Animation Package
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.animation;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.reporting.BarChart;

/**
 * @author ff
 *
 */
public class BarChartAnimator extends DefaultNodeAnimator{

	private BarChart f_chart = null;

	private List<List<Integer>> f_oldData;
	private List<List<Integer>> f_diffs;
	private List<List<Integer>> f_newData;

	private double f_maxValue;
	private int f_newDataRemoveCount = 0;
	
	/**
	 * @param toAnimate
	 * @param parent
	 */
	public BarChartAnimator(ProcessNode toAnimate, Animator parent) {
		super(toAnimate, parent);
		if((toAnimate instanceof BarChart)) {
			f_chart = (BarChart) toAnimate;
		}
	}
	
	public void setNewData(List<List<Integer>> newData,double maxValue) {
		f_maxValue = maxValue;
		f_newData = newData;
	}

	@Override
	protected void firstStep() {
		super.firstStep();
		if(f_chart != null) {
			f_chart.setMaxHeight(f_maxValue);
			f_oldData = f_chart.getData();
			if(f_newData != null) {
				f_diffs = new ArrayList<List<Integer>>();
				//extending f_oldData if necessary
				extend(f_oldData,f_newData,false);
				extend(f_newData,f_oldData,true);
				//building differenceList				
				for(int i=0;i<f_newData.size();i++) {
					List<Integer> oldList = f_oldData.get(i);					
					List<Integer> newList = f_newData.get(i);
					ArrayList<Integer> _diffList = new ArrayList<Integer>();
					for(int j=0;j<newList.size();j++) {
						Integer _new = newList.get(j);
						Integer _old = oldList.get(j);						
						_diffList.add(_new - _old);						
					}
					f_diffs.add(_diffList);
				}
			}
		}		
	}

	private void extend(List<List<Integer>> toExtend,List<List<Integer>> template,boolean count) {
		for(int i=0;i<template.size();i++) {
			if(toExtend.size()<=i) {
				toExtend.add(new ArrayList<Integer>());
				if(count) {
					f_newDataRemoveCount++;
				}
			}
			List<Integer> oldList = toExtend.get(i);
			List<Integer> newList = template.get(i);
			while(oldList.size() < newList.size()) {
				oldList.add(new Integer(0));
			}					
		}
	}

	@Override
	protected void setNewValues() {
		super.setNewValues();
		if(getCurrentTick() < getSteps()) {
			if(f_newData != null) {
				List<List<Integer>> _data = getNewValues();
				f_chart.setStackedData(_data);
			}
		}else {
			while(f_newDataRemoveCount > 0) {
				f_newData.remove(f_newData.size()-1);
				f_newDataRemoveCount--;
			}
			if(f_newData != null) f_chart.setStackedData(f_newData);
		}
	}

	/**
	 * @return
	 */
	private List<List<Integer>> getNewValues() {
		double _prog = getLinearProgress();
		List<List<Integer>> _newData = new ArrayList<List<Integer>>();
		for(int i=0;i<f_diffs.size();i++) {
			List<Integer> _part = new ArrayList<Integer>();
			List<Integer> oldList = f_oldData.get(i);
			List<Integer> diffList = f_diffs.get(i);
			for(int j=0;j<diffList.size();j++) {
				_part.add((int)(oldList.get(j) + _prog * diffList.get(j)));
			}
			_newData.add(_part);
		}
		return _newData;
	}
	

}
