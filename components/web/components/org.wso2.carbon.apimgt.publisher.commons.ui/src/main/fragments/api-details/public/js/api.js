$(function () {
    $('#example').DataTable();
    var client = new SwaggerClient({
        url: swaggerURL,
        success: function(swaggerData) {
            client["Tag Collection"].get_tags(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script
                    debugger;
                    $.get('/store/public/components/root/base/templates/api/tagsTemplate.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Define our data object
                        var context=jsonData.obj;

                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#tagcloud').html(theCompiledHtml);
                    }, 'html');

                }
            );


            client.apisAPI.get_apis(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script
                    $.get('/store/public/components/root/base/templates/api/apisTemplate.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Define our data object
                        var context={apis: jsonData.obj.list};

                        // Pass our data to the template
                        var theCompiledHtml = template(context);

                        // Add the compiled html to the page
                        $('#api-content').html(theCompiledHtml);
                    }, 'html');

                }
            );
        }
    });


});
