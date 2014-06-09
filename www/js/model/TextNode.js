
function TextNode(uri, edge) {
    this.paint = function(canvas) {
        this.domNode = canvas.image(this.uri + "?" + new Date(), Math.round(this.x - this.width / 2),
                                    Math.round(this.y - this.height / 2), this.width, this.height);

        this.bindEvents( canvas );
    }

    this.bindEvents = function( canvas ) {
        var edge = this.edge;
        this.domNode.node.onmousedown = function(event) {
           edge.select();

           //stop event forwarding
           Util.stopEvent(event);

           //disable image move
           return false;
        }
    }

    this.setPos = function(x, y) {
        this.x = Math.round(x);
        this.y = Math.round(y);
    }

    this.setSize = function(width, height) {
        this.width = width;
        this.height = height;
    }

    this.remove = function() {
        if (this.domNode)
            this.domNode.remove();
    }

    this.moveBy = function(diffx, diffy) {
        if (this.domNode)
            this.domNode.translate(diffx, diffy);
    }

    this.moveTo = function(x, y) {
        if (this.domNode) {
            this.domNode.translate( x - this.x, y - this.y);
        }

        this.x = x;
        this.y = y;
    }

    this.moveAnimatedTo = function(x, y) {
        if (this.domNode)
            this.domNode.animate({x: x, y:y}, 1000);

        this.setPos(x, y);
    }

    this.toFront = function() {
        if (this.domNode)
            this.domNode.toFront();
    }

    this.uri = uri;
    this.x = 0;
    this.y = 0;
    this.width = 100;
    this.height = 20;
    this.domNode = null;

    this.edge = edge;
}


