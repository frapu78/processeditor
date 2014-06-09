Ext.namespace('Inubit.WebModeler');

/**
 * Simple version of the node context menu which only creates a comment and a 
 * reference button.
 * 
 * @class Inubit.WebModeler.ViewNodeContextMenu
 * @extends Inubit.WebModeler.NodeContextMenu
 */
Inubit.WebModeler.ViewNodeContextMenu = Ext.extend(Inubit.WebModeler.NodeContextMenu, {
	constructor : function(config) {
		Inubit.WebModeler.ViewNodeContextMenu.superclass.constructor.call(this, config);
	},
	 createContextButtons : function() {
        var x = this.object.getFrameBounds().x;
        var y = this.object.getFrameBounds().y;

        this.createCommentButton(x, y);
    },
    addPluginContextButtons: function(x,y){
        return false;
    },
    getViewMode : function() {
    	return true;
    }
});
