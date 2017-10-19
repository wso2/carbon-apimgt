var api_client;
$(function () {
     api_client = new API();
    api_client.getEndpoints(getEndpointsCallback);
    $('.help_popup').popover({ trigger: "hover" });
    $('#new-api-name').change(fillApiLevelEndpointNames);
    $('#new-api-version').change(fillApiLevelEndpointNames);

});
   function showHideCreateEndpoint(obj){
            var elementName = obj.name;
            var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
            var level = $('input[name='+elementName+']:checked').val();
          var endpoint = {'type':type};
          if(level =="global"){
            $('#create-new-endpoint-'+type).addClass('hidden');
            $('#select-global-endpoint-'+type).removeClass('hidden');
          }else{
            $('#select-global-endpoint-'+type).addClass('hidden');
            $('#create-new-endpoint-'+type).removeClass('hidden');
          }
          $('#endpoint-config-'+type).val(endpoint);
   }

function getEndpointsCallback(response){
    var data = response.obj.list;
    if(data.length == 0){
        $('#no-global-endpoint-message-production').removeClass('hidden');
        $('#no-global-endpoint-message-sandbox').removeClass('hidden');
        $('#global-endpoint-production').addClass('hidden');
        $('#global-endpoint-sandbox').addClass('hidden');
    }else{
        $('#no-global-endpoint-message-production').addClass('hidden');
        $('#no-global-endpoint-message-sandbox').addClass('hidden');
        $('#global-endpoint-production').removeClass('hidden');
        $('#global-endpoint-sandbox').removeClass('hidden');
        for(var i in data){
            var name = data[i].name;
            var id = data[i].id;
            var selectChild = "<option title='"+name+"' data-content='<span><strong>"+name+"</strong><br /></span>' value='"+id+"'>"+name+"</option>";
            $('#global-endpoint-sandbox').append(selectChild);
            $('#global-endpoint-production').append(selectChild);
        }
    }
 }
function fillApiLevelEndpointNames(){
    var apiName = $('#new-api-name').val();
    var version = $('#new-api-version').val();
    $("input[name='endpoint-name']").each(function() {
       var elementName = this.id;
       var type = elementName.substring(elementName.lastIndexOf('-')+1,elementName.length);
       var endpointName =apiName + ' -- ' + version + ' -- ' + type.toUpperCase()+ ' -- Endpoint';
       this.value = endpointName;
    });
}
