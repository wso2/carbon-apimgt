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
$(
    function () {
        $('#api-create-submit').on('click', createAPIHandler);
        $('input[type=radio][name=implementation-options]').on('change', implementationOptionsHandler);
    }
);

/**
 * This callback function is called after the success response from API creation
 * @param response {object} Response object receiving from swagger-js client
 */
function createAPICallback(response) {
    // Grab the template script
    var responseObject = JSON.parse(response.data);
    window.location.replace(contextPath + "/apis?create_success=true&id=" + responseObject.id + "&name=" + encodeURI(responseObject.name));
}

/**
 * Jquery event handler for options element for switch between implementation types
 * @param event
 */
function implementationOptionsHandler(event) {
    var selected_value = this.value;
    $(".sub-implementation-option").hide();
    $("#" + selected_value + "-form").fadeIn();
    if (selected_value == 'swagger-option') {
        $('.basic-inputs :input').prop("disabled", true);
    } else {
        $('.basic-inputs :input').prop("disabled", false);
    }
}
/**
 * Jquery event handler on click event for api create submit button
 * @param event
 */
function createAPIHandler(event) {
    event.preventDefault();
    let implementation_method = $('input[name=implementation-options]:checked', '#implementation-option-form').val();
    let input_type = $('input[name=import-definition]:checked', '#swagger-option-form').val();

    switch (implementation_method) {
        case "endpoint-option":
            break;
        case "swagger-option":
            createAPIFromSwagger(input_type);
            break;
        case "mock-option":
            var api_data = {
                name: $("#new-api-name").val(),
                context: $('#new-api-context').val(),
                version: $('#new-api-version').val()
            };
            var new_api = new API('');
            new_api.create(api_data, createAPICallback);
            break;
    }
}
/**
 * Do create API from either swagger URL or swagger file upload.In case of URL pre fetch the swagger file and make a blob
 * and the send it over REST API.
 * @param input_type
 */
function createAPIFromSwagger(input_type) {
    if (input_type === "swagger-url") {
        var url = $('#swagger-url').val();
        var fetch_data = $.get(url);
        fetch_data.then(
            (swagger_json) => {
                let data = new Blob([JSON.stringify(swagger_json)], {type: 'application/json'});
                var new_api = new API('');
                new_api.create(data, createAPICallback);
            });
    } else if (input_type === "swagger-file") {
        /* TODO: Implement swagger file  upload support ~tmkb*/
    }
}