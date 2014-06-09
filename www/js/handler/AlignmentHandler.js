AlignmentHandler = function() {
    this.verticalAlign = -1;
    this.horizontalAlign = -1;

    this.horizontalRuler = null;
    this.verticalRuler = null;

    this.updateAlignmentRulers = function( model, selection ) {
        if ( selection.length == 0 ) return;

        var avx = 0;
        var avy = 0;
        
        //determine avarage position of selection
        for ( var i = 0; i < selection.length; i++ ) {
            avx += selection[i].x;
            avy += selection[i].y;
        }

        selection.length == 0 ? avx = -1 : avx /= selection.length;
        selection.length == 0 ? avy = -1 : avy /= selection.length;

        if ( avx < 0 && avy < 0 ) return;
        
        //get node(s) to align with
        var xAlignNode = null;
        var yAlignNode = null;

        var nodes = model.nodes;

        for ( var i = 0; i < nodes.length; i++ ) {
            if ( Util.checkContainment( selection, nodes[i] ) )
                continue;

            if ( nodes[i].x == avx ) {
                xAlignNode = nodes[i];
            }

            if ( nodes[i].y == avy ) {
                yAlignNode = nodes[i];
            }
        }

        this.horizontalAlign = ( yAlignNode != null ?  yAlignNode.y : -1 );
        this.verticalAlign =   ( xAlignNode != null ?  xAlignNode.x : -1 );
    }

    this.drawRulers = function( canvas ) {
        this.removeRulers();

        if ( this.horizontalAlign > -1 ) {
            this.horizontalRuler = canvas.path("M0 " + (this.horizontalAlign + 3) +"L10000 " + (this.horizontalAlign + 3));
            this.horizontalRuler.attr( { 
                stroke:Util.COLOR_LIGHTBLUE,
                "stroke-width": 3,
                "stroke-dasharray": "."
            })
        }
        if ( this.verticalAlign > -1 ) {
            this.verticalRuler = canvas.path( "M" + (this.verticalAlign + 3) + " 0L" + (this.verticalAlign + 3) + " 10000");
            this.verticalRuler.attr( {
                stroke:Util.COLOR_LIGHTBLUE,
                "stroke-width": 3,
                "stroke-dasharray": "."
            })
        }
    }

    this.isVerticallyAligned = function() {
        return this.verticalAlign > - 1;
    }

    this.isHorizontallyAligned = function() {
        return this.horizontalAlign > -1;
    }

    this.removeRulers = function() {
        if ( this.horizontalRuler )
            this.horizontalRuler.remove();
        if ( this.verticalRuler )
            this.verticalRuler.remove();
    }
}


