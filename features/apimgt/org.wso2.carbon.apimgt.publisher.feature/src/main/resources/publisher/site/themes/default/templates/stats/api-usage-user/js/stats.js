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
                             var fromStr = from.split(" ");
                             var toStr = to.split(" ");
                             var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                             $("#date-range").html(dateStr);
                             drawAPIUsage(from,to);
                        });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    getDateTime(to,from);


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
var groupData = [];
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
                        groupData = [];

                        if (length > 0) {
                            $('#pie-chart').empty();

                        //grouping data(subscriber count) according to name and version
                             var inputDataStr="";
                             var apiData="";
                             var apiName_Provider="";

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
                        }
                        else{
                                $('#apiUsageByUserTable').hide();
                                $('#tempLoadingSpaceUsageByUser').html('');
                                $('#tempLoadingSpaceUsageByUser').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

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

}
var parsedResponse;
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
                    $('#apiUsage_note').show();
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

                    parsedResponse = JSON.parse(JSON.stringify(webapps));
                    var data=[];
                        for ( var i = 0; i < parsedResponse.length; i++) {
                        var count = 0;
                        var app =(parsedResponse[i][0].replace(/\s+/g, ''));
                        var maximumUsers = 0;
                        var allSubCount =0;
                        var hitCount = 0;
                            for ( var j = 0; j < parsedResponse[i][1].length; j++) {

                        allSubCount = allSubCount+parsedResponse[i][1][j][1].length

                        maximumUsers=parsedResponse[i][1][j][1].length;

                            for ( var k = 0; k < maximumUsers; k++) {
                                count++;
                                hitCount = Number(hitCount)+Number(parsedResponse[i][1][j][1][k][1]);

                            }

                            }
                            var status = false;
                            for(var z =0;z<subscriberDetails.length;z++){
                                if(app == subscriberDetails[z].api_name){
                                    status = true;
                                    allSubCount = subscriberDetails[z].sub_count;
                                    subscriberDetails[z].check=true;
                                    data.push({
                                        API_name:app,
                                        SubscriberCount:allSubCount,
                                        Hits:hitCount,
                                        API:app
                                    });
                                }
                            }

                            userParsedResponse = parsedResponse;
                            if(!status){
                                data.push({
                                API_name: app,
                                SubscriberCount: 0,
                                Hits: hitCount,
                                API: app
                                });
                            }
                        }
                        for(var z =0;z<subscriberDetails.length;z++){
                            if(subscriberDetails[z].check == false){
                                data.push({
                                    API_name:subscriberDetails[z].api_name,
                                    SubscriberCount:subscriberDetails[z].sub_count,
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
                    var y=chart.addMeasureAxis("y", "SubscriberCount");
                    y.title = "Subscription Count";
                    x.title = "APIs";
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
                                            '<th style="text-align:right" width="20%" >Subscriber Count</th>'+
                                            '<th class="details-control pull-right sorting_disabled"></th>'+
                                        '</tr></thead>'));

                    sortData = dimple.filterData(data, "API", filterValues);
                    sortData.sort(function(obj1, obj2) {
                        return obj2.SubscriberCount - obj1.SubscriberCount;
                    });

                    //default display of 20 checked entries on table
                    for(var n=0;n<sortData.length;n++){
                        if(n<20){
                            $dataTable.append($('<tr><td >'
                                                +'<input name="item_checkbox'+n+'"  checked   id='+n+'  type="checkbox"  data-item='+sortData[n].API_name +' class="inputCheckbox"/>'
                                                +'</td>'
                                                +'<td style="text-align:left;"><label for='+n+'>'+sortData[n].API_name +'</label></td>'
                                                +'<td style="text-align:right;"><label for='+n+'>'+sortData[n].SubscriberCount +'</label></td>'
                                                +'<td class="details-control sorting_disabled" style="text-align:right;padding-right:30px;">Show more details<div style="display :inline"class="showDetail"></div></td></tr>'));
                            state_array.push(true);
                            defaultFilterValues.push(sortData[n].API_name);
                            chartData.push(sortData[n].API_name);
                        }else{
                            $dataTable.append($('<tr><td >'
                                                +'<input name="item_checkbox'+n+'"  id='+n+'  type="checkbox"  data-item='+sortData[n].API_name +' class="inputCheckbox"/>'
                                                +'</td>'
                                                +'<td style="text-align:left;"><label for='+n+'>'+sortData[n].API_name +'</label></td>'
                                                +'<td style="text-align:right;"><label for='+n+'>'+sortData[n].SubscriberCount +'</label></td>'
                                                +'<td class="details-control" style="text-align:right;padding-right:30px;">Show more details<div style="display :inline"class="showDetail"></div></td></tr>'));
                            state_array.push(false);
                            chartData.push(sortData[n].API_name);
                        }
                    }

                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();

                    var table =$('#apiSelectTable').DataTable({
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
                        null,
                        false,
                        ],
                    });
                    $('.details-control').removeClass('sorting');

                    // show more detail table display
                    var detailRows = [];
                    $('#apiSelectTable tbody').on( 'click', 'tr td.details-control', function () {
                       var tr = $(this).closest('tr');
                               var row = table.row( tr );
                               if ( row.child.isShown() ) {
                                   // This row is already open - close it
                                   $('div.slider', row.child()).slideUp( function () {
                                                   row.child.hide();
                                                   tr.removeClass('shown');
                                               } );
                               }
                               else {
                                   row.child( format(row.data()), 'no-padding' ).show();
                                   tr.addClass('shown');
                                   $('div.slider', row.child()).slideDown();
                               }
                    } );

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

                                          hitCount = 0;
                                          for ( var k = 0; k < maximumUsers; k++) {
                                            count++;
                                            hitCount = Number(hitCount)+Number(parsedResponse[i][1][j][1][k][1]);
                                          }

                                        versionCount.push({version:parsedResponse[i][1][j][0],count:hitCount});
                                    }

                                div.style("left", d3.event.pageX+10+"px");
                                div.style("top", d3.event.pageY-25+"px");
                                div.style("display", "inline-block");

                                div.html('<div style="color:#555; text-align:left">API : '+app +'</div><div style="color:#666;margin-top:5px;text-align:left">Subscription Count : '+data[i].SubscriberCount+'</div><table class="table" id="tooltipTable"><thead><tr><th>Version</th><th>Hits</th></tr></thead><tbody></tbody></table>');
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

