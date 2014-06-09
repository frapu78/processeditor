/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.tracking;

import net.frapu.code.visualization.*;

/**
 *
 * @author frank
 */
public class ProcessEditorDragableMovedAction extends ProcessEditorActionRecord {

    private Dragable node;
    private int oldX;
    private int oldY;
    private int newX;
    private int newY;

    public ProcessEditorDragableMovedAction(Dragable node, int oldX, int oldY, int newX, int newY) {
        this.node = node;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
    }

    public int getNewX() {
        return newX;
    }

    public int getNewY() {
        return newY;
    }

    public Dragable getDragable() {
        return node;
    }

    public int getOldX() {
        return oldX;
    }

    public int getOldY() {
        return oldY;
    }

    @Override
    public String toString() {
        return "Dragable "+node+" moved from "+oldX+","+oldY+" to "+newX+","+newY;
    }



}
