Ext.namespace("Inubit.Plugins");

Inubit.Plugins.Plugin = Ext.extend( Ext.util.Observable, {
    constructor: function( config ) {
        this.uri = config.uri;
        this.scope = config.scope;
        this.inToolbar = config.inToolbar;
        this.menuItem = null;
        this.addEvents("load");
        this.listeners = config.listeners;
        
        Inubit.Plugins.Plugin.superclass.constructor.call( this, config );
        this.initMenuItem();
    },

    getMenuItem : function() {
        if ( this.menuItem.iconOffset == null  )
            this.menuItem.iconOffset = this.iconOffset;
        return this.menuItem;
    },

    initMenuItem : function() {
        Ext.Ajax.request({
           url: this.uri + "?menu",
           method: "GET",
           success: function( response, options ){
               var json = Ext.decode(response.responseText);
               this.menuItem = json.menuItem;

               if ( this.scope != "OBJECT" )
                   this.setMenuItemAction();

               this.fireEvent("load");
           },
           scope: this
        });
    },
    
    setIconOffset : function( offset ) {
        this.iconOffset = offset;
    },

    //abstract
    bindActionToDomNode : function( domNode ) {
        return false;
    },

    //abstract
    setMenuItemAction : function() {
        return false;
    }
});