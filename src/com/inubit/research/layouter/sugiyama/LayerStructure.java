/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.sugiyama;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.inubit.research.layouter.interfaces.NodeInterface;


/**
 * @author ff
 *
 */
public class LayerStructure{
	
	private ArrayList<ArrayList<NodeWrapper>> f_layers;
	private ArrayList<Boolean> f_layerIsSubLayer;
	private BarycenterComparator f_bcComp = new BarycenterComparator();
	
	private int f_totalWidth = 0;
	private int f_totalHeight = 0;
	
	private boolean f_initialSortDirection = true; //true = down, false = up
	
	private long bary = 0;	
	private long cross=0;
	
	
	
	public LayerStructure(int layers) {
		f_layerIsSubLayer = new ArrayList<Boolean>(layers);
		f_layers = new ArrayList<ArrayList<NodeWrapper>>(layers);
		for(int i=0;i<layers;i++) {
			f_layers.add(new ArrayList<NodeWrapper>());
			f_layerIsSubLayer.add(false);
		}
	}
	
	public void addAll(NodeWrapper w) {
		if(!w.isAddedToLayerStructure()) {
			w.setAddedToLayerStructure(true);
			f_layers.get(w.getLayer()).add(w);
			for(NodeWrapper w2:w.getSuccessors()) {
				addAll(w2);
			}
		}
	}
	
	public void addSubLayer(int after) {
		int _newLayer = after+1;
		f_layers.add(_newLayer, new ArrayList<NodeWrapper>());
		f_layerIsSubLayer.add(_newLayer,true);
		//tell all nodes that they were shifted down
		for(int i=after+2;i<f_layers.size();i++) {
			ArrayList<NodeWrapper> _layer = f_layers.get(i);
			for(NodeWrapper n:_layer) {
				n.setMinLayer(_newLayer);
			}
		}
	}
	
	public int getNumberOfLayers() {
		return f_layers.size();
	}
	
	

	

	/**
	 * @param sugiyamaLayoutAlgorithm 
	 * 
	 */
	public void reduceCrossings(final SugiyamaLayoutAlgorithm sugiyamaLayoutAlgorithm) {
		boolean _up = f_initialSortDirection;
		boolean _stop= false;
		int _counter = 0;
		int _cross = Integer.MAX_VALUE;
		int _newCross = Integer.MAX_VALUE-1;
		do {
			_stop = !(_newCross < _cross);
			_cross = _newCross;
			if(_up) {
				barySortDown(0);
				optimiseDown();
			}else {
				barySortUp(f_layers.size()-1);
				optimiseUp();
			}
			
			_counter++;
			_up = !_up;
			
			_newCross = getTotalCrossings();
		}while(_counter < 19 && (!_stop) && (_cross > 0));
	}
	
