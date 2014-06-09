Ext.namespace("Inubit.Plugins");

Inubit.Plugins.DialogPlugin = Ext.extend( Inubit.Plugins.Plugin, {
    constructor : function(config) {
        Ext.Ajax.request({
            method: "GET",
            url: config.uri + "/js",
            success: function( response, options ) {
                var json = Ext.JSON.decode( response.responseText );
                this.panelClass = json.mainclass;
                Ext.require( json.files, function() {
                    Inubit.Plugins.DialogPlugin.superclass.constructor.call(this, config);
                }, this, true );
            },
            scope: this
        });
    },

    setMenuItemAction : function() {
        var listenerConfig = {
            click: function() {
                this.showWindow();
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
            me.showWindow();
            Util.stopEvent(event);
        }
    },

    showWindow : function() {
        this.item = eval("new " + this.panelClass + "()");
        this.window = new Ext.window.Window({
            closable: true,
            modal: true,
            items: this.item,
            title: this.item.getTitle(),
            buttons: [
                {
                    text: 'OK', 
                    handler: function() {
                        if(this.item.form.isValid()){
                            this.save();
                            this.window.close();
                        }
                    }, 
                    scope: this
                },
                {text: 'Cancel', handler: function() { this.window.close() }, scope: this}
            ],
            listeners: {
                close: function() {
                    ProcessEditor.instance.isDialogShown = false;
                }, scope: this
            }
        })

        this.load();
    },

    save : function() {
        var json = {};
        json.mi = Inubit.Plugins.PluginManager.getModelStateInfoJSON();

        json.data = this.item.getJSONData();
        Ext.Ajax.request({
            url: this.uri + "/save",
            method: 'POST',
            jsonData : json,
            success : function ( response, options ) {
                var json = Ext.decode( response.responseText );
                new PluginResponseProcessor().processJSONResponse(json);

                if ( Ext.isDefined( this.item.afterSave ) )
                    this.item.afterSave();
            },
            scope: this,
            failure : function( response, options ) {
                var obj = Ext.JSON.decode(response.responseText);
                new PluginResponseProcessor().processJSONResponse(obj);
            }
        });
    },

    load : function() {
        Ext.Ajax.request({
            url: this.uri + "/load",
            method: 'POST',
            jsonData: Inubit.Plugins.PluginManager.getModelStateInfoJSON(),
            success : function( response, options ) {
                var json = Ext.decode( response.responseText );
                this.item.setData( json.data  );
                this.window.show();

                //Chrome workaround
                if ( this.window.getWidth() > 3000 ) {
                    this.window.setPosition( this.window.getPosition()[0] + ( (this.window.getWidth() - 800) / 2 ), this.window.getPosition()[1] );

                    if ( this.item.getWidth() < 3000 )
                        this.window.setWidth( this.item.getWidth() + 10);
                    else
                        this.window.setWidth(800);

                    if ( this.window.getPosition()[0] < 0 ) {
                        this.window.setPosition(200, this.window.getPosition()[1]);
                    }
                }

                ProcessEditor.instance.isDialogShown = true;
            },
            scope: this,
            failure : function( response, options ) {
                var json = Ext.decode( response.responseText );
                new PluginResponseProcessor().processJSONResponse(json);
            }
        });
    }
});
