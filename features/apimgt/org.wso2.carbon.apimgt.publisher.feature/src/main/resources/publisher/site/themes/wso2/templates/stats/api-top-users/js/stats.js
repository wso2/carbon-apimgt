var currentLocation = "allAPIs";
var statsEnabled = isDataPublishingEnabled();
var apiNameVersionMap;
var apiName;
var version;
var d = new Date();
var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(),d.getSeconds());

//setting default date
var to;
var from;

$( document ).ready(function() {
    populateAPIList();
    
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
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });
                    $("#versionSelect").change(function (e) {
                        version = this.value;
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });
                    
                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    //day picker
                    $('#today-btn').on('click',function(){
                        getDateTime(currentDay, currentDay-86400000);
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });

                    //week picker
                    $('#week-btn').on('click',function(){
                        getDateTime(currentDay, currentDay-604800000);
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });

                    //month picker
                    $('#month-btn').on('click',function(){
                        getDateTime(currentDay, currentDay-(604800000*4));
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });

                    $('#date-range').click(function(){
                        $(this).removeClass('active');
                    });

                    //date picker
                    $('#date-range').daterangepicker({
                        timePicker: true,
                        timePickerIncrement: 30,
                        format: 'YYYY-MM-DD',
                        opens: 'left'
                    });

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                        btnActiveToggle(this);
                        from = convertTimeString(picker.startDate);
                        to = convertTimeString(picker.endDate);
                        var fromStr = from.split(" ");
                        var toStr = to.split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        $("#date-range span").html(dateStr);
                        var table = $('#apiTopUsersTable').DataTable();
                        table.ajax.reload();
                    });
                    
                    //set time picker for last month
                    getDateTime(currentDay, currentDay-(604800000*4));
                    drawTopAPIUsersTable();
                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    showNoDataAnalyticsMsg();
                }

                else {
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
    /*
    $("#apiSelect").change(function (e) {
        apiName = this.value;
        //populateVersionList(apiName,false);
        var table = $('#apiTopUsersTable').DataTable();
        table.ajax.reload();
    });
    $("#versionSelect").change(function (e) {
        version = this.value;
        var table = $('#apiTopUsersTable').DataTable();
        table.ajax.reload();
        //drawTopAPIUsersTable();
    });*/

    
});

var drawTopAPIUsersTable = function() {
    $('#spinner').hide();
    var $dataTable =$('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiTopUsersTable"></table>');

    //getting timezone value
    var date=new Date();
    var offset = date.getTimezoneOffset();

    function convertToHHMM(info) {
       var hrs = parseInt(Number(info));
       var min = Math.round((Number(info)-hrs) * 60);
       return (('' + hrs).length < 2 ? '0' : '') + hrs+':'+(('' + min).length < 2 ? '0' : '')+min;
    }

    var timezone;
    if(offset>=(-840) && offset<=720){
         if(offset==0 || offset<0){
            timezone=" (GMT+"+convertToHHMM(Math.abs(offset)/60)+")";
         }
         else{
            timezone=" (GMT-"+ convertToHHMM(Math.abs(offset)/60)+")";
         }
    }else{
       timezone=" ";
    }

    $dataTable.append($('<thead class="tableHead"><tr>'+
                            '<th width="20%">User</th>'+
                            '<th  width="15%">Number of API Calls</th>'+
                        '</tr></thead>'));
    $('#tableContainer').append($dataTable);

    $('#apiTopUsersTable').DataTable( {
    "processing": true,
    "serverSide": true,
    "searching": false,
    "ajax": {
        "url": "../../site/blocks/stats/api-top-users/ajax/stats.jag",
        "data": function ( d ) {
            d.action = "getTopApiUsers";
            d.currentLocation = currentLocation;
            d.apiName = apiName;
            d.version = version;
            d.fromDate = from;
            d.toDate = to;
        }
    },
    "columns": [
        { "data": "user" },
        { "data": "totalRequestCount" }
    ]
    } );
};

var populateAPIList = function() {
    jagg.post("/site/blocks/stats/ajax/stats.jag", { action : "getAPIList" ,currentLocation:currentLocation},
        function (json) {
            if (!json.error) {
                apiNameVersionMap = json.apiNameVersionMap;
                var i=0;
                var apis= '';
                for (var name in apiNameVersionMap) {
                    if (i==0) {
                        apis+='<option selected="selected" value='+name+'>' + name + '</option>';
                    }else{
                        apis+='<option value='+name+'>' + name+ '</option>';
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

var populateVersionList = function(apiName){
    var i=0;
    var selectVersions = '<option selected="selected" value="FOR_ALL_API_VERSIONS">All Versions</option>';
    for (var version in apiNameVersionMap[apiName]) {
        var tempVersion = apiNameVersionMap[apiName][version];
        selectVersions += '<option value='+tempVersion+'>' + tempVersion+ '</option>';
        i++;
    }
    $('#versionSelect')
        .empty()
        .append(selectVersions)
        .selectpicker('refresh')
        .trigger('change');
};

function getDateTime(currentDay,fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
}

function round(value, decimals) {
    return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}