/**
 *
 * Process Editor - inubit IS Converter
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.exporter;

import java.util.HashMap;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ff
 *
 */
public abstract class ISDrawElementExporter {
	

    protected static HashMap<String, String> propertyMappings = new HashMap<String, String>();
	
    static {   	
    	propertyMappings.put("PARALLEL","MultiInstance");
    	propertyMappings.put("NONE","None");
    	propertyMappings.put("STANDARD","Standard");
    	propertyMappings.put("SEQUENCE","Sequence");
    }
    
    
	public abstract String getWorkflowType();

	/**
	 * @param node 
	 * @param element 
	 * @param doc 
	 * @return
	 */
	public abstract String writeProperties(Element element, ProcessNode node, Document doc);
	
	protected void writeProp(Element props, String name, String text,Document doc) {
		writeProp(props, name, text, null, doc);
	}
	
	protected void writeProp(Element props, String name, String text,String type, Document doc) {
		Element _prop = doc.createElement("Property");
		_prop.setAttribute("name", name);
		if(type != null) {
			_prop.setAttribute("type", type);
		}		
		_prop.setTextContent(propertyMappings.containsKey(text) ? propertyMappings.get(text) : text);
		props.appendChild(_prop);
	}

	/**
	 * @param _props
	 * @param edge
	 * @param doc
	 */
	public abstract void writeProperties(Element _props, ProcessEdge edge, Document doc);

	/**
	 * @param _conn
	 * @param edge
	 */
	public abstract void setConnectionType(Element _conn, ProcessEdge edge);

	/**
	 * @param node 
	 * @return
	 */
	public abstract String getPropertyBlockSubElementName(ProcessObject node);

	/**
	 * @param obj
	 * @return
	 */
	public boolean hasProperties(ProcessObject obj) {
		return true;
	}

}
