$(function () {
 $(document).ready(function(){
     $.ajaxSetup({
                     contentType: "application/x-www-form-urlencoded; charset=utf-8"
                 });

     $.get( "<%= jagg.url("/site/blocks/item-design/ajax/add.jag?" + apiUrlId ) %>&action=swagger" , function( data ) {
         var data = jQuery.parseJSON(data);
         var designer = APIDesigner();
         designer.load_api_document(data);
         designer.set_default_management_values();
         designer.render_resources();
         $("#swaggerUpload").modal('hide');
     });

     var publish_api = function(e){
         $.ajax({
                    type: "POST",
                    url: "<%= jagg.url("/site/blocks/life-cycles/ajax/life-cycles.jag") %>",
                    async : false,
                    data: {
                        action :"updateStatus",
                        name:"<%=api.name%>",
                        version:"<%=api.version%>",
                        provider: "<%=api.provider%>",
                        status: "PUBLISHED",
                        publishToGateway:true,
                        requireResubscription:true
                    },
                    success: function(responseText){
                        $("body").unbind('api_saved');
                        if (!responseText.error) {
                            //jagg.message({content:"API Published",type:"info"});
                            location.href = "<%= jagg.url("/info?"+ apiUrlId ) %>";
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
                                    jagg.message({content:responseText.message,type:"error"});
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
                                                if(!validate_Transports()){
                                                    return false;
                                                }
                                                var designer = APIDesigner();
                                                $('#swagger').val(JSON.stringify(designer.api_doc));
                                                $('#'+$this).button('loading');
                                                $(form).ajaxSubmit({
                                                                       success:function(responseText, statusText, xhr, $form) {
                                                                           $('#'+$this).button('reset');
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

     var $this='';
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
});
