function ProcessEditorMouseListener(editor) {
    var editorID = 'editor';
    this.editor = editor;
    this.alignmentHandler = new AlignmentHandler();

    this.mouseStatus = 0;
    this.lastMouseX = 0;
    this.lastMouseY = 0;
    this.mouseX = 0;
    this.mouseY = 0;
    this.mouseOffsetX = 0;
    this.mouseOffsetY = 0;

    this.diffToVerticalAlign = 0;
    this.diffToHorizontalAlign = 0;

    this.selTopX = 0;
    this.selTopY = 0;
    this.selBottomX = 0;
    this.selBottomY = 0;

    this.rubberBand = null;
    this.currentlyResizedFrame = null;
    this.currentEdgeSource = null;
    this.currentTargetEdge = null;
    this.currentlyDraggedNode = null;
    this.currentCluster = null;
    this.currentAttachment = null;
    this.currentEdgeResize = null;
    this.currentTextNode = null;

    this.frameDiffX = 0;
    this.frameDiffY = 0;

    this.updateMousePos = function(event) {
        var editorPos = Ext.getCmp(editorID).getPosition();
        var scrollPos = Ext.getCmp(editorID).body.getScroll()

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

        this.mouseX = x-editorPos[0]+scrollPos.left;
        this.mouseY = y-editorPos[1]+scrollPos.top;
    }

    this.mouseDownOnNode = function(event, node) {
        this.updateMousePos(event);
        this.mouseOffsetX = node.x-this.mouseX;
        this.mouseOffsetY = node.y-this.mouseY;

        var x = this.mouseX + this.mouseOffsetX;
        var y = this.mouseY + this.mouseOffsetY;

        //if single node is selected that is attached, highlight attachment
        if (node.isAttached()) {
        	var currentlyOver = this.editor.getModel().getNodeWithId(node.getAttachedTo());
        	
        	currentlyOver.setSelected(true, false, Util.COLOR_ORANGE);
        	
        	this.currentAttachment = currentlyOver;

                return;
        }

        this.alignmentHandler.updateAlignmentRulers(this.editor.getModel(), this.editor.selectionHandler.getSelectedNodes());
        this.alignmentHandler.drawRulers( this.editor.getCanvas() );

        if ( this.alignmentHandler.isVerticallyAligned() )
            this.diffToVerticalAlign = this.mouseX - this.alignmentHandler.verticalAlign;
        else
            this.diffToVerticalAlign = 0;

        if ( this.alignmentHandler.isHorizontallyAligned() )
            this.diffToHorizontalAlign = this.mouseY - this.alignmentHandler.horizontalAlign;
        else
            this.diffToHorizontalAlign = 0;

        //determine if a cluster has to be highlighted
        var cluster = this.editor.model.getDropableClusterAtPosition(x, y, node);

        if (this.currentlyDraggedNode.equalsOrContains(cluster))
                return false;

        if (cluster && (!(this.editor.selectionHandler.isPoolSelected()) || (!cluster.isBPMNLane() && !cluster.isBPMNPool()))) {
            if (!cluster.equalsOrContains(this.currentlyDraggedNode))
                cluster.setSelected(true, false, Util.COLOR_BLUE);

            if (this.currentCluster && this.currentCluster != cluster) {
                this.currentCluster.setSelected(false);
            }

            this.currentCluster = cluster;
        }
        return false;
    }

    this.mouseDownOnEdge = function(edge) {
        this.editor.selectionHandler.singleSelect(edge);
        edge.setSelected(true, true);
        this.editor.displayProperties(edge);

        if ( edge.isCloseToHorizontalFragment( this.mouseX, this.mouseY ) ) {
            this.currentEdgeResize = new EdgeSegmentMoveHandler(edge, this.mouseX, this.mouseY, EdgeSegmentMoveHandler.MODE_HORIZONTAL)
        } else if ( edge.isCloseToVerticalFragment( this.mouseX, this.mouseY ) ) {
            this.currentEdgeResize = new EdgeSegmentMoveHandler(edge, this.mouseX, this.mouseY, EdgeSegmentMoveHandler.MODE_VERTICAL)
        } 
    }

    this.mouseDownOnTextNode = function( textNode ) {
        this.currentTextNode = textNode;
    }

    this.mouseDown = function(event) {
        this.updateMousePos(event);
        var button;
        if (event.which)
            button = event.which;
        else
            button = event.button;

        //if context menu is requested, determine if an edge was hit
        if (button == 2 || button == 3) {
            this.editor.selectionHandler.dropSelection();
            return false;
        }

        //check if click is close to an edge
        var edge = this.editor.getModel().getEdgeCloseTo(this.mouseX, this.mouseY)
        if (edge) {
           this.mouseDownOnEdge(edge);
        //else drop selection and start new rubber band selection
        } else {
            this.startRubberBand();
        }

        return false;
    }

    this.mouseMove = function(event){
        this.updateMousePos(event);
        if (this.currentlyResizedFrame) {
            this.frameDiffX += this.mouseX - this.lastMouseX;
            this.frameDiffY += this.mouseY - this.lastMouseY;

            this.currentlyResizedFrame.resize(this.frameDiffX, this.frameDiffY)
            this.editor.canvas.nodeReaches( this.mouseX + 30, this.mouseY + 30, true );
            this.editor.canvas.nodeReaches( this.mouseX - 30, this.mouseY - 30, true );

            return false;
        }

        if (this.currentEdgeSource) {
            var tEdge = this.editor.getModel().getEdgeCloseTo(this.mouseX, this.mouseY);
            if (tEdge) {
                if (tEdge != this.currentTargetEdge) {
                    if (this.currentTargetEdge)
                        this.currentTargetEdge.setSelected(false);

                    tEdge.setSelected(true, false, Util.COLOR_GREEN);
                    this.currentTargetEdge = tEdge;
                }
            } else {
                if (this.currentTargetEdge) {
                    this.currentTargetEdge.setSelected(false);
                    this.currentTargetEdge = null;
                }
            }

            return false;
        }

        if ( this.currentTextNode ) {
            this.currentTextNode.moveOnEdge( this.mouseX, this.mouseY );
        }

        if (this.mouseStatus == 1) {
            var selectedNodes = this.editor.selectionHandler.getSelectedNodes();

            if ( this.alignmentHandler.isVerticallyAligned() )
                if ( Math.abs( this.mouseX - ( this.alignmentHandler.verticalAlign + this.diffToVerticalAlign )) <= 3 )
                    this.mouseX = this.alignmentHandler.verticalAlign + this.diffToVerticalAlign;

            if ( this.alignmentHandler.isHorizontallyAligned() )
                if ( Math.abs( this.mouseY - ( this.alignmentHandler.horizontalAlign + this.diffToHorizontalAlign )) <= 3 )
                    this.mouseY = this.alignmentHandler.horizontalAlign + this.diffToHorizontalAlign;

            var diffx = this.mouseX - this.lastMouseX;
            var diffy = this.mouseY - this.lastMouseY;
            var selectedEdges =  this.editor.selectionHandler.getSelectedEdges();
            if (selectedNodes.length > 0) {
            	// check if one of the nodes crosses the upper or left border
            	var xBorderCrossed = false;
            	var yBorderCrossed = false;
            	for (var i = 0; i < selectedNodes.length; i++) {
            		var node = selectedNodes[i];
            		var xVal = node.x;
            		var yVal = node.y
            		if(Ext.isDefined(node.getFrameBounds)) {
            			var xVal = node.getFrameBounds().x;
                		var yVal = node.getFrameBounds().y;
            		}
            		if (this.checkBorderReached(xVal, diffx, 25)) {
            			xBorderCrossed = true;
            		}
            		if (this.checkBorderReached(yVal, diffy, 25)) {
            			yBorderCrossed = true;
            		}
            	}
            	// check if one of the edge routing points crosses the upper or left border
                if (selectedEdges.length > 0) {
                	for (var i = 0; i < selectedEdges.length; i++) {
                		var edge = selectedEdges[i];
                		var xVal = Math.min(edge.topX, edge.bottomX);
                		var yVal = Math.min(edge.topY, edge.bottomY);
                		if(Ext.isDefined(edge.getFrameBounds)) {
                			var xVal = edge.getFrameBounds().x;
                    		var yVal = edge.getFrameBounds().y;
                		}
                		
                		
                		if (this.checkBorderReached(xVal, diffx, 25)) {
                			xBorderCrossed = true;
                		}
                		if (this.checkBorderReached(yVal, diffy, 25)) {
                			yBorderCrossed = true;
                		}
                	}
                }
            	for (var i = 0; i < selectedNodes.length; i++) {
                   	// check if the left/upper borders are reached.
                    
                   	if (xBorderCrossed) {
                   		diffx = 0;
                   	}
                   	if (yBorderCrossed) {
                   		diffy = 0;
                   	}
                   	var node = selectedNodes[i];
//                   	this.editor.canvas.nodeReaches(this.mouseX + node.width + 20, this.mouseY + node.height + 20, true);
//                   	this.editor.canvas.nodeReaches(this.mouseX - node.width - 20, this.mouseY - node.height - 20, true);
                   	this.editor.canvas.nodeReaches(node.x + diffx + node.width + 20, node.y + diffy + node.height + 20, true);
                   	this.editor.canvas.nodeReaches(node.x - diffx - node.width - 20, node.y- diffy - node.height - 20, true);

                   	node.moveTo(node.x + diffx, node.y + diffy);
               	}
            }

            this.alignmentHandler.updateAlignmentRulers( this.editor.getModel(), selectedNodes );
            this.alignmentHandler.drawRulers( this.editor.getCanvas() );

            if ( this.alignmentHandler.isVerticallyAligned() )
                this.diffToVerticalAlign = this.mouseX - this.alignmentHandler.verticalAlign;
            else
                this.diffToVerticalAlign = 0;

            if ( this.alignmentHandler.isHorizontallyAligned() )
                this.diffToHorizontalAlign = this.mouseY - this.alignmentHandler.horizontalAlign;
            else
                this.diffToHorizontalAlign = 0;

// Edges are selected earlier            
//            var selectedEdges = this.editor.selectionHandler.getSelectedEdges();

            if ( this.currentEdgeResize == null ) {
                if (selectedEdges.length > 0) {
                    for (var i = 0; i < selectedEdges.length; i++) {
                        if ( this.editor.selectionHandler.isPartOfMultiSelect(selectedEdges[i].source) &&
                             this.editor.selectionHandler.isPartOfMultiSelect(selectedEdges[i].target) )
                                selectedEdges[i].moveBy(diffx, diffy);
                    }
                }
            }
        }

        if (this.currentlyDraggedNode/* && !this.editor.selectionHandler.isPoolSelected()*/) {
            var node = this.currentlyDraggedNode;
            var cluster = this.editor.model.getDropableClusterAtPosition(this.mouseX, this.mouseY, node);

            for (var i = 0; i < this.editor.selectionHandler.getSelectedNodes().length; i++) {
                var n = this.editor.selectionHandler.getSelectedNodes()[i];
                
                if (n.equalsOrContains(cluster))
                    return false;
            }

            if (cluster && (!(this.editor.selectionHandler.isPoolSelected()) || (!cluster.isBPMNLane() && !cluster.isBPMNPool()))) {
                //highlight the possible new cluster
                if (!cluster.equalsOrContains(node))
                    cluster.setSelected(true, false, Util.COLOR_BLUE);

                //drop highlight when mouse is over another cluster
                if (this.currentCluster && this.currentCluster != cluster) {
                    this.currentCluster.setSelected(false);
                }

                this.currentCluster = cluster;
            } else if (this.currentCluster) {
                //drop highlight if the mouse is not over a cluster
                this.currentCluster.setSelected(false);
                this.currentCluster = null;
            }

            if (!this.editor.selectionHandler.isPartOfMultiSelect(node)) {
                var currentlyOver = this.editor.model.getNodeAroundPosition(node.x, node.y, node);

                //highlight if an attachment is possible
                if (Util.isAttachable(currentlyOver, node)) {
                    currentlyOver.setSelected(true, false, Util.COLOR_ORANGE);
                    this.currentCluster = null;
                } else {
                    currentlyOver = null;
                }

                //drop highlight of  possible last attachment
                if (this.currentAttachment != null && this.currentAttachment != currentlyOver ) {
                    this.currentAttachment.setSelected(false);
                }

                this.currentAttachment = currentlyOver;
            }

            return false;
        }

        if ( this.currentEdgeResize )
            this.currentEdgeResize.move( this.mouseX, this.mouseY );

        //update rubber band
        if (this.rubberBand) {
            this.selBottomX = this.mouseX;
            this.selBottomY = this.mouseY;

            var tX = Math.min(this.selTopX, this.selBottomX);
            var tY = Math.min(this.selTopY, this.selBottomY);

            var rWidth = Math.abs(this.selBottomX - this.selTopX);
            var rHeight = Math.abs(this.selBottomY - this.selTopY);

            this.rubberBand.attr({x: tX, y:tY, width: rWidth, height: rHeight});
            return false;
        }

        if ( this.mouseStatus == 0 ) {
            var edge = this.editor.getModel().getEdgeCloseTo( this.mouseX, this.mouseY );

            if ( edge != null && ( edge instanceof Inubit.WebModeler.EditableProcessEdge ) ) {
                if ( edge.isCloseToHorizontalFragment( this.mouseX, this.mouseY ) )
                    Ext.get("editor").setStyle("cursor", "n-resize");
                else if ( edge.isCloseToVerticalFragment( this.mouseX, this.mouseY ) )
                    Ext.get("editor").setStyle("cursor", "e-resize");
                else {
                    Ext.get("editor").setStyle("cursor", "auto");
                }
            }
            else if ( Ext.get("editor").getStyle("cursor") != "se-resize" && Ext.get("editor").getStyle("cursor") != "s-resize" ){
                Ext.get("editor").setStyle("cursor", "auto");
            }
        }

        return false;
    }
    
    
    this.checkBorderReached = function (coord, diff, additionalSpace) {
    	if(diff < 0 && ((coord + (diff - additionalSpace)) < 0)) {
    		return true;
    	}
    	return false;
    }

    this.mouseUp = function(event) {
        this.mouseStatus = 0;
        Ext.get("editor").setStyle("cursor", "default");
        
        //if frame resize was in progress
        if (this.currentlyResizedFrame) {
            this.currentlyResizedFrame.object.handleResize(this.frameDiffX, this.frameDiffY);
            this.currentlyResizedFrame = null;
            return false;
        }

        if ( this.currentTextNode ) {
            var offset = this.currentTextNode.getOffsetAtEdge();

            this.currentTextNode.edge.updateProperty("#label_offset", "" + offset);
            this.currentTextNode = null;

            return false;
        }
        
        //if edge creation was in progress
        if (this.currentEdgeSource) {
            this.currentEdgeSource.setSelected(false);

            if (this.currentTargetEdge) {
                //create edge docker and edge
                this.currentTargetEdge.setSelected(false);

                if (this.currentTargetEdge.getDocker())
                    this.editor.getModel().createEdge(this.currentEdgeSource, this.currentTargetEdge.getDocker(), true);
                else
                    this.editor.getModel().createEdgeDocker(this.currentEdgeSource, this.currentTargetEdge);
                this.currentTargetEdge = null;
            }

            this.currentEdgeSource = null;
        } else if (this.currentlyDraggedNode) {
        	//if node(s) was/were dragged
            var node = this.currentlyDraggedNode;
            var selectedNodes = this.editor.selectionHandler.getSelectedNodes();
            var cCluster = this.currentCluster;

            //handle cluster containment
            if (cCluster && (!(this.editor.selectionHandler.isPoolSelected()) || (!cCluster.isBPMNLane() && !cCluster.isBPMNPool()))) {
                if (!this.currentCluster.equalsOrContains(node)) {
                    for (var i = 0; i < selectedNodes.length; i++) {
                        if ( !selectedNodes[i].isAncestorMoved() )
                            selectedNodes[i].updateCluster(this.currentCluster);
                    	selectedNodes[i].alignZOrder();
                    }
                }
                this.currentCluster.setSelected(false);
                this.currentCluster = null;
            } else if (!this.editor.selectionHandler.isPartOfMultiSelect(node) && 
            			this.currentAttachment){

            	this.currentAttachment.setSelected(false);
            	node.attachToNode(this.currentAttachment);
            	node.alignZOrder();
            	this.currentAttachment = null;
            
            } else {
                node.detach();
                for (var i = 0; i < selectedNodes.length; i++) {
                    if ( !selectedNodes[i].isAncestorMoved() )
                        selectedNodes[i].updateCluster(null);
                }
            }

            this.currentlyDraggedNode = null;
        }

        var selectedNodes = this.editor.selectionHandler.getSelectedNodes();

        //update selected nodes and their corresponding edges at/from server
        for (var i = 0; i < selectedNodes.length; i++) {
            var node = selectedNodes[i];
            if (selectedNodes.length == 1 || this.editor.selectionHandler.getSelectedEdges().length == 0 )
                node.movementFinished();

            this.editor.canvas.nodeReaches(node.x + node.width / 2, node.y + node.height / 2, false);
        }

        if ( selectedNodes.length > 1 && this.editor.selectionHandler.getSelectedEdges().length > 0 )
            this.editor.getModel().multiSelectionMoved( selectedNodes, this.editor.selectionHandler.getSelectedEdges() )

        this.alignmentHandler.removeRulers();

        Ext.getCmp('editor').doLayout();
        
        if (this.rubberBand) {
            this.rubberBand.remove();
            this.rubberBand = null;
            this.editor.selectionHandler.selectArea(this.selTopX, this.selTopY, this.selBottomX, this.selBottomY);

            var selectedObjects = this.editor.selectionHandler.getSelection();

           
            for (var i = 0; i < selectedObjects.length; i++) {
                selectedObjects[i].setSelected(true);
            }
            

            if (selectedObjects.length == 1) {
                this.editor.displayProperties(selectedObjects[0]);
                selectedObjects[0].showContextMenu();
            }

            if (selectedObjects.length == 0) {
                this.editor.selectionHandler.singleSelect(this.editor.model);
                this.editor.displayProperties(this.editor.model);
            }
        }

        if ( this.currentEdgeResize ) {
            this.currentEdgeResize.movementFinished( this.mouseX, this.mouseY );
            this.currentEdgeResize = null;
        }
        return false;
    }

    /**
     * Decide if an event is forwarded or not
     */
    this.decideOnMouseDown = function(event) {
        var button;

        if (event.which)
            button = event.which;
        else
            button = event.button;

        if (button != 3 && button != 2) {
             //stop event forwarding
            Util.stopEvent(event);

            return true;
        }

        return false;
    }

    this.enableDragging = function() {
        this.mouseStatus = 1;
    }

    this.getMousePosition = function() {
        return {x : this.mouseX, y : this.mouseY};
    }

    this.startRubberBand = function() {
        if (this.rubberBand) this.rubberBand.remove();

        this.editor.selectionHandler.dropSelection();
        this.rubberBand = this.editor.getCanvas().rect(this.mouseX, this.mouseY, 0, 0).
                                attr({"stroke-width": '2', "stroke" : Util.COLOR_RED});

        this.selTopX = this.mouseX;
        this.selTopY = this.mouseY;
        this.selBottomX = this.mouseX;
        this.selBottomY = this.mouseY;
    }

    this.startResizeFrame = function(selectionFrame) {
        this.currentlyResizedFrame = selectionFrame;
        this.frameDiffX = 0;
        this.frameDiffY = 0;
    }
}