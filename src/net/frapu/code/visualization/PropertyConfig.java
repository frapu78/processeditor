/*
 * Process Editor
 *
 * (C) 2010 inubit AG
 *
 * http://inubit.com
 *
 */
package net.frapu.code.visualization;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author fel
 */
public class PropertyConfig {
    private static final String RESOURCE_NAME = "/properties.xml";
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();
    private static final XPath xpath = xpathFactory.newXPath();

    private static final String TYPE_BASE = "base";
    private static final String TYPE_EXTENDED = "extended";

    private static Set<Class> supportedClasses;
    private static Document configDoc = null;

    /**
     * Initialize document and supported classes
     */
    static {
        URL resUrl = PropertyConfig.class.getResource(RESOURCE_NAME);
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        
        try {
            xmlFactory.setNamespaceAware(false);
            DocumentBuilder db = xmlFactory.newDocumentBuilder();

            configDoc = db.parse( resUrl.openStream() );

            supportedClasses = new HashSet<Class>();

            String query = "//class[@name]";
            NodeList result = (NodeList) xpath.evaluate(query, configDoc, XPathConstants.NODESET);

            for ( int i = 0; i < result.getLength(); i++ ) {
                Node n = result.item(i);
                String className = n.getAttributes().getNamedItem("name").getNodeValue();
                try {
                    Class c =  Class.forName(className);
                    supportedClasses.add(c);
                } catch ( ClassNotFoundException ex ) {
                    System.err.println("Class not found: " + className);
                } catch ( NoClassDefFoundError er ) {
                    System.err.println("Class not found: " + className);
                }
            }
        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        } 
    }

    /**
     * Determine the property's label
     * @param o the object that has the given porperty
     * @param propertyName the property's name
     * @param l the requested language
     * @return
     */
    public static String getPropertyLabel( Object o, String propertyName, Locale l ) {
        return getPropertyLabel(o, propertyName, l.toString());
    }

    public static String getPropertyLabel( Object o, String propertyName, String lang ) {
        if ( configDoc == null )
            return propertyName;

        String query = buildQuery(o.getClass(), propertyName, lang);

        try {
            //check direct class name
            Node n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);

            if ( n == null ) {
                //determine all supported superclasses and check them
                Set<Class> superclasses = determineSupportedSuperclasses(o);

                for ( Class c : superclasses ) {
                    query = buildQuery(c, propertyName, lang);
                    n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);

                    if ( n != null ) break;
                }
            }

            if ( n == null ) {
                //check default values
                query = buildQuery(null, propertyName, lang);
                n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);
            }

            return ( n != null ? n.getTextContent() : propertyName );
        } catch ( Exception ex ) {
            ex.printStackTrace();
            return propertyName;
        }
    }

    public static String getPropertyType( Object o, String propertyName ) {
        if ( configDoc == null )
            return TYPE_BASE;

        String query = buildQuery( o.getClass() , propertyName, null );

        try {
            //check direct class name
            Node n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);

            if ( n == null ) {
                //determine all supported superclasses and check them
                Set<Class> superclasses = determineSupportedSuperclasses(o);

                for ( Class c : superclasses ) {
                    query = buildQuery(c, propertyName, null);
                    n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);

                    if ( n != null ) break;
                }
            }

            if ( n == null ) {
                //check default values
                query = buildQuery(null, propertyName, null);
                n = (Node) xpath.evaluate(query, configDoc, XPathConstants.NODE);
            }

            return ( n != null && n.getAttributes().getNamedItem("type") != null ?
                        n.getAttributes().getNamedItem("type").getNodeValue() : TYPE_BASE );
        } catch ( Exception e ) {
            e.printStackTrace();
            return TYPE_BASE;
        }
    }

    private static Set<Class> determineSupportedSuperclasses( Object o ) {
        Set<Class> superclasses = new HashSet<Class>();

        for ( Class c : supportedClasses )
            if ( c.isInstance(o) )
                superclasses.add(c);

        return superclasses;
    }

    private static String buildQuery( Class c, String propertyName, String lang ) {
        StringBuffer sb = new StringBuffer();

        if ( c != null ) {
            sb.append("//class[@name='");
            sb.append(c.getName());
            sb.append("']");
        } else {
            sb.append("//default");
        }
        
        sb.append("/property[@name='");
        sb.append(propertyName);
        sb.append("']");

        if ( lang != null ) {
            sb.append("/label[@language='");
            sb.append(lang);
            sb.append("']");
        }

        return sb.toString();
    }
}
