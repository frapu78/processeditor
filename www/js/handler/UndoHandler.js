function UndoHandler( size ) {
    this.size = size ? size : 200;

    this.undoStack = new Array();
    this.redoStack = new Array();

    this.currentUndo = false;
    this.currentRedo = false;

    this.undoButton = new Ext.Button( {
        id: 'undo_action',
        tooltip: 'undo the last action',
        icon: Util.getContext() + Util.ICON_UNDO,
        disabled: true,
        handler: function() {
            this.performUndo();
        },
        scope: this
    });

    this.redoButton = new Ext.Button({
        id: 'redo_action',
        tooltip: 'redo the last action',
        icon: Util.getContext() + Util.ICON_REDO,
        disabled: true,
        handler: function() {
            this.performRedo();
        },
        scope: this
    });

    this.addUndoAction = function( scope, f, params ) {
        this.undoStack.unshift( {scope: scope, f: f, params : params} );
        this.undoButton.enable();

        if ( this.undoStack.length > this.size )
            this.undoStack.pop();

        if ( !this.isUndoInProgress() && this.redoStack.length > 0 ) {
            this.redoButton.disable();
            this.redoStack = new Array();
        }
    }

    this.addRedoAction = function( scope, f, params ) {
        this.redoStack.unshift( {scope: scope, f: f, params : params} );
        this.redoButton.enable();
    }

    this.addAction = function( scope, f, params ) {
        if ( this.isUndoInProgress() )
            this.addRedoAction(scope, f, params);
        else
            this.addUndoAction(scope, f, params);
    }

    this.isUndoPossible = function() {
        return this.undoStack.length > 0;
    }

    this.isUndoInProgress = function() {
        return this.currentUndo;
    }

    this.isRedoPossible = function() {
        return this.redoStack.length > 0;
    }

    this.isRedoInProgress = function() {
        return this.currentRedo;
    }

    this.performUndo = function() {
        this.currentUndo = true;
        var action = this.undoStack.shift();
        this.performAction(action);

        if ( this.undoStack.length <= 0 )
            this.undoButton.disable();
        this.currentUndo = false;
    }

    this.performRedo = function() {
        this.currentRedo = true;
        var action = this.redoStack.shift();
        this.performAction(action);

        if ( this.redoStack.length <= 0 )
            this.redoButton.disable();
        this.currentRedo = false;
    }

    this.performAction = function( action ) {
        action.f.apply( action.scope , action.params );
    }

    this.getUndoButton = function () {
        return this.undoButton;
    }

    this.getRedoButton = function() {
        return this.redoButton;
    }
}