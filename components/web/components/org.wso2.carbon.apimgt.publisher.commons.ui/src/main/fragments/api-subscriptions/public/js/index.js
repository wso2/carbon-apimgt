function getSubscriptionsCallback(response) {
    var dt_data = subscriptionResponseToData(response);
    if (dt_data.data.length > 0) {
        $('#subscriptions-welcome-message').addClass('hidden');
        $('#subscriptions-listing-container').removeClass('hidden');
        initDataTable(dt_data);
    }

}

function blockSubscriptionsHandler(event) {
    var subscription_id = this.id;
    var key_type = $('#' + subscription_id).val();
    var api_client = event.data.api_instance;
    var data_table = $('#subscriptions-listing').DataTable();
    var current_row = data_table.row($(this).parents('tr'));
    var promised_update = api_client.blockSubscriptions(subscription_id, key_type);
    promised_update.then(
        function (response) {
            var message = "Subscription blocked successfully.";
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
            var dt_data = response.obj;
            current_row.invalidate();
            current_row.data(dt_data);
            data_table.draw();
        }
    );
    promised_update.catch(
        function (error_response) {
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
}

function unblockSubscriptionsHandler(event) {
    var subscription_id = this.id;
    var api_client = event.data.api_instance;
    var data_table = $('#subscriptions-listing').DataTable();
    var current_row = data_table.row($(this).parents('tr'));
    var promised_update = api_client.unblockSubscriptions(subscription_id);
    promised_update.then(
        function (response) {
            var message = "Subscription unblocked successfully.";
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
            var dt_data = response.obj;
            current_row.invalidate();
            current_row.data(dt_data);
            data_table.draw();
        }
    );
    promised_update.catch(
        function (error_response) {
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
}

$(function () {
    var api_client = new API();
    var api_id = $("#apiId").val();
    api_client.subscriptions(api_id, getSubscriptionsCallback);
    // Event-type, Selector, data, method
    $(document).on('click', ".block-subscription", {api_instance: api_client}, blockSubscriptionsHandler);
    $(document).on('click', ".unblock-subscription", {api_instance: api_client}, unblockSubscriptionsHandler);
});


function initDataTable(raw_data) {
    $('#subscriptions-listing').DataTable({
        ajax: function (data, callback, settings) {
            callback(raw_data);
        },
        destroy: true,
        columns: [
            {'data': 'applicationInfo.name'},
            {'data': 'applicationInfo.subscriber'},
            {'data': 'subscriptionTier'},
            {'data': null},
            {'data': null},
            {'data': null}
        ],
        columnDefs: [
            {
                targets: ["subscription-listing-environment"], //class name will be matched on the TH for the column
                searchable: false,
                sortable: false,
                render: _renderKeyTypes // Method to render the action buttons per row
            },
            {
                targets: ["subscription-listing-action"], //class name will be matched on the TH for the column
                searchable: false,
                sortable: false,
                render: _renderActionButtons // Method to render the action buttons per row
            },
            {
                targets: ["subscription-listing-status"], //class name will be matched on the TH for the column
                searchable: false,
                sortable: false,
                render: _renderStatus // Method to render the action buttons per row
            }
        ]
    });

    function _renderActionButtons(data, type, row) {
        if (type === "display") {
            var status = data.subscriptionStatus;
            var icon = $("<i>").addClass("fw");
            var action_button;
            if (status === "ACTIVE") {
                action_button = $('<a>', {id: data.subscriptionId})
                    .text('BLOCK ')
                    .addClass("cu-reg-btn btn-edit text-danger block-subscription")
                    .append(icon.addClass("fw-block"));
            } else {
                action_button = $('<a>', {id: data.subscriptionId})
                    .text('UNBLOCK ')
                    .addClass("cu-reg-btn btn-edit text-success unblock-subscription")
                    .append(icon.clone().removeClass("fw-block").addClass("fw-activate"));
            }
            return $('<div></div>').append(action_button).html();
        } else {
            return data;
        }
    }

    function _renderKeyTypes(data, type, row) {
        if (type === "display") {
            var key_types = $('<select  id="' + data.subscriptionId + '"></select>');
            var status = data.subscriptionStatus;
            var options;
            if (status === "ACTIVE") {
                options = '<option value="PROD_ONLY_BLOCKED">PRODUCTION</option>' +
                    '<option value="SANDBOX_ONLY_BLOCKED">SANDBOX</option>' +
                    '<option value="BLOCKED">PRODUCTION & SANDBOX</option>';
            } else if (status === "BLOCKED") {
                options = '<option value="BLOCKED" selected="selected" disabled>PRODUCTION & SANDBOX</option>';
            } else if (status === "PROD_ONLY_BLOCKED") {
                options = '<option value="PROD_ONLY_BLOCKED" selected="selected" disabled>PRODUCTION</option>';
            } else if (status === "SANDBOX_ONLY_BLOCKED") {
                options = '<option value="SANDBOX_ONLY_BLOCKED" selected="selected" disabled>SANDBOX</option>';
            }
            key_types.append(options);
            return $('<div></div>').append(key_types).html();
        } else if (type === "filter") {
            return data.subscriptionStatus;
        } else {
            return data;
        }
    }

    function _renderStatus(data, type, row) {
        if (type === "display") {
            var status = data.subscriptionStatus;
            var status_element;
            if (status === "ACTIVE") {
                status_element = $('<span>')
                    .text(data.subscriptionStatus)
                    .addClass("label label-success");
            } else {
                status_element = $('<span>')
                    .text(data.subscriptionStatus)
                    .addClass("label label-danger");
            }
            return $('<h4>').append(status_element).html();
        } else if (type === "filter") {
            return data.subscriptionStatus;
        } else {
            return data;
        }
    }
}

function subscriptionResponseToData(response) {
    var raw_data = {
        data: response.obj.list
    };
    return raw_data;
}
