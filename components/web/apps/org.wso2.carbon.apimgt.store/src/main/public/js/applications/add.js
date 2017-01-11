var bearerToken = "Basic YWRtaW46YWRtaW4=";

function tierChanged(element) {
    var index = element.selectedIndex;
    var selectedDesc = $("#tierDescriptions").val().split(",")[index];
    $("#tierHelpStr em").text(selectedDesc);
}

$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var tierClient = new SwaggerClient({
        url: swaggerURL + "tiers",
        success: function (swaggerData) {
            tierClient.setBasePath("");
            //Get available tiers
            tierClient.default.tiersTierLevelGet({"tierLevel": "application"},
                function (jsonData) {
                    var tierList = jsonData.obj.list;
                    $.get('/store/public/components/root/base/templates/applications/appTiersTemplate.hbs', function (templateData) {
                        var template = Handlebars.compile(templateData);
                        // Define our data object
                        var context = {
                            "appTiers": tierList
                        };

                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#appTierList').html(theCompiledHtml)
                    }, 'html');

                },
                function (error) {
                    console.log('failed with the following: ' + error.statusText);
                });
        }
    });

    $("#appAddForm").validate({
        submitHandler: function (form) {
            addApplication();
        }
    });

//Add application
    var addApplication = function () {

        var applicationClient = new SwaggerClient({
            url: swaggerURL + "applications",
            success: function (swaggerData) {
                applicationClient.setBasePath("");

                var applicationName = $("#application-name").val();
                var tier = $("#appTier").val();
                var goBack = $("#goBack").val();
                var description = $("#description").val();

                var application = {
                    name: applicationName,
                    throttlingTier: tier,
                    description: description
                };

                applicationClient.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                applicationClient.default.applicationsPost({
                        "body": application,
                        "Content-Type": "application/json"
                    },
                    function (data) {
                        window.location = "/store/applications/" + data.obj.applicationId;
                    },
                    function (error) {
                        alert("Error occurred while adding Application : " + applicationName + " " + error.obj.message);
                    });
            }
        });
    };

    $("#application-name").charCount({
        allowed: 70,
        warning: 50,
        counterText: i18n.t('Characters left: ')
    });
    $("#application-name").val('');

});