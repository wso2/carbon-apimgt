function constructEndpoint(type){
        var endpoint = {};
        var security = {enabled:false};
        var endpointConfig = {};
        var name = $('#endpoint-name-'+type).val();
        if(!validateInput(name,$('#endpoint-name-'+type),"Name Required")){
        return false;
        }
        endpoint.name = name;
        var typeElementName ='#endpoint-type-'+type;
        endpoint.type = $(typeElementName).find(":selected").val();
        var endpointMaxTpsOption = $('input[name=endpoint-maxtps-option-'+type+']:checked').val();
               if(endpointMaxTpsOption !="unlimited"){
               var endpointMaxTps=$('#endpoint-maxtps-'+type).val();
                 if(!validateInput(endpointMaxTps,$('#endpoint-maxtps-'+type),"Max Tps Required")){
                     return false;
                  }else{
                    endpoint.maxTps = endpointMaxTps;
               }
        }
        serviceUrl =  $('#endpoint-url-'+type);
        if(!validateInput(serviceUrl.val(),serviceUrl,"Service Url Required")){
                return false;
           }
        endpointConfig.serviceUrl = serviceUrl.val();
        var endpointSecurity = $('input[name=endpoint-security-'+type+']:checked').val();
        if(endpointSecurity =="true"){
            security.enabled=true;
            var securityType = $('#endpoint-security-type-'+type).find(":selected").val();
            security.type=securityType;
            var username = $('#endpoint-security-username-'+type);
        if(!validateInput(username.val(),username,"Username Required")){
                return false;
           }
        var password = $('#endpoint-security-password-'+type);
        if(!validateInput(password.val(),password,"Password Required")){
                return false;
             }
            security.username = username.val();
            security.password = password.val();
        }else{
            security.enabled=false;
                    }
        endpoint.endpointSecurity=security;
        endpoint.endpointConfig = JSON.stringify(endpointConfig);
        return endpoint;
}
function showHideEndpointSecurity(obj){
    var endpointSecurity = obj.value;
    var elementName = obj.name;
    var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
    if(endpointSecurity =="true"){
          $('#endpoint-security-group-'+type).removeClass('hidden');
    }else{
          $('#endpoint-security-group-'+type).addClass('hidden');
    }
}
function validateInput(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}
   function showHideEndpointMaxTps(obj){
       var endpointSecurity = obj.value;
       var elementName = obj.name;
       var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
       if(endpointSecurity =="unlimited"){
             $('#endpoint-maxtps-group-'+type).addClass('hidden');
             $('#endpoint-maxtps-'+type).val("");
       }else{
             $('#endpoint-maxtps-group-'+type).removeClass('hidden');
       }
   }
function checkEndpointExist(obj){
     var endpointName = obj.value;
     var elementId = obj.id;
     var promised_checkEndpointExist =  api_client.checkEndpointExist(endpointName);
         promised_checkEndpointExist.then(function(response){
     if(response.status == "200"){
                var element  = $('#'+elementId);
                 var errorMsg = "Endpoint Name Already Exist";
                 element.css("border", "1px solid red");
                 $('#label'+elementId).remove();
                 element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
     }
      }).catch(function (error_response) {
             if(error_response.status =="404"){
                             var element  = $('#'+elementId);
                             $('#label'+elementId).remove();
                             element.css("border", "1px solid #cccccc");
             }
         }
 );
}
function populateEndpointDetails(data,type){
var serviceUrl = JSON.parse(data.endpointConfig).serviceUrl;
var endpointSecurity = data.endpointSecurity;
$('#endpoint-name-'+type).val(data.name).attr("readonly", true);
$('#endpoint-type-'+type).val(data.type).change();
$('#endpoint-url-'+type).val(serviceUrl);
if(data.maxTps==0){
$("input[name=endpoint-maxtps-option-"+type+"][value=unlimited]").prop('checked', true);
}else{
$("input[name=endpoint-maxtps-option-"+type+"][value=specific]").prop('checked', true);
$('#endpoint-maxtps-'+type).val(data.maxTps);
$('#endpoint-maxtps-group-'+type).removeClass('hidden');
}
if(endpointSecurity.enabled){
$("input[name=endpoint-security-"+type+"][value=true]").prop('checked', true);
$('#endpoint-security-group-'+type).removeClass('hidden');
$('#endpoint-security-type-'+type).val(endpointSecurity.type).change();
$('#endpoint-security-username-'+type).val(endpointSecurity.username);
$('#endpoint-security-password-'+type).val(endpointSecurity.password);

}else{
$("input[name=endpoint-security-"+type+"][value=false]").prop('checked', true);
}
}