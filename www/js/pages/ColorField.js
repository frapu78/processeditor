Ext.define('Ext.form.field.Color', {
    extend: 'Ext.form.field.Picker',
    
    constructor: function( config ) {
        Ext.form.field.Color.superclass.constructor.call(this, config);
        this.addEvents("valueChange");
    },
    
    initComponent: function() {
        Ext.form.field.Color.superclass.initComponent.call(this);
        
        this.listeners = Ext.applyIf({
            afterRender: function() {
                this.setRawValue(Util.fromJavaRGB(this.value));
                
                this.inputEl.addKeyMap( {
                    key: Ext.EventObject.ENTER,
                    fn: function() {
                        this.setRawValue(this.getRawValue());
                        this.fireEvent("valueChange");
                    },
                    scope: this
                })
            }, scope: this
        }, this.listeners);        
    },
    
    createPicker: function() {
        return Ext.create('Ext.picker.Color', {
            renderTo: document.body,
            floating: true,
            ownerCt: this.ownerCt,
            pickerOffset: 0,
            height: 150,
            listeners: {
                select: function() {
                    this.onSelect();
                }, scope: this
            }
        });
    },
    
    alignPicker: function() {
        Ext.form.field.Color.superclass.alignPicker.call(this);
        this.picker.setHeight(130);
    },
    
    onSelect: function() {
        this.setRawValue(this.picker.getValue());
        this.fireEvent("select", this, this.picker.getValue());
        this.collapse();        
    },    
    
    setRawValue: function(v) {
        Ext.form.field.Color.superclass.setRawValue.call(this, v);
        if ( v != null && v != '' && this.inputEl ) {
            this.inputEl.setStyle( {
                'background': 'none',
                'background-color': '#' + v
            });
        }
    },
    
    rawToValue: function(rawV) {
        return "" + Util.toJavaRGB(rawV);
    }
});