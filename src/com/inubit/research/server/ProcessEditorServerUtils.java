/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.frapu.code.visualization.ProcessEditor;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility functions for ProcessEditorServer and all its handlers
 * @author fel
 */
public class ProcessEditorServerUtils {

//    private static final byte[] lineBreak = "\n".getBytes();
    /**
     * writes the given XML Document to the given Writer.
     * @param osw - can be either a Java.io.Writer or a java.io.OutputStream
     * @param doc - the XML Document to be written to the osw
     * @throws IOException
     */
    public static void writeXMLtoStream(Object osw, Document doc) throws IOException {
        try {
            TransformerFactory _transformerFactory = TransformerFactory.newInstance();
            //number of spaces per indent
            try {
                _transformerFactory.setAttribute("indent-number", new Integer(4));
            } catch (IllegalArgumentException ex) {
                // Ignore
            }
            Transformer _transformer = _transformerFactory.newTransformer();
            //tell the transformer to format the output xml nicely
            _transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource _source = new DOMSource(doc);
            StreamResult _result;
            if (osw instanceof OutputStream) {
                _result = new StreamResult((OutputStream) osw);
            } else {
                _result = new StreamResult((Writer) osw);
            }
            _transformer.transform(_source, _result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create PNG graphics for the given model
     * @param currentModel the current model the image is created from
     * @return the created image
     */
    public static BufferedImage createNodeImage( ProcessNode node ) {
        // Create buffered image
        Rectangle bounds = node.getBoundingBox();
        BufferedImage img = new BufferedImage(bounds.width+6, bounds.height+6, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();
        AffineTransform _trans = new AffineTransform();
        //_trans.setToTranslation(-(node.getPos().x - node.getBoundingBox().width/2 - 3), -(node.getPos().y - node.getSize().height/2 -3));
        _trans.setToTranslation(-(bounds.x-3), -(bounds.y-3));
        g.setTransform(_trans);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setBackground(new Color(255,255,0,0));
        g.clearRect(0, 0, bounds.width+6, bounds.height+6);
        node.paint(g);

        return img;
    }

    public static BufferedImage createModelImage(ProcessModel model) {
        ProcessEditor editor = ProcessEditorPool.getPool().getEditor();

        editor.setModel(model);

        editor.setSize(((int)editor.getPreferredSize().getWidth()) + 6, ((int) editor.getPreferredSize().getHeight()) + 6);

        editor.setEditable(false);
        editor.setDrawBackground(false);

        BufferedImage img = new BufferedImage(editor.getSize().width+6, editor.getSize().height+6, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = img.createGraphics();
        g.setBackground(new Color(255,255,0,0));
        g.clearRect(0, 0, editor.getSize().width+6, editor.getSize().height+6);

        editor.paintComponent(g);
        ProcessEditorPool.getPool().returnEditor(editor);

        return img;
    }

    public static String createNodeBoundsXMLFragment(ProcessNode node) {
        StringBuilder b = new StringBuilder(200);
        b.append("<property name='x' value='");
        b.append(node.getPos().x +"'/>");
        b.append("<property name='y' value='");
        b.append(node.getPos().y +"'/>");
        b.append("<property name='width' value='");
        b.append(node.getSize().width +"'/>");
        b.append("<property name='height' value='");
        b.append(node.getSize().height +"'/>");

        return b.toString();
    }

    /**
     * Replaces all reservered characters with escape codes, including "/" and "?"!!!
     * @param str
     * @return
     */
    public static String escapeString(String str) {
        if (str==null) return null;

        str = str.replaceAll("%","%25");
        str = str.replaceAll(" ","%20");
        str = str.replaceAll("<","%3C");
        str = str.replaceAll(">","%3E");
        str = str.replaceAll("#","%23");
        str = str.replaceAll("\\{","%7B");

        str = str.replaceAll("\\}","%7D");
        str = str.replaceAll("\\|","%7C");
        str = str.replaceAll("\\\\","%5C");
        str = str.replaceAll("\n", "%0A");
        str = str.replaceAll("\\^","%5E");
        str = str.replaceAll("~","%7E");

        str = str.replaceAll("\\[","%5B");
        str = str.replaceAll("\\]","%5D");
        str = str.replaceAll("'","%60");
        str = str.replaceAll(";","%3B");
        str = str.replaceAll("/","%2F");

        str = str.replaceAll("\\?","%3F");
        str = str.replaceAll(":","%3A");
        str = str.replaceAll("@","%40");
        str = str.replaceAll("=","%3D");
        str = str.replaceAll("&","%26");
        str = str.replaceAll("\\$","%24");

         str = str.replaceAll("\u00e4", "%E4");
         str = str.replaceAll("\u00c4", "%C4");
         str = str.replaceAll("\u00f6", "%F6");
         str = str.replaceAll("\u00d6", "%D6");
         str = str.replaceAll("\u00fc", "%FC");
         str = str.replaceAll("\u00dc", "%DC");
         str = str.replaceAll("\u00df", "%DF");

        return str;
    }

    public static String unEscapeString(String str) {
        if (str==null) return null;

        str = str.replaceAll("%20"," ");
        str = str.replaceAll("%3C","<");
        str = str.replaceAll("%3E",">");
        str = str.replaceAll("%23","#");
        str = str.replaceAll("%7B","{");

        str = str.replaceAll("%7D","}");
        str = str.replaceAll("%7C","|");
        str = str.replaceAll("%5C","\\\\");
        str = str.replaceAll("%0A","\n");
        str = str.replaceAll("%5E","^");
        str = str.replaceAll("%7E","~");

        str = str.replaceAll("%5B","[");
        str = str.replaceAll("%5D","]");
        str = str.replaceAll("%60","'");
        str = str.replaceAll("%3B",";");
        str = str.replaceAll("%2F",",");

        str = str.replaceAll("%3F","?");
        str = str.replaceAll("%3A",":");
        str = str.replaceAll("%40","@");
        str = str.replaceAll("%3D","=");
        str = str.replaceAll("%26","&");
        str = str.replaceAll("%24","$");

        str = str.replaceAll("%E4", "\u00e4");
        str = str.replaceAll("%C4", "\u00c4");
        str = str.replaceAll("%F6", "\u00f6");
        str = str.replaceAll("%D6", "\u00d6");
        str = str.replaceAll("%FC", "\u00fc");
        str = str.replaceAll("%DC", "\u00dc");
        str = str.replaceAll("%DF", "\u00df");

        str = str.replaceAll("%25","%");
        str = str.replaceAll("%22","\"");
        str = str.replaceAll("%2C",",");

        return str;
    }

    /**
     * Parse the childnodes of a node and look for &lt;property&gt; elements with attributes name and value.
     * Example:
     * &lt;node&gt;
     *    &lt;property name='n' value='v'/&gt;
     * &lt;node/&gt;
     * @param n the XML node
     * @return the parsed properties
     * @deprecated Use XMLHelper.parseProperties( Node doc ) instead
     */
    @Deprecated
    public static Map<String, String> parseProperties(Node doc) {
        NodeList propertyNodes = doc.getChildNodes();
        Map<String, String> properties = new HashMap<String, String>();

        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node node = propertyNodes.item(i);

            if (node.getNodeName().equals("property")) {
                String key = node.getAttributes().getNamedItem("name").getNodeValue();
                String value = node.getAttributes().getNamedItem("value").getNodeValue();

                properties.put(key, value);
            }
        }

        return properties;
    }

    /**
     * Parse an HTTP-Request query string
     * @param query the query string
     * @return the parameters
     * @deprecated Use RequestUtils.getQueryParameters( RequestFacade req ) instead
     */
    @Deprecated
    public static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> params = new HashMap<String, String>();
        if(query != null) {
	        String[] parts = query.split("\\&");

	        for (String part : parts) {
	        	if(part.contains("=")) {
	        		String[] _values = part.split("=");
	        		if(_values.length >= 2) {
	        			params.put(_values[0], _values[1]);
	        		}
	        	}
	        }
        }
        return params;
    }

    public static String getMD5Hash( String s ) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        md.reset();
        byte[] result = md.digest(s.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            if(result[i] <= 15 && result[i] >= 0){
                    hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & result[i]));
        }

        return hexString.toString();
    }
}
