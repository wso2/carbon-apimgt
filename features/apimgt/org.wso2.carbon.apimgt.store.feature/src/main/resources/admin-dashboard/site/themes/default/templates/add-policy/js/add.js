var attributeCount = 0;

var addTier = function () {
    if(!validateInputs()){
        return;
    }

    jagg.post("/site/blocks/add-policy/ajax/add-policy.jag", {
        action:"addTier",
        tierName:$('#tierName').val(),
        requestCount:$('#requestCount').val(),
        unitTime:$('#unitTime').val(),
            startingIP:$('#startingIP').val(),
            endingIP:$('#endingIP').val(),
            httpVerb:$('#httpVerb').val()
        }, function (result) {
            if (result.error == false) {
                location.reload(true)
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        },
    "json");
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


function validateNumbersInput(text, element, requiredMsg, invalidErrorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + requiredMsg + '</label>');
        return false;
    }else if(!text.match(/^\d+$/)){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + invalidErrorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateIPAddressInput(text, element, invalidErrorMsg){
    var elementId = element.attr('id');
    text = text.trim();

    if(!text.match(/^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/)){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + invalidErrorMsg +" IP Address "+ '</label>');
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
        var tierName = $('#tierName');
        var tierNameTxt = tierName.val();

        if(!validateInput(tierNameTxt,tierName,requiredMsg)){
            return false;
        }

        if(!validateInputCharactors(tierNameTxt,tierName,illegalChars)){
            return false;
        }

        var requestCount = $('#requestCount');
        var requestCountTxt = requestCount.val();

        if(!validateNumbersInput(requestCountTxt,requestCount,requiredMsg, invalidErrorMsg)){
            return false;
        }

        var unitTime = $('#unitTime');
        var unitTimeTxt = unitTime.val();

        if(!validateNumbersInput(unitTimeTxt,unitTime,requiredMsg, invalidErrorMsg)){
            return false;
        }

        var startingIP = $('#startingIP');
        var startingIPTxt = startingIP.val();

        if(!validateIPAddressInput(startingIPTxt,startingIP, invalidErrorMsg)){
            return false;
        }

        var endingIP = $('#endingIP');
        var endingIPTxt = endingIP.val();

        if(!validateIPAddressInput(endingIPTxt,endingIP, invalidErrorMsg)){
            return false;
        }

        var isInvalidAttribute = false;
            if(isInvalidAttribute){
                return false;
            }
            return true;
    };
