Ext.namespace("Inubit.WebModeler");

/**
* This class represents nodes that can be edited by the user during the
* process of modeling. These nodes also can be moved around and linked to
* other nodes using edges.
*/
Inubit.WebModeler.EditableProcessNode = Ext.extend(Inubit.WebModeler.ProcessNode, {
    constructor : function (config) {
        this.dragStartX = 0;
        this.dragStartY = 0;
        this.inPlaceEditor = null;
        this.contextMenu = null;

        this.attachments = new Array();
        this.commentCount = 0;

        Inubit.WebModeler.EditableProcessNode.superclass.constructor.call(this, config);
        this.setPicture(config.uri+".png");
    },
    drawCommentHighlight : function(canvas) {
        if (ProcessEditor.instance.allowComments) {
            Inubit.WebModeler.EditableProcessNode.superclass.drawCommentHighlight.call(this, canvas);
        }
    },
    animate : function( attr ) {
        if (this.graphics)
            this.graphics.animate( attr , 1000 );

        if (this.commentSign) {
            var bounds = this.getFrameBounds();
            var x = bounds.x - 5;
            var y = bounds.y - 5;

            this.commentSign.animate({x : x, y : y}, 1000);
        }
    },

    bindEventsToNode : function() {
        var processNode = this;

        //handle mousedown event
        this.graphics.node.onmousedown = function(event) {
            //stop event forwarding
            ProcessEditor.instance.mouseListener.decideOnMouseDown(event);

            //first check for edge selection before proceeding with node selection
            ProcessEditor.instance.mouseListener.updateMousePos(event);
            var mouseX = ProcessEditor.instance.mouseListener.mouseX;
            var mouseY = ProcessEditor.instance.mouseListener.mouseY;

            var edge = processNode.model.getEdgeCloseTo(mouseX, mouseY);

            if (edge) {
                if ( event.ctrlKey ) {
                    edge.handleMultiSelectionAttempt();
                } else
                    ProcessEditor.instance.mouseListener.mouseDownOnEdge(edge);
                return false;
            }

            if (event.button == 0 || event.button == 1) {
                if (event.shiftKey) {
                    //on shift key start edge creation
                    processNode.setSelected(true, false, Util.COLOR_GREEN);
                    ProcessEditor.instance.mouseListener.currentEdgeSource = processNode;
                } else if ( event.ctrlKey ) {
                    var node = processNode;

                    if ( node.isSelected ) {
                        node.setSelected( false );
                        ProcessEditor.instance.selectionHandler.removeNodeFromSelection(node);
                    } else {
                        node.setSelected(true, false);
                        ProcessEditor.instance.selectionHandler.addSelectedNode(node);
                    }
                } else {
                    //else, handle selection
                    if ( processNode.isCluster() ) {
                        if ( !processNode.isSelectableAtPoint(mouseX, mouseY) ) {
                            ProcessEditor.instance.mouseListener.startRubberBand();
                            return false;
                        }
                    }

                    if (processNode.isBPMNPool()) {
                        //if mousedown on pool --> check if lane is hit
                        var newNode = processNode.model.getClusterAtPosition(mouseX, mouseY, null);

                        //if mousedown on lane --> select lane instead of surrounding pool
                        if (newNode && newNode.isBPMNLane()) {
                            newNode.getLaneHandler().handleSelection();
                            return false;
                        }
                    }
                    ProcessEditor.instance.mouseListener.enableDragging();
                    ProcessEditor.instance.mouseListener.currentlyDraggedNode = processNode;

                    //if node is already selected in a multiselection --> return
                    if (ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(processNode))
                        return false;

                    processNode.select();
                    ProcessEditor.instance.mouseListener.mouseDownOnNode(event, processNode);
                }
            }
            //make Firefox move node, not the image it contains
            return false;
        };
        //handle mouseover
        this.graphics.node.onmouseover = function(event) {
            //if edge creation is in progress
            if (ProcessEditor.instance.mouseListener.currentEdgeSource &&
                    ( !ProcessEditor.instance.mouseListener.currentTargetEdge || processNode.isUMLClass() || processNode.isDomainClass() ) ) {
                processNode.setSelected(true, false, Util.COLOR_GREEN);
            }

            if ( ProcessEditor.instance.mouseListener.currentEdgeSource && ProcessEditor.instance.mouseListener.currentTargetEdge ) {
                processNode.setSelected(false);
            }

            //if context menu is shown, hide node recommendations
            if (processNode.contextMenu)
                processNode.contextMenu.hideNodeRecommendations();
        };
        //handle mouseout
        this.graphics.node.onmouseout = function(event) {
            //deselect while edge creation is in progress
            if ((ProcessEditor.instance.mouseListener.currentEdgeSource != null &&
                    ProcessEditor.instance.mouseListener.currentEdgeSource != processNode))

                processNode.setSelected(false);
        };
        //handle mouseup
        this.graphics.node.onmouseup = function(event) {
            var edgeSource = ProcessEditor.instance.mouseListener.currentEdgeSource;
            //if edge should be created, create one with this node as target
            if (edgeSource != null && (edgeSource != processNode || processNode.isUMLClass() || processNode.isDomainClass()) &&
                    ProcessEditor.instance.mouseListener.currentTargetEdge == null ) {
                ProcessEditor.instance.getModel().createEdge(edgeSource, processNode);
                processNode.setSelected(false);
            }
        };

        this.graphics.node.onclick = function( event ) {
            return !( event.shiftKey || event.ctrlKey );
        };

        //handle double click
        this.graphics.node.ondblclick = function( event ) {
            //open in place editor
            processNode.select();
            processNode.showInPlaceEditor();
        };
    },
    /**
     * Open an in-place editor for this node
     */
    createInPlaceEditor : function() {
        this.inPlaceEditor = new Ext.form.TextField( {
            width: 100,
            hidden: true,
            enableKeyEvents: true
        });

        Ext.getCmp('editor').add( this.inPlaceEditor );

        this.inPlaceEditor.on("keydown", function(field, event) {
            if ( event.getKey() == Ext.EventObject.ENTER ) {
                var text = field.getValue();
                this.updateProperty("text", Util.escapeString(text));
                this.hideInPlaceEditor();
            }

            if ( event.getKey() == Ext.EventObject.ESC ) {
                this.hideInPlaceEditor();
            }

            if ( event.getKey() == Ext.EventObject.DELETE ) {
                Util.stopEvent( event );
            }
        }, this);

        this.inPlaceEditor.on("keyup", function(field, event) {
            if ( event.getKey() == Ext.EventObject.DELETE ) {
                Util.stopEvent( event );
            }
        }, this);
    },
    showInPlaceEditor : function() {
    	if (this.isDomainClass()) {
        	this.inPlaceEditor.setPosition( this.x - 46.5, this.y - (this.height / 2));
    	} else  {
    		this.inPlaceEditor.setPosition( this.x - 46.5, this.y - 6);
    	}
        this.inPlaceEditor.setValue( this.properties.text );
        this.inPlaceEditor.setVisible(true);

        this.inPlaceEditor.el.fadeIn({duration: Util.ANIMATION_FADE_IN_TIME, concurrent: true});

        this.inPlaceEditor.focus(true);
    },
	hideInPlaceEditor : function() {
        if ( this.inPlaceEditor ) {
            if ( this.inPlaceEditor.el )
                this.inPlaceEditor.el.fadeOut({duration: Util.ANIMATION_FADE_OUT_TIME, remove: false, concurrent: true});
            this.inPlaceEditor.setVisible(false);
        }
    },
    /**
    * Remove (e.g destroy) this node
    */
    remove : function() {
        if ( this.isBPMNLane() ) {
            var pool = this.getLaneHandler().getSurroundingPool();
            pool.removeLane( this );
            return;
        }

        //remove node at server
        var delReq = new XMLHttpRequest();
        delReq.open("DELETE", this.uri, true);
        delReq.send(null);

        this.removeNodeInBrowser();

        if ( ProcessEditor.instance.undoHandler.isUndoInProgress() )
            ProcessEditor.instance.undoHandler.addRedoAction(
                this.model, this.model.createNodeFromSerialization, [ this.serialize(), this.cluster ]
            );
        else
            ProcessEditor.instance.undoHandler.addUndoAction(
                this.model, this.model.createNodeFromSerialization, [ this.serialize(), this.cluster, true ]
            );
    },
    removeNodeInBrowser : function() {
        this.setSelected(false);

        //remove graphical representation
        if (this.graphics) {
            this.graphics.remove();
        }
        //remove this node from its cluster (if exists)
        if (this.cluster) {
            this.cluster.removeChild(this);
        }
        //remove node from model
        this.model.removeNode(this);
        this.dropCommentHighlight();

        while ( this.hintGraphics.length > 0 ) 
            this.hintGraphics.pop().remove();

        //if a lane is deleted --> do special handling
        if (this.isBPMNLane()) {
            this.getLaneHandler().handleDeletion();
            return;
        }

        //remove all childs of this node
        while (this.children.length > 0) {
            var child = this.children.pop();
            child.remove();
        }

        //remove all edges that start/end with this node
        while (this.edges.length > 0) {
            var edge = this.edges.pop();
            edge.removeFromEditor();
        }
    },
    /**
     * Selects or deselects the node.
     *
     * @param {boolean} selected
     * @param {boolean} showButtons
     * @param {} color
     */
    setSelected : function(selected, showButtons, color) {
        this.setNodeSelected(selected, showButtons, color, ProcessEditor.instance);

        if ( selected ) {
            this.dragStartX = this.x;
            this.dragStartY = this.y;
        }

        if ( selected == false && this.inPlaceEditor ) {
            this.hideInPlaceEditor();
        }
    },

    /**
    * Create a context menu for this node
    *
    * @return{NodeContextMenu} the context menu
    */
    createContextMenu : function() {
        return new Inubit.WebModeler.NodeContextMenu({object: this, controller : ProcessEditor.instance});
    },
    /**
     * Move this node to a given point
     * @param{int} x x-coordinate
     * @param{int} y y-coordinate
     **/
    moveTo : function(x, y) {
        var diffx = x - this.x;
        var diffy = y - this.y;

        if ( (!this.isAncestorMoved() && !this.isBPMNLane()) ||
                (this.isAncestorMoved() && this.cluster != null && this.cluster.isBPMNLane()) ||
                (this.isAncestorMoved() && this.cluster != null && this.cluster.isBPMNPool() && !this.isBPMNLane()) ||
                 ProcessEditor.instance.undoHandler.isUndoInProgress() ||
                   ProcessEditor.instance.undoHandler.isRedoInProgress() )
                this.moveBy(diffx, diffy);

        //redraw edges that start/end with this node
        for (var i = 0; i < this.edges.length; i++) {
            if ( !ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.edges[i]) ||
                 !ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.edges[i].source) ||
                 !ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.edges[i].target) )
                        this.edges[i].redraw(ProcessEditor.instance.getCanvas());
        }
    },
    jumpTo : function( x, y ) {
        ProcessEditor.instance.undoHandler.addAction( this, this.jumpTo, [ this.x, this.y ] );
        this.moveTo( x, y);
        this.updatePositionAtServer(true);
    },
    /**
    * Move this node by a certain number of pixels in x- and y-direction
    * @param{int} diffx number of pixels in x-direction
    * @param{int} diffy number of pixels in y-direction
    */
    moveBy : function(diffx, diffy) {
        //update all edges starting/ending with this node

        for (var i = 0; i < this.edges.length; i++) {
            this.edges[i].moveConnectionPoint(this, diffx, diffy);

            if ( !this.edges[i].source.isAncestorMoved() && !ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.edges[i]))
                this.edges[i].redraw( ProcessEditor.instance.getCanvas() );
        }
        //move child nodes
        for (var i = 0; i < this.children.length; i++) {
            if (!ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.children[i]))
                if (this.children[i].getAttachedTo() == null)
                    this.children[i].moveBy(diffx, diffy);
        }

        for (var i = 0; i < this.attachments.length; i++) {
            if (!ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.attachments[i])) {
                this.attachments[i].moveBy(diffx, diffy);
            }
        }

        //move edges that are contained in this node
        for (var i = 0; i < this.clusterEdges.length; i++) {
            if (!ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.clusterEdges[i]))
                this.clusterEdges[i].moveBy(diffx, diffy);
        }
        //redraw all cross cluster edges
        for (var i = 0; i < this.crossClusterEdges.length; i++) {
            if (!ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this.crossClusterEdges[i]))
                this.crossClusterEdges[i].redraw(ProcessEditor.instance.getCanvas());
        }
        //move node graphically
        if (this.graphics)
            this.graphics.translate(diffx, diffy);
        //move selection frame
        if (this.selectionFrame)
            this.selectionFrame.moveBy(diffx, diffy);
        if (this.commentSign)
            this.commentSign.translate(diffx, diffy);

        for ( var i = 0; i < this.hintGraphics.length; i++ )
            this.hintGraphics[i].translate(diffx, diffy);

        //update position data
        this.setPos(this.x + diffx, this.y + diffy);
        //update context menu positioning
        if (this.contextMenu)
            this.contextMenu.updatePosition();
    },
    /**
     * Signal that this node has finished moving
     */
    movementFinished : function() {
        //update position at server side
        if ( this.dragStartX != this.x || this.dragStartY != this.y ) {
            ProcessEditor.instance.undoHandler.addAction( this, this.jumpTo, [ this.dragStartX, this.dragStartY ] );
            this.updatePositionAtServer(true);

            if (this.attachments)
                for (var i = 0; i < this.attachments.length; i++)
                    this.attachments[i].updatePositionAtServer(true);
        }
    },
    handleResize : function(diffx , diffy) {
        //if the resize would lead to negative width or height, do nothing
        if (diffx < -this.width || diffy < -this.height) {
            this.setSelected(true);
            return false;
        }
        //depending on the lanes alignment only height OR width is updateable --> the other value is determined by surrounding pool/lane
        if (this.isBPMNLane()) {
        	if(this.getLaneHandler().isVertical()) {
        		 this.updateProperty("width", "" + (this.width + diffx));
        	} else {
            	this.updateProperty("height", "" + (this.height + diffy));
        	}
        }else {
            ProcessEditor.instance.undoHandler.addAction( this, this.handleResize, [ -diffx, -diffy ]);
            //get new centre point coordinates
            var x = this.x + Math.floor(diffx/2);
            var y = this.y + Math.floor(diffy/2);

            this.setPos(x, y);
            this.setSize(this.width + diffx, this.height + diffy);

            //update size at server
            var putReq = new XMLHttpRequest();
            putReq.open("PUT", this.uri, true);

            var node = this;
            putReq.onreadystatechange = function() {
                if (putReq.readyState == 4 && putReq.status == 200) {
                     var xml = putReq.responseXML;
                     node.applyPropertyUpdateResponse(xml);
                     //on pool resize, update child positions
                     if (node.isBPMNPool()) {
                         node.updateLaneBounds(xml);
                     }
                 }
            };

            putReq.send(this.createSizeXML());
        }
    },
     /**
     * Set this node's picture URI
     * Temporary nodes undergo several changes due to refactorings and other
     * property changes. Therefore we always need a new (uncached) image.
     */
    setPicture : function(uri) {
            this.picture = Util.getPath(uri) + "?time=" + new Date();
    },
     /**
     * Remove cross cluster edge from this node
     * @param{ProcessEdge} edge the edge
     */
    removeClusterEdge : function(edge) {
        var newClusterEdges = new Array();
        for (var i = 0; i < this.clusterEdges.length; i++) {
            if (this.clusterEdges[i] == edge) continue;

            newClusterEdges.push(this.clusterEdges[i]);
        }

        this.clusterEdges = newClusterEdges;
    },
    /**
     * Remove an edge that ends/starts with this node
     * @param{ProcessEdge} edge the edge
     */
    removeEdge : function(edge) {
        var newEdges = new Array();
        for (var i = 0; i < this.edges.length; i++) {
            if (this.edges[i] == edge) continue;

            newEdges.push(this.edges[i]);
        }
        //remove this edge as a cross cluster edge(if it is one)
        this.removeCrossClusterEdge(edge);

        this.edges = newEdges;
    },
    /**
     * Attach this node to another node
     * @param{ProcessNode} parentNode the node to attach to
     */
    attachToNode : function(parentNode) {
        //if this is a new attachment
        if (this.attachedTo != parentNode.getId()) {
            //change this node's cluster
            var oldParent = this.model.getNodeWithId( this.attachedTo );

            if (oldParent) {
                var newAttachments = new Array();
                for ( var i = 0; i < oldParent.attachments.length; i++)
                    if ( oldParent.attachments[i] != this )
                        newAttachments.push( oldParent.attachments[i]);
                oldParent.attachments = newAttachments;
            }

            if ( this.cluster )
                this.cluster.removeChild( this );

            parentNode.attachments.push( this );

            this.attachedTo = parentNode.getId();

            this.setCluster(parentNode.getCluster());

            if ( this.cluster )
                this.cluster.addChild( this );

            //update attachment at server
            var putReq = new XMLHttpRequest();
            putReq.open("PUT", this.uri, true);
            putReq.send(this.createAttachmentXML(parentNode));
        }
    },
    /**
     * Detach this node from its parent.
     */
    detach : function() {
    	if (this.attachedTo != null) {
                var oldParent = this.model.getNodeWithId( this.attachedTo );

                if (oldParent) {
                    var newAttachments = new Array();
                    for ( var i = 0; i < oldParent.attachments.length; i++)
                        if ( oldParent.attachments[i] != this )
                            newAttachments.push( oldParent.attachments[i]);
                    oldParent.attachments = newAttachments;
                }

    		this.attachedTo = null;
	        var putReq = new XMLHttpRequest();
	        putReq.open("PUT", this.uri, true);
	        putReq.send(this.createDetachmentXML());
    	}
    },
       /**
     * Replace a child with a new node
     * @param{ProcessNode} oldChild the node to be replaced
     * @param{ProcessNode} newChild the new node
     */
    replaceChild : function(oldChild, newChild) {
       this.removeChild(oldChild);

       this.children.push(newChild);
    },
    /**
     * Remove a child node from this node
     * @param{ProcessNode} child the child to be removed
     */
    removeChild : function(child) {
        var newChildren = new Array();
        for (var i = 0; i < this.children.length; i++) {
            if (this.children[i] == child) continue;

            newChildren.push(this.children[i]);
        }

        this.children = newChildren;
    },
    /**
     * Update this node's cluster
     * @param{ProcessNode} newCluster the new cluster
     * @param{Boolean} synch determine if update request should be synchronous
     */
    updateCluster : function(newCluster, synch) {
        var oldCluster = null;

        if (this.cluster) {
            this.cluster.removeChild(this);
            oldCluster = this.getCluster();
        }
        if (newCluster) {
            newCluster.addChild(this);
        }

        for ( var i = 0; i < this.attachments.length; i++ ) {
            this.attachments[i].updateCluster(newCluster);
        }

        this.setCluster(newCluster);

        //if cluster has changed -> update at server
        if (newCluster != oldCluster) {
            this.updateClusterAtServer(synch);

            //redefine cluster for all edges
            for (var i = 0; i < this.edges.length; i++) {
                if (oldCluster)
                    oldCluster.removeClusterEdge(this.edges[i]);

                this.edges[i].target.removeCrossClusterEdge(this.edges[i]);
                this.edges[i].source.removeCrossClusterEdge(this.edges[i]);
                this.edges[i].defineCluster();
            }
        }
    },
    removeCrossClusterEdge : function(edge) {
        var newCrossEdges = new Array();

        for (var i = 0; i < this.crossClusterEdges.length; i++) {
            if (this.crossClusterEdges[i] == edge) {
                continue;
            }

            newCrossEdges.push(this.crossClusterEdges[i]);
        }

        this.crossClusterEdges = newCrossEdges;
    },
    isAttached : function() {
    	return this.attachedTo != null;
    },
    isAncestorMoved : function() {
        if (this.cluster) {
            if (ProcessEditor.instance.selectionHandler.isSelected(this.cluster))
                return true;
            else
                return this.cluster.isAncestorMoved();
        }

        return false;
    },
	isSelectableAtPoint : function( x, y ) {

        if ( this.y - this.height / 2 <= y && this.y + this.height / 2 >= y ) {
            if (   //check if close to left border
                this.x - this.width / 2 <= x && this.x - this.width / 2 + Inubit.WebModeler.ProcessNode.CLUSTER_SELECTION_RIM >= x ||
                //check if close to right border
                this.x + this.imageWidth / 2 + 3 >= x && this.x + this.imageWidth / 2 + 3 - Inubit.WebModeler.ProcessNode.CLUSTER_SELECTION_RIM <= x
            )
                return true;
        }

        if ( this.x - this.width / 2 <= x && this.x + this.width / 2 >= x ) {
            if (   //check if close to top border
                this.y - this.height / 2 <= y && this.y - this.height / 2 + Inubit.WebModeler.ProcessNode.CLUSTER_SELECTION_RIM >= y ||
                //check if close to bottom border
                this.y + this.imageHeight / 2 + 3>= y && this.y + this.imageHeight / 2 + 3- Inubit.WebModeler.ProcessNode.CLUSTER_SELECTION_RIM <= y
            )
                return true;
        }

        if ( ( this.isBPMNPool() || this.isBPMNLane() )&& this.children.length > 0 ) {
            for ( var i = 0; i < this.children.length; i++ )
                if ( this.children[i].isBPMNLane() && this.children[i].isSelectableAtPoint(x, y) )
                    return true;
        }
        return false;
    },
