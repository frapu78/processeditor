/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.inubit.research.client.UserCredentials;
import com.inubit.research.client.XmlHttpRequest;
import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.interfaces.AbstractModelAdapter;
import com.inubit.research.server.HttpConstants;
import com.inubit.research.server.ProcessEditorServerUtils;
import com.inubit.research.server.request.handler.UserRequestHandler;

/**
 * 
 * This class provides static methods for drawing process elements.
 * 
 * @author frank
 */
public abstract class ProcessUtils {

    public final static String ATTR_NAME = "name";
    public final static String ATTR_VALUE = "value";
    public final static String TAG_PROPERTY = "property";
    public final static String TAG_PROPERTIES = "properties";

    public final static String TRANS_PROP_CREDENTIALS = "credentials";
    public static Color commentColor = new Color(255, 255, 204);

    protected RoutingPointLayouter rpLayouter = null;

    public enum Orientation {

        TOP, CENTER, RIGHT, LEFT
    }

    public enum Position {

        TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT
    }
    //arrow head definitions
    private final static int xArrowPoints[] = {0, -10, -10};
    private final static int yArrowPoints[] = {0, 6, -6};
    public final static Polygon standardArrowFilled = new Polygon(xArrowPoints, yArrowPoints, 3);
    
    
    
    //strokes and line definitions
    public final static float dash1[] = {2.0f};
    public final static float dash2[] = {5.0f, 3.0f};
    public final static float dash3[] = {5.0f, 3.0f, 2.0f, 3.0f};
    public final static BasicStroke dashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash1, 0.0f);
    public final static BasicStroke boldDashedStroke = new BasicStroke(3.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash1, 0.0f);
    public final static BasicStroke longDashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash2, 0.0f);
    public final static BasicStroke dottedDashedStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            5.0f, dash3, 0.0f);
    public final static BasicStroke thinDashedStroke = new BasicStroke(0.5f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            3.0f, dash1, 0.0f);
    public final static BasicStroke thinStroke = new BasicStroke(0.5f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            3.0f, null, 0.0f);
    public final static BasicStroke extraThinStroke = new BasicStroke(0.25f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            1.0f, null, 0.0f);
    public final static BasicStroke gatterStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1.0f, null, 0.0f);
    public final static BasicStroke defaultStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10.0f, null, 0.0f);
    public final static BasicStroke boldStroke = new BasicStroke(1.5f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10.0f, null, 0.0f);
    public final static BasicStroke extraBoldStroke = new BasicStroke(3.0f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            10.0f, null, 0.0f);
    public final static Stroke doubleLinedStroke = new Stroke() {
        @Override
        public Shape createStrokedShape(Shape s) {
            return defaultStroke.createStrokedShape( extraBoldStroke.createStrokedShape( s) );
        }
    };
    
    /** The selection color */
    public static Color selectionColor = Color.RED;
    /** The stroke used for selection */
    public static BasicStroke selectionStroke = dashedStroke;
    /** The default color of the Rulers */
    public static Color RULERCOLOR = new Color(104, 104, 255);
    /** The default Font */
    public static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 11);
    /** The fade time for rulers etc. */
    public static final int RULER_FADE_TIME = 500;
   /** The fade time for selectors */
    public static final int SELECTOR_FADE_TIME = 125;
    /** The outline color */
    public final static Color OUTLINE_COLOR = new Color(220,220,220);

    public static void drawGatter(Graphics g, int xDist, int yDist) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(gatterStroke);
        g2.setPaint(new Color(230, 230, 230));
        for (int x = 0; x < g2.getClipBounds().x + g2.getClipBounds().width; x += xDist) {
            g2.drawLine(x, 0, x, g2.getClipBounds().y + g2.getClipBounds().height);
        }

        for (int y = 0; y < g2.getClipBounds().y + g2.getClipBounds().height; y += yDist) {
            g2.drawLine(0, y, g2.getClipBounds().x + g2.getClipBounds().width, y);
        }
    }

    public static void drawSelectionPoint(Graphics g, Point p) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(boldStroke);
        g2.setPaint(selectionColor);
        Shape s = new Ellipse2D.Double(p.x - 5, p.y - 5, 10, 10);
        g2.draw(s);
    }

    public static void drawSelectionBorder(Graphics g, Shape s) {
        if (s == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(selectionStroke);
        g2.setPaint(selectionColor);
        g2.draw(s);
    }

    public static void drawHighlight(Graphics g, Shape s) {
        if (s == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(boldStroke);
        g2.setPaint(Color.red);
        g2.draw(s);
    }

    public static void drawRubberBand(Graphics2D g, Rectangle r) {
        g.setStroke(dashedStroke);
        g.setPaint(Color.BLUE);
        g.drawRect(r.x, r.y, r.width, r.height);
    }

    public static void drawHorizontalAligmentRuler(Graphics2D g, int pos) {
        g.setStroke(boldDashedStroke);
        g.setPaint(RULERCOLOR);
        g.drawLine(0, pos, 10000, pos);
    }

    public static void drawVerticalAlignmentRuler(Graphics2D g, int pos) {
        g.setStroke(boldDashedStroke);
        g.setPaint(RULERCOLOR);
        g.drawLine(pos, 0, pos, 10000);
    }

    /**
     * Moves a polygon to new coordinates
     *
     */
    public static void movePolygon(Polygon poly, int offsetX, int offsetY) {
        for (int i = 0; i < poly.npoints; i++) {
            poly.xpoints[i] += offsetX;
            poly.ypoints[i] += offsetY;
        }
    }

    /**
     * Scales a polygon to a new size
     * @param poly
     * @param w
     * @param h
     */
    public static void scalePolygon(Polygon poly, int w, int h) {
        // Detect the leftmost/rightmost x-coordinate
        int x1 = 100000;
        int x2 = -100000;
        for (int x : poly.xpoints) {
            if (x < x1) {
                x1 = x;
            }
            if (x > x2) {
                x2 = x;
            }
        }
        // Detect the leftmoste/rightmost y-coordinate
        int y1 = -100000;
        int y2 = 100000;
        for (int y : poly.ypoints) {
            if (y > y1) {
                y1 = y;
            }
            if (y < y2) {
                y2 = y;
            }
        }
        // Calculate scale factor
        double xFac = (double) w / (double) (x2 - x1);
        double yFac = (double) h / (double) (y1 - y2);
        // Scale to factor
        for (int i = 0; i < poly.npoints; i++) {
            poly.xpoints[i] = (int) ((double) poly.xpoints[i] * xFac);
            poly.ypoints[i] = (int) ((double) poly.ypoints[i] * yFac);
        }
    }

    /**
     * Rotates a polygon.
     * @param poly
     * @param degree 
     */
    public static void rotatePolygon(Polygon poly, double degree) {
        int[] nx = new int[poly.npoints];
        int[] ny = new int[poly.npoints];

        for (int i = 0; i < poly.npoints; i++) {
            // x'= x*cos(degree)+y*sin(degree)
            nx[i] = (int) Math.round(((double) poly.xpoints[i]) * Math.cos(degree) + ((double) poly.ypoints[i]) * Math.sin(degree));
            // y'= x*cos(degree)-y*sin(degree)
            ny[i] = (int) Math.round(((double) poly.xpoints[i]) * Math.sin(degree) - ((double) poly.ypoints[i]) * Math.cos(degree));
        }

        poly.xpoints = nx;
        poly.ypoints = ny;

    }

    public static Rectangle drawText(Graphics2D g2, int x, int y, int maxLength, String text, Orientation o) {
        return drawText(g2, x, y, maxLength, text, o, false, true,1.0f);
    }

    public static Rectangle drawText(Graphics2D g2, int x, int y, int maxLength, String text, Orientation o, boolean clearBackground) {
        return drawText(g2, x, y, maxLength, text, o, clearBackground, true,1.0f);
    }
    
    /**
     * Draws text centered at the given position. Also multiple lines are
     * supported.
     */
    public static Rectangle drawText(Graphics2D g2, int x, int y, int maxLength, String text, Orientation o, boolean clearBackground, boolean lineBreaks) {
    	return drawText(g2, x, y, maxLength, text, o, clearBackground, lineBreaks, 1.0f);
    }
    
    /**
     * Draws text centered at the given position. Also multiple lines are
     * supported.
     */
    public static Rectangle drawText(Graphics2D g2, int x, int y, int maxLength, String text, Orientation o, boolean clearBackground, boolean lineBreaks, float backgroundAlpha) {
        // A rectangle containing the bounding box of the drawn text
        //divide by 2, overflow can occur otherwise!!
        Rectangle bounds = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);
        // get metrics from the graphics
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        // get the height of a line of text in this font and render context
        int hgt = metrics.getHeight();
        if(hgt%2 == 1) { //for division by 2
        	hgt++;
        }
        // Preprocess text (add line breaks after maxLength)
        String newText = "";
        if (lineBreaks) {
            String currLine = "";         
            String[] st = text.split(" ");            
            // Parse word for word
            for(int i=0;i<st.length;i++){
                String currWord = st[i];
                if ((metrics.stringWidth(currLine + (i==0?"":" ") + currWord) > maxLength) & (!currLine.isEmpty())) {
                    // Add line break
                    newText += currLine + "\n";
                    currLine = currWord;
                } else {
                    // add current word to current line
                    currLine += (i==0?"":" ") + currWord;
                }
            }
            // Add last line without line break
            newText += currLine;
        } else {
            newText = text;
        }

        // Split text into different lines
//        boolean manual = false;
        StringTokenizer st = new StringTokenizer(newText, "\n");
        int numOfLines = st.countTokens();
//      if (st.countTokens() == 1) {
        	  // Try manual line breaks
//            st = new StringTokenizer(newText, "\\");
//            if (st.countTokens() > 1) {
//                manual = true;
//            }
//      }
        int yOffset = hgt;

        // Check if center orientation, in this case update yOffset
        if (o == Orientation.CENTER) {
            yOffset -= (numOfLines / 2.0) * hgt;
        }

        while (st.hasMoreElements()) {
            String currText = st.nextToken();
//            if (manual == true && currText.startsWith("n")) {
//                currText = currText.substring(1);
//            }
            // get the advance of my text in this font and render context
            int stringWidth = metrics.stringWidth(currText); 
            if (stringWidth > maxLength) {
                while (stringWidth > maxLength) {
                    if ((currText.length() - 2) > 0) {
                        currText = currText.substring(0, currText.length() - 2);
                    } else {
                        currText = "";
                        break;
                    }
                    stringWidth = metrics.stringWidth(currText + "...");
                }
                currText += "...";
            }

            // Check if orientation is RIGHT
            int xStart;
            if (o == Orientation.RIGHT) {
            	xStart = x - stringWidth;
            }else if (o == Orientation.LEFT) {
                xStart = x;
            }else { //CENTER
            	xStart = x - (stringWidth / 2); 
            }

            Color textColor = g2.getColor();

            // Draw the text
            if (clearBackground) {
            	if(backgroundAlpha < 1.0f) {
	            	AlphaComposite _alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundAlpha);
	            	g2.setComposite(_alpha);
            	}
                g2.setColor(Color.WHITE);
                g2.fillRect(xStart, y + yOffset - hgt, stringWidth, hgt);
                if(backgroundAlpha < 1.0f) {
	            	AlphaComposite _alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	            	g2.setComposite(_alpha);
            	}
            }
            g2.setColor(textColor);
            g2.drawString(currText, xStart, y + yOffset);
            

            // Set bounds
            if (bounds.x > xStart) {
                bounds.x = xStart;
            }
            if ((bounds.x + bounds.width) < (xStart + stringWidth)) {
                bounds.width = stringWidth;
            }                     
         // Increase yOffset (for more lines)
            if(st.hasMoreElements()) //but only if it is not the last line
            	yOffset += hgt;
        }
        //height and y can be set directly
        bounds.height = (numOfLines * hgt); 
        if(o == Orientation.CENTER) {
        	bounds.y = y - bounds.height/2 + hgt/4;
        }else {
        	bounds.y = y + hgt/4;
        }

        return bounds;
    }

    /**
     * Draws text centered at the given position (vertical).
     * @param maxLength -  maximum width of the text, not working yet!
     * @param o - not used yet!
     */
    public static void drawTextVertical(Graphics2D g2, int x, int y, int maxLength, String text, Orientation o) {
        // get font metrics
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());

        // get the height of a line of text in this font and render context
        //int hgt = metrics.getHeight();
        // get the length of the text        
        int lgt = metrics.stringWidth(text);
        
        AffineTransform oldAF = g2.getTransform();
       
        g2.rotate(-Math.PI/2, x, y + (lgt/2));
                
        // Draw the text vertical
        g2.drawString(text, x, y + (lgt/2));
        
        g2.setTransform(oldAF);

        
        /*
            AffineTransform at = new AffineTransform();
            at.setToRotation(Math.PI / 4.0);
            g2d.setTransform(at);
            g2d.drawString("aString", 100, 100);
        */

    }

    public static int dist(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
    }

    /**
     * Draws text centered at the given position. Automatically inserts
     * line breaks.
     * @param height - not used yet!
     */
    public static void drawFitText(Graphics2D g2, int x, int y, int width, int height, String text) {
        // get metrics from the graphics
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        // get the height of a line of text in this font and render context
        int hgt = metrics.getHeight();
        int yOffset = hgt;

        // Set minimum width
        if (width < 10) {
            width = 10;
        }

        String remainingText = text;

        while (remainingText.length() > 0) {
            String currText = remainingText;
            // get the advance of my text in this font and render context
            int adv = metrics.stringWidth(currText);
            if (adv > width) {
                while (adv > width) {
                    if (currText.lastIndexOf(' ') == 0) {
                        currText = currText.substring(0, currText.length() - 2);
                    } else {
                        if (currText.lastIndexOf(' ') == -1) {
                            currText = "";
                            remainingText = "";
                        } else {
                            currText = currText.substring(0, currText.lastIndexOf(' '));
                        }
                    }
                    adv = metrics.stringWidth(currText);
                }
            }
            remainingText = remainingText.substring(currText.length()).trim();

            // Draw the text
            g2.drawString(currText, x - width / 2, y + yOffset);
            // Increase yOffset (for more lines)
            yOffset += hgt;
        }
    }

    /**
     * Draws a nice-looking glas reflex.
     * @param g2
     */
    public static void drawReflex(Graphics2D g2, ProcessNode n, Position pos) {
        Composite oldComposite = g2.getComposite();
        Path2D path = new Path2D.Double();
        // Handle all supported cases here
        switch (pos) {
            case TOP:
                path.moveTo(n.getPos().x - n.getSize().width / 2, n.getPos().y - n.getSize().height / 2);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y - n.getSize().height / 2);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y);
                path.lineTo(n.getPos().x - n.getSize().width / 2, n.getPos().y);
                break;
            case BOTTOM:
                path.moveTo(n.getPos().x - n.getSize().width / 2, n.getPos().y);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y + n.getSize().height / 2);
                path.lineTo(n.getPos().x - n.getSize().width / 2, n.getPos().y + n.getSize().height / 2);
                break;
            case BOTTOM_RIGHT:
                path.moveTo(n.getPos().x + n.getSize().width / 2, n.getPos().y - n.getSize().height / 2);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y + n.getSize().height / 2);
                path.lineTo(n.getPos().x - n.getSize().width / 2, n.getPos().y + n.getSize().height / 2);
                break;
            default:
                path.moveTo(n.getPos().x - n.getSize().width / 2, n.getPos().y - n.getSize().height / 2);
                path.lineTo(n.getPos().x + n.getSize().width / 2, n.getPos().y - n.getSize().height / 2);
                path.lineTo(n.getPos().x - n.getSize().width / 2, n.getPos().y + n.getSize().height / 2);
        }
        path.closePath();

        g2.setColor(Color.LIGHT_GRAY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.fill(path);

        // Restore Composite
        g2.setComposite(oldComposite);
    }

    /**
     * This method reads a ProcessModel from an URI and creates
     * a ProcessModel.
     *
     * @deprecated Fetching models with global credentials might be removed at any time.
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static ProcessModel parseProcessModelSerialization(URI modelUri) throws IOException, ParserConfigurationException, Exception {
        // Fetch ProcessModel
        XmlHttpRequest req = new XmlHttpRequest(modelUri);
        req.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, HttpConstants.CONTENT_TYPE_APPLICATION_PROCESSMODEL);
        Document doc = req.executeGetRequest();
        ProcessModel model = ProcessUtils.parseProcessModelSerialization(doc);
        model.setProcessModelURI(modelUri.toASCIIString());
        model.markAsDirty(false);
        return model;
    }

    /**
     * This method reads a ProcessModel from an URI and creates
     * a ProcessModel.
     *
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static ProcessModel parseProcessModelSerialization(URI modelUri, UserCredentials credentials) throws IOException, ParserConfigurationException, Exception {
        // Fetch ProcessModel
        XmlHttpRequest req = new XmlHttpRequest(modelUri);
        req.setRequestProperty(HttpConstants.HEADER_KEY_ACCEPT, HttpConstants.CONTENT_TYPE_APPLICATION_PROCESSMODEL);
        if (credentials!=null) {
            // Check if pre-defined cookies exist
            if (credentials.getCookies()!=null) {
                // Copy all existing cookies
                req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, credentials.getCookies());
            } else {
                // Only set session attribute cookie
                req.setRequestProperty(HttpConstants.HEADER_KEY_COOKIE, UserRequestHandler.SESSION_ATTRIBUTE+"="+credentials.getSessionId());
            }
        }
        Document doc = req.executeGetRequest();
        ProcessModel model = ProcessUtils.parseProcessModelSerialization(doc);
        model.setProcessModelURI(modelUri.toASCIIString());
        model.markAsDirty(false);
        model.setTransientProperty(TRANS_PROP_CREDENTIALS, credentials);
        model.setProperty(ProcessModel.PROP_EDITOR, credentials.getUser());
        // Free login
        credentials.logout();
        return model;
    }






    /**
     * This method reads a ProcessModel from an InputStream and creates
     * a ProcessModel.
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static ProcessModel parseProcessModelSerialization(InputStream in) throws Exception {

        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        xmlFactory.setNamespaceAware(false);
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document xmlDoc = builder.parse(in);

        return parseProcessModelSerialization(xmlDoc);
    }
    
    /**
     * This method reads a ProcessModel from an URI and creates
     * a ProcessModel.
     * @deprecated Use variant with credentials!
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static ProcessModel parseProcessModelSerialization(String uri) throws Exception {

        return parseProcessModelSerialization(new URI(uri));
    }

    /**
     * This method reads a ProcessModel from an DOM Document and creates
     * a ProcessModel.
     * @param in
     * @return
     * @throws java.lang.Exception
     */
    public static ProcessModel parseProcessModelSerialization(Document xmlDoc) throws Exception {
        ProcessModel model = null;

        HashMap<String, ProcessNode> processNodeMap = new HashMap<String, ProcessNode>();
        HashMap<String, ProcessEdge> processEdgeMap = new HashMap<String, ProcessEdge>();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        // Retrieve model type
        String query = "/" + ProcessModel.TAG_MODEL;
        Object res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) res;

        if (nodes.getLength() != 1) {
            throw new Exception("Wrong number of root nodes!");
        }
        if (!nodes.item(0).getNodeName().equals(ProcessModel.TAG_MODEL)) {
            throw new Exception("Wrong root node!");
        }

        Element rootNode = (Element) nodes.item(0);

        // Retrieve process name and type
        String processName = rootNode.getAttribute(ProcessModel.ATTR_NAME);
        String processType = rootNode.getAttribute(ProcessModel.ATTR_TYPE);
        String processId = rootNode.getAttribute(ProcessModel.ATTR_ID);

        // Try to create corresponding process model instance
        Object o = Class.forName(processType).newInstance();
        if (!(o instanceof ProcessModel)) {
            throw new Exception("Unkown process model type!");
        }
        // Cast to process model
        model = (ProcessModel) o;

        // Set name
        model.setProcessName(processName);
        // Set id
        model.setId(processId);

        // Read model properties
        query = "/" + ProcessModel.TAG_MODEL + "/" + ProcessModel.TAG_PROPERTIES;
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        nodes = (NodeList) res;
        if (nodes.getLength() >= 1) {
            // Just consider first property element
            HashMap<String, String> modelProps = readProperties(xpath, nodes.item(0));
            // Add properties to model
            for (String key : modelProps.keySet()) {
                model.setProperty(key, modelProps.get(key));
            }
        }

        // Retrieve all elements
        query = "/" + ProcessModel.TAG_MODEL + "/" + ProcessModel.TAG_NODES + "/" + ProcessNode.TAG_NODE;
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        nodes = (NodeList) res;

        for (int i = 0; i < nodes.getLength(); i++) {
            ProcessNode pn = ProcessNode.newInstanceFromSerialization(nodes.item(i));
            // Add node to model
            model.addNode(pn);
            // Insert node into hashmap for later lookup
            processNodeMap.put(pn.getProperty(ProcessNode.PROP_ID), pn);
        }

        // Retrieve all edges
        query = "/" + ProcessModel.TAG_MODEL + "/" + ProcessModel.TAG_EDGES + "/" + ProcessEdge.TAG_EDGE;
        res = xpath.evaluate(query, xmlDoc, XPathConstants.NODESET);
        nodes = (NodeList) res;

        for (int i = 0; i < nodes.getLength(); i++) {
            // Create flows according to type
            ProcessEdge pn = ProcessEdge.newInstanceFromSerialization(nodes.item(i), processNodeMap);
            // Add process edge to model
            model.addEdge(pn);
            // Insert edge into hashmap
            processEdgeMap.put(pn.getProperty(ProcessEdge.PROP_ID), pn);
        }

        // Update cluster containments
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof Cluster) {
                Cluster c = (Cluster) n;
                // Iterate over all ids of "#nodes"
                StringTokenizer st = new StringTokenizer(n.getProperty(Cluster.PROP_CONTAINED_NODES), ";");
                while (st.hasMoreTokens()) {
                    c.addProcessNode(processNodeMap.get(st.nextToken()));
                }
            }
        }

        // Update EdgeDockers
        for (ProcessNode n : model.getNodes()) {
            if (n instanceof EdgeDocker) {
                EdgeDocker docker = (EdgeDocker) n;
                docker.setDockedEdge(processEdgeMap.get(n.getProperty(EdgeDocker.PROP_DOCKED_EDGE)));
            }
        }

        return model;
    }



    /**
     * refactors a node and also takes care
     * of repainting and updating the selections
     * @param editor
     * @param selectedNode
     * @param nodeType
     */
    public static void refactorNode(ProcessEditor editor,ProcessNode selectedNode,Class<?> nodeType) {

        System.out.println("Refactoring "+selectedNode+" to "+nodeType);
        try {
	        ProcessNode newNode = refactorNode(editor.getModel(), selectedNode, nodeType);       
	        // SelectionHandler
	        editor.getSelectionHandler().removeSelectedObject(selectedNode);
	        editor.getSelectionHandler().addSelectedObject(newNode);
	        // Inform listeners
	        for (ProcessEditorListener l : editor.getListeners()) {
	            l.processObjectClicked(newNode);
	        }        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        editor.repaint();
    }

	public static ProcessNode refactorNode(ProcessModel model,
			ProcessNode selectedNode, Class<?> nodeType)
			throws InstantiationException, IllegalAccessException {
		// Collect all incoming/outgoing edges
		List<ProcessEdge> inEdges = new LinkedList<ProcessEdge>();
		List<ProcessEdge> outEdges = new LinkedList<ProcessEdge>();
		for (ProcessEdge edge : model.getEdges()) {
		    if (edge.getTarget() == selectedNode) {
		        inEdges.add(edge);
		    }
		    if (edge.getSource() == selectedNode) {
		        outEdges.add(edge);
		    }
		}

      
		ProcessNode newNode = (ProcessNode) nodeType.newInstance();
		newNode.setText(selectedNode.getText());
		newNode.setPos(selectedNode.getPos());
		newNode.setStereotype(selectedNode.getStereotype());
		newNode.setSize(selectedNode.getSize().width, selectedNode.getSize().height);
		

		model.addNode(newNode);

		//update cluster containment, so that refactored node
		//still belongs to the cluster and is moved with it
		List<Cluster> cluster = model.getClusters();
		for (Cluster c : cluster) {
		    if (c.isContained(selectedNode)) {
		        c.removeProcessNode(selectedNode);
		        c.addProcessNode(newNode);
		    }
		}
                String oldId = selectedNode.getId();
		model.removeNode(selectedNode, false);
		//ID should be the last thing to set as otherwise the new node will get deleted
		//e.g. from its newly assigned cluster as newNode.equals(selectedNode) yields true
		//Set id of newNode to oldNode
		newNode.setId(oldId);                
                
                for (ProcessEdge edge : inEdges) {
		    edge.setTarget(newNode);
		}
		for (ProcessEdge edge : outEdges) {
		    edge.setSource(newNode);
		}
                
		return newNode;
	}

    public static HashMap<String, String> readProperties(XPath xpath, Node node) {
        HashMap<String, String> props = new HashMap<String, String>();
        String query;
        Object res;
        // Get all properties
        query = "./" + TAG_PROPERTY;
        try {
            res = xpath.evaluate(query, node, XPathConstants.NODESET);
        } catch (Exception ex) {
            ex.printStackTrace();
            return props;
        }
        NodeList propertyNodes = (NodeList) res;

        for (int i1 = 0; i1 < propertyNodes.getLength(); i1++) {
            Element property = (Element) propertyNodes.item(i1);

            String key = property.getAttribute(ATTR_NAME);
            String value = property.getAttribute(ATTR_VALUE);

            // Hack to update old ProcessModels with editable sources and targets
            if (key.equals("sourceNode")) {
                key = ProcessEdge.PROP_SOURCENODE;
            }
            if (key.equals("targetNode")) {
                key = ProcessEdge.PROP_TARGETNODE;
            }

            props.put(key, value);
        }
        return props;
    }

    /**
     * Returns the default edge class for connecting to process nodes. 
     * @param source
     * @param target
     * @return
     */
    public abstract ProcessEdge createDefaultEdge(ProcessNode source, ProcessNode target);

    /**
     * Returns the list of supported layouters for this model.
     * @return
     */
    public abstract List<ProcessLayouter> getLayouters();

    /**
     * Returns the list of recommendations for a following ProcessNode based
     * @param model The ProcessModel
     * @param node The ProcessNode used for recommandation
     */
    public List<Class<? extends ProcessNode>> getNextNodesRecommendation(ProcessModel model, ProcessNode node) {
        // Return empty list by default, might be overwritten by subclasses.
        return new LinkedList<Class<? extends ProcessNode>>();
    }

    /**
     * helper method that writes a Properties Map to an xmlNode
     * @param xmlDoc
     * @param nodeToAddTo
     * @param properties
     */
    public static void writeProperties(Document xmlDoc, Element nodeToAddTo, HashMap<String, String> props) {
        // Insert all properties
        for (String key : props.keySet()) {
            Element property = xmlDoc.createElement(TAG_PROPERTY);
            property.setAttribute(ATTR_NAME, key);
            property.setAttribute(ATTR_VALUE, props.get(key));
            nodeToAddTo.appendChild(property);
        }
    }

    /**
     * @param model
     * @return
     */
    public static AbstractModelAdapter getAdapter(ProcessModel model) {
        return LayoutUtils.getAdapter(model);
    }

    public static void drawImage(String resourceName, Graphics2D g2, int x, int y) {
        try {
            ImageIcon img = new ImageIcon(ProcessUtils.class.getResource(resourceName));
            g2.drawImage(img.getImage(), x, y, null);
        } catch (Exception ex) {
            // sorry, cannot be drawn...
        }
    }

    public static Image createPreviewImage(Class<?> c, int size) {
        try {
            ProcessNode _node = (ProcessNode) c.newInstance();

            BufferedImage _buff;
            _buff = ProcessEditorServerUtils.createNodeImage(_node);
            return _buff.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Taken from http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
     * @param image
     * @return
     */
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }

    /**
     * Taken from http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
     * @param image
     * @return
     */
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image using the default color model
        int type = BufferedImage.TYPE_INT_RGB;
        if (hasAlpha) {
            type = BufferedImage.TYPE_INT_ARGB;
        }
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    /**
     * uses the movToFront to arrange the nodes in a non-overlapping manner.
     * This is necessary, as the nodes in the workflow.xml can in an arbitrary ordering.
     */
    public static void sortTopologically(ProcessModel model) {
        //work on a copy as moveToFront changes the list
        ArrayList<ProcessNode> _nodes = new ArrayList<ProcessNode>();
        _nodes.addAll(model.getNodes());

        //moving subProcesses to the front, starting with the largest
        HashSet<ProcessNode> _moved = new HashSet<ProcessNode>();
        ProcessNode _largest;
        do {
            _largest = null;
            //finding the (widest) subProcess, so it is behind the other elements
            for (ProcessNode sub : _nodes) {
                if ((sub instanceof Cluster) && !_moved.contains(sub)) {
                    if (_largest == null) {
                        _largest = sub;
                    } else {
                        if (_largest.getSize().getWidth() < sub.getSize().getWidth()) {
                            _largest = sub;
                        }
                    }
                }
            }
            if (_largest != null) {
                _moved.add(_largest);
                model.moveToFront(_largest);
            }
        } while (_largest != null);
        //moving all other nodes to the front (due to that, pools are in the back)
        for (ProcessNode p : _nodes) {
            if (!(p instanceof Cluster)) {
                //changes list structure, but does not cause problems!
                model.moveToFront(p);
            }
        }
    }

    // Returns a set of templates for a certain model class
    public static List<ProcessModel> getTemplates(ProcessModel orig) {
        List<ProcessModel> result = new LinkedList<ProcessModel>();

        // @todo: Needs fetch templates from template directory!!!
        result.add(orig);

        if (orig instanceof BPMNModel) {
            BPMNModel test = new BPMNModel();
            test.setProperty(ProcessModel.PROP_COMMENT, "Hello World in BPMN");
            Task t1 = new Task(100, 50, "Hello");
            Task t2 = new Task(250, 50, "World");
            SequenceFlow f1 = new SequenceFlow(t1, t2);
            test.addNode(t1);
            test.addNode(t2);
            test.addEdge(f1);
            result.add(test);
        }


        return result;
    }

    public static void dumpXML(Document xmlDoc) throws IOException {
        Writer writer = new StringWriter();
        ProcessEditorServerUtils.writeXMLtoStream(writer,xmlDoc);
        writer.close();
        System.out.println(writer.toString());
    }

    /**
     * Returns the RoutingPointLayouter for the given model type via layz initialization.
     * Default is null.
     * @return
     */
    public RoutingPointLayouter getRoutingPointLayouter() {
        if (rpLayouter==null) rpLayouter = new DefaultRoutingPointLayouter();
        return rpLayouter;
    }

}
