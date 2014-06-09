/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.gridLayouter;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.interfaces.BPMNEdgeInterface;
import com.inubit.research.layouter.interfaces.BPMNModelInterface;
import com.inubit.research.layouter.interfaces.BPMNNodeInterface;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;
import com.inubit.research.layouter.interfaces.TWFModelInterface;
import com.inubit.research.layouter.preprocessor.DummyEdge;
import com.inubit.research.layouter.preprocessor.LayoutPreprocessor;

/**
 * This Grid Layouter is an implementation of an algorithm
 * presented in the paper "A Simple Algorithm for Automatic Layout of BPMN Processes" by
 * Ingo Kitzmann, Christopher Koenig, Danile Luebke and Leif Singer of the Leibniz University Hannover
 * 
 * It can be applied to BPMN and TWF diagrams. it places all objects along a grid (like in excel).
 * positions of objects are considered while layouting, therefore a user can e.g. switch two pools
 * and relayout.
 * 
 * @author ff
 *
 */
public class GridLayouter extends ProcessLayouter {

    public static final int LAYOUT_RIGHT_GRID_SPACING = 4;
    public static final int BLACKBOX_POOL_HEIGHT = 100;
    /**
     * when debugging or when a closer look onto the algorithm is needed set this to true
     * and recompile. The JPanel is possibly not updated, just resize the frame
     * while layouting to invoke a repaint.
     */
    private boolean f_animated = false;
    private boolean f_debug = false;
    private Hypergrid f_grid;
    private ArrayList<BPMNNodeInterface> f_Pools = new ArrayList<BPMNNodeInterface>(); //pools that only hold lanes
    private int f_maxLaneDepth; //saves the max lane depth, so the left pool padding can be defined appropriately
    private Properties f_props;
    private ArrayList<BPMNNodeInterface> f_LayoutPools = new ArrayList<BPMNNodeInterface>(); //pools that contain layoutable objects
    private ArrayList<SubProcessModel> f_subProcesses = new ArrayList<SubProcessModel>();
    private boolean f_rectify = true;
    /**
     * needed for pool layouting
     */
    private HashMap<BPMNNodeInterface, Point> f_posCache = new HashMap<BPMNNodeInterface, Point>();
    private HashMap<BPMNNodeInterface, Dimension> f_sizeCache = new HashMap<BPMNNodeInterface, Dimension>();
    //saves the lane depth of a cluster so the position can be determined correctly
    private HashMap<BPMNNodeInterface, Integer> f_levelCache = new HashMap<BPMNNodeInterface, Integer>();
    //saves to which Pool a Lane belongs, so the distance can be correctly calculated in Hypergrid
    private HashMap<BPMNNodeInterface, Integer> f_PoolIDCache = new HashMap<BPMNNodeInterface, Integer>();
    private int f_twfMaxWidth = 800;
    private boolean f_manual = false;
    private boolean f_synchronizePools = false;
    private boolean f_routeMSF = false;
    private GridLayouterEdgeRouter f_edgeRouter = null;
    private AbstractModelAdapter f_model; // the model to be layouted
    public static final int POOL_PADDING_LEFT = 20;
    public static final int POOL_PADDING_RIGHT = 20;
    public static final int POOL_PADDING_Y = 20;

    /**
     * standard Inubit Workbench constructor
     * all settings will be extracted from the properties
     */
    public GridLayouter(Properties properties) {
        super();
        f_props = properties;

    }

    /**
     * all configuration parameters are set here.
     * use this constructor in an IS environment!
     */
    public GridLayouter(boolean rectifyEdges, int maxTWFwidth, boolean synchronizePools) {
        this(rectifyEdges, maxTWFwidth, synchronizePools, FlowObjectWrapper.getSpacingX(), FlowObjectWrapper.getSpacingY());
    }

    /**
     * all configuration parameters are set here.
     * use this constructor in an IS environment!
     * *should not be needed anymore Olli now uses a Property Object too!
     */
    public GridLayouter(boolean rectifyEdges, int maxTWFwidth, boolean synchronizePools, int spacingX, int spacingY) {
        super();
        f_manual = true;
        FlowObjectWrapper.setSpacingX(spacingX);
        FlowObjectWrapper.setSpacingY(spacingY);
        f_rectify = rectifyEdges;
        f_twfMaxWidth = maxTWFwidth;
        f_synchronizePools = synchronizePools;
    }

