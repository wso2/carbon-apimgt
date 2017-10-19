function disableSubmitButton() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#F9BFBB');
    submitButton.disabled = true;
}

function doSubmit() {
    disableSubmitButton();
    var email = $("#email").val();
    jagg.post("/site/blocks/password-reset/initiate/ajax/initiate.jag", {
            action: "initiatePasswordReset",
            email: email
        },
        function (result) {
            $('#userForm').hide();
            $('#helper_text').hide();
            var response = JSON.parse(result);
            if (!response.error) {
                jagg.message({
                    content: 'Password recovery instructions have been sent to ' + email + '. Please check your email.',
                    type: 'success',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            } else if (response.message.indexOf("Invalid User name") != -1) {
                jagg.message({
                    content: 'No account found with the given email. Please try again with a correct email.',
                    type: 'error',
                    cbk: function () {
                        window.location.href = "initiate.jag";
                    }
                });
            } else {
                jagg.message({
                    content: 'Error occurred while resetting your password. Please try again after few minutes.' +
                    ' If you still have issues, please contact us <a href="mailto:cloud@wso2.com">(cloud@wso2.com)</a>',
                    type: 'error',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            }
        });
}

$(document).ready(function ($) {
    jQuery.validator.setDefaults({
        errorElement: 'span'
    });
    $('#userForm').validate({
        rules: {
            email: {
                required: true
            }
        },
        submitHandler: function (form) {
            doSubmit();
        }
    });
});
