/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var attributeCount =0;
var headerTables =[];
var queryParamTables =[];
var claimsTables = [];
$(function () {
    $('.help_popup').popover({ trigger: "hover" });
    //$('#addThrottleBtn').on('click', addPolicyToBackend);

    $('#cancel-tier-btn').on('click', function () {
        window.location = contextPath + "/throttling/advanced-throttling"
    });
    $('#add-attribute-btn').on('click',function(){
        ++ attributeCount;
        var tBody = $('#custom-attribute-tbody');
        addCustomAttribute(tBody, attributeCount);
    });
});

$('#addThrottleBtn').on('click', addPolicyToBackend );

var apiPolicy =
{
    "policyId": "",
    "policyName": "",
    "policyLevel": "",
    "policyDescription": "",
    "executionFlows": [],
    "defaultLimit": {
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
        "roleList" : "",
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
        },
        onFailure: function () {
        }
    };
    UUFClient.renderFragment("condition-group", context,"pipeline-content", "APPEND",callbacks);
    index++;
};



function addPolicyToBackend(e) {
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
    policy.tierLevel = policyQuery ; // from send to client.
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

function addCustomAttribute(element, count){

    var elementId = element.attr('id');
    element.parent().append(
        '<tr id="attribute'+count+'">'+
        '<td><div class="clear"></div></td>'+
        '<td><input type="text" class="form-control" id="attributeName'+count+'" name="attributeName'+count+'" placeholder="Attribute Name"/></td>'+
        '<td><input type="text" class="form-control" id="attributeValue'+count+'" name="attributeValue'+count+'" placeholder="Value"/></td>'+
        '<td class="delete_resource_td"><a  id="attributeDelete'+count+'"  href="javascript:removeCustomAttribute('+count+')">'+
        '<span class="fw-stack"> <i class="fw fw-delete fw-stack-1x"></i> <i class="fw fw-circle-outline fw-stack-2x"></i></span></td></a></td>'+
        '</tr>'
    );
}

function removeCustomAttribute(count){
    $('#attribute'+count).remove();
}



