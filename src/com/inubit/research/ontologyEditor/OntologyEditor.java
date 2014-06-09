/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ontologyEditor;

import java.awt.Event;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import com.inubit.research.animation.Animator;
import com.inubit.research.animation.LayoutingAnimator;
import com.inubit.research.animation.NodeAnimator;
import com.inubit.research.animation.PolarNodeAnimator;
import com.inubit.research.layouter.LayoutHelper;
import com.inubit.research.layouter.adapter.ProcessNodeAdapter;
import com.inubit.research.layouter.radial.RadialDistanceRing;
import com.inubit.research.layouter.radial.RadialLayouter;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.Dragable;
import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessEditorListener;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.ontology.Concept;
import net.frapu.code.visualization.ontology.OntologyModel;


/**
 * @author ff
 *
 */
public class OntologyEditor extends ProcessEditor implements ProcessEditorListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7307269278782425203L;
	private Animator f_animator;
	private List<NodeAnimator> f_wrappers;
	
	public OntologyEditor() {
		init();
		OntologyModel _o = new OntologyModel("test");
		final Concept c = new Concept("test");
		_o.addNode(c);
		c.setPos(200,100);
		
		PolarNodeAnimator _w = new PolarNodeAnimator(c,f_animator);
		_w.setCenter(new Point(200,200));
		_w.setNewCoords(new Point(200, 500));
		f_wrappers.add(_w);
		this.setModel(_o);
	}
	
	private void init() {
		this.addListener(this);
		f_wrappers = new ArrayList<NodeAnimator>();
		f_animator = new Animator(this,60);
		f_animator.setAnimationObjects(f_wrappers);
		f_animator.start();
	}

	/**
	 * 
	 */
	public OntologyEditor(OntologyModel m) {
		init();
		this.setModel(m);
		this.setAnimationEnabled(true);
	}
	
	public static void main(String[] args) {
		
		OntologyEditor b = new OntologyEditor();
		b.setVisible(true);
	}

	
	
	@Override
	public void processObjectDoubleClicked(ProcessObject obj) {
        if (!(obj instanceof ProcessNode)) return;
        ProcessNode node = (ProcessNode)obj;
     
		this.removeProcessHelperClass(RadialDistanceRing.class);
		ProcessModel _copy = this.getModel().clone();
		//performing layout on copy
		RadialLayouter _layouter = new RadialLayouter(Configuration.getInstance());
		_layouter.setSelectedNode(new ProcessNodeAdapter(findNode(node,_copy)));
		int _x = this.getSize().width/2; //node.getPos().x
		int _y = this.getSize().height/2; //node.getPos().y
		try {
			_layouter.layoutModel(LayoutUtils.getAdapter(_copy), _x, _y, 0);
			Point _center = new Point(_x,_y);
			this.addProcessHelper(new RadialDistanceRing(_center, 1));
			this.addProcessHelper(new RadialDistanceRing(_center, _layouter.getLayerDistance()));
			this.addProcessHelper(new RadialDistanceRing(_center, 2*_layouter.getLayerDistance()));
			this.addProcessHelper(new RadialDistanceRing(_center, 3*_layouter.getLayerDistance()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//writing back coords to wrappers
		f_wrappers.clear();
		for(ProcessNode n : this.getModel().getNodes()){
			PolarNodeAnimator _w = new PolarNodeAnimator(n,f_animator);
			_w.setAnimationTime(LayoutHelper.toInt(Configuration.getInstance().getProperty(LayoutingAnimator.CONF_ANIMATION_SPEED),3000));
			_w.setCenter(new Point(_x,_y));
			ProcessNode _dup = findNode(n,_copy);
			_w.setNewCoords(new Point(_dup.getPos().x, _dup.getPos().y));
			f_wrappers.add(_w);
		}
		this.getAnimator().getAnimator().setAnimationObjects(f_wrappers);
	}

	private ProcessNode findNode(ProcessNode original, ProcessModel copy) {
		String _id = original.getProperty(ProcessNode.PROP_ID);
		for(ProcessNode n:copy.getNodes()){
			if(n.getProperty(ProcessNode.PROP_ID).equals(_id)){
				return n;
			}
		}
		return null;
	}

	@Override
	public void modelChanged(ProcessModel m) {
		
	}

	@Override
	public void processObjectClicked(ProcessObject o) {
		
	}

	@Override
	public void processObjectDragged(Dragable o, int oldX, int oldY) {	
		
	}

	@Override
	public void processNodeEditingFinished(ProcessNode o) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void processNodeEditingStarted(ProcessNode o, JTextField textfield) {
		textfield.keyDown(new Event(textfield,0,null), KeyEvent.VK_ENTER);
	}
}
