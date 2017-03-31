//This is the default place holder
var api_doc_local =
{
    "swagger": "2.0",
    "paths": {},
    "info": {
        "title": "",
        "version": ""
    }
};

var context = {};

var VERBS = [ 'get' , 'post' , 'put' , 'delete', 'patch', 'head'];

var AUTH_TYPES = [
    { "value": 'None', "text":'None'} ,
    { "value": 'Application', "text":'Application'} ,
    { "value": 'Application User', "text":'Application User'} ,
    { "value": 'Application & Application User', "text":'Application & Application User'}
];

var api_id = $('input[name="apiId"]').val();
var api_client = new API('');

var content_types = [
    { value : "application/json", text :  "application/json"},
    { value : "application/xml", text :  "application/xml"},
    { value : "text/plain", text :  "text/plain"},
    { value : "text/html", text :  "text/html"}
];

//Create a designer class
function APIDesigner(){

    //implement singleton pattern
    if ( arguments.callee._singletonInstance )
        return arguments.callee._singletonInstance;
    arguments.callee._singletonInstance = this;

    this.api_doc = api_doc_local;
    this.initControllersCall = "";

    this.container = $( "#api_designer" );

}

APIDesigner.prototype.check_if_resource_exist = function(path, method){
    for (var key in this.api_doc.paths) {

        //remove tailing slash
        if (path.lastIndexOf('/') == path.length -1) {
            path = path.substring(0, path.length -1);
        }

        var keyWithoutTailingSlash = key;
        if (key.lastIndexOf('/') == key.length -1) {
            keyWithoutTailingSlash = key.substring(0, key.length -1);
        }

        if(keyWithoutTailingSlash.toLowerCase() == path.toLowerCase()){

            if (this.api_doc.paths[key].hasOwnProperty(method)) {

                return true;
            }
        }
    }
    return false;
}

APIDesigner.prototype.has_resources = function(){
    if(Object.keys(this.api_doc.paths).length == 0)
        return false;
    else
        return true;
}


