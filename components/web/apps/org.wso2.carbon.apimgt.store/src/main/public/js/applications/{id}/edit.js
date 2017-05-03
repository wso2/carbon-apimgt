function tierChanged(element) {
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}
var applicationClient;
$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var bearerToken = "Basic YWRtaW46YWRtaW4=";
    var tierList;

    var tierClient = new SwaggerClient({
        url: swaggerURL + "tiers",
        success: function (swaggerData) {
            setAuthHeader(tierClient);
            //Get available tiers
            tierClient["Tier Collection"].get_policies_tierLevel
            ({"tierLevel": "application"},
                function (jsonData) {
                    tierList = jsonData.obj.list;
                    applicationClient = new SwaggerClient({
                        url: swaggerURL + "applications",
                        success: function (swaggerData) {

                            var applicationId = $("#applicationId").val();
                            setAuthHeader(applicationClient);
                            applicationClient["Application (individual)"].get_applications_applicationId
                            ({"applicationId": applicationId},
                                function (jsonData) {

                                    var application = jsonData.obj;
                                    var context = {};
                                    for (var i=0; i < tierList.length; i++) {
                                        if(tierList[i].name == application.name) {
                                            tierList[i].selected = true;
                                        }
                                    }
                                    context.appTiers = tierList;
                                    context.application = application;

                                    $.get('/store/public/components/root/base/templates/applications/editAppContentTemplate.hbs', function (templateData) {
                                        var template = Handlebars.compile(templateData);
                                        // Inject data into template
                                        var theCompiledHtml = template(context);

                                        // Add the compiled html to the page
                                        $('#editAppContent').html(theCompiledHtml)
                                    }, 'html');

                                },
                                function (error) {
                                    console.log('failed with the following: ' + error.statusText);
                                    if(error.status==401){
                                        redirectToLogin(contextPath);
                                    }
                                });
                        }
                    });
                },
                function (error) {
                    console.log('failed with the following: ' + error.statusText);
                });
        }
    });

    $("#appEditForm").validate({
        submitHandler: function (form) {
            updateApplication();
        }
    });

    var updateApplication = function () {
        var application = {};
        var applicationId = $("#applicationId").val();
        application.name = $("#application-name").val();
        application.throttlingTier = $("#appTier").val();
        application.description = $("#description").val();
        application.lifeCycleStatus = $("#status").val();
        setAuthHeader(applicationClient);
        applicationClient["Application (individual)"].put_applications_applicationId
            ({
                "applicationId": applicationId,
                "body": application,
                "Content-Type": "application/json"
            },
            function (jsonData) {
                if (jsonData.status == 202) {
                    noty({
                                text : "Request has been submitted and is now awaiting approval.",
                                type : "alert",
                                dismissQueue: true,
                                layout : "topCenter",
                                theme : 'relax',
                                buttons : [
                                    {addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                                        $noty.close();
                                        window.location = contextPath + jsonData.headers.location;
                                    }
                                    }
                                    
                                ]
                            });
                } else {
                    window.location = contextPath + "/applications/" + jsonData.obj.applicationId;
                }
                
            },
            function (error) {
                noty({
                        text: error.obj.description,
                        type: 'error',
                        dismissQueue: true,
                        modal: true,
                        progressBar: true,
                        timeout: 2000,
                        layout: 'top',
                        theme: 'relax',
                        maxVisible: 10,
                    });
            }
        );
    };
});
