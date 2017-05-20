/**
 *
 * Process Editor - Core Package
 *
 * (C) 2008, 2009 Frank Puhlmann
 * (C) 2010 inubit AG
 * (C) 2017 the authors
 *
 * http://frapu.net
 *
 */
package net.frapu.code.visualization;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * This class provides a data model for a process used within ProcessEditor.
 * 
 * @author frank
 */
public abstract class ProcessModel implements ProcessObjectListener, Cloneable {

    /**
     * Serialization properties
     */
    public final static String ATTR_XMLNS = "xmlns";
    public final static String VALUE_XMLNS = "http://frapu.net/xsd/ProcessEditor";
    public final static String ATTR_XMLNS_XSI = "xmlns:xsi";
    public final static String VALUE_XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String ATTR_XSI_SCHEMALOCATION = "xsi:schemaLocation";
    public final static String VALUE_XSI_SCHEMALOCATION = "http://frapu.net/xsd/ProcessEditor http://frapu.net/xsd/ProcessEditor.xsd";
    public final static String TAG_MODEL = "model";
    public final static String TAG_NODES = "nodes";
    public final static String TAG_EDGES = "edges";
    public final static String TAG_PROPERTIES = "properties";
    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";
    public final static String ATTR_ID = "id";
    /** Optional attribute that references to the model that has a reference to the actual model (and was used to open it) */
    public final static String ATTR_PARENT_REF = "parent_ref";
    /** A dirty flag */
    private boolean dirtyFlag = false;
    /** Holds the properties of the process model */
    private HashMap<String, String> properties = new HashMap<String, String>();
    /** Holds the transient properties of the process model */
    private transient HashMap<String, Object> transientProperties = new HashMap<String, Object>();
    /** List of ProcessNodes; elements should be rendered from start to end */
    private LinkedList<ProcessNode> processNodes = new LinkedList<ProcessNode>();
    /** List of ProcessEdge */
    private LinkedList<ProcessEdge> processEdges = new LinkedList<ProcessEdge>();
    /** List of Clusters */
    private LinkedList<Cluster> clusterNodes = new LinkedList<Cluster>();
    /** Set of ProcessModelListeners */
    private Set<ProcessModelListener> listeners = new HashSet<ProcessModelListener>();
    /** An instance of a sub-class of ProcessUtils */
    protected ProcessUtils processUtils = null;
    /** A cache for the predecessors of a node */
    protected Map<ProcessNode, List<ProcessNode>> predecessorCache =
            new HashMap<ProcessNode, List<ProcessNode>>();
    /** A cache for the top level nodes */
    protected LinkedList<ProcessNode> topLevelNodesCache = new LinkedList<ProcessNode>();
    /** A cache for the currently visible nodes */
    protected List<ProcessNode> visibleNodesCache = new LinkedList<ProcessNode>();
    /** This color will be used to fill the whole background of the model prior to painting (not serialized) */
    /** A cache for the incoming edges of a node */
    protected Map<ProcessNode, List<ProcessEdge>> precEdgeCache = new HashMap<ProcessNode, List<ProcessEdge>>();
    private Color bgColor = Color.WHITE;
    //ff added to find the corresponding nodes from outside the class after cloning
//    protected Map<String, String> idMap;
//    protected Map<ProcessEdge, ProcessEdge> edgeMap;
    // The id of this process model
    protected String id;
    /** The name of the ProcessModel */
    public final static String PROP_PROCESS_NAME = "name";
    /** The URI for this model (if applicable) */
    public final static String PROP_PROCESS_URI = "#uri";
    /** A field for the owner of this model (if applicable */
    public final static String PROP_EDITOR = "#editor";
    /** The creation date of the ProcessModel */
    public final static String PROP_CREATE_DATE = "#creationDate";
    /** The author of this ProcessModel */
    public final static String PROP_AUTHOR = "author";
    /** A comment for this ProcessModel */
    public final static String PROP_COMMENT = "comment";
    /** The source version of this ProcessModel (optional) */
    public final static String PROP_SOURCE_VERSION = "#source_version";
    /** An optional property holding the source folder alias */
    public final static String PROP_FOLDERALIAS = "#folder";
    /** The last time this model was changed  */
    public static final String PROP_LASTCHECKIN = "#LAST_CHECKIN_TIME";

    /**
     * Creates a new ProcessModel.
     */
    public ProcessModel() {
        super();
        init();
    }

    /** 
     * Creates a new ProcessModel with a name.
     * @param name
     */
    public ProcessModel(String name) {
        init();
        setProperty(PROP_PROCESS_NAME, name);
    }

    protected void init() {
        Date now = new Date();
        String creationDate = DateFormat.getDateTimeInstance(
                DateFormat.LONG, DateFormat.LONG).format(now);
        setProperty(PROP_PROCESS_NAME, "");
        setProperty(PROP_PROCESS_URI, "");
        setProperty(PROP_CREATE_DATE, creationDate);
        setProperty(PROP_AUTHOR, "");
        setProperty(PROP_COMMENT, "");

        // Set id
        setId("" + this.hashCode());
    }

    public void markAsDirty(boolean dirty) {
        dirtyFlag = dirty;
        // Update caches
        clearCaches();
    }

    public boolean isDirty() {
        return dirtyFlag;
    }

    /**
     * Returns a single string describing the kind of model this ProcessModel
     * supports.
     * @return
     */
    public abstract String getDescription();

    /**
     * Clears the caches after a model change.
     */
    protected void clearCaches() {
        // Clear predecessor cache
        predecessorCache.clear();
        topLevelNodesCache.clear();
        visibleNodesCache.clear();
        precEdgeCache.clear();
    }

    /**
     * Like clone, but does not copy transient properties.
     * Uses Serializer-Deserializer pattern.
     */
    public synchronized ProcessModel clonePersistent() throws Exception {
        return ProcessUtils.parseProcessModelSerialization(this.getSerialization());
    }

    /**
     * Creates a deep-copy of this ProcessModel, including all edges and
     * nodes, but NOT listeners AND only reference to transient properties.
     * @return
     */
    @Override
    public synchronized ProcessModel clone() {

        try {
            ProcessModel newModel = null;
            newModel = (ProcessModel) super.clone();
            newModel.properties = new HashMap<String, String>(properties);
            newModel.transientProperties = new HashMap<String, Object>(transientProperties);
            newModel.clusterNodes = new LinkedList<Cluster>();


            //nodes
            newModel.processNodes = new LinkedList<ProcessNode>();
            for (ProcessNode node : processNodes) {
                newModel.addNode(node.clone());
            }

            //attached Nodes
            for (ProcessNode node : newModel.processNodes) {
                if (node instanceof AttachedNode) {
                    AttachedNode attached = (AttachedNode) node;
                    attached.setParentNode(attached.getParentNode(newModel));
                }
            }
            
            //adjust cluster containement
            for (Cluster cluster : this.clusterNodes) {
                LinkedList<ProcessNode> containedNodesInNewModel = new LinkedList<ProcessNode>();
                Cluster newModelCluster = (Cluster) newModel.getNodeById(cluster.getId());
                for (ProcessNode oldContainedNode : cluster.getProcessNodes()) {
                    containedNodesInNewModel.add(newModel.getNodeById(oldContainedNode.getId()));
                }
                newModelCluster.setProcessNodes(containedNodesInNewModel);
            }

            //edges
            newModel.processEdges = new LinkedList<ProcessEdge>();
            for (ProcessEdge edge : processEdges) {
                newModel.addEdge(edge.clone());
                ProcessEdge newEdge = ((ProcessEdge) newModel.getObjectById(edge.getId()));
//                newEdge.addListener(newModel);
                newEdge.setSource((ProcessNode) newModel.getObjectById(edge.getSource().getId()));
                newEdge.setTarget((ProcessNode) newModel.getObjectById(edge.getTarget().getId()));
            }
            
            /*
             * TODO Be aware that the method comment explicitly states that listeners are NOT copied.
             * This is, partly, due to the fact the listener instances hold a reference to the process
             * model they are registered for, which is not equal to the new model.
             * 
             * Therefore, I commented this line out! (fel)
             */
            
            //newModel.listeners = new HashSet<ProcessModelListener>(listeners);
            
            newModel.listeners = new HashSet<ProcessModelListener>();

            /*
            // Stores the relations between (org_id, new_id)
            Map<String, String> localIdMap = new HashMap<String, String>();
            Map<ProcessEdge, ProcessEdge> localEdgeMap = new HashMap<ProcessEdge, ProcessEdge>();
            // Stores the relations between (new_id, ProcessNode)
            Map<String, ProcessNode> nodeMap = new HashMap<String, ProcessNode>();
            
            // Nodes, Edges, Name, Utils
            newModel.setProcessName(getProcessName());
            newModel.setUtils(getUtils());
            newModel.setProcessModelURI(getProcessModelURI());
            // Clone all nodes
            for (ProcessNode node : new ArrayList<ProcessNode>(getNodes())) {
            ProcessNode newNode = node.clone();
            newNode.handleCloning(localIdMap);
            localIdMap.put(node.getProperty(ProcessNode.PROP_ID),
            newNode.getProperty(ProcessNode.PROP_ID));
            nodeMap.put(newNode.getProperty(ProcessNode.PROP_ID), newNode);
            newModel.addNode(newNode);
            }
            // Check if node is Cluster, if so copy containments
            for (ProcessNode node : getNodes()) {
            if (node instanceof Cluster) {
            // Copy containments
            Cluster c = (Cluster) node;
            Cluster newC = (Cluster) nodeMap.get(localIdMap.get(c.getProperty(ProcessNode.PROP_ID)));
            for (ProcessNode subNode : c.getProcessNodes()) {
            newC.addProcessNode(nodeMap.get(localIdMap.get(subNode.getProperty(ProcessNode.PROP_ID))));
            }
            //correcting Lane Positions
            if (c instanceof LaneableCluster) {
            LaneableCluster lc = (LaneableCluster) c;
            LaneableCluster nlc = (LaneableCluster) newC;
            for (Lane l : new LinkedList<Lane>(lc.getLanes())) {
            nlc.removeLane((Lane) nodeMap.get(localIdMap.get(l.getProperty(ProcessNode.PROP_ID))));
            }
            for (Lane l : new LinkedList<Lane>(lc.getLanes())) {
            nlc.addLane((Lane) nodeMap.get(localIdMap.get(l.getProperty(ProcessNode.PROP_ID))));
            }
            }
            }
            }
            
            // Clone all edges
            for (ProcessEdge edge : getEdges()) {
            ProcessEdge newEdge = edge.getClass().newInstance();
            
            //copy properties
            newEdge = edge.clone();
            
            // Get new source id
            String newSourceId = localIdMap.get(edge.getSource().getProperty(ProcessNode.PROP_ID));
            String newTargetId = localIdMap.get(edge.getTarget().getProperty(ProcessNode.PROP_ID));
            ProcessNode src = nodeMap.get(newSourceId);
            ProcessNode tgt = nodeMap.get(newTargetId);
            if (src == null || tgt == null) {
            System.out.println("Warning!");
            }
            newEdge.setSource(src);
            newEdge.setTarget(tgt);
            
            newModel.addEdge(newEdge);
            localEdgeMap.put(edge, newEdge);
            }
            
            //update docked edges for edge docker
            for (ProcessNode node : getNodes()) {
            if (node instanceof EdgeDocker) {
            String newId = localIdMap.get(node.getId());
            EdgeDocker oldDocker = (EdgeDocker) node;
            EdgeDocker newDocker = (EdgeDocker) nodeMap.get(newId);
            newDocker.setDockedEdge(localEdgeMap.get(oldDocker.getDockedEdge()));
            }
            }
            
            // clone properties
            for (String key : this.getPropertyKeys()) {
            if (key == null ? ProcessModel.ATTR_ID == null : key.equals(ProcessModel.ATTR_ID)) {
            continue;
            }
            newModel.setProperty(key, this.getProperty(key));
            }
            
            // clone references to transient properties
            for (String key : this.getTransientPropertyKeys()) {
            newModel.setTransientProperty(key, this.transientProperties.get(key));
            }
            
            
            //ff added to find the corresponding nodes from outside the class after cloning
            newModel.idMap = localIdMap;
            newModel.edgeMap = localEdgeMap;
            } catch (Exception ex) {
            ex.printStackTrace();
            }
             */

            return newModel;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ProcessModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    /**
     * Returns the ProcessNode of the model that belongs to a certain id.
     * @param id
     * @return 
     */
    public ProcessNode getNodeById(String id) {
        for (ProcessNode node : getNodes()) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Returns the ProcessNode(s) of the model that have the given name. More than one node
     * is potentially returned since multiple nodes can have the same name. Returns an empty
     * List if no node was found
     * @param id
     * @return 
     */
    public LinkedList<ProcessNode> getNodeByName(String name) {
        LinkedList<ProcessNode> result = new LinkedList<ProcessNode>(); 
        for (ProcessNode node : getNodes()) {
            if (node.getName().equals(name)) {
                result.add(node);
            }
        }
        return result;
    }

    public ProcessObject getObjectById(String id) {
        for (ProcessObject obj : getObjects()) {
            if (obj.getId().equals(id)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Returns the ProcessNode of the model that lies on the given position
     * @param id
     * @return null if nothing was found at that location
     */
    public ProcessNode getNodeAt(Point p) {
        for (int i = getNodes().size() - 1; i >= 0; i--) {
            ProcessNode node = getNodes().get(i);
            if (node.getBounds().contains(p)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Returns an (optional) AttachedNodeHandler.
     * @return
     */
    public AttachedNodeHandler getAttachedNodeHandler() {
        return null;
    }

    /**
     * Adds a listener to this ProcessModel.
     * @param listener
     */
    public void addListener(ProcessModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from this ProcessModel.
     * @param listener
     */
    public void removeListener(ProcessModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the list of current listeners
     */
    public List<ProcessModelListener> getListeners() {
        return new LinkedList<ProcessModelListener>(listeners);
    }

    /**
     * Returns the name of the process model.
     * @return
     */
    public String getProcessName() {
        return getProperty(PROP_PROCESS_NAME);
    }

    /**
     * Sets the name for this process model.
     * @param processName
     */
    public void setProcessName(String processName) {
        setProperty(PROP_PROCESS_NAME, processName);
    }

    /**
     * Returns the id of this ProcessModel
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of this ProcessModel.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the URI for this ProcessModel.<br>
     * Set after a model is loaded.
     * @return
     */
    public String getProcessModelURI() {
        return getProperty(PROP_PROCESS_URI);
    }

    /**
     * Sets the URI for this ProcessModel.
     * @param processModelURI
     */
    public void setProcessModelURI(String processModelURI) {
        setProperty(PROP_PROCESS_URI, processModelURI);
    }

    /**
     * Returns the creation date of this ProcessModel.
     * @return
     */
    public String getCreationDate() {
        return getProperty(PROP_CREATE_DATE);
    }

    /**
     * Adds a process edge to the model.
     * @param f
     */
    public synchronized void addEdge(ProcessEdge f) {
        processEdges.add(f);
        // Add listener
        f.addListener(this);
        // Add context
        f.addContext(this);
        // Mark as dirty
        markAsDirty(true);
        // Inform listeners
        for (ProcessModelListener l : new ArrayList<ProcessModelListener>(listeners)) {
            l.processEdgeAdded(f);
        }
        ProcessUtils.sortTopologically(this);
    }

    /**
     * Removes a process edge from the model.
     * @param f
     */
    public synchronized void removeEdge(ProcessEdge f) {
        processEdges.remove(f);
        // Remove listener
        f.removeListener(this);
        f.removeContext(this);
        // Mark as dirty
        markAsDirty(true);
        // Check if any EdgeDocker contained this node; if so, remove it too
        List<ProcessNode> removalList = new LinkedList<ProcessNode>();
        for (ProcessNode node : getNodes()) {
            if (node instanceof EdgeDocker) {
                EdgeDocker docker = (EdgeDocker) node;
                if (docker.getDockedEdge() == f) {
                    removalList.add(docker);
                }
            }
        }
        if (f.getTarget() instanceof EdgeDocker) {
            this.removeNode(f.getTarget());
        }
        if (f.getSource() instanceof EdgeDocker) {
            this.removeNode(f.getSource());
        }
        // Remove dockers
        for (ProcessNode n : removalList) {
            removeNode(n);
        }
        // Inform listeners
        for (ProcessModelListener l : listeners) {
            l.processEdgeRemoved(f);
        }
    }

    /**
     * Returns the list of process edges.
     * @return
     */
    public synchronized List<ProcessEdge> getEdges() {
        return processEdges;
    }

    /**
     * Adds a process node to the model.
     * @param o
     */
    public synchronized void addNode(ProcessNode node) {
        if (node != null) {
            processNodes.add(node);
            // Add listener
            node.addListener(this);
            // Add context
            node.addContext(this);
            // Mark as dirty
            markAsDirty(true);
            // Inform listeners
            for (ProcessModelListener l : listeners) {
                l.processNodeAdded(node);
            }
            if (node instanceof Cluster) {
                clusterNodes.add((Cluster) node);
            }
            ProcessUtils.sortTopologically(this);
        }
    }

    /**
     * Removes a ProcessNode from the ProcessModel (incl. edges).
     * @param o
     */
    public synchronized void removeNode(ProcessNode node) {
        removeNode(node, true);
        // Mark as dirty
        markAsDirty(true);
    }

    /**
     * Removes a ProcessNode from the ProcessModel (care of Edge handling
     * required).
     * @param node
     */
    public synchronized void removeNode(ProcessNode node, boolean includeEdges) {
        if (node == null) {
            return;
        }
        processNodes.remove(node);
        // Remove listener
        node.removeListener(this);
        // Mark as dirty
        markAsDirty(true);

        for (ProcessNode n : this.getNodes()) {
            if (n instanceof Cluster) {
                Cluster c = (Cluster) n;
                c.removeProcessNode(node);
            }
        }
        List<ProcessEdge> removalList = new LinkedList<ProcessEdge>();
        // Collect all incoming/outgoing edges to/from this node
        for (ProcessEdge e : getEdges()) {
            if (e.getTarget() == node | e.getSource() == node) {
                removalList.add(e);
            }
        }
        // Remove them
        if (includeEdges) {
            for (ProcessEdge e : removalList) {
                removeEdge(e);
            }
        }

        // Remove context
        node.removeContext(this);

        // Inform listeners
        for (ProcessModelListener l : listeners) {
            l.processNodeRemoved(node);
        }

        if (node instanceof Cluster) {
            clusterNodes.remove((Cluster) node);
        }

        //Remove from clusters containing this node
        for (Cluster c : this.getClusters()) {
            if (c.isContained(node)) {
                c.removeProcessNode(node);
            }
        }


    }

    /**
     * Returns the list of contained process nodes. 
     * @return
     */
    public synchronized List<ProcessNode> getNodes() {
        return processNodes;
    }

    /**
     * Returns a list of all contained nodes with a certain class.
     * @param c
     * @return
     */
    public synchronized List<ProcessNode> getNodesByClass(Class<?> c) {
        List<ProcessNode> result = new LinkedList<ProcessNode>();
        for (ProcessNode n : getNodes()) {
            if (c.isAssignableFrom(n.getClass())) {
                result.add(n);
            }
        }
        return result;
    }

    /**
     * Returns the list of contained Clusters.
     * @return
     */
    public List<Cluster> getClusters() {
        return clusterNodes;
    }

    /**
     * Returns all ProcessObjects (combination of Nodes and Edges) in this
     * model.
     * @return
     */
    public List<ProcessObject> getObjects() {
        List<ProcessObject> result = new LinkedList<ProcessObject>();
        result.addAll(getNodes());
        result.addAll(getEdges());
        return result;
    }

    /**
     * Returns the Cluster where the ProcessNode is contained, <b>null</b>
     * if not in any Cluster.
     * @param node
     * @return
     */
    public Cluster getClusterForNode(ProcessNode node) {
        for (Cluster c : getClusters()) {
            if (c.isContained(node)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Returns the list of successor nodes for a node n.
     * @param n
     * @return
     */
    public List<ProcessNode> getSuccessors(ProcessNode n) {
        List<ProcessNode> result = new LinkedList<ProcessNode>();

        for (ProcessEdge e : getEdges()) {
            if (e.getSource() == n) {
                result.add(e.getTarget());
            }
        }

        return result;
    }

    /**
     * Returns the list of predecessor nodes for a node n.
     * @param n
     * @return
     */
    public List<ProcessNode> getPredecessors(ProcessNode n) {
        // Look up cache
        if (predecessorCache.containsKey(n)) {
            return predecessorCache.get(n);
        }

        // Calculate result
        List<ProcessNode> result = new LinkedList<ProcessNode>();
        for (ProcessEdge e : getEdges()) {
            if (e.getTarget() == n) {
                result.add(e.getSource());
            }
        }

        // Add result to cache
        predecessorCache.put(n, result);
        // Return result
        return result;
    }

    /**
     * Returns the list of supported node classes for this model. This 
     * method needs to be implemented.
     * @return
     */
    public abstract List<Class<? extends ProcessNode>> getSupportedNodeClasses();

    public List<Class<? extends ProcessNode>> getCreateableNodeClasses() {
        List<Class<? extends ProcessNode>> result = new LinkedList<Class<? extends ProcessNode>>();
        for (Class<? extends ProcessNode> nodeClass : getSupportedNodeClasses()) {
            try {
                ProcessNode superNode = (ProcessNode) nodeClass.newInstance();
                for (Class<? extends ProcessNode> nodeC : superNode.getVariants()) {
                    result.add(nodeC);
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(ProcessModel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ProcessModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /**
     * Returns the dimension of this model.
     * @return
     */
    public synchronized Dimension getSize() {
        int maxX = 0;
        int maxY = 0;

        // Find outermost elements
        for (ProcessNode o : new LinkedList<ProcessNode>(this.getNodes())) {
            Rectangle bounds = o.getBoundingBox();
            if ((bounds.x + bounds.width) > maxX) {
                maxX = bounds.x + bounds.width;
            }
            if ((bounds.y + bounds.height) > maxY) {
                maxY = bounds.y + bounds.height;
            }
        }
        // Check routing points
        for (ProcessEdge e : new LinkedList<ProcessEdge>(this.getEdges())) {
            for (Point p : e.getRoutingPoints()) {
                if (p.x > maxX) {
                    maxX = p.x;
                }
                if (p.y > maxY) {
                    maxY = p.y;
                }
            }
        }

        return new Dimension(maxX, maxY);
    }

    /**
     * Returns the list of supported edge classes for this model. This
     * method needs to be implemented.
     * @return
     */
    public abstract List<Class<? extends ProcessEdge>> getSupportedEdgeClasses();

    /**
     * Returns the corresponding ProcessUtils.
     */
    public ProcessUtils getUtils() {
        return processUtils;
    }

    /**
     * Sets the ProcessUtils.
     * @param utils
     */
    public void setUtils(ProcessUtils utils) {
        this.processUtils = utils;
    }

    /**
     * Moves the given ProcessNode n after the ProcessNode baseNode; i.e.
     * it is drawn later. If n is a Cluster, it is processed recursivly.
     * @param n
     * @param baseNode
     */
    public void moveAfter(ProcessNode n, ProcessNode baseNode) {
        processNodes.remove(n);
        int basePos = processNodes.indexOf(baseNode);
        processNodes.add(basePos + 1, n);

        //moveAfter(n, baseNode, new LinkedList<ProcessNode>());
    }

    /*private void moveAfter(ProcessNode n, ProcessNode baseNode, List<ProcessNode> processed) {
    if (n == null | baseNode == null) {
    return;
    }
    // Check if already processed
    if (processed.contains(n)) {
    return;
    }
    // Check if n and baseNode is contained
    if (!processNodes.contains(n) | !processNodes.contains(baseNode)) {
    return;
    }
    processNodes.remove(n);
    int basePos = processNodes.indexOf(baseNode);
    if (basePos < processNodes.size()) {
    processNodes.add(basePos + 1, n);
    } else {
    processNodes.add(n);
    }
    if (n instanceof Cluster) {
    processed.add(n);
    // Process childs recursivly
    for (ProcessNode child : ((Cluster) n).getProcessNodes()) {
    moveAfter(child, n, processed);
    }
    }
    }*/
    /**
     * Moves the given ProcessNode to the start of the NodeList; i.e. it 
     * should be drawn first.
     * @param n
     */
    public void moveToBack(ProcessNode n) {
        // Check if n is contained
        if (!processNodes.contains(n)) {
            return;
        }
        // Move to back
        processNodes.remove(n);
        processNodes.addFirst(n);
    }

    /**
     * Moves the given ProcessNode to the end of the NodeList; i.e. it 
     * should be drawn last.
     * @param n
     */
    public void moveToFront(ProcessNode n) {
        // Check if n is contained     
        if (!processNodes.contains(n)) {
            return;
        }
        // Move to front
        processNodes.remove(n);
        processNodes.addLast(n);
    }

    /**
     * Returns false if all of the nodes of this ProcessModel are located at 
     * (0,0).
     * @return
     */
    public boolean isLayouted() {
        for (ProcessNode node : getNodes()) {
            if (node.getPos().x != 0 || node.getPos().y != 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks whether this model has an URI on a server, so that it can be found online.
     * @return True If the model has an URI to a server
     * False if it is a local model
     */
    public boolean isOnlineModel() {
        return getProcessModelURI().startsWith("http");
    }

    /**
     * Returns all incoming Edges of a certain type.
     * @param type
     * @param node
     * @return
     */
    public List<ProcessEdge> getIncomingEdges(Class<? extends ProcessEdge> type, ProcessNode node) {
        List<ProcessEdge> result = new LinkedList<ProcessEdge>();
        for (ProcessEdge e : getEdges()) {
            if ((type.isInstance(e)) && e.getTarget() == node) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Returns all outgoing edges of a certain type.
     * @param type
     * @param node
     * @return
     */
    public List<ProcessEdge> getOutgoingEdges(Class<? extends ProcessEdge> type, ProcessNode node) {
        List<ProcessEdge> result = new LinkedList<ProcessEdge>();
        for (ProcessEdge e : getEdges()) {
            if ((type.isInstance(e)) && e.getSource() == node) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Returns all nodes that follow the given Node directly.
     * @param type connecting edge need to be descendant of given type
     * @param node
     * @return
     */
    public List<ProcessNode> getSucceedingNodes(Class<? extends ProcessEdge> type, ProcessNode node) {
        List<ProcessNode> result = new LinkedList<ProcessNode>();
        List<ProcessEdge> OutgoingEdges = getOutgoingEdges(type, node);
        for (ProcessEdge e : OutgoingEdges) {
            result.add(e.getTarget());
        }
        return result;
    }

    /**
     * Returns all nodes that lead to the given Node directly.
     * @param type connecting edge need to be descendant of given type
     * @param node
     * @return
     */
    public List<ProcessNode> getPrecedingNodes(Class<? extends ProcessEdge> type, ProcessNode node) {
        List<ProcessNode> result = new LinkedList<ProcessNode>();
        List<ProcessEdge> IncommingEdges = getIncomingEdges(type, node);
        for (ProcessEdge e : IncommingEdges) {
            result.add(e.getSource());
        }
        return result;
    }

    /**
     * Returns all nodes that are connected via exactly one edge to the given node
     * @param type connecting edge need to be descendant of given type
     * @param node
     * @return
     */
    public List<ProcessNode> getNeighbourNodes(Class<? extends ProcessEdge> type, ProcessNode node) {
        List<ProcessNode> result = getPrecedingNodes(type, node);
        result.addAll(getSucceedingNodes(type, node));
        return result;
    }

    /**
     * Returns a short textual description of the process model.
     * @return
     */
    @Override
    public String toString() {
        if (getProcessName() == null) {
            return super.toString();
        }
        return getProcessName() + " (GenericProcessModel)";
    }

    /**
     * Returns a serialization of the Process Model. ATTENTION: Not all 
     * properties are serialized; esp. userObject() from ProcessNode is not 
     * included!
     * Sub-classses of ProcessNode and ProcessEdge might implement further
     * serialization properties.
     * @return A Document containing the process model serialized as XML.
     * @throws java.lang.Exception
     */
    public Document getSerialization() throws Exception {

        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = xmlFactory.newDocumentBuilder();
        Document xmlDoc = builder.newDocument();

        // Create root element
        Element rootNode = xmlDoc.createElement(TAG_MODEL);
        rootNode.setAttribute(ATTR_XMLNS, VALUE_XMLNS);
        rootNode.setAttribute(ATTR_XMLNS_XSI, VALUE_XMLNS_XSI);
        rootNode.setAttribute(ATTR_XSI_SCHEMALOCATION, VALUE_XSI_SCHEMALOCATION);
        rootNode.setAttribute(ATTR_NAME, getProcessName());
        rootNode.setAttribute(ATTR_TYPE, this.getClass().getName());
        rootNode.setAttribute(ATTR_ID, getId());
        xmlDoc.appendChild(rootNode);

        // Create process nodes tag
        Element pNodes = xmlDoc.createElement(TAG_NODES);
        rootNode.appendChild(pNodes);

        // Insert all process nodes                
        for (ProcessNode n : getNodes()) {
            Element elem = n.getSerialization(xmlDoc);
            if (elem != null) {
                pNodes.appendChild(elem);
            }
        }

        // Create an edges tag
        Element pEdges = xmlDoc.createElement(TAG_EDGES);
        rootNode.appendChild(pEdges);

        // Insert all edges
        for (ProcessEdge e : getEdges()) {
            Element elem = e.getSerialization(xmlDoc);
            if (elem != null) {
                pEdges.appendChild(elem);
            }
        }

        // Create a properties tag
        Element propertiesE = xmlDoc.createElement(TAG_PROPERTIES);
        rootNode.appendChild(propertiesE);
        ProcessUtils.writeProperties(xmlDoc, propertiesE, properties);

        return xmlDoc;
    }

    /**
     * Sets a property.
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Removes a property.
     * @param key
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }

    /**
     * Returns a property.
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Returns the set of keys of the properties.
     * @return
     */
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    /**
     * Sets a transient (non-serialized) property.
     * @param key
     * @param value
     */
    public void setTransientProperty(String key, Object value) {
        transientProperties.put(key, value);
    }

    /**
     * Removes a transient (non-serialized) property.
     * @param key
     */
    public void removeTransientProperty(String key) {
        transientProperties.remove(key);
    }

    /**
     * Returns a transient (non-serialized) property.
     * @param key
     * @return
     */
    public Object getTransientProperty(String key) {
        return transientProperties.get(key);
    }

    /**
     * Returns the currently registered transient property keys.
     * @return
     */
    public Set<String> getTransientPropertyKeys() {
        return transientProperties.keySet();
    }

    /**
     * @return
     */
    public Color getBackgroundColor() {
        return bgColor;
    }

    /**
     * @return
     */
    public void setBackgroundColor(Color c) {
        bgColor = c;
    }

    /**
     * @param object
     * @throws Exception
     */
    public synchronized void addObject(ProcessObject object) {
        if (object instanceof ProcessNode) {
            addNode((ProcessNode) object);
        } else if (object instanceof ProcessEdge) {
            addEdge((ProcessEdge) object);
        } else {
            throw new RuntimeException("Could not add Object " + object);
        }
    }

    /**
     * @param object
     */
    public synchronized void removeObject(ProcessObject object) {
        removeObject(object, true);
    }

    public synchronized void removeObject(ProcessObject object, boolean includeEdges) {
        if (object == null) {
            return;
        } else if (object instanceof ProcessNode) {
            removeNode((ProcessNode) object, includeEdges);
        } else if (object instanceof ProcessEdge) {
            removeEdge((ProcessEdge) object);
        } else {
            throw new RuntimeException("Could not remove Object " + object);
        }
    }
    /*
     * all relations between objects need to be corrected here
     */

    private void prepareEdgeSubstitution(ProcessEdge object, ProcessEdge substituteBy) {
        //correct source and target
        substituteBy.setSource(object.getSource());
        substituteBy.setTarget(object.getTarget());
    }

    /*
     * all relations between objects need to be corrected here, but not the attributes itself (e.g. contained nodes in cluster)
     */
    private void prepareNodeSubstitution(ProcessNode node, ProcessNode substituteBy) {
        //correct edges
        for (ProcessEdge e : this.getIncomingEdges(ProcessEdge.class, node)) {
            e.setTarget(substituteBy);
        }
        for (ProcessEdge e : this.getOutgoingEdges(ProcessEdge.class, node)) {
            e.setSource(substituteBy);
        }
        //correct cluster containment relations
        for (Cluster c : node.getParentClusters()) {
            c.removeProcessNode(node);
            c.addProcessNode(substituteBy);
        }
        //correct attached events
        for (ProcessNode n : this.getNodes()) {
            if (n instanceof AttachedNode) {
                AttachedNode attNode = (AttachedNode) n;
                if (attNode.getParentNode(this) == node) {
                    attNode.setParentNode(substituteBy);
                }
            }
        }
    }

    /*
     * makes all preparations so that original can be deleted and substituteBy can be added taking the position of original
     * onlyUpdateEnvironment controls whether original and substituteBy should actually replaced.
     */
    public void substitute(ProcessObject original, ProcessObject substituteBy, boolean onlyUpdateEnvironment) {
        if (original instanceof ProcessEdge && substituteBy instanceof ProcessEdge) {
            prepareEdgeSubstitution((ProcessEdge) original, (ProcessEdge) substituteBy);
        } else if (original instanceof ProcessNode && substituteBy instanceof ProcessNode) {
            prepareNodeSubstitution((ProcessNode) original, (ProcessNode) substituteBy);
        } else {
            throw new UnsupportedOperationException("substitution not possible");
        }
        if (!onlyUpdateEnvironment) {
            removeObject(original, false);
            addObject(substituteBy);
        }
    }

    /*
     * substitutes one Processobject by another one, updates all relations of the old one to the new one
     */
    public void substitute(ProcessObject original, ProcessObject substituteBy) {
        substitute(original, substituteBy, false);
    }

    public ProcessEdge getConnectingEdge(ProcessNode node1, ProcessNode node2) {
        List<ProcessEdge> edgesNode1 = this.getOutgoingEdges(ProcessEdge.class, node1);
        List<ProcessEdge> edgesNode2 = this.getIncomingEdges(ProcessEdge.class, node2);
        //intersection
        edgesNode1.retainAll(edgesNode2);
        if (!edgesNode1.isEmpty()) {
            return edgesNode1.get(0);
        }
        //vice versa
        edgesNode1.clear();
        edgesNode2.clear();
        edgesNode1 = this.getIncomingEdges(ProcessEdge.class, node1);
        edgesNode2 = this.getOutgoingEdges(ProcessEdge.class, node2);
        edgesNode1.retainAll(edgesNode2);
        if (!edgesNode1.isEmpty()) {
            return edgesNode1.get(0);
        }
        return null;
    }

    public AttachedNode getAttachedNode(ProcessNode parent) {
        for (ProcessNode n : getNodes()) {
            if (n instanceof AttachedNode) {
                AttachedNode attached = (AttachedNode) n;
                if (attached.getParentNode(this) == parent) {
                    return attached;
                }
            }
        }
        return null;
    }

    /**
     * This method returns the top level nodes of this model (i.e. all that
     * are not contained in a Cluster)
     */
    public LinkedList<ProcessNode> getTopLevelNodes() {
        if (topLevelNodesCache.size() > 0) {
            // Simply return
            return topLevelNodesCache;
        }

        // Retrieve all nodes that are in a cluster
        LinkedList<ProcessNode> nodesInCluster = new LinkedList<ProcessNode>();
        for (Cluster c : getClusters()) {
            nodesInCluster.addAll(c.getProcessNodes());
        }

        // Substract them from all other nodes
        for (ProcessNode n : getNodes()) {
            if (!nodesInCluster.contains(n)) {
                topLevelNodesCache.add(n);
            }
        }

        return topLevelNodesCache;
    }

    /**
     * This method returns a list of all currently vissible nodes, i.e.
     * excluding all nodes that are inside collapsed clusters (recursivly).
     */
    public List<ProcessNode> getVisibleNodes() {
        if (visibleNodesCache.size() > 0) {
            return visibleNodesCache;
        }
        visibleNodesCache.addAll(getTopLevelNodes());
        // Follow open clusters
        for (ProcessNode n : new LinkedList<ProcessNode>(getTopLevelNodes())) {
            if (n instanceof Cluster) {
                addVisibleNodes((Cluster) n);
            }
        }
        // Restore ordering
        LinkedList<ProcessNode> allNodes = new LinkedList<ProcessNode>(getNodes());
        allNodes.retainAll(visibleNodesCache);
        visibleNodesCache = allNodes;
        // Return result
        return visibleNodesCache;
    }

    /**
     * Internal method that fills the visibleNodesCache directly.
     * @param c
     */
    private synchronized void addVisibleNodes(Cluster c) {
        if (c.isCollapsed()) {
            return;
        }
        for (ProcessNode n : c.getProcessNodes()) {
            if (visibleNodesCache.contains(n)) {
                continue;
            }
            visibleNodesCache.add(n);
            // Follow open clusters
            if (n instanceof Cluster) {
                addVisibleNodes((Cluster) n);
            }
        }
        return;
    }

    public List<ProcessEdge> getPreceedingEdges(ProcessNode node) {
        // Check if already in cache
        if (precEdgeCache.containsKey(node)) {
            return precEdgeCache.get(node);
        }
        // No, continue
        List<ProcessEdge> precEdges = new LinkedList<ProcessEdge>();
        // Find succeeding edges
        for (ProcessEdge e : getEdges()) {
            if (e.getTarget() == node) {
                precEdges.add(e);
            }
        }
        // Add to cache and return
        precEdgeCache.put(node, precEdges);
        return precEdges;

    }

    /**
     * Starts a transaction on this model.
     */
    public void startTransaction() {
    }

    /**
     * Ends a transaction on this model.
     */
    public void stopTransaction() {
    }

    /**
     * Adds a transaction handler. A transaction handler is called each time
     * a transaction is started or stopped.
     * @param h 
     */
    public void addTransactionHandler(TransactionHandlerInterface h) {
    }

    /**
     * Removes a transaction handler.
     * @param h 
     */
    public void removeTransactionHandler(TransactionHandlerInterface h) {
    }

    /*
     *
     * ProcessObjectListener follows
     *
     */
    @Override
    public void propertyChanged(ProcessObject o, String key, String oldValue, String newValue) {
        // Inform all model listeners
        for (ProcessModelListener l : this.listeners) {
            l.processObjectPropertyChange(o, key, oldValue, newValue);
        }
    }
}