APIDesigner.prototype.update_elements = function(resource, newValue){
    var swaggerSchema = JSON.parse('{"type":"object"}');
    var API_DESIGNER = APIDesigner();
    var obj = API_DESIGNER.query($(this).attr('data-path'));
    var obj = obj[0]
    if(obj["$ref"]!=undefined){
        var obj = API_DESIGNER.query(obj["$ref"].replace("#","$").replace(/\//g,"."));
        var obj = obj[0];
    }
    if ($(this).attr('data-attr-type') == "comma_seperated") {
        newValue = $.map(newValue.split(","), $.trim);
    }
    var i = $(this).attr('data-attr');
    obj[i] = newValue;
    if (i == "in") {
        //Add body parameter to the swagger
        if (newValue == "body") {
            delete obj.type;
            obj['schema'] = swaggerSchema;
        } else { //other parameters
            delete obj.schema;
            obj['type'] = "string";
        }
    }

};

APIDesigner.prototype.update_elements_boolean = function(resource, newValue){
    if(newValue == "true")
        newValue = true;
    else
        newValue = false;
    var API_DESIGNER = APIDesigner();
    var obj = API_DESIGNER.query($(this).attr('data-path'));
    var obj = obj[0];
    if(obj["$ref"]!=undefined ){
        var obj = API_DESIGNER.query(obj["$ref"].replace("#","$").replace(/\//g,"."));
        var obj = obj[0];
    }
    var i = $(this).attr('data-attr');
    obj[i] = newValue;
};

APIDesigner.prototype.init_controllers = function(container){
    var API_DESIGNER = this;

    if(this.initControllersCall != $(container).attr('data-path')){

        this.container.delegate(".movedown_resource","click", function(){
            var operations = API_DESIGNER.query($(this).attr('data-path'));
            var operations = operations[0]
            var i = parseInt($(this).attr('data-index'));
            if(i != (operations.length - 1)){
                var tmp = operations[i];
                operations[i] = operations[i+1];
                operations[i+1] = tmp;
            }
            API_DESIGNER.render_resources();
        });

        this.container.delegate(".moveup_resource","click", function(){
            var operations = API_DESIGNER.query($(this).attr('data-path'));
            var operations = operations[0];
            var i = parseInt($(this).attr('data-index'));
            if(i != 0){
                var tmp = operations[i];
                operations[i] = operations[i-1];
                operations[i-1] = tmp;
            }
            API_DESIGNER.render_resources();
        });

        container.delegate(".add_parameter", "click", function(event){
            var parameter = $(this).parent().find('.parameter_name').val();
            if(parameter == "") return false;
            var resource_body = $(this).parent().parent();
            var resource = API_DESIGNER.query(resource_body.attr('data-path'));
            var resource = resource[0]
            if(resource.parameters ==undefined){
                resource.parameters = [];
            }
            resource.parameters.push({ name : parameter , in : "query", required : false , type: "string"});
            //@todo need to checge parent.parent to stop code brak when template change.
            API_DESIGNER.render_resource(resource_body);
        });

        container.delegate(".delete_parameter", "click", function (event) {
            var deleteData = $(this).attr("data-path");
            var i = $(this).attr("data-index");
            var resource_body = $(this).parent().parent().parent().parent().parent().parent().parent();

            var deleteDataArray = deleteData.split(".");
            var operations = deleteDataArray[2];
            var operation = deleteDataArray[3];
            var paramName = API_DESIGNER.api_doc.paths[operations][operation]['parameters'][i]['name'];

            noty({
                text: 'Do you want to delete the parameter <span class="text-info">' + paramName + '</span> ?',
                type: 'alert',
                dismissQueue: true,
                layout: "topCenter",
                modal: true,
                theme: 'relax',
                buttons: [
                    {
                        addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                        $noty.close();
                        API_DESIGNER = APIDesigner();
                        API_DESIGNER.api_doc.paths[operations][operation]['parameters'].splice(i,1);
                        API_DESIGNER.render_resource(resource_body);
                    }
                    },
                    {
                        addClass: 'btn btn-info', text: 'Cancel', onClick: function ($noty) {
                        $noty.close();
                    }
                    }
                ]
            });
        });

    }

    this.initControllersCall = $(container).attr('data-path');

}

APIDesigner.prototype.load_api_document = function(api_document){
    this.api_doc = api_document;
    this.render_resources();
    $("#version").val(api_document.info.version);
    $("#name").val(api_document.info.title);
    if(api_document.info.description){
        $("#description").val(api_document.info.description);
    }

    $( "#api_designer" ).delegate( "#more", "click", this, function( event ) {
        $("#options").css("display", "inline-block");
        $("#less").css("display", "inline-block");
        $("#more").hide();
    });
    $( "#api_designer" ).delegate( "#less", "click", this, function( event ) {
        $("#options").hide();
        $("#more").css("display", "inline-block");
        $("#less").hide();
    });

    $( "#api_designer" ).delegate( "a.help_popup", "mouseover", this, function( event ) {
        $('a.help_popup').popover({
            html : true,
            container: 'body',
            content: function() {
                var msg = $('#'+$(this).attr('help_data')).html();
                return msg;
            },
            template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
        });
    });

    $('a.help_popup i').popover({
        html : true,
        container: 'body',
        content: function() {
            var msg = $('#'+$(this).attr('help_data')).html();
            return msg;
        },
        template: '<div class="popover default-popover" role="tooltip"><div class="arrow"></div><div class="popover-content"></div></div>'
    });

    $( "#api_designer" ).delegate("#add_resource","click", this, function( event ) {
        var designer = APIDesigner();
        if($("#resource_url_pattern").val() == ""){
            var message = "URL pattern cannot be empty.";
            noty({
                text: message,
                type: 'warning',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10
            });
            return;
        }
        // checking for white spaces in URL template
        if (/\s/.test( $("#resource_url_pattern").val() )) {
            var message = "URL pattern cannot contain white space";
            noty({
                text: message,
                type: 'warning',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10
            });
            return;
        }

        var path = $("#resource_url_pattern").val();
        if(path.charAt(0) != "/")
            path = "/"+path;

        var resource_exist = false;
        $(".http_verb_select").each(function(){    //added this validation to fix https://wso2.org/jira/browse/APIMANAGER-2671
            if($(this).is(':checked')){
                if(designer.check_if_resource_exist( path , $(this).val() ) ){
                    resource_exist = true;
                    // @todo: param_string
                    var message = "Resource already exist for URL Pattern "+path+" and Verb "+$(this).val();
                    noty({
                        text: message,
                        type: 'warning',
                        dismissQueue: true,
                        progressBar: true,
                        timeout: 5000,
                        layout: 'topCenter',
                        theme: 'relax',
                        maxVisible: 10
                    });
                    return;
                }
            }
        });
        if(resource_exist){
            return;
        }

        var resource = {

        };
        //create parameters
        var re = /\{[a-zA-Z0-9_-]*\}/g;
        var parameters = [];

        while ((m = re.exec($("#resource_url_pattern").val())) != null) {
            if (m.index === re.lastIndex) {
                re.lastIndex++;
            }
            parameters.push({
                name : m[0].replace("{","").replace("}",""),
                "in": "path",
                "allowMultiple": false,
                "required": true,
                "type":"string"
            })
        }

        var vc=0;
        var ic=0;
        $(".http_verb_select").each(function(){
            if($(this).is(':checked')){
                if(!designer.check_if_resource_exist( path , $(this).val() ) ){
                    parameters = $.extend(true, [], parameters);

                    var method = $(this).val();
                    var tempPara = parameters.concat();

                    if(method.toUpperCase() == "POST" || method.toUpperCase() == "PUT" || method.toUpperCase() == "PATCH") {
                        tempPara.push({
                            "name" : "Payload",
                            "description": "Request Body",
                            "required": false,
                            "in": "body",
                            "schema": {
                                "type" : "object",
                                "properties" : {
                                    "payload" : {
                                        "type": "string"
                                    }
                                }
                            }
                        });
                    }
                    resource[method] = {
                        responses : { '200': {
                            "description" : ""
                        }
                        }
                    };
                    if(tempPara.length > 0){
                        resource[method].parameters = tempPara;
                    }
                    ic++
                }
                vc++;
            }
        });
        if(vc==0){
            var message = "You should select at least one HTTP verb.";
            noty({
                text: message,
                type: 'warning',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10
            });
            return;
        }
        event.data.add_resource(resource, path);
        $("#resource_url_pattern").val("");
        $(".http_verb_select").attr("checked",false);
    });
    $("#save_resources").click(function() {
        var designer = APIDesigner();
        api_client.updateSwagger(api_id, designer.api_doc).then(
            function (response) {
                api_doc_local = response.obj;
                var designer = new APIDesigner();
                designer.api_doc = api_doc_local;
                designer.initControllersCall = "";
                designer.render_resources();
                var message = "API Resources saved successfully.";
                noty({
                    text: message,
                    type: 'success',
                    dismissQueue: true,
                    progressBar: true,
                    timeout: 5000,
                    layout: 'topCenter',
                    theme: 'relax',
                    maxVisible: 10
                });
            }).catch(apiGetErrorHandler);
    });
};


APIDesigner.prototype.render_scopes = function(){
    if($('#scopes-template').length){
        context = {
            "api_doc" : this.api_doc
        }
        var output = Handlebars.partials['scopes-template'](context);
        $('#scopes_view').html(output);
    }
};

APIDesigner.prototype.transform = function(api_doc){
    var swagger = jQuery.extend(true, {}, api_doc);
    var i=0;
    for(var pathkey in swagger.paths){
        var path = swagger.paths[pathkey];
        delete path.parameters;
        for(var verbkey in path){
            var verb = path[verbkey];
            verb.path = pathkey;
            verb.index = i;
            i++;
        }
    }
    return swagger;
}

APIDesigner.prototype.render_resources = function(){
    context = {
        "doc" : this.transform(this.api_doc),
        "verbs" :VERBS,
        "has_resources" : this.has_resources()
    }
    var callbacksResources = {
        onSuccess: function (data) {
            $('#resource_details').html(data);
            var designer = new APIDesigner();
            $('#resource_details').find('.change_summary').editable({
                emptytext: '+ Summary',
                success : designer.update_elements,
                inputclass : 'resource_summary'
            });
            $.fn.editableform.buttons =
                '<button type="submit" class="btn btn-primary btn-sm editable-submit">'+
                '<i class="fw fw-check"></i>'+
                '</button>'+
                '<button type="button" class="btn btn-secondary btn-sm editable-cancel">'+
                '<i class="fw fw-cancel"></i>'+
                '</button>';
            $(".resource_expand").click(function() {
                if(this.resource_created == undefined){
                    designer.render_resource($(this).parent().next().find('.resource_body'));
                    this.resource_created = true;
                    $(this).parent().next().find('.resource_body').show();
                }
                else{
                    $(this).parent().next().find('.resource_body').toggle();
                }
            });

            $(".delete_resource").click(function( event ) {
                $("#messageModal div.modal-footer").html("");
                var operations = designer.query($(this).attr('data-path'));
                var operations = operations[0]
                var i = $(this).attr('data-index');
                var pn = $(this).attr('data-path-name');
                var op = $(this).attr('data-operation');

                noty({
                    text: 'Do you want to remove <span class="text-info">' +op+' : '+ Handlebars.Utils.escapeExpression(pn) + '</span> resource from list?',
                    type: 'alert',
                    dismissQueue: true,
                    layout: "topCenter",
                    modal: true,
                    theme: 'relax',
                    buttons: [
                        {
                            addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                            $noty.close();
                            var API_DESIGNER = APIDesigner();
                            delete API_DESIGNER.api_doc.paths[pn][op];
                            API_DESIGNER.render_resources();
                            if(Object.keys(API_DESIGNER.api_doc.paths[pn]).length == 0) {
                                delete API_DESIGNER.api_doc.paths[pn];
                            }
                        }
                        },
                        {
                            addClass: 'btn btn-info', text: 'Cancel', onClick: function ($noty) {
                            $noty.close();
                        }
                        }
                    ]
                });
            });
        }, onFailure: function (data) {
        }
    };

    UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-resources-table", context, callbacksResources);
};

APIDesigner.prototype.render_resource = function(container){
    var isBodyRequired = false;
    var operation = this.query(container.attr('data-path'));
    var context = jQuery.extend(true, {}, operation[0]);
    context.resource_path = container.attr('data-path');
    if (context.resource_path.match(/post/i) || context.resource_path.match(/put/i)) {
        isBodyRequired = true;
    }

    this.render_resource_view(container, context, isBodyRequired);
};

APIDesigner.prototype.render_resource_view = function(container, context, isBodyRequired){

    var API_Designer = this;
    var callbacksResources = {
        onSuccess: function (data) {
            container.html(data);
            container.show();
            container.find('.notes').editable({
                type: 'textarea',
                emptytext: '+ Add Implementation Notes',
                success : API_Designer.update_elements,
                rows: 1,
                tpl: '<textarea cols="50"></textarea>',
                mode: 'popup'
            });
            container.find('.produces').editable({
                source: content_types,
                success : API_Designer.update_elements
            });
            container.find('.consumes').editable({
                source: content_types,
                success : API_Designer.update_elements
            });
            container.find('.param_desc').editable({
                emptytext: '+ Empty',
                success : API_Designer.update_elements,
                mode: 'popup'
            });
            if(isBodyRequired){
                container.find('.param_paramType').editable({
                    emptytext: '+ Set Param Type',
                    source: [ { value:"body", text:"body" },{ value:"query", text:"query" },{ value:"header", text:"header" }, { value:"formData", text:"formData"} ],
                    success : API_Designer.update_elements,
                    mode: 'popup'
                });
            } else {
                container.find('.param_paramType').editable({
                    emptytext: '+ Set Param Type',
                    source: [{ value:"query", text:"query" },{ value:"header", text:"header" }, { value:"formData", text:"formData"} ],
                    success : API_Designer.update_elements,
                    mode: 'popup',
                    error: function(response, newValue) {
                        console.log(response);
                    }
                });
            }

            container.find('.param_type').editable({
                emptytext: '+ Empty',
                success : API_Designer.update_elements,
                mode: 'popup'
            });
            container.find('.param_required').editable({
                emptytext: '+ Empty',
                autotext: "always",
                display: function(value, sourceData){
                    if(value == true || value == "true")
                        $(this).text("True");
                    if(value == false || value == "false")
                        $(this).text("False");
                },
                source: [ { value:true, text:"True" },{ value:false, text:"False"} ],
                success : API_Designer.update_elements_boolean,
                mode: 'popup'
            });
            API_Designer.init_controllers(container);
        }, onFailure: function (data) {
            alert(data);
        }
    };

    UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-resource-row", context, callbacksResources);
}

APIDesigner.prototype.query = function(path){
    return JSONPath(path, this.api_doc);
}

APIDesigner.prototype.add_resource = function(resource, path){
    if(path.charAt(0) != "/")
        path = "/" + path;
    if(this.api_doc.paths[path] == undefined){
        this.api_doc.paths[path] = resource;
    }
    else{
        this.api_doc.paths[path] = $.extend({}, this.api_doc.paths[path], resource);
    }
    //this.initControllersCall = false;
    this.render_resources();
};


$(document).ready(function(){
    $.fn.editable.defaults.mode = 'inline';
});
