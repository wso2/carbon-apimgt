$('input').on('change', function() {
    var corsenableManaged;
    var allowOriginsManaged;
    var enableAllowCredentialsManaged;
    if($('#toggleCorsManaged').is(":checked")) {
        corsenableManaged = true;
    } else {
        corsenableManaged = false;
    }

    if($('#toggleallOriginManaged').is(":checked")) {
        allowOriginsManaged = "*";
        enableAllowCredentialsManaged = false;
        $('#allowCredentialsManaged').attr("checked",false);
        $('#allowCredentialsManaged').hide();
        $('.originContainerManaged').hide();
    } else {
        $('#allowCredentialsManaged').show();
        $('#allOriginContainerManaged').hide();
        $('.originContainerManaged').show();
    }

    if (validate_AllowCredentialsManaged()) {
        console.log("adding *");
        $(".credentialContainerManaged").hide();
        $("#allowCredentialsManaged").attr("checked",false);
    }else{
        $(".credentialContainerManaged").show();
    }

    if($('#allowCredentialsManaged').is(":checked")) {
        enableAllowCredentialsManaged = true;
    } else {
        enableAllowCredentialsManaged = false;
    }
    allowOriginsManaged = $('#accessOriginManaged').val();
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
    var allowOriginsPrototyped;
    var enableAllowCredentialsPrototyped;
    if($('#toggleCorsPrototyped').is(":checked")) {
        corsenablePrototyped = true;
    } else {
        corsenablePrototyped = false;
    }

    if($('#toggleallOriginPrototyped').is(":checked")) {
        allowOriginsPrototyped = "*";
        enableAllowCredentialsPrototyped = false;
        $('#allowCredentialsPrototyped').attr("checked",false);
        $('#allowCredentialsPrototyped').hide();
        $('.originContainerPrototyped').hide();
    } else {
        $('#allowCredentialsPrototyped').show();
        $('#allOriginContainerPrototyped').hide();
        $('.originContainerPrototyped').show();
    }

    if (validate_AllowCredentialsPrototyped()) {
        console.log("adding *");
        $(".credentialContainerPrototyped").hide();
        $("#allowCredentialsPrototyped").attr("checked",false);
    }else{
        $(".credentialContainerPrototyped").show();
    }

    if($('#allowCredentialsPrototyped').is(":checked")) {
        enableAllowCredentialsPrototyped = true;
    } else {
        enableAllowCredentialsPrototyped = false;
    }
    allowOriginsPrototyped = $('#accessOriginPrototyped').val();
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

function validate_AllowCredentialsPrototyped(){
    var origins = $('#accessOriginPrototyped').val().split(",");
    var status;
    for (var origin in origins) {
        if (origins[origin] == "*") {
            console.log("in validate *");
            return true;
            break;
        }
    }
}

function validate_AllowCredentialsManaged(){
    var origins = $('#accessOriginManaged').val().split(",");
    var status;
    for (var origin in origins) {
        if (origins[origin] == "*") {
            console.log("in validate *");
            return true;
            break;
        }
    }
}

