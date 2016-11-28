$(function () {

    var apiId = $("#apiId").val();
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {

            client["API (individual)"].get_apis_apiId({"apiId": apiId},
                {"responseContentType": 'application/json'},
                function (jsonData) {

                    /*             $.get('/store/public/components/root/base/templates/api/api-header.hbs', function (templateData) {
                     var template=Handlebars.compile(templateData);
                     // Define our data object
                     var context={api: jsonData.obj};

                     // Pass our data to the template
                     var theCompiledHtml = template(context);

                     // Add the compiled html to the page
                     $('#api-header').html(theCompiledHtml);
                     }, 'html');*/

                    $.get('/store/public/components/root/base/templates/api/api-overview-template.hbs', function (templateData) {

                        var template = Handlebars.compile(templateData);
                        var context = {api: jsonData.obj};
                        // Inject template data
                        var apiOverviewTemplate = template(context);

                        // Append compiled template into page
                        $('.row-height').append(apiOverviewTemplate);
                    }, 'html');
                }
            );
        }
    });
});



































