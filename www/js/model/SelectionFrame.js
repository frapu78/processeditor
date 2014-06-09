Ext.namespace('Inubit.WebModeler');

Inubit.WebModeler.SelectionFrame = Ext.extend(Object, {
    constructor: function (config) {
        this.object = config.object;
        this.graphics = null;
    },
    paint : function(canvas, color) {
        this.remove();

        if (color == null)
            color = Util.COLOR_RED;

        var bounds = this.object.getFrameBounds();
        this.graphics = canvas.rect(bounds.x - 3, bounds.y - 3,
                            bounds.width + 12 , bounds.height + 12)
                            .attr({"stroke":color, "stroke-width":1.25});

        this.graphics.toFront();
    },
    toFront : function() {
        if (this.graphics)
            this.graphics.toFront();
    },
    remove : function() {
        if (this.graphics)
            this.graphics.remove();

        this.graphics = null;
    },
    moveBy : function(diffx, diffy) {
        if (this.graphics != null) {
            this.graphics.translate(diffx, diffy);
            this.graphics.toFront();
        }
    },
    resize : function(diffx, diffy) {

    	var newWidth = this.object.getFrameBounds().width + 12 + diffx;
        var newHeight = this.object.getFrameBounds().height + 12 + diffy;

        if (this.object.isBPMNLane()) {
        	if (this.object.getLaneHandler().isVertical()){
        		 newHeight = this.object.getFrameBounds().height + 12;
        	} else {
        		newWidth = this.object.getFrameBounds().width + 12;
        	}
        }

        this.graphics.attr({width: newWidth,
                                     height: newHeight});
    }
});