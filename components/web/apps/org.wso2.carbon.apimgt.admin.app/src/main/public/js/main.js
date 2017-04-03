$(document).ready(function(){

    $('a[rel="popover"]').popover({
        container: 'body',
        html: true,
        trigger:'hover',
        content: function () {
            var clone = $($(this).data('popover-content')).clone(true).removeClass('hide');
            return clone;
        }
    }).click(function(e) {
        e.preventDefault();
    });
});

var redirectToLogin = function (contextPath) {
    var message = "The session has expired" + ".<br/> You will be redirect to the login page ...";
    noty({
        text: message,
        type: 'error',
        dismissQueue: true,
        modal: true,
        progressBar: true,
        timeout: 5000,
        layout: 'top',
        theme: 'relax',
        maxVisible: 10,
        callback: {
            afterClose: function () {
                window.location = loginPageUri;
            },
        }
    });
}