    /**
     * layouts the given model.
     * the start coordinates and direction will be ignored as they are not needed.
     */
    @Override
    public void layoutModel(AbstractModelAdapter model, int xstart, int ystart, int direction) throws Exception {
        f_model = model;
        if (!f_manual) {
            f_rectify = "1".equals(f_props.getProperty(LayoutHelper.CONF_RECTIFY));
            f_routeMSF = "1".equals(f_props.getProperty(LayoutHelper.CONF_ROUTE_MESSAGEFLOW));
            f_twfMaxWidth = LayoutHelper.toInt(f_props.getProperty(LayoutHelper.CONF_MAX_TWF_WIDTH, "800"), 800);
            f_synchronizePools = "1".equals(f_props.getProperty(LayoutHelper.CONF_SYNC_POOLS));
            FlowObjectWrapper.setSpacingX(LayoutHelper.toInt(f_props.getProperty(LayoutHelper.CONF_X_DISTANCE_GRID, "20"), 20));
            FlowObjectWrapper.setSpacingY(LayoutHelper.toInt(f_props.getProperty(LayoutHelper.CONF_Y_DISTANCE_GRID, "10"), 10));
        } else {
            //so SubProcessModel layouters can access it
            f_props = new Properties();
            f_props.put(LayoutHelper.CONF_RECTIFY, f_rectify ? "1" : "0");
            f_props.put(LayoutHelper.CONF_ROUTE_MESSAGEFLOW, f_routeMSF ? "1" : "0");
            f_props.put(LayoutHelper.CONF_MAX_TWF_WIDTH, f_twfMaxWidth);
            f_props.put(LayoutHelper.CONF_SYNC_POOLS, f_synchronizePools ? "1" : "0");
            f_props.put(LayoutHelper.CONF_X_DISTANCE_GRID, "" + FlowObjectWrapper.getSpacingX());
            f_props.put(LayoutHelper.CONF_Y_DISTANCE_GRID, "" + FlowObjectWrapper.getSpacingY());
        }
        f_edgeRouter = null;
        FlowObjectWrapper.clear();
        f_subProcesses.clear();
        f_LayoutPools.clear();
        f_maxLaneDepth = 0;
        f_Pools.clear();
        f_posCache.clear();
        f_sizeCache.clear();
        f_levelCache.clear();
        f_PoolIDCache.clear();
        final BPMNDataObjectStackProcessor _stackProcessor = new BPMNDataObjectStackProcessor();
        LayoutPreprocessor.process(model);

        handleSubProcesses(model);

        //topological sorting
        final ModifiedTopologicalSorter _m = new ModifiedTopologicalSorter(model);
        if (f_animated) {
            printTopologicalOrder(_m);
        }
        final List<NodeInterface> _sorted = _m.getSortedNodes();
        //Pool identification
        findPools(_sorted);
        Collections.sort(f_LayoutPools, new YPositionComparator());
        //initialization
        f_grid = new Hypergrid(f_LayoutPools, this, f_PoolIDCache);
        f_grid.positionGrids(f_LayoutPools, model);
        //each node gets assigned to the correct grid
        assignPools(model, f_LayoutPools);

        //set starting points for layout
        FlowObjectWrapper _start = null;
        for (int i = 0; i < _sorted.size(); i++) {
            _start = FlowObjectWrapper.getFlowObjectWrapper(_sorted.get(i), _m.getModel());
            if (model.getNodes().contains(_sorted.get(i))) { //otherwise special handling will be applied
                if (_start.getPredecessors().size() == 0) {
                    f_grid.addRow(0, _start.getGrid());
                    f_grid.setObject(0, 0, _start);
                    if (_start.isSplit()) {
                        prepareSuccessors(_start);
                    }
                    _sorted.remove(i);
                    i--;
                } else {
                    //break;
                }
            }
        }
        //extracting stacks after start nodes were placed
        //so all flowobjectwrappers were initialized and all links were build
        if (model instanceof BPMNModelInterface) {
            _stackProcessor.extractStackedDataObjects(model);
        }

        //layouting all objects
        if (f_animated) {
            final AbstractModelAdapter _model2 = model;
            final GridLayouter _lay = this;
            new Thread() {

                public void run() {
                    performLayout(_model2, _m, _sorted);
                    if (_model2 instanceof BPMNModelInterface) {
                        _stackProcessor.LayoutStacks(_model2, f_grid, _lay);
                    }
                    interleave();
                    displayChanges(_model2);
                    System.out.println("finished layouting!");
                }
            }.start();
        } else {
            performLayout(model, _m, _sorted);
            if (model instanceof BPMNModelInterface) {
                _stackProcessor.LayoutStacks(model, f_grid, this);
            }
            interleave();
            displayChanges(model);
        }


        layoutPools(model);
        //route edges afterward, edges in sub process are already layouted by the recursive process
        for (EdgeInterface e : model.getEdges()) {
            e.clearRoutingPoints();
        }
        if (f_rectify) {
            f_edgeRouter = new GridLayouterEdgeRouter(this, model);
            f_edgeRouter.routeEdges();
        }
        if ((model instanceof TWFModelInterface)) {
            respectMaxWidth();
        }
        //reinserting sub processes
        for (SubProcessModel spm : f_subProcesses) {
            spm.reInsertSubProcess(model);
        }
        if (f_rectify && f_routeMSF) {
            f_edgeRouter.routeMessageFlows();
        }
        _m.restoreEdges();
        LayoutPreprocessor.unprocess(model);

    }

