/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008, 2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.layouter;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;

import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.gui.Workbench;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.WorkBenchSpecific.WorkbenchHandler;
import com.inubit.research.layouter.adapter.ProcessNodeAdapter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

/**
 * @author ff
 *
 */
public class LayoutMenuitemActionListener implements ActionListener,MouseMotionListener {

    private ProcessEditor editor;
    private ProcessLayouter layouter;
    private static Configuration f_conf = Configuration.getInstance();
    private Point f_mousePos = new Point();
    
    /**
     *
     */
    public LayoutMenuitemActionListener(ProcessEditor editor, ProcessLayouter layouter) {
        this.editor = editor;
        this.layouter = layouter;
        this.editor.addMouseMotionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {  
        	long start = System.currentTimeMillis();
            if (f_conf.getProperty(Workbench.CONF_ANIMATION_ENABLED).equals("1")) {
            	LayoutingAnimator anLayouter = new LayoutingAnimator(layouter);
            	anLayouter.layoutModelWithAnimation(editor, null,f_mousePos.x , f_mousePos.y, 0);
            }else {
            	AbstractModelAdapter _model = LayoutUtils.getAdapter(editor.getModel());
            	NodeInterface _selectedNode = new ProcessNodeAdapter(editor.getSelectionHandler().getLastSelectedNode());
            	layouter.setSelectedNode(_selectedNode);
            	layouter.layoutModel(_model, f_mousePos.x , f_mousePos.y, 0);      
            	WorkbenchHandler.postProcess(layouter, editor.getModel());            	
            }
            System.out.println("Model layouted in: " + (System.currentTimeMillis() - start) + " ms");
            
            //Align process model
           //LayoutUtils.alignProcessModel(editor.getModel());
            
            for (ProcessEditorListener l : editor.getListeners()) {
                // Inform Listener about model change
                l.modelChanged(editor.getModel());
            }

        } catch (Exception ex) {
            // Do nothing here
            ex.printStackTrace();
        }
        // Refresh
        editor.repaint();
    }

    

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		f_mousePos = arg0.getPoint();
	}
}
