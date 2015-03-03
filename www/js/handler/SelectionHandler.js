function SelectionHandler() {
    this.singleObject = null;
    this.selectedNodes = new Array();
    this.selectedEdges = new Array();

    this.poolSelected = false;

    this.dropSelection = function() {
        if (this.singleObject) {
            this.singleObject.setSelected(false);
            this.singleObject = null;

            ProcessEditor.instance.basePropertyGrid.removeAll(true);
            ProcessEditor.instance.extendedPropertyGrid.removeAll(true);
//            if ( ProcessEditor.instance.extendedPropertyGrid.isVisible() ) 
//                ProcessEditor.instance.extendedPropertyGrid.removeAll(true);
        }
        
        while(this.selectedNodes.length > 0) {
            var node = this.selectedNodes.pop();
            node.setSelected(false);
        }

        while(this.selectedEdges.length > 0) {
            var edge = this.selectedEdges.pop();
            edge.setSelected(false);
        }

        this.poolSelected = false;
    }

    this.singleSelect = function(processObject) {
        if (this.singleObject == processObject) 
            return false;
        
        this.dropSelection();
        this.singleObject = processObject;  
        if (processObject.isProcessNode && processObject.isProcessNode()) {
            this.selectedNodes.push(processObject);
            if (processObject.isBPMNPool())
                this.poolSelected = true;
        } else if (processObject.isProcessEdge && processObject.isProcessEdge())
            this.selectedEdges.push(processObject);

        return true;
    }

    this.getSingleObject = function() {
        return this.singleObject;
    }

    this.getSelectedNodes = function() {
        return this.selectedNodes;
    }

    this.addSelectedNode = function(node) {
        if (!(node.isProcessNode && node.isProcessNode())) return;

        this.selectedNodes.push(node);

        if ( this.singleObject != null && ( this.selectedEdges.length + this.selectedNodes.length ) > 1 ) {
            this.singleObject.setSelected(false);

            if ( this.singleObject.isBPMNLane && this.singleObject.isBPMNLane() ) {
                this.removeNodeFromSelection(this.singleObject);
                return;
            }

            this.singleObject.setSelected(true, false);
            this.singleObject = null;
        }
    }

    this.addSelectedEdge = function(edge) {
        if (!(edge.isProcessEdge && edge.isProcessEdge())) return;

        this.selectedEdges.push(edge);

        if ( this.singleObject != null && ( this.selectedEdges.length + this.selectedNodes.length ) > 1 ) {
            this.singleObject.setSelected(false);

            if ( this.singleObject.isBMPNLane && this.singleObject.isBPMNLane() ) {
                // I guess it was a mistake: this.removeNodeFromSelection(this.singleObject);
                this.removeEdgeFromSelection(this.singleObject);
                return;
            }

            this.singleObject.setSelected(true, false);
            this.singleObject = null;
        }
    }

    this.removeEdgeFromSelection = function( edge ) {
        var newSelEdges = new Array();

        for ( var i = 0; i < this.selectedEdges.length; i++ ) {
            if ( edge != this.selectedEdges[i] )
                newSelEdges.push( this.selectedEdges[i] );
        }

        this.selectedEdges = newSelEdges;
    }

    this.removeNodeFromSelection = function( node ) {
        var newSelNodes = new Array();

        for ( var i = 0; i < this.selectedNodes.length; i++ ) {
            if ( node != this.selectedNodes[i] )
                newSelNodes.push( this.selectedNodes[i] );
        }

        this.selectedNodes = newSelNodes;
    }

    this.getSelectedEdges = function() {
        return this.selectedEdges;
    }

    this.getSelection = function() {
        var selection = this.selectedNodes.concat(this.selectedEdges);

        return selection;
    }

    this.selectArea = function(topX, topY, bottomX, bottomY) {
        this.dropSelection();
        this.selectedNodes = ProcessEditor.instance.model.getNodesInArea(topX, topY, bottomX, bottomY);
        this.selectedEdges = ProcessEditor.instance.model.getEdgesInArea(topX, topY, bottomX, bottomY);

        //check if pool is selected
        for (var i = 0; i < this.selectedNodes.length; i++)
            if (this.selectedNodes[i].isBPMNPool()) {
                this.poolSelected = true;
                break;
            }

        //if only one object is selected, set single object
        if (this.selectedNodes.length == 1 && this.selectedEdges.length == 0)
            this.singleObject = this.selectedNodes[0];

        if (this.selectedNodes.length == 0 && this.selectedEdges.length == 1)
            this.singleObject = this.selectedEdges[0];
    }

    this.isPartOfMultiSelect = function(processObject) {
        if (this.singleObject) return false;

        var selected = this.getSelection();

        for (var i = 0; i < selected.length; i++)
            if (selected[i] == processObject)
                return true;

        return false;
    }

    this.isSelected = function( processObject ) {
        return this.singleObject == processObject || this.isPartOfMultiSelect(processObject);
    }

    this.areMultipleObjectsSelected = function() {
        return (this.singleObject == null);
    }

    this.isPoolSelected = function() {
        return this.poolSelected;
    }

    this.deleteSelection = function() {
        while (this.selectedNodes.length > 0) {
            var node = this.selectedNodes.pop();
            node.remove();
        }

        while (this.selectedEdges.length > 0) {
            var edge = this.selectedEdges.pop();
            edge.remove();
        }

        this.dropSelection();
    }
}