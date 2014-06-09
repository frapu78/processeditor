Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.ModelEdgesLoader = Ext.extend(Inubit.WebModeler.Loader, {
	constructor : function (config) {
		this.chunkSize = 25;
		this.loaded = 0;
		this.toLoad = 0;
		this.currentChunk = 0;
		this.chunks = new Array();
		this.edges = new Array();
		this.editable = config.editable;
		Inubit.WebModeler.ModelEdgesLoader.superclass.constructor.call(this, config);
	},
	load : function(baseUri, canvas) {
		var handler = this;
		Ext.Ajax.request({
			method : 'GET',
			url : baseUri + "/edges",
			disableCaching : false,
			success : function(response) {
                            if (response.status == 200) {
                                var xmlDoc = response.responseXML;
                                var uris = xmlDoc.getElementsByTagName("uri");

                                handler.toLoad = Math.ceil(uris.length / handler.chunkSize);

                                if (handler.toLoad == 0)
                                    handler.fireEvent("load");

                                for (var i = 0; i < handler.toLoad; i++) {
                                        handler.chunks[i] = new Array();
                                }

                                for (i = 0; i < uris.length; i++) {
                                    var ci = Math.floor(i / handler.chunkSize);
                                    handler.chunks[ci].push(uris[i].firstChild.nodeValue);
                                }
                                handler.loadNextChunk(canvas);
                            } else if (response.status == 403) {
                                    // forbidden..... do something
                            } else {
                                    // do something on error
                            }
			},
			failure : function(){
				handler.fireEvent("error");
				// do something on error
			}
		});
    },
    loadNextChunk : function( canvas ) {
        var chunk = this.chunks[this.currentChunk];
        if(!Ext.isDefined(chunk)) {
        	return;
        }
        for (var i = 0; i < chunk.length; i++) {
            var newEdge = null;

            var config = {
                uri: Util.getPath(chunk[i]),
                model : null,
                listeners: {
                    load: function() {
                        this.edgeLoaded(canvas);
                    },
                    scope: this
                }
            }

            if (this.editable)
                newEdge = new Inubit.WebModeler.EditableProcessEdge(config);
            else
                newEdge = new Inubit.WebModeler.model.viewer.AnnotatedProcessEdge(config);

            newEdge.load(canvas, true);
            this.edges.push(newEdge);
        }
    },
    edgeLoaded : function(canvas) {
        this.loaded++;
        if (this.loaded == this.chunks[this.currentChunk].length) {
            this.currentChunk++;

            if (this.currentChunk == this.chunks.length) 
                this.fireEvent("load");

            this.loaded = 0;
            this.loadNextChunk(canvas);
        }
    },
    getEdges : function() {
        return this.edges;
    }
});