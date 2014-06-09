MoveableTextNode = function( uri, edge ) {
    this.prototype = new TextNode(uri, edge);
    this.uri = uri;
    this.edge = edge;
    this.x = 0;
    this.y = 0;
    this.width = 100;
    this.height = 20;
    this.domNode = null;
    this.boundingBox = null;

    this.paint = this.prototype.paint;

    this.bindEvents = function( canvas ) {
        var edge = this.edge;
        var textNode = this;
        this.domNode.node.onmousedown = function(event) {
           edge.select();

           textNode.drawBoundingBox( canvas );
           ProcessEditor.instance.mouseListener.mouseDownOnTextNode(textNode);
           //stop event forwarding
           Util.stopEvent(event);

           //disable image move
           return false;
        }
    }

    this.drawBoundingBox = function( canvas ) {
        var x = this.x - this.width / 2 ;
        var y = this.y - this.height / 2 + 6;

        this.boundingBox = canvas.rect( x , y , this.width + 3, this.height -6 );
    }

    this.dropBoundingBox = function() {
        if ( this.boundingBox )
            this.boundingBox.remove();
    }

    this.setPos = this.prototype.setPos;

    this.setSize = this.prototype.setSize;

    this.remove = this.prototype.remove;

    this.moveBy = this.prototype.moveBy;

    this.moveTo = function( x , y ) {
        if ( this.boundingBox ) {
            this.boundingBox.translate( x - this.x , y - this.y );
        }
        this.super_moveTo( x, y );
    }

    this.super_moveTo = this.prototype.moveTo;

    this.moveOnEdge = function( x, y ) {
        var segments = this.edge.getSegmentsForPoint(x, y);
        var closestPoint = null;
        var distance = 9999999;
        for ( var i = 0; i < segments.length; i++ ) {
            var d = Util.computeDistanceLineToPoint( segments[i].p1 , segments[i].p2, {x : x , y : y});

            if ( d < distance ) {
                distance = d;
                closestPoint = Util.getFootPoint(segments[i].p1 , segments[i].p2, {x : x , y : y});
            }
        }

        for ( var i = 0; i < this.edge.points.length; i++ ) {
            var p = this.edge.points[i];
            var d = Math.sqrt( (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
            if ( d < distance ) {
                distance = d;
                closestPoint = p;
            }
        }

        this.moveTo( closestPoint.x, closestPoint.y );
    }

    this.getOffsetAtEdge = function() {
        var length = this.edge.getLength();
        var curr = 0;
        for ( var i = 1; i < this.edge.points.length; i++ ) {
            var p1 = this.edge.points[i -1];
            var p2 = this.edge.points[i];

            if ( this.x <= Math.max( p1.x, p2.x ) && this.x >= Math.min( p1.x, p2.x ) &&
                    this.y <= Math.max( p1.y, p2.y ) && this.y >= Math.min( p1.y, p2.y ) ) {

                curr += Math.sqrt( (p1.x - this.x) * (p1.x - this.x) + (p1.y - this.y) * (p1.y - this.y) )
                 break;
            } else {
                curr += Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
            }
        }

        return ( curr / length );
    }

    this.moveAnimatedTo = this.prototype.moveAnimatedTo;

    this.toFront = this.prototype.toFront;
}