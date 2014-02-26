$(document).ready(function() {
    $('#uriTemplate').click(function() {
        $('#resourceTableError').hide('fast');
    });
    //Adding the default row
    $('#resourceRow').clone().addClass('resourceRow').insertAfter($('#resourceRow')).show();
    $('.resourceTemplate',$('#resourceRow').next()).val('/*');
    $('input:checkbox',$('#resourceRow').next()).attr('checked','checked');
    loadTiers($('#resourceRow').get(0));
    loadTiers($('#resourceRow').next().get(0));

    $('#context').change(function() {
        getContextValue();
    });

    $('#version').change(function() {
        getContextValue();
    });

    enableDisableButtons();
    $('#resourceTable tr.resourceRow').each(function() {
    $('input', this).unbind('change');
    $('input:checkbox', this).change(function() {
        createHiddenForm();
        validateResourceTable();
    });

    $('input:text', this).change(function() {
        createHiddenForm();
        validateResourceTable();
    });

    $('select', this).change(function() {
        createHiddenForm();
        validateResourceTable();
    });
});

});

var addResourcesToApi = function () {
    $('#resourceRow').clone().addClass('resourceRow').insertAfter($('#resourceRow')).show();
    enableDisableButtons();
    $('#resourceTable tr.resourceRow').each(function(){
        $('input',this).unbind('change');
        $('input:checkbox',this).change(function(){
            createHiddenForm();
            validateResourceTable();
        });

        $('input:text',this).change(function(){
            createHiddenForm();
            validateResourceTable();
        });

        $('select',this).change(function(){
            createHiddenForm();
            validateResourceTable();
        });
    });
};
var enableDisableButtons = function(){

   $('#resourceTable tr').each(function(index){
        var allRows = $('#resourceTable tr');
        if(index > 1){
            if(index == 2){
                    $('.upButton',this).attr('disabled','disabled');
                    $('.downButton',this).removeAttr('disabled');
            }
            if(index > 2 && allRows.length-1 > index){
                    $('.downButton',this).removeAttr('disabled');
                    $('.upButton',this).removeAttr('disabled');
            }
            if(allRows.length-1 == index){
                $('.deleteButton',this).removeAttr('disabled','disabled');
                if(index != 2){
                    $('.upButton',this).removeAttr('disabled');
                }else {
                    $('.upButton',this).attr('disabled','disabled');
                    $('.deleteButton',this).attr('disabled','disabled');
                }
                $('.downButton',this).attr('disabled','disabled');
            }
        }
    });
};
var moveMe = function(moveButton){
    var action = "move-up";
    if($(moveButton).hasClass('downButton')){
        action = "move-down";
    }

    if(action == "move-up"){
        $(moveButton).parent().parent().insertBefore($(moveButton).parent().parent().prev());
    }
    if(action == "move-down"){
        $(moveButton).parent().parent().insertAfter($(moveButton).parent().parent().next());
    }

    enableDisableButtons();
    createHiddenForm();
    validateResourceTable();
};
var createHiddenForm = function(){
    $('#hiddenFormElements input').remove();

    $('#resourceTable tr').each(function(index){
        var resourcesCount = index - 2;
        var resourceMethodValues = "";
        var resourceMethodAuthValues = "";
        var resourceThrottlingTierValues = "";
        var tr = this;
        //read the checkbox values
        if($('.resource-get',tr).is(':checked')){
            if(resourceMethodValues == ""){resourceMethodValues += "GET"}else{resourceMethodValues += ",GET"}
            var selectedValue = $('.getAuthType',tr).val();
            if(resourceMethodAuthValues == ""){resourceMethodAuthValues += selectedValue }else{resourceMethodAuthValues += ","+selectedValue}
            <!--Throttling-fix-->
            var selectedValueThrottling = $('.getThrottlingTier',tr).val();
            if(resourceThrottlingTierValues == ""){resourceThrottlingTierValues += selectedValueThrottling }else{resourceThrottlingTierValues += ","+selectedValueThrottling}
            <!--Throttling-fix-->
        }
        if($('.resource-put',tr).is(':checked')){
            if(resourceMethodValues == ""){resourceMethodValues += "PUT"}else{resourceMethodValues += ",PUT"}
            var selectedValue = $('.putAuthType',tr).val();
            if(resourceMethodAuthValues == ""){resourceMethodAuthValues += selectedValue }else{resourceMethodAuthValues += ","+selectedValue}
            <!--Throttling-fix-->
            var selectedValueThrottling = $('.putThrottlingTier',tr).val();
            console.log(selectedValueThrottling);
            if(resourceThrottlingTierValues == ""){resourceThrottlingTierValues += selectedValueThrottling }else{resourceThrottlingTierValues += ","+selectedValueThrottling}
            <!--Throttling-fix-->
        }
        if($('.resource-post',tr).is(':checked')){
            if(resourceMethodValues == ""){resourceMethodValues += "POST"}else{resourceMethodValues += ",POST"}
            var selectedValue = $('.postAuthType',tr).val();
            if(resourceMethodAuthValues == ""){resourceMethodAuthValues += selectedValue }else{resourceMethodAuthValues += ","+selectedValue}
            <!--Throttling-fix-->
            var selectedValueThrottling = $('.postThrottlingTier',tr).val();
            console.log(selectedValueThrottling);
            if(resourceThrottlingTierValues == ""){resourceThrottlingTierValues += selectedValueThrottling }else{resourceThrottlingTierValues += ","+selectedValueThrottling}
            <!--Throttling-fix-->
        }
        if($('.resource-delete',tr).is(':checked')){
            if(resourceMethodValues == ""){resourceMethodValues += "DELETE"}else{resourceMethodValues += ",DELETE"}
            var selectedValue = $('.deleteAuthType',tr).val();
            if(resourceMethodAuthValues == ""){resourceMethodAuthValues += selectedValue }else{resourceMethodAuthValues += ","+selectedValue}
            <!--Throttling-fix-->
            var selectedValueThrottling = $('.deleteThrottlingTier',tr).val();
            console.log(selectedValueThrottling);
            if(resourceThrottlingTierValues == ""){resourceThrottlingTierValues += selectedValueThrottling }else{resourceThrottlingTierValues += ","+selectedValueThrottling}
            <!--Throttling-fix-->
        }
        if($('.resource-options',tr).is(':checked')){
            if(resourceMethodValues == ""){resourceMethodValues += "OPTIONS"}else{resourceMethodValues += ",OPTIONS"}
            var selectedValue = $('.optionsAuthType',tr).val();
            if(resourceMethodAuthValues == ""){resourceMethodAuthValues += selectedValue }else{resourceMethodAuthValues += ","+selectedValue}
            <!--Throttling-fix-->
            var selectedValueThrottling = $('.optionsThrottlingTier',tr).val();
            console.log(selectedValueThrottling);
            if(resourceThrottlingTierValues == ""){resourceThrottlingTierValues += selectedValueThrottling }else{resourceThrottlingTierValues += ","+selectedValueThrottling}
            <!--Throttling-fix-->
        }


       if(index > 1){
           $('<input>').attr('type', 'hidden')
                   .attr('name', 'uriTemplate-' + resourcesCount).attr('id', 'uriTemplate-' + resourcesCount).attr('value', $('.resourceTemplate',tr).val())
                   .appendTo('#hiddenFormElements');

           $('<input>').attr('type', 'hidden')
                   .attr('name', 'resourceMethod-' + resourcesCount).attr('id', 'resourceMethod-' + resourcesCount).attr('value', resourceMethodValues)
                   .appendTo('#hiddenFormElements');

           $('<input>').attr('type', 'hidden')
                   .attr('name', 'resourceMethodAuthType-' + resourcesCount).attr('id', 'resourceMethodAuthType-' + resourcesCount).attr('value', resourceMethodAuthValues)
                   .appendTo('#hiddenFormElements');
           <!--Throttling-fix-->
           $('<input>').attr('type', 'hidden')
               .attr('name', 'resourceMethodThrottlingTier-' + resourcesCount).attr('id', 'resourceMethodThrottlingTier-' + resourcesCount).attr('value', resourceThrottlingTierValues)
               .appendTo('#hiddenFormElements');
           <!--Throttling-fix-->
       }
   });

   $('#resourceCount').val($('#resourceTable tr').length-2);
};
var deleteResource = function (deleteButton) {
    var count=$('#resourceTable tr').length;
    //Check whether only one defined resource remains before delete operation
    if(count==3){
        $('#resourceTableError').show('fast');
        $('#resourceTableError').html('Sorry. This row can not be deleted. Atleast one resource entry has to be available.<br />');
        return;
    }
    $('#resourceTableError').hide('fast');
    $(deleteButton).parent().parent().remove();

    enableDisableButtons();
    createHiddenForm();
};

