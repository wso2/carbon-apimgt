var apiName = "";
var appName = "";

var apiFilter = "allAPIs";
var currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

$( document ).ready(function() {

    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action:"getFirstAccessTime", currentLocation : currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    from = new Date(json.usage[0].year, json.usage[0].month-1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes());

                    //day picker
                    $('#today-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - 86400000);
                    });

                    //hour picker
                    $('#hour-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - 3600000);
                    });

                    //week picker
                    $('#week-btn').on('click', function () {
                        getDateTime(currentDay, currentDay - 604800000);
                    });

                    //month picker
                    $('#month-btn').on('click', function () {
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
                        opens: 'left'
                    });
                    
                    $("#apiFilter").change(function (e) {
                    	apiFilter = this.value;
                    	drawThrottledTimeGraph(apiName, appName, from,to,apiFilter);
                    });
                    
                    $('#date-range').on('apply.daterangepicker', function (ev, picker) {
                        btnActiveToggle(this);
                        from = convertTimeString(picker.startDate);
                        to = convertTimeString(picker.endDate);
                        var fromStr = from.split(" ");
                        var toStr = to.split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        $("#date-range span").html(dateStr);
                        drawThrottledTimeGraph(apiName, appName, from, to,apiFilter);
                    });


                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    $("#apiSelect").change(function (e) {
                        apiName = this.value;
                        pupulateAppList(apiName);
                        var datePicker=$('#date-range').data('daterangepicker');
                        drawThrottledTimeGraph(apiName, appName, from, to,apiFilter);
                    });

                    $("#appSelect").change(function (e) {
                        appName = this.value;
                        var datePicker=$('#date-range').data('daterangepicker');
                        drawThrottledTimeGraph(apiName, appName, from, to,apiFilter);
                    });

                    pupulateAPIList();
                    getDateTime(to, from);
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

});

var pupulateAPIList = function() { 
    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action : "getAPIsForThrottleStats", currentLocation : currentLocation},
        function (json) {
            if (!json.error) {

                var  apis = '';
                
                if (json.usage.length == 0) {
                    apis = '<option data-hidden="true">No APIs Available</option>';
                }

                for ( var i=0; i < json.usage.length ; i++){
                    if ( i == 0){
                        apis += '<option selected="selected">' + json.usage[i] + '</option>'
                    } else {
                        apis += '<option>' + json.usage[i] + '</option>'
                    }
                }

                $('#apiSelect')
                    .empty()
                    .append(apis)
                    .selectpicker('refresh')                    
                    .trigger('change');
            } else {
                $('#chartContainer').html('');
                //$('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class=\"col-sm-4 alert alert-info\" role=\"alert\"><i class=\"icon fw fw-warning\"></i>No Data Available.<button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden=\"true\"><i class=\"fw fw-cancel\"></i></span></button></div></div>'));
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
};

var pupulateAppList = function(apiName) { 
    jagg.post("/site/blocks/stats/application-throttledcounts/ajax/stats.jag", { action : "getAppsForThrottleStats", currentLocation : currentLocation, apiName : apiName },
        function (json) {
            if (!json.error) {
                var  apps = '<option selected="selected">All Apps</option>';

                for ( var i=0; i< json.usage.length ; i++){
                    apps += '<option>' + json.usage[i] + '</option>'
                }

                $('#appSelect')
                    .empty()
                    .append(apps)
                    .selectpicker('refresh')
                    .trigger('change');

            } else {
                $('#chartContainer').html('');
                $('#chartContainer').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
};

var drawThrottledTimeGraph = function (apiName, appName, fromDate, toDate) {
    
    // example date format - 2015-06-25 14:00:00 
    if (fromDate.split(":").length == 2) {
        fromDate = fromDate + ":00";
    }

    if (toDate.split(":").length == 2) {
        toDate = toDate + ":00";
    }

    if (appName == "All Apps") {
        appName = "";
    }
    
    if(apiName == ""){
        return;
    }

    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action: "getThrottleDataOfAPIAndApplication", currentLocation : currentLocation, apiName : apiName , appName : appName , fromDate: fromDate, toDate: toDate, apiFilter:apiFilter },

        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                    var length = json.usage.result.length;
                    var result = json.usage.result;
                    var groupBy = json.usage.groupBy;
                    var timeUnitMili = json.usage.timeUnitMili;
                    var successValues = [];
                    var throttledValues = [];
                    var data = [];
                    if (length > 0) {

                        $('#chartContainer').show();
                        $('#chartContainer').empty();

                        var normalizeTime = function(time) {
                            return (time - convertDateToLong(result[0].time)) /timeUnitMili;
                        };

                        var denormalizeTime = function(time) {
                            return time * timeUnitMili + convertDateToLong(result[0].time);
                        };

                        nv.addGraph(function() {
                        var chart =  nv.models.stackedAreaChart();
                        var dateFormat = '%d/%m %H:%M';
                        if (groupBy == 'day') {
                            dateFormat = '%d/%m';
                        }

                        chart.xAxis.axisLabel('Time (' + groupBy + ')')
                           .tickFormat(function (d) {
                                return d3.time.format(dateFormat)(new Date(denormalizeTime(d)))});
                        chart.yAxis.axisLabel('Count')
                            .tickFormat(d3.format('d'));
                        chart.useInteractiveGuideline(true);

                        var timeX = normalizeTime(convertDateToLong(result[0].time));
                        successValues.push({
                                "x" : timeX,
                                "y" : result[0].successRequestCount
                        });
                        throttledValues.push({
                                "x" : timeX,
                                "y" : result[0].throttleOutCount
                        });

                        for (var i = 1; i < length; i++) {

                            var timeSegStart = convertDateToLong(result[i-1].time);
                            var timeSegEnd = convertDateToLong(result[i].time);

                            for (var j = timeSegStart + timeUnitMili; j < timeSegEnd ; j += timeUnitMili) {

                                timeX = normalizeTime(j);
                                successValues.push({
                                    "x" : timeX,
                                    "y" : 0
                                });
                                throttledValues.push({
                                    "x" : timeX,
                                    "y" : 0
                                });
                            }

                            timeX = normalizeTime(convertDateToLong(result[i].time));
                            successValues.push({
                                "x" : timeX,
                                "y" : result[i].successRequestCount
                            });
                            throttledValues.push({
                                "x" : timeX,
                                "y" : result[i].throttleOutCount
                            });
                        }

                        d3.select('#throttledTimeChart svg').datum([
						  {
						    key: "Throttled Count",
						    color: "#BD362F",
						    values: throttledValues
						  },
						  {
						    key: "Success Count",
						    color: "#60CA60",
						    values: successValues
						  }
                        ]).transition().duration(500).call(chart);
                        nv.utils.windowResize(chart.update);
                            return chart;
                        });

                        $('#chartContainer').append($('<div id="throttledTimeChart"><svg style="height:600px;"></svg></div>'));
                        $('#throttledTimeChart svg').show();

                    }else if(length == 0) {
                        $('#chartContainer').html('');
                        $('#chartContainer').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
                    }
            } else {
                $('#chartContainer').html('');
                //$('#chartContainer').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
};

function getDateTime(currentDay,fromDay){  
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawThrottledTimeGraph(apiName, appName, from, to, apiFilter);
}