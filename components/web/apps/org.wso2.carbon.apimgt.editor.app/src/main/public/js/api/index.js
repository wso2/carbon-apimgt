$(function () {
    var api = new API();
    api.getAll(getAPIsCallback);
    $(document).on('click', ".api-listing-delete", {api_instance: api}, deleteAPIHandler); // Event-type, Selector, data, method
});

function getAPIsCallback(response) {
    // var context = {apis: response.obj.list};
    // var mode = "OVERWRITE"; // Available modes [OVERWRITE,APPEND, PREPEND]
    // UUFClient.renderFragment("org.wso2.carbon.apimgt.web.editor.feature.api-listing", context, "apiListingContainer", mode);
    var dt_data = _apiResponseToData(response);
    _initDataTable(dt_data);

}

function deleteAPIHandler(event) {
    let data_table = $('#apim-publisher-listing').DataTable();
    let current_row = data_table.row($(this).closest('tr'));
    let api_id = current_row.data().id;
    let api = event.data.api_instance;
    api.deleteAPI(api_id); /*TODO: Need to handle success and error cases ~tmkb */
    current_row.remove();
    data_table.draw();
}

function _initDataTable(raw_data) {
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
                targets: ["api-listing-name"], //class name will be matched on the TH for the column
                searchable: true,
                render: _renderNameLink // Method to render the action buttons per row
            },
            {
                targets: ["api-listing-status"], //class name will be matched on the TH for the column
                render: _renderStatus // Method to render the action buttons per row
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

function _apiResponseToData(response) {
    var raw_data = {
        data: response.obj.list
    };

    return raw_data;
}

