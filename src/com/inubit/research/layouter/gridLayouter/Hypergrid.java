/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * datastructure that contains several grids
 * @author ff
 *
 */
public class Hypergrid {

	/**
	 * 
	 */
	public static final int GRID_DISTANCE_X = 40;
	public static final int GRID_DISTANCE_Y = 80;
	
	private ArrayList<Grid> f_grids = new ArrayList<Grid>();
	private boolean[] f_addDistance;
	private boolean[] f_vertical;
	private GridLayouter f_parent;

	/**
	 * @param poolIDCache 
	 * @param layoutPools
	 */
	public Hypergrid(ArrayList<BPMNNodeInterface> clusters,GridLayouter parent, HashMap<BPMNNodeInterface, Integer> poolIDCache) {
		f_parent = parent;
		boolean[] _addDistance = new boolean[clusters.size()+1];
		f_vertical = new boolean[clusters.size()+1];
		_addDistance[0] = true;
		for(int i=1;i<clusters.size();i++) {
			//if both clusters/lanes belong to different pools, distance has to be added
			if(poolIDCache.get(clusters.get(i-1)) != poolIDCache.get(clusters.get(i))) {
				_addDistance[i] = true; //addDistance is one field bigger, this the value is actually set for cluster(i-1)
			}else {//if both clusters/lanes belong to the same pool, no distance has to be added
				_addDistance[i] = false; 
			}
			f_vertical[i] = clusters.get(i-1).isVertical();
		}	
		for(int i=0;i<clusters.size();i++) {
			f_vertical[i+1] = clusters.get(i).isVertical();
		}
		f_addDistance = _addDistance;
		for(int i=0;i<clusters.size()+1;i++) { //the "no Pool" (implicit pool) Cluster gets spot 0
			f_grids.add(new Grid());
		}
	}

	/**
	 * @param i
	 * @param grid
	 */
	public void addRow(int i, int grid) {
		f_grids.get(grid).addRow(i);
		
	}

	/**
	 * @param row
	 * @param col	
	 * @param _obj
	 */
	public void setObject(int row, int col,FlowObjectWrapper obj) {
		if(this.getRowCount(obj.getGrid()) <= row) {
			this.addRow(row, obj.getGrid());
		}
		f_grids.get(obj.getGrid()).setObject(row, col, obj);
	}

	/**
	 * 
	 */
	public void printToConsole() {
		int i=0;
		for(Grid g:f_grids) {
			System.out.println((++i)+"grid:");
			g.printToConsole();
		}
	}

	/**
	 * 
	 */
	public void interleave() {
		for(Grid g:f_grids) {
			g.interleave();
		}
	}

	/**
	 * 
	 */
	public void calculateSizes(boolean synchronizePools) {
		for(int i=0;i<f_grids.size();i++) {
			Grid g = f_grids.get(i);
			g.calculateSizes();
			boolean _synced = false;
			//sync upwards
			int j=i-1;
			for(;j>= 0;j--) {
				if(f_addDistance[j] && !synchronizePools) {
					j++; //otherwise we skipped a row
					break;
				}
				synchronize(f_grids.get(i),f_grids.get(j));
				_synced = true;
			}
			if(j == -1) j = 0;
			//sync downwards
			for(;j<i;j++) {	
				if(_synced == true)
					synchronize(f_grids.get(i),f_grids.get(j));
				if(f_addDistance[j]  && !synchronizePools) {
					break;
				}				
			}			
		}
		int x=GRID_DISTANCE_X + f_parent.getMaxLaneDepth()*GridLayouter.POOL_PADDING_LEFT;
		int y=GRID_DISTANCE_Y;
		
		for(int i=0;i<f_grids.size();i++) {
			if(i>0 && f_vertical[i-1] == false && f_vertical[i] == true) {
				y+=f_parent.getMaxLaneDepth()*GridLayouter.POOL_PADDING_LEFT; //add padding for labels
			}
			Grid g = f_grids.get(i);
			g.calculatePositions(x,y);
			if((g.getGridHeight()>0)) {
				y += g.getGridHeight()+ 
				(f_addDistance[i] ? GRID_DISTANCE_Y : 2*GridLayouter.POOL_PADDING_Y);
			}else if(i != 0){
				//for blackbox pools
				y += GridLayouter.BLACKBOX_POOL_HEIGHT + GRID_DISTANCE_X; 
			}
		}
	}

