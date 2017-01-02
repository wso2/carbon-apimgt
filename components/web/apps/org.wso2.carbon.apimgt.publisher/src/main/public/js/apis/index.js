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
            client.clientAuthorizations.add("apiKey", new SwaggerClient.ApiKeyAuthorization("Authorization", "Bearer " + $.cookie('token'), "header"));
            client["APIs"].get_apis(
                {"responseContentType": 'application/json'},
                function(jsonData) {
                    // Grab the template script
                    $.get('/publisher/public/components/root/base/templates/apis/index.hbs', function (templateData) {
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
