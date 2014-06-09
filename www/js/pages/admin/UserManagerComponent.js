function UserManagerComponent() {
    var widthWest = 200;

    this.button = null;
    this.component = null;
    this.prototype = new AdminPageComponent();

    this.getButton = this.prototype.getButton;
    this.getComponent = this.prototype.getComponent;

    this.userPanel = null;
    this.groupPanel = null

    this.userSort = null;
    this.groupSort = null;

    this.init = function(width, height) {
        this.createBasicLayout(width, height);
        this.userPanel.getStore().sort('text', 'ASC');
        this.groupPanel.getStore().sort('text', 'ASC');
//        this.groupSort.doSort(this.groupPanel.root);

        return this.component;
    }

    this.createButton = function(id) {
        //if id is 0, the integer value is not taken as id
        id = "" + id;
        var button =  new Ext.Button({
            text: 'User Manager',
            enableToggle: true,
            icon: Util.getContext() + Util.ICON_GROUP,
            itemId: id
        })
        return button;
    }

    this.createBasicLayout = function(width, height) {
        var centerWidth = width - widthWest;
        this.userPanel = this.createUserPanel();
//        this.userSort = new Ext.tree.TreeSorter( this.userPanel, {});

        this.groupPanel = this.createGroupPanel();
//        this.groupSort = new Ext.tree.TreeSorter( this.groupPanel, {});

        this.setEvents();

        this.component = new Ext.Panel({
            width: width,
            maxWidth: width,
            height: height,
            maxHeight: height,
            border: false,
            layout: 'border',
            items: [
                {region: 'center',
                     id: 'center_region_um',
                     autoWidth: true,
                     height: height,
                     collapsible: false,
                     split: true,
                     title: 'Details',
                     style: 'background-color:"white"',
                     layout:  'anchor',
                     defaults: {
                         anchor: '100%'
                     }
                },
                {
                    region: 'west',
                    id: 'west_region_um',
                    height: height,
                    width: widthWest,
                    autoScroll: true,
                    layout: { type: 'vbox', align: 'stretch'},
                    items: [
                        this.userPanel,
                        this.groupPanel
                    ]
                }
            ]
        });
    }

    this.createUserPanel = function() {
        var users = Util.getUsers();

        var treeRoot = {
           text: 'Users',
           expanded: true,
           draggable: false,
           children: []
        };

        for (var i = 0; i < users.length; i++) {
            var user = users[i];

            treeRoot.children.push( {
                id: "U_" + user,
                text: user,
                iconCls: 'user-single',
                draggable: true,
                leaf: true
            } );
        }

        var tree = new Ext.tree.TreePanel({
            title: 'Users',
            flex: 1,
            rootVisible: false,
            lines: false,
            root: treeRoot,
            border: false,
            enableDD: true,
            viewConfig: {
                copy: true,
                plugins: {
                    ptype: 'treeviewdragdrop',
                    allowContainerDrops: true,
                    appendOnly: true,
                    enableDrop: false,
                    ddGroup: 'users'
                }
            },
            tools: [
                {
                   baseCls: 'my-tools',
                   cls: 'x-tool x-tool-default x-box-item',
                   type: 'plus',
                   handler: function() {
                       var registerPanel = new RegisterUserPanel();
                       var win = new Ext.Window({
                          id: 'regwin',
                          title: "Register new User",
                          autoHeight: true,
                          width: 350,
                          height: 250,
                          closable: true,
                          layout: 'anchor',
                          defaults: {
                              anchor: '100%, 100%'
                          },                          
                          items: [registerPanel.getPanel()],
                          buttons: [
                              { text: 'Cancel', handler: function() { win.close() }},
                              { text: 'Save', handler: function() {
                                    registerPanel.register( this.processUserCreationResponse, this);
                              }, scope: this}
                          ]
                       });
                       win.show();
                   }, scope: this
                }
            ]
        });

        tree.on("enddrag", function(tree, node, event) {
            if (!tree.getNodeById("U_" + node.text))
                tree.root.appendChild(new Ext.tree.TreeNode({
                    id: "U_" + node.text,
                    text: node.text,
                    icon: Util.ICON_MAN,
                    draggable: true,
                    leaf: true
                }))
        })

        return tree;
    }
    
    this.createGroupPanel = function() {
        var groups = Util.getGroups();

        var treeRoot = {
           text: 'Groups',
           expanded: true,
           children: [],
           draggable: false
        };

        for (var i = 0; i < groups.length; i++) {
            var group = groups[i];

            treeRoot.children.push( {
                id: "G_" + group,
                text: group,
                iconCls: 'user-group',
                draggable: true,
                leaf: true
            } );
        }

        var tree = new Ext.tree.TreePanel({
            title: 'Groups',
            flex: 1,
            rootVisible: false,
            lines: false,
            root: treeRoot,
            border: false,
            viewConfig: {
                copy: true,
                plugins: {
                    ptype: 'treeviewdragdrop',
                    allowContainerDrops: true,
                    appendOnly: true,
                    ddGroup: 'groups'
                }
            },
            tools: [ {
                baseCls: 'my-tools',
                cls: 'x-tool x-tool-default x-box-item',
                type: 'plus',
                handler: function() {
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
                                    this.addGroup(text);
                                else
                                    Ext.Msg.show({
                                        title: 'Could not create group',
                                        msg: "A group with the given name already exists!",
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.Msg.ERROR
                                    });
                            } 
                        }, this)
                }, scope: this
            }]
        })

        tree.on("enddrag", function(tree, node, event) {
            if (!tree.getNodeById("G_" + node.text))
                tree.root.appendChild(new Ext.tree.TreeNode({
                    id: "G_" + node.text,
                    text: node.text,
                    icon: Util.ICON_GROUP,
                    draggable: true,
                    leaf: true
                }))
        })

        return tree;
    }

    this.setEvents = function() {
        this.userPanel.on("itemclick", function(view, node) {
            this.component.getLayout().getLayoutItems()[0].removeAll();
            var userPanel = new UserDetailsPanel(node.get('text')).getPanel();
            this.component.getLayout().getLayoutItems()[0].add(userPanel);
        }, this)

        this.groupPanel.on("itemclick", function(view, node) {
            var gdp = new GroupDetailsPanel(node.get('text'))
            var panel = gdp.getPanel()
            this.component.getLayout().getLayoutItems()[0].removeAll();
            this.component.getLayout().getLayoutItems()[0].add(panel);
            this.component.getLayout().getLayoutItems()[0].doLayout();

            gdp.setKeyMaps();
        }, this)
    }

    this.addUser  = function( name ) {
        this.userPanel.getRootNode().appendChild( {
            text: name,
            iconCls: 'user-single',
            leaf: true
        });
        this.userPanel.doLayout();
        this.userPanel.getStore().sort('text', 'ASC');
    }

    this.addGroup = function( name )  {
        this.groupPanel.getRootNode().appendChild({
            text: name,
            iconCls: 'user-group',
            leaf: true
        });

        this.groupPanel.doLayout();
        this.groupPanel.getStore().sort('text', 'ASC');
    }
    
    this.processUserCreationResponse = function( response ) {
       var code = response.code;
       var msg = "";
       if (code == RegisterUserPanel.OK) {
           this.addUser(response.user);
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
    
    this.createGroupXML = function( name )  {
        return "<group name='" + name + "'/>";
    }
}