Ext.namespace("Inubit.Plugins")

Inubit.Plugins.FormPlugin = Ext.extend( Inubit.Plugins.SimplePlugin, {
    constructor : function( config ) {
        Inubit.Plugins.FormPlugin.superclass.constructor.call( this, config );
    },

    setMenuItemAction : function() {
        var listenerConfig = {
            click: function() {
                this.showForm();
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
        domNode.onmousedown = function(event) {
            me.showForm();
            Util.stopEvent(event);
        }
    },

    showForm : function() {
        Ext.Ajax.request({
            url : this.uri + "?form",
            method : "POST",
            jsonData : Inubit.Plugins.PluginManager.getModelStateInfoJSON(),
            success : function( response, options ) {
                var json = Ext.JSON.decode(response.responseText);
                var formConfig = json.form;

                formConfig.method = 'POST';
                formConfig.url = this.uri;
                formConfig.fileUpload = true;

                formConfig.items.push( {xtype: 'textfield', hidden:true, name: '_modelinfo_',value: Ext.JSON.encode( Inubit.Plugins.PluginManager.getModelStateInfoJSON() )}  );
                
                var closeButton = new Ext.Button({text: 'Cancel', handler: function() {win.close()}});

                var submitButton = new Ext.Button({
                    text: 'Submit',
                    formBind: true,
                    handler: function() {
                        form.getForm().submit({
                            waitMsg: 'Submitting form...',
                            success: function(form, action) {
                                var obj = Ext.JSON.decode(action.response.responseText);
                                win.close();
                                win.destroy();
                                new PluginResponseProcessor().processJSONResponse(obj);
                            },
                            failure: function(form, action) {
                                var obj = Ext.JSON.decode(action.response.responseText);
                                win.close();
                                win.destroy();
                                new PluginResponseProcessor().processJSONResponse(obj);
                            }
                        })
                    }
                });
                formConfig.buttons = [
                    closeButton,
                    submitButton
                ];
                Ext.QuickTips.init();
                var form = new Ext.form.FormPanel(formConfig);
                var win = new Ext.window.Window( {
                    header: false,
                    closable:false,
                    buttonAlign: 'right',
                    modal: true,
                    items: form,
                    listeners: {
                        close : function() {
                            ProcessEditor.instance.isDialogShown = false
                        }
                    }
                });

                win.show();

                //workaround for Chrome
                var panelWidth = form.getWidth();
                if (panelWidth > 3000) {
                    win.setPosition( win.getPosition()[0] + ( (win.getWidth() - 800) / 2 ), win.getPosition()[1] );
                    panelWidth = 800;
                }
                win.setWidth(panelWidth + 20);

                ProcessEditor.instance.isDialogShown = true;
                win.doLayout();
            },
            scope: this,
            failure : function( response, options ) {
                new PluginResponseProcessor().performErrorAction(response.responseText);
            }
        });
    }
});
