function AdminPage(components) {
    this.page = null;
    this.components = components;

    this.render = function(cmp) {
        var size = Util.getComponentSize(cmp);

        this.createBasicLayout(size.width, size.height, cmp)

        Ext.EventManager.onWindowResize(this.resizeWindow, this);
    }

    this.createBasicLayout = function(width, height, cmp) {
        this.page = new Ext.panel.Panel({
            title: 'ProcessEditorServer - Adminstration Platform',
            renderTo: cmp,
            width: width,
            height: height,
            layout: 'anchor',
            items: [
                {
                     id: 'center_region',
                     anchor: '100%, 100%',
                     collapsible: false,
                     split: true,
                     margins: '5 0 5 0',
                     style: 'background-color:"white"',
                     layout: 'fit',
                     items: new Ext.panel.Panel({
                        html: "<div align='center' style='padding-top:50px'><img src='pics/icon_256x256_inubit-Workbench.gif'/></div>"
                     })
                }
            ],
            tbar: new AdminPageMenu(this, this.components).getToolbar()
        });
    }

    this.resizeWindow = function(newWidth, newHeight) {
        this.pageWidth = newWidth;
        this.pageHeight = newHeight;

        this.page.setSize(newWidth, newHeight);

        this.page.getLayout().getLayoutItems()[0].setSize(newWidth, newHeight);

        this.page.doLayout();
    }

    this.getContentArea = function() {
        return this.page.getLayout().getLayoutItems()[0];
    }
}


