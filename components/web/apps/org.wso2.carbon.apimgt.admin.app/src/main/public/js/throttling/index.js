$(function () {

    var bearerToken = "Basic YWRtaW46YWRtaW4=";
    var swaggerClient = new SwaggetClient({
        url:swaggerUrl,
        succss: function (swaggerData) {
            setAuthHeader(swaggerData);
            swaggerClient["Throttling Tier Collection"].get_policies({"responseContentType": 'application/json'},
            function (jsonData) {
                var raw_data = {
                    data: jsonData.obj.list
                };
                var callbacks = {
                    onSuccess: function () {

                    },
                    onFailure: function (message, e) {

                    }
                };

            })

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