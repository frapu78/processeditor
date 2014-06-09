Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.EdgeContextMenu = Ext.extend(Inubit.WebModeler.ContextMenu, {
    constructor : function(config) {
        Inubit.WebModeler.EdgeContextMenu.superclass.constructor.call(this, config);
    },
    createContextButtons : function() {
        var bounds = this.object.getFrameBounds();

        this.createDeleteButton(bounds.x, bounds.y);
        this.createRefactorButton(bounds.x, bounds.y);
    },
    updatePosition : function() {
        var bounds = this.object.getFrameBounds();

        for (var i = 0; i < this.contextButtons.length; i++) {
            var button = this.contextButtons[i];

            button.setPosition(bounds.x + this.getXOffset(button.getItemId()),
                               bounds.y + this.getYOffset(button.getItemId()));
        }
    }

});
