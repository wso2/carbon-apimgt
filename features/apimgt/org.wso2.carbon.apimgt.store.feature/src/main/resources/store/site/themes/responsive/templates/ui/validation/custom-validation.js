$(document).ready(function() {
   $.validator.addMethod('validName', function(value, element) {
        var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
        return !illegalChars.test(value);
    }, 'The Name contains one or more illegal characters (~ ! @ # $ ; % ^ * + = { } | &lt; &gt;, \' / " \\ ) .');

   $.validator.addMethod('validPassword', function(value, element) {
        var pwdregex = /^[\S]{5,30}$/;
        return pwdregex.test(value);
    }, 'Invalid Password');
   
   $.validator.addMethod('validEmail', function(value, element) {
       var emailRegex = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
       return emailRegex.test(value);
   }, 'Invalid email address');

    $.validator.addMethod('validInput', function(value, element) {
        var illegalChars = /([<>\"\'])/;
        return !illegalChars.test(value);
    }, 'Input contains one or more illegal characters  (& &lt; &gt; \'  " ');

    $("#newUsername").charCount({
        allowed: 30,
        warning: 20,
        counterText: 'Characters left: '
    });

});
