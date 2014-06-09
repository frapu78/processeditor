/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;

/**
 * This class is used for informing about any problem, that was found when validating
 * a model.
 * @author tmi
 */
public class ValidationMessage {

    private int type;
    private String description, shortDescription;
    private Collection<ProcessObject> involvedObjects;
    /**
     * The primary object related to this message. It does not need to be
     * specified. It does not need to be included in the list involvedObjects.
     */
    private ProcessObject primaryObject;
    public static final int TYPE_INFO = 1, TYPE_WARNING = 2, TYPE_ERROR = 4;

    public ValidationMessage(int type, String description,
            String shortDescription, Collection<ProcessObject> involvedObjects,
            ProcessObject primaryObject) {
        this.type = type;
        this.description = description;
        this.shortDescription = shortDescription;
        this.involvedObjects = new HashSet<ProcessObject>(involvedObjects);
        this.primaryObject = primaryObject;
    }

    public ValidationMessage(int type, String description,
            String shortDescription, Collection<ProcessObject> involvedObjects) {
        this(type, description, shortDescription, involvedObjects, null);
    }

    /**
     * getter for the type of this problem. Possible values are TYPE_INFO,
     * TYPE_WARNING and TYPE_ERROR
     * @return the type of validation message (information, warning or error)
     */
    public int getType() {
        return type;
    }

    public String getTypeString() {
        switch (type) {
            case TYPE_INFO:
                return "Information";
            case TYPE_WARNING:
                return "Warning";
            default:
                return "Error";
        }
    }

    /**
     * returns, whether this is an error message
     * @return true, iff getType() == TYPE_ERROR
     */
    public boolean isError() {
        return type == TYPE_ERROR;
    }

    /**
     * returns, whether this is a warning message
     * @return true, iff getType() == TYPE_WARNING
     */
    public boolean isWarning() {
        return type == TYPE_WARNING;
    }

    /**
     * returns, whether this is an information message
     * @return true, iff getType() == TYPE_INFO
     */
    public boolean isInformation() {
        return type == TYPE_INFO;
    }

    /**
     * sets the type of message, which this object represents (information,
     * warning or error)
     * @param type the type to set (user TYPE_INFO, TYPE_WARNING or TYPE_ERROR)
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * getter for the long version of this problem´s description
     * @return the long description
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the long version of this problem´s description
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * getter for the short version of this problem´s description (suitable e.g.
     * as a title in a list of problems)
     * @return the short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * sets the short version of this problem´s description (should be suitable
     * as a title in a problem list or similiar things)
     * @param shortDescription the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * getter for the collection of the objects, that are related to this problem
     * @return the involvedObjects
     */
    public Collection<ProcessObject> getInvolvedObjects() {
        return new HashSet<ProcessObject>(involvedObjects);
    }

    /**
     * sets the involved objects to the elements of the provided collection
     */
    public void setInvolvedObjects(Collection<ProcessObject> involvedObjects) {
        this.involvedObjects = new HashSet<ProcessObject>(involvedObjects);
    }

    public String getInvolvedObjectsString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<ul>");
        if (primaryObject != null) {
            buffer.append("<li>");
            buffer.append(involvedObjectAsString(primaryObject));
            buffer.append("</li>");
        }
        for (ProcessObject object : involvedObjects) {
            buffer.append("<li>");
            buffer.append(involvedObjectAsString(object));
            buffer.append("</li>");
        }
        buffer.append("</ul>");
        return buffer.toString();
    }

    private String involvedObjectAsString(ProcessObject object) {
        StringBuffer buffer = new StringBuffer();
        if (object instanceof ProcessNode) {
            buffer.append(displayName((ProcessNode) object));
        } else if (object instanceof ProcessEdge) {
            buffer.append("Edge from ");
            buffer.append(displayName(((ProcessEdge) object).getSource()));
            buffer.append(" to \\\"");
            buffer.append(displayName(((ProcessEdge) object).getTarget())).append("\\\"");
        }
        return buffer.toString();
    }

    private String displayName(ProcessNode node) {
        if (node.getText() == null || node.getText().trim().isEmpty()) {
            return "(unnamed)";
        }
        return node.getText();
    }

    private List<ProcessNode> allRelatedNodes() {
        List<ProcessNode> nodes = new LinkedList<ProcessNode>();
        if (primaryObject instanceof ProcessNode) {
            nodes.add((ProcessNode)primaryObject);
        }
        for (ProcessObject object : involvedObjects) {
            if (object instanceof ProcessNode) {
                nodes.add((ProcessNode)object);
            }
        }
        return nodes;
    }

    public List<String> getRelatedNodeIDs() {
        List<String> related = new LinkedList<String>();
        for (ProcessNode node : allRelatedNodes()) {
            related.add(node.getId());
        }
        return related;
    }

    private List<ProcessEdge> allRelatedEdges() {
        List<ProcessEdge> nodes = new LinkedList<ProcessEdge>();
        if (primaryObject instanceof ProcessEdge) {
            nodes.add((ProcessEdge)primaryObject);
        }
        for (ProcessObject object : involvedObjects) {
            if (object instanceof ProcessEdge) {
                nodes.add((ProcessEdge)object);
            }
        }
        return nodes;
    }

    public List<String> getRelatedEdgeIDs() {
        List<String> related = new LinkedList<String>();
        for (ProcessEdge edge : allRelatedEdges()) {
            related.add(edge.getId());
        }
        return related;
    }

    /*public String getInvolvedNodesIDString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        Iterator<ProcessNode> iter = allRelatedNodes().iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next().getId());
            if (iter.hasNext()) {
                buffer.append(",");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String getInvolvedEdgesIDString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        Iterator<ProcessEdge> iter = allRelatedEdges().iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next().getId());
            if (iter.hasNext()) {
                buffer.append(",");
            }
        }
        buffer.append("]");
        return buffer.toString();
    }*/

    public ProcessObject getPrimaryObject() {
        return primaryObject;
    }

    public boolean hasPrimaryObject() {
        return primaryObject != null;
    }
}
