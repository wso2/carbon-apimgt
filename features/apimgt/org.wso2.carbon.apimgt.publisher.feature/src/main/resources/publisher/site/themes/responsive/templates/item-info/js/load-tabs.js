var getLastAccessTime = function(name) {
    var lastAccessTime = null;
    var provider = $("#spanProvider").text();
    jagg.syncPost("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",provider:provider,mode:'browse' },
                  function (json) {
                      if (!json.error) {
                          var length = json.usage.length;
                          for (var i = 0; i < length; i++) {
                              if (json.usage[i].api_name == name) {
                                  lastAccessTime = json.usage[i].lastAccess + " (Accessed version: " + json.usage[i].api_version + ")";
                                  break;
                              }
                          }
                      } else {
                          if (json.message == "AuthenticateError") {
                              jagg.showLogin();
                          } else {
                              jagg.message({content:json.message,type:"error"});
                          }
                      }
                  });
    return lastAccessTime;
};

var getResponseTime = function(name) {
    var responseTime = null;
    var provider = $("#spanProvider").text();
    jagg.syncPost("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIServiceTime",provider:provider,mode:'browse'},
                  function (json) {
                      if (!json.error) {
                          var length = json.usage.length;
                          for (var i = 0; i < length; i++) {
                              if (json.usage[i].apiName == name) {
                                  responseTime = json.usage[i].serviceTime + " ms";
                                  break;
                              }
                          }
                      } else {
                          if (json.message == "AuthenticateError") {
                              jagg.showLogin();
                          } else {
                              jagg.message({content:json.message,type:"error"});
                          }
                      }
                  });
    return responseTime;
};


$(document).ready(function() {

    // Converting dates from timestamp to date string
    jagg.printDate();

    if (($.cookie("selectedTab") != null)) {
        var tabLink = $.cookie("selectedTab");
        $('#' + tabLink + "Link").tab('show');
        //$.cookie("selectedTab", null);
        pushDataForTabs(tabLink);
    }

    $('a[data-toggle="tab"]').on('click', function (e) {
        jagg.sessionAwareJS({callback:function(){
            var clickedTab = e.target.href.split('#')[1];
            ////////////// edit tab
            pushDataForTabs(clickedTab);
            $.cookie("selectedTab", clickedTab,  {path: "/"});
        }});

    });
    
});

