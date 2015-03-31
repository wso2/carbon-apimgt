var t_on = {
    'apiChart': 1,
    'subsChart': 1,
    'serviceTimeChart': 1,
    'tempLoadingSpace': 1
};
var currentLocation;

var chartColorScheme1 = ["#3da0ea", "#bacf0b", "#e7912a", "#4ec9ce", "#f377ab", "#ec7337", "#bacf0b", "#f377ab", "#3da0ea", "#e7912a", "#bacf0b"];
//fault colors || shades of red
var chartColorScheme2 = ["#ED2939", "#E0115F", "#E62020", "#F2003C", "#ED1C24", "#CE2029", "#B31B1B", "#990000", "#800000", "#B22222", "#DA2C43"];
//fault colors || shades of blue
var chartColorScheme3 = ["#0099CC", "#436EEE", "#82CFFD", "#33A1C9", "#8DB6CD", "#60AFFE", "#7AA9DD", "#104E8B", "#7EB6FF", "#4981CE", "#2E37FE"];
currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

require(["dojo/dom", "dojo/domReady!"], function (dom) {
    currentLocation = window.location.pathname;
    //Initiating the fake progress bar
    jagg.fillProgress('apiChart');
    jagg.fillProgress('subsChart');
    jagg.fillProgress('serviceTimeChart');
    jagg.fillProgress('tempLoadingSpace');

    jagg.post("/site/blocks/stats/api-usage-user/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());


                    //day picker
                    $('#today-btn').on('click',function(){
                        var to = convertTimeString(currentDay);
                        var from = convertTimeString(currentDay-86400000);
                        var dateStr= from+" to "+to;
                        $("#date-range").html(dateStr);
                        $('#date-range').data('dateRangePicker').setDateRange(from,to);

                        drawAPIUsage(from,to);

                    });

                    //hour picker
                    $('#hour-btn').on('click',function(){
                        var to = convertTimeString(currentDay);
                        var from = convertTimeString(currentDay-3600000);
                        var dateStr= from+" to "+to;
                        $("#date-range").html(dateStr);
                        $('#date-range').data('dateRangePicker').setDateRange(from,to);
                        drawAPIUsage(from,to);
                    })

                    //week picker
                    $('#week-btn').on('click',function(){
                        var to = convertTimeString(currentDay);
                        var from = convertTimeString(currentDay-604800000);
                        var dateStr= from+" to "+to;
                        $("#date-range").html(dateStr);
                        $('#date-range').data('dateRangePicker').setDateRange(from,to);
                        drawAPIUsage(from,to);
                    })

                    //month picker
                    $('#month-btn').on('click',function(){

                        var to = convertTimeString(currentDay);
                        var from = convertTimeString(currentDay-(604800000*4));
                        var dateStr= from+" to "+to;
                        $("#date-range").html(dateStr);
                        $('#date-range').data('dateRangePicker').setDateRange(from,to);
                        drawAPIUsage(from,to);
                    });

                    //date picker
                    $('#date-range').dateRangePicker(
                        {
                            startOfWeek: 'monday',
                            separator : ' to ',
                            format: 'YYYY-MM-DD HH:mm',
                            autoClose: false,
                            time: {
                                enabled: true
                            },
                            shortcuts:'hide',
                            endDate:currentDay
                        })
                        .bind('datepicker-apply',function(event,obj)
                        {
                             btnActiveToggle(this);
                             var from = convertDate(obj.date1);
                             var to = convertDate(obj.date2);
                             $('#date-range').html(from + " to "+ to);
                             drawAPIUsage(from,to);
                        });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    $('#date-range').data('dateRangePicker').setDateRange(from,to);
                    $('#date-range').html($('#date-range').val());
                    var fromStr = convertDate(from);
                    var toStr = convertDate(to);
                    drawAPIUsage(fromStr,toStr);


                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                    var width = $("#rangeSliderWrapper").width();
                    //$("#rangeSliderWrapper").affix();
                    $("#rangeSliderWrapper").width(width);

                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><img src="../themes/default/templates/stats/images/statsEnabledThumb.png" alt="Stats Enabled"></div>'));
                }

                else {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><span class="label top-level-warning"><i class="icon-warning-sign icon-white"></i>'
                        + i18n.t('errorMsgs.checkBAMConnectivity') + '</span><br/><img src="../themes/default/templates/stats/api-usage-user/images/statsThumb.png" alt="Smiley face"></div>'));
                }
            }
            else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
            t_on['apiChart'] = 0;
        }, "json");

});

