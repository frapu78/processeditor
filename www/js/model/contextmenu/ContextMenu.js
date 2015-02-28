Ext.namespace('Inubit.WebModeler');

Inubit.WebModeler.ContextMenu = Ext.extend(Object, {
    constructor : function(config) {
        this.object = config.object;
        this.contextButtons = new Array();
        this.pluginButtons = new Array();
    },
    show : function() {
        if (this.contextButtons.length == 0) {
            var x = this.object.getFrameBounds().x;
            var y = this.object.getFrameBounds().y;
            this.createContextButtons( x,y );
            this.addPluginContextButtons(x, y);
        } else {
            this.updatePosition();

            for (var i = 0; i < this.contextButtons.length; i++) {
                if (!this.contextButtons[i].isVisible())
                    this.contextButtons[i].setVisible(true);
                this.contextButtons[i].resumeEvents();
                this.contextButtons[i].enable();

                this.contextButtons[i].el.fadeIn({duration: Util.ANIMATION_FADE_IN_TIME, concurrent: true});
            }
        }
    },
    hide : function(destroy) {
        if (destroy == null)
            destroy = false;

        for (var i = 0; i < this.contextButtons.length; i++) {
            var b = this.contextButtons[i];
            if (b.hasVisibleMenu && b.hasVisibleMenu())
                // b.menu.el.fadeOut({duration: Util.ANIMATION_FADE_OUT_TIME-0.1, remove: destroy, concurrent: true});
                b.suspendEvents(false);
            b.disable();
            if (b.isVisible()) {
                b.el.fadeOut({duration: Util.ANIMATION_FADE_OUT_TIME, remove: destroy, concurrent: true, scope: b, callback: function() { this.hide() }});
            }
        }
    },
    //"abstract method"
    createContextButtons : function() {
        //should be implemented by subclass
    },
    createDeleteButton : function(x, y) {
        var itemId = 'del';
        var delButton = new Ext.Button({
            itemId: itemId,
            icon: Util.getContext() + Util.ICON_DELETE,
            x: x + this.getXOffset(itemId),
            y: y + this.getYOffset(itemId)
        });

        //add button to editor
        this.addContextMenuButton(delButton)
        //bind deletion event
        var menu = this;
        delButton.el.dom.onmousedown = function() {
            menu.hide();
            menu.object.remove();
            //ProcessEditor.instance.getModel().deleteNode(processNode);
        };
        delButton.el.unselectable();
        this.contextButtons.push(delButton);
    },
    createRefactorButton : function(x, y) {
        var itemId = 'ref';

        //button for node refactoring (if one is needed)
        var nodeVariants = this.object.model.getType().variants[this.object.getType()];

        //create refactoring button if this node has variants
        if (nodeVariants && nodeVariants.length > 0) {
            var refMenu = this.createRefactoringMenu(nodeVariants);

            var refButton = new Ext.Button({
                itemId: itemId,
                icon: Util.getContext() + Util.ICON_REFACTORING,
                menu: refMenu,
                x: x + this.getXOffset(itemId),
                y: y + this.getYOffset(itemId)

            });
            this.addContextMenuButton(refButton);

            //Bind events
            refButton.el.dom.onmousedown = function(event) {
                if (refButton.hasVisibleMenu())
                    refButton.hideMenu();
                else
                    refButton.showMenu();

                Util.stopEvent(event);
            };

            refButton.el.dom.onmouseup = Util.stopEvent;
            //refButton.el.unselectable();
            this.contextButtons.push(refButton);
        }
    },
    createRefactoringMenu : function( nodeVariants ) {
        var refMenu = new Ext.menu.Menu();
        var menu = this;
        for (var i = 0; i < nodeVariants.length; i++) {
            var type = nodeVariants[i];

            //ignore own type
            if (type == this.object.getType()) continue;

            //get object label
            var text = type.substring(type.lastIndexOf(".") + 1);

            var icon = null;
            if (this.object.isProcessNode())
                icon = Util.getContext() + '/utils/dummy?name='+type+'&icon=true';

            var item = new Ext.menu.Item({text: text, itemId: type, icon: icon});
            refMenu.add(item);

            //on item click, change type of this node
            item.on("click", function() {menu.object.updateType(this.getItemId());});
        }

        return refMenu;
    },
    updateRefactoringButton : function( ) {
        for ( var i = 0; i < this.contextButtons.length; i++ )
            if ( this.contextButtons[i].getItemId() == 'ref' ) {
                var nodeVariants = this.object.model.getType().variants[this.object.getType()];
                this.contextButtons[i].menu = this.createRefactoringMenu(nodeVariants);
                break;
            }
    },
    applyRefactoring : function() {
        this.updateRefactoringButton();
        while ( this.pluginButtons.length > 0 ) {
            var newContext = new Array();
            var plugButton = this.pluginButtons.pop();
            for ( var i = 0; i < this.contextButtons.length; i++ )
                if ( this.contextButtons[i] != plugButton )
                    newContext.push( this.contextButtons[i] );
            plugButton.destroy();
            this.contextButtons = newContext;
        }
        var x = this.object.getFrameBounds().x;
        var y = this.object.getFrameBounds().y;
        this.addPluginContextButtons(x, y);
    },
    addPluginContextButtons : function( x, y ) {
        var plugins = Inubit.Plugins.PluginManager.instance.getObjectPlugins( this.object.getType() );
        this.pluginButtons = new Array();
        for ( var i = 0; i < plugins.length; i++ ) {
            var menuItem = plugins[i].getMenuItem();
            menuItem.x = x + this.getXOffset("", menuItem.iconOffset );
            menuItem.y = y + this.getYOffset("", menuItem.iconOffset );
            var button = new Ext.Button( menuItem );
            Inubit.WebModeler.ContextMenu.getExtCmp().add( button );
            Inubit.WebModeler.ContextMenu.getExtCmp().doLayout();

            plugins[i].bindActionToDomNode( button.el.dom );

            this.contextButtons.push( button );
            this.pluginButtons.push( button );
        }
    },
    addContextMenuButton : function(button) {
        Inubit.WebModeler.ContextMenu.getExtCmp().add(button);
        Inubit.WebModeler.ContextMenu.getExtCmp().doLayout();
        button.el.unselectable();
    },
    getXOffset : function(itemId, offsetInfo) {
        if ( offsetInfo == null )
            offsetInfo = Inubit.WebModeler.ContextMenu.CONTEXT_BUTTON_OFFSETS[itemId];
        if (offsetInfo) {
            var x = offsetInfo.x;
            if (offsetInfo.w == 'r')
                x += this.object.getFrameBounds().width;
            else if (offsetInfo.w == 'c')
                x += this.object.getFrameBounds().width / 2;

            return x;
        }

        return 0;
    },

    getYOffset : function(itemId, offsetInfo) {
        if ( offsetInfo == null )
            offsetInfo = Inubit.WebModeler.ContextMenu.CONTEXT_BUTTON_OFFSETS[itemId];
        if (offsetInfo) {
            var y = offsetInfo.y;
            if (offsetInfo.h == 'b')
                y += this.object.getFrameBounds().height;
            else if (offsetInfo.h == 'c')
                y += this.object.getFrameBounds().height / 2;

            return y;
        }

        return 0;
    }
});


//Ext component the buttons will be added to
Inubit.WebModeler.ContextMenu.extCmp = null;

Inubit.WebModeler.ContextMenu.getExtCmp = function() {
    if (Inubit.WebModeler.ContextMenu.extCmp == null)
        Inubit.WebModeler.ContextMenu.extCmp = Ext.getCmp('editor');

    return Inubit.WebModeler.ContextMenu.extCmp;
};

//All button offsets
Inubit.WebModeler.ContextMenu.CONTEXT_BUTTON_OFFSETS = {
    'edge':{x:12, w:'r', y:-4,h:'t'},
    'ref':{x:-4, w:'l', y:12,h:'b'},
    'del':{x:-11, w:'r', y:-27,h:'t'},
    'lane':{x:-4, w:'l', y:-27,h:'t'},
    'res':{x:5, w:'r', y:5, h:'b'},
    'lresh':{x:-15, w:'c', y:10, h:'b'},
    'lresv':{x:10, w:'r', y:-10, h:'c'},
    'anno': {x:-27, w:'l', y:-4,h:'t'}
};
