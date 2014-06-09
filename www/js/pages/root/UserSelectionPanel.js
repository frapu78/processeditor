Ext.define('Inubit.WebModeler.pages.root.UserSelectionPanel', {
    extend: 'Ext.tree.Panel',
    labelStyle: 'font-size: 11px',
    rootVisible: false,
    border: false,
    height: 128,
    autoScroll: true,
    lines: false,
    buttonAlign: 'left',
    selModel: new Ext.selection.Model({
        type: 'MULTI'
    }),
    
    constructor: function( config ) {
        this.addAllowed = config.allowAdd,
        this.members = config.members;
        
        Inubit.WebModeler.pages.root.UserSelectionPanel.superclass.constructor.call(this, config);
    },
    
    initComponent: function() {
        this.root = {
            text: 'root',
            expanded: true,
            children: this.getChildren(this.members)
        };
        
        this.bbar = {
            items: [
                {
                    icon: Util.getContext() + Util.ICON_PLUS,
                    disabled: !this.addAllowed,
                    handler: function() {
                        this.openMemberAdditionWindow();
                    }, scope: this
                },
                {
                    icon: Util.getContext() + Util.ICON_MINUS,
                    disabled: !this.addAllowed,
                    handler: function() {
                        this.deleteSelection();
                    }, scope: this
                }
            ]
        }
        Inubit.WebModeler.pages.root.UserSelectionPanel.superclass.initComponent.call(this);
    },
    
    getChildren : function (members) {
        var children = [];
        for (var i = 0; i < members.length; i++) {
            var icon = '../' + Util.ICON_MAN;

            if (members[i].type == "GROUP")
                icon = '../' + Util.ICON_GROUP;

            children.push( {
                icon: icon,
                text: members[i].name,
                id: members[i].type + "_" + members[i].name,
                typeId: members[i].type,
                leaf: true
            });
        }
        return children;
    },
    
    openMemberAdditionWindow : function() {
        var win = new RootPageRightAdditionWindow(this.tree, this, this.tree, 'Add users');
        win.show(this.tree);
    },
    
    addUsers : function(tree, selectedNodes) {
        var node = selectedNodes.pop();
        if (!this.getStore().getNodeById(node.getId())) {
            this.getRootNode().appendChild(node);
        }
    },

    deleteSelection : function() {
        var nodes = this.tree.getSelectionModel().getSelectedNodes();

        while (nodes.length > 0)
            nodes.pop().remove();
    }
});


function UserSelectionPanel( title , members, addAllowed ) {

    this.tree = null;

    this.init = function() {
        var config = {
            labelStyle: 'font-size: 11px',
            rootVisible: false,
            border: false,
            height: 128,
            autoScroll: true,
            lines: false,
            buttonAlign: 'left',
            root: {
                text: 'root',
                expanded: true,
                children: this.getChildren(members)
            },
            selModel: new Ext.selection.Model({
                type: 'MULTI'
            })
        };

        if (title) {
            config.fieldLabel = title;
            config.id = title.toLowerCase()
        }

        this.tree = new Ext.tree.Panel(config);
//        new Ext.tree.TreeSorter( this.tree, {});

//        this.addButtons( );
    }

    this.getChildren = function (members) {
        var children = [];
        for (var i = 0; i < members.length; i++) {
            var icon = '../' + Util.ICON_MAN;

            if (members[i].type == "GROUP")
                icon = '../' + Util.ICON_GROUP;

            children.push( {
                icon: icon,
                text: members[i].name,
                id: members[i].type + "_" + members[i].name,
                typeId: members[i].type,
                leaf: true
            });
        }
        return children;
    }

    this.addButtons = function ( ) {
        this.tree.addButton( {
                icon: '../' + Util.ICON_PLUS,
                disabled: !addAllowed
            }, function() {
                this.openMemberAdditionWindow();
            }, this
        );

        this.tree.addButton( {
                icon: '../' + Util.ICON_MINUS,
                disabled: !addAllowed
            }, function() {
                this.deleteSelection();
            },
            this
        );
    }

    this.openMemberAdditionWindow = function() {
        var win = new RootPageRightAdditionWindow(this.tree, this, this.tree, 'Add users');
        win.show(this.tree);
    }

    this.addUsers = function(tree, selectedNodes) {
        var node = selectedNodes.pop();
        if (!tree.getNodeById(node.id)) {
            tree.root.appendChild(new Ext.tree.TreeNode(
                node.attributes
            ));
        }
    }

    this.deleteSelection = function() {
        var nodes = this.tree.getSelectionModel().getSelectedNodes();

        while (nodes.length > 0)
            nodes.pop().remove();
    }

    this.getPanel = function() {
        return this.tree;
    }

    this.init();

}