//      /**
//     * @returns true if the process node contains an attribute with the given name false if not.
//     */
//    containsAttributeWithName : function (name) {
//    	var atts = this.getAttributes();
//    	for (var i = 0; i < atts.length; i++) {
//    		if (atts[i].name == name) {
//    			return true;
//    		}
//    	}
//    	return false;
//    },
//    getAttributes : function() {
//        var attributes = new Array();
//
//        var attString = this.properties["#attributes"];
//        if ( attString == null )
//            attString = this.properties.attributes;
//
//        var atts = attString.split(";");
//
//        for ( var i = 0; i < atts.length; i++ ) {
//            var attribute = atts[i];
//            if( attribute.length > 0 ) {
//                var open = attribute.indexOf('[');
//                var defOpen = attribute.lastIndexOf('(');
//                var sep = attribute.indexOf(':');
//
//                if ( sep < 0 )
//                    sep = attribute.length - 1;
//
//                if ( defOpen < 0 || defOpen < sep)
//                    defOpen = attribute.length;
//
//                var attName = ((open<0) ? attribute.substring(1,sep) : attribute.substring(1, open));
//                var attType = sep < attribute.length ? attribute.substring( sep + 1, defOpen ) : "";
//
//                var multi = ((open>0) ? attribute.substring(open+1, attribute.indexOf(']')) : "1");
//
//                var vis = attribute[0];
//
//                if ( vis == null || vis == "~")
//                    vis = "PACKAGE";
//                else if ( vis == "+" )
//                    vis = "PUBLIC";
//                else if ( vis == "-")
//                    vis = "PRIVATE";
//                else if ( vis == "#")
//                    vis = "PROTECTED";
//
//                var def = "";
//
//                if ( defOpen > -1 && defOpen < attribute.length ) {
//                    var close = attribute.indexOf(')', defOpen);
//                    if ( close > -1 )
//                        def = attribute.substring( defOpen + 1, close );
//                }
//
//                attributes.push( {name : attName, type : attType, multiplicity : multi, visibility : vis, defaultValue: def} );
//            }
//        }
//        return attributes;
//    },
//	getAttributeAttributes : function() {
//        var atts = [
//            'name',
//            'type',
//            'multiplicity',
//            'visibility'
//        ];
//
//        if ( this.isDomainClass() )
//            atts.push("defaultValue");
//
//        return atts;
//    },
//    addAttribute : function( attConfig ) {
//        this.attributes.push( attConfig );
//    },
	getRecursiveChildNodes : function() {
        var recChildren = new Array();

        for (var i = 0; i < this.children.length; i++) {
            recChildren.push(this.children[i]);
            if (this.children[i].isBPMNLane()) {
                recChildren = recChildren.concat(this.children[i].getRecursiveChildNodes());
            }
        }

        return recChildren;
    },
	updateLane : function(laneId, key, value) {
        var node = this;
        var putReq = new XMLHttpRequest();
        putReq.open("PUT", this.uri, true);
        putReq.onreadystatechange = function() {
             if (putReq.readyState == 4 && putReq.status == 200) {
                 var xml = putReq.responseXML;
                 node.applyPoolUpdateResponse(xml);
             }
         };

         putReq.send(this.createLaneUpdateXML(laneId, key, value));
    },
    addLane : function(cluster, lane) {
        var node = this;
        var putReq = new XMLHttpRequest();
        putReq.open("PUT", this.uri, true);
        putReq.onreadystatechange = function() {
             if (putReq.readyState == 4 && putReq.status == 200) {
                 var xml = putReq.responseXML;
                 node.applyPoolUpdateResponse(xml);

                 lane.setCluster(cluster);

                 if (cluster.children.length != 0 && !cluster.children[0].isBPMNLane()) {
                     while ( cluster.children.length > 0 ) {
                         cluster.children.pop().updateCluster(lane);
                     }
                 }

                 cluster.addChild(lane);

                 lane.getLaneHandler().handleSelection();
             }
         };

         putReq.send(this.createLaneAdditionXML(cluster, lane));
    },
	removeLane : function( lane ) {
        var delReq = new XMLHttpRequest();
        delReq.open("DELETE", lane.uri, true);

        var pool = this;
        delReq.onreadystatechange = function() {
            if (delReq.readyState == 4 && delReq.status == 200) {
                var xml = delReq.responseXML;

                if ( xml && xml.getElementsByTagName("imageuri")[0]) {
                    pool.applyPoolUpdateResponse(xml);

                    //remove this node from its cluster (if exists)
                    if (lane.cluster) {
                        lane.cluster.removeChild(lane);
                    }
                }
                //remove node from model
                lane.model.removeNode(lane);
                lane.dropCommentHighlight();

                if ( xml && xml.getElementsByTagName("imageuri")[0]) {
                    lane.getLaneHandler().handleDeletion();
                }
            }
        };

        delReq.send( null );
    },
    updateProperty : function(propertyName, value) {
        var oldValue = this.getProperty( propertyName );

        if ( oldValue == Util.unEscapeString(value) )
            return;

        ProcessEditor.instance.undoHandler.addAction( this.model, this.model.updateNodeProperty, [ this.getId(), name, oldValue ]);

        if (this.isBPMNLane()) {
             this.getLaneHandler().updateProperty( propertyName, value);
        } else if (this.isBPMNPool()) {
        	// redraw the
        	if (propertyName == 'vertical_Pool') {
        		this.updatePoolRotateProperty(propertyName, value);
        	} else {
        		Inubit.WebModeler.EditableProcessNode.superclass.updateProperty.call( this, propertyName, value );
      		}
        } else {
            Inubit.WebModeler.EditableProcessNode.superclass.updateProperty.call( this, propertyName, value );
        }
    },
	applyPropertyUpdateResponse : function(xml) {
         var newUri = xml.getElementsByTagName("imageuri")[0].getAttribute("value");

         this.setPicture(newUri + ".png");
         this.parseBounds(xml.getElementsByTagName("bounds")[0]);
         this.applyMetadataUpdateResponse(xml.getElementsByTagName("metadata")[0]);

         this.redraw(ProcessEditor.instance.getCanvas(), true);

         this.model.applyEdgeRoutings(xml.getElementsByTagName("edges")[0]);
    },
	applyPoolUpdateResponse : function(xml) {
         var newUri = xml.getElementsByTagName("imageuri")[0].getAttribute("value");

         this.setPicture(newUri + ".png");
         this.parseBounds(xml.getElementsByTagName("bounds")[0]);

         this.updateLaneBounds(xml);

         this.redraw(ProcessEditor.instance.getCanvas(), true);
    },
	updateLaneBounds : function( xml ) {
        var laneEls = xml.getElementsByTagName("lanes")[0].getElementsByTagName("lane");

         for ( var i = 0; i < laneEls.length; i++ ) {
             var lId = laneEls[i].getAttribute("id");
             this.model.getNodeWithId(lId).parseBounds( laneEls[i] );
         }
    },
     applyTypeUpdateResponse : function(xml, newType, isUndoInProgress ) {

        if ( isUndoInProgress )
            ProcessEditor.instance.undoHandler.addRedoAction( this, this.updateType, [ this.getType() ]);
        else
            ProcessEditor.instance.undoHandler.addUndoAction( this, this.updateType, [ this.getType(), true ]);
        this.properties[Inubit.WebModeler.ProcessObject.PROPERTY_TYPE] = newType;
        this.setPicture(this.uri + ".png");

        if (this.graphics) {
            this.graphics.remove();
            this.graphics = null;
        }

        this.contextMenu.applyRefactoring();
        this.clearListeners();
        this.load(ProcessEditor.instance.getCanvas(), true);
    },
    getCommentURI : function() {
        return this.uri + "/comments?version=" + this.model.version + "&baseId=" + this.model.baseId;
    },
	updatePositionAtServer : function() {
        var xml = this.createPositionUpdateXML();

        var putReq = new XMLHttpRequest();
        putReq.open("PUT", this.uri, true);
        var node = this;

        putReq.onreadystatechange = function() {
            if (putReq.status == 200 && putReq.readyState == 4) {
                node.model.applyEdgeRoutings( putReq.responseXML );
            }
        };

        putReq.send(xml);
    },
    updateBoundsFromServer : function(propagate) {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, true);

        var node = this;
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var props = Util.parseProperties(req.responseXML);
                var x = parseInt(props.x);
                var y = parseInt(props.y);
                var w = parseInt(props.width);
                var h = parseInt(props.height);

                node.setPos(x, y);
                node.setSize(w, h);

                if (node.isBPMNLane())
                    node.setImageSize(w, h);

                node.redraw(ProcessEditor.instance.getCanvas(), false);
            }
        };

        if(propagate) {
            for (var i = 0; i < this.children.length; i++)
                this.children[i].updateBoundsFromServer(propagate);

            for (var i = 0; i < this.edges.length; i++) {
                this.edges[i].updateRouting();
            }
        }

        req.send(null);
    },
    updateClusterAtServer : function( synch ) {
        if ( synch == null ) synch = false;
        var xml = this.createClusterUpdateXML();

        var putReq = new XMLHttpRequest();
        putReq.open("PUT", this.uri, !synch);
        putReq.send(xml);
    },
    /**
     * XML CREATORS
     */
    serialize : function() {
        var xml = "<node>";
        xml += Inubit.WebModeler.EditableProcessNode.superclass.serialize.call(this);
        xml += "</node>";

        return xml;
    },
    createSizeXML : function() {
        var xml = "<update type='resize'>";
        xml += "    <property name='width' value='"+ this.width +"'/>";
        xml += "    <property name='height' value='"+ this.height +"'/>";
        xml += "    <property name='x' value='"+ this.x +"'/>";
        xml += "    <property name='y' value='"+ this.y +"'/>";
        xml += "</update>";

        return xml;
    },
    createClusterUpdateXML : function() {
        var xml = "<update type='cluster'>";
        xml += "<new value='" ;
        if (this.cluster)
            xml += this.cluster.getId() ;
        else
            xml += null;

        xml += "'/>";
        xml += "</update>";

        return xml;
    },
    createPositionUpdateXML : function() {
        var xml = "<update type='position' id='" + this.getId() + "'>";
        xml += "<property name='x' value='" + this.x + "'/>";
        xml += "<property name='y' value='" + this.y + "'/>";
        xml += "</update>";

        return xml;
    },
    createAttachmentXML : function(node) {
        var xml = "<update type='attach'>";
        xml += "<property name='target' value='" + node.getId() + "'/>";
        xml += "</update>";

        return xml;
    },
    createDetachmentXML : function(node) {
        var xml = "<update type='detach'/>";

        return xml;
    },
    createLaneUpdateXML : function(laneId, key, value) {
        var xml = "<update type='lane'>";
        xml += "<property name='laneId' value='" + laneId + "'/>";
        xml += "<property name='key' value='" + key + "'/>";
        xml += "<property name='value' value='" + value + "'/>";
        xml += "</update>";

        return xml;
    },
    setNodeSelected : Inubit.WebModeler.ProcessNode.prototype.setSelected,
    createLaneAdditionXML : function(cluster, lane) {
        var xml = "<update type='addLane'>";
        xml += "<property name='clusterId' value='" + cluster.getId() + "'/>";
        xml += "<property name='laneId' value='" + lane.getId() + "'/>";
        xml += "</update>";

        return xml;
    },

    init : function(xml, canvas) {
        this.attributes = new Array();

        //ignore properties thar are located underneath an attribute node
        var attsEl = xml.getElementsByTagName("attributes")[0];
        if ( attsEl ) {
            xml.childNodes[0].removeChild( attsEl );
        }

        var properties = Util.parseProperties(xml);
        this.properties = properties;
        this.parseBounds(xml);

        ProcessEditor.instance.propertyConfig.registerClass( this.getType() , true);

        this.updateMetadata();
        this.paint(canvas);
        this.alignZOrder();
        this.createInPlaceEditor();
//        this.loadFinished();
        this.fireEvent("load");
    },
//    loadFinished : function() {
//        return false;
//    },
    updatePoolRotateProperty : function (name, value) {
    	this.setProperty(name, Util.unEscapeString(value));
    	var object = this;
    	Ext.Ajax.request({
    		method: 'PUT',
    		url: this.uri,
    		xmlData:this.createUpdateXML(name, value),
    		success: function(result, request) {
    			var xml = result.responseXML;
                object.applyPropertyUpdateResponse(xml);
                object.updateBoundsFromServer(true);
                if (Ext.isDefined(object.children)) {
                	for (var i = 0; i < object.children.length; i++) {
                		if (object.children[i].isBPMNLane()) {
                			object.children[i].refreshContextMenu();
                		}
                	}
                }
    		}
    	});
    }


});
