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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.petrinets.PetriNetUtils;

/**
 *
 * @author fpu
 */
public class BarChart extends ProcessNode {

    public final static int MIN_WIDTH = 150;
    public final static int MIN_HEIGHT = 75;
    /** Values */
    public final static String PROP_VALUES = "values";
    /** Labels */
    public final static String PROP_LABELS = "labels";
    /** Label of X-Axis */
    public final static String PROP_XLABEL = "x_label";
    /** Label of Y-Axis */
    public final static String PROP_YLABEL = "y_label";
    /** The background color (see java.awt.Color) for values */
    public final static String PROP_BARCOLOR = "color_bars";
    /** The data values cache */
    protected List<List<Integer>> data = null;
    /** The data labels cache */
    protected List<String> dataLabels = null;
    /** The color cache */
    protected List<Color> colors = null;
    /** The error message to be shown if no data is available */
    protected String errorMessage = "No data available";
	private double maxHeight = 0.0;

    public BarChart() {
        setText("BarChart");
        initializeProperties();
    }

    protected void initializeProperties() {
        setSize(300, 200);
        setProperty(PROP_XLABEL, "x-Axis");
        setProperty(PROP_YLABEL, "y-Axis");
        setProperty(PROP_VALUES, "5,10,15");
        setProperty(PROP_LABELS, "A,B,C");
        setProperty(PROP_BARCOLOR, "" +
                new Color(253, 0, 0).getRGB() + "," +
                new Color(253, 253, 22).getRGB() + "," +
                new Color(0, 250, 0).getRGB() + "," +
                new Color(25, 250, 250).getRGB() + "," +
                new Color(47, 47, 249).getRGB() + "," +
                new Color(252, 19, 252).getRGB() + "," +
                new Color(75, 75, 75).getRGB());
    }

