var currentLocation;

currentLocation=window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

    currentLocation=window.location.pathname;
    jagg.post("/site/blocks/stats/perAppAPICount/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
        function (json) {
            if (!json.error) {
                if( json.usage && json.usage.length > 0){
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month, json.usage[0].day);
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

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       //$('#date-range').toggleClass('active');
                       var from = convertTimeString(picker.startDate);
                       var to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>" + i18n.t("to") + "</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawGraphAPIUsage(from,to);
                    });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    getDateTime(to,firstAccessDay);

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
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


$(document).ready(function(){
    $(document).scroll(function(){
        var top=$(document).scrollTop();
        var width = $("#rangeSliderWrapper").width();
        if(top > 180){
            $("#rangeSliderWrapper").css("position","fixed").css("top","50px").width(width);
        }else{
           $("#rangeSliderWrapper").css({ "position": "relative", "top": "0px" });
        }
    })
})

var drawGraphAPIUsage = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/perAppAPICount/ajax/stats.jag", { action:"getProviderAPIUsage",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                var dataLength = json.usage.length;
                if(dataLength>0){
                $('#apiUsage').empty();
                    for(var k=0 ; k<dataLength ;k++){
                    $('#apiUsage').append($('<h4>Application Name:  '+json.usage[k].appName+'</h4><div class="col-md-12"><div class="col-md-6"><div id="apiChart'+(k+1)+'" class="chart"><svg style="height:400px;"></svg></div></div> <div class="col-md-6"> <table class="table table-striped table-bordered" id="apiTable'+(k+1)+'" class="display" cellspacing="0" width="100%"><thead><tr> <th>API Name</th><th>Number of API Calls</th></tr></thead> </table> </div></div>'));
                    }

                     for(var k=0 ; k<dataLength ;k++){

                        var length = json.usage[k].apiCountArray.length,data = [];
                        var chartData=[];
                        for (var i = 0; i < length; i++) {

                            data[i] = [ json.usage[k].apiCountArray[i].apiName, parseInt( json.usage[k].apiCountArray[i].count )];
                            $('#apiTable'+(k+1)).append($('<tr><td>' +  json.usage[k].apiCountArray[i].apiName + '</td><td class="tdNumberCell">' +json.usage[k].apiCountArray[i].count + '</td></tr>'));
                            chartData.push({"apiName":json.usage[k].apiCountArray[i].apiName,
                                            "count":parseInt( json.usage[k].apiCountArray[i].count )
                            });

                        }
                        drawChart('#apiChart'+(k+1),k,chartData);
                        if (length > 0) {
                            $('#apiTable'+(k+1)).datatables_extended({
                            "fnDrawCallback": function(){
                                if(this.fnSettings().fnRecordsDisplay()<=$('#apiTable'+(k+1)+'_length option:selected' ).val()
                              || $('#apiTable'+(k+1)+'_length option:selected' ).val()==-1)
                                $('#apiTable'+(k+1)+'_paginate').hide();
                                else $('#apiTable'+(k+1)+'_paginate').show();
                              }
                            });
                            $('#apiTable'+(k+1)).show();
                        }
                     }
                    }else{
                        $('#apiUsage').html($('<div id="noData" class="message message-info"><h4><i class="icon fw fw-info"></i>'+i18n.t("No Data Available")+'</h4></div>'));
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
function drawChart(div,i,data) {
    var h = 600;
    var r = h/2;
    var arc = d3.svg.arc().outerRadius(r);

    nv.addGraph(function() {
    var chart = nv.models.pieChart()
      .x(function(d) { return d.apiName })
      .y(function(d) { return d.count })
      .showLabels(true)
      .labelType("percent")
      .showLegend(false)
      .color(d3.scale.category20().range())
      .tooltipContent( function(key, x, y){
         return  '<b>'+key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
       } );;
    var chartID = "#apiChart"+(i+1) +" svg";
    d3.select(chartID)
        .datum(data)
        .transition().duration(350)
        .call(chart);
    d3.selectAll(".nv-label text")
      .attr("transform", function(d){
          d.innerRadius = -450;
          d.outerRadius = r;
          return "translate(" + arc.centroid(d) + ")";}
      )
      .attr("text-anchor", "middle")
      .style({"font-size": "0.7em"});

    nv.utils.windowResize(chart.update);
    return chart;
    });
}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/perAppAPICount/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawGraphAPIUsage(from,to);
}