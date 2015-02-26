/**
 * JS File for root page rendering
 */
RootPage = function( portletMode ) {
    this.portletMode = portletMode;

    var centerID = 'center_region';
    var westID = 'west_region';
    var southID = 'south_region';
    var page;
    var imageSize = Util.PREVIEW_MEDIUM;
    var rootpage = this;
    
    this.pageWidth = 0;
    this.westWidth = 200;
    this.pageHeight = 0;
    this.models = 0;
    this.user = null;

    var sizeBox =
        new Ext.form.field.ComboBox({
            store: new Ext.data.ArrayStore({
                fields: ['size', 'value'],
                data : [
                    [Util.PREVIEW_SMALL_LABEL, Util.PREVIEW_SMALL],
                    [Util.PREVIEW_MEDIUM_LABEL, Util.PREVIEW_MEDIUM],
                    [Util.PREVIEW_LARGE_LABEL, Util.PREVIEW_LARGE],
                    [Util.PREVIEW_XLARGE_LABEL, Util.PREVIEW_XLARGE]
                    ]
                }),
            displayField:'size',
            valueField: 'value',
            fieldLabel: 'Size',
            queryMode: 'local',
            triggerAction: 'all',
            labelAlign: 'right',
            editable: false
            });
    sizeBox.on('select', function() {
    	imageSize = this.getValue();
        rootpage.reloadImageView();
        // Store new size in cookie
        document.cookie = "previewSize="+imageSize+"; expires="+(new Date(2099,12,31)).toGMTString();
    }, sizeBox);
    
    sizeBox.on('afterrender', function() {
    	imageSize = parseInt(Util.readCookie('previewSize'));
    	if ( Ext.isDefined(imageSize) && !isNaN(imageSize)) {
    		this.setValue( imageSize );
    	} else {
            imageSize = Util.PREVIEW_MEDIUM;
    		this.setValue( Util.PREVIEW_MEDIUM );
    	}
    }, sizeBox)


    var statusLabel =
        new Ext.form.Label({
            text: ''
        });

    var statusBar = new Ext.toolbar.Toolbar({
            id: 'statusbar',
            items: [
                statusLabel,
                '->',
                sizeBox
            ],
        })

    /**
     * 
     * @param parentNode
     */
    this.create = function(parentNode) {
        var size = this.getPageSize(parentNode);

        this.user = Util.getCurrentUserInfo();
        this.createBasicLayout(size.width, size.height);
        var myMask = new Ext.LoadMask(Ext.getBody(), {msg:"Please wait, while your models are loaded!", removeMask: true});
        myMask.show();

        this.treeView = new TreeView(this);
        
        page.getLayout().getLayoutItems()[1].add(this.treeView.getExtCmp());
//        page.getLayout().getLayoutItems()[1].doLayout();
        
        this.createImageView();
        page.getLayout().getLayoutItems()[0].setTitle(this.treeView.selectedPath);

        myMask.hide();
        Ext.EventManager.onWindowResize(this.resizeWindow, this);
        page.getLayout().getLayoutItems()[1].on("resize", function(comp, width, height) {
            if ( this.westWidth != null && width != this.westWidth ) {
                this.westWidth = width;
                this.reloadImageView();
            }
        }, this)
    }

    /**
     * Creates the basic layout of the page.
     * The page's size is given by width and height
     *
     * @param width the width of the surrounding container
     * @param height the height of the surrounding container
     */
    this.createBasicLayout = function(width, height) {
        var centerWidth = width;    
        page = new Ext.panel.Panel({
//            renderTo: Ext.getBody(),
            id: RootPage.PAGE_ID,
            anchor: '100% 100%',
//            width: width,
//            maxWidth: width,
//            height: height,
//            maxHeight: height,
            layout: 'border',
            items: [
                {region: 'center',
                     id: centerID,
                     width: centerWidth,
                     height: height,
                     collapsible: false,
                     split: true,
                     margins: '0 2 5 2',
                     border: false,
                     autoScroll: true
                },
                {
                    region: 'west',
                    layout: 'fit',
                    id: westID,
                    width: this.westWidth,
                    height: height,
                    collapsible: true,
                    split: true,
                    border: false,
                    autoScroll: true,
                    margins: '0 0 5 0',
                    title: 'Folders'
                }
            ],
            tbar: new RootPageMenu(this.user, this.portletMode).getToolbar(),
            bbar: statusBar
        });
        
        Ext.create( "Ext.container.Viewport", {
        	renderTo: Ext.getBody(),
        	layout: 'anchor',
        	items: [
        	    page
	        ]
        })
    }

    this.getPageSize = function(parentNode) {
        var width = 0;
        var height = 0;

        if (parentNode == document.body) {
            if (window.innerWidth) {
                width = parseInt(window.innerWidth);
            } else if (document.body && document.body.offsetWidth) {
                width = parseInt(document.body.offsetWidth);
            }

            if (window.innerHeight) {
                height = parseInt(window.innerHeight);
            } else if (document.body && document.body.offsetHeight) {
                height = parseInt(document.body.offsetHeight);
            }

        } else {
            width = parentNode.style.width;
            height = parentNode.style.height;
        }

        this.pageWidth = width;
        this.pageHeight = height;

        return {"width":width, "height":height};
    }

    this.createImageView = function() {
        var panel = this.createMainViewPanel();

        var models = this.treeView.getModelsToDisplay();

        var folders = this.treeView.getFoldersToDisplay();

        var rootpage = this;
        for (var i = 0; i < folders.length; i++) {
            var newPanel = this.createSubFolderPanel(panel, folders[i]);

            if (newPanel == null)
                continue;

            newPanel.body.on("dblclick", function() {
                rootpage.treeView.select(this.getItemId());
                rootpage.selectionChanged();
            }, newPanel)
        }

        if (models != null) {
            var rp = this;
            for (var i = 0; i < models.length; i++) {
                var props = models[i];
//                alert(props);
                var newPanel = this.createPreviewPanel(panel, props);
                newPanel.body.on("click", function() {
                   var imgsrc = this.getImageUri();
                   var modelURI = this.getModelUri();
                   var access = this.getAccess();

                   var win = new Ext.ux.RootPagePreviewWindow( {
                       width: 500,
                       height: 500,
                       imgsrc : imgsrc,
                       imageSize : 200,
                       modelURI : modelURI,
                       access : access,
                       portletMode : rp.portletMode,
                       maskEl: Ext.getCmp(RootPage.PAGE_ID).getEl()
                   })
                   win.show( this.body );
                }, newPanel );

            }
        }
//        panel.doLayout();
    }

    this.createMainViewPanel = function() {

        var width = this.pageWidth - 30 - this.westWidth;

        var reqSize = imageSize + 60;

        var noColumns = Math.floor(width / reqSize);

        //compute column width so that most of avaiable space is used
        var columnWidth = Math.floor(reqSize * (width / (noColumns * reqSize)));
        
        var panel = new Ext.Panel({
            id:'image-view',
            width:width,
            autoHeight:true,
            collapsible:false,
            border: false,
            layout: {
                type: 'table',
                columns: noColumns
            },
            defaults: {
                border:false, 
                width: columnWidth , 
                height: reqSize
            }
        });

        Ext.getCmp(centerID).add(panel);
        return panel;
    }

    this.createSubFolderPanel= function(panel, folderConfig) {
        if (folderConfig.path == TreeView.ATTIC_FOLDER)
            return null;

        var newPanel = new Ext.panel.Panel( {
            preventHeader: true,
            tbar: {
                items: [
                    new Ext.toolbar.TextItem({
                        text: folderConfig.name,
                        height: 19
                    })
                ]
            },
            layout: 'fit',
            itemId: folderConfig.path
        } );

        panel.add(newPanel);

        var div = document.createElement("div");
        div.setAttribute("class", "x-panel-mc");
        div.style.textAlign = "center";
        div.style.paddingTop = "5px";
        div.style.backgroundColor = "white";
        div.innerHTML = "<img src='" + Util.getContext() + Util.IMG_FOLDER + "' width='" + imageSize + "'>";
        div.childNodes[0].style.position = 'relativ';

        newPanel.body.dom.appendChild(div);

        return newPanel;
    }

    this.createPreviewPanel = function(panel, props) {
        var newPanel = new Inubit.WebModeler.ModelPreviewPanel({
            modelInfo : props,
            imageSize: imageSize,
            listeners: {
                "delete": function(uri) {
                    this.deleteModel(uri)
                }, scope: this
            }
        })
        
        panel.add(newPanel);
        
        return newPanel;
    }

    this.resizeWindow = function(newWidth, newHeight) {
        this.pageWidth = newWidth;
        this.pageHeight = newHeight;
        
        page.setSize(newWidth, newHeight);
        page.getLayout().getLayoutItems()[0].setSize(newWidth - this.westWidth , newHeight);
        this.reloadImageView();
    }

    this.reloadImageView = function() {
        page.getLayout().getLayoutItems()[0].removeAll();

        this.createImageView();
    }

    this.selectionChanged = function() {
        page.getLayout().getLayoutItems()[0].removeAll();
        page.getLayout().getLayoutItems()[0].setTitle(this.treeView.selectedPath);

        this.createImageView();
    }

    this.deleteModel = function(uri) {
        var req = new XMLHttpRequest();
        req.open("DELETE", uri, true);

        var rootpage = this;
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var xml = req.responseXML;

                var result = xml.getElementsByTagName("delete")[0].textContent;
                if (result == "moved") {
                    rootpage.treeView.moveToTrash(uri);
                } else if (result == "deleted") {
                    rootpage.treeView.removeModelPhysically(uri);
                }
                
                rootpage.reloadImageView();
            }
        }

        req.send(null);
    }

    this.moveLocationAtServer = function(source, target) {
        var req = new XMLHttpRequest();
        req.open("PUT", Util.getContext() + "/models/locations", true);
        req.setRequestHeader("Content-Type", "text/xml");
        req.send(this.createMoveLocationXML(source, target));
    }

    this.createMoveLocationXML = function(source, target) {
        var xml = "<update>"
        xml += "<property name='source' value='" + source + "'/>";
        xml += "<property name='target' value='" + target + "'/>";
        xml += "</update>"

        return xml;
    }

    this.removeLocation = function(id) {
        var req = new XMLHttpRequest();
        req.open("DELETE", Util.getContext() + "/models/locations", true);

        var rootpage = this;

        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var xml = req.responseXML;
                var result = xml.getElementsByTagName("delete")[0].textContent;

                if (result == "moved") {
                    rootpage.treeView.moveLocationToTrash(id);
                } else if (result == "deleted") {
                    rootpage.treeView.removeLocationPhysically(id);
                }
            }
        }

        req.send("<delete><property name='location' value='" + id + "'/></delete>");
    }

    this.setCenterTitle = function(title) {
        Ext.getCmp(centerID).setTitle(title);
    }

    this.saveCurrentFolder = function() {
        var currentTime = new Date();
        currentTime.setHours( 23 );
        currentTime.setMinutes( 59 );
        document.cookie = "lastFolder="+this.treeView.selectedPath+"; expires="+currentTime.toGMTString();
    }
    
}

RootPage.PAGE_ID = 'rootpage';