$(function () {
    $('#example').DataTable();
    var api = new API();
    api.getAll(getAPIsCallback);
});

function getAPIsCallback(response) {
    var context = {apis: response.obj.list};
    var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.editor.feature.api-listing",
        context,
        "apiListingContainer", mode);
    // Grab the template script
    /*$.get('public/components/root/base/templates/api/index.hbs', function (templateData) {
     var template = Handlebars.compile(templateData);
     // Define our data object
     var context = {apis: response.obj.list};
     // Pass our data to the template
     var theCompiledHtml = template(context);

     // Add the compiled html to the page
     $('#apiListingContainer').html(theCompiledHtml);
     }, 'html');*/
}