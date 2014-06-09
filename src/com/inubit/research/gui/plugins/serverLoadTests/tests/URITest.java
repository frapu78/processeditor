/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.gui.plugins.serverLoadTests.tests;

import com.inubit.research.gui.plugins.serverLoadTests.LoadTest;
import java.net.URI;

/**
 *
 * @author uha
 */
public abstract class URITest extends LoadTest{

   public abstract void setURI(URI uri, URI second);


}
