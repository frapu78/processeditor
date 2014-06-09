/**
 *
 * Process Editor
 *
 * (C) 2009, 2010 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.server.merger;

import com.inubit.research.server.merger.ProcessObjectDiff.ProcessObjectState;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.SequenceFlow;
import net.frapu.code.visualization.bpmn.Task;

/**
 *
 * @author uha
 */
public class ProcessObjectMerger implements Tupel {

    public enum Destiny {
        Keep, CommitNew, CommitChanged, CommitRemoved, Conflict, Equal, Solved
    }

    public enum SpecificConflictDescription {

        AttributeConflict, RemoveConflictKeep, RemoveConflictCommit, Conflict
    }
    static Destiny[][] actionMatrix =
            new Destiny[ProcessObjectState.values().length + 1][ProcessObjectState.values().length + 1];
    private ProcessObject mergedObject;
    private ProcessObject unmarkedMergedObject = null;
    private Destiny destiny;
    private ProcessObjectDiff sourceRelation;
    private ProcessObjectDiff targetRelation;
    private HashMap<String, ProcessObjectPropertyMerger> resolvedPropertyConflicts;
    Color oldColor;
    public static final Color ColorNew = new Color(128, 255, 128); //light green
    public static final Color ColorRemoved = new Color(255, 168, 168); //light pink
    public static final Color ColorChanged = Color.yellow;
    public static final Color ColorConflict = Color.red;
    public static final Color ColorKeep = (new Task()).getBackground();
    public static final Color ColorEqual = ColorKeep;

    public static void createActionMatrix() {
        //set null as default
        for (Destiny[] a1 : actionMatrix) {
            for (Destiny d : a1) {
                d = null;
            }
        }
        actionMatrix[1][1] = Destiny.Conflict;
        actionMatrix[1][2] = Destiny.Conflict;
        actionMatrix[1][3] = Destiny.Keep;
        actionMatrix[2][1] = Destiny.Conflict;
        actionMatrix[2][2] = Destiny.Keep;
        actionMatrix[2][3] = Destiny.Conflict;
        actionMatrix[3][1] = Destiny.CommitChanged;
        actionMatrix[3][2] = Destiny.Conflict;
        actionMatrix[3][3] = Destiny.Equal;
        actionMatrix[0][4] = Destiny.Keep; //null at new value
        actionMatrix[4][0] = Destiny.CommitNew; //null at new value
    }

    public ProcessObjectMerger(ProcessObjectDiff sourceRelation, ProcessObjectDiff targetRelation) {
        if (sourceRelation == null && targetRelation == null) {
            throw new IllegalArgumentException("At least one relation must not be null");
        }
        this.sourceRelation = sourceRelation;
        this.targetRelation = targetRelation;
        createActionMatrix();
        createMergedObject();
    }

    public ProcessObjectState getSourceState() {
        if (sourceRelation == null) {
            return null;
        }
        return sourceRelation.getState();
    }

    public ProcessObjectState getTargetState() {
        if (targetRelation == null) {
            return null;
        }
        return targetRelation.getState();
    }

    public ProcessObject getSourceObject() {
        if (sourceRelation == null) {
            return null;
        }
        return sourceRelation.getObject2();
    }

    public ProcessObject getTargetObject() {
        if (targetRelation == null) {
            return null;
        }
        return targetRelation.getObject2();
    }

    public ProcessModel getSourceModel() {
        if (sourceRelation == null) {
            return null;
        }
        return sourceRelation.getObject2Origin();
    }

    public ProcessModel getTargetModel() {
        if (targetRelation == null) {
            return null;
        }
        return targetRelation.getObject2Origin();
    }

    public ProcessObject getOriginalObject() {
        if (targetRelation.getObject1() != sourceRelation.getObject1()
                && targetRelation.getObject1() != null && sourceRelation.getObject1() != null) {
            throw new IllegalStateException("relations have no common object");
        }
        return (targetRelation != null) ? targetRelation.getObject1() : sourceRelation.getObject1();
    }

    private int getStateIndex(ProcessObjectState state) {
        if (state == null) {
            return ProcessObjectState.values().length;
        }
        int result = 0;
        for (ProcessObjectState s : ProcessObjectState.values()) {
            if (state == s) {
                return result;
            }
            result++;
        }
        throw new IllegalStateException();
    }

