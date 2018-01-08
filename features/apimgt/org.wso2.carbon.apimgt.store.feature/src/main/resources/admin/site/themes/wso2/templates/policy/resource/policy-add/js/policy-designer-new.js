var index = 0;
$.fn.editableform.buttons =
    '<button type="submit" class="btn btn-primary btn-sm editable-submit">' +
    '<i class="fw fw-check"></i>' +
    '</button>' +
    '<button type="button" class="btn btn-secondary btn-sm editable-cancel">' +
    '<i class="fw fw-cancel"></i>' +
    '</button>';

Handlebars.registerHelper('if_eq', function (a, b, opts) {
    if (a == b) {
        return opts.fn(this);
    } else {
        return opts.inverse(this);
    }
});

var apiPolicy =
{
    "policyName": "",
    "policyLevel": "",
    "policyDescription": "",
    "executionFlows": [],
    "defaultQuotaPolicy": {
        "type": "",
        "limit": {
            "requestCount": 0,
            "timeUnit": "",
            "dataAmount": 0,
            "dataUnit": ""
        }
    }
};

var changeStatusIcon = function (flowId, type, checkBoxOb) {
    var spanId;
    var iconId;
    if (type == "IP") {
        spanId = "ip-condition-configured-" + flowId;
        iconId = "ip-condition-configured-icon-" + flowId;
    } else if (type == "Header") {
        spanId = "header-condition-configured-" + flowId;
        iconId = "header-condition-configured-icon-" + flowId;
    } else if (type == "QueryParam") {
        spanId = "queryparam-condition-configured-" + flowId;
        iconId = "queryparam-condition-configured-icon-" + flowId;
    } else if (type == "JWTClaim") {
        spanId = "claim-condition-configured-" + flowId;
        iconId = "claim-condition-configured-icon-" + flowId;
    } else {
        return;
    }

    if ($(checkBoxOb).is(":checked")) {
        $('#' + spanId).removeClass('has-configured');
        $('#' + iconId).removeClass('fw-circle-outline');
        $('#' + spanId).addClass('has-success');
        $('#' + iconId).addClass('fw-check');
    } else {
        $('#' + spanId).removeClass('has-success');
        $('#' + iconId).removeClass('fw-check');
        $('#' + spanId).addClass('has-configured');
        $('#' + iconId).addClass('fw-circle-outline');
    }
};

var addPolicy = function () {

    var executionFlow = {
        "id": 0,
        "enabled": false,
        "description": "",
        "quotaPolicy": {
            "type": "",
            "limit": {
                "requestCount": 5,
                "unitTime": 1,
                "timeUnit": "min",
                "dataAmount": 1,
                "dataUnit": "KB"
            }
        },
        "conditions": [
            {
                "type": "IP",
                "ipType": "specific",
                "startingIP": "",
                "endingIP": "",
                "specificIP": "",
                "invertCondition": false,
                "enabled": false
            },
            {
                "type": "Header",
                "keyValPairs": [],
                "invertCondition": false,
                "enabled": false
            },
            {
                "type": "QueryParam",
                "keyValPairs": [],
                "invertCondition": false,
                "enabled": false
            },
            {
                "type": "JWTClaim",
                "keyValPairs": [],
                "invertCondition": false,
                "hasValues": false,
                "enabled": false
            }
        ]
    };

    var source = $("#designer-policy-template").html();
    executionFlow.id = index;
    executionFlow.description = "Sample description about condition group";
    Handlebars.partials['designer-policy-template'] = Handlebars.compile(source);
    var context = {
        "executionFlow": executionFlow
    };
    var output = Handlebars.partials['designer-policy-template'](context);
    $('#pipeline-content').append(output);
    $('#executionFlow-desc-' + index).editable();
    apiPolicy.executionFlows.push(executionFlow);
    console.log(apiPolicy);
    index++;
};

