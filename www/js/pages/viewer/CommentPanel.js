Ext.define('Inubit.WebModeler.Comment', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'elementid', 
        'timestamp', 
        'user', 
        'validuntil', 
        'text', 
        {name: 'editable', type: 'boolean'}
    ]            
});

Ext.define('Inubit.WebModeler.CommentPanel', {
    extend: 'Ext.panel.Panel',
    imageRatio : 0.3,
    newCommentArea : null,
    processObject : null,
    store : null,
    loadMask : null,
    
    constructor : function( config ) {
        this.canvas = config.canvas;
        Inubit.WebModeler.CommentPanel.superclass.constructor.call( this, config );
    },
    
    initComponent : function() {
        Inubit.WebModeler.CommentPanel.superclass.initComponent.call( this );
        
        this.autoScroll = true;
        
        this.layout = 'anchor';
        this.defaults = {
            anchor: '100%'
        }
        
        this.add(
            this.createNewCommentArea()
        );
    },
    
    initStore: function() {
        this.proxy = new Ext.data.proxy.Rest({
                url: this.processObject.getCommentURI(),
                reader: {
                    type: 'json',
                    root: 'comments'
                },
                writer: {
                    type: 'json'
                }
            });
        
        this.store = new Ext.data.Store({
            model: 'Inubit.WebModeler.Comment',
            proxy: this.proxy,
            listeners: {
                load: function() {
                    this.addCommentItems();
                },
                scope: this
            }
        });
    },
    
    load : function( processObject ) {
        if ( processObject != null )
            this.processObject = processObject;
        
        if ( !this.isEditable() )
            this.getLayout().getLayoutItems()[0].destroy();
        
        this.initStore( );

        this.loadMask = new Ext.LoadMask(this.body, {
            msg: 'Loading comments',
            removeMask: true,
            store: this.store
        })
        this.loadMask.show();
        this.store.load();
    },
    
    loadOrReload : function( processObject ) {
        if ( this.processObject == null )
            this.load( processObject );
        else {
            var items = this.getLayout().getLayoutItems();
            var downTo = 0;
            while ( items.length > 1 ) 
                items.shift().destroy();
            this.store.removeAll( true );
            this.store.load();
        }
    },
    
    createNewCommentArea : function() {
        this.newCommentArea = new Ext.form.field.TextArea({
            style: {
                marginTop: '3px',
                marginLeft: '3px',
                marginRight: '3px'
            },
            enableKeyEvents: true,
            height: 100,
            autoWidth: true,
            autoScroll: true
        });

        this.newCommentArea.on("keypress", function( area, event ) {
            if (event.getKey() == Ext.EventObject.ENTER) {
                this.createComment();
                event.stopEvent();
            }
        }, this)
        
        this.newCommentArea.on("keyup", function( tA, event ) {
                if (event.getKey() == Ext.EventObject.DELETE) {
                    event.stopEvent();
                }
            })

        this.newCommentArea.on("render", function(cmp) {cmp.focus(true)} )
        
        var newCommentPanel = new Ext.panel.Panel({
            preventHeader: true,
            height: 150,
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            tbar: {
                items: [
                    new Ext.toolbar.TextItem({
                        text: 'New Comment'
                    })
                ]
            },
            bbar: {
                items: [
                    '->',
                    new Ext.panel.Tool({
                       baseCls: 'my-tools',
                       cls: 'x-tool x-tool-default x-box-item',
                       type: 'plus',
                       handler: function() {
                           this.createComment();
                       }, scope: this
                    })
                ]
            },            
            items: this.newCommentArea
        })
        
        return newCommentPanel;
    },
    
    addCommentItems : function() {
        var count = this.store.getTotalCount();
        
        for ( var i = count - 1; i >= 0; i-- ) {
            this.addCommentItem( this.store.getAt(i), 0 );
        }
    },
    
    addCommentItem : function ( comment , index ) {
        comment.setProxy(this.createModelProxy(comment));
        var commItem = new Inubit.WebModeler.CommentPanelItem({
            comment : comment,
            height: 170,
            access : this.processObject.model.getAccess(),
            resolved : parseInt(comment.get('validuntil')) == this.processObject.model.version,

            listeners: {
                "delete": function(comment){
                    comment.destroy({
                        success: function() {
                            this.store.remove(comment, true);
                            this.processObject.commentCount--;
                            this.updateCommentHighlight();
                            this.doLayout();
                        }, scope: this
                    });
                },

                resolve: function(comment) {
                    comment.set("validuntil", this.processObject.model.version);
                    comment.save();
                },

                change: function(comment, text) {
                    comment.set("text", text);
                    comment.save();
                },
                scope: this
            }
        });

        this.insert(index, commItem);
    },
    
    createComment : function() {
        var text = this.newCommentArea.getValue();

        if (this.processObject.model.isNewModel()) {
            Ext.Msg.show({
                title: 'Cannot save comments',
                msg: 'You cannot save any comments until the model has been saved ' +
                      'for the first time.\nPlease save your model first.',
                buttons: Ext.Msg.OK,
                icon: Ext.Msg.INFO
            })

            return;
        }

        var newComment = new Inubit.WebModeler.Comment({
            text: text
        });
        
        newComment.setProxy( this.proxy );
        newComment.save({
            success: function(record, operation) {
                this.store.insert(0, record);
                this.addCommentItem( record, this.items.length - 1);
                this.newCommentArea.setValue("");
                this.processObject.commentCount++;
                this.updateCommentHighlight();
                newComment.setProxy( this.createModelProxy(newComment) );
            }, scope: this
        });
    },
    
    createModelProxy : function( comment ) {
        var proxyUri = this.processObject.getCommentURI();
        var base = proxyUri.split("?")[0]
        var query = "?" + proxyUri.split("?")[1];
        
        return new Ext.data.proxy.Rest({	
            url: base + "/" + comment.getId() + query,
            reader: {
                type: 'json',
                root: 'comments'
            },
            writer: {
                type: 'json'
            }
        })
    },
    
    updateCommentHighlight : function() {
        this.processObject.dropCommentHighlight();
        this.processObject.drawCommentHighlight( this.canvas );
    },
    
    isEditable : function() {
        if ( this.processObject == null )
            return false;
        else 
            return (this.processObject.model.getAccess() == 'OWNER' ||
                this.processObject.model.getAccess() == 'WRITE' ||
                this.processObject.model.getAccess() == 'ADMIN' ||
                this.processObject.model.getAccess() == 'COMMENT')
    }
});