    private int[] getDestinyCode() {
        int[] result = {0,0};
        result[0] = getStateIndex(getSourceState());
        result[1] = getStateIndex(getTargetState());
        return result;
    }

    private Destiny getDestiny() {
        if (destiny == null) {
            int[] code = getDestinyCode();
            Destiny res = actionMatrix[code[0]][code[1]];
            if (res == null) {
                throw new IllegalStateException("the mapping of the contained relations is invalid");
            }
            destiny = res;
        }
        return destiny;
    }

    public HashMap<String, ProcessObjectPropertyMerger> getResolvedPropertyConflicts() {
        if (resolvedPropertyConflicts==null) resolvedPropertyConflicts = resolvedPropertyConflicts();
        return resolvedPropertyConflicts;
    }

    public boolean isDestinyCommit() {
        return getDestiny() == ProcessObjectMerger.Destiny.CommitChanged
                || getDestiny() == ProcessObjectMerger.Destiny.CommitNew
                || getDestiny() == ProcessObjectMerger.Destiny.CommitRemoved;
    }

    public boolean isDestinyKeep() {
        return getDestiny() == ProcessObjectMerger.Destiny.Keep;
    }

    public boolean isDestinyEqual() {
        return getDestiny() == ProcessObjectMerger.Destiny.Equal;
    }

    public boolean isDestinyConflict() {
        return getDestiny() == ProcessObjectMerger.Destiny.Conflict;
    }

    public boolean isDestinyRemove() {
        return getDestiny() == ProcessObjectMerger.Destiny.CommitRemoved;
    }

    public boolean isDestinyNew() {
        return getDestiny() == ProcessObjectMerger.Destiny.CommitNew;
    }

    public boolean isDestinyChanged() {
        return getDestiny() == ProcessObjectMerger.Destiny.CommitChanged;
    }
    
    public boolean isDestinySolved() {
        return getDestiny() == ProcessObjectMerger.Destiny.Solved;
    }

    void setDestiny(Destiny destiny) {
        this.destiny = destiny;
        createMergedObject();
    }

    public void setRemoveConflictResolved() {
        if (getConflictDescription() == null) {
            return;
        }
        switch (getConflictDescription()) {
            case RemoveConflictCommit:
                setDestiny(destiny.CommitRemoved);
                break;
            case RemoveConflictKeep:
                setDestiny(destiny.Keep);
                break;
        }
        createMergedObject();
    }

    public SpecificConflictDescription getConflictDescription() {
        if (getDestiny() != Destiny.Conflict) {
            return null;
        }
        if (getSourceState() == ProcessObjectState.Changed && getTargetState() == ProcessObjectState.Changed) {
            return SpecificConflictDescription.AttributeConflict;
        }
        if ((getSourceState() == ProcessObjectState.Changed && getTargetState() == ProcessObjectState.Removed) || (getSourceState() == ProcessObjectState.Removed && getTargetState() == ProcessObjectState.Changed)) {
            return SpecificConflictDescription.Conflict;
        }
        if (getSourceState() == ProcessObjectState.Equal && getTargetState() == ProcessObjectState.Removed) {
            return SpecificConflictDescription.RemoveConflictCommit;
        }
        if (getSourceState() == ProcessObjectState.Removed && getTargetState() == ProcessObjectState.Equal) {
            return SpecificConflictDescription.RemoveConflictKeep;
        }
        throw new IllegalArgumentException("Implementation Error - check getConflictDescription()");
    }

    @Override
    public Object getKey1() {
        if (sourceRelation == null) {
            return null;
        }
        return sourceRelation.getKey2();
    }

    @Override
    public Object getKey2() {
        if (targetRelation == null) {
            return null;
        }
        return targetRelation.getKey2();
    }

