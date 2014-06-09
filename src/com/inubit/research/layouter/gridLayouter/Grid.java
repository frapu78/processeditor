/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Point;
import java.util.ArrayList;



/**
 * 2D-data structure where flowobjects are added to.
 * afterwards a concrete x-y position can be determined by
 * the location in the grid and the sizes of all contained elements.
 * @author ff
 *
 */
public class Grid {
	
	private int f_colCount  = 1;
	private ArrayList<ArrayList<FlowObjectWrapper>> f_data = new ArrayList<ArrayList<FlowObjectWrapper>>();
	private int[] f_RowSizes;
	private int[] f_ColSizes;
	private int[] f_RowY;
	private int[] f_ColX;
	private int f_gridHeight;
	private int f_gridWidth;
	private int f_gridx;
	private int f_gridy;
	/**
	 * initialises an empty grid
	 */
	public Grid() {
		
	}
	
	/**
	 * adds a new row at the given index.
	 * All objects in a row with index x >= rowIDx
	 * will be moved one row to the bottom and the 
	 * Position will be propagated to them.
	 * @param rowIdx
	 */
	public void addRow(int rowIdx) {
		int _rIdx = Math.min(getRowCount(), rowIdx);
		
		do{//init cols
			ArrayList<FlowObjectWrapper> _newRow = new ArrayList<FlowObjectWrapper>();
			for(int i=0;i<f_colCount;i++) {	
				_newRow.add(null);
			}
			f_data.add(_rIdx, _newRow);
			
			for(int i=_rIdx;i<f_data.size();i++) {
				for(FlowObjectWrapper obj:f_data.get(i)) {
					if(obj != null) {
						Point p = obj.getPosition();
						p.translate(0, 1);
						obj.setPosition(p);
					}
				}
			}
		}while(_rIdx++ < rowIdx);
	}
	
	/**
	 * returns the current number of columns.
	 * @return
	 */
	public int getColCount() {
		return f_colCount;
	}
	
	/**
	 * adds columns up to colIdx
	 * @param colIdx
	 */
	public void addCol(int colIdx) {
		while(f_colCount <= colIdx) {
			f_colCount++;
			//adding new Object to all rows so index becomes available
			for(int i=0;i<f_data.size();i++) {
				f_data.get(i).add(null);
			}
		}
	}
	
	/**
	 * places an Object at the given position and informs the object of
	 * that position change.
	 * @param rowIdx
	 * @param colIdx
	 * @param obj
	 */
	public void setObject(int rowIdx,int colIdx,FlowObjectWrapper obj) {
		if(f_data.get(rowIdx).get(colIdx) != null && obj != null) {
			this.addRow(rowIdx);
		}
		f_data.get(rowIdx).set(colIdx,obj);
		if(obj != null)
			obj.setPosition(new Point(colIdx, rowIdx));
	}

	/**
	 * calculates width,height  for all grid-cells.
	 * needed so they can be applied with applyCoordinates. 
	 */
	public void calculateSizes() {
		//getting max size of element in data grid
		int rows = f_data.size();
		f_RowSizes = new int[rows];
		f_ColSizes = new int[f_colCount];
		for(int i=0;i<f_data.size();i++) {
			ArrayList<FlowObjectWrapper> row = f_data.get(i);
			for(int j=0;j<row.size();j++) {
				FlowObjectWrapper obj = row.get(j);
				if(obj != null) {
					f_RowSizes[i] = Math.max(f_RowSizes[i], obj.getSize().height);
					f_ColSizes[j] = Math.max(f_ColSizes[j], obj.getSize().width);
				}
			}
		}				
	}
	
	/**
	 * calculates x,y position for all grid-cells.
	 * needed so they can be applied with applyCoordinates. 
	 */
	public void calculatePositions(int xOffset,int yOffset) {
		f_gridx = xOffset;
		f_gridy = yOffset;
		//calculating x,y positions for objects
		f_RowY = new int[f_RowSizes.length];
		int _y = 0;
		for(int i=0;i<f_RowSizes.length;i++) {
			f_RowY[i] = _y + f_RowSizes[i]/2+yOffset;
			_y += f_RowSizes[i];
		}
		f_gridHeight = _y;
		
		f_ColX = new int[f_ColSizes.length];
		int _x = 0;
		for(int i=0;i<f_ColSizes.length;i++) {
			f_ColX[i] = _x + f_ColSizes[i]/2+xOffset;
			_x += f_ColSizes[i];
		}
		f_gridWidth = _x;
	}
	
