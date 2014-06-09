/**
 *
 * Process Editor - Reporting Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.reporting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import net.frapu.code.visualization.editors.BooleanPropertyEditor;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.petrinets.PetriNetUtils;

/**
 *
 * @author fpu
 */
public class PieChart extends ProcessNode {

    public final static int MIN_WIDTH = 80;
    public final static int MIN_HEIGHT = 80;
    /** Values */
    public final static String PROP_VALUES = "values";
    /** Labels */
    public final static String PROP_LABELS = "labels";
    /** The background color (see java.awt.Color) for values */
    public final static String PROP_PIECECOLORS = "color_pieces";
    /** The index to be highlighted */
    public final static String PROP_HIGHLIGHTINDEX = "highlight_index";
    /** Flag if the percents are shown or not */
    public final static String PROP_SHOW_PERCENT = "show_percent";
    /** The data values cache */
    protected List<Integer> data = null;
    /** The data labels cache */
    protected List<String> dataLabels = null;
    /** The color cache */
    protected List<Color> colors = null;
    /** The error message to be shown if no data is available */
    protected String errorMessage = "No data available";

    public PieChart() {
        setText("PieChart");
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(300, 200);
        setProperty(PROP_VALUES, "8,10,12");
        setProperty(PROP_LABELS, "A,B,C");
        setProperty(PROP_SHOW_PERCENT, TRUE);
        setPropertyEditor(PROP_SHOW_PERCENT, new BooleanPropertyEditor());

        setProperty(PROP_HIGHLIGHTINDEX, "-1");
        setProperty(PROP_PIECECOLORS, "" +
                new Color(253,0,0).getRGB() + "," +
                new Color(253,253,22).getRGB() + "," +
                new Color(0,250,0).getRGB() + "," +
                new Color(25,250,250).getRGB() + "," +
                new Color(47,47,249).getRGB() + "," +
                new Color(252,19,252).getRGB() + "," +
                new Color(75,75,75).getRGB());
    }

    public boolean isShowPercent() {
        return getProperty(PROP_SHOW_PERCENT).equals("1");
    }

