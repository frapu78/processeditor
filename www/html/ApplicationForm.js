 Ext.onReady(function(){
      var _tasklist_tasklist_ext_isp_WAR_isp_Widget_16965163_FormPanel =
        new Ext.Panel({
            layout: 'form',
            frame: 'true',
            bodyStyle:'padding:5px 5px 0',
            width: 550,labelWidth: 100,
              items: [{
                   layout: 'column',
                   items:[

                     {    columnWidth: .5,
                          layout:'form',
                          labelAlign: 'top',

                          defaults: {width: 240},
              items: [
              {
                   xtype:'textfield',
                   fieldLabel: 'First name',
                   name: 'firstName',
                   value: 'James'
                },
              {
                   xtype:'textfield',
                   fieldLabel: 'Last name',
                   name: 'lastName',
                   value: 'Smith'
                },
              {
                   xtype:'textfield',
                   fieldLabel: 'City',
                   name: 'city',
                   value: 'London'
                },
              {
                   xtype:'textfield',
                   fieldLabel: 'E-Mail*',
                   name: 'email',
                   value: 'hr@localhost'
                }        ]

                          },
                     {    columnWidth: .5,
                          layout:'form',
                          labelAlign: 'top',

                          defaults: {width: 240},
              items: [
              {
                 xtype: 'combo',
                 fieldLabel: 'Position',
                 emptyText: 'Select a position...',
                 forceSelection: true,
                 mode: 'local',
                 name: 'position',
                 triggerAction: 'all',
                 editable: false,
                 store: ['Sales Manager','R&D Engineer']
              },
              {
                   xtype:'textfield',
                   fieldLabel: 'Requested Salary',
                   name: 'salary',
                   value: '45000'
                },
              {

                    xtype: 'textarea',
                    height: 75,
                   fieldLabel: 'Summary',
                   name: 'summary',
                   value: 'I\'m applying for your Web Artist\'s position.'
                }        ]

                          }
                     ]
              },
                  {
                  xtype:'spacer',
                  height: '5px',
                  orientation:'vertical'
                 },{
                 xtype:'fieldset',
                 collapsible: true,
                 width: 510,
                 defaults: {width: 350},
                 title: 'Attachements',
                 collapsed: true,
                 bodyStyle:'padding:10px',
                 labelWidth: 120,
              items: [
                        new Ext.ux.form.FileUploadField({
                           fieldLabel: 'CV',
                           name: 'upload01'
                        })
                    ,
                        new Ext.ux.form.FileUploadField({
                           emptyText: 'Attach Letter of Recommendation here',
                           fieldLabel: 'Recommendation',

                           name: 'upload02'
                        })
                    ,
                        new Ext.ux.form.FileUploadField({
                           emptyText: 'Attach further document here',
                           fieldLabel: 'Additional document',

                           name: 'upload03'
                        })
                            ]

              }        ]
     ,
            buttons: [
              {
                   text: 'Cancel',

                    handler: function(){
                    setSubmitPropertyIS('cancel','false','_tasklist_tasklist_ext_isp_WAR_isp_');
                    dataIS.submitter=this;
                    document.getElementById('_tasklist_tasklist_ext_isp_WAR_isp_formIS').submit();
                    //document.getElementById('_tasklist_tasklist_isp_WAR_isp_formIS').submit();
                    }
              },
              {
                   text: 'Create new application',
                   icon: '/ibis/servlet/Repository/Root/hr-admin/pics/new_small.gif',
                    handler: function(){
                    setSubmitPropertyIS('ok','false','_tasklist_tasklist_ext_isp_WAR_isp_');
                    dataIS.submitter=this;
                    document.getElementById('_tasklist_tasklist_ext_isp_WAR_isp_formIS').submit();
                    //document.getElementById('_tasklist_tasklist_isp_WAR_isp_formIS').submit();
                    }
              }],
              renderTo: '_tasklist_tasklist_ext_isp_WAR_isp_Widget_16965163'
        });
    })
