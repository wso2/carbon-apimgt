var changeOwner = function () {
    if (!validateInputs()){
        return;
    }
    $('#add-tier-btn').buttonLoader('start');
    jagg.post("/site/blocks/application-owner/change-owner/ajax/change-owner.jag", {
        action:$('#applicationAction').val(),
        newOwner:$('#applicationOwner').val(),
        oldOwner:$('#oldOwner').val(),
        applicationUuid:$('#applicationUuid').val(),
        applicationName:$('#applicationName').val(),
        }, function (result) {
            if (result.error == false) {
                jagg.message({content:i18n.t("Successfully updated owner of the application "
                + document.getElementById('applicationName').value + " to "
                + document.getElementById('applicationOwner').value),type:"info",cbk : function() {
                    $('#add-tier-btn').buttonLoader('stop');
               }});
            } else {
                $('#add-tier-btn').buttonLoader('stop');
                jagg.message({content:result.message,type:"error"});
            }
        },
    "json");
};

function validateInput(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if (text == "") {
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    } else {
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

function validateInputs(){
    //validate name
    var requiredMsg = $('#errorMsgRequired').val();
    var invalidErrorMsg = $('#errorMessageInvalid').val();
    var illegalChars = $('#errorMessageIllegalChar').val();
    var applicationName = $('#applicationName');
    var applicationNameTxt = applicationName.val();
    var errorHasSpacesMsg = $('#errorMessageSpaces').val();
 
    if (!validateInput(applicationNameTxt,applicationName,requiredMsg)){
        return false;
    }
 
    if (!validateInputCharactors(applicationNameTxt,applicationName,illegalChars)){
        return false;
    }
 
     if (!validateForSpaces(applicationNameTxt, applicationName, errorHasSpacesMsg)) {
         return false;
     }
 
         var isInvalidAttribute = false;
         $('#custom-attribute-tbody tr').each(function() {
                 var attributeElement= $(this).find('input[name^=attributeName]');
                 var attributeValueElement = $(this).find('input[name^=attributeValue]');
 
                 var attributeName = attributeElement.val();
                 var attributeValue = attributeValueElement.val();
 
                 if(!validateAttributesInput(attributeName, attributeElement, requiredMsg, invalidErrorMsg)){
                     isInvalidAttribute = true;
                     return false;
                 }
                 // We do not validate the attribute value input as it can be empty.
             });
             if(isInvalidAttribute){
                 return false;
             }
             return true;
     };
 
 function validateForSpaces(text, element, errorMsg){
     var elementId = element.attr('id');
     text = text.trim();
     if (text.indexOf(' ') >= 0) {
         element.css("border", "1px solid red");
         $('#label'+elementId).remove();
         element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
         return false;
     } else {
         $('#label'+elementId).remove();
         element.css("border", "1px solid #cccccc");
         return true;
     }
 }
 