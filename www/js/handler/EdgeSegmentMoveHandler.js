/**
 * Class for handling the movement of an edge segment
 */
function EdgeSegmentMoveHandler( edge, x, y, mode ) {
    this.edge = edge;
    this.startX = x - 3.5;
    this.startY = y - 3.5;

    this.lastX = x;
    this.lastY = y;

    this.p1 = null;
    this.p2 = null;
    
    this.graphics = null;

    this.vertical = false;
    this.horizontal = false;

    this.movementFinished = function( x , y ) {
        x -= 3.5;
        y -= 3.5;
        
        if ( this.graphics ) {
            if ( (this.vertical && x != this.startX)  || (this.horizontal && this.startY != y) ) {
                if ( this.horizontal ) {
                    this.edge.moveEdgeSegment( this.startX, this.startY, this.startX, y )
                } else if ( this.vertical ) {
                    this.edge.moveEdgeSegment( this.startX, this.startY, x, this.startY );
                }
            }
            this.graphics.remove();
        }
    }

    this.move = function( x , y ) {
        if ( this.graphics ) {
//            this.graphics.remove();
            if ( this.vertical ) {
                this.graphics.attr( {path: this.createVerticalMovementPath( this.p1, this.p2 , x) });
            } else if ( this.horizontal )
                this.graphics.attr( {path: this.createHorizontalMovementPath( this.p1, this.p2 , y) });
        }

        this.lastX = x;
        this.lastY = y;
    }

    this.setMode = function( mode ) {
        this.vertical = ( mode == EdgeSegmentMoveHandler.MODE_VERTICAL );
        this.horizontal = ( mode == EdgeSegmentMoveHandler.MODE_HORIZONTAL );

        if ( this.graphics )
            this.graphics.remove();

        if ( this.vertical ) {
            var points = this.edge.getVerticalSegmentPoints( this.startX + 3.5 , this.startY + 3.5 );

            if ( points.length == 2 ) {
                this.p1 = points[0];
                this.p2 = points[1];
                this.graphics = ProcessEditor.instance.getCanvas().path( this.createVerticalMovementPath(points[0], points[1], this.startX + 3.5) )
            }
        } else if ( this.horizontal ) {
            var points = this.edge.getHorizontalSegmentPoints( this.startX + 3.5 , this.startY + 3.5 );

            if ( points.length == 2 ) {
                this.p1 = points[0];
                this.p2 = points[1];
                this.graphics = ProcessEditor.instance.getCanvas().path( this.createHorizontalMovementPath(points[0], points[1], this.startY + 3.5 ));
            }
        }

        if ( this.graphics )
            this.graphics.attr({
                    stroke:Util.COLOR_ORANGE,
                    "stroke-width": 3
            })
    }

    this.createVerticalMovementPath = function( p1, p2, currentX ) {
        var path = "M";
        path += ( p1.x + 3.5 );
        path += " ";
        path += ( p1.y + 3.5 );
        
        path += "L";
        path += currentX;
        path += " ";
        path += ( p1.y + 3.5 );

        path += "L";
        path += currentX;
        path += " ";
        path += ( p2.y + 3.5 );

        path += "L";
        path += ( p2.x + 3.5 );
        path += " ";
        path += ( p2.y + 3.5 );

        return path;
    }

    this.createHorizontalMovementPath = function( p1 , p2 , currentY ) {
        var path = "M";
        path += ( p1.x + 3.5 );
        path += " ";
        path += ( p1.y + 3.5 );

        path += "L";
        path += ( p1.x + 3.5 );
        path += " ";
        path += currentY;

        path += "L";
        path += ( p2.x + 3.5 );
        path += " ";
        path += currentY;

        path += "L";
        path += ( p2.x + 3.5 );
        path += " ";
        path += ( p2.y + 3.5 );

        return path;
    }

    this.setMode(mode);


}

EdgeSegmentMoveHandler.MODE_VERTICAL = "vertical";
EdgeSegmentMoveHandler.MODE_HORIZONTAL = "horizontal";


