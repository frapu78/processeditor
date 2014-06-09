Ext.define("Inubit.WebModeler.handler.ReferenceChooserMouseListener", {
    extend: 'Inubit.WebModeler.handler.ProcessViewerMouseListener',
    constructor : function(config) {
		this.nodeSelectionHandler = config.nodeSelectionHandler;
		Inubit.WebModeler.handler.ReferenceChooserMouseListener.superclass.constructor.call(this, config);
    },
    mouseDownOnNode : function(event, node) {
        this.updateMousePos(event);
        if (Ext.isDefined(this.nodeSelectionHandler)) {
                // propagate the selection to the selection handler.
                this.nodeSelectionHandler.handleNodeSelection(node);
        }
        return false;
    }
});