    /**
     *
     */
    private void respectMaxWidth() {
        int _width = Math.max(400, f_twfMaxWidth);
        Grid _g = f_grid.getGrid(0);
        //okay it is necessary
        if ((_g.getColCount() > 1) && (_g.getGridWidth() > _width)) {
            int height = _g.getGridHeight() + Hypergrid.GRID_DISTANCE_Y / 2;

            int _w = _g.getColSize(0);
            for (int c = 1; c < _g.getColCount(); c++) {
                if (_w + _g.getColSize(c) > _width) {
                    //make a break here
                    addToAllNodes(_g, c, -_w, height);
                    _w = _g.getColSize(c);
                } else {
                    _w += _g.getColSize(c);
                }
            }
            _w = 0;
            int _gridY = _g.getGridYOffset();
            for (int c = 0; c < _g.getColCount(); c++) {
                if (_w + _g.getColSize(c) > _width) {
                    routeEdgeBreak(_g, Math.max(c - LAYOUT_RIGHT_GRID_SPACING, 0), _gridY, (_width - _w));
                    _gridY += height;
                    _w = _g.getColSize(c);
                } else {
                    _w += _g.getColSize(c);
                }
            }
        }
    }

    /**
     * @param _g
     * @param c
     */
    private void routeEdgeBreak(Grid g, int col, int gridY, int spaceToborder) {
        //adding routing point so it looks better
        Point _p;
        int _xMin = Integer.MAX_VALUE;

        int _gH = g.getGridHeight();
        int off = spaceToborder - 10;
        for (int row = 0; row < g.getRowCount(); row++) {
            if (g.getObject(row, col) != null) {
                FlowObjectWrapper _obj = g.getObject(row, col);
                for (EdgeInterface e : _obj.getSuccessorEdges()) {
                    Point _start = ((NodeInterface) e.getSource()).getPos();
                    _start.x += ((NodeInterface) e.getSource()).getSize().width / 2;
                    Point _end = ((NodeInterface) e.getTarget()).getPos();
                    _end.x += ((NodeInterface) e.getTarget()).getSize().width / 2;

                    List<Point> _rps = new ArrayList<Point>();
                    int _yDist = ((NodeInterface) e.getTarget()).getPos().y - ((NodeInterface) e.getSource()).getPos().y;
                    //to the right of "upper" node
                    //translating to the right
                    _p = _start;
                    _p.translate(off + 10, 0);
                    _rps.add(_p);
                    //translating down
                    _p = new Point(_p);
                    int _yOffset = _gH - (_obj.getWrappedObject().getPos().y - gridY) + 20;//20 = label offset
                    _p.translate(0, _yOffset);
                    _rps.add(_p);
                    //to the left of "lower" node
                    _p = _end;
                    if (_p.x > _xMin) {
                        _p.x = _xMin;
                    } else if (_p.x < _xMin) {
                        _xMin = _p.x;
                    }
                    //translating left
                    _p.translate(-20 - ((NodeInterface) e.getTarget()).getSize().width, 0);
                    _rps.add(_rps.size(), _p);
                    //translating up
                    _p = new Point(_p);
                    _p.translate(0, -(_yDist - _yOffset));
                    _rps.add(_rps.size() - 1, _p);
                    off += 6;
                    e.setRoutingPoints(_rps);
                }
            }
        }
    }

