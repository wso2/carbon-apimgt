var currentLocation;
var chartColorScheme=[];
var colorRangeArray=[];
var statsEnabled = isDataPublishingEnabled();
var apiFilter = "allAPIs";

currentLocation = window.location.pathname;

$("#apiFilter").change(function (e) {
	apiFilter = this.value;
	drawAPIUsage();
});

var drawAPIUsage = function () {
    jagg.post("/site/blocks/stats/api-subscriptions/ajax/stats.jag", { action: "getSubscriberCountByAPIs", currentLocation: currentLocation, apiFilter: apiFilter },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                var length = json.usage.length, data = [];
                var newLength=0;
                var inputData=[];
                if (length > 0) {
                    $('#pie-chart').empty();

                     //grouping data according to name and version
                     var inputDataStr="";
                     var apiData="";
                     var apiName_Provider="";
                     var groupData = [];

                     for (var i = 0; i < length; i++) {

                         var apiData = json.usage[i].apiName;

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

                     //get all subscription count
                     var allSubscriptionCount = 0;
                     for (var i = 0; i < length; i++) {
                          allSubscriptionCount += json.usage[i].count;
                     }

                    var versionCount;
                    for (var i = 0; i < groupData.length; i++) {
                        var dataStructure=[];
                        dataStructure.push(groupData[i]);

                        var grpCount=groupData[i];
                        versionCount=0;
                        for(var j = 0; j < groupData[i].versions.length; j++){
                            versionCount += grpCount.versions[j].Count;
                        }

                        dataStructure[0].versions.push({ version: "other", Count:allSubscriptionCount-versionCount});
                        colorRangeArray.push(chartColorScheme);

                        //generating random colors for paths
                        var randomColor=getRandomColor();
                        var rgb=hexToRgb(randomColor);

                        var r=rgb.r;
                        var g=rgb.g;
                        var b=rgb.b;

                        //getting shades of the generated random color
                        if(r<210 && g<210 && b<210){
                             for(var j=0;j<=dataStructure[0].versions.length;j++){
                                 r-=10;
                                 g-=10;
                                 b-=10;
                                 chartColorScheme.push("rgb("+r+","+g+","+b+")");
                             }
                        }else{
                             for(var j=0;j<=dataStructure[0].versions.length;j++){
                                  r-=13;
                                  g-=13;
                                  b-=13;
                                  chartColorScheme.push("rgb("+r+","+g+","+b+")");
                             }
                        }

                        //adding color to fill the rest of the donut path
                        chartColorScheme.splice(dataStructure[0].versions.length-1, 0, "#eee");

                        var div = d3.select("body").append("div").attr("class", "toolTip");

                        var w = 250;
                        var h = 270;
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
                        return d.Count;
                        });

                        //D3 helper function to create colors from an ordinal scale
                        var color = d3.scale.ordinal()
                          .range(colorRangeArray[i]);

                        chartColorScheme=[];

                        //D3 helper function to draw arcs, populates parameter "d" in path object
                        var arc = d3.svg.arc()
                            .startAngle(function(d){ return d.startAngle; })
                            .endAngle(function(d){ return d.endAngle; })
                            .innerRadius(ir)
                            .outerRadius(r);

                        var versions;

                        // CREATE VIS & GROUPS

                        var vis = d3.select("#pie-chart").append("svg:svg")
                            .attr("width", w)
                            .attr("height", h-10);

                        vis.append("text").attr("class", "title_text")
                               .attr("x", 125)
                               .attr("y", 17)
                               .style("font-size", "14px").style("font-weight", "10px")
                               .style("font-family", "'Helvetica Neue',Helvetica,Arial,sans-serif")
                               .style("z-index", "19")
                               .style("text-anchor", "middle")
                               .style("color","gray")
                               .text(groupData[i].apiName_Provider.substring(0, 30));

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
                            data = dataStructure[number].versions;
                            oldPieData = filteredPieData;
                            pieData = donut(data);
                            var api_name=groupData[i].api_name;
                            var provider = groupData[i].provider;
                            var apiName_Provider = "" + api_name + "(" + provider + ")" ;
                            var sliceProportion = 0; //size of this slice
                            filteredPieData = pieData.filter(filterData);

                            function filterData(element, index, array) {
                                element.name = data[index].version;
                                element.value = data[index].Count;
                                sliceProportion += element.value;
                                return (element.value > 0);
                            }

                            //DRAW ARC PATHS
                            paths = arc_group.selectAll("path").data(filteredPieData);

                            paths.enter().append("svg:path")
                                .attr("stroke", "white")
                                .attr("stroke-width", 0.5)
                                .attr("fill", function(d, i) { return color(i); })
                                .attr("cursor", function(d) {
                                     if(d.name=="other"){
                                           return "default";
                                     }else{
                                           return "pointer";
                                     }
                                })
                                .attr("pointer-events", function(d) {
                                     if(d.name=="other"){
                                           return "none";
                                     }
                                })
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


                            paths.on("mousemove", function(d){

                                var percentage = (d.value/sliceProportion)*100;

                                //show tooltip
                                div.style("left", d3.event.pageX+10+"px");
                                div.style("top", d3.event.pageY-25+"px");
                                div.style("display", "inline-block");
                                div.html("Name : " + apiName_Provider + "<br>Version : " + (d.data.version) + "<br>Count : " +
                                (d.data.Count) + "<br>Percentage : " + percentage.toFixed(1) + "%");

                                if(d.data.version=="other"){
                                    div.style("display", "none");
                                }
                            });

                            paths.on("mouseout", function(d){
                                div.style("display", "none");
                            });

                            paths.on("click", function(d){
                            document.location.href=jagg.site.context+"/info?name="+api_name+"&version="+d.data.version+"&provider="+provider;
                            });


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
                                return d.name;
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

                    //paginator
                    var items = $("svg");
                    var numItems = items.length;
                    if (numItems<=10){
                        $('#pagination').hide();
                    }else{
                    var perPage = 10;
                    items.slice(perPage).hide();

                    $("#pagination").pagination({
                    items: numItems,
                    itemsOnPage: perPage,
                    cssStyle: "light-theme",
                    onPageClick: function(pageNumber) {
                        var showFrom = perPage * (pageNumber - 1);
                        var showTo = showFrom + perPage;

                        items.hide() // first hide everything, then show for the new page
                             .slice(showFrom, showTo).show();
                        }
                    });
                    }
                }else{
                    $('#pie-chart').html($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
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

drawAPIUsage();


function getRandomColor() {
  var letters = '0123456789ABCDEF'.split('');
  var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function hexToRgb(hex) {
var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
    } : null;
}


