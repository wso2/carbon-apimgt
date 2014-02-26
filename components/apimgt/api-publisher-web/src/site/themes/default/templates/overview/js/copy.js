var copyAPIToNewVersion = function (provider) {
    var apiName = $("#overviewAPIName").val();
    var version = $("#overviewAPIVersion").val();
    var newVersion = $("#copy-api #new-version").val();
    jagg.post("/site/blocks/overview/ajax/overview.jag", { action:"createNewAPI", provider:provider,apiName:apiName, version:version, newVersion:newVersion },
              function (result) {
                  if (!result.error) {
                      $("#copy-api #new-version").val('');
                      var current = window.location.pathname;
                      if (current.indexOf(".jag") >= 0) {
                          location.href = "index.jag";
                      } else {
                          location.href = 'site/pages/index.jag';
                      }

                  } else {
                      if (result.message == "AuthenticateError") {
                          jagg.showLogin();
                      } else {
                          jagg.message({content:result.message,type:"error"});
                      }
                  }
              }, "json");

};
$(document).ready(
                 function() {
                     $('#copyApiForm').validate({
                         submitHandler: function(form) {
                             copyAPIToNewVersion(provider)
                         }
                     });
                 }
        );