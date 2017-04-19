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
                policies: context.policies.join(', '),
                labels: context.labels.join(', ')
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
        var lcState = response[2];
        var lcHistory = response[3];
        var labels =  response[4];
        var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
        var api_data = JSON.parse(api.data);
        var policies_data = JSON.parse(policies.data);
        var label_data = JSON.parse(labels.data)['list'];
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

                if(label_data.length == 0) {
                    $('#labels-list-dropdown').multiselect(
                        {
                            nonSelectedText:"No Labels Found"
                        }
                    );
                }else {
                    $('#labels-list-dropdown').multiselect(
                        {
                            allSelectedText: false
                        }
                    );
                }

                // Handle svg object
                var svg_object = document.getElementById("lifecycle-svg");
                var state_array = {
                    'Created': 'Prototyped,Published',
                    'Published': 'Published,Created,Blocked,Deprecated,Prototyped',
                    'Prototyped': 'Published,Created,Prototyped',
                    'Blocked': 'Published,Deprecated',
                    'Deprecated': 'Retired,',
                    'Retired': ','
                };
                // Add an load event listener to the object, as it will load asynchronously
                svg_object.addEventListener("load", function () {
                    // get the inner DOM of lifecycleSVG.svg
                    var svg_doc = svg_object.contentDocument;
                    var api_state = api_data.lifeCycleStatus;

                    // Highlight next transition paths
                    var next_states = state_array[api_state].split(',')
                    for (var val in next_states) {
                        var transition_path = svg_doc.getElementById("_force_id_1-transition/_transition/"
                            + api_state + "/" + next_states[val] + "/1");
                        if (transition_path != null) {
                            transition_path.style.stroke = "#d9534f";
                            transition_path.setAttribute("stroke-width", "2");
                        }
                    }

                    // Change fill and set animation for current lifecycle state
                    var state_rectangle = svg_doc.getElementById(api_state.toString());
                    state_rectangle.style.fill = "#37474F";
                    // Create and append the animation element to the shape element.
                    var animateElement = document.createElementNS('http://www.w3.org/2000/svg', "animate");
                    animateElement.setAttribute("attributeType", "XML");
                    animateElement.setAttribute("attributeName", "fill");
                    animateElement.setAttribute("values", "##37474F;#c3cfd5;##37474F;##37474F");
                    animateElement.setAttribute("dur", "0.8s");
                    animateElement.setAttribute("repeatCount", "indefinite");
                    state_rectangle.appendChild(animateElement);
                }, false);

            }, onFailure: function (data) {
            }
        };
        for (var index in policies_data) {
            if (policies_data.hasOwnProperty(index)) {
                var policy = policies_data[index];
                policies_data[index].isSelected = api_data.policies.indexOf(policy.policyName) >= 0;
            }
        }
        for (var index in label_data) {
            if (label_data.hasOwnProperty(index)) {
                var label = label_data[index];
                label_data[index].isSelected = api_data.labels.indexOf(label.name) >= 0;
            }
        }
        var data = {
            lifeCycleStatus: api_data.lifeCycleStatus,
            isPublished: api_data.lifeCycleStatus.toLowerCase() === "published",
            policies: policies_data,
            lcState: lcState.obj,
            lcHistory: lcHistory.obj,
            labels: label_data
        };
        UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-lifecycle", data, "lc-tab-content", mode, callbacks);
    }

    var promised_api = api_client.get(api_id);
    var promised_tiers = api_client.policies('api');
    var promised_lcState = api_client.getLcState(api_id);
    var promised_lcHistory = api_client.getLcHistory(api_id);
    var promised_labels = api_client.labels();
    Promise.all([promised_api, promised_tiers, promised_lcState, promised_lcHistory, promised_labels]).then(renderLCTab);

    $(document).on('click', "#update-tiers-button", {api_client: api_client, api_id: api_id, promised_api: promised_api}, updateTiersHandler);
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
            $(document).on('click', "#update-endpoints-configuration", {api_client: api_client, api_id: api_id, promised_all_endpoints: all_endpoints}, updateEndpointsHandler);
        }
    ).catch(apiGetErrorHandler);
}

