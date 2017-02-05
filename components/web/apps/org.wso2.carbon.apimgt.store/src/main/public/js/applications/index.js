$(function () {

    $(".navigation ul li.active").removeClass('active');
    var prev = $(".navigation ul li:first")
    $(".green").insertBefore(prev).css('top', '0px').addClass('active');

    _renderTopNavBar();

    var bearerToken = "Basic YWRtaW46YWRtaW4=";
    var swaggerClient = new SwaggerClient({
        url: swaggerURL,
        success: function (swaggerData) {
            //TODO:Need to have a proper fix from swagger definition retrieval service
            swaggerClient.setSchemes(["http"]);
            swaggerClient.setHost("localhost:9090");
            //List Applications
            swaggerClient["Application Collection"].get_applications({"responseContentType": 'application/json'},
                function (jsonData) {

                    var raw_data = {
                        data: jsonData.obj.list
                    };

                    var callbacks = {
                        onSuccess: function () {
                            _initDataTable(raw_data);

                        }, onFailure: function (message, e) {
                        }
                    };
                    var mode = "OVERWRITE";
                    //Render Applications listing page
                    UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.applications-list", jsonData.obj,
                        "applications-list", mode, callbacks);

                    //Delete Application
                    $(document).on('click', 'a.deleteApp', function () {
                        alert("Are you sure you want to delete Application");

                        var appId = $(this).attr("data-id")

                        swaggerClient["Application (individual)"].delete_applications_applicationId({"applicationId": appId},
                            function (success) {
                                //TODO: Reload element only
                                window.location.reload(true);
                            },
                            function (error) {
                                alert("Error occurred while deleting application")
                            });
                    })

                });


        }
    });

    function _initDataTable(raw_data) {
        $('#application-table').DataTable({
            ajax: function (data, callback, settings) {
                callback(raw_data);
            },
            columns: [
                {'data': 'name'},
                {'data': 'throttlingTier'},
                {'data': 'status'},
                {'data': 'subscriber'},
                {'data': 'applicationId'},
            ],
            columnDefs: [
                {
                    targets: ["application-listing-action"], //class name will be matched on the TH for the column
                    searchable: false,
                    sortable: false,
                    render: _renderActionButtons // Method to render the action buttons per row
                }
            ]
        });
    }

    function _renderActionButtons(data, type, row) {
        if (type === "display") {
            var viewIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
            var viewIcon2 = $("<i>").addClass("fw fw-view fw-stack-1x");
            var viewSpanIcon = $("<span>").addClass("fw-stack").append(viewIcon1).append(viewIcon2);
            var viewSpanText = $("<span>").addClass("hidden-xs").text("View");
            var view_button = $('<a>', {id: data, href: contextPath + '/applications/' + data, title: 'View'})
                .addClass("btn  btn-sm padding-reduce-on-grid-view")
                .append(viewSpanIcon)
                .append(viewSpanText);

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
            return $('<div></div>').append(view_button).append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

    function _renderTopNavBar() {
        var data = {};
        data.isApplicationList = true;
        var mode = "OVERWRITE";
        //Render Applications listing page
        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.store.feature.top-navbar", data,
            "top-navbar", mode, {
                onSuccess: function () {

                }, onFailure: function (message, e) {
                }
            });
    }
});




