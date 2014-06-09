Ext.namespace('Inubit.WebModeler.model.viewer');

Inubit.WebModeler.model.viewer.AnnotatedProcessEdge = Ext.extend(Inubit.WebModeler.ProcessEdge, {
    constructor: function(config) {
        this.commentSign = null;
        this.comments = new Array();

        Inubit.WebModeler.model.viewer.AnnotatedProcessEdge.superclass.constructor.call(this, config);
        this.textNode = new TextNode(this.uri + "/label", this);
    },
    drawCommentHighlight : function(canvas) {
        if (this.hasComments()) {
            var x = 0;
            var y = 0;
            if (this.points.length < 3) {
                x = (this.points[0].x + this.points[1].x) / 2 + 3.5;
                y = (this.points[0].y + this.points[1].y) / 2 + 3.5;
            } else {
                x = this.points[1].x + 3.5;
                y = this.points[1].y + 3.5;
            }

            this.commentSign = canvas.circle(x, y, 5).attr({fill: Util.COLOR_BLUE, stroke: Util.COLOR_BLUE});
        }
    },
    setSelected : function(selected, showMenu, color) {
        Inubit.WebModeler.model.viewer.AnnotatedProcessEdge.superclass.setSelected.call( this, selected, showMenu, color, ProcessViewer.instance );
    },
    alignZOrder : function() {
        Inubit.WebModeler.model.viewer.AnnotatedProcessEdge.superclass.alignZOrder.call(this);

        if (this.commentSign)
            this.commentSign.toFront();
    },
    /**
     *  Check if a certain point is in short distance to this edge
     */
    select : function() {
        ProcessViewer.instance.mouseListener.mouseDownOnEdge(this);
    },
//    handleSelect : Inubit.WebModeler.ProcessEdge.select,
    hasComments : function() {
        return this.comments.length != 0;
    },
    /**
     * LOAD UTILITIES
     */
    init : function(xml, canvas) {
        var properties = Util.parseProperties(xml);
        this.properties = properties;
        this.source = properties["#sourceNode"];
        this.target = properties["#targetNode"];

        this.updateMetadata();
        this.textNode.paint(canvas);
        this.paint(canvas);

//        this.loadFinished();
        this.fireEvent("load");
    },
    /**
     * "ABSTRACT" METHODS
     */
    bindEventsToEdge : function() {
    }
});