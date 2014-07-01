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

$(document).ready(function(){

    $('.multiselect').multiselect();

    $('#tier').change(validate_tiers);

    $("#manage_form").submit(function (e) {
      e.preventDefault();
    });

    $('#subscriptions').change(function(e){
        var subscription = $('#subscriptions').find(":selected").val();
        if (subscription == "current_tenant" || subscription == "all_tenants"){
            $('#tennatsDiv').hide();
        } else {
            $('#tennatsDiv').show();
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


    var v = $("#manage_form").validate({
        submitHandler: function(form) {
        if(!validate_tiers()){
            return false;
        }
        var designer = APIDesigner();
        $('#swagger').val(JSON.stringify(designer.api_doc));
        $('#saveMessage').show();
        $('#saveButtons').hide();        
        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
                $('#saveMessage').hide();
                $('#saveButtons').show();                
                if (!responseText.error) {                
                    $( "body" ).trigger( "api_saved" );       
                } else {
                     if (responseText.message == "timeout") {
                         if (ssoEnabled) {
                             var currentLoc = window.location.pathname;
                             if (currentLoc.indexOf(".jag") >= 0) {
                                 location.href = "add.jag";
                             } else {
                                 location.href = 'site/pages/add.jag';
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
        }
    });

    loadInSequences();
    loadOutSequences();
    loadFaultSequences();
});



function loadInSequences() {

    if(inSequencesLoaded){
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", {
        action : "getCustomInSequences"
    },
    function(result) {
        if (!result.error) {
            var arr = [];
            if (result.sequences.length == 0) {
                var msg = "No defined sequences";
                $('<input>').
                attr('type', 'hidden').
                attr('name', 'inSeq').
                attr('id', 'inSeq').
                attr('value', msg).
                appendTo('#manage_form');
            } else {
                for ( var j = 0; j < result.sequences.length; j++) {
                    arr.push(result.sequences[j]);
                }
                for ( var i = 0; i < arr.length; i++) {
                    if(result.sequences[i] == insequence){
                        $('#inSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                    }else{
                        $('#inSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                    }
                    $('<input>').
                    attr('type', 'hidden').
                    attr('name', 'inSeq').
                    attr('id', 'inSeq').
                    attr('value', result.sequences[i]).
                    appendTo('#manage_form');
 
                }
            }
            inSequencesLoaded = true;
        }
    }, "json");
}
 
function loadOutSequences() {

    if(outSequencesLoaded){
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", {
        action : "getCustomOutSequences"
    },
            function(result) {
                if (!result.error) {
                    var arr = [];
                    if (result.sequences.length == 0) {
                        var msg = "No defined sequences";
                        $('<input>').
                        attr('type', 'hidden').
                        attr('name', 'outSeq').
                        attr('id', 'outSeq').
                        attr('value', msg).
                        appendTo('#manage_form');
                    }else {
                        for ( var j = 0; j < result.sequences.length; j++) {
                            arr.push(result.sequences[j]);
                        }
                        for(var i=0; i<arr.length; i++){
                            if(result.sequences[i] == outsequence){
                                $('#outSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                            }
                            else{
                                $('#outSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                            }
                            $('<input>').
                            attr('type', 'hidden').
                            attr('name', 'outSeq').
                            attr('id', 'outSeq').
                            attr('value', result.sequences[i]).
                            appendTo('#manage_form');

                        }
                    }
                    outSequencesLoaded = true;
                }
            }, "json");
}

function loadFaultSequences() {

    if(faultSequencesLoaded){
        return;
    }

    jagg.post("/site/blocks/item-add/ajax/add.jag", {
        action : "getCustomFaultSequences"
    },
            function(result) {
                if (!result.error) {
                    var arr = [];
                    if (result.sequences.length == 0) {
                        var msg = "No defined sequences";
                        $('<input>').
                        attr('type', 'hidden').
                        attr('name', 'faultSeq').
                        attr('id', 'faultSeq').
                        attr('value', msg).
                        appendTo('#manage_form');
                    }else {
                        for ( var j = 0; j < result.sequences.length; j++) {
                            arr.push(result.sequences[j]);
                        }
                        for(var i=0; i<arr.length; i++){
                            if(result.sequences[i] == faultsequence){
                                $('#faultSequence').append('<option value="'+result.sequences[i]+'" selected="selected">'+result.sequences[i]+'</option>');
                            }
                            else{
                                $('#faultSequence').append('<option value="'+result.sequences[i]+'">'+result.sequences[i]+'</option>');
                            }
                            $('<input>').
                            attr('type', 'hidden').
                            attr('name', 'faultSeq').
                            attr('id', 'faultSeq').
                            attr('value', result.sequences[i]).
                            appendTo('#manage_form');

                        }
                    }
                    faultSequencesLoaded = true;
                }
            }, "json");
}



$("#toggleSequence").change(function(e){
    if($(this).is(":checked")){
        $(this).parent().next().show();
        loadInSequences();
        loadOutSequences();
        loadFaultSequences();
    }else{
        $(this).parent().next().hide();
        faultsequence=null;
        insequence=null;
        outsequence=null;

        $('#faultSequence').remove();
        $('#inSequence').remove();
        $('#outSequence').remove();

    }
});


