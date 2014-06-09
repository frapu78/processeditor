/*
 * Process Editor
 *
 * (C) 2011 inubit AG
 *
 * http://inubit.com
 *
 */
Ext.namespace("Ext.ux");

Ext.ux.InfoDialog = Ext.extend( Ext.ux.Dialog, {
    constructor : function( config ) {
        Ext.ux.InfoDialog.superclass.constructor.call(this, config);
    },
    
    initComponent : function() {
        this.title = this.getTitle();
        Ext.ux.InfoDialog.superclass.initComponent.call(this)
    },

    setPlugin : function( plugin ) {
        this.plugin = plugin;
    }
});