/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import java.util.LinkedList;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;

/**
 * @author ff
 *
 */
public class LayoutPreprocessor {
	
	private static LinkedList<IPreProcessor> f_processors = new LinkedList<IPreProcessor>();
	
	static {
		f_processors.add(new EdgeToEdgePreProcessor());
		f_processors.add(new EdgeDockerPreProcessor());
		
		f_processors.add(new PetriNetPreProcessor());
		f_processors.add(new BPMNPreProcessor());
		f_processors.add(new TWFPreProcessor());
		f_processors.add(new OrgChartPreProcessor());
		f_processors.add(new UMLPreProcessor());
		
		
		f_processors.add(new LoopPreProcessor());
	}

	/**
	 * @param model
	 * @return
	 */
	public static void process(AbstractModelAdapter model) {
		for(IPreProcessor _proc:f_processors) {
			if(_proc.supports(model)) {
				_proc.process(model);
			}
		}
	}
	
	/**
	 * @param model
	 * @return
	 */
	public static void unprocess(AbstractModelAdapter model) {
		for(IPreProcessor _proc:f_processors) {
			if(_proc.supports(model)) {
				_proc.unprocess(model);
			}
		}
	}

}
