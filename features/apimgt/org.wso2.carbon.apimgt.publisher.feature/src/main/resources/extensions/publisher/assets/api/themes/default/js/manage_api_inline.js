$(function () {

    var swaggerUrl = caramel.context + "/asts/api/apis/swagger?provider=+"+store.publisher.api.provider+"&name="+store.publisher.api.name+ "&version="+store.publisher.api.version;
    $( document ).ready(function() {
    $.ajaxSetup({
                    contentType: "application/x-www-form-urlencoded; charset=utf-8"
                });


    $.get(swaggerUrl , function( data ) {
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

        var publish_api = function(e){
            $.ajax({
                       type: "POST",
                       url: "",
                       async : false,
                       data: {
                           action :"updateStatus",
                           name:store.publisher.api.name,
                           version:store.publisher.api.version,
                           provider: store.publisher.api.provider,
                           status: "PUBLISHED",
                           publishToGateway:true,
                           requireResubscription:true
                       },
                       success: function(responseText){
                           $("body").unbind('api_saved');
                           if (!responseText.error) {
                               //jagg.message({content:"API Published",type:"info"});
                               location.href = "TODO";
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
                                       jagg.showLogin();
                                   }
                               } else {
                                   var message=responseText.message;
                                   if(message.split("||")[1]=="warning"){
                                       var environmentsFailed=JSON.parse(message.split("||")[0]);
                                       var failedToPublishEnvironments=environmentsFailed.PUBLISHED;
                                       var failedToUnpublishedEnvironments=environmentsFailed.UNPUBLISHED;
                                       var divPublish="",divUnpublished="";
                                       for(i= 0; i< failedToPublishEnvironments.split(",").length;i++){
                                           divPublish+=failedToPublishEnvironments.split(",")[i]+"<br>";
                                       }
                                       for(i= 0; i< failedToUnpublishedEnvironments.split(",").length;i++){
                                           divUnpublished+=failedToUnpublishedEnvironments.split(",")[i]+"<br>";
                                       }
                                       $( "#modal-published-content" ).empty();
                                       $( "#modal-unpublished-content" ).empty();
                                       $( "#modal-published-content" ).append(divPublish);
                                       $( "#modal-unpublished-content" ).append(divUnpublished);
                                       if(failedToPublishEnvironments==""){
                                           $("#modal-published-header").hide();
                                           $("#modal-published-content").hide();
                                       }
                                       if(failedToUnpublishedEnvironments==""){
                                           $("#modal-unpublished-header").hide();
                                           $("#modal-unpublished-content").hide();
                                       }
                                       $("#retryType").val("manage");
                                       $("#environmentsRetry-modal").modal('show');
                                   }
                                   else{
                                       //jagg.message({content:responseText.message,type:"error"});
                                       alert("ERROR");
                                   }
                               }
                           }
                       },
                       dataType: "json"
                   });
        };

        var v = $("#manage_form").validate({
                                               submitHandler: function(form) {
                                                   if(!validate_tiers()){
                                                       return false;
                                                   }
                                                   var designer = APIDesigner();
                                                   $('#swagger').val(JSON.stringify(designer.api_doc));
                                                   $('#saveMessage').show();
                                                   $('#saveButtons').hide();
                                                   $(form).ajaxSubmit({
                                                                          success:function(responseText, statusText, xhr, $form) {
                                                                              $('#saveMessage').hide();
                                                                              $('#saveButtons').show();
                                                                              if (!responseText.error) {
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
                                                                                          jagg.showLogin();
                                                                                      }
                                                                                  } else {
                                                                                      var message=responseText.message;
                                                                                      if(message.split("||")[1]=="warning"){
                                                                                          var environmentsFailed=JSON.parse(message.split("||")[0]);
                                                                                          var failedToPublishEnvironments=environmentsFailed.PUBLISHED;
                                                                                          var failedToUnpublishedEnvironments=environmentsFailed.UNPUBLISHED;
                                                                                          var divPublish="",divUnpublished="";
                                                                                          for(i= 0; i< failedToPublishEnvironments.split(",").length;i++){
                                                                                              divPublish+=failedToPublishEnvironments.split(",")[i]+"<br>";
                                                                                          }
                                                                                          for(i= 0; i< failedToUnpublishedEnvironments.split(",").length;i++){
                                                                                              divUnpublished+=failedToUnpublishedEnvironments.split(",")[i]+"<br>";
                                                                                          }
                                                                                          $( "#modal-published-content" ).empty();
                                                                                          $( "#modal-unpublished-content" ).empty();
                                                                                          $( "#modal-published-content" ).append(divPublish);
                                                                                          $( "#modal-unpublished-content" ).append(divUnpublished);
                                                                                          if(failedToPublishEnvironments==""){
                                                                                              $("#modal-published-header").hide();
                                                                                              $("#modal-published-content").hide();
                                                                                          }
                                                                                          if(failedToUnpublishedEnvironments==""){
                                                                                              $("#modal-unpublished-header").hide();
                                                                                              $("#modal-unpublished-content").hide();
                                                                                          }
                                                                                          $("#retryType").val("lifeCycle");
                                                                                          $("#environmentsRetry-modal").modal('show');
                                                                                      }
                                                                                      else{
                                                                                          jagg.message({content:responseText.message,type:"error"});
                                                                                      }
                                                                                  }
                                                                              }
                                                                          }, dataType: 'json'
                                                                      });
                                               }
                                           });

        $('#publish_api').click(function(e){
            $("body").on("api_saved", publish_api);
            $("#manage_form").submit();
        });
});
});
