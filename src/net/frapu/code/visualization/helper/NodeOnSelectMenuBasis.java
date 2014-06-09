/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessNode;

/**
 * @author ff
 *
 */
public abstract class NodeOnSelectMenuBasis extends ProcessHelper {

	
	private ProcessNode f_attachee;
	private ProcessEditor f_editor;
	
	private List<PEButton> f_buttons = new ArrayList<PEButton>();
	private Map<PEButton,Point> f_relativeCoords = new HashMap<PEButton, Point>();
	
	public NodeOnSelectMenuBasis(ProcessEditor editor){
		f_editor = editor;
	}
	
	public ProcessEditor getEditor() {
		return f_editor;
	}
	
	protected abstract void buildMenu(ProcessNode node, ProcessEditor editor);
	
	/**
	 * adds a button to this menu.
	 * The button will appear relative to the nodes center, given the relative coordinates
	 * @param button
	 * @param relativeCoords
	 */
	public void addButton(PEButton button, Point relativeCoords) {
		f_buttons.add(button);
		f_relativeCoords.put(button, new Point(relativeCoords));
	}

    public void moveButton(PEButton button, Point relativeCoords) {
    	f_relativeCoords.put(button, new Point(relativeCoords));
	}
	
        
    /**
     * removes all buttons from this menu (so they can be rebuild later
     */
    public void clearButtons() {
    	for(PEButton b:f_buttons) {
			b.destroy();
		}	
    	f_buttons.clear();
    	f_relativeCoords.clear();
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DefaultNodeOnSelectMenu) {
			return ((DefaultNodeOnSelectMenu)obj).getNode() == (this.getNode());
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		if(getNode() != null)
			return getNode().hashCode();
		return super.hashCode();
	}
	
	/**
	 * cleans up all resources and removes all listeners and buttons from
	 * the ProcessEditor.
	 */
	public void destroy() {
		getEditor().removeProcessHelper(this);		
		for(PEButton b:f_buttons) {
			b.destroy();
		}		
		getEditor().repaint();
	}
	
	/**
	 * returns the node this menu was attached to;
	 * @return
	 */
	public ProcessNode getNode() {
		return f_attachee;
	}
	
	public void setNode(ProcessNode node) {
		f_attachee = node;
		this.setAlpha(1.0f);
		if(node != null) {
			buildMenu(f_attachee, f_editor);
			updatePositions();
		}else {
			clearButtons();
		}
	}
	
	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public void paint(Graphics g) {
		//not needed buttons will draw themselves
		updatePositions();
	}

	
	@Override
	public void setAlpha(float value) {
		super.setAlpha(value);
		for(PEButton b:f_buttons) {
			b.setAlpha(value);
		}
	}
	/**
	 * 
	 */
	private void updatePositions() {
		if(getNode() != null) {
			Point _pos = getNode().getPos();
			for(PEButton b:f_buttons) {
				Point _rel = f_relativeCoords.get(b);
				b.setPosition(new Point(_pos.x+_rel.x,_pos.y+_rel.y));
			}
		}
	}

}