var validateResourceTable = function(){
    var errors = "";

    $('.resourceRow input.resourceTemplate').each(function(){
        var myVal = $(this).val();
        var foundMyVal = 0;
        $('.resourceRow input.resourceTemplate').each(function(){
            if($(this).val()==myVal){
                foundMyVal++;
            }
        });
        if(foundMyVal > 1){
            errors += "URL Pattern has to be unique. <strong>" + myVal + "</strong> has duplicated entries.<br/>";
        }
        if(myVal == ""){
            errors += "URL Pattern can't be empty.<br />";
        }
    });

    var allRowsHas_at_least_one_check = true;
    $('.resourceRow').each(function(){
        var tr = this;
        var noneChecked = true;
        $('input:checkbox',tr).each(function(){
            if($(this).is(":checked")){
                noneChecked = false;
            }
        });

        if(noneChecked){
            allRowsHas_at_least_one_check = false;
        }
    });



    if(!allRowsHas_at_least_one_check){
        errors += "At least one HTTP Verb has to be checked for a resource.<br />";
    }
    console.info(errors);
    if(errors != ""){
        $('#resourceTableError').show('fast');
        $('#resourceTableError').html(errors);
        $('#addNewAPIButton').attr('disabled','disabled');
    }else{
        $('#resourceTableError').hide('fast');
        $('#addNewAPIButton').removeAttr('disabled');
    }
    return errors;
};