cru.importers.giveDataImporter = CQ.Ext.extend(CQ.wcm.Viewport, {
    props: null,
    conn:new CQ.Ext.data.Connection(),
    data:null,
    constructor: function(config) {
        this.debug = config.debug;
        
        var processOptions = [];
        var defaultPaths = {};
        var requireAdditionalMappingFile = [];
        config.options.forEach(function(item,index){
            var path = item.trim();
            var request = CQ.HTTP.get(path + ".json");
            var option = CQ.Util.eval(request);
            processOptions.push({text:option.title,value:path});
            defaultPaths[path] = option.defaultPath;
            if (option.additionalMappingFile) {
                requireAdditionalMappingFile.push(path);
            }
        });

        var formParameters = new CQ.Ext.FormPanel({
            url: "/services/givedataimport",
            title:"",
            fileUpload:true,
            border:false,
            labelWidth:100,
            buttonAlign:"left",
            bodyStyle: {
                "background-color": "#f0f0f0",
                "padding": "0px 5px 0px 5px"
            },
            items: [
                {
                  "id":"importer-filename",
                  "xtype":"pathfield",
                  "anchor": "99%",
                  "selectOnFocus":true,
                  "allowBlank":false,
                  "name":"filename",
                  "fieldLabel":"Zip file",
                  "fieldDescription":"Select a zip file to be imported. If the file to process is not uploaded yet, please upload it using webDav " +
                                     "(In MAC: go to Finder -> Go -> Connect to server and enter the URL " + window.location.origin + "/crx/repository/crx.default/var/cru-content-importer)",
                  "rootPath": config.tempStorePath
                },
                {
                    "id":"importer-option",
                    "xtype":"selection",
                    "type":"select",
                    "allowBlank":false,
                    "name":"configpath",
                    "fieldLabel":"Process",
                    "fieldDescription":"Select the type of archive to process.",
                    "options":processOptions,
                    "listeners":{
                        "selectionchanged": function(selection,value,checked) {
                            CQ.Ext.getCmp("importer-baselocation").setRawValue(defaultPaths[value]);
                            if ($.inArray(value,requireAdditionalMappingFile) > -1) {
                                CQ.Ext.getCmp("additional-mapping-file").show();
                                CQ.Ext.getCmp("additional-mapping-file").allowBlank = false;
                                CQ.Ext.getCmp("additional-mapping-file").syncSize();
                            } else {
                                CQ.Ext.getCmp("additional-mapping-file").hide();
                                CQ.Ext.getCmp("additional-mapping-file").allowBlank = true;
                            }
                        }
                    }
                },
                {
                    "id":"importer-baselocation",
                    "xtype":"pathfield",
                    "anchor": "99%",
                    "selectOnFocus":true,
                    "allowBlank":false,
                    "name":"baselocation",
                    "fieldLabel":"Base Path",
                    "fieldDescription":"Select a base location where the resources will be imported."
                },
                {
                    "xtype": "fileuploadfield",
                    "id": "additional-mapping-file",
                    "fieldLabel": "Additional Mapping File",
                    "fieldDescription": "Enter a CSV file containing the aditional mapping information (required for fund).",
                    "name": "additionalMappingFile",
                    "allowBlank":true,
                    "anchor": "99%",
                    "hideMode": "display",
                    "hidden":true
                }
            ],
            buttons: [{
              text: 'Import',
              handler: function() {
                  var config = {
                      waitTitle: "Processing",
                      waitMsg: "Process In Progress",
                      success: function(form, action){
                          return CQ.Ext.getCmp("importer-container").showResults(action.response.responseText);
                      },
                      failure: function(form, action){
                          return CQ.Ext.getCmp("importer-container").showResults(action.response.responseText);
                      }
                  };
                  var importer = CQ.Ext.getCmp("importer-container");
                  if (importer.validateProperties()) {
                      var action = new CQ.form.SlingSubmitAction(formParameters.getForm(), config);
                      formParameters.getForm().doAction(action);
                  }
              }
            },{
              text: 'Import SSE',
              handler: function() {
                  var config = {
                      waitTitle: "Processing",
                      waitMsg: "Starting process...",
                      success: function(form, action){
                          console.log("success");
                          var source = new EventSource('/services/givedataimport');
                          source.onmessage=function(event) {
                              var data = JSON.parse(event.data);
                              if (data.type=="close") {
                                  source.close();
                              }
                              console.log(data);
                          };
                          return CQ.Ext.getCmp("importer-container").showResults(action.response.responseText);
                      },
                      failure: function(form, action){
                          console.log("failure");
                          var source = new EventSource('/services/givedataimport');
                          source.onmessage=function(event) {
                              var data = JSON.parse(event.data);
                              if (data.type=="close") {
                                  source.close();
                              }
                              console.log(data);
                          };
                          return CQ.Ext.getCmp("importer-container").showResults(action.response.responseText);
                      }
                  };
                  var importer = CQ.Ext.getCmp("importer-container");
                  if (importer.validateProperties()) {
                      var action = new CQ.form.SlingSubmitAction(formParameters.getForm(), config);
                      formParameters.getForm().doAction(action);
                  }
              }
          }]
        });
        
        var panelResult = new CQ.Ext.Panel({
            "id": "importer-results-container",
            "xtype": "panel",
            "title": "Import Results",
            "autoScroll": true,
            "items": [
                  {
                      "id":"importer-results",
                      "xtype":"fieldset",
                      "collapsible": false,
                      "items": []
                  }
            ]
        });
        
        // init component by calling super constructor
        cru.importers.giveDataImporter.superclass.constructor.call(this, {
            "id":"importer-container",
            "items": [{
                "id":"importer-wrapper",
                "xtype":"panel",
                "title": config["jcr:title"],
                "layout":"border",
                "region":"center",
                "border":false,
                "items": [
                    {
                        "id":"importer-north",
                        "xtype":"container",
                        "region":"north",
                        "autoEl":"div",
                        "height": 250,
                        "border":"false",
                        "items": [
                            formParameters
                        ]
                    },
                    {
                        "id":"importer-center",
                        "xtype":"container",
                        "region":"center",
                        "autoEl":"div",
                        "border":"false",
                        "items": [
                            panelResult
                        ]
                    }
                ]
            }]
        });
    },
    
    initComponent: function() {
        cru.importers.giveDataImporter.superclass.initComponent.call(this);
    },
    
    showResults: function(responseText) {
        var results = CQ.Ext.getCmp('importer-results');
        results.removeAll();
        try{
            var obj = JSON.parse(responseText);
            if (obj.errors.length > 0) {
                results.add(this.showResultSection("Errors",obj.errors));
            }
            if (obj.createdPages.length > 0) {
                results.add(this.showResultSection("Created Resources",obj.createdPages));
            }
            if (obj.modifiedPages.length > 0) {
                results.add(this.showResultSection("Modified Resources",obj.modifiedPages));
            }
            if (obj.notModifiedPages.length > 0) {
                results.add(this.showResultSection("Not Modified Resources",obj.notModifiedPages));
            }
            if (obj.ignoredPages.length > 0) {
                results.add(this.showResultSection("Ignored Resources",obj.ignoredPages));
            }
        } catch(e) {
            results.add(new CQ.Ext.form.Label({html: responseText}));
        }
        results.doLayout();
        return true;
    },
    
    showResultSection: function(label, values){
        var result = new CQ.Ext.form.FieldSet({
            title: label + ": " + values.length,
            collapsible: true,
            collapsed: true,
        });
        $.each(values,function(index, element){
            result.add(new CQ.Static({html: element}));
        });

        return result;
    },
    
    validateProperties: function() {
        zipFile = CQ.Ext.getCmp("importer-filename").isValid(false);
        baselocation = CQ.Ext.getCmp("importer-baselocation").isValid(false);
        option = CQ.Ext.getCmp("importer-option").isValid(false);
        additional = true;
        if (CQ.Ext.getCmp("additional-mapping-file").isVisible()) {
            additional = CQ.Ext.getCmp("additional-mapping-file").isValid(false);
        }
        return zipFile && baselocation && option && additional;
    }
    
});

CQ.Ext.reg("cruGiveDataImporter", cru.importers.giveDataImporter);
