$(function () {
    var client = new API();
    var apiId = $('input[name="apiId"]').val();
    client.get(apiId,loadOverview);
    $('#bodyWrapper').on('click','button',function(e){
        var elementName = $(this).attr('data-name');
        if(elementName == "editApiButton"){
            $('#apiOverviewForm').toggleClass('edit').toggleClass('view');
        }
    });

});

function loadOverview(jsonData) {
    //Manipulating data for the UI
    var context=jsonData.obj;
    console.info(context);
    if(context.endpointConfig){
        var endpointConfig = $.parseJSON(context.endpointConfig);
        context.productionEndpoint = endpointConfig.production_endpoints.url;
        console.info(context.productionEndpoint);
    }
    // Grab the template script
    $.get('/editor/public/components/root/base/templates/api/{id}Overview.hbs', function (templateData) {
        var template=Handlebars.compile(templateData);
        // Pass our data to the template
        var theCompiledHtml = template(context);
        // Add the compiled html to the page
        $('#overview-content').html(theCompiledHtml);
    }, 'html');

}