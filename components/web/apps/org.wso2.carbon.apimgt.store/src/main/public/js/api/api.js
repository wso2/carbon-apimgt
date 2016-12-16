$(function () {
    
    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".purple").insertBefore(prev).css('top','0px').addClass('active');
    
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function(swaggerData) {
            client["Tag Collection"].get_tags(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script

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
