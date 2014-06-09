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
import net.frapu.code.visualization.domainModel.Aggregation;
import net.frapu.code.visualization.domainModel.Association;
import net.frapu.code.visualization.domainModel.Comment;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.DomainModel;
import net.frapu.code.visualization.domainModel.Inheritance;
import net.frapu.code.visualization.general.ColoredFrame;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author ff
 * 
 */
public class DomainModelDiagrammExtractor extends ISDrawElementExtactor {
    private static final String PROP_EDGETYPE = "EdgeType";
    private static final String PROP_MODIFIER = "modifier";
    private static final String PROP_MULTIPLICITY = "multiplicity";
    private static final String PROP_NAME = "name";
    private static final String PROP_NAVIGABLE = "navigable";
    private static final String PROP_VISIBILITY = "visibility";
    
	@Override
	public ProcessNode extractNode(Element node) {
		// Get Module type
		String type = node.getAttribute("moduleType");
		ProcessNode _result = null;

		try {
			if (type.equals("cdClass")) {
				_result = new DomainClass();
				//stereotypes
				Element _st = (Element) getPropertyNode(node, "stereotypes");
				if(_st != null) {
					NodeList _sts = _st.getElementsByTagName("Property");
					StringBuilder _stString = new StringBuilder();
					for(int i=0;i<_sts.getLength();i++) {
						Element _child = (Element) _sts.item(i);
						if(_child.getAttribute(PROP_NAME).equals(PROP_NAME)) {
							_stString.append(_child.getTextContent());
							_stString.append(',');
						}
					}
					if(_stString.length() > 0) {
						_stString.deleteCharAt(_stString.length()-1); //deleting last comma
						_result.setProperty(DomainClass.PROP_STEREOTYPE, _stString.toString());
					}
				}
				
				//attributes
				Element _atts = getPropertyNode(node, "attributes");	
				if(_atts != null) {
					NodeList _allAtts = _atts.getChildNodes();
					StringBuilder _attributesString = new StringBuilder();
					for(int i=0;i<_allAtts.getLength();i++) {
						if(_allAtts.item(i) instanceof Element) {
							_attributesString.append(buildAttributeString((Element)_allAtts.item(i)));
							_attributesString.append(DomainClass.ELEMENT_DELIMITER);
						}
					}
					_result.setProperty(DomainClass.PROP_ATTRIBUTES, _attributesString.toString());
				}
				//operations are not supported in domain models
			} else if (type.equals("Comment")) {
				_result = new Comment();				
			}else if (type.equals("Label")) {
				_result = new ColoredFrame();				
			}else if (type.equals("Text")) {
				_result = new Comment();				
			}			 
		}catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return _result;
	}   
   
	@Override
	public void postProcessing(ProcessModel model) {
		
	}

	@Override
	public void extractStyleSheet(Element xmlNode, ProcessNode node) {

	}

	/**
	 * 
		<Property name="operation" type="List">
          <Property name="name">operation</Property>
          <Property name="comment"/>
          <Property name="visibility">protected</Property>
          <Property name="concurrency">synchronized</Property>
          
          <Property name="modifiers" type="List">
            <Property name="static"/>
          </Property>
          
          <Property name="parameters" type="List">
            <Property name="parameter" type="List">
              <Property name="name">parameter</Property>
              <Property name="comment"/>
              <Property name="kindof">in</Property>
              <Property name="type">int</Property>
              <Property name="modifiers" type="List">
                <Property name="final"/>
              </Property>
            </Property>
          </Property>
          
          <Property name="return" type="List">
            <Property name="name">returnParameter</Property>
            <Property name="comment"/>
            <Property name="kindof">return</Property>
            <Property name="type">int</Property>
            <Property name="modifiers" type="List"/>
          </Property>
        </Property>
	 * @param item
	 * @return
	 */
	private String buildMethodString(Element item) {
		StringBuilder _b = new StringBuilder();
		//visibility
		addVisibility(item, _b);
		//name
		_b.append(getChildByName( PROP_NAME, item).getTextContent());
		_b.append('(');
		Element _params = getChildByName("parameters", item);
		NodeList _nl = _params.getChildNodes();
		for(int i=0;i<_nl.getLength();i++) {
			if(_nl.item(i) instanceof Element) {
				Element _par = (Element) _nl.item(i);
				_b.append(getChildByName( PROP_NAME, _par).getTextContent());
				_b.append(':');
				_b.append(getChildByName("type", _par).getTextContent());
				_b.append(',');
			}
		}
		if(_nl.getLength() > 0) {
			_b.deleteCharAt(_b.length()-1);
		}
		_b.append(')');
		_b.append(':');
		Element _ret = getChildByName("return", item);
		_b.append(getChildByName("type", _ret).getTextContent());
		
		return _b.toString();
	}

