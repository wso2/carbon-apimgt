$(function () {
    $('#example').DataTable();
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/publisher/v0.10/swagger.json',
        success: function(swaggerData) {
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer c5a61c8c-3d05-3f45-a04f-c458c88d20ad", "header"));
            client["APIs"].get_apis(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script
                    $.get('/publisher/public/components/root/base/templates/api/index.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Define our data object
                        var context={apis: jsonData.obj.list};
                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#apiListingContainer').html(theCompiledHtml);
                    }, 'html');

                }
            );
        }
    });


});
