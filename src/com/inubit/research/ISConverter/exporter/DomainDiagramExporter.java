/**
 *
 * Process Editor - inubit IS Converter
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.ISConverter.exporter;

import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.domainModel.Aggregation;
import net.frapu.code.visualization.domainModel.Association;
import net.frapu.code.visualization.domainModel.Comment;
import net.frapu.code.visualization.domainModel.DomainClass;
import net.frapu.code.visualization.domainModel.Inheritance;
import net.frapu.code.visualization.general.ColoredFrame;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ff
 *
 */
public class DomainDiagramExporter extends ISDrawElementExporter {

	@Override
	public String getWorkflowType() {
		return "constraintsdiagram";
	}

	@Override
	public void setConnectionType(Element _conn, ProcessEdge edge) {
		if(edge instanceof Inheritance) {
			_conn.setAttribute("type", "generalization");
		}else {
			//Explicit type is set in writeProperties for this diagramm type
			_conn.setAttribute("type", "aggregation");
		}
	}

	@Override
	public String writeProperties(Element element, ProcessNode node,
			Document doc) {
		String _result = null;
		if(node instanceof Comment) {
			_result = "Comment";
		}else if(node instanceof ColoredFrame) {
			_result = "Label";
		}else if(node instanceof DomainClass) {
			_result = "cdClass";
			//standard elements
			Element e = doc.createElement("Property");
			e.setAttribute("name", "visibility");
			e.setTextContent("public");
			element.appendChild(e);
			
			e = doc.createElement("Property");
			e.setAttribute("name", "modifiers");
			e.setAttribute("type", "List");
			element.appendChild(e);
			
			e = doc.createElement("Property");
			e.setAttribute("name", "constraints");
			element.appendChild(e);
			
			//stereotypes
			String _stereo = node.getProperty(DomainClass.PROP_STEREOTYPE);
			String[] _sts = _stereo.split(",");
			Element _stParent = doc.createElement("Property");
			_stParent.setAttribute("name", "stereotypes");
			_stParent.setAttribute("type", "List");
			for(String st:_sts) {
				if(!st.isEmpty()) {
					Element _st = doc.createElement("Property");
					_st.setAttribute("name", "stereotype");
					_st.setAttribute("type", "List");
					_stParent.appendChild(_st);
					
					writeProperty(_st, "name", st, doc);
				}
			}
			element.appendChild(_stParent);
			
			
			//attributes
			try {
				writeAttributes(node,element,doc);
			}catch(Exception ex) {
				ex.printStackTrace();
				System.out.println("wrong attribute format, cannot export!");
			}
			//methods -> not used in domain models
//			try {
//				writeMethods(node,element,doc);
//			}catch(Exception ex) {
//				ex.printStackTrace();
//				System.out.println("wrong method format, cannot export!");
//			}
			
		}		
		return _result;		
	}

