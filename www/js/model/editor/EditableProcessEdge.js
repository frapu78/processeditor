Ext.namespace('Inubit.WebModeler');
/**
 * Objects of this class are editable edges. Properties can be changed, routing
 * points added, and the routing of the edge can be changed.
 */
Inubit.WebModeler.EditableProcessEdge = Ext.extend(Inubit.WebModeler.ProcessEdge, {
    constructor: function (config) {
        this.docker = null;
        Inubit.WebModeler.EditableProcessEdge.superclass.constructor.call(this, config);
        this.textNode = new MoveableTextNode(config.uri + "/label", this);
    },
    animate : function( attr ) {
        if (this.domTargetShape) this.domTargetShape.remove();
        if (this.domSourceShape) this.domSourceShape.remove();

        this.drawEndShapes(ProcessEditor.instance.getCanvas());
        if (this.graphics)
            this.graphics.animate( attr, 1000 );
        this.animateTextNode();
    },
    bindEventsToEdge : function() {
        var processEdge = this;
        this.graphics.node.onmousedown = function(event) {
            processEdge.select();

            var stopped = ProcessEditor.instance.mouseListener.decideOnMouseDown(event);

            if ( !stopped )
                ProcessEditor.instance.mouseListener.mouseDownOnEdge(processEdge);
            //disable image move
            return false;
        };

        if (this.sourceShape) {
            this.domSourceShape.node.onmousedown = function(event) {
                processEdge.select();

                ProcessEditor.instance.mouseListener.decideOnMouseDown(event);

                //disable image move
                return false;
            };
        }

        if (this.targetShape) {
            this.domTargetShape.node.onmousedown = function(event) {
                processEdge.select();

                ProcessEditor.instance.mouseListener.decideOnMouseDown(event);

                //disable image move
                return false;
            };
        }
    },
    remove : function() {
        this.removeFromEditor();

        var delReq = new XMLHttpRequest();
        delReq.open("DELETE", this.uri, true);
        delReq.send(null);
    },
    removeFromEditor : function() {
        this.setSelected(false);

        if ( this.graphics ) {
            this.removeShapes();
            var isRedo = !ProcessEditor.instance.undoHandler.isUndoInProgress();

            if ( this.target.getType() == Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER )
                ProcessEditor.instance.undoHandler.addAction( this.model, this.model.createEdgeDocker, [ this.source, this.model.getEdgeWithId(this.target.properties["#docked_edge"]), isRedo ]);
            else
                ProcessEditor.instance.undoHandler.addAction( this.model, this.model.createEdgeFromSerialization, [ this.serialize(), false, isRedo ] );
        }

        if (this.target) {
            this.target.removeEdge(this);
            if (this.target.getCluster())
                this.target.getCluster().removeClusterEdge(this);
        }

        if (this.source) {
            this.source.removeEdge(this);
            if (this.source.getCluster())
                this.source.getCluster().removeClusterEdge(this);
        }

        this.model.removeEdge(this);
    },
    redraw : function(canvas) {
        if (this.domTargetShape) {
            this.domTargetShape.remove();
            this.domTargetShape = null;
        }

        if (this.domSourceShape) {
            this.domSourceShape.remove();
            this.domSourceShape = null;
        }
        Inubit.WebModeler.EditableProcessEdge.superclass.redraw.call(this, canvas);
    },
    setSelected : function(selected, showMenu, color) {
        Inubit.WebModeler.EditableProcessEdge.superclass.setSelected.call(this, selected, showMenu, color, ProcessEditor.instance);
        this.textNode.dropBoundingBox();
    },
    handleMultiSelectionAttempt : function() {
        if ( this.isSelected ) {
            this.setSelected( false );
            ProcessEditor.instance.selectionHandler.removeEdgeFromSelection(this);
        } else {
            this.setSelected(true, false);
            ProcessEditor.instance.selectionHandler.addSelectedEdge(this);
        }
    },
    moveConnectionPoint : function(node, diffx, diffy) {
        var last = this.points.length - 1;

        if ( node.getId() == this.source.getId() && node.getId() == this.target.getId() ) {
            diffx /= 2;
            diffy /= 2;
        }

        if (node.getId() == this.source.getId()) {
            this.points[0]["x"] = this.points[0]["x"] + diffx;
            this.points[0]["y"] = this.points[0]["y"] + diffy;

            this.updateTextNode();

            if (this.docker) {
               this.docker.moveBy(diffx / 2, diffy / 2);
            }
        }
        if (node.getId() == this.target.getId()) {
            this.points[last]["x"] = this.points[last]["x"] + diffx;
            this.points[last]["y"] = this.points[last]["y"] + diffy;
        }

        this.resetFrameValues();
        this.calculateFrameValues();
    },
    moveBy : function(diffx, diffy) {
        if (this.graphics) {
            this.graphics.translate(diffx, diffy);

            if (this.domSourceShape) {
               this.domSourceShape.remove();
            }
            if (this.domTargetShape) {
                this.domTargetShape.remove();
            }
            for (var i = 1; i < this.points.length -1; i++) {
            	this.points[i].x += diffx;
            	this.points[i].y += diffy;
            }

            this.drawEndShapes(ProcessEditor.instance.getCanvas());

            if (this.docker && !ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this)) {
                this.docker.moveBy(diffx / 2, diffy / 2);
            }
        }

        if ( this.textNode ) {
            this.textNode.moveBy( diffx, diffy );
        }

        if (this.selectionFrame)
            this.selectionFrame.moveBy(diffx, diffy);

        this.topX += diffx;
        this.bottomX += diffx;

        this.topY += diffy;
        this.bottomY += diffy;
    },
    updateRouting : function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri + "/meta", true);

        var edge = this;

        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                edge.parsePoints(req.responseXML);
                edge.redraw(ProcessEditor.instance.getCanvas());
                // Update label position
                var labelXML = req.responseXML.getElementsByTagName("labelpos")[0];
                var props = Util.parseProperties(labelXML);
                edge.labelX = parseInt(props.x);
                edge.labelY = parseInt(props.y);
                edge.updateTextNode();
            }
        };

        req.send(null);

        if (this.docker) {
            for (var i = 0; i < this.docker.edges.length; i++) {
                this.docker.edges[i].updateRouting();
            }
        }
    },
	updateProperty : function( name, value ) {
        var oldValue = this.getProperty( name );

        if ( oldValue == Util.unEscapeString(value) )
            return;

        ProcessEditor.instance.undoHandler.addAction( this.model, this.model.updateEdgeProperty, [ this.getId(), name, oldValue ]);
        Inubit.WebModeler.EditableProcessEdge.superclass.updateProperty.call(this, name, value);
//        this.super_updateProperty( name, value );
    },