var subscriberDetails=[];
var drawAPIUsage = function (from,to) {

    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-subscriptions/ajax/stats.jag", { action: "getSubscriberCountByAPIs", currentLocation: currentLocation  },
                function (json) {
                    if (!json.error) {
                        var length = json.usage.length, data = [];
                        var newLength=0;
                        subscriberDetails=[];
                        var inputData=[];

                        if (length > 0) {
                            $('#pie-chart').empty();

                        //grouping data(subscriber count) according to name and version
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

                             var versionCount;
                             for (var i = 0; i < groupData.length; i++) {

                                 var grpCount=groupData[i];
                                 versionCount=0;
                                 var name;
                                 for(var j = 0; j < groupData[i].versions.length; j++){
                                     versionCount += grpCount.versions[j].Count;
                                 }
                                 subscriberDetails.push({
                                  "api_name":groupData[i].api_name,
                                  "sub_count":versionCount,
                                  "check":false
                                  });
                                 }
                                 drawChart(from,to);
                        } else {
				//No subscriber details available.
				$('#apiUsageByUserTable').hide();
                    		$('#tempLoadingSpaceUsageByUser').html('');
                    		$('#tempLoadingSpaceUsageByUser').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
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


var drawChart = function (from, to) {
    var fromDate = from;
    var toDate = to;

    jagg.post("/site/blocks/stats/api-usage-user/ajax/stats.jag", { action: "getAPIUsageByUser", currentLocation: currentLocation, fromDate: fromDate, toDate: toDate},
        function (json) {
            if (!json.error) {
                $('#tooltipTable').find("tr:gt(0)").remove();
                var length = json.usage.length;

                $('#tempLoadingSpaceUsageByUser').empty();
                $('#chartContainer').empty();
                $('div#apiSelectTable_wrapper.dataTables_wrapper.no-footer').remove();

                if (length == 0){
                    $('#apiUsageByUserTable').hide();
                    $('#tempLoadingSpaceUsageByUser').html('');
                    $('#tempLoadingSpaceUsageByUser').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

                } else {
                    var inputData=[];
                    for (var i = 0; i < length; i++) {
                        inputData.push({
                            "api_name":json.usage[i].apiName,
                            "version":json.usage[i].version,
                            "user":json.usage[i].userId,
                            "count": json.usage[i].count
                        } );
                    }

                    dataUsage = JSON.parse(JSON.stringify(inputData));

                    function orderByCountAscending(a, b) {
                        return b.count - a.count;
                    }

                    dataUsage = dataUsage.sort(orderByCountAscending);
                    var webapps = [];

                    for(x=0;x<dataUsage.length;x++){

                         var webappIndex = -1;
                         var webappVersionIndex = -1;

                         for(y=0;y<webapps.length;y++){
                             if(webapps[y][0] == dataUsage[x].api_name){
                                 webappIndex = y;
                                 var z;
                                 for(z=0;z<webapps[y][1].length;z++){
                                     if(webapps[y][1][z][0] == dataUsage[x].version){
                                         webappVersionIndex = z;
                                         break;
                                     }
                                 }
                                 if((webappVersionIndex == -1) && (z == webapps[y].length)){
                                     break;
                                 }
                             }
                         }

                         if(webappIndex == -1){
                             var version = [];
                             var requestCount = [];
                             requestCount.push([dataUsage[x].user,dataUsage[x].count.toString()]);
                             version.push([dataUsage[x].version,requestCount]);
                             webapps.push([dataUsage[x].api_name,version]);
                         }else{
                             if(webappVersionIndex == -1){
                                 var requestCount = [];
                                 requestCount.push([dataUsage[x].user,dataUsage[x].count.toString()]);
                                 webapps[webappIndex][1].push([dataUsage[x].version,requestCount]);
                             }
                             else{
                                 webapps[webappIndex][1][webappVersionIndex][1].push([dataUsage[x].user,dataUsage[x].count.toString()]);
                             }
                         }
                    }

                    if (dataUsage == null) {
                         obj = {
                             error:true
                         };
                    } else {
                         obj = {
                             error:false,
                             webapps:webapps
                         };
                    }

                    var parsedResponse = JSON.parse(JSON.stringify(webapps));
                    var data=[];
                        for ( var i = 0; i < parsedResponse.length; i++) {
                        var count = 0;
                        var app =(parsedResponse[i][0].replace(/\s+/g, ''));
                        var maximumUsers = 0;
                        var allSubCount =0;
                        var allcount = 0;
                            for ( var j = 0; j < parsedResponse[i][1].length; j++) {

                        allSubCount = allSubCount+parsedResponse[i][1][j][1].length

                        maximumUsers=parsedResponse[i][1][j][1].length;

                            for ( var k = 0; k < maximumUsers; k++) {
                                count++;
                                allcount = Number(allcount)+Number(parsedResponse[i][1][j][1][k][1]);

                            }

                            }

                            for(var z =0;z<subscriberDetails.length;z++){
                                if(app == subscriberDetails[z].api_name){
                                    allSubCount = subscriberDetails[z].sub_count;
                                    subscriberDetails[z].check=true;
                                    data.push({
                                        API_name:app,
                                        Subscriber_Count:allSubCount,
                                        Hits:allcount,
                                        API:app
                                    });
                                }
                            }

                            userParsedResponse = parsedResponse;
                        }
                        for(var z =0;z<subscriberDetails.length;z++){
                            if(subscriberDetails[z].check == false){
                                data.push({
                                            API_name:subscriberDetails[z].api_name,
                                            Subscriber_Count:subscriberDetails[z].sub_count,
                                            Hits:0,
                                            API:subscriberDetails[z].api_name
                                });
                            }
                        }

                    var chart;
                    var svg = dimple.newSvg("#chartContainer", "100%", 600);


                    chart = new dimple.chart(svg, data);
                    chart.setMargins("60px", "30px", "110px", "70px");
                    chart.setBounds("10%", "10%", "75%", "60%");
                    var x= chart.addCategoryAxis("x", "API");
                    var y=chart.addMeasureAxis("y", "Subscriber_Count");
                    y.title = "Subscriber Count";
                    y.tickFormat = '1d';
                    chart.addMeasureAxis("z", "Hits");
                    s=chart.addSeries("API", dimple.plot.bubble);

                    var div = d3.select("body").append("div").attr("class", "toolTip");
                    var filterValues = dimple.getUniqueValues(data, "API");
                    var state_array = [];
                    var defaultFilterValues=[];
                    var sortData=[];
                    var chartData=[];

                    var $dataTable =$('<table class="display defaultTable" width="100%" cellspacing="0" id="apiSelectTable"></table>');

                    $dataTable.append($('<thead class="tableHead"><tr>'+
                                            '<th width="10%"></th>'+
                                            '<th>API</th>'+
                                            '<th style="text-align:right" width="30%" >Subscriber Count</th>'+
                                        '</tr></thead>'));

                    sortData = dimple.filterData(data, "API", filterValues);
                    sortData.sort(function(obj1, obj2) {
                        return obj2.Hits - obj1.Hits;
                    });

                    //default display of 20 checked entries on table
                    for(var n=0;n<sortData.length;n++){
                        if(n<20){
                            $dataTable.append($('<tr><td >'
                                                +'<input name="item_checkbox'+n+'"  checked   id='+n+'  type="checkbox"  data-item='+sortData[n].API_name +' class="inputCheckbox"/>'
                                                +'</td>'
                                                +'<td style="text-align:left;"><label for='+n+'>'+sortData[n].API_name +'</label></td>'
                                                +'<td style="text-align:right;"><label for='+n+'>'+sortData[n].Subscriber_Count +'</label></td></tr>'));
                            state_array.push(true);
                            defaultFilterValues.push(sortData[n].API_name);
                            chartData.push(sortData[n].API_name);
                        }else{
                            $dataTable.append($('<tr><td >'
                                                +'<input name="item_checkbox'+n+'"  id='+n+'  type="checkbox"  data-item='+sortData[n].API_name +' class="inputCheckbox"/>'
                                                +'</td>'
                                                +'<td style="text-align:left;"><label for='+n+'>'+sortData[n].API_name +'</label></td>'
                                                +'<td style="text-align:right;"><label for='+n+'>'+sortData[n].Subscriber_Count +'</label></td></tr>'));
                            state_array.push(false);
                            chartData.push(sortData[n].API_name);
                        }
                    }

                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();

                    $('#apiSelectTable').DataTable({
                        "order": [[ 2, "desc" ]],
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
                    $('select').css('width','60px');
                    chart.data = dimple.filterData(data, "API", defaultFilterValues);

                    var count=20;

                    //on checkbox check and uncheck event
                    $('#apiSelectTable').on( 'change', 'input.inputCheckbox', function () {
                          var id =  $(this).attr('id');
                          var check=$(this).is(':checked');
                          var draw_chart=[];

                          if (check) {
                          $('#displayMsg').html('');
                          count++;
                            //limiting to show 20 entries at a time
                            if(count>20){
                                $('#displayMsg').html('<h5 style="color:#555" >Please Note that the graph will be showing only 20 entries</h5>');
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

                          $.each(chartData, function (index, value) {
                                if (state_array[index]){
                                    draw_chart.push(value);
                                }
                          });

                          chart.data = dimple.filterData(data, "API", draw_chart);
                          chart.draw();
                    });

                    s.afterDraw = function (shp, d) {
                        var shape = d3.select(shp);

                        var circle=d3.select("#"+d.aggField+"_"+d.aggField+"__");

                            circle.on("click", function(d){
                            //circle on click
                            for ( var i = 0; i < parsedResponse.length; i++) {
                                var count = 0;
                                var app ='';

                                if(d.aggField == parsedResponse[i][0].replace(/\s+/g, '')){
                                    var versionCount=[];
                                    for ( var j = 0; j < parsedResponse[i][1].length; j++) {
                                          app =(parsedResponse[i][0]);

                                          var maximumUsers = parsedResponse[i][1][j][1].length;
                                          maxrowspan = parsedResponse[i][1][j][1].length;

                                          allcount = 0;
                                          for ( var k = 0; k < maximumUsers; k++) {
                                            count++;
                                            allcount = Number(allcount)+Number(parsedResponse[i][1][j][1][k][1]);
                                          }

                                        versionCount.push({version:parsedResponse[i][1][j][0],count:allcount});
                                    }

                                div.style("left", d3.event.pageX+10+"px");
                                div.style("top", d3.event.pageY-25+"px");
                                div.style("display", "inline-block");


                                div.html('<table class="table graphTable" id="tooltipTable"><thead><tr><th>version</th><th>Hits</th></tr></thead><tbody></tbody></table>');
                                    for (var l=0;l<versionCount.length;l++){
                                        var versionName=versionCount[l].version;
                                        var version_Count=versionCount[l].count;
                                        $('#tooltipTable tbody').append('<tr><td>'+versionName+'</td><td>'+version_Count+'</td></tr>');
                                    }
                                }
                            }
                        });

                        circle.on("mouseout", function(d){
                           div.style("display", "none");
                        });
                    };
                    chart.draw();
                    window.onresize = function () {

                    chart.draw(0, true);
                    };
                }
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
            t_on['tempLoadingSpaceUsageByUser'] = 0;
        }, "json");
}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/api-usage-user/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

var convertTimeStringPlusDay = function (date) {
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth() + 1)) + "-" + formatTimeChunk(d.getDate() + 1);
    return formattedDate;
};

var formatTimeChunk = function (t) {
    if (t < 10) {
        t = "0" + t;
    }
    return t;
};

function convertDate(date) {
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour=date.getHours();
    var minute=date.getMinutes();
    return date.getFullYear() + '-' + (('' + month).length < 2 ? '0' : '')
        + month + '-' + (('' + day).length < 2 ? '0' : '') + day +" "+ (('' + hour).length < 2 ? '0' : '')
        + hour +":"+(('' + minute).length < 2 ? '0' : '')+ minute;
}

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

