/*
 * Process Editor
 *
 * (C) 2009 - 2011 inubit AG
 *
 * http://inubit.com
 *
 */

Ext.define( 'Inubit.WebModeler.AnnotationBalloon', {
    extend: 'Ext.tip.ToolTip',
    require: [
        'Inubit.WebModeler.ProcessNode'
    ],
    height: 400,
    width: 240,
    layout: 'anchor',
    autoScroll: true,
    closable: true,
    draggable: true,
    anchor: 'left',
    hidden: true,
    autoHide: false,
    showDelay: 0,
    footer: true,
    hideDelay: 0,
    
    constructor: function( config ) {
        this.processObject = config.processObject;
        this.canvas = config.canvas;
        Inubit.WebModeler.AnnotationBalloon.superclass.constructor.call( this, config );
    },
    
    initComponent: function() {
        this.title = 'Enter your comment';
        if ( ! this.processObject.model.canBeCommented()) {
            this.title = 'Comments: ';
            this.height = 150;
        }
      
        this.commentsPanel = new Inubit.WebModeler.CommentPanel({
            anchor: "100%, 100%",
            buttonAlign: 'right',
            autoScroll: true,
            border: false,
            canvas : this.canvas
        });
        
        this.items = [ 
            this.commentsPanel
        ];
        
        if (this.processObject.isBPMNLane() && this.target == null) 
            this.target = this.processObject.getLaneHandler().getSurroundingPool().graphics.node;
        
        Inubit.WebModeler.AnnotationBalloon.superclass.initComponent.call( this );
        
        this.on("hide", function() {
            this.lastTarget = this.target;
            this.setTarget(null);
        }, this);
    },
    
    show : function() {
        if ( this.target == null )
            this.setTarget(this.lastTarget);
        Inubit.WebModeler.AnnotationBalloon.superclass.show.call( this );
        this.commentsPanel.loadOrReload( this.processObject );
    }
} );