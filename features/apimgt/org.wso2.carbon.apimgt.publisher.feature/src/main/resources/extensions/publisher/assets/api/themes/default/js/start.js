$( document ).ready(function() {

    $("#startFromExistingAPI").click(function(){
        $('#startFromExistingAPI-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                if (!responseText.error) {
                    window.location = jagg.site.context + "/design"
                }else {
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
            }, dataType: 'json'
        });
    });

    $("#startFromExistingSOAPEndpoint").click(function(){
        
        $( "#startFromExistingSOAPEndpoint-form" ).submit();
        /*var wsdl = $("#wsdl-url").val();
        window.location.href = 'design?wsdl=' + wsdl;
        $('#startFromExistingSOAPEndpoint-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                if (!responseText.error) {
                    window.location = jagg.site.context + "/design"
                }else {
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
            }, dataType: 'json'
        });*/
    });
    

    $('.create-api').click(function(){
        $('.create-options').each(function(){
            $(this).removeClass('selected');
        });

        $(this).closest('.create-options').addClass('selected');
        $('.designNewAPIContainer').slideUp();
    });

    $('#create-new-api').click(function(){
        $('.designNewAPIContainer').slideDown();
    });


    $('.create-options input[type=radio]').click(function(){
       $(this).prop('checked', true);

       $('.create-options input[type=radio]').each(function(){
           if(!$(this).is(':checked')){
              $($(this).attr('data-target')).slideUp();
           }
           else {
               $($(this).attr('data-target')).slideToggle();
           }
       });
    });

    $('.toggleRadios input[type=radio]').click(function(){
        $('.toggleContainers .controls').hide();
        $('.toggleRadios input[type=radio]').prop('checked', false);
        $('#' + $(this).val()).closest('div').fadeIn();
        $(this).prop('checked', true);
    });

});
