function RootPageRightsPanel(modelURI, access, width, height) {
    this.panel = null;
    
    this.rights = new Array();

    this.adminAccess = (access == "ADMIN");
    this.ownerAccess = (access == "ADMIN") || (access == "OWNER")
    this.width = width;
    this.height = height;
    if ( this.width == null )
        this.width = 300;
    if ( this.height == null )
        this.height = 300;

    this.init = function() {
        this.loadData();
        var users = this.getUserList();

        var ownerCombo =  this.createOwnerComboBox(users);
        
        this.panel = new Ext.panel.Panel({
            title: 'Rights',
            disabled: !this.ownerAccess,
            height: height - 32,
            border: false,
            tbar: {
                items: [
                    new Ext.toolbar.TextItem({
                        text: "Owner:"
                    }),
                    ownerCombo
                ]
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                this.createRightTree("Viewers", this.rights.viewers),
                this.createRightTree("Annotators", this.rights.annotators),
                this.createRightTree("Editors", this.rights.editors)
            ]
        });
        
        return this.panel;
    }

    this.createOwnerComboBox = function(data) {
        if (this.adminAccess) {
            var combo = new Ext.form.ComboBox({
                allowBlank: false,
               editable: false,
               forceSelection: true,
               disableKeyFilter: true,
                id: 'oCombo',
                queryMode: 'local',
                store: this.createUserStore(data),
                valueField: 'name',
                displayField: 'name',
                width : this.width * 0.62
                
            })

            combo.on("select", function() {
                var req = new XMLHttpRequest();
                req.open("PUT", modelURI + "/meta", true);

                req.send("<update type='owner'><property name='owner' value='" + this.getValue() + "'/></update>");
            })
            
            combo.setValue(this.rights.owner);
            
            return combo;
        } else
            return new Ext.toolbar.TextItem({
                text: this.rights.owner
            })
    }

    this.createUserStore = function(data) {
        var store = new Ext.data.ArrayStore({
            id: 0,
            fields: [
                'key',
                'name'
            ],
            data : data,
            sortInfo: {
                field: 'name',
                direction: 'ASC'
            }
        })

        this.userStore = store;
        return store;
    }

    this.getUserList = function() {
        var array = new Array();
        var users = Util.getUsers();
        for (var i = 0; i < users.length; i++) {
            array.push([i, users[i]]);
        }

        return array;
    }

    this.createRightTree = function( title, members ) {
         var tree = new Ext.tree.TreePanel({
            title: title,
            cls: 'rights-add-remove',
            labelStyle: 'font-size: 11px',
            flex: 1,
            rootVisible: false,
            height: this.height * 0.25,
            width: this.width * 0.62,
            autoScroll: true,
            id: title.toLowerCase(),
            lines: false,
            buttonAlign: 'left',
            root: {
                text: title,
                expanded: true,
                children: this.createChildNodes(members)
            },
            tools: [
                {
                   type: 'add',
                   handler: function() {
                       this.openMemberAdditionWindow(tree);
                   }, scope: this
                },
                {
                   type: 'remove',
                   handler: function() {
                       this.deleteSelection(tree);
                   }, scope: this
                }
            ],
            multiSelect: true
        });

        //new Ext.tree.TreeSorter( tree, {});

        return tree;
    }
    
    this.createChildNodes = function( members ) {
        var children = [];
        for (var i = 0; i < members.length; i++) {
            var icon = "user-single"

            if (members[i].type == "GROUP")
                icon = "user-group"

            children.push( {
                iconCls: icon,
                text: members[i].name,
                id: members[i].type + "_" + members[i].name,
                leaf: true
            });
        }
        return children;
    }
    
    this.loadData = function() {
        var req = new XMLHttpRequest();
        req.open("GET", modelURI + "/access", false);
        req.send(null);

        var xml = req.responseXML;

        var ownerEl = xml.getElementsByTagName("owner")[0];
        this.rights["owner"] = ownerEl.textContent;

        var viewerEls = xml.getElementsByTagName("viewer");
        var viewers = new Array();
        for (var i = 0; i < viewerEls.length; i++) {
            viewers.push(Util.parseProperties(viewerEls[i]));
        }

        this.rights["viewers"] = viewers;

        var annotatorEls = xml.getElementsByTagName("annotator");
        var annotators = new Array();
        for (var i = 0; i < annotatorEls.length; i++) {
            annotators.push(Util.parseProperties(annotatorEls[i]))
        }

        this.rights["annotators"] = annotators;

        var editorEls = xml.getElementsByTagName("editor");
        var editors = new Array();
        for (var i = 0; i < editorEls.length; i++) {
            editors.push(Util.parseProperties(editorEls[i]))
        }

        this.rights["editors"] = editors;
    }

    this.openMemberAdditionWindow = function(tree) {
        var win = new RootPageRightAdditionWindow(tree, this, this.panel, 'Add ' + tree.id);

        win.show(tree);
    }

    this.addUsers = function( tree, selectedNodes ) {
        var req = new XMLHttpRequest();
        req.open("PUT", modelURI + "/access", true);

        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                while (selectedNodes.length > 0) {
                    var node = selectedNodes.pop();
                    if (!tree.getStore().getNodeById(node.id)) {
                        tree.getRootNode().appendChild(  node );
                    }
                }
            }
        }
        req.send(this.createAccessXML(tree.id, "add", selectedNodes))
    }

    this.deleteSelection = function( tree ) {
        var req = new XMLHttpRequest();
        req.open("PUT", modelURI + "/access", true);

        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var nodes = tree.getSelectionModel().getSelection();

                while (nodes.length > 0)
                    nodes.pop().remove();
            }
        }
        req.send(this.createAccessXML(tree.id, "delete", tree.getSelectionModel().getSelection()))
    }

    this.createAccessXML = function( type, method, selectedNodes ) {
        var xml = "<access type='" + type + "' method='" + method + "'>"

        for (var i = 0; i < selectedNodes.length; i++) {
            var usertype = 'SINGLE_USER';
            if ( selectedNodes[i].get('iconCls') == "user-group" )
                usertype = "GROUP";
            xml += "<accessor>"
            xml += "<property name='name' value='" + selectedNodes[i].get('text') + "'/>"
            xml += "<property name='type' value='" + usertype + "'/>"
            xml += "</accessor>"
        }

        xml += "</access>"
        return xml;
    }

    return this.init();
}