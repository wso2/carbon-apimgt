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

var isToday=false;
var isMonth=false;
var isHour=false;
var isDefault=false;
var isWeek=false;

require(["dojo/dom", "dojo/domReady!"], function (dom) {
    currentLocation = window.location.pathname;
    //Initiating the fake progress bar
    jagg.fillProgress('apiChart');
    jagg.fillProgress('subsChart');
    jagg.fillProgress('serviceTimeChart');
    jagg.fillProgress('tempLoadingSpace');

    jagg.post("/site/blocks/stats/api-usage-resource-path/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());//                    if (firstAccessDay.valueOf() == currentDay.valueOf()) {


                    //day picker
                    $('#today-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-86400000);
                        isToday=true;
                        isWeek,isMonth,isDefault,isHour=false;

                    });

                    //hour picker
                    $('#hour-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-3600000);
                        isHour=true;
                        isWeek,isMonth,isDefault,isToday=false;
                    })

                    //week picker
                    $('#week-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-604800000);
                        isWeek=true;
                        isToday,isMonth,isDefault,isHour=false;
                    })

                    //month picker
                    $('#month-btn').on('click',function(){
                        getDateTime(currentDay,currentDay-(604800000*4));
                        isMonth=true;
                        isWeek,isToday,isDefault,isHour=false;
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
                             drawAPIUsageByResourcePath(from,to);

                             $('.apply-btn').on('click',function(){
                                 isDefault=true;
                                 isWeek,isMonth,isToday,isHour=false;
                             });
                        });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    getDateTime(to,from);
                    isMonth=true;
                    isWeek,isToday,isDefault,isHour=false;


                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).siblings().removeClass('active');
                        $(this).addClass('active');
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
                        + i18n.t('errorMsgs.checkBAMConnectivity') + '</span><br/><img src="../themes/default/templates/stats/api-usage-resource-path/images/statsThumb.png" alt="Smiley face"></div>'));
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


