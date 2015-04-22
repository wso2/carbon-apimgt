$(document).ready(function(){

    $(".implementation_methods").change(function(event){
        $(".implementation_method").hide();
        $(".implementation_method_"+$(this).val()).show();
    });


    $('#endpointType').on('change',function(){
        var endpointType = $('#endpointType').find(":selected").val();
        if(endpointType == "secured"){
            $('#credentials').show();
        }
        else{
            $('#credentials').hide();
        }
    });
    $('#endpointType').trigger('change');

    /*$("#implement_form").submit(function (e) {
      e.preventDefault();
    });*/


   var previousClicked = "";
    $('.api-implement-type').click(function(){
        $($(this).attr('value')).slideToggle();
        if(previousClicked !="" && previousClicked != $(this).attr('value')){
            $(previousClicked).slideUp();
        }
        previousClicked=$(this).attr('value');
    });


    var v = $("#implement_form").validate({
        submitHandler: function(form) {        
        var designer = APIDesigner();
        APP.update_ep_config();
        $('.swagger').val(JSON.stringify(designer.api_doc));

        $('#'+thisID).addClass('active');

        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
             if (!responseText.error) {
                var designer = APIDesigner();
                designer.saved_api = {};
                designer.saved_api.name = responseText.data.apiName;
                designer.saved_api.version = responseText.data.version;
                designer.saved_api.provider = responseText.data.provider;
                $('#'+thisID).removeClass('active');
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
                     jagg.message({content:responseText.message,type:"error"});
                 }
                 $('#'+thisID).removeClass('active');
             }
            }, dataType: 'json'
        });
        }
    });

    var v = $("#prototype_form").validate({
        submitHandler: function(form) {        
        var designer = APIDesigner();
        APP.update_ep_config();
        $('.swagger').val(JSON.stringify(designer.api_doc));

        $('#'+thisID).addClass('active');

        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
             if (!responseText.error) {
                var designer = APIDesigner();
                designer.saved_api = {};
                designer.saved_api.name = responseText.data.apiName;
                designer.saved_api.version = responseText.data.version;
                designer.saved_api.provider = responseText.data.provider;
                $('#'+thisID).removeClass('active');
                $( "body" ).trigger( "prototype_saved" );                             
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
                     jagg.message({content:responseText.message,type:"error"});
                 }
                 $('#'+thisID).removeClass('active');
             }
            }, dataType: 'json'
        });
        }
    });
    
    $("#prototyped_api").click(function(e){
        $("body").on("prototype_saved", function(e){
            $("body").unbind("prototype_saved");
                var designer = APIDesigner();            
                $.ajax({
                    type: "POST",
                    url: jagg.site.context + "/site/blocks/life-cycles/ajax/life-cycles.jag",
                    data: {
                        action :"updateStatus",
                        name:designer.saved_api.name,
                        version:designer.saved_api.version,
                        provider: designer.saved_api.provider,
                        status: "PROTOTYPED",
                        publishToGateway:true,
                        requireResubscription:true
                    },
                    success: function(responseText){
                        if (!responseText.error) {
                            jagg.message({content:"API deployed as a Prototype.",type:"info"});
                        }else{
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
                                 jagg.message({content:responseText.message,type:"error"});
                             }
                        }
                    },
                    dataType: "json"
                });               
            });
        $("#prototype_form").submit();                        
    });

});

var thisID='';
$('#saveBtn').click(function(e){
    $(this).siblings('button').button('reset');
    thisID = $(this).attr('id');
});

$('#savePrototypeBtn').click(function(e){
    $(this).siblings('button').button('reset');
    thisID = $(this).attr('id');
});

$('#prototyped_api').click(function(e){
    $(this).siblings('button').button('reset');
    thisID = $(this).attr('id');
});

$('#go_to_manage').click(function(e){
    $(this).siblings('button').button('reset');
    thisID = $(this).attr('id');
});
