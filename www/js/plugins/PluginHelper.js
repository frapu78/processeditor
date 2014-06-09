/*
 * Process Editor
 *
 * (C) 2011 inubit AG
 *
 * http://inubit.com
 *
 */

Ext.namespace("Inubit.Plugins");

Inubit.Plugins.PluginHelper = Ext.extend( Object, {
});

Inubit.Plugins.PluginHelper.selectObjects = function( nodeIDs, edgeIDs, color, scrollIntoView ) {
    var modelSelectionHandler = ProcessEditor.instance.getSelectionHandler();
    var model = ProcessEditor.instance.getModel();
    if (modelSelectionHandler) {
        modelSelectionHandler.dropSelection();
        var i
        for (i=0; i<nodeIDs.length; i++) {
            var node = model.getNodeWithId(nodeIDs[i]);
            modelSelectionHandler.addSelectedNode(node);
            node.setSelected(true, false, color);
            if ( scrollIntoView && i == 0) {
                ProcessEditor.instance.scrollToNodeById(nodeIDs[i]);
            }
        }

        for (i=0; i<edgeIDs.length; i++) {
            var edge = model.getEdgeWithId(edgeIDs[i]);
            modelSelectionHandler.addSelectedEdge(edge);
            edge.setSelected(true, false, Util.COLOR_RED, ProcessEditor.instance);
            if ( scrollIntoView && nodeIDs.length == 0 && i == 0) {
                ProcessEditor.instance.scrollToEdgeById(edgeIDs[i]);
            }
        }
    }
}

Inubit.Plugins.PluginHelper.getSelectedNodes = function() {
    return ProcessEditor.instance.getSelectionHandler().getSelectedNodes();
}

Inubit.Plugins.PluginHelper.getEditorCanvas = function() {
    return ProcessEditor.instance.getCanvas();
}