	/**
	 * @param grid
	 * @param grid2
	 */
	private void synchronize(Grid g1, Grid g2) {
		for(int col=0;col<Math.min(g1.getColCount(), g2.getColCount());col++) {
			int w = Math.max(g1.getColSize(col), g2.getColSize(col));
			g1.setColSize(col, w);
			g2.setColSize(col, w);			
		}			
	}

	/**
	 * 
	 */
	public void applyCoordinates() {
		int _lastPoolEnd = 0;		
		for(int i=0;i<f_grids.size();i++) {
			int _xStart = f_grids.get(_lastPoolEnd).getGridXOffset();
			int _yStart = f_grids.get(_lastPoolEnd).getGridYOffset();			
			if(f_addDistance[i] == true) {
				//this is the end of the pool - save for next iteration
				_lastPoolEnd = i+1;
			}
			Grid g = f_grids.get(i);
			g.applyCoordinates(_xStart,_yStart,f_vertical[i]);
		}
	}

	/**
	 * @param i
	 * @return
	 */
	public int getColCount(int i) {
		return f_grids.get(i).getColCount();
	}

	/**
	 * @param x
	 * @param grid
	 */
	public void addCol(int x, int grid) {
		f_grids.get(grid).addCol(x);
	}

	/**
	 * @param grid
	 * @return
	 */
	public int getRowCount(int grid) {
		return f_grids.get(grid).getRowCount();
	}
	
	public Grid getGrid(int index) {
		return f_grids.get(index);
	}
	
	public int getNumOfGrids() {
		return f_grids.size();
	}

	/**
	 * tries to minimize the length of message flows by switching pools
	 * - not used anymore -  instead the pools will stay where they are (ordered by y position) so
	 * the user can define the ordering
	 * @param pools
	 * @param model
	 */
	public void positionGrids(ArrayList<BPMNNodeInterface> pools,AbstractModelAdapter model) {
		if(pools.size() > 2) {
			Collections.sort(pools, new YPositionComparator());
			/*
			//creating a working copy
			ArrayList<BPMNNodeInterface> _poolsCopy = new ArrayList<BPMNNodeInterface>();
			_poolsCopy.addAll(pools);
			int _mfw = getMessageFlowWeight(_poolsCopy, model);
			//switching nodes
			for(int i=0;i<pools.size();i++) {
				for(int j=i+1;j<pools.size();j++) {
					switchNodes(_poolsCopy,i,j);
					int _mfwNew = getMessageFlowWeight(_poolsCopy, model);
					if(_mfwNew < _mfw) {
						_mfw = _mfwNew;
						pools.clear();
						pools.addAll(_poolsCopy);
					}
					switchNodes(_poolsCopy,i,j);
				}
			}*/
		}
	}
	
	/*
	 * @param pools
	 * @param i
	 * @param j
	 */
	/*private void switchNodes(ArrayList<BPMNNodeInterface> pools, int i, int j) {
		BPMNNodeInterface _obj = pools.get(i);
		pools.set(i, pools.get(j));
		pools.set(j, _obj);
	}*/

	/**
	 * determines the abstarct length of all message flows given the pool ordering
	 * @param pools
	 * @param model
	 * @return
	 */
	public int getMessageFlowWeight(ArrayList<BPMNNodeInterface> pools,AbstractModelAdapter model) {
		int _result = 0;
		for(EdgeInterface e:model.getEdges()) {
			if(e instanceof BPMNEdgeInterface) {
				if(((BPMNEdgeInterface)e).isMessageFlow()) {
					int _pool1 = findPoolIndex((NodeInterface) e.getSource(),pools);
					int _pool2 = findPoolIndex(((NodeInterface)e.getTarget()),pools);
					if((_pool1 >-1) && (_pool2>-1)) {
						_result += Math.abs(_pool1-_pool2);
					}
				}
			}
		}
		return _result;
	}

	/**
	 * find the pool that is or contains the given Node
	 * @param source
	 * @return
	 */
	private int findPoolIndex(NodeInterface source,ArrayList<BPMNNodeInterface> pools) {
		for(int i=0;i<pools.size();i++) {
			BPMNNodeInterface p = pools.get(i);
			if(p.equals(source) || p.getContainedNodes().contains(source)) {
				return i;
			}
		}
		return -1;
	}
	
}
