$(function () {

    $(document).ready(function () {
        $('#subscribers').dataTable();
    });

    var apiName = store.publisher.apiIdentifier.name;
    var version = store.publisher.apiIdentifier.version;
    var provider = store.publisher.apiIdentifier.provider;
    var usageurl = caramel.context + "/asts/api/apis/usage";
    $.post(usageurl, {
               action: "getProviderAPIVersionUsage",
               providerName: provider,
               apiName: apiName
           },
           function (responseData) {
               content = responseData.data;
               $('#usageByCurrentSubscribersAcrossAllSpinner').hide();
               if (!content.error) {
                   var length = content.usage.length,data = [];
                   $('#userChart').empty();
                   $('#usageByCurrentSubscribersAcrossAllNoData').empty();
                   $('#userTable').find("tr:gt(0)").remove();
                   var chartData =[];
                   for (var i = 0; i < length; i++) {
                       data[i] = [content.usage[i].user, parseInt(content.usage[i].count)];
                       $('#userTable').append($('<tr><td>' + content.usage[i].user + '</td><td>' + content.usage[i].count + '</td></tr>'));
                       chartData.push({"user":content.usage[i].user,"count":content.usage[i].count});
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
               action: "getProviderAPIVersionUserUsage",
               providerName: provider,
               apiName: apiName,
               apiVersion : version
           },
           function (responseData) {
               content = responseData.data;
               $('#usageByCurrentSubscribersSpinner').hide();
               if (!content.error) {
                   var length = content.usage.length,data = [];
                   $('#userVersionChart').empty();
                   $('#usageByCurrentSubscribersNoData').empty();
                   $('#userVersionTable').find("tr:gt(0)").remove();
                   var userVersionChartData = [];
                   for (var i = 0; i < length; i++) {
                       data[i] = [content.usage[i].user, parseInt(content.usage[i].count)];
                       $('#userVersionTable').append($('<tr><td>' + content.usage[i].user + '</td><td>' + content.usage[i].count + '</td></tr>'));
                       userVersionChartData.push({"user":content.usage[i].user,"count":content.usage[i].count});
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
});

