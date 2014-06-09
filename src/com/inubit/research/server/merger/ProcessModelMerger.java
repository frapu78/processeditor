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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.frapu.code.visualization.AttachedNode;
import net.frapu.code.visualization.Cluster;
import net.frapu.code.visualization.EdgeDocker;
import net.frapu.code.visualization.ProcessEdge;
import net.frapu.code.visualization.ProcessModel;
import net.frapu.code.visualization.ProcessNode;
import net.frapu.code.visualization.ProcessObject;
import net.frapu.code.visualization.bpmn.MessageFlow;

/**
 *
 * Tries to apply a ProcessModelDiff to a ProcessModel.
 *
 * @author fpu
 * @author uha
 */
public class ProcessModelMerger {

    private ProcessModel modelFrom;
    private ProcessModel originalModel;
    private ProcessModel modelTo;
    private ProcessModel mergedModel;
    private ProcessModelDiff diffToApply;
    private ProcessModelDiff diffHead;
    private Relation<String, ProcessObjectMerger> objectRelations = new Relation<String, ProcessObjectMerger>();
    private LinkedList<ProcessObjectMerger> clusterRelations = new LinkedList<ProcessObjectMerger>();
    private boolean layouted = false;

    public boolean isLayouted() {
        return layouted;
    }

    public void setLayouted(boolean layouted) {
        this.layouted = layouted;
    }

    public ProcessModel getModelFrom() {
        return modelFrom;
    }

    public ProcessModel getModelTo() {
        return modelTo;
    }

    //constants
    public ProcessObject getCorrespondingMergedObject(ProcessObject o) {
        if (modelFrom.getObjectById(o.getId()) == null) {
            return null;
        }
        ProcessObjectMerger m = getObjectMerger(o);
        return m.getAnimateTo();
    }

    public void addObjectMerger(ProcessObjectMerger merger) {
        this.objectRelations.put(merger);
    }

    public void removeObjectMerger(ProcessObject mergedObject) {
        ProcessObjectMerger m = getObjectMerger(mergedObject);  
        this.objectRelations.remove(m.getKey1(),m.getKey2());
    }

//    public void removeMergeStateVisualization(ProcessObject o) {
//        if (headModel.getObjectById(o.getId())==null) throw new IllegalArgumentException("Object not contained in original model");
//        ProcessObjectMerger m = getObjectMerger(o);
//        m.unmarkMergedObject();
//
//    }
//
//    public void removeAllMergeStateVisualizations(){
//        for (ProcessObjectMerger m: this.objectRelations.values()) {
//            m.unmarkMergedObject();
//        }
//    }
    public List<ProcessObjectMerger> getMergeRelations() {
        List<ProcessObjectMerger> result = new ArrayList<ProcessObjectMerger>();
        result.addAll(objectRelations.values());
        for (ProcessObjectMerger r : objectRelations.values()) {
            if (r.getMergedObject() == null) {
                continue;
            }
            if (r.getMergedObject() instanceof Cluster) {
                result.remove(r);
                result.add(0, r);
            }
        }
        return result;
    }

    public List<ProcessObject> getAffectedObjects() {
        LinkedList<ProcessObject> result = new LinkedList<ProcessObject>();
        for (ProcessObjectMerger r : getMergeRelations()) {
            if (r.isDestinyRemove() /*|| r.getMergedObject() instanceof Cluster*/) {
                if (r.getMergedObject() == null) {
                    continue;
                }
                result.add(r.getMergedObject());
            }
        }
        return result;
    }

