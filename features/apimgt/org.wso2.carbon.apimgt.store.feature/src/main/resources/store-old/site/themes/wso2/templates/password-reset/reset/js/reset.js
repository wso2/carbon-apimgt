$(document).ready(function ($) {
    generateResponse();
});

function generateResponse() {
    //alert($("#id").attr('value'));
    jagg.post("/site/blocks/password-reset/reset/ajax/reset.jag", {
            action: "verifyPasswordResetConfirmationCode",
            confirm: $("#confirm").attr('value'),
            id: $("#id").attr('value')
        },
        function (result) {
            var json = JSON.parse(result.replace(/[\r\n]/g, ""));
            if (json.error) {
                if (json.message.indexOf("Invalid code") != -1) {
                    jagg.message({
                        content: "You have already clicked the one-time password reset link that was emailed to you. " +
                        "Please try again by generating a new link.",
                        type: 'error',
                        cbk: function () {
                            window.location.href = "initiate.jag";
                        }
                    });
                } else if (json.message.indexOf("Expired code") != -1) {
                    jagg.message({
                        content: "The one-time password reset link has been expired. Please try again by generating " +
                        "a new link.",
                        type: 'error',
                        cbk: function () {
                            window.location.href = "initiate.jag";
                        }
                    });
                } else {
                    jagg.message({
                        content: "You have either already clicked the link that was emailed to you or it must have " +
                        "been expired",
                        type: 'error',
                        cbk: function () {
                            window.location.href = "index.jag";
                        }
                    });
                }
            } else {
                window.location.href = "password-verifier.jag";
            }
        });
}
