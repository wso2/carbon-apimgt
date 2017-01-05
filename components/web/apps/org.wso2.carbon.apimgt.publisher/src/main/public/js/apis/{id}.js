/**
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(function () {
    var client = new SwaggerClient({
        url: swaggerUrl,
        success: function(swaggerData) {
            client.setBasePath(":9443/api/am/publisher/v0.10"); //TODO Remove this line when move to C5 rest api
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
                    $.get('/publisher/public/components/root/base/templates/apis/{id}Title.hbs', function (templateData) {
                        var template=Handlebars.compile(templateData);
                        // Pass our data to the template
                        var theCompiledHtml = template(context);
                        // Add the compiled html to the page
                        $('#apiTitleContainer').html(theCompiledHtml);
                    }, 'html');

                    $.get('/publisher/public/components/root/base/templates/apis/{id}Overview.hbs', function (templateData) {
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
