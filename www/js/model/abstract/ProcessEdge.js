Ext.namespace('Inubit.WebModeler');

/**
 * Abstract base class for all edge objects. This class should not be instantiated
 * directly.
 */
Inubit.WebModeler.ProcessEdge = Ext.extend(Inubit.WebModeler.ProcessObject, {
	constructor: function(config) {
		this.source = null;
    	this.target = null;

    	this.topY = 0;
    	this.topX = 0;
    	this.bottomX = 0;
    	this.bottomY = 0;

    	this.labelX = 0;
   		this.labelY = 0;

    	this.strokeWidth = 1.3;

	    this.points = new Array();
	    this.sourceShape = null;
    	this.domSourceShape = null;
    	this.targetShape = null;
    	this.domTargetShape = null;
		Inubit.WebModeler.ProcessEdge.superclass.constructor.call(this, config);
	},
	   /**
     * Paints the ProcessEdge on the given Canvas.
     * @param {Object} canvas
     */
    paint : function(canvas) {
        var path = canvas.path(this.getPath());
        var color =  "black";

        if (this.getProperty("color_arc"))
            color = "#" + Util.fromJavaRGB(this.getProperty("color_arc"));

        path.attr({stroke:color,
                    "stroke-width": this.strokeWidth,
                    "stroke-dasharray": this.getDashArray()
        });

        this.graphics = path;
        this.drawEndShapes(canvas);
        this.updateTextNode();
        this.bindEventsToEdge();
    },
    drawEndShapes : function(canvas) {
        if (this.sourceShape) {
            var uri = Util.getPath( this.sourceShape.uri );
            var width = this.sourceShape.width;
            var height = this.sourceShape.height;
            var x = this.points[0].x + 3.5;
            var y = this.points[0].y - height  / 2 + 3.5;

            this.domSourceShape = canvas.image(uri, x,
                                                y, width, height);

            var m_alpha = this.getSourceArc();

            this.domSourceShape.rotate(m_alpha, x, this.points[0].y + 3.5);

            var processEdge = this;
        }

        if (this.targetShape) {
            var last = this.points.length - 1;

            var uri = Util.getPath( this.targetShape.uri );
            var width = this.targetShape.width;
            var height = this.targetShape.height;
            var x = this.points[last].x - width + 3.5;
            var y = this.points[last].y - height / 2 + 3.5;

            this.domTargetShape = canvas.image(uri, x, y, width , height );

            var m_alpha = this.getTargetArc();

            this.domTargetShape.rotate(m_alpha, this.points[last].x + 3.5, this.points[last].y + 3.5);

            var processEdge = this;
        }
    },
    getSourceArc : function() {
        return this.getArc(0, 1);
    },
    getTargetArc : function() {
        var last = this.points.length - 1;

        return this.getArc(last-1, last);
    },
    /**
     * calculate arc for the line that connects to points
     */
    getArc : function(from, to) {
        var x1 = parseFloat(this.points[from].x);
        var y1 = parseFloat(this.points[from].y);
        var x2 = parseFloat(this.points[to].x);
        var y2 = parseFloat(this.points[to].y);

        var m = (y2 - y1) / (x2 - x1);

        var atan = Math.atan(m);

        if (x2 - x1 == 0) {
            if (y1 > y2)
                atan = Math.PI/2;
            else
                atan = -Math.PI/2;
        }

        var m_alpha = 0;
        if (m != 0)
            m_alpha = atan * 180.0 / Math.PI;

        if (m_alpha < 0)
            m_alpha += 360;

        if (x2 <= x1)
            m_alpha -= 180;

        return m_alpha;
    },
	getPath : function() {
        var x = this.points[0].x + 3.5;
        var y = this.points[0].y + 3.5;

        //if a source shape is defined we got to make the path shorter than the points tell
//        if (this.sourceShape && !(this.getType() == ProcessEdge.CLASS_CONVERSATION_LINK) &&
//            !(this.getType() == ProcessEdge.CLASS_ASSOCIATION)) {
        if ( this.sourceShape && this.sourceShape.outline == "true" ) {
            var alpha = this.getSourceArc();
            var rad_alpha = alpha * Math.PI / 180.0;

            x += Math.cos(rad_alpha) * ( this.sourceShape.width - 2);
            y += Math.sin(rad_alpha) * ( this.sourceShape.width - 2);
        }
        var path = "M " + x + " " + y;

        //all other points stay were they are
        for (var i = 1; i < this.points.length - 1; i++) {
            var x = this.points[i].x + 3.5;
            var y = this.points[i].y + 3.5;
            path += " L " + x + " " + y;
        }

        //if a target shape is defined we got to make the path shorter than the points tell
        var x = this.points[this.points.length - 1].x +3.5;
        var y = this.points[this.points.length - 1].y +3.5;

//        if (this.targetShape && !(this.getType() == ProcessEdge.CLASS_ASSOCIATION)) {
        if ( this.targetShape && this.targetShape.outline == "true" ) {
            var alpha = this.getTargetArc();
            var rad_alpha = alpha * Math.PI / 180;
            x -= Math.cos(rad_alpha) * ( this.targetShape.width - 2);
            y -= Math.sin(rad_alpha) * ( this.targetShape.width - 2);
        }

        path += " L " + x + " " + y;

        return path;
    },
    //get the dash-array according to edge class
    getDashArray : function() {
        if (this.getType() == Inubit.WebModeler.ProcessEdge.CLASS_MESSAGE_FLOW) {
            return "-";
        } else if(this.getType() == Inubit.WebModeler.ProcessEdge.CLASS_ASSOCIATION ||
                  this.getType() == Inubit.WebModeler.ProcessEdge.CLASS_STORY_ASSOCIATION) {
            return ".";
        }

        return "";
    },
    updateTextNode : function() {
        this.textNode.moveTo(this.labelX, this.labelY);
        this.textNode.setPos(this.labelX, this.labelY);
        this.textNode.toFront();
    },
    setSelected : function(selected, showMenu, color, rootCmp) {
        if (showMenu == null) showMenu = false;

        this.isSelected = selected;

        if (selected) {
            var canvas = rootCmp.getCanvas();

            if (this.selectionFrame == null)
                this.selectionFrame = new Inubit.WebModeler.SelectionFrame({object : this});

            this.selectionFrame.paint(canvas, color);

            if (showMenu) {
                this.showContextMenu();
//                this.highlightRoutingPoints(canvas);
            }
        } else {
            if (this.selectionFrame)
                this.selectionFrame.remove();

//            this.dropRoutingPointsHighlight();

            if (this.contextMenu)
                this.contextMenu.hide();
        }
    },
    getFrameBounds : function() {
        return {
            x: this.topX,
            y: this.topY,
            width: this.bottomX - this.topX,
            height: this.bottomY - this.topY
        };
    },
    alignZOrder : function() {
        if (this.graphics)
            this.graphics.toFront();
        if (this.domTargetShape)
            this.domTargetShape.toFront();
        if (this.domSourceShape)
            this.domSourceShape.toFront();
        if (this.textNode.domNode)
            this.textNode.domNode.toFront();
        if (this.selectionFrame)
            this.selectionFrame.toFront();
    },
    parsePoints : function(xml) {
        this.resetFrameValues();
        var points = new Array();
        var pointTags = xml.getElementsByTagName("point");
        for (var i = 0; i < pointTags.length; i++) {
            var p = pointTags[i];
            var x = parseInt(p.getAttribute("x"));
            var y = parseInt(p.getAttribute("y"));

            points.push({"x": x,
                          "y": y});
        }

        this.points = points;
        this.calculateFrameValues();
    },

    parsePointsFromJSON : function(json) {
        this.resetFrameValues();
        var points = new Array();

        for ( var i = 0; i < json.length; i++ )
            points.push( {x: json[i].x, y: json[i].y});

        this.points = points;
        this.calculateFrameValues();
    },
	setDocker : function(docker) {
        this.docker = docker;
    },
	getDocker : function() {
        return this.docker;
    },
	resetFrameValues : function() {
        this.topX = 2147483647;
        this.topY = 2147483647;

        this.bottomX = 0;
        this.bottomY = 0;
    },
	calculateFrameValues : function() {
        for (var i = 0; i < this.points.length; i++) {
            var x = this.points[i].x;
            var y = this.points[i].y;

            if (y < this.topY) this.topY = y;
            if (x < this.topX) this.topX = x;

            if (y > this.bottomY) this.bottomY = y;
            if (x > this.bottomX) this.bottomX = x;
        }
    },
    defineCluster : function() {
        if (   this.source.getCluster() != null &&
                   this.source.getCluster() == this.target.getCluster() ) {

                        this.source.getCluster().addClusterEdge(this);
           } else {
                this.source.addCrossClusterEdge(this);
                this.target.addCrossClusterEdge(this);
           }
    },
    /**
     *  Check if a certain point is in short distance to this edge
     */
    isCloseToPoint : function(x, y) {
        var x1 = 0;
        var x2 = 0;
        var y1 = 0;
        var y2 = 0;

        for (var i = 1; i < this.points.length; i++) {
            x1 = this.points[i-1].x + 3.5;
            y1 = this.points[i-1].y + 3.5;
            x2 = this.points[i].x + 3.5;
            y2 = this.points[i].y + 3.5;

            if (x1 == x2) {
                if ((y1 <= y && y <= y2) || (y2 <= y && y <= y1)) {
                    if (Math.abs(x1 - x) <= Inubit.WebModeler.ProcessEdge.POINT_DISTANCE)
                        return true;
                }
            } else if (y1 == y2) {
                if ((x1 <= x && x <= x2) || (x2 <= x && x <= x1))
                    if (Math.abs(y1 - y) <= Inubit.WebModeler.ProcessEdge.POINT_DISTANCE)
                        return true;
            } else {
                //check if edge (part) has to be considered --> click point must be in this edge's 'area'
                if ((   (y1 - Inubit.WebModeler.ProcessEdge.POINT_DISTANCE <= y && y <= y2 + Inubit.WebModeler.ProcessEdge.POINT_DISTANCE) ||
                        (y2 - Inubit.WebModeler.ProcessEdge.POINT_DISTANCE <= y && y <= y1 + Inubit.WebModeler.ProcessEdge.POINT_DISTANCE)) &&

                        ((x1 - Inubit.WebModeler.ProcessEdge.POINT_DISTANCE <= x && x <= x2 + Inubit.WebModeler.ProcessEdge.POINT_DISTANCE) ||
                        (x2 - Inubit.WebModeler.ProcessEdge.POINT_DISTANCE <= x && x <= x1 + Inubit.WebModeler.ProcessEdge.POINT_DISTANCE))) {

                    //compute distance
                    var a = (y2 - y1) / (x2 - x1);
                    var b = -1;

                    var diffx = x - x1;
                    var diffy = y - y1;

                    var absn = Math.abs( Math.sqrt(a * a + 1) );

                    var absscalar = Math.abs ( a * diffx + b * diffy );
                    var d = (absscalar / absn);

                    if (d <= Inubit.WebModeler.ProcessEdge.POINT_DISTANCE)
                        return true;
                }
            }
        }

        return false;
    },
	select : function() {
        if (ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this)) {
            ProcessEditor.instance.mouseListener.enableDragging();
            return false;
        }

        Inubit.WebModeler.ProcessEdge.superclass.select.call(this);
    },
    
    applyMetadataUpdateResponse : function(xml) {
        this.parsePoints(xml);
        var sourceXML = xml.getElementsByTagName("sourceshape")[0];

        if (sourceXML) {
            var props = Util.parseProperties(sourceXML);
            this.sourceShape = props;
        }

        var targetXML = xml.getElementsByTagName("targetshape")[0];

        if (targetXML) {
            var props = Util.parseProperties(targetXML);
            this.targetShape = props;
        }

        var textBoundsXML = xml.getElementsByTagName("textbounds")[0];
        var props = Util.parseProperties(textBoundsXML);
        var textWidth = parseInt(props.width);
        var textHeight = parseInt(props.height);
        this.textNode.setSize(textWidth, textHeight);

        // (fpu): Parse label position and stroke width
        var strokeXML = xml.getElementsByTagName("stroke")[0];
        var props = Util.parseProperties(strokeXML);
        this.strokeWidth = parseFloat(props.width);

        var labelXML = xml.getElementsByTagName("labelpos")[0];
        var props = Util.parseProperties(labelXML);
        this.labelX = parseInt(props.x);
        this.labelY = parseInt(props.y);

    },

    applyJSONMetadataUpdateResponse : function(json) {
        this.parsePointsFromJSON(json.points);

        if (json.sourceshape)
            this.sourceShape = json.sourceshape;

        if (json.targetshape)
            this.targetShape = json.targetshape;

        var textBounds = json.textbounds;
        this.textNode.setSize( textBounds.width, textBounds.Height)

        // (fpu): Parse label position and stroke width
        this.strokeWidth = json.stroke.width;

        this.labelX = parseInt(json.labelpos.x);
        this.labelY = parseInt(json.labelpos.y);
    },
    isProcessEdge : function() {
        return true;
    },
    /**
     * LOAD UTILITIES
     */
    load : function(canvas, async) {
        if (async == null) async = false;
        var req  = new XMLHttpRequest();
        req.open("GET", this.uri, async);

        if (async) {
            var edge = this;
            req.onreadystatechange = function() {
                if (req.readyState == 4 && req.status == 200)
                    edge.init(req.responseXML, canvas);
            };
            req.send(null);
        } else {
            req.send(null);
            this.init(req.responseXML, canvas);
        }
    },
//    onLoadFinish : function(funct, scope, params) {
//        if (funct == null) return;
//
//        this.loadFinished = function() {
//            funct.apply(scope, params);
//        };
//    },
//    loadFinished : function() {
//        return false;
//    },

    /**
     * "ABSTRACT" METHODS
     */
    bindEventsToEdge : function() {
        return false;
    },
    createContextMenu : function() {
        return false;
    }
});

