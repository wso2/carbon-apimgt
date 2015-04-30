$(function () {

    var swaggerUrl = caramel.context + "/asts/api/apis/swagger?provider=+"+store.publisher.api.provider+"&name="+store.publisher.api.name+ "&version="+store.publisher.api.version;
    $( document ).ready(function() {
    $.ajaxSetup({
                    contentType: "application/x-www-form-urlencoded; charset=utf-8"
                });


    $.get(swaggerUrl , function( data ) {
            var data = jQuery.parseJSON(data);
            var designer =new  APIMangerAPI.APIDesigner();
            designer.load_api_document(data.data);
            designer.set_default_management_values();
             designer.render_resources();
            $("#swaggerUpload").modal('hide');
     });


    $('#publish_api').click(function(e){
        $("body").on("api_saved", publish_api);
        $this = $(this).attr('id');
        $("#manage_form").submit();
    });

    $('#save_api').click(function(e){
        $this=$(this).attr('id');
    });


    $('#responseCache').change(function(){
        var cache = $('#responseCache').find(":selected").val();
        if(cache == "enabled"){
            $('#cacheTimeout').show();
        }
        else{
            $('#cacheTimeout').hide();
        }
    });

    $('input').on('change', function() {
        var values = $('input:checked.env').map(function() {
            return this.value;
        }).get();
        if(values==""){
            values="none";
        }
        $('#environments').val(values.toString());
    });

    function doGatewayAction() {
        var type=$("#retryType").val();
        if(type=="manage"){
            $("#environmentsRetry-modal").modal('hide');
            $( "body" ).trigger( "api_saved" );
            location.href = "";//TODO
        }else{
            location.href = "";//TODO
        }
    }
});
});