	private void optimiseDown() {
		for(int i=0;i<f_layers.size()-1;i++) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i+1);
			calculateBaryCenters(layer1, layer2, true);
			optimise(layer1, layer2,true,i);
		}
	}
	
	private void optimiseUp() {
		for(int i=f_layers.size()-1;i>0;i--) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i-1);
			calculateBaryCenters(layer1, layer2, false);
			optimise(layer1, layer2,false,i);	
		}
	}

	private void optimise(ArrayList<NodeWrapper> layer1,ArrayList<NodeWrapper> layer2,boolean down,int layer) {
		int _cross = getCrossings(layer1, layer2,down);
		//int __cross= getTotalCrossings();
		if(_cross > 0) {
			outer: for(int n1=0;n1<layer2.size()-1;n1++) {
				for(int n2=n1+1;n2<layer2.size();n2++) {
					if(Float.compare(layer2.get(n1).getBaryCenter(), layer2.get(n2).getBaryCenter()) == 0) {
						//switching nodes
						switchNodes(layer2, n1, n2);
						if(down) {
							barySortDown(layer+1);
						}else{
							barySortUp(layer-1);
						}
						int _cross2 = getCrossings(layer1, layer2,down);
						//int __cross2 = getTotalCrossings();
						if(_cross2>=_cross) {
							//switchNodes back
							switchNodes(layer2,n1,n2);
							if(down) {
								barySortDown(layer+1);
							}else{
								barySortUp(layer-1);
							}
						}
					}else {
						continue outer;
					}
				}	
			}
		}
	}

	private void switchNodes(ArrayList<NodeWrapper> layer2, int n1, int n2) {
		NodeWrapper _tmp = layer2.get(n1);
		layer2.set(n1, layer2.get(n2));
		layer2.set(n2, _tmp);
	}
	
	public ArrayList<NodeWrapper> getLayer(int num){
		return f_layers.get(num);
	}
	
	
	public int getTotalCrossings() {
		int _result = 0;
		for(int i=0;i<f_layers.size()-1;i++) {
			_result += getCrossings(f_layers.get(i), f_layers.get(i+1),true);
		}
		return _result;
	}

	/**
	 * tries to reduce crossings moving through the layers from top to bottom
	 * @param j 
	 */
	private void barySortDown(int startLayer) {
		for(int i=startLayer;i<f_layers.size()-1;i++) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i+1);
			barySort(layer1,layer2,true);
		}
	}
	
	/**
	 * @param j 
	 */
	private void calculateBaryCentersDown(int startLayer) {
		for(int i=startLayer;i<f_layers.size()-1;i++) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i+1);
			calculateBaryCenters(layer1, layer2, true);
		}
	}
	
	/**
	 * tries to reduce crossings moving through the layers from bottom to top
	 */
	private void barySortUp(int startLayer) {
		for(int i=startLayer;i>0;i--) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i-1);
			barySort(layer1,layer2,false);
		}
	}
	
	/**
	 */
	private void calculateBaryCentersUp(int startLayer) {
		for(int i=startLayer;i>0;i--) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(i);
			ArrayList<NodeWrapper> layer2 = f_layers.get(i-1);
			calculateBaryCenters(layer1, layer2, false);
		}
	}
	
	
	
	/**
	 * @param layer1
	 * @param layer2
	 * @param down - indicating if the matrix has to be build upwards or downwards
	 */
	private void barySort(ArrayList<NodeWrapper> layer1,ArrayList<NodeWrapper> layer2,boolean down) {
		long start = System.currentTimeMillis();
		//calculating BCs for all cols
		calculateBaryCenters(layer1, layer2, down);
		Collections.sort(layer2, f_bcComp);
		bary += System.currentTimeMillis() - start;
	}

	private void calculateBaryCenters(ArrayList<NodeWrapper> layer1,ArrayList<NodeWrapper> layer2, boolean down) {
		for(NodeWrapper w:layer2) {
			if(w != null) {
				w.setBaryCenter(0.0f);
			}
		}
		for(int i=0;i<layer1.size();i++) {
			NodeWrapper w = layer1.get(i);
			if(w != null) {
				for(NodeWrapper target : down ?  w.getSuccessors() : w.getPredecessors()) {
					target.addToBaryCenter(i);
				}			
			}
		}
		for(NodeWrapper w:layer2) {
			if(w != null) {
				List<NodeWrapper> _targets = down ?  w.getPredecessors() : w.getSuccessors();
				if(_targets.size() == 0) {
					w.setBaryCenter(layer2.indexOf(w));
				}else {
					w.setBaryCenter(w.getBaryCenter() / _targets.size());
				}
			}
		}
		/*for(NodeWrapper w:layer2) {
			if(w != null) {
				float _bc = 0.0f;
				for(NodeWrapper target : down ?  w.getPredecessors() : w.getSuccessors()) {
					_bc += layer1.indexOf(target);
				}
				w.setBaryCenter(_bc / (down ?  w.getPredecessors().size(): w.getSuccessors().size()));
			}
		}*/
	}

	

	/**
	 * @param layer1
	 * @param layer2
	 * @return
	 */
	private int getCrossings(ArrayList<NodeWrapper> layer1,ArrayList<NodeWrapper> layer2,boolean down) {
		long start = System.currentTimeMillis();
		int _result = 0;
		int _edgeEnd2;
		int _edgeEnd;
		int j;
		for(int i=0;i<layer1.size();i++) {
			NodeWrapper n = layer1.get(i);
			if(n != null) {
				for(NodeWrapper target : down ? n.getSuccessors() : n.getPredecessors()) {
					_edgeEnd = layer2.indexOf(target);
					for (j=i+1;j<layer1.size();j++) {
						NodeWrapper n2 = layer1.get(j);
						if(n2 != null) {
							for(NodeWrapper target2 : down ? n2.getSuccessors() : n2.getPredecessors()) {
								_edgeEnd2 = layer2.indexOf(target2);
								//i = position of node 1 in layer1
								//j = position of node 2 in layer1
								if(!(_edgeEnd <= _edgeEnd2) ) {
									_result++;
								}
							}
						}
					}
				}
			}
		}
		cross += System.currentTimeMillis()-start;
		return _result;
	}

	@Override
	public String toString() {
		StringBuffer _result = new StringBuffer();
		_result.append("LayerStructure:\n");
		for (int i = 0; i < f_layers.size(); i++) {
			ArrayList<NodeWrapper> array_element = f_layers.get(i);
				_result.append(array_element.toString()+"\n");
		}
		return _result.toString();
	}

	
	/**
	 * 
	 */
	public void calculatePosition(int xDistance,int yDistance,boolean topToBottom,int xOffset,int yoffset,boolean center) {
		int[] _xPos = new int[f_layers.get(0).size()];
		int[] _yPos = new int[f_layers.size()];
		
		//building sizes
		for(int row=0;row<f_layers.size();row++) {
			ArrayList<NodeWrapper> list = f_layers.get(row);
			for(int col=0;col<list.size();col++) {
				NodeWrapper _n =list.get(col);
				if(_n != null) {
					NodeInterface _node = _n.getNode();
					if(_node != null) {
						if(topToBottom) {
							_xPos[col] = Math.max(_xPos[col], _n.getNode().getSize().width);
							_yPos[row] = Math.max(_yPos[row], _n.getNode().getSize().height);
						}else {
							_xPos[col] = Math.max(_xPos[col], _n.getNode().getSize().height);
							_yPos[row] = Math.max(_yPos[row], _n.getNode().getSize().width);
						}
					}else {
						_xPos[col] = Math.max(_xPos[col], 10);
						_yPos[row] = Math.max(_yPos[row], 10);
					}
				}
			}
		}
		//setting coordinates
		int _x = xDistance+xOffset;
		int _y = yDistance+yoffset;
		for(int row=0;row<f_layers.size();row++) {
			_x = xDistance+xOffset;
			ArrayList<NodeWrapper> list = f_layers.get(row);
			for(int col=0;col<list.size();col++) {
				NodeWrapper _n = list.get(col);
				if(_n != null) {
					if(topToBottom) {	
						_n.setPos(_x+_xPos[col]/2, _y+_yPos[row]/2);
					}else {
						_n.setPos(_y+_yPos[row]/2, _x+_xPos[col]/2);
					}			
					_n.setCellHeight(_yPos[row]);
				}
				_x += _xPos[col]+xDistance;
			}
			_y += _yPos[row]+yDistance;
		}
		f_totalWidth = _x;
		f_totalHeight = _y;
		if(center) {
			int _centeringOffset= 0;
			calculateBaryCentersUp(0);
			//centering from bottom to top
			for(int row=0;row<f_layers.size()-1;row++) {
				ArrayList<NodeWrapper> list = f_layers.get(row);
				for(int col=0;col<list.size();col++) {
					NodeWrapper _n = list.get(col);
					if(_n != null) {
						_centeringOffset = determineCenteringOffset(xDistance,_xPos, row, col, _n,true);
						if(_centeringOffset != 0) {
							applyCenteringOffset(_n,_centeringOffset,true,0,topToBottom);
						}
					}
				}
			}

			calculateBaryCentersDown(0);
			//centering from top to bottom
			for(int row=1;row<f_layers.size();row++) {
				ArrayList<NodeWrapper> list = f_layers.get(row);
				for(int col=0;col<list.size();col++) {
					NodeWrapper _n = list.get(col);
					if(_n != null) {
						_centeringOffset = determineCenteringOffset(xDistance,_xPos, row, col, _n,false);
						if(_centeringOffset != 0) {
							applyCenteringOffset(_n,_centeringOffset,false,0,topToBottom);
						}
					}
				}
			}
		}	
		
	}

	/**
	 * @param _n
	 * @param offset
	 * @param topToBottom 
	 * @param b
	 */
	private void applyCenteringOffset(NodeWrapper node, int offset, boolean up,int depth, boolean topToBottom) {
		List<NodeWrapper> _target = up ? node.getPredecessors() : node.getSuccessors();
		if(_target.size() <= 1 || depth == 0) {
			Point p = node.getPos();
			if(topToBottom) {
				if(node.getNode() != null && (p.x+offset-node.getNode().getSize().width/2 >= 0)) {
					node.setPos(p.x+(offset-node.getMoved()), p.y);
				}
			}else {
				if(node.getNode() != null && (p.x+offset+node.getNode().getSize().height/2 >= 0)) {
					node.setPos(p.x, p.y+(offset-node.getMoved()));
				}
			}
			node.setMoved(offset);
			if(_target.size() <= 1) {
				for(NodeWrapper w:_target) {
					applyCenteringOffset(w, offset, up, depth++,topToBottom);
				}
			}
		}
		
	}

	private int determineCenteringOffset(int xDistance, int[] _xPos,
			int row, int col, NodeWrapper _n,boolean up) {
		int _centeringOffset = 0;
		float f = _n.getBaryCenter();
		f = (f-col);
		
		if(f>0.5f) {
			f = 0.5f;
		}
		if(f<-0.5f) {
			f = -0.5f;
		}
		if((f<0.0)) {
			if(checkFreeLeftRec(row,col,up)) {
				_centeringOffset = (int) (f*(_xPos[col])) - xDistance/2;
			}
		}else if((f>0.0)) {
			if(checkFreeRightRec(row, col,up)) {
				_centeringOffset = (int) (f*(_xPos[col])) + xDistance/2;
			}
		}
		
		return _centeringOffset;
	}

	/**
	 * @param row
	 * @param col
	 */
	private boolean checkFreeLeftRec(int row, int col,boolean up) {
		NodeWrapper _left = null;
		if(col>0) {
			_left = f_layers.get(row).get(col-1);
		}
		if(_left== null) {
			NodeWrapper _me = f_layers.get(row).get(col);
			if((up ? _me.getPredecessors() : _me.getSuccessors()).size() == 0) {
				return true;
			}
			NodeWrapper _leftMostPred = null;
			int _leftMostPredIndex = Integer.MAX_VALUE;
			int _row = row+(up?-1:1);
			if(_row >= 0){
				ArrayList<NodeWrapper> _predLayer = f_layers.get(_row);
				for(NodeWrapper w: up ? _me.getPredecessors() : _me.getSuccessors()) {
					if(_leftMostPred == null) {
						_leftMostPred = w;
						_leftMostPredIndex = _predLayer.indexOf(w);
					}else {
						if(_predLayer.indexOf(w) < _leftMostPredIndex) {
							_leftMostPred = w;
							_leftMostPredIndex = _predLayer.indexOf(w);
						}
					}
				}
				return checkFreeLeftRec(_row, _leftMostPredIndex,up);
			}
			return true;
			
		}
		return false;
	}
	
	/**
	 * @param row
	 * @param col
	 */
	private boolean checkFreeRightRec(int row, int col,boolean up) {
		NodeWrapper _right = null;
		if(col+1<f_layers.get(row).size()) {
			_right = f_layers.get(row).get(col+1);
		}
		if(_right == null) {

			NodeWrapper _me = f_layers.get(row).get(col);
			if((up ? _me.getPredecessors() : _me.getSuccessors()).size() == 0) {
				return true;
			}
			NodeWrapper _rightMostPred = null;
			int _rightMostPredIndex = Integer.MAX_VALUE;
			ArrayList<NodeWrapper> _predLayer = f_layers.get(row+(up?-1:1));
			for(NodeWrapper w: up ? _me.getPredecessors() : _me.getSuccessors()) {
				if(_rightMostPred == null) {
					_rightMostPred = w;
					_rightMostPredIndex = _predLayer.indexOf(w);
				}else {
					if(_predLayer.indexOf(w) > _rightMostPredIndex) {
						_rightMostPred = w;
						_rightMostPredIndex = _predLayer.indexOf(w);
					}
				}
			}
			return checkFreeRightRec(row+(up?-1:1), _rightMostPredIndex,up);
		}
		return false;
	}

	/**
	 * aligns the positions of all nodes
	 */
	public void position() {
		int _maxWidth = 0;
		for (int i = 0; i < f_layers.size(); i++) {
			if(_maxWidth < f_layers.get(i).size()) {
				_maxWidth = f_layers.get(i).size();
			}
		}

		positionUP(_maxWidth);
		positionDOWN(_maxWidth);
		positionUP(_maxWidth);
		//positionDOWN(_maxWidth);
	}

	/**
	 * @param width
	 */
	private void positionUP(int width) {
		for(int l = f_layers.size()-1;l>0;l--) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(l);
			ArrayList<NodeWrapper> layer2 = f_layers.get(l-1);
			calculateBaryCenters(layer1, layer2, false);
			setPriorities(layer2);
			setNodes(layer2,width);
		}
	}

	/**
	 * @param width 
	 * 
	 */
	private void positionDOWN(int width) {
		for(int l = 0;l<f_layers.size()-1;l++) {
			ArrayList<NodeWrapper> layer1 = f_layers.get(l);
			ArrayList<NodeWrapper> layer2 = f_layers.get(l+1);
			calculateBaryCenters(layer1, layer2, true);
			setPriorities(layer2);
			setNodes(layer2,width);
		}
	}

	/**
	 * @param layer2
	 * @param width
	 */
	private void setNodes(ArrayList<NodeWrapper> layer2, int width) {
		for(int i=layer2.size();i<width;i++) {
			//expanding layer
			layer2.add(null);
		}
		NodeWrapper n;
		while((n = getMax(layer2))!= null){
			move(n,layer2);
		}
	}

	/**
	 * defines if the bary-centric ordering starts upwards or downwards
	 * (true = down (default), false = up)
	 * @param value
	 */
	public void setInitialSortDirection(boolean value) {
		f_initialSortDirection = value;
	}
	
	/**
	 * @param n
	 * @param layer2
	 */
	private void move(NodeWrapper n, ArrayList<NodeWrapper> layer2) {
		int _layerPosition = Math.round(n.getBaryCenter());
		int idx;
		while(_layerPosition > layer2.indexOf(n)) {
			//try to move right
			if((idx = canMoveRight(layer2,layer2.indexOf(n)+1)) != -1) {
				layer2.remove(idx);
				layer2.add(layer2.indexOf(n), null);
			}else {
				break;
			}
		}
		while(_layerPosition < layer2.indexOf(n)) {
			//try to move left
			if((idx = canMoveLeft(layer2,layer2.indexOf(n)-1)) != -1) {
				layer2.remove(idx);
				layer2.add(layer2.indexOf(n)+1, null);
			}else {
				break;
			}
		}
		n.setFixed(true);
		n.setPriority(-5.0f);
	}
	
	/**
	 * @param layer2
	 * @param i
	 * @return
	 */
	private int canMoveLeft(ArrayList<NodeWrapper> layer2, int index) {
		for(int i=index;i>=0;i--) {
			NodeWrapper n = layer2.get(i);
			if(n == null) {
				return i;
			}
			if(n.isFixed()) {
				return -1;
			}
			continue;
		}
		return -1;
	}

	/**
	 * @param layer2
	 * @param i
	 * @return
	 */
	private int canMoveRight(ArrayList<NodeWrapper> layer2, int index) {
		for(int i=index;i<layer2.size();i++) {
			NodeWrapper n = layer2.get(i);
			if(n == null) {
				return i;
			}
			if(n.isFixed()) {
				return -1;
			}
			continue;
		}
		return -1;
	}

	/**
	 * gets the node highest priority within this layer
	 * @param layer2
	 * @return
	 */
	private NodeWrapper getMax(ArrayList<NodeWrapper> layer2) {
		NodeWrapper _result = null;
		float _prio = -1;
		for (NodeWrapper n : layer2) {
			if(n != null) {
				if((n.getPriority() > _prio)) {
					_result = n;
					_prio = _result.getPriority();
				}
			}
		}
		return _result;
	}

	/**
	 * @param layer2
	 */
	private void setPriorities(ArrayList<NodeWrapper> layer2) {
		float _maxBC = 0.0f;
		for (NodeWrapper nodeWrapper : layer2) {
			if(nodeWrapper != null) {
				if(nodeWrapper.getBaryCenter() > _maxBC) {
					_maxBC = nodeWrapper.getBaryCenter();
				}
			}
		}
		for(NodeWrapper n:layer2) {
			if(n != null) {
				n.setPriority(n.getBaryCenter() + (!n.isDummyNode() ? _maxBC : 0.0f));
			}
		}
	}

	/**
	 * @param i
	 */
	public boolean isSubLayer(int i) {
		return i<f_layerIsSubLayer.size() && f_layerIsSubLayer.get(i);
	}

	/**
	 * 
	 */
	public void markAllAsHierarchy() {
		for(ArrayList<NodeWrapper> l:f_layers) {
			for(NodeWrapper w:l) {
				if(!w.isDummyNode()) {
					w.markAsHierarchyNode();
				}
			}
		}
	}
	
	public int getWidth() {
		return f_totalWidth;
	}
	
	public int getHeight() {
		return f_totalHeight;
	}

	/**
	 * shortens the edges by moving nodes to lower layers if possible
	 * this also reduces the number of nodes present in the layers
	 */
	public void shortenEdges() {
		//going through all layers from bottom to top
		for(int i = f_layers.size()-2;i>=0;i--) {
			ArrayList<NodeWrapper> _WorkingLayer = f_layers.get(i);
			for(int j=_WorkingLayer.size()-1;j>=0;j--) {
				NodeWrapper w = _WorkingLayer.get(j);
				//is it worthwhile to pull this node down?
				if(!w.isDummyNode() && (w.getPredecessors().size() <= w.getSuccessors().size())) {
					int current = i;
					int index = j;
					while(allDummySuccessors(w)) {
						ArrayList<NodeWrapper> _layer = f_layers.get(current);
						ArrayList<NodeWrapper> _nextLayer = f_layers.get(current+1);
						index = _layer.indexOf(w);
						//okay lets do it!
						_layer.remove(index);
						if(_nextLayer.size()>index) {
							_nextLayer.add(index,w);
						}else {
							_nextLayer.add(w);
						}
						//introduce new dummy nodes for all predecessors
						for(int pred=0;pred<w.getPredecessors().size();pred++) {
							
							NodeWrapper _node = w.getPredecessors().get(pred); //my predecessor
							_node.getSuccessors().remove(w); //remove myself
							//dummy node needed
							NodeWrapper _dn = new NodeWrapper(w.getLayer(),_node,w);
							_layer.add(index,_dn);
							_node.getSuccessors().add(_dn);//adding dummy node to my former predecessor
							
							w.getPredecessors().remove(pred);//updating my own connections
							w.getPredecessors().add(pred,_dn);
							
						}
						for(int suc=w.getSuccessors().size()-1;suc>=0;suc--) {
							NodeWrapper _dummy = w.getSuccessors().get(suc);
							NodeWrapper _target = _dummy.getSuccessors().get(0);
							
							w.getSuccessors().remove(suc);
							w.getSuccessors().add(_target);
							
							_target.getPredecessors().remove(_dummy);
							_target.getPredecessors().add(w);

							_nextLayer.remove(_dummy);
						}
						w.setMinLayer(w.getLayer()+1);
						current++;
						
					}
				}
			}
		}
	}

	/**
	 * @param w
	 * @return
	 */
	private boolean allDummySuccessors(NodeWrapper w) {
		for(NodeWrapper suc:w.getSuccessors()) {
			if(!suc.isDummyNode()) {
				//sorry, lowest possible layer is reached!
				return false;
			}
		}
		return true;
	}

}
