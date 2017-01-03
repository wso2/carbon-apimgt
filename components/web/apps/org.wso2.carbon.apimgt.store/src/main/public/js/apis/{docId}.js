$(function () {


    var apiId = $("#apiId").val();
    var docId = $("#docId").val();
    var client = new SwaggerClient({
        url: 'https://apis.wso2.com/api/am/store/v0.10/swagger.json',
        success: function (swaggerData) {
            var documentName = null;
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer 200d5b62-7837-3dd9-b4a8-896cbcd755ab", "header"));
            client["API (individual)"].get_apis_apiId_documents_documentId({"documentId": docId, "apiId": apiId},
                function (jsonData) {
                     documentName = jsonData.obj.name;


                    client["API (individual)"].get_apis_apiId_documents_documentId_content({"documentId": docId, "apiId": apiId},
                        function (jsonData) {

                            $.get('/store/public/components/root/base/templates/apis/{apiId}/documents/documentation-content.hbs', function (templateData) {
                                var template = Handlebars.compile(templateData);
                                // Define our data object
                                var context = {content: jsonData.data, documentName : documentName};

                                // Pass our data to the template
                                var theCompiledHtml = template(context);

                                // Add the compiled html to the page
                                $('.document-content').html(theCompiledHtml);
                            }, 'html');



                        },
                        function (error) {
                            alert("Error occurred while retrieve Applications" + erro);
                        });
                },
                function (error) {
                    alert("Error occurred while retrieve Applications" + erro);
                });


        }
    });

});

