    public ProcessModelMerger(ProcessModel headModel, ProcessModelDiff diff) {
        this.modelFrom = headModel;
        this.diffToApply = diff;
        try {
            this.diffHead = diff.getClass().newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(ProcessModelMerger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ProcessModelMerger.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.diffHead.compare(diffToApply.getModel1(), headModel);
        this.originalModel = diff.getModel1();
        this.modelTo = diff.getModel2();
        if (!ModelCheckUtils.IDsAreUnique(modelTo)
                || !ModelCheckUtils.IDsAreUnique(originalModel)
                || !ModelCheckUtils.IDsAreUnique(modelFrom)) {
            throw new IllegalArgumentException("Object IDs in Model are not unique");
        }
        categorizeAll();
        solveConflicts();
        merge();
    }

    public ProcessModelMerger(ProcessModel originalModel, ProcessModel mergeFrom, ProcessModel mergeToApply) {
        this(mergeFrom.clone(), new IDbasedModelDiff(originalModel.clone(), mergeToApply.clone()));
    }

    private void categorizeObject(ProcessObjectDiff relation) {
        ProcessObjectDiff partnerRelation;
        if (!(relation.getObject1Origin() == diffToApply.getModel1() || relation.getObject1Origin() == null)) {
            throw new IllegalArgumentException("Relation does not belong to original model");
        }
        if (relation.getObject2Origin() == modelFrom) {
            partnerRelation = getEquivalentNewDiff(relation);
        } else if (relation.getObject2Origin() == diffToApply.getModel2()) {
            partnerRelation = relation;
            relation = getEquivalentHeadDiff(partnerRelation);
        } else {
            throw new IllegalArgumentException("target of relation unknown");
        }

        ProcessObjectMerger mergeRelation = new ProcessObjectMerger(relation, partnerRelation);
        addMergeRelation(mergeRelation);



    }

    private void addMergeRelation(ProcessObjectMerger r) {
        objectRelations.put(r);
        // collect clusters to correct contained nodes later
        if ((r.getSourceObject() != null && r.getSourceObject() instanceof Cluster)
                || (r.getTargetObject() != null && r.getTargetObject() instanceof Cluster)) {
            clusterRelations.add(r);
        }
    }

    private void categorizeAll() {
        for (ProcessObject o : diffHead.getAddedObjects()) {
            categorizeObject(new ProcessObjectDiff(o, diffHead.getModel1(), diffHead.getModel2(), ProcessObjectState.New));
        }
        for (ProcessObject o : diffHead.getRemovedObjects()) {
            categorizeObject(new ProcessObjectDiff(o, diffHead.getModel1(), diffHead.getModel2(), ProcessObjectState.Removed));
        }
        for (ProcessObject o : diffToApply.getAddedObjects()) {
            categorizeObject(new ProcessObjectDiff(o, diffToApply.getModel1(), diffToApply.getModel2(), ProcessObjectState.New));
        }
        Relation[] pairs = {diffHead.getChangedObjectDiffs(), diffHead.getEqualObjectDiffs()};
        for (Relation h : pairs) {
            for (Object r : h.values()) {
                categorizeObject((ProcessObjectDiff) r);
            }
        }
    }

    public Relation<String, ProcessObjectMerger> getCommitedObjects() {
        Relation<String, ProcessObjectMerger> result = new Relation<String, ProcessObjectMerger>();
        for (ProcessObjectMerger r : getMergeRelations()) {
            if (r.isDestinyCommit()) {
                result.put(r);
            }
        }
        return result;
    }

    public Relation<String, ProcessObjectMerger> getConflictingObjects() {
        Relation<String, ProcessObjectMerger> result = new Relation<String, ProcessObjectMerger>();
        for (ProcessObjectMerger r : getMergeRelations()) {
            if (r.isDestinyConflict()) {
                result.put(r);
            }
        }
        return result;
    }

    public Relation<String, ProcessObjectMerger> getKeptObjects() {
        Relation<String, ProcessObjectMerger> result = new Relation<String, ProcessObjectMerger>();
        for (ProcessObjectMerger r : getMergeRelations()) {
            if (r.isDestinyKeep()) {
                result.put(r);
            }
        }
        return result;
    }

    private void correctUncontainedButConnectedNodes(ProcessModel model, Cluster cluster) {
        List<ProcessNode> notTestedNodes = new LinkedList<ProcessNode>(model.getNodes());
        List<ProcessNode> connectedNodes = new LinkedList<ProcessNode>();
        while (!notTestedNodes.isEmpty()) {
            nodeConnectedToCluster(model, cluster, notTestedNodes.get(0), notTestedNodes, connectedNodes);
        }

        for (ProcessNode n : connectedNodes) {
            cluster.addProcessNode(n);
        }

//        for (ProcessNode n : new ArrayList<ProcessNode>(cluster.getProcessNodes())) {
//            //search for attached nodes that have to be added as well
//            for (ProcessNode attached : model.getNodes()) {
//                if (attached instanceof AttachedNode) {
//                    if (((AttachedNode) attached).getParentNodeId().equals(n.getId())) {
//                        cluster.addProcessNode(attached);
//                    }
//                }
//            }
//        }
    }

    private boolean nodeConnectedToCluster(ProcessModel model, Cluster cluster, ProcessNode node, List<ProcessNode> notTestedNodes, List<ProcessNode> connectedNodes) {
        //Idee: finde die Knoten, die mit einem Knoten im Cluster verbunden sind (außer die, die durch einen Messageflow verbunden sind)
        //Lösung mittels Backtracking
        notTestedNodes.remove(node);
        if (cluster.isContained(node)) {
            connectedNodes.add(node);
            return true;
        }
        if (node == cluster) {
            connectedNodes.add(node);
            return true;
        }
        List<ProcessNode> neighbours = model.getNeighbourNodes(ProcessEdge.class, node);
        if (node.getId().equals("8490467") && (neighbours.size() == 0)) {
            System.out.println("srh");
        }
        ProcessNode attached = (ProcessNode) model.getAttachedNode(node);
        if (attached != null) {
            neighbours.add(attached);
        }
        if (node instanceof AttachedNode) {
            neighbours.add(((AttachedNode) node).getParentNode(model));
        }
        for (ProcessNode neighbour : neighbours) {
            if (notTestedNodes.contains(neighbour)) {
                if (nodeConnectedToCluster(model, cluster, neighbour, notTestedNodes, connectedNodes)) {
                    connectedNodes.add(node);
                    return true;
                }
            } else if (connectedNodes.contains(neighbour)) {
                connectedNodes.add(node);
                return true;
            }
        }
        return false;
    }

    //just for compatibility
    public ProcessModel mergeModels() {
        return getMergedModel();
    }

    private void solveConflicts() {
        for (ProcessObjectMerger d : objectRelations.values()) {
            //conflicts
            if (d.getConflictDescription() == ProcessObjectMerger.SpecificConflictDescription.RemoveConflictCommit) {
                tryResolveRemoveConflict(d.getSourceObject(), d.getSourceModel());
            }
            if (d.getConflictDescription() == ProcessObjectMerger.SpecificConflictDescription.RemoveConflictKeep) {
                tryResolveRemoveConflict(d.getTargetObject(), d.getTargetModel());
            }
        }
    }

    private void mergeObjects(ProcessModel model) {
        for (ProcessObjectMerger d : objectRelations.values()) {
            if (d.getMergedObject() == null) {
                continue;
            }
            ProcessObject o = d.getMergedObject();
            if (o instanceof ProcessEdge) {
                ProcessEdge edge = (ProcessEdge) o;
                rewire(edge);
            }
            if (o instanceof ProcessNode) {
                ProcessNode node = (ProcessNode) o;
                restoreClusterContainement(node);
            }
            if (o instanceof AttachedNode) {
                restoreAttachedRelations((AttachedNode) o);
            }
            if (o instanceof EdgeDocker) {
                restoreEdgeDocker((EdgeDocker) o);
            }
            //d.printMergedObject();
            if (o != null) {
                model.addObject(o);
            }
        }
        //checkMergedModelObjectMergerIntegrity();
    }

//    private void mergeConflictingObjects(ProcessModel model) {
//        for (ProcessObjectMerger d : getConflictingObjects().values()) {
//            ProcessObject o = d.getMergedObject();
//            if (d.getConflictDescription() == ProcessObjectMerger.SpecificConflictDescription.RemoveConflictCommit) {
//                tryResolveRemoveConflict(d.getSourceObject(), d.getSourceModel());
//            }
//            if (d.getConflictDescription() == ProcessObjectMerger.SpecificConflictDescription.RemoveConflictKeep) {
//                tryResolveRemoveConflict(d.getTargetObject(), d.getTargetModel());
//            }
//            if (o instanceof ProcessEdge) {
//                ProcessEdge edge = (ProcessEdge) o;
//                rewire(edge);
//            }
//            if (o instanceof ProcessNode) {
//                ProcessNode node = (ProcessNode) o;
//                restoreClusterContainement(node);
//            }
//            if (o instanceof AttachedNode) {
//                restoreAttachedRelations((AttachedNode) o);
//            }
//            if (o != null) {
//                model.addObject(o);
//            }
//            //d.printMergedObject();
//        }
//    }
//    private void setEqualObjects(ProcessModel model) {
//        for (ProcessObjectMerger d : getKeptObjects().values()) {
//            ProcessObject o = d.getMergedObject();
//            if (o instanceof ProcessEdge) {
//                ProcessEdge edge = (ProcessEdge) o;
//                rewire(edge);
//            }
//            if (o instanceof ProcessNode) {
//                ProcessNode node = (ProcessNode) o;
//                restoreClusterContainement(node);
//            }
//            if (o instanceof AttachedNode) {
//                restoreAttachedRelations((AttachedNode) o);
//            }
//            if (o != null) {
//                model.addObject(o);
//            }
//        }
//    }
    private void mergeClusters_old(ProcessModel model) {
        Iterator<ProcessNode> i = new LinkedList<ProcessNode>(model.getNodes()).iterator();
        ProcessNode n;
        while (i.hasNext()) {
            n = (ProcessNode) i.next();
            if (n instanceof Cluster) {
                correctUncontainedButConnectedNodes(model, (Cluster) n);
                model.moveToBack(n);
            }
        }
    }

    private void mergeClusters(ProcessModel model) {
        List<Cluster> clusters = model.getClusters();
        for (Cluster cluster : clusters) {
            List<ProcessNode> containedNodes = new LinkedList<ProcessNode>(cluster.getProcessNodes());
            for (ProcessNode containedNode : new LinkedList<ProcessNode>(containedNodes)) {
                addConnectedNodes(model,containedNodes, containedNode);
            }
            cluster.setProcessNodes(containedNodes);
        }
    }

    private ProcessModel merge() {
        ProcessModel result = getModelFrom().clone();
        for (ProcessObject o : new ArrayList<ProcessObject>(result.getObjects())) {
            result.removeObject(o);
        }
        mergeObjects(result);
        mergeClusters(result);
//        if ((this.getModelFrom() instanceof BPMNModel) && (this.getModelTo() instanceof BPMNModel))
//            ProcessUtils.sortClusters(result);
        mergedModel = result;
        return result;

    }

    private void checkMergedModelObjectMergerIntegrity() {
        for (ProcessObjectMerger m : objectRelations.values()) {
            if (m.getMergedObject() == null) {
                continue;
            }
            assert mergedModel.getObjects().contains(m.getMergedObject());
        }
    }

    public ProcessModel getMergedModel() {
        mark();
        checkMergedModelObjectMergerIntegrity();
        return mergedModel;
    }

    public ProcessModel getUnmarkedMergedModel() {
        ProcessModel m = mergedModel.clone();
        unmark(m);
        return m;
    }


    /*
     * establishes transitive relation over model from which both models to merge origninate
     * returns null if given Object is new
     */
    private ProcessObjectDiff getEquivalentHeadDiff(ProcessObjectDiff object) {
        if (object == null) {
            throw new NullPointerException("null not permitted");
        }
        ProcessObjectDiff newRelation = object;
        if (newRelation.getObject1() == null) {
            return null; // given Object is new
        }
        ProcessObjectDiff oldRelation = this.diffHead.getProcessObjectRelation(newRelation.getObject1().getId(), diffHead.getModel1());
        return oldRelation;
    }

    /*
     * establishes transitive relation over model from which both models to merge origninate
     * returns null if given Object is new
     */
    private ProcessObjectDiff getEquivalentNewDiff(ProcessObjectDiff object) {
        if (object == null) {
            throw new NullPointerException("null not permitted");
        }
        ProcessObjectDiff newRelation = object;
        if (newRelation.getObject1() == null) {
            return null; // given Object is new
        }
        ProcessObjectDiff oldRelation = this.diffToApply.getProcessObjectRelation(newRelation.getObject1().getId(), diffToApply.getModel1());
        return oldRelation;
    }

    /*
     * establishes transitive relation over model from which both models to merge origninate
     */
//     private ProcessObject getHeadEquivalent (ProcessObject object) {
//        ProcessObjectDiff d = getEquivalentHeadDiff(object);
//        return (d==null) ? null : d.getObject2();
//     }

    /*
     * Relation between Models to merge
     * returns no null, throws exception instead
     */
    public ProcessObjectMerger getObjectMerger(ProcessObject object) {
        return getObjectMerger(object.getId());
    }

    public ProcessObjectMerger getObjectMerger(String objectID) {
        if (objectID == null) {
            throw new NullPointerException();
        }
        ProcessObjectMerger result1 = null;
        ProcessObjectMerger result2 = null;
        result1 = objectRelations.getWithKey1(objectID);
        result2 = objectRelations.getWithKey2(objectID);
        if (result1 != null && result2 != null && result1 != result2) {
            throw new IllegalStateException("objects with same ids are not mapped to each other, cannot determine desired relation");
        }
        if (result1 != null /*&& object.equals(result1.get(0).getSourceObject())*/) {
            return result1;
        }
        if (result2 != null /*&& object.equals(result2.get(0).getTargetObject())*/) {
            return result2;
        }
        throw new IllegalArgumentException("ProcessObject not contained");
    }

    public void rewire(ProcessEdge processEdge) {
        //if ids are not equal...
        if (processEdge == null) {
            throw new NullPointerException();
        }
        ProcessNode n;
        ProcessObjectMerger r;

        r = getObjectMerger(processEdge.getSource());
        if (r.getMergedObject() == null) {
            return;
        }
        n = (ProcessNode) r.getMergedObject();

        processEdge.setSource(n);


        r = getObjectMerger(processEdge.getTarget());
        if (r.getMergedObject() == null) {
            return;
        }
        n = (ProcessNode) r.getMergedObject();

        processEdge.setTarget(n);
    }

    public void restoreAttachedRelations(AttachedNode attachedNode) {
        if (attachedNode == null) {
            throw new NullPointerException();
        }
        if (attachedNode.getParentNodeId().equals("")) {
            return;
        }
        ProcessObjectMerger r;
        ProcessNode newParentNode;

        r = getObjectMerger(attachedNode.getParentNodeId());
        if (r.getMergedObject() == null) {
            return;
        }
        newParentNode = (ProcessNode) r.getMergedObject();

        attachedNode.setParentNode(newParentNode);
    }

    public void restoreEdgeDocker(EdgeDocker edgeDocker) {

        ProcessObjectMerger r;
        r = getObjectMerger(edgeDocker.getProperty(EdgeDocker.PROP_DOCKED_EDGE));
        if (r.getMergedObject() == null) {
            return;
        }
        ProcessEdge newEdge = (ProcessEdge) r.getMergedObject();

        edgeDocker.setDockedEdge(newEdge);



    }

    private void restoreClusterContainement(ProcessNode n) {
        if (n == null) {
            throw new NullPointerException();
        }
        for (ProcessObjectMerger relation : clusterRelations) {
            if (relation.getMergedObject() == null) {
                continue;
            }
            if (!(relation.getMergedObject() instanceof Cluster)) {
                continue;
            }
            Cluster mergedCluster = (Cluster) relation.getMergedObject();

            //TODO: so far it is not possible to remove a node from a cluster
            try {
                if (mergedCluster.isContained((ProcessNode) getObjectMerger(n).getSourceObject())
                        || mergedCluster.isContained((ProcessNode) getObjectMerger(n).getTargetObject())) {
                    mergedCluster.addProcessNode(n);
                }
            } catch (Exception e) {
                if (mergedCluster.isContained(n)) {
                    mergedCluster.addProcessNode(n);
                }
            }
        }
    }

    public boolean hasConflict() {
        for (ProcessObjectMerger r : this.getMergeRelations()) {
            if (r.isDestinyConflict()) {
                return true;
            }
        }
        return false;
    }

    private boolean tryResolveRemoveConflict(ProcessObject notDeletedObject, ProcessModel containingModel) {
        if (!(notDeletedObject instanceof ProcessNode)) {
            getObjectMerger(notDeletedObject).setRemoveConflictResolved();
            return true;
        }
        ProcessNode notDeletedNode = (ProcessNode) notDeletedObject;
        List<ProcessEdge> incommingEdges = containingModel.getIncomingEdges(ProcessEdge.class, notDeletedNode);
        List<ProcessEdge> outgoingEdges = containingModel.getOutgoingEdges(ProcessEdge.class, notDeletedNode);
        if (incommingEdges.size() > 1 && outgoingEdges.size() > 1) {
            return false;
        }
//        if (incommingEdges.size() == 1) {
//            ProcessNode source = incommingEdges.get(0).getSource();
//            getObjectMerger(notDeletedObject).setRemoveConflictResolved();
//            for (ProcessEdge e : outgoingEdges) {
//                ProcessEdge mergedEdge = (ProcessEdge) getObjectMerger(e).getMergedObject();
//                mergedEdge.setSource(source);
//            }
//        } else if (outgoingEdges.size() == 1) {
//            ProcessNode target = outgoingEdges.get(0).getTarget();
//            getObjectMerger(notDeletedObject).setRemoveConflictResolved();
//            for (ProcessEdge e : incommingEdges) {
//                ProcessEdge mergedEdge = (ProcessEdge) getObjectMerger(e).getMergedObject();
//                mergedEdge.setTarget(target);
//            }
//        }
        LinkedList<ProcessEdge> allEdges = new LinkedList<ProcessEdge>(incommingEdges);
        allEdges.addAll(outgoingEdges);
        for (ProcessEdge e : allEdges) {
            getObjectMerger(e).setRemoveConflictResolved();
        }
        getObjectMerger(notDeletedObject).setRemoveConflictResolved();
        return true;

    }

    public int getDistance() {
        return getAffectedObjects().size();
    }

    public void unmark(ProcessModel model) {
        for (ProcessObjectMerger o : objectRelations.values()) {
            if (o.getMergedObject() == null) {
                continue;
            }
            if (o.isDestinyRemove() && mergedModel.getObjectById(o.getMergedObject().getId()) != null) {
                mergedModel.removeObject(mergedModel.getObjectById(o.getMergedObject().getId()), false);
            }
            o.getUnmarkedMergedObject();
        }
    }

    @SuppressWarnings("element-type-mismatch")
    public void mark() {
        for (ProcessObjectMerger o : objectRelations.values()) {
            if (o.getMergedObject() == null) {
                continue;
            }
            if (o.isDestinyRemove()) {
                if (mergedModel.getObjectById(o.getMergedObject().getId()) == null) {
                    if (o.getAnimateTo() == null) {
                        continue;
                    }
                    mergedModel.addObject(o.getAnimateTo());
                }
            }
            assert (mergedModel.getNodes().contains(o.getAnimateTo()) || mergedModel.getEdges().contains(o.getAnimateTo())) : "internal error: ObjectMerger not mirrored in merged Model ";
        }
    }

    public void setMergedModel(ProcessModel mergedModel) {
        this.mergedModel = mergedModel;
    }


    private Set<ProcessNode> getNeighbourNodes(ProcessModel model, Class<? extends ProcessEdge> typeIsNot, ProcessNode node) {
        Set<ProcessNode> result = new HashSet<ProcessNode>();
        for (ProcessEdge e : model.getEdges()) {
            if (!typeIsNot.isInstance(e)) {
                if (e.getSource() == node) {
                    result.add(e.getTarget());
                }
                if (e.getTarget() == node) {
                    result.add(e.getSource());
                }
            }
        }
        return result;
    }

    private void addConnectedNodes(ProcessModel model, List<ProcessNode> containedNodes, ProcessNode containedNode) {
        Set<ProcessNode> neighbours = getNeighbourNodes(model, MessageFlow.class, containedNode);
        ProcessNode attached = (ProcessNode) model.getAttachedNode(containedNode);
        if (attached != null) {
            neighbours.add(attached);
        }
        if (containedNode instanceof AttachedNode) {
            neighbours.add(((AttachedNode) containedNode).getParentNode(model));
        }
        for (ProcessNode neighbour : neighbours) {
            if (!containedNodes.contains(neighbour)) {
                containedNodes.add(neighbour);
                addConnectedNodes(model, containedNodes, neighbour);
            }
        }
    }
}
