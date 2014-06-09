function LanguageField() {
    this.listeners = new Array();

    this.fields = [ 'id', 'short','long' ];

    this.data = [   [1, 'de', 'Deutsch'], 
                    [2, 'en', 'English']
                ];

    this.comboBox = new Ext.form.ComboBox({
                typeAhead: false,
                triggerAction: 'all',
                lazyRender:false,
                mode: 'local',
                store: new Ext.data.ArrayStore({
                    id: 0,
                    fields: this.fields,
                    data: this.data
                }),
                valueField: 'short',
                allowBlank: false,
                forceSelection: true,
                displayField: 'long'});

    this.comboBox.on("select", function() {
        for ( var i = 0; i < this.listeners.length; i++ )
            this.listeners[i].languageChanged( this.comboBox.getValue() );
    }, this)

    this.getComboBox = function() {
        return this.comboBox;
    }

    this.addListener = function( listener ) {
        this.listeners.push( listener );
    }
    
}