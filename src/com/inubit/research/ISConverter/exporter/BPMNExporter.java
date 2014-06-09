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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.CallActivity;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.Event;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Group;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TextAnnotation;
import net.frapu.code.visualization.bpmn.UserArtifact;

/**
 * @author ff
 *
 */
public class BPMNExporter extends ISDrawElementExporter {
	
	private static HashMap<String, String> eventTypeMappings = new HashMap<String, String>();
    private static HashMap<String, String> eventSubTypeMappings = new HashMap<String, String>();
    private static HashMap<String, String> gatewayTypeMappings = new HashMap<String, String>();
    
    static {
    	eventTypeMappings.put("EndEvent","StopEvent");    	
    	eventSubTypeMappings.put("","None");    	
    	
    	gatewayTypeMappings.put("ParallelGateway","Parallel");
    	gatewayTypeMappings.put("ExclusiveGateway","ExclusiveDataBased");
    	gatewayTypeMappings.put("EventBasedGateway","ExclusiveEventBased");
    	gatewayTypeMappings.put("InclusiveGateway","Inclusive");
    	gatewayTypeMappings.put("ComplexGateway","Complex");
    	gatewayTypeMappings.put("Gateway","ExclusiveDataBased");
    	
    	
    }

	@Override
	public String getWorkflowType() {
		return "bpd";
	}

	@Override
	public String writeProperties(Element element, ProcessNode node,Document doc) {
		String value = null;
		if (node instanceof Task) {
            //What about CallActivities?
			value = "Task";
			writeProp(element, "LoopType", node.getProperty(Activity.PROP_LOOP_TYPE), doc);
			//fixed values so far
			writeProp(element, "StartQuantity", "1","Integer", doc);
			writeProp(element, "CompletionQuantity", "1","Integer", doc);
			writeProp(element, "GlobalTask", "false","Boolean", doc);
			setTaskType(element,doc,node);
        } else if (node instanceof SubProcess) {
        	value = "SubProcess";
        	writeProp(element, "SubProcessType", node.getProperty(SubProcess.PROP_COLLAPSED), doc);
        	if(SubProcess.TRUE.equals(node.getProperty(SubProcess.PROP_EVENT_SUBPROCESS))){
        		writeProp(element, "TriggeredByEvent", "true", "Boolean",doc);
            }
        } else if (node instanceof CallActivity) {
            value = "CallActivity";
        } else if (node instanceof Event) {
        	value = writeEvent(node,element,doc);
        } else if (node instanceof Gateway) {
        	value = writeGateway(node,element,doc);
        } else if (node instanceof DataObject) {
        	if(node.getProperty(DataObject.PROP_DATA).equals(DataObject.DATA_INPUT)) {
        		value = "DataInput";
        	}else if(node.getProperty(DataObject.PROP_DATA).equals(DataObject.DATA_OUTPUT)) {
        		value = "DataOutput";
        	}else {
        		value = "DataObject";
        	}
        } else if (node instanceof DataStore) {
        	value = "DataStore";
        } else if (node instanceof TextAnnotation) {
        	value = "TextAnnotation";
        } else if (node instanceof Pool) {
        	value = "PoolHorizontal";
        	if("1".equals(node.getProperty(Pool.PROP_BLACKBOX_POOL))){
            	value = "PoolBlackBox";
            }
        } else if (node instanceof Lane) {
        	value = "PoolLane";
        } else if (node instanceof UserArtifact) {
        	value = "RepositoryImage";
        } else if (node instanceof EdgeDocker) {
        	//just ignore, will be handled appropriately by writeConnection()
        } else if (node instanceof Group) {
        	value = "Group";
        }
        //BPMN2.0 Elements--------------------------
        else if (node instanceof Message) {
        	value = "Message";
        } 
        //Conversation Diagram types
        else if (node instanceof ChoreographyTask) {
        	value = "ChoreographyTask";
        	writePaticipants(element,doc,node);
        } else if (node instanceof Conversation) {
        	if(Conversation.TRUE.equals(node.getProperty(Conversation.PROP_COMPOUND))) {
        		value = "SubConversation";
        	}else if(Conversation.TRUE.equals(node.getProperty(Conversation.PROP_CALL))) {
        		value = "CallConversation";
        	}else {
        		value = "Communication";
        	}	
        }else if (node instanceof ChoreographySubProcess) {
        	value = "ChoreographySubProcess";
        	//TODO add Choreography call activity support
        	//if(Call activity)
        	//value = "CallChoreographyActivity";
        	writePaticipants(element, doc, node);
        }
		return value;
	}
	
