Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.Loader = Ext.extend(Ext.util.Observable, {
	errorOccurred : false,

        constructor: function( config ) {
            this.addEvents("load", "error");
            this.listeners = config.listeners;
            Inubit.WebModeler.Loader.superclass.constructor.call(this, config);
        },

	hasErrors : function() {
		return this.errorOccurred;
	},
	setErrorOccurred : function (errorOccurred) {
		this.errorOccurred = errorOccurred;
	}
});

