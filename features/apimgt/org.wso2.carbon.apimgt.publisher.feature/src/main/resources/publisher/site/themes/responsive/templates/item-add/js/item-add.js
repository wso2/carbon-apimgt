$( document ).ready(function() {

    $("#startFromExistingAPI").click(function(){
        var btn = $(this);
        $(btn).buttonLoader('start');
        $('#startFromExistingAPI-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                $(btn).buttonLoader('stop');
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

    $('#wsdl-url').change(function(){
        $('.wsdlError').hide();
        $('#wsdl-url').removeClass('error');
    });

    $('#wsdl-url').keyup(function(){
        $('.wsdlError').hide();
        $('#wsdl-url').removeClass('error');
    });

    $("#startFromExistingSOAPEndpoint").click(function(){
        var wsdlURL = $('#wsdl-url').val();
        if(wsdlURL!=""){
            if (wsdlURL.toLowerCase().indexOf("wsdl") < 0) {
                $('#wsdl-url').addClass('error');
                $('.wsdlError').show();
                console.log("Wrong endpoint.");
                return;
            }
        }

        var btn = $(this);
        $(btn).buttonLoader('start');
        $('#startFromExistingSOAPEndpoint-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                $(btn).buttonLoader('stop');
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
    

    $('.create-api').click(function(){
        $('.create-options').each(function(){
            $(this).removeClass('selected');
        });

        $(this).closest('.create-options').addClass('selected');
        $('#designNewAPI').hide();
        $('.wsdlError').hide();
    });

    $('#create-new-api').click(function(){
        $('#designNewAPI').show();
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

    $('.toggleContainers .controls').hide();
    $('.toggleRadios input[type=radio]').prop('checked', false);
    $('.toggleRadios input[type=radio]').click(function(){
        $('.toggleContainers .controls').hide();
        $('.toggleRadios input[type=radio]').prop('checked', false);
        $('#' + $(this).val()).parent().parent().fadeIn();
        $(this).prop('checked', true);
    });

});
