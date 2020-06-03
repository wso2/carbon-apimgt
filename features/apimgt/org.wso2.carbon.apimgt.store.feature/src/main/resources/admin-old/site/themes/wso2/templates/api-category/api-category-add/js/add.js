var attributeCount = 0;

var addAPICategory = function () {
    if (!validateInputs()) {
        return;
    }

    $('#add-api-category-btn').buttonLoader('start');
    jagg.post("/site/blocks/api-category/api-category-add/ajax/api-category-edit.jag", {
        action: $('#action').val(),
        categoryName: $('#categoryName').val().trim(),
        uuid: $('#uuid').val(),
        description: htmlEscape($('#description').val()),
    }, function (result) {
        if (result.error == false) {
            location.href = 'api-category-list'
        } else {
            $('#add-api-category-btn').buttonLoader('stop');
            jagg.message({ content: result.message, type: "error" });
        }
    },
        "json");

};

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

function validateInputs() {
    //validate name
    var requiredMsg = $('#errorMsgRequired').val();
    var invalidErrorMsg = $('#errorMessageInvalid').val();
    var illegalChars = $('#errorMessageIllegalChar').val();
    var errorHasSpacesMsg = $('#errorMessageSpaces').val();
    var lengthIsTooLong = $('#errorMessageTooLengthy').val();
    var categoryNameAlreadyExists = $('#categoryNameExists').val();
    var categoryName = $('#categoryName');
    var categoryNameTxt = categoryName.val();

    if (!validateInput(categoryNameTxt, categoryName, requiredMsg)) {
        return false;
    }
    if (!validateLength(categoryNameTxt, categoryName, lengthIsTooLong)) {
        return false;
    }
    if (!validateInputCharacters(categoryNameTxt, categoryName, illegalChars)) {
        return false;
    }
    if (!validateForSpaces(categoryNameTxt, categoryName, errorHasSpacesMsg)) {
        return false;
    }
    if (validateForNameAlreadyExists(categoryNameTxt, categoryName, categoryNameAlreadyExists)) {
        return false;
    }
    return true;
}

function validateInput(text, element, errorMsg) {
    var elementId = element.attr('id');
    text = text.trim();
    if (text == "") {
        element.css("border", "1px solid red");
        $('#category' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + errorMsg + '</label>');
        return false;
    } else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateLength(text, element, errorMsg) {
    var elementId = element.attr('id');
    text = text.trim();
    if (text.length > 255) {
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

function validateInputCharacters(text, element, errorMsg) {
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

function validateForSpaces(text, element, errorMsg) {
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

function validateForNameAlreadyExists(text, element, errorMsg) {
    var elementId = element.attr('id');
    var apiCategoryNameExist = false;
    jagg.syncPost("/site/blocks/api-category/api-category-add/ajax/api-category-edit.jag", {
        action: "isAPICategoryNameExists",
        categoryName: $('#categoryName').val().trim(),
        uuid: $('#uuid').val(),
    }, function (result) {
        if (!result.error) {
            apiCategoryNameExist = result.exist;
        }
    });
    if (apiCategoryNameExist) {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >' + errorMsg + '</label>');
        return true;
    } else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return false;
    }
}