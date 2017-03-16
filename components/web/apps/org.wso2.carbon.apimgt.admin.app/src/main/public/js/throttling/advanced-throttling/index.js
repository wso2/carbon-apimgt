$(function () {
    debugger;
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            setAuthHeader(swaggerData);
            swaggerClient["Throttling Tier Collection"].get_policies({"responseContentType": 'application/json'},
            function (jsonData) {
                var raw_data = {
                    data: jsonData.obj.list
                };
                var callbacks = {
                    onSuccess: function () {
                        _initDataTable(raw_data);

                    },
                    onFailure: function (message, e) {

                    }
                };
                var mode = "OVERWRITE";
                UUFClient.renderFragment("org.wso2.carbon.apimgt.web.admin.feature.policy-view", jsonData.obj,
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
        $('#api-policy').DataTable({
            ajax: function (data, callback, setting) {
                callback(raw_data);

            },
            columns: [
                {'data': 'name'},
                {'data': 'quotaPolicy'},
                {'data': 'quota'},
                {'unitTime': 'unitTime'}
            ],
            columnDefs: [
                {
                    targets: ["application-listing-action"], //class name will be matched on the TH for the column
                    searchable: false,
                    sortable: false
                }
            ]
        })

    }


})