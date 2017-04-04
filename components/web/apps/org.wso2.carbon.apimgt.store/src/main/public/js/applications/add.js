var mode = "OVERWRITE";

function tierChanged(element) {
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}

$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');
    _renderTopNavBar();
    _renderApplicationAddPage();

    $(document).on('click', '#application-add-button', function () {
        $("#appAddForm").validate({
            submitHandler: function (form) {
                addApplication();
            }
        });
    });
});

var _renderTopNavBar = function() {
    var mode = "OVERWRITE";
    var data = {};
    data.isApplicationAdd = true;
    //Render Applications listing page
    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.top-navbar", data,
        "top-navbar", mode, {
            onSuccess: function () {

            }, onFailure: function (message, e) {
            }
        });
}

var _renderApplicationAddPage = function(){
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (data) {
            //TODO:Replace this once the tierList retrieval api is implemented
            var list = [];
            setAuthHeader(swaggerClient);
            swaggerClient["Tier Collection"].get_policies_tierLevel
            ({
                    "tierLevel": "application",
                    "Content-Type": "application/json"
                },
                function (data) {
                    for(var i=0; i<data.obj.count;i++) {
                        var tier = {};
                        tier.name = data.obj.list[i].name;
                        tier.description = data.obj.list[i].description;
                        list.push(tier);
                    }
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.application-add", {"list":list},
                        "application-add", mode, {
                            onSuccess: function () {
                            }, onFailure: function (message, e) {
                                console.debug(message);
                            }
                        });
                },
                function (error) {
                    if(error.status==401){
                        redirectToLogin(contextPath);
                    }
                });

        }
    });
};

var addApplication = function () {

    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {

            var applicationName = $("#application-name").val();
            var tier = $("#appTier").val();
            var description = $("#description").val();

            var application = {
                name: applicationName,
                throttlingTier: tier,
                description: description
            };
            setAuthHeader(swaggerClient);
            swaggerClient["Application (individual)"].post_applications({
                    "body": application,
                    "Content-Type": "application/json"
                },
                function (data) {
                	var jsonPayload = data.obj.workflowResponse.jsonPayload;
                    if(goBack == "yes") {
                        noty({
                            text : "Return back to API detail page",
                            type : "alert",
                            dismissQueue: true,
                            layout : "topCenter",
                            theme : 'relax',
                            buttons : [
                                {addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                                    $noty.close();
                                    window.location = "/store/apis/" + apiId; //apiId from sentToClient
                                }
                                },
                                {addClass: 'btn btn-danger', text: 'Cancel', onClick: function ($noty) {
                                    $noty.close();
                                    window.location = "/store/applications/" + data.obj.applicationId;
                                }
                                }
                            ]
                        });
                    } else if (jsonPayload != null && jsonPayload != "") {
                        var jsonResponse = JSON.parse(jsonPayload);
                        noty({
                            text : jsonResponse.redirectConfirmationMsg,
                            type : "alert",
                            dismissQueue: true,
                            layout : "topCenter",
                            theme : 'relax',
                            buttons : [
                                {addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                                    $noty.close();
                                    window.location = jsonResponse.redirectUrl;
                                }
                                },
                                {addClass: 'btn btn-danger', text: 'Cancel', onClick: function ($noty) {
                                    $noty.close();
                                    window.location = "/store/applications/" + data.obj.applicationId;
                                }
                                }
                            ]
                        });
                    } else {
                        window.location = "/store/applications/" + data.obj.applicationId;
                    }                },
                function (error) {
                    if(error.status==401){
                        redirectToLogin(contextPath);
                    }
                });
        }
    });
};


