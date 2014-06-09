/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter;

import java.util.List;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.EdgeInterface;
import com.inubit.research.layouter.interfaces.NodeInterface;


/**
 *
 * Provides an interface for encapsulating ProcessModel layouters
 *
 * @author frank
 */
public abstract class ProcessLayouter{

    public final static int LAYOUT_VERTICAL = 0;
    public final static int LAYOUT_HORIZONTAL = 1;
    
    
    /**
     * Layouts the model - changes the x and y positions of all elements.
     * @param model
     * @param direction One of LAYOUT_VERTICAL or LAYOUT_HORIZONTAL
     * @throws net.frapu.code.visualization.UnsupportedModelTypeException
     */
    public abstract void layoutModel(AbstractModelAdapter model, int xstart, int ystart, int direction) throws Exception;

    public void layoutModel(AbstractModelAdapter model) throws Exception{
    	layoutModel(model, 0, 0, LAYOUT_HORIZONTAL);
    }

    public abstract String getDisplayName();

    /**
     * @param selectedNode
     */
    public abstract void  setSelectedNode(NodeInterface selectedNode);
    
    /**
     * hook, can be overwritten by layouters to provide a list
     * of edges that should be routed by the tools default edge layouter.
     * @return
     */
    public List<EdgeInterface> getUnroutedEdges(){
    	return null;
    }

	

}
