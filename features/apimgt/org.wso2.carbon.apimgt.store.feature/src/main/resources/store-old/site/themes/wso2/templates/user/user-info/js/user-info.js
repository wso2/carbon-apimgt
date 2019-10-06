$(document).ready(function() {
    $.validator.addMethod("matchPasswords", function(value) {
		return value == $("#newPassword").val();
	}, "The passwords you entered do not match.");

    $.validator.addMethod('noSpace', function(value, element) {
            return !/\s/g.test(value);
    }, 'The name contains white spaces.');


    $("#change-password").validate({
     submitHandler: function(form) {
        var tenantDomain = document.getElementById('hiddenTenantDomain').value;
        var fullUserName;
        if(tenantDomain == "null" || tenantDomain == "carbon.super") {
            fullUserName = document.getElementById('hiddenUsername').value;
        } else {
            fullUserName = document.getElementById('hiddenUsername').value + "@" 
                    + tenantDomain;
        }

    	jagg.post("/site/blocks/user/user-info/ajax/user-info.jag", {
            action:"changePassword",
            username:fullUserName,
            currentPassword:$('#currentPassword').val(),
            newPassword:$('#newPassword').val()
        }, function (result) {
            if (result.error == false) {
            	$('#logoutForm').submit();
                    jagg.message({content:i18n.t("User password changed successfully. You can now sign in to the API store using the new password."),type:"info",
                        cbk:function() {
                            $('#signUpRedirectForm').submit();
                        }
                    });
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
     }
    });
    
    $("#newPassword").keyup(function() {
        $(this).valid();
    });
    $('#newPassword').focus(function(){
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#newPassword').blur(function(){
        $('#password-help').hide();
        $('.password-meter').hide();
    });
    
});
