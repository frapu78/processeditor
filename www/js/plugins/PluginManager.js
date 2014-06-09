Ext.namespace("Inubit.Plugins");

Inubit.Plugins.PluginManager = Ext.extend( Ext.util.Observable, {
    constructor : function( config ) {
        this.addEvents("load");
        this.listeners = config.listeners;
        this.plugins = new Array();
        this.toolbarPlugins = new Array();
        this.objectPlugins = new Array();
        this.renderingHints = new Array();

        this.toLoad = 0;
        this.loaded = 0;

        Inubit.Plugins.PluginManager.superclass.constructor.call( this, config );

        Inubit.Plugins.PluginManager.instance = this;
    },

    loadPlugins: function( modelType ) {
        var reqUri = "/plugins";
        if ( modelType )
            reqUri += "?modeltype=" + modelType.type;
        
        Ext.Ajax.request({
            method: "GET",
            url: Util.getContext() + reqUri,
            success : function ( response, options ) {
                var json = Ext.decode( response.responseText );
                var modelPlugs = json.model;

                this.toLoad = json.model.length + json.object.length;

                var i;
                for (i = 0; i < modelPlugs.length; i++) {
                    var p = modelPlugs[i];
                    var id = p.id;

                    var newPlugin = this.parsePlugin( p, "MODEL" );
                    if ( p.toolbar)
                        this.toolbarPlugins.push(newPlugin);
                    else {
                        this.plugins.push({id: id, plugin: newPlugin});
                    }
                }

                var objectPlugs = json.object;
                for (i = 0; i < objectPlugs.length; i++) {
                    var o = objectPlugs[i];
                    var className = o.classname;
                    var plugins = new Array();
                    var hints = new Array();
                    var plugs = o.plugins;
                    for ( var j = 0; j < plugs.length; j++ ) {
                        var p = plugs[j];
                        var newPlugin = this.parsePlugin(p, "OBJECT");

                        if ( Ext.isDefined(p.renderInfo) )
                            hints.push(p.renderInfo);

                        newPlugin.setIconOffset( p.iconOffset );
                        plugins.push( newPlugin );
                    }
                    this.objectPlugins[className] = plugins;
                    this.renderingHints[className] = hints;
                }
            },
            scope: this
        });
    },

    parsePlugin : function( p, scope ) {
        var type = p.type;
        var uri = p.uri;

        var newPlugin;

        var config = {
            uri : uri,
            scope : scope,
            inToolbar : p.toolbar,
            listeners: {
                load : this.pluginLoaded,
                scope: this
            }
        };

        if (type == "SIMPLE")
            newPlugin = new Inubit.Plugins.SimplePlugin( config );
        else if (type == "FORM")
            newPlugin = new Inubit.Plugins.FormPlugin( config );
        else if (type == "DIALOG")
            newPlugin = new Inubit.Plugins.DialogPlugin( config );
        else if (type == "INFODIALOG")
            newPlugin = new Inubit.Plugins.InfoDialogPlugin( config );

        return newPlugin;
    },

    getMenuButton : function() {
        if ( this.button == null ) {
            this.menu = new Ext.menu.Menu();

            this.button = new Ext.Button({
                text: 'Plugins',
                icon: Util.getContext() + Util.ICON_PLUGIN,
                menu: this.menu
            });

            for ( var i = 0; i < this.plugins.length; i++ ) 
                this.menu.add( this.plugins[i].plugin.getMenuItem() );
        }
        return this.button;
    },

    getObjectPlugins : function( className ) {
        return this.objectPlugins[className] == null ? new Array() : this.objectPlugins[className];
    },

    getRenderingHints : function( className ) {
        return this.renderingHints[className] == null ? new Array() : this.renderingHints[className];
    },

    pluginLoaded : function() {
        this.loaded++;
        if ( this.loaded == this.toLoad ) {
            this.fireEvent("load");
        }
    },
    
    getToolbarPlugins : function() {
        var tbPlugins = new Array();

        for (var i = 0; i < this.toolbarPlugins.length; i++) {
            tbPlugins.push(this.toolbarPlugins[i].getMenuItem());
        }

        return tbPlugins;
    },

    getPluginById : function (id) {
        for (var i = 0; i < this.plugins.length; i++) {
            if (this.plugins[i].id == id) {
                return this.plugins[i].plugin;
            }
        }
        return null;
    }
    
});

Inubit.Plugins.PluginManager.getModelStateInfoXML = function() {
        var id = ProcessEditor.instance.getModel().tmpId;
        var selHandler = ProcessEditor.instance.getSelectionHandler();

        var xml = "<modelinfo id='" + id + "'>";
        xml += "<selection>";

        xml += "<nodes>"
        var nodes = selHandler.getSelectedNodes();
        for (var i = 0; i < nodes.length; i++)
            xml += "<node id='" + nodes[i].getId() + "'/>";
            
        xml += "</nodes>";

        xml += "<edges>"
        var edges = selHandler.getSelectedEdges();
        for (i = 0; i < edges.length; i++)
            xml += "<edge id='" + edges[i].getId() + "'/>";
            
        xml += "</edges>";


        xml += "</selection>";
        xml += "</modelinfo>";

        return xml;
}

Inubit.Plugins.PluginManager.getModelStateInfoJSON = function() {
    var id = ProcessEditor.instance.getModel().tmpId;
    var selHandler = ProcessEditor.instance.getSelectionHandler();

    var nodeIds = new Array();
    var nodes = selHandler.getSelectedNodes();
    //alert(nodes.length);
    for (var i = 0; i < nodes.length; i++)
        nodeIds.push(nodes[i].getId());
    //alert(nodeIds.length);
    var edgeIds = new Array();
    var edges = selHandler.getSelectedEdges();
    for (i = 0; i < edges.length; i++)
        edgeIds.push(edges[i].getId());

    return {id: id, nodes: nodeIds, edges: edgeIds};
}