	/**
	 * gets the total height of this grid (sum of all rows)
	 * @return
	 */
	public int getGridHeight() {
		return f_gridHeight;
	}
	
	/**
	 * gets the total width of this grid (sum of all cols)
	 * @return
	 */
	public int getGridWidth() {
		return f_gridWidth;
	}

	/**
	 * applies specific x,y coordinates to the underlying objects (wrapped FlowObjects)
	 * @param vertical invert x and y coordinates relative to the center of the grid (e.g. for vertical Pools)
	 */
	public void applyCoordinates(int xUL, int yUL,boolean vertical) {
		for(int i=0;i<f_data.size();i++) {
			ArrayList<FlowObjectWrapper> row = f_data.get(i);
			for(int j=0;j<row.size();j++) {
				FlowObjectWrapper obj = row.get(j);
				if(obj != null) {
					Point _pos = new Point(f_ColX[j],f_RowY[i]);
					if(obj.getMoveMode() < 0) {
						//move it slightly to the left if space is available in that column
						_pos.x -=(f_ColSizes[j]+FlowObjectWrapper.getSpacingX())/2;					
					}else if(obj.getMoveMode() > 0) {
						//move it slightly to the right if space is available in that column
						_pos.x += (f_ColSizes[j]-FlowObjectWrapper.getSpacingY())/2;
					}//else nothing needed
					if(!vertical) {
						obj.getWrappedObject().setPos(_pos.x, _pos.y);
					}else {
						Point _invPos = new Point(_pos.y-yUL,_pos.x-xUL);
						_invPos.x += xUL;
						_invPos.y += yUL;	
						obj.getWrappedObject().setPos(_invPos.x, _invPos.y);
					}
				}
			}
		}	
	}


	/**
	 * performs the interleaving (elimination of unnecessary rows)
	 */
	public void interleave() {
		interleaveRow();
		interleaveSingleElements();
		interleaveRow();
	}

	/**
	 * DataObjects that are only connected to one node,
	 * can be put close to them
	 */
	private void interleaveSingleElements() {
		//from top to bottom
		
		//for each row
		for(int i=0;i<f_data.size()-1;i++) {
			ArrayList<FlowObjectWrapper> _row = f_data.get(i);
			//for each element
			for(int j=0;j<_row.size();j++) {
				FlowObjectWrapper _element = _row.get(j);
				interleaveCheckSingleElement(j, _element);
			}
		}
		
		//from bottom to top
		
		//for each row
		for(int i=f_data.size()-1;i>=0;i--) {
			ArrayList<FlowObjectWrapper> _row = f_data.get(i);
			//for each element
			for(int j=0;j<_row.size();j++) {
				FlowObjectWrapper _element = _row.get(j);
				interleaveCheckSingleElement(j, _element);
			}
		}
	}

	private void interleaveCheckSingleElement(int j,
			FlowObjectWrapper _element) {
		if(_element != null) {
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			for(FlowObjectWrapper _p :_element.getPredecessors()) {
				minY = Math.min(minY, _p.getPosition().y);
				maxY = Math.max(maxY, _p.getPosition().y);
			}
			for(FlowObjectWrapper _s :_element.getSuccessors()) {
				minY = Math.min(minY, _s.getPosition().y);
				maxY = Math.max(maxY, _s.getPosition().y);
			}
			int myY = _element.getPosition().y;
			if(myY < minY) {
				moveObjectCloser(j, _element, myY, minY);	
			}else if(myY > maxY) {
				moveObjectCloser(j, _element, myY, maxY);
			}			
		}
	}

	private void moveObjectCloser(int j, FlowObjectWrapper _element, int myY,
			int otY) {
		if(myY < otY) {
			//we are higher, moving down	
			while(myY+1 < otY && myY+1 <f_data.size()) {
				if(f_data.get(++myY).get(j) == null) {
					setObject(myY, j, (FlowObjectWrapper) _element);
					setObject(myY-1, j, null);
				}else {
					break;
				}
			}
		}else {
			//we are lower, moving up	
			while(myY-1 > otY && myY-1 >= 0) {
				if(f_data.get(--myY).get(j) == null) {
					setObject(myY, j, (FlowObjectWrapper) _element);
					setObject(myY+1, j, null);
				}else {
					break;
				}
			}
		}
	}

