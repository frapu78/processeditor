/**
 * Base class for all ProccesEdges and ProcessNodes
 */
Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.ProcessObject = Ext.extend(Ext.util.Observable, {
    constructor : function(config) {
        this.uri = config.uri;
    	this.model = config.model;
    	this.rootComponent = config.rootComponent;

    	this.graphics = null;
    	this.properties = null;

    	this.selectionFrame = null;
    	this.contextMenu = null;
    	this.isSelected = false;
        
        this.addEvents("load");
        this.listeners = config.listeners;
        Inubit.WebModeler.ProcessObject.superclass.constructor.call(this, config);
    },
        
    //"abstract method"
    paint : function(canvas) {
        //should be implemented by subclass
    },
    redraw : function(canvas) {
        var sel = (this.isSelected);

        if (this.graphics) {
            this.graphics.remove();
        }
        this.paint(canvas);
        this.setSelected(sel, true);
    },
    getVisibleProperties : function () {
    	return this.properties;
    },
    //"abstract method"
    setSelected : function(selected, showButtons) {
        //should be implemented by subclass
    },
    select : function() {
        if ( ProcessEditor.instance.selectionHandler.singleSelect(this) ) {
            ProcessEditor.instance.displayProperties(this);
            this.setSelected(true, true);
        }
     },
     showContextMenu : function() {
        if (this.contextMenu == null)
            this.contextMenu = this.createContextMenu();

        this.contextMenu.show();
    },
    /**
     * Refreshes the process objects context menu.
     */
    refreshContextMenu : function () {
    	this.contextMenu = this.createContextMenu();
    },
    //"abstract method"
    createContextMenu : function() {
        //should by implemented by subclass
    },
    //"abstract method"
    //should return upper left corner + width and height
    getBounds : function() {
        //should be implemented by subclass
    },
    //"abstract method"
    remove : function() {
        //should be implemented by subclass
    },
      /**
     * Update a certain property at server
     * @param name name of the updated property
     * @param value new value of that property
     */
    updateProperty : function(name, value) {
        this.setProperty(name, Util.unEscapeString(value));

        var req = new XMLHttpRequest();
        req.open("PUT", this.uri, true);

        var object = this;
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var xml = req.responseXML;
                object.applyPropertyUpdateResponse(xml);
            }
        };

        req.send(this.createUpdateXML(name, value));
    },

    /**
     * Update the type/class of this object. This leads to object deletion and
     * creation, which is why it can't be done using simple property update.
     * @param newType new type for this object
     */
    updateType : function(newType, isUndo) {
        var req = new XMLHttpRequest();
        req.open("PUT", this.uri, true);

        var object = this;
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status == 200) {
                var xml = req.responseXML;
                object.applyTypeUpdateResponse(xml, newType, isUndo);
            }
        };

        req.send(this.createTypeUpdateXML(newType));
    },
    /**
     * Retruns the objects root component (Editor or viewer).
     *
     * @return the root component.
     */
    getRootComponent : function() {
    	return this.rootComponent;
    },
    /**
     * Get recent meta data for this object
     */
    updateMetadata : function() {
        var req = new XMLHttpRequest();
        req.open("GET", this.uri + "/meta", false);
        req.send(null);

        var xml = req.responseXML;
        this.applyMetadataUpdateResponse(xml);
    },
    //"abstract method"
    applyPropertyUpdateResponse : function(xml) {
        //should be implemented by subclass
    },

    //"abstract method"
    applyTypeUpdateResponse : function(xml) {
        //should be implemented by subclass
    },

    //"abstract method"
    applyMetadataUpdateResponse : function(xml) {
        //should be implemented by subclass
    },

    /*
     * XML CREATORS
     */
    serialize : function() {
        var xml = "";
        for (var name in this.properties) {
            //@TODO: Check if this 'remove'-thing still occurs, maybe we'll find out why
            if (name == 'remove') continue;
            xml += "<property name='" + name + "' value='" + this.properties[name] + "'/>";
        }

        return xml;
    },
    createUpdateXML : function(name, value) {
        var xml  = "<update type='property'>\n";
        xml     += "<property name='" + name + "' value='" + value + "'/>";
        xml     += "</update>";

        return xml;
    },
    createTypeUpdateXML : function(newType) {
        var xml  = "<update type='type'>";
        xml     += "<property name='newtype' value='" + newType + "'/>";
        xml     += "</update>";

        return xml;
    },
    /*
     * GETTERS AND SETTERS
     */
    getProperty : function(name) {
        return this.properties[name];
    },
    setProperty : function(name, value) {
        this.properties[name] = value;
    },
    getId : function() {
        return this.getProperty(Inubit.WebModeler.ProcessObject.PROPERTY_ID);
    },
    getType : function() {
        return this.getProperty(Inubit.WebModeler.ProcessObject.PROPERTY_TYPE);
    },
    setModel : function(model) {
        this.model = model;
    },
    isProcessNode : function() {
        return false;
    },
    isProcessEdge : function() {
        return false;
    },
    isResizable : function() {
        return this.properties["#resizable"] == "1";
    }
});

//Property constants
Inubit.WebModeler.ProcessObject.PROPERTY_ID = "#id";
Inubit.WebModeler.ProcessObject.PROPERTY_TYPE = "#type";

