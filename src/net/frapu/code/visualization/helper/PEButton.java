/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package net.frapu.code.visualization.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessHelper;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.ProcessUtils.Orientation;

/**
 * @author ff
 *
 */
public class PEButton extends ProcessHelper implements MouseListener, MouseMotionListener {

    protected ProcessEditor f_editor;
    protected String f_text;
    protected Orientation f_textOrientation = Orientation.LEFT;
    protected Image f_image;
    protected Point f_position = new Point();
    protected Dimension f_size = new Dimension(20, 20);
    private boolean f_visible = true;
    private boolean f_highlighted = false;
    protected boolean f_centerImage = true;
    protected final Stroke f_lineStroke = new BasicStroke(0.5f);
    //listeners which are attached to this button
    private List<PEButtonListener> f_listeners = new ArrayList<PEButtonListener>();

    public PEButton(ProcessEditor editor) {
        f_editor = editor;
        f_editor.addProcessHelper(this);
        f_editor.addMouseListener(this);
        f_editor.addMouseMotionListener(this);
    }

    public PEButton(ProcessEditor editor, Image image) {
        this(editor);
        f_text = null;
        f_image = image;
    }

    public PEButton(ProcessEditor editor, String text) {
        this(editor);
        f_text = text;
        f_image = null;
    }

    @Override
    public Object clone() {
        // @todo: Implement clone()
        return null;
    }

    public void addListener(PEButtonListener listener) {
        f_listeners.add(listener);
    }

    /**
     * returns true if the given point is within the bounds
     * of this PEButton
     * @param p
     * @return
     */
    public boolean contains(Point p) {
        return getBounds().contains(p);
    }
    
    @Override
    protected void finalize() throws Throwable {
    	destroy();
    	super.finalize();    	
    }

    public void destroy() {
        f_editor.removeMouseListener(this);
        f_editor.removeMouseMotionListener(this);
        f_editor.removeProcessHelper(this);
    }

    public Rectangle getBounds() {
        return new Rectangle(f_position, f_size);
    }

    /**
     * returns the position in coordinate-system of the corresponding ProcessEditor
     * @return
     */
    public Point getPosition() {
        return f_position;
    }

    /**
     * returns the Orientation value which is used for the alignment of the text
     * @return
     */
    public Orientation getTextOrientation() {
        return f_textOrientation;
    }

