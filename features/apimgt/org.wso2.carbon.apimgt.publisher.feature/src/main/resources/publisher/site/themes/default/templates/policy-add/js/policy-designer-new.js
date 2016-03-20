var index = 0;

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

var addPolicy = function () {

    var executionFlow = {
        "id": 0,
        "enabled": false,
        "quotaPolicy": {
            "type": "",
            "limit": {
                "requestCount": 0,
                "timeUnit": "",
                "dataAmount": 0,
                "dataUnit": ""
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
                "type": "Date",
                "dateType": "specific",
                "startingDate": "",
                "endingDate": "",
                "specificDate": "",
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
                "type": "HTTPVerb",
                "httpVerb": "",
                "invertCondition": false,
                "enabled": false
            },
            {
                "type": "Payload",
                "maxSize": "",
                "invertCondition": false,
                "enabled": false
            },
            {
                "type": "Context",
                "contextName": "",
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
    Handlebars.partials['designer-policy-template'] = Handlebars.compile(source);
    var context = {
        "executionFlow": executionFlow
    };
    var output = Handlebars.partials['designer-policy-template'](context);
    $('#pipeline-content').append(output);

    apiPolicy.executionFlows.push(executionFlow);
    console.log(apiPolicy);
    index++;
};

$(document).ready(function () {
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
    var tableRow = {
        "name": claimName,
        "value": claimValue,
        "flowId": id
    };

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

var addPolicyToBackend = function () {
    var apiPolicyString = JSON.stringify(apiPolicy);
    var apiPolicyNew = JSON.parse(apiPolicyString)
    var policyName = $('#policy-name').val();
    var policyDescription = $('#policy-description').val();
    var policyLevel = $("#policy-level option:selected").val();
    var defaultPolicyType = $("#default-policy-level option:selected").val();
    var defaultPolicyLimit;
    var defaultPolicyUnit;

    apiPolicyNew.policyName = policyName;
    apiPolicyNew.policyDescription = policyDescription;
    apiPolicyNew.policyLevel = policyLevel;
    apiPolicyNew.defaultQuotaPolicy.type = defaultPolicyType;

    if (defaultPolicyType == 'requestCount') {
        defaultPolicyLimit = $('#request-count').val();
        defaultPolicyUnit = $("#request-count-unit option:selected").val();
        apiPolicyNew.defaultQuotaPolicy.limit.requestCount = defaultPolicyLimit;
        apiPolicyNew.defaultQuotaPolicy.limit.timeUnit = defaultPolicyUnit;
    } else {
        defaultPolicyLimit = $('#bandwidth').val();
        defaultPolicyUnit = $("#bandwidth-unit option:selected").val();
        apiPolicyNew.defaultQuotaPolicy.limit.dataAmount = defaultPolicyLimit;
        apiPolicyNew.defaultQuotaPolicy.limit.dataUnit = defaultPolicyUnit;
    }

    var executionFlow, executionFlowId, checked;
    for (var i = 0; i < apiPolicyNew.executionFlows.length; i++) {
        executionFlow = apiPolicyNew.executionFlows[i];
        executionFlowId = executionFlow.id;
        executionFlow = apiPolicyNew.executionFlows[i].enabled = true;
        for (var j = 0; j < apiPolicyNew.executionFlows[i].conditions.length; j++) {
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "IP") {
                checked = $('#ip-condition-checkbox-' + executionFlowId).attr('checked');
                //Ip condition related properties
                if (checked) {
                    var ipConditionType = $("#ip-condition-type-" + executionFlowId + " option:selected").val();
                    if (ipConditionType == 'specificIp') {
                        var specificIp = $('#specific-ip-address-input-' + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].ipType = 'specific';
                        apiPolicyNew.executionFlows[i].conditions[j].specificIP = specificIp;
                    } else {
                        var startIp = $('#ip-range-start-address-input-' + executionFlowId).val();
                        var endIp = $('#ip-range-end-address-input-' + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].ipType = 'range';
                        apiPolicyNew.executionFlows[i].conditions[j].endingIP = startIp;
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = endIp;
                    }

                    var ipInvertCondition = $('#ip-condition-invert-' + executionFlowId).attr('checked');
                    if(ipInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    }
                }
            }

            if (apiPolicyNew.executionFlows[i].conditions[j].type == "Header") {
                //Header condition related properties
                checked = $('#header-condition-checkbox-' + executionFlowId).attr('checked');
                if (checked) {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                    var headerName, headerVal;
                    var table = $("#header-value-table-content-" + executionFlowId + " > tbody");
                    table.find('tr').each(function (i, el) {
                        var $tds = $(this).find('td');
                        headerName = $tds.eq(0).text();
                        headerVal = $tds.eq(1).text();
                        var keyValPair = {
                            "name": headerName,
                            "value": headerVal
                        };
                        apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                    });
                    var headerInvertCondition = $('#header-condition-invert-' + executionFlowId).attr('checked');
                    if(headerInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    }
                }
            }

            //Date Condition related properties
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "Date") {
                checked = $('#date-condition-checkbox-' + executionFlowId).attr('checked');
                if (checked) {
                    var dateConditionType = $("#date-condition-type-" + executionFlowId + " option:selected").val();
                    if (dateConditionType == 'specificDate') {
                        var specificDate = $('#specific-date-address-input-' + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].specificDate = specificDate;
                    } else {
                        var startingDate = $('#idate-range-start-date-input-' + executionFlowId).val();
                        var endingDate = $('#date-range-end-date-input-' + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        apiPolicyNew.executionFlows[i].conditions[j].startingDate = startingDate;
                        apiPolicyNew.executionFlows[i].conditions[j].endingDate = endingDate;
                    }

                    var dateInvertCondition = $('#date-condition-invert-' + executionFlowId).attr('checked');
                    if(dateInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    }
                }
            }

            //Query param condition related properties
            if (apiPolicyNew.executionFlows[i].conditions[j].type == "QueryParam") {
                checked = $('#query-param-condition-checkbox-' + executionFlowId).attr('checked');
                if (checked) {
                    apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                    var queryParamName, queryParamVal;
                    var table = $("#query-param-value-table-content-" + executionFlowId + " > tbody");
                    table.find('tr').each(function (i, el) {
                        var $tds = $(this).find('td');
                        queryParamName = $tds.eq(0).text();
                        queryParamVal = $tds.eq(1).text();
                        var keyValPair = {
                            "name": queryParamName,
                            "value": queryParamVal
                        };
                        apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                    });
                    var queryParamInvertCondition = $('#query-param-condition-invert-' + executionFlowId).attr('checked');
                    if(queryParamInvertCondition) {
                        apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                    }
                }
            }

                //HTTP Verb condition related properties
                if (apiPolicyNew.executionFlows[i].conditions[j].type == "HTTPVerb") {
                    checked = $('#http-verb-condition-checkbox-' + executionFlowId).attr('checked');
                    if (checked) {
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        var httpVerb = $("#http-verb-selection-" + executionFlowId + " option:selected").val();
                        apiPolicyNew.executionFlows[i].conditions[j].httpVerb = httpVerb;
                        var httpVerbInvertCondition = $('#http-verb-condition-invert-' + executionFlowId).attr('checked');
                        if(httpVerbInvertCondition) {
                            apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                        }
                    }
                }

                //Payload condition related properties
                if (apiPolicyNew.executionFlows[i].conditions[j].type == "Payload") {
                    checked = $('#payload-condition-checkbox-' + executionFlowId).attr('checked');
                    if (checked) {
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        var payloadSize = $("#payload-size-" + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].maxSize = payloadSize;
                        var payloadInvertCondition = $('#payload-condition-invert-' + executionFlowId).attr('checked');
                        if(payloadInvertCondition) {
                            apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                        }
                    }
                }

                //Context related properties
                if (apiPolicyNew.executionFlows[i].conditions[j].type == "Context") {
                    checked = $('#context-condition-checkbox-' + executionFlowId).attr('checked');
                    if (checked) {
                        apiPolicyNew.executionFlows[i].conditions[j].enabled = true;
                        var contextName = $("#context-name-" + executionFlowId).val();
                        apiPolicyNew.executionFlows[i].conditions[j].contextName = contextName;
                        var contextInvertCondition = $('#context-condition-invert-' + executionFlowId).attr('checked');
                        if(contextInvertCondition) {
                            apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                        }
                    }
                }

                //Jwt claim condition related properties
                if (apiPolicyNew.executionFlows[i].conditions[j].type == "JWTClaim") {
                    checked = $('#jwt-claim-condition-checkbox-' + executionFlowId).attr('checked');
                    if (checked) {
                        var claimName, claimVal;
                        var table = $("#jwt-claim-value-table-content-" + executionFlowId + " > tbody");
                        table.find('tr').each(function (i, el) {
                            var $tds = $(this).find('td');
                            claimName = $tds.eq(0).text();
                            claimVal = $tds.eq(1).text();
                            var keyValPair = {
                                "name": claimName,
                                "value": claimVal
                            };
                            apiPolicyNew.executionFlows[i].conditions[j].keyValPairs.push(keyValPair);
                        });
                        var jwtClaimInvertCondition = $('#jwt-claim-condition-invert-' + executionFlowId).attr('checked');
                        if(jwtClaimInvertCondition) {
                            apiPolicyNew.executionFlows[i].conditions[j].invertCondition = true;
                        }
                    }
                }

            var executionPolicyQuotaType = $("#execution-policy-level-" + executionFlowId + " option:selected").val();
            apiPolicyNew.executionFlows[i].quotaPolicy.type = executionPolicyQuotaType;
            if (defaultPolicyType == 'requestCount') {
                apiPolicyNew.executionFlows[i].quotaPolicy.limit.requestCount = $('#execution-flow-request-count-' + executionFlowId).val();
                apiPolicyNew.executionFlows[i].quotaPolicy.limit.timeUnit = $("#execution-flow-request-count-unit-" + executionFlowId + " option:selected").val();
            } else {
                apiPolicyNew.executionFlows[i].quotaPolicy.limit.dataAmount = $('#execution-flow-bandwidth-' + executionFlowId).val();
                apiPolicyNew.executionFlows[i].quotaPolicy.limit.dataUnit = $("#execution-flow-bandwidth-unit-" + executionFlowId + " option:selected").val();
            }
        }
        console.log(JSON.stringify(apiPolicyNew));
    }
};
