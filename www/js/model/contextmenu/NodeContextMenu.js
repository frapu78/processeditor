Ext.namespace('Inubit.WebModeler');

Inubit.WebModeler.NodeContextMenu = Ext.extend(Inubit.WebModeler.ContextMenu, {
    constructor : function(config) {

        this.succButtons = new Array();
        this.commentTool = null;
        this.controller = config.controller;

        Inubit.WebModeler.NodeContextMenu.superclass.constructor.call(this, config);
    },
    show : function() {
        if (this.contextButtons.length == 0) {
            var x = this.object.getFrameBounds().x;
            var y = this.object.getFrameBounds().y;
            this.createContextButtons( x,y );
            this.addPluginContextButtons(x, y);
        } else {
            this.updatePosition();
            for (var i = 0; i < this.contextButtons.length; i++) {
                if (this.contextButtons[i].getItemId() == 'anno') {
                    if (!this.controller.allowComments) {
                        if (this.contextButtons[i].isVisible())
                        	this.contextButtons[i].hide();
                        continue;
                    }
                }
                if (!this.contextButtons[i].isVisible())
                    this.contextButtons[i].setVisible(true);
                this.contextButtons[i].resumeEvents();
                this.contextButtons[i].enable();
                this.contextButtons[i].el.fadeIn({duration: Util.ANIMATION_FADE_IN_TIME, concurrent: true});
            }
            
            if ( this.commentTool )
                this.commentTool.setTarget( this.oldCommentTarget );
        }
    },
    hide : function(destroy) {
        if (destroy == null)
            destroy = false;
        Inubit.WebModeler.NodeContextMenu.superclass.hide.call(this);

        this.hideNodeRecommendations();
        if (this.commentTool) {
            this.commentTool.hide();
        }
    },
    destroy : function() {
        this.hide(true);
    },
    createContextButtons : function(x,y) {
        this.createDeleteButton(x, y);
        if (this.object.isBPMNLane() || this.object.isBPMNPool())
            this.createLaneButton(x, y);

        this.createCommentButton(x, y);
        //for lanes add resize button and return
        if (this.object.isBPMNLane()) {
            this.createLaneResizeButton(x, y);
            return;
        }

        this.createRefactorButton(x, y);
        this.createEdgeButton(x, y);
//        if (this.object.isResizable()) {
        this.createDefaultResizeButton(x, y);
//        }
    },
    getViewMode : function() {
    	return false;
    },
    createLaneButton : function(x, y) {
        var itemId = 'lane';

        var laneButton = new Ext.Button({
            itemId: itemId,
            icon: Util.getContext() + Util.ICON_LANE_BELOW,
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId)
        });
		this.addContextMenuButton(laneButton);

        //bind event
        var processNode = this.object;
        laneButton.el.dom.onmousedown = function(event) {
            Util.stopEvent(event);
            //processNode.dropContextButtons();
            processNode.model.createLane(processNode);
        };

        if (this.object.getProperty("blackbox_pool") == '1')
            laneButton.hide();

        this.contextButtons.push(laneButton);
    },
    createEdgeButton : function(x, y) {
        var itemId = 'edge';
        var menu = this;

        //button for edge ( + maybe node) creation
        var edgeButton = new Ext.Button({
            itemId: itemId,
            icon: Util.getContext() + Util.ICON_NEW_EDGE,
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId),
            hideMode : 'visibility'
        });

        this.addContextMenuButton(edgeButton);

        var successors = this.object.model.getType().nodeSuccessors[this.object.getType()];

        if (successors && successors.length > 0) {
            for (var i = 0; i < successors.length; i++) {
                //calculate position for 'button'
                var rowCount = Math.floor(i/ 3);
                var colCount = i % 3;
                var bx = edgeButton.getPosition(true)[0] + 25 * (colCount + 1);
                var by = edgeButton.getPosition(true)[1] + 25 * rowCount;

                var button = new Ext.Panel({
                    height: 22,
                    width: 22,
                    html: '<img src="' + Util.getContext() + '/utils/dummy?name='+ successors[i] +'" style="width:18;height:18;padding:1px;"/>',
                    hidden: true,
                    x: bx,
                    y: by
                });

                Ext.getCmp('editor').add(button);
                Ext.getCmp('editor').doLayout();

                //place reference to type
                button.body.dom.setAttribute("type", successors[i]);

                this.succButtons.push(button);
				var ctrl = this.controller;
                button.body.dom.onmousedown = function(event) {
                    Util.stopEvent(event);
                    var type = this.getAttribute("type");

                    var x = ctrl.mouseListener.getMousePosition().x;
                    var y = ctrl.mouseListener.getMousePosition().y;

                    var width = menu.object.model.getType().defaultSizes[type].width;
                    var height = menu.object.model.getType().defaultSizes[type].height;

                    var img = Util.getContext() + '/utils/dummy?name=' + type;

                    menu.hide();

                    var dummy = new DummyNode(x, y, width, height, img, type, menu.object);
                    ctrl.selectionHandler.singleSelect(dummy);
                    ctrl.mouseListener.currentlyDraggedNode = dummy;
                    ctrl.mouseListener.enableDragging();

                    return false;
                };
            }
        }
		var ctrl = this.controller;
        //bind edge creation event
        edgeButton.el.dom.onmousedown = function(event) {
            Util.stopEvent(event);
            menu.hide();
            menu.object.setSelected(false);
            menu.object.setSelected(true, false, Util.COLOR_GREEN);
            ctrl.mouseListener.currentEdgeSource = menu.object;
        };

        //on mouse over display 'buttons' for node creation
        edgeButton.on("mouseover", function() {
            for (var i = 0; i < menu.succButtons.length; i++) {
                menu.succButtons[i].setVisible(true);
            }
        });
//        edgeButton.el.dom.onmouseover = function(event) {
//            for (var i = 0; i < menu.succButtons.length; i++) {
//                menu.succButtons[i].setVisible(true);
//            }
//        };

        this.contextButtons.push(edgeButton);
    },
    createDefaultResizeButton : function(x, y) {
        var itemId = 'res';
        //use panel instead of button, since button size was not able to be pushed below 20x20
        var resButton = new Ext.panel.Panel({
            itemId: itemId,
            border:false,
            bodyStyle: {
              background: 'none'  
            },
            html: "<img src='" + Util.getContext() + Util.ICON_SE_RESIZE + "'/>",
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId),
            width: 12,
            height: 12
        });
        this.addContextMenuButton(resButton);
                
        var menu = this;
        resButton.body.dom.onmousedown = function(event) {
            Util.stopEvent(event);
            menu.hide();
            menu.controller.mouseListener.startResizeFrame(menu.object.selectionFrame);

            return false;
        };

        resButton.body.dom.onmouseover = function(event) {
            Ext.get("editor").setStyle("cursor", "se-resize");
        };

        resButton.body.dom.onmouseout = function(event) {
            Ext.get("editor").setStyle("cursor", "default");
        };

        this.contextButtons.push(resButton);
    },
    createCommentButton : function(x, y) {
        var itemId = 'anno';
        var ctrl = this.controller;
        var commentButton = new Ext.Button({
            itemId: itemId,
            icon: Util.getContext() + Util.ICON_COMMENTS,
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId),
            hidden: !ctrl.allowComments
        });
		this.addContextMenuButton(commentButton);
                
        //bind event
        var menu = this;
        commentButton.el.dom.onmousedown = function(event) {
            Util.stopEvent(event);
            if (menu.commentTool == null || !menu.commentTool.isVisible() )
                menu.commentTool = new Inubit.WebModeler.AnnotationBalloon( {
                    processObject: menu.object, 
                    target: commentButton.el, 
                    canvas: menu.controller.getCanvas()
                } );

            menu.commentTool.show();
        };
        this.contextButtons.push(commentButton);
    },
    createLaneResizeButton : function(x, y) {
    	var itemId = 'lresh';
    	var bWidth = 35;
    	var bHeight = 5;
    	var cls = "x-resizable-handle-south";
    	var addCls = "wm-resize-lane-south";
    	if (this.object.getLaneHandler().isVertical()) {
			if(this.object.getLaneHandler().isRightBorderLane()) {
				return;
			}
    		itemId = 'lresv';
    		bWidth = 5;
    		bHeight = 35;
    		cls = "x-resizable-handle-east";
    		addCls = "wm-resize-lane-east";
    	} else {
        	if (this.object.getLaneHandler().isLowestLane()) {
        		return;
        	}
    	}

        var resButton = new Ext.Panel({
            itemId: itemId,
            border:false,
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId),
            width: bWidth,
            height: bHeight,
            baseCls : cls,
            bodyCssClass : addCls
        });
		this.addContextMenuButton(resButton);

        var menu = this;
        resButton.body.dom.onmousedown = function(event) {
            Util.stopEvent(event);
            menu.hide();
            menu.controller.mouseListener.startResizeFrame(menu.object.selectionFrame);

            return false;
        };

        this.contextButtons.push(resButton);
    },
    hideNodeRecommendations : function() {
         for (var i = 0; i < this.succButtons.length; i++)
                this.succButtons[i].hide();
    },
    updatePosition : function() {
        var edgeOldX;
        var edgeOldY;
        var diffx = 0;
        var diffy = 0;
        var bounds = this.object.getFrameBounds();

        for (var i = 0; i < this.contextButtons.length; i++) {
            var button = this.contextButtons[i];
            if (button.getItemId() == 'edge') {
                edgeOldX = button.getPosition(true)[0];
                edgeOldY = button.getPosition(true)[1];
            }

            button.setPosition(bounds.x + this.getXOffset(button.getItemId(), button.initialConfig.iconOffset),
                               bounds.y + this.getYOffset(button.getItemId(), button.initialConfig.iconOffset));

            if (button.getItemId() == 'edge') {
                diffx = button.getPosition(true)[0] - edgeOldX;
                diffy = button.getPosition(true)[1] - edgeOldY;
            }
        }

        for (var i = 0; i < this.succButtons.length; i++) {
            var button = this.succButtons[i];
            var x = button.getPosition(true)[0];
            var y = button.getPosition(true)[1];

            button.setPosition(x + diffx, y + diffy);
        }
    }
});