var drawAPIUsageByResourcePath = function (from, to) {
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-usage-resource-path/ajax/stats.jag", { action: "getAPIUsageByResourcePath", currentLocation: currentLocation, fromDate: fromDate, toDate: toDate},
        function (json) {
            if (!json.error) {
                $('#resourcePathUsageTable').find("tr:gt(0)").remove();
                var length = json.usage.length;

                $('#tempLoadingSpaceResourcePath').empty();
                $('#chartContainer').empty();

                $('div#resourcePathUsageTable_wrapper.dataTables_wrapper.no-footer').remove();
                var chart;
                var $dataTable =$('<table class="display defaultTable" width="100%" cellspacing="0" id="resourcePathUsageTable"></table>');

                $dataTable.append($('<thead class="tableHead"><tr>'+
                                        '<th id="api">api</th>'+
                                        '<th id="version">version</th>'+
                                        '<th id="context">context</th>'+
                                        '<th id="method">method</th>'+
                                        '<th style="text-align:right">Hits</th>'+
                                    '</tr></thead>'));
                var obj, result;
                var apis = [];

                 //grouping data according to api name, version, context, method
                for(x=0;x<length;x++){

                     var apiIndex = -1;
                     var apiVersionIndex = -1;
                     var webresourceIndex = -1;
                     var methodIndex =-1;

                     for(y=0;y<apis.length;y++){

                         if(apis[y][0] == json.usage[x].apiName){
                             apiIndex = y;
                             var z;
                             for(z=0;z<apis[y][1].length;z++){
                                 if(apis[y][1][z][0] ==  json.usage[x].version){
                                     apiVersionIndex = z;
                                     var t;

                                     for(t=0;t<apis[y][1][z][1].length;t++){
                                         if(apis[y][1][z][1][t][0] == json.usage[x].context){
                                             webresourceIndex = t;
                                              var b;

                                              for(b=0;b<apis[y][1][z][1][t][1].length;b++){
                                                  if(apis[y][1][z][1][t][1][b][0] == json.usage[x].method){
                                                      methodIndex = b;
                                                      break;
                                                  }
                                              }
                                         }
                                     }
                                 }
                             }
                             if((apiVersionIndex == -1) && (z == apis[y].length)){
                                 break;
                             }
                         }
                     }

                     if(apiIndex == -1){
                         var version = [];
                         var requestCount = [];
                         var resourse =[];
                         var method =[];
                         requestCount.push([json.usage[x].count,json.usage[x].time]);
                         method.push([json.usage[x].method,requestCount]);
                         resourse.push([json.usage[x].context,method]);
                         version.push([json.usage[x].version,resourse]);
                         apis.push([json.usage[x].apiName,version]);
                     }else{
                         if(apiVersionIndex == -1){
                             var requestCount = [];
                             var resourse =[];
                              var method =[];
                             requestCount.push([json.usage[x].count,json.usage[x].time]);
                             method.push([json.usage[x].method,requestCount]);
                             resourse.push([json.usage[x].context,method]);
                             apis[apiIndex][1].push([json.usage[x].version,resourse]);
                         }else{
                             if(webresourceIndex == -1){
                                 var requestCount = [];
                                 var method =[];
                                 requestCount.push([json.usage[x].count,json.usage[x].time]);
                                 method.push([json.usage[x].method,requestCount]);
                                 apis[apiIndex][1][apiVersionIndex][1].push([json.usage[x].context,method]);
                             }else{
                                if(methodIndex == -1){
                                     var requestCount = [];
                                     requestCount.push([json.usage[x].count,json.usage[x].time]);
                                     apis[apiIndex][1][apiVersionIndex][1][webresourceIndex][1].push([json.usage[x].method,requestCount]);
                                }else{
                                 apis[apiIndex][1][apiVersionIndex][1][webresourceIndex][1][methodIndex][1].push([json.usage[x].count,json.usage[x].time]);
                                 }
                            }
                         }
                     }
                }
                var parsedResponse=apis;
                var data=[];
                var rowId=0;

                //adding data to the table
                for ( var i = 0; i < parsedResponse.length; i++) {

                    var appName =(parsedResponse[i][0]);
                    var version;
                    var hitCount =0;
                    var contextName;
                    var numberOfaccesTime;
                    var numberOfContext;
                    var method;


                    for ( var j = 0; j < parsedResponse[i][1].length; j++) {
                        numberOfContext = parsedResponse[i][1][j][1].length;
                        version = parsedResponse[i][1][j][0]

                        for ( var k = 0; k < numberOfContext; k++) {

                            numberOfmethods =parsedResponse[i][1][j][1][k][1].length;
                            contextName = parsedResponse[i][1][j][1][k][0];

                            for(var m = 0; m < numberOfmethods; m++){
                               method = parsedResponse[i][1][j][1][k][1][m][0];
                               numberOfaccesTime=parsedResponse[i][1][j][1][k][1][m][1].length
                               for(var l = 0; l < numberOfaccesTime; l++){
                                  hitCount = Number(hitCount)+Number(parsedResponse[i][1][j][1][k][1][m][1][l][0]);

                                  hits=parsedResponse[i][1][j][1][k][1][m][1][l][0];
                                   time = parsedResponse[i][1][j][1][k][1][m][1][l][1];
                               }
                               rowId++;
                               $dataTable.append($('<tr id='+rowId+'><td>' + appName + '</td><td>' + version + '</td><td>' +'<a id="'+rowId+'" class="link" href="#" >'+contextName+'</a>'+ '</td><td>' + method + '</td><td class="tdNumberCell">' + hitCount+ '</td></tr>'));
                               hitCount =0;
                            }
                        }
                    }
                }

                    var methodName=" ";
                    var row=0;
                    //on context click to show the graph
                    $dataTable.on("click", '.link',function(){

                        //disable scrolling
                        $('body').css('overflow','hidden');
                        window.onmousewheel = document.onmousewheel = function(e) {
                            e = e || window.event;
                            if (e.preventDefault)
                                e.preventDefault();
                            e.returnValue = false;
                        };

                        var row= $(this).closest('tr').attr('id');
                        var context=$(this).text();
                        methodName=getCell('method', ''+row+'').html();

                        for ( var i = 0; i < parsedResponse.length; i++) {

                        if( parsedResponse[i][0] ==  getCell('api', ''+row+'').html()){
                            for ( var j = 0; j < parsedResponse[i][1].length; j++) {

                                if( parsedResponse[i][1][j][0] ==  getCell('version', ''+row+'').html()){
                                   numOfVersion = parsedResponse[i][1][j][1].length;
                                    for( var t = 0; t < numOfVersion; t++) {
                                           if( parsedResponse[i][1][j][1][t][0] ==  context){

                                           var dataStructure=[];
                                                for( var k = 0; k < parsedResponse[i][1][j][1][t][1].length; k++) {

                                                    if(parsedResponse[i][1][j][1][t][1][k][0] ==  methodName){

                                                        for( var p = 0; p < parsedResponse[i][1][j][1][t][1][k][1].length; p++){

                                                             hits=parsedResponse[i][1][j][1][t][1][k][1][p][0];

                                                             var time=parsedResponse[i][1][j][1][t][1][k][1][p][1];
                                                             var str = time;
                                                             var d=new Date(str.split(' ')[0].split('-').join(',') + ',' + str.split(' ')[1].split('-').join(','));
                                                             var year= d.getFullYear();
                                                             var month=d.getMonth();
                                                             var date= d.getDate();
                                                             var hour=d.getHours();
                                                             var min= d.getMinutes();
                                                             var second=d.getSeconds();

                                                            var dateInMiliSeconds = dateToUnix(year,(month+1),date,hour,min, second);

                                                            dataStructure.push({
                                                                     'y':hits,
                                                                     'x':dateInMiliSeconds
                                                                 });
                                                        }
                                                    }
                                                }

                                                dataStructure.sort(function(obj1, obj2) {
                                                    return obj1.x - obj2.x;
                                                });


                                                nv.addGraph(function () {
                                                    chart = nv.models.lineWithFocusChart().margin({right: 120,top: 100,left: 120});
                                                    var fitScreen = false;

                                                    chart.color(d3.scale.category20b().range());
                                                    chart.xAxis.axisLabel('Time');
                                                    chart.yAxis.axisLabel('Hits');
                                                    chart.clipEdge(false);

                                                    //chart.lines.xScale(d3.time.scale());
                                                    //chart.lines2.xScale(d3.time.scale());

                                                    chart.yAxis.tickFormat(d3.format(',d'));
                                                    chart.y2Axis.tickFormat(d3.format(',d'));

                                                    chart.xAxis.tickFormat(function (d) {
                                                    if(isToday){
                                                        return d3.time.format('%d %b %H:%M')(new Date(d))
                                                    }else if(isHour){
                                                        return d3.time.format('%H:%M')(new Date(d))
                                                    }else if(isWeek){
                                                        return d3.time.format('%d %b')(new Date(d))
                                                    }else if(isMonth){
                                                        return d3.time.format('%d %b')(new Date(d))
                                                    }else if(isDefault){
                                                        return d3.time.format('%d %b %Y')(new Date(d))

                                                    }else{
                                                        return d3.time.format('%d %b %Y %H:%M')(new Date(d))

                                                    }

                                                    });

                                                    chart.x2Axis.tickFormat(function (d) {
                                                    if(isToday){
                                                        return d3.time.format('%d %b %H:%M')(new Date(d))
                                                    }else if(isHour){
                                                        return d3.time.format('%H:%M')(new Date(d))
                                                    }else if(isWeek){
                                                        return d3.time.format('%d %b')(new Date(d))

                                                    } else if(isMonth){
                                                        return d3.time.format('%d %b')(new Date(d))
                                                    }else if(isDefault){
                                                        return d3.time.format('%d %b %Y')(new Date(d))
                                                    }else{
                                                        return d3.time.format('%d %b %Y %H:%M')(new Date(d))
                                                    }

                                                    });
                                                    chart.tooltipContent(function (key, y, e, graph) {
                                                        var x = d3.time.format('%d %b %Y %H:%M:%S')(new Date(parseInt(graph.point.x)));
                                                        var y = String(graph.point.y);
                                                        if (key == 'Hits') {
                                                            var y = 'There is ' + String(graph.point.y) + ' Hit(s)';
                                                        }

                                                        tooltip_str = '<center><b>' + key + '</b></center>' + y + ' on ' + x;
                                                        return tooltip_str;
                                                    });

                                                //adding default focus area
                                                var dataLength=0;
                                                dataLength=dataStructure.length-1;
                                                chart.brushExtent([dataStructure[0].x,dataStructure[dataLength].x]);

                                                    d3.select('#lineWithFocusChart svg')
                                                        .datum(data_lineWithFocusChart)
                                                        .transition().duration(500)
                                                        .attr('height', 450)
                                                        .call(chart);

                                                    nv.utils.windowResize(chart.update);

                                                return chart;
                                                });

                                                data_lineWithFocusChart = [{
                                                    'values': dataStructure,
                                                    'key': 'Hits',
                                                    'yAxis': '1',
                                                    'color': '#1f77b4'
                                                }];
                                           }
                                    }
                                }
                            }
                        }
                    }
                    $('#light').css('display','block');
                    $('#fade').css('display','block');
                });

                $('#fade').on("click",function(){
                    $('#light').css('display','none');$('#fade').css('display','none'); $('body').css('overflow','auto');
                    window.onmousewheel = document.onmousewheel = function(e) {
                        e = e || window.event;
                        if (e.preventDefault)
                            e.preventDefault();
                        e.returnValue = true;
                    };
                });

                if (length == 0) {
                    $('#resourcePathUsageTable').hide();
                    $('#tempLoadingSpaceResourcePath').html('');
                    $('#tempLoadingSpaceResourcePath').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

                } else {
                    $('#tableContainer').append($dataTable);
                    $('#chartContainer').append($('<div id="lineWithFocusChart"><svg style="height:450px;"></svg></div>'));
                    $('#tableContainer').show();
                    $('#resourcePathUsageTable').DataTable({
                        "fnDrawCallback": function(){
                            if(this.fnSettings().fnRecordsDisplay()<=$("#resourcePathUsageTable_length option:selected" ).val()
                            || $("#resourcePathUsageTable_length option:selected" ).val()==-1)
                                $('#resourcePathUsageTable_paginate').hide();
                            else
                                $('#resourcePathUsageTable_paginate').show();
                        },
                    });
                    $('select').css('width','60px');
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }
            t_on['tempLoadingSpaceResourcePath'] = 0;
        }, "json");

}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/api-usage-resource-path/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

function getCell(column, row) {
    var column = $('#' + column).index();
    var row = $('#' + row)
    return row.find('td').eq(column);
}

function dateToUnix(year, month, day, hour, minute, second) {
    return ((new Date(year, month - 1, day, hour, minute, second)).getTime() );
}

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

function onClose(){
    $('#light').css('display','none');
    $('#fade').css('display','none');
    $('body').css('overflow','auto');
    window.onmousewheel = document.onmousewheel = function(e) {
        e = e || window.event;
        if (e.preventDefault)
            e.preventDefault();
        e.returnValue = true;
    };
}

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range").html(dateStr);
    $('#date-range').data('dateRangePicker').setDateRange(from,to);
    drawAPIUsageByResourcePath(from,to);
}