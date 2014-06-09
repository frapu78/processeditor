Ext.define('Inubit.WebModeler.plugins.DomainAttribute', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 
        'name', 
        'type', 
        'multiplicity', 
        'defaultValue', 
        'visibility'
    ]
})

Ext.define('Inubit.WebModeler.plugins.DomainAttributeDialog', {
    extend: 'Inubit.WebModeler.plugins.UMLAttributeDialog', 
    constructor : function( config ) {
        Inubit.WebModeler.plugins.DomainAttributeDialog.superclass.constructor.call( this, config );
    },
    
    initStore: function() {
        return new Ext.data.ArrayStore({
            autoDestroy: true,
            idIndex: 0,
            model: "Inubit.WebModeler.plugins.DomainAttribute"
        })
    },
    
    getColumns: function() {
        var columns = this.callParent();
        columns.pop();
        columns.push({header: 'Default', dataIndex: 'defaultValue', field: 'textfield'});
        return columns;
    },

    addNewAttribute : function() {
        var ids = this.store.collect("id");
        var contains = true;
        var id;

        while( contains ) {
            id = "" + Math.floor(Math.random() * 1111111111);
            contains = false;
            for ( var i = 0; i < ids.length; i++ ) {
                if ( ids[i] == id ) {
                    contains = true;
                    break;
                }
            }
        }

        var att = new Inubit.WebModeler.plugins.DomainAttribute({
            id: id,
            name : Util.generateAttributeName(this.object),
            type : 'text',
            multiplicity : '0..1',
            visibility : 'PUBLIC',
            defaultValue: ''
        });

        this.store.add( att );
    }
} );