    public void setShowPercent(boolean showPercent) {
        setProperty(PROP_SHOW_PERCENT, showPercent == true ? "1" : "0");
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        // Check if value cache needs to be updated
        if (key.equals(PROP_VALUES)) {
            data = new ArrayList<Integer>();
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreElements()) {
                try {
                    int v = Integer.parseInt(st.nextToken());
                    data.add(v);
                } catch (Exception e) {
                }
            }
        }
        // Check if label cache needs to be updated
        if (key.equals(PROP_LABELS)) {
            dataLabels = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreElements()) {
                dataLabels.add(st.nextToken());
            }
        }
        // Check if color cache needs to be updated
        if (key.equals(PROP_PIECECOLORS)) {
            colors = new ArrayList<Color>();
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreElements()) {
                try {
                    int v = Integer.parseInt(st.nextToken());
                    colors.add(new Color(v));
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void setSize(int w, int h) {
        if (w < MIN_WIDTH) {
            w = MIN_WIDTH;
        }
        if (h > w) {
            h = w;
        }
        super.setSize(w, h);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getHighlightIndex() {
        int highlightIndex = -1;
        try {
            highlightIndex = Integer.parseInt(getProperty(PROP_HIGHLIGHTINDEX));
        } catch (Exception e) {
        }
        // Don't highlight if only one value
        if (getData().size()<2) return -1;

        return highlightIndex;
    }

    public void setHighlightIndex(int highlightIndex) {
        setProperty(PROP_HIGHLIGHTINDEX, "" + highlightIndex);
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        // Check if data==null
        if (data == null) {
            setProperty(PROP_VALUES, "");
            return;
        }
        // set property
        String values = "";
        for (Integer v : data) {
            values += "" + v + ",";
        }
        if (values.length() > 0) {
            values = values.substring(0, values.length() - 1);
        }
        setProperty(PROP_VALUES, values);
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        // Check if data==null
        if (colors == null) {
            setProperty(PROP_PIECECOLORS, "");
            return;
        }
        // set property
        String values = "";
        for (Color v : colors) {
            values += "" + v.getRGB() + ",";
        }
        if (values.length() > 0) {
            values = values.substring(0, values.length() - 1);
        }
        setProperty(PROP_PIECECOLORS, values);
    }

    /**
     * Sets the labels for the values. Shown at the x-axis.
     * @param data
     */
    public void setLabels(List<String> data) {
        // set property
        String values = "";
        for (String s : data) {
            values += "" + s + ",";
        }
        values = values.substring(0, values.length() - 1);
        setProperty(PROP_LABELS, values);
    }

    private Point getDistancePoint(double x, double y, double degree, double dist) {
        // Add offset
        degree = degree - 90;
        // Rotate point at origin
        double nx = dist * Math.sin(Math.toRadians(degree));
        double ny = 0.0 - dist * Math.cos(Math.toRadians(degree));

        return new Point((int) (x - nx), (int) (y + ny));
    }

    @Override
    protected void paintInternal(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setStroke(PetriNetUtils.defaultStroke);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape outline = getOutlineShape();

        g2.setPaint(getBackground());
        g2.fill(outline);

        g2.setPaint(Color.BLACK);
        g2.draw(outline);

        // Render data
        if (data.size() > 0) {
            renderData(g2);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Arial Narrow", Font.ITALIC, 16));

            // Draw label
            ProcessUtils.drawText(g2, getPos().x, getPos().y,
                    getSize().width - 5, getErrorMessage(), ProcessUtils.Orientation.CENTER);
        }


        // Set font
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial Narrow", Font.BOLD, 14));

        // Draw label
        ProcessUtils.drawText(g2, getPos().x, (getPos().y - getSize().height / 2) + 10,
                getSize().width - 5, getText(), ProcessUtils.Orientation.CENTER);
        // Draw label y-axis
        g2.setFont(new Font("Arial Narrow", Font.PLAIN, 12));

        // Draw Reflex
        ReportingUtils.drawReflex(g2, this, ProcessUtils.Position.TOP_LEFT);

    }

    private void renderData(Graphics2D g2) {

        // Distance to edges
        final double spacing = 100.0;

        // Highlight distance
        final double highLightDist = 15.0;

        // Scan for  values
        double sumValue = 0;
        for (Integer value : getData()) {
            sumValue += value;
        }

        double currentAngle = 90.0;

        double yOffset = 5;

        // Draw data
        int index = 0;
        for (Integer value : getData()) {

            double step = -(3.6) * ((value / sumValue) * 100.0);

            Shape pieShape = new Arc2D.Double(getPos().x - (getSize().height / 2) + (spacing / 2),
                    getPos().y - (getSize().height / 2) + (spacing / 2) + (yOffset/2),
                    getSize().height - spacing, getSize().height - spacing,
                    currentAngle, step, Arc2D.PIE);

            // Check if single value
            if (getData().size()==1) {
                // Draw circle instead
                pieShape = new Ellipse2D.Double(getPos().x - (getSize().height / 2) + (spacing / 2),
                    getPos().y - (getSize().height / 2) + (spacing / 2) + (yOffset/2),
                    getSize().height - spacing, getSize().height - spacing);
                step = -359;
            }

            // Move highlight outside
            Point hp = new Point(getPos().x, (int)(getPos().y+yOffset));
            if (index == getHighlightIndex()) {
                hp = getDistancePoint(getPos().x, getPos().y + yOffset,
                        (currentAngle + (step / 2)),
                        highLightDist);
                pieShape = new Arc2D.Double(hp.x - (getSize().height / 2) + (spacing / 2),
                        hp.y - (getSize().height / 2) + (spacing / 2) + (yOffset/2),
                        getSize().height - spacing, getSize().height - spacing,
                        currentAngle, step, Arc2D.PIE);
            }

            int extraDist = 0;
            if (index == getHighlightIndex()) {
                extraDist = (int) highLightDist;
            }

            Point labelPos = getDistancePoint(getPos().x, getPos().y + yOffset,
                    (currentAngle + (step / 2)),
                    (getSize().height / 2) - (spacing / 2) + 20 + extraDist);

            try {
                g2.setColor(colors.get(index % colors.size()));
            } catch (Exception e) {
                g2.setColor(Color.LIGHT_GRAY);
            }
            g2.fill(pieShape);
            
            if(!g2.getColor().equals(Color.WHITE)) { //WHITE is a special color, used for free space
            	g2.setColor(Color.BLACK);
            	g2.draw(pieShape);
            }
           

            // Sets the label if applicable
            String label = "";
            if (index < dataLabels.size()) {
                label = dataLabels.get(index) + "\n";
            }
            // Shows the percents if set
            if (isShowPercent()) {
                label += (int) Math.round((value / sumValue) * 100.0) + "%";
            }

            // Draw text
            if(!g2.getColor().equals(Color.WHITE)) { //WHITE is a special color, used for free space
            	g2.setColor(Color.BLACK);
	            g2.setFont(new Font("Arial Narrow", Font.BOLD, 12));
	
	            ProcessUtils.drawText(g2, (int) labelPos.x, (int) labelPos.y,
	                    50, label, ProcessUtils.Orientation.CENTER);
            }
            // Debugging: Draw lines to breakout points
//            g2.fillOval(labelPos.x-4, labelPos.y-4, 8, 8);
//            g2.drawLine(getPos().x, (int)(getPos().y+yOffset), labelPos.x, labelPos.y);
//
//            g2.fillOval(hp.x-4, hp.y-4, 8, 8);
//            g2.drawLine(getPos().x, (int)(getPos().y+yOffset), hp.x, hp.y);

            currentAngle += step;

            // Increase index
            index++;

        }
    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }
}
