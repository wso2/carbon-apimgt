var currentLocation;
var apiFilter = "allAPIs";
var statsEnabled = isDataPublishingEnabled();

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

currentLocation=window.location.pathname;
    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
        function (json) {
            if (!json.error) {
                if( json.usage && json.usage.length > 0){
                    var d = new Date();
                    from = new Date(json.usage[0].year, json.usage[0].month-1, json.usage[0].day);
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

                    $('#date-range').click(function(){
                         $(this).removeClass('active');
                    });

                    //date picker
                    $('#date-range').daterangepicker({
                          timePicker: true,
                          timePickerIncrement: 30,
                          format: 'YYYY-MM-DD h:mm',
                          opens: 'left',
                    });
                    
                    $("#apiFilter").change(function (e) {
                    	apiFilter = this.value;
                    	drawAPIResponseFaultCountChart(from,to,apiFilter);
                    });

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       from = convertTimeString(picker.startDate);
                       to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawAPIResponseFaultCountChart(from,to,apiFilter);
                    });
                    

                    getDateTime(to,from);

                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).siblings().removeClass('active');
                        $(this).addClass('active');
                    });
                } else {
                    $('.stat-page').html("");
                    showEnableAnalyticsMsg();
                }
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
        }, "json");


var drawAPIResponseFaultCountTable = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getAPIResponseFaultCount", currentLocation:currentLocation,fromDate:fromDate,toDate:toDate, apiFilter:apiFilter},
        function (json) {
            if (!json.error) {
                $('#apiFaultyTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                $('#noData').empty();
                $('#tableContainer').empty();

                if(length>0){

                $('div#apiFaultyTable_wrapper.dataTables_wrapper.no-footer').remove();
                var chart;
                var $dataTable =$('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiFaultyTable"></table>');

                $dataTable.append($('<thead class="tableHead"><tr>'+
                                        '<th>' + i18n.t("API")+ '</th>'+
                                        '<th>' + i18n.t("Version") + '</th>'+
                                        '<th>' + i18n.t("Count")+ '</th>'+
                                        '<th width="20%" >' + i18n.t("Percentage")+ '</th>'+
                                    '</tr></thead>'));

                for (var i = 0; i < json.usage.length; i++) {
                    $dataTable.append($('<tr><td>' + json.usage[i].apiName + '</td><td>' + json.usage[i].version + '</td><td>' + json.usage[i].count + '</td><td style="text-align:right">' + Math.round(json.usage[i].faultPercentage * 100) / 100 +'%</span></td></tr>'));
                }

                $('#tableContainer').append($dataTable);
                $('#tableContainer').show();
                $('#apiFaultyTable').datatables_extended({
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
                //$('select').css('width','80px');

                }else if (length == 0) {
                    $('#tableContainer').hide();
                    $('div#apiFaultyTable_wrapper.dataTables_wrapper.no-footer').remove();
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
        }, "json");
}

var drawAPIResponseFaultCountChart = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/faulty-invocations/ajax/stats.jag", { action:"getAPIResponseFaultCount",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate,apiFilter:apiFilter },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                var length = json.usage.length,s1 = [];
                $('#chartContainer').empty();
                $('#noData').empty();

                if (length > 0) {
                    var faultData = [];
                    var data=[];

                    //chart data
                    for (var i = 0; i < length; i++) {
                        faultData.push({x:i, y:parseFloat(json.usage[i].count), label: json.usage[i].apiName +" v"+json.usage[i].version});
                        data.push({x:i, y:parseFloat(json.usage[i].totalRequestCount)-parseFloat(json.usage[i].count), label: json.usage[i].apiName});
                    }


                    var dataStructure = [
                        {
                            "key": "Success",
                            "values": data
                        },{
                            "key": "Fault",
                            "values": faultData
                        }];

                    (function (data) {
                    var colorRangeArray=["#4aa3df","#e74c3c"];
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
                            for(var i =0;i<dataStructure[0].values.length;i++){
                                labels.push(dataStructure[0].values[i].label)
                            }

                            chart.xAxis
                                .axisLabel('APIs')
                                .tickFormat(function (i) {
                                return labels[i];
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
                    $('#noData').html('');
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
        }, "json");
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
    drawAPIResponseFaultCountChart(from,to);
}
