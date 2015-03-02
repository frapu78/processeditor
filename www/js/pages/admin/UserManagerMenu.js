function UserManagerMenu(manager) {
    this.toolbar = null;
    this.manager = manager;

    this.init = function() {
        this.toolbar = new Ext.Toolbar({
            items: [
                this.createNewUserButton(),
                {xtype: 'tbseparator'},
                this.createNewGroupButton()
            ]
        });
    }

    this.getToolbar = function() {
        return this.toolbar;
    }

    this.createNewUserButton = function() {
        var button = new Ext.Button({
            text: 'User',
            icon: Util.ICON_PLUS
        });

        button.on("click", function() {
            var cancelButton = new Ext.Button({text: 'Cancel'});
            var submitButton = new Ext.Button({text: 'Register'});
            var registerPanel = new RegisterUserPanel([cancelButton, submitButton]);
            var win = new Ext.Window({
                id: 'regwin',
                title: "Register new User",
                autoHeight: true,
                width: 350,
                header: true,
                closable: true,
                closeAction: 'close',
                items: [registerPanel.getPanel()]
            });

            cancelButton.on("click", function() {win.close()} );
            submitButton.on("click", function() {
                registerPanel.register( this.processUserCreationResponse, this);
//               var code = result.code;
//               var msg = "";
//               if (code == RegisterUserPanel.OK) {
//                   this.manager.addUser(result.user);
//                   win.close();
//                   return;
//               }
//               else if (code == RegisterUserPanel.EMPTY_USER)
//                   msg = 'Please enter a user name!';
//               else if (code == RegisterUserPanel.CHECK_PWD)
//                   msg = 'The entered passwords are not equal. Please type again!';
//               else if ( code == RegisterUserPanel.EMPTY_PWD)
//                   msg = 'Please enter a password!';
//               else if ( code == RegisterUserPanel.USER_EXISTS)
//                   msg = 'A user with the given name already exists!';
//               else if ( code == RegisterUserPanel.NAME_WITH_BLANKS)
//                   msg = 'Name must not contain blanks!';
//
//               Ext.Msg.show({
//                    title: 'Could not create user',
//                    msg: msg,
//                    buttons: Ext.Msg.OK,
//                    icon: Ext.Msg.ERROR
//               })

            }, this);

            win.doLayout();
            win.show(button);
        }, this);

        return button
    }

    this.processUserCreationResponse = function( response ) {
        var code = response.code;
        var msg = "";
        if (code == RegisterUserPanel.OK) {
            this.manager.addUser(response.user);
            Ext.getCmp("regwin").close();
            return;
        }
        else if (code == RegisterUserPanel.EMPTY_USER)
            msg = 'Please enter a user name!';
        else if (code == RegisterUserPanel.CHECK_PWD)
            msg = 'The entered passwords are not equal. Please type again!';
        else if ( code == RegisterUserPanel.EMPTY_PWD)
            msg = 'Please enter a password!';
        else if ( code == RegisterUserPanel.USER_EXISTS)
            msg = 'A user with the given name already exists!';
        else if ( code == RegisterUserPanel.NAME_WITH_BLANKS)
            msg = 'Name must not contain blanks!';
        else if ( code == RegisterUserPanel.INVALID_MAIL)
            msg = 'Please enter a valid e-mail address!';

        Ext.Msg.show({
            title: 'Could not create user',
            msg: msg,
            buttons: Ext.Msg.OK,
            icon: Ext.Msg.ERROR
        })
    }

    this.createNewGroupButton = function() {
        var button = new Ext.Button({
            text: 'Group',
            icon: Util.ICON_PLUS
        })

        button.on("click", function() {
            Ext.Msg.prompt('Create new user group', "Please enter the group's name:",
                function(btn, text) {
                    if (btn == 'ok') {
                        if (text.indexOf(" ") > -1 ) {
                            Ext.Msg.show({
                                title: 'Could not create group',
                                msg: "Name must not contain blanks",
                                buttons: Ext.Msg.OK,
                                icon: Ext.Msg.ERROR
                            });
                            return;
                        }

                        var req = new XMLHttpRequest();
                        req.open("POST", "/users/groups", false);

                        req.send(this.createGroupXML(text));

                        var xml = req.responseXML;

                        var success = xml.getElementsByTagName("success")[0].textContent;
                        if (success == 'true')
                            this.manager.addGroup(text);
                        else
                            Ext.Msg.show({
                                title: 'Could not create group',
                                msg: "A group with the given name already exists!",
                                buttons: Ext.Msg.OK,
                                icon: Ext.Msg.ERROR
                            });
                    }
                }, this)
        }, this)

        return button;
    }
    this.createGroupXML = function( name )  {
        return "<group name='" + name + "'/>";
    }

    this.init();
}


