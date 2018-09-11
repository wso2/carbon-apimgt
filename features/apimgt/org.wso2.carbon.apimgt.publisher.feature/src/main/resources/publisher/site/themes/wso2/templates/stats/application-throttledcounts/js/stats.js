var appName = "";
var currentLocation = window.location.pathname;
var apiFilter = "allAPIs";
var statsEnabled = isDataPublishingEnabled();

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

$( document ).ready(function() {

    jagg.post("/site/blocks/stats/application-throttledcounts/ajax/stats.jag", { action:"getFirstAccessTime", currentLocation : currentLocation},

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
                    	drawThrottledTimeGraph(from,to,apiFilter);
                    });
                    
                    $('#date-range').on('apply.daterangepicker', function (ev, picker) {
                        btnActiveToggle(this);
                        from = convertTimeString(picker.startDate);
                        to = convertTimeString(picker.endDate);
                        var fromStr = from.split(" ");
                        var toStr = to.split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        $("#date-range span").html(dateStr);
                        drawThrottledTimeGraph(from, to, apiFilter);
                    });

                    populateAppList();

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    $("#appSelect").change(function (e) {
                        appName = this.value;
                        getDateTime(to, from);
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
});

var populateAppList = function() { 
    jagg.post("/site/blocks/stats/application-throttledcounts/ajax/stats.jag", { action : "getAppsForThrottleStats", currentLocation : currentLocation},
        function (json) {
            if (!json.error) {
                var  apps = '';

                if (json.usage.length == 0) {
                    apps = '<option data-hidden="true">' + i18n.t('No apps available')+ '</option>';
                }
                for ( var i=0; i < json.usage.length ; i++){
                    if ( i == 0){
                        apps += '<option selected="selected">' + json.usage[i] + '</option>'
                    } else {
                        apps += '<option>' + json.usage[i] + '</option>'
                    }
                }

                $('#appSelect')
                    .empty()
                    .append(apps)
                    .selectpicker('refresh')
                    .trigger('change');
            } else {
                $('#chartContainer').html('');
                $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="' + i18n.t('No Stats')+ '"></i>' + i18n.t('No Data Available')+ '.</h4></div></div>'));
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
};

var drawThrottledTimeGraph = function (fromDate, toDate) {

    // example date format - 2015-06-25 14:00:00 
    if (fromDate.split(":").length == 2) {
        fromDate = fromDate + ":00";
    }
    if (toDate.split(":").length == 2) {
        toDate = toDate + ":00";
    }
    if(appName == ""){
        return;
    }

    jagg.post("/site/blocks/stats/application-throttledcounts/ajax/stats.jag", { action: "getThrottleDataOfApplication", currentLocation : currentLocation, appName : appName, fromDate: fromDate, toDate: toDate, apiFilter: apiFilter },

        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                    var length = json.usage.length;
                    var data = [];
                    if (length > 0) {
                        $('#chartContainer').show();
                        $('#chartContainer').empty();

                        nv.addGraph(function() {
                        var chart = nv.models.multiBarChart();
                        chart.xAxis.axisLabel('APIs');
                        chart.yAxis.axisLabel('Count')
                            .tickFormat(d3.format('d'));
                        chart.multibar.stacked(true);

                        var result = json.usage;
                        var successValues = [];
                        var throttledValues = [];

                        for (var i = 0; i < length; i++) {
                            successValues.push({
                                    "x" : result[i].apiName,
                                    "y" : result[i].successRequestCount
                            });
                            throttledValues.push({
                                    "x" : result[i].apiName,
                                    "y" : result[i].throttleOutCount
                            });
                        }

                        d3.select('#throttledTimeChart svg').datum([
                          {
                            key: "Success Count",
                            color: "#60CA60",
                            values: successValues
                          },
                          {
                            key: "Throttled Count",
                            color: "#BD362F",
                            values: throttledValues
                          }
                        ]).transition().duration(500).call(chart);
                        nv.utils.windowResize(chart.update);
                            return chart;
                        });

                        $('#chartContainer').append($('<div id="throttledTimeChart"><svg style="height:600px;"></svg></div>'));
                        $('#throttledTimeChart svg').show();

                    }else if(length == 0) {
                        $('#chartContainer').html('');
                        $('#chartContainer').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="'+i18n.t('No-Stats')+'"></i>'+i18n.t('No Data Available')+'.</h4></div></div>'));
                    }
            } else {
                $('#chartContainer').html('');
                $('#chartContainer').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="'+i18n.t('No-Stats')+'"></i>'+i18n.t('No Data Available')+'.</h4></div></div>'));
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
}


function getDateTime(currentDay,fromDay){  
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawThrottledTimeGraph(from,to);
}