$(document).ready(function () {

    //validate requestCount is numeric
    $.validator.addMethod('requestCountValidator', function (value, element) {
        return !isNaN(value);
    }, "Error: Value Entered for Request Count is not valid!!");
    //validate time is numeric
    $.validator.addMethod('timeValidator', function (value, element) {
        return !isNaN(value);
    }, "Error: Value entered for unit time is not valid");
    //validate bandwidth is numberic
    $.validator.addMethod('bandwidthValidator', function (value, element) {
        return !isNaN(value);
    }, "Error: Value entered for bandwidth is not valid");

    $("form.form-horizontal").validate({
        rules: {
            'request-count': {
                requestCountValidator: true
            },
            'unit-time-count': {
                required: true,
                timeValidator: true
            },
            'bandwidth': {
                bandwidthValidator: true
            }
        }
    })


    $("#default-policy-level").change(function () {
        var selectedText = "";
        $("#default-policy-level option:selected").each(function () {
            selectedText = $(this).val();
            if (selectedText == 'bandwidthVolume') {
                $("#request-count-block").hide();
                $("#bandwidth-block").show();
            } else {
                $("#bandwidth-block").hide();
                $("#request-count-block").show();
            }
        });
    });
    $('body').on('click', '.editable-click', function (e) {
        e.stopPropagation();
    });
    $('body').on('click', '.editable-submit', function (e) {
        e.stopPropagation();
    });
});

var showAdvanceOperation = function (operation, button, id) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

var onIpConditionChange = function (id, optionTextOb) {
    var selectedText = $(optionTextOb).val();
    if (selectedText == 'IPRange') {
        $("#specific-ip-block-" + id).hide();
        $("#ip-range-block-" + id).show();
    } else {
        $("#ip-range-block-" + id).hide();
        $("#specific-ip-block-" + id).show();
    }
};

var onExecutionFlowTypeChange = function (id, optionTextOb) {
    var selectedText = $(optionTextOb).val();
    if (selectedText == 'requestCount') {
        $("#execution-flow-request-count-block-" + id).show();
        $("#execution-flow-bandwidth-block-" + id).hide();
    } else {
        $("#execution-flow-request-count-block-" + id).hide();
        $("#execution-flow-bandwidth-block-" + id).show();
    }
};