    /**
     * returns true if the button is highlighted (Mouse is over the button)
     * @return
     */
    public boolean isHighlighted() {
        return f_highlighted;
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    /**
     * Returns a boolean which defines whether this button is visible (will be drawn onto the
     * ProcessEditor) or not
     * @return
     */
    public boolean isVisible() {
        return f_visible;
    }

    /**
     * draws the button on the ProcessEditors Canvas
     * @param g
     */
    public void paint(Graphics gra) {
        Graphics2D g = (Graphics2D) gra;
        if (!f_visible) {
            return;
        }

        g.setStroke(f_lineStroke);
        //inner rectangle------------------------------------------------------
        if (!isHighlighted()) {
            g.setColor(new Color(240, 240, 240));
        } else {
            g.setColor(new Color(230, 210, 140));
        }
        g.fillRoundRect(f_position.x, f_position.y,
                f_size.width, f_size.height, 5, 5);
        //smaller inner rectangle (for nice 3d-like effects)-------------------
        if (!f_highlighted) {
            g.setColor(new Color(224, 224, 244));
        } else {
            g.setColor(new Color(246, 226, 136));
        }
        final int BORDER = 2;
        g.fillRect(f_position.x + BORDER, f_position.y + f_size.height / 2, f_size.width - 2 * BORDER, f_size.height / 2 - BORDER);

        //drawing a border-----------------------------------------------------
        g.setColor(new Color(154, 154, 154));
        g.drawRoundRect(f_position.x, f_position.y,
                f_size.width, f_size.height, 5, 5);

        //drawing image and/or text--------------------------------------------
        g.setColor(new Color(100, 100, 100));
        if (f_image != null) {
            //used to center image
            int _w = f_centerImage ? (f_size.width - f_image.getWidth(null)) / 2 : 1;
            int _h = (f_size.height - f_image.getHeight(null)) / 2;
            g.drawImage(f_image, f_position.x + _w, f_position.y + _h, null);
            if (f_text != null) {
                int _offsetX = f_textOrientation == Orientation.CENTER ? ((f_size.width - f_image.getWidth(null) - 2) / 2) : 0;
                int _offsetY = f_textOrientation == Orientation.CENTER ? (f_size.height / 2) : 0;
                ProcessUtils.drawText(g,
                        f_position.x + f_image.getWidth(null) + _offsetX,
                        f_position.y + _offsetY, (f_size.width - f_image.getWidth(null)) - 2, f_text, f_textOrientation);
            }
        } else {
            if (f_text != null) {
                int _offsetX = f_textOrientation == Orientation.CENTER ? (f_size.width) / 2 : 0;
                int _offsetY = f_textOrientation == Orientation.CENTER ? (f_size.height / 2) : 0;
                ProcessUtils.drawText(g, f_position.x + _offsetX, f_position.y + _offsetY, f_size.width - 2, f_text, f_textOrientation);
            }
        }
    }

    /**
     * If set to true (the default value) the image of the button will be drawn
     * in the center.
     * otherwise it will be aligned to the left.
     * @param b
     */
    public void setCenterImage(boolean value) {
        f_centerImage = value;
    }

    /**
     * tells this button whether it should be drawn highlighted or not.
     * The corresponding editor has to be repainted to see the changes.
     * @param value
     */
    public void setHighlighted(boolean value) {
        f_highlighted = value;
        f_editor.repaint();
    }

    /**
     * sets a new image to be drawn on the button
     * @param image
     */
    public void setImage(Image image) {
        f_image = image;
    }

    /**
     * specifies the alignemnt of the text
     * @param orientation
     */
    public void setTextOrientation(Orientation orientation) {
        f_textOrientation = orientation;
    }

    /**
     * Sets a new position for the button, relative to the corresponding
     * ProcessEditor
     * @param pos
     */
    public void setPosition(Point pos) {
        f_position = new Point(pos);
    }

    /**
     * Sets the new size for the button
     * @param width
     * @param height
     */
    public void setSize(Dimension size) {
        f_size = new Dimension(size);
    }

    /**
     * Sets the new size for the button
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    /**
     * Sets the text which will be displayed on the button
     * @param text
     */
    public void setText(String text) {
        f_text = text;
    }

    /**
     * Sets a boolean which defines whether this button is visible (will be drawn onto the
     * ProcessEditor) or not
     * @return
     */
    public void setVisible(boolean value) {
        f_visible = value;
    }

    //-----------------------------------------------------------------------------------------
    // LISTENER IMPLEMENTATIONS
    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    /**
     * listens for clicks and notifies all listeners if this button was clicked
     */
    public void mousePressed(MouseEvent e) {
        if (!f_visible) {
            return;
        }
        Point _loc = f_editor.getMouseClickLocation(e.getPoint());
        if (this.contains(_loc)) {
            for (PEButtonListener l : f_listeners) {
                l.buttonClicked(this);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }

    @Override
    /**
     * listens for mouse movements. (De)-Highlights the button and notifies all listeners
     * if the mouse enters/exits the bounds of the button.
     */
    public void mouseMoved(MouseEvent evt) {
        if (!f_visible) {
            return;
        }
        Point p = f_editor.getMouseClickLocation(evt.getPoint()); // convert coordinates (for zoom)
        if (this.contains(p)) {
            if (!this.isHighlighted()) {
                this.setHighlighted(true);
                for (PEButtonListener l : f_listeners) {
                    l.buttonMouseIn(this);
                }
                f_editor.repaint();
            }
        } else {
            if (this.isHighlighted()) {
                this.setHighlighted(false);
                for (PEButtonListener l : f_listeners) {
                    l.buttonMouseOut(this);
                }
                f_editor.repaint();
            }
        }

    }
}
