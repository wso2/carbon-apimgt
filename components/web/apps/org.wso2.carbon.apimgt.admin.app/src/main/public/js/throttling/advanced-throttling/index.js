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