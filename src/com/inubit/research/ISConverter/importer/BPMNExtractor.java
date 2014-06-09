/**
 *
 * Process Editor - inubit IS Converter Importer
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.bpmn.Activity;
import net.frapu.code.visualization.bpmn.Association;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.bpmn.CallActivity;
import net.frapu.code.visualization.bpmn.ChoreographySubProcess;
import net.frapu.code.visualization.bpmn.ChoreographyTask;
import net.frapu.code.visualization.bpmn.Conversation;
import net.frapu.code.visualization.bpmn.DataObject;
import net.frapu.code.visualization.bpmn.DataStore;
import net.frapu.code.visualization.bpmn.EndEvent;
import net.frapu.code.visualization.bpmn.Gateway;
import net.frapu.code.visualization.bpmn.Group;
import net.frapu.code.visualization.bpmn.IntermediateEvent;
import net.frapu.code.visualization.bpmn.Lane;
import net.frapu.code.visualization.bpmn.Message;
import net.frapu.code.visualization.bpmn.MessageFlow;
import net.frapu.code.visualization.bpmn.Pool;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.StartEvent;
import net.frapu.code.visualization.bpmn.SubProcess;
import net.frapu.code.visualization.bpmn.Task;
import net.frapu.code.visualization.bpmn.TextAnnotation;
import net.frapu.code.visualization.bpmn.UserArtifact;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author ff
 * 
 */
public class BPMNExtractor extends ISDrawElementExtactor {

	
	private static HashMap<String, String> eventTypeMappings = new HashMap<String, String>();
    private static HashMap<String, String> eventSubTypeMappings = new HashMap<String, String>();
    private static HashMap<String, String> gatewayTypeMappings = new HashMap<String, String>();
    private static HashMap<String, String> propertyMappings = new HashMap<String, String>();

   
    static {
        eventTypeMappings.put("StopEvent", "EndEvent");

        eventSubTypeMappings.put("None", "");
        eventSubTypeMappings.put("MessageThrowing", "Message");
        eventSubTypeMappings.put("LinkThrowing", "Link");
        eventSubTypeMappings.put("EscalationThrowing", "Escalation");
        eventSubTypeMappings.put("MultipleThrowing", "Multiple");
        eventSubTypeMappings.put("SignalThrowing", "Signal");
        eventSubTypeMappings.put("CompensationThrowing", "Compensation");

        gatewayTypeMappings.put("Parallel", "ParallelGateway");
        gatewayTypeMappings.put("ExclusiveDataBased", "ExclusiveGateway");
        gatewayTypeMappings.put("ExclusiveEventBased", "EventBasedGateway");
        gatewayTypeMappings.put("Inclusive", "InclusiveGateway");
        gatewayTypeMappings.put("Complex", "ComplexGateway");
        gatewayTypeMappings.put("None", "Gateway");
        propertyMappings.put("MultiInstance", "Parallel");
        propertyMappings.put("Reusable", "1");
    }
	@Override
	public ProcessNode extractNode(Element node) {
		// Get Module type
		String type = node.getAttribute("moduleType");
		ProcessNode _result = null;

		try {
			if (type.equals("Task")) {
				_result = new Task();
				setTaskType(node, (Task) _result);
			} else if (type.equals("SubProcess")) {
				_result = new SubProcess();
				if ("true".equals(getProperty(node, "TriggeredByEvent"))) {
					_result.setProperty(SubProcess.PROP_EVENT_SUBPROCESS,
							SubProcess.TRUE);
				}
			} else if (type.equals("CallActivity")) {
				_result = new CallActivity();
			} else if (type.endsWith("Event")) {
				_result = getEvent(node, type);
			} else if (type.equals("Gateway")) {
				_result = getGateway(node);
			} else if (type.startsWith("Data")) {
				_result = getData(type);
			} else if (type.equals("TextAnnotation")) {
				_result = new TextAnnotation();
			} else if (type.equals("PoolLane")) {
				_result = new Lane("", 100, null);
			} else if (type.startsWith("Pool")) {
				_result = new Pool();
				if(type.endsWith("Vertical")) {
					_result.setProperty(Pool.PROP_VERTICAL, Pool.TRUE);
				}
				if (type.endsWith("BlackBox")) {
					_result.setProperty(Pool.PROP_BLACKBOX_POOL, "1");
				}
			} else if (type.equals("RepositoryImage")) {
				_result = new UserArtifact();
			} else if (type.equals("Group")) {
				_result = new Group();
			} else if (type.equals("Message")) {
				_result = new Message();
			} // Conversation Diagram types
			else if (type.equals("ChoreographyTask")) {
				_result = new ChoreographyTask();
				loadPaticipants(node, _result);
			} else if (type.equals("SubConversation")) {
				_result = new Conversation();
				_result.setProperty(Conversation.PROP_COMPOUND, Conversation.TRUE);
			} else if (type.equals("CallConversation")) {
				_result = new Conversation();
				_result.setProperty(Conversation.PROP_CALL, Conversation.TRUE);
			} else if (type.equals("Communication")) {
				_result = new Conversation();
			} else if (type.equals("CallChoreographyActivity")) {
				// TODO add support for CALL Activities
				_result = new ChoreographySubProcess();
				loadPaticipants(node, _result);
			} else if (type.equals("ChoreographySubProcess")) {
				_result = new ChoreographySubProcess();
				loadPaticipants(node, _result);
			}
		}catch(XPathExpressionException ex) {
			ex.printStackTrace();
			return null;
		}
		return _result;
	}

	private boolean findParticipants(Element node, String xpathQuerry,
			StringBuffer _ppts) throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		Object res = xpath.evaluate(xpathQuerry, node, XPathConstants.NODESET);
		NodeList nodes = (NodeList) res;

		boolean _mi = false;
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			if (e.getAttribute("name").equals("ParticipantName")) {
				_ppts.append(e.getTextContent() + ";");
			} else {
				_mi = true;
			}
		}
		if (_ppts.length() > 0) {
			_ppts.deleteCharAt(_ppts.length() - 1);
		}
		return _mi;
	}
	
	 /**
     * 
     * 
     * @param node
     * @return
     */
    private ProcessNode getEvent(Element node, String type) {
        ProcessNode _result;
    	String resultType = getProperty(node, "ResultType");
        if (resultType != null) {
        	_result = instantiateEvent(type, resultType);
        }else {
	        String triggerType = getProperty(node, "TriggerType");
	        _result = instantiateEvent(type, triggerType);
        }
        String _nonInter = getProperty(node, "CancelActivity");
        if(_nonInter != null) {
        	if(_nonInter.equals("false")) {
        		_result.setProperty(IntermediateEvent.PROP_NON_INTERRUPTING, IntermediateEvent.EVENT_NON_INTERRUPTING_TRUE);
        	}
        }
        return _result;
    }

    /**
     * 
     * 
     * @param node
     * @return
     */
    private ProcessNode getGateway(Element node) {
        String resultType = getProperty(node, "GatewayType");
        if (resultType != null) {
            return instantiateGateway(resultType);
        }
        return new Gateway();
    }

	
	/**
     * @param ty
     * @return
     */
    private ProcessNode getData(String type) {
        if (type.equals("DataStore")) {
            return new DataStore();
        }

        DataObject result = new DataObject();

        if (type.endsWith("Output")) {
            result.setProperty(DataObject.PROP_DATA, DataObject.DATA_OUTPUT);
        } else if (type.endsWith("Input")) {
            result.setProperty(DataObject.PROP_DATA, DataObject.DATA_INPUT);
        }
        return result;
    }

    /**
     * @param pool
     * @return
     */
    private List<Lane> getLanes(Pool pool) {
        ArrayList<Lane> _result = new ArrayList<Lane>();
        for (ProcessNode p : pool.getProcessNodes()) {
            if (p instanceof Lane) {
                _result.add((Lane) p);
            }
        }
        return _result;
    }
    
    private static final String bpmnPackage = "net.frapu.code.visualization.bpmn";

    
    private ProcessNode instantiateEvent(String type, String subType) {
        if (eventTypeMappings.containsKey(type)) {
            type = eventTypeMappings.get(type);
        }
        boolean throwing = false;
        if (eventSubTypeMappings.containsKey(subType)) {
            if (subType.endsWith("Throwing")) {
                throwing = true;
            }
            subType = eventSubTypeMappings.get(subType);
        }
        String className = bpmnPackage + "." + subType + type;
        try {
            Class<?> c = Class.forName(className);
            ProcessNode n = (ProcessNode) c.newInstance();
            if (n instanceof IntermediateEvent) {
                IntermediateEvent ie = (IntermediateEvent) n;
                if (throwing) {
                    ie.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE, IntermediateEvent.EVENT_SUBTYPE_THROWING);
                } else {
                    ie.setProperty(IntermediateEvent.PROP_EVENT_SUBTYPE, IntermediateEvent.EVENT_SUBTYPE_CATCHING);
                }
            }
            return n;
        } catch (InstantiationException e) {
            System.out.println("Class " + className + " not found!");
            return createDefaultEvent(type);
        } catch (ClassNotFoundException e) {
            System.out.println("Class " + className + " not found!");
            return createDefaultEvent(type);
        } catch (IllegalAccessException e) {
            System.out.println("Class " + className + " not found!");
            return createDefaultEvent(type);
        }
    }

    private ProcessNode instantiateGateway(String type) {
        if (gatewayTypeMappings.containsKey(type)) {
            type = gatewayTypeMappings.get(type);
        }

        String className = bpmnPackage + "." + type;
        try {
            Class<?> c = Class.forName(className);
            ProcessNode n = (ProcessNode) c.newInstance();
            return n;
        } catch (InstantiationException e) {
            System.out.println("Class " + className + " not found!");
            return createDefaultEvent(type);
        } catch (ClassNotFoundException e) {
            System.out.println("Class " + className + " not found!");
            return createDefaultEvent(type);
        } catch (IllegalAccessException e) {
            System.out.println("Class " + className + " not found!");
            return new Gateway();
        }
    }

    private ProcessNode createDefaultEvent(String type) {
        if (type.equals("StartEvent")) {
            return new StartEvent();
        } else if (type.equals("IntermediateEvent")) {
            return new IntermediateEvent();
        } else {
            return new EndEvent();
        }
    }

	/**
	 * <Property name="ModuleSpecific" type="Map"> <Property
	 * name="InitiatingParticipant">up1</Property> <Property
	 * name="ParticipantsAtTop" type="List"> <Property name="Participant"
	 * type="Map"> <Property name="ParticipantName">up1</Property> </Property>
	 * </Property> <Property name="ParticipantsAtBottom" type="List"> <Property
	 * name="Participant" type="Map"> <Property
	 * name="ParticipantName">down2</Property> <Property
	 * name="ParticipantMultiplicity" type="Map"> <Property name="Minimum"
	 * type="Integer">2</Property> <Property name="Maximum"
	 * type="Integer">7</Property> </Property> </Property> </Property>
	 * 
	 * @param node
	 * @param node2
	 * @throws XPathExpressionException
	 */
	private void loadPaticipants(Element node, ProcessNode ca)
			throws XPathExpressionException {
		String init = getProperty(node, "InitiatingParticipant");
		if (init == null) {
			init = "";
		}
		if (ca instanceof ChoreographyTask) {
			ca.setProperty(ChoreographyTask.PROP_ACTIVE_PARTICIPANT, init);
		} else {
			ca.setProperty(ChoreographySubProcess.PROP_ACTIVE_PARTICIPANTS,
					init);
		}
		StringBuffer _ppts = new StringBuffer();
		boolean _mi = findParticipants(
				node,
				"Properties/Property/Property[@name='ParticipantsAtTop']/Property/Property",
				_ppts);
		if (ca instanceof ChoreographyTask) {
			ca.setProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT, _ppts
					.toString());
			ca.setProperty(ChoreographyTask.PROP_UPPER_PARTICIPANT_MULTI,
					_mi ? ChoreographyTask.TRUE : ChoreographyTask.FALSE);
		} else {
			ca.setProperty(ChoreographySubProcess.PROP_UPPER_PARTICIPANTS,
					_ppts.toString());
			ca.setProperty(
					ChoreographySubProcess.PROP_UPPER_PARTICIPANTS_MULTI,
					_mi ? ChoreographySubProcess.TRUE
							: ChoreographySubProcess.FALSE);
		}

		_ppts = new StringBuffer();
		_mi = findParticipants(
				node,
				"Properties/Property/Property[@name='ParticipantsAtBottom']/Property/Property",
				_ppts);
		if (ca instanceof ChoreographyTask) {
			ca.setProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT, _ppts
					.toString());
			ca.setProperty(ChoreographyTask.PROP_LOWER_PARTICIPANT_MULTI,
					_mi ? ChoreographyTask.TRUE : ChoreographyTask.FALSE);
		} else {
			ca.setProperty(ChoreographySubProcess.PROP_LOWER_PARTICIPANTS,
					_ppts.toString());
			ca.setProperty(
					ChoreographySubProcess.PROP_LOWER_PARTICIPANTS_MULTI,
					_mi ? ChoreographySubProcess.TRUE
							: ChoreographySubProcess.FALSE);
		}
	}

	/**
	 * @param node
	 * @param node2
	 */
	private void setTaskType(Element node, Task node2) {
		String _tt = getProperty(node, "TaskType");
		if (_tt != null) {
			if (_tt.equals("Send")) {
				node2.setStereotype(Activity.TYPE_SEND);
			} else if (_tt.equals("Manual")) {
				node2.setStereotype(Activity.TYPE_MANUAL);
			} else if (_tt.equals("User")) {
				node2.setStereotype(Activity.TYPE_USER);
			} else if (_tt.equals("BusinessRule")) {
				node2.setStereotype(Activity.TYPE_RULE);
			} else if (_tt.equals("Script")) {
				node2.setStereotype(Activity.TYPE_SCRIPT);
			} else if (_tt.equals("Receive")) {
				node2.setStereotype(Activity.TYPE_RECEIVE);
			} else if (_tt.equals("Service")) {
				node2.setStereotype(Activity.TYPE_SERVICE);
			} else if (_tt.equals("None") || _tt.equals("Custom")) {
				String _st = getProperty(node, "Stereotype");
				if (_st != null) {
					node2.setStereotype(_st);
				}
				// none is okay too
			} else {
				System.out.println("Unknow Task type found: " + _tt);
			}
		}
	}

	@Override
	public void postProcessing(ProcessModel model) {
		//fixing lane ordering
        for (ProcessNode node:model.getNodes()) {
        	if(node instanceof Pool) {
        		Pool pool = (Pool) node;
	            List<Lane> _lanes = getLanes(pool);
	            Collections.sort(_lanes, new LaneSorter());
	            //lanes were automatically added through "addProcessNode"
	            //clear the list first
	            for (Lane l : _lanes) {
	                pool.removeLane(l);
	            }
	            for (Lane l : _lanes) {
	                l.setParent(pool);
	                pool.addLane(l);
	            }
        	}
        }
	}

	@Override
	public void extractStyleSheet(Element xmlNode, ProcessNode node) {
		//only for Task - set Loop type
        if (node instanceof Task) {
            findProperty(xmlNode, node, "LoopType", Task.PROP_LOOP_TYPE, propertyMappings);
        }
        //only for Subprocess -  set collapsed
        if (node instanceof SubProcess) {
            findProperty(xmlNode, node, "SubProcessType", SubProcess.PROP_COLLAPSED, propertyMappings);
        }
        
	}

	@Override
	public void extractEdgeProperties(Element connectionNode,EdgeHolder eh) {
		eh.setProperty("Expression", "" +"Expression".equals(getProperty(connectionNode, "ConditionType")));
        eh.setProperty("Default", ""+"Default".equals(getProperty(connectionNode, "ConditionType")));
       
	}

	@Override
	public ProcessEdge createEdge(Properties props, String f_type) {
		ProcessEdge _edge;
		if(f_type.equals("SequenceFlow"))
		_edge = new SequenceFlow();
		else if(f_type.equals("Association"))
			_edge = new Association();
		else 
			_edge = new MessageFlow();
		if(props.getProperty("Expression").equals("true"))
			_edge.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_CONDITIONAL);
        if(props.getProperty("Default").equals("true"))
        	_edge.setProperty(SequenceFlow.PROP_SEQUENCETYPE, SequenceFlow.TYPE_DEFAULT);
	   return _edge;			
	}

	@Override
	public ProcessModel getEmptyModel() {
		return new BPMNModel();
	}

	
	@Override
	public void setDefaultSize(ProcessNode node) {
		if (node instanceof SubProcess) {
	       node.setSize(120, 60); //default SubProcess size in IS
	    }else if(node instanceof DataObject) {
	    	node.setSize(25, 30);//different size in IS
	    }
	}

	@Override
	public void setParentChildRelationship(ProcessNode child, ProcessNode parent) {
		if (child instanceof IntermediateEvent && parent instanceof Task) {
            IntermediateEvent _ie = (IntermediateEvent) child;
            _ie.setParentNode(parent);
        }
	}

	@Override
	public void processDockedEdge(ProcessEdge edge, EdgeDocker e) {
		edge.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
	}

}
