var inSequencesLoaded = false;

//hack to validate tiers
function validate_tiers(){
    var selectedValues = $('#tier').val();
    if(selectedValues && selectedValues.length > 0){
        $("button.multiselect").removeClass('error-multiselect');
        $("#tier_error").remove();
        return true;
    }
    //set error
    $("button.multiselect").addClass('error-multiselect').after('<label id="tier_error" class="error" for="tenants" generated="true" style="display: block;">This field is required.</label>').focus();
    return false;
}

function validate_subscription(){
    var subscriptionType = $('#subscriptions').val();
    if(subscriptionType == 'specific_tenants'){
        var tenants = $('#tenants').val().trim();
        $("#subscriptions_error").remove();
        if(tenants.length > 0){
            return true;
        }
        //set error
        $("#tenants").after('<label id="subscriptions_error" class="error" for="tenants" generated="true" style="display: block;">This field is required.</label>').focus();
        return false;
    } else {
        return true;
    }
}

$(document).ready(function(){

    $('.multiselect').multiselect();

    $('#tier').change(validate_tiers);
    $('#transport_http').change(validate_Transports);
    $('#transport_https').change(validate_Transports);

    $("#manage_form").submit(function (e) {
      e.preventDefault();
    });

    $('#subscriptions').change(function(e){
        var subscription = $('#subscriptions').find(":selected").val();
        if (subscription == "current_tenant" || subscription == "all_tenants"){
            $('#tenantsDiv').hide();
        } else {
            $('#tenantsDiv').show();
        }
    });
    
    $('.default_version_check').change(function(){
        if($(this).is(":checked")){
            $(default_version_checked).val($(this).val());
        }else{
            $(default_version_checked).val("");
        }
    });


    $("select[name='tier']").change(function() {
            // multipleValues will be an array
            var multipleValues = $(this).val() || [];
            var countLength = $('#tiersCollection').length;
            if (countLength == 0) {

                $('<input>').attr('type', 'hidden')
                        .attr('name', 'tiersCollection')
                        .attr('id', 'tiersCollection')
                        .attr('value', multipleValues)
                        .appendTo('#manage_form');
            } else {
                $('#tiersCollection').attr('value', multipleValues);

            }

        });

    if ( $("#toggleSequence").attr('checked') ) {
	$('#toggleSequence').parent().next().show();
    } 
    else {
	$('#toggleSequence').parent().next().hide();
    }
    
    if( $("#toggleThrottle").attr('checked') ) {
    $('#toggleThrottle').parent().next().show();
    } 
    else {
    $('#toggleThrottle').parent().next().hide();
    }

});

$('.js_hidden_section_title').click(function(){
        var $next = $(this).next();
        var $i = $('i',this);
        if($next.is(":visible")){
            $next.hide();
            $i.removeClass('icon-chevron-down');
            $i.addClass('icon-chevron-right');
        }else{
            $next.show();
            $i.removeClass('icon-chevron-right');
            $i.addClass('icon-chevron-down');
        }
    });

$("#toggleThrottle").change(function(e){
    if($(this).is(":checked")){
        $(this).parent().next().show();
    }else{
        $(this).parent().next().hide();
    }
});

function validate_Transports(){
    var checkedHttpTransport=$('#transport_http').is(":checked");
    var checkedHttpsTransport=$('#transport_https').is(":checked");
    $("#transport_error").remove();
    if(checkedHttpTransport || checkedHttpsTransport){
    $( "div.checkbox" ).removeClass('error-multiselect');
        return true;
    }
    $( "div.checkbox" ).addClass('error-multiselect').after('<div id="transport_error" class="error">This field is required.</div>');
    return false;
}