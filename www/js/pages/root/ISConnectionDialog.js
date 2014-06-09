function ISConnectionDialog() {
    var cWindowID = 'connect_window';
    var cFormID = 'connect_form';
    var closeID = 'close_button';
    var submitID = 'submit_button';
    this.window = null;
    
    this.init = function() {
        this.window = new Ext.Window({
            id: cWindowID,
            autoHeight: true,
            autoWidth: true,
            plain: false,
            closeable:false,
            closeAction:'hide',
            items: this.createConnectForm()
        });

        this.setSubmitAction();
    }

    this.show = function(cmp) {
        this.window.show(cmp);
        this.window.doLayout();
    }

    this.createConnectForm = function() {
        var closeButton = new Ext.Button({id: closeID, text:'Cancel'});
        var submitButton = new Ext.Button({id: submitID, text: 'Connect'});

        closeButton.on("click", function() {
            this.window.close();
        }, this)

        var formPanel = new Ext.FormPanel({
            id: cFormID,
            title: 'Connect to IS',
            labelWidth: 80,
            frame: true,
            autoWidth: true,
            fileUpload: true,
            method: 'POST',
            url: 'models/locations',
            reader: null,
            items: [
                new Ext.form.TextField({
                    id: 'urlfield',
                    name: 'isurl',
                    fieldLabel: 'URL',
                    width: 300,
                    value: ISConnectionDialog.DEFAULT_URL
               }),
               new Ext.form.TextField({
                   id: 'userField',
                   name: 'username',
                   fieldLabel: 'Username',
                   width: 300,
                   value: 'root'
               }),
               new Ext.form.TextField({
                   id: 'pwdField',
                   inputType: 'password',
                   name: 'passwd',
                   fieldLabel: 'Password',
                   width: 300,
                   value: 'inubit'
               })
            ],
            buttons: [
                closeButton,
                submitButton
            ]
        })

        return formPanel;
    }

    this.setSubmitAction = function() {
        var formPanel = Ext.getCmp(cFormID);
        var submitButton = Ext.getCmp(submitID);
        var win = this.window;
        submitButton.on('click', function() {
            if (formPanel.getForm())
                formPanel.getForm().submit({
                    waitMsg: 'Establishing Connection...',
                    success: function(form, action) {
                        var obj = Ext.util.JSON.decode(action.response.responseText);
                        //window.open(Util.getPath(obj.url));
                        win.close();
                        window.location.reload();
                        //@Todo: separate reload of tree
                    },
                    failure: function() {
                        Ext.Msg.show({
                            title: 'Connectivity problem!',
                            msg: 'Could not connect to IS.',
                            icon: Ext.Msg.ERROR,
                            buttons: Ext.Msg.OK
                        })
                    }
                });
        }, this);

    }

    this.init();
}

ISConnectionDialog.DEFAULT_URL = 'http://localhost:8000/ibis/rest/rc/models';

