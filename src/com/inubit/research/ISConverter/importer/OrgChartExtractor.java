/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.util.Properties;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.general.ColoredFrame;
import net.frapu.code.visualization.orgChart.Connection;
import net.frapu.code.visualization.orgChart.ManagerialRole;
import net.frapu.code.visualization.orgChart.OrgChartModel;
import net.frapu.code.visualization.orgChart.OrgUnit;
import net.frapu.code.visualization.orgChart.Person;
import net.frapu.code.visualization.orgChart.Role;
import net.frapu.code.visualization.orgChart.Substitute;

import org.w3c.dom.Element;

/**
 * @author ff
 *
 */
public class OrgChartExtractor extends ISDrawElementExtactor {

	@Override
	public ProcessEdge createEdge(Properties f_props, String f_type) {
		return new Connection();
	}

	@Override
	public void extractEdgeProperties(Element connectionNode, EdgeHolder _eh) {
		//no special properties
	}

	@Override
	public ProcessNode extractNode(Element node) {
		String _type = node.getAttribute("moduleType");
		ProcessNode _result = null;
		if(_type.equals("orgRole")) {
			_result = new Role();			
		}else if(_type.equals("orgPerson")) {
			_result = new Person();			
		}else if(_type.equals("orgSubstitutePerson")) {
			_result = new Substitute();			
		}else if(_type.equals("orgUnit")) {
			_result = new OrgUnit();			
		}else if(_type.equals("orgChiefRole")) {
			_result = new ManagerialRole();			
		}else if(_type.equals("Label")) {
			_result = new ColoredFrame();			
		}
		return _result;
	}

	@Override
	public void extractStyleSheet(Element xmlNode, ProcessNode node) {
		//no special stylehseet
	}

	@Override
	public ProcessModel getEmptyModel() {
		return new OrgChartModel();
	}

	@Override
	public void postProcessing(ProcessModel model) {
	}

	@Override
	public void processDockedEdge(ProcessEdge edge, EdgeDocker e) {
	}

	@Override
	public void setDefaultSize(ProcessNode node) {
	}

	@Override
	public void setParentChildRelationship(ProcessNode child, ProcessNode parent) {
		if(parent instanceof ColoredFrame) {
			((ColoredFrame)parent).addProcessNode(child);
		}
	}

}
