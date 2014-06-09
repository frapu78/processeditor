/**
 * This class is used to represent meta information about the model such as
 *      - node variants
 *      - edge variants
 *      - recommendations for succeeding nodes
 *      - default sizes of nodes
 */
function ProcessModelType(type, baseUri) {
    this.type = type;
    this.baseUri = baseUri;

    this.variants = new Array();
    this.paletteElements = new Array();
    //this.edgeVariants = new Array();
    this.nodeSuccessors = new Array();
    this.propertyTypes = new Array();
    this.defaultSizes = new Array();

    this.propertyEditors = new Array();

    this.loadEdgeVariants = function(type) {
        var req = new XMLHttpRequest();
        req.open("GET", this.baseUri + "/utils/edgetypes?modeltype="+type, false);
        req.send(null);

        var edgeClasses = req.responseXML.getElementsByTagName("edgeclass");
        var edgeVariants = new Array();
        for (var i = 0; i < edgeClasses.length; i++) {
            edgeVariants.push(edgeClasses[i].getAttribute("name"));

            this.variants[edgeClasses[i].getAttribute("name")] = edgeVariants;

            this.parsePropertyTypes(edgeClasses[i]);
        }
    }

    this.loadNodeVariants = function(type) {
        var req = new XMLHttpRequest();
        req.open("GET", this.baseUri + "/utils/nodetypes?modeltype="+type, false);
        req.send(null);

        var xml = req.responseXML;

        var nodeClasses = xml.getElementsByTagName("nodeclass");

        for (var i = 0; i < nodeClasses.length; i++) {
            var nodeClass = nodeClasses[i];
            var name = nodeClass.getAttribute("name");

            this.parseSuccessors(nodeClass);
            this.parsePropertyTypes(nodeClass, name);
            this.parseDefaultSize(nodeClass);

            this.variants[name] = new Array();

            if (nodeClass.childNodes.length > 3) {
                //three children always exist, which are the "successors" and "property-type"-node and "default-size"
                var nodeVariants = new Array();
                var nodeTypes = nodeClass.getElementsByTagName("nodetype");

                for (var j = 0; j < nodeTypes.length; j++) {
                    var className = nodeTypes[j].getAttribute("name");

                    if (j == 0) this.paletteElements.push(className);

                    this.variants[className] = nodeVariants;
                    this.parseSuccessors(nodeTypes[j]);
                    this.parsePropertyTypes(nodeTypes[j], className);
                    this.parseDefaultSize(nodeTypes[j]);

                    nodeVariants.push(className);
                }
            } else {
                this.paletteElements.push(name);
            }
        }
    }

    this.parseSuccessors = function(nodeClass) {
        if (nodeClass.getElementsByTagName("successors")[0].childNodes.length > 0) {
            var successors = nodeClass.getElementsByTagName("successors")[0].childNodes;

            var succ = new Array();

            for (var i = 0; i < successors.length; i++) {
                succ.push(successors[i].getAttribute("class"));
            }

            this.nodeSuccessors[nodeClass.getAttribute("name")] = succ;
        }
    }

    this.parsePropertyTypes = function(nodeClass, name) {
    	if ( name == "net.frapu.code.visualization.bpmn.Lane" )
    		alrt();
        if (nodeClass.getElementsByTagName("property-editor-types")[0].childNodes.length > 0) {
            var properties = nodeClass.getElementsByTagName("property-editor-types")[0].childNodes;
            var props = new Array();

            for (var i = 0; i < properties.length; i++) {
                var typeInfo = new Array();
                var type = properties[i].getAttribute("type");
                typeInfo["type"] = type;

                if (type == 'LIST') {
                    var valuesString = properties[i].getAttribute("values");
                    valuesString = valuesString.substring(1, valuesString.length - 1);

                    typeInfo["values"] = valuesString.split(",");
                }

                props[properties[i].getAttribute("name")] = typeInfo;
            }

            this.propertyTypes[name] = props;
        }
    }

    this.createEditor = function(typeInfo, attribute, initValue, label, formItemCls) {
        if (typeInfo == null)
            typeInfo = {type: 'DEFAULT'};
        
        var type = typeInfo.type;

        if ( label == null )
            label = attribute;

        function onChange() {
            var value = this.getValue();

            if ( value == null )
                value = '';

            if (this instanceof Ext.form.Checkbox) {
                if (value == true) value = '1';
                if (value == false) value = '0';
            }

            ProcessEditor.instance.selectionHandler.singleObject.updateProperty(attribute, Util.escapeString(value))
        }

        var editor = null;
        if (type == 'BOOLEAN') {
            var s = false;
            if (initValue == 'true' || initValue == '1')
                s = true;
            
            editor = new Ext.form.field.Checkbox({
                fieldLabel: label, 
                labelStyle: "font-size:11px;width:89px;margin-left:3px;margin-top: 3px",
                formItemCls: formItemCls,
                checked: s
            });
            
//            editor.setValue(s);
            editor.on("change", onChange);
        } else if (type == 'MULTILINE') {
            editor = new Ext.form.TextArea({
                fieldLabel: label,
                style: "font-size:11px;",
                formItemCls: formItemCls,
                value: initValue,
                enableKeyEvents: true});
            editor.on("blur", onChange);
        } else if (type == 'COLOR') {
            editor = new Ext.form.field.Color({
                fieldLabel: label,
                value: initValue,
                formItemCls: formItemCls
            });
            
            editor.on("select", onChange);
            editor.on("valueChange", onChange);
        } else if (type == 'LIST') {
            var data = new Array();
            for (var i = 0; i < typeInfo.values.length; i++)
                data.push([i, Ext.util.Format.trim(typeInfo.values[i])])

            editor = new Ext.form.ComboBox({
                triggerAction: 'all',
                queryMode: 'local',
                store: new Ext.data.ArrayStore({
                    id: 0,
                    fields: ['id', 'fieldValue'],
                    data: data
                }),
                valueField: 'fieldValue',
                displayField: 'fieldValue',
                fieldLabel: label,
                editable: false,
                disableKeyFilter: true,
                formItemCls: formItemCls
            })
            editor.setValue(initValue);
            editor.on("select", onChange)
        } else {
            editor = new Ext.form.field.Text({
                fieldLabel: label,
                style: "font-size:11px;",
                enableKeyEvents: true, 
                autoWidth: true,
                value: initValue,
                formItemCls: formItemCls
            });
            editor.on("blur", onChange);
        }

        return editor;
    }

    this.parseDefaultSize = function(nodeClass) {
        var name = nodeClass.getAttribute("name");
        
        for (var i = 0; i < nodeClass.childNodes.length; i++) {
            if(nodeClass.childNodes[i].nodeName == "default-size") {
                var props = Util.parseProperties(nodeClass.childNodes[i]);

                this.defaultSizes[name] = {width: parseInt(props.width), height: parseInt(props.height)};
            }
        }
    }

    this.fetchPropertiesFromServer = function(className) {
        var req = new XMLHttpRequest();
        req.open("GET", this.baseUri + "/utils/propertyeditortypes?objecttype=" + className, false);
        req.send(null);
        if (req.status == 200)
            this.parsePropertyTypes(req.responseXML, className);
    }

    this.loadEdgeVariants(this.type);
    this.loadNodeVariants(this.type);

    this.propertyTypes['model'] = new Array();
}

