/*
 * Process Editor
 *
 * (C) 2011 inubit AG
 *
 * http://inubit.com
 *
 */
Ext.define('Inubit.WebModeler.CommentPanelItem',{
    extend: 'Ext.panel.Panel',
    imageRatio: 0.3,
    
    constructor: function( config ) {
        this.comment = config.comment;
        this.access = config.access;
        this.resolved = config.resolved;
        this.addEvents('resolve', 'delete', 'change');
        Inubit.WebModeler.CommentPanelItem.superclass.constructor.call( this, config );
    },
    
    initComponent: function() {
        this.preventHeader = true;
        this.tbar = new Ext.toolbar.Toolbar({
            items: [
                new Ext.toolbar.TextItem({
                    text: this.comment.get('timestamp') + ", " + this.comment.get('user')
                })
            ]
        });
        
        var bbar = this.createBottomBar();
        if ( bbar ) 
            this.bbar = bbar;
        
        this.image = new Ext.Img({
            border: false,
            src : Util.getContext() + '/users/users/' + this.comment.get('user') + "/img?width=50&height=80",
            style: {
                marginLeft: '3px',
                marginTop: '3px'
            }
        });
        
        this.commentArea = this.createCommentArea();
        
        this.layout = 'border';
        
        this.items = [
            {
                region: 'center',
                border: false,
                items: [
                    this.commentArea
                ]
            },
            {
                region: 'west',
                border: false,
                items: [
                    this.image
                ]
            }
            
        ]
        
        Inubit.WebModeler.CommentPanelItem.superclass.initComponent.call( this );
        
        this.on("afterrender", function() {
            this.adjustChildrenWidth();
        }, this);
    },
    
    createBottomBar : function() {
        var bbarItems = ['->'];
        if (    (this.access == 'OWNER' ||
                this.access == 'WRITE' ||
                this.access == 'ADMIN') && !this.resolved ) {

            bbarItems.push(new Ext.panel.Tool({
                   baseCls: 'my-tools',
                   cls: 'x-tool x-tool-default x-box-item',
                   type: 'check',
                   handler: function(event, toolEl, panel, tool) {
                       this.resolved = true;
                       var width = this.commentArea.getWidth();
                       this.commentArea.destroy();
                       this.commentArea = this.createCommentArea();
                       this.commentArea.setWidth(width);
                       this.getLayout().getLayoutItems()[0].add(this.commentArea);
                       tool.destroy();
                       this.fireEvent("resolve", this.comment);
                   }, scope: this
                })
            );
        }

        if (this.comment.get('editable')) {
            bbarItems.push( new Ext.panel.Tool({
                   baseCls: 'my-tools',
                   cls: 'x-tool x-tool-default x-box-item',
                   type: 'delete',
                   handler: function() {
                       this.fireEvent('delete', this.comment);
                       this.destroy();
                   }, scope: this
                }) 
            );
        }
        
        if ( bbarItems.length > 1 )
            return new Ext.toolbar.Toolbar({
                items: bbarItems
            });
        else
            return null;
    },
    
    createCommentArea : function() {
        var config = {
            itemId: this.comment.id,
            border: false,
            style: {
                fontSize: '11px',
                fontFamily: 'tahoma,arial,helvetica,sans-serif',
                marginLeft: '3px',
                marginRight: '3px',
                marginTop: '3px'
            },
            height: 120,
            autoScroll: true,
            value: Util.unEscapeString(this.comment.get('text'))
        }
        
        if ( this.resolved ) 
            config.style.backgroundColor = Util.COLOR_LIGHTGREEN;
        
        if (this.comment.get('editable') && !this.resolved) {
            config.enableKeyEvents = true;
            var commentArea = new Ext.form.TextArea(config);

            commentArea.on("keypress", function( textArea, event) {
                if (event.getKey() == Ext.EventObject.ENTER) {
                    this.fireEvent("change", this.comment, commentArea.getValue() );
                    event.stopEvent();
                } 
            }, this )
            
            commentArea.on("keyup", function( tA, event ) {
                if (event.getKey() == Ext.EventObject.DELETE) {
                    event.stopEvent();
                }
            })
            
            return commentArea;
        } else {
            return new Ext.form.DisplayField(config);
        }
    },
    
    adjustChildrenWidth : function() {
        var width = this.getWidth();
        var imgWidth = parseInt(this.imageRatio * width);
        var textWidth = width - imgWidth - 12;
        
        this.commentArea.setWidth(textWidth);
        this.image.setWidth(imgWidth);
    }
})