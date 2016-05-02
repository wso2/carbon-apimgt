var saveGlobalPolicy = function () {
    if(!validateInputs()){
        return;
    }

    var action = isNewPolicy ? "add" : "update";
    jagg.post("/site/blocks/policy/global/edit/ajax/global-policy-edit.jag", {
            action: action,
            policyName:$('#policyName').val(),
            description:$('#description').val().trim(),
            siddhiQuery:$('#siddhiQuery').val(),
            keyTemplate:$('#keyTemplate').val()

        }, function (result) {
            if (result.error == false) {
                location.href = 'site/pages/global-policy-manage.jag'
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        },
        "json");

};

function validateInputs(){
    //validate name
    var requiredMsg = $('#errorMsgRequired').val();
    var invalidErrorMsg = $('#errorMessageInvalid').val();
    var illegalChars = $('#errorMessageIllegalChar').val();
    var policyName = $('#policyName');
    var policyNameTxt = policyName.val();
    var keyTemplate = $('#keyTemplate');
    var keyTemplateTxt = keyTemplate.val();
    var keyTemplate = $('#keyTemplate');
    var keyTemplateTxt = keyTemplate.val();

    if(!validateInput(policyNameTxt,policyName,requiredMsg)){
        return false;
    }

    if(!validateInputCharactors(policyNameTxt,policyName,illegalChars)){
        return false;
    }

    if(!validateInput(keyTemplateTxt,keyTemplate,requiredMsg)){
        return false;
    }

    formValidated = $('#formValidated').val();
    if(formValidated === 'false'){
        return false;
    }

    return true;
};

function validateInput(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateInputCharactors(text, element, errorMsg){
    var elementId = element.attr('id');
    var illegalChars = /([~!&@#;%^*+={}\|\\<>\"\',])/;
    text = text.trim();
    if(illegalChars.test(text)){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

