//This is the default place holder
var api_doc = 
{
    "swagger": "2.0",
    "paths": {},
    "info": {
        "title": "",
        "version": ""
    }
};

Handlebars.registerHelper('countKeys', function(value){
    return Object.keys(value).length * 2 + 1;
});

Handlebars.registerHelper('setIndex', function(value){
    this.index = Number(value);
});

Handlebars.registerHelper('console_log', function(value){
    console.log(value);
});

Handlebars.registerHelper( 'toString', function returnToString( x ){
    return ( x === void 0 ) ? 'undefined' : x.toString();
} );

Handlebars.registerHelper('ref', function(items, options) {
  if(items["$ref"] != undefined){
    var api = APIDesigner();
    var result = api.query(items["$ref"].replace("#","$").replace(/\//g,"."));
    if(result.length > 0){
        items = result[0];
    }
  }
  out = options.fn(items);
  return out;
});

var content_types = [
       { value : "application/json", text :  "application/json"},
       { value : "application/xml", text :  "application/xml"},
       { value : "text/plain", text :  "text/plain"},
       { value : "text/html", text :  "text/html"}
];

//function to check if an attribute exists in a nested series of objects
function checkNested(obj) {
  for (var i = 1; i < arguments.length; i++) {
    if (!obj.hasOwnProperty(arguments[i])) {
      return false;
    }
    obj = obj[arguments[i]];
  }
  return true;
}

//Create a designer class
function APIDesigner(){
    //implement singleton pattern
    this.baseURLValue = "";

    if ( arguments.callee._singletonInstance )
        return arguments.callee._singletonInstance;
    arguments.callee._singletonInstance = this;

    this.api_doc = {};
    this.resources = [] ;

    this.container = $( "#api_designer" );

    //initialise the partials
    source   = $("#designer-resources-template").html();
    Handlebars.partials['designer-resources-template'] = Handlebars.compile(source);
    source   = $("#designer-resource-template").html();
    Handlebars.partials['designer-resource-template'] = Handlebars.compile(source);
    if($('#scopes-template').length){
        source   = $("#scopes-template").html();
        Handlebars.partials['scopes-template'] = Handlebars.compile(source);        
    }

    this.init_controllers();

    $( "#api_designer" ).delegate( "#more", "click", this, function( event ) {
                        $("#options").css("display", "inline-block");
                        $("#more").hide();
    });
  $( "#api_designer" ).delegate( "#less", "click", this, function( event ) {
                         $("#options").hide();
                         $("#more").css("display", "inline-block");
    });

    $( "#api_designer" ).delegate( "a.help_popup", "mouseover", this, function( event ) {
        $('a.help_popup').popover({
            html : true,
            content: function() {
                return $('#'+$(this).attr('help_data')).html();
            }
        });
    });

    $('a.help_popup i').popover({
        html : true,
        content: function() {
            return $('#'+$(this).attr('help_data')).html();
        }
    });

    $( "#api_designer" ).delegate( ".resource_expand", "click", this, function( event ) {
        if(this.resource_created == undefined){
            event.data.render_resource($(this).parent().next().find('.resource_body'));
            this.resource_created = true;
            $(this).parent().next().find('.resource_body').show();
        }
        else{
            $(this).parent().next().find('.resource_body').toggle();
        }
    });

    $( "#api_designer" ).delegate( "#add_resource", "click", this, function( event ) {
        var designer = APIDesigner();
        if($("#resource_url_pattern").val() == ""){
            jagg.message({content:"URL pattern cannot be empty.",type:"error"});
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
                    var err_message = "Resource already exist for URL Pattern "+path+" and Verb "+$(this).val();
                    jagg.message({content:err_message,type:"error"});
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

                    if(method.toUpperCase() == "POST" || method.toUpperCase() == "PUT") {
                        tempPara.push({
                            "name" : "Payload",
                            "description": "Request Body",
                            "required": false,
                            "in": "body",
                            "schema": {
                                "type" : "object"
                            }
                        });
                    }
                    resource[method] = { 
                        responses : { '200':{}}
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
            jagg.message({content:"You should select at least one HTTP verb." ,type:"error"});            
            return;
        }
        event.data.add_resource(resource, path);
        //RESOURCES.unshift(resource);
        $("#resource_url_pattern").val("");
        updateContextPattern();
        $(".http_verb_select").attr("checked",false);
    });

 
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


APIDesigner.prototype.set_default_management_values = function(){
    var operations = this.query("$.paths.*.*");
    for(var i=0;i < operations.length;i++){
        if(!operations[i]["x-auth-type"]){
            if(operations[i].method == "OPTIONS"){
                operations[i]["x-auth-type"] = OPTION_DEFAULT_AUTH;
            }
            else{
                operations[i]["x-auth-type"] = DEFAULT_AUTH;                
            }
        }
        if(!operations[i]["x-throttling-tier"]){
            operations[i]["x-throttling-tier"] = DEFAULT_TIER;
        }
    }
}

APIDesigner.prototype.add_default_resource = function(){
    $("#resource_url_pattern").val("*");
    $(".http_verb_select:lt(5)").attr("checked","checked");
    $("#inputResource").val("Default");
    $("#add_resource").trigger('click');
}

APIDesigner.prototype.get_scopes = function() {
    var options = [{ "value": "" , "text": "" }];
    if(checkNested(this.api_doc, 'x-wso2-security','apim','x-wso2-scopes')){
    	var scopes = this.api_doc['x-wso2-security'].apim['x-wso2-scopes'];
    	for(var i =0; i < scopes.length ; i++ ){
    	    options.push({ "value": scopes[i].key , "text": scopes[i].name });
    	}	
    }
    return options;
}

APIDesigner.prototype.has_resources = function(){
    if(Object.keys(this.api_doc.paths).length == 0) 
        return false;
    else
        return true;
}

APIDesigner.prototype.display_elements = function(value,source){
    for(var i =0; i < source.length; i++ ){
        if(value == source[i].value){
            $(this).text(source[i].text);
        }
    }
};

APIDesigner.prototype.update_elements = function(resource, newValue){
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

APIDesigner.prototype.init_controllers = function(){
    var API_DESIGNER = this;

    $("#version").change(function(e){
        APIDesigner().api_doc.info.version = $(this).val();
        // We do not need the version anymore. With the new plugable version strategy the context will have the version
        APIDesigner().baseURLValue = "http://localhost:8280/"+$("#context").val().replace("/","")});
    $("#context").change(function(e){ APIDesigner().baseURLValue = "http://localhost:8280/"+$(this).val().replace("/","")});
    $("#name").change(function(e){ APIDesigner().api_doc.info.title = $(this).val() });
    $("#description").change(function(e){ APIDesigner().api_doc.info.description = $(this).val() });

    this.container.delegate( ".delete_resource", "click", function( event ) {        
        var operations = API_DESIGNER.query($(this).attr('data-path'));
        var operations = operations[0]
        var i = $(this).attr('data-index');
        var pn = $(this).attr('data-path-name');
        var op = $(this).attr('data-operation');        
        jagg.message({content:'Do you want to remove "'+op+' : '+pn+'" resource from list.',type:'confirm',title:"Remove Resource",
        okCallback:function(){
            API_DESIGNER = APIDesigner();
            delete API_DESIGNER.api_doc.paths[pn][op];
            API_DESIGNER.render_resources();
            if(Object.keys(API_DESIGNER.api_doc.paths[pn]).length == 0) {
                delete API_DESIGNER.api_doc.paths[pn];
            }
        }});
        //delete resource if no operations       
    });

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

    this.container.delegate(".add_parameter", "click", function(event){
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

    this.container.delegate(".delete_parameter", "click", function (event) {
        //var elementToDelete =  $(this).parent().parent();
        var deleteData = $(this).attr("data-path");
        var i = $(this).attr("data-index");

        var deleteDataArray = deleteData.split(".");
        var operations = deleteDataArray[2];
        var operation = deleteDataArray[3];
        var paramName = API_DESIGNER.api_doc.paths[operations][operation]['parameters'][i]['name'];

        jagg.message({content: 'Do you want to delete the parameter <strong>' + paramName + '</strong> ?',
            type: 'confirm', title: "Delete Parameter",
            okCallback: function () {
                API_DESIGNER = APIDesigner();
                API_DESIGNER.api_doc.paths[operations][operation]['parameters'].splice(i,1);
                API_DESIGNER.render_resources();
            }});
    });

    this.container.delegate(".delete_scope","click", function(){
        var i = $(this).attr("data-index");
        API_DESIGNER.api_doc['x-wso2-security'].apim['x-wso2-scopes'].splice(i, 1);
        API_DESIGNER.render_scopes();
    });

    this.container.delegate("#define_scopes" ,'click', function(){
        $("#scopeName").val('');
        $("#scopeDescription").val('');
        $("#scopeKey").val('');
        $("#scopeRoles").val('');
        $("#define_scope_modal").modal('show');
    });

    $("#scope_submit").click(function(){
        if(!$("#scope_form").valid()){
            return;
        }     
        var securityDefinitions = {
            "apim":{
                "x-wso2-scopes":[]
            }
        };
        var API_DESIGNER = APIDesigner();        
		var scope = {
			name : $("#scopeName").val(),
			description : $("#scopeDescription").val(),
			key : $("#scopeKey").val(),
			roles : $("#scopeRoles").val()
		};

		jagg.post("/site/blocks/item-design/ajax/add.jag", { action:"validateScope", scope:$("#scopeKey").val()},
			function (result) {
			    if (!result.error) {

				API_DESIGNER.api_doc['x-wso2-security'] = $.extend({}, securityDefinitions, API_DESIGNER.api_doc['x-wso2-security']);

				for (var i = 0; i < API_DESIGNER.api_doc['x-wso2-security'].apim['x-wso2-scopes'].length; i++) {
					if (API_DESIGNER.api_doc['x-wso2-security'].apim['x-wso2-scopes'][i].key === $(
							"#scopeKey").val() || API_DESIGNER.api_doc['x-wso2-security'].apim['x-wso2-scopes'][i].key === $(
							"#scopeName").val()) {
						jagg.message({
							content : "Scope " + $("#scopeKey").val() + " already exists",
							type : "error"
						});
						return;
					}
				}
		      		if (result.isScopeExist == "true") {
					jagg.message({
						content : "Scope " + $("#scopeKey").val() + " already assigned by an API.",
						type : "error"
					});
					return;
				} 
			
				API_DESIGNER.api_doc['x-wso2-security'].apim['x-wso2-scopes'].push(scope);
				$("#define_scope_modal").modal('hide');
				API_DESIGNER.render_scopes();
				API_DESIGNER.render_resources();
			
			    } else {
				jagg.message({
					content : result.message,
					type : "error"
				});
					return;
				}       

		}, "json"); 
	}); 

    $("#swaggerEditor").click(API_DESIGNER.edit_swagger);

    $("#update_swagger").click(API_DESIGNER.update_swagger);

    $("#close_swagger_editor").click(API_DESIGNER.close_swagger_editor);
}
 
APIDesigner.prototype.load_api_document = function(api_document){
    this.api_doc = api_document;
    this.render_resources();
    this.render_scopes();
    $("#version").val(api_document.info.version);
    $("#name").val(api_document.info.title);
    if(api_document.info.description){
    	$("#description").val(api_document.info.description);
    }
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
    var swagger = jQuery.extend(true, {}, this.api_doc);
    for(var pathkey in swagger.paths){
        var path = swagger.paths[pathkey];
        var parameters = path.parameters;
        delete path.parameters;
        for(var verbkey in path){
            var verb = path[verbkey];
            verb.path = pathkey;
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
    var output = Handlebars.partials['designer-resources-template'](context);
    $('#resource_details').html(output);
    $('#resource_details').find('.scope_select').editable({
        emptytext: '+ Scope',
        source: this.get_scopes(),
        success : this.update_elements
    });

    if(typeof(TIERS) !== 'undefined'){
        $('#resource_details').find('.throttling_select').editable({
            emptytext: '+ Throttling',        
            source: TIERS,
            success : this.update_elements
        });
    }   

    if(typeof(AUTH_TYPES) !== 'undefined'){
        $('#resource_details').find('.auth_type_select').editable({
            emptytext: '+ Auth Type',        
            source: AUTH_TYPES,
            autotext: "always",
            display: this.display_element,
            success : this.update_elements
        });
    }

    $('#resource_details').find('.change_summary').editable({
        emptytext: '+ Summary',        
        success : this.update_elements,
        inputclass : 'resource_summary'
    });  
};

APIDesigner.prototype.render_resource = function(container){
    var operation = this.query(container.attr('data-path'));
    var context = jQuery.extend(true, {}, operation[0]);
    context.resource_path = container.attr('data-path');
    var output = Handlebars.partials['designer-resource-template'](context);
    container.html(output);
    container.show();


    if(container.find('.editor').length){
        var textarea = container.find('.editor').ace({ theme: 'textmate', lang: 'javascript' ,fontSize: "10pt"});
        var decorator = container.find('.editor').data('ace');
        var aceInstance = decorator.editor.ace;
        aceInstance.getSession().on('change', function(e) {   
            operation[0]["x-mediation-script"] = aceInstance.getValue();
        });
    }

    container.find('.notes').editable({
        type: 'textarea',
        emptytext: '+ Add Implementation Notes',
        success : this.update_elements
    });
    container.find('.produces').editable({
        type:"text",
        success : this.update_elements
    });
    container.find('.consumes').editable({
        source: content_types,
        success : this.update_elements
    });
    container.find('.param_desc').editable({
        emptytext: '+ Empty',
        success : this.update_elements
    });
    container.find('.param_paramType').editable({
        emptytext: '+ Set Param Type',
        source: [ { value:"body", text:"body" },{ value:"query", text:"query" },{ value:"header", text:"header" }, { value:"formData", value:"formData"} ],
        success : this.update_elements
    });
    container.find('.param_type').editable({
        emptytext: '+ Empty',
        success : this.update_elements
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
        success : this.update_elements_boolean
    });   
};

APIDesigner.prototype.query = function(path){
    return jsonPath(this.api_doc,path);
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
    this.render_resources();
};

APIDesigner.prototype.edit_swagger = function(){
    $("body").addClass("modal-open");
    $(".content-data.row").hide();
    $("#swaggerEditer").append('<iframe id="se-iframe"  style="border:0px;"background: #4a4a4a; width="100%" height="100%"></iframe>');    
    document.getElementById('se-iframe').src = $("#swaggerEditer").attr("editor-url");
    $("#swaggerEditer").fadeIn("fast");
};

APIDesigner.prototype.close_swagger_editor = function(){
    $("body").removeClass("modal-open");
    $(".content-data.row").show();
    $("#se-iframe").remove();
    $("#swaggerEditer").fadeOut("fast");
};

APIDesigner.prototype.update_swagger = function(){
    $("body").removeClass("modal-open");
    $("#se-iframe").remove();
    $("#swaggerEditer").fadeOut("fast");    
    var designer =  APIDesigner();
    var json = jsyaml.safeLoad(designer.yaml);
    designer.load_api_document(json);          
};



$(document).ready(function(){
    $.fn.editable.defaults.mode = 'inline';
    var designer = new APIDesigner();
    designer.load_api_document(api_doc);

    $("#swaggerEditer").on("keyup", function () {
        try {
            jsyaml.load(designer.swagger_editor.getSession().getValue());
            document.getElementById('output_string').innerHTML = "";
        } catch (err) {
            document.getElementById('output_string').innerHTML = err;
            console.log(err);
        }
    });

    $("#clearThumb").on("click", function () {
        $('#apiThumb-container').html('<input type="file" id="apiThumb" class="input-xlarge validateImageFile" name="apiThumb" />');
    });
    $('.toggleContainers .controls').hide();
    $('#import_swagger').attr('disabled','disabled');
    $('.toggleRadios input[type=radio]').click(function(){
        if (($(this).val() == 'swagger_import_file' &&
            typeof jsonFile != 'undefined') ||
            ($(this).val() == 'swagger_import_url' &&
            $('#swagger_import_url').val().length != 0)) {
            $('#import_swagger').removeAttr("disabled");
        } else {
            $('#import_swagger').attr('disabled','disabled');
        }
        $('#swagger_help').hide();
        $('#swagger_file_help').hide();
        $('.toggleContainers .controls').hide();
        $('.toggleRadios input[type=radio]').prop('checked', false);
        $('#' + $(this).val()).parent().fadeIn();
        $(this).prop('checked', true);
    });

    $('#swagger_import_file').change(function (event) {
        var file = event.target.files[0];
        var fileReader = new FileReader();
        fileReader.addEventListener("load", function (event) {
            jsonFile = event.target;
        });
        //Read the text file
        fileReader.readAsText(file);
        $('#import_swagger').removeAttr("disabled");
    });

    $('#swagger_import_url').keyup(function(){
        if($('#swagger_import_url').val().length != 0) {
            $('#import_swagger').removeAttr("disabled");
        } else {
            $('#import_swagger').attr('disabled','disabled');
        }
    });

    $('#import_swagger').click(function () {
        if ($('.toggleRadios input[type=radio]:checked').val() == 'swagger_import_file') {

            $('#import_swagger').buttonLoader('start');
            $('#swagger_help').hide();
            $('#swagger_file_help').hide();
            try{
                var data = JSON.parse(jsonFile.result); //swagger file content
                var designer = APIDesigner();
                designer.load_api_document(data);
                $('#import_swagger').buttonLoader('stop');
                $("#swaggerUpload").modal('hide');
            } catch (err){
                $('#swagger_file_help').show();
                $('#import_swagger').buttonLoader('stop');
                $('#fileErrorMsgClose').on('click', function (e) {
                    $('#swagger_file_help').hide();
                });
            }
        } else {
            $('#import_swagger').buttonLoader('start');
            $('#swagger_help').hide();
            $('#swagger_file_help').hide();
            var data = {
                "swagger_url": $("#swagger_import_url").val() // "http://petstore.swagger.wordnik.com/api/api-docs"
            }
            $.get(jagg.site.context + "/site/blocks/item-design/ajax/import.jag", data, function (data) {
                $('#import_swagger').buttonLoader('stop');
                $('#swagger_help').hide();
                $("#swaggerUpload").modal('hide');
                var designer = APIDesigner();
                designer.load_api_document(data);
            }).fail(function (data) {
                $('#swagger_help').show();
                $('#import_swagger').buttonLoader('stop');
                $('#errorMsgClose').on('click', function (e) {
                    $('#swagger_help').hide();
                });
            });
        }
    });

    $('body').on('change',"#resource_url_pattern",function(){
        var re = new RegExp("^/?([a-zA-Z0-9]|-|_)+");
        var arr = re.exec($(this).val());
        if(arr && arr.length)
            $('#inputResource').val(arr[0]);
    });

    var v = $("#design_form").validate({
        contentType : "application/x-www-form-urlencoded;charset=utf-8",
        dataType: "json",
	    onkeyup: false,
        submitHandler: function(form) {
        var designer = APIDesigner();

        if(designer.has_resources() == false){
            jagg.message({
                content:"At least one resource should be specified. Do you want to add a wildcard resource (/*)?" ,
                type:"confirm",
                title:"Resource not specified",
                anotherDialog:true,
                okCallback:function(){
                    var designer = APIDesigner();
                    designer.add_default_resource();
                    $("#design_form").submit();
                }
            });            
            return false;
        }

        $('#swagger').val(JSON.stringify(designer.api_doc));

        $('#'+thisID).buttonLoader('start');

        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){

                $('#'+thisID).buttonLoader('stop');
                if (!responseText.error) {
                    var designer = APIDesigner();
                    designer.saved_api = {};
                    designer.saved_api.name = responseText.data.apiName;
                    designer.saved_api.version = responseText.data.version;
                    designer.saved_api.provider = responseText.data.provider;
                    $( "body" ).trigger( "api_saved" );
                } else {
                    if (responseText.message == "timeout") {
                        if (ssoEnabled) {
                             var currentLoc = window.location.pathname;
                             var queryString=encodeURIComponent(window.location.search);
                             if (currentLoc.indexOf(".jag") >= 0) {
                                 location.href = "login.jag?requestedPage=" + currentLoc + queryString;
                             } else {
                                 location.href = 'site/pages/login.jag?requestedPage=' + currentLoc + queryString;
                             }
                        } else {
                             jagg.showLogin();
                        }
                    } else {
                        jagg.message({content:responseText.message,type:"error"});
                    }
                }
            },
            error: function() {
                $('#'+thisID).buttonLoader('stop');
                jagg.message({content:"Error occurred while updating API",type:"error"});
            },
            dataType: 'json'
        });
        }
    });

    $("#design_form").keypress(function(e){
        $('.tagContainer .bootstrap-tagsinput input').keyup(function(e) {
            var tagName = $(this).val();
            $tag = $(this);

            if(tagName.match(/[^a-zA-Z0-9_ -]/g)){
                $tag.val( $tag.val().replace(/[^a-zA-Z0-9_ -]/g, function(str) {
                        $('.add-tags-error').html('');
                        $('.tags-error').html('The tag "' + tagName + '" contains one or more illegal characters  (~ ! @ #  ; % ^ * + = { } | &lt; &gt;, \' " \\ ) .');
                        return '';
                }));
            }
        });
        $('.tags-error').html('');
        $("#tags").on('itemAdded', function(event) {
             $('.tags-error').html('');
             $('.add-tags-error').html('');
        });
    });

    $('.tagContainer .bootstrap-tagsinput input').blur(function() {
        if($(this).val().length > 0){
            $('.add-tags-error').html('Please press Enter to add the tag.')
            $('.tags-error').html('');
        }
        else if($(this).val().length == 0){
            $('.add-tags-error').html('');
        }
    });

    $('body').on('change','#apiThumb', function() {
        var imageFileSize = this.files[0].size/1024/1024;
        if (imageFileSize > 1){
          $('#error-invalidImageFileSize').modal('show');
          $('#apiThumb-container').html('<input type="file" id="apiThumb" class="input-xlarge validateImageFile" name="apiThumb" />');
        }
    });
});

var thisID;
$('#saveBtn').click(function(e){
    thisID = $(this).attr('id');
});

$('#go_to_implement').click(function(e){
    thisID = $(this).attr('id');
});

function getContextValue() {
    var context = $('#context').val();
    var version = $('#apiVersion').val();

    if (context == "" && version != "") {
        $('#contextForUrl').html("/{context}/" + version);
        $('#contextForUrlDefault').html("/{context}/" + version);
    }
    if (context != "" && version == "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('#contextForUrl').html(context + "/{version}");
        $('#contextForUrlDefault').html(context + "/{version}");
    }
    if (context != "" && version != "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('.contextForUrl').html(context + "/" + version);
    }
    updateContextPattern();
}

function updateContextPattern(){
    var context = $('#context').val();
    var version = $('#version').val();

    if(context != ""){
        if(context.indexOf("{version}") < 0){
            context = context + '/';
            context = context + "{version}";
        }
        $('#resource_url_pattern_refix').text(context);
    }else{
        $('#resource_url_pattern_refix').text("/{context}/{version}/");
    }

    if(version){
        context = context.replace("{version}",version);
        $('#resource_url_pattern_refix').text(context);
    }
}

