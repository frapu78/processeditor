Ext.define("Inubit.WebModeler.plugins.ValidationInfoDialog", {
    extend: 'Ext.ux.InfoDialog',
    closable: false,
    layout: 'anchor',
    height: 210,
    defaults: {
        anchor: '100%'
    },
    
    initComponent: function() {
        this.store = new Ext.data.ArrayStore({
            autoDestroy:true,
            idIndex:0,
            fields:["ResultLineID","TypeIcon","ShortText","LongText","InvolvedNodeIDs","InvolvedEdgeIDs","ProblemType"]
        });

        this.filterOptions = {"Error" : true, "Warning" : true, "Information" : true}

        this.selModel = new Ext.selection.RowModel({
            type: 'SINGLE',
            listeners: {
                select: function (selectionModel, record, index) {
                    var description = record.get('LongText');
                    this.detailsArea.setValue(description);
                    Inubit.Plugins.PluginHelper.selectObjects(record.get('InvolvedNodeIDs'), record.get('InvolvedEdgeIDs'), Util.COLOR_RED, true);
                },
                scope: this
            }
        });

        this.grid = new Ext.grid.Panel({
            anchor: '100%, 75%',
            tbar : new Ext.toolbar.Toolbar({
                items: [
                    {
                        cls:'x-btn-text-icon',
                        listeners:{
                            click: function (button, event) {
                                this.plugin.refreshData();
                                this.filterData();
                            },
                            scope: this
                        },
                        width:80,
                        icon:'/pics/symbols/refresh15x16.png',
                        text:'Refresh',
                        xtype:'button'
                    },
                    {xtype:'tbseparator'},
                    {value:'Show',xtype:'displayfield'},
                    {xtype:'tbspacer'},
                    {
                        boxLabel:'Errors',
                        listeners:{
                            change: function (checkbox, newValue) {
                                this.filterOptions.Error = newValue;
                                this.filterData();
                            },
                            scope: this
                        },
                        checked:true,
                        xtype:'checkbox'},
                    {xtype:'tbspacer'},
                    {
                        boxLabel:'Warnings',
                        listeners:{
                            change: function (checkbox, newValue) {
                                this.filterOptions.Warning = newValue;
                                this.filterData();
                            },
                            scope: this
                        },
                        checked:true,
                        xtype:'checkbox'
                    }, {xtype:'tbspacer'},
                    {
                        boxLabel:'Information',
                        listeners:{
                            change: function (checkbox, newValue) {
                                this.filterOptions.Information = newValue;
                                this.filterData();
                            },
                            scope: this
                        },
                        checked:true,
                        xtype:'checkbox'
                    },
                    {xtype:'tbspacer'}
                ]
            }),
            selModel:this.selModel,
            autoExpandColumn:'ShortText',
            split:true,
            region:'center',
            autoWidth:'true',
            stripeRows:true,
            collapsible:false,
            store: this.store,
            columns:[
                {id:'TypeIcon', header:'Type', dataIndex:'TypeIcon', width:50},
                {id:'ShortText', header:'Problem', dataIndex:'ShortText', flex:1}
            ]
        });
        
        this.detailsArea = new Ext.form.DisplayField({
            disabled:true,
            style:{padding:'5px'},
            value:'Click on a problem to view its detailed description.'
        });

        this.detailsPanel = new Ext.Panel({
            collapseMode:'mini',
            layout:'fit',
            titleCollapse:true,
            listeners:{
                resize: function (panel, width, height) {
                    panel.setSize(width, height);
                }
            },
            split:true,
            region:'south',
            title:'Details',
            anchor: '100%, 75%',
            collapsible:true,
            items:this.detailsArea
        });
        
        this.items = [
                this.grid,
                this.detailsPanel
            ];
            
         Inubit.WebModeler.plugins.ValidationInfoDialog.superclass.initComponent.call(this);   
    },

    setData : function( data ) {
        if ( data.length == 0 ) {
            Ext.Msg.show({
                title: 'Validation Result',
                msg: "No problems found!",
                icon: Ext.Msg.INFO,
                buttons: Ext.Msg.OK
            })
        }

        this.store.loadData( data );
    },

    filterData : function() {
        this.store.filterBy( function( record, id ) {
            return this.filterOptions[record.get('ProblemType')];
        }, this );
    },

    getTitle : function() {
        return "Validation Result"
    }
});