var onIPChange = function (IPtextEle) {
    //validate the IP
    var IP_value = $(IPtextEle).val();
    var blocks = IP_value.split(".");
    var elementId = $(IPtextEle)[0].id;
    var element = $('#' + elementId);

    if (blocks.length != 4 || !blocks.every(valid_block)) {
        element.css("border", "1px solid red");
        $('#label' + elementId).remove();
        element.parent().append('<label class="error" id="label' + elementId + '" >Invalid IP</label>');
        return false;
    }
    else {
        $('#label' + elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
};

var onEndIPChange = function (IPtextEle) {

    var validIP = onIPChange(IPtextEle);
    if (validIP) {
        var temp = $(IPtextEle)[0].id.split("-");
        var startIP = $('#ip-range-start-address-input-' + temp[5]).val();
        var IsValidIPRange = validIPRange(startIP, $(IPtextEle).val());
        var elementId = $(IPtextEle)[0].id;
        var element = $('#' + elementId);
        if (!IsValidIPRange) {
            element.css("border", "1px solid red");
            $('#label' + elementId).remove();
            element.parent().append('<label class="error" id="label' + elementId + '" >Invalid IP Range</label>');
        }
        else {
            $('#label' + elementId).remove();
            element.css("border", "1px solid #cccccc");
        }
    }
};

var valid_block = function (block) {

    if (block > 0 && block < 255) {
        return true;
    }
    return false;
};

var validIPRange = function (startIP, endIP) {

    if (startIP == null || endIP == null || startIP == "" || endIP == "") {
        return false;
    }
    var startIPBlocks = startIP.split(".");
    var endIPBlocks = endIP.split(".");

    for (var i = 0; i < 4; i++) {
        if (startIPBlocks[i] >= endIPBlocks[i]) {
            return false;
        }
    }
    return true;
};

var onDateConditionChange = function (id, optionTextOb) {
    var selectedText = $(optionTextOb).val();
    if (selectedText == 'dateRange') {
        $("#specific-date-block-" + id).hide();
        $("#date-range-block-" + id).show();
    } else {
        $("#date-range-block-" + id).hide();
        $("#specific-date-block-" + id).show();
    }
};

var removeQueryParamRow = function (rowObj, flowId) {
    $(rowObj).closest('tr').remove();
    var rowCount = $('#query-param-value-table-content-' + flowId + ' tr').length;
    //Using 1 here as even row is removed above the row count will not set to 0 during this flow
    if (rowCount <= 1) {
        $('#query-param-value-table-' + flowId).hide();
    }
};

var addQueryParam = function (id) {
    var source = $("#designer-query-pram-table-content").html();
    var paramName = $('#query-param-name-' + id).val();
    var paramValue = $('#query-param-value-' + id).val();
    var isDuplicate = false;
    var requiredMsg = $('#errorMsgRequired').val();

    if(!validateInput(paramName,  $('#query-param-name-' + id), requiredMsg)) {
        return false;
    }

    if(!validateInput(paramValue,   $('#query-param-value-' + id), requiredMsg)) {
        return false;
    }

    var table = $("#query-param-value-table-content-" + id + " > tbody");
    table.find('tr').each(function (k, el) {
        var $tds = $(this).find('td');
        queryParamName = $tds.eq(0).text();
        queryParamVal = $tds.eq(1).text();
        if(queryParamName == paramName) {
            addDuplicateError("duplicate", $('#query-param-name-' + id), "Duplicate Query Param");
            isDuplicate = true;
        }
    });

    if(isDuplicate) {
        return;
    }

    var tableRow = {
        "name": paramName,
        "value": paramValue,
        "flowId": id
    };

    Handlebars.partials['designer-query-pram-table-content'] = Handlebars.compile(source);
    var context = {
        "tableRow": tableRow
    };

    var output = Handlebars.partials['designer-query-pram-table-content'](context);
    $('#query-param-value-table-' + id).show();
    $('#query-param-value-table-content-' + id + ' > tbody:first').append(output);
    $('#query-param-name-' + id).val("");
    $('#query-param-value-' + id).val("");
};

var removeHeaderRow = function (rowObj, flowId) {
    $(rowObj).closest('tr').remove();
    var rowCount = $('#header-value-table-content-' + flowId + ' tr').length;
    //Using 1 here as even row is removed above the row count will not set to 0 during this flow
    if (rowCount <= 1) {
        $('#header-value-table-' + flowId).hide();
    }
};

var addHeader = function (id) {
    var source = $("#designer-header-value-table-content").html();
    var headerName = $('#header-name-' + id).val();
    var headerValue = $('#header-value-' + id).val();
    var requiredMsg = $('#errorMsgRequired').val();
    var isDuplicate = false;
    if(!validateInput(headerName,  $('#header-name-' + id), requiredMsg)) {
        return false;
    }

    if(!validateInput(headerValue,   $('#header-value-' + id), requiredMsg)) {
        return false;
    }

    var table = $("#header-value-table-content-" + id + " > tbody");
    table.find('tr').each(function (k, el) {
        var $tds = $(this).find('td');
        headerNameT = $tds.eq(0).text();
        headerValT = $tds.eq(1).text();
        if(headerNameT == headerName) {
            addDuplicateError("duplicate", $('#header-name-' + id), "Duplicate Header");
            isDuplicate = true;
        }
    });

    if(isDuplicate) {
        return;
    }

    var tableRow = {
        "name": headerName,
        "value": headerValue,
        "flowId": id
    };

    Handlebars.partials['designer-header-value-table-content'] = Handlebars.compile(source);
    var context = {
        "tableRow": tableRow
    };

    var output = Handlebars.partials['designer-header-value-table-content'](context);
    $('#header-value-table-' + id).show();
    $('#header-value-table-content-' + id + ' > tbody:first').append(output);
    $('#header-name-' + id).val("");
    $('#header-value-' + id).val("");
};

var removeJwtClaimRow = function (rowObj, flowId) {
    $(rowObj).closest('tr').remove();
    var rowCount = $('#jwt-claim-value-table-content-' + flowId + ' tr').length;
    //Using 1 here as even row is removed above the row count will not set to 0 during this flow
    if (rowCount <= 1) {
        $('#jwt-claim-value-table-' + flowId).hide();
    }
};

var addJwtClaim = function (id) {
    var source = $("#designer-jwt-claim-value-table-content").html();
    var claimName = $('#jwt-claim-name-' + id).val();
    var claimValue = $('#jwt-claim-value-' + id).val();
    var isDuplicate = false;
    var tableRow = {
        "name": claimName,
        "value": claimValue,
        "flowId": id
    };
    var requiredMsg = $('#errorMsgRequired').val();

    if(!validateInput(claimName,  $('#jwt-claim-name-' + id), requiredMsg)) {
        return false;
    }

    if(!validateInput(claimValue,   $('#jwt-claim-value-' + id), requiredMsg)) {
        return false;
    }

    var table = $("#jwt-claim-value-table-content-" + id + " > tbody");
    table.find('tr').each(function (k, el) {
        var $tds = $(this).find('td');
        jwtClaimName = $tds.eq(0).text();
        jwtClaimValue = $tds.eq(1).text();
        if(jwtClaimName == claimName) {
            addDuplicateError("duplicate", $('#jwt-claim-name-' + id), "Duplicate Claim");
            isDuplicate = true;
        }
    });

    if(isDuplicate) {
        return;
    }

    Handlebars.partials['designer-jwt-claim-value-table-content'] = Handlebars.compile(source);
    var context = {
        "tableRow": tableRow
    };

    var output = Handlebars.partials['designer-jwt-claim-value-table-content'](context);
    $('#jwt-claim-value-table-' + id).show();
    $('#jwt-claim-value-table-content-' + id + ' > tbody:first').append(output);
    $('#jwt-claim-name-' + id).val("");
    $('#jwt-claim-value-' + id).val("");
};

var loadPolicy = function (policyName) {
    jagg.post("/site/blocks/policy/resource/policy-add/ajax/policy-operations.jag", {
        action: "getApiPolicy",
        policyName: policyName},
        function (data) {
            if (!data.error) {
                policy = data.apiPolicy;
                for (var i = 0 ; i < policy.executionFlows.length; i++) {
                    var source = $("#designer-policy-template").html();
                    policy.executionFlows[i].id = index;
                    Handlebars.partials['designer-policy-template'] = Handlebars.compile(source);
                    var context = {
                        "executionFlow": policy.executionFlows[i]
                    };
                    var output = Handlebars.partials['designer-policy-template'](context);
                    $('#pipeline-content').append(output);
                    $('#executionFlow-desc-' + index).editable();
                    apiPolicy.executionFlows.push(policy.executionFlows[i]);
                    console.log(apiPolicy);
                    index++;
                }
            } else {

            }
        }
    , "json");
};

var addPolicyToBackend = function () {

    //validate the input fields in the form
    var isValid = $("form.form-horizontal").valid();
    if(!isValid) {
      return false;
    }

    var apiPolicyString = JSON.stringify(apiPolicy);
    var apiPolicyNew = JSON.parse(apiPolicyString)
    var policyName = $('#policy-name').val();
    var policyDescription = htmlEscape($('#policy-description').val());
    var policyLevel = $("#policy-level option:selected").val();
    //var defaultPolicyType = $("#default-policy-level option:selected").val();
    var defaultPolicyType = $('input[name=select-quota-type]:checked').val();
    var defaultPolicyLimit;
    var defaultPolicyUnit;
    var defaultPolicyUnitTime;
    var requiredMsg = $('#errorMsgRequired').val();
    var errorHasSpacesMsg = $('#errorMessageSpaces').val();

    apiPolicyNew.policyName = policyName;

    if(!validateInput(policyName, $('#policy-name'), requiredMsg)) {
        return false;
    }

    if (!validateForSpaces(policyName, $('#policy-name'), errorHasSpacesMsg)) {
        return false;
    }

    apiPolicyNew.policyDescription = policyDescription;
    apiPolicyNew.policyLevel = policyLevel;
    apiPolicyNew.defaultQuotaPolicy.type = defaultPolicyType;

    if (defaultPolicyType == 'requestCount') {
        defaultPolicyLimit = $('#request-count').val();
        defaultPolicyUnit = $("#request-count-unit option:selected").val();
        defaultPolicyUnitTime = $("#unit-time-count").val();
        apiPolicyNew.defaultQuotaPolicy.limit.requestCount = defaultPolicyLimit;
        apiPolicyNew.defaultQuotaPolicy.limit.unitTime = defaultPolicyUnitTime;
        apiPolicyNew.defaultQuotaPolicy.limit.timeUnit = defaultPolicyUnit;

        if(!validateInput(defaultPolicyLimit, $('#request-count'), requiredMsg)) {
            return false;
        }

        if(!validateInput(defaultPolicyUnitTime, $('#unit-time-count'), requiredMsg)) {
            return false;
        }

        if(!validateInput(defaultPolicyUnit, $("#request-count-unit option:selected"), requiredMsg)) {
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

        if(!validateInput(defaultPolicyLimit, $('#bandwidth'), requiredMsg)) {
            return false;
        }

        if(!validateInput(defaultPolicyDataUnit, $("#bandwidth-unit option:selected"), requiredMsg)) {
            return false;
        }

        if(!validateInput(defaultPolicyUnitTime,  $("#unit-time-count"), requiredMsg)) {
            return false;
        }

        if(!validateInput(defaultPolicyUnit,  $("#request-count-unit option:selected"), requiredMsg)) {
            return false;
        }
    }

    var executionFlow, executionFlowId, checked;
    for (var i = 0; i < apiPolicyNew.executionFlows.length; i++) {
        executionFlow = apiPolicyNew.executionFlows[i];
        executionFlowId = executionFlow.id;
        apiPolicyNew.executionFlows[i].description = $("#executionFlow-desc-" + executionFlowId).text();
        apiPolicyNew.executionFlows[i].enabled = true;
        for (var j = 0; j < apiPolicyNew.executionFlows[i].conditions.length; j++) {
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "IP") {
                checked = $('#ip-condition-checkbox-' + executionFlowId).is(':checked');
                //Ip condition related properties
                if (checked) {
                    var ipConditionType = $("#ip-condition-type-" + executionFlowId + " option:selected").val();
                    if (ipConditionType == 'specificIp') {
                        var specificIp = $('#specific-ip-address-input-' + executionFlowId).val();
                        if(!validateInput(specificIp, $('#specific-ip-address-input-' + executionFlowId), requiredMsg)) {
                            return false;
                        }
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].ipType = 'specific';
                        apiPolicyNew.executionFlows[i].conditions[j].specificIP = specificIp;
                    } else {
                        var startIp = $('#ip-range-start-address-input-' + executionFlowId).val();
                        var endIp = $('#ip-range-end-address-input-' + executionFlowId).val();

                        if(!validateInput(startIp, $('#ip-range-start-address-input-' + executionFlowId), requiredMsg)) {
                            return false;
                        }
                        if(!validateInput(endIp, $('#ip-range-end-address-input-' + executionFlowId), requiredMsg)) {
                            return false;
                        }
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].ipType = 'range';
                        apiPolicyNew.executionFlows[i].conditions[j].startingIP = startIp;
                        apiPolicyNew.executionFlows[i].conditions[j].endingIP = endIp;
                    }

                    var ipInvertCondition = $('#ip-condition-invert-' + executionFlowId).is(':checked');
                    if (ipInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    } else {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = false;
                    }
                } else {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = false;
                }
            }

            if (apiPolicyNew.executionFlows[i].conditions[j].type == "Header") {
                //Header condition related properties
                checked = $('#header-condition-checkbox-' + executionFlowId).is(':checked');
                if (checked) {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                    apiPolicyNew.executionFlows[i].conditions[j].keyValPairs = [];
                    var headerName, headerVal;
                    var table = $("#header-value-table-content-" + executionFlowId + " > tbody");
                    table.find('tr').each(function (k, el) {
                        var $tds = $(this).find('td');
                        headerName = $tds.eq(0).text();
                        headerVal = $tds.eq(1).text();
                        var keyValPair = {
                            "name": headerName,
                            "value": headerVal
                        };
                        apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                    });
                    var headerInvertCondition = $('#header-condition-invert-' + executionFlowId).is(':checked');
                    if (headerInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    } else {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = false;
                    }
                } else {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = false;
                }
            }

            //Query param condition related properties
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "QueryParam") {
                checked = $('#query-param-condition-checkbox-' + executionFlowId).is(':checked');
                if (checked) {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                    apiPolicyNew.executionFlows[i].conditions[j].keyValPairs = [];
                    var queryParamName, queryParamVal;
                    var table = $("#query-param-value-table-content-" + executionFlowId + " > tbody");
                    table.find('tr').each(function (k, el) {
                        var $tds = $(this).find('td');
                        queryParamName = $tds.eq(0).text();
                        queryParamVal = $tds.eq(1).text();
                        var keyValPair = {
                            "name": queryParamName,
                            "value": queryParamVal
                        };
                        apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                    });
                    var queryParamInvertCondition = $('#query-param-condition-invert-' + executionFlowId).is(':checked');
                    if (queryParamInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    } else {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = false;
                    }
                } else {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = false;
                }
            }

            //Jwt claim condition related properties
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "JWTClaim") {
                apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                apiPolicyNew.executionFlows[i].conditions[j].keyValPairs = [];
                checked = $('#jwt-claim-condition-checkbox-' + executionFlowId).is(':checked');
                if (checked) {
                    var claimName, claimVal;
                    var table = $("#jwt-claim-value-table-content-" + executionFlowId + " > tbody");
                    table.find('tr').each(function (k, el) {
                        var $tds = $(this).find('td');
                        claimName = $tds.eq(0).text();
                        claimVal = $tds.eq(1).text();
                        var keyValPair = {
                            "name": claimName,
                            "value": claimVal
                        };
                        apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                    });
                    var jwtClaimInvertCondition = $('#jwt-claim-condition-invert-' + executionFlowId).is(':checked');
                    if (jwtClaimInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    } else {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = false;
                    }
                } else {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = false;
                }
            }
        }

        var executionPolicyQuotaType = $("#execution-policy-level-" + executionFlowId + " option:selected").val();
        apiPolicyNew.executionFlows[i].quotaPolicy.type = executionPolicyQuotaType;
        if (defaultPolicyType == 'requestCount') {
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.unitTime = $("#execution-flow-request-count-request-unit-time-" + executionFlowId).val();
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.requestCount = $('#execution-flow-request-count-' + executionFlowId).val();
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.timeUnit = $("#execution-flow-request-count-unit-" + executionFlowId + " option:selected").val();
        } else {
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.unitTime = $("#execution-flow-request-count-bandwidth-unit-time-" + executionFlowId).val();
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.dataAmount = $('#execution-flow-bandwidth-' + executionFlowId).val();
            apiPolicyNew.executionFlows[i].quotaPolicy.limit.dataUnit = $("#execution-flow-bandwidth-unit-" + executionFlowId + " option:selected").val();
        }
    }

    var value = $("#action").val();
    $('#addThrottleBtn').buttonLoader('start');
    var action;
    if(value == "new") {
       action =  "addApiPolicy";
    } else {
       action = "updateApiPolicy"
    }
    console.log(JSON.stringify(apiPolicyNew));
    jagg.post("/site/blocks/policy/resource/policy-add/ajax/policy-operations.jag", {
        action: action,
        apiPolicy: JSON.stringify(apiPolicyNew)
    }, function (data) {
        if (!data.error) {
            location.href = 'api-policy-list';
        } else {
            $('#addThrottleBtn').buttonLoader('stop');
            jagg.message({content:data.message,type:"error"});
        }
    }, "json");
};

function validateInput(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == ""){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function validateForSpaces(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text.indexOf(' ') >= 0){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    }else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}

function htmlEscape(str) {
    if(str == null || str == "") {
        return "";
    }
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

function addDuplicateError(text, element, errorMsg){
    var elementId = element.attr('id');
    text = text.trim();
    if(text == "duplicate"){
        element.css("border", "1px solid red");
        $('#label'+elementId).remove();
        element.parent().append('<label class="error" id="label'+elementId+'" >' + errorMsg + '</label>');
        return false;
    } else{
        $('#label'+elementId).remove();
        element.css("border", "1px solid #cccccc");
        return true;
    }
}