	/**
	 * 
	 * <Property name="operation" type="List">
          <Property name="name">operation</Property>
          <Property name="comment"/>
          <Property name="visibility">protected</Property>
          <Property name="concurrency">synchronized</Property>
          <Property name="modifiers" type="List"/>
          
          <Property name="parameters" type="List">
            <Property name="parameter" type="List">
              <Property name="name">parameter</Property>
              <Property name="comment"/>
              <Property name="kindof">in</Property>
              <Property name="type">int</Property>
              <Property name="modifiers" type="List"/>
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
//
//        #name(parameter:int):int
//	 * @param node
//	 * @param element
//	 * @param doc
//	 */
//	private void writeMethods(ProcessNode node, Element propElement, Document doc) {
//		Element _ops = doc.createElement("Property");
//		_ops.setAttribute("name", "operations");
//		_ops.setAttribute("type", "List");
//		propElement.appendChild(_ops);
//
//		for(String s:node.getProperty(DomainClass.PROP_METHODS).split(DomainClass.ELEMENT_DELIMITER)) {
//			if(!s.isEmpty()) {
//				Element _opMain = doc.createElement("Property");
//				_opMain.setAttribute("name", "operation");
//				_opMain.setAttribute("type", "List");
//
//				String _opName = s.substring(1,s.indexOf('('));
//				writeProperty(_opMain, "name", _opName, doc);
//				writeProperty(_opMain, "comment", null, doc);
//				writeProperty(_opMain, "visibility", getVisibility(s.charAt(0)), doc);
//				writeProperty(_opMain, "concurrency", "sequential", doc);
//
//				Element _modifier = doc.createElement("Property");
//				_modifier.setAttribute("name", "modifiers");
//				_modifier.setAttribute("type", "List");
//				_opMain.appendChild(_modifier);
//
//				Element _paramsMain = doc.createElement("Property");
//				_paramsMain.setAttribute("name", "parameters");
//				_paramsMain.setAttribute("type", "List");
//				_opMain.appendChild(_paramsMain);
//
//				String _params = s.substring(s.indexOf('(')+1,s.indexOf(')'));
//				for(String par:_params.split(",")) {
//					if(!par.isEmpty()) {
//						Element _param = doc.createElement("Property");
//						_param.setAttribute("name", "parameter");
//						_param.setAttribute("type", "List");
//						_paramsMain.appendChild(_param);
//						String[] _parParts = par.split(":");
//						if(_parParts.length > 1) {
//							writeProperty(_param,"name",_parParts[0],doc);
//							writeProperty(_param, "comment", null, doc);
//							writeProperty(_param, "kindof", null, doc);
//							writeProperty(_param, "type", _parParts[1], doc);
//
//							Element _parMod = doc.createElement("Property");
//							_parMod.setAttribute("name", "modifiers");
//							_parMod.setAttribute("type", "List");
//							_param.appendChild(_parMod);
//
//							_paramsMain.appendChild(_param);
//						}
//					}
//				}
//
//				Element _returnMain = doc.createElement("Property");
//				_returnMain.setAttribute("name", "return");
//				_returnMain.setAttribute("type", "List");
//
//				writeProperty(_returnMain,"name","returnParameter",doc);
//				writeProperty(_returnMain,"comment",null,doc);
//				writeProperty(_returnMain,"kindof","return",doc);
//				writeProperty(_returnMain,"type",s.substring(s.lastIndexOf(':')+1),doc);
//
//				Element _mod = doc.createElement("Property");
//				_mod.setAttribute("name", "modifiers");
//				_mod.setAttribute("type", "List");
//				_returnMain.appendChild(_mod);
//				_opMain.appendChild(_returnMain);
//
//				_ops.appendChild(_opMain);
//			}
//		}
//
//	}

	/**
	 * @param _param
	 * @param string
	 * @param string2
	 * @param doc 
	 */
	private void writeProperty(Element addHere, String attName, String textContent, Document doc) {
		Element _p = doc.createElement("Property");
		_p.setAttribute("name", attName);
		if(textContent != null && !textContent.isEmpty()) {
			_p.setTextContent(textContent);
		}
		addHere.appendChild(_p);
	}

	/**<Property name="attributes" type="List">
	 * 		<Property name="attribute" type="List">
		              <Property name="name">2</Property>
		              <Property name="comment"/>
		              <Property name="type">int</Property>
		              <Property name="multiplicity">1</Property>
		              <Property name="visibility">package</Property>
		   </Property> 
	 	</Property> 
	 * @param node
	 * @param element
	 */
	private void writeAttributes(ProcessNode node, Element propElement,Document doc) {
		Element _atts = doc.createElement("Property");
		_atts.setAttribute("name", "attributes");
		_atts.setAttribute("type", "List");
		propElement.appendChild(_atts);
		for(String s:node.getProperty(DomainClass.PROP_ATTRIBUTES).split(DomainClass.ELEMENT_DELIMITER)) {
			if(!s.isEmpty()) {
				Element _main = doc.createElement("Property");
				_main.setAttribute("name", "attribute");
				_main.setAttribute("type", "List");
				
				int _open = s.indexOf('[');
                                int defOpen = s.indexOf('(');
				int _sep = s.indexOf(':');

                                if ( _sep < 0 )
                                    _sep = s.length() - 1;

                                if ( defOpen < 0 )
                                    defOpen = s.length();

				String _attName = (String) ((_open<0) ? s.substring(1,_sep) : s.subSequence(1, _open));
				
				writeProperty(_main, "name", _attName, doc);				
				writeProperty(_main, "comment", null, doc);				
				writeProperty(_main, "type", s.substring(_sep+1, defOpen), doc);
				
				String _multi = (String) ((_open>0) ? s.substring(_open+1,s.indexOf(']')) : "1");				
				writeProperty(_main, "multiplicity", _multi, doc);						
				writeProperty(_main, "visibility", getVisibility(s.charAt(0)), doc);

                                String defValue = null;

                                if ( defOpen > -1 && defOpen < s.length() ) {
                                    int close = s.indexOf(')', defOpen);
                                    if ( close > -1 )
                                        defValue = s.substring( defOpen, close );
                                }

				writeProperty(_main, "startvalue", defValue, doc);
				
				_atts.appendChild(_main);
			}
		}		
	}

