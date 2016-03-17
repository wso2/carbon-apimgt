var login = function () {
    var name = $("#username").val();
    var pass = $("#pass").val();
    var tenantDomain = $("#tenant").val();
    jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:name, password:pass,tenant:tenantDomain },
              function (result) {
                  if (!result.error) {
                      var current = window.location.pathname;
                      var currentHref=window.location.search;
                      var requestedPage=getParameterByName("requestedPage");
                      var queryParam;
                      if(requestedPage){
                        window.location.href=requestedPage;
                      } else {
                        if(currentHref.indexOf("tenant")>-1){queryParam=currentHref;}
                        else{queryParam='';}
                        if (current.indexOf(".jag") >= 0) {
                            location.href = "index.jag"+queryParam;
                        } else {
                            location.href = 'site/pages/index.jag'+queryParam;
                        }
                      }

                  } else {
                      $('#loginError').show('fast');
                      $('#loginErrorSpan').text(result.message).prepend('<strong>'+i18n.t("errorMsgs.login")+'</strong><br />');
                  }
              }, "json");


};

$(document).ready(
        function() {
            $('#username').focus();
            $('#username').keydown(function(event) {
                if (event.which == 13) {
                    event.preventDefault();
                    login();
                }
            });
            $('#pass').keydown(function(event) {
                if (event.which == 13) {
                    event.preventDefault();
                    login();
                }
            });
        }
        );

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}