var currentLocation;
var statsEnabled = isDataPublishingEnabled();

    currentLocation=window.location.pathname;
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
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
                       var from = convertTimeString(picker.startDate);
                       var to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>" + i18n.t("to") + "</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawRegisteredUserCountByApplications(from,to);
                       drawTopUsersGraph(from,to);
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
});

var drawTopUsersGraph = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action:"getTopAppUsers",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            $('#topUsersSpinner').hide();
            if (!json.error) {
                var length = json.usage.length;
                $('#topUsersView').empty();
                if (length > 0) {
                for(var k=0 ; k<length ;k++){
                     $('#topUsersView').append($(' <h4>'+i18n.t("Application Name: ")+ json.usage[k].appName+'</h4><div class="col-md-12" ><div class="col-md-6" ><div id="userChart'+(k+1)+'" ><svg style="height:400px;"></svg></div> </div> <div class="col-md-6"> <table class="table table-striped table-bordered" id="userTable'+(k+1)+'" class="userTable display" cellspacing="0" width="100%"><thead><tr> <th>'+i18n.t("User")+'</th><th>'+i18n.t("Number of API Calls")+'</th></tr></thead> </table> </div> </div>'));

                    var dataLength = json.usage[k].userCountArray.length,data = [];
                    $('#userTable'+(k+1)).find("tr:gt(0)").remove();
                    var chartData=[];

                    $('#userTable'+(k+1)).append($(json.usage[k].chartTableRows));

                    chartData = json.usage[k].chartData;
                    drawChart('#userChart'+(k+1),k,chartData);
                    $('#userTable'+(k+1)).datatables_extended({
                        "fnDrawCallback": function(){
                            if(this.fnSettings().fnRecordsDisplay()<=$('#userTable'+(k+1)+'_length option:selected' ).val()
                          || $('#userTable'+(k+1)+'_length option:selected' ).val()==-1)
                            $('#userTable'+(k+1)+'_paginate').hide();
                            else $('#userTable'+(k+1)+'_paginate').show();
                          }
                        });
                }
            }else{
                $('#topUsersView').html($('<div id="noData" class="message message-info"><h4><i class="icon fw fw-info"></i>'+i18n.t("No Data Available")+'</h4></div>'));
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
          .x(function(d) { return d.userName })
          .y(function(d) { return d.count })
          .showLabels(true)
          .labelType("percent")
          .showLegend(false)
          .color(d3.scale.category20().range())
          .tooltipContent( function(key, x, y){
             return  '<b>'+key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
           } );
      var chartID = "#userChart"+(i+1) +" svg";
        d3.select(chartID)
            .datum(data)
            .transition().duration(350)
            .call(chart);
      d3.selectAll(".nv-label text")
          /* Alter SVG attribute (not CSS attributes) */
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

var drawTopAppUsers = function(from,to){

    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action:"getTopAppUsers",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            if (!json.error) {
                $('#topAppUsersTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                $('#topAppUsersTable').show();
                for (var i = 0; i < json.usage.length; i++) {
                    $('#topAppUsersTable').append($('<tr><td>' + json.usage[i].appName + '</td><td>' + json.usage[i].userCountArray[0].user + '</td><td class="tdNumberCell">' + json.usage[i].userCountArray[0].count + '</td></tr>'));
                     if(json.usage[i].userCountArray.length > 1){
                        for (var j =1 ; j < json.usage[i].userCountArray.length; j++) {
                             $('#topAppUsersTable').append($('<tr><td>' + "" + '</td><td>' + json.usage[i].userCountArray[j].user + '</td><td class="tdNumberCell">' + json.usage[i].userCountArray[j].count + '</td></tr>'));
                        } 
                    }
                }
                if (length == 0) {
                    $('#topAppUsersTable').hide();
                    $('#tempLoadingSpace').html('');
                    $('#tempLoadingSpace').append($('<span class="label label-info">'+i18n.t('No Data Found ... ')+'</span>'));

                }
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
            t_on['tempLoadingSpace'] = 0;
        }, "json");
}

var drawRegisteredUserCountByApplications = function(from,to){
  var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action:"getPerAppSubscribers",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            $('#registeredUseresSpinner').hide();
            if (!json.error) {
                var length = json.usage.length,data = [];
                var appData=[];
                if (length > 0) {
                    $('#subsChart').empty();
                    var colorRangeArray =[];
                    var allSubscriptionCount = 0;
                    for (var i = 0; i < length; i++) {
                        allSubscriptionCount+=json.usage[i].userArray.length;
                    }

                    for (var i = 0; i < length; i++) {
                        appData=[];
                        var chartVal = Math.round((json.usage[i].userArray.length/ allSubscriptionCount)*100);
                        var applicationName = json.usage[i].appName;
                        var usersCount = json.usage[i].userArray.length;

                        //generating random colors for paths
                        var randomColor=getRandomColor();

                        appData.push({"name":"other","value":100-chartVal,"color":"#eee"});
                        appData.push({"name":json.usage[i].appName,"value":chartVal,"color":randomColor});

                        var w = 250;
                        var h = 250;
                        var r = 60;
                        var ir = 35;
                        var textOffset = 24;
                        var tweenDuration = 1050;

                        var lines, valueLabels, nameLabels;
                        var pieData = [];
                        var oldPieData = [];
                        var filteredPieData = [];

                        //D3 helper function to populate pie slice parameters from array data
                        var donut = d3.layout.pie().value(function(d){
                        return d.value;
                        });

                        //D3 helper function to draw arcs, populates parameter "d" in path object
                        var arc = d3.svg.arc()
                            .startAngle(function(d){ return d.startAngle; })
                            .endAngle(function(d){ return d.endAngle; })
                            .innerRadius(ir)
                            .outerRadius(r);


                        // CREATE VIS & GROUPS

                        var vis = d3.select("#subsChart").append("div:div").attr("class", "col-xs-12 col-sm-6 col-md-4 col-lg-3");
                        
                        vis = vis.append("svg:svg").style("height","200px");

                        vis.append("text").attr("class", "title_text")
                               .attr("x", 125)
                               .attr("y", 14)
                               .style("font-size", "14px").style("font-weight", "bold")
                               .style("z-index", "19")
                               .style("text-anchor", "middle")
                               .style("color","red")
                               .text(applicationName);

                        vis.append("text").attr("class", "title_text")
                               .attr("x", 125)
                               .attr("y", 30)
                               .style("font-style","italic")
                               .style("font-size", "14px").style("font-weight", "10px")
                               .style("z-index", "19")
                               .style("text-anchor", "middle")
                               .text(usersCount + " User(s)");

                        //GROUP FOR ARCS/PATHS
                        var arc_group = vis.append("svg:g")
                            .attr("class", "arc")
                            .attr("transform", "translate(" + (w/2) + "," + (h/2) + ")");

                        //GROUP FOR LABELS
                        var label_group = vis.append("svg:g")
                            .attr("class", "label_group")
                            .attr("transform", "translate(" + (w/2) + "," + (h/2) + ")");

                        //GROUP FOR CENTER TEXT
                        var center_group = vis.append("svg:g")
                            .attr("class", "center_group")
                            .attr("transform", "translate(" + (w/2) + "," + (h/2) + ")");

                        //WHITE CIRCLE BEHIND LABELS
                        var whiteCircle = center_group.append("svg:circle")
                            .attr("fill", "white")
                            .attr("r", ir)

                        // STREAKER CONNECTION
                        // to run each time data is generated
                        function update(number) {
                            data = appData;
                            oldPieData = filteredPieData;
                            pieData = donut(data);

                            //var api_name=groupData[i].api_name;
                            //var provider = groupData[i].provider;

                            var sliceProportion = 0; //size of this slice
                            filteredPieData = pieData.filter(filterData);

                            function filterData(element, index, array) {
                                element.name = data[index].name;
                                element.value = data[index].value;
                                element.color = data[index].color;
                                sliceProportion += element.value;
                                return (element.value > 0);
                            }

                            //DRAW ARC PATHS
                            paths = arc_group.selectAll("path").data(filteredPieData);

                            paths.enter().append("svg:path")
                                .attr("stroke", "white")
                                .attr("stroke-width", 0.5)
                                .attr("fill", function(d, i) { return d.color; })


                                .transition()
                                .duration(tweenDuration)
                                .attrTween("d", pieTween);

                            paths
                                .transition()
                                .duration(tweenDuration)
                                .attrTween("d", pieTween);
                            paths.exit()
                                .transition()
                                .duration(tweenDuration)
                                .attrTween("d", removePieTween)
                            .remove();



                            //DRAW TICK MARK LINES FOR LABELS
                            lines = label_group.selectAll("line").data(filteredPieData);
                            lines.enter().append("svg:line")
                                .attr("x1", 0)
                                .attr("x2", 0)
                                .attr("y1", -r-3)
                                .attr("y2", -r-15)
                                .attr("stroke", "gray")
                                .attr("display", function(d) {
                                     if(d.name=="other"){
                                        return "none";
                                     }
                                })
                                .attr("transform", function(d) {
                                    return "rotate(" + (d.startAngle+d.endAngle)/2 * (180/Math.PI) + ")";
                                });

                            lines.transition()
                                .duration(tweenDuration)
                                .attr("transform", function(d) {
                                    return "rotate(" + (d.startAngle+d.endAngle)/2 * (180/Math.PI) + ")";
                                });

                            lines.exit().remove();

                            //DRAW LABELS WITH PERCENTAGE VALUES
                            valueLabels = label_group.selectAll("text.value").data(filteredPieData);

                            valueLabels.enter().append("svg:text")
                                .attr("class", "value")
                                .attr("transform", function(d) {
                                return "translate(" + Math.cos(((d.startAngle+d.endAngle - Math.PI)/2)) * (r+textOffset) + "," + Math.sin((d.startAngle+d.endAngle - Math.PI)/2) * (r+textOffset) + ")";
                            })
                                .attr("dy", function(d){
                                if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
                                    return 5;
                                } else {
                                    return -7;
                                }
                            })
                                .attr("text-anchor", function(d){
                                if ( (d.startAngle+d.endAngle)/2 < Math.PI ){
                                    return "beginning";
                                } else {
                                    return "end";
                                }
                            });

                            valueLabels.transition().duration(tweenDuration).attrTween("transform", textTween);
                            valueLabels.exit().remove();

                            //DRAW LABELS WITH ENTITY NAMES
                            nameLabels = label_group.selectAll("text.units").data(filteredPieData);

                            nameLabels.enter().append("svg:text")
                                .attr("class", "units")
                                .attr("transform", function(d) {
                                return "translate(" + Math.cos(((d.startAngle+d.endAngle - Math.PI)/2)) * (r+textOffset) + "," + Math.sin((d.startAngle+d.endAngle - Math.PI)/2) * (r+textOffset) + ")";
                            })
                                .attr("dy", function(d){
                                if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
                                    return 17;
                                } else {
                                    return 5;
                                }
                            })
                                .attr("text-anchor", function(d){
                                if ((d.startAngle+d.endAngle)/2 < Math.PI ) {
                                    return "beginning";
                                } else {
                                    return "end";
                                }
                            }).text(function(d){
                                if(d.name=="other"){
                                    return "";
                                }else{
                                    return d.value+"%";
                                    }
                            });

                            nameLabels.transition().duration(tweenDuration).attrTween("transform", textTween);
                            nameLabels.exit().remove();
                        }

                        // Interpolate the arcs in data space.
                        function pieTween(d, i) {
                            var s0;
                            var e0;
                            if(oldPieData[i]){
                                s0 = oldPieData[i].startAngle;
                                e0 = oldPieData[i].endAngle;
                            } else if (!(oldPieData[i]) && oldPieData[i-1]) {
                                s0 = oldPieData[i-1].endAngle;
                                e0 = oldPieData[i-1].endAngle;
                            } else if(!(oldPieData[i-1]) && oldPieData.length > 0){
                                s0 = oldPieData[oldPieData.length-1].endAngle;
                                e0 = oldPieData[oldPieData.length-1].endAngle;
                            } else {
                                s0 = 0;
                                e0 = 0;
                            }
                            var i = d3.interpolate({startAngle: s0, endAngle: e0}, {startAngle: d.startAngle, endAngle: d.endAngle});
                                return function(t) {
                                    var b = i(t);
                                    return arc(b);
                            };
                        }

                        function removePieTween(d, i) {
                            s0 = 2 * Math.PI;
                            e0 = 2 * Math.PI;
                            var i = d3.interpolate({startAngle: d.startAngle, endAngle: d.endAngle}, {startAngle: s0, endAngle: e0});
                                return function(t) {
                                    var b = i(t);
                                    return arc(b);
                                };
                        }

                        function textTween(d, i) {
                        var a;
                        if(oldPieData[i]){
                            a = (oldPieData[i].startAngle + oldPieData[i].endAngle - Math.PI)/2;
                        } else if (!(oldPieData[i]) && oldPieData[i-1]) {
                            a = (oldPieData[i-1].startAngle + oldPieData[i-1].endAngle - Math.PI)/2;
                        } else if(!(oldPieData[i-1]) && oldPieData.length > 0) {
                            a = (oldPieData[oldPieData.length-1].startAngle + oldPieData[oldPieData.length-1].endAngle - Math.PI)/2;
                        } else {
                            a = 0;
                        }
                        var b = (d.startAngle + d.endAngle - Math.PI)/2;

                        var fn = d3.interpolateNumber(a, b);
                        return function(t) {
                                var val = fn(t);
                                return "translate(" + Math.cos(val) * (r+textOffset) + "," + Math.sin(val) * (r+textOffset) + ")";
                            };
                        }

                    update(0);
                }
            } else {
            	$('#subsChart').html($('<div id="noData" class="message message-info"><h4><i class="icon fw fw-info"></i>'+i18n.t("No Data Available")+'</h4></div>'));
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




var drawAppUsers = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action:"getPerAppSubscribers",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            if (!json.error) {
                $('#appUsersTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                $('#appUsersTable').show();
                for (var i = 0; i < json.usage.length; i++) {
                    $('#appUsersTable').append($('<tr><td>' + json.usage[i].appName + '</td><td>' + json.usage[i].userArray.length + '</td></tr>'));
                }
                if (length == 0) {
                    $('#appUsersTable').hide();
                    $('#tempLoadingSpace').html('');
                    $('#tempLoadingSpace').append($('<span class="label label-info">'+i18n.t('No Data Found ... ')+'</span>'));
                }else{
                    $('#tempLoadingSpace').hide();
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

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/topUsers/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawRegisteredUserCountByApplications(from,to);
    drawTopUsersGraph(from,to);
}

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

function getRandomColor() {
  var letters = '0123456789ABCDEF'.split('');
  var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}
