cru.importers.giveDataImporter = CQ.Ext.extend(CQ.wcm.Viewport, {
    props: null,
    conn:new CQ.Ext.data.Connection(),
    data:null,
    resultsTemplate:null,
    resultsFrame:null,
    resultsDiv:null,
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
            id: "form-parameters",
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
                                CQ.Ext.getCmp("form-parameters").doLayout();
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
                    "xtype": "pathfield",
                    "id": "additional-mapping-file",
                    "anchor": "99%",
                    "selectOnFocus":true,
                    "allowBlank":true,
                    "hideMode": "display",
                    "hidden":true,
                    "name": "additionalMappingFile",
                    "fieldLabel":"Additional Mapping File",
                    "fieldDescription":"Select CSV file containing the aditional mapping information (required for fund). If the file to process is not uploaded yet, please upload it using webDav " +
                                       "(In MAC: go to Finder -> Go -> Connect to server and enter the URL " + window.location.origin + "/crx/repository/crx.default/var/cru-content-importer)",
                    "rootPath": config.tempStorePath
                }
            ],
            buttons: [{
              text: 'Import',
              handler: function() {
                  var config = {
                      waitTitle: "Processing",
                      waitMsg: "Starting process...",
                      success: function(form, action){
                           try {
                               var data = JSON.parse(action.response.responseXML.body.innerText);
                               CQ.Ext.getCmp("importer-container").showResults(data);
                           } catch(e) {
                               console.log(e);
                           }
                           var source = new EventSource('/services/givedataimport');
                           source.onmessage=function(event) {
                               var data = JSON.parse(event.data);
                               CQ.Ext.getCmp("importer-container").showResults(data);
                               if (data.type=="finished") {
                                   source.close();
                               }
                           };
                           return true;
                      },
                      failure: function(form, action){
                           try {
                               var data = JSON.parse(action.response.responseXML.body.innerText);
                               CQ.Ext.getCmp("importer-container").showResults(data);
                           } catch(e) {
                               console.log(e);
                           }
                           var source = new EventSource('/services/givedataimport');
                           source.onmessage=function(event) {
                               var data = JSON.parse(event.data);
                               CQ.Ext.getCmp("importer-container").showResults(data);
                               if (data.type=="finished") {
                                   source.close();
                               }
                           };
                           return true;
                      }
                  };
                  var importer = CQ.Ext.getCmp("importer-container");
                  if (importer.validateProperties()) {
                      importer.initResults();
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
            "html": "<iframe id='importer-results' height='400px' width='100%'/>"
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
        cru.importers.giveDataImporter.resultsTemplate = CQ.HTTP.get('/etc/clientlibs/cru-content-importer/widgets/import-results.html');
    },
    
    initComponent: function() {
        cru.importers.giveDataImporter.superclass.initComponent.call(this);
    },
    
    initResults: function() {
        var results = document.getElementById('importer-results');
        results.srcdoc = cru.importers.giveDataImporter.resultsTemplate.responseText;
        cru.importers.giveDataImporter.resultsFrame = null;
        cru.importers.giveDataImporter.resultsDiv = null;
    },

    showResults: function(data) {
        var content = "";
        switch (data.type) {
            case "started": content = "<strong>Import process started...</strong><br/><hr size='1'>"; break;
            case "running": content = "<strong class='error'>Import process aready running</strong><br/><hr size='1'>"; break;
            case "finished": content = "<hr size='1'><br/><strong>Finished. Errors: " + data.description + "</strong>"; break;
            case "created": content = "<div class='action'>Created</div><div class='title'></div><div class='path'>" + data.description + "</div>"; break;
            case "modified": content = "<div class='action'>Modified</div><div class='title'></div><div class='path'>" + data.description + "</div>"; break;
            case "notModified": content = "<div class='action ignore'>Not Modified</div><div class='title'></div><div class='path'>" + data.description + "</div>"; break;
            case "ignored": content = "<div class='action ignore'>Ignored (not modified)</div><div class='title'></div><div class='path'>" + data.description + "</div>"; break;
            case "error": content = "<div class='action error'>Error</div><div class='title'></div><div class='path'>" + data.description + "</div>"; break;
        }
        CQ.Ext.getCmp("importer-container").addResult(content);
    },
    
    addResult: function(content) {
        var results = cru.importers.giveDataImporter.resultsFrame;
        if (results==null) {
            results = document.getElementById('importer-results').contentWindow;
            cru.importers.giveDataImporter.resultsDiv = results.document.getElementById("results-content");
            cru.importers.giveDataImporter.resultsFrame = results;
        }
        var contentNode = results.document.createElement('div');
        contentNode.innerHTML = content;
        for (var i = 0; i < contentNode.childNodes.length; i++) {
            cru.importers.giveDataImporter.resultsDiv.appendChild(contentNode.childNodes[i]);
        }
        contentNode = results.document.createElement('br');
        cru.importers.giveDataImporter.resultsDiv.appendChild(contentNode);
        results.scrollTo(0, 100000);
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
