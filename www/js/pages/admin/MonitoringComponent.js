function MonitoringComponent() {
    this.prototype = new AdminPageComponent();
    this.component = null;

    this.getButton = this.prototype.getButton;
    this.getComponent = this.prototype.getComponent;

    this.imageSize = 406;

    this.init = function(width, height) {
        var noColumns = Math.floor(width / this.imageSize);
        
        if (noColumns < 1)
            noColumns = 1

        this.component = new Ext.Panel({
            title: 'Response Times',
            layout: 'table',
            autoWidth: true,
            height: height,
            layoutConfig: {
                columns: noColumns
            },
            items: this.getItems()
        })

        return this.component;
    }

    this.createButton = function(id) {
        return new Ext.Button({
            itemId: id,
            text: 'Monitoring',
            enableToggle: true,
            icon: Util.getContext() + Util.ICON_CHART
        })
    }

    this.getItems = function() {
        var items = new Array();

        items = [
            new Ext.Img({src: "/admin/data/response_times/0"}),
            new Ext.Img({src: "/admin/data/response_times/1"}),
            new Ext.Img({ src: "/admin/data/response_times/2"})
        ];

        return items;
    }
}