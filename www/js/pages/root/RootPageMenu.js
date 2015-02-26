function RootPageMenu(user, portletMode) {
    var importID = 'import_button'
    var connectID = 'connect_button'
    var cWindowID = 'connct_window'
    var newID = 'new_button'
    var closeID = 'close_button'
    var submitID = 'submit_button'
    var windowID = 'import_window'
    var formID = 'import_form'
    var cFormID = 'connect_form'

    this.toolbar = null;
    this.portletMode = portletMode;
    this.user = user;

    this.createToolbar = function() {
        var req = new XMLHttpRequest();
        req.open("GET", Util.getContext() + "/utils/modeltypes", false);
        req.send(null);

        var classes = req.responseXML.getElementsByTagName("modelclass");
        var items = new Array;
        var rpm = this;
        for (var i = 0; i < classes.length; i++) {
            var value = classes[i].getElementsByTagName("class")[0].textContent;
            var name = classes[i].getElementsByTagName("name")[0].textContent;

            var item = new Ext.menu.Item({text: name, itemId: value});

            item.on("click", function() {
                if ( rpm.portletMode )
                    window.location = "models/new?type=" + this.getItemId();
                else
                    window.open("models/new?type=" + this.getItemId());
            })
            
            items.push(item);
        }

        // @todo: Needs to be generated automatically!!!
        var newMenu = new Ext.menu.Menu({
            items: items
        });
        this.toolbar = new Ext.toolbar.Toolbar({
            items: [
                     {id: newID, icon: Util.getContext() + Util.ICON_NEW, text: "New", menu: newMenu},
                     {xtype: 'tbseparator'},
                     this.createImportButton(),
                     {xtype: 'tbseparator'},
                     this.createConnectButton(),
                     {xtype: 'tbseparator'},
                     this.createAdminButton()
                  ]
        })
        
        Ext.Ajax.request({
        	method: 'GET',
        	url: 'plugins/rootpage',
        	success: function(resp) {
        		var plugs = Ext.decode(resp.responseText).plugins
        		while ( plugs.length > 0 ) 
        			this.toolbar.add( {xtype: 'tbseparator'}, plugs.pop() );

                /* don't add inubit logo
        		this.toolbar.add(
        				{xtype: 'tbseparator'},
                        this.createLogoutButton(),
                        '->',
                        new Ext.toolbar.Item({autoEl: {tag: 'img', src: Util.getContext() + Util.IMG_INUBIT, height: 15}}) 
				);
				*/
        	},
        	scope: this
        })
        
    }

    this.createImportButton = function() {
        var button = new Ext.button.Button({id: importID, icon: Util.getContext() + Util.ICON_IMPORT, text: 'Import'});

        button.on("click", function() {
           var win =  new Ext.Window({
              id: windowID,
              title: 'Import Model',
              layout: 'fit',
              closable: false,
              resizable: false,
              items: this.createImportForm()
            });

            this.setImportEvents();

            win.show();
            win.doLayout();
        }, this);

        return button;
    }

    this.createConnectButton = function() {
        var button = new Ext.button.Button({id: connectID, icon: Util.getContext() + Util.ICON_CONNECT, text: 'Connect IS'});

        button.on("click", function() {
            alert("WARNING:  + functionality currently under construction");
            var dialog = new ISConnectionDialog();
            dialog.show(this);
        });

        return button;
    }

    this.createAdminButton = function() {
        return new Ext.button.Button({
            icon: Util.getContext() + Util.ICON_KEY,
            disabled: this.user.isadmin != 'true',
            text: 'Admin',
            handler: function() {
                window.location = "admin";
            }
        })
    }

    this.createLogoutButton = function() {
        var button = new Ext.button.Button({id: 'logout', text: 'Logout', icon:  Util.getContext() + Util.ICON_LOGOUT});

        button.on("click", function() {
            Util.logoutCurrentUser();
        });

        return button;
    }

    this.createImportForm = function() {
        var closeButton = new Ext.button.Button({id: closeID, text:'Cancel'});
        var submitButton = new Ext.button.Button({
            id: submitID,
            formBind: true, //only enabled once the form is valid
            text: 'Import'
        });

        var formPanel = new Ext.FormPanel({
                      id: formID,
                      labelWidth: 100,
                      width:400,
//                      height:50,
//                      autoWidth: true,
                      fileUpload: true,
                      method: 'POST',
                      url: Util.getContext() + '/models',
                      reader: null,
                      items:[
                       new Ext.form.TextField({
                            id: 'filefield',
                            inputType: 'file',
                            name: 'uploadfile',
                            fieldLabel: 'Import file',
                            autoWidth: true,
                            allowBlank: false
                       })],
                       buttons: [
                            closeButton,
                            submitButton
                       ]
        });

        return formPanel;
    }

    this.setImportEvents = function() {
        var closeButton = Ext.getCmp(closeID);
        var submitButton = Ext.getCmp(submitID);
        var win = Ext.getCmp(windowID);
        var formPanel = Ext.getCmp(formID);

        closeButton.on("click", function() {win.close()});
        //on submit open page with new model or display error message
        submitButton.on("click", function() {
            if (formPanel.getForm())
                formPanel.getForm().submit({
                    waitMsg: 'Uploading model...',
                    success: function(form, action) {
                        var obj = Ext.JSON.decode(action.response.responseText);

                        win.close();
                        if ( this.portletMode )
                            window.location( Util.getPath( obj.url ) );
                        else {
                            window.open(Util.getPath(obj.url));
                            window.location.reload();
                        }
                    },
                    failure: function() {
                        Ext.Msg.show({
                            title: 'Error occured on upload!',
                            msg: 'This is no valid model file.',
                            icon: Ext.Msg.ERROR,
                            buttons: Ext.Msg.OK
                        })
                    }
                }, this);
        }, this);
    }

    this.getToolbar = function() {
        return this.toolbar;
    }

    this.createToolbar();
}