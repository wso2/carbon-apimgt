var currentLocation;
var statsEnabled = isDataPublishingEnabled();
var apiFilter = "allAPIs";
//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

currentLocation = window.location.pathname;

    jagg.post("/site/blocks/stats/api-response-times/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());//                    if (firstAccessDay.valueOf() == currentDay.valueOf()) {

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
                    	drawProviderAPIServiceTime(from,to,apiFilter);
                    });

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       from = convertTimeString(picker.startDate);
                       to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawProviderAPIServiceTime(from,to,apiFilter);
                    });
                    

                    getDateTime(to,from);

                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><img src="../themes/wso2/images/statsEnabledThumb.png" alt="' + i18n.t("Thumbnail image when stats are enabled") + '"></div>'));
                }

                else{
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><span class="top-level-warning"><span class="glyphicon glyphicon-warning-sign blue"></span>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/wso2/images/statsThumb.png" alt="' + i18n.t("Thumbnail image when stats are not configured") + '"></div>'));
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


var drawProviderAPIServiceTime = function (from, to) {
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-response-times/ajax/stats.jag", { action: "getProviderAPIServiceTime", currentLocation: currentLocation, fromDate: fromDate, toDate: toDate, apiFilter: apiFilter },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {

                    var length = json.usage.length, s1 = [];
                    var data = [];

                    if (length > 0) {
                    $('#noData').empty();
                    $('#tableContainer').empty();
                    $('#chartContainer').empty();


                    var $dataTable = $('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiSelectTable"></table>');
                        $dataTable.append($('<thead class="tableHead"><tr>' +
                            '<th width="10%"></th>' +
                            '<th>API</th>' +
                            '<th width="30%" style="text-transform:none;text-align:right">RESPONSE TIME(ms)</th>'+
                            '</tr></thead>'));


                    var chartData=[];
                    var state_array = [];
                    var defaultFilterValues=[];
                    var filterValues=[];

                        for (var i = 0; i < length; i++) {
                            chartData.push({"label":json.usage[i].apiName,"value":parseFloat(json.usage[i].serviceTime)})
                        }

                        chartData.sort(function(obj1, obj2) {
                            return obj2.value - obj1.value;
                        });

                        //default display of 15 checked entries on table
                        for (var i = 0; i < chartData.length; i++) {
                            if(i<15){
                                $dataTable.append($('<tr><td >'
                                    + '<input name="item_checkbox"  checked   id=' + i + '  type="checkbox"  data-item=' + chartData[i].label
                                    + ' class="inputCheckbox" />'
                                    + '</td><td style="text-align:left;"><label for=' + i + '>' + chartData[i].label + '</label></td>'
                                    + '<td style="text-align:right;"><label for=' + i + '>' + chartData[i].value + '</label></td></tr>'));
                                filterValues.push({"label":chartData[i].label,"value":chartData[i].value});
                                state_array.push(true);
                                defaultFilterValues.push({"label":chartData[i].label,"value":chartData[i].value});
                            } else {

                                $dataTable.append($('<tr><td >'
                                     + '<input name="item_checkbox" id=' + i + '  type="checkbox"  data-item=' + chartData[i].label
                                     + ' class="inputCheckbox" />'
                                     + '</td><td style="text-align:left;"><label for=' + i + '>' + chartData[i].label + '</label></td>'
                                     + '<td style="text-align:right;"><label for=' + i + '>' + chartData[i].value + '</label></td></tr>'));
                                filterValues.push({"label":chartData[i].label,"value":chartData[i].value});
                                state_array.push(false);
                            }
                        }

                        var data_chart = [{
                              'values': defaultFilterValues,
                              'key': 'Time',
                        }];

                        var chart;
                        nv.addGraph(function() {
                            chart = nv.models.multiBarHorizontalChart()
                                .x(function(d) { return d.label })
                                .y(function(d) { return d.value })
                                .margin({top: 0, right: 5, left: 5,bottom:50})
                                .barColor(d3.scale.category20().range())
                                .tooltips(false)
                                .showControls(true);

                        chart.yAxis
                            .axisLabel('Response Time(ms)');

                        chart.yAxis.tickFormat(d3.format('d'));
                        chart.valueFormat(d3.format('d'));

                            d3.select('#serviceTimeChart svg')
                                .datum(data_chart)
                                .call(chart);

                            $('.nv-legend').hide();
                            d3.selectAll(".nv-bar")
                                .append("text")
                                .attr("y", $('rect').attr('height')/2)
                                .attr("x", function(d) {
                                  return d3.select(this.previousSibling).attr('width')+5 ;
                                })
                                .text(function(d) {
                                  return d.label +" : "+ d.value;
                                })

                            return chart;
                        });

                        if(length<2||length==2){
                            $('#chartContainer').append($('<div id="serviceTimeChart" class="with-3d-shadow with-transitions"><svg style="height:300px;"></svg></div>'));
                        }else{
                            $('#chartContainer').append($('<div id="serviceTimeChart" class="with-3d-shadow with-transitions"><svg style="height:500px;"></svg></div>'));
                        }
                        $('#chartContainer').show();
                        $('#serviceTimeChart svg').show();
                        $('#tableContainer').append($dataTable);
                        $('#tableContainer').show();
                        $('#apiSelectTable').datatables_extended({
                             retrieve: true,
                             "order": [
                                 [ 2, "desc" ]
                             ],
                            "fnDrawCallback": function(){
                                if(this.fnSettings().fnRecordsDisplay()<=$("#apiSelectTable_length option:selected" ).val()
                                || $("#apiSelectTable_length option:selected" ).val()==-1)
                                    $('#apiSelectTable_paginate').hide();
                                else
                                    $('#apiSelectTable_paginate').show();
                            },
                             "aoColumns": [
                                 { "bSortable": false },
                                 null,
                                 null
                             ],
                         });
                         //$('select').css('width','80px');

                        var count=15;
                        //on checkbox check and uncheck event
                        $('#apiSelectTable').on('change', 'input.inputCheckbox', function () {

                            $('#chartContainer').empty();
                            var id = $(this).attr('id');
                            var check = $(this).is(':checked');
                            var tickValue = $(this).attr('data-item');
                            var draw_chart = [];

                            if (check) {
                            $('#displayMsg').html('');
                                count++;
                                //limiting to show 15 entries at a time
                                if(count>15){
                                    $('#displayMsg').html('<h5 style="color:#555">' + i18n.t('Note that the graph only shows 15 entries') + '</h5>');
                                    state_array[id] = false;
                                    $(this).prop("checked", "");
                                    count--;
                                }else{
                                state_array[id] = true;
                                }
                            } else {
                                $('#displayMsg').html('');
                                state_array[id] = false;
                                count--;
                            }

                            for(var i=0;i<filterValues.length;i++){
                                if (state_array[i]) {
                                    draw_chart.push({"label":filterValues[i].label,"value":filterValues[i].value});
                                }
                            }

                            //data for checked values
                            var data_chart = [{
                                  'values': draw_chart,
                                  'key': 'Time',
                            }];

                                var chart;
                                nv.addGraph(function() {
                                    chart = nv.models.multiBarHorizontalChart()
                                        .x(function(d) { return d.label })
                                        .y(function(d) { return d.value })
                                        .margin({top: 0, right: 5, left: 5,bottom:50})
                                        .barColor(d3.scale.category20().range())
                                        .tooltips(false)
                                        .showControls(false);

                                chart.yAxis.axisLabel('Response Time(ms)');
                                chart.yAxis.tickFormat(d3.format('d'));
                                chart.valueFormat(d3.format('d'));

                                d3.select('#serviceTimeChart svg')
                                    .datum(data_chart)
                                    .call(chart);

                                $('.nv-legend').hide();

                                d3.selectAll(".nv-bar")
                                    .append("text")
                                    .attr("y", $('rect').attr('height')/2)
                                    .attr("x", function(d) {
                                      return d3.select(this.previousSibling).attr('width');
                                    })
                                    .text(function(d) {
                                      return d.label + " : "+ d.value;
                                    })

                                return chart;
                            });

                            if(draw_chart.length<2||draw_chart.length==2){
                                $('#chartContainer').append($('<div id="serviceTimeChart" class="with-3d-shadow with-transitions"><svg style="height:300px;"></svg></div>'));
                            }else{
                                $('#chartContainer').append($('<div id="serviceTimeChart" class="with-3d-shadow with-transitions"><svg style="height:500px;"></svg></div>'));
                            }

                            $('#serviceTimeChart svg').show();
                        });
                    }else if(length == 0) {
                        $('#chartContainer').hide();
                        $('#tableContainer').hide();
                        $('#noData').html('');
                        $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="' + i18n.t("No Stats")+ '"></i>'+ i18n.t("No Data Available")+'.</h4></div></div>'));
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
function getDateTime(currentDay,fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawProviderAPIServiceTime(from,to,apiFilter);
}

