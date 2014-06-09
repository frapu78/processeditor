/**
 * The dummy nodes represented by this class are used for the node context
 * menu to make the nodes, that should be created, draggable.
 */
function DummyNode(x, y, width, height, img, type, edgeSource) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.img = img;
    this.type = type;
    this.edgeSource = edgeSource;

    this.attachTo = null;
    this.cluster = null;
    this.domNode = null;

    this.paint = function(canvas) {
        var x = this.x - this.width / 2;
        var y = this.y - this.height / 2;

        this.domNode = canvas.image(this.img,
                                    x, y, this.width + 6 , this.height + 6);
    }

    this.moveTo = function(x, y) {
        var diffx = x - this.x;
        var diffy = y - this.y;

        if (this.domNode) this.domNode.translate(diffx, diffy);

        this.x += diffx;
        this.y += diffy;
    }

    this.setSelected = function() {
        return false;
    }

    this.equalsOrContains = function(cluster) {
        return false;
    }

    this.updateCluster = function(newCluster) {
        this.cluster = newCluster;
    }

    this.attachToNode = function(node) {
        this.attachTo = node.getId();
    }
    
    this.detach = function() {
    	this.attachTo = null;
    }

    this.alignZOrder = function() {
        return false;
    }

    this.isBPMNPool = function() {
        return this.type == Inubit.WebModeler.ProcessNode.CLASS_BPMN_POOL;
    }

    this.isBPMNLane = function() {
        return false;
    }

    this.isProcessNode = function() {
        return true;
    }

    this.movementFinished = function() {
        ProcessEditor.instance.model.createNode(this.type, this.x, this.y, this.cluster, true, this.edgeSource);

        if (this.domNode) this.domNode.remove();
    }

    this.isAncestorMoved = function() {
        return false;
    }

    this.paint(ProcessEditor.instance.getCanvas());
}