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

var updateApplication;
var removeApplication;
var changeAppRowtoEditView;
var addApplication;
/*
 This js function will populate the UI after metadata generation in pages/my_applications.jag
 */
$(function () {

    /*
     * This function refresh the by adding the new entries applications table
     */
    var refreshApplicationList = function () {
        $.ajax({
            url: caramel.context + '/apis/applications', success: function (result) {
                var partial = 'list_applications';
                var container = 'list_applications';
                var data = {};
                data.applications = JSON.parse(result);
                renderPartial(partial, container, data);
            }
        });
    };

    /*
     * This function generate the location of the templates used in the rendering
     */
    var getApplicationsAPI = function (action) {
        return caramel.context + '/apis/applications/' + action;
    };

    var partial = function (name) {
        return '/extensions/assets/api/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };
    var id = function (name) {
        return '#' + name;
    };

    var renderPartial = function (partialName, containerName, data, fn) {
        fn = fn || function () {
        };
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        if (!containerName) {
            throw 'A container name has not been specified for container key ' + containerKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function () {
            var template = Handlebars.partials[partialName](data);
            $(id(containerName)).html(template);
            fn(containerName);
        });
    };

    /*
     * This function set the metadata related to edit application
     */
    var setMetadataForEditRow = function (appName, userName, appId, tier, status, callbackUrl, description) {
        metadata.editRowData = [];
        metadata.editRowData.appName = appName;
        metadata.editRowData.userName = userName;
        metadata.editRowData.appId = appId;
        metadata.editRowData.tier = tier;
        metadata.editRowData.status = status;
        metadata.editRowData.callbackUrl = callbackUrl;
        metadata.editRowData.description = description;
    };

    /*
     * This function returns the html <tr> content according to the metadata provided.
     */
    var getNewTrUpdated = function () {
        return "<td>" + metadata.editRowData.appName + "</td> <td>" + metadata.editRowData.tier + "</td> <td>" +
            getState(metadata.editRowData.status) + "</td> <td>" + metadata.editRowData.callbackUrl + "</td> <td>" +
            metadata.editRowData.description + "</td> <td> <a href=\"javascript:changeAppRowtoEditView('" +
            metadata.editRowData.appName + "','" + metadata.editRowData.userName + "','" +
            metadata.editRowData.appId + "','" + metadata.editRowData.tier + "','" + metadata.editRowData.status +
            "','" + metadata.editRowData.callbackUrl + "','" + metadata.editRowData.description +
            "');\"><i class='icon-edit'></i> Edit</a> <a href=\"javascript:removeApplication('" +
            metadata.editRowData.appName + "','" + metadata.editRowData.userName + "','" +
            metadata.editRowData.appId + "');\"><i class='icon-trash'></i> Delete</a> </td>";
    };

    /*
     * This function triggers the api function for updateApplication
     */
    updateApplication = function (userName, appId, status) {
        setMetadataForEditRow($('#new_overview_name_' + appId).val(), userName, appId,
            $('#new_overview_tier_' + appId).val(), status, $('#new_overview_callbackurl_' + appId).val(),
            $('#new_overview_description_' + appId).val());
        var updateApplicationData = {};
        updateApplicationData.appName = $('#new_overview_name_' + appId).val();
        updateApplicationData.userName = userName;
        updateApplicationData.appId = appId;
        updateApplicationData.tier = $('#new_overview_tier_' + appId).val();
        updateApplicationData.callbackUrl = $('#new_overview_callbackurl_' + appId).val();
        updateApplicationData.description = $('#new_overview_description_' + appId).val();
        $.ajax({
            type: 'POST',
            url: getApplicationsAPI('editapp'),
            data: updateApplicationData,
            success: function (data) {
                document.getElementById("tr-application-" + metadata.editRowData.appId + "-data").innerHTML =
                    getNewTrUpdated();
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_SUCCESS,
                    title: 'Application Updated!',
                    message: '<div><i class="fw fw-check"></i> Application: ' +
                    metadata.editRowData.appName + ' has been updated.</div>',
                    buttons: [{
                        label: 'Close',
                        action: function (dialogItself) {
                            dialogItself.close();
                        }
                    }]

                });
            }
        });
    };

    /*
     * This function set the metadata related to delete application
     */
    var setMetadataForDelRow = function (appName, appId) {
        metadata.delRowData = [];
        metadata.delRowData.appName = appName;
        metadata.delRowData.appId = appId;
    };

    /*
     * This function triggers the api function for removeApplication
     */
    removeApplication = function (appName, userName, appId) {
        setMetadataForDelRow(appName, appId);
        BootstrapDialog.show({
            type: BootstrapDialog.TYPE_WARNING,
            title: 'Warning!',
            message: '<div><i class="fw fw-warning"></i>Are you sure you want to remove the application ' + appName +
            '? This will cancel all the existing subscriptions and keys associated with the application.</div>',
            buttons: [{
                label: 'Yes',
                action: function (dialogItself) {
                    var removeApplicationData = {};
                    removeApplicationData.appName = appName;
                    removeApplicationData.userName = userName;
                    removeApplicationData.appId = appId;

                    $.ajax({
                        type: 'POST',
                        url: getApplicationsAPI('delapp'),
                        data: removeApplicationData,
                        success: function (data) {
                            $("#tr-application-" + metadata.delRowData.appId + "-data").remove();
                        }
                    });
                    dialogItself.close();
                }
            }, {
                label: 'No',
                action: function (dialogItself) {
                    dialogItself.close();
                }
            }]

        });
    };

    /*
     * This function returns the corresponding value of the application status for the given boolean
     */
    var getState = function (isActive) {
        if (isActive) {
            return "ACTIVE";
        } else {
            return "INACTIVE";
        }
    };

    /*
     * This function change the application html table row to edit view
     */
    changeAppRowtoEditView = function (appName, userName, appId, tier, status, callbackUrl, description) {
        var rowContent = "<td><input type='text' id='new_overview_name_" + appId +
            "' name='new_overview_name_" + appId + "' class='input-medium' value='" + appName +
            "'/></td> <td><select id='new_overview_tier_" + appId +
            "' name='new_overview_tier_" + appId + "' value='" + tier +
            "'> <option>Unlimited</option> <option>Bronze</option> <option>Silver</option> <option>Gold</option> </select></td> <td>" +
            getState(status) + "</td> <td><input id='new_overview_callbackurl_" + appId +
            "' type='text' name='new_overview_callbackurl_" + appId + "' class='input-medium' value='" +
            callbackUrl + "'/></td> <td><textarea id='new_overview_description_" + appId +
            "' type='text' name='new_overview_description_" + appId + "' rows='1' cols='10'>" + description +
            "</textarea></td> <td> <a href=\"javascript:updateApplication('" + userName + "','" +
            appId + "','" + status + "');\"><i class='icon-save'></i> Save</a> <a href=\"javascript:removeApplication('" +
            appName + "','" + userName + "','" + appId + "');\"><i class='icon-trash'></i> Delete</a> </td>";

        document.getElementById("tr-application-" + appId + "-data").innerHTML = rowContent;
    };

    /*
     * This function triggers the api function for add new application.
     */
    addApplication = function () {
        var appName = $('#overview_name').val();
        if (appName != '') {
            var addNewApplicationData = {};
            addNewApplicationData.appName = $('#overview_name').val();
            addNewApplicationData.tier = $('#overview_tier').val();
            addNewApplicationData.callbackUrl = $('#overview_callbackurl').val();
            addNewApplicationData.description = $('#overview_description').val();
            $.ajax({
                type: 'POST',
                url: getApplicationsAPI('newapp'),
                data: addNewApplicationData,
                success: function (data) {
                    refreshApplicationList();
                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_SUCCESS,
                        title: 'Application Created!',
                        message: '<div><i class="fw fw-check"></i> Application: ' +
                        appName + ' has been created.</div>',
                        buttons: [{
                            label: 'Close',
                            action: function (dialogItself) {
                                dialogItself.close();
                            }
                        }]

                    });
                },
                error: function (result) {

                    BootstrapDialog.show({
                        type: BootstrapDialog.TYPE_DANGER,
                        title: 'Application not Created!',
                        message: '<div><i class="fw fw-warning"></i> Application: ' +
                        appName + ' has not been created.</div>',
                        buttons: [{
                            label: 'Close',
                            action: function (dialogItself) {
                                dialogItself.close();
                            }

                        }]

                    });
                }
            });
        } else {
            BootstrapDialog.show({
                type: BootstrapDialog.TYPE_DANGER,
                title: 'Error',
                message: '<div><i class="fw fw-warning"></i>Can\'t leave application name empty!</div>',
                buttons: [{
                    label: 'Close',
                    action: function (dialogItself) {
                        dialogItself.close();
                    }

                }]

            });
        }
    };


});