    private ProcessObject unmarkProcessObject(ProcessObject o) {
        Float oldAlpha = unmarkedMergedObject.getAlpha();
        if (unmarkedMergedObject instanceof ProcessEdge) {
            ProcessEdge e = (ProcessEdge) unmarkedMergedObject;
            oldColor = e.getColor();
            o.setAlpha(oldAlpha);
            ((ProcessEdge) o).setColor(oldColor);
            if (!((ProcessEdge) o).getColor().equals((new SequenceFlow()).getColor())) {
                System.err.println("Edge color mismatch possible" + (((ProcessEdge) unmarkedMergedObject).getColor()));
            }
        } else if (unmarkedMergedObject instanceof ProcessNode) {
            ProcessNode n = (ProcessNode) unmarkedMergedObject;
            oldColor = n.getBackground();
            Dimension oldSize = n.getSize();
            o.setAlpha(oldAlpha);
            ((ProcessNode) o).setBackground(oldColor);
            ((ProcessNode) o).setSize(oldSize);
            if (!((ProcessNode) o).getBackground().equals((new Task()).getBackground())) {
                System.err.println("Node color mismatch possible " + ((ProcessNode) unmarkedMergedObject).getBackground());
            }
        } else {
            throw new IllegalStateException();
        }
        return o;
    }

    public ProcessObject getUnmarkedMergedObject() {
        if (unmarkedMergedObject==null) return (ProcessObject) mergedObject;
        return unmarkProcessObject(mergedObject);
    }

    private void MarkProcessNode(ProcessNode node, Destiny destiny) {
        if (unmarkedMergedObject == null) {
            unmarkedMergedObject = node.clone();
        }
        switch (destiny) {
            case CommitNew:
                node.setBackground(ColorNew);
                break;
            case CommitChanged:
                node.setBackground(ColorChanged);
                break;
            case CommitRemoved:
                node.setBackground(ColorRemoved);
                node.setAlpha(0.3f);
                Dimension d;
        try {
            d = node.getClass().newInstance().getSize();
            node.setSize(d.width / 2, d.height / 2);
        } catch (InstantiationException ex) {
            Logger.getLogger(ProcessObjectMerger.class.getName()).log(Level.WARNING, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ProcessObjectMerger.class.getName()).log(Level.WARNING, null, ex);
        }
                break;
            case Conflict:
                node.setBackground(ColorConflict);
                break;
        }
    }

    private void MarkProcessEdge(ProcessEdge edge, Destiny destiny) {
        if (unmarkedMergedObject == null) {
            unmarkedMergedObject = (ProcessEdge) edge.clone();  
        }
        switch (destiny) {
            case CommitNew:
                edge.setColor(ColorNew);
                break;
            case CommitChanged:
                edge.setColor(ColorChanged);
                break;
            case CommitRemoved:
                edge.setColor(ColorRemoved);
                edge.setAlpha(0.5f);
                break;
            case Conflict:
                edge.setColor(ColorConflict);
                break;
        }
    }



    private void markProcessObject(ProcessObject object, Destiny destiny) {
        if (object instanceof ProcessEdge) {
            MarkProcessEdge((ProcessEdge) object, destiny);
        } else if (object instanceof ProcessNode) {
            MarkProcessNode((ProcessNode) object, destiny);
        } else {
            new UnsupportedOperationException("not implemented yet");
        }
    }



    private void setSourceAsMergedObject() {
        if (getSourceObject() != null) {
            mergedObject = (ProcessObject) getSourceObject().clone();
        } else {
            throw new IllegalStateException("merged object must not be null");
        }
        if (getSourceObject() instanceof Cluster) {
            Cluster c = (Cluster) getSourceObject();
            for (ProcessNode n : c.getProcessNodes()) {
                ((Cluster) mergedObject).addProcessNode(n);
            }
        }
        if (getSourceObject() instanceof ProcessEdge) {
            setMergedObjectEdgeLinks((ProcessEdge) getSourceObject());
        }
        if (getSourceObject() instanceof AttachedNode) {
            ((AttachedNode)mergedObject).setParentNode(((AttachedNode)getSourceObject()).getParentNode(getSourceModel()));
        }

    }

    private void setTargetAsMergedObject() {
        if (getTargetObject() != null) {
            mergedObject = (ProcessObject) getTargetObject().clone();
        } else {
            throw new IllegalStateException("merged object must not be null");
        }
        if (getTargetObject() instanceof Cluster) {
            Cluster c = (Cluster) getTargetObject();
            for (ProcessNode n : c.getProcessNodes()) {
                ((Cluster) mergedObject).addProcessNode(n);
            }
        }
        if (getTargetObject() instanceof ProcessEdge) {
            setMergedObjectEdgeLinks((ProcessEdge) getTargetObject());
        }
        if (getTargetObject() instanceof AttachedNode) {
            ((AttachedNode)mergedObject).setParentNode(((AttachedNode)getTargetObject()).getParentNode(getTargetModel()));
        }

    }

