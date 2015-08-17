/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

var getAPIUrl;
var updateSubscription;

$(function () {

    /*
     *This function generate the location of the templates used in the rendering
     */
    var getSubscriptionAPI = function (action) {
        return caramel.context + '/assets/api/apis/api-subscriptions/' + action;
    };

    /*
     *This function bind the data and get the UUID needed to return API URL.
     */
    getAPIUrl = function (apiProvider, apiName, apiVersion) {
        var apiData = {};
        apiData.apiName = apiName;
        apiData.apiVersion = apiVersion;
        apiData.apiProvider = apiProvider;
        $.ajax({
            type: 'POST',
            url: getSubscriptionAPI('getUUID'),
            data: apiData,
            success: function (data) {
                var uuid = data.data;
                window.location = 'details/' + uuid;
            }
        });
    };

    /*
     *This function update the metadata by new row values.
     */
    var setMetadata = function (apiProvider, apiName, apiVersion, appId, status, apiUsername, application) {
        metadata.newRowData = [];
        metadata.newRowData.apiProvider = apiProvider;
        metadata.newRowData.apiName = apiName;
        metadata.newRowData.apiVersion = apiVersion;
        metadata.newRowData.appId = appId;
        metadata.newRowData.status = status;
        metadata.newRowData.apiUsername = apiUsername;
        metadata.newRowData.application = application;
    };

    /*
     *This function crete the new content for the "Subscribed APIs & Actions" td.
     */
    var getNewDivContent = function () {
        var contentData = "Unknown state selected!";
        if (metadata.newRowData.status == "UNBLOCKED") {
            contentData = "<form><input type=\"radio\" name=\"" +
            metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" id=\"r1-" + metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" value=\"PROD_ONLY_BLOCKED\"> Production Only <input type=\"radio\" name=\"" +
            metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" id=\"r2-" + metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" value=\"BLOCKED\" checked> Production & Sandbox <a href=\"javascript:updateSubscription('" +
            metadata.newRowData.apiProvider + "','" + metadata.newRowData.apiName + "','" +
            metadata.newRowData.apiVersion + "','" + metadata.newRowData.appId + "','GET_SELECTED','" +
            metadata.newRowData.apiUsername + "','" + metadata.newRowData.application + "');\"> Block </a> </form>";
        } else if (metadata.newRowData.status == "BLOCKED") {
            contentData = "<form><input type=\"radio\" name=\"" + metadata.newRowData.apiProvider +
            "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" disabled> Production Only <input type=\"radio\" name=\"" +
            metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" checked disabled> Production & Sandbox <a href=\"javascript:updateSubscription('" +
            metadata.newRowData.apiProvider +
            "','" + metadata.newRowData.apiName + "','" + metadata.newRowData.apiVersion + "','" +
            metadata.newRowData.appId + "','UNBLOCKED','" + metadata.newRowData.apiUsername + "','" +
            metadata.newRowData.application + "');\"> Unblock </a> </form>";
        } else if (metadata.newRowData.status == "PROD_ONLY_BLOCKED") {
            contentData = "<form><input type=\"radio\" name=\"" + metadata.newRowData.apiProvider +
            "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" checked disabled> Production Only <input type=\"radio\" name=\"" +
            metadata.newRowData.apiProvider + "-" + metadata.newRowData.apiName + "-" +
            metadata.newRowData.apiVersion + "-" + metadata.newRowData.appId + "-" +
            metadata.newRowData.apiUsername + "-" + metadata.newRowData.application +
            "\" disabled> Production & Sandbox <a href=\"javascript:updateSubscription('" +
            metadata.newRowData.apiProvider +
            "','" + metadata.newRowData.apiName + "','" + metadata.newRowData.apiVersion + "','" +
            metadata.newRowData.appId + "','UNBLOCKED','" + metadata.newRowData.apiUsername + "','" +
            metadata.newRowData.application + "');\"> Unblock </a> </form>";
        }
        return contentData;
    };


    /*
     *This function update the api subscription status according to the data sent.
     */
    updateSubscription = function (apiProvider, apiName, apiVersion, appId, status, apiUsername, application) {
        var apiData = {};
        var newStatus = status;
        if (status == 'GET_SELECTED') {
            if (document.getElementById('r1-' + apiProvider + '-' + apiName + '-' + apiVersion + '-' +
                appId + '-' + apiUsername + '-' + application).checked) {
                newStatus = document.getElementById('r1-' + apiProvider + '-' + apiName + '-' +
                apiVersion + '-' + appId + '-' + apiUsername + '-' + application).value;
            } else if (document.getElementById('r2-' + apiProvider + '-' + apiName + '-' +
                apiVersion + '-' + appId + '-' + apiUsername + '-' + application).checked) {
                newStatus = document.getElementById('r2-' + apiProvider + '-' + apiName + '-' +
                apiVersion + '-' + appId + '-' + apiUsername + '-' + application).value;
            }
        }
        setMetadata(apiProvider, apiName, apiVersion, appId, newStatus, apiUsername, application);
        apiData.apiName = apiName;
        apiData.apiVersion = apiVersion;
        apiData.apiProvider = apiProvider;
        apiData.appId = appId;
        apiData.status = newStatus;
        $.ajax({
            type: 'POST',
            url: getSubscriptionAPI('updateSubscription'),
            data: apiData,
            success: function (data) {
                if (data) {
                    document.getElementById("td-" + metadata.newRowData.apiUsername + "-" +
                    metadata.newRowData.application + "-" + metadata.newRowData.apiName + "-" +
                    metadata.newRowData.apiVersion).innerHTML = getNewDivContent();
                }
            }
        });
    };

    //events.publish(EV_TD_API);

});