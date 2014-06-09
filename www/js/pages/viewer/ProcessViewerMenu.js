
function ProcessViewerMenu( portletMode ) {
    this.portletMode = portletMode;
    
    var exportItemNames = [
        {format:'PNG', id:'png'},
        {format:'PDF', id:'pdf'},
        {format:'XPDL', id:'xpdl'},
        {format:'ProcessModel', id:'pm'}
    ]

    this.init = function() {
        //create export menu
        var items = new Array();

        for (var i = 0; i < exportItemNames.length; i++) {
            var item = new Ext.menu.Item({
                text: 'Export as ' + exportItemNames[i].format,
                itemId: exportItemNames[i].id
            })

            item.on("click", function() {
                window.open(ProcessViewer.instance.getModel().uri + "." + this.getItemId());
            })

            items.push(item);
        }

        var exportMenu = new Ext.menu.Menu({
            items: items
        });

        var doneButton = new Ext.Button( {text: 'Done', icon: Util.getContext() + Util.ICON_LOGOUT});
        doneButton.on("click", function() {
            if ( this.portletMode )
                window.location = ( Util.getContext()[0] != "/"? "/" + Util.getContext() : Util.getContext() );
            else
                window.location = "../html/thx.html";
        }, this )

        return new Ext.Toolbar( {items: [
            {icon: Util.getContext() + Util.ICON_EXPORT, menu: exportMenu, text: 'Export'},
            {xtype: 'tbseparator'},
            doneButton,
            '->',
            {
                html:'<img height="15px" src="'+ Util.getContext() + Util.IMG_INUBIT + '" />'
            }
        ]});
    }

    return this.init();
}


