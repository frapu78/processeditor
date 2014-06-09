/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.layouterTests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.frapu.code.converter.ConverterHelper;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessUtils;
import net.frapu.code.visualization.bpmn.BPMNModel;
import net.frapu.code.visualization.twf.TWFModel;

import com.inubit.research.layouter.ProcessLayouter;
import com.inubit.research.layouter.gridLayouter.GridLayouter;
import com.inubit.research.layouter.sugiyama.SugiyamaLayoutAlgorithm;

/**
 * @author ff
 *
 */
public class LayouterTests extends TestCase {

	/**
	 * @param name
	 */
	public LayouterTests(String name) {
		super(name);
	}
	
	ProcessLayouter f_layouter;


	public void testSugiyamaLayouter() {
		
		f_layouter = new SugiyamaLayoutAlgorithm(true,30,35,true,true,true);
		File f = new File("models/");
		importAndLayout(f,null);
	}
	
	public void testGridLayouter() {
		
		f_layouter = new GridLayouter(true,800,true,80,50);
		File f = new File("models/");
		ArrayList<Class<? extends ProcessModel>> _types = new ArrayList<Class<? extends ProcessModel>>();
		_types.add(BPMNModel.class);
		_types.add(TWFModel.class);
		importAndLayout(f,_types);
	}

	/**
	 * works recursively
	 * @param f
	 */
	private void importAndLayout(File f,List<Class<? extends ProcessModel>> modelTypes) {
		if(f.isDirectory()) {
			for(File child:f.listFiles()) {
				importAndLayout(child,modelTypes);
			}
		}else{
			try {
				List<ProcessModel> _models = ConverterHelper.importModels(f);
				for(ProcessModel pm:_models) {
					if(pm != null) {
						try {
							boolean _doLayout = false;
							if(modelTypes == null) {
								_doLayout = true;
							}else {
								for(Class<? extends ProcessModel> cl : modelTypes) {
									if(pm.getClass().isAssignableFrom(cl)) {
										_doLayout = true;
									}
								}
							}
							if(_doLayout) {
								System.out.println("Layouting "+f.getName());
								f_layouter.layoutModel(ProcessUtils.getAdapter(pm));
								
							}
						} catch (Exception e) {
							e.printStackTrace();
							fail("Error while Layouting "+f.getAbsolutePath());
						}	
					}
				}
			} catch (Exception e) {
				System.out.println("Could not import "+f.getName());
			}
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