    /**
     * @param _g
     * @param c
     * @param i
     * @param height
     */
    private void addToAllNodes(Grid g, int colToStartAt, int x, int y) {
        for (int i = colToStartAt; i < g.getColCount(); i++) {
            for (int row = 0; row < g.getRowCount(); row++) {
                if (g.getObject(row, i) != null) {
                    NodeInterface _node = g.getObject(row, i).getWrappedObject();
                    Point _p = _node.getPos();
                    _p.translate(x, y);
                    _node.setPos(_p.x, _p.y);
                }
            }
        }
    }

    /**
     * handles the subProccesses.
     * It first determines all subProcesses, removes all nodes which are contained in them
     * and create a new temporary model out of them. This temporary model is then layouted (recursion).
     * Later the subprocess is set to its appropriate size (depending on the size of the temporary model
     * and all elements are reinserted.
     * @param model
     * @throws Exception
     */
    private void handleSubProcesses(AbstractModelAdapter model) throws Exception {
        List<NodeInterface> _list = new ArrayList<NodeInterface>(model.getNodes());
        for (int i = 0; i < _list.size(); i++) {
            if (_list.get(i) instanceof BPMNNodeInterface) {
                BPMNNodeInterface _n = (BPMNNodeInterface) _list.get(i);
                if (_n.isSubProcess()) {
                    if (_n != null && _n.getContainedNodes().size() > 0) {
                        f_subProcesses.add(new SubProcessModel(_n, model, this));
                    }
                }
            }
        }
    }

    /**
     * moves and sizes pools and lanes so they are fit around their content.
     * @param model
     * @param model
     *
     */
    private void layoutPools(AbstractModelAdapter model) {
        for (NodeInterface n : f_LayoutPools) {//lanes first (everything containing nodes other then lanes)
            layoutCluster(n, model, true);
        }
        f_Pools.removeAll(f_LayoutPools);//then pools (everything containing only lanes or nothing)
        for (NodeInterface n : f_Pools) {
            layoutCluster(n, model, false);
        }
    }

    /**
     * sets dimension and position for a Pool/Lane.
     * It is important to set the dimension first, otherwise problems
     * in the IS porting can occur.
     * @param n - the Cluster to be layouted
     */
    private void layoutCluster(NodeInterface n, AbstractModelAdapter model, boolean addPadding) {
        BPMNNodeInterface _cluster = (BPMNNodeInterface) n;
        Point _min = new Point();
        Point _max = new Point();
        int _idx = f_LayoutPools.indexOf(n) + 1;
        Grid _g = f_grid.getGrid(_idx);
        _min.x = Integer.MAX_VALUE;
        _min.y = Integer.MAX_VALUE;
        if (_cluster.getContainedNodes().size() == 0) {
            _min.x = _g.getGridXOffset();
            _min.y = _g.getGridYOffset();
            //subtract Pool Padding as it is already included in the
            //other nodes
            _max.x = _min.x + Math.max(Math.max(getGridWidth(_idx - 1), getGridWidth(_idx + 1)), (_cluster.isVertical() ? BLACKBOX_POOL_HEIGHT : 300)) - POOL_PADDING_LEFT;
            _max.y = _min.y + BLACKBOX_POOL_HEIGHT - 2 * POOL_PADDING_Y;
        }
        for (NodeInterface cont : _cluster.getContainedNodes()) {
            FlowObjectWrapper _node = FlowObjectWrapper.getFlowObjectWrapper(cont, model);
            Point _pos_min = getPos(_node);
            _pos_min.x -= getSize(_node).width / 2;
            _pos_min.y -= getSize(_node).height / 2;
            Point _pos_max = getPos(_node);
            _pos_max.x += getSize(_node).width / 2;
            _pos_max.y += getSize(_node).height / 2;

            _min.x = Math.min(_min.x, _pos_min.x);
            _min.y = Math.min(_min.y, _pos_min.y);
            _max.x = Math.max(_max.x, _pos_max.x);
            _max.y = Math.max(_max.y, _pos_max.y);
        }
        Dimension _d = new Dimension(_max.x - _min.x + 3, _max.y - _min.y);
        if (addPadding) {
            if (_cluster.isVertical()) {
                _d.height += f_levelCache.get(_cluster) * POOL_PADDING_Y + POOL_PADDING_Y;
                _d.width += 3;// POOL_PADDING_LEFT+POOL_PADDING_RIGHT;
            } else {
                _d.width += f_levelCache.get(_cluster) * POOL_PADDING_LEFT + POOL_PADDING_RIGHT;
                _d.height += 2 * POOL_PADDING_Y;
            }
        }
        f_sizeCache.put(_cluster, _d);
        _cluster.setSize(_d.width, _d.height);

        Point _p = new Point((_min.x + _max.x) / 2, (_min.y + _max.y) / 2);
        if (addPadding) {
            if (_cluster.isVertical()) {
                _p.y += (POOL_PADDING_Y - f_levelCache.get(_cluster) * POOL_PADDING_Y) / 2;
            } else {
                _p.x += (POOL_PADDING_RIGHT - f_levelCache.get(_cluster) * POOL_PADDING_LEFT) / 2;
            }
        }
        f_posCache.put(_cluster, _p);
        _cluster.setPos(_p.x, _p.y);
    }

