Ext.define("Inubit.WebModeler.LoginDialog", {
	extend: 'Ext.panel.Panel',
	redirectPath: '',
	layout: 'anchor',
	title: 'Login to Modeler',
	id: 'login-dialog',
	
	
	initComponent: function() {
		var search = window.location.search;
        
        if (search.indexOf("?") > -1) {
            var queryParams = Util.parseParameters(search.substring(search.indexOf("?") + 1));

            if (queryParams.redirect)
                this.redirectPath = queryParams.redirect;
        }
		
		this.pwdField = new Ext.form.field.Text({
			inputType: 'password',
			fieldLabel: 'Password'
		});
		
		this.nameField = new Ext.form.field.Text({
			fieldLabel: 'Name',
			style: {
				paddingTop: '10px'
			}
		});
		
		this.rememberCheck = new Ext.form.field.Checkbox({
			checked: true,
			boxLabel: 'Remember login'
		});
		
		this.items = [
          this.nameField,
          this.pwdField,
          this.rememberCheck
        ]
		
		this.bbar = {items: [ '->',
            { 
            	text: 'Login',
            	icon: Util.getContext() + Util.ICON_LOGIN,
            	handler: function() {
            		this.login();
            	},
            	scope: this
            }
        ]}
		
		this.on( "afterrender", function() {
			new Ext.util.KeyMap("login-dialog", {
	            key: Ext.EventObject.ENTER,
	            fn: this.login,
	            scope: this
	        });
			
			if ( Util.readCookie("wm-loginName") != null )
				this.nameField.setValue(Util.readCookie("wm-loginName"));
			if ( Util.readCookie("wm-passwd") != null )
				this.pwdField.setValue(Util.readCookie("wm-passwd"));
		}, this)
		
		Inubit.WebModeler.LoginDialog.superclass.initComponent.call( this )
	},
	
	login: function() {
		Ext.Ajax.request({
            method: "POST",
            url: "users/login",
            jsonData: { name: this.nameField.getValue(), pwd: this.pwdField.getValue() },
            success: function(response, options) {
                if ( this.rememberCheck.getValue() ) {
                	document.cookie = "wm-loginName="+this.nameField.getValue()+"; expires="+(new Date(2099,12,31)).toGMTString();
                	document.cookie = "wm-passwd="+this.pwdField.getValue()+"; expires="+(new Date(2099,12,31)).toGMTString();
                }
                window.location = '.' + this.redirectPath;
            },
            failure: function() {
                Ext.Msg.show({
                   title:'Login failed',
                   msg: 'The given user name does not exist, or the password is not correct!',
                   buttons: Ext.Msg.OK,
                   icon: Ext.MessageBox.ERROR
                });
            },
            scope: this
        })
	}
});
