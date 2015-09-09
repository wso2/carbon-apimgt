var apiName = "";
var appName = "";
var currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

require(["dojo/dom", "dojo/domReady!"], function (dom) {

    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action:"getFirstAccessTime", currentLocation : currentLocation },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
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
                    $('#date-range').dateRangePicker(
                        {
                            startOfWeek: 'monday',
                            separator: ' <b>to</b> ',
                            format: 'YYYY-MM-DD HH:mm',
                            autoClose: false,
                            time: {
                                enabled: true
                            },
                            shortcuts: 'hide',
                            endDate: currentDay
                        })
                        .bind('datepicker-apply', function (event, obj) {
                            btnActiveToggle(this);
                            from = convertDate(obj.date1);
                            to = convertDate(obj.date2);
                            var fromStr = from.split(" ");
                            var toStr = to.split(" ");
                            var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                            $("#date-range").html(dateStr);
                            drawThrottledTimeGraph(apiName, appName, from, to);
                        });


                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    $("#apiSelect").change(function (e) {
                        apiName = this.value;
                        pupulateAppList(apiName);
                        getDateTime(to, from);
                    });

                    $("#appSelect").change(function (e) {
                        appName = this.value;
                        getDateTime(to, from);
                    });

                    pupulateAPIList();

                } else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><img src="../themes/default/templates/stats/images/statsEnabledThumb.png" alt="Stats Enabled"></div>'));
                } else {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><span class="label top-level-warning"><i class="icon-warning-sign icon-white"></i>'
                        + i18n.t('errorMsgs.checkBAMConnectivity') + '</span><br/><img src="../themes/default/templates/stats/api-throttledcounts/images/statsThumb.png" alt="Smiley face"></div>'));
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
    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action : "getAPIsForThrottleStats", currentLocation : currentLocation },
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
                    .trigger('change');

            } else {
                $('#tempLoadingSpaceAPIThrottleCount').html('');
                $('#tempLoadingSpaceAPIThrottleCount').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
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
                    .trigger('change');

            } else {
                $('#tempLoadingSpaceAPIThrottleCount').html('');
                $('#tempLoadingSpaceAPIThrottleCount').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
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

    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action: "getThrottleDataOfAPIAndApplication", currentLocation : currentLocation, apiName : apiName , appName : appName , fromDate: fromDate, toDate: toDate},

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
                        $('#tempLoadingSpaceAPIThrottleCount').empty();

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
                                "y" : result[0].success_request_count
                        });
                        throttledValues.push({
                                "x" : timeX,
                                "y" : result[0].throttleout_count
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
                                "y" : result[i].success_request_count
                            });
                            throttledValues.push({
                                "x" : timeX,
                                "y" : result[i].throttleout_count
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
                        $('.filters').css('display','none');
                        $('#chartContainer').hide();
                        $('#tableContainer').hide();
                        $('#tempLoadingSpaceAPIThrottleCount').html('');
                        $('#tempLoadingSpaceAPIThrottleCount').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                    }
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }
    , "json");
};


var convertTimeString = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate())+" "+formatTimeChunk(d.getHours())+":"+formatTimeChunk(d.getMinutes());
    return formattedDate;
};

var convertTimeStringPlusDay = function (date) {
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth() + 1)) + "-" + formatTimeChunk(d.getDate() + 1);
    return formattedDate;
};

function convertDate(date) {
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour=date.getHours();
    var minute=date.getMinutes();
    return date.getFullYear() + '-' + (('' + month).length < 2 ? '0' : '')
        + month + '-' + (('' + day).length < 2 ? '0' : '') + day +" "+ (('' + hour).length < 2 ? '0' : '')
        + hour +":"+(('' + minute).length < 2 ? '0' : '')+ minute;
}

var formatTimeChunk = function (t) {
    if (t < 10) {
        t = "0" + t;
    }
    return t;
};

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

function getDateTime(currentDay,fromDay){  
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('dateRangePicker').setDateRange(from,to);
    drawThrottledTimeGraph(apiName, appName, from, to);
}

function convertDateToLong(date){
    var allSegments=date.split(" ");
    var dateSegments=allSegments[0].split("-");
    var timeSegments=allSegments[1].split(":");
    var newDate = new Date(dateSegments[0],(dateSegments[1]-1),dateSegments[2],timeSegments[0],timeSegments[1],timeSegments[2]);
    return newDate.getTime();
}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/api-throttledcounts/ajax/stats.jag", { action: "isDataPublishingEnabled"},
        function (json) {
            if (!json.error) {
                statsEnabled = json.usage;
                return statsEnabled;
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");
}