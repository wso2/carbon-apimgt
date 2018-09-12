$(function() {
    var apisElement = $("#apiName");
    var submitForm = $("#submitConfigForm");
    var alertType = $("#alertType").attr("data-type");
    populateApiList();

    /**
     * Populate API Name 'select' element with the providers API list
     * apiName--apiVersion format is used to list the APIs. This is to
     * avoid complexity in the UI and the logic required to show the API list
    **/
    function populateApiList() {
        $.get("../blocks/configure-alert/ajax/configure-alert.jag", {action: "getAPIs"}, function(data) {
            if (data && data.error == false) {
                $.each(data.apis, function() {
                    var apiId = this.name + '--' + this.version;
                    apisElement.append($("<option />").val(apiId).text(apiId));
                });

                apisElement.selectpicker('refresh');
            }
        });
    }

    /**
     * Remove an alert configuration
    **/
    function deleteConfig(apiName, apiVersion) {
        jagg.post("/site/blocks/configure-alert/ajax/configure-alert.jag", {
            action:"removeAlertConfig",
            apiName: apiName,
            apiVersion: apiVersion,
            alertType: alertType
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
                apiName: splitApi[0],
                apiVersion: splitApi[1],
                alertType: alertType,
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
        var apiName = $(this).attr("data-name");
        var apiVersion = $(this).attr("data-version");

        jagg.message({
            content: i18n.t('Are you sure you want to remove the configuration?'),
            type: 'confirm',
            okCallback: function () {
                deleteConfig(apiName, apiVersion);
            }
        });
    });

    // Data table configuration for loading configured alert information
    $('#configTable').datatables_extended({
        "ajax": {
            "url": jagg.url("/site/blocks/configure-alert/ajax/configure-alert.jag?action=getAlertConfigs&alertType=" + alertType),
            "dataSrc": function (json) {
                if (json.error) {
                    return {};
                }

                return json.list;
            }
        },
        "columns": [
            {"data": "apiName"},
            {"data": "apiVersion"},
            {"data": "value"},
            {
                "data": "action",
                "render": function (data, type, rec, meta) {

                    return '<a href="#" title="' + i18n.t("Remove") + '" class="btn btn-sm deleteConfig"' +
                            'data-name="' + rec.apiName + '" data-version="' + rec.apiVersion + '">' +
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

});
