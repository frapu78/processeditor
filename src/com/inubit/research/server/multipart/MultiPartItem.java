/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.multipart;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fel
 */
public class MultiPartItem {
    private Map<String, String> dispositionHeader = new HashMap<String, String>();

    private String content;
    private String contentType;

    public MultiPartItem(Map<String, String> dispoHeader, String content, String contentType) {
        this.dispositionHeader = dispoHeader;
        this.content = content;
        this.contentType = contentType;
    }

    public void setDispositionAttribute(String attribute, String value) {
        dispositionHeader.put(attribute, value);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String getDispositionAttribute(String key) {
        return dispositionHeader.get(key);
    }

    public String getContentType() {
        return this.contentType;
    }
}