function format ( d ) {
    var subTable=$('<div class="slider pull-right " ><table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;"></table></div>');
    subTable.append($('<thead><tr><th>Version</th><th>Subscriber Count</th><th>Hit Count</th></tr></thead>'));

    var versions=[];
    var isChecked=false;
    for (var i = 0; i < groupData.length; i++) {
        if(groupData[i].api_name == $(d[0]).attr('data-item')){
            for(var j=0;j<groupData[i].versions.length;j++){
                versions.push({"apiName":groupData[i].api_name,"version":groupData[i].versions[j].version,"SubCount":groupData[i].versions[j].Count,"isChecked":isChecked,"hitCount":0});
            }
        }
    }

    for ( var i = 0; i < parsedResponse.length; i++) {
        var count = 0;
        var app ='';

        if( $(d[0]).attr('data-item')== parsedResponse[i][0].replace(/\s+/g, '')){
            var versionCount=[];
            for ( var j = 0; j < parsedResponse[i][1].length; j++) {
                  app =(parsedResponse[i][0]);
                  var maximumUsers = parsedResponse[i][1][j][1].length;
                  hitCount = 0;

                  for ( var l = 0; l < versions.length; l++) {
                      if(parsedResponse[i][1][j][0]==versions[l].version){
                          versions[l].isChecked=true;
                          for ( var k = 0; k < maximumUsers; k++) {
                            count++;
                            hitCount = Number(hitCount)+Number(parsedResponse[i][1][j][1][k][1]);
                          }
                          versionCount.push({version:parsedResponse[i][1][j][0],count:hitCount});
                          versions[l].hitCount = hitCount;
                      }
                  }
            }

            for ( var l = 0; l < versions.length; l++) {
                if(versions[l].isChecked==false){
                   versionCount.push({version:versions[l].version,count:0});
                   versions[l].isChecked=true;
                }
            }

            for(var k = 0; k < versions.length;k++){
                subTable.append($('<tr><td >'+versions[k].version +'</td><td style="text-align:right">'+versions[k].SubCount+'</td><td style="text-align:right">'+versions[k].hitCount+'</td></tr>'));
            }
        }
    }

    for ( var l = 0; l < versions.length; l++) {
        if($(d[0]).attr('data-item')==versions[l].apiName){
            if(versions[l].isChecked==false){
                   subTable.append($('<tr style="text-align:right"><td >'+versions[l].version +'</td><td >'+versions[l].SubCount+'</td><td style="text-align:right">'+versions[l].hitCount+'</td></tr>'));
            }
        }
    }
    return subTable;
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

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range").html(dateStr);
    $('#date-range').data('dateRangePicker').setDateRange(from,to);
    drawAPIUsage(from,to);
}