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


    $('select').selectpicker();


    $('.rating-tooltip-manual').rating({
      extendSymbol: function () {
        var title;
        $(this).tooltip({
          container: 'body',
          placement: 'bottom',
          trigger: 'manual',
          title: function () {
            return title;
          }
        });
        $(this).on('rating.rateenter', function (e, rate) {
          title = rate;
          $(this).tooltip('show');
        })
        .on('rating.rateleave', function () {
          $(this).tooltip('hide');
        });
      }
    });


});

var timestampSkew = 100;

var requestMetaData = function(data={}) {
    var access_key_header = "Bearer " + getCookie("WSO2_AM_TOKEN_1"); //TODO: tmkb Depend on result from
    // promise
    var request_meta = {
        clientAuthorizations: {
            OAuth2Security: new SwaggerClient.ApiKeyAuthorization("Authorization", access_key_header, "header")
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
    refreshTokenOnExpire();
    var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    swaggerClient.clientAuthorizations.add("OAuth2Security", 
        new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
    swaggerClient.setHost(location.host);
};

var refreshTokenOnExpire = function(){
    var currentTimestamp =  Math.floor(Date.now() / 1000);
    var tokenTimestamp = window.localStorage.getItem("expiresIn");
    var rememberMe = (window.localStorage.getItem("rememberMe") == 'true');
    if(rememberMe && (tokenTimestamp - currentTimestamp < timestampSkew)) {
        var bearerToken = "Bearer " + getCookie("WSO2_AM_REFRESH_TOKEN_1");
        var loginPromise = authManager.refresh(bearerToken);
        loginPromise.then(function(data,status,xhr){
            authManager.setAuthStatus(true);
            var expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
            window.localStorage.setItem("expiresIn", expiresIn);
        });
        loginPromise.error(
            function (error) {
                var error_data = JSON.parse(error.responseText);
                var message = "Error while refreshing token" + "<br/> You will be redirect to the login page ..." ;
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
        );
    }
}

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
