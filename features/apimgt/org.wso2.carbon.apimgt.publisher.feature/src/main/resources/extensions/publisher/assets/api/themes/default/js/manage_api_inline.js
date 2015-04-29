$(function () {
    var swaggerUrl = caramel.context + "/asts/api/apis/swagger?provider=+"+store.publisher.api.provider+"&name="+store.publisher.api.name+ "&version="+store.publisher.api.version;
    $.ajaxSetup({
                    contentType: "application/x-www-form-urlencoded; charset=utf-8"
                });

    $.get(swaggerUrl , function( data ) {
        var data = jQuery.parseJSON(data);
        var designer = APIDesigner();
        designer.load_api_document(data);
        designer.set_default_management_values();
        designer.render_resources__manage_template();
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
});
