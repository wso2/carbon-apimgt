$(function () {
    $('[data-toggle="loading"]').loading('show');
    /* TODO: need to render page with loading animation embed , doing this create a flick in showing loading animation and page loading ~tmkb*/
    var api = new API();
    api.getAll(getAPIsCallback);
    $(document).on('click', ".api-listing-delete", {api_instance: api}, deleteAPIHandler); // Event-type, Selector, data, method
});

/**
 * Callback method to handle apis data after receiving them via the REST API
 * @param response {object} Raw response object returned from swagger client
 */
function getAPIsCallback(response) {
    $('[data-toggle="loading"]').loading('hide');
    var dt_data = apiResponseToData(response);
    if (dt_data.data.length === 0) {
        $('#api-listing-welcome-message').removeClass('hidden');
        return false;
    }
    $('#api-listing-container').removeClass('hidden');
    initDataTable(dt_data);
}

/**
 * Handler method to handle the `onclick` event of the delete button
 * Send delete API call and remove the row from data tables
 * @param event {object} Click event
 */
function deleteAPIHandler(event) {
    var data_table = $('#apim-publisher-listing').DataTable();
    var current_row = data_table.row($(this).parents('tr'));
    var api_id = current_row.data().id;
    var api_name = current_row.data().name;
    var api = event.data.api_instance;
    noty({
        text: 'Do you want to delete <span class="text-info">' + api_name + '</span> ?',
        type: 'alert',
        dismissQueue: true,
        layout: "topCenter",
        modal: true,
        theme: 'relax',
        buttons: [
            {
                addClass: 'btn btn-danger', text: 'Ok', onClick: function ($noty) {
                $noty.close();
                let promised_delete = api.deleteAPI(api_id);
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

function initDataTable(raw_data) {
    $('#apim-publisher-listing').DataTable({
        ajax: function (data, callback, settings) {
            callback(raw_data);
        },
        columns: [
            {'data': null},
            {'data': 'version'},
            {'data': 'provider'},
            {'data': null},
            {'data': null},
        ],
        columnDefs: [
            {
                targets: ["api-listing-action"], //class name will be matched on the TH for the column
                searchable: false,
                sortable: false,
                render: _renderActionButtons // Method to render the action buttons per row
            },
            {
                targets: ["api-listing-name"],
                searchable: true,
                render: _renderNameLink
            },
            {
                targets: ["api-listing-status"],
                render: _renderStatus
            }
        ]
    });

    function _renderActionButtons(data, type, row) {
        if (type === "display") {
            var icon = $("<i>").addClass("fw");
            var cssEdit = "cu-reg-btn btn-edit text-warning";
            if(!hasValidScopes("/apis/{apiId}", "put")) {
                cssEdit = "cu-reg-btn btn-edit text-warning not-active";
            }
            var edit_button = $('<a>', {id: data.id, href: data.id})
                .text('Edit ')
                .addClass(cssEdit)
                .append(icon.addClass("fw-edit"));

            var cssDelete = "cu-reg-btn btn-delete text-danger api-listing-delete";
            if(!hasValidScopes("/apis/{apiId}", "delete")) {
                cssDelete = "cu-reg-btn btn-delete text-danger api-listing-delete not-active";
            }
            var delete_button = $('<a>', {id: data.id})
                .text('Delete ')
                .addClass(cssDelete)
                .append(icon.clone().removeClass("fw-edit").addClass("fw-delete"));

            return $('<div></div>').append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

    /**
     * Return the data which need to render, filter or other action related to Name column in the table
     * @param data {object} Data need to be sent to the server
     * @param type {string} Type of the action currently performing i:e display, filter ect
     * @param row {object} Data for the currently processing row
     * @returns {*} {string} HTML for name column if display action name data if filtering action
     * @private
     */
    function _renderNameLink(data, type, row) {
        if (type === "display") {
            var icon = $("<i>").addClass("fw");
            var name_link = $('<a>', {href: data.id})
                .text(" " + data.name)
                .prepend(icon.addClass("fw-2x fw-api"));
            return $('<div></div>').append(name_link).html();
        } else if (type === "filter") {
            return data.name;
        } else {
            return data;
        }
    }

    function _renderStatus(data, type, row) {
        if (type === "display") {
            var status_element = $('<span>')
                .text(data.lifeCycleStatus)
                .addClass("label label-success");
            if(data.workflowStatus === "PENDING") {
                var wf_element = $('<span>')
                    .text("Pending")
                    .addClass("label label-pending");
                return $('<h4>').append(status_element).append("&nbsp;").append(wf_element).html();
            } else {
                return $('<h4>').append(status_element).html();
            }   
        } else if (type === "filter") {
            return data.lifeCycleStatus;
        } else {
            return data;
        }
    }
}

function apiResponseToData(response) {
    var raw_data = {
        data: response.obj.list
    };
    return raw_data;
}

