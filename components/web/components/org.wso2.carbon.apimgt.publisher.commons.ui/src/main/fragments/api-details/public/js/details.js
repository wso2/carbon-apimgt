'use strict';
/**
 * Handle errors while getting API data by UUID, when loading the details page.
 * @param {object} error_response object returned from Swagger-client library
 */
function apiGetErrorHandler(error_response) {
    var message;
    if (error_response.data) {
        message = "Error[" + error_response.status + "]: " + error_response.data;
    } else {
        message = error_response;
    }
    noty({
        text: message,
        type: 'error',
        dismissQueue: true,
        modal: true,
        progressBar: true,
        timeout: 5000,
        layout: 'top',
        theme: 'relax',
        maxVisible: 10,
        callback: {
            afterClose: function () {
                window.location = contextPath + "/";
            },
        }
    });
}

function overviewTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    api_client.get(api_id).then(
        function (response) {
            var context = response.obj;
            if (context.endpointConfig) {
                var endpointConfig = $.parseJSON(context.endpointConfig);
                context.productionEndpoint = endpointConfig.production_endpoints.url;
            }
            var callbacks = {
                onSuccess: function (renderedHTML) {
                    $('#overview-content').html(renderedHTML);
                }, onFailure: function (data) {
                }
            };
            var data = {
                name: context.name,
                version: context.version,
                context: context.context,
                isDefaultVersion: context.isDefaultVersion,
                visibility: context.visibility,
                lastUpdatedTime: context.lastUpdatedTime,
                provider: context.provider,
                id: context.id,
                lifeCycleStatus: context.lifeCycleStatus,
                policies: context.policies.join(', ')
            };
            UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-overview", data, callbacks);
        }
    ).catch(apiGetErrorHandler);
}

/**
 * Append page name (with hash i.e: #resources-tab )to URL
 * @param event {object} Browser click event.
 */
function addHashToURL(event) {
    window.location.hash = event.target.hash;
    window.scrollTo(0, 0);
}

/**
 * Javascript to enable link to tab. Change hash for page-reload, If hashed page name is available load that page
 * If no hash value is present load the default page which is overview-tab
 */
function loadFromHash() {
    var hash = document.location.hash;
    if (hash) {
        $('a[href="#' + hash.substr(1) + '"]').tab('show');
        window.scrollTo(0, 0);
    } else {
        showTab('overview-tab');
    }
    // Register tab click event to append hashed page name to current URL
    $('a[role="tab"]').on('shown.bs.tab', addHashToURL);
}

/**
 * Event handler for API Lifecycle detail tab onclick event;Get the current API lifecycle tab HTML via UUFClient and display.
 * @param event {object} Click event of the LC state tab
 */
function lifecycleTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;

    function renderLCTab(response) {
        var api = response[0];
        var policies = response[1];
        var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
        var api_data = JSON.parse(api.data);
        var policies_data = JSON.parse(policies.data);
        var callbacks = {
            onSuccess: function (data) {
                $('#policies-list-dropdown').multiselect(
                    {
                        onChange: function (option, checked, select) {
                            if (!checked && !option.parent().val()) {
                                var message = "Please select at least one subscription tier.";
                                noty({
                                    text: message,
                                    type: 'warning',
                                    dismissQueue: true,
                                    progressBar: true,
                                    timeout: 5000,
                                    layout: 'topCenter',
                                    theme: 'relax',
                                    maxVisible: 10,
                                });
                                return false;
                            }
                        }
                    }
                );
            }, onFailure: function (data) {
            }
        };
        for (var index in policies_data) {
            if (policies_data.hasOwnProperty(index)) {
                var policy = policies_data[index];
                policies_data[index].isSelected = api_data.policies.indexOf(policy.policyName) >= 0;
            }
        }
        var data = {
            lifeCycleStatus: api_data.lifeCycleStatus,
            isPublished: api_data.lifeCycleStatus.toLowerCase() === "published",
            policies: policies_data
        };
        UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-lifecycle", data, "lc-tab-content", mode, callbacks);
    }

    var promised_api = api_client.get(api_id);
    var promised_tiers = api_client.policies('api');
    Promise.all([promised_api, promised_tiers]).then(renderLCTab)
}

/**
 * Load ballerina composer from here
 * @param event {object} Click event
 */
function mediationTabHandler(event) {

}

function endpointsTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    api_client.get(api_id).then(
        function (response) {
            var api = response.obj;
            var data = {
                name: api.name,
                endpoints: {},
            };
            var promised_endpoints = [];
            for (var endpoint_index in api.endpoint) {
                if (api.endpoint.hasOwnProperty(endpoint_index)) {
                    var id = api.endpoint[endpoint_index].id;
                    promised_endpoints.push(api_client.getEndpoint(id));
                    data.endpoints[id] = api.endpoint[endpoint_index];
                }
            }
            var all_endpoints = Promise.all(promised_endpoints);
            all_endpoints.then(
                function (responses) {
                    for (var endpoint_index in responses) {
                        if (responses.hasOwnProperty(endpoint_index)) {
                            var endpoint = responses[endpoint_index].obj;
                            Object.assign(data.endpoints[endpoint.id], endpoint);
                        }
                    }
                    var callbacks = {
                        onSuccess: function (data) {
                        }, onFailure: function (data) {
                        }
                    };
                    var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-endpoints", data, "endpoints-tab-content", mode, callbacks);
                }
            );
        }
    ).catch(apiGetErrorHandler);
}

