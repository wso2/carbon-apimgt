function doSubmit() {
    disable();
    var username = $("#username").val();
    var password = $("#password").val();
    var confirmationKey = $("#confirmationKey").val();
    jagg.post("/site/blocks/user/change/ajax/user.jag", {
            action: "updatePasswordWithUserInput",
            username: username,
            password: password,
            confirmationKey: confirmationKey
        },
        function (result) {
            $('#userForm').hide();
            $('#helper_text').hide();
            var json = JSON.parse(result.replace(/[\r\n]/g, ""));
            if (!json.error) {
                jagg.message({
                    content: 'You have successfully reset your password. Please log in using your new password.',
                    type: 'success',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            } else {
                jagg.message({
                    content: 'Error occurred while resetting your password. Please try again after few minutes.',
                    type: 'error',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            }
        });
}

function disable() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#F9BFBB');
    submitButton.disabled = true;
}

$(document).ready(function ($) {
    jQuery.validator.setDefaults({
        errorElement: 'span'
    });

    $.validator.addMethod("matchPasswords", function(value) {
        return value == $("#password").val();
    }, i18n.t("The passwords you entered do not match."));

    $('#userForm').validate({
        rules: {
            password: {
                required: true,
                minlength: 8
            }
        },
        messages: {
            password: {
                minlength: "Minimum is 8 characters "
            }
        },

        submitHandler: function (form) {
            doSubmit();
        }
    });
    $("#password").keyup(function () {
        $('#password').valid();
    });
    $('#password').focus(function () {
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#password').blur(function () {
        $('#password-help').hide();
        $('.password-meter').hide();
        $('#password').valid();
    });
});
