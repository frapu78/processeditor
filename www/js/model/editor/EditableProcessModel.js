Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.EditableProcessModel = Ext.extend(Inubit.WebModeler.ProcessModel, {
    constructor : function (config) {
    	this.commentCount = 0;
        Inubit.WebModeler.EditableProcessModel.superclass.constructor.call(this, config);
        this.model = this;
        this.loadProperties();
    	this.applyNodeHierarchy();
    	this.loadNodeComments(ProcessEditor.instance.getCanvas());
    	this.resolveEdgeEnds();
    	this.alignZOrder();
    },
    removeEdge : function(edge) {
         var newEdges = new Array();

        for (var i = 0; i < this.edges.length; i++) {
            if (this.edges[i] == edge) continue;

            newEdges.push(this.edges[i]);
        }

        this.edges = newEdges;
    },
    removeNode : function(node) {
        var newNodes = new Array();

        for (var i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i] == node) continue;

            newNodes.push(this.nodes[i]);
        }
        this.nodes = newNodes;
    },
    getNodeAroundPosition : function(x, y, currentNode) {
        var result = null;

        for (var i = this.nodes.length - 1; i >=0; i--) {
            var node = this.nodes[i];

            var x1 = node.x - node.width/2;
            var x2 = node.x + node.width/2;
            var y1 = node.y - node.height/2;
            var y2 = node.y + node.height/2;

            if ((((x>=x1) & (x<=x2) & (y>=y1) & (y<=y2)) ||
                ((x-10>=x1) & (x-10<=x2) & (y>=y1) & (y<=y2)) ||
                ((x+10>=x1) & (x+10<=x2) & (y>=y1) & (y<=y2)) ||
                ((x>=x1) & (x<=x2) & (y-10>=y1) & (y-10<=y2)) ||
                ((x>=x1) & (x<=x2) & (y+10>=y1) & (y+10<=y2))) & node != currentNode) {

                return node;
            }
        }

        return result;
    },
    getDropableClusterAtPosition : function(x, y, currentNode) {
        var cluster = this.getClusterAtPosition(x, y, currentNode);

        if (cluster && cluster.containsLanes())
            return null;

        return cluster;
    },
    getAttachedNodes : function(attachmentSource) {
        if (!attachmentSource.isAttachmentPossible)
            return null;

        var attachments = new Array();
        var children = attachmentSource.children;
        for (var i = 0; i < children.length; i++) {
            var node = children[i];

            if (node.isAttachable && node.getAttachedTo() == attachmentSource.getId())
                attachments.push(node);
        }

        return attachments;
    },
    createNode : function( className, x, y, cluster, withEdge, sourceNode ) {
        var xml = "<node>";

        xml += "<property name='#type' value='" + className + "'/>";
        xml += "<property name='x' value='" + x + "'/>";
        xml += "<property name='y' value='" + y + "'/>";

        xml += "</node>";

        var model = this;
        var req = new XMLHttpRequest();
        req.open("POST", this.uri + "/nodes", true);
        req.onreadystatechange = function() {
            if (req.status == 200 && req.readyState == 4) {
                var id = req.responseXML.getElementsByTagName("node")[0].getAttribute("newId");
//                var newNode = new ProcessNode(model.uri + "/nodes/" + id, model);
                var newNode = new Inubit.WebModeler.EditableProcessNode({
                    uri: model.uri + "/nodes/" + id,
                    model: model,
                    listeners: {
                        load: function() {
                            this.nodeCreated(newNode, cluster, withEdge, sourceNode);
                        },
                        scope: model
                    }
                });


                model.addNode(newNode);
//                newNode.onLoadFinish(model.nodeCreated, model, [newNode, cluster, withEdge, sourceNode]);
                newNode.load(ProcessEditor.instance.getCanvas());

                ProcessEditor.instance.canvas.nodeReaches(newNode.x + newNode.width / 2, newNode.y + newNode.height / 2);

                ProcessEditor.instance.undoHandler.addAction( newNode, newNode.remove, [] );
            }
        };

        req.send(xml);
    },
    createNodeFromSerialization : function( xml, cluster, isUndo ) {
        var model = this;
        var req = new XMLHttpRequest();
        req.open("POST", this.uri + "/nodes", true);
        req.onreadystatechange = function() {
            if (req.status == 200 && req.readyState == 4) {
                var id = req.responseXML.getElementsByTagName("node")[0].getAttribute("newId");
//                var newNode = new ProcessNode(model.uri + "/nodes/" + id, model);
                var newNode = new Inubit.WebModeler.EditableProcessNode({
                    uri: model.uri + "/nodes/" + id,
                    model: model,
                    listeners: {
                        load: function() {
                            this.nodeCreated(newNode, cluster);
                        },
                        scope: model
                    }
                });
                model.addNode(newNode);

//                newNode.onLoadFinish(model.nodeCreated, model, [newNode, cluster]);
                newNode.load(ProcessEditor.instance.getCanvas());

                ProcessEditor.instance.canvas.nodeReaches(newNode.x + newNode.width / 2, newNode.y + newNode.height / 2);

                if ( isUndo )
                    ProcessEditor.instance.undoHandler.addRedoAction( newNode, newNode.remove, [] );
                else
                    ProcessEditor.instance.undoHandler.addUndoAction( newNode, newNode.remove, [] );
            }
        };

        req.send(xml);
    },
    nodeCreated : function(newNode, cluster, withEdge, sourceNode) {
        if (cluster)
            newNode.updateCluster(cluster, true);

        if (withEdge == true && sourceNode != null)
            this.createEdge(sourceNode, newNode);

        var attachment = this.getNodeAroundPosition(newNode.x, newNode.y, newNode);
        if (Util.isAttachable(attachment, newNode))
            newNode.attachToNode(attachment);

        newNode.setModel(this);
        newNode.select();
    },
    createLane : function(cluster) {
        var xml = "<node>";
        xml += "<property name='#type' value='" + Inubit.WebModeler.ProcessNode.CLASS_BPMN_LANE + "'/>";
        xml += "<property name='text' value='Lane'/>";
        xml += "</node>";

        var model = this;
        var req = new XMLHttpRequest();
        req.open("POST", this.uri + "/nodes", true);
        req.onreadystatechange = function() {
            if (req.status == 200 && req.readyState == 4) {
                var id = req.responseXML.getElementsByTagName("node")[0].getAttribute("newId");
                var newNode = new Inubit.WebModeler.EditableProcessNode({
                    uri: model.uri + "/nodes/" + id,
                    model: model,
                    listeners: {
                        load: function() {
                            this.laneCreated(cluster, newNode);
                        },
                        scope: model
                    }
                });

                newNode.load(ProcessEditor.instance.getCanvas(), true);
            }
        };

        req.send(xml);
    },
    laneCreated : function(cluster, lane) {
        this.addNode(lane);

        if (cluster.isBPMNLane())
            cluster.getLaneHandler().getSurroundingPool().addLane(cluster, lane);
        else
            cluster.addLane(cluster, lane);
    },
    createEdgeDocker : function( source, edge, isUndo ) {
        var last = edge.points.length - 1;
        var x = (edge.points[last].x + edge.points[last-1].x - 5) / 2;
        var y = (edge.points[last].y + edge.points[last-1].y - 5) / 2;
        var xml = "<node>";

        xml += "<property name='#type' value='" + Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER + "'/>";
        xml += "<property name='#docked_edge' value='" + edge.getId() + "'/>";
        xml += "<property name='x' value='" + x + "'/>";
        xml += "<property name='y' value='" + y + "'/>";

        xml +=  "</node>";

        var model = this;
        var req = new XMLHttpRequest();
        req.open("POST", this.uri + "/nodes", true);
        req.onreadystatechange = function() {
            if (req.status == 200 && req.readyState == 4) {
                var id = req.responseXML.getElementsByTagName("node")[0].getAttribute("newId");
                var docker = new Inubit.WebModeler.EditableProcessNode({uri: model.uri + "/nodes/" + id, model: model});
                model.addNode(docker);
                docker.onLoadFinish(model.dockerCreated, model, [docker, source, isUndo]);
                docker.load(ProcessEditor.instance.getCanvas(), true);
            }
        };

        req.send(xml);
    },
    dockerCreated : function( docker , source, isUndo ) {
        this.createEdge(source, docker, true, isUndo);
    },
    createEdge : function(source, target, select, isUndo) {
        var xml = "<edges><edge>";

        xml += "<property name='#sourceNode' value='" + source.getId() + "'/>";
        xml += "<property name='#targetNode' value='" + target.getId() + "'/>";

        xml += "</edge></edges>";

        this.createEdgeFromSerialization(xml, select, isUndo);
    },
    createEdgeFromSerialization : function( serialization, select, isUndo ) {
        var model = this;

        var req = new XMLHttpRequest();
        req.open("POST", this.uri + "/edges", true);

        req.onreadystatechange = function() {
            if (req.status == 200 && req.readyState == 4) {
                var id = req.responseXML.getElementsByTagName("edge")[0].getAttribute("id");

                if (id == 'null') return;

                var newEdge = new Inubit.WebModeler.EditableProcessEdge({
                    uri: model.uri + "/edges/" + id,
                    model: model,
                    listeners: {
                        load: function() {
                            this.edgeCreated(newEdge, select);
                        },
                        scope: model
                    }
                });

//                newEdge.onLoadFinish(model.edgeCreated, model, [newEdge, select]);
                newEdge.load(ProcessEditor.instance.getCanvas(), true);

                if ( isUndo )
                    ProcessEditor.instance.undoHandler.addRedoAction( newEdge, newEdge.remove, [] );
                else
                    ProcessEditor.instance.undoHandler.addUndoAction( newEdge, newEdge.remove, [] );
            }
        };

        req.send(serialization);
    },
    updateEdgeProperty : function( id, key, value ) {
        var edge = this.getEdgeWithId( id );
        if ( edge != null )
            edge.updateProperty(key, value);
    },
    updateNodeProperty : function( id, key, value ) {
        var node = this.getNodeWithId( id );
        if ( node != null )
            node.updateProperty(key, value);
    },
    edgeCreated : function(newEdge, select) {
        if (select) {
            newEdge.setSelected(true);
            ProcessEditor.instance.selectionHandler.addSelectedEdge(newEdge);
        }
        this.addEdge(newEdge);
    },
    persistChanges : function(name, folder, comment, newModel, force) {
        var req = new XMLHttpRequest();

        if (newModel) {
            req.open("POST", Util.getContext() + "/models", true);
        } else { //if(force){
            req.open("POST", Util.getContext() + "/models/" + this.baseId, true);
//        } else {
//            req.open("POST", "/models/" + this.baseId + "/versions/" + this.version, true);
        }
//

        //set request headers
        req.setRequestHeader("Commit-SourceRef", this.tmpId);
        req.setRequestHeader("Comment", Util.escapeString(comment));
        req.setRequestHeader("Commit-Name", Util.escapeString(name));
        req.setRequestHeader("Folder-Alias", Util.escapeString(folder));
        req.setRequestHeader("Accept", "text/xml");

        if (this.version != null)
            req.setRequestHeader("Source-Version", this.version);

        var model = this;

        req.onreadystatechange = function() {
            if (req.readyState == 4) {
                if (req.status == 201) {
                    var url = req.responseXML.getElementsByTagName("url")[0].textContent;

                    var regEx = /.+\/models\/(\d+)\/versions\/(\d+)/;
                    var result = regEx.exec(url);

                    model.baseId = result[1];
                    model.version = result[2];

                } else if (req.status == 500) {
                    Ext.Msg.alert("Internal Server Error", "An error occured while processing your request.\n"+
                                    "Please contact the server administrator for further details.");
                } else if (req.status == 404) {
                    Ext.Msg.alert("Model not found", "The model you want to create a new version for is not present at server.");
                } else if (req.status == 409) {
                    Ext.Msg.alert("Conflict", "The server is not able to merge your changes with the requested version.");
                } else if (req.status == 403) {
                    Ext.Msg.alert("Forbidden", req.responseXML.getElementsByTagName("error")[0].textContent);
                }
            }
        };

        req.send(null);
    },
    getEdgesInArea : function(topX, topY, bottomX, bottomY) {
        var edges = new Array();

        for (var i = 0; i < this.edges.length; i++) {
            var points = this.edges[i].points;
            var last = points.length - 1;

            //if first and last point are part of the selected area, mark edge as selected
            if (Util.isPointInArea(points[0].x, points[0].y, topX, topY, bottomX, bottomY) &&
                Util.isPointInArea(points[last].x, points[last].y, topX, topY, bottomX, bottomY))
                    edges.push(this.edges[i]);
        }

        return edges;
    },
    getNodesInArea : function(topX, topY, bottomX, bottomY) {
        var nodes = new Array();
        for (var i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i].getType() == Inubit.WebModeler.ProcessNode.CLASS_EDGE_DOCKER || this.nodes[i].isBPMNLane() )
                continue;
            if (Util.isPointInArea(this.nodes[i].x, this.nodes[i].y, topX, topY, bottomX, bottomY) &&
                !this.nodes[i].isCluster() )
                    nodes.push(this.nodes[i]);
            else if ( this.nodes[i].isCluster() ) {
                //if dealing with clusters, the whole cluster must be contained within the selection frame
                var x1 = this.nodes[i].x - this.nodes[i].width / 2;
                var x2 = this.nodes[i].x + this.nodes[i].width / 2;
                var y1 = this.nodes[i].y - this.nodes[i].height / 2;
                var y2 = this.nodes[i].y + this.nodes[i].height / 2;
                if ( Util.isPointInArea(x1, y1, topX, topY, bottomX, bottomY ) &&
                     Util.isPointInArea(x2, y2, topX, topY, bottomX, bottomY ) )
                        nodes.push( this.nodes[i] );
            }
        }

        return nodes;
    },
    updateProperty : function(attribute, value) {
        var oldValue = this.properties[attribute];

        if ( oldValue == Util.unEscapeString(value) )
            return;

        this.properties[attribute] = Util.unEscapeString(value);

        var postReq = new XMLHttpRequest();
        postReq.open("PUT", this.uri, true);
        postReq.send(this.createUpdateXML(attribute, value));
    },
	multiSelectionMoved : function( nodes, edges ) {
        var req = new XMLHttpRequest();
        req.open( "PUT", this.uri, true );
        var model = this;
        req.onreadystatechange = function() {
            if ( req.readyState == 4 && req.status == 200 ) {
               model.applyEdgeRoutings( req.responseXML );
            }
        };

        req.send( this.createMultiSelectXML(nodes, edges));
    },
    applyEdgeRoutings : function( xml ) {
        var edgeEls = xml.getElementsByTagName("edge");
        for ( var i = 0; i < edgeEls.length; i++ ) {
            var edgeId = edgeEls[i].getAttribute("id");
            var edge = this.getEdgeWithId(edgeId);
            edge.applyMetadataUpdateResponse( edgeEls[i]);
            edge.redraw( ProcessEditor.instance.getCanvas() );
            edge.updateTextNode();
        }

        var dockerEls = xml.getElementsByTagName("docker");
        for ( var k = 0; k < dockerEls.length; k++ ) {
            var dId = dockerEls[k].getAttribute("id");

            var children = dockerEls[k].childNodes;

            for ( var l = 0; l < children.length; l++ ) {
                var tagName = children[l].tagName;

                if ( tagName == "position" ) {
                    var props = Util.parseProperties(children[l]);
                    this.getNodeWithId(dId).setPos( props.x, props.y );
                }
            }
        }
    },
    createUpdateXML : function(prop, value) {
        var serialization = "<update type='property'>\n";

        var property = "    <property ";
        property += "name=\"" + prop + "\" ";
        property += "value=\"" + value +"\"/>\n";

        serialization += property;

        serialization += "</update>";
        return serialization;
    },
    createLayoutXML : function() {
        var xml = "<update type='layout'></update>";

        return xml;
    },
    createMultiSelectXML : function( nodes, edges ) {
        var xml = "<update type='position'>";

        var i;
        for ( i = 0; i < nodes.length; i++ )
            xml += nodes[i].createPositionUpdateXML();


        for ( i = 0; i < edges.length; i++ )
            xml += "<ignore-edge id='" + edges[i].getId() + "'/>";


        xml += "</update>";
        return xml;
    },
    loadNodeComments : function(canvas) {
        for (var i = 0; i < this.nodes.length; i++) {
            this.nodes[i].loadComments(canvas);
        }
    },
    getCommentURI : function() {
        return this.uri + "/comments?version=" + this.version + "&baseId=" + this.baseId;
    },
    drawCommentHighlight : function() {
        return false;
    },
    dropCommentHighlight : function() {
        return false;
    },
    enableComments : function(canvas) {
        for (var i = 0; i < this.nodes.length; i++) {
            var node = this.nodes[i];
            node.drawCommentHighlight(canvas);
        }
    },
    disableComments : function() {
         for (var i = 0; i < this.nodes.length; i++) {
            var node = this.nodes[i];
            node.dropCommentHighlight();
        }
    }
});