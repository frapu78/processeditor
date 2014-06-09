function AdminPageMenu(adminPage, components) {
    this.toolbar = null;
    this.adminPage = adminPage,
    this.lastButton = null;
    this.components = components;
    
    this.init = function() {
        var cButtons = new Array();
        for (var i = 0; i < components.length; i++) {
            var button = components[i].getButton(i);

            var adminMenu = this;
            button.on("click", function() {
                if (adminMenu.lastButton && adminMenu.lastButton != this)
                    adminMenu.lastButton.toggle(false);
                
                adminMenu.lastButton = this;
                this.toggle(true);
                var area = adminMenu.adminPage.getContentArea()

                area.removeAll(true);
                area.add(adminMenu.components[this.getItemId()].getComponent(area));
                area.doLayout();
                area.body.repaint();
            });
            
            cButtons.push(button);
            cButtons.push( {xtype: 'tbseparator'} )
        }

        var items = cButtons.concat(this.defaultItems);

        this.toolbar = new Ext.Toolbar({
            items: items
        });
    }

    this.createLogoutButton = function() {
        var button = new Ext.Button({id: 'logout', text: 'Logout', icon: Util.getContext() + Util.ICON_LOGOUT});

        button.on("click", function() {
            Util.logoutCurrentUser();
        });

        return button;
    }

    this.getToolbar = function() {
        return this.toolbar;
    }

    this.defaultItems = [
        new Ext.Button({
            text: 'Models',
            icon: Util.getContext() + Util.ICON_MODEL,
            handler: function() {window.location = "."}
        }),
        {xtype: 'tbseparator'},
        this.createLogoutButton(),
        '->',
        new Ext.toolbar.Item({autoEl: {tag: 'img', src: Util.getContext() + Util.IMG_INUBIT, height: 15}})
    ];

    this.init();
}