$(function () {
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            setAuthHeader(swaggerClient);
            swaggerClient["Throttling Tier (Collection)"].get_policies_tierLevel({"tierLevel":"api"},
                                                                       {"responseContentType": 'application/json'},
            function (jsonData) {
                var raw_data = {
                    data: jsonData.obj
                };
                var callbacks = {
                    onSuccess: function () {
                        _initDataTable(raw_data);
                    },
                    onFailure: function (message, e) {

                    }
                };
                var mode = "OVERWRITE";
                var obj = {};
                obj.list=jsonData.obj;
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.admin.feature.policy-view", obj,
                                         "policy-view", mode, callbacks);
            }, function (error) {
                        if (error.status == 401) {
                            redirectToLogin(contextPath);
                        }
                    }
            );
        },
        failure: function (error) {
            console.log("Error occurred while loading swagger definition");
        }
    });

    function _initDataTable(raw_data) {
        debugger;
        $('#api-policy').DataTable({
            ajax: function (data, callback, settings) {
                callback(raw_data);
            },
            columns: [
                {'data': 'name'},
                {'data': 'description'},
                {'data': 'name'}
            ],
            columnDefs: [
                {
                    targets: ["policy-listing-action"], //class name will be matched on the TH for the column
                    searchable: false,
                    sortable: false,
                    render: _renderActionButtons // Method to render the action buttons per row
                }
            ]
        })
    }

    function _renderActionButtons(data, type, row) {
        if (type === "display") {

            var editIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
            var editIcon2 = $("<i>").addClass("fw fw-edit fw-stack-1x");
            var editSpanIcon = $("<span>").addClass("fw-stack").append(editIcon1).append(editIcon2);
            var editSpanText = $("<span>").addClass("hidden-xs").text("Edit");
            var edit_button = $('<a>', {
                id: data.id,
                href: contextPath + '/applications/' + data + '/edit',
                title: 'Edit'
            })
                    .addClass("btn  btn-sm padding-reduce-on-grid-view")
                    .append(editSpanIcon)
                    .append(editSpanText);

            var deleteIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
            var deleteIcon2 = $("<i>").addClass("fw fw-delete fw-stack-1x");
            var deleteSpanIcon = $("<span>").addClass("fw-stack").append(deleteIcon1).append(deleteIcon2);
            var deleteSpanText = $("<span>").addClass("hidden-xs").text("delete");
            var delete_button = $('<a>', {id: data, href: '#', 'data-id': data, title: 'delete'})
                    .addClass("btn btn-sm padding-reduce-on-grid-view deleteApp")
                    .append(deleteSpanIcon)
                    .append(deleteSpanText);
            return $('<div></div>').append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

})