function InvitationDialog( model ) {
    this.model = model;

    this.window = null;
    this.users = null;
    this.guestAlias = null;
    this.guestReal = null;
    this.guestMail = null;
    this.datePicker = null;
    this.textBox = null;

    this.init = function () {

        this.datePicker = new Ext.form.DateField({
            fieldLabel: 'Invitation is valid until',
            editable: false,
            allowBlank: false,
            format: 'd. m. Y',
            minValue: Ext.Date.add(new Date(), Date.DAY, -1),
            labelWidth: 150,
            style: {
                marginLeft: '5px',
                marginRight: '5px'
            },
            value: new Date()
        })

        this.guestAlias = new Ext.form.TextField({
            allowBlank: false,
            fieldLabel: 'Alias',
            width: 200
        })

        this.guestReal = new Ext.form.TextField({
            allowBlank: false,
            fieldLabel: 'Real name',
            width: 200
        })

        this.guestMail = new Ext.form.TextField({
            allowBlank: false,
            fieldLabel: 'E-mail',
            width: 200
        })

        this.users = new Inubit.WebModeler.pages.root.UserSelectionPanel({
            title: '', 
            members: [], 
            allowAdd: true
        });

        this.textBox = new Ext.form.TextArea({
            width: 300,
            height: 270,
            autoScroll: true,
            border: false,
            value: InvitationDialog.DEFAULT_MAIL,
            style: {
                padding: '5px 5px 5px 5px',
                fontSize: '11px'
            }
        });

        this.tabbedPanel = new Ext.TabPanel({
            activeTab: 0,
            deferredRender: false,
            items: [
                {
                    xtype: 'form',
                    title: 'User',
                    items: this.users,
                    height: 130
                },
                {
                    xtype: 'form',
                    height: 130,
                    title: 'Guest',
                    items: [
                        this.guestAlias,
                        this.guestReal,
                        this.guestMail
                    ]
                }
            ],
            style: {
                paddingBottom: '5px'
            }
        })

        this.window = new Ext.Window({
            layout: 'border',
            title: 'Invite somebody to comment on this model',
            width: 700,
            height: 270,
            items: [
                { 
                    region: 'center',
                    labelWidth: 150,
                    layout: 'anchor',
                    defaults: {
                        anchor: '100%'
                    },
                    width: 250,
                    items: [this.tabbedPanel, this.datePicker],
                    buttons: [
                        new Ext.Button({
                            text: 'Cancel',
                            handler: function() {
                                this.window.close();
                            },
                            scope: this
                        }),

                        new Ext.Button({
                            text: 'Invite',
                            handler: function() {
                                this.sendInvitation();
                                this.window.close();
                            },
                            scope: this
                        })
                    ]
                },
                {
                    title: 'Message',
                    region: 'east',
                    width: 300,
                    layout: 'fit',
                    items: this.textBox
                }
            ]
            
        })

        this.window.on("close", function() {
            Ext.getBody().unmask();
        })
    }

    this.show = function() {
        if (this.window) {
            Ext.getBody().mask();
            this.window.show();
        }
    }

    this.sendInvitation = function() {
        var xml = '<invite>'
        
        var expire = this.datePicker.getValue();
        expire.setMinutes(59);
        expire.setHours(23);
        expire.setSeconds(59);

        xml += "<expire>" + Ext.Date.format(expire, "d.m.Y, H:i:s") + "</expire>";
        xml += "<text>" + Util.escapeString(this.textBox.getValue()) + "</text>";
        xml += "<model>"
        xml += "<id>" + this.model.baseId + "</id>";
        xml += "<version>" + this.model.version + "</version>";
        xml += "</model>"

        var knownUsers = this.users.getRootNode().childNodes;

        if (    this.guestAlias.getValue() != '' &&
                this.guestMail.getValue() != '' && 
                this.guestReal.getValue() != '') {

            xml += "<guest>"
            xml += "<name>" + this.guestAlias.getValue() + "</name>";
            xml += "<real>" + this.guestReal.getValue() + "</real>";
            xml += "<mail>" + this.guestMail.getValue() + "</mail>";
            xml += "</guest>"
        }

        for (var i = 0; i < knownUsers.length; i++) {
            if (knownUsers[i].get("iconCls") == 'user-single')
                xml += '<user>'
            else
                xml += '<group>'

            xml += knownUsers[i].get('text');

            if (knownUsers[i].get("iconCls") == 'user-single')
                xml += '</user>'
            else
                xml += '</group>'
        }



        xml += '</invite>'

        var req = new XMLHttpRequest();
        req.open("POST", this.model.uri + "/invite");
        req.send(xml);
    }


    this.init();
}

InvitationDialog.DEFAULT_MAIL = "Hi, \n\nI would like to invite you to comment on a process model. \n\nBest regards,";