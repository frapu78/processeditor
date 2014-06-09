Ext.namespace("Inubit.WebModeler");

/**
 * Loader responsible for loading models from the server.
 *
 * @class Inubit.WebModeler.ModelLoader
 * @extends Inubit.WebModeler.Loader
 */
Inubit.WebModeler.ModelLoader = Ext.extend(Inubit.WebModeler.Loader, {
    /**
     * @param {id , version , type, mode, rootComponent} config
     */
    constructor : function(config) {
        this.tmpUri = null;
        this.loadState = 0;
        this.metaLoader = null;
        this.nodeLoader = null;
        this.edgesLoader = null;
        this.editable = false;
        this.modelId = config.id;
        this.version = config.version;
        this.type = config.type;
        this.mode = config.mode;
        this.customContext = config.customContext;
        if (this.mode == Inubit.WebModeler.ModelLoader.MODE_EDIT)
            this.editable = true;
        this.rootComponent = config.rootComponent;
        this.mask  = new Ext.LoadMask(Ext.getBody(), {msg: 'Please wait while loading model.', removeMask: true}),

        Inubit.WebModeler.ModelLoader.superclass.constructor.call(this, config);

        this.nodeLoader = new Inubit.WebModeler.ModelNodesLoader({
            mode : this.mode,
            rootComponent : this.rootComponent,
            listeners: {
                load: this.partLoaded,
                error: this.errorWhileLoadingComponents,
                scope: this
            }
        });
    	this.edgeLoader = new Inubit.WebModeler.ModelEdgesLoader({
            editable: this.editable,
            listeners: {
                load: this.partLoaded,
                error: this.errorWhileLoadingComponents,
                scope: this
            }
        });
    	this.metaLoader = new Inubit.WebModeler.ModelMetaLoader({
            listeners: {
                load: this.partLoaded,
                error: this.errorWhileLoadingComponents,
                scope: this
            }
        });
	},
    load : function(canvas) {
        this.setErrorOccurred(false);
        this.mask.show();

        var basePart;
        if (this.editable == true && this.modelId.indexOf("_") < 0) {
            var req = this.getModelRequest();
            req.send(null);

            //get uri of created model
            var xml = req.responseXML;
            this.tmpUri = xml.getElementsByTagName("uri")[0].getAttribute("value");
            this.tmpUri = Util.getPath(this.tmpUri);

            var basePart = this.tmpUri.substring( 0, this.tmpUri.indexOf("/models") );
        } else {
            if (this.modelId.indexOf("_") >= 0)
                this.tmpUri = Util.getContext( this.customContext ) + "/models/tmp/" + this.modelId;
            else if (this.version == null || this.version < 0)
                this.tmpUri = Util.getContext( this.customContext ) + "/models/" + this.modelId;
            else
                this.tmpUri = Util.getContext( this.customContext ) + "/models/" + this.modelId + "/versions/" + this.version;

            basePart = Util.getContext( this.customContext );
        }

        this.metaLoader.load(this.tmpUri, basePart);
        this.nodeLoader.load(this.tmpUri, canvas);
        this.edgeLoader.load(this.tmpUri, canvas);
    },
    partLoaded : function() {
        this.loadState++;
        if (this.loadState == Inubit.WebModeler.ModelLoader.FINISH_STATE) {
        	// if there are errors - > notify
        	if (this.hasErrors()) {
        		this.errorOnLoading();
        		return;
        	}
            if (this.modelId == 'new' || this.modelId.indexOf("_") > -1 )
                this.modelId = null;
            
            if (this.editable)
                this.model = new Inubit.WebModeler.EditableProcessModel({
                								uri : this.tmpUri,
                                                baseId : this.modelId,
                                                tmpId: this.getTmpId(),
                                                type : this.getModelType(),
                                                version : this.getModelVersion(),
                                                nodes : this.nodeLoader.getNodes(),
                                                edges: this.edgeLoader.getEdges() } );
            else
                this.model = new Inubit.WebModeler.model.viewer.AnnotatedProcessModel({
                								uri : this.tmpUri,
                                                baseId : this.modelId,
                                                tmpId : this.getTmpId(),
                                                type : this.getModelType(),
                                                version : this.getModelVersion(),
                                                nodes : this.nodeLoader.getNodes(),
                                                edges : this.edgeLoader.getEdges()
                                                });

            if ( this.modelId == null )
                this.model.properties["name"] = "New Model";

            this.model.setFolder(this.metaLoader.folder);
            this.model.setAccess(this.metaLoader.access);
            this.fireEvent("load");
        }
    },
    onNodeLoaded : function(funct, scope) {
        if (funct == null) return;

        this.nodeLoaded = function() {
            funct.call(scope);
        };
    },
    /**
     * Is called if an error occurred while loading the models components.
     */
    errorWhileLoadingComponents : function() {
    	this.setErrorOccurred(true);
    	this.loadState++;
    	// ensure that the error on loading method is only called once.
        if (this.loadState == Inubit.WebModeler.ModelLoader.FINISH_STATE) 
                this.fireEvent("error");
    },
    nodeLoaded : function() {
        return false;
    },
    hideLoaderMask : function() {
    	this.mask.hide();
    },
    getModelRequest : function() {
        var urlAddition = "";
        if (this.version)
            urlAddition = "/versions/" + this.version;

        var putReq = new XMLHttpRequest();

        putReq.open("POST", Util.getContext( this.customContext ) + "/models/tmp", false);
        if (this.modelId == 'new') {
            putReq.setRequestHeader("Model-Type",this.type);
        } else {
            putReq.setRequestHeader("Model-Source", "/models/" + this.modelId + urlAddition);
        }

        return putReq;
    },
    getModelVersion : function() {
        if (this.version == null && this.modelId) {
            var verReq = new XMLHttpRequest();
            verReq.open("GET", this.modelId +  "/versions", false);
            verReq.send(null);

            this.version = verReq.responseXML.getElementsByTagName("version").length - 1;
        }

        return this.version;
    },
    getTmpId : function() {
        return this.tmpUri.substring(this.tmpUri.lastIndexOf("/") + 1);
    },
    getModelWidth : function() {
        return this.metaLoader.modelWidth;
    },
    getModelHeight : function() {
        return this.metaLoader.modelHeight;
    },
    getModelType : function() {
        return this.metaLoader.modelType;
    },
    getModel : function() {
        return this.model;
    }
});


Inubit.WebModeler.ModelLoader.FINISH_STATE = 3;
Inubit.WebModeler.ModelLoader.MODE_EDIT   = 0;
Inubit.WebModeler.ModelLoader.MODE_VIEW   = 1;
Inubit.WebModeler.ModelLoader.MODE_CHOOSE = 2;