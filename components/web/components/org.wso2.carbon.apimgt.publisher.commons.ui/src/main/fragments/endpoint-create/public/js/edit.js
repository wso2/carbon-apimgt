/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
var api_client;
$(function () {
     api_client = new API();
     var endpointId = $('#endpointId').val();
     api_client.getEndpoint(endpointId).then(getEndpointCallBack);
         $('.help_popup').popover({ trigger: "hover" });
         $('#addEndpointBtn').on('click', updateEndpoint);
         $('#cancel-endpoint-btn').on('click', function () {
             window.location = contextPath + "/endpoints"
         });
});

function updateEndpoint(){
        var endpoint = constructEndpoint('global');
        endpoint.id= $('#endpointId').val();
        var promised_create =  api_client.updateEndpoint(endpoint);
            promised_create
                .then(updateEndpointCallBack)
                .catch(
                    function (error_response) {
                        var error_data = JSON.parse(error_response.data);
                        var message = "Error: " + error_data.description;
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
                        console.debug(error_response);
                    });
}

function updateEndpointCallBack(response) {
    var responseObject = JSON.parse(response.data);
    var message = responseObject.name + " Global Endpoint Updated successfully.";
    noty({
        text: message,
        type: 'success',
        dismissQueue: true,
        modal: true,
        closeWith: ['click', 'backdrop'],
        timeout: 2000,
        layout: 'top',
        theme: 'relax',
        maxVisible: 10,
        callback: {
            afterClose: function () {
                window.location = contextPath + "/endpoints";
            },
        }
    });
}
function getEndpointCallBack(response){
var data = JSON.parse(response.data);
populateEndpointDetails(data,'global');
}
