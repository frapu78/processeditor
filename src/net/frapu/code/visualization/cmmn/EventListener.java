package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.editors.ColorPropertyEditor;
import net.frapu.code.visualization.editors.ListSelectionPropertyEditor;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;

/**
 * @author Stephan
 * @version 13.10.2014.
 */
public class EventListener extends ProcessNode {

    /** Stereotypes */
    public static final String TYPE_USER = "USER";
    public static final String TYPE_TIMER = "TIMER";

    public EventListener() {
        super();
        setSize(50, 50);
        initializeProperties();
    }

    private void initializeProperties() {
        String[] ttype = { "", EventListener.TYPE_USER, EventListener.TYPE_TIMER};
        setPropertyEditor(PROP_STEREOTYPE, new ListSelectionPropertyEditor(ttype));

        setProperty(PROP_BACKGROUND, "" + Color.WHITE.getRGB());
        setPropertyEditor(PROP_BACKGROUND, new ColorPropertyEditor());
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        outline = new Ellipse2D.Double(getPos().x - (getSize().width / 2) + 3,
                getPos().y - (getSize().width / 2) + 3, getSize().width - 6, getSize().width - 6);
        g2.setColor(Color.BLACK);
        g2.draw(outline);

        drawMarker(g2);

        // Draw text
        g2.setFont(CMMNUtils.defaultFont);
        if (getText() != null) {
            CMMNUtils.drawText(g2, getPos().x, getPos().y + (getSize().width / 2),
                    getSize().width * 3, getText(), CMMNUtils.Orientation.TOP);
        }
    }

    protected void drawMarker(Graphics2D g2) {
        // Implemented in Subtypes
    }

    @Override
    public java.util.List<Class<? extends ProcessNode>> getVariants() {
        java.util.List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(EventListener.class);
        result.add(TimerEventListener.class);
        result.add(UserEventListener.class);
        return result;
    }

    @Override
    protected Shape getOutlineShape() {
        return new Ellipse2D.Double(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().width / 2), getSize().width, getSize().width);
    }

    @Override
    public String toString() { return "CMMN Event Listener ("+getText()+")"; }
}
