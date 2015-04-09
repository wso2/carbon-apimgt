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
    

    $('.create-api').click(function(){
        $('.create-options').each(function(){
            $(this).removeClass('selected');
        });

        $(this).closest('.create-options').addClass('selected');
        $('#designNewAPI').hide();
    });

    $('#create-new-api').click(function(){
        $('#designNewAPI').show();
    });

    $('.slideContainer').hide();
    $('.create-options input[type=radio]').click(function(){
        $('.slideContainer').slideUp();
        $('.create-options input[type=radio]').prop('checked', false);
        $($(this).attr('data-target')).slideDown();
        $(this).prop('checked', true);
    });

    $('.toggleRadios input[type=radio]').click(function(){
        $('.toggleContainers .controls').hide();
        $('.toggleRadios input[type=radio]').prop('checked', false);
        $('#' + $(this).val()).closest('div').show();
        $(this).prop('checked', true);
    });

});