    /**
     * @param _node
     * @return
     */
    private Point getPos(FlowObjectWrapper _node) {
        if (f_posCache.containsKey(_node.getWrappedObject())) {
            return new Point(f_posCache.get(_node.getWrappedObject()));
        }
        return _node.getWrappedObject().getPos();
    }

    /**
     * @param _node
     * @return
     */
    private Dimension getSize(FlowObjectWrapper _node) {
        if (f_sizeCache.containsKey(_node.getWrappedObject())) {
            return f_sizeCache.get(_node.getWrappedObject());
        }
        return _node.getSize();
    }

    /**
     * @param i
     * @return
     */
    private int getGridWidth(int i) {
        if ((i >= 0) && (i < f_grid.getNumOfGrids())) {
            Grid _g = f_grid.getGrid(i);
            return _g.getGridWidth();
        }
        return 300;
    }

    /**
     * @return the f_maxLaneDepth
     */
    public int getMaxLaneDepth() {
        return f_maxLaneDepth;
    }

    /**
     * saves the pool number to each FlowObjectWrapper.
     * s
     * @param _sorted
     * @param pools
     */
    private void assignPools(AbstractModelAdapter model, ArrayList<BPMNNodeInterface> pools) {
        int i = 0;
        for (BPMNNodeInterface n : pools) {
            i++;
            for (NodeInterface _con : n.getContainedNodes()) {
                FlowObjectWrapper _fow = FlowObjectWrapper.getFlowObjectWrapper(_con, model);
                _fow.setGrid(i);
            }
        }
    }

    /**
     * scans all nodes for pools, deletes them and saves them in the f_LayoutPools list
     * @param _sorted
     * @return
     */
    private void findPools(List<NodeInterface> _sorted) {
        boolean _foundPool = true;
        int _poolID = 0; //so lanes can be related to their parent pool
        outer:
        while (_foundPool) {
            for (int i = 0; i < _sorted.size(); i++) {
                NodeInterface n = _sorted.get(i);
                if (n instanceof BPMNNodeInterface) {
                    BPMNNodeInterface _n = (BPMNNodeInterface) n;
                    if (_n.isPool()) {
                        addClustersRecursively(_n, _sorted, 0, _poolID++);
                        _foundPool = true;
                        continue outer;
                    }
                }
            }
            _foundPool = false;
        }
    }

    /**see layoutPools
     * @param sortedNodes
     * @param _n
     */
    private void addClustersRecursively(BPMNNodeInterface cluster, List<NodeInterface> sortedNodes, int level, int poolID) {
        if (!isLaneHolderOnly(cluster)) {//meaning it contains nodes
            //this pools layout has to be determined first
            f_LayoutPools.add(cluster);
        } else {
            //as he is a lane holder only, all contained nodes are lanes -> Clusters
            for (NodeInterface n : cluster.getContainedNodes()) {
                BPMNNodeInterface _node = (BPMNNodeInterface) n;
                addClustersRecursively(_node, sortedNodes, level + 1, poolID);
            }
        }
        f_levelCache.put(cluster, level);
        f_PoolIDCache.put(cluster, poolID);
        f_Pools.add(cluster);
        sortedNodes.remove(cluster);//no usual layout anymore
        f_maxLaneDepth = Math.max(f_maxLaneDepth, level + 1);
    }

    /**
     * Find out if a given Cluster only contains Lanes
     * (e.g. first level pool) and has no other BPMNObjects except for lanes
     * @param _n
     * @return
     */
    private boolean isLaneHolderOnly(BPMNNodeInterface _n) {
        if (_n.getContainedNodes().size() == 0) {
            return false;
        }
        for (NodeInterface n : _n.getContainedNodes()) {
            BPMNNodeInterface n2 = (BPMNNodeInterface) n;
            if (!n2.isPool() && !n2.isLane()) {
                return false;
            }
        }
        return true;
    }

