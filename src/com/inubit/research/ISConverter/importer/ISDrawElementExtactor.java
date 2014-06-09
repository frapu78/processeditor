/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.util.HashMap;
import java.util.Properties;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * abstract class serving as a basis to extend the ISImporter for
 * further diagram types
 * @author ff
 *
 */
public abstract class ISDrawElementExtactor {

	/**
	 * @param node
	 * @return
	 */
	public abstract ProcessNode extractNode(Element node);

	
	 /**
     * used to simplify finding elements in the "Properties" xml structure
     * @param node
     * @param pNode
     * @param xmlName
     * @param propertyName
     * @param customMappings
     */
    public static void findProperty(Element node, ProcessNode pNode, String xmlName, String propertyName, HashMap<String, String> customMappings) {
        String _prop = getProperty(node, xmlName);
        if(_prop != null) {
	        if (customMappings.containsKey(_prop)) {
	            _prop = customMappings.get(_prop);
	        }
	        pNode.setProperty(propertyName, _prop);       
        }
    }
	
	 /**
     * if the property value cannot be found null is returned
     * @param node
     * @param string
     * @return
     */
    public static String getProperty(Element node, String xmlName) {
        Element e = getPropertyNode(node, xmlName);
        if(e != null) {
            String prop = e.getTextContent();
            return prop;         
        }
        return null;
    }
    
    public static Element getPropertyNode(Element node,String xmlName) {
    	Element node2 = (Element) node.getElementsByTagName("Properties").item(0);
        if (node2 != null) {
            node2 = (Element) node2.getElementsByTagName("Property").item(0);
            return getChildByName(xmlName, node2);
        }
        return null;
    }


	public static Element getChildByName(String name, Element node2) {
		NodeList nodelist = node2.getElementsByTagName("Property");
		for (int j = 0; j < nodelist.getLength(); j++) {
		    if (nodelist.item(j) instanceof Element) {
		        Element e = (Element) nodelist.item(j);
		        if (e.getAttribute("name").equals(name)) {
		            return e;
		        }
		    }
		}
		return null;
	}

	/**
	 * @param node
	 * @param node2
	 */
	public abstract void extractStyleSheet(Element xmlNode, ProcessNode node);


	/**
	 * @param connectionNode
	 * @param _eh 
	 */
	public abstract void extractEdgeProperties(Element connectionNode, EdgeHolder _eh);


	/**
	 * @param f_props
	 * @param f_type
	 * @return
	 */
	public abstract ProcessEdge createEdge(Properties f_props, String f_type) ;


	/**
	 * called before the model is returned
	 */
	public abstract void postProcessing(ProcessModel model);


	/**
	 * @return
	 */
	public abstract ProcessModel getEmptyModel();


	/**
     * if no size information is given the default size was used in the IS model.
     * As this default size can differ from the default size used in the workbench,
     * it will be transformed here
     * @param node
     */
	public abstract void setDefaultSize(ProcessNode node);


	/**
	 * Is called when a parent child relationship was detected 
	 * in the xml of the is workflow, but the parent module is not
	 * an instance of the Cluster class. It has to be resolved manually then
	 * @param p
	 * @param _pn
	 */
	public abstract void setParentChildRelationship(ProcessNode child, ProcessNode parent);


	/**
	 * @param edge
	 * @param e
	 */
	public abstract void processDockedEdge(ProcessEdge edge, EdgeDocker e);
}