	/**
	 * @param charAt
	 * @return
	 */
	private String getVisibility(char c) {
		if(c == '+') {
			return "public";
		}else if(c == '-') {
			return "private";
		}else if(c == '#') {
			return "protected";
		}else if(c == '~') {
			return "package";
		}else {
			return "unknown";
		}
	}

	/**
	 * <Properties version="4.1">
              <Property name="composition" type="Map">
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
	 */
	@Override
	public void writeProperties(Element _props, ProcessEdge edge, Document doc) {
		if(!(edge instanceof Inheritance)) {			
			Element _start = doc.createElement("Property");
			_start.setAttribute("name", "start");
			_start.setAttribute("type", "Map");			
			fillConnectionStandardTags(_start,doc);
			_props.appendChild(_start);
			
			Element _end = doc.createElement("Property");
			_end.setAttribute("name", "end");
			_end.setAttribute("type", "Map");			
			fillConnectionStandardTags(_end,doc);
			_props.appendChild(_end);				
		}
	}

	/**
	 *    <Property name="name"/>
	      <Property name="comment"/>
	      <Property name="initialvalue"/>
	      <Property name="multiplicity">1</Property>
	      <Property name="visibility">public</Property>
	      <Property name="arrangement">unspecified</Property>
	      <Property name="modifier" type="List">
	        <Property name="navigable"/>
	      </Property>
	 * @param node
	 * @param doc
	 */
	private void fillConnectionStandardTags(Element node, Document doc) {
		Element e = doc.createElement("Property");
		e.setAttribute("name", "name");
		node.appendChild(e);
		
		e = doc.createElement("Property");
		e.setAttribute("name", "comment");
		node.appendChild(e);

		e = doc.createElement("Property");
		e.setAttribute("name", "initialvalue");
		node.appendChild(e);

		e = doc.createElement("Property");
		e.setAttribute("name", "multiplicity");
		e.setTextContent("1");
		node.appendChild(e);

		e = doc.createElement("Property");
		e.setAttribute("name", "visibility");
		e.setTextContent("public");
		node.appendChild(e);

		e = doc.createElement("Property");
		e.setAttribute("name", "arrangement");
		e.setTextContent("unspecified");
		node.appendChild(e);
		
		e = doc.createElement("Property");
		e.setAttribute("name", "modifier");
		e.setAttribute("type", "List");
		node.appendChild(e);
		
		Element e2 = doc.createElement("Property");
		e2.setAttribute("name", "navigable");
		e.appendChild(e2);		
	}

	@Override
	public String getPropertyBlockSubElementName(ProcessObject obj) {
		if(obj instanceof ProcessEdge) {
			ProcessEdge edge = (ProcessEdge) obj;
			if(edge instanceof Association) {
				 return "association";
			}else if(edge instanceof Aggregation) {
				if(edge.getProperty(Aggregation.PROP_COMPOSITION).equals(Aggregation.TRUE)) {
					return "composition";
				}
				return "aggregation";				
			}
		}else {
			//ProcessNode node = (ProcessNode) obj;
			return "class";
		}
		return "unknown";		
	}
	
	@Override
	public boolean hasProperties(ProcessObject obj) {
		return obj instanceof DomainClass || (obj instanceof ProcessEdge && !(obj instanceof Inheritance));
	}

}
