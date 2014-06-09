RootPageRightAdditionWindow = function( tree, userPanel, parent, title ) {
    this.tree = tree;
    this.userPanel = userPanel;

    this.userTree = null;
    this.groupTree = null;

    this.init  = function() {
        var win = new Ext.window.Window({
            height: 300,
            width: 150,
            layout: 'accordion',
            title: title,
            modal: true,
            items: [
                this.createUserTree(),
                this.createGroupTree()
                ],
                buttons: [
                    new Ext.button.Button({
                        text: 'Add',
                        icon: Util.getContext() + Util.ICON_PLUS,
                        width: 50,
                        handler: function() { this.processAddition(); win.close() },
                        scope: this}),
                    new Ext.button.Button({
                        text: "Close",
                        width: 50,
                        handler: function() { win.close() }
                    })
                ],
                buttonAlign: 'center'
                       
        })

        return win;
    }

    this.createUserTree = function() {
        var rootNode = {
            text: 'Users',
            expanded: true,
            children: []
        }
        
        var users = Util.getUsers();

        for (var i = 0; i < users.length; i++) {
            rootNode.children.push( {
                text: users[i],
                iconCls: "user-single",
                id: "SINGLE_USER_" + users[i],
                leaf: true
            })
        }
        
        this.userTree = new Ext.tree.TreePanel( {
            title: 'Users',
            root: rootNode,
            rootVisible: false,
            multiSelectlines: false,
            multiSelect: true
        });

        return this.userTree;
    }

    this.createGroupTree = function() {
        var rootNode = {
            text: 'Groups',
            expanded: true,
            children: []
        }
        
        var groups = Util.getGroups();
        for (var i = 0; i < groups.length; i++) {
            rootNode.children.push( {
                text: groups[i],
                iconCls: "user-group",
                id: "GROUP" + groups[i],
                leaf: true
            });
        }
        
        
        this.groupTree = new Ext.tree.TreePanel({
            title: 'Groups',
            root: rootNode,
            rootVisible: false,
            lines: false,
            multiSelect: true
        })

        return this.groupTree;
    }

    this.processAddition = function() {
        var selectedNodes = this.userTree.getSelectionModel().getSelection().concat(
                            this.groupTree.getSelectionModel().getSelection());

        this.userPanel.addUsers(this.tree, selectedNodes);
    }

    return this.init();
}