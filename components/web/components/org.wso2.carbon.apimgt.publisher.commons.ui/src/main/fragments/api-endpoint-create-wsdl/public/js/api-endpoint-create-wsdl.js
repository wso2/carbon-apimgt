var api_client;
$(function () {
    api_client = new API();
    api_client.getEndpoints(getEndpointsCallbackWSDL);
    $('.help_popup').popover({ trigger: "hover" });
    $('#new-api-name').change(fillApiLevelEndpointNamesWSDL);
    $('#new-api-version').change(fillApiLevelEndpointNamesWSDL);
    $('#wsdl-url').focusout(validateWSDL);

});
   function showHideCreateEndpointWSDL(obj){
            var elementName = obj.name;
            var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
            var level = $('input[name='+elementName+']:checked').val();
          var endpoint = {'type':type};
          if(level =="global"){
            $('#create-new-endpoint-'+ type + '-wsdl').addClass('hidden');
            $('#select-global-endpoint-' + type + '-wsdl').removeClass('hidden');
          }else{
            $('#select-global-endpoint-' + type + '-wsdl').addClass('hidden');
            $('#create-new-endpoint-' + type + '-wsdl').removeClass('hidden');
          }
          $('#endpoint-config-' + type + '-wsdl').val(endpoint);
   }

function getEndpointsCallbackWSDL(response){
    var data = response.obj.list;
    if(data.length == 0){
        $('#no-global-endpoint-message-production-wsdl').removeClass('hidden');
        $('#no-global-endpoint-message-sandbox-wsdl').removeClass('hidden');
        $('#global-endpoint-production-wsdl').addClass('hidden');
        $('#global-endpoint-sandbox-wsdl').addClass('hidden');
    }else{
        $('#no-global-endpoint-message-production-wsdl').addClass('hidden');
        $('#no-global-endpoint-message-sandbox-wsdl').addClass('hidden');
        $('#global-endpoint-production-wsdl').removeClass('hidden');
        $('#global-endpoint-sandbox-wsdl').removeClass('hidden');
        for(var i in data){
            var name = data[i].name;
            var id = data[i].id;
            var selectChild = "<option title='"+name+"' data-content='<span><strong>"+name+"</strong><br /></span>' value='"+id+"'>"+name+"</option>";
            $('#global-endpoint-sandbox-wsdl').append(selectChild);
            $('#global-endpoint-production-wsdl').append(selectChild);
        }
    }
 }
function fillApiLevelEndpointNamesWSDL(){
    var apiName = $('#new-api-name').val();
    var version = $('#new-api-version').val();
    $("input[name='endpoint-name']").each(function() {
        var elementName = this.id;
        if (elementName.indexOf("wsdl") != -1) {
            var type = elementName.substring('endpoint-name'.length + 1, elementName.length - "wsdl".length);
            var endpointName = apiName + ' -- ' + version + ' -- ' + type.toUpperCase() + ' -- Endpoint';
            this.value = endpointName;
        }
    });
}

function validateWSDL() {
    var wsdlUrl = $("#wsdl-url").val();
    $("#wsdl-url").parent().find('.wsdl_url_validate_label').remove();
    if (wsdlUrl) {
        api_client.validateWSDL(wsdlUrl, validateWSDLCallback);    
    } else {
        $("#wsdl-url").after('<span class="label label-danger wsdl_url_validate_label"><i class="fw fw-warning icon-white" title="missing Url"></i>Please provide a URL to test.</span>');
    }
}

function validateWSDLCallback (response) {
    if (response.obj.isValid) {
        $("#wsdl-url").after('<span class="label label-success wsdl_url_validate_label"><i class="fw fw-check icon-white" title="success Url"></i>Valid.</span>');
        
        var uniqueEndpointUrls = getUniqueEndpoints(response.obj);
        var ep_options = "";
        uniqueEndpointUrls.forEach(url => {
            ep_options += '<option>' + url + '</option>';
        });

        $('#endpoint-urls-for-production-wsdl').html(ep_options);
        $('#endpoint-urls-for-sandbox-wsdl').html(ep_options);
    } else {
        $("#wsdl-url").after('<span class="label label-danger wsdl_url_validate_label"><i class="fw fw-cancel icon-white" title="invalid Url"></i>Invalid WSDL URL.</span>');
    }
}

function getUniqueEndpoints(response) {
    var endpoints = response.wsdlInfo.endpoints;
    var keys = {}, uniqueUrls = [];
    for(var i = 0, l = endpoints.length; i < l; ++i){
        if(!keys.hasOwnProperty(endpoints[i])) {
            uniqueUrls.push(endpoints[i].location);
            keys[endpoints[i]] = 1;
        }
    }
    return uniqueUrls;
}