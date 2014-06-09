/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.layouter;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.adapter.ProcessModelAdapter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.layouter.interfaces.NodeInterface;

import net.frapu.code.visualization.Configuration;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.petrinets.Edge;
import net.frapu.code.visualization.petrinets.Lane;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.Transition;

/**
 *
 * Provides methods for converting process models to DOT, invoking DOT
 * and reading DOT output to layout a process model
 *
 * @author frank
 */
public class DOTLayouter extends ProcessLayouter {

    public final static double factor = 0.01;
    private String dotLocation = "/usr/local/bin/dot";

    public DOTLayouter() {
        Configuration conf = Configuration.getInstance();
        dotLocation = conf.getProperty(Configuration.PROP_DOT_LOCATION);
    }

    // Returns the location of the Graphviz dot programm
    public String getDotLocation() {
        return dotLocation;
    }

    // Sets the location of the Graphviz dot programm
    public void setDotLocation(String dotLocation) {
        this.dotLocation = dotLocation;
    }

    private double getWidth(ProcessNode node) {
        double width = 30;
        try {
            width = Double.parseDouble(node.getProperty(ProcessNode.PROP_WIDTH));
        } catch (Exception e) {
        }
        return width * factor;
    }

    private double getHeight(ProcessNode node) {
        double width = 30;
        try {
            width = Double.parseDouble(node.getProperty(ProcessNode.PROP_HEIGHT));
        } catch (Exception e) {
        }
        return width * factor;
    }

    public void layoutModel(AbstractModelAdapter model2, int xstart, int ystart, int direction)
            throws Exception {
    	ProcessModelAdapter adap = (ProcessModelAdapter) model2;
    	ProcessModel model = adap.getModel();
        // 1. Check if ProcessModel type is supported (currently only Petri nets)
        if (!(model instanceof PetriNetModel)) {
            throw new UnsupportedModelTypeException("Only PetriNet models are currently supported!");
        }

        List<String> dotInput = new ArrayList<String>();
        List<String> dotResult = new ArrayList<String>();


        // 2. Map model nodes/edges/size to .DOT format based on Lanes!

         //@todo: Add support if no Lanes are contained!!!

        for (ProcessNode node1 : model.getNodes()) {

            if (node1 instanceof Lane) {
                Lane lane = (Lane) node1;

                System.out.println("LANE=" + lane.getProcessNodes().size());

                // Create subgraph entry
                dotInput.add("subgraph cluster" + lane.getProperty(ProcessNode.PROP_ID) + " {");
                dotInput.add("style=filled; label=\""+lane.getText()+"\";");

                // Iterate over the nodes contained inside the lane
                for (ProcessNode node : lane.getProcessNodes()) {

                    String line = "";
                    // Create Place entry
                    if (node instanceof Place) {
                        line = "" + node.getProperty(ProcessNode.PROP_ID) +
                                " [shape=circle, height=" + getHeight(node) +
                                ", width=" + getWidth(node) +
                                ", fixedsize=true];";
                    }
                    // Create Transition entry
                    if (node instanceof Transition) {
                        line = "" + node.getProperty(ProcessNode.PROP_ID) +
                                " [shape=box, height=" + getHeight(node) +
                                ", width=" + getWidth(node) +
                                ", fixedsize=true];";
                    }

                    // Add only if line has been filled
                    if (line.length() > 0) {
                        dotInput.add(line);
                    }
                }
                // Close subgraph
                dotInput.add("}");
            }

        }

        for (ProcessEdge edge : model.getEdges()) {
            String line = "";
            if (edge instanceof Edge) {
                line = "" + edge.getSource().getProperty(ProcessNode.PROP_ID) +
                        " -> " +
                        edge.getTarget().getProperty(ProcessNode.PROP_ID) + ";";
            }

            // Add only if line has been filled
            if (line.length() > 0) {
                dotInput.add(line);
            }
        }

        if (direction == LAYOUT_HORIZONTAL) {
            dotInput.add(0, "graph [rankdir=LR]");
        }
        dotInput.add(0, "digraph G {");
        dotInput.add("}");

        // 3. Call DOT
        File tempFile = File.createTempFile("Layout", ".dot");
        tempFile.deleteOnExit();

        System.out.println("Create temp file " + tempFile);

        PrintWriter w = new PrintWriter(tempFile);
        for (String line : dotInput) {
            w.println(line);
        }
        w.flush();

        String line;
        Process p = Runtime.getRuntime().exec(dotLocation + " -Gcharset=latin1 -Tplain " + tempFile);

        BufferedReader input =
                new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            dotResult.add(line);
            if (line.equals("stop")) {
                break;
            }
        }
        input.close();

        // 4. Update model with DOT results
        for (String line2 : dotResult) {
            // Look only for node
            if (line2.startsWith("node ")) {
                StringTokenizer st = new StringTokenizer(line2, " ");
                String id = st.nextToken();
                id = st.nextToken();
                int xpos = (int) (Double.parseDouble(st.nextToken()) / factor);
                int ypos = (int) (Double.parseDouble(st.nextToken()) / factor);

                // Find ProcessNode with id
                for (ProcessNode node : model.getNodes()) {

                    if (node.getProperty(ProcessNode.PROP_ID).equals(id)) {
                        // Set new x and y positions
                        node.setPos(xpos + xstart, ypos + ystart);
                    }
                }
            }
        }

        // List of Lanes
        LinkedList<ProcessNode> laneList = new LinkedList<ProcessNode>();

        // Update Lanes!
        for (ProcessNode node: model.getNodes()) {
            if (node instanceof Lane) {
                Lane lane = (Lane)node;
                // Get bounding box
                int x1=Integer.MAX_VALUE, y1=Integer.MAX_VALUE, x2=0, y2=0;
                for (ProcessNode subNode: lane.getProcessNodes()) {
                    // Detect positions
                    Point pos = subNode.getPos();
                    Dimension dim = subNode.getSize();
                    // Update positions if needed
                    if ((pos.x-(dim.width/2))<x1) x1 = pos.x-(dim.width/2);
                    if ((pos.y-(dim.height/2))<y1) y1 = pos.y-(dim.height/2);
                    if ((pos.x+(dim.width/2))>x2) x2 = pos.x+(dim.width/2);
                    if ((pos.y+(dim.height/2))>y2) y2 = pos.y+(dim.height/2);
                }
                // Set Lane to floating
                lane.setProperty(Lane.PROP_FLOATING, "1");
                // Set position and size (use setProperty to skip updateContainments)
                lane.setProperty(Lane.PROP_XPOS, ""+(x1+((x2-x1)/2)-10));
                lane.setProperty(Lane.PROP_YPOS, ""+(y1+((y2-y1)/2)));
                lane.setProperty(Lane.PROP_WIDTH, ""+(x2-x1+20));
                lane.setProperty(Lane.PROP_HEIGHT, ""+(y2-y1+20));
                // Mode Lane to background
                laneList.add(lane);
            }
        }

        // Move all Lanes to background
        for (ProcessNode n: laneList) {
            model.moveToBack(n);
        }


    }

	@Override
	public String getDisplayName() {
		return "DOT Layouter";
	}

	@Override
	public void setSelectedNode(NodeInterface selectedNode) {
	}
}
