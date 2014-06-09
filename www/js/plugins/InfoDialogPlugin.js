Ext.namespace("Inubit.Plugins");

Inubit.Plugins.InfoDialogPlugin = Ext.extend( Inubit.Plugins.DialogPlugin, {
    constructor : function( config ) {
        Inubit.Plugins.InfoDialogPlugin.superclass.constructor.call( this, config );
    },

    setMenuItemAction : function() {
        var listenerConfig = {
            click: function() {
                this.load();
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
        domNode.onmousedown = function( event ) {
            me.load();
            Util.stopEvent(event);
        }
    },

    load : function() {
        if ( this.item == null ) {
            this.item = eval("new " + this.panelClass + "()");
            if ( this.item instanceof Ext.ux.InfoDialog )
                this.item.setPlugin(this);
        
        
	        Ext.Ajax.request({
	            url: this.uri + "/load",
	            method: 'POST',
	            jsonData: Inubit.Plugins.PluginManager.getModelStateInfoJSON(),
	            success : function( response, options ) {
	                var json = Ext.decode( response.responseText );
	                this.item.setData( json.data  );
	                ProcessEditor.instance.addSouthPanel(this.item, this.item.getTitle());
	            },
	            failure : function( response, options ) {
	                var json = Ext.util.JSON.decode(response.responseText);
	                new PluginResponseProcessor().processJSONResponse(json);
	            },
	            scope : this
	        });
        } else {
        	ProcessEditor.instance.showSouthPanel();
        	this.item.show();
        	this.refreshData();
        }
    },

    refreshData : function() {
        Ext.Ajax.request({
            url: this.uri + "/load",
            method: 'POST',
            jsonData: Inubit.Plugins.PluginManager.getModelStateInfoJSON(),
            success: function( response, options ) {
                var json = Ext.decode( response.responseText );
                this.item.setData( json.data  );
            },
            failure : function( response, options ) {
                var json = Ext.util.JSON.decode(response.responseText);
                new PluginResponseProcessor().processJSONResponse(json);
            },
            scope: this
        });
    }
});