	/**
	 * @param element
	 * @param doc
	 * @param node
	 */
	private void setTaskType(Element props, Document doc, ProcessNode node) {
		String _tt = node.getStereotype();
		if(_tt != null && !_tt.isEmpty()) {
			if(_tt.equals(Activity.TYPE_SEND)) {
				writeProp(props, "TaskType", "Send", doc);
			}else if(_tt.equals(Activity.TYPE_MANUAL)) {
				writeProp(props, "TaskType", "Manual", doc);
			}else if(_tt.equals(Activity.TYPE_USER)) {
				writeProp(props, "TaskType", "User", doc);
			}else if(_tt.equals(Activity.TYPE_RULE)) {
				writeProp(props, "TaskType", "BusinessRule", doc);
			}else if(_tt.equals(Activity.TYPE_SCRIPT)) {
				writeProp(props, "TaskType", "Script", doc);
			}else if(_tt.equals(Activity.TYPE_RECEIVE)) {
				writeProp(props, "TaskType", "Receive", doc);
			}else if(_tt.equals(Activity.TYPE_SERVICE)) {
				writeProp(props, "TaskType", "Service", doc);
			}else {
				writeProp(props, "TaskType", "Custom", doc);
				writeProp(props, "Stereotype", _tt, doc);
			}			
		}		
	}
	
	
	/**
	 * write something like this:
	 * 
	 * <Property name="ModuleSpecific" type="Map">
              <Property name="InitiatingParticipant">up1</Property>
              
              <Property name="ParticipantsAtTop" type="List">
                <Property name="Participant" type="Map">
                  <Property name="ParticipantName">up1</Property>
                </Property>
              </Property>
              
              <Property name="ParticipantsAtBottom" type="List">
                <Property name="Participant" type="Map">
                  <Property name="ParticipantName">down2</Property>
                  <Property name="ParticipantMultiplicity" type="Map">
                    <Property name="Minimum" type="Integer">2</Property>
                    <Property name="Maximum" type="Integer">7</Property>
                  </Property>
                </Property>
              </Property>
	 * @param element -  the top level property element
	 * @param doc
	 * @param node
	 */
	private void writePaticipants(Element element, Document doc,ProcessNode node) {
		if(node instanceof ChoreographySubProcess) {
			writeProp(element, "InitiatingParticipant", node.getProperty(ChoreographySubProcess.PROP_ACTIVE_PARTICIPANTS), doc);
		}else if(node instanceof ChoreographyTask) {
			writeProp(element, "InitiatingParticipant", node.getProperty(ChoreographyTask.PROP_ACTIVE_PARTICIPANT), doc);
		}		
		writeParticipants(element, doc, node,"ParticipantsAtTop",true);		
		writeParticipants(element, doc, node,"ParticipantsAtBottom",false);		
	}


	private void writeParticipants(Element element, Document doc,ProcessNode node,String listName,boolean upper) {
		Element _list = doc.createElement("Property");
		_list.setAttribute("name", listName);
		_list.setAttribute("type", "List");
				 
		String _prop_part = "";
		String _prop_mult = "";
		if(node instanceof ChoreographySubProcess) {
			_prop_part = upper ? ChoreographySubProcess.PROP_UPPER_PARTICIPANTS : ChoreographySubProcess.PROP_LOWER_PARTICIPANTS;
			_prop_mult = upper ? ChoreographySubProcess.PROP_UPPER_PARTICIPANTS_MULTI : ChoreographySubProcess.PROP_LOWER_PARTICIPANTS_MULTI; 
			
		}else if(node instanceof ChoreographyTask) {
			_prop_part = upper ? ChoreographyTask.PROP_UPPER_PARTICIPANT : ChoreographyTask.PROP_LOWER_PARTICIPANT;
			_prop_mult = upper ? ChoreographyTask.PROP_UPPER_PARTICIPANT_MULTI : ChoreographyTask.PROP_LOWER_PARTICIPANT_MULTI; 	
		}
		addParticipants(_list,node.getProperty(_prop_part),ProcessNode.TRUE.equals(node.getProperty(_prop_mult)),doc);
		element.appendChild(_list);
	}

