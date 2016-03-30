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

    var corsenablePrototyped;
    var accessControlAllowMethodsPrototyped;
    var enableAllowCredentialsPrototyped;
    if($('#toggleCorsPrototyped').is(":checked")) {
        corsenablePrototyped = true;
    } else {
        corsenablePrototyped = false;
    }
    if($('#allowCredentialsPrototyped').is(":checked")) {
        enableAllowCredentialsPrototyped = true;
    } else {
        enableAllowCredentialsPrototyped = false;
    }
    var allowOriginsPrototyped = $('#accessOriginPrototyped').val();
    var allowHeadersValuesPrototyped = $('#allowHeadersPrototyped').val();
    var allowMethodsValuesPrototyped = $('#allowMethodsPrototyped').val();
    if(allowOriginsPrototyped!=""){
        allowOriginsPrototyped = allowOriginsPrototyped.replace(/,/g,"','");
        allowOriginsPrototyped= "['"+allowOriginsPrototyped+"']";
    }else{
        allowOriginsPrototyped = "[]";
    }
    if(allowHeadersValuesPrototyped!=""){
        allowHeadersValuesPrototyped = allowHeadersValuesPrototyped.replace(/,/g,"','");
        allowHeadersValuesPrototyped= "['"+allowHeadersValuesPrototyped+"']";
    }else{
        allowHeadersValuesPrototyped = "[]";
    }
    if(allowMethodsValuesPrototyped != ""){
        allowMethodsValuesPrototyped = allowMethodsValuesPrototyped.replace(/,/g,"','");
        allowMethodsValuesPrototyped = "['" + allowMethodsValuesPrototyped + "']";
    }else{
        allowMethodsValuesPrototyped = "[]";
    }
    var corsJsonStringPrototyped = "{'corsConfigurationEnabled':"+corsenablePrototyped+",'accessControlAllowOrigins':"+allowOriginsPrototyped+",'accessControlAllowCredentials':"+enableAllowCredentialsPrototyped+",'accessControlAllowHeaders':"+allowHeadersValuesPrototyped+",'accessControlAllowMethods':"+allowMethodsValuesPrototyped+"}";
    $('#corsConfigurationPrototyped').val(corsJsonStringPrototyped);
});
