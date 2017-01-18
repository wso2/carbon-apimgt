function tierChanged(element) {
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}
$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var bearerToken = "Basic YWRtaW46YWRtaW4=";
    var tierList;

    var tierClient = new SwaggerClient({
        url: swaggerURL + "tiers",
        success: function (swaggerData) {
            tierClient.setBasePath("");
            //Get available tiers
            tierClient.default.tiersTierLevelGet({"tierLevel": "application"},
                function (jsonData) {
                    tierList = jsonData.obj.list;
                },
                function (error) {
                    console.log('failed with the following: ' + error.statusText);
                });
        }
    });


    var applicationClient = new SwaggerClient({
        url: swaggerURL + "applications",
        success: function (swaggerData) {

            applicationClient.setBasePath("");
            var applicationId = $("#applicationId").val();
            applicationClient.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
            applicationClient.default.applicationsApplicationIdGet({"applicationId": applicationId},
                function (jsonData) {

                    var application = jsonData.obj;
                    var context = {};
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

        applicationClient.default.applicationsApplicationIdPut({
                "applicationId":applicationId,
                "body": application,
                "Content-Type": "application/json"
            },
            function (jsonData) {
                window.location = "/store/applications/" + jsonData.obj.applicationId;
            },
            function (error) {
                console.log('Failed with the following: ' + error.statusText);
            }
        );
    };
});