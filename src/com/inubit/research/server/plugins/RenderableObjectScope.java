/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.plugins;

import com.inubit.research.server.extjs.JavaScriptFunction;

/**
 * Extension of the ObjectScope interface such that the client is enabled to
 * render an arbitrary icon (size: 16x16) in order to signal that a certain condition holds true
 * .
 * @author fel
 */
public interface RenderableObjectScope extends ObjectScope {

    public IconOffsetInfo getRenderingIconOffset();

    public String getRenderingIconPath();

    public JavaScriptFunction getCheckFunction();
}
