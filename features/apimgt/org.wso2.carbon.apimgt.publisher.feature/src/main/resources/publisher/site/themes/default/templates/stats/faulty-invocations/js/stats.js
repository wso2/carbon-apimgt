var t_on = {
    'apiChart':1,
    'subsChart':1,
    'serviceTimeChart':1,
    'tempLoadingSpace':1
};
var currentLocation;
var statsEnabled = isDataPublishingEnabled();

var chartColorScheme1 = ["#3da0ea","#bacf0b","#e7912a","#4ec9ce","#f377ab","#ec7337","#bacf0b","#f377ab","#3da0ea","#e7912a","#bacf0b"];
//fault colors || shades of red
var chartColorScheme2 = ["#ED2939","#E0115F","#E62020","#F2003C","#ED1C24","#CE2029","#B31B1B","#990000","#800000","#B22222","#DA2C43"];
//fault colors || shades of blue
var chartColorScheme3 = ["#0099CC","#436EEE","#82CFFD","#33A1C9","#8DB6CD","#60AFFE","#7AA9DD","#104E8B","#7EB6FF","#4981CE","#2E37FE"];
currentLocation=window.location.pathname;

require(["dojo/dom", "dojo/domReady!"], function(dom){
    currentLocation=window.location.pathname;
    //Initiating the fake progress bar
    jagg.fillProgress('apiChart');jagg.fillProgress('subsChart');jagg.fillProgress('serviceTimeChart');jagg.fillProgress('tempLoadingSpace');

    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
        function (json) {

            if (!json.error) {

                if( json.usage && json.usage.length > 0){
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month-1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());

                    //day picker
                    $('#today-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-86400000);
                    });

                    //hour picker
                    $('#hour-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-3600000);
                    })

                    //week picker
                    $('#week-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-604800000);
                    })

                    //month picker
                    $('#month-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-(604800000*4));
                    });

                    //date picker
                    $('#date-range').dateRangePicker(
                        {
                            startOfWeek: 'monday',
                            separator : ' to ',
                            format: 'YYYY-MM-DD HH:mm',
                            autoClose: false,
                            time: {
                                enabled: true
                            },
                            shortcuts:'hide',
                            endDate:currentDay
                        })
                        .bind('datepicker-apply',function(event,obj)
                        {
                             btnActiveToggle(this);
                             var from = convertDate(obj.date1);
                             var to = convertDate(obj.date2);
                             var fromStr = from.split(" ");
                             var toStr = to.split(" ");
                             var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                             $("#date-range").html(dateStr);
                             drawAPIResponseFaultCountChart(from,to);

                        });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    getDateTime(to,from);

                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).siblings().removeClass('active');
                        $(this).addClass('active');
                    });

                    var width = $("#rangeSliderWrapper").width();
                    //$("#rangeSliderWrapper").affix();
                    $("#rangeSliderWrapper").width(width);

                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><img src="../themes/default/templates/stats/images/statsEnabledThumb.png" alt="Stats Enabled"></div>'));
                }

                else{
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><span class="label top-level-warning"><i class="icon-warning-sign icon-white"></i>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/default/templates/stats/faulty-invocations/images/statsThumb.png" alt="Smiley face"></div>'));
                }
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
            t_on['apiChart'] = 0;
        }, "json");

});

