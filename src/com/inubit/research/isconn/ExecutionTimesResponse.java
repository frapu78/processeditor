/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.isconn;

import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * @author ff
 *
 */
public class ExecutionTimesResponse {
	
	public static final String ROOT_TAG = "getExecutionTimesResponse";
	public static final String TIMES_HOLDER = "executionTimes";
	public static final String TIME_TAG = "time";
	public static final String TOTAL_TIME = "totalTime";
	public static final String COUNT = "count";
	public static final String AVG_TIME = "avgTime";
	
	private ArrayList<Integer> f_times = new ArrayList<Integer>();
	private int f_count = 0;
	private int f_totalTime = 0;
	private Integer f_averageTime = null;

	/**
	 * @param _response
	 */
	public ExecutionTimesResponse(String _response) {
		DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();
	try {
		  DocumentBuilder _builder = _factory.newDocumentBuilder();
		  Document _document = _builder.parse(new InputSource(new StringReader(_response)));
		  Element root = (Element) _document.getElementsByTagName(ROOT_TAG).item(0);
		  for(int i=0;i<root.getChildNodes().getLength();i++) {
			  Node n = root.getChildNodes().item(i);
			  if(n.getNodeName().equals(TIMES_HOLDER)) {
				  extractTimes((Element) n);
			  }else if(n.getNodeName().equals(TOTAL_TIME)) {
				  f_totalTime = Integer.parseInt(n.getTextContent());
			  }else if(n.getNodeName().equals(COUNT)) {
				  f_count = Integer.parseInt(n.getTextContent());
			  }else if(n.getNodeName().equals(AVG_TIME)) {
				  String _text = n.getTextContent();
				  if(_text != null && _text.length() >0)
					  f_averageTime = Integer.parseInt(n.getTextContent());
			  }
		  }
		} catch (Exception spe) {
			spe.printStackTrace();
		}
	}

	/**
	 * @param n
	 */
	private void extractTimes(Element n) {
		for(int i=0;i<n.getChildNodes().getLength();i++) {
			Node _n = n.getChildNodes().item(i);
			if(_n.getNodeName().equals(TIME_TAG)) {
				String _text = _n.getTextContent();
				f_times.add(Integer.parseInt(_text));
			}
		}
	}

	/**
	 * @return the times
	 */
	public ArrayList<Integer> getTimes() {
		return f_times;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return f_count;
	}

	/**
	 * @return the totalTime
	 */
	public int getTotalTime() {
		return f_totalTime;
	}

	/**
	 * @return the averageTime
	 */
	public Integer getAverageTime() {
		return f_averageTime;
	}
	
	
	@Override
	public String toString() {
		return "Execution Times (Total: "+f_totalTime+" Count: "+f_count+" Average:"+f_averageTime+")";
	}
}