function subscriptionsTabHandler(event) {
    var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
    var callbacks = {
        onSuccess: function (data) {
            var api_client = new API();
            var api_id = $("#apiId").val();
            api_client.subscriptions(api_id, getSubscriptionsCallback);
        },
        onFailure: function (data) {
            console.debug("Failed");
        }
    };
    UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-subscriptions", {}, "subscriptions-tab-content", mode, callbacks);

}

/**
 * Do the life cycle update when user clicks on the relevant life cycle state button.
 * @param event {object} click event of the lc state button
 */
function updateLifecycleHandler(event) {
    var api_client = event.data.api_client;
    var new_state = $(this).data("lcstate");
    var api_id = event.data.api_id;
    var checked_items = getCheckListItems();
    if (checked_items != '') {
        var promised_update = api_client.updateLcState(api_id, new_state, checked_items);
    } else {
        var promised_update = api_client.updateLcState(api_id, new_state);
    }
    var event_data = {
        data: {
            api_client: api_client,
            api_id: api_id,
            checkedItems: checked_items,
        }
    };
    promised_update.then(
        (response, event = event_data) => {     
            var lcResponse = response.obj;
            var jsonPayload = lcResponse.jsonPayload;
                            
            if(jsonPayload) {
                var jsonResponse = JSON.parse(jsonPayload);
                var message = jsonResponse.redirectConfirmationMsg;
                noty({
                    text: message,
                    layout: "top",
                    theme: 'relax',
                    dismissQueue: true,
                    type: "alert",
                    buttons: [
                        {addClass: 'btn btn-primary', text: 'Leave page', onClick: function($noty) {
                            $noty.close();
                             location.href = jsonResponse.redirectUrl;
                            }
                        },
                        {addClass: 'btn btn-default', text: 'Stay on this page', onClick: function($noty) {
                            $noty.close();
                            location.href = contextPath + "/apis/" + event_data.obj.api_id;
                            }
                         }
                        ]
                });
            } else {
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
            }
 
            lifecycleTabHandler(event);
        }
    ).catch((response, event = event_data) => {
            let message_element = $("#general-alerts").find(".alert-danger");
            message_element.find(".alert-message").html(response.statusText);
            message_element.fadeIn("slow");
            lifecycleTabHandler(event);
        }
    );
}

/**
 * Do the life cycle checklist update when user clicks on the relevant life cycle checkbox.
 * @param event {object} click event of the lc state button
 */
