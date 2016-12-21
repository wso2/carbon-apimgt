$(function () {
    var client = new SwaggerClient({
        url: '/publisher/public/components/root/base/js/swagger.json',
        success: function(swaggerData) {
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer "+$.cookie('token'), "header"));
            var apiId = $('input[name="apiId"]').val();
            client["APIs"].get_apis_apiId(
                {apiId:apiId},
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    //Manipulating data for the UI
                    var context=jsonData.obj;
                    console.info(context);
                    if(context.endpointConfig){
                        var endpointConfig = $.parseJSON(context.endpointConfig);
                        context.productionEndpoint = endpointConfig.production_endpoints.url;
                        console.info(context.productionEndpoint);
                    }
                    // Grab the template script
                    $.get('/publisher/public/components/root/base/templates/api/{id}Title.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Pass our data to the template
                        var theCompiledHtml = template(context);
                        // Add the compiled html to the page
                        $('#apiTitleContainer').html(theCompiledHtml);
                    }, 'html');

                    $.get('/publisher/public/components/root/base/templates/api/{id}Overview.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Pass our data to the template
                        var theCompiledHtml = template(context);
                        // Add the compiled html to the page
                        $('#overview-r').html(theCompiledHtml);
                    }, 'html');

                }
            );
        }
    });
    $('#bodyWrapper').on('click','button',function(e){
        var elementName = $(this).attr('data-name');
        if(elementName == "editApiButton"){
            $('#apiOverviewForm').toggleClass('edit').toggleClass('view');
        }
    });

});
