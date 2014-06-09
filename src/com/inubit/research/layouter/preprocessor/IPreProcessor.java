/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.layouter.preprocessor;

import com.inubit.research.layouter.interfaces.AbstractModelAdapter;


/**
 * @author ff
 *
 */
public interface IPreProcessor {
	
	public boolean supports(AbstractModelAdapter model);
	public void process(AbstractModelAdapter model);
	public void unprocess(AbstractModelAdapter model);

}
