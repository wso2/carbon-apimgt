'use strict';
/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
    $('#composite-api-submit').on('click', createAPIHandler);
    validateActionButtons('#composite-api-submit');
});

/**
 * This callback function is called after the success response from API creation
 *
 * @param response {object} Response object receiving from swagger-js client
 */
function createAPICallback(response) {
    // Grab the template script
    var responseObject = JSON.parse(response.data);
    window.location.replace(contextPath + "/apis?create_success=true&id=" + responseObject.id + "&name=" + encodeURI(responseObject.name));
}

/**
* Jquery event handler on click event for #composite-api-submit button
*
* @param event
*/
function createAPIHandler(event) {
alert();
    event.preventDefault();
    $('[data-toggle="loading"]').loading('show');

    var apiData = {
        name: $("#new-api-name").val(),
        context: $('#new-api-context').val(),
        version: $('#new-api-version').val()
    };
    var api = new API('');
    var createPromise = api.create(apiData);
    createPromise.then(createAPICallback)
    .catch(function (errorResponse) {
        var errorData = JSON.parse(errorResponse.data);
        var message = "Error[" + errorData.code + "]: " + errorData.description + " | " + errorData.message + ".";
        noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            modal: true,
            closeWith: ['click', 'backdrop'],
            progressBar: true,
            timeout: 5000,
            layout: 'top',
            theme: 'relax',
            maxVisible: 10
        });
        $('[data-toggle="loading"]').loading('hide');
        console.debug(errorResponse);
    });
}
