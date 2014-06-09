function ModelUpdater() {
    this.applyDifference = function(xml) {
        var bounds = xml.getElementsByTagName("bounds")[0];
        var boundProps = Util.parseProperties(bounds);

        var mWidth = parseInt(boundProps["width"]) + 50;
        var mHeight = parseInt(boundProps["height"]) + 50;

        ProcessEditor.instance.canvas.setSize(mWidth, mHeight);
        ProcessEditor.instance.getSelectionHandler().dropSelection();
        
        var currentModel = ProcessEditor.instance.getModel();

        var addElement = xml.getElementsByTagName("add")[0];
        var remElement = xml.getElementsByTagName("remove")[0];
        var upElement = xml.getElementsByTagName("update")[0];

        var i;
        for ( i = 0; i < addElement.childNodes.length; i++ ) {
            var child = addElement.childNodes[i];

            var tagName = child.nodeName;
            if ( tagName == "node" ) {
                var newNodeId = child.getAttribute("id");
                var newNodeUri = currentModel.uri + "/nodes/" + newNodeId;

                var newNode = new Inubit.WebModeler.EditableProcessNode({uri: newNodeUri, model: currentModel})
                currentModel.addNode(newNode);
                newNode.load(ProcessEditor.instance.getCanvas(), false);
            } else if ( tagName == "edge" ) {
                var newEdgeId = child.getAttribute("id");
                var newEdgeUri = currentModel.uri + "/edges/" + newEdgeId;

                var newEdge = new Inubit.WebModeler.EditableProcessEdge({uri: newEdgeUri, model: currentModel})
                currentModel.addEdge(newEdge);
                newEdge.load(ProcessEditor.instance.getCanvas(), true);
            }
        }

        for ( i = 0; i < remElement.childNodes.length; i++ ) {
            var child = remElement.childNodes[i];

            var tagName = child.nodeName;
            if ( tagName == "node" ) {
                currentModel.getNodeWithId( child.getAttribute("id")).removeNodeInBrowser();
            } else if ( tagName == "edge" ) {
                currentModel.getEdgeWithId( child.getAttribute("id") ).removeFromEditor();
            }
        }

        for ( i = 0; i < upElement.childNodes.length; i++ ) {
            var child = upElement.childNodes[i];
            var tagName = child.nodeName;
            if ( tagName == "node" ) {
                var node = currentModel.getNodeWithId( child.getAttribute("id"));

                var props = Util.parseProperties(child.getElementsByTagName("properties")[0]);
                for ( var name in props ) {
                    node.setProperty(name, props[name]);
                }

                if ( child.getAttribute("image") != null ) {
                    var imageUri = child.getAttribute("image") + ".png";
                    node.setPicture(imageUri);
                    node.applyMetadataUpdateResponse( child.getElementsByTagName("metadata")[0] );
                    node.redraw(ProcessEditor.instance.getCanvas());
                }

                var x = parseInt(node.getProperty("x"));
                var y = parseInt(node.getProperty("y"));
                var height = parseInt(node.getProperty("height"));
                var width = parseInt(node.getProperty("width"));

                node.setSize(width, height);
                node.setPos(x, y);
                var animx = x - node.imageWidth / 2;
                var animy = y - height / 2;
                node.animate( {x: animx, y: animy} );

            } else if ( tagName == "edge" ) {
                var edge = currentModel.getEdgeWithId( child.getAttribute("id"));

                var props = Util.parseProperties(child.getElementsByTagName("properties")[0]);
                for ( var name in props ) {
                    edge.setProperty(name, props[name]);
                }

                edge.applyMetadataUpdateResponse( child.getElementsByTagName("metadata")[0] );
                edge.animate( {path: edge.getPath()} );
            }

        }
    }

    this.applyJSONDifference = function( difference ) {
        var bounds = difference.bounds;

        var mWidth = parseInt(bounds.width) + 50;
        var mHeight = parseInt(bounds.height) + 50;

        ProcessEditor.instance.canvas.setSize(mWidth, mHeight);
        ProcessEditor.instance.getSelectionHandler().dropSelection();

        var currentModel = ProcessEditor.instance.getModel();

        var addElement = difference.add;
        var remElement = difference.remove;
        var upElement = difference.update;

        var i, jsonNode, jsonEdge;

        //ADDED OBJECTS
        for ( i = 0; i < addElement.nodes.length; i++ ) {
            jsonNode = addElement.nodes[i];

            var newNodeId = jsonNode;
            var newNodeUri = currentModel.uri + "/nodes/" + newNodeId;

            var newNode = new Inubit.WebModeler.EditableProcessNode({
            	uri: newNodeUri, 
            	model: currentModel})
            currentModel.addNode(newNode);
            newNode.load(ProcessEditor.instance.getCanvas(), false);
        }

        for ( i = 0; i < addElement.edges.length; i++ ) {
            jsonEdge = addElement.edges[i]
            var newEdgeId = jsonEdge;
            var newEdgeUri = currentModel.uri + "/edges/" + newEdgeId;

            var newEdge = new Inubit.WebModeler.EditableProcessEdge({
            	uri: newEdgeUri, 
            	model: currentModel,
            	listeners: {
            		load: function() {
            			currentModel.addEdge(newEdge);
            		}
            	}
        	})
//            currentModel.addEdge(newEdge);
            newEdge.load(ProcessEditor.instance.getCanvas(), true);
        }


        //DELETED OBJECTS
        for ( i = 0; i < remElement.nodes.length; i++ ) {
            jsonNode = remElement.nodes[i];
            // to avoid jsonNode.id = null in grid-layouter update 
            if(jsonNode.id != null)
                currentModel.getNodeWithId( jsonNode.id ).removeNodeInBrowser();
            else
                currentModel.getNodeWithId( jsonNode ).removeNodeInBrowser();
        }

        for ( i = 0; i < remElement.edges.length; i++ ) {
            jsonEdge = remElement.edges[i];
             // to avoid jsonE.id = null in grid-layouter update 
            if(jsonEdge.id != null)
                currentModel.getEdgeWithId( jsonEdge.id ).removeFromEditor();
            else{
                if(currentModel.getEdgeWithId( jsonEdge ) != null)
                    currentModel.getEdgeWithId( jsonEdge ).removeFromEditor();
            }
        }

        //UPDATED OBJECTS
        for ( i = 0; i < upElement.nodes.length; i++ ) {
            jsonNode = upElement.nodes[i];
            var node = currentModel.getNodeWithId( jsonNode.id );

            var props = Util.parsePropertiesFromJSON(jsonNode.properties);
            for ( var name in props ) 
                node.setProperty(name, props[name]);

            if ( jsonNode.image != null ) {
                var imageUri = jsonNode.image + ".png";
                node.setPicture(imageUri);
                node.applyJSONMetadataUpdateResponse( jsonNode.metadata );
                node.redraw(ProcessEditor.instance.getCanvas());
            }

            var childProperty = node.getProperty("#nodes")
            if ( Ext.isDefined(childProperty) ) {
                var childIds = childProperty.split(";");
                if ( childProperty != "" && childIds.length != node.children.length ) {
                    node.children = new Array();
                    for ( var j = 0; j < childIds.length; j++ ) {
                        var child = currentModel.getNodeWithId(childIds[j]);
                        child.setCluster(node);
                        node.addChild(child);
                    }
                }
            }

            if ( node.isBPMNLane() )
                node.paintRenderingHints(ProcessEditor.instance.getCanvas());

            var x = parseInt(node.getProperty("x"));
            var y = parseInt(node.getProperty("y"));
            var height = parseInt(node.getProperty("height"));
            var width = parseInt(node.getProperty("width"));

            node.setSize(width, height);
            node.setPos(x, y);
            var animx = x - node.imageWidth / 2;
            var animy = y - height / 2;
            node.animate( {x: animx, y: animy} );
        }

        for ( i = 0; i < upElement.edges.length; i++ ) {
            jsonEdge = upElement.edges[i];
            var edge = currentModel.getEdgeWithId( jsonEdge.id );

            var props = Util.parsePropertiesFromJSON(jsonEdge.properties);
            for ( var name in props ) 
                edge.setProperty(name, props[name]);

            edge.applyJSONMetadataUpdateResponse( jsonEdge.metadata );
            edge.animate( {path: edge.getPath()} );
        }
    }
}


