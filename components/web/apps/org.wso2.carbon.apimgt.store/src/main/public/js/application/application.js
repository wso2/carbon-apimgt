$(function () {

    var bearerToken = "Bearer 3dcec29c-9aae-3fbf-b23e-7e11dc908637";
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {


            client.apisAPI.get_apis(
                {"responseContentType": 'application/json'},
                function (jsonData) {
                    // Grab the template script
                    $.get('/store/public/components/root/base/templates/api/apisTemplate.hbs', function (templateData) {
                        var template = Handlebars.compile(templateData);
                        // Define our data object
                        var context = {apis: jsonData.obj.list};

                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#api-content').html(theCompiledHtml);
                    }, 'html');

                }
            );

            //Delete Application
            $('#application-table').on('click', 'a.deleteApp', function () {
                //TODO: Get Application id
                var appId = "d8f4c84e-1875-4b30-b484-835f8f1e748e";
                client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));
                client["Application (individual)"].delete_applications_applicationId({"applicationId": appId},
                    function (data) {
                        console.log(JSON.stringify(data.obj));
                    });
            });
        }
    });


});


