function ProcessEditorMenu( portletMode ) {
    this.portletMode = portletMode;

    var saveID = 'save_button';
    var exportID = 'export_button';
    var cutID = 'cut_button';
    var copyID = 'copy_button';
    var pasteID = 'paste_button';
    var exportItemNames = [
        {format:'PNG', id:'png', icon: Util.getContext() + Util.ICON_PNG, disabled: false},
        {format:'PDF', id:'pdf', icon: Util.getContext() + Util.ICON_PDF, disabled: false},
        {format:'XPDL', id:'xpdl', icon: Util.getContext() + Util.ICON_EXPORT, disabled: false},
        {format:'IS Diagram', id:'zip', icon: Util.getContext() + Util.ICON_CONNECT, disabled: false},
        {format:'ProcessModel', id:'pm', icon: Util.getContext() + Util.ICON_EXPORT, disabled: false}
    ]

    this.toolbar = null;
    this.languageField = null;
    this.createToolbar = function() {
        var items = new Array();
        
        
        for (var i = 0; i < exportItemNames.length; i++) {
            var item = new Ext.menu.Item({
                text: 'Export as ' + exportItemNames[i].format,
                icon: exportItemNames[i].icon,
                itemId: exportItemNames[i].id,
                disabled: exportItemNames[i].disabled
                
            })

            item.on("click", function() {
                window.open(ProcessEditor.instance.getModel().uri + "." + this.getItemId());
            })

            items.push(item);
        }

        var exportMenu = new Ext.menu.Menu({
            items: items
        });

        var cutButton = new Ext.Button({id: cutID, icon: Util.getContext() + Util.ICON_CUT});
        cutButton.on("click", function() {
            ClipboardHandler.getInstance().cut(ProcessEditor.instance.selectionHandler);
        })

        var copyButton = new Ext.Button({id: copyID, icon: Util.getContext() + Util.ICON_COPY});
        copyButton.on("click", function() {
            ClipboardHandler.getInstance().copy(ProcessEditor.instance.selectionHandler);
        })

        var pasteButton = new Ext.Button({id: pasteID, icon: Util.getContext() + Util.ICON_PASTE});
        pasteButton.on("click", function(event) {
            ClipboardHandler.getInstance().paste();
            //Util.stopEvent(event);
        })

        var undoButton = ProcessEditor.instance.undoHandler.getUndoButton();

        var redoButton = ProcessEditor.instance.undoHandler.getRedoButton();

//        if (this.portletMode) {
//        	var toolbarItems = [
//                {id: saveID, icon: Util.getContext() + Util.ICON_SAVE, text: 'Save'},
//             	{xtype: 'tbseparator'},
//             	{id: exportID, icon: Util.getContext() + Util.ICON_EXPORT, menu: exportMenu, text: 'Export'},
//             	{xtype: 'tbseparator'},
//             	cutButton,
//             	copyButton,
//             	pasteButton,
//             	{xtype: 'tbseparator'},
//             	undoButton,
//             	redoButton,
//             	{xtype: 'tbseparator'},
//             	//toggleButton
//             	//{xtype: 'tbseparator'},
//             ]
//        } else {
        	var toolbarItems = [
                {id: saveID, icon: Util.getContext() + Util.ICON_SAVE, text: 'Save'},
              	{xtype: 'tbseparator'},
              	{id: exportID, icon: Util.getContext() + Util.ICON_EXPORT, menu: exportMenu, text: 'Export'},
               	{xtype: 'tbseparator'},
               	cutButton,
               	copyButton,
               	pasteButton,
               	{xtype: 'tbseparator'},
              	undoButton,
              	redoButton,
              	{xtype: 'tbseparator'},
              	{icon: Util.getContext() + Util.ICON_DISCUSS,
               	handler: function() {
              	 	this.openInvitationDialog();
               	}, scope: this, id: 'discuss_button', tooltip: 'Invite'},
                {xtype: 'tbseparator'},
               	//toggleButton
              	//{xtype: 'tbseparator'},
             ]
//        }

        this.toolbar = new Ext.Toolbar({
            items: toolbarItems
        })
        
        this.setEvents();
    }

    this.setSaveEvent = function() {
        var saveButton = Ext.getCmp(saveID);
        var editor = ProcessEditor.instance;
        saveButton.on("click", function() {
            var saveDialog = new Inubit.WebModeler.pages.editor.ProcessEditorSaveDialog({model: editor.model});
            saveDialog.show();
        });
    }

    this.openInvitationDialog = function() {
        new InvitationDialog(ProcessEditor.instance.getModel()).show(Ext.getCmp('discuss_button'));
    }

    this.setEvents = function() {
        this.setSaveEvent();
    }

    this.getToolbar = function() {
        return this.toolbar;
    }

    this.addPluginsAndFinishing = function() {
        var toggleButton = new Ext.Button({enableToggle: true, text: 'Comments', icon: Util.getContext() + Util.ICON_COMMENTS, pressed: true});
        toggleButton.on("toggle", function(button, state) {
            ProcessEditor.instance.allowComments = state;

            if (state) {
                //allow comment button + comment icon
                ProcessEditor.instance.getModel().enableComments(ProcessEditor.instance.getCanvas());
            } else {
                ProcessEditor.instance.getModel().disableComments();
                //Drop all comment icons
            }
        }, this);

        var toolbarPlugins = ProcessEditor.instance.pluginManager.getToolbarPlugins();

        for (var i = 0; i < toolbarPlugins.length; i++) {
        	if (toolbarPlugins[i]) {
        		this.toolbar.add(toolbarPlugins[i]);
        		this.toolbar.add({xtype: 'tbseparator'});
        	}
        }

//      tg: Plugin menu disabled.
        this.toolbar.add(
             ProcessEditor.instance.pluginManager.getMenuButton(),
             {xtype: 'tbseparator'},
             toggleButton
        )

        if ( this.portletMode ) {
            this.toolbar.add(
                /* tg: Disabled for SC version
                 *  {xtype: 'tbseparator'}, */
                {icon: Util.getContext() + Util.ICON_HOME, text: 'Models',
                 handler : function() {
                     var newLoc = Util.getContext();
                     if ( !(newLoc[0] == "/") )
                         newLoc = "/" + newLoc;
                     window.location = newLoc;
                 }}
            );
        }

        this.languageField = new LanguageField();
        var langExtCmp = this.languageField.getComboBox();

        /*
        this.toolbar.add (
            '->',
            // tg removed for sc version
            //langExtCmp,
            {xtype:'tbspacer', width: 5},
            {
				html:'<img height="15px" src="'+ Util.getContext() + Util.IMG_INUBIT + '" />',
				src: Util.getContext() + Util.IMG_INUBIT,
				handler: function() {
				   	// Util.displayVersionInfo()
				}
			}
        )
        */

        langExtCmp.setValue('en');

        this.toolbar.doLayout();
    }

    this.createToolbar();
}