var getLastAccessTime = function(name) {
    var lastAccessTime = null;
    var provider = $("#spanProvider").text();
    jagg.syncPost("/site/blocks/stats/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",provider:provider,mode:'browse' },
                  function (json) {
                      if (!json) {
                        return 0;
                      }
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
                      if (!json) {
                        return 0;
                      }
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

    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var currentTab = $(e.target).attr("title"); // get current tab
        if (currentTab == 'Lifecycle') {
            loadLC();
        } else if (currentTab == 'Versions') {
            loadVersionUsageChart();
        } else if (currentTab == 'Users') {
            loadSubscriptionsChart();
        }
    });


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
            $.cookie("selectedTab", clickedTab, {path: "/"});
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
                                  $('#apiUsageByVersionNoData').append($('<div class="message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div>'));
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
                                  $('#apiSubscriptionsByVersionsNoData').append($('<div class="message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div>'));

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
                                  $('#usageByCurrentSubscribersAcrossAllNoData').append($('<div class="message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div>'));
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
                                  $('#usageByCurrentSubscribersNoData').append($('<div class="message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div>'));
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

var loadVersionUsageChart = function () {
    jagg.post("/site/blocks/usage/ajax/usage.jag", {action: "getProviderAPIVersionUsage"},
        function (json) {
            if (!json.error) {
                pushDataForTabs("versions");
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");
};

var loadSubscriptionsChart = function () {
    jagg.post("/site/blocks/usage/ajax/usage.jag", {action: "getProviderAPIUserUsage"},
        function (json) {
            if (!json.error) {
                pushDataForTabs("users");
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");
};

var loadLC = function () {

    jagg.post("/site/blocks/life-cycles/ajax/life-cycles.jag", { action: "getAPILC" },
        function (json) {
            if (!json.error) {
                var statesList = json.definition.configuration.lifecycle.scxml.state;
                var states = [];

// Create a new directed graph
                var g = new dagreD3.graphlib.Graph().setGraph({});

                for (var key in statesList) {
// States and transitions from RFC 793
                    states.push(key.toUpperCase());
                }

// Automatically label each of the nodes
                states.forEach(function (state) {
                    g.setNode(state, {
                        label: state.toUpperCase(),
                        shape: 'rect',
                        labelStyle: 'font-size: 12px;font-weight: lighter;fill: rgb(51, 51, 51);'
                    });
                });


// Set up the edges
                g.setEdge("CREATED", "PUBLISHED", { label: "PUBLISH", labelStyle: "fill: white", lineInterpolate: 'cardinal' });
                for (var key in statesList) {
                    var transition = statesList[key].transition;
                    if (transition != null) {
                        for (var i = 0; i < transition.length; i++) {
                            var obj = transition[i];
                            var event = obj.event;
                            var target = obj.target;
                            // Set up the edges
                            var eventLabel = event.toUpperCase().replace(" ", "\n");
                            eventLabel = eventLabel.trim().replace(/(\S(.{0,78}\S)?)\s+/g, '$1\n');
                            g.setEdge(key.toUpperCase(), target.toUpperCase(), { label: eventLabel, labelStyle: "fill: white", lineInterpolate: 'cardinal' });
                        }
                    }
                }
                g.nodes().forEach(function (v) {
                    var node = g.node(v);
                    node.rx = node.ry = 5;
                });

                var status = $('#status').text().trim();

// Add some custom colors based on state
                g.node(status).style = "fill: #5D76E4";


                var svg = d3.select("svg"),
                    inner = svg.select("g");

// Set up zoom support
                var zoom = d3.behavior.zoom();
                zoom.on("zoom", function () {
                    inner.attr("transform", "translate(" + d3.event.translate + ")" +
                        "scale(" + d3.event.scale + ")");
                });
                zoom.scaleExtent([0.3, 2]);
                svg.call(zoom);

// Create the renderer
                var render = new dagreD3.render();

// Run the renderer. This is what draws the final graph.
                render(inner, g);
                var initialScale = 1;
                var initialX = (svg.attr('width') - g.graph().width * initialScale) / 2;
                var initialY = 10;
// Center the graph
                inner.attr("transform", "translate(" + [initialX, initialY] + ") scale(" + initialScale + ")");
                zoom.translate([initialX, initialY]).scale(initialScale).event(svg);
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
        }, "json");


};


