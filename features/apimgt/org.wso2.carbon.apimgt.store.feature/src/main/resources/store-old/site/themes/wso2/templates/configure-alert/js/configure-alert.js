$(function() {
    var appsElement = $("#appName");
    var apisElement = $("#apiName");
    var submitForm = $("#submitConfigForm");
    var applicationList = {};
    populateAppList();

    /**
     * Populate Application Name 'select' element with subscribed application list
    **/
    function populateAppList() {
        var params = {action: "getApplicationsForUser"};
        
        $.get("../blocks/configure-alert/ajax/configure-alert.jag", params, function(data) {
            if (data && data.error == false) {
                applicationList = data.applications;

                $.each(data.applications, function() {
                    appsElement.append($("<option />").val(this.id).text(this.name));
                });

                appsElement.selectpicker('refresh');
                populateApiList();
            }
        });
    }

    /**
     * Populate API Name 'select' element with subscribed list of APIs
     * apiName--apiVersion format is used to list the APIs. This is to
     * avoid complexity in the UI and the logic required to show the API list
    **/
    function populateApiList() {
        var appId = $("#appName option:selected").text();
        var params = {action: "getSubscriptionByApplication", app: appId, groupId: ""};
        apisElement.empty();

        $.get("../blocks/subscription/subscription-list/ajax/subscription-list.jag", params, function(data) {
            if (data && data.error == false) {
                $.each(data.apis, function() {
                    var apiId = this.apiName + '--' + this.apiVersion;
                    apisElement.append($("<option />").val(apiId).text(apiId));
                });

                apisElement.selectpicker('refresh');
            }
        });
    }

    /**
     * Remove an alert configuration
    **/
    function deleteConfig(appId, apiName, apiVersion) {
        jagg.post("/site/blocks/configure-alert/ajax/configure-alert.jag", {
            action:"removeAlertConfig",
            applicationId:appId,
            apiName: apiName,
            apiVersion: apiVersion,
        }, function (result) {
            if (!result.error) {
                window.location.reload(true);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    }

    // Validate and submit the form
    var validator = $("#submitConfigForm").validate({
        rules: {
            threshold: {
                required: true,
                number: true
            }
        },
        submitHandler: function (form) {
            var api = apisElement.val();
            var splitApi = apisElement.val().split('--');
            var threshold = $("#threshold").val();

            jagg.post("/site/blocks/configure-alert/ajax/configure-alert.jag", {
                action: "configureAlert",
                applicationId: appsElement.val(),
                apiName: splitApi[0],
                apiVersion: splitApi[1],
                threshold: threshold
            }, function(result) {
                if (result.error) {
                    jagg.message({content:result.message,type:"error"});
                } else {
                    window.location.reload();
                }
            });
        }
    });

    // Button click listener for remove alert config button
    $('#configTable').on( 'click', 'a.deleteConfig', function () {
        var appId = $(this).attr("data-id");
        var apiName = $(this).attr("data-name");
        var apiVersion = $(this).attr("data-version");

        jagg.message({
            content: i18n.t('Are you sure you want to remove the configuration?'),
            type: 'confirm',
            okCallback: function () {
                deleteConfig(appId, apiName, apiVersion);
            }
        });
    });

    // Data table configuration for loading configured alert information
    $('#configTable').datatables_extended({
        "ajax": {
            "url": jagg.getBaseUrl()+ "/site/blocks/configure-alert/ajax/configure-alert.jag?action=getAlertConfigs",
            "dataSrc": function (json) {
                if (json.error) {
                    return {};
                }

                // Find the application name of each applicationId in the result
                // This is required to populate the application name in the table
                var configs = json.list;
                configs.forEach(function(config) {
                    applicationList.forEach(function(app) {
                        if (app.id == config.applicationId) {
                            config.applicationName = app.name;
                        }
                    });
                });

                return configs;
            }
        },
        "columns": [
            {"data": "applicationName"},
            {"data": "apiName"},
            {"data": "apiVersion"},
            {"data": "thresholdRequestCountPerMin"},
            {
                "data": "apiName",
                "render": function (data, type, rec, meta) {

                    return '<a href="#" title="' + i18n.t("Remove") + '" class="btn btn-sm padding-reduce-on-grid-view deleteConfig"' +
                            'data-id="' + rec.applicationId + '" data-name="' + rec.apiName + '" data-version="' + rec.apiVersion + '">' +
                                '<span class="fw-stack">' +
                                    '<i class="fw fw-ring fw-stack-2x"></i>' +
                                    '<i class="fw fw-delete fw-stack-1x"></i>' +
                                '</span>' +
                                '<span class="hidden-xs">' + i18n.t("Remove") + '</span>' +
                            '</a>';
                }
            }
        ],
    });

    // Register on change event listener for Application Name 'select' element
    appsElement.on('changed.bs.select', function (e) {
        populateApiList();
    });

});
