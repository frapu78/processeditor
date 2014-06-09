Ext.define( "Inubit.WebModeler.plugins.UMLAttribute", {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'type',
        'multiplicity',
        'visibility'
    ]
});

Ext.define( "Inubit.WebModeler.plugins.AttributeTyp", {
    extend: 'Ext.data.Model',
    fields: [
        'type'
    ]
});


Ext.define( "Inubit.WebModeler.plugins.UMLAttributeDialog", {
    extend: 'Ext.ux.Dialog',
    border: false,
    layout: 'fit',
    height: 150,
    width: 400,
    autoScroll: true,
    constructor : function( config ) {
        if ( config == null )
            config = {};

        Inubit.WebModeler.plugins.UMLAttributeDialog.superclass.constructor.call( this, config );
    },
    
    initComponent: function() {
        this.store = this.initStore();
        
        this.typesStore = this.initTypesStore();
        this.typesCombobox = new Ext.form.field.ComboBox ({                                         
            typeAhead: true,
            lazyRender:false,
            disableKeyFilter: true,
            triggerAction: 'all',
            width: 165,
            queryMode: 'local',
            store: this.typesStore,
            valueField: 'type',
            allowBlank: false,
            displayField: 'type'
        });
        
        this.grid = new Ext.grid.Panel({
            border: false,
            autoScroll: true,
            store: this.store,
            height: 150,
            plugins: [
                Ext.create('Ext.grid.plugin.CellEditing', {
                    clicksToEdit: 2
                })
            ],
            columns: this.getColumns(),
            selType: 'rowmodel',
            selModel: new Ext.selection.RowModel({
                type: 'MULTI',
                listeners: {
                    select: function( selModel ) {
                        Ext.getCmp( "attr_del_button" ).enable();
                        Ext.getCmp( "attr_up_button" ).enable();
                        Ext.getCmp( "attr_down_button" ).enable();
                    },
                    deselect: function( selModel ) {
                        if (selModel.getCount() == 0) {
                            Ext.getCmp( "attr_del_button" ).disable();
                            Ext.getCmp( "attr_up_button" ).disable();
                            Ext.getCmp( "attr_down_button" ).disable();
                        }
                    }
                }
            })
        });
    
        this.items = [
            this.grid
        ];
        
        this.tbar =  new Ext.toolbar.Toolbar({
                items: [
                    {icon: Util.getContext() + Util.ICON_PLUS,
                        handler: function() {
                            this.addNewAttribute();
                        },
                        scope: this
                    },
                    {icon: Util.getContext() + Util.ICON_DELETE,
                        disabled: true,
                        id: "attr_del_button",
                        handler: function() {
                            this.deleteSelectedAttribute();
                        },
                        scope : this
                    },
                    {icon: Util.getContext() + Util.ICON_MOVE_UP,
                        disabled: true,
                        id: "attr_up_button",
                        handler : function() {
                            this.moveAttribute("up");
                        },
                        scope: this
                    },
                        {icon: Util.getContext() + Util.ICON_MOVE_DOWN,
                        disabled: true,
                        id: "attr_down_button",
                        handler : function() {
                            this.moveAttribute("down");
                        },
                        scope: this
                    }
                ]
            });
            
        Inubit.WebModeler.plugins.UMLAttributeDialog.superclass.initComponent.call(this);
    },

    addNewAttribute : function() {
         var att = new Inubit.WebModeler.plugins.UMLAttribute({
            name : 'newAttribute',
            type : 'text',
            multiplicity : '1',
            visibility : 'PUBLIC'
        })

        this.store.add( att );

    },
    
    deleteSelectedAttribute : function() {
        var selections = this.grid.getSelectionModel().getSelection();

        for ( var i = 0; i < selections.length; i++ )
            this.store.remove( selections[i] );
        
        Ext.getCmp( "attr_del_button" ).disable();
    },

    moveAttribute : function( dir ) {
        var selModel = this.grid.getSelectionModel();
        var selections = selModel.getSelection();

        for ( var i = 0; i < selections.length; i++ ) {
            var index = this.store.indexOf( selections[i] );
            if ( index > 0 ) {
                var changeWith = this.store.getAt( index - 1 );
                this.store.remove( selections[i] );
                this.store.remove( changeWith );
                this.store.insert( index + ( dir == "up" ?  -1 :  1) , [ selections[i] ]);
                this.store.insert( index, [ changeWith ]);
            }
        }

        selModel.select( selections );
    },

    getTitle : function() {
        return "Attributes"
    },

    getJSONData : function() {
        var data = new Array();

        this.store.each( function() { data.push(this.data) });

        return data;
    },

    setData : function( data ) {
       
       this.store.loadData( data.attributes );
       
       for (var i=0; i < data.types[0].length; i++) {
           var typeModel = new Inubit.WebModeler.plugins.AttributeTyp({
               "type" : data.types[0][i]
           });
           this.typesStore.add(typeModel);
       }
    },
    
    initStore: function() {
        return new Ext.data.ArrayStore({
            autoDestroy: true,
            idIndex: 0,
            model: "Inubit.WebModeler.plugins.UMLAttribute"
        });
    },
    
    initTypesStore: function() {
        return new Ext.data.ArrayStore({
            autoDestroy: true,
            idIndex: 0,
            model: "Inubit.WebModeler.plugins.AttributeTyp"
        });
    },
    
    getColumns: function() {
        return [
            {id: 'name', header: 'Name', dataIndex: 'name', flex: 1, field: 'textfield'},
            {header: 'Type', dataIndex: 'type',
                field: this.typesCombobox
            },
            {header: 'Multiplicity', dataIndex: 'multiplicity', editable: true,
                field: new Ext.form.field.ComboBox( {
                    typeAhead: true,
                    lazyRender:false,
                    disableKeyFilter: true,
                    triggerAction: 'all',
                    width: 165,
                    queryMode: 'local',
                    store: new Ext.data.ArrayStore({
                        id: 0,
                        fields: ["id", "text"],
                        data: [ [0, "1..*" ], [1, "0..1"], [2, "*"], [ 3, "1"] ]
                    }),
                    valueField: 'text',
                    allowBlank: true,
                    displayField: 'text'
                })
            },
            {header: 'Visibility', dataIndex: 'visibility', editable: true,
             field: new Ext.form.field.ComboBox( {
                typeAhead: false,
                triggerAction: 'all',
                lazyRender:false,
                width: 165,
                queryMode: 'local',
                store: new Ext.data.ArrayStore({
                    id: 0,
                    fields: ["id", "text"],
                    data: [ [0, "PUBLIC" ], [1, "PRIVATE"], [2, "PROTECTED"], [ 3, "PACKAGE"] ]
                }),
                valueField: 'text',
                editable: false,
                allowBlank: false,
                forceSelection: true,
                displayField: 'text'} )
            }
        ];
    }

} );


