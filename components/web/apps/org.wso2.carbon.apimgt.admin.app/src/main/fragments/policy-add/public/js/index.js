/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
var index = 0;
var headerTables =[];
var queryParamTables =[];
var claimsTables = [];
$(function () {
    $('.help_popup').popover({ trigger: "hover" });
    $('#addThrottleBtn').on('click', addPolicyToBackend);

    $('#cancel-tier-btn').on('click', function () {
        window.location = contextPath + "/throttling/advanced-throttling"
    });
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
        "description": "",
        "quotaPolicy": {
            "requestCount": true,
            "badndwidth" : true,
            "type": false,
            "limit": {
                "requestCount": "5",
                "unitTime": "1",
                "timeUnit": "min",
                "dataAmount": 1,
                "dataUnit": "KB"
            }
        },
        "conditions": [
            {
                "type": "IP",
                "ipType": "specific",
                "specific": true,
                "startingIP": "",
                "endingIP": "",
                "specificIP": "",
                "invertCondition": false,
                "enabled": false,
                "IP": true
            },
            {
                "type": "Header",
                "keyValPairs": [],
                "invertCondition": false,
                "enabled": false,
                "Header": true
            },
            {
                "type": "QueryParam",
                "keyValPairs": [],
                "invertCondition": false,
                "enabled": false,
                "QueryParam": true
            },
            {
                "type": "JWTClaim",
                "keyValPairs": [],
                "invertCondition": false,
                "hasValues": false,
                "enabled": false,
                "JWTClaim" : true
            }
        ]
    };

    executionFlow.id = "" + index;
    executionFlow.description = "Sample description about condition group";
    var context = {
        "executionFlow": executionFlow
    };
    var callbacks = {
        onSuccess: function (response) {
            $('#pipeline-content').append(response);
            //$('label[data-toggle="collapse"]').each(function(a,b){console.log($(b).data().target);
            // $(this).click(function(){console.log("oksssssssss"); $($(b).data().target).collapse()})})
        },
        onFailure: function () {
            alert("error");
        }
    };
    UUFClient.renderFragment("condition-group", context, callbacks);
    /*$('#executionFlow-desc-' + index).editable();
    apiPolicy.executionFlows.push(executionFlow);
    console.log(apiPolicy);*/
    index++;
};

var showAdvanceOperation = function (operation, button, id) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

var onExecutionFlowTypeChange = function (id, optionTextOb) {
    var selectedText = $(optionTextOb).val();
    if (selectedText == 'requestCount') {
        $("#execution-flow-request-count-block-" + id).show();
        $("#execution-flow-bandwidth-block-" + id).hide();
    } else {
        $("#execution-flow-request-count-block-" + id).hide();
        $("#execution-flow-bandwidth-block-" + id).removeClass('hide');
        $("#execution-flow-bandwidth-block-" + id).show();
    }
};

