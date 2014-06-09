Ext.namespace("Inubit.Plugins");

Inubit.Plugins.SimplePlugin = Ext.extend( Inubit.Plugins.Plugin, {
    constructor : function(config) {
        Inubit.Plugins.SimplePlugin.superclass.constructor.call( this, config );
    },

    setMenuItemAction : function () {
        var listenerConfig = {
            click: function() {
                this.performAction();
            },
            scope: this
        }

        if ( this.menuItem.listeners != null )
            this.menuItem.listeners = Ext.apply( listenerConfig, this.menuItem.listeners );
        else
            this.menuItem.listeners = listenerConfig;
    },

    bindActionToDomNode : function( domNode ) {
        var me = this;
        domNode.onmousedown = function() {
            me.performAction();
        }
    },

    performAction : function() {
        Ext.Ajax.request({
            url: this.uri,
            method: "POST",
            jsonData: Inubit.Plugins.PluginManager.getModelStateInfoJSON(),
            success: function( response, options ) {
                var json = Ext.decode(response.responseText);
                new PluginResponseProcessor().processJSONResponse(json);
            },
            failure : function( response, options ) {
                var json = Ext.decode(response.responseText);
                new PluginResponseProcessor().processJSONResponse(json);
            }
        });
    }
});