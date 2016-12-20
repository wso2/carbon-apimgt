$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top','0px').addClass('active');

    var bearerToken = "Bearer f8009e9c-ae7a-3bc4-a39b-56b058f84743";
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {
            //Get available tiers
            client["Tier Collection"].get_tiers_tierLevel({"tierLevel": "application"},
                function (jsonData) {
                    var tierList = jsonData.obj.list;
                    $.get('/store/public/components/root/base/templates/application/appTiersTemplate.hbs', function (templateData) {
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


            $("#appAddForm").validate({
                submitHandler: function (form) {
                    addApplication();
                }
            });

            var addApplication = function () {
                var applicationName = $("#application-name").val();
                var tier = $("#appTier").val();
                var goBack = $("#goBack").val();
                var description = $("#description").val();

                var application = {};
                application.name = applicationName;
                application.throttlingTier = tier;
                application.description = description;
                application.callbackUrl = "";

                client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                client["Application (individual)"].post_applications({
                        "body": application,
                        "Content-Type": "application/json"
                    },
                    function (jsonData) {
                        window.location = "/store/application/" + jsonData.obj.applicationId;
                    },
                    function (error) {
                        alert("Error occurred while adding Application : " + applicationName);
                    });
            };
        }
    });

    $("#application-name").charCount({
        allowed: 70,
        warning: 50,
        counterText: i18n.t('Characters left: ')
    });
    $("#application-name").val('');

});



































