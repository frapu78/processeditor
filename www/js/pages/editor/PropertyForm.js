Ext.define("Inubit.WebModeler.PropertyForm", {
    extend: 'Ext.form.Panel',
    displayProperties: function( object ) {
        //this.removeAll( true );
        
        if ( object.properties ) {
            var gray = false;
            var visProperties = object.getVisibleProperties();
            var toAdd = []
            for (var key in visProperties) {
                if (    key.indexOf("#") == 0 || 
                        key == "remove" || 
                        this.modeler.propertyConfig.getType(object, key) != this.propType) 
                    continue;
                
                var objectType = object.getType();
                var props = this.modeler.getModel().getType().propertyTypes[objectType];
                
                if (props == null) {
                    if (object instanceof Inubit.WebModeler.ProcessModel)
                        props = new Array();
                    else {
                        this.modeler.getModel().getType().fetchPropertiesFromServer(objectType);
                        props = ProcessEditor.instance.getModel().getType().propertyTypes[objectType];
                    }
                }
                
                if(props) {
                    if (props[key] == null)
                        props[key] = {type: 'DEFAULT'};
                    
                    var value = object.getProperty(key);
                    var label = ProcessEditor.instance.propertyConfig.getLabel(object, key);
                    
                    var formItemCls = "x-form-item";  
                    if ( gray )
                        formItemCls += " gray-field";
                    
                    var edi = this.modeler.getModel().getType().createEditor(props[key], key, value, label, formItemCls);
                    toAdd.push( edi );
                    gray = !gray;
                }
            }
            
            this.add(toAdd);
        }
    },
    
    applyParentSize : function( parentWidth, parentHeight ) {
        var items = this.getLayout().getLayoutItems();
        var newWidth = parentWidth;
        var itemsHeight = 0;
        
        for ( var i = 0; i < items.length; i++ ) 
            itemsHeight += items[i].getHeight();
        
        if ( itemsHeight > parentHeight ) 
            newWidth -= 25;

        this.items.each( function() { 
            this.setWidth(newWidth)
        });
    }
});

//
//Ext.ux.PropertyForm = function ( config ) {
//    Ext.ux.PropertyForm.superclass.constructor.call( this, config );
//}
//
//Ext.extend( Ext.ux.PropertyForm , Ext.form.FormPanel , {
//
//    displayProperties : function( object ) {
//        this.expand( false );
//        this.removeAll(true);
//
//        if (object.properties)
//            var gray = false;
//
//            for (var key in object.getVisibleProperties()) {
//                if (key.indexOf("#") == 0 || key == "remove" || this.modeler.propertyConfig.getType(object, key) != this.propType) continue;
//                var props = this.modeler.getModel().getType().propertyTypes[object.properties["#type"]];
//
//                //if props is undefined try to fetch properties from server
//                if (props == null) {
//                    this.modeler.getModel().getType().fetchPropertiesFromServer(object.properties["#type"]);
//                    props = ProcessEditor.instance.getModel().getType().propertyTypes[object.properties["#type"]];
//                }
//
//                if(props) {
//                    if (props[key] == null)
//                        props[key] = {type: 'DEFAULT'};
//
//                    var value = object.getProperty(key);
//                    var label = ProcessEditor.instance.propertyConfig.getLabel(object, key);
//                    var edi = this.modeler.getModel().getType().createEditor(props[key], key, value, label);
//                    //edi.setSize( this.el.parent().getSize().width - 100);
//                    this.add( edi );
//                    this.doLayout();
//
////                    //color field value must be set after rendering
////                    if (props[key].type == 'COLOR') {
////                        edi.setValue(value);
////                    }
//
//                    if (props[key].type == 'DEFAULT' || props[key].type == 'MULTILINE') {
//                        var keymap = new Ext.KeyMap(edi.id, [{
//                            key: Ext.EventObject.ENTER,
//                            ctrl: false,
//                            shift: false,
//                            fn: function() {
//                               this.fireEvent("change");
//                            },
//                            stopEvent: true,
//                            scope:edi
//                        }]);
//                        keymap.enable();
//                    }
//
//                    this.doLayout();
////                    alert("jo");
//                    var el
//                    if ( props[key].type == 'BOOLEAN') {
//                        el = edi.el.parent().parent();
//                        edi.getEl().setStyle("margin-top", "4px");
//                        edi.getEl().setStyle("margin-left", "1px");
//
//                    } else {
//                        el = edi.getEl().parent();
//                    }
//
//                    el.setStyle("padding-top", "3px");
//                    el.setStyle("padding-bottom", "3px");
//
//                    if ( props[key].type == 'LIST' || props[key].type == 'COLOR') {
//                        el.dom.getElementsByTagName("img")[0].style.marginTop = "3px";
//                    }
//
//                    if ( gray ) {
//                        el.setStyle("background-color", Util.COLOR_GRAY);
//
//                        if ( props[key].type == 'LIST' || props[key].type == 'COLOR') {
//                            el.parent().setStyle("background-color", Util.COLOR_GRAY);
//                        }
//                    }
//
//                    gray = !gray;
//                }
//            }
//
//        var eastRegion = Ext.getCmp('east_region');
//
//        var items = this.items;
//        var newWidth = eastRegion.getWidth(true) - 100;
//        if ((items.getCount() + 1)* 30 > eastRegion.getHeight(true)) {
//            newWidth -= 25;
//        }
//
//        items.each( function() { 
//            if (this instanceof Ext.form.TextField)
//                this.el.setWidth( newWidth );
//            
//            this.setWidth(newWidth)
//        });
//
//        this.doLayout();
//    }
//
//});

