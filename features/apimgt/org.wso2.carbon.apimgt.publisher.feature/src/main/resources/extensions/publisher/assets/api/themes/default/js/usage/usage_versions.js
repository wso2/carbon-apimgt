$(function () {
    var apiName = store.publisher.apiIdentifier.name;
    var version = store.publisher.apiIdentifier.version;
    var provider = store.publisher.apiIdentifier.provider;
    var usageurl = caramel.context + "/asts/api/apis/usage";
    $.post(usageurl, {
                  action: "getProviderAPIVersionUsage",
                  provider: provider,
                  apiName: apiName
              },
              function (responseData) {
                  data = responseData.data;
                  $('#apiUsageByVersionSpinner').hide();
                  if (!data.error) {
                      var length = data.usage.length, data = [];
                      $('#versionChart').empty();
                      $('#apiUsageByVersionNoData').empty();
                      $('#versionTable').find("tr:gt(0)").remove();
                      var versionChartData = [];
                      for (var i = 0; i < length; i++) {
                          data[i] = [data.usage[i].version, parseInt(data.usage[i].count)];
                          $('#versionTable').append($('<tr><td>' + data.usage[i].version + '</td><td>' + data.usage[i].count + '</td></tr>'));
                          versionChartData.push({"version": data.usage[i].version, "count": data.usage[i].count});
                      }

                      if (length > 0) {
                          $('#versionTable').show();
                          d3.select('#versionChart').append('svg').style('height', '400px');
                          drawVersionChart('versionChart', versionChartData);
                          $('#versionChart svg').show();
                      } else {
                          $('#versionTable').hide();
                          $('#versionChart').css("fontSize", 14);
                          $('#apiUsageByVersionNoData').html('');
                          $('#apiUsageByVersionNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                      }
                  } else {
                      BootstrapDialog.show({
                                               type: BootstrapDialog.TYPE_DANGER,
                                               title: 'Error',
                                               message:  data.message,
                                               buttons: [{
                                                             label: 'Ok',
                                                             action: function(dialogItself){
                                                                 dialogItself.close();
                                                             }
                                                         }]
                                           });
                  }
              });


    $.post(usageurl, {
                  action: "getSubscriberCountByAPIVersions",
                  provider: provider,
                  apiName: apiName
              },
              function (responseData) {
                  data = responseData.data;
                  $('#apiSubscriptionsByVersionSpinner').hide();
                  if (!data.error) {
                      var length = data.usage.length, data = [];
                      $('#versionUserChart').empty();
                      $('#apiSubscriptionsByVersionsNoData').empty();
                      $('#versionUserTable').find("tr:gt(0)").remove();
                      var versionUserChartData = [];
                      for (var i = 0; i < length; i++) {
                          data[i] = [data.usage[i].apiVersion, parseInt(data.usage[i].count)];
                          $('#versionUserTable').append($('<tr><td>' + data.usage[i].apiVersion + '</td><td>' + data.usage[i].count + '</td></tr>'));
                          versionUserChartData.push({"version": data.usage[i].apiVersion, "count": data.usage[i].count});
                      }

                      if (length > 0) {
                          $('#versionUserTable').show();
                          d3.select('#versionUserChart').append('svg').style('height', '400px');
                          drawVersionChart('versionUserChart', versionUserChartData);
                          $('#versionUserChart svg').show();

                      } else {
                          $('#versionUserTable').hide();
                          $('#versionUserChart').css("fontSize", 14);
                          $('#apiSubscriptionsByVersionsNoData').html('');
                          $('#apiSubscriptionsByVersionsNoData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

                      }

                  } else {
                      BootstrapDialog.show({
                                               type: BootstrapDialog.TYPE_DANGER,
                                               title: 'Error',
                                               message:  data.message,
                                               buttons: [{
                                                             label: 'Ok',
                                                             action: function(dialogItself){
                                                                 dialogItself.close();
                                                             }
                                                         }]
                                           });
                  }
              });


    Object.size = function (obj) {
        var size = 0, key;
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                size++;
            }
        }
        return size;
    };

    function drawUserChart(chartID, data) {
        var h = 600;
        var r = h / 2;
        var arc = d3.svg.arc().outerRadius(r);

        nv.addGraph(function () {
            var chart = nv.models.pieChart()
                    .x(function (d) {
                           return d.user
                       })
                    .y(function (d) {
                           return d.count
                       })
                    .showLabels(true)
                    .labelType("percent")
                    .showLegend(false)
                    .color(d3.scale.category20().range())
                    .tooltipContent(function (key, x, y) {
                                        return '<b>' + key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
                                    });

            d3.select('#' + chartID + ' svg')
                    .datum(data)
                    .transition().duration(350)
                    .call(chart);
            d3.selectAll(".nv-label text")
                    .attr("transform", function (d) {
                              d.innerRadius = -450;
                              d.outerRadius = r;
                              return "translate(" + arc.centroid(d) + ")";
                          }
            )
                    .attr("text-anchor", "middle")
                    .style({"font-size": "0.7em"});

            nv.utils.windowResize(chart.update);
            return chart;
        });
    };

    function drawVersionChart(chartID, data) {
        var h = 600;
        var r = h / 2;
        var arc = d3.svg.arc().outerRadius(r);

        nv.addGraph(function () {
            var chart = nv.models.pieChart()
                    .x(function (d) {
                           return d.version
                       })
                    .y(function (d) {
                           return d.count
                       })
                    .showLabels(true)
                    .labelType("percent")
                    .showLegend(false)
                    .color(d3.scale.category20().range())
                    .tooltipContent(function (key, x, y) {
                                        if (chartID == "versionUserChart") {
                                            return '<b>' + key + '</b> - ' + Math.round(x) + " <i>subscription(s)</i>"
                                        } else {
                                            return '<b>' + key + '</b> - ' + Math.round(x) + " <i>call(s)</i>"
                                        }
                                    });

            d3.select('#' + chartID + ' svg')
                    .datum(data)
                    .transition().duration(350)
                    .call(chart);
            d3.selectAll(".nv-label text")
                    .attr("transform", function (d) {
                              d.innerRadius = -450;
                              d.outerRadius = r;
                              return "translate(" + arc.centroid(d) + ")";
                          }
            )
                    .attr("text-anchor", "middle")
                    .style({"font-size": "0.7em"});

            nv.utils.windowResize(chart.update);
            return chart;
        });
    };
});

