function RegisterUserPanel(buttons) {
    this.panel = null;

    var width = 220
    this.nameField = new Ext.form.TextField({fieldLabel: '*Name', name: 'alias'});
    this.pwdField = new Ext.form.TextField({fieldLabel: '*Password', inputType: 'password', name: 'pwd'});
    this.repeatPwdField = new Ext.form.TextField({fieldLabel: '*Repeat password', inputType: 'password'});

    this.mailField = new Ext.form.TextField({fieldLabel: '*E-mail', name: 'mail'});
    this.realField = new Ext.form.TextField({fieldLabel: 'Real name', name: 'realname'});
    this.picLoad = {
        xtype: 'filefield',
        name: 'pic',
        fieldLabel: 'Picture',
        labelWidth: 50,
        msgTarget: 'side',
        allowBlank: true,
        anchor: '100%',
        buttonText: 'Select Picture...'
    };


    this.init = function() {
        this.panel = Ext.create('Ext.form.Panel', {
            labelWidth: 200,
            fileUpload: true,
            method: 'POST',
            url: '/users/users',
            reader: null,
            defaults: {
                anchor: '100%',
                style: {
                    marginLeft: '3px',
                    marginRight: '3px',
                    marginTop: '3px'
                },
                labelStyle: "font-size:11px"
            },
            items: [
                this.nameField,
                this.pwdField,
                this.repeatPwdField,
                this.mailField,
                this.realField,
                this.picLoad
            ],
            buttons: buttons
        } )
    }

    this.getPanel = function() {
        return this.panel;
    }

    this.register = function( fn, scope ) {
        var name = this.nameField.getValue();
        var pwd = this.pwdField.getValue();
        var mail = this.pwdField.getValue();

        if (name == "")
            fn.call(scope , {code: RegisterUserPanel.EMPTY_USER});

        if (name.indexOf(" ") > -1)
            fn.call(scope , {code: RegisterUserPanel.NAME_WITH_BLANKS});

        if (pwd != this.repeatPwdField.getValue())
            fn.call(scope , {code: RegisterUserPanel.CHECK_PWD});

        if (pwd == "")
            fn.call(scope ,  {code: RegisterUserPanel.EMPTY_PWD});

        var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
        if (!emailPattern.test(mail))
            fn.call(scope , {code: RegisterUserPanel.INVALID_MAIL});

        if (this.panel.getForm())
                this.panel.getForm().submit({
                    waitMsg: 'Please wait...',
                    success: function() {
                        fn.call(scope , { code : RegisterUserPanel.OK, user : name });
                    },
                    failure: function() {
                        fn.call(scope, { code : RegisterUserPanel.USER_EXISTS });
                    }
                });
    }

    this.createRegisterXML = function( name, pwd ) {
        var xml = "<user name='" + name + "'>";
        xml += "<property name='pwd' value='" + pwd + "'/>";
        xml += "</user>"

        return xml;
    }

    this.init();
}

RegisterUserPanel.CHECK_PWD = 0;
RegisterUserPanel.USER_EXISTS = 1;
RegisterUserPanel.EMPTY_PWD = 2;
RegisterUserPanel.OK = 3;
RegisterUserPanel.EMPTY_USER = 4;
RegisterUserPanel.NAME_WITH_BLANKS = 5;
RegisterUserPanel.INVALID_MAIL = 6;


