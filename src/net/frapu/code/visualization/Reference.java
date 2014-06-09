/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008,2009 Frank Puhlmann
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.net.URI;

/**
 *
 * This class wraps a reference to an external model.
 * @author fpu
 */
public class Reference {

    private URI uri;
    private ProcessObject refObject;
    private ProcessModel refModel;

    public Reference(URI uri, ProcessObject refObject, ProcessModel refModel) {
        this.uri = uri;
        this.refObject = refObject;
        this.refModel = refModel;
    }

    public ProcessModel getRefModel() {
        return refModel;
    }

    public ProcessObject getRefObject() {
        return refObject;
    }

    public URI getUri() {
        return uri;
    }

}
