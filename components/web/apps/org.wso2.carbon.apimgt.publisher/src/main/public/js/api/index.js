$(function () {
    var client = new SwaggerClient({
        url: '/publisher/public/components/root/base/js/swagger.json',
        success: function(swaggerData) {
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer " + $.cookie('token'), "header"));
            client["APIs"].get_apis(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script
                    $.get('/publisher/public/components/root/base/templates/api/index.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Define our data object
                        var context={apis: jsonData.obj.list,contextPath:contextPath};
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