var drawAPIResponseFaultCountTable = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getAPIResponseFaultCount", currentLocation:currentLocation,fromDate:fromDate,toDate:toDate},
        function (json) {
            if (!json.error) {
                $('#apiFaultyTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                $('#tempLoadingSpace').empty();
                $('#tableContainer').empty();

                if(length>0){

                $('div#apiFaultyTable_wrapper.dataTables_wrapper.no-footer').remove();
                var chart;
                var $dataTable =$('<table class="display defaultTable" width="100%" cellspacing="0" id="apiFaultyTable"></table>');

                $dataTable.append($('<thead class="tableHead"><tr>'+
                                        '<th>api</th>'+
                                        '<th>version</th>'+
                                        '<th>count</th>'+
                                        '<th width="20%" >percentage</th>'+
                                    '</tr></thead>'));

                for (var i = 0; i < json.usage.length; i++) {
                    $dataTable.append($('<tr><td>' + json.usage[i].apiName + '</td><td>' + json.usage[i].version + '</td><td>' + json.usage[i].count + '</td><td style="text-align:right">' + Math.round(json.usage[i].faultPercentage * 100) / 100 +'%</span></td></tr>'));
                }

                $('#tableContainer').append($dataTable);
                $('#tableContainer').show();
                $('#apiFaultyTable').DataTable({
                     "order": [
                        [ 3, "desc" ]
                     ],
                     "fnDrawCallback": function(){
                         if(this.fnSettings().fnRecordsDisplay()<=$("#apiFaultyTable_length option:selected" ).val()
                         || $("#apiFaultyTable_length option:selected" ).val()==-1)
                             $('#apiFaultyTable_paginate').hide();
                         else
                             $('#apiFaultyTable_paginate').show();
                     },
                });
                $('select').css('width','60px');

                }else if (length == 0) {
                    $('#tableContainer').hide();
                    $('#tempLoadingSpace').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
            t_on['tempLoadingAPIFaulty'] = 0;
        }, "json");

}

var drawAPIResponseFaultCountChart = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getAPIResponseFaultCount",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate },
        function (json) {
            if (!json.error) {
                var length = json.usage.length,s1 = [];
                $('#chartContainer').empty();
                $('#tempLoadingSpace').empty();

                if (length > 0) {
                    var faultData = [];
                    var data=[];

                    //chart data
                    for (var i = 0; i < length; i++) {
                        faultData.push({x:i, y:parseFloat(json.usage[i].count), label: json.usage[i].apiName +" v"+json.usage[i].version});
                        data.push({x:i, y:parseFloat(json.usage[i].totalRequestCount)-parseFloat(json.usage[i].count), label: json.usage[i].apiName});
                    }


                    var dataStructure = [{
                            "key": "Fault",
                            "values": faultData
                        },
                        {
                            "key": "Success",
                            "values": data
                        }];

                    (function (data) {
                    var colorRangeArray=["#e74c3c","#4aa3df"];
                        var colors = d3.scale.ordinal()
                           .range(colorRangeArray);
                        keyColor = function (d, i) {
                            return colors(d.key)
                        };

                        var chart;
                        nv.addGraph(function () {
                            chart = nv.models.stackedAreaChart().margin({left:80,right:50})
                                .x(function (d) {
                                return d.x
                            })
                                .y(function (d) {
                                return d.y
                            })
                                .color(keyColor)
                                .useInteractiveGuideline(true);

                            if (dataStructure[0].values.length > 4) chart.margin({bottom: 160});

                            var labels = [];
                            for(var i = 0; i <dataStructure[0].values.length ; i++){
                                labels.push(dataStructure[0].values[i].label)
                            }

                            chart.xAxis
                                .axisLabel('APIs')
                                .tickFormat(function (d,i) {
                                return labels[i];;
                            });
                            chart.xAxis.tickValues(dataStructure[0].values.map( function(d){return d.x;}));
                            if (dataStructure[0].values.length > 4) chart.xAxis.rotateLabels(-45);

                            chart.yAxis.axisLabel('Total Hits');
                            chart.yAxis.tickFormat(d3.format(',d'));

                            d3.select('#faultyCountChart svg')
                              .datum(data)
                              .transition().duration(0)
                              .call(chart);

                            nv.utils.windowResize(chart.update);
                            return chart;
                      });
                    })(dataStructure);
                    $('#chartContainer').append($('<div id="faultyCountChart" class="with-3d-shadow with-transitions"><svg style="height:500px;"></svg></div>'));
                    $('#chartContainer').show();
                    $('#faultyCountChart svg').show();

                    drawAPIResponseFaultCountTable(fromDate,toDate);

                } else {
                    $('#tableContainer').hide();
                    $('#chartContainer').hide();
                    $('#tempLoadingSpace').html('');
                    $('#tempLoadingSpace').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
            t_on['faultyCountChart'] = 0;
        }, "json");
}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/api-usage/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

var convertTimeString = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate())+" "+formatTimeChunk(d.getHours())+":"+formatTimeChunk(d.getMinutes());
    return formattedDate;
};

var convertTimeStringPlusDay = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate()+1);
    return formattedDate;
};

var formatTimeChunk = function (t){
    if (t<10){
        t="0" + t;
    }
    return t;
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
    $("#date-range").html(dateStr);
    $('#date-range').data('dateRangePicker').setDateRange(from,to);
    drawAPIResponseFaultCountChart(from,to);
}