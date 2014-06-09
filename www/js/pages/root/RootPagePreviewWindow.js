Ext.namespace("Ext.ux");

Ext.ux.RootPagePreviewWindow = function ( config ) {
	// ATTN getVersionData which donwloads the version data (comments etc) must be executed before 
	// calling createPropertyStore -> getModelProperties because this method uses the downloaded and 
	// stored information. TODO: CHECK DESIGN!
	config.bbar = this.createVersionToolbar( this.createComboBox( this.getVersionData(config.modelURI)  ), config.access);
	
    config.items = [
        {
            region: "center",		
            width: config.width * 0.6,
            layout: "border",
            items: [{
                region : 'center',
                title: 'Preview',
                layout: {
                    type: 'vbox',
                    align: 'center'
                },
                border: false,
                items: [
                    Ext.create('Ext.Img', {
                        src: config.imgsrc + "?size="+ config.imageSize + "&" + new Date(),
                        width: config.imageSize,
                        style: {
                            paddingTop: '5px'
                        }
                    })
                ]
            },
            {
                region: 'south',
                height : 200,
                border: false,
                autoScroll: true,
                items : new Ext.grid.GridPanel({
                      id: 'metainformation',
                      border: false,
                      autoWidth: true,
                      autoHeight: true,
                      store: this.createPropertyStore( config.modelURI ),
                      title: 'Meta-Information',
                      columns: [{header: 'name', width: 100, dataIndex: 'key'}, {id: 'value' , header: 'value', dataIndex: 'value', flex: 1}],
                      stripeRows: true,
                      viewConfig : {
                            forceFit: true,
                            scrollOffset: 2 // the grid will never have scrollbars
                      }
                  })
            }]
        },
        {
            region: "east",
            width: config.width * 0.45,
            items: new RootPageRightsPanel( config.modelURI, config.access, config.width * 0.45, config.height - 30)
        }
    ];
   
    config.maskEl.mask();

    Ext.ux.RootPagePreviewWindow.superclass.constructor.call( this, config );

    this.on("close", function() {
        this.maskEl.unmask();
    }, this)
}

Ext.extend( Ext.ux.RootPagePreviewWindow, Ext.Window, {
    store : new Ext.data.ArrayStore({
            fields: [
               {name: 'key'},
               {name: 'value'}
            ]
    }),

    title : "Select model version...",
    comments : new Array(),
    users : new Array(),
    layout: "border",

    getVersionData : function( uri ) {
        //one entry for the HEAD-revision
       var data = [[-1, "HEAD"]];

       var req = new XMLHttpRequest();
       req.open("GET", uri + "/versions", false);
       req.send(null);

       var count = req.responseXML.getElementsByTagName("version").length;

       //add entries for all other versions
       for (var i = count - 2; i >= 0; i--) {
           data.push([i, ""+i]);
       }

       var versions = req.responseXML.getElementsByTagName("version");
       for (var i = 0; i < count; i++) {
           var index = parseInt(versions[i].getAttribute("id"));
           var commentEl = versions[i].getElementsByTagName("comment")[0];

           if (commentEl) {
               var comment = commentEl.textContent;
               this.comments[index] = comment;
           }

           var userEl = versions[i].getElementsByTagName("user")[0];
           if (userEl) {
               var user = userEl.textContent;
               this.users[index] = user;
           }
       }

       return data;
    },

    createComboBox : function(data) {
        var combo =  new Ext.form.field.ComboBox({
           allowBlank: false,
           editable: false,
           forceSelection: true,
           disableKeyFilter: true,
           id: 'vCombo',
           queryMode: 'local',
           store: new Ext.data.ArrayStore({
                id: 0,
                fields: [
                    'version',  // numeric value is the key
                    'displayText'
                ],
                data: data  // data is local
            }),
            valueField: 'version',
            displayField: 'displayText',
            triggerAction: 'all',

           width:80
       });

       combo.setValue(-1);

       combo.on("select", function() {
               var imgUrl;
               var dataUrl;
               var version;

               if (combo.getValue() == "HEAD" || combo.getValue() == -1) {
                   imgUrl = this.imgsrc + "?size=" + this.imageSize + "&" + new Date();
                   dataUrl = this.modelURI;
                   version = -1;
               }
               else {
                   imgUrl = this.imgsrc.replace(/preview/, "versions/" + combo.getValue() + "/preview?size=" + this.imageSize);
                   dataUrl = this.modelURI + "/versions/" + combo.getValue();
                   version = parseInt(combo.getValue());
               }

               this.updatePreview(imgUrl);
               this.updateMetaInformation(dataUrl, version);
           }, this)
           
       return combo;
    },

    createPropertyStore :  function( uri ) {
        var store = new Ext.data.ArrayStore({
            fields: [
               {name: 'key'},
               {name: 'value'}
            ]
        })
        store.loadData(this.getModelProperties( uri, -1));
        this.store = store;
        return store;
    },

    createVersionToolbar : function(combo, access) {
       var openText = 'Edit';
       var openIcon = Util.getContext() + Util.ICON_PENCIL;

       if (access == 'VIEW') {
           openText = 'View'
           openIcon = Util.getContext() + Util.ICON_MODEL;
       }
       if (access == 'COMMENT') {
           openText = 'Comment'
           openIcon = Util.getContext() + Util.ICON_EDIT_COMMENT;
       }


       return new Ext.Toolbar({
          items: [
              combo, '->',
              { text: openText,
                icon: openIcon,
                handler: function() {
                   this.close();
                   this.maskEl.unmask();

                   var editorUrl = this.modelURI
                   if (combo.getValue() != "HEAD" && combo.getValue() != -1) {
                       editorUrl += "/versions/" + combo.getValue();
                   }

                   if ( this.portletMode )
                       window.location = editorUrl;
                   else
                       window.open(editorUrl);
                },
                scope: this
              },
              { text: 'OK' ,
                width: 50,
                handler: function() {
                     this.close();
                   this.maskEl.unmask()
                },
                scope: this}
          ]
       });
    },

    getModelProperties : function( uri , version) {
        var req = new XMLHttpRequest();

        req.open("GET", uri, false);
        req.setRequestHeader("Accept", "application/xml+model")
        req.send(null);
        var xml = req.responseXML.getElementsByTagName("properties")[0];

        var props = Util.parseProperties(xml);
        var data = new Array();

        for (var name in props) {
            if (name[0] == "#" || name == 'remove') continue;
            data.push([name, props[name]])
        }

        if (version == -1)
            version = this.comments.length - 1;

        data.push(["commit message", this.comments[version]])
        data.push(["committed by user", this.users[version]])

        return data;
    },

    reloadStore : function(uri, version) {
        var data = this.getModelProperties( uri, version);
        this.store.loadData(data);
    },

    updateMetaInformation : function( uri, version ) {
        this.reloadStore( uri, version );
    },

    updatePreview : function(imgUrl) {
        var imgPanel = this.getLayout().getLayoutItems()[0].getLayout().getLayoutItems()[0].getComponent(0);
        imgPanel.setSrc(imgUrl);
    }
});