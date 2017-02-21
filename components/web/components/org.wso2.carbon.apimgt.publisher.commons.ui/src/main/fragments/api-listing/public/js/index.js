$(function () {
    var api = new API();
    api.getAll(getAPIsCallback);
    $(document).on('click', ".api-listing-delete", {api_instance: api}, deleteAPIHandler); // Event-type, Selector, data, method
});

/**
 * Callback method to handle apis data after receiving them via the REST API
 * @param response {object} Raw response object returned from swagger client
 */
function getAPIsCallback(response) {
    var dt_data = apiResponseToData(response);
    initDataTable(dt_data);
}

/**
 * Handler method to handle the `onclick` event of the delete button
 * Send delete API call and remove the row from data tables
 * @param event {object} Click event
 */
function deleteAPIHandler(event) {
    let data_table = $('#apim-publisher-listing').DataTable();
    let current_row = data_table.row($(this).closest('tr'));
    let api_id = current_row.data().id;
    let api = event.data.api_instance;
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
            var edit_button = $('<a>', {id: data.id, href: data.id})
                .text('Edit ')
                .addClass("cu-reg-btn btn-edit text-warning")
                .append(icon.addClass("fw-edit"));
            var delete_button = $('<a>', {id: data.id})
                .text('Delete ')
                .addClass("cu-reg-btn btn-delete text-danger api-listing-delete")
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
            return $('<h4>').append(status_element).html();
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