Inubit.WebModeler.ProcessEdge.CLASS_MESSAGE_FLOW = "net.frapu.code.visualization.bpmn.MessageFlow";
Inubit.WebModeler.ProcessEdge.CLASS_ASSOCIATION = "net.frapu.code.visualization.bpmn.Association";
Inubit.WebModeler.ProcessEdge.CLASS_STORY_ASSOCIATION = "net.frapu.code.visualization.storyboard.Association";
Inubit.WebModeler.ProcessEdge.CLASS_CONVERSATION_LINK = "net.frapu.code.visualization.bpmn.ConversationLink";

Inubit.WebModeler.ProcessEdge.PROPERTY_SOURCE = "#sourceNode";
Inubit.WebModeler.ProcessEdge.PROPERTY_TARGET = "#targetNode";

Inubit.WebModeler.ProcessEdge.POINT_DISTANCE = 5;

///**
// * Abstract base class for all edge objects. This class should not be instantiated
// * directly.
// */
//function ProcessEdge(uri, m) {
//    this.prototype = new ProcessObject(uri, m);
//
//    this.uri = uri;
//    this.model = m;
//
//    this.source = null;
//    this.target = null;
//
//    this.isSelected = false;
//    this.selectionFrame = null;
//    this.contextMenu = null;
//
//    this.topY = 0;
//    this.topX = 0;
//    this.bottomX = 0;
//    this.bottomY = 0;
//
//    this.labelX = 0;
//    this.labelY = 0;
//
//    this.strokeWidth = 1.3;
//
//    this.properties = new Array();
//    this.points = new Array();
//    this.graphics = null;
//    this.sourceShape = null;
//    this.domSourceShape = null;
//    this.targetShape = null;
//    this.domTargetShape = null;
//    this.textNode = new TextNode(this.uri + "/label", this);
//
//
//    /**
//     * Paints the ProcessEdge on the given Canvas.
//     * @param {Object} canvas
//     */
//    this.paint = function(canvas) {
//        var path = canvas.path(this.getPath());
//        var color =  "black";
//
//        if (this.getProperty("color_arc"))
//            color = "#" + Util.fromJavaRGB(this.getProperty("color_arc"));
//
//        path.attr({stroke:color,
//                    "stroke-width": this.strokeWidth,
//                    "stroke-dasharray": this.getDashArray()
//        });
//
//        this.graphics = path;
//        this.drawEndShapes(canvas);
//        this.updateTextNode();
//        this.bindEventsToEdge();
//    }
//
//    this.drawEndShapes = function(canvas) {
//        if (this.sourceShape) {
//            var uri = Util.getPath( this.sourceShape.uri );
//            var width = this.sourceShape.width;
//            var height = this.sourceShape.height;
//            var x = this.points[0].x + 3.5;
//            var y = this.points[0].y - height  / 2 + 3.5;
//
//            this.domSourceShape = canvas.image(uri, x,
//                                                y, width, height);
//
//            var m_alpha = this.getSourceArc();
//
//            this.domSourceShape.rotate(m_alpha, x, this.points[0].y + 3.5);
//
//            var processEdge = this;
//        }
//
//        if (this.targetShape) {
//            var last = this.points.length - 1;
//
//            var uri = Util.getPath( this.targetShape.uri );
//            var width = this.targetShape.width;
//            var height = this.targetShape.height;
//            var x = this.points[last].x - width + 3.5;
//            var y = this.points[last].y - height / 2 + 3.5;
//            this.domTargetShape = canvas.image(uri, x, y, width , height );
//            var m_alpha = this.getTargetArc();
//
//            this.domTargetShape.rotate(m_alpha, this.points[last].x + 3.5, this.points[last].y + 3.5);
//
//            var processEdge = this;
//        }
//    }
//
//    this.getSourceArc = function() {
//        return this.getArc(0, 1);
//    }
//
//    this.getTargetArc = function() {
//        var last = this.points.length - 1;
//
//        return this.getArc(last-1, last);
//    }
//
//    /**
//     * calculate arc for the line that connects to points
//     */
//    this.getArc = function(from, to) {
//        var x1 = parseFloat(this.points[from].x);
//        var y1 = parseFloat(this.points[from].y);
//        var x2 = parseFloat(this.points[to].x);
//        var y2 = parseFloat(this.points[to].y);
//
//        var m = (y2 - y1) / (x2 - x1);
//
//        var atan = Math.atan(m);
//
//        if (x2 - x1 == 0) {
//            if (y1 > y2)
//                atan = Math.PI/2;
//            else
//                atan = -Math.PI/2;
//        }
//
//        var m_alpha = 0;
//        if (m != 0)
//            m_alpha = atan * 180.0 / Math.PI;
//
//        if (m_alpha < 0)
//            m_alpha += 360;
//
//        if (x2 <= x1)
//            m_alpha -= 180;
//
//        return m_alpha;
//    }
//
//    this.getPath = function() {
//        var x = this.points[0].x + 3.5;
//        var y = this.points[0].y + 3.5;
//
//        //if a source shape is defined we got to make the path shorter than the points tell
////        if (this.sourceShape && !(this.getType() == ProcessEdge.CLASS_CONVERSATION_LINK) &&
////            !(this.getType() == ProcessEdge.CLASS_ASSOCIATION)) {
//        if ( this.sourceShape && ( this.sourceShape.outline == "true" || this.sourceShape.outline == true ) ) {
//            var alpha = this.getSourceArc();
//            var rad_alpha = alpha * Math.PI / 180.0;
//
//            x += Math.cos(rad_alpha) * ( this.sourceShape.width - 2);
//            y += Math.sin(rad_alpha) * ( this.sourceShape.width - 2);
//        }
//        var path = "M " + x + " " + y;
//
//        //all other points stay were they are
//        for (var i = 1; i < this.points.length - 1; i++) {
//            var x = this.points[i].x + 3.5;
//            var y = this.points[i].y + 3.5;
//            path += " L " + x + " " + y;
//        }
//
//        //if a target shape is defined we got to make the path shorter than the points tell
//        var x = this.points[this.points.length - 1].x +3.5;
//        var y = this.points[this.points.length - 1].y +3.5;
//
////        if (this.targetShape && !(this.getType() == ProcessEdge.CLASS_ASSOCIATION)) {
//        if ( this.targetShape && ( this.targetShape.outline == "true" || this.targetShape.outline == true )) {
//            var alpha = this.getTargetArc();
//            var rad_alpha = alpha * Math.PI / 180;
//            x -= Math.cos(rad_alpha) * ( this.targetShape.width - 2);
//            y -= Math.sin(rad_alpha) * ( this.targetShape.width - 2);
//        }
//
//        path += " L " + x + " " + y;
//
//        return path;
//    }
//
//    //get the dash-array according to edge class
//    this.getDashArray = function() {
//        if (this.getType() == ProcessEdge.CLASS_MESSAGE_FLOW) {
//            return "-";
//        } else if(this.getType() == ProcessEdge.CLASS_ASSOCIATION ||
//                  this.getType() == ProcessEdge.CLASS_STORY_ASSOCIATION) {
//            return ".";
//        }
//
//        return ""
//    }
//
//    this.updateTextNode = function() {
//        this.textNode.moveTo(this.labelX, this.labelY);
//        this.textNode.setPos(this.labelX, this.labelY);
//        this.textNode.toFront();
//    }
//
//     this.setSelected = function(selected, showMenu, color, rootCmp) {
//        if (showMenu == null) showMenu = false;
//
//        this.isSelected = selected;
//
//        if (selected) {
//            var canvas = rootCmp.getCanvas();
//
//            if (this.selectionFrame == null)
//                this.selectionFrame = new SelectionFrame(this);
//
//            this.selectionFrame.paint(canvas, color);
//
//            if (showMenu) {
//                this.showContextMenu();
////                this.highlightRoutingPoints(canvas);
//            }
//        } else {
//            if (this.selectionFrame)
//                this.selectionFrame.remove();
//
////            this.dropRoutingPointsHighlight();
//
//            if (this.contextMenu)
//                this.contextMenu.hide();
//        }
//    }
//
//    this.getFrameBounds = function() {
//        return {
//            x: this.topX,
//            y: this.topY,
//            width: this.bottomX - this.topX,
//            height: this.bottomY - this.topY
//        }
//    }
//
//    this.getProperty = this.prototype.getProperty;
//
//    this.getType = this.prototype.getType;
//
//    this.setProperty = this.prototype.setProperty;
//
//    this.alignZOrder = function() {
//        if (this.graphics)
//            this.graphics.toFront();
//        if (this.domTargetShape)
//            this.domTargetShape.toFront();
//        if (this.domSourceShape)
//            this.domSourceShape.toFront();
//        if (this.textNode.domNode)
//            this.textNode.domNode.toFront();
//        if (this.selectionFrame)
//            this.selectionFrame.toFront();
//    }
//
//    this.parsePoints = function(xml) {
//        this.resetFrameValues();
//        var points = new Array();
//        var pointTags = xml.getElementsByTagName("point");
//        for (var i = 0; i < pointTags.length; i++) {
//            var p = pointTags[i];
//            var x = parseInt(p.getAttribute("x"));
//            var y = parseInt(p.getAttribute("y"))
//
//            points.push({"x": x,
//                          "y": y});
//        }
//
//        this.points = points;
//        this.calculateFrameValues();
//    }
//
//    this.parsePointsFromJSON = function(json) {
//        this.resetFrameValues();
//        var points = new Array();
//
//        for ( var i = 0; i < json.length; i++ )
//            points.push( {x: json[i].x, y: json[i].y});
//
//        this.points = points;
//        this.calculateFrameValues();
//    }
//
//    this.setDocker = function(docker) {
//        this.docker = docker;
//    }
//
//    this.getDocker = function() {
//        return this.docker;
//    }
//
//     this.resetFrameValues = function() {
//        this.topX = 2147483647;
//        this.topY = 2147483647;
//
//        this.bottomX = 0;
//        this.bottomY = 0;
//    }
//
//    this.calculateFrameValues = function() {
//        for (var i = 0; i < this.points.length; i++) {
//            var x = this.points[i].x;
//            var y = this.points[i].y;
//
//            if (y < this.topY) this.topY = y;
//            if (x < this.topX) this.topX = x;
//
//            if (y > this.bottomY) this.bottomY = y;
//            if (x > this.bottomX) this.bottomX = x;
//        }
//    }
//
//    this.defineCluster = function() {
//        if (   this.source.getCluster() != null &&
//                   this.source.getCluster() == this.target.getCluster() ) {
//
//                        this.source.getCluster().addClusterEdge(this);
//           } else {
//                this.source.addCrossClusterEdge(this);
//                this.target.addCrossClusterEdge(this);
//           }
//    }
//
//    /**
//     *  Check if a certain point is in short distance to this edge
//     */
//    this.isCloseToPoint = function(x, y) {
//        var x1 = 0;
//        var x2 = 0;
//        var y1 = 0;
//        var y2 = 0;
//
//        for (var i = 1; i < this.points.length; i++) {
//            x1 = this.points[i-1].x + 3.5;
//            y1 = this.points[i-1].y + 3.5;
//            x2 = this.points[i].x + 3.5;
//            y2 = this.points[i].y + 3.5;
//
//            if (x1 == x2) {
//                if ((y1 <= y && y <= y2) || (y2 <= y && y <= y1)) {
//                    if (Math.abs(x1 - x) <= ProcessEdge.POINT_DISTANCE)
//                        return true;
//                }
//            } else if (y1 == y2) {
//                if ((x1 <= x && x <= x2) || (x2 <= x && x <= x1))
//                    if (Math.abs(y1 - y) <= ProcessEdge.POINT_DISTANCE)
//                        return true;
//            } else {
//                //check if edge (part) has to be considered --> click point must be in this edge's 'area'
//                if ((   (y1 - ProcessEdge.POINT_DISTANCE <= y && y <= y2 + ProcessEdge.POINT_DISTANCE) ||
//                        (y2 - ProcessEdge.POINT_DISTANCE <= y && y <= y1 + ProcessEdge.POINT_DISTANCE)) &&
//
//                        ((x1 - ProcessEdge.POINT_DISTANCE <= x && x <= x2 + ProcessEdge.POINT_DISTANCE) ||
//                        (x2 - ProcessEdge.POINT_DISTANCE <= x && x <= x1 + ProcessEdge.POINT_DISTANCE))) {
//
//                    //compute distance
//                    var a = (y2 - y1) / (x2 - x1);
//                    var b = -1;
//
//                    var diffx = x - x1;
//                    var diffy = y - y1;
//
//                    var absn = Math.abs( Math.sqrt(a * a + 1) );
//
//                    var absscalar = Math.abs ( a * diffx + b * diffy );
//                    var d = (absscalar / absn);
//
//                    if (d <= ProcessEdge.POINT_DISTANCE)
//                        return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    this.select = function() {
//        if (ProcessEditor.instance.selectionHandler.isPartOfMultiSelect(this)) {
//            ProcessEditor.instance.mouseListener.enableDragging();
//            return false;
//        }
//
//        this.handleSelect();
//    }
//
//    this.handleSelect = this.prototype.select;
//
//    this.showContextMenu = this.prototype.showContextMenu;
//
//    this.serialize = this.prototype.serialize;
//
//    this.updateMetadata = this.prototype.updateMetadata;
//
//    this.applyMetadataUpdateResponse = function(xml) {
//        this.parsePoints(xml);
//        var sourceXML = xml.getElementsByTagName("sourceshape")[0];
//
//        if (sourceXML) {
//            var props = Util.parseProperties(sourceXML);
//            this.sourceShape = props;
//        }
//
//        var targetXML = xml.getElementsByTagName("targetshape")[0];
//
//        if (targetXML) {
//            var props = Util.parseProperties(targetXML);
//            this.targetShape = props;
//        }
//
//        var textBoundsXML = xml.getElementsByTagName("textbounds")[0];
//        var props = Util.parseProperties(textBoundsXML);
//        var textWidth = parseInt(props.width);
//        var textHeight = parseInt(props.height);
//        this.textNode.setSize(textWidth, textHeight)
//
//        // (fpu): Parse label position and stroke width
//        var strokeXML = xml.getElementsByTagName("stroke")[0];
//        var props = Util.parseProperties(strokeXML);
//        this.strokeWidth = parseFloat(props.width);
//
//        var labelXML = xml.getElementsByTagName("labelpos")[0];
//        var props = Util.parseProperties(labelXML);
//        this.labelX = parseInt(props.x);
//        this.labelY = parseInt(props.y);
//
//    }
//
//    this.applyJSONMetadataUpdateResponse = function(json) {
//        this.parsePointsFromJSON(json.points);
//
//        if (json.sourceshape)
//            this.sourceShape = json.sourceshape;
//
//        if (json.targetshape)
//            this.targetShape = json.targetshape;
//
//        var textBounds = json.textbounds;
//        this.textNode.setSize( textBounds.width, textBounds.Height)
//
//        // (fpu): Parse label position and stroke width
//        this.strokeWidth = json.stroke.width;
//
//        this.labelX = parseInt(json.labelpos.x);
//        this.labelY = parseInt(json.labelpos.y);
//    }
//
//    this.isProcessEdge = function() {
//        return true;
//    }
//
//    this.isProcessNode = this.prototype.isProcessNode;
//
//    this.getId = this.prototype.getId;
//
//    this.setModel = this.prototype.setModel;
//
//    /**
//     * LOAD UTILITIES
//     */
//
//    this.load = function(canvas, async) {
//        if (async == null) async = false;
//        var req  = new XMLHttpRequest();
//        req.open("GET", this.uri, async);
//
//        if (async) {
//            var edge = this;
//            req.onreadystatechange = function() {
//                if (req.readyState == 4 && req.status == 200)
//                    edge.init(req.responseXML, canvas);
//            }
//            req.send(null);
//        } else {
//            req.send(null);
//            this.init(req.responseXML, canvas);
//        }
//    }
//
//    this.onLoadFinish = function(funct, scope, params) {
//        if (funct == null) return;
//
//        this.loadFinished = function() {
//            funct.apply(scope, params);
//        }
//    }
//
//    this.loadFinished = function() {
//        return false;
//    }
//
//    /**
//     * "ABSTRACT" METHODS
//     */
//
//    this.bindEventsToEdge = function() {
//        return false;
//    }
//
//    this.createContextMenu = function() {
//        return false;
//    }
//}
//
//ProcessEdge.CLASS_MESSAGE_FLOW = "net.frapu.code.visualization.bpmn.MessageFlow";
//ProcessEdge.CLASS_ASSOCIATION = "net.frapu.code.visualization.bpmn.Association";
//ProcessEdge.CLASS_STORY_ASSOCIATION = "net.frapu.code.visualization.storyboard.Association";
//ProcessEdge.CLASS_CONVERSATION_LINK = "net.frapu.code.visualization.bpmn.ConversationLink";
//
//ProcessEdge.PROPERTY_SOURCE = "#sourceNode";
//ProcessEdge.PROPERTY_TARGET = "#targetNode";
//
//ProcessEdge.POINT_DISTANCE = 5;