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

var requestMetaData = function(data={}) {
    var access_key_header = "Bearer " + getCookie("WSO2_AM_TOKEN_1"); //TODO: tmkb Depend on result from
    // promise
    var request_meta = {
        clientAuthorizations: {
            api_key: new SwaggerClient.ApiKeyAuthorization("Authorization", access_key_header, "header")
        },
        responseContentType: data['Content-Type'] || "application/json"
    };
    return request_meta;
};

var getCookie = function(name) {
    var value = "; " + document.cookie;
    var parts = value.split("; " + name + "=");
    if (parts.length == 2) return parts.pop().split(";").shift();
};

var setAuthHeader = function(swaggerClient) {
    var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    swaggerClient.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
    swaggerClient.setHost(location.host);

};

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
