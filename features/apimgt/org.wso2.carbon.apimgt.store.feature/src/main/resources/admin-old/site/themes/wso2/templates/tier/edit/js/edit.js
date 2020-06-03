var attributeCount = 0;

var addTier = function () {
    if(!validateInputs()){
        return;
    }
    var attributes =  getCustomAttributesArray();
    jagg.post("/site/blocks/tier/edit/ajax/tier-edit.jag", {
        action:"addTier",
        tierName:$('#tierName').val(),
        requestCount:$('#requestCount').val(),
        unitTime:$('#unitTime').val(),
        description:$('#description').val(),
        stopOnQuotaReach:$('#stopOnQuotaReach').is( ":checked" ),
        tierPlan:$('#tierPlan').val(),
        permissionType:$('#permissionTypes input:radio:checked').val(),
        roles:$('#roles').val(),
        attributes:JSON.stringify(attributes)
        }, function (result) {
            if (result.error == false) {
                location.href = 'site/pages/tier-manage.jag'
            } else {
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
            ++ attributeCount;
            addCustomAttributeInitially(tBody, attributeCount,index, value);
        });
    }
}

function addCustomAttribute(element, count){
    
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
            '<td><div class="clear"></div></td>'+
            '<td><input type="text" class="form-control" id="attributeName'+count+'" name="attributeName'+count+'" placeholder="'+i18n.t('Attribute Name')+'"/></td>'+
            '<td><input type="text" class="form-control" id="attributeValue'+count+'" name="attributeValue'+count+'" placeholder="'+i18n.t('Value')+'"/></td>'+
            '<td class="delete_resource_td "><a  id="attributeDelete'+count+'" href="javascript:removeCustomAttribute('+count+');">' + 
            '<span class="fw-stack"> <i class="fw fw-delete fw-stack-1x"></i> <i class="fw fw-circle-outline fw-stack-2x"></i></span></td>'+
        '</tr>'
        );
}

function addCustomAttributeInitially(element, count, name, value){
    
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
            '<td><div class="clear"></div></td>'+
            '<td><input type="text" class="form-control" id="attributeName'+count+'" name="attributeName'+count+'" readonly/></td>'+
            '<td><input type="text" class="form-control" id="attributeValue'+count+'" name="attributeValue'+count+'" readonly/></td>'+
            '<td class="delete_resource_td "><a  id="attributeDelete'+count+'" href="javascript:removeCustomAttribute('+count+');">' + 
            '<span class="fw-stack"> <i class="fw fw-delete fw-stack-1x"></i> <i class="fw fw-circle-outline fw-stack-2x"></i></span></td>'+
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
