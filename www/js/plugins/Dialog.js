Ext.namespace("Ext.ux");

Ext.ux.Dialog = Ext.extend( Ext.form.Panel, {
    constructor : function( config ) {
        Ext.ux.Dialog.superclass.constructor.call(this, config);
    },

    setData : function( data ) {
        //override this in subclass
    },

    getJSONData : function() {
        //override this in subclass
        return {};
    },
    
    getTitle : function() {
        //override this in subclass
        return "Dialog";
    }
});