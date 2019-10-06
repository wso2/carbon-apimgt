$(document).ready(function() {
   $.validator.addMethod('validName', function(value, element) {
        var illegalChars = /([~!#$;%^&*+={}\|\\<>\"\'\/,])/;
        return !illegalChars.test(value);
    }, i18n.t('The name contains one or more illegal characters')+' (~ ! # $ ; % ^ & * + = { } | &lt; &gt;, \' / " \\ ) .');

    $.validator.addMethod('validateAt', function (value, element) {
        var validString = /^(?!(.*@){2})/;
        return validString.test(value);
    }, i18n.t('The Name contains more than one @ signs'));

   $.validator.addMethod('validPassword', function(value, element) {
        var pwdregex = /^[\S]{5,30}$/;
        return pwdregex.test(value);
    }, i18n.t('Invalid Password'));
   
   $.validator.addMethod('validEmail', function(value, element) {
       var emailRegex = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
       return emailRegex.test(value);
   }, i18n.t('Invalid email address'));

    $.validator.addMethod('validInput', function(value, element) {
        var illegalChars = /([<>\"\'])/;
        return !illegalChars.test(value);
    }, i18n.t('Input contains one or more illegal characters')+' (& &lt; &gt; \'  " ');

    $("#newUsername").charCount({
        allowed: 30,
        warning: 20,
        counterText: i18n.t('Characters left: ')
    });

});
