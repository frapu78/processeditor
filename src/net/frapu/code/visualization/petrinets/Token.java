/**
 *
 * Process Editor - Petri net Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization.petrinets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Represents a token
 * 
 * @author frank
 */
public class Token {
    
    // Each Token belongs to a process instance
    private int processInstance;
    
    // Each token has a Set of key value pairs
    private Map<String,String> metaData = new HashMap<String,String>();

    public Token(int processInstance) {
        this.processInstance = processInstance;
    }

    @Override
    public Token clone() {
        Token cloneToken = new Token(getProcessInstance());
        // Copy meta-data
        for (String key: metaData.keySet()) {
            cloneToken.setProperty(key, metaData.get(key));
        }
        // Return clone
        return cloneToken;
    }

    public int getProcessInstance() {
        return processInstance;
    }
    
    public String getProperty(String key) {
        return metaData.get(key);
    }
    
    public void setProperty(String key, String value) {
        metaData.put(key, value);
    }

    public Set<String> getPropertyKeys() {
        return metaData.keySet();
    }
 
    @Override
    public String toString() {
        return "TOKEN (PID="+processInstance+")";
    }
    
}
