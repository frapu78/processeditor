/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.extjs;

import org.json.JSONString;

/**
 * Class for storing JavaScript functions in ExtJS element configurations.
 * @author fel
 */
public class JavaScriptFunction implements JSONString {

    private String sourceCode;

    public JavaScriptFunction( String code ) {
            this.sourceCode = code;
    }

    public String toJSONString() {
        return sourceCode;
    }
}
