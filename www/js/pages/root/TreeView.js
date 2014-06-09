/**
 * Class representing the folder structure on root page
 */
function TreeView(rootPage) {
    this.rootPage = rootPage;
    this.treePanel = null;
    this.selectedPath = '/';

    this.structure = null;
    this.parentOfDragged = null;

    //map folder --> models contained in this folder
    this.models = new Array();

    this.init = function() {
        this.loadData();

        this.initTreePanel();
    }

    this.loadData = function() {
        var req = new XMLHttpRequest();
        req.open("GET", Util.getContext() + TreeView.DATA_URL, false);
        req.setRequestHeader("Detailed", "true");
        req.send(null);

        var modelElements = req.responseXML.getElementsByTagName("model");
        var structureElement = req.responseXML.getElementsByTagName("structure")[0];
        this.structure = new DirectoryStructure(structureElement);
        this.selectedPath = this.structure.homeFolder;

        for (var i = 0; i < modelElements.length; i++) {
            var model = modelElements[i];
            var props = Util.parseProperties(model);
            if (this.models[props.folder]) {
                this.models[props.folder].push(props);
            } else {
                var newGroup = new Array();
                newGroup.push(props)
                this.models[props.folder] = newGroup;
            }
        }
    }

    this.initTreePanel = function() {
        var structure = this.createTreeStructure();
        this.treePanel = new Ext.tree.Panel({
            useArrows: true,
            border: false,
            root: structure,
            rootVisible: false,
            autoHeight: true,
            autoWidth: true,
            viewConfig : {
            	plugins : [
    	           {ptype: 'treeviewdragdrop',
    	        	appendOnly: true}
	            ],
	            listeners: {
	            	beforedrop: function( node, data, overModel ) {
	            		var draggedNode = data.records[0];
        				if (draggedNode.isLeaf()) {
        					var oldParentId = draggedNode.parentNode.getId();
	            			var targetId = overModel.getId();
	            			this.changeFolder(draggedNode.getId(), targetId, oldParentId);
        					return true;
        				} else 
        					return false;
	            	},
	            	drop: function( node, data, overModel ) {
	            		var draggedNode = data.records[0];
	            		if ( draggedNode.isLeaf() ) {
	            			var targetId = overModel.getId();
	            			this.changeFolderAtServer(draggedNode.getId(), targetId);
	            		}
	            	}, scope: this
	            }
            }
        })

        this.treePanel.on('itemclick', function( view, record, item, index, e ) {
            var node = this.treePanel.getStore().getNodeById(record.getId()); 
            if (node.isLeaf()) {
               return;
            }
            
            this.selectedPath = record.getId();
            this.rootPage.selectionChanged();
        }, this);
        
        this.treePanel.on('itemdblclick', function( view, record, item, index, e ) {
            var node = this.treePanel.getStore().getNodeById(record.getId()); 
            
            if (!node.isLeaf()) return;

            var path = node.parentNode.getId();
            var uri = node.getId();
            var model = this.getModel(path, uri);

            new Ext.ux.RootPagePreviewWindow( {
                       width: 500,
                       height: 500,
                       imgsrc : model.image,
                       imageSize : 200,
                       modelURI : model.model,
                       access : model.access,
                       portletMode : rootPage.portletMode,
                       maskEl: Ext.getCmp(RootPage.PAGE_ID).getEl()
                   }).show();
        }, this);
        
        this.treePanel.on("afterrender", function() {
            if ( Util.readCookie("lastFolder") ) {
                this.select( Util.readCookie("lastFolder"));
            }
        }, this)
    }

    this.createTreeStructure = function() {	
        //initial structure with root node
        var created = {
            '/': {
                text : '/',
                children: [],
                id: '/',
                expanded: true,
                draggable: false,
                allowDrop: false
            }
        };

        created[this.structure.sharedFolder] = this.createFolder("Shared Documents", this.structure.sharedFolder);
        created[this.structure.sharedFolder].iconCls = "shared-folder"
        created[this.structure.sharedFolder].allowDrop = false;

        created[this.structure.isRoot] = this.createFolder("My iS", this.structure.isRoot);
        created[this.structure.isRoot].iconCls = "is-folder";
        created[this.structure.isRoot].allowDrop = false;

        created[this.structure.homeFolder] = this.createFolder("My Documents", this.structure.homeFolder);
        created[this.structure.homeFolder].expanded = true;
        created[this.structure.homeFolder].iconCls = "home-folder";

        created[this.structure.userTrash] = this.createFolder("Trash", this.structure.userTrash);
        created[this.structure.userTrash].iconCls = "trash-folder";
        
        created['/'].children.push(created[this.structure.homeFolder]);
        created['/'].children.push(created[this.structure.isRoot]);
        created['/'].children.push(created[this.structure.sharedFolder]);
        created['/'].children.push(created[this.structure.userTrash]);

        this.createSubdirectories(
            created[this.structure.homeFolder],
            this.structure.getChildren(this.structure.homeFolder),
            created);

        this.createSubdirectories(
            created[this.structure.isRoot],
            this.structure.getChildren(this.structure.isRoot),
            created);

        this.createSubdirectories(
            created[this.structure.sharedFolder],
            this.structure.getChildren(this.structure.sharedFolder),
            created);

        this.createSubdirectories(
            created[this.structure.userTrash],
            this.structure.getChildren(this.structure.userTrash),
            created);

        this.addModelsToFolder(created['/'], "/");

        return created['/'];
    }

    this.createSubdirectories = function( parentNode, children, allFolders ) {
        for ( var name in children ) {
            if (name == 'remove') continue;
            var child = children[name];
            var newFolder = this.createFolder(child.name, name);

            parentNode.children.push(newFolder);
            allFolders[name] = newFolder;

            this.createSubdirectories(newFolder, child.children, allFolders);
        }
    }

    this.select = function(path) {
        if ( this.treePanel.getStore().getNodeById(path) ) {
            this.treePanel.getStore().getNodeById(path).expand();
            this.selectedPath = path;
        } else {
            var parts = path.split("/");
            var fullPath = "";

            for ( var i = 1; i < parts.length; i++ ) {
                fullPath += "/" + parts[i];
                var n = this.treePanel.getStore().getNodeById(fullPath);
                if ( n != null ) {
                    n.expand();
                } 
            }

            if ( this.treePanel.getStore().getNodeById(path) ) {
                this.treePanel.getStore().getNodeById(path).expand();
                this.selectedPath = path;
            }
        }
    }

    this.createFolder = function(folderName, fullPath) {
        var iconCls = null;
        if (this.structure.types[fullPath] == 'IS') {
            if ( ("/" + folderName) == "/attic" ) {
                iconCls = "trash-folder";
                folderName = "Trash";
            }
            else
                iconCls = "is-folder";
        } 
//        else {
//            icon = Util.getContext() + Util.ICON_FOLDER;
//        }

        var draggable = false;
        var allowDrop = true;
//
        if (fullPath.indexOf(this.structure.sharedFolder) == 0)
            allowDrop = false;

        if (this.structure.types[fullPath] != 'FILE')
            allowDrop = false;

        if (fullPath.indexOf(this.structure.homeFolder) == 0)
            draggable = true;

        var newFolder = {
            text: folderName,
            children: [],
            id: fullPath,
            draggable: draggable,
            allowDrop: allowDrop,
            iconCls: iconCls
        };
        
        this.addModelsToFolder(newFolder, fullPath);

        return newFolder;
    }

    this.addModelsToFolder = function(folder, path) {
        var models = this.models[path];

        if (models == null) {
            this.models[path] = [];
            return;
        }

        for (var i = 0; i < models.length; i++) {
            var node = {text: models[i].name, leaf: true, id: models[i].model, iconCls: "model"};
            if ((path.indexOf(this.structure.sharedFolder) == 0))
                node.draggable = false;
            if (path.indexOf(this.structure.isRoot) == 0) 
                node.draggable = false;
            
            folder.children.push( node );
        }
    }

    this.getModelsToDisplay = function() {
        return this.models[this.selectedPath];
    }

    this.getFoldersToDisplay = function() {
        var folders = new Array();
        var f = this.treePanel.getStore().getNodeById(this.selectedPath);

        var childNodes = f.childNodes;

        for (var i = 0; i < childNodes.length; i++) {
            if (childNodes[i].isLeaf())
                continue;
            
            var path = childNodes[i].get("id");
            var name = childNodes[i].get("text");

            folders.push( {path: path, name: name} );
        }

        return folders;
    }

    this.getModel = function(path, modelUri) {
        var modelsAtPath = this.models[path];

        for (var i = 0; i < modelsAtPath.length; i++) {
            if (modelsAtPath[i].model == modelUri)
                return modelsAtPath[i];
        }

        return null;
    }

    this.getExtCmp = function() {
        return this.treePanel;
    }

    this.getLocationType = function(folder) {
        return this.structure.types[folder];
    }

    this.moveToTrash = function(uri) {
        var node = this.treePanel.getStore().getNodeById(uri);

        var origPath =  node.parentNode.getId();

        var newDir;
        if ( origPath.indexOf( this.structure.isRoot ) == 0 ) {
            var isPath = node.parentNode.parentNode.getId();
            newDir = this.treePanel.getStore().getNodeById(isPath + "/attic");
            if ( newDir == null ) {
                var newDir = new Ext.tree.TreeNode({
                    text: "Trash",
                    children: [],
                    id: isPath + "/attic",
                    draggable: false,
                    allowDrop: false,
                    icon: Util.getContext() + Util.ICON_DELETE
                });

                node.parentNode.parentNode.appendChild( newDir );
            }
        } else {
            newDir = this.treePanel.getStore().getNodeById(this.structure.userTrash);
        }
        newDir.expand();
        newDir.appendChild(node);

        this.treePanel.doLayout();
        return this.changeFolder(uri, this.structure.userTrash, origPath);
    }

    this.moveLocationToTrash = function(id) {
        var node = this.treePanel.getStore().getNodeById(id);

        var newDir = this.treePanel.getStore().getNodeById(this.structure.userTrash);
        newDir.expand();
        newDir.appendChild(node);

        this.updateId(node, newDir);

        node.parentNode = newDir;

        this.treePanel.doLayout();
    }

    this.updateId = function(node, parent) {
        var id = node.id;
        var name = node.text;

        var newId = parent.id + "/" + name;
        this.structure.types[newId] = this.structure.types[id];
        this.models[newId] = this.models[id];

        node.setId(newId);

        var childNodes = node.childNodes;

        for (var i = 0; i < childNodes.length; i++) {
            if (!childNodes[i].isLeaf())
                this.updateId(childNodes[i], node);
        }
    }

    this.removeModelPhysically = function(uri) {
        var node = this.treePanel.getStore().getNodeById(uri);

        var trashPath = this.structure.userTrash;

        if ( node.parentNode.getId().indexOf( this.structure.isRoot ) > -1 ) {
            //this trash is an iS-Trash
            trashPath = node.parentNode.getId();
        }

        node.remove();
        var newTrash = new Array();
        var currentTrash = this.models[trashPath];

        for (var i = 0; i < currentTrash.length; i++) {
            if (currentTrash[i].model == uri) continue;

            newTrash.push(currentTrash[i]);
        }

        this.models[trashPath] = newTrash;
    }

    this.removeLocationPhysically = function(id) {
        var node = this.treePanel.getStore().getNodeById(id);
        node.remove();
        this.rootPage.reloadImageView();
        this.treePanel.doLayout();
    }

    this.changeFolder = function(uri, newFolder, oldFolder) {
        if (this.models[newFolder] == null)
           return false;
        
        var modelsAtPath = this.models[oldFolder];
        var newAtPath = new Array();
        var model = null;
        for (var i = 0; i < modelsAtPath.length; i++) {
            if (modelsAtPath[i].model == uri) {
                model = modelsAtPath[i];
                continue;
            }

            newAtPath.push(modelsAtPath[i]);
        }

        this.models[oldFolder] = newAtPath;
        this.models[newFolder].push(model);
        this.rootPage.reloadImageView();

        return true;
    }

    this.changeFolderAtServer = function(uri, newFolder) {
        var req = new XMLHttpRequest();
        req.open("PUT", uri + "/meta", false);

        //@TODO handle possible refuse by server (e.g if target folder belongs to is and model is not an iS model)

        var xml = "<update type='folder'><property name='folder' value='";
        xml += newFolder;
        xml += "'/></update>";

        req.send(xml);
    }

    this.init();
}

TreeView.DATA_URL = '/models';
//TreeView.ATTIC_FOLDER = '/attic';