	private void interleaveRow() {
		for(int i=0;i<f_data.size()-1;i++) {
			if(canBeInterleaved(f_data.get(i),f_data.get(i+1))) {
				ArrayList<FlowObjectWrapper> row1 = f_data.get(i);
				this.removeRow(i);
				for (int j = 0; j < row1.size(); j++) {	
					Object _o = row1.get(j);
					if(_o != null) {
						setObject(i, j, (FlowObjectWrapper) _o);
					}
				}
				i--;
			}
		}
	}

	/**
	 * @param i
	 */
	private void removeRow(int rowIdx) {
		f_data.remove(rowIdx);
		for(int i=rowIdx;i<f_data.size();i++) {
			for(FlowObjectWrapper obj:f_data.get(i)) {
				if(obj != null) {
					Point p = obj.getPosition();
					p.translate(0, -1);
					obj.setPosition(p);
				}
			}
		}
	}

	/**
	 * used for interleaving check
	 * @param arrayList
	 * @param arrayList2
	 * @return
	 */
	private boolean canBeInterleaved(ArrayList<FlowObjectWrapper> arrayList,ArrayList<FlowObjectWrapper> arrayList2) {
		for (int i = 0; i < arrayList.size(); i++) {
			//checking from both sides
			FlowObjectWrapper _obj = arrayList.get(i);
			if(!testInterleaveability(arrayList2, i, _obj)) {
				return false;
			}
			_obj = arrayList2.get(i);
			if(!testInterleaveability(arrayList, i, _obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * used for interleaving check
	 * @param targetList
	 * @param index
	 * @param _obj
	 * @return
	 */
	private boolean testInterleaveability(ArrayList<FlowObjectWrapper> targetList, int index,FlowObjectWrapper _obj) {
		if(_obj != null) {
			int _min = Math.max(index - GridLayouter.LAYOUT_RIGHT_GRID_SPACING, 0);
			int _max = Math.min(index + GridLayouter.LAYOUT_RIGHT_GRID_SPACING, targetList.size());
			for(int i= _min;i<_max;i++) {
				if(targetList.get(i) != null) {
					return false;
				}
			}				
			//join/split check
			if(!checkJoinSplit(targetList,index)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks weither the position i lies directly in between a join split pair
	 * required for better interleave quality
	 * @param arrayList2
	 * @param i
	 * @return
	 */
	private boolean checkJoinSplit(ArrayList<FlowObjectWrapper> list,	int i) {
		FlowObjectWrapper _split = null;
		FlowObjectWrapper _join = null;
		for(int idx = i;idx>=0;idx--) {
			FlowObjectWrapper _obj = list.get(idx);
			if(_obj != null) {
				if(_obj.isSplit()) {
					_split = list.get(idx);
					break;
				}
			}
		}
		for(int idx = i;idx<list.size();idx++) {
			FlowObjectWrapper _obj = list.get(idx);
			if(_obj != null) {
				if(_obj.isJoin()) {
					_join = list.get(idx);
					break;
				}
			}
		}
		if(_split != null) {
			for (FlowObjectWrapper _fow : _split.getSuccessors()) {
				if(_fow.equals(_join)) {
					//directly connected
					return false;
				}
			}
		}
		//allFine
		return true;
	}

	public FlowObjectWrapper getObject(int row,int col) {
		return f_data.get(row).get(col);
	}
	
	/**
	 * @return
	 */
	public int getRowCount() {
		return f_data.size();
	}
	
	
	public void printToConsole() {
		for(ArrayList<FlowObjectWrapper> _row : f_data) {
			for(FlowObjectWrapper o:_row) {
				String s = "";
				if(o != null) {
					s = o.toString();
				}
				System.out.print(s);
				int limit = ((35-s.length()));
				for(int i=0;i<limit;i++) {
					System.out.print(" ");
				}
				System.out.print("\t|\t");
			}
			System.out.println();
		}
	}
	
	public int getGridXOffset() {
		return f_gridx;
	}
	
	public int getGridYOffset() {
		return f_gridy;
	}
		
	public int getColSize(int col) {
		return f_ColSizes[col];
	}
	
	public int getRowSize(int row) {
		return f_RowSizes[row];
	}
	
	public void setColSize(int col,int value) {
		f_ColSizes[col] = value;
	}
	
	public void setRowSize(int row,int value) {
		f_RowSizes[row] = value;
	}
	
	public int getX(int col) {
		return f_ColX[col];
	}
	
	public int getY(int row) {
		return f_RowY[row];
	}
	
}

 
