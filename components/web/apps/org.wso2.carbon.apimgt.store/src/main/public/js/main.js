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
    window.location = contextPath + "/auth/login";
}