    /*
     * sets source and target node of given edgen to mergedObject (if ProcessEdge)
     */
    private void setMergedObjectEdgeLinks(ProcessEdge copyLinkFrom) {
        if (copyLinkFrom.getSource() == null || copyLinkFrom.getTarget() == null) {
            throw new IllegalArgumentException("the given diagramm does not link all edges to nodes");
        }
        if (mergedObject instanceof ProcessEdge) {
            ProcessEdge edge = (ProcessEdge) mergedObject;
            edge.setSource(copyLinkFrom.getSource());
            edge.setTarget(copyLinkFrom.getTarget());
        }
    }

    private void createMergedObject() {
        switch (getDestiny()) {
            case CommitNew:
                setTargetAsMergedObject();
                break;
            case CommitChanged:
                setTargetAsMergedObject();
                break;
            case CommitRemoved:
                setSourceAsMergedObject();
                if (mergedObject instanceof ProcessEdge) {
                    ((ProcessEdge)mergedObject).clearRoutingPoints();
                }
                break;
            case Keep:
                if (getSourceObject() == null) {
                    setMergedObject(null);
                    break;
                    // not yet complete check from sibling methods missing
                } else {
                    setSourceAsMergedObject();
                    if (getTargetObject()!=null) {
                        if (getTargetObject() instanceof ProcessNode) {
                            ((ProcessNode)getMergedObject()).setPos(((ProcessNode)getTargetObject()).getPos());
                        }
                    }
                }
                break;
            case Equal:                
                setTargetAsMergedObject();
                break;
            case Conflict:
                if (getSourceObject() == null) {
                    setTargetAsMergedObject();
                } else {
                    setSourceAsMergedObject();
                }
                if (getConflictDescription() == SpecificConflictDescription.AttributeConflict) {
                    tryToResolveConflict();
                }
                break;


            default:
                if (getSourceObject() == null) {
                    setTargetAsMergedObject();
                } else {
                    setSourceAsMergedObject();
                }
                System.err.println("Warning: RemoteObject's state unknown");
                break;
        }

    }

    private void checkEdgeValidity() {
        if (mergedObject instanceof ProcessEdge) {
            ProcessEdge edge = (ProcessEdge) mergedObject;
            if (edge.getSource() == null) {
                throw new IllegalStateException("something went wrong while rewiring the edge or the given diagramm contains edges without source");
            }
            if (edge.getTarget() == null) {
                throw new IllegalStateException("something went wrong while rewiring the edge or the given diagramm contains edges without target");
            }
        }

    }

    public ProcessObject getMergedObject() {
//        if (mergedObject==null) createMergedObject();
//        if (mergedObject==null) throw new IllegalStateException("One of the objects must be a nonzero value");
//        checkEdgeValidity();
        return mergedObject;
    }

    public void setMergedObject(ProcessObject o) {
        mergedObject = o;
    }

    public ProcessObject getAnimateTo() {
        ProcessObject o = getMergedObject();
        markProcessObject(o, getDestiny());
        return o;
    }

    public ProcessObject getAnimateFrom() {
        switch (getDestiny()) {
            case CommitNew:
                return null;
            default:
                return getSourceObject() == null ? null : (ProcessObject) getSourceObject();
        }


    }

    public ProcessObject getSourceAlternative() {
        return getSourceObject();
    }

    public ProcessObject getTargetAlternative() {
        return getTargetObject();
    }

    private HashMap<String, ProcessObjectPropertyMerger> resolvedPropertyConflicts() {
        HashMap<String, ProcessObjectPropertyMerger> result = new HashMap<String, ProcessObjectPropertyMerger>();
        HashMap<String, String[]> changedSourceProperties = sourceRelation.getChangedProperties();
        HashMap<String, String[]> changedTargetProperties = targetRelation.getChangedProperties();

        List<String> allChangedPropKeys = new LinkedList<String>();
        allChangedPropKeys.addAll(changedSourceProperties.keySet());
        allChangedPropKeys.addAll(changedTargetProperties.keySet());

        for (String propKey : allChangedPropKeys) {
            String[] sourceProps = changedSourceProperties.get(propKey);
            if (sourceProps == null) {
                sourceProps = sourceRelation.getPropertyPair(propKey);
            }
            String[] targetProps = changedTargetProperties.get(propKey);
            if (targetProps == null) {
                targetProps = targetRelation.getPropertyPair(propKey);
            }
            //what if props did not exist
            
            ProcessObjectPropertyMerger propMerger = new ProcessObjectPropertyMerger(propKey, sourceProps,
                    targetProps, mergedObject);
            result.put(propKey, propMerger);
        }
        return result;
    }

