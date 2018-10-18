var attributeCount = 0;

var addLabel = function () {
    if (!validateInputs()) {
        return;
    }
    var attributes = getCustomAttributesArray();
    if (attributes != null) {
        $('#add-label-btn').buttonLoader('start');
        jagg.post("/site/blocks/label/label-add/ajax/label-edit.jag", {
            action: $('#action').val(),
            labelName: $('#labelName').val(),
            uuid: $('#uuid').val(),
            description: htmlEscape($('#description').val()),
            attributes: JSON.stringify(attributes)
        }, function (result) {
            if (result.error == false) {
                location.href = 'label-list'
            } else {
                $('#add-label-btn').buttonLoader('stop');
                jagg.message({ content: result.message, type: "error" });
            }
        },
            "json");
    }
};

var getCustomAttributesArray = function () {
    customAttributesArray = new Array();
    var hostValid = "At least one host is required";
    $('#custom-attribute-tbody tr').each(function () {
        var attributeValue = $(this).find('input[name^=attributeValue]').val();
        var attributeObj = {};
        attributeObj.value = attributeValue;
        customAttributesArray.push(attributeObj);
    });
    if (customAttributesArray.length == 0) {
        document.getElementById("mandate-host").innerHTML = hostValid;
        return null;
    }
    return customAttributesArray;
}


function removeCustomAttribute(count) {
    $('#attribute' + count).remove();
}

function htmlEscape(str) {
    if (str == null || str == "") {
        return "";
    }
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function populateCustomerAttributes(attributesList) {
    var attributes = attributesList;
    var tBody = $('#custom-attribute-tbody');
    if (attributes != null) {
        $.each(attributes, function (index, value) {
            ++attributeCount;
            addCustomAttributeInitially(tBody, attributeCount, index, value);
        });
    }
}

function addCustomAttribute(element, count) {
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute' + count + '">' +
        '<td><div class="clear"></div></td>' +
        '<td><input type="text" class="form-control" id="attributeValue' + count + '" name="attributeValue' + count + '" placeholder="Value"/></td>' +
        '<td class="delete_resource_td">&nbsp;&nbsp;<a  id="attributeDelete' + count + '"  href="javascript:removeCustomAttribute(' + count + ')">' +
        '<span class="fw-stack"> <i class="fw fw-delete fw-stack-1x"></i> <i class="fw fw-circle-outline fw-stack-2x"></i></span></td></a></td>' +
        '</tr>'
    );
}

function addCustomAttributeInitially(element, count, name, value) {
    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute' + count + '">' +
        '<td><div class="clear"></div></td>' +
        '<td><input type="text" class="form-control" id="attributeValue' + count + '" name="attributeValue' + count + '" /></td>' +
        '<td class="delete_resource_td ">&nbsp;&nbsp;<a  id="attributeDelete' + count + '" href="javascript:removeCustomAttribute(' + count + ');">' +
        '<span class="fw-stack"> <i class="fw fw-delete fw-stack-1x"></i> <i class="fw fw-circle-outline fw-stack-2x"></i></span></td>' +
        '</tr>'
    );
    $('#attributeValue' + count).val(value);
}

$(document).ready(function () {
    $('#add-attribute-btn').on('click', function () {
        document.getElementById("mandate-host").innerHTML = "";
        ++attributeCount;
        var tBody = $('#custom-attribute-tbody');
        addCustomAttribute(tBody, attributeCount);
    });

});

function validateInput(text, element, errorMsg) {
    var elementId = element.attr('id');
    text = text.trim();
    if (text == "") {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + errorMsg + '</label>');
        return false;
    } else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateInputCharactors(text, element, errorMsg) {
    var elementId = element.attr('id');
    var illegalChars = /([~!&@#;%^*+={}$\|\\<>\"\',])/;
    text = text.trim();
    if (illegalChars.test(text)) {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + errorMsg + '</label>');
        return false;
    } else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateAttributesInput(text, element, requiredMsg, invalidErrorMsg) {
    var elementId = element.attr('id');
    text = text.trim();
    var illegalChars = /([~!&@#;%^*+={}\|\\<>\"\',])/;
    if (text == "") {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + requiredMsg + '</label>');
        return false;
    } else if (text.match(illegalChars)) {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + invalidErrorMsg + '</label>');
        return false;
    } else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateInputs() {
    //validate name
    var requiredMsg = $('#errorMsgRequired').val();
    var invalidErrorMsg = $('#errorMessageInvalid').val();
    var illegalChars = $('#errorMessageIllegalChar').val();
    var errorHasSpacesMsg = $('#errorMessageSpaces').val();
    var labelName = $('#labelName');
    var labelNameTxt = labelName.val();

    if (!validateInput(labelNameTxt, labelName, requiredMsg)) {
        return false;
    }
    if (!validateInputCharactors(labelNameTxt, labelName, illegalChars)) {
        return false;
    }
    if (!validateForSpaces(labelNameTxt, labelName, errorHasSpacesMsg)) {
        return false;
    }
    var isInvalidAttribute = false;
    $('#custom-attribute-tbody tr').each(function () {
        var attributeValueElement = $(this).find('input[name^=attributeValue]');
        var attributeValue = attributeValueElement.val();
        if (!validateAttributesInput(attributeValue, attributeValueElement, requiredMsg, invalidErrorMsg)) {
            isInvalidAttribute = true;
            return false;
        }
    });
    if (isInvalidAttribute) {
        return false;
    }
    return true;
}

function validateForSpaces(text, element, errorMsg) {
    var elementId = element.attr('id');
    text = text.trim();
    if(text.indexOf(' ') >= 0) {
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
