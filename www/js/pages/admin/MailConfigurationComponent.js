function MailConfigurationComponent() {
    this.button = null;
    this.component = null;

    this.uri = "/mail";

    this.prototype = new AdminPageComponent;

    this.mailField = null;
    this.userField = null;
    this.pwdField = null;
    this.fromField = null;

    this.getButton = this.prototype.getButton;

    //"abstract" method, to be implemented by subclasses
    this.createButton = function(id) {
        return new Ext.Button({
            text: 'Mail server',
            itemId: id,
            icon: Util.getContext() + Util.ICON_MAIL
        })
    }

    this.getComponent = this.prototype.getComponent;

     //"abstract" method, to be implemented by subclasses
    this.init = function(width, height) {
        var data = this.getMailConfig();

        if (!data.user || !data.pwd) {
            data["user"] = "";
            data["pwd"] = "";
        }
        this.mailField = new Ext.form.TextField({
            fieldLabel: 'Server',
            value: data.host
        });

        this.userField = new Ext.form.TextField({
            fieldLabel: 'User name',
            value: data.user
        })

        this.pwdField = new Ext.form.TextField({
            inputType: 'password',
            fieldLabel: 'Password',
            value: data.pwd
        })

        this.fromField = new Ext.form.TextField({
            fieldLabel: 'From address',
            value: data.from
        })

        this.component = new Ext.form.Panel({
            title: 'Mail server configuration',
            defaults: {
                style: {
                    marginLeft: '10px',
                    marginTop: '5px'
                },
                width: 300
            },
            items: [
                this.mailField,
                this.fromField,
                this.userField,
                this.pwdField,
                new Ext.Button({
                    text: 'Save',
                    width: 55,
                    icon: Util.ICON_SAVE,
                    handler: function() {
                        this.saveMailConfig();
                    },
                    scope: this
                })
            ]
        })
        return this.component;
    }

    this.getMailConfig = function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, false);
        req.send(null);

        var xml = req.responseXML;
        return Util.parseProperties(xml.getElementsByTagName("mail")[0]);
    }

    this.saveMailConfig = function() {
        var req = new XMLHttpRequest();
        req.open("PUT", this.uri, true);

        var xml = "<mail>";

        if (this.mailField.getValue() == "")
            return;

        xml += "<property name='host' value='" + this.mailField.getValue() + "'/>";
        xml += "<property name='from' value='" + this.fromField.getValue() + "'/>";

        if (this.userField.getValue() != "" && this.pwdField.getValue() != "") {
            xml += "<property name='user' value='" + this.userField.getValue() + "'/>";
            xml += "<property name='pwd' value='" + this.pwdField.getValue() + "'/>";
        }

        xml += "</mail>";
        req.send(xml);
    }
}