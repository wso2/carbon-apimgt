var attributeCount = 0;

var addAppPolicy = function () {
    if(!validateInputs()){
        return;
    }
    var attributes =  getCustomAttributesArray();
    $('#add-tier-btn').buttonLoader('start');
    jagg.post("/site/blocks/policy/app/edit/ajax/app-policy-edit.jag", {
        action:$('#policyAction').val(),
        policyName:$('#policyName').val(),
        description:htmlEscape($('#description').val()),
        defaultQuotaPolicy:$('input[name=select-quota-type]:checked').val(),
        defaultRequestCount:$('#defaultRequestCount').val(),
        defaultBandwidth:$('#defaultBandwidth').val(),
        defaultBandwidthUnit:$('#defaultBandwidthUnit').val(),
        defaultUnitTime:$('#defaultUnitTime').val(),
        defaultTimeUnit:$('#defaultTimeUnit').val(),

        stopOnQuotaReach:$('#stopOnQuotaReach').is( ":checked" ),
        tierPlan:$('#tierPlan').val(),
        attributes:JSON.stringify(attributes)
        }, function (result) {
            if (result.error == false) {
                location.href = 'app-policy-manage'
            } else {
                $('#add-tier-btn').buttonLoader('stop');
                jagg.message({content:result.message,type:"error"});
            }
        },
    "json");
};

var getCustomAttributesArray = function(){
    customAttributesArray = new Array();

    $('#custom-attribute-tbody tr').each(function() {
        var attributeName = $(this).find('input[name^=attributeName]').val();
        var attributeValue = $(this).find('input[name^=attributeValue]').val();

        var attributeObj = {};
        attributeObj.name = attributeName;
        attributeObj.value = attributeValue;

        customAttributesArray.push(attributeObj);
    });

    return customAttributesArray;
}

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

function validateAttributesInput(text, element, requiredMsg, invalidErrorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + requiredMsg + '</label>');
        return false;
    }else if(text.match(/^(!?xml|\.+)|^\d+(\d|[A-Za-z]+)?$/)){
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

function removeCustomAttribute(count){
    $('#attribute'+count).remove();
}

function populateCustomerAttributes(attributesList){
    
    var attributes = attributesList;    
    var tBody = $('#custom-attribute-tbody');
        
    if(attributes != null){
        $.each(attributes, function( index, value ) {
            ++attributeCount;
            addCustomAttributeInitially(tBody, attributeCount,index, value);
        });
    }
}

function htmlEscape(str) {
    if(str == null || str == "") {
        return "";
    }
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function addCustomAttribute(element, count){
    
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
            '<td><div class="clear"></div></td>'+
            '<td><input type="text" id="attributeName'+count+'" name="attributeName'+count+'" placeholder="Attribute Name"/></td>'+
            '<td><input type="text" id="attributeValue'+count+'" name="attributeValue'+count+'" placeholder="Value"/></td>'+
            '<td class="delete_resource_td"><a  id="attributeDelete'+count+'"  href="javascript:removeCustomAttribute('+count+')"><i class="icon-trash"></i></a></td>'+
        '</tr>'
        );
}

function addCustomAttributeInitially(element, count, name, value){
    
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
            '<td><div class="clear"></div></td>'+
            '<td><input type="text" id="attributeName'+count+'" name="attributeName'+count+'" readonly/></td>'+
            '<td><input type="text" id="attributeValue'+count+'" name="attributeValue'+count+'" readonly/></td>'+
            '<td class="delete_resource_td "><a  id="attributeDelete'+count+'" href="javascript:removeCustomAttribute('+count+');"><i class="icon-trash"></i></a></td>'+
        '</tr>'
        );
    
    $('#attributeName'+count).val(name);
    $('#attributeValue'+count).val(value);    
}

$(document).ready(function(){
    $('#add-attribute-btn').on('click',function(){
        ++ attributeCount;
        var tBody = $('#custom-attribute-tbody');
        addCustomAttribute(tBody, attributeCount);
    });

});

function showHideDefaultQuotaPolicy(){
    var quotaPolicy = $('input[name=select-quota-type]:checked').val();
    if (quotaPolicy == "requestCount"){
        $('#defaultBandwidthBasedDiv').hide();
    } else{
        $('#defaultBandwidthBasedDiv').show();
    }

    if (quotaPolicy == "bandwidthVolume"){
        $('#defaultRequestCountBasedDiv').hide();
    } else{
        $('#defaultRequestCountBasedDiv').show();
    }

}

function validateInputs(){
   //validate name
   var requiredMsg = $('#errorMsgRequired').val();
   var invalidErrorMsg = $('#errorMessageInvalid').val();
   var illegalChars = $('#errorMessageIllegalChar').val();
   var policyName = $('#policyName');
   var policyNameTxt = policyName.val();
   var errorHasSpacesMsg = $('#errorMessageSpaces').val();

   if(!validateInput(policyNameTxt,policyName,requiredMsg)){
       return false;
   }

   if(!validateInputCharactors(policyNameTxt,policyName,illegalChars)){
       return false;
   }

    if (!validateForSpaces(policyNameTxt, policyName, errorHasSpacesMsg)) {
        return false;
    }

    var defaultQuotaPolicy=$('input[name=select-quota-type]:checked').val();
    var defaultRequestCount = $('#defaultRequestCount');
    var defaultRequestCountTxt = defaultRequestCount.val();
    var defaultUnitTime = $('#defaultUnitTime');
    var defaultUnitTimeTxt = defaultUnitTime.val();
    var defaultBandwidth = $('#defaultBandwidth');
    var defaultBandwidthTxt = defaultBandwidth.val();

    if(defaultQuotaPolicy=="requestCount") {
        if(!validateNumbersInput(defaultRequestCountTxt,defaultRequestCount,requiredMsg, invalidErrorMsg)){
            return false;
        }
    }
    if(defaultQuotaPolicy=="bandwidthVolume"){
        if(!validateNumbersInput(defaultBandwidthTxt,defaultBandwidth,requiredMsg, invalidErrorMsg)){
            return false;
        }
    }
    if(!validateNumbersInput(defaultUnitTimeTxt,defaultUnitTime,requiredMsg, invalidErrorMsg)){
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
    if(text.indexOf(' ') >= 0){
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
