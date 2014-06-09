Ext.namespace("Inubit.WebModeler");

Inubit.WebModeler.ModelNodesLoader = Ext.extend(Inubit.WebModeler.Loader, {
	constructor : function (config) {
		this.chunkSize = 25;
		this.loaded = 0;
		this.toLoad = 0;
		this.currentChunk = 0;
		this.chunks = new Array();
		this.nodes = new Array();
		this.mode = config.mode;
		this.rootComponent = config.rootComponent;
		Inubit.WebModeler.ModelNodesLoader.superclass.constructor.call(this, config);
	},
	load : function(baseUri, canvas) {
		var handler = this;
		Ext.Ajax.request({
			method : 'GET',
			url : baseUri + "/nodes",
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
                            this.fireEvent("error");
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
            var newNode = null;

            var config = {
                uri: Util.getPath(chunk[i]),
                model: null,
                rootComponent: this.rootComponent,
                listeners: {
                    load: function() {
                        this.nodeLoaded(canvas);
                    },
                    scope: this
                }
            };

            if (this.mode == Inubit.WebModeler.ModelLoader.MODE_EDIT) {
            	newNode = new Inubit.WebModeler.EditableProcessNode(config);
            } else if (this.mode == Inubit.WebModeler.ModelLoader.MODE_VIEW) {
            	newNode = new Inubit.WebModeler.model.viewer.AnnotatedProcessNode(config);
            } else {
                newNode = new Inubit.WebModeler.model.viewer.ReferenceNode(config);
            }
            newNode.load(canvas, true);
            this.nodes.push(newNode);
        }
    },
    nodeLoaded : function(canvas) {
        this.loaded++;
        if (this.loaded == this.chunks[this.currentChunk].length) {
            this.currentChunk++;

            if (this.currentChunk == this.chunks.length) 
                this.fireEvent("load");

            this.loaded = 0;
            this.loadNextChunk(canvas);
        }
    },
    getNodes : function() {
        return this.nodes;
    }
});