$(document).ready(function(){
    var designer = new APIMangerAPI.APIDesigner();
    designer.set_partials('design');
    var swaggerUrl = caramel.context + "/asts/api/apis/swagger?action=swaggerDoc&provider="+store.publisher.api.provider+"&name="+store.publisher.api.name+ "&version="+store.publisher.api.version;
    // $.fn.editable.defaults.mode = 'inline';
    if(store.publisher.api.name != ""){
     $.get(swaggerUrl , function( data ) {
            designer.load_api_document(data.data);
            designer.render_resources();
            $("#swaggerUpload").modal('hide');
     });
     } else if(store.publisher.swaggerAvailable) {
        var sessionSwaggerUrl = caramel.context + "/asts/api/apis/swagger?action=sessionSwaggerDoc";
        $.get(sessionSwaggerUrl , function( data ) {
            designer.load_api_document(data.data);
            designer.render_resources();
            $("#swaggerUpload").modal('hide');
        });
    }

    //If API is not yet created api save trigger is registered in here
    if(store.publisher.api.name == "") {
        $("body").on("api_saved" , function(e){
            location.href = caramel.context+"/asts/api/design/"+designer.saved_api.id+"?name="+designer.saved_api.name+"&version="+designer.saved_api.version+"&provider="+designer.saved_api.provider;
        });
    }

    var v = $("#form-asset-create").validate({
                                                 contentType : "application/x-www-form-urlencoded;charset=utf-8",
                                                 dataType: "json",
                                                 onkeyup: false,
                                                 submitHandler: function(form) {
                                                     if(designer.has_resources() == false && !$('#wsdl').val()){
                                                         /* jagg.message({
                                                          content:"At least one resource should be specified. Do you want to add a wildcard resource (/*)." ,
                                                          type:"confirm",
                                                          title:"Resource not specified",
                                                          anotherDialog:true,
                                                          okCallback:function(){
                                                          var designer = APIDesigner();
                                                          designer.add_default_resource();
                                                          $("#design_form").submit();
                                                          }
                                                          });*/
                                                          BootstrapDialog.confirm('At least one resource should be specified. Do you want to add a wildcard resource (/*).', function(result){
                                                          if(result) {
                                                          var designer = new APIMangerAPI.APIDesigner();
                                                          designer.add_default_resource();
                                                          $("#form-asset-create").submit();
                                                          }
                                                          });
                                                         return false;
                                                     }

                                                     $('#swagger').val(JSON.stringify(designer.api_doc));
                                                     $('#saveMessage').show();
                                                     $('#saveButtons').hide();
                                                     $(form).ajaxSubmit({
                                                                            success:function(responseText, statusText, xhr, $form){
                                                                                $('#saveMessage').hide();
                                                                                $('#saveButtons').show();
                                                                                if (responseText.data) {
                                                                                    designer.saved_api = {};
                                                                                    designer.saved_api.name = responseText.data.name;
                                                                                    designer.saved_api.version = responseText.data.version;
                                                                                    designer.saved_api.provider = responseText.data.provider;
                                                                                    designer.saved_api.id = responseText.data.id;
                                                                                    $( "body" ).trigger( "api_saved" );
                                                                                } else {
                                                                                    if (responseText.message == "timeout") {
                                                                                        if (ssoEnabled) {
                                                                                            var currentLoc = window.location.pathname;
                                                                                            if (currentLoc.indexOf(".jag") >= 0) {
                                                                                                location.href = "index.jag";
                                                                                            } else {
                                                                                                location.href = 'site/pages/index.jag';
                                                                                            }
                                                                                        } else {
                                                                                            //jagg.showLogin();
                                                                                        }
                                                                                    } else {
                                                                                        //jagg.message({content:responseText.message,type:"error"});
                                                                                    }
                                                                                }
                                                                            }, 
                                                                            error: function (data) {
                                                                            BootstrapDialog.show({
                                                                            type: BootstrapDialog.TYPE_DANGER,
                                                                            title: 'Error',
                                                                            message: 'Error while creating the API.',
                                                                            buttons: [{
                                                                            label: 'OK',
                                                                            action: function(dialogRef){
                                                                            dialogRef.close();
                                                                            }
                                                                            }]             
                                                                            });  
                                                                            },

                                                                            dataType: 'json'
                                                                        });
                                                 }
                                             });
    $('#visibility').trigger('change');

    $('#go_to_implement').click(function(e){
        //TODO
        $("body").unbind("api_saved");
        $("body").on("api_saved" , function(e){
        if(store.publisher.api.id!=""){
        location.href = caramel.context + "/asts/api/implement/"+store.publisher.api.id;
        }else{
        location.href = caramel.context + "/asts/api/implement/"+designer.saved_api.id;
        }
        });
        $("#form-asset-create").submit();
    });
});

function getContextValue() {
    var context = $('#context').val();
    var version = $('#apiVersion').val();

    if (context == "" && version != "") {
        $('#contextForUrl').html("/{context}/" + version);
        $('#contextForUrlDefault').html("/{context}/" + version);
    }
    if (context != "" && version == "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('#contextForUrl').html(context + "/{version}");
        $('#contextForUrlDefault').html(context + "/{version}");
    }
    if (context != "" && version != "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('.contextForUrl').html(context + "/" + version);
    }
}
