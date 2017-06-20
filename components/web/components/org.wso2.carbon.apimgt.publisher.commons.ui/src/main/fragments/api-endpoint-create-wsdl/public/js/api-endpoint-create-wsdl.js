var api_client;
$(function () {
    api_client = new API();
    api_client.getEndpoints(getEndpointsCallbackWSDL);
    $('.help_popup').popover({ trigger: "hover" });
    $('#new-api-name').change(fillApiLevelEndpointNamesWSDL);
    $('#new-api-version').change(fillApiLevelEndpointNamesWSDL);

});
   function showHideCreateEndpointWSDL(obj){
            var elementName = obj.name;
            var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
            var level = $('input[name='+elementName+']:checked').val();
          var endpoint = {'type':type};
          if(level =="global"){
            $('#wsdl-create-new-endpoint-'+type).addClass('hidden');
            $('#wsdl-select-global-endpoint-'+type).removeClass('hidden');
          }else{
            $('#wsdl-select-global-endpoint-'+type).addClass('hidden');
            $('#wsdl-create-new-endpoint-'+type).removeClass('hidden');
          }
          $('#endpoint-config-'+type).val(endpoint);
   }

function getEndpointsCallbackWSDL(response){
    var data = response.obj.list;
    if(data.length == 0){
        $('#wsdl-no-global-endpoint-message-production').removeClass('hidden');
        $('#wsdl-no-global-endpoint-message-sandbox').removeClass('hidden');
        $('#wsdl-global-endpoint-production').addClass('hidden');
        $('#wsdl-global-endpoint-sandbox').addClass('hidden');
    }else{
        $('#wsdl-no-global-endpoint-message-production').addClass('hidden');
        $('#wsdl-no-global-endpoint-message-sandbox').addClass('hidden');
        $('#wsdl-global-endpoint-production').removeClass('hidden');
        $('#wsdl-global-endpoint-sandbox').removeClass('hidden');
        for(var i in data){
            var name = data[i].name;
            var id = data[i].id;
            var selectChild = "<option title='"+name+"' data-content='<span><strong>"+name+"</strong><br /></span>' value='"+id+"'>"+name+"</option>";
            $('#wsdl-global-endpoint-sandbox').append(selectChild);
            $('#wsdl-global-endpoint-production').append(selectChild);
        }
    }
 }
function fillApiLevelEndpointNamesWSDL(){
    var apiName = $('#new-api-name').val();
    var version = $('#new-api-version').val();
    $("input[name='endpoint-name']").each(function() {
       var elementName = this.id;
       var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
       var endpointName =apiName + ' -- ' + version + ' -- ' + type.toUpperCase()+ ' -- Endpoint';
       this.value = endpointName;
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