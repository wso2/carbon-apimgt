
   function deletePendingTasks(apiName, version, provider){

        jagg.post("/site/blocks/life-cycles/ajax/life-cycles.jag", { action:"deleteTask",provider:provider,name:apiName,version:version },
                   function (result) {
                      if (!result.error) {

                         setTimeout(function(){
                                location = ''
                         },1000)
                      } else {
                        if (result.message == "AuthenticateError") {
                           jagg.showLogin();
                        } else {
                               jagg.message({content:result.message,type:"error"});
                        }
                    }
                  },"json");
    };

    function handleWorkflowRedirection(jsonPayload, apiInfo) {
        var jsonObj = JSON.parse(jsonPayload);

        var additionalParameters = jsonObj.additionalParameters;

        if (jsonObj.redirectUrl != null) {
            if (jsonObj.redirectConfirmationMsg == null) {
                if (additionalParameters != null && Object.keys(additionalParameters).length > 0) {
                    var params = "";
                    for (var key in additionalParameters) {
                        if (params != "") {
                            params = params.concat("&");
                        }
                        if (additionalParameters.hasOwnProperty(key)) {
                            params = params.concat((key.concat("=")).concat(additionalParameters[key]));
                        }
                    }
                    location.href = jsonObj.redirectUrl + "?" + params;
                } else {
                    location.href = jsonObj.redirectUrl;
                }
            } else {
                jagg.message({
                    content: jsonObj.redirectConfirmationMsg,
                    type: "confirm",
                    title: "Redirection",
                    anotherDialog: true,
                    okCallback: function() {

                        if (additionalParameters != null && Object.keys(additionalParameters).length > 0) {
                            var params = "";
                            for (var key in additionalParameters) {
                                if (params != "") {
                                    params = params.concat("&");
                                }
                                if (additionalParameters.hasOwnProperty(key)) {
                                    params = params.concat((key.concat("=")).concat(additionalParameters[key]));
                                }
                            }
                            location.href = jsonObj.redirectUrl + "?" + params;
                        } else {
                            location.href = jsonObj.redirectUrl;
                        }
                    },
                    cancelCallback: function() {
                        deletePendingTasks(apiInfo.name, apiInfo.version, apiInfo.provider);
                    }
                });
            }
        }
    }
    function showWorkflowSubmittedMessage() {

        jagg.message({
            content: "Lifecycle state change request has been submitted. ",
            type: "info",
            title: "Workflow State change",
            anotherDialog: true,
            cbk : function() {
                 location.reload(); 
            }
         });

    }

    function showWorkflowRejectedMessage() {

        jagg.message({
            content: "Your state change request has been rejected. Please contact the API publisher for more information.",
            type: "custom",
            title: "Workflow State change",
            anotherDialog: true

        });
    }

