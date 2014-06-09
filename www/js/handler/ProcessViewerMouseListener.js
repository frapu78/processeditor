Ext.namespace("Inubit.WebModeler.handler");

Inubit.WebModeler.handler.ProcessViewerMouseListener = Ext.extend(Object, {
	constructor : function (config) {
		this.viewer = config.viewer;
    	this.lastMouseX = 0;
    	this.lastMouseY = 0;
        this.componentId = config.componentId;
    	this.mouseX = 0;
    	this.mouseY = 0;
    	this.currentlySelected = null;
	},
	mouseDownOnNode : function(event, node) {
        this.updateMousePos(event);

        if (this.currentlySelected != null) {
            this.currentlySelected.setSelected(false);

            if (this.currentlySelected.commentBalloon) {
                this.currentlySelected.commentBalloon.destroy();
                this.currentlySelected.commentBalloon = null;
            }
        }

        this.currentlySelected = node;

        return false;
    },
	mouseDownOnEdge : function(edge) {
        if (this.currentlySelected != null)
            this.currentlySelected.setSelected(false);

        this.currentlySelected = edge;

        edge.setSelected(true, false, Util.COLOR_BLUE);
    },
	updateMousePos : function(event) {
        var viewerPos = Ext.getCmp(this.componentId).getPosition();
        var scrollPos = Ext.getCmp(this.componentId).body.getScroll();

        var x,y;
        //distinguish between ordinary Browser-Event and ExtJS-EventObject
        if (event.pageX) {
            x = event.pageX;
            y = event.pageY;
        } else {
            x = event.getPageX();
            y = event.getPageY();
        }

        this.lastMouseX = this.mouseX;
        this.lastMouseY = this.mouseY;

        this.mouseX = x-viewerPos[0]+scrollPos.left;
        this.mouseY = y-viewerPos[1]+scrollPos.top;
    },
	mouseDown : function() {
        if (this.currentlySelected)
            this.currentlySelected.setSelected(false);

        this.currentlySelected = null;
       	this.viewer.displayProperties(this.viewer.getModel());
    },
    mouseMove : function() {
        return false;
    },
    mouseUp : function()  {
        return false;
    }
} );