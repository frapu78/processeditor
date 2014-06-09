Ext.define('Inubit.WebModeler.plugins.ReferenceChooserDialog', {
    extend: 'Ext.ux.Dialog',
    activeModel: null,
    layout : 'border',
    width : 700,
    height : 500,
    
    defaults : {
        collapsible : true,
        split : true,
        collapseMode : 'mini',
        hideCollapseTool : true
    },
    //selection  handler for this component
    selectionHandler : {
        selectedObject : null,
        singleSelect : function(processObject) {
            if (this.selectedObject != processObject) {
                if (this.selectedObject != null) {
                    this.selectedObject.setSelected(false);
                }

                this.selectedObject = processObject;
                this.selectedObject.select();
            }
        },
        unselect : function() {
                if (this.selectedObject != null) {
                        this.selectedObject.setSelected(false);
                        this.selectedObject = null;
                }
        }
    },
    constructor : function( config ) {
        Inubit.WebModeler.plugins.ReferenceChooserDialog.superclass.constructor.call( this, config );

        this.mouseListener = new Inubit.WebModeler.handler.ReferenceChooserMouseListener({
            componentId : 'region-center',
            nodeSelectionHandler : this
        });
    },
    
    initComponent : function() {
        this.tree = new Ext.tree.Panel({
            header : false,
            id : 'referenceChooserTreePanel',
            useArrows : true,
            autoScroll : true,
            animate : true,
            enableDD : false,
            containerScroll : true,
            border : false,
            listeners:{
                selectionchange: function(selModel, treeNode) {
                    treeNode = treeNode[0];
                    if (treeNode == null) {
                        this.selectionHandler.unselect();
                        return;
                    }
                    if (!treeNode.isRoot()) {
                        var modelId;
                        if (!treeNode.isLeaf()) {
                            modelId = treeNode.getId().substring(treeNode.getId().lastIndexOf("@") + 1);
                            this.selectedModelValue = modelId;
                            this.displayModel(modelId);
                        } else {
                            var parentNode = treeNode.parentNode;
                            modelId = parentNode.getId().substring(parentNode.getId().lastIndexOf("@") + 1);
                            var nodeId = treeNode.getId().substring(treeNode.getId().lastIndexOf("@") + 1);
                            this.selectedNodeValue = nodeId;
                            this.selectedModelValue = modelId;
                            this.displayModel(modelId);
                            this.selectNodeInModel(nodeId);
                        }
                    }
                },
                scope: this
            }
        });
        
        this.editor = new Ext.Panel({
            header : false,
            layout : 'fit',
            autoScroll: true,
            border : false
        });
        
        this.tbar = new Ext.toolbar.Toolbar({
            items: [
                {text: 'Drop Link',
                    icon: Util.getContext() + Util.ICON_DELETE,
                    handler: function() {
                        this.tree.getSelectionModel().clearSelections();
                    },
                    scope: this
                },
                {xtype:'tbseparator'},
                {text: 'Follow Link',
                    icon: Util.getContext() + Util.ICON_FOLLOW,
                    handler: function() {
                        if(Ext.isDefined(this.model)) {
                           if (ProcessEditor.instance.portletMode)
                               location.href = this.model.uri;
                           else
                               window.open(this.model.uri);
                        }
                    },
                    scope: this
                }
            ]
        });
        
        this.items = [
            {
                region : 'west',
                header : false,
                id : 'region-west',
                margins : '0 0 0 0',
                cmargins : '0 0 0 0',
                layout : 'fit',
                width : 175,
                items: this.tree
            }, {
                header : false,
                collapsible : false,
                id : 'region-center',
                region : 'center',
                autoScroll : true,
                layout: 'anchor',
                defaults: {
                    anchor: '100%, 100%',
                    autoScroll: true
                },
                width : 500,
                height : 500,
                margins : '0 0 0 0',
                cmargins : '0 0 0 0',
                items: this.editor
            }
        ];
        
        Inubit.WebModeler.plugins.ReferenceChooserDialog.superclass.initComponent.call(this);
        
        this.on("afterrender", function() {
                if ( this.selectedModelValue && this.selectedNodeValue ) {
                    this.displayModel(this.selectedModelValue);
                    this.selectNodeInTree("models@@@" + this.selectedModelValue + "/nodes@@@" + this.selectedNodeValue);
                }
            }, this);
    },

    setData : function( data ) {
        this.tree.setRootNode({
            text : 'Models',
            icon : Util.getContext() + Util.ICON_HOME,
            draggable : false,
            expanded: true,
            children: data.treedata,
            id : 'source'
        });
        if ( Ext.isDefined(data.currentRef) ) {
            this.selectedModelValue = data.currentRef.split("/")[1];
            this.selectedNodeValue = data.currentRef.split("/")[3];
        }
    },

    getJSONData : function() {
        var data = new Array();
        
        var selection = this.tree.getSelectionModel().getSelection();
        var treeNode = ( selection.length > 0 ? selection[0] : null );
        if ( Ext.isDefined(treeNode) && treeNode != null && treeNode.isLeaf() ) {
            var parentNode = treeNode.parentNode;
//            var modelId = parentNode.getId().substring(parentNode.getId().lastIndexOf("@") + 1);
//            var nodeId = treeNode.getId().substring(treeNode.getId().lastIndexOf("@") + 1);
            data.push( {
                ref: parentNode.getId().replace("@@@", "/") + "/" + treeNode.getId().replace("@@@", "/"),
                text: treeNode.raw.text
            } );
        }

        return data;
    },

    getTitle : function() {
        return "Select Link";
    },

    /**
     * Loads a model with a given id and displays it.
     *
     * @param {}
     *            modelId the model id.
     */
    displayModel : function(modelId) {
            if (this.activeModel == modelId) 
                return;

            if ( Ext.isDefined(this.canvasWrapper) ) {
                this.selectionHandler.unselect();
                this.canvasWrapper.clear();
                this.canvasWrapper.setSize(500,500);
            } else {
                this.canvasWrapper = new Raphael(this.editor.el.dom.childNodes[0], this.editor.getWidth(), this.editor.getHeight());
            }

            this.loader = null;
            this.loader = new Inubit.WebModeler.ModelLoader({
                id : modelId,
                version : -1,
                mode : Inubit.WebModeler.ModelLoader.MODE_CHOOSE,
                rootComponent : this,
                listeners: {
                    load: this.modelLoaded,
                    scope: this
                }
            });
            this.loader.load(this.canvasWrapper);
            this.activeModel = modelId;
    },
    /**
     * Called when the model loading process is finished.
     */
    modelLoaded : function() {
        this.model = this.loader.getModel();
        this.canvasWrapper.setSize(parseInt(this.loader.getModelWidth()) + 30,
                        parseInt(this.loader.getModelHeight()) + 30);
        this.selectionHandler.singleObject = this.model;
        this.loader.hideLoaderMask();
        this.editor.doLayout();
        if (Ext.isDefined(this.selectedNodeValue))
                this.selectNodeInModel(this.selectedNodeValue);
    },

    /**
     * Handles the node selection.
     *
     * @param {}
     *            node the selected node.
     */
    handleNodeSelection : function(node) {
        if (node == null) {
            this.selectionHandler.unselect();
            return;
        }
        if (Ext.isDefined(node) && Ext.isDefined(this.tree)
                        && Ext.isDefined(node.model)) {
            var nodeId = 'models@@@' + node.model.baseId + '/nodes@@@' + node.getId();
            this.selectNodeInTree(nodeId);
        }
    },

    /**
     * Selects a node in the tree. If the node is not part of the loaded model
     * the corresponding model is loaded.
     *
     * @param {}
     *            theId the node id.
     */
    selectNodeInTree : function(theId) {
        if (!Ext.isDefined(theId))
            return;

        if (Ext.isDefined(this.tree)) {
            theId = '/source/' + theId;
            this.tree.expandPath(theId, "id", "/", function(success, node) {
                if ( success ) 
                    this.tree.selectPath(theId);
            }, this);
        }
    },

    /**
     * Selects a node in the model and highlights it on the canvas.
     *
     * @param {}
     *            nodeId the node id.
     */
    selectNodeInModel : function(nodeId) {
        if (Ext.isDefined(this.model)) {
            var selectedNode = this.model.getNodeWithId(nodeId);
            if (Ext.isDefined(selectedNode) && selectedNode != null) {
                this.selectionHandler.singleSelect(selectedNode);
                this.editor.container.dom.scrollLeft = (selectedNode.x - selectedNode.width);
                this.editor.container.dom.scrollTop = (selectedNode.y - selectedNode.height);
            }
        } else if(Ext.isDefined(this.selectedModelValue))
            this.displayModel(this.selectedModelValue);
    },

    getCanvas : function() {
        return this.canvasWrapper;
    }
});
