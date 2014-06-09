/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;


import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;


/**
 * @author ff
 *
 */
public class EdgeHolder {
	
	/**
	 * 
	 */
	public static final String PROP_EDGEDOCKERTARGET = "TARGET";
	private String f_connectionID;
	private String f_sourceID;
	private String f_targetID;
	private String f_type;
	private LinkedList<Point> f_junctures = new LinkedList<Point>();
	private String f_label = "";
	private Properties f_props = new Properties();
	
	private Point f_startPoint = null;
	private Point f_endPoint = null;
	
	private static HashSet<EdgeDocker> f_dockers = new HashSet<EdgeDocker>();
	
	/**
	 * 
	 */
	public EdgeHolder(String sourceID, String targetID,String type) {
		f_sourceID = sourceID;
		f_targetID = targetID;
		f_type = type;
	}
	
	
	public void addJuncture(int x,int y) {
		f_junctures.add(new Point(x,y));
	}
	
	public static HashSet<EdgeDocker> getEdgeDockers(){
		return f_dockers;
	}

	/**
	 * @param converter
	 * @return
	 */
	public ProcessEdge toSequenceFlow(ISDiagramImporter converter,ISDrawElementExtactor ext) {
		ProcessEdge edge = ext.createEdge(f_props,f_type);
		
		
		ProcessNode _source = converter.getFlowObject(f_sourceID);
		ProcessNode _target = converter.getFlowObject(f_targetID);
        edge.setSource(_source);
        if(_target != null) {
        	edge.setTarget(_target);
        }else {
        	//edgeDocker needed
        	EdgeDocker e = new EdgeDocker();
        	e.setProperty(PROP_EDGEDOCKERTARGET, f_targetID);
        	f_dockers.add(e);
        	edge.setTarget(e);
        	ext.processDockedEdge(edge,e);
        }
        edge.setLabel(f_label);
        for(Point p:f_junctures) {
        	edge.addRoutingPoint(edge.getRoutingPoints().size(),p);
        }
        if(f_startPoint != null) {
	        Point p = edge.getSource().getPos();
	        Point offSet = new Point();
	        offSet.x = f_startPoint.x - p.x;
	        offSet.y = f_startPoint.y - p.y;
	        edge.setSourceDockPointOffset(offSet);
        }
        if(f_endPoint != null && !(edge.getTarget() instanceof EdgeDocker)) {
        	Point p = edge.getTarget().getPos();
	        Point offSet = new Point();
	        offSet.x = f_endPoint.x - p.x;
	        offSet.y = f_endPoint.y - p.y;
	        edge.setTargetDockPointOffset(offSet);
        }
        return edge;
	}


	public void setLabel(String f_label) {
		this.f_label = f_label;
	}


	public String getLabel() {
		return f_label;
	}


	public void setProperty(String key, String value) {
		f_props.put(key, value);
	}


	public String getProperty(String key) {
		return f_props.getProperty(key);
	}


	public Properties getProperties() {
		return f_props;
	}
	
	public void setStart(Point p) {
		f_startPoint = p;
	}
	
	public void setEnd(Point p) {
		f_endPoint = p;
	}


	public void setConnectionID(String connectionID) {
		this.f_connectionID = connectionID;
	}


	public String getConnectionID() {
		return f_connectionID;
	}

}
