$(function() {
    $("#startFromExistingAPI").click(function(){
        $('#startFromExistingAPI-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                if (!responseText.success) {
                    window.location = caramel.context + "/asts/api/design";
                }else {
                    BootstrapDialog.show({
                                             type: BootstrapDialog.TYPE_DANGER,
                                             title: 'Error',
                                             message: responseText.message,
                                             buttons: [{
                                                           label: 'Ok',
                                                           action: function(dialogItself){
                                                               dialogItself.close();
                                                           }
                                                       }]
                                         });
                }                
            }, dataType: 'json'
        });
    });

    $("#startFromExistingSOAPEndpoint").click(function(){
        $( "#startFromExistingSOAPEndpoint-form" ).submit();
        var wsdl = $("#wsdl-url").val();
        window.location.href = 'design?wsdl=' + wsdl;
        $('#startFromExistingSOAPEndpoint-form').ajaxSubmit({
            success:function(responseText, statusText, xhr, $form){
                if (!responseText.success) {
                    window.location = caramel.context + "/asts/api/design";
                }else {
                    BootstrapDialog.show({
                                             type: BootstrapDialog.TYPE_DANGER,
                                             title: 'Error',
                                             message: responseText.message,
                                             buttons: [{
                                                           label: 'Ok',
                                                           action: function(dialogItself){
                                                               dialogItself.close();
                                                           }
                                                       }]
                                         });
                }                
            }, dataType: 'json'
        });
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
