var currentLocation;
var statsEnabled = isDataPublishingEnabled();
var apiFilter = "allAPIs";

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

currentLocation=window.location.pathname;

    jagg.post("/site/blocks/stats/api-usage/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
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
                    	drawProviderAPIUsage(from,to,apiFilter);
                    });

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       from = convertTimeString(picker.startDate);
                       to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawProviderAPIUsage(from,to,apiFilter);
                    });
                    

                    getDateTime(to,from);

                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

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


var drawProviderAPIUsage = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-usage/ajax/stats.jag", { action:"getProviderAPIUsage",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate, apiFilter: apiFilter  },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {

                var length = json.usage.length,data = [];
                var inputData=[];
                $('#apiChart').empty();
                $('#tableContainer').empty();
                $('#noData').empty();
                $('div#apiTable_wrapper.dataTables_wrapper.no-footer').remove();

                var $dataTable =$('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiTable"></table>');

                $dataTable.append($('<thead class="tableHead"><tr>'+
                                        '<th>' + i18n.t("API") + '</th>'+
                                        '<th style="text-align:right">' + i18n.t("Hits") + '</th>'+
                                    '</tr></thead>'));
                
                if (length > 0) {

                //grouping data according to name and version
                var inputDataStr="";
                var apiData="";
                var apiName_Provider="";
                var groupData = [];

                for (var i = 0; i < length; i++) {

                     var apiData= JSON.parse(json.usage[i].apiName);

                     apiName_Provider=""+apiData[0]+" ("+apiData[2]+")";
                     inputData.push({
                              "apiName_Provider":apiName_Provider,
                              "api_name":apiData[0],
                              "versions":apiData[1],
                              "subscriberCount":json.usage[i].count,
                              "provider":apiData[2]
                     });
                }

                //check the existence of the array
                function isExist(array, label){
                     var result = false;
                     for(var i = 0; i < array.length; i++){
                             //check with the incoming label and current array label
                             var arrLabel = array[i].apiName_Provider;
                             if(arrLabel == label){
                                  result = true;
                                  break;
                             }
                     }
                     return result;
                }

                var apiName;
                var version;
                var api_name;
                var provider;

                inputData.map(function(data){
                      //filter apiName and version
                      apiName = data.apiName_Provider;
                      version = { version :  data.versions, Count : data.subscriberCount};
                      api_name=data.api_name;
                      provider=data.provider;

                     if(!isExist(groupData, apiName)){
                        //create new object to push data
                        var versionObj = {};
                        versionObj.apiName_Provider = apiName;
                        versionObj.api_name=api_name;
                        versionObj.provider=provider;
                        //versions array
                        versionObj.versions = [];
                        versionObj.versions.push(version);
                        groupData.push(versionObj);

                     }
                     else{
                        //push new version to existing object
                        for(var i = 0; i < groupData.length; i++){
                            if(groupData[i].apiName_Provider == apiName){
                                groupData[i].versions.push(version);
                                break;
                            }
                        }
                     }
                });

                    colors = d3.scale.category20();
                    // Synthetic data generation
                    var data = [];
                    var children = [];
                    var versionCount;
                    for (var i = 0; i < groupData.length; i++) {
                        var api_name=groupData[i].api_name;
                        var provider = groupData[i].provider;
                        var grpCount=groupData[i];
                        children = [];
                        var color = colors(i);
                        versionCount=0;
                        var allVersionCount=0;

                        for(var j = 0; j < groupData[i].versions.length; j++){
                            allVersionCount+=grpCount.versions[j].Count;
                        }
                        $dataTable.append($('<tr><td>' + api_name + '</td><td class="tdNumberCell" style="text-align:right">' + allVersionCount + '</td></tr>'));

                        for(var j = 0; j < groupData[i].versions.length; j++){
                            var version=grpCount.versions[j].version;
                            versionCount += grpCount.versions[j].Count;
                            children.push({
                                  name:grpCount.versions[j].version,
                                  cat: "cat"+j+((i+1)*100+j),
                                  val: grpCount.versions[j].Count,
                                  color: d3.rgb(color).darker(1/(j+1)),
                                  percentage:grpCount.versions[j].Count/allVersionCount*100,
                            });
                        }
                        data.push({
                            name:api_name,
                            cat: "cat"+i,
                            val: versionCount,
                            color: color,
                            children: children});
                    }

                    var div = d3.select("body").append("div").attr("class", "toolTip");
                    var width = 450,
                        height = 300,
                        margin = 80,
                        radius = Math.min(width - margin, height - margin) / 2,
                        // Pie layout will use the "val" property of each data object entry
                        pieChart = d3.layout.pie().sort(null).value(function(d){return d.val;}),
                        arc = d3.svg.arc().innerRadius(0).outerRadius(radius);

                        // SVG elements init
                        var svg = d3.select("#apiChart").append("svg").attr("class","pie").data([data]).attr("width", width).attr("height", height),
                            defs = svg.append("svg:defs"),
                            // Declare a main gradient with the dimensions for all gradient entries to refer
                            mainGrad = defs.append("svg:radialGradient")
                              //.attr("gradientUnits", "userSpaceOnUse")
                              .attr("cx", 0).attr("cy", 0).attr("r", radius).attr("fx", 0).attr("fy", 0)
                              .attr("id", "master"),

                            // The pie sectors container
                            arcGroup = svg.append("svg:g")
                              .attr("class", "arcGroup")
                              //.attr("filter", "url(#shadow)")
                              .attr("transform", "translate(" + (width / 2 ) + "," + (height / 2 +30) + ")"),
                            header = svg.append("text")
                              .attr("transform", "translate(30, 40)").attr("class", "header").style({'font-size':'16px','fill':'#555'}).text("");
                            // Declare shadow filter
                            var shadow = defs.append("filter").attr("id", "shadow")
                                          .attr("filterUnits", "userSpaceOnUse")
                                          .attr("x", -1*(width / 2)).attr("y", -1*(height / 2))
                                          .attr("width", width).attr("height", height);
                            shadow.append("feGaussianBlur")
                              .attr("in", "SourceAlpha")
                              .attr("stdDeviation", "2")
                              .attr("result", "blur");
                            shadow.append("feOffset")
                              .attr("in", "blur")
                              .attr("dx", "2").attr("dy", "2")
                              .attr("result", "offsetBlur");
                            shadow.append("feBlend")
                              .attr("in", "SourceGraphic")
                              .attr("in2", "offsetBlur")
                              .attr("mode", "normal");


                        function findChildenByCat(cat){
                              var breadcumb = "";
                              for(i=-1; i++ < data.length - 1; ){
                                if(data[i].cat == cat){
                                  breadcumb += cat;
                                  $(".header").text(' APIs → ' + data[i].name);
                                  return data[i].children;
                                }else{
                                }
                              }
                              d3.select(".header").text(breadcumb);
                              return data;
                        }

                        function drawPie(cat){
                              var currData = data;
                              if(cat != undefined){
                                currData = findChildenByCat(cat);
                              }

                              // Create a gradient for each entry (each entry identified by its unique category)
                              var gradients = defs.selectAll(".gradient").data(currData, function(d){return d.cat;});
                              gradients.enter().append("svg:radialGradient")
                                .attr("id", function(d, i) { return "gradient" + d.cat; })
                                .attr("class", "gradient")
                                .attr("xlink:href", "#master");

                              gradients.append("svg:stop").attr("offset", "0%").attr("stop-color", getColor );
                              gradients.append("svg:stop").attr("offset", "90%").attr("stop-color", getColor );
                              gradients.append("svg:stop").attr("offset", "100%").attr("stop-color", getDarkerColor );

                              // Create a sector for each entry in the enter selection
                              var paths = arcGroup.selectAll("path")
                                            .data(pieChart(currData), function(d) {return d.data.cat;} );

                              var pathsG = paths.enter().append("g")
                                                .attr("class", "slice")
                                                .attr("cursor","pointer")
                                                .append("path")
                                                .attr("class", "sector");

                              // Each sector will refer to its gradient fill
                              pathsG.attr("fill", function(d, i) { return "url(#gradient"+d.data.cat+")"; })
                                .transition().duration(1000).attrTween("d", tweenIn).each("end", function(){
                                  this._listenToEvents = true;
                                });

                              var tots = d3.sum(data, function(d) {
                                  return d.val;
                              });

                              // Collapse sectors for the exit selection
                              paths.exit().transition()
                                .duration(1000)
                                .attrTween("d", tweenOut).remove();

                              pathsG.on("click", function(d){
                              $(".header").text("");

                                if(d.data.name=="Other"){
                                     d.stopPropagation();
                                }else{

                                    arcGroup.selectAll('g.slice').remove();
                                    arcGroup.selectAll(".textVal").remove();

                                     if (this._listenToEvents){
                                      d3.select(this).attr("transform", "translate(0,0)");

                                      // Change level on click if no transition has started
                                      pathsG.each(function(){
                                        this._listenToEvents = false;
                                      });
                                      drawPie(d.data.cat);
                                    }
                                }
                              });

                              arcGroup.selectAll("g.slice path").on("mouseover", function(d){
                                     // Mouseover effect if no transition has started
                                    if(this._listenToEvents){
                                      // Calculate angle bisector
                                      var ang = d.startAngle + (d.endAngle - d.startAngle)/2;
                                      // Transformate to SVG space
                                      ang = (ang - (Math.PI / 2) ) * -1;

                                      // Calculate a 10% radius displacement
                                      var x = Math.cos(ang) * radius * 0.1;
                                      var y = Math.sin(ang) * radius * -0.1;

                                      d3.select(this).transition()
                                        .duration(250).attr("transform", "translate("+x+","+y+")");
                                    }

                                    var tots = d3.sum(data, function(d) {
                                        return d.val;
                                    });

                                    //show tooltip
                                    div.style("left", d3.event.pageX+10+"px");
                                    div.style("top", d3.event.pageY-25+"px");
                                    div.style("display", "inline-block");
                                    var children=d.data.children;
                                    if(children !== undefined){
                                       div.html(d.data.name+"<br>"+round((d.data.percentage = d.data.val  / tots*100),2)+"%");
                                    }else{
                                       var name =d.data.name;
                                       if(name.indexOf('v') === -1)
                                       {
                                           div.html("v"+d.data.name+"<br>"+round(d.data.percentage.toFixed(2),2)+"%");
                                       }
                                       else{
                                           div.html(d.data.name+"<br>"+round(d.data.percentage.toFixed(2),2)+"%");
                                       }
                                    }

                              }).on("mouseout", function(d){
                                  // Mouseout effect if no transition has started
                                  if(this._listenToEvents){
                                    d3.select(this).transition()
                                      .duration(150).attr("transform", "translate(0,0)");
                                  }
                                  div.style("display", "none");
                              });
                        }

                        function tweenOut(data) {
                          data.startAngle = data.endAngle = (2 * Math.PI);
                          var interpolation = d3.interpolate(this._current, data);
                          this._current = interpolation(0);
                          return function(t) {
                              return arc(interpolation(t));
                          };
                        }

                        // "Unfold" pie sectors by tweening its start/end angles
                        // from 0 into their final calculated values
                        function tweenIn(data) {
                          var interpolation = d3.interpolate({startAngle: 0, endAngle: 0}, data);
                          this._current = interpolation(0);
                          return function(t) {
                              return arc(interpolation(t));
                          };
                        }

                         // Helper function to extract color from data object
                        function getColor(data, index){
                          return data.color;
                        }

                        // Helper function to extract a darker version of the color
                        function getDarkerColor(data, index){
                          return d3.rgb(getColor(data, index)).darker();
                        }

                        drawPie();

                        $('#tableContainer').append($dataTable);
                        $('#tableContainer').show();

                        $('#apiTable').datatables_extended({
                            "order": [[ 1, "desc" ]],
                            "fnDrawCallback": function(){
                                 if(this.fnSettings().fnRecordsDisplay()<=$("#apiTable_length option:selected" ).val()
                                 || $("#apiTable_length option:selected" ).val()==-1)
                                     $('#apiTable_paginate').hide();
                                 else
                                     $('#apiTable_paginate').show();
                            },
                        });
                        //$('select').css('width','80px');

                } else {
                    $('#apiTable').hide();
                    $('#apiChart').css("fontSize", 14);
                    $('#noData').html($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="' + i18n.t("No Stats") + '"></i>' + i18n.t("No Data Available") + '</h4></div></div>'));
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
    drawProviderAPIUsage(from,to,apiFilter);
}

function round(value, decimals) {
    return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}