function pushDataForTabs(clickedTab){
     if (clickedTab == "versions") {

            var apiName = $("#infoAPIName").val();
            var version = $("#infoAPIVersion").val();
            var provider = $("#spanProvider").text();
            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIVersionUsage", provider:provider,apiName:apiName },
                      function (json) {
                          $('#apiUsageByVersionSpinner').hide();
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#versionChart').empty();
                              $('#apiUsageByVersionNoData').empty();
                              $('#versionTable').find("tr:gt(0)").remove();
                              var versionChartData = [];
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].version, parseInt(json.usage[i].count)];
                                  $('#versionTable').append($('<tr><td>' + json.usage[i].version + '</td><td>' + json.usage[i].count + '</td></tr>'));
                                  versionChartData.push({"version":json.usage[i].version,"count":json.usage[i].count});
                              }

                              if (length > 0) {
                                  $('#versionTable').show();
                                   d3.select('#versionChart').append('svg').style('height','400px');
                                   drawVersionChart('versionChart',versionChartData);
                                   $('#versionChart svg').show();
                              } else {
                                  $('#versionTable').hide();
                                  $('#versionChart').css("fontSize", 14);
                                  $('#apiUsageByVersionNoData').html('');
                                  $('#apiUsageByVersionNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                              }
                          } else {
                              if (json.message == "AuthenticateError") {
                                  jagg.showLogin();
                              } else {
                                  jagg.message({content:json.message,type:"error"});
                              }
                          }
                      }, "json");


            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getSubscriberCountByAPIVersions", provider:provider,apiName:apiName },
                      function (json) {
                          $('#apiSubscriptionsByVersionSpinner').hide();
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#versionUserChart').empty();
                              $('#apiSubscriptionsByVersionsNoData').empty();
                              $('#versionUserTable').find("tr:gt(0)").remove();
                              var versionUserChartData =[];
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].apiVersion, parseInt(json.usage[i].count)];
                                  $('#versionUserTable').append($('<tr><td>' + json.usage[i].apiVersion + '</td><td>' + json.usage[i].count + '</td></tr>'));
                                  versionUserChartData.push({"version":json.usage[i].apiVersion,"count":json.usage[i].count});
                              }

                              if (length > 0) {
                                  $('#versionUserTable').show();
                                  d3.select('#versionUserChart').append('svg').style('height','400px');
                                  drawVersionChart('versionUserChart',versionUserChartData);
                                  $('#versionUserChart svg').show();

                              } else {
                                  $('#versionUserTable').hide();
                                  $('#versionUserChart').css("fontSize", 14);
                                  $('#apiSubscriptionsByVersionsNoData').html('');
                                  $('#apiSubscriptionsByVersionsNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

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

        if (clickedTab == "users") {

            var name = $("#infoAPIName").val();
            var version = $("#infoAPIVersion").val();
            var provider = $("#spanProvider").text();

            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIUserUsage", provider:provider,apiName:name },
                      function (json) {
                          $('#usageByCurrentSubscribersAcrossAllSpinner').hide();
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#userChart').empty();
                              $('#usageByCurrentSubscribersAcrossAllNoData').empty();
                              $('#userTable').find("tr:gt(0)").remove();
                              var chartData =[];
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].user, parseInt(json.usage[i].count)];
                                  $('#userTable').append($('<tr><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));
                                  chartData.push({"user":json.usage[i].user,"count":json.usage[i].count});
                              }

                              if (length > 0) {
                                  $('#userTable').show();
                                  d3.select('#userChart').append('svg').style('height','400px');
                                  drawUserChart('userChart',chartData);
                                  $('#userChart svg').show();
                              } else {
                                  $('#userTable').hide();
                                  $('#userChart').hide();
                                  $('#userChart').css("fontSize", 14);
                                  $('#usageByCurrentSubscribersAcrossAllNoData').html('');
                                  $('#usageByCurrentSubscribersAcrossAllNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                              }

                          } else {
                              if (json.message == "AuthenticateError") {
                                  jagg.showLogin();
                              } else {
                                  jagg.message({content:json.message,type:"error"});
                              }
                          }
                      }, "json");

            jagg.post("/site/blocks/usage/ajax/usage.jag", { action:"getProviderAPIVersionUserUsage", provider:provider,apiName:name,version:version, server:"https://localhost:9444/" },
                      function (json) {
                          $('#usageByCurrentSubscribersSpinner').hide();
                          if (!json.error) {
                              var length = json.usage.length,data = [];
                              $('#userVersionChart').empty();
                              $('#usageByCurrentSubscribersNoData').empty();
                              $('#userVersionTable').find("tr:gt(0)").remove();
                              var userVersionChartData = [];
                              for (var i = 0; i < length; i++) {
                                  data[i] = [json.usage[i].user, parseInt(json.usage[i].count)];
                                  $('#userVersionTable').append($('<tr><td>' + json.usage[i].user + '</td><td>' + json.usage[i].count + '</td></tr>'));
                                  userVersionChartData.push({"user":json.usage[i].user,"count":json.usage[i].count});
                              }

                              if (length > 0) {
                                  $('#userVersionTable').show();
                                  d3.select('#userVersionChart').append('svg').style('height','400px');
                                  drawUserChart('userVersionChart',userVersionChartData);
                                  $('#userVersionChart svg').show();
                              } else {
                                  $('#userVersionTable').hide();
                                  $('#userVersionChart').css("fontSize", 14);
                                  $('#usageByCurrentSubscribersNoData').html('');
                                  $('#usageByCurrentSubscribersNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                              }

                          } else {
                              if (json.message == "AuthenticateError") {
                                  jagg.showLogin();
                              } else {
                                  jagg.message({content:json.message,type:"error"});
                              }
                          }
                      }, "json");

            var responseTime = getResponseTime(name);
            var lastAccessTime = getLastAccessTime(name);

            if (responseTime != null && lastAccessTime != null) {
                $("#usageSummary").show();
                var doc = document;
                var tabBody = doc.getElementById("usageTable");

                var row1 = doc.createElement("tr");
                var cell1 = doc.createElement("td");
                cell1.setAttribute("class", "span4");
                cell1.innerHTML = i18n.t('titles.responseTimeGraph');
                var cell2 = doc.createElement("td");
                cell2.innerHTML = responseTime != null ? responseTime : i18n.t('errorMsgs.unavailableData');
                row1.appendChild(cell1);
                row1.appendChild(cell2);

                var row2 = doc.createElement("tr");
                var cell3 = doc.createElement("td");
                cell3.setAttribute("class", "span4");
                cell3.innerHTML = i18n.t('titles.lastAccessTimeGraph');
                var cell4 = doc.createElement("td");
                cell4.innerHTML = lastAccessTime != null ? lastAccessTime : i18n.t('errorMsgs.unavailableData');
                row2.appendChild(cell3);
                row2.appendChild(cell4);

                tabBody.appendChild(row1);
                tabBody.appendChild(row2);

            }

        }
}

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) {
            size++;
        }
    }
    return size;
};

function drawUserChart(chartID,data) {
    var h = 600;
    var r = h/2;
    var arc = d3.svg.arc().outerRadius(r);

    nv.addGraph(function() {
    var chart = nv.models.pieChart()
      .x(function(d) { return d.user })
      .y(function(d) { return d.count })
      .showLabels(true)
      .labelType("percent")
      .showLegend(false)
      .color(d3.scale.category20().range())
      .tooltipContent( function(key, x, y){
               return  '<b>'+key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
             } );

    d3.select('#'+chartID+ ' svg')
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
};

function drawVersionChart(chartID,data) {
    var h = 600;
    var r = h/2;
    var arc = d3.svg.arc().outerRadius(r);

    nv.addGraph(function() {
    var chart = nv.models.pieChart()
      .x(function(d) { return d.version })
      .y(function(d) { return d.count })
      .showLabels(true)
      .labelType("percent")
      .showLegend(false)
      .color(d3.scale.category20().range())
      .tooltipContent( function(key, x, y){
        if(chartID=="versionUserChart"){
            return  '<b>'+key + '</b> - ' + Math.round(x) + " <i>subscription(s)</i>"
        }else{
            return  '<b>'+key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
        }
       });

    d3.select('#'+chartID+ ' svg')
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
};


