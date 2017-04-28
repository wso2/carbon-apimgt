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
            //List Applications
            setAuthHeader(swaggerClient);
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

                        var appId = $(this).attr("data-id");
                        var type="alert";
                        var layout="topCenter";
                        noty({
                            text : "Do you want to delete the application",
                            type : type,
                            dismissQueue: true,
                            layout : layout,
                            theme : 'relax',
                            buttons : [
                                {addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                                    $noty.close();
                                    setAuthHeader(swaggerClient);
                                    swaggerClient["Application (individual)"].delete_applications_applicationId({"applicationId": appId},
                                        function (success) {
                                            //TODO: Reload element only
                                            var message = "Application deleted successfully";
                                            noty({
                                                text: message,
                                                type: 'success',
                                                dismissQueue: true,
                                                modal: true,
                                                progressBar: true,
                                                timeout: 3000,
                                                layout: 'top',
                                                theme: 'relax',
                                                maxVisible: 10,
                                            });
                                            setTimeout(function(){ window.location.reload(true); }, 3000);


                                        },
                                        function (error) {
                                            var message = "Error occurred while deleting application";
                                            noty({
                                                text: message,
                                                type: 'warning',
                                                dismissQueue: true,
                                                modal: true,
                                                progressBar: true,
                                                timeout: 2000,
                                                layout: 'top',
                                                theme: 'relax',
                                                maxVisible: 10,
                                            });
                                        });
                                }
                                },
                                {addClass: 'btn btn-danger', text: 'Cancel', onClick: function ($noty) {
                                    $noty.close();
                                }
                                }
                            ]
                        });

                    })

                },
                function (error) {
                    if(error.status==401){
                        redirectToLogin(contextPath);
                    }
                }
            );


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
                {'data': 'lifeCycleStatus'},
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

            if ( row.lifeCycleStatus == "APPROVED" ) {
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

            }

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




