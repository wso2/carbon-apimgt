$( document ).ready(function() {

    $("#designNewAPI").click(function(){
        var btn = $(this);
        $(btn).buttonLoader('start');
        $('#designNewAPI-form').ajaxSubmit({
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

    $("#designNewWSAPI").click(function(){
        var btn = $(this);
        $('#designNewWSAPI-form').ajaxSubmit({
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

    $('#swagger-file').change(function () {
        $('.swaggerFileError').hide();
        $('#swagger-file').removeClass('error');
    });

    $('#swagger-file').keyup(function () {
        $('.swaggerFileError').hide();
        $('#swagger-file').removeClass('error');
        if ($('#swagger-file').val().length != 0) {
            $('#startFromExistingAPI').removeAttr("disabled");
        }
    });

    $('#swagger-url').change(function () {
        $('.swaggerUrlError').hide();
        $('#swagger-url').removeClass('error');
    });

    $('#swagger-url').keyup(function () {
        $('.swaggerUrlError').hide();
        $('#swagger-url').removeClass('error');
        if ($('#swagger-url').val().length != 0) {
            $('#startFromExistingAPI').removeAttr("disabled");
        }
    });

    $("#startFromExistingAPI").click(function(){
        var importDefinition = $("input[name=import-definition]:checked").val();
        var swaggerUrl = $('#swagger-url').val();
        var swaggerFile = $('#swagger-file').val();
        if (importDefinition == "swagger-file") {
            if (swaggerFile.trim() == "") {
                $('#swagger-file').addClass('error');
                $('.swaggerFileError').show();
                return;
            }
        } else {
            if (swaggerUrl.trim() == "") {
                $('#swagger-url').addClass('error');
                $('.swaggerUrlError').show();
                return;
            }
        }

        var btn = $(this);
        $(btn).buttonLoader('start');
        $('#startFromExistingAPI-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                $(btn).buttonLoader('stop');
                if (!responseText.error) {
                    window.location = jagg.site.context + "/design"
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
                }                
            }   ,error: function() {
                $(btn).buttonLoader('stop');
                jagg.message({content:"Error occurred while importing swagger URL",type:"error"});

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
        if($('#wsdl-url').val().length != 0) {
            $('#startFromExistingSOAPEndpoint').removeAttr("disabled");
        } else {
            //$('#startFromExistingSOAPEndpoint').attr('disabled','disabled');
        }
    });

    $("#startFromExistingSOAPEndpoint").click(function(){
        var wsdlURL = $('#wsdl-url').val();
        if (wsdlURL.trim() == "" || wsdlURL.toLowerCase().indexOf("wsdl") < 0) {
                $('#wsdl-url').addClass('error');
                $('.wsdlError').show();
                console.log("Wrong endpoint.");
                return;
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


    $('#swagger-url').val('');
    $('#swagger-file').val('');
    $('#wsdl-url').val('');
    //$('#startFromExistingAPI').attr('disabled',true);

    $('.create-options input[type=radio]').click(function(){
       $(this).prop('checked', true);

        $("input#swagger-file:file").change(function (){
           if ($('#swagger-file').val().length != 0) {
               $('#startFromExistingAPI').removeAttr("disabled");
           } else {
               //$('#startFromExistingAPI').attr('disabled','disabled');
           }
        });

        $('#swagger-url').keyup(function(){
            if($('#swagger-url').val().length != 0) {
                $('#startFromExistingAPI').removeAttr("disabled");
            } else {
                //$('#startFromExistingAPI').attr('disabled','disabled');
            }
        });

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
    $('.toggleRadios input[type=radio]').first().click();

    $('input[name=soap-options-pass-thru]').trigger("click");
});