function updateLifecycleCheckListHandler(event) {
    var api_client = event.data.api_client;
    var new_state = 'CheckListItemChange';
    var api_id = event.data.api_id;
    var checked_items = getCheckListItems();
    if (checked_items != '') {
        var promised_update = api_client.updateLcState(api_id, new_state, checked_items);
    } else {
        var promised_update = api_client.updateLcState(api_id, new_state);
    }
    var event_data = {
        data: {
            api_client: api_client,
            api_id: api_id,
            checkedItems: checked_items,
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
    ).catch((response, event = event_data) => {
            let message_element = $("#general-alerts").find(".alert-danger");
            message_element.find(".alert-message").html(response.statusText);
            message_element.fadeIn("slow");
            lifecycleTabHandler(event);
        }
    );
}

/**
 * Create a string with selected checklist items.
 * @returns {string} Checklist items in following format.
 * 'Deprecate old versions after publish the API:true,Require re-subscription when publish the API:false'
 */
function getCheckListItems() {
    var itemList = "";
    $('#checkItem[type=checkbox]').each(function () {
        itemList += $(this).val() + (this.checked ? ":true" : ":false") + ",";
    });
    return itemList;
}

/**
 * Handles the update endpoint submit button event, Get all the endpoint  inputs and update endpoint by using the endpoint UUID in input element data attribute
 * @param event {Event} DOM click event
 */
function updateEndpointsHandler(event) {
    event.preventDefault();
    var api_client = event.data.api_client;
    var promised_all_endpoints = event.data.promised_all_endpoints;
    var promised_updates = [];

    promised_all_endpoints.then(
        function (responses) {
            for (var endpoint_index in responses) {
                if (responses.hasOwnProperty(endpoint_index)) {
                    var endpoint = responses[endpoint_index].obj;

                    var input = $("#" + endpoint.id);
                    var url = input.val();
                    var data = {
                        endpointConfig: url
                    };
                    //sanity check
                    for (var attribute in data) {
                        if (!endpoint.hasOwnProperty(attribute)) {
                            throw 'Invalid key : ' + attribute + ', Valid keys are `' + Object.keys(endpoint) + '`';
                        }
                    }
                    var updated_endpoint = Object.assign(endpoint, data);
                    promised_updates.push(api_client.updateEndpoint(endpoint));
                }
            }
        }
    );

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
}

function updateTiersHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var promised_api = event.data.promised_api;
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
    promised_api.then(
        function (response) {
            var api_data = JSON.parse(response.data);
            api_data.policies = selected_policy_uuids;
            var promised_update = this.api_client.update(api_data);
            promised_update.then(
                function (response) {
                    var message = "Updated policies successfully.";
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
                    var message;
                    if (error_response.status == 412) {
                        message = "Error: You have provided an outdated request. " +
                            "Please try refreshing and updating again.";
                    } else {
                        $('#policies-list-dropdown').multiselect("deselectAll", false).multiselect("refresh");
                        message = "Error[" + error_response.status + "]: " + error_response.data;
                    }
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

function updateLabelsHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var data = {
        api_client: api_client,
        api_id: api_id
    };
    var selected_label_name = $('#labels-list-dropdown').val();
    api_client.get(api_id).then(
        function (response) {
            var api_data = JSON.parse(response.data);
            api_data.labels = selected_label_name;
            var promised_update = this.api_client.update(api_data);
            promised_update.then(
                function (response) {
                    var message = "Updated labels successfully.";
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
                    $('#labels-list-dropdown').multiselect("deselectAll", false).multiselect("refresh");
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

  function _renderActionButtons(data, type, row) {
        if (type === "display") {
            var icon = $("<i>").addClass("fw");
            var edit_button = $('<a>', {id: data.id, href: data.id})
                .text('Edit ')
                .addClass("cu-reg-btn btn-edit text-warning doc-listing-update")
                .append(icon.addClass("fw-edit"));
            var delete_button = $('<a>', {id: data.id})
                .text('Delete ')
                .addClass("cu-reg-btn btn-delete text-danger doc-listing-delete")
                .append(icon.clone().removeClass("fw-edit").addClass("fw-delete"));
            return $('<div></div>').append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

function initDataTable(raw_data) {
    $('#doc-table').DataTable({
        ajax: function (data, callback, settings) {
            callback(raw_data);
        },
        columns: [
            {'data': 'documentId'},
            {'data': 'name'},
            {'data': 'sourceType'},
            {'data': null}
            //TODO add modified date column once the service implementation is completed.
        ],
                columnDefs: [
                    {
                        "targets": [ 0 ],
                        "visible": false,
                        "searchable": false
                    },
                    {
                        targets: ["doc-listing-action"], //class name will be matched on the TH for the column
                        searchable: false,
                        sortable: false,
                        render: _renderActionButtons // Method to render the action buttons per row
                    }
                ]
    })
   }


function getDocsCallback(response) {
    var dt_data = apiResponseToData(response);
    initDataTable(dt_data);
}

function apiResponseToData(response) {
    var raw_data = {
        data: response.obj.list
    };
    return raw_data;
}

function resourcesTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var api_context;

    api_client.get(api_id).then(
        function (response) {
            api_context = response.obj.context;

            api_client.getSwagger(api_id).then(
                function (response) {
                    var api = response.obj;
                    var data = {
                        name: api.info.title,
                        id: api_id,
                        version: api.info.version,
                        context: api_context,
                        verbs: [ 'get' , 'post' , 'put' , 'delete', 'patch', 'head']
                    };
                    var callbacks = {
                        onSuccess: function (data) {
                            $.fn.editable.defaults.mode = 'inline';
                            api_client.getSwagger(api_id).then(
                                function (response) {
                                    api_doc_local = response.obj;
                                    var designer = new APIDesigner();
                                    designer.initControllersCall = "";
                                    designer.load_api_document(api_doc_local);
                                });
                        }, onFailure: function (data) {
                        }
                    };
                    var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-resources", data, "resources-tab-content", mode, callbacks);
                }
            ).catch(apiGetErrorHandler);

        }).catch(apiGetErrorHandler);
}

function documentTabHandler(event) {
    var api_client = event.data.api_client;
    var api_id = event.data.api_id;
    var callbacks = {
            onSuccess: function (data) {
            api_client.getDocuments(api_id,getDocsCallback);
            }, onFailure: function (data) {
           }
          };
     var mode = "OVERWRITE";
     var data = {};
     UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-documents", data, "api-tab-doc-content", mode, callbacks);
    }

/**
 * Jquery event handler on click event for api create submit button
 * @param event
 */
function createDocHandler(event) {
            var api_id = event.data.api_id;
            var api_client = event.data.api_client;
            var api_documents_data = {
                 documentId: "",
                 name: $('#docName').val(),
                 type: "HOWTO",
                 summary: $('#summary').val(),
                 sourceType: $('input[name=optionsRadios1]:checked').val(),
                 sourceUrl: $('#docUrl').val(),
                 inlineContent: "string",
                 otherTypeName: $('#specifyBox').val(),
                 permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
                 visibility: "API_LEVEL"
                 };
            var promised_add = api_client.addDocument(api_id, api_documents_data);

      promised_add.catch(function(error) {
             var error_data = JSON.parse(error_response.data);
             var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
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
            }).then(function(done) {
              var dt_data = done.obj;
              var documentId = dt_data.documentId;
              var name = dt_data.name;
              var sourceType = dt_data.sourceType;
              var docId = dt_data.documentId;

              if(sourceType == "FILE"){
                var file_input = $('#doc-file');
                var file = file_input[0].files[0];
                var promised_add_file = api_client.addFileToDocument(api_id,docId,file);
                promised_add_file.catch(function(error) {}).then(function(done){
                 var addedFile = done;
                });
              }

              var data_table = $('#doc-table').DataTable();
              data_table.row.add({documentId,name,sourceType, _renderActionButtons}).draw();
            });
 }


    function deleteDocHandler(event) {
        var data_table = $('#doc-table').DataTable();
        var current_row = data_table.row($(this).parents('tr'));
        var documentId = current_row.data().documentId;
        var doc_name = current_row.data().name;
        var api_client = event.data.api_client;
        var api_id = event.data.api_id;
        noty({
            text: 'Do you want to delete <span class="text-info">' + doc_name + '</span> ?',
            type: 'alert',
            dismissQueue: true,
            layout: "topCenter",
            modal: true,
            theme: 'relax',
            buttons: [
                {
                    addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                    $noty.close();
                    let promised_delete = api_client.deleteDocument(api_id,documentId);
                    promised_delete.then(
                        function (response) {
                            if (!response) {
                                return;
                            }
                            current_row.remove();
                            data_table.draw();
                        }
                    );
                }
                },
                {
                    addClass: 'btn btn-info', text: 'Cancel', onClick: function ($noty) {
                    $noty.close();
                }
                }
            ]
        });
    }

    function getAPIDocumentByDocId(event){
        var data_table = $('#doc-table').DataTable();
        var current_row = data_table.row($(this).parents('tr'));
        var documentId = current_row.data().documentId;
        $('#docId').val(documentId);
        var doc_name = current_row.data().name;
        var api_client = event.data.api_client;
        var api_id = event.data.api_id;

        let promised_update = api_client.getDocument(api_id,documentId);
           promised_update.then(
             function (response) {
                   if (!response) {
                  return;
                 }

             let promised_get_content = api_client.getFileForDocument(api_id,documentId);
                promised_get_content.catch(function(error) {
                var error_data = JSON.parse(error.data);
             }).then(function(done) {

             })

             loadDocumentDataToForm(response);
             }
         );
    }

    function updateAPIDocument(event){
           var api_id = event.data.api_id;
           var api_client = event.data.api_client;
           var documentId = $('#docId').val();
           var update_documents_data = {
                 documentId: $('#docId').val(),
                 name: $('#docName').val(),
                 type: "HOWTO",
                 summary: $('#summary').val(),
                 sourceType: $('input[name=optionsRadios1]:checked').val(),
                 sourceUrl: $('#docUrl').val(),
                 inlineContent: "",
                 otherTypeName: $('#specifyBox').val(),
                 permission: '[{"groupId" : "1000", "permission" : ["READ","UPDATE"]},{"groupId" : "1001", "permission" : ["READ","UPDATE"]}]',
                 visibility: "API_LEVEL"
                 };
           var promised_update = api_client.updateDocument(api_id, documentId, update_documents_data);


        promised_update.catch(function(error) {
             var error_data = JSON.parse(error_response.data);
             var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
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
             }).then(function(done) {
             var dt_data = done.obj;
             var name = dt_data.name;
             var type = dt_data.type;
             var docId = dt_data.documentId;

             var data_table = $('#doc-table').DataTable();
             var current_row = data_table.cell(documentId, 0).row();
             current_row.invalidate();
             current_row.data(dt_data);
             data_table.draw();
             $('#newDoc').fadeOut();
            });
    }

    function loadDocumentDataToForm(response){
         $('#add-doc-submit').hide();
         $('#newDoc').fadeIn();
         $('#add-new-doc').hide();
         $('#doc-header').html("<h4> Update Document - "+ response.obj.name +"</h4>");
         $('#docName').val(response.obj.name);
         $('#summary').val(response.obj.summary);
         $("input[value='"+response.obj.sourceType+"']").prop("checked",true);
         if(response.obj.sourceType == "URL"){
            $('#sourceUrlDoc').show("slow");
            $('#docUrl').val(response.obj.sourceUrl);
         }else if(response.obj.sourceType == "FILE"){
            $('#fileNameDiv').show("slow");
            $('#docUrl').val(response.obj.sourceUrl);
         }else{
            $('#sourceUrlDoc').fadeOut("slow");
            $('#fileNameDiv').fadeOut("slow");
         }
            $('#fileNameDiv').fadeOut("slow");
            $('#update-doc-submit').fadeIn("slow");
         $('#docName').disabled = true;
    }


    function toggleDocAdder() {
        $('#newDoc').toggle();
    }
    
    
    var select_environment = function(){
        var selectedEnvironment = $("#environment_name");
        var name = selectedEnvironment.val();

        //Token to load the swagger definition
        var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
        swaggerUi.api.clientAuthorizations.add("key", new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header"));

        //Set Swagger URL
        if (window.swaggerUi.api.url.indexOf("?") != -1) {
        	if (window.swaggerUi.api.url.indexOf("labelName") != -1) {
        		window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url.split("labelName")[0] + "labelName=" + name + "&scheme=" + location.protocol});
        	} else {
                window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "&labelName=" + name + "&scheme=" + location.protocol});
        	}
        } else {
        	window.swaggerUi.updateSwaggerUi({ "url" : swaggerUi.api.url + "?labelName=" + name + "&scheme=" + location.protocol});
        }
        //change_token();
      };
    
    /**
     * Event handler for API Console tab onclick event;Get the API swagger definition and display in Swagger UI
     * @param event {object} Click event of the API Console tab
     */
    function apiConsoleTabHandler(event) {
    	  var api_client = event.data.api_client;
    	  var api_id = event.data.api_id;

    	   var mode = "OVERWRITE";
    	   var data = {};
    	   api_client.get(api_id).then(
    		        function (response) {
    		        	var context = response.obj;
    		        	var callbacks = {
    		    	          onSuccess: function (data) {
    		    	                 api_client.getSwagger(api_id).then(
    		    	                		 function (jsonData) {
    		    	                			 var swaggerURL = jsonData.url;
    		    	                			 if (context.labels.length > 0) {
    		    	                				 swaggerURL = jsonData.url + "?labelName=" + context.labels[0] + "&scheme=" + location.protocol;
    		    	                				 }
    		    	                			 var bearerToken = "Bearer " + getCookie("WSO2_AM_TOKEN_1");
    		    	                			 $(document).ready(function() {
    		    	         		    	        window.swaggerUi = new SwaggerUi({
    		    	         		    	                url: swaggerURL,
    		    	         		    	                dom_id: "swagger-ui-container",
    		    	         		    	                authorizations: {
    		    	         		    	                        key: new SwaggerClient.ApiKeyAuthorization("Authorization", bearerToken, "header")
    		    	         		    	                },
    		    	         		    	                supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch', 'head'],
    		    	         		    	                onComplete: function(swaggerApi, swaggerUi){
    		    	         		    	                        console.log("Loaded SwaggerUI");
    		    	         		    	                },
    		    	         		    	                onFailure: function(data) {
    		    	         		    	                        console.log("Unable to Load SwaggerUI");
    		    	         		    	                },
    		    	         		    	                docExpansion: "list",
    		    	         		    	                jsonEditor: false,
    		    	         		    	                defaultModelRendering: 'schema',
    		    	         		    	                showRequestHeaders: true,
    		    	         		    	                validatorUrl: null
    		    	         		    	        });
    		    	         		    	        window.swaggerUi.load();
    		    	         		            });
    		    	                		 }   		    	                		 
    		    	                     		    	                 
    		    	                 ).catch(apiGetErrorHandler);
    		    	                 $(".env_name").change(select_environment);
    		    	          }, onFailure: function (data) {
    		    	         }
    		    	        };
    		            var data = {
    		                labels: context.labels
    		            };    		            
    		            UUFClient.renderFragment("org.wso2.carbon.apimgt.publisher.commons.ui.api-console", data, 
    		     			   "api-console-content", mode, callbacks);
    		        }
    		    ).catch(apiGetErrorHandler);
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
    $('#tab-4').bind('show.bs.tab', {api_client: client, api_id: api_id}, resourcesTabHandler);
    $('#tab-5').bind('show.bs.tab', {api_client: client, api_id: api_id}, documentTabHandler);
    $('#tab-9').bind('show.bs.tab', {api_client: client, api_id: api_id}, subscriptionsTabHandler);
    $('#tab-10').bind('show.bs.tab', {api_client: client, api_id: api_id}, apiConsoleTabHandler);
    $(document).on('click', ".lc-state-btn", {api_client: client, api_id: api_id}, updateLifecycleHandler);
    $(document).on('click', "#checkItem", {api_client: client, api_id: api_id}, updateLifecycleCheckListHandler);
    $(document).on('click', ".doc-listing-delete", {api_client: client,api_id: api_id}, deleteDocHandler);
    $(document).on('click', ".doc-listing-update", {api_client: client,api_id: api_id}, getAPIDocumentByDocId);
    $(document).on('click', "#add-doc-submit", {api_client: client, api_id: api_id}, createDocHandler);
    $(document).on('click', "#update-doc-submit", {api_client: client, api_id: api_id}, updateAPIDocument);
    $(document).on('click', "#add-new-doc", {}, toggleDocAdder);
    $(document).on('click', "#update-labels-button", {api_client: client, api_id: api_id}, updateLabelsHandler);
    loadFromHash();
});
