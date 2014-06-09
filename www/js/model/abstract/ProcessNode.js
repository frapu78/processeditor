Ext.namespace("Inubit.WebModeler");

Ext.define( 'Inubit.WebModeler.ProcessNode',  {
    extend : 'Inubit.WebModeler.ProcessObject',
    
    constructor: function (config) {
        this.x = 0;
    	this.y = 0;
    	this.width = 0;
    	this.height = 0;
    	this.imageWidth = 0;
    	this.imageHeight = 0;

    	this.offsetX = 0;
    	this.offsetY = 0;

    	this.picture = null;
    	this.laneHandler = null;

    	this.cluster = null;
    	this.edges = new Array;
    	this.clusterEdges = new Array();
   	 	this.crossClusterEdges = new Array();
    	this.children = new Array();
    	this.commentCount = 0;
        this.hintGraphics = new Array();

    	this.commentSign = null;
        Inubit.WebModeler.ProcessNode.superclass.constructor.call(this, config);
    },
    /**
     * Paint this node
     *
     * @param{Raphael canvas} canvas
     *          the canvas to draw on
     */
    paint : function(canvas) {
        //if this a lane, it will be painted by its surrounding pool
        if (this.isBPMNLane()) {
            return;
        }
        //get top left corner of image
        var x = Math.floor(this.x - this.imageWidth / 2 - this.offsetX);
        var y = Math.floor(this.y - this.height / 2 - this.offsetY);
        this.graphics = canvas.image(this.picture,
                                    x, y, this.imageWidth + 6 , this.imageHeight + 6);

        //move children to front, if they exist
        this.alignZOrder();

        if (this.getType() == Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER)
            return;

        this.paintRenderingHints(canvas);

        //bind events to the graphical representation
        this.bindEventsToNode();
    },
    getVisibleProperties : function () {
    	if (this.isBPMNLane()) {
    		var tmpProperties = new Object();
    		for (var key in this.properties) {
    			if (key != 'vertical_Pool') {
    				tmpProperties[key] = this.properties[key];
    			}
    		}
    		return tmpProperties;
    	} else {
    		return this.properties;
    	}
    },
    drawCommentHighlight : function( canvas ) {
        if (this.hasComments()) {
            var bounds = this.getFrameBounds();
            var x = bounds.x - 5;
            var y = bounds.y - 5;

            this.commentSign = canvas.image(Util.getContext() + Util.ICON_COMMENTS,
                                    x, y, 16, 16);

            if (this.graphics)
                this.commentSign.node.onmousedown = this.graphics.node.onmousedown;
        }
    },
    dropCommentHighlight : function() {
        if (this.commentSign)
            this.commentSign.remove();
    },
    hasComments : function() {
        return this.commentCount > 0;
    },
    paintRenderingHints : function(canvas) {
        if ( !Ext.isDefined(Inubit.Plugins) || !Ext.isDefined(Inubit.Plugins.PluginManager.instance) )
            return;

        var i;
        for ( i = 0; i < this.hintGraphics.length; i++ )
            this.hintGraphics[i].remove();

        this.hintGraphics = new Array();

        var hints = Inubit.Plugins.PluginManager.instance.getRenderingHints( this.getType() );
        var bounds = this.getFrameBounds();
        var x = bounds.x - 5;
        var y = bounds.y - 5;
        for ( i = 0; i < hints.length; i++ ) {
            if ( hints[i].check(this) ) {
                var iconX = x;
                var iconY = y;
                var offsetInfo = hints[i].renderOffset;
                
                if (offsetInfo.w == 'r')
                    iconX += bounds.width - 10;
                else if (offsetInfo.w == 'c')
                    iconX += ( bounds.width - 10 )/ 2;

                iconX += offsetInfo.x;

                if (offsetInfo.h == 'b')
                    iconY += bounds.height - 10;
                else if (offsetInfo.h == 'c')
                    iconY += ( bounds.height - 10 ) / 2;

                iconY += offsetInfo.y;

                var hintGraphic = canvas.image(Util.getContext() + hints[i].icon, iconX, iconY, 16, 16);

                if ( this.graphics ) 
                    hintGraphic.node.onmousedown = this.graphics.node.onmousedown;

                this.hintGraphics.push(hintGraphic);
            }
        }

        if ( this.isBPMNPool() || this.isBPMNLane() ) {
            if ( this.children.length > 0 && this.children[0].isBPMNLane() )
                for ( i = 0; i < this.children.length; i++ )
                    this.children[i].paintRenderingHints(canvas);
        }

    },

    /**
     * Select or deselect this node.
     *
     * @param{Boolean} selected
     *      indicates if the node should be selected (true) or deselected(false)
     * @param{Boolean} showButtons
     *      indicates if context menu should by displayed
     * @param{String} color
     *      RGB color value ("#rrggbb") for the color of the selection frame
     * @param{Object} rootCmp
     *      ProcessViewer or ProcessEditor instance
     */
    setSelected : function(selected, showButtons, color, rootCmp) {
        if (showButtons == null) showButtons = false;
        this.isSelected = selected;

        if (selected) {
            var canvas = rootCmp.getCanvas();

            if (this.selectionFrame == null)
                this.selectionFrame = new Inubit.WebModeler.SelectionFrame({object : this});

            this.selectionFrame.paint(canvas, color);

            if (showButtons)
                this.showContextMenu();
        } else {
            if (this.selectionFrame != null)
                this.selectionFrame.remove();
            if (this.contextMenu)
                this.contextMenu.hide();
        }
    },

    /**
     * Get the bounds of the selection frame for this node
     * @return the bounds
     */
    getFrameBounds : function() {
        return {
            x: this.x - this.width / 2,
            y: this.y - this.height / 2,
            width: this.width,
            height: this.height
        };
    },
    /**
     * Set this nodes position. This does not include graphical movement of the node.
     * @param{int} x x-coordinate
     * @param{int} y y-coordinate
     */
    setPos : function(x,y) {
            this.x = x;
            this.y = y;
            this.properties.x = x;
            this.properties.y = y;
    },
    /**
     * Set size of this node. This does not change the size graphically.
     * @param{int} width the new width
     * @param{int} height the new height
     */
    setSize : function(width, height) {
        this.width = width;
        this.height = height;
        this.properties.width = width;
        this.properties.height = height;
    },

    /**
     * Set size of this node's image (no graphical change)
     * @param{int} width the new width
     * @param{int} height the new height
     */
    setImageSize : function(width, height) {
        this.imageWidth = width;
        this.imageHeight = height;
    },
    /**
     * Set this node's picture URI
     * @param{String} uri the URI
     */
    setPicture : function(uri) {
            this.picture = uri;
    },
    /**
     * Add an edge that ends/starts with this node
     * @param{ProcessEdge} edge the edge
     */
    addEdge : function(edge) {
        this.edges.push(edge);
    },
     /**
     * Set offsets for selection frame. The offset value is relative to this
     * node's centre point
     * @param{int} offsetX the x-offset
     * @param{int} offsetY the y-offset
     */
    setSelectionOffsets : function(offsetX, offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    },
    /**
     * Add a child node to this node
     * @param{ProcessNode} node the child to be added
     */
    addChild : function(node) {
        this.children.push(node);
    },
    /**
     * Get this nodes cluster
     * @return{ProcessNode} the cluster
     */
    getCluster : function() {
        if (this.cluster && this.cluster.isBPMNLane())
            return this.cluster.getLaneHandler().getSurroundingPool();
        else
            return this.cluster;
    },
    /**
     * Set this nodes cluster
     * @param{ProcessNode} c the cluster
     */
    setCluster : function(c) {
        this.cluster = c;

        if ( this.attachments != null )
            for ( var i = 0; i < this.attachments.length; i++)
                this.attachments[i].setCluster(c);
    },
       /**
     * Bring this node and all its child nodes and edges to front according to their
     * containment hierarchy.
     */
    alignZOrder : function() {
        if (this.graphics)
            this.graphics.toFront();
        if (this.selectionFrame)
            this.selectionFrame.toFront();
        if (this.commentSign)
            this.commentSign.toFront();
        
        var i;
        for ( i = 0; i < this.hintGraphics.length; i++ ) 
            this.hintGraphics[i].toFront();

        for ( i = 0; i < this.clusterEdges.length; i++)
            this.clusterEdges[i].alignZOrder();

        for (i = 0; i < this.crossClusterEdges.length; i++)
            this.crossClusterEdges[i].alignZOrder();

        for (i = 0; i < this.children.length; i++)
            this.children[i].alignZOrder();

        if ( this.attachments ) {
            for ( i = 0; i < this.attachments.length; i++ )
                this.attachments[i].alignZOrder();
        }
    },
    addCrossClusterEdge : function(edge) {
        if (!(edge.isProcessEdge && edge.isProcessEdge())) return;

        this.crossClusterEdges.push(edge);
    },
    addClusterEdge : function(edge) {
        this.clusterEdges.push(edge);
    },
    getAttachedTo : function() {
    	return this.attachedTo;
    },
    setAttachedTo : function(nodeId) {
        if (nodeId == "")
            this.attachedTo = null;
        else
            this.attachedTo =  nodeId;
    },
    parseBounds : function(xml) {
        var props = Util.parseProperties(xml);

        for (var name in props)
            this.properties[name] = props[name];

        var x = parseInt(props["x"]);
        var y = parseInt(props["y"]);
        var width = parseInt(props["width"]);
        var height = parseInt(props["height"]);

        this.setPos(x, y);
        this.setSize(width, height);
    },
    equalsOrContains : function(node) {
        if (this == node) return true;
        for (var i = 0; i < this.children.length; i++) {
            if (this.children[i] == node) return true;
        }

        return false;
    },
    isBPMNLane : function() {
        return this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_BPMN_LANE;
    },
    isBPMNPool : function() {
        return this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_BPMN_POOL ||
            this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_SIM_BPMN_POOL;
    },
    isUMLClass : function() {
        return this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_UML_CLASS;
    },
    isDomainClass : function() {
        return this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_DOMAIN_CLASS;
    },
    isProcessNode : function() {
        return true;
    },
    isBPMNSubProcess : function() {
        return this.properties["#type"] == Inubit.WebModeler.ProcessNode.CLASS_BPMN_SUBPROCESS;
    },
    isCluster : function() {
        return this.isBPMNLane() || ( this.isBPMNPool() && this.properties['blackbox_pool'] != '1' ) || this.isBPMNSubProcess() || this.isClusterNode;
    },
    getLaneHandler : function() {
        if (this.laneHandler == null)
            this.laneHandler = new Inubit.WebModeler.LaneHandler({lane:this});
        return this.laneHandler;
    },
    containsLanes : function() {
        if (this.children[0])
            return this.children[0].isBPMNLane();
    },
    applyMetadataUpdateResponse : function(xml) {
        var metaproperties = Util.parseProperties(xml);

        var imageWidth = parseInt(metaproperties["imagewidth"]);
        var imageHeight = parseInt(metaproperties["imageheight"]);
        var offsetX = parseInt(metaproperties["offsetx"]);
        var offsetY = parseInt(metaproperties["offsety"]);
        var attachedTo = metaproperties["attachedTo"];
        var attachable = metaproperties["attachable"];
        var attachmentPossible = metaproperties["attachmentPossible"];

        if (attachable == "true")
            this.isAttachable = true;

        if (attachmentPossible == "true")
            this.isAttachmentPossible = true;
        
        this.isClusterNode = metaproperties["isCluster"] == "true";

        this.setImageSize(imageWidth, imageHeight);
        this.setSelectionOffsets(offsetX, offsetY);
        this.setAttachedTo(attachedTo);
    },
    applyJSONMetadataUpdateResponse : function(json) {
        var imageWidth = parseInt(json.imagewidth);
        var imageHeight = parseInt(json.imageheight);
        var offsetX = parseInt(json.offsetx);
        var offsetY = parseInt(json.offsety);
        var attachedTo = json.attachedTo;
        var attachable = json.attachable;
        var attachmentPossible = json.attachmentPossible;

        if (attachable == "true")
            this.isAttachable = true;

        if (attachmentPossible == "true")
            this.isAttachmentPossible = true;
        
        this.isClusterNode = json.isCluster;

        this.setImageSize(imageWidth, imageHeight);
        this.setSelectionOffsets(offsetX, offsetY);
        this.setAttachedTo(attachedTo);
    },
    loadComments : function(canvas) {
        var node = this;
        
        Ext.Ajax.request({
            url: this.getCommentURI(),
            method: 'GET',
            success: function(response, options) {
                var json = Ext.decode(response.responseText);
                if ( json.comments && json.comments.length > 0 ) {
                    node.commentCount = json.comments.length;
                    node.drawCommentHighlight(canvas);
                }
            },
            scope: this
        })
    },
    
    /**
     * LOADING UTILITIES
     */
    load : function(canvas, async) {
        if (async == null) async = false;
         // Fetch xml from uri
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, async);
        if (async) {
            var node = this;
            req.onreadystatechange = function() {
                if (req.readyState == 4 && req.status == 200)
                    node.init(req.responseXML, canvas);
            };
            req.send(null);
        } else {
            req.send(null);
            this.init(req.responseXML, canvas);
        }
    },

    /**
     * "ABSTRACT" METHODS
     */

    init : function(xml, canvas) {
        return false;
    },
    bindEventsToNode : function() {
        return false;
    },
    createContextMenu : function() {
        return false;
    },
    getCommentURI : function() {
        return false;
    }

});

Inubit.WebModeler.ProcessNode.addStatics({
    CLASS_BPMN_LANE : "net.frapu.code.visualization.bpmn.Lane",
    CLASS_EDGE_DOCKER : "net.frapu.code.visualization.EdgeDocker"
})

//Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER = "net.frapu.code.visualization.EdgeDocker";
//Inubit.WebModeler.ProcessNode.CLASS_BPMN_LANE = "net.frapu.code.visualization.bpmn.Lane";
Inubit.WebModeler.ProcessNode.CLASS_BPMN_POOL = "net.frapu.code.visualization.bpmn.Pool";
Inubit.WebModeler.ProcessNode.CLASS_SIM_BPMN_POOL = "net.frapu.code.visualization.bpmn4sim.Pool";
Inubit.WebModeler.ProcessNode.CLASS_BPMN_SUBPROCESS = "net.frapu.code.visualization.bpmn.SubProcess";
Inubit.WebModeler.ProcessNode.CLASS_UML_CLASS = "net.frapu.code.visualization.uml.UMLClass";
Inubit.WebModeler.ProcessNode.CLASS_DOMAIN_CLASS = "net.frapu.code.visualization.domainModel.DomainClass";

Inubit.WebModeler.ProcessNode.CLUSTER_SELECTION_RIM = 20;
