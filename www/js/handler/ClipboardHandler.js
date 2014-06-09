function ClipboardHandler() {
    this.nodes = null;
    this.edges = null;

    this.toPaste = 0;
    this.pasted = 0;

    this.cut = function(selectionHandler) {
        this.copy(selectionHandler);
        selectionHandler.deleteSelection();
    };

    this.copy = function(selectionHandler) {
        this.nodes = new Array();

        for (var i = 0; i < selectionHandler.getSelectedNodes().length; i++) {
            var node =  selectionHandler.getSelectedNodes()[i];
            this.nodes.push(node);
        }

        var edges = selectionHandler.getSelectedEdges();
        this.edges = new Array();
        for (var i = 0; i < edges.length;i++) {
            var edge = edges[i];
            if (selectionHandler.isPartOfMultiSelect(edge.source) && selectionHandler.isPartOfMultiSelect(edge.target) ) {
                this.edges.push(edge);
            }
        }
    };

    this.paste = function() {
        var xml = "<nodes>";
        for (var i = 0; i < this.nodes.length; i++) {
            this.nodes[i].properties["x"] += 5;
            this.nodes[i].properties["y"] += 5;
            xml += this.nodes[i].serialize();
            this.nodes[i].properties["x"] -= 5;
            this.nodes[i].properties["y"] -= 5;
        }

        this.toPaste = this.nodes.length;
        this.pasted = 0;

        xml += "</nodes>";
        var postReq = new XMLHttpRequest();
        var modelUri = ProcessEditor.instance.model.uri;
        postReq.open("POST", modelUri + "/nodes" , true);

        var idMap = new Array();

        var clipboard = this;
        postReq.onreadystatechange = function() {
            if (postReq.status == 200 && postReq.readyState == 4) {
                ProcessEditor.instance.selectionHandler.dropSelection();
                var respXml = postReq.responseXML;
                var nodes = respXml.getElementsByTagName("node");
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    var oldId = node.getAttribute("oldId");
                    var newId = node.getAttribute("newId");

                    idMap[oldId] = newId;

                    var newNode = new Inubit.WebModeler.EditableProcessNode({
                        uri: modelUri + "/nodes/" + newId,
                        model: ProcessEditor.instance.getModel(),
                        listeners: {
                            load: function() {
                                this.nodePasted(newNode, idMap);
                            },
                            scope: clipboard
                        }
                    });
//                    newNode.onLoadFinish(clipboard.nodePasted, clipboard, [newNode, idMap]);
                    newNode.load(ProcessEditor.instance.getCanvas());
                }
            }
        };

        postReq.send(xml);
    };

    this.nodePasted = function(newNode, idMap) {
        this.pasted++;

        ProcessEditor.instance.model.addNode(newNode);
        newNode.setSelected(true, false, Util.COLOR_RED);
        ProcessEditor.instance.selectionHandler.addSelectedNode(newNode);

        if (this.edges.length == 0 && this.nodes.length == 1) {
            ProcessEditor.instance.selectionHandler.singleSelect(newNode);
            newNode.setSelected(true, true, Util.COLOR_RED);

            ProcessEditor.instance.displayProperties(newNode);
        }

        if (this.pasted == this.toPaste) {
            this.pasteEdges(idMap);
        }
    };

    this.pasteEdges = function(idMap) {
        var modelUri = ProcessEditor.instance.model.uri;
        var edgeReq = new XMLHttpRequest();
        edgeReq.open("POST", modelUri + "/edges", true);
        var xml = "<edges>";

        for (var i=0; i < this.edges.length; i++) {
            var edge = this.edges[i];

            var oldSource = edge.getProperty(ProcessEdge.PROPERTY_SOURCE);
            var oldTarget = edge.getProperty(ProcessEdge.PROPERTY_TARGET);
            edge.properties[ProcessEdge.PROPERTY_SOURCE] = idMap[edge.source.getId()];
            edge.properties[ProcessEdge.PROPERTY_TARGET] = idMap[edge.target.getId()];

            xml += edge.serialize() ;

            edge.properties["#sourceNode"] = oldSource;
            edge.properties["#targetNode"] = oldTarget;
        }

        xml += "</edges>";
        var clipboard = this;

        edgeReq.onreadystatechange = function() {
            if (edgeReq.readyState == 4 && edgeReq.status == 200) {
                var respXML = edgeReq.responseXML;
                var edges = respXML.getElementsByTagName("edge");
                for (var i = 0; i < edges.length; i++) {
                    var edge = edges[i];
                    var id = edge.getAttribute("id");

                    var newEdge = new Inubit.WebModeler.EditableProcessEdge({
                        uri: modelUri + "/edges/" + id,
                        model: ProcessEditor.instance.getModel(),
                        listeners: {
                            load: function() {
                                this.edgePasted(newEdge);
                            },
                            scope: clipboard
                        }
                    });
                    newEdge.load(ProcessEditor.instance.getCanvas());
                }
            }
        };
        edgeReq.send(xml);
    };

    this.edgePasted = function(newEdge) {
        ProcessEditor.instance.model.addEdge(newEdge);
        newEdge.setSelected(true, false);
        ProcessEditor.instance.selectionHandler.addSelectedEdge(newEdge);
    };

    this.getClippedNodeWithId = function(id) {
        for (var i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i].getId() == id)
                return this.nodes[i];
        }

        return null;
    };
}

ClipboardHandler.instance = null;
ClipboardHandler.getInstance = function() {
    if(ClipboardHandler.instance == null) {
        ClipboardHandler.instance = new ClipboardHandler();
    }

    return ClipboardHandler.instance;
};