/**
 * Do the life cycle update when user clicks on the relevant life cycle state button.
 * @param event {object} click event of the lc state button
 */
function updateLifecycleHandler(event) {
    var api_client = event.data.api_client;
    var new_state = $(this).data("lcstate");
    var api_id = event.data.api_id;
    var promised_update = api_client.updateLcState(api_id, new_state);
    var event_data = {
        data: {
            api_client: api_client,
            api_id: api_id
        }
    };
    promised_update.then(
        (response, event = event_data) => {
            var message = "Life cycle state updated successfully!";
            noty({
                text: message,
                type: 'success',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10,
            });
            lifecycleTabHandler(event);
        }
    ).catch(
        (response, event = event_data) => {
            let message_element = $("#general-alerts").find(".alert-danger");
            message_element.find(".alert-message").html(response.statusText);
            message_element.fadeIn("slow");
            lifecycleTabHandler(event);
        }
    );
}

function updateEndpointsHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var inputs = $(".endpoint-inputs");
    var promised_updates = [];
    for (var endpoint_input of inputs) {
        var input = $(endpoint_input);
        var id = input.data().uuid;
        var url = input.val();
        var data = {
            endpointConfig: url,
        };
        promised_updates.push(api_client.updateEndpoint(id, data));
    }
    Promise.all(promised_updates).then(
        function (responses) {
            var message = "Endpoint configuration(s) updated successfully!";
            noty({
                text: message,
                type: 'success',
                dismissQueue: true,
                progressBar: true,
                timeout: 5000,
                layout: 'topCenter',
                theme: 'relax',
                maxVisible: 10,
            });
        }
    ).catch(apiGetErrorHandler);
    return false;
}

function updateTiersHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var data = {
        api_client: api_client,
        api_id: api_id
    };
    var selected_policy_uuids = $('#policies-list-dropdown').val();
    if (!selected_policy_uuids) {
        var message = "Please select at least one subscription tier.";
        noty({
            text: message,
            type: 'warning',
            dismissQueue: true,
            progressBar: true,
            timeout: 5000,
            layout: 'topCenter',
            theme: 'relax',
            maxVisible: 10,
        });
        return false;
    }
    api_client.get(api_id).then(
        function (response) {
            var api_data = JSON.parse(response.data);
            api_data.policies = selected_policy_uuids;
            var promised_update = this.api_client.update(api_data);
            promised_update.then(
                function (response) {
                    var message = "Update policies successfully.";
                    noty({
                        text: message,
                        type: 'success',
                        dismissQueue: true,
                        progressBar: true,
                        timeout: 5000,
                        layout: 'topCenter',
                        theme: 'relax',
                        maxVisible: 10,
                    });
                }
            );
            promised_update.catch(
                function (error_response) {
                    $('#policies-list-dropdown').multiselect("deselectAll", false).multiselect("refresh");
                    var message = "Error[" + error_response.status + "]: " + error_response.data;
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
                }
            );
        }.bind(data));
}

function showTab(tab_name) {
    $('.nav a[href="#' + tab_name + '"]').tab('show');
}

/**
 * Execute once the page load is done.
 */
$(function () {
    var client = new API();
    /* Re-use same api client in all the tab show events */
    var api_id = $('input[name="apiId"]').val(); // Constant(immutable) over all the tabs since parsing as event data to event handlers
    $('#bodyWrapper').on('click', 'button', function (e) {
        var elementName = $(this).attr('data-name');
        if (elementName == "editApiButton") {
            $('#apiOverviewForm').toggleClass('edit').toggleClass('view');
        }
    });
    $('#tab-1').bind('show.bs.tab', {api_client: client, api_id: api_id}, overviewTabHandler);
    $('#tab-2').bind('show.bs.tab', {api_client: client, api_id: api_id}, lifecycleTabHandler);
    $('#tab-3').bind('show.bs.tab', {api_client: client, api_id: api_id}, endpointsTabHandler);
    $(document).on('click', ".lc-state-btn", {api_client: client, api_id: api_id}, updateLifecycleHandler);
    $(document).on('click', "#update-tiers-button", {api_client: client, api_id: api_id}, updateTiersHandler);
    $(document).on('click', "#update-endpoints-configuration", {
        api_client: client,
        api_id: api_id
    }, updateEndpointsHandler);
    loadFromHash();
});
