/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var currentLocation;
var statsEnabled = isDataPublishingEnabled();
var apiFilter = "allAPIs";
var apiName;
var apiVersion;
var apiNameVersionMap;

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

jagg.post("/site/blocks/stats/api-usage-by-applications/ajax/stats.jag", {
        action: "getFirstAccessTime",
        currentLocation: window.location.pathname
    },
    function (json) {
        $('#spinner').hide();
        if (!json.error) {

            if (json.usage && json.usage.length > 0) {
                var d = new Date();
                from = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes());

                //day picker
                $('#today-btn').on('click', function () {
                    currentDay = getDate();
                    getDateTime(currentDay, currentDay - 86400000);
                });

                //hour picker
                $('#hour-btn').on('click', function () {
                    currentDay = getDate();
                    getDateTime(currentDay, currentDay - 3600000);
                })

                //week picker
                $('#week-btn').on('click', function () {
                    currentDay = getDate();
                    getDateTime(currentDay, currentDay - 604800000);
                })

                //month picker
                $('#month-btn').on('click', function () {
                    currentDay = getDate();
                    getDateTime(currentDay, currentDay - (604800000 * 4));
                });

                $('#date-range').click(function () {
                    $(this).removeClass('active');
                });

                //date picker
                $('#date-range').daterangepicker({
                    timePicker: true,
                    timePickerIncrement: 30,
                    format: 'YYYY-MM-DD h:mm',
                    opens: 'left',
                });

                $("#apiSelect").change(function (e) {
                    apiName = this.value;
                    populateVersionList(apiName);
                });

                $("#versionSelect").change(function (e) {
                    apiVersion = this.value;
                    drawAPIUsageByApplications(from, to);
                });

                $("#apiFilter").change(function (e) {
                    apiFilter = this.value;
                    populateAPIList();
                    drawAPIUsageByApplications(from, to, apiFilter);
                });

                $('#date-range').on('apply.daterangepicker', function (ev, picker) {
                    btnActiveToggle(this);
                    var from = convertTimeString(picker.startDate);
                    var to = convertTimeString(picker.endDate);
                    var fromStr = from.split(" ");
                    var toStr = to.split(" ");
                    var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                    $("#date-range span").html(dateStr);
                    drawAPIUsageByApplications(from, to, apiFilter);
                });

                populateAPIList();
                getDateTime(to, from);

                $('#date-range').click(function (event) {
                    event.stopPropagation();
                });

                $('body').on('click', '.btn-group button', function (e) {
                    $(this).addClass('active');
                    $(this).siblings().removeClass('active');
                });
            } else {
                $('.stat-page').html("");
                showEnableAnalyticsMsg();
            }
        } else {
            if (json.message == "AuthenticateError") {
                jagg.showLogin();
            } else {
                jagg.message({content: json.message, type: "error"});
            }
        }
    }, "json");

var drawAPIUsageByApplications = function (from, to) {
    var fromDate = convertTimeStringUTC(from);
    var toDate = convertTimeStringUTC(to);

    jagg.post("/site/blocks/stats/api-usage-by-applications/ajax/stats.jag", {
            action: "getAPIUsageByApplications",
            apiName: apiName,
            apiVersion: apiVersion,
            fromDate: fromDate,
            toDate: toDate,
            apiFilter: apiFilter
        },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                var length = json.usage.length;
                $('#noData').empty();
                $('div#apiUsageByApplicationsTable_wrapper.dataTables_wrapper.no-footer').remove();

                var $dataTable = $('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiUsageByApplicationsTable"></table>');

                $dataTable.append($('<thead class="tableHead"><tr>' +
                    '<th>API</th>' +
                    '<th>VERSION</th>' +
                    '<th>APPLICATION NAME</th>' +
                    '<th>USAGE</th>' +
                    '</tr></thead>'));

                for (var i = 0; i < json.usage.length; i++) {
                    $dataTable.append($('<tr><td>' + json.usage[i].values.apiName + '</td><td>' + json.usage[i].values.apiVersion + '</td><td>' + json.usage[i].values.applicationName + '</td><td>' + json.usage[i].values.requstCount + '</td></tr>'));
                }
                if (length == 0) {
                    $('#apiUsageByApplicationsTable').hide();
                    $('div#apiUsageByApplicationsTable_wrapper.dataTables_wrapper.no-footer').remove();
                    $('#noData').html('');
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>' + i18n.t("No Data Available") + '</h4></div></div>'));

                } else {
                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();
                    $('#apiUsageByApplicationsTable').datatables_extended({
                        "order": [[3, "desc"]],
                        "fnDrawCallback": function () {
                            if (this.fnSettings().fnRecordsDisplay() <= $("#apiUsageByApplicationsTable_length option:selected").val()
                                || $("#apiUsageByApplicationsTable_length option:selected").val() == -1)
                                $('#apiUsageByApplicationsTable_paginate').hide();
                            else
                                $('#apiUsageByApplicationsTable_paginate').show();
                        },
                    });
                }
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");
}

function getDateTime(currentDay, fromDay) {
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr = fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawAPIUsageByApplications(from, to, apiFilter);
}

var populateAPIList = function () {
    jagg.post("/site/blocks/stats/ajax/stats.jag", {action: "getAPIList", currentLocation: apiFilter},
        function (json) {
            if (!json.error) {
                apiNameVersionMap = json.apiNameVersionMap;
                var i = 0;
                var apis = '';
                for (var name in apiNameVersionMap) {
                    if (name == window.requestedApiName) {
                        apis += '<option selected="selected" value=' + name + '>' + name + '</option>';
                    } else {
                        apis += '<option value=' + name + '>' + name + '</option>';
                    }
                    i++;
                }
                $('#apiSelect')
                    .empty()
                    .append(apis)
                    .selectpicker('refresh')
                    .trigger('change');
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                }
            }
        }
    );
};

var populateVersionList = function (apiName) {
    var i = 0;
    var selectVersions = '<option selected="selected" value="FOR_ALL_API_VERSIONS">All Versions</option>';
    for (var version in apiNameVersionMap[apiName]) {
        var tempVersion = apiNameVersionMap[apiName][version];
        if (tempVersion == window.requestedVersion) {
            selectVersions += '<option selected="selected" value=' + tempVersion + '>' + tempVersion + '</option>';
        } else {
            selectVersions += '<option value=' + tempVersion + '>' + tempVersion + '</option>';
        }
        i++;
    }
    $('#versionSelect')
        .empty()
        .append(selectVersions)
        .selectpicker('refresh')
        .trigger('change');
};
