$(function () {

    var policyInstance = new Policy();

    var promised_get_tiers =  policyInstance.getAllApplicationPolicies();

    var APPLICATION = "application";

    promised_get_tiers.then(function (response) {
        var raw_data = {
            data: response.obj
        };

        var callbacks = {
            onSuccess: function () {
                _initDataTable(raw_data.data.list);
            },
            onFailure: function (message, e) {

            }
        };
        var mode = "OVERWRITE";
        var obj = {};
        obj.applicationTier = true;
        obj.list = response.obj;
        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.admin.feature.policy-view", obj,
                                 "policy-view", mode, callbacks);
        promised_get_tiers.catch(
                function (error) {
                    if (error.status == 401) {
                        redirectToLogin(contextPath);
                    }
                }
        );
    })

        $(document).on('click', 'a.deletePolicy', function () {
            var policyId = $(this).attr("data-uuid");
            var type = "alert";
            var layout = "topCenter";

            noty({
                     text : "Do you want to delete the policy",
                     type : type,
                     dismissQueue: true,
                     layout: layout,
                     theme: 'relax',
                     buttons : [
                         { addClass: 'btn btn-primary', text: 'Ok', onClick: function ($noty) {
                             $noty.close();
                             var promised_delete_tier =  policyInstance.deletePolicyByUuid("application", policyId);
                             promised_delete_tier.then(deletePolicySuccessCallback)
                                     .catch(function (error) {
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
                $noty.close();}
            }
            ]
        })})

    function _initDataTable(raw_data) {
        $('#api-policy').DataTable({
            data: raw_data,
            columns: [
                {'data': "displayName"},
                {'data': "description"},
                {'data': "defaultLimit.unitTime"},
                {'data': "defaultLimit.timeUnit"},
                {'data': "displayName"}
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
                href: "",
                title: 'Edit'
            })
                    .attr("data-uuid", row.policyId)
                    .addClass("btn btn-sm padding-reduce-on-grid-view tier-edit")
                    .append(editSpanIcon)
                    .append(editSpanText);

            var deleteIcon1 = $("<i>").addClass("fw fw-rin  g fw-stack-2x");
            var deleteIcon2 = $("<i>").addClass("fw fw-delete fw-stack-1x");
            var deleteSpanIcon = $("<span>").addClass("fw-stack").append(deleteIcon1).append(deleteIcon2);
            var deleteSpanText = $("<span>").addClass("hidden-xs").text("delete");
            var delete_button = $('<a>', {id: data, href: '#', 'data-id': data, title: 'delete'})
                    .attr("data-uuid", row.policyId)
                    .addClass("btn btn-sm padding-reduce-on-grid-view deletePolicy")
                    .append(deleteSpanIcon)
                    .append(deleteSpanText);
            return $('<div></div>').append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

    function deletePolicySuccessCallback(response) {
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
                 callback: {
                     afterClose: function () {
                         window.location = contextPath + "/throttling/application-throttling";
                     },
                 }
             });
    }


    $(document).on('click', ".tier-edit", function (e) {
        e.preventDefault();
        var policyId = $(this).data('uuid');
        var policy_obj = policyInstance.getPoliciesByUuid(policyId, APPLICATION);

        policy_obj.then(function (response) {
            var raw_data = {
                data: response.obj
            };
            var callbacks = {
                onSuccess: function () {

                },
                onFailure: function (message, e) {

                }
            };
            var mode = "OVERWRITE";
            var obj = {};
            obj.list = response.obj;
            UUFClient.renderFragment("org.wso2.carbon.apimgt.web.admin.feature.policy-add?application=true&update=true", obj,
                "policy-view", mode, callbacks);
        });
        policy_obj.catch(
            function (error) {
                console.log("Error occurred while loading swagger definition");
                if (error.status == 401) {
                    redirectToLogin(contextPath);
                }
            }
        );
    });

    $(document).on('click', "#addThrottleBtn", function (e) {
        var apiPolicyString = JSON.stringify(apiPolicy);
        var apiPolicyNew = JSON.parse(apiPolicyString);
        var policyId = $('#policy-id').val();
        var policyName = $('#policy-name').val();
        var policyDescription = htmlEscape($('#policy-description').val());
        var policyLevel = $("#policy-level option:selected").val();
        var defaultPolicyType = $('input[name=select-quota-type]:checked').val();
        var defaultPolicyLimit;
        var defaultPolicyUnit;
        var defaultPolicyUnitTime;
        var requiredMsg = $('#errorMsgRequired').val();
        var errorHasSpacesMsg = $('#errorMessageSpaces').val();

        apiPolicyNew.policyId = policyId;
        apiPolicyNew.policyName = policyName;

        if (!validateInput(policyName, $('#policy-name'), requiredMsg)) {
            return false;
        }

        if (!validateForSpaces(policyName, $('#policy-name'), errorHasSpacesMsg)) {
            return false;
        }

        apiPolicyNew.policyDescription = policyDescription;
        apiPolicyNew.policyLevel = policyLevel;
        apiPolicyNew.defaultQuotaPolicy.type = defaultPolicyType;

        var defaultPolicyDataUnit;
        if (defaultPolicyType == 'requestCount') {
            defaultPolicyLimit = $('#request-count').val();
            defaultPolicyUnit = $("#request-count-unit option:selected").val();
            defaultPolicyUnitTime = $("#unit-time-count").val();
            apiPolicyNew.defaultQuotaPolicy.limit.requestCount = defaultPolicyLimit;
            apiPolicyNew.defaultQuotaPolicy.limit.unitTime = defaultPolicyUnitTime;
            apiPolicyNew.defaultQuotaPolicy.limit.timeUnit = defaultPolicyUnit;

            if (!validateInput(defaultPolicyLimit, $('#request-count'), requiredMsg)) {
                return false;
            }
            if (!validateInput(defaultPolicyUnitTime, $('#unit-time-count'), requiredMsg)) {
                return false;
            }
            if (!validateInput(defaultPolicyUnit, $("#request-count-unit option:selected"), requiredMsg)) {
                return false;
            }
        } else {
            defaultPolicyLimit = $('#bandwidth').val();
            defaultPolicyDataUnit = $("#bandwidth-unit option:selected").val();
            defaultPolicyUnitTime = $("#unit-time-count").val();
            defaultPolicyUnit = $("#request-count-unit option:selected").val();
            apiPolicyNew.defaultQuotaPolicy.limit.dataAmount = defaultPolicyLimit;
            apiPolicyNew.defaultQuotaPolicy.limit.unitTime = defaultPolicyUnitTime;
            apiPolicyNew.defaultQuotaPolicy.limit.dataUnit = defaultPolicyDataUnit;
            apiPolicyNew.defaultQuotaPolicy.limit.timeUnit = defaultPolicyUnit;

            if (!validateInput(defaultPolicyLimit, $('#bandwidth'), requiredMsg)) {
                return false;
            }

            if (!validateInput(defaultPolicyDataUnit, $("#bandwidth-unit option:selected"), requiredMsg)) {
                return false;
            }

            if (!validateInput(defaultPolicyUnitTime, $("#unit-time-count"), requiredMsg)) {
                return false;
            }

            if (!validateInput(defaultPolicyUnit, $("#request-count-unit option:selected"), requiredMsg)) {
                return false;
            }
        }
        var policy = {};
        policy.policyId = apiPolicyNew.policyId;
        policy.name = apiPolicyNew.policyName;
        policy.name = apiPolicyNew.policyName;
        policy.description = apiPolicyNew.policyDescription;
        policy.tierLevel = APPLICATION; // from send to client.
        policy.unitTime = parseInt(apiPolicyNew.defaultQuotaPolicy.limit.unitTime);
        policy.timeUnit = apiPolicyNew.defaultQuotaPolicy.limit.timeUnit;
        policy.stopOnQuotaReach = true;
        policy.requestCount = defaultPolicyLimit;

        var policyInstance = new Policy();
        var promised_update = policyInstance.update(policy);
        promised_update
            .then(createPolicyCallback)
            .catch(
            function (error_response) {
                var error_data = JSON.parse(error_response.data);
                var message = "Error[" + error_data.code + "]: " + error_data.description + " | " + error_data.message + ".";
                noty({
                    text: message,
                    type: 'error',
                    dismissQueue: true,
                    modal: true,
                    closeWith: ['click', 'backdrop'],
                    progressBar: true,
                    timeout: 5000,
                    layout: 'top',
                    theme: 'relax',
                    maxVisible: 10
                });
                $('[data-toggle="loading"]').loading('hide');
                console.debug(error_response);
            });
    });
})