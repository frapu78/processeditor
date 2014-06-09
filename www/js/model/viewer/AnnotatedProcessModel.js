Ext.namespace("Inubit.WebModeler.model.viewer");

Inubit.WebModeler.model.viewer.AnnotatedProcessModel = Ext.extend(Inubit.WebModeler.ProcessModel, {
    constructor : function (config) {
    	this.commentCount = 0;
        Inubit.WebModeler.model.viewer.AnnotatedProcessModel.superclass.constructor.call(this, config);
        this.model = this;
        this.loadProperties();
        this.applyNodeHierarchy();
        this.resolveEdgeEnds();
        this.alignZOrder();
    },
    drawCommentHighlight : function() {
        return false;
    },
    dropCommentHighlight : function() {
        return false;
    },
    getCommentURI : function() {
        return this.uri + "/comments";
    }
});
