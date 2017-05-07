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
$(function () {
    var api_client = new API();
    api_client.getEndpoints(getEndpointsCallBack);
    $(document).on('click', ".endpoint-listing-delete", {api_client: api_client}, deleteEndpointHandler); // Event-type, Selector, data, method
});


function initDataTable(raw_data) {
    $('#endpoint-listing').DataTable({
        ajax: function (data, callback, settings) {
            callback(raw_data);
        },
        destroy: true,
        columns: [
            {'data': 'name'},
            {'data': 'type'},
            {'data': null},
            {'data': null},
            {'data': null}
        ],
        columnDefs: [
            {
               targets: ["endpoint-service-url"],
               searchable: false,
               sortable: false,
               render: _renderServiceUrl
            },
            {
               targets: ["endpoint-security"],
               searchable: false,
               sortable: false,
               render: _renderEndpointSecurity
            },
            {
               targets: ["endpoint-action"],
               searchable: false,
               sortable: false,
               render: _renderActionButtons
             }]
    });

    function _renderEndpointSecurity(data, type, row) {
        if (type === "display") {
            var security = data.endpointSecurity;
            var status = security.enabled;
            var status_element;
            var icon = $("<i>").addClass("fw");
            if (status) {
                status_element = $('<span>')
                    .text(" Secured ")
                    .append(icon.addClass("fw-lock"));
            } else {
                status_element = $('<span>')
                    .text("Unsecured");
            }
            return $('<h4>').append(status_element).html();
        }else{
        return data;
        }
    }

    function _renderServiceUrl(data, type, row) {
            if (type === "display") {
            var endpointConfig = JSON.parse(data.endpointConfig);
            return $('<span>').text(endpointConfig.serviceUrl).html();
            }else{
            return data;
            }
        }
    function _renderActionButtons(data, type, row) {
                if (type === "display") {
                    var viewIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
                    var viewIcon2 = $("<i>").addClass("fw fw-view fw-stack-1x");
                    var viewSpanIcon = $("<span>").addClass("fw-stack").append(viewIcon1).append(viewIcon2);
                    var viewSpanText = $("<span>").addClass("hidden-xs").text("View");
                    var view_button = $('<a>', {id: row.id, href: contextPath + '/endpoints/' + row.id, title: 'View'})
                        .addClass("btn  btn-sm padding-reduce-on-grid-view")
                        .append(viewSpanIcon)
                        .append(viewSpanText);
                    var editIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
                    var editIcon2 = $("<i>").addClass("fw fw-edit fw-stack-1x");
                    var editSpanIcon = $("<span>").addClass("fw-stack").append(editIcon1).append(editIcon2);
                    var editSpanText = $("<span>").addClass("hidden-xs").text("Edit");
                    var edit_button = $('<a>', {
                        id: data.id,
                        href: contextPath + '/endpoints/' + row.id + '/edit',
                        title: 'Edit'
                    })
                        .addClass("btn  btn-sm padding-reduce-on-grid-view")
                        .append(editSpanIcon)
                        .append(editSpanText);
                    var deleteIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
                    var deleteIcon2 = $("<i>").addClass("fw fw-delete fw-stack-1x");
                    var deleteSpanIcon = $("<span>").addClass("fw-stack").append(deleteIcon1).append(deleteIcon2);
                    var deleteSpanText = $("<span>").addClass("hidden-xs").text("delete");
                    var delete_button = $('<a>', {id: row.id, href: '#', 'data-id': row.id, title: 'delete'})
                        .addClass("btn btn-sm padding-reduce-on-grid-view endpoint-listing-delete")
                        .append(deleteSpanIcon)
                        .append(deleteSpanText);
                    return $('<div></div>').append(view_button).append(edit_button).append(delete_button).html();
                } else {
                    return data;
                }
          }
}
    /**
     * Handler method to handle the `onclick` event of the delete button
     * Send delete API call and remove the row from data tables
     * @param event {object} Click event
     */
    function deleteEndpointHandler(event) {
        var data_table = $('#endpoint-listing').DataTable();
        var current_row = data_table.row($(this).parents('tr'));
        var endpointId = current_row.data().id;
        var endpointName = current_row.data().name;
        var api = event.data.api_client;
        noty({
            text: 'Do you want to delete <span class="text-info">' + endpointName + '</span> ?',
            type: 'alert',
            dismissQueue: true,
            layout: "topCenter",
            modal: true,
            theme: 'relax',
            buttons: [
                {
                    addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                    $noty.close();
                    let promised_delete = api.deleteEndpoint(endpointId);
                    promised_delete.then(
                        function (response) {
                            if (!response) {
                                return;
                            }
                            current_row.remove();
                            data_table.draw();
                        }
                    ).catch(
                            function (error_response) {
                                     var error_data = JSON.parse(error_response.data);
                                     if(error_data.code =="900453"){
                                        var message = "Endpoint "+ endpointName + "Couldn't Delete ";
                                       }else{
                                         var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                                       }
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
                },
                {
                    addClass: 'btn btn-info', text: 'Cancel', onClick: function ($noty) {
                    $noty.close();
                }
                }
            ]
        });
    }
function endpointResponseToData(response) {
    var raw_data = {
        data: response.obj.list
    };
    return raw_data;
}
function getEndpointsCallBack(response) {
    var dt_data = endpointResponseToData(response);
    if (dt_data.data.length > 0) {
        $('#endpoint-listing-welcome-message').addClass('hidden');
        $('#endpoint-listing-container').removeClass('hidden');
        initDataTable(dt_data);
    }else{
       $('#endpoint-listing-welcome-message').removeClass('hidden');
       $('#endpoint-listing-container').addClass('hidden');
    }

}