    public List<ProcessObjectPropertyMerger> getConflictingProperties() {
        LinkedList<ProcessObjectPropertyMerger> result = new LinkedList<ProcessObjectPropertyMerger>();
        for (ProcessObjectPropertyMerger m : getResolvedPropertyConflicts().values()) {
            if (m.isConflict()) {
                result.add(m);
            }
        }
        return result;
    }

    public List<ProcessObjectPropertyMerger> getChangedProperties() {
        LinkedList<ProcessObjectPropertyMerger> result = new LinkedList<ProcessObjectPropertyMerger>();
        for (ProcessObjectPropertyMerger m : getResolvedPropertyConflicts().values()) {
            if (m.isChanged() && !m.isConflict()) {
                result.add(m);
            }
        }
        return result;
    }

    private boolean tryToResolveConflict() {
        switch (getConflictDescription()) {
            case AttributeConflict:
                return resolveAttributeConflict();
            case Conflict:
                return false;
            case RemoveConflictCommit:
                return false;
            case RemoveConflictKeep:
                return false;
        }
        throw new IllegalArgumentException("no conflict");
    }

    private boolean sourceClassChanged() {
        return sourceRelation.getChangedProperties().containsKey(ProcessObject.PROP_CLASS_TYPE);
    }

    private boolean targetClassChanged() {
        return targetRelation.getChangedProperties().containsKey(ProcessObject.PROP_CLASS_TYPE);
    }

    @Override
    public String toString() {
        String result = "unknown";
        switch (destiny) {
            case CommitChanged:
                result = "Changed: " + getSourceObject() + " -> " + getTargetObject();
                break;
            case CommitNew:
                result = "New: " + getMergedObject();
                break;
            case CommitRemoved:
                result = "Removed: " + getSourceObject();
                break;
            case Equal:
                result = "Unchanged";
                break;
            case Keep:
                result = "Kept:" + getSourceObject();
                break;
            case Conflict:
                result = "Conflict detected: " + " Reason: " + getConflictDescription() + " :" + getSourceObject() + " <-> " + getTargetObject();
                break;
            case Solved:
                result = "Manually solved";
                break;
        }
        return result;
    }

    public Color getStateColor() {
        switch (destiny) {
            case CommitChanged:
                return ColorChanged;
            case CommitNew:
                return ColorNew;
            case CommitRemoved:
                return ColorRemoved;
            case Conflict:
                return ColorConflict;
            case Equal:
                return ColorKeep;
            default:
                return ColorKeep;
        }
    }





    private boolean resolveAttributeConflict() {
        setTargetAsMergedObject();
        this.resolvedPropertyConflicts = resolvedPropertyConflicts();
        if (!getConflictingProperties().isEmpty()) {
            setSourceAsMergedObject();
            this.resolvedPropertyConflicts = resolvedPropertyConflicts();
            if (!getConflictingProperties().isEmpty()) {
                setTargetAsMergedObject();
                this.resolvedPropertyConflicts = resolvedPropertyConflicts();
            }
        }
        // set resolved props
        for (ProcessObjectPropertyMerger m : getResolvedPropertyConflicts().values()) {
            if (!m.isConflict() && m.isChanged()) {
                mergedObject.setProperty(m.getPropertyName(), m.getMergedValue());
            }
        }
        if (getChangedProperties().isEmpty() && getConflictingProperties().isEmpty()) setDestiny(destiny.Keep);
        if (!getChangedProperties().isEmpty() && getConflictingProperties().isEmpty()) setDestiny(destiny.CommitChanged);
        return getConflictingProperties().isEmpty();
    }

    public void setSolved() {
        setDestiny(ProcessObjectMerger.Destiny.Solved);
    }
}
