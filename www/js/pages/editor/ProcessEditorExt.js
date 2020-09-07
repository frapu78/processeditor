
function ProcessEditor( portletMode ) {
    this.portletMode = portletMode;

    var widthEast = 225;
    var widthWest = 60;
    var scrollBarWidth = 17;
    var southRatio = 0.5;

    var page = null;
    var dropZone = null;

    this.canvas = null;
    this.basePropertyGrid = null;
    this.extendedPropertyGrid = null;
    this.commentsPanel = null;
    this.newElementsPanel = null;
    this.model = null;
    this.loader = null;
    this.menu = null;
    this.propertyConfig = new PropertyConfig(this);

    this.allowComments = true;
    this.isDialogShown = false;

    this.selectionHandler = new SelectionHandler();
    this.undoHandler = new UndoHandler();
    this.mouseListener = new ProcessEditorMouseListener(this);
    this.pluginManager;
    this.modelType = null;

    function keyUp(event) {
        var keyCode = null;
        if (event.which)
            keyCode = event.which;
        else
            keyCode = event.keyCode;

        //del-key was hit
        if ( keyCode == 46 && !ProcessEditor.instance.isDialogShown ) {
            ProcessEditor.instance.selectionHandler.deleteSelection();
        }
    }

    function unload(event) {
        return "Be sure that all changes you made have been saved. Otherwise, these changes will be lost if you continue!";
    }
    
    function calculateWestWidth(west) {
        /*var elements = west.items.filterBy(function(o,k) { return (o instanceof Ext.panel.Panel) });
        if(elements.length > 1){
            if( west.getHeight() < elements[0].getHeight() + scrollBarWidth )
                return widthWest + scrollBarWidth;
        }*/
        return widthWest;
    }

    this.resizeCanvas = function () {
        var cWidth = parseInt(Ext.getCmp('center_region').getWidth());
        var cHeight = parseInt(Ext.getCmp('center_region').getHeight());

        console.log("resizeCanvas: cWidth="+cWidth+", cHeight="+cHeight);

        this.canvas.resize(cWidth, cHeight);
        Ext.getCmp('center_region').doLayout();
        page.getLayout().getLayoutItems()[2].suspendEvents();
        page.getLayout().getLayoutItems()[2].setHeight(cHeight);
        page.getLayout().getLayoutItems()[2].resumeEvents();
    }

    this.resizeWindow = function(newWidth, newHeight) {

        var west = Ext.getCmp('west_region');
        var east = Ext.getCmp('east_region');
        
        page.setSize(newWidth, newHeight);
        var wWidth = calculateWestWidth(west);
        var eWidth = east.getWidth();

        page.getLayout().getLayoutItems()[1].setSize(wWidth, newHeight);
        page.getLayout().getLayoutItems()[2].setSize(eWidth, newHeight);
        page.getLayout().getLayoutItems()[0].setSize(newWidth - eWidth - wWidth, newHeight);

        page.doLayout();

        ProcessEditor.instance.resizeCanvas();
    }

    this.createEditor = function(parentNode) {
        var size = Util.getComponentSize(parentNode);
        var width = size.width;
        var height = size.height;

        this.basePropertyGrid = new Inubit.WebModeler.PropertyForm({
            id: 'base_properties',
            autoWidth: false,
            layout: 'anchor',
            autoHeight: true,
            collapsible: true,
            title: 'Basic Properties',
            labelWidth: 87,
            defaults: {
                anchor: '100%',
                labelStyle: "width: 87;font-size:11px;margin-left:3px;margin-top: 3px"
            },
            modeler: this,
            propType: 'base'
        });

        this.extendedPropertyGrid = new Inubit.WebModeler.PropertyForm({
            id: 'extended_properties',
            autoWidth: false,
            autoHeight: true,
            collapsible: true,
            layout: 'anchor',
            title: 'Extended Properties',
            labelWidth: 87,
            collapsed: true,
            defaults: {
                labelStyle: "width: 87;font-size:11px;margin-left:3px;margin-top: 3px",
                anchor: '100%'
            },
            modeler: this,
            propType: 'extended'
        })

        this.extendedPropertyGrid.on( "expand", function() {
            if ( ProcessEditor.instance.selectionHandler.singleObject && this.items.length == 0) 
                this.displayProperties(ProcessEditor.instance.selectionHandler.singleObject)
        });

        this.propertyGrid = new Ext.Panel({
            id: 'properties',
            width: eastWidth,
            autoHeight: true,
            title: 'Properties',
            layout: 'accordion',
            border: false,
            items: [
                this.basePropertyGrid,
                this.extendedPropertyGrid,
            ]
        });
        
        this.commentsPanel = new Inubit.WebModeler.CommentPanel({
            buttonAlign: 'right',
            autoScroll: true,
            border: false,
            title: 'Model comments'
        });
        
        this.createBasicLayout(width, height, parentNode);

        var eastRegion = Ext.getCmp('east_region');
        var eastWidth = eastRegion.getWidth() - 2;
        var westRegion = Ext.getCmp('west_region');

        this.createSurface();

        this.newElementsPanel = westRegion;

        eastRegion.doLayout();
    }

    this.createSurface = function(uri) {
        var centerRegion = Ext.getCmp('center_region');
        var canvasMinWidth = centerRegion.getWidth() - 2;
        var canvasMinHeight = centerRegion.getHeight() - 2;

        var width = canvasMinWidth; //Math.max(properties["width"], canvasMinWidth - 20) + 20;
        var height = canvasMinHeight; //Math.max(properties["height"], canvasMinHeight - 20) + 20;

        this.canvas = new Canvas(width, height, centerRegion, canvasMinWidth, canvasMinHeight, this.mouseListener);

        //Define drop zone for Ext drag'n'drop
        dropZone = this.canvas.getExtCmp().body;
        new Ext.dd.DropZone(dropZone, {ddGroup:'newNodes'});
        
        // Is now drawn with css (background image)
        //this.canvas.drawGatter(20, 20);
    }

    this.loadModelTypeInformation = function(type) {
        this.modelType = new ProcessModelType(type);
    }

    this.loadNewElementPalette = function() {
        var panel = this.newElementsPanel;
        var uri = Util.getContext() + '/utils';
        var elementPalette = new Ext.Panel({
           id: 'palette',
           layout:'anchor',
           autoScroll: true,
           defaults: {
               anchor: '100%'
           },
           width: widthWest,
           height: panel.getHeight(),
           border: false
        });

        panel.add(elementPalette);
        panel.doLayout();

        var paletteElements = this.model.getType().paletteElements;

        for (var i = 0; i < paletteElements.length; i++) {
            var newPanel = this.createNewNodePanel(uri, paletteElements[i], elementPalette, true);
            newPanel.dd = new Ext.dd.DDProxy(newPanel.getId(), 'newNodes');
        }
        panel.setWidth(calculateWestWidth(panel));
        panel.doLayout();
        Ext.QuickTips.init();
    }

    this.createNewNodePanel = function(uri, name, panel) {
        var maxWidth = panel.getWidth();
        var nameParts = name.split(".");
        var tipName = nameParts[nameParts.length - 1];
        var newPanel = new Ext.Panel({
           width: maxWidth,
           maxWidth: maxWidth,
           height: 50,
           layout: 'fit',
           itemId: name
        });

        //add panel to be able to edit panel.body
        panel.add(newPanel);
        panel.doLayout();

        new Ext.ToolTip({
            target: newPanel.body.id,
            html: tipName
        })

        var div = document.createElement("div");
        div.style.maxWidth = maxWidth +"";
        div.style.maxHeight = "60";
        div.style.textAlign = "center";

        newPanel.body.dom.setAttribute("nodeclass", name);
        var src = uri + "/dummy?name=" + name + "&preview=true";

        div.innerHTML = "<img src='" + src + "'>";

        div.childNodes[0].style.position = 'relativ';
        div.childNodes[0].style.maxHeight = "40";
        div.childNodes[0].style.maxWidth = maxWidth - 15;
        div.childNodes[0].style.paddingTop = "5";

        newPanel.body.dom.appendChild(div);

        return newPanel;
    }

    this.displayProperties = function(object) {
        this.extendedPropertyGrid.collapse( false );
        this.basePropertyGrid.expand(false);
        this.basePropertyGrid.displayProperties(object);
        
//        var east = page.getLayout().getLayoutItems()[2];
//        this.basePropertyGrid.applyParentSize( east.getWidth(true), east.getHeight(true) );
        
        this.basePropertyGrid.expand( true );
    }

    this.clearProperties = function() {
        this.basePropertyGrid.removeAll();
        this.extendedPropertyGrid.remove();
    }

    this.init = function(m_id, m_version, m_type) {
         this.loader = new Inubit.WebModeler.ModelLoader({
             id : m_id,
             version : m_version,
             type : m_type,
             mode : Inubit.WebModeler.ModelLoader.MODE_EDIT,
             listeners: {
                 load: this.modelLoaded,
                 scope: this
             }
        });
//         this.loader.onLoadFinish(this.modelLoaded, this);
         this.loader.load(this.getCanvas());

         document.onkeyup = keyUp;

         window.onbeforeunload = unload;
         window.onunload = function() {
             var req = new XMLHttpRequest();
             req.open("DELETE", ProcessEditor.instance.getModel().uri, true);
             req.send(null);
         }
    }

    this.createBasicLayout = function(width, height, parentNode) {
        var centerWidth = width - widthEast - widthWest;

        this.menu = new ProcessEditorMenu( this.portletMode );

        page = new Ext.Panel({
            renderTo: parentNode,
            width: width,
            //maxWidth: 100000,
            height: height,
            //maxHeight: 100000,
            layout: 'border',
            items: [
                {region: 'center',
                     id: 'center_region_wrapper',
                     width: centerWidth,
                     height: height,
                     items: [
                          {
                              region: 'center',
                              id: 'center_region',
                              collapsible: false,
                              border: false,
                              margins: '0 0 0 0',
                              style: 'background-color:"white"',
                              split: true,
                              autoScroll: false
                         },
                         {
                             autoRender: true,
                             autoScroll: false,
                             collapsed: true,
                             collapsible: true,
                             collapseMode: 'mini',
                             frame: false,
                             frameHeader: false,
                             height: 250,
                             hideCollapseTool: true,
                             id: 'south_region',
                             margins: '0 0 0 0',
                             region: 'south',
                             split: true,
                             width: centerWidth,
                             xtype: 'tabpanel',
                             tools:[{
                                     type:'minimize',
                                     tooltip: 'Collapse',
                                     scope: this,
                                     handler: function(event, toolEl, panel){
                                     	Ext.getCmp("south_region").collapse()
                                     }
                                 }
                             ]
                         }
                     ],
                     collapsible: false,
                     split: true,
                     margins: '5 0 5 0',
                     layout: 'border'
                },
                {
                    region: 'west',
                    id: 'west_region',
                    height: height,
                    width: widthWest,
                    collapsible: false,
                    collapseMode: 'header',
                    autoScroll: false,
                    margins: '5 0 5 0',
                    layout: 'fit',
                    title: 'New'
                },
                {
                    region: 'east',
                    id: 'east_region',
                    height: height,
                    autoScroll: true,
                    width: widthEast,
                    collapseMode: 'header',
                    title: 'Details',
                    collapsible:  false,
                    split: false,
                    autoScroll: false,
                    layout: 'accordion',
                    layoutConfig: {
                        animate: true
                    },
                    items: [
                        this.propertyGrid,
                        this.commentsPanel
                    ],
                    margins: '5 0 5 0'
                }
            ],
            tbar: this.menu.getToolbar()
        });
    }

    this.setEvents = function(){
        page.getLayout().getLayoutItems()[1].on("collapse", function() {this.resizeCanvas()}, this);
        page.getLayout().getLayoutItems()[1].on("expand", function() {this.resizeCanvas()}, this );
//
        page.getLayout().getLayoutItems()[2].on("collapse", function() {this.resizeCanvas()}, this);
        page.getLayout().getLayoutItems()[2].on("expand", function() {this.resizeCanvas()}, this);
    }

    this.getCanvas = function() {
        return this.canvas.getCanvas();
    }

    this.getModel = function() {
        return this.model;
    }

    this.getSelectionHandler = function() {
        return this.selectionHandler;
    }

    this.getCurrentLanguage = function() {
        if ( this.menu.languageField != null )
            return this.menu.languageField.getComboBox().getValue();
        else
            return "en";
    }

    this.modelLoaded = function() {
        this.model = this.loader.getModel();
        document.title = this.model.getName();
        
        if (document.title == "")
            document.title = "ProcessEditor@Web";
        //this.menu.languageField.addListener( this.propertyConfig );
        this.pluginManager = new Inubit.Plugins.PluginManager({
            listeners : {
                load: function() {
                    for ( var i = 0; i < this.model.nodes.length; i++)
                        this.model.nodes[i].paintRenderingHints(this.getCanvas());

                    this.model.alignZOrder();

                    this.menu.addPluginsAndFinishing();
                    this.loader.mask.hide();
                },
                scope: this
            }
        });
        this.pluginManager.loadPlugins(this.model.getType());

        this.loadNewElementPalette();
        this.canvas.setSize(parseInt(this.loader.getModelWidth()) + 30, parseInt(this.loader.getModelHeight()) + 30);
        this.displayProperties(this.model);
        this.selectionHandler.singleObject = this.model;
        
        var eastWidth = page.getLayout().getLayoutItems()[2].getWidth();

        page.getLayout().getLayoutItems()[2].items.each( function() {
            this.setWidth(eastWidth);
        });
        
        this.propertyGrid.items.each( function() {
            this.setWidth(eastWidth);
        })
        this.commentsPanel.load( this.model );

        this.setEvents();
    }

    this.addSouthPanel = function (content, titleString) {
        var southPanel = Ext.getCmp("south_region"),
            insertPosition = null;
        for (var i = 0; i < southPanel.items.length; i++) {
            if (southPanel.items.get(i).title == titleString) {
                southPanel.remove(southPanel.items.get(i));
                insertPosition = i;
                break;
            }
        }
        southPanel.expand(true);
        
//        content.addListener("close", function (component) {
//           var items = Ext.getCmp("south_region").items.clone();
//           //items.remove(component);
//           if (items.length == 0) {
//               ProcessEditor.instance.removeSouthPanel();
//           }
//           content.hide();
////           component.removeAll(true);
//        });
        if (insertPosition) {
            southPanel.insert(insertPosition, content);
        } else {
            southPanel.add(content);
        }
        content.setTitle(titleString);
        southPanel.setActiveTab(content);
    }

    this.removeFromSouthPanel = function (title) {
        var southPanel = Ext.getCmp("south_region");
        for (var i = 0; i < southPanel.items.length; i++) {
            if (southPanel.items.get(i).title == title) {
                southPanel.remove(southPanel.items.get(i));
                if (southPanel.items.length == 0) {
                    this.removeSouthPanel();
                }
            }
        }
    }

    this.removeSouthPanel = function () {
        var southPanel = Ext.getCmp("south_region");
        southPanel.removeAll(true);
        southPanel.collapse(false);
    }
    
    this.showSouthPanel = function() {
    	var southPanel = Ext.getCmp("south_region");
    	southPanel.expand();
    }

    this.scrollToNodeById = function (nodeId) {
        var node = this.model.getNodeWithId(nodeId);
        if (node) {
            this.scrollToFrameBounds(node.getFrameBounds());
        }
    }

    this.scrollToEdgeById = function (edgeId) {
        var edge = this.model.getEdgeWithId(edgeId);
        if (edge) {
            this.scrollToFrameBounds(edge.getFrameBounds());
        }
    }

    this.scrollToFrameBounds = function (bounds) {
        if (bounds) {
            var x = bounds.x + bounds.width / 2,
                y = bounds.y + bounds.height / 2,
                scrollElement = this.canvas.getCanvasDOMElement().parentNode;
            if (x && y && scrollElement) {
                x = x - scrollElement.offsetWidth / 2;
                if (x < 0) {
                    x = 0;
                }
                if (x > scrollElement.scrollWidth) {
                    x = scrollElement.scrollWidth
                }
                y = y - scrollElement.offsetHeight / 2;
                if (y < 0) {
                    y = 0;
                }
                if (y > scrollElement.scrollWidth) {
                    y = scrollElement.scrollWidth
                }
                scrollElement.scrollTop = y;
                scrollElement.scrollLeft = x;
            }
        }
    }

    //Change ExtJS DD behavior for DD node creation
    Ext.override(Ext.dd.DDProxy, {
        startDrag: function(x, y) {
            var dragEl = Ext.get(this.getDragEl());
            var el = Ext.get(this.getEl());

            dragEl.applyStyles({border:'','z-index':2000});
            dragEl.update(el.dom.innerHTML);
        },

        onDragOver: function (e, targetId) {
            if(dropZone.getAttribute("id") === targetId) {
                var target = Ext.get(targetId);
                this.lastTarget = target;
            }
        },

        onDragOut: function (e, tragetId) {
            this.lastTarget = null;
        },

        endDrag: function(e) {
            var dragEl = Ext.get(this.getDragEl());
            
            if(this.lastTarget) {
                var nodeClass = dragEl.dom.childNodes[0].getAttribute("nodeclass")

                var x = e.getPageX();
                var y = e.getPageY();

                var editorPos = ProcessEditor.instance.canvas.getExtCmp().getPosition();
                var scroll = dropZone.getScroll();

                x = x - editorPos[0] + scroll.left - 3;
                y = y - editorPos[1] + scroll.top - 3;

                var cluster = ProcessEditor.instance.getModel().getDropableClusterAtPosition(x, y, null);

                ProcessEditor.instance.getModel().createNode(nodeClass, x, y, cluster);
            }
        }
    });

    Ext.EventManager.onWindowResize(this.resizeWindow);

    ProcessEditor.instance = this;
}