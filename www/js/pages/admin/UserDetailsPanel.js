function UserDetailsPanel(user) {
    this.uri = "users/users/" + user
    this.user = null;
    this.panel = null;

    this.nameField = new Ext.form.field.Display({
                       fieldLabel : 'Name',
                       style: {
                           paddingTop: '3px',
                           paddingBottom: '3px'
                       }
                   });
                   
    this.emailField = new Ext.form.TextField(
                        {
                            fieldLabel : 'E-mail',
                            style: {
                               paddingTop: '3px',
                               paddingBottom: '3px',
                               paddingLeft: '3px'
                            }
                   });
    this.fullNameField = new Ext.form.TextField(
                        {
                            fieldLabel : 'Full name',
                            style: {
                               paddingTop: '3px',
                               paddingBottom: '3px',
                               paddingLeft: '3px'
                            }
                   });

    this.fileModelBox = new Ext.form.Checkbox({
        boxLabel: "Is allowed to save models to the server's  file system"
    });

    var store = new Ext.data.Store({
            proxy:  {
                type: 'ajax',
                url: Util.getContext() + "/models/locations?user=" + user,
                reader: {
                    type: 'xml',
                    record: 'location',
                    fields: [
                        "id",
                        "name"
                    ]
                }
            },
            autoDestroy: true
//            autoLoad: true
        });

    this.homeLocationField = new Ext.form.ComboBox({
        store: store,
        hidden: true,
        mode: 'local',
        valueField: "name",
        displayField: "name",
        fieldLabel: 'Home location',
        style: {
            paddingLeft: '3px'
        }
    });
    

    this.getPanel = function() {
        return this.panel;
    }

    this.init = function() {
        this.fetchData();

        this.imgPanel = new Ext.Panel({
            border: false,
            layout: 'fit',
            items : [new Ext.Img({
                /*
                 * since Opera and Chrome do not automatically adjust Accept headers when
                 * an image is requested, we have to do this manually :(
                 */
                src: this.uri + "/img?time=" +  new Date(),
                maxWidth: 150,
                maxHeight: 200,
                autoWidth: true,
                autoHeight: true,
                style: {
                    marginLeft: '3px'
                }
            })],
            bbar: {
                   items: [new Ext.Button({
                    text: 'Change image',
                    handler: this.showImageUpload,
                    scope: this
                })]
            },
            style: {
                    paddingTop: '5px',
                    paddingBottom: '5px',
                    verticalAlign: 'bottom'
            },
            width: 150,
            height: 230
        });

        this.nameField.setValue( this.user.name );

        var items = [
                   this.nameField,
                   this.imgPanel,
                   this.fullNameField,
                   this.emailField,
                   this.fileModelBox,
                   this.homeLocationField
            ];

        if (this.user.mail) {
            this.emailField.setValue(this.user.mail);
        }

        if (this.user.realname) {
            this.fullNameField.setValue(this.user.realname);
        }

        if (this.user.filesave) {
            this.fileModelBox.setValue( this.user.filesave == "true");
        }

        if ( this.user.home ) {
            this.homeLocationField.setValue(this.user.home);
        }

        this.panel = new Ext.form.Panel({
            border: false,
            items: items,
            bbar: {
                items: [
                    this.createDeleteButton(),
                    this.createSubmitButton()
                ]
            }           
        })

//        this.panel.on( "afterrender",  function() { this.panel.getBody().doLayout(); }, this)
    }

    this.fetchData = function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, false);
        req.send(null);

        this.user = Util.parseProperties(req.responseXML.getElementsByTagName("user")[0]);
    }

    this.showImageUpload = function() {
        var formPanel = new Ext.form.FormPanel( {
                 title: 'Change user image',
                      labelWidth: 100,
                      fileUpload: true,
                      reader: null,
                      method: 'POST',
                      items:
                       new Ext.form.TextField({
                            id: 'image',
                            inputType: 'file',
                            name: 'pic',
                            fieldLabel: 'New image',
                       })                       
              } )

        var me = this;
        
        var win = new Ext.Window({
              autoHeight: true,
              autoWidth: true,
              plain: true,
              title: 'Select new image file...',
              closable: true,
              items: formPanel,
              buttons: [
                    new Ext.Button({text: 'Cancel', handler: function() {win.close()}}),
                    new Ext.Button({text: 'Upload', handler: function() {
                            formPanel.getForm().submit({
                                method: 'POST',
                                url: this.uri + "/img",
                                waitMsg: 'Uploading image...',
                                success: function(form, action) {
                                    this.imgPanel.items.get(0).getEl().dom.setAttribute("src", this.uri + "/img?time=" + new Date())
                                    this.panel.doLayout();
                                    win.close();
                                },
                                failure: function(form, action) {
                                    alert("Error occured");
                                    win.close();
                                },
                                scope: this})
                            }, scope: this
                    })
              ]
        })

        win.show( this.imgPanel );
    }

    this.createDeleteButton = function() {
         var button =  new Ext.Button({text : "Delete this user", icon: Util.ICON_DELETE});

         button.on("click", function() {
            alert("currenty not implemented");
         });

         return button;
    }

    this.createSubmitButton = function() {
        var button = new Ext.Button({text : "Save changes", icon: Util.ICON_SAVE});

        button.on("click", function() {
            if ( !this.fileModelBox.getValue() && this.homeLocationField.getValue() == "") {
                Ext.Msg.alert("Invalid configuration", "Since this user is not allowed to save models into the server's file system, you have to specify a home location!");
            } else {
                var req = new XMLHttpRequest();
                req.open("PUT", this.uri, true);
                req.send(this.createUpdateXML());
            }
        }, this);

        return button;
    }

    this.createUpdateXML = function() {
        var xml = "<update>";
        xml += "<property name='email' value='" + this.emailField.getValue() + "'/>";
        xml += "<property name='realname' value='" + this.fullNameField.getValue() + "'/>";
        xml += "</update>";
        return xml;
    }

    this.init();
}

