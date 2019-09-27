/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* This function check whether the enjoyhint is enabled or not. If enabled include the configuration
 files link jag and return true */
function isEnjoyHintEnabled() {
    var isInteractiveTutorialEnabled = localStorage.getItem("interactiveTutorialEnabled");
    return isInteractiveTutorialEnabled === "true";
}

// Function to run enjoyhint instance
function runEnjoyHintScript(enjoyhint_instance, script_data) {
    // Start running Enjoyhint instance
    enjoyhint_instance = new EnjoyHint({});
    enjoyhint_instance.setScript(script_data);
    enjoyhint_instance.runScript();
    return enjoyhint_instance;
}

// Set the api name into local storage value
function addApiNameToLocalStorage(event, name) {
    event = event || window.event; //For IE compatibility
    if (event.keyCode === 9) {
        var apiName = name.value;
        localStorage.setItem("apiName", apiName);
    }
}

// Method to remove all the local storage values when closing the tutorial
function removeLocalStorageVariables() {
    var isInteractiveTutorialEnabled = localStorage.getItem("interactiveTutorialEnabled");
    if (null != isInteractiveTutorialEnabled) {
        localStorage.removeItem("interactiveTutorialEnabled");
    }
    var isEnjoyHintEnabledWithMenu = localStorage.getItem("isEnjoyHintEnabledWithMenu");
    if (null != isEnjoyHintEnabledWithMenu) {
        localStorage.removeItem("isEnjoyHintEnabledWithMenu");
    }
    var apiNameExists = localStorage.getItem("apiName");
    if (null != apiNameExists) {
        localStorage.removeItem("apiName");
    }
    var isWorldBankApiExist = localStorage.getItem("worldBankApiExist");
    if (null != isWorldBankApiExist) {
        localStorage.removeItem("worldBankApiExist");
    }
}

// Intercept Enter key press
$(document).keypress(function(e) {
    if (e.which == 13) {
        e.preventDefault();
        return false;
    }
});