//    super_updateProperty : Inubit.WebModeler.ProcessObject.prototype.updateProperty,
    applyPropertyUpdateResponse : function(xml) {
        this.sourceShape = null;
        this.targetShape = null;
        this.updateMetadata();
        this.redraw(ProcessEditor.instance.getCanvas());
        this.textNode.remove();
        this.textNode.paint(ProcessEditor.instance.getCanvas());
        if (this.selectionFrame)
            this.selectionFrame.toFront();
        this.showContextMenu();
    },
    createSegmentMoveXML : function( fromX, fromY, toX, toY ) {
        var serialization = "<update type='segment'>";

        serialization += "<property name='fromX' value='" + fromX + "'/>";
        serialization += "<property name='fromY' value='" + fromY + "'/>";
        serialization += "<property name='toX' value='" + toX + "'/>";
        serialization += "<property name='toY' value='" + toY + "'/>";

        serialization += "</update>";
        return serialization;
    },
    applyMetadataUpdateResponse : function( xml ) {
        this.super_applyMetadataUpdateResponse( xml );
    },
	super_applyMetadataUpdateResponse : Inubit.WebModeler.ProcessEdge.prototype.applyMetadataUpdateResponse,
    applyTypeUpdateResponse : function ( xml, newType, isUndoInProgress ) {
        var newUri = xml.getElementsByTagName("uri")[0].getAttribute("value");
        var newEdge = new Inubit.WebModeler.EditableProcessEdge({
            uri:Util.getPath(newUri),
            model: this.model,
            listeners: {
                load: function() {
                    this.replaceWith(newEdge);
                }, scope: this
            }
        });

        if ( isUndoInProgress )
            ProcessEditor.instance.undoHandler.addRedoAction( newEdge, newEdge.updateType, [ this.getType() ] );
        else
            ProcessEditor.instance.undoHandler.addUndoAction( newEdge, newEdge.updateType, [ this.getType(), true ] );

        newEdge.load(ProcessEditor.instance.getCanvas(), true);
    },
    replaceWith : function(newEdge) {
        this.removeFromEditor();
        this.model.addEdge(newEdge);
        newEdge.select();
    },
    removeShapes : function() {
        if (this.graphics) {
            this.graphics.remove();
            this.graphics = null;

            if (this.domSourceShape)
                this.domSourceShape.remove();

            if (this.domTargetShape)
                this.domTargetShape.remove();

            this.textNode.remove();
        }
    },
    animateTextNode : function() {
        var textX = (this.points[0].x + 7 + this.points[1].x) / 2;
        var textY = (this.points[0].y + 7 + this.points[1].y) / 2;

        this.textNode.moveAnimatedTo(textX, textY);
    },
    getSegmentsForPoint : function( x , y ) {
        var segments = new Array();

        for ( var i = 1; i < this.points.length; i++ ) {
            var p1 = this.points[i - 1];
            var p2 = this.points[i];

            if ( p1.x == p2.x ) {
                if ( y - 3.5 <= Math.max( p1.y , p2.y ) && y - 3.5 >= Math.min( p1.y, p2.y ) )
                    segments.push( {p1 : p1, p2 : p2} );
            }
            else if ( p1.y == p2.y ) {
                if ( x - 3.5 <= Math.max( p1.x , p2.x ) && x - 3.5 >= Math.min( p1.x, p2.x ) )
                    segments.push( {p1 : p1, p2 : p2} );
            }
            else {
                if ( y - 3.5 <= Math.max( p1.y , p2.y ) && y - 3.5 >= Math.min( p1.y, p2.y ) )
                    if ( x - 3.5 <= Math.max( p1.x , p2.x ) && x - 3.5 >= Math.min( p1.x, p2.x ) )
                        segments.push( {p1 : p1, p2 : p2} );
            }
        }

        return segments;
    },
	getLength : function() {
        var length = 0;

        for ( var i = 1; i < this.points.length; i++ ) {
            var p1 = this.points[i - 1];
            var p2 = this.points[i];

            length += Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
        }

        return length;
    },
    isCloseToHorizontalFragment : function( x, y ) {
        for ( var i = 0; i < this.points.length - 1; i++ ) {
            var p1 = this.points[i];
            var p2 = this.points[i+1];

            if ( p1.y == p2.y && Math.abs( p1.y + 3.5 - y ) <= 3 )
                if ( (p1.x + 3.5 <= x && x <= p2.x + 3.5) || (p2.x + 3.5 <= x && x <= p1.x + 3.5) )
                    return true;
        }

        return false;
    },
    getHorizontalSegmentPoints : function( x , y ) {
        for ( var i = 0; i < this.points.length - 1; i++ ) {
            var p1 = this.points[i];
            var p2 = this.points[i+1];

            if ( p1.y == p2.y && Math.abs( p1.y + 3.5 - y ) <= 3 )
                if ( (p1.x + 3.5 <= x && x <= p2.x + 3.5) || (p2.x + 3.5 <= x && x <= p1.x + 3.5) )
                    return [p1, p2];
        }

        return [];
    },
    isCloseToVerticalFragment : function( x, y ) {
        for ( var i = 0; i < this.points.length - 1; i++ ) {
            var p1 = this.points[i];
            var p2 = this.points[i+1];

            if ( p1.x == p2.x && Math.abs( p1.x + 3.5 - x ) <= 3 )
                if ( (p1.y + 3.5 <= y && y <= p2.y + 3.5) || (p2.y + 3.5 <= y && y <= p1.y + 3.5) )
                    return true;
        }

        return false;
    },
    getVerticalSegmentPoints : function( x, y ) {
        for ( var i = 0; i < this.points.length - 1; i++ ) {
            var p1 = this.points[i];
            var p2 = this.points[i+1];

            if ( p1.x == p2.x && Math.abs( p1.x + 3.5 - x ) <= 3 )
                if ( (p1.y + 3.5 <= y && y <= p2.y + 3.5) || (p2.y + 3.5 <= y && y <= p1.y + 3.5) )
                    return [p1, p2];
        }

        return [];
    },
    createContextMenu : function() {
        return new Inubit.WebModeler.EdgeContextMenu({object : this});
    },
    isSelectionFrameHit : function(x, y) {
        var bounds = this.getFrameBounds();

        if (x >= this.topX - 3 && x <= this.topX + bounds.width + 12 &&
            y >= this.topY - 3 && y <= this.topY + bounds.height +  12)
                return true;

        return false;
    },
	moveEdgeSegment : function( fromX, fromY, toX, toY ) {
        var putReq = new XMLHttpRequest();
        putReq.open("PUT", this.uri, true);

        var edge = this;
        putReq.onreadystatechange = function() {
            if ( putReq.status == 200 && putReq.readyState == 4 ) {
                edge.applyMetadataUpdateResponse( putReq.responseXML );
                edge.redraw( ProcessEditor.instance.getCanvas() );
                edge.updateTextNode();
            }
        };

        ProcessEditor.instance.undoHandler.addAction( edge, edge.moveEdgeSegment, [toX, toY, fromX, fromY] );

        putReq.send( this.createSegmentMoveXML( fromX, fromY, toX, toY ));
    },
    serialize : function() {
        var xml = "<edge>";
        xml += Inubit.WebModeler.EditableProcessEdge.superclass.serialize.call(this);
        xml += "</edge>";
        return xml;
    },
    init : function(xml, canvas) {
        var properties = Util.parseProperties(xml);
        this.properties = properties;
        this.source = properties["#sourceNode"];
        this.target = properties["#targetNode"];

        ProcessEditor.instance.propertyConfig.registerClass( this.getType() , true);

        this.updateMetadata();

        this.textNode.paint(canvas);
        this.paint(canvas);

        this.fireEvent("load");
    }
});