    @Override
    public void setProperty(String key, String value) {
        super.setProperty(key, value);
        // Check if value cache needs to be updated
        if (key.equals(PROP_VALUES)) {
            data = new ArrayList<List<Integer>>();
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreElements()) {
                try {
                    String values = st.nextToken();
                    // Split up into different sub-bars
                    StringTokenizer st2 = new StringTokenizer(values, "+");
                    List<Integer> valueData = new ArrayList<Integer>();
                    data.add(valueData);
                    while (st2.hasMoreElements()) {
                        int v = Integer.parseInt(st2.nextToken());
                        valueData.add(v);
                    }
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
        if (key.equals(PROP_BARCOLOR)) {
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
        if (h < MIN_HEIGHT) {
            h = MIN_HEIGHT;
        }
        super.setSize(w, h);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getXLabel() {
        return getProperty(PROP_XLABEL);
    }

    public void setXLabel(String xLabel) {
        setProperty(PROP_XLABEL, xLabel);
    }

    public String getYLabel() {
        return getProperty(PROP_YLABEL);
    }

    public void setYLabel(String yLabel) {
        setProperty(PROP_YLABEL, yLabel);
    }

    public List<List<Integer>> getData() {
        return data;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        // Check if data==null
        if (colors == null) {
            setProperty(PROP_BARCOLOR, "");
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
        setProperty(PROP_BARCOLOR, values);
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

    public void setStackedData(List<List<Integer>> data) {
        // Check if data==null
        if (data == null) {
            setProperty(PROP_VALUES, "");
            return;
        }
        // set property
        String values = "";
        for (List<Integer> l : data) {
            if (l != null) {
                for (Integer v : l) {
                    values += "" + v + "+";
                }
                // Remove last "+"
                values = values.substring(0, values.length() - 1);
                // Add ","
                values += ",";
            }
        }
        if (values.length() > 0) {
            values = values.substring(0, values.length() - 1);
        }
        setProperty(PROP_VALUES, values);
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
        ProcessUtils.drawTextVertical(g2,
                getPos().x - (getSize().width / 2) + 15, getPos().y, getSize().height,
                getYLabel(), ProcessUtils.Orientation.CENTER);
        ProcessUtils.drawText(g2,
                getPos().x, getPos().y + (getSize().height / 2) - 15, getSize().width,
                getXLabel(), ProcessUtils.Orientation.CENTER);

        // Draw Reflex
        ReportingUtils.drawReflex(g2, this, ProcessUtils.Position.TOP_LEFT);

    }

    private void renderData(Graphics2D g2) {

        double labelOffset = 0.0;
        if (dataLabels.size() > 0) {
            labelOffset = 15.0;
        }

        double xPos = getPos().x - (getSize().width / 2) + 55; // The start x-pos
        double yPos = getPos().y + (getSize().height / 2) - 25 - labelOffset; // The start y-pos
        final double xWidth = getSize().width - 65; // The maximum xWidth
        final double yWidth = getSize().height - 50 - labelOffset; // The maximum yWidth

        final double barWidth = xWidth / getData().size();
        double spacing = 0.0;
        if (barWidth > 2.0) {
            spacing = 1.0;
        }
        if (barWidth > 10.0) {
            spacing = barWidth / 10;
        }

        // Scan for max height
        double maxHeight = Math.max(this.maxHeight, getMaxHeight());
        
        final double barHeightFactor = yWidth / maxHeight;

        // Draw Gatter in 10 steps
        double y = yPos - (maxHeight * barHeightFactor);
        double num = maxHeight;
        int lines = (maxHeight>=10)?10:(int)num;
        g2.setStroke(ProcessUtils.thinDashedStroke);
        g2.setFont(new Font("Arial Narrow", Font.PLAIN, 11));
        for (int i = 0; i <= lines; i++) {
            g2.setColor(Color.GRAY);
            g2.drawLine((int) xPos - 5, (int) y, (int) (xPos + xWidth), (int) y);
            g2.setColor(Color.DARK_GRAY);
            ProcessUtils.drawText(g2, (int) xPos - 10, (int) y - 10, 25, "" + ((int)num), ProcessUtils.Orientation.RIGHT);

            y += ((maxHeight * barHeightFactor) / lines);
            num -= (maxHeight / lines);
        }

        try {
            new Color(Integer.parseInt(this.getProperty(PROP_BARCOLOR)));
        } catch (NumberFormatException e) {
        }
        int index = 0;
        for (List<Integer> l : getData()) {
            double lastY = yPos;
            int innerIndex = 0;
            for (Integer value : l) {
                double height = ((double) value) * barHeightFactor;
                Shape barShape = new Rectangle2D.Double(xPos, lastY - height, barWidth - spacing, height);
                try {
                    g2.setColor(colors.get(innerIndex % colors.size()));
                } catch (Exception e) {
                    g2.setColor(Color.LIGHT_GRAY);
                }
                g2.fill(barShape);
                lastY -= height - 2;
                innerIndex++;
            }
            // Draw bar label if applicable
            if (index < dataLabels.size()) {
                g2.setColor(Color.DARK_GRAY);
                ProcessUtils.drawText(g2, (int) (xPos + (barWidth - spacing) / 2), (int) yPos + 7, (int) barWidth, dataLabels.get(index), ProcessUtils.Orientation.CENTER);
            }
            // Increase index, xPos
            index++;
            xPos += barWidth;
        }
    }

    @Override
    protected Shape getOutlineShape() {
        Rectangle2D outline = new Rectangle2D.Float(getPos().x - (getSize().width / 2),
                getPos().y - (getSize().height / 2), getSize().width, getSize().height);
        return outline;
    }
    
    public void setMaxHeight(double value) {
    	maxHeight = value;
    }

	/**
	 * @return
	 */
	public double getMaxHeight() {
		double result = 0.0;
		for (List<Integer> l : getData()) {
            int sum = 0;
            for (Integer value : l) {
                sum += value;
            }
            if (sum > result) {
            	result = sum;
            }
        }
		return result;
	}
}
