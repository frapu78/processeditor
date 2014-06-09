Ext.namespace("Inubit.WebModeler");
/**
 * Abstract base class for models
 */


Inubit.WebModeler.ProcessModel = Ext.extend(Object, {
    constructor : function(config) {
        this.uri = config.uri;
    	this.baseId = config.baseId;
    	this.tmpId = config.tmpId;
    	this.type = config.type;
    	this.version = config.version;
    	this.nodes = config.nodes;
    	this.edges = config.edges;

    	this.properties = new Array();
    	this.folder = "/";
    	this.access = "NONE";
    },
    fetchMetadata : function() {
        var metaReq = new XMLHttpRequest();
        metaReq.open("GET", this.uri + "/meta", false);
        metaReq.send(null);

        var props = Util.parseProperties(metaReq.responseXML);

        this.folder = props.folder;

        this.access = props.access;
    },
    getVisibleProperties : function() {
    	return this.properties;
    },
    setFolder : function(folder) {
        this.folder = folder;
    },
    setAccess : function(access) {
        this.access = access;
    },
    addEdge : function(edge) {
        if (!edge.isProcessEdge || !edge.isProcessEdge())
            return;
        this.resolveNodesForEdge(edge);

        this.edges.push(edge);
    },
    addNode : function(node) {
        if (!node.isProcessNode || !node.isProcessNode())
            return;

        this.nodes.push(node);
    },
    getNodeWithId : function(id) {
        for (var i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i].getProperty("#id") == id) {
                return this.nodes[i];
            }
        }

        return null;
    },
    getEdgeWithId : function(id) {
        for (var i = 0; i < this.edges.length; i++) {
            if (this.edges[i].getProperty("#id") == id) {
                return this.edges[i];
            }
        }

        return null;
    },
    getClusterAtPosition : function(x, y, currentNode) {
        var result = null;
        var possibleClusters = new Array();
        for (var i = this.nodes.length - 1; i >=0; i--) {
            var node = this.nodes[i];

            if (!node.isCluster()) continue;


            var x1 = node.x - node.width/2;
            var x2 = node.x + node.width/2;
            var y1 = node.y - node.height/2;
            var y2 = node.y + node.height/2;

            if ((x>=x1) & (x<=x2) & (y>=y1) & (y<=y2) & node != currentNode)
                possibleClusters.push(node);
        }

        if (possibleClusters.length == 1) {
            result = possibleClusters[0];
        } else {
            for (var i = 0; i < possibleClusters.length; i++) {
                var cluster = possibleClusters[i];
                var match = false;

                for (var j = 0; j < possibleClusters.length; j++ ) {
                    if (j == i) continue;
                    if (cluster.equalsOrContains(possibleClusters[j]))
                        match = true;
                }

                if (!match) {
                    result = cluster;
                    break;
                }
            }
        }

        return result;
    },
    getEdgeCloseTo : function(x, y) {
        for (var i = 0; i < this.edges.length; i++)
            if (this.edges[i].isCloseToPoint(x, y))
                return this.edges[i];

        return null;
    },
    applyNodeHierarchy : function() {
        for (var i = 0; i < this.nodes.length; i++) {
            this.nodes[i].setModel(this);
            if (this.nodes[i].attachedTo != null) {
                var parent = this.getNodeWithId(this.nodes[i].attachedTo);
                if (parent != null) {
                    parent.attachments.push(this.nodes[i]);
                    this.nodes[i].setCluster(parent.getCluster());
                }
            }
            if (this.nodes[i].isCluster()) {
                var cluster = this.nodes[i];
                var value = cluster.getProperty("#nodes");

                if (value == null) continue;

                var nodeIds = value.split(";");
                for (var j = 0; j < nodeIds.length; j++) {
                    var node = this.getNodeWithId(nodeIds[j]);

                    if (node != null && node != cluster) {
                        cluster.addChild(node);
                        node.setCluster(cluster);
                    }
                }
            }
        }
    },
    getType : function() {
        return this.type;
    },
    getName : function() {
        return this.properties["name"];
    },
    getAccess : function() {
        return this.access;
    },
    isNewModel : function() {
        return this.baseId == null;
    },
    loadProperties : function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, false);
        req.setRequestHeader("Accept", "application/xml+model");
        req.send(null);
        var xml = req.responseXML;

        if (xml) {
            this.properties = Util.parseProperties(xml.getElementsByTagName("properties")[0]);
            this.properties["#type"] = 'model';
        }
    },
    setSelected : function() {
        return false;
    },
    getFolder : function() {
        return this.folder;
    },
    getProperty : function(key) {
        return this.properties[key];
    },
    resolveEdgeEnds : function() {
        for (var i = 0; i < this.edges.length; i++) {
            var edge = this.edges[i];

            this.resolveNodesForEdge(edge);
        }
    },
    resolveNodesForEdge : function(edge) {
        edge.setModel(this);
        var source = this.getNodeWithId(edge.source);
        var target = this.getNodeWithId(edge.target);

        edge.source = source;
        edge.target = target;
        edge.source.addEdge(edge);
        edge.target.addEdge(edge);

        //handle edge docker as target
        if (edge.target.getProperty("#type") == Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER) {
            var dockId = edge.target.getProperty("#docked_edge");
            var dockEdge = this.getEdgeWithId(dockId);
            dockEdge.setDocker(edge.target);

            var last = edge.points.length - 1;
            var dockx = (edge.points[last].x + edge.points[last-1].x - 5) / 2;
            var docky = (edge.points[last].y + edge.points[last-1].y - 5) / 2;

            edge.target.setPos(dockx, docky);

            edge.target.setSize(0, 0);
        }
        edge.defineCluster();
    },
    alignZOrder : function() {
        for (var i = 0; i < this.nodes.length; i++) {
            var node = this.nodes[i];
            if (node.cluser != null)
                continue;

            node.alignZOrder();
        }
    },
    canBeCommented : function() {
        return (this.access && this.access != 'NONE' && this.access != 'VIEW');
    },
    //ABSTRACT
    getCommentURI : function() {
        return false;
    }
});