/**
 * Process Editor - CMMN Package
 *
 * (C) 2014 the authors
 */
package net.frapu.code.visualization.cmmn;

import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import java.awt.*;
import java.util.LinkedList;

/**
 * @author Stephan
 * @version 14.10.2014
 */
public class Criterion extends ProcessNode implements AttachedNode {


    /** The parent node */
    public final static String PROP_SOURCE_NODE = "#source";

    public Criterion() {
        super();
        setSize(10, 20);
        initializeProperties();
    }

    public Criterion(int x, int y, String label) {
        super();
        setPos(x, y);
        setSize(10, 20);
        setText(label);
        initializeProperties();
    }

    private void initializeProperties() {
        setProperty(PROP_BACKGROUND, "" + Color.WHITE.getRGB());
        setProperty(PROP_SOURCE_NODE, "");
    }

    @Override
    public ProcessNode getParentNode(ProcessModel model) {
        return model.getNodeById(getProperty(PROP_SOURCE_NODE));
    }

    @Override
    public String getParentNodeId() {
        String result = getProperty(PROP_SOURCE_NODE);
        return result;
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Polygon shape = new Polygon();
        shape.addPoint(getPos().x - (getSize().width / 2), getPos().y);
        shape.addPoint(getPos().x, getPos().y - (getSize().height / 2));
        shape.addPoint(getPos().x + (getSize().width / 2), getPos().y);
        shape.addPoint(getPos().x, getPos().y + (getSize().height / 2));

        g2.setPaint(getBackground());
        g2.fill(shape);

        g2.setStroke(CMMNUtils.defaultStroke);
        g2.setPaint(Color.BLACK);
        g2.draw(shape);
    }

    @Override
    protected Shape getOutlineShape() {
        Polygon result = new Polygon();
        result.addPoint(getPos().x - (getSize().width / 2), getPos().y);
        result.addPoint(getPos().x, getPos().y - (getSize().height / 2));
        result.addPoint(getPos().x + (getSize().width / 2), getPos().y);
        result.addPoint(getPos().x, getPos().y + (getSize().height / 2));

        return result;
    }

    @Override
    public void setParentNode(ProcessNode node) {
        String id = "";
        if (null != node) id = node.getProperty(PROP_ID);
        setProperty(PROP_SOURCE_NODE, null == id ? "" : id);
    }

    @Override
    public String toString() { return "CMMN Criterion (" + getText() + ")"; }

    @Override
    public java.util.List<Class<? extends ProcessNode>> getVariants() {
        java.util.List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        result.add(EntryCriterion.class);
        result.add(ExitCriterion.class);

        return result;
    }
}
