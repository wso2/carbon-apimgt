var currentLocation = "allAPIs";
var statsEnabled = isDataPublishingEnabled();
var apiNameVersionMap;
var apiName;
var version;
var d = new Date();
var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());

//setting default date
var to;
var from;

$(document).ready(function () {
    jagg.post("/site/blocks/stats/api-top-users/ajax/stats.jag", {
            action: "getFirstAccessTime",
            currentLocation: currentLocation
        },
        function (json) {

            if (!json.error) {
                if (json.usage && json.usage.length > 0) {
                    from = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    to = new Date();

                    $("#apiFilter").change(function (e) {
                        currentLocation = this.value;
                        populateAPIList();
                    });

                    $("#apiSelect").change(function (e) {
                        apiName = this.value;
                        populateVersionList(apiName);
                    });

                    $("#versionSelect").change(function (e) {
                        version = this.value;
                        reloadDataTable();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    //day picker
                    $('#today-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - 86400000);
                        reloadDataTable();
                    });

                    //week picker
                    $('#week-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - 604800000);
                        reloadDataTable();
                    });

                    //month picker
                    $('#month-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - (604800000 * 4));
                        reloadDataTable();
                    });

                    var dateRange = $('#date-range');
                    var dateRangeSpan = $("#date-range span");
                    dateRange.click(function () {
                        $(this).removeClass('active');
                    });

                    //date picker
                    dateRange.daterangepicker({
                        timePicker: true,
                        timePickerIncrement: 30,
                        format: 'YYYY-MM-DD',
                        opens: 'left'
                    });

                    dateRange.on('apply.daterangepicker', function (ev, picker) {
                        btnActiveToggle(this);
                        from = convertTimeString(picker.startDate);
                        to = convertTimeString(picker.endDate);
                        var fromStr = from.split(" ");
                        var toStr = to.split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        dateRangeSpan.html(dateStr);
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });

                    //set time picker for last month
                    getDateTime(currentDay, currentDay - (604800000 * 4));
                    populateAPIList();
                    drawTopAPIUsersTable();
                } else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    showNoDataAnalyticsMsg();
                } else {
                    $('.stat-page').html("");
                    showEnableAnalyticsMsg();
                }
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }

        }, "json");
});

var drawTopAPIUsersTable = function () {
    $('#spinner').hide();
    var $dataTable = $('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiTopUsersTable"></table>');

    $dataTable.append($('<thead class="tableHead"><tr>' +
        '<th width="20%">User</th>' +
        '<th  width="15%">Number of API Calls</th>' +
        '</tr></thead>'));
    $('#tableContainer').append($dataTable);

    $dataTable.DataTable({
        "processing": true,
        "serverSide": true,
        "searching": false,
        "deferLoading": 0, //stop drawing automatically for the first time
        "ajax": {
            "url": "../../site/blocks/stats/api-top-users/ajax/stats.jag",
            "data": function (d) {
                d.action = "getTopApiUsers";
                d.currentLocation = currentLocation;
                d.apiName = apiName;
                d.version = version;
                d.fromDate = from;
                d.toDate = to;
            }
        },
        "columns": [
            {"data": "user"},
            {"data": "totalRequestCount"}
        ]
    });

    $dataTable.on( 'draw.dt', function () {
        var $noDataContainer = $('#noData');
        if($dataTable.DataTable().data().length === 0) {
            $('#tableContainer').hide();
            $noDataContainer.html('');
            $noDataContainer.append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div></div>'));
        } else {
            $('#tableContainer').show();
            $noDataContainer.html('');
        }
    });
};

var populateAPIList = function () {
    jagg.post("/site/blocks/stats/ajax/stats.jag", {action: "getAPIList", currentLocation: currentLocation},
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

function getDateTime(currentDay, fromDay) {
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var dateRange = $('#date-range');
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr = fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    dateRange.data('daterangepicker').setStartDate(from);
    dateRange.data('daterangepicker').setEndDate(to);
}

function reloadDataTable() {
    var dataTable = $('#apiTopUsersTable').DataTable();
    dataTable.draw();
    
    /*if (dataTable.data().length === 0) {
        alert();
    }*/
}

