$( document ).ready(function() {
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