	/**
	 * returns everything inside of the type="List" Element
	 * <Property name="ParticipantsAtBottom" type="List">
                <Property name="Participant" type="Map">
                  <Property name="ParticipantName">down2</Property>
                  <Property name="ParticipantMultiplicity" type="Map">
                    <Property name="Minimum" type="Integer">2</Property>
                    <Property name="Maximum" type="Integer">7</Property>
                  </Property>
                </Property>
       </Property>
	 * @param property
	 * @param equals
	 * @return
	 */
	private void addParticipants(Element parent,String participants, boolean multiInstance,Document doc) {
		if(participants != null) {
			for(String player:participants.split(";")) {
				Element _part = doc.createElement("Property");
				_part.setAttribute("name", "Participant");
				_part.setAttribute("type", "Map");
				
				Element _prop = doc.createElement("Property");
				_prop.setAttribute("name", "ParticipantName");
				_prop.setTextContent(player);
				_part.appendChild(_prop);
				
				if(multiInstance) {
					Element _multi = doc.createElement("Property");
					_multi.setAttribute("name", "ParticipantMultiplicity");
					_multi.setAttribute("type", "Map");
					_part.appendChild(_multi);
					Element _min = doc.createElement("Property");
					_min.setAttribute("name", "Minimum");
					_min.setAttribute("type", "Integer");
					_min.setTextContent("2");
					Element _max = doc.createElement("Property");
					_max.setAttribute("name", "Maximum");
					_max.setAttribute("type", "Integer");
					_max.setTextContent("7");
					_multi.appendChild(_min);
					_multi.appendChild(_max);
				}
				parent.appendChild(_part);
			}
			
		}		
	}


	/**
	 * @param node
	 * @param element
	 * @param doc
	 * @return
	 */
	private String writeGateway(ProcessNode node, Element props, Document doc) {
		String _type = node.getClass().getSimpleName();
		if(gatewayTypeMappings.containsKey(_type)) {
			_type = gatewayTypeMappings.get(_type);
		}
		writeProp(props, "GatewayType", _type, doc);
		return "Gateway";
	}


	/**
	 * @param node
	 * @param el
	 * @param doc
	 * @return
	 */
	private String writeEvent(ProcessNode node, Element propElement, Document doc) {
		String _name = node.getClass().getSimpleName();
		String _subType;
		String _type;
		//boolean _setCancel = false;
		if(_name.endsWith("StartEvent")) {
			_subType = _name.substring(0,_name.indexOf("StartEvent"));
			_type = eventTypeMappings.containsKey("StartEvent") ? eventTypeMappings.get("StartEvent") : "StartEvent";
		}else if(_name.endsWith("EndEvent")) {
			_subType = _name.substring(0,_name.indexOf("EndEvent"));
			_type = eventTypeMappings.containsKey("EndEvent") ? eventTypeMappings.get("EndEvent") : "EndEvent";
		}else {
			_subType = _name.substring(0,_name.indexOf("IntermediateEvent"));
			_type = eventTypeMappings.containsKey("IntermediateEvent") ? eventTypeMappings.get("IntermediateEvent") : "IntermediateEvent";
		}
		if(eventSubTypeMappings.containsKey(_subType)) {
			_subType = eventSubTypeMappings.get(_subType);
		}
		String _prop = "TriggerType";
		//which property to write
		if(_name.endsWith("EndEvent") || IntermediateEvent.EVENT_SUBTYPE_THROWING.equals(node.getProperty(IntermediateEvent.PROP_EVENT_SUBTYPE))) {
			if(!_name.endsWith("EndEvent")) {
				_subType += "Throwing";
				//_setCancel = true;
			}else {
				_prop = "ResultType";
			}
		}		
		writeProp(propElement, _prop, _subType, doc);
		//if(_setCancel) { // this is needed for throwing intermediate events to work properly
			boolean _nonInter = IntermediateEvent.EVENT_NON_INTERRUPTING_TRUE.equals((node.getProperty(IntermediateEvent.PROP_NON_INTERRUPTING)));
			writeProp(propElement, "CancelActivity", _nonInter?"false":"true", "Boolean", doc);
		//}
		return _type;
	}

	@Override
	public void writeProperties(Element _props, ProcessEdge edge, Document doc) {
		if(SequenceFlow.TYPE_CONDITIONAL.equals(edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE))) {
			writeProp(_props,"ConditionType","Expression",doc);
		}else if(SequenceFlow.TYPE_DEFAULT.equals(edge.getProperty(SequenceFlow.PROP_SEQUENCETYPE))) {
			writeProp(_props,"ConditionType","Default",doc);
		}else{
			writeProp(_props,"ConditionType","None",doc);
		}
	}

	@Override
	public void setConnectionType(Element _conn, ProcessEdge edge) {
		if(edge instanceof SequenceFlow) {
			_conn.setAttribute("type", "SequenceFlow");
		}else if(edge instanceof Association) {
			_conn.setAttribute("type", "Association");
		}else {
			_conn.setAttribute("type", "MessageFlow");
		}
	}

	@Override
	public String getPropertyBlockSubElementName(ProcessObject node) {
		return "ModuleSpecific";
	}

}
