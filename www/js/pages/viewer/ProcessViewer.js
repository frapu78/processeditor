
function ProcessViewer( portletMode ) {
    var widthEast = 225;
    var eastId = 'east_region';
    var centerId = 'center_region';
    var page = null;

    this.canvas = null;
    this.portletMode = portletMode;
    this.commentsPanel = null;
    this.model = null;
    this.modelType = null;
    this.loader = null;

    this.mouseListener = new Inubit.WebModeler.handler.ProcessViewerMouseListener({
        viewer: this,
        componentId: centerId
    });

    this.createViewer = function( parentNode ) {
        var size = Util.getComponentSize(parentNode);
        var width = size.width;
        var height = size.height;

         this.commentsPanel = new Inubit.WebModeler.CommentPanel({
            autoHeight: true,
            buttonAlign: 'right',
            autoScroll: true,
            border: false,
            title: 'Model comments'
        });

        this.createBasicLayout(width, height, parentNode);

        this.createSurface();
    }

    this.createBasicLayout = function( width, height, parentNode ) {
        var centerWidth = width - widthEast;

        page = new Ext.Panel({
            renderTo: parentNode,
            width: width,
            maxWidth: width,
            height: height,
            maxHeight: height,
            layout: 'border',
            items: [
                {region: 'center',
                     id: centerId,
                     width: centerWidth,
                     height: height,
                     border: false,
                     collapsible: false,
                     split: true,
                     margins: '5 0 5 0',
                     style: 'background-color:"white"'
                },
                {
                    region: 'east',
                    id: eastId,
                    height: height,
                    width: widthEast,
                    collapsible:  true,
                    collapseMode: 'mini',
                    split: true,
                    autoScroll: true,
                    layout: 'accordion',
                    margins: '5 0 5 0',
                    items: [
                        this.commentsPanel
                    ]
                }
            ],
            tbar: new ProcessViewerMenu( this.portletMode )
        });

        page.getLayout().getLayoutItems()[1].on("collapse", function() {this.resizeCanvas()}, this);
        page.getLayout().getLayoutItems()[1].on("expand", function() {this.resizeCanvas()}, this);
//        page.getLayout().getLayoutItems()[0].on("resize", function() {this.resizeCanvas()}, this);
//        page.getLayout().getLayoutItems()[1].on("resize", function() {
//        })
    }

    this.createSurface = function() {
        var centerRegion = Ext.getCmp(centerId);
        var canvasMinWidth = centerRegion.getWidth() - 2;
        var canvasMinHeight = centerRegion.getHeight() - 
            page.getDockedItems()[0].getHeight() - 2 + 5;

        var width = canvasMinWidth - 20;
        var height = canvasMinHeight - 20;

        this.canvas = new Canvas(width, height, centerRegion, canvasMinWidth, canvasMinHeight, this.mouseListener);
    }

    this.getCanvas = function() {
        return this.canvas.getCanvas();
    }

    this.getModel = function() {
        return this.model;
    }

    this.displayProperties = function( object ) {
        return false;
    }

    this.init = function( id, version, type ) {
        this.loader = new Inubit.WebModeler.ModelLoader({
             id : id,
             version : version,
             type : type,
             mode : Inubit.WebModeler.ModelLoader.MODE_VIEW,
             listeners: {
                 load: this.modelLoaded,
                 scope: this
             }
        });
        this.loader.load(this.getCanvas());
    }

    this.modelLoaded = function() {
        this.model = this.loader.getModel();
        this.allowComments = (this.model.access == "COMMENT");
        document.title = this.model.getName();

        if (document.title == "")
            document.title = "ProcessViewer@Web";

        this.canvas.setInitialSize(parseInt(this.loader.getModelWidth()) + 30, parseInt(this.loader.getModelHeight()) + 30);

        this.commentsPanel.load( this.model );

        this.loader.mask.hide();
        Ext.QuickTips.init();
    }

    this.resizeWindow = function(newWidth, newHeight) {
        var east = Ext.getCmp(eastId);

        var eWidth = east.getWidth();
        getLayout().getLayoutItems()[1].setSize(eWidth, newHeight);
        getLayout().getLayoutItems()[0].setSize(newWidth - eWidth, newHeight);

        page.setSize(newWidth, newHeight);

        page.doLayout();
        ProcessViewer.instance.resizeCanvas();

    }

    this.resizeCanvas = function () {
        var cWidth = parseInt(page.getLayout().getLayoutItems()[0].getWidth(true));
        var cHeight = parseInt(page.getLayout().getLayoutItems()[0].getHeight(true));

        this.canvas.resize(cWidth, cHeight);
        Ext.getCmp(centerId).doLayout();
    }

    Ext.EventManager.onWindowResize(this.resizeWindow);

    ProcessViewer.instance = this;
}