	/**
	 * <Property name="attribute" type="List">
                  <Property name="name">2</Property>
                  <Property name="comment"/>
                  <Property name="type">int</Property>
                  <Property name="multiplicity">1</Property>
                  <Property name="visibility">package</Property>
       </Property>
	 * @param item
	 * @return
	 */
	private String buildAttributeString(Element item) {
		StringBuilder _b = new StringBuilder();
		//visibility
		addVisibility(item, _b);
		//name
		_b.append(getChildByName( PROP_NAME, item).getTextContent());
		//multiplicity
		String _mult = getChildByName( PROP_MULTIPLICITY, item).getTextContent();
		if(!_mult.equals("1")) {
			_b.append(' ');
			_b.append('[');
			_b.append(_mult);
			_b.append(']');
		}
		_b.append(':');
		_b.append(getChildByName("type", item).getTextContent());
		return _b.toString();
	}

	private void addVisibility(Node item, StringBuilder _b) {
		String _vis = getChildByName(PROP_VISIBILITY, (Element)item).getTextContent();
		if(_vis.equals("public")) {
			_b.append("+");
		}else if(_vis.equals("private")) {
			_b.append("-");
		}else if(_vis.equals("protected")) {
			_b.append("#");
		}else if(_vis.equals("package")) {
			_b.append("~");
		}else {
			System.out.println("Unknown visibility: "+_vis);
		}
	}


