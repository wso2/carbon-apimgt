/**
 * Callback method to handle api data after receiving them via the REST API
 * Sets the API name and version on the header.
 * @param response {object} Raw response object returned from swagger client
 */
function getAPICallback(response) {
    var api_name = response.obj.name;
    var api_version = response.obj.version;
    $("#api-name").text(api_name + " - " + api_version);
}

/**
 * Handler method to handle the `onclick` event of the create new version button
 * Send copy API call and handle response from backend
 * @param event {object} Click event
 */
function createNewVersion(event) {
    var api_id = event.data.api_id;
    var api_client = event.data.api_client;
    var api_version = $('#new-version').val();
    if(api_version.length === 0){
        var message = "New API version cannot be empty !";
        noty({
            text: message,
            type: 'error',
            dismissQueue: true,
            progressBar: true,
            timeout: 5000,
            layout: 'topCenter',
            theme: 'relax',
            maxVisible: 10,
        });
        return;
    }
    var promised_update = api_client.createNewAPIVersion(api_id,api_version);
    promised_update.then(
        function (response) {
            var message = "New API version created successfully !";
            noty({
                text: message,
                type: 'success',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10,
                callback: {
                    afterClose: function () {
                        window.location = contextPath + "/apis/" + response.obj.id;
                    },
                }
            });
        }
    );
    promised_update.catch(
        function (error_response) {
            var message = "Error [" + error_response.obj.description + "]: " + error_response.obj.message;
            noty({
                text: message,
                type: 'error',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10
            });
        }
    );
}


$(document).ready(function () {
    var client = new API();
    var api_id = document.getElementById("apiId").value;
    client.get(api_id, getAPICallback);
    $(document).on('click', "#btn-add-new-version", {api_id:api_id,api_client:client}, createNewVersion);
});
