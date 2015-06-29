//var subAPI=metadata.swaggerAPI;
$(function(){
    window.swaggerUi = new SwaggerUi({
                                         url:'/store/asts/api/swagger/'+store.publisher.apiIdentifier.provider+'/'+store.publisher.apiIdentifier.name+'/'+store.publisher.apiIdentifier.version,
                                         dom_id: "swagger-ui-container",
                                         supportedSubmitMethods: ['get', 'post', 'put', 'delete','head'],
                                         onComplete: function(swaggerApi, swaggerUi){
                                             console.log("Loaded SwaggerUI");
                                         },
                                         onFailure: function(data) {
                                             console.log("Unable to Load SwaggerUI");
                                         },
                                         docExpansion: "list",
                                         validatorUrl: null
                                     });

    window.swaggerUi.load();
});
$(document).ready(function(){
    var change_token = function(){
        $(".notoken").hide();
        var option = $("#sub_app_list option:selected");
        var type = $("#key_type").val();
        var key = option.attr("data-"+type);
        if(key == "null"){
            $(".notoken").show("slow");
            $("#access_token").val("");
        }else{
            $("#access_token").val(key);
        }
        $("#access_token").trigger("change");
    };
    var show_environments = function(){
        var type = $("#key_type").val();
        $("#environment_name").children('option').hide();
        $("#environment_name").children("option[class='" + type + "']").show();
        var options = $("#environment_name").children("option[class='"+ type + "']");
        if (options.length <= 1) {
            $("#environment_name").hide();
            $("#OnEnvironment").hide();
        }
        if(options.length > 0){
            $("#OnEnvironment").show();
            $("#environment_name").show();
            options[0].selected=true;
        } else if ($("#environment_name").children("option[class='hybrid']").length > 0) {
            $("#environment_name").children("option[class='hybrid']")[0].selected = true;
        }else{
            $("#environment_name").hide();
            $("#OnEnvironment").hide();
        }
        select_environment();
    };
    var select_environment = function(){
        var selectedEnvironment = $("#environment_name");
        var name =selectedEnvironment.val();
        if (window.swaggerUi.api.url.indexOf("?")!=-1) {
            window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "&envName="+name});
        }else{
            window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "? envName="+name});
        }
        change_token();
    };
    $("#access_token").change(function(){
        var key = $(this).val();
        if(key && key.trim() != "") {
            swaggerUi.api.clientAuthorizations.add("key", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer "+ key, "header"));
        }
    });

    $("#sub_app_list").change(change_token);
    $("#key_type").change(show_environments);
    $("#environment_name").change(select_environment);
    show_environments();


    function checkOnKeyPress(e) {
        if (e.which == 13 ||e.keyCode == 13) {
            return false;
        }
    }
});
