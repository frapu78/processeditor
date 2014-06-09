Ext.define('Inubit.WebModeler.pages.editor.ProcessEditorSaveDialog', {
    extend: 'Ext.window.Window',
    modal: true,
    structure:  null,
    model: null,
    width: 400,
    plain: true,
    layout:'anchor',
    defaults : {
    	bindToOwnerCtContainer : true,
    	anchor : '100%'
    },
    title: 'Save Model..',
    closable: true,
    
    constructor: function( config ) {
        this.model = config.model;
        this.currentSaveDirectory = this.model.getFolder();
        Inubit.WebModeler.pages.editor.ProcessEditorSaveDialog.superclass.constructor.call( this, config );
    },
    
    initComponent: function() {
        this.nameField = new Ext.form.TextField({
            fieldLabel: 'Model name',
            value: this.model.getName(),
            enableKeyEvents: true,
            listeners: {
                keydown: function( field, event ) {
                    if ( event.getKey() == Ext.EventObject.DELETE ) 
                        Util.stopEvent(event);
                },
                keyup: function( field, event ) {
                    if ( event.getKey() == Ext.EventObject.DELETE ) 
                        Util.stopEvent(event);
                }
            }
        });
        
        this.folderTree = new Ext.tree.Panel({
            fieldLabel: "Folder",
            useArrows: true,
            lines:false,
            titleCollapse:true,
            title: this.currentSaveDirectory,
            root: this.createStructure(),
            rootVisible: false,
            height: 150,
            autoScroll: true,
            collapsed: true,
            collapsible: true,
            enableDD: false,
            enableDrag: false,
            enableDrop: false,
            disabled: !(this.model.access == 'OWNER' || this.model.baseId == null),
            bbar: {
                items: new Ext.Button({
                    text: 'New Folder',
                    icon: Util.getContext() + Util.ICON_NEWFOLDER,
                    handler: function() {
                        var path = this.currentSaveDirectory;
                        if ( path.indexOf(this.structure.isRoot) == 0 && path.indexOf("/", 4) > 0 ) {
                            Ext.Msg.alert("Not allowed", "It is not allowed to create a folder under: " + this.currentSaveDirectory);
                            return;
                        }

                        Ext.Msg.prompt('Name', "Please enter the folder's name:", function(btn, text){
                            if (btn == 'ok') {
                                var node = this.folderTree.getStore().getNodeById(path);
                                node.appendChild( {
                                    text: text,
                                    id: path+ "/" + text
                                });
                                node.expand();
                            }
                        }, this);

                    },
                    scope: this
                })
            },
            listeners: {
                beforeitemclick: function( view, node, htmlItem, index, e) {
                    if (node.disabled)
                        return false;

                    if (!this.newModel.getValue())
                        if ((node.id.indexOf(this.structure.isRoot) == 0 && this.model.getFolder().indexOf(this.structure.homeFolder) == 0) ||
                            (node.id.indexOf(this.structure.homeFolder) == 0 && this.model.getFolder().indexOf(this.structure.isRoot) == 0)) {

                            var msg = "For saving this model to " + node.id + " mark option 'Save as new model'. ";

                            Ext.Msg.show({
                                title: 'Cannot change location',
                                msg: msg,
                                buttons: Ext.Msg.OK,
                                icon: Ext.Msg.WARNING})

                            return false;
                        }
                },
                
                itemclick: function( view, node, htmlItem, index, e) {
                    this.currentSaveDirectory = node.getId();
                    this.folderTree.setTitle(node.getId());
                },
                scope: this
            }
        });
        
        this.folderTree.on("beforeclick", function(node) {
            if (node.getId() == this.structure.isRoot )
                return false;

            if (!this.newModel.getValue())
                if ((node.id.indexOf(this.structure.isRoot) == 0 && this.model.getFolder().indexOf(this.structure.homeFolder) == 0) ||
                    (node.id.indexOf(this.structure.homeFolder) == 0 && this.model.getFolder().indexOf(this.structure.isRoot) == 0)) {

                    var msg = "For saving this model to " + node.id + " mark option 'Save as new model'. ";

                    Ext.Msg.show({
                        title: 'Cannot change location',
                        msg: msg,
                        buttons: Ext.Msg.OK,
                        icon: Ext.Msg.WARNING})

                    return false;
                }
        }, this);

        this.versionComment = new Ext.form.field.TextArea({
            height: 80,
            width: 275,
            fieldLabel: 'Comment',
            enableKeyEvents: true,
            listeners: {
                keydown: function( field, event ) {
                    if ( event.getKey() == Ext.EventObject.DELETE ) 
                        Util.stopEvent(event);
                },
                keyup: function( field, event ) {
                    if ( event.getKey() == Ext.EventObject.DELETE ) 
                        Util.stopEvent(event);
                }
            }
        });

        this.newVersion = new Ext.form.field.Radio({
            boxLabel: '..new version',
            name: 'postType',
            disabled: this.model.baseId == null,
            checked: this.model.baseId != null,
            listeners: {
                change: function(box, newValue, oldValue){
                    if (newValue) {
                        if (this.newVersion.getValue())
                            this.forceCommit.enable();
                        else
                            this.forceCommit.disable();

                        if (this.model.access == 'OWNER') 
                            this.folderTree.enable();
                        else 
                            this.folderTree.disable();
                    }
                }, scope: this
            }
        });

        // force commit cb is now hidden.

        this.forceCommit = new Ext.form.Checkbox({
            disabled: this.newVersion.disabled,
            boxLabel: 'Force commit(do not merge changes)',
            selected: true,
            checked: this.model.baseId != null,
            hidden: true
        });

        this.newModel = new Ext.form.field.Radio({
            labelStyle: 'padding-bottom:5px;',
            boxLabel: '..new model',
            name: 'postType',
            checked: this.model.baseId == null,
            listeners: {
                change: function(box, newValue, oldValue){
                    if (newValue) {
                        this.folderTree.enable();
                        this.forceCommit.disable();
                    }
                },
                scope: this
            }
        });
         
        this.items = [
                this.nameField,
                new Ext.form.FieldContainer({
                    fieldLabel: 'Folder',
                    layout: 'anchor',
                    items: [
                        this.folderTree,
                    ]
                }),
                this.versionComment,
                new Ext.form.FieldContainer({
                    fieldLabel: 'Save as...',
                    layout: 'anchor',
                    items: [
                        this.newModel,
                        
                        this.newVersion
                    ]
                }),
                this.forceCommit
        ];
        
        this.buttons = [
            {
                text: 'Save',
                handler: function(event) {
                    var folder = this.currentSaveDirectory;
                    if (folder[0] != '/')
                        folder = "/" + folder;
                    this.model.properties.name = this.nameField.getValue();
                    this.model.persistChanges(
                        this.nameField.getValue(),
                        folder,
                        this.versionComment.getValue(),
                        this.newModel.getValue(),
                        this.forceCommit.getValue()
                    );
                    this.close();

                    document.title = this.nameField.getValue();
                },
                scope: this                
            },
            {
                text: 'Cancel',
                handler: function() {
                    this.close();
                },
                scope: this
            }
        ]
        
        Inubit.WebModeler.pages.editor.ProcessEditorSaveDialog.superclass.initComponent.call(this);
    },
    
    createStructure : function() {
        var req = new XMLHttpRequest();
        req.open("GET", Util.getContext() + "/models/locations", false);
        req.send(null);

        var structure = new DirectoryStructure(req.responseXML);

        if ( this.currentSaveDirectory == '/' )
            this.currentSaveDirectory = structure.homeFolder;

        var created = {
            '/': {
                text : '/',
                children: [],
                id: '/',
                expanded: true
            }
        };

        this.structure = structure;

        created[structure.isRoot] = this.createFolder("My iS", structure.isRoot);
        created[structure.isRoot].iconCls = 'is-folder';
        created[structure.isRoot].expanded = true;
        created[structure.isRoot].disabled = true;

        created[structure.homeFolder] = this.createFolder("My Models", structure.homeFolder);
        created[structure.homeFolder].iconCls = 'home-folder';
        created[structure.homeFolder].expanded = true;
        
        created['/'].children.push(created[structure.homeFolder]);
        created['/'].children.push(created[structure.isRoot]);

        this.createSubdirectories(
            created[structure.homeFolder],
            structure.getChildren(structure.homeFolder),
            created);

        this.createSubdirectories(
            created[structure.isRoot],
            structure.getChildren(structure.isRoot),
            created);

        return created['/'];
    },

    createSubdirectories : function(parentNode, children, allFolders) {
         for ( var name in children ) {
            if (name == 'remove') continue;
            var child = children[name];
            var newFolder = this.createFolder(child.name, name);

            parentNode.children.push(newFolder);
            allFolders[name] = newFolder;

            this.createSubdirectories(newFolder, child.children, allFolders);
        }
    },

    createFolder : function(folderName, fullPath) {
        var icon = null;
        if (this.structure.types[fullPath] == 'IS') {
            if ( ("/" + folderName) == "/attic" ) {
                icon = Util.getContext() + Util.ICON_DELETE;
                folderName = "Trash";
            }
            else
                icon = Util.getContext() + Util.ICON_CONNECT;
        } else {
            icon = Util.getContext() + Util.ICON_FOLDER;
        }

        var expanded = false;

        if (this.currentSaveDirectory.indexOf(fullPath) == 0)
            expanded = true;

        var newFolder = {
            expanded: expanded,
            text: folderName,
            children: [],
            id: fullPath,
            icon: icon
        };

        return newFolder;
    }
});