	/**
	 * <Connection moduleOutId="2120547866" type="aggregation">
            <ConnectionId>2120547886</ConnectionId>
            <Properties version="4.1">
              <Property name="association" type="Map">
                <Property name="start" type="Map">
                  <Property name="name"/>
                  <Property name="comment"/>
                  <Property name="initialvalue"/>
                  <Property name="multiplicity">1</Property>
                  <Property name="visibility">public</Property>
                  <Property name="arrangement">unspecified</Property>
                  <Property name="modifier" type="List">
                    <Property name="navigable"/>
                  </Property>
                </Property>
                <Property name="end" type="Map">
                  <Property name="name"/>
                  <Property name="comment"/>
                  <Property name="initialvalue"/>
                  <Property name="multiplicity">1</Property>
                  <Property name="visibility">public</Property>
                  <Property name="arrangement">unspecified</Property>
                  <Property name="modifier" type="List">
                    <Property name="navigable"/>
                  </Property>
                </Property>
              </Property>
            </Properties>
          </Connection>
	 */
	@Override
	public void extractEdgeProperties(Element connectionNode,EdgeHolder eh) {
		Element _properties = (Element) connectionNode.getElementsByTagName("Properties").item(0);
		if(_properties != null) {
			if(_properties.getElementsByTagName("Property").item(0) instanceof Element) {
				Element _typeNode = (Element) _properties.getElementsByTagName("Property").item(0);
				eh.setProperty(PROP_EDGETYPE, _typeNode.getAttribute(PROP_NAME));
                                NodeList nl = _typeNode.getElementsByTagName("Property");
                                boolean startNav = false;
                                boolean endNav = false;
                                for (int i = 0; i < nl.getLength(); i++ ) {
                                    Element n = (Element) nl.item(i);
                                   
                                    if ( n.getAttribute(PROP_NAME).equals("start") ) {
                                        NodeList children = n.getChildNodes();
                                        for ( int j = 0; j < children.getLength(); j++ ) {
                                            if ( children.item(j).getNodeType() != Node.ELEMENT_NODE )
                                                continue;

                                            Element e = (Element) children.item(j);
                                            if ( e.getAttribute(PROP_NAME).equals(PROP_NAME) )
                                                eh.setProperty( Association.PROP_SOURCE_NAME, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals(PROP_VISIBILITY) )
                                                eh.setProperty( Association.PROP_SOURCE_VISIBILITY, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals(PROP_MULTIPLICITY) )
                                                eh.setProperty( Association.PROP_SOURCE_MULTIPLICITY, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals( PROP_MODIFIER) ) {
                                                for( int k = 0; k < e.getChildNodes().getLength(); k++ ) {
                                                    if ( e.getChildNodes().item(k).getNodeType() != Node.ELEMENT_NODE )
                                                        continue;
                                                    if ( ( (Element) e.getChildNodes().item(k)).getAttribute( PROP_NAME ).equals( PROP_NAVIGABLE ) )
                                                        startNav = true;
                                                }
                                            }
                                        }
                                    } else if ( n.getAttribute(PROP_NAME).equals("end") ) {
                                        NodeList children = n.getChildNodes();
                                        for ( int j = 0; j < children.getLength(); j++ ) {
                                            if ( children.item(j).getNodeType() != Node.ELEMENT_NODE )
                                                continue;
                                            
                                            Element e = (Element) children.item(j);
                                            if ( e.getAttribute(PROP_NAME).equals(PROP_NAME) )
                                                eh.setProperty( Association.PROP_TARGET_NAME, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals(PROP_VISIBILITY) )
                                                eh.setProperty( Association.PROP_TARGET_VISIBILITY, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals(PROP_MULTIPLICITY) )
                                                eh.setProperty( Association.PROP_TARGET_MULTIPLICITY, e.getTextContent() );
                                            else if ( e.getAttribute(PROP_NAME).equals( PROP_MODIFIER) ) {
                                                for( int k = 0; k < e.getChildNodes().getLength(); k++ ) {
                                                    if ( e.getChildNodes().item(k).getNodeType() != Node.ELEMENT_NODE )
                                                        continue;
                                                    if ( ( (Element) e.getChildNodes().item(k)).getAttribute( PROP_NAME ).equals( PROP_NAVIGABLE ) )
                                                        endNav = true;
                                                }
                                            }
                                        }
                                    }
                                }

                                if ( startNav ) {
                                    if ( endNav )
                                        //should be BOTH?
                                        eh.setProperty( Association.PROP_DIRECTION, Association.DIRECTION_NONE );
                                    else
                                        eh.setProperty( Association.PROP_DIRECTION, Association.DIRECTION_SOURCE );
                                } else if ( endNav ) {
                                    eh.setProperty( Association.PROP_DIRECTION, Association.DIRECTION_TARGET );
                                } else {
                                    eh.setProperty( Association.PROP_DIRECTION, Association.DIRECTION_NONE );
                                }
			}
		}
	}

	@Override
	public ProcessEdge createEdge(Properties props, String type) {
		ProcessEdge _edge;
		if(type.equals("generalization")){
			_edge = new Inheritance();
		}else {
			String _type = props.getProperty(PROP_EDGETYPE);
			if(_type == null) {
				_edge = new Aggregation();
			}else {
				 if(_type.equals("composition")) {
					_edge = new Aggregation();
					_edge.setProperty(Aggregation.PROP_COMPOSITION, Aggregation.TRUE);
				}else if(_type.equals("aggregation")) {
					_edge = new Aggregation();
				}else{
					if(!_type.equals("association")){
						System.out.println("Unknow edge-type: "+_type);
					}
					_edge = new Association();
				}
			}

                        for ( String key : props.stringPropertyNames() ) {
                            if ( !key.equals( PROP_EDGETYPE ) )
                                _edge.setProperty(key, props.getProperty(key));
                        }

		}
	   return _edge;			
	}

	@Override
	public ProcessModel getEmptyModel() {
		return new DomainModel();
	}
	
	@Override
	public void setDefaultSize(ProcessNode node) {
		if(node instanceof DomainClass) {
			node.setSize(120, 100);
		}
		  
	}

	@Override
	public void setParentChildRelationship(ProcessNode child, ProcessNode parent) {
		
	}

	@Override
	public void processDockedEdge(ProcessEdge edge, EdgeDocker e) {
		edge.setProperty(Association.PROP_DIRECTION, Association.DIRECTION_NONE);
	}

}
