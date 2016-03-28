$('input').on('change', function() {
    var corsenableManaged;
    var accessControlAllowMethodsManaged;
    var enableAllowCredentialsManaged;
    if($('#toggleCorsManaged').is(":checked")) {
        corsenableManaged = true;
    } else {
        corsenableManaged = false;
    }
    if($('#allowCredentialsManaged').is(":checked")) {
        enableAllowCredentialsManaged = true;
    } else {
        enableAllowCredentialsManaged = false;
    }
    var allowOriginsManaged = $('#accessOriginManaged').val();
    var allowHeadersValuesManaged = $('#allowHeadersManaged').val();
    var allowMethodsValuesManaged = $('#allowMethodsManaged').val();
    if(allowOriginsManaged!=""){
        allowOriginsManaged = allowOriginsManaged.replace(/,/g,"','");
        allowOriginsManaged= "['"+allowOriginsManaged+"']";
    }else{
        allowOriginsManaged = "[]";
    }
    if(allowHeadersValuesManaged!=""){
        allowHeadersValuesManaged = allowHeadersValuesManaged.replace(/,/g,"','");
        allowHeadersValuesManaged= "['"+allowHeadersValuesManaged+"']";
    }else{
        allowHeadersValuesManaged = "[]";
    }
    if(allowMethodsValuesManaged != ""){
        allowMethodsValuesManaged = allowMethodsValuesManaged.replace(/,/g,"','");
        allowMethodsValuesManaged = "['" + allowMethodsValuesManaged + "']";
    }else{
        allowMethodsValuesManaged = "[]";
    }
    var corsJsonStringManaged = "{'corsConfigurationEnabled':"+corsenableManaged+",'accessControlAllowOrigins':"+allowOriginsManaged+",'accessControlAllowCredentials':"+enableAllowCredentialsManaged+",'accessControlAllowHeaders':"+allowHeadersValuesManaged+",'accessControlAllowMethods':"+allowMethodsValuesManaged+"}";
    $('#corsConfigurationManaged').val(corsJsonStringManaged);
});
