$(function () {

    var bearerToken = "Bearer f8009e9c-ae7a-3bc4-a39b-56b058f84743";
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {

            //Delete Application
            $('#application-table').on('click', 'a.deleteApp', function () {
                alert("Are you sure you want to delete Application");

                var appId = $(this).attr("data-id")
                client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                client["Application (individual)"].delete_applications_applicationId({"applicationId": appId},
                    function (success) {
                        //TODO: Reload element only
                        window.location.reload(true);
                    },
                    function (error) {
                        alert("Error occurred while deleting application")
                    });
            });

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
        }
    });
});



































