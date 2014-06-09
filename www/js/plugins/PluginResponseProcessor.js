function PluginResponseProcessor() {
    this.updater = null;
    
    this.processJSONResponse = function( json ) {
        var action = json.action;
        if (action == "OPEN") {
            this.performOpenAction(json.uri);
        } else if (action == "UPDATE") {
             this.performUpdateActionFromJSON(json);
        } else if (action == "INFO") {
            this.performInfoAction(json.infomsg);
        } else if (action == "ERROR") {
            this.performErrorAction(json.errormsg);
        } else if (action == "COPY") {
            this.performCopyToClipboardAction(json.copymsg);
        }
    }

    this.processXMLResponse = function( xml ) {
        var action = xml.getElementsByTagName("action")[0].textContent;

        if (action == "OPEN") {
            var uri = xml.getElementsByTagName("uri")[0];
            this.performOpenAction(uri);
        } else if (action == "UPDATE") {
            var difference = xml.getElementsByTagName("difference")[0];

            this.performUpdateAction( difference );
        } else if (action == "INFO") {
            var infomsg = xml.getElementsByTagName("infomsg")[0];

            this.performInfoAction(infomsg);
        } else if (action == "ERROR") {
            var msg = xml.getElementsByTagName("errormsg")[0];

            this.performErrorAction(msg);
        }
    }

    this.performOpenAction = function ( uri ) {
        window.open(uri);
    }

    this.performUpdateAction = function ( difference ) {
        if (this.updater == null)
            this.updater = new ModelUpdater();

        this.updater.applyDifference( difference );
    }

    this.performUpdateActionFromJSON = function( json ) {
        if (this.updater == null)
            this.updater = new ModelUpdater();

        this.updater.applyJSONDifference( json.data );
        if(json.updatemsg != null && json.updatemsg != "")
            this.performInfoAction(json.updatemsg);
    }

    this.performInfoAction = function ( msg ) {
        Ext.Msg.show({
            title: 'Information',
            msg: msg,
            icon: Ext.Msg.INFO,
            buttons: Ext.Msg.OK
        })
    }

    this.performErrorAction = function ( msg ) {
        Ext.Msg.show({
            title: 'Error',
            msg: msg,
            icon: Ext.Msg.ERROR,
            buttons: Ext.Msg.OK
        })
    }
    
     this.performCopyToClipboardAction = function (msg) {
         var copyWin = Ext.create('Ext.window.Window', {
            title: 'Copy to clipboard',
            height: 200,
            width: 400,
            layout: 'fit',
            bodyStyle: 'padding: 10px;',
            items: {
                xtype:'form',
                layout:'fit',
                border: false,
                items: [{ 
                    xtype: 'textarea',
                    value: msg,
                    listeners:{
                        focus: function(field, options) {
                            field.selectText();
                        }
                    }
                }],
                buttonAlign: 'center', 
                buttons:[{
                    text:'OK',
                    handler: function() {
                        closeCopyWin();
                    }
                }]
            }
        }).show();
        
        closeCopyWin = function() {
            copyWin.close();
            copyWin.destroy();
        }
        
     }
}

