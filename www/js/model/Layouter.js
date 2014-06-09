function Layouter() {
    this.layout = function(xml) {
        var bounds = xml.getElementsByTagName("bounds")[0];
        var boundProps = Util.parseProperties(bounds);

        var mWidth = parseInt(boundProps["width"]) + 50;
        var mHeight = parseInt(boundProps["height"]) + 50;

        ProcessEditor.instance.canvas.setSize(mWidth, mHeight);
        ProcessEditor.instance.getSelectionHandler().dropSelection();
        
        var currentModel = ProcessEditor.instance.getModel();

        var nodeXMLs = xml.getElementsByTagName("node");
        for (var i = 0; i < nodeXMLs.length;  i++) {
            var nXML = nodeXMLs[i];
            var node = currentModel.getNodeWithId(nXML.getAttribute("id"));

            var actionTag = nXML.getElementsByTagName("added");
            if (actionTag.length > 0) {
                var newNodeId = nXML.getAttribute("id");
                var newNodeUri = currentModel.uri + "/nodes/" + newNodeId;

                var newNode = new EditableProcessNode(newNodeUri, currentModel)
                currentModel.addNode(newNode);
                newNode.load(ProcessEditor.instance.getCanvas(), true);

                continue;
            }

            actionTag = nXML.getElementsByTagName("removed");
            if (actionTag.length > 0) {
                node.removeNodeInBrowser();
                continue;
            }

            var props = Util.parseProperties(nXML);
            if (! (node instanceof EditableProcessNode)) continue;
            var imgTag = nXML.getElementsByTagName("imageuri")[0];
            if (imgTag) {
                node.setPicture(imgTag.getAttribute("value") + ".png");
                node.updateMetadata();
                node.redraw(ProcessEditor.instance.getCanvas());
            }

            var x = parseInt(props["x"]);
            var y = parseInt(props["y"]);
            var height = parseInt(props["height"]);
            var width = parseInt(props["width"]);

            node.setSize(width, height);
            node.setPos(x, y);
            var animx = x - node.imageWidth / 2;
            var animy = y - height / 2;
            node.animate( {x: animx, y: animy} );
        }

        var edgeXMLs = xml.getElementsByTagName("edge");

        for (i = 0; i < edgeXMLs.length; i++) {
            var eXML = edgeXMLs[i];
            var edge = currentModel.getEdgeWithId(eXML.getAttribute("id"));

            var actionTag = eXML.getElementsByTagName("added");
            if (actionTag.length > 0) {
                var newEdgeId = eXML.getAttribute("id");
                var newEdgeUri = currentModel.uri + "/edges/" + newEdgeId;

                var newEdge = new EditableProcessEdge(newEdgeUri, currentModel)
                currentModel.addEdge(newEdge);
                newEdge.load(ProcessEditor.instance.getCanvas(), true);
                continue;
            }

            actionTag = eXML.getElementsByTagName("removed");
            if (actionTag.length > 0) {
                currentModel.deleteEdgeInBrowser(edge);
                continue;
            }

            props = Util.parseProperties(eXML);
            edge.parsePoints(eXML);
            edge.animate( {path: edge.getPath()} );
        }
    }
}


