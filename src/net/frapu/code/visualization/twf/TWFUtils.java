/**
 *
 * Process Editor - TWF Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.twf;

import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.general.ColoredFrame;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.freeSpace.FreeSpaceLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

/**
 * @author ff
 *
 */
public class TWFUtils extends ProcessUtils {

    private ArrayList<ProcessLayouter> layouters = null;

    @Override
    public ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target) {
        // No connections to Frame
        if ((source instanceof ColoredFrame) || (target instanceof ColoredFrame)) return null;
        if(!toolCheck(source)) {
        	return null;        	
        } 
        if(!toolCheck(target)) {
        	return null;        	
        } 
        if(source instanceof ToolErrorConnector || target instanceof ToolErrorConnector) {
        	return new ErrorConnection(source,target);
        }
        return new Connection(source,target);
    }
    
    public boolean toolCheck(ProcessNode node) {
    	 if (node instanceof Tool){
         	Tool _tool = (Tool) node;
         	if(_tool.getDefaultConnectionPoints().size() == 0) {
         		return false;
         	}
         } 
    	 return true;
    }

    @Override
    public List<ProcessLayouter> getLayouters() {
        if (layouters == null) {
            layouters = new ArrayList<ProcessLayouter>();
            layouters.add(new GridLayouter(Configuration.getProperties()));
            layouters.add(new FreeSpaceLayouter());
            layouters.add(new SugiyamaLayoutAlgorithm(false,Configuration.getProperties()));
            layouters.add(new SugiyamaLayoutAlgorithm(true,Configuration.getProperties()));
        }
        return layouters;
    }
}