    /**
     * the main loop, which assigns a spot in the grid to every node
     * @param model
     * @param _m
     * @param _sorted
     */
    private void performLayout(AbstractModelAdapter model, final ModifiedTopologicalSorter _m, final List<NodeInterface> _sorted) {
        for (NodeInterface n : _sorted) {
            if (model.getNodes().contains(n)) {//if not, special handling will be performed somewhere else
                FlowObjectWrapper _fow = FlowObjectWrapper.getFlowObjectWrapper(n, _m.getModel());
                if (f_animated) {
                    System.out.println("Layouting: " + _fow.getWrappedObject().getText()
                            + " (" + _fow.getWrappedObject().getClass().getSimpleName() + ")");
                }
                layout(_fow);
                if (f_debug) {
                    f_grid.printToConsole();
                    System.out.println("---------------------------------------------------------");
                    System.out.println();
                }
                if (f_animated) {
                    try {
                        displayChanges(model);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * interleaves the grid (see paper) to reduce space consumption
     */
    private void interleave() {
        f_grid.interleave();
    }

    /**
     * turns the grid position into actual coordinates and stores them in the nodes.
     * @param model
     */
    private void displayChanges(AbstractModelAdapter model) {
        f_grid.calculateSizes(f_synchronizePools);
        f_grid.applyCoordinates();
        for (EdgeInterface _edge : model.getEdges()) {
            _edge.clearRoutingPoints();
        }
    }

    /**
     * standard layout for all nodes
     * @param _fow
     */
    private void layout(FlowObjectWrapper _fow) {
        if (LayoutHelper.isDataObject(_fow)) {
            layoutDataObject(_fow);
        } else {
            if (!_fow.isJoin()) {
                if (_fow.hasRecommendedPosition()) {
                    layoutRecommendation(_fow);
                } else {
                    layoutRight(_fow);
                }
            } else {
                layoutJoin(_fow);
            }
        }
        if (_fow.isSplit()) {
            prepareSuccessors(_fow);
        }
    }

    /**
     * layouts a node according to the recommendation given by "prepareSuccessors"
     * @param _fow
     */
    private void layoutRecommendation(FlowObjectWrapper _fow) {
        Point _p = _fow.getRecommendedPosition();
        if (_p.y < 0) {
            _p.y = 0;
        }
        if (f_grid.getColCount(_fow.getGrid()) <= _p.x) {
            f_grid.addCol(_p.x, _fow.getGrid());
        }
        if (_fow.getNeedToAddRow()) {
            f_grid.addRow(_p.y, _fow.getGrid());
        }
        f_grid.setObject(_p.y, _p.x, _fow);
    }

    /**
     * places and layout nodes which have an inflow >1
     * @param _fow
     */
    private void layoutJoin(FlowObjectWrapper _fow) {
        int x = 0;
        int y = 0;
        for (FlowObjectWrapper _pred : _fow.getPredecessors()) {
            Point _p = _pred.getPosition();
            x = Math.max(x, _p.x);
            y += _p.y;
        }
        x += LAYOUT_RIGHT_GRID_SPACING;
        y /= _fow.getPredecessors().size();
        boolean _usedLine = false;
        //override to place in line
        for (FlowObjectWrapper _pred : _fow.getPredecessors()) {
            if (_pred.isSplit() && !_pred.isJoinAlreadyPlaced()) {
                if (checkJoinSplitCorrespondence(_fow, _pred)) {
                    y = _pred.getPosition().y;
                    //position in same row
                    _pred.setJoinAlreadyPlaced(true);
                    _usedLine = true;
                }
            }
        }
        //override to place how recommended (only if not already placed in line)
        if (!_usedLine && _fow.hasRecommendedPosition()) {
            //y = _fow.getRecommendedPosition().y;
        }


        if (f_grid.getRowCount(_fow.getGrid()) <= y) {
            f_grid.addRow(y, _fow.getGrid());
        }
        f_grid.addCol(x, _fow.getGrid());
        f_grid.setObject(y, x, _fow);
    }

    /**
     * @param _fow
     * @param _pred
     * @return
     */
    private boolean checkJoinSplitCorrespondence(FlowObjectWrapper node, FlowObjectWrapper split) {
        if (node.equals(split)) {
            return true;
        }
        if (node.getPredecessors().size() == 0) {
            return false;
        }
        for (FlowObjectWrapper p : node.getPredecessors()) {
            if (!checkJoinSplitCorrespondence(p, split)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used when the FlowObject is a split.
     * the position for following objects will be prepared, but is not final yet
     * and can be changed by the object itself.
     * @param _fow
     */
    private void prepareSuccessors(FlowObjectWrapper _fow) {
        int _succCount = _fow.getSuccessors().size();
        for (EdgeInterface e : _fow.getSuccessorEdges()) {
            if (e instanceof BPMNEdgeInterface) {
                if (e instanceof DummyEdge || ((BPMNEdgeInterface) e).isMessageFlow()) {
                    _succCount--;
                }
            }
        }
        if (_succCount > 0) {
            int _y = -_succCount / 2;
            int _x = LAYOUT_RIGHT_GRID_SPACING;
            boolean _leaveMiddleRowFree = false;
            for (FlowObjectWrapper o : _fow.getSuccessors()) {
                if (o.isJoin()) {
                    _leaveMiddleRowFree = true;
                    break;
                }
            }

            for (int i = 0; i < _succCount; i++) {
                FlowObjectWrapper _w = _fow.getSuccessors().get(i);
                if ((_y < 0)) {
                    _w.recommendPosition(_x, ++_y, _fow);
                    continue;
                }
                if ((_y == 0)) {
                    if (_succCount % 2 == 0 || _leaveMiddleRowFree) {
                        _y++;//skip row
                    } else {
                        _w.recommendPosition(_x, _y++, false, _fow);
                        continue;
                    }
                }

                if ((_y > 0)) {
                    _w.recommendPosition(_x, _y++, _fow);
                }
            }
        }
    }

    /**
     * simplest layouting case. If a FlowObject just has 1 predecessor
     * it is layouted directly next to it.
     * @param _fow
     */
    private void layoutRight(FlowObjectWrapper _fow) {
        //simplest case
        if (_fow.getPredecessorsSizeInGrid() == 1) {
            for (FlowObjectWrapper f : _fow.getPredecessors()) {
                if (!(f.getWrappedObject() instanceof BPMNNodeInterface)
                        || !(((BPMNNodeInterface) f.getWrappedObject()).isDataObject()
                        || ((BPMNNodeInterface) f.getWrappedObject()).isPool())) {
                    Point _p = f.getPosition();
                    f_grid.addCol(_p.x + LAYOUT_RIGHT_GRID_SPACING, _fow.getGrid());

                    //special handling for attached intermediate events
                    int idx = f.getSuccessors().indexOf(_fow);
                    if (idx >= 0) {
                        if (f.getSuccessorEdges().get(idx) instanceof DummyEdge) {
                            //aha!
                            f_grid.addRow(_p.y + 1, _fow.getGrid());
                            _p.x -= 1; //special placement so it does not interfere with a branched flow (AND, OR)
                            _p.y += 1;
                        }
                    }
                    //--------------

                    f_grid.setObject(_p.y, _p.x + LAYOUT_RIGHT_GRID_SPACING, _fow);
                    break;
                }
            }
        } else {
            boolean layouted = false;
            //finding predecessor in own grid
            Point _p = null;
            for (FlowObjectWrapper f : _fow.getPredecessors()) {
                if (f.getGrid() == _fow.getGrid()) {
                    _p = f.getPosition();
                }
            }
            if (_p != null) {
                //has a predecessor in own grid, just align with x position
                for (FlowObjectWrapper f : _fow.getPredecessors()) {
                    if (f.getGrid() != _fow.getGrid()) {
                        _p.x = Math.max(_p.x, f.getPosition().x);
                    }
                }
            } else {
                //depends entirely on other grids
                _p = new Point();
                for (FlowObjectWrapper f : _fow.getPredecessors()) {
                    if (f.getGrid() != _fow.getGrid()) {
                        _p.x = Math.max(_p.x, f.getPosition().x);
                        if (f.getGrid() < _fow.getGrid()) {
                            _p.y = 0;
                        } else {
                            _p.y = Math.max(0, f_grid.getRowCount(_fow.getGrid()) - 1);
                        }
                    }
                }
            }
            if (_p != null) {
                f_grid.addCol(_p.x + LAYOUT_RIGHT_GRID_SPACING, _fow.getGrid());
                f_grid.setObject(_p.y, _p.x + LAYOUT_RIGHT_GRID_SPACING, _fow);
                layouted = true;
            }


            if (!layouted) {
                //no idea how to layout
                f_grid.addRow(0, _fow.getGrid());
                f_grid.setObject(0, 0, _fow);
                System.out.println("could not layout " + _fow.getWrappedObject());
            }
        }
    }

    /**
     * special handling for data objects, they are usually places on the upper right
     * of their associated nodes. To ensure that this space is still free, they are places
     * outside of the standard grid (multiples of Layout_right_grid_spacing)
     * @param _fow
     */
    protected void layoutDataObject(FlowObjectWrapper _fow) {
        Point _p;
        if (LayoutHelper.isDataObject(_fow)) {
            _fow.setMoveMode(-1);
            //experimental getting mean position of all predecessors
            if (true) {
                _p = new Point();
                int _count = 0;
                for (FlowObjectWrapper wrap : _fow.getPredecessors()) {
                    if (wrap.getGrid() == _fow.getGrid()) {
                        _p.x += wrap.getPosition().x;
                        _p.y += wrap.getPosition().y;
                        _count++;
                    }
                }
                if (_count > 0) {
                    _p.x /= _count;
                    _p.y /= _count;
                }
            } else {
                _p = _fow.getPredecessors().get(0).getPosition();
            }
            _p.x++;
            f_grid.addCol(_p.x, _fow.getGrid());
            if (_fow.hasRecommendedPosition()) {
                if (_fow.getRecommendedPosition().y > _p.y) {
                    //layouting on the down side
                    _p.y++;
                }
            } else {
                if (_fow.getWrappedObject() instanceof BPMNNodeInterface) {
                    if (!((BPMNNodeInterface) _fow.getWrappedObject()).placeDataObjectUpwards()) {
                        _p.y++;
                    } else {
                        _p.y--;
                    }
                }
            }
            if (_p.y < 0) {
                _p.y++;
                f_grid.addRow(_p.y, _fow.getGrid());
            }
            if (_p.y >= f_grid.getGrid(_fow.getGrid()).getRowCount()) {
                f_grid.addRow(_p.y, _fow.getGrid());
            } else {
                if (f_grid.getGrid(_fow.getGrid()).getObject(_p.y, _p.x) != null) {
                    _p.x--;
                    _fow.setMoveMode(0);
                    if (f_grid.getGrid(_fow.getGrid()).getObject(_p.y, _p.x) != null) {
                        _p.x--;
                        _fow.setMoveMode(+1);
                        if (_p.x < 0 || f_grid.getGrid(_fow.getGrid()).getObject(_p.y, _p.x) != null) {
                            _p.x += 2;
                            _fow.setMoveMode(-1);
                            f_grid.addRow(_p.y, _fow.getGrid());
                        }
                    }
                } else {
                    //the first position is free, but could be inside of a flow -> check and add row
                    int _start = Math.max(_p.x - 1, 0);
                    int _end = Math.min(_p.x + 1, f_grid.getColCount(_fow.getGrid()));
                    for (int i = _start; i < _end; i++) {
                        if (f_grid.getGrid(_fow.getGrid()).getObject(_p.y, i) != null) {
                            f_grid.addRow(_p.y + 1, _fow.getGrid());
                            _p.y++;
                            break;
                        }
                    }
                }
            }
            f_grid.setObject(_p.y, _p.x, _fow);
        }
    }

    /**
     * used for debugging
     * @param _m
     */
    private void printTopologicalOrder(ModifiedTopologicalSorter _m) {
        System.out.println("Topological Order: ");
        List<NodeInterface> _l = _m.getSortedNodes();
        for (NodeInterface n : _l) {
            System.out.println(n.getText() + " (" + n.getClass().getSimpleName() + ")");
        }
    }

    @Override
    public String getDisplayName() {
        return "Grid Layouter";
    }

    @Override
    public void setSelectedNode(NodeInterface selectedNode) {
        //not needed for this layouter
    }

    public Hypergrid getGrids() {
        return f_grid;
    }

    /**
     * @return
     */
    public Properties getProperties() {
        return f_props;
    }

    /**
     * used only within ProcessEditor so far.
     * To all of those edges the standard layout algorithm (by Uwe) is applied)
     */
    @Override
    public List<EdgeInterface> getUnroutedEdges() {
        List<EdgeInterface> _result = new ArrayList<EdgeInterface>(f_model.getEdges());
        for (SubProcessModel spm : f_subProcesses) {
            _result.removeAll(spm.getEdges());
            _result.addAll(spm.getUnroutedEdges());
        }
        if (f_edgeRouter != null) {
            _result.removeAll(f_edgeRouter.getRoutedEdges());
        }

        return _result;


    }
}
