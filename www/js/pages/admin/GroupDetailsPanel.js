function GroupDetailsPanel(group) {
    this.uri = "users/groups/" + group;
    this.group = null;
    this.panel = null;

    this.memberPanel = null;
    this.subgroupPanel = null;

    this.getPanel = function() {
        return this.panel;
    }

    this.init = function() {
        this.fetchData();

        this.panel = new Ext.form.Panel({
            items: [
                   new Ext.form.field.Display({fieldLabel : 'Name', value: this.group.name}),
                   this.createMemberField(),
                   this.createSubgroupField()
            ],
            bbar: {
                items: [
                    this.createDeleteButton(),
                    this.createSubmitButton()
                ]
            },
            listeners: {
                afterrender: function(){
                    this.memberPanel.getStore().sort("text", 'ASC')
                    this.subgroupPanel.getStore().sort("text", 'ASC')
                }, scope: this
            }
        })
    }

    this.createMemberField = function() {
        this.memberPanel =  new Ext.tree.Panel({
            preventHeader: true,
            id: ' member-tree',
            height: 250,
            root: {
                text: 'Members',
                expanded: true
            },
            //since ExtJS 4.0.0 is not able to drop on the panel (though configured for) we have to show the root
            rootVisible: true,
            lines: false,
            autoScroll: true,
            viewConfig: {
                plugins: {
                    ptype: 'treeviewdragdrop',
                    allowContainerDrops: true,
                    appendOnly: true,
                    ddGroup: 'users',
                    enableDrag: false
                },
                listeners: {
                    beforedrop: function( htmlView, data, overModel ){
                        return (overModel.store.getById( data.records[0].getId() ) == null);
                    }
                }
            },
            multiSelect: true,
            tbar: {items: [ 
                new Ext.toolbar.TextItem({
                    text: 'Members'
                }),
                new Ext.panel.Tool({
                    baseCls: 'my-tools',
                    cls: 'x-tool x-tool-default x-box-item',
                    type: 'delete',
                    handler: function() {
                        var nodes = this.memberPanel.getSelectionModel().getSelection();

                        if ( nodes.length == 0 )
                            alert("No nodes selected")
                        else
                            while (nodes.length > 0)
                                nodes.pop().remove();
                    },
                    scope: this
                })
            ]}
        })

        var members = this.group.members;

        for (var i = 0; i < members.length; i++) {
            if (members[i] == "")
                continue;

            this.memberPanel.getRootNode().appendChild(
                {
                    text: members[i],
                    icon: Util.ICON_MAN,
                    leaf: true,
                    id: "M_" + members[i]
                }
            )
        }

        return this.memberPanel;
    }

    this.createSubgroupField = function() {
        this.subgroupPanel = new Ext.tree.TreePanel({
            root: {
                text: 'Subgroups',
                expanded: true
            },
            height: 250,
            //since ExtJS 4.0.0 is not able to drop on the panel (though configured for) we have to show the root
            rootVisible: true,
            lines: false,
            autoScroll: true,
            enableDD: true,
            viewConfig: {
                plugins: {
                    ptype: 'treeviewdragdrop',
                    allowContainerDrops: true,
                    appendOnly: true,
                    allowDrag: false,
                    ddGroup: 'groups'
                },
                listeners: {
                    beforedrop: function( htmlView, data, overModel ){
                        return (overModel.store.getById( data.records[0].getId() ) == null);
                    }
                }
            },
            multiSelect: true,
            tbar: {
                items: [ 
                    new Ext.toolbar.TextItem( {
                        text: 'Subgroups'
                    }),
                    new Ext.panel.Tool({
                        baseCls: 'my-tools',
                        cls: 'x-tool x-tool-default x-box-item',
                        type: 'delete',
                        handler: function() {
                            var nodes = this.subgroupPanel.getSelectionModel().getSelection();
                            while (nodes.length > 0)
                                nodes.pop().remove();
                        },
                        scope: this
                    })
                ]
            },
            style: {
                paddingTop: '10px'
            }
        })

        var members = this.group.subgroups;

        for (var i = 0; i < members.length; i++) {
            if (members[i] == "")
                continue;
            
            this.subgroupPanel.getRootNode().appendChild(
                {
                    text: members[i],
                    icon: Util.ICON_GROUP,
                    leaf: true,
                    id: "S_" + members[i]
                }
            )
        }

        return this.subgroupPanel;
    }

    this.createDeleteButton = function() {
         var button =  new Ext.Button({text : "Delete this group", icon: Util.ICON_DELETE});

         button.on("click", function() {
            alert("currenty not implemented");
         });

         return button;
    }

    this.createSubmitButton = function() {
        var button = new Ext.Button({text : "Save changes", icon: Util.ICON_SAVE});

        button.on("click", function() {
            var req = new XMLHttpRequest();
            req.open("PUT", this.uri, true);
            req.send(this.createGroupUpdateXML());
        }, this);

        return button;
    }

    this.fetchData = function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri, false);
        req.send(null);

        this.group = Util.parseProperties(req.responseXML.getElementsByTagName("group")[0]);

        this.group.members = this.group.members.split(",");
        this.group.subgroups = this.group.subgroups.split(",");
    }

    this.setKeyMaps = function() {
        new Ext.KeyMap(this.memberPanel.id, {
            key: Ext.EventObject.DELETE,
            fn: function() {
                var nodes = this.getSelectionModel().getSelectedNodes();
                while (nodes.length > 0)
                    nodes.pop().remove();
            },
            scope: this.memberPanel
        });

        new Ext.KeyMap(this.subgroupPanel.id, {
            key: Ext.EventObject.DELETE,
            fn: function() {
                var nodes = this.getSelectionModel().getSelectedNodes();
                while (nodes.length > 0)
                    nodes.pop().remove();
            },
            scope: this.subgroupPanel
        })
    }

    this.createGroupUpdateXML = function() {
        var xml = "<group>";
        xml += "<property name='members' value='";
        
        var users = new Array();
        var userNodes =this.memberPanel.getRootNode().childNodes;
        for (var i = 0; i < userNodes.length; i++) 
            users.push(userNodes[i].get('text'));

        xml += users.join(",");
        xml += "'/>";
        xml += "<property name='subgroups' value='";

        var groups = new Array();
        var groupNodes = this.subgroupPanel.getRootNode().childNodes;

        for (var i = 0; i < groupNodes.length; i++)
            groups.push(groupNodes[i].get('text'));

        xml += groups.join(",");

        xml += "'/>";
        xml += "</group>";
        return xml;
    }

    this.init();

}