var changeStatusIcon = function(flowId, type, checkBoxOb) {
    var spanId;
    var iconId;
    if(type == "IP") {
        spanId = "ip-condition-configured-" + flowId;
        iconId = "ip-condition-configured-icon-" + flowId;
    } else if(type == "Header") {
        spanId = "header-condition-configured-" + flowId;
        iconId = "header-condition-configured-icon-" + flowId;
    } else if(type == "QueryParam") {
        spanId = "queryparam-condition-configured-" + flowId;
        iconId = "queryparam-condition-configured-icon-" + flowId;
    } else if(type == "JWTClaim") {
        spanId = "claim-condition-configured-" + flowId;
        iconId = "claim-condition-configured-icon-" + flowId;
    } else {
        return;
    }

    if($(checkBoxOb).is(":checked")) {
        $('#'+spanId).removeClass('has-configured');
        $('#'+iconId).removeClass('fw-circle-outline');
        $('#'+spanId).addClass('has-success');
        $('#'+iconId).addClass('fw-check');
    } else {
        $('#'+spanId).removeClass('has-success');
        $('#'+iconId).removeClass('fw-check');
        $('#'+spanId).addClass('has-configured');
        $('#'+iconId).addClass('fw-circle-outline');
    }
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

var addHeader = function (id) {
    if(!headerTables[id]){
        var headerTable = $('#header-value-table-content-'+id).DataTable({
            "bPaginate": false,
            "bLengthChange": false,
            "bFilter": false,
            "bInfo": false,
            "bAutoWidth": false });
        headerTables[id] = headerTable;
    }
    $('#header-value-table-content-'+id).removeClass('hide');
    $('#header-value-table-content-'+id).show();
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

        headerTables[id].row.add( [
           headerName,
           headerValue,
           '<button type="button" class="btn btn-primary has-spinner"' +
           ' onclick="removeHeaderRow(this,$(this).closest(\'table\').attr(\'id\'))">Delete</button>'

        ] ).draw( false );


    $('#header-name-' + id).val("");
    $('#header-value-' + id).val("");
};

var removeHeaderRow = function (rowObj, flowId) {
    var splittedId = flowId.split("-");
    var tableId = splittedId[splittedId.length-1];
    headerTables[tableId]
        .row( $(rowObj).parents('tr') )
        .remove()
        .draw();
    var rowCount =headerTables[tableId].data().count();
    if (rowCount < 1) {
        $('#'+flowId).hide();
    }
};

var addQueryParam = function (id) {
    if(!queryParamTables[id]){
        var queryParamTable = $('#query-param-value-table-content-'+id).DataTable({
            "bPaginate": false,
            "bLengthChange": false,
            "bFilter": false,
            "bInfo": false,
            "bAutoWidth": false
            });
        queryParamTables[id] = queryParamTable;
    }
    $('#query-param-value-table-content-'+id).removeClass('hide');
    $('#query-param-value-table-content-'+id).show();
    var paramName = $('#query-param-name-' + id).val();
    var paramValue = $('#query-param-value-' + id).val();
    var isDuplicate = false;
    var requiredMsg = $('#errorMsgRequired').val();
    var objectId= id;

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

    queryParamTables[id].row.add( [
        paramName,
        paramValue,
        '<button type="button" class="btn btn-primary has-spinner"' +
        ' onclick="removeQueryParamRow(this,$(this).closest(\'table\').attr(\'id\'))">Delete</button>'
    ] ).draw( false );

    $('#query-param-name-' + id).val("");
    $('#query-param-value-' + id).val("");
};

var addPolicyToBackend = function () {
    var apiPolicyString = JSON.stringify(apiPolicy);
    var apiPolicyNew = JSON.parse(apiPolicyString);
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
    // for now lets create the simple object expected by the back end;
    var policy = {};
    policy.name = apiPolicyNew.policyName;
    policy.description = apiPolicyNew.policyDescription;
    policy.tierLevel = "api";
    policy.unitTime = parseInt(apiPolicyNew.defaultQuotaPolicy.limit.unitTime);
    policy.timeUnit = apiPolicyNew.defaultQuotaPolicy.limit.timeUnit;
    policy.stopOnQuotaReach = true;

    var policyInstance = new Policy();
    var promised_create =  policyInstance.create(policy);
    promised_create
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


};

function createPolicyCallback(response) {
    var responseObject = JSON.parse(response.data);
    var message = responseObject.policyName + " policy added successfully.";
    noty({
        text: message,
        type: 'success',
        dismissQueue: true,
        modal: true,
        closeWith: ['click', 'backdrop'],
        timeout: 2000,
        layout: 'top',
        theme: 'relax',
        maxVisible: 10,
        callback: {
            afterClose: function () {
                window.location = contextPath + "/throttling/advanced-throttling";
            },
        }
    });
}

var removeQueryParamRow = function (rowObj, flowId) {
    var splittedId = flowId.split("-");
    var tableId = splittedId[splittedId.length-1];
    queryParamTables[tableId]
        .row( $(rowObj).parents('tr') )
        .remove()
        .draw();
    var rowCount =queryParamTables[tableId].data().count();
    if (rowCount < 1) {
        $('#'+flowId).hide();
    }
};

var addJwtClaim = function (id) {
    if(!claimsTables[id]){
        var claimTable = $('#jwt-claim-value-table-content-'+id).DataTable({
            "bPaginate": false,
            "bLengthChange": false,
            "bFilter": false,
            "bInfo": false,
            "bAutoWidth": false
        });
        claimsTables[id] = claimTable;
    }
    $('#jwt-claim-value-table-content-'+id).removeClass('hide');
    $('#jwt-claim-value-table-content-'+id).show();
    var claimName = $('#jwt-claim-name-' + id).val();
    var claimValue = $('#jwt-claim-value-' + id).val();
    var isDuplicate = false;
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

    claimsTables[id].row.add( [
        claimName,
        claimValue,
        '<button type="button" class="btn btn-primary has-spinner"' +
        ' onclick="removeClaimRow(this,$(this).closest(\'table\').attr(\'id\'))">Delete</button>'
    ] ).draw( false );

    $('#jwt-claim-name-' + id).val("");
    $('#jwt-claim-value-' + id).val("");
};



var removeClaimRow = function (rowObj, flowId) {
    var splittedId = flowId.split("-");
    var tableId = splittedId[splittedId.length-1];
    claimsTables[tableId]
        .row( $(rowObj).parents('tr') )
        .remove()
        .draw();
    var rowCount =claimsTables[tableId].data().count();
    if (rowCount < 1) {
        $('#'+flowId).hide();
    }
};

function showHideDefaultQuotaPolicy(){
    var quotaPolicy = $('input[name=select-quota-type]:checked').val();
    if (quotaPolicy == "requestCount"){
        $('#defaultBandwidthBasedDiv').hide();
    } else{
        $('#defaultBandwidthBasedDiv').show();
    }

    if (quotaPolicy == "bandwidthVolume"){
        $('#defaultBandwidthBasedDiv').removeClass('hide');
        $('#defaultRequestCountBasedDiv').hide();
    } else{
        $('#defaultRequestCountBasedDiv').show();
    }
}

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

