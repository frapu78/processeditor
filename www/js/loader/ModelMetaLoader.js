Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.ModelMetaLoader = Ext.extend(Inubit.WebModeler.Loader, {

	constructor : function (config) {
		this.modelWidth = 0;
		this.modelHeight = 0;
		this.modelType = null;
		this.folder = '/';
		this.access = 'NONE';
                Inubit.WebModeler.ModelMetaLoader.superclass.constructor.call(this, config);
	},
	load : function(modelUri, basePart) {
		var handler = this;
		Ext.Ajax.request({
			method : 'GET',
			url : modelUri + "/meta",
			disableCaching : false,
			success : function(response) {
                            if (response.status == 200) {
                                var xml = response.responseXML;
                                var properties = Util.parseProperties(xml);

                                handler.modelHeight = properties.height;
                                handler.modelWidth = properties.width;
                                handler.modelType = new ProcessModelType(properties.type, basePart);
                                handler.folder = properties.folder;
                                handler.access = properties.access;

                                handler.fireEvent("load");
                            } else if (response.status == 403) {
                                    // forbidden..... do something
                            } else {
                                    // do something on error
                            }
			},
			failure : function(){
				handler.fireEvent("load");
				// do something on error
			}
		});
    }

});