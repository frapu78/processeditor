/*
 * Process Editor
 *
 * (C) 2011 inubit AG
 *
 * http://inubit.com
 *
 */

Ext.define("Inubit.WebModeler.ModelPreviewPanel",{
    extend: 'Ext.panel.Panel',
    modelInfo: null,
    imageSize: 200,
    
    constructor : function( config ) {
        this.model = config.modelInfo.model;
        this.name = config.modelInfo.name;
        this.image = config.modelInfo.image;
        this.access = config.modelInfo.access;
        this.imageSize = config.imageSize;
        this.addEvents("delete");
        Inubit.WebModeler.ModelPreviewPanel.superclass.constructor.call( this, config );
    },
    
    initComponent : function() {
        this.title = Util.unEscapeString(this.modelInfo.name);
        this.layout = {
            type: 'vbox', 
            align : 'center'
        };
        
        this.preventHeader = true;
        this.textItem = new Ext.toolbar.TextItem({
            text: this.name
        });
        
        this.tbar = new Ext.toolbar.Toolbar({
            cls: 'root-preview-tb',
            height: 24,
            items: [
                this.textItem,
                '->',
                new Ext.panel.Tool({
                   tooltip: 'Export as png',
                   type: 'png',
                   handler: function() {
                       window.open(this.modelInfo.model + ".png");
                   }, scope: this
                }),
                new Ext.panel.Tool({
                   type: 'pdf',
                   handler: function() {
                       window.open(this.modelInfo.model + ".pdf");
                   }, scope: this
                }),
                new Ext.panel.Tool({
                   type: 'delete',
                   handler: function(event, tEl, panel, tool){
                       this.fireEvent("delete", this.modelInfo.model);
                   }, scope: this
                })
            ]
        });

        this.items = [
           new Ext.Img({
               src: this.image + "?size=" + this.imageSize,
               width : this.imageSize,
               style: {
                   paddingTop: "15px"
               }
           })
       ];
       
       this.on("resize", function( comp, width, height) {
            //shorten title to ensure that tools are shown
            this.textItem.setWidth( width - 70 );
            //TODO think about showing long title with dots 
       });
        
        Inubit.WebModeler.ModelPreviewPanel.superclass.initComponent.call(this);
    },
    
    getModelUri: function() {
        return this.model;
    },
    
    getImageUri: function() {
        return this.image;
    },
    
    getName: function() {
        return this.name;
    },
    
    getAccess: function() {
        return this.access;
    }
    
});