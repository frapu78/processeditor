/**
 *
 * Process Editor - Converter Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.converter;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.visualization.LayoutUtils;
import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.layouter.DOTLayouter;
import net.frapu.code.visualization.petrinets.Edge;
import net.frapu.code.visualization.petrinets.Lane;
import net.frapu.code.visualization.petrinets.PetriNetModel;
import net.frapu.code.visualization.petrinets.Place;
import net.frapu.code.visualization.petrinets.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import com.inubit.research.layouter.ProcessLayouter;


/**
 *
 * Helper class for converting .PNML files to PetriNetModels.
 *
 * @author frank
 */
public class PNMLImporter implements Importer {

    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    /** Stores (lane_id, Lane) */
    HashMap<String, Lane> lanes = new HashMap<String, Lane>();
    /** Internal list of parsed models */
    private List<ProcessModel> models;

    public PNMLImporter() {
        // Do nothing here...
    }

    /**
     * Creates a new PNML-Parser based on Document.
     * @param xmlDoc
     * @throws java.lang.Exception
     */
    public PNMLImporter(Document xmlDoc) throws Exception {
        models = parsePNML(xmlDoc);
    }

    /**
     * Returns the PetriNetModels found in the PNML file.
     * @return
     */
    public List<ProcessModel> getModels() {
        return models;
    }

    private String getName(Element e2) throws Exception {

        String query = "name/text/text()";
        Object res = xpath.evaluate(query, e2, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;

        if (nodes1.getLength() == 0) {
            return "";
        }

        return nodes1.item(0).getNodeValue();
    }

    private String getFirstDuration(Element e) throws Exception {
        String query = "toolspecific/metadata/property[@type='Duration']";
        Object res = xpath.evaluate(query, e, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;
        if (nodes1.getLength() == 0) {
            return "";
        }

        String duration = ((Element) nodes1.item(0)).getAttribute("value");
        ;
        return duration;
    }

    private String getFirstCost(Element e) throws Exception {
        String query = "toolspecific/metadata/property[@type='Cost']";
        Object res = xpath.evaluate(query, e, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;
        if (nodes1.getLength() == 0) {
            return "";
        }

        String cost = ((Element) nodes1.item(0)).getAttribute("value");
        ;
        return cost;
    }

    private Lane getLane(Element e) throws Exception {
        String laneId = "default";
        String laneName = "Default";

        String query = "toolspecific/poollane";
        Object res = xpath.evaluate(query, e, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;
        if (nodes1.getLength() != 0) {
            laneId = ((Element) nodes1.item(0)).getAttribute("id");
            laneName = ((Element) nodes1.item(0)).getAttribute("name");
        }

        // Check if lane is already created
        Lane lane = lanes.get(laneId);
        if (lane == null) {
            // Create new Lane
            lane = new Lane();
            lane.setText(laneName);
            lanes.put(laneId, lane);
        }


        return lane;
    }

    private String getProbability(Element e) throws Exception {
        String query = "inscription/value/text()";
        Object res = xpath.evaluate(query, e, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;
        if (nodes1.getLength() == 0) {
            return "";
        }

        String prop = nodes1.item(0).getNodeValue();

        return prop;
    }

    private int getXPos(Element e2) throws Exception {
        String query = "graphics/position";
        Object res = xpath.evaluate(query, e2, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;

        if (nodes1.getLength() == 0) {
            return 0;
        }

        Element e = (Element) nodes1.item(0);

        int result = 0;

        try {
            result = Integer.parseInt(e.getAttribute("x"));
        } catch (NumberFormatException ex) {
        }

        return result;
    }

    private int getYPos(Element e2) throws Exception {
        String query = "graphics/position";
        Object res = xpath.evaluate(query, e2, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;

        if (nodes1.getLength() == 0) {
            return 0;
        }

        Element e = (Element) nodes1.item(0);

        int result = 0;

        try {
            result = Integer.parseInt(e.getAttribute("y"));
        } catch (NumberFormatException ex) {
        }

        return result;
    }

    /**
     * Converts the content of the Document (that should be PNML) to
     * PetriNetModels.
     *
     * @param xmlDoc
     */
    private List<ProcessModel> parsePNML(Document xmlDoc) throws Exception {

        HashMap<String, ProcessNode> nodeMap = new HashMap<String, ProcessNode>();
        List<ProcessModel> resultNets = new LinkedList<ProcessModel>();
        lanes = new HashMap<String, Lane>();

        // Retrieve all nets contained in the Document
        String query = "/pnml/net";
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList nodes1 = (NodeList) res;

        if (nodes1.getLength()==0) throw new UnsupportedFileTypeException("No PNML file!");

        System.out.println("Nets contained: " + nodes1.getLength());

        // Iterate over all contained nets
        for (int i1 = 0; i1 < nodes1.getLength(); i1++) {
            // Get net id
            Element e = (Element) nodes1.item(i1);
            //String netId = e.getAttribute("id");

            PetriNetModel model = new PetriNetModel(getName(e));
            resultNets.add(model);

            // Collect all places (from all pages)
            String query2 = "//place";
            Object res2 = xpath.evaluate(query2, e, XPathConstants.NODESET);
            NodeList nodes2 = (NodeList) res2;

            System.out.println("Places contained: " + nodes2.getLength());

            // Add all places to the model
            for (int i2 = 0; i2 < nodes2.getLength(); i2++) {
                Element e2 = (Element) nodes2.item(i2);
                Place p = new Place();
                p.setText(getName(e2));
                p.setPos(getXPos(e2), getYPos(e2));
                model.addNode(p);
                // put place into nodeMap
                nodeMap.put(e2.getAttribute("id"), p);
            }

            // Collect all transitions (from all pages)
            String query3 = "//transition";
            Object res3 = xpath.evaluate(query3, e, XPathConstants.NODESET);
            NodeList nodes3 = (NodeList) res3;

            System.out.println("Transitions contained: " + nodes2.getLength());

            // Add all transitions to the model
            for (int i3 = 0; i3 < nodes3.getLength(); i3++) {
                Element e3 = (Element) nodes3.item(i3);
                Transition t = new Transition();
                t.setText(getName(e3));
                t.setPos(getXPos(e3), getYPos(e3));
                t.setProperty(Transition.PROP_DURATION, getFirstDuration(e3));
                t.setProperty(Transition.PROP_COST, getFirstCost(e3));
                // Put Transition into laneNodes
                Lane lane = getLane(e3);
                lane.addProcessNode(t);

                model.addNode(t);
                // put transition into nodeMap
                nodeMap.put(e3.getAttribute("id"), t);
            }

            // Collect all arcs
            String query4 = "//arc";
            Object res4 = xpath.evaluate(query4, e, XPathConstants.NODESET);
            NodeList nodes4 = (NodeList) res4;

            System.out.println("Arcs contained: " + nodes4.getLength());

            // Add all arcs to the model
            for (int i4 = 0; i4 < nodes4.getLength(); i4++) {
                Element e4 = (Element) nodes4.item(i4);
                ProcessNode source = nodeMap.get(e4.getAttribute("source"));
                ProcessNode target = nodeMap.get(e4.getAttribute("target"));
                Edge edge = new Edge(source, target);
                model.addEdge(edge);

                // Set probability for target edge
                edge.getTarget().setProperty(Transition.PROP_PROBABILITY, getProbability(e4));
            }

            // Add all Places into corresponding Lanes
            for (ProcessNode n: model.getNodes()) {
                if (n instanceof Place) {
                    Transition t = null;
                    Place p = (Place)n;
                    // Check if *p={}
                    if (model.getPredecessors(p).size()==0) {
                        // Belongs to Lane of following Transition
                        if (model.getSuccessors(p).size()==0) continue;
                        for (ProcessNode succ: model.getSuccessors(p)) {
                            // Consider first Transition
                            if (succ instanceof Transition) {
                                t = (Transition)succ;
                                break;
                            }
                        }
                        } else {
                            // Belongs to the Lane of the preceding Transition
                            for (ProcessNode pre: model.getPredecessors(p)) {
                                // Consider first Transition
                                if (pre instanceof Transition) {
                                t = (Transition)pre;
                                break;
                            }

                        }
                    }
                    // Find out Lane of Transition
                    for (Lane lane: lanes.values()) {
                        if (lane.isContained(t)) {
                            // Add Place to the same Lane
                            lane.addProcessNode(p);
                        }
                    }
                }
            }

            // Add Lanes to model
            for (Lane lane: lanes.values()) {
                model.addNode(lane);
            }

        }

        return resultNets;

    }

    public static void main(String[] args) {
        System.out.println("PNML2DOTConverter-Test");


        try {
            // Open file and convert to Document
            //FileInputStream in = new FileInputStream("models/philo.pnml");
            FileInputStream in = new FileInputStream("/Users/frank/Desktop/loopdemo.xml");
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            xmlFactory.setNamespaceAware(false);
            DocumentBuilder builder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = builder.parse(in);

            // Call converter
            PNMLImporter conv = new PNMLImporter(xmlDoc);
            List<ProcessModel> models = conv.getModels();

            // Show first result
            ProcessModel model = models.get(0);
            ProcessLayouter layouter = new DOTLayouter();
            layouter.layoutModel(LayoutUtils.getAdapter(model), 20, 20, ProcessLayouter.LAYOUT_HORIZONTAL);

            ProcessEditor editor = new ProcessEditor(model);
            JFrame frame = new JFrame("PNML import");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JScrollPane scrollPane = new JScrollPane(editor);

            frame.add(scrollPane);
            frame.pack();
            frame.setVisible(true);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<ProcessModel> parseSource(File f) throws Exception {
	    try{
	        List<ProcessModel> result = new LinkedList<ProcessModel>();

	        FileInputStream in = new FileInputStream(f);
	        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
	        xmlFactory.setNamespaceAware(false);
	        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
	        Document xmlDoc = builder.parse(in);

	        result = parsePNML(xmlDoc);

	        ProcessModel model = result.get(0);
	        ProcessLayouter layouter = new DOTLayouter();
	        layouter.layoutModel(LayoutUtils.getAdapter(model), 20, 20, ProcessLayouter.LAYOUT_HORIZONTAL);

	        return result;
        }catch(SAXParseException ex) {
        	throw new UnsupportedFileTypeException("Not an PNML File!");
        }
    }

    @Override
    public String getDisplayName() {
        return "Petri net Markup Language";
    }

    @Override
    public String[] getFileTypes() {
        String[] types = {"pnml"};
        return types;
    }
}