//function ProcessObject(uri, model) {
//    this.uri = uri;
//    this.model = model;
//
//    this.graphics = null;
//    this.properties = null;
//
//    this.selectionFrame = null;
//    this.contextMenu = null;
//    this.isSelected = false;
//
//    //"abstract method"
//    this.paint = function(canvas) {
//        //should be implemented by subclass
//    }
//
//    this.redraw = function(canvas) {
//        var sel = (this.isSelected);
//
//        if (this.graphics) {
//            this.graphics.remove();
//        }
//        this.paint(canvas);
//        this.setSelected(sel, true);
//    }
//
//    //"abstract method"
//    this.setSelected = function(selected, showButtons) {
//        //should be implemented by subclass
//    }
//
//    this.select = function() {
//        if ( ProcessEditor.instance.selectionHandler.singleSelect(this) ) {
//            ProcessEditor.instance.displayProperties(this);
//            this.setSelected(true, true);
//        }
//    }
//
//    this.showContextMenu = function() {
//        if (this.contextMenu == null)
//            this.contextMenu = this.createContextMenu();
//
//        this.contextMenu.show();
//    }
//
//    //"abstract method"
//    this.createContextMenu = function() {
//        //should by implemented by subclass
//    }
//
//    //"abstract method"
//    //should return upper left corner + width and height
//    this.getBounds = function() {
//        //should be implemented by subclass
//    }
//
//    //"abstract method"
//    this.remove = function() {
//        //should be implemented by subclass
//    }
//
//    /*
//     * UPDATES TO/FROM SERVER
//     */
//
//    /**
//     * Update a certain property at server
//     * @param name name of the updated property
//     * @param value new value of that property
//     */
//    this.updateProperty = function(name, value) {
//        this.setProperty(name, Util.unEscapeString(value));
//
//        var req = new XMLHttpRequest();
//        req.open("PUT", this.uri, true);
//
//        var object = this;
//        req.onreadystatechange = function() {
//            if (req.readyState == 4 && req.status == 200) {
//                var xml = req.responseXML;
//                object.applyPropertyUpdateResponse(xml);
//            }
//        }
//
//        req.send(this.createUpdateXML(name, value));
//    }
//
//    /**
//     * Update the type/class of this object. This leads to object deletion and
//     * creation, which is why it can't be done using simple property update.
//     * @param newType new type for this object
//     */
//    this.updateType = function(newType, isUndo) {
//        var req = new XMLHttpRequest();
//        req.open("PUT", this.uri, true);
//
//        var object = this;
//        req.onreadystatechange = function() {
//            if (req.readyState == 4 && req.status == 200) {
//                var xml = req.responseXML;
//                object.applyTypeUpdateResponse(xml, newType, isUndo);
//            }
//        }
//
//        req.send(this.createTypeUpdateXML(newType));
//    }
//
//    /**
//     * Get recent meta data for this object
//     */
//    this.updateMetadata = function() {
//        var req = new XMLHttpRequest();
//        req.open("GET", this.uri + "/meta", false);
//        req.send(null);
//
//        var xml = req.responseXML;
//        this.applyMetadataUpdateResponse(xml);
//    }
//
//    //"abstract method"
//    this.applyPropertyUpdateResponse = function(xml) {
//        //should be implemented by subclass
//    }
//
//    //"abstract method"
//    this.applyTypeUpdateResponse = function(xml) {
//        //should be implemented by subclass
//    }
//
//    //"abstract method"
//    this.applyMetadataUpdateResponse = function(xml) {
//        //should be implemented by subclass
//    }
//
//    /*
//     * XML CREATORS
//     */
//    this.serialize = function() {
//        var xml = "";
//        for (var name in this.properties) {
//            //@TODO: Check if this 'remove'-thing still occurs, maybe we'll find out why
//            if (name == 'remove') continue;
//            xml += "<property name='" + name + "' value='" + this.properties[name] + "'/>";
//        }
//
//        return xml;
//    }
//
//    this.createUpdateXML = function(name, value) {
//        var xml  = "<update type='property'>\n";
//        xml     += "<property name='" + name + "' value='" + value + "'/>";
//        xml     += "</update>";
//
//        return xml;
//    }
//
//    this.createTypeUpdateXML = function(newType) {
//        var xml  = "<update type='type'>"
//        xml     += "<property name='newtype' value='" + newType + "'/>";
//        xml     += "</update>";
//
//        return xml;
//    }
//
//    /*
//     * GETTERS AND SETTERS
//     */
//    this.getProperty = function(name) {
//        return this.properties[name];
//    }
//
//    this.setProperty = function(name, value) {
//        this.properties[name] = value;
//    }
//
//    this.getId = function() {
//        return this.getProperty(ProcessObject.PROPERTY_ID);
//    }
//
//    this.getType = function() {
//        return this.getProperty(ProcessObject.PROPERTY_TYPE);
//    }
//
//    this.setModel = function(model) {
//        this.model = model;
//    }
//
//    this.isProcessNode = function() {
//        return false;
//    }
//
//    this.isProcessEdge = function() {
//        return false;
//    }
//}
//
////Property constants
//ProcessObject.PROPERTY_ID = "#id";
//ProcessObject.PROPERTY_TYPE = "#type";


