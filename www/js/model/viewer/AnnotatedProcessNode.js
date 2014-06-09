Ext.namespace('Inubit.WebModeler.model.viewer');
/**
* Creates a new ProcessNode from a given URI.
* @param config{uri, model}
*/
Inubit.WebModeler.model.viewer.AnnotatedProcessNode = Ext.extend(Inubit.WebModeler.ProcessNode, {
    constructor : function(config) {
		this.attachments = new Array();
    	this.commentCount = 0;
    	this.commentSign = null;
		this.commentBalloon = null;

		Inubit.WebModeler.model.viewer.AnnotatedProcessNode.superclass.constructor.call(this, config);

		if (!Ext.isDefined(this.rootComponent)) {
			this.rootComponent = ProcessViewer.instance;
		}

		this.setPicture(config.uri + ".png");
	},
    addCommentBalloon : function() {
        if (this.commentBalloon != null && !this.commentBalloon.isVisible()) {
                    this.commentBalloon.destroy();
                    this.commentBalloon = null;
        }
        if (this.commentBalloon == null) {
            var el = null;

            if (this.graphics)
                el = this.graphics.node;

            this.commentBalloon = new AnnotationBalloon(this, el, this.rootComponent.getCanvas());
        }
    },

    /**
     * Select or deselect this node.
     */
    setSelected : function(selected, showButtons, color) {
//        this.setNodeSelected(selected, showButtons, color, this.rootComponent);
        Inubit.WebModeler.model.viewer.AnnotatedProcessNode.superclass.setSelected.call(this, selected, showButtons, color, this.rootComponent );
    },
//    setNodeSelected : Inubit.WebModeler.ProcessNode.prototype.setSelected,
    select : function() {
//        this.addCommentBalloon();
//        this.commentBalloon.show();
        this.setSelected(true, false, Util.COLOR_BLUE);
        this.showContextMenu();
    },
    init : function(xml, canvas) {
        var properties = Util.parseProperties(xml);
        this.properties = properties;
        this.parseBounds(xml);
        this.updateMetadata();

        this.paint(canvas);
        this.loadComments(canvas);
        this.alignZOrder();

        this.fireEvent("load");
//        this.loadFinished();
    },
    /**
     * "ABSTRACT" METHODS
     */

    bindEventsToNode : function() {
        var processNode = this;

        //handle mousedown event
        this.graphics.node.onmousedown = function(event) {
            //stop event forwarding
            Util.stopEvent(event);

            //first check for edge selection before proceeding with node selection
            processNode.rootComponent.mouseListener.updateMousePos(event);
            var mouseX = processNode.rootComponent.mouseListener.mouseX;
            var mouseY = processNode.rootComponent.mouseListener.mouseY;

            var edge = processNode.model.getEdgeCloseTo(mouseX, mouseY);

            if (edge) {
                processNode.rootComponent.mouseListener.mouseDownOnEdge(edge);
                return false;
            }

            if (event.button == 0 || event.button == 1) {
                //else, handle selection
                if (processNode.isBPMNPool()) {
                    //if mousedown on pool --> check if lane is hit
                    var newNode = processNode.model.getClusterAtPosition(mouseX, mouseY, null);

                    //if mousedown on lane --> select lane instead of surrounding pool
                    if (newNode && newNode.isBPMNLane()) {
                        newNode.getLaneHandler().handleSelection(event);
                        return false;
                    }
                }

                processNode.rootComponent.mouseListener.mouseDownOnNode(event, processNode);
                processNode.select();
            }
            //make Firefox move node, not the image it contains
            return false;
        };
    },
    /**
    * Create a context menu for this node
    *
    * @return{NodeContextMenu} the context menu
    */
    createContextMenu : function() {
        return new Inubit.WebModeler.ViewNodeContextMenu({object: this, controller : ProcessViewer.instance});
    },
	getCommentURI : function() {
        return this.uri + "/comments";
    },
        getLaneHandler : function() {
            if (this.laneHandler == null)
                this.laneHandler = new Inubit.WebModeler.handler.ProcessViewerLaneHandler({ lane: this});

            return this.laneHandler;
    }
});

Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER = "net.frapu.code.visualization.EdgeDocker";
Inubit.WebModeler.ProcessNode.CLASS_BPMN_LANE = "net.frapu.code.visualization.bpmn.Lane";
Inubit.WebModeler.ProcessNode.CLASS_BPMN_POOL = "net.frapu.code.visualization.bpmn.Pool";