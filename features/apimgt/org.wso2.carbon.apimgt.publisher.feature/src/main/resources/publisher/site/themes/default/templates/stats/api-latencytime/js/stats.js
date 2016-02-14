var currentLocation;
currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();
var apiNameVersionMap = {}; 
var apiName;
var version;
var d = new Date();
var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes());
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24);
$( document ).ready(function() {
    populateAPIList();
   $("#apiSelect").change(function (e) {
       apiName = this.value;
       populateVersionList(apiName);
    });
    $("#versionSelect").change(function (e) {
      version = this.value;
      var fromDate = from;
      var toDate = to;
      renderGraph(fromDate,toDate);
    });
    $('#today-btn').on('click', function () {
      renderGraph((currentDay - 86400000),currentDay);
    });
       $('#hour-btn').on('click', function () {
         renderGraph((currentDay - 3600000),currentDay);
      });
       $('#week-btn').on('click', function () {
         renderGraph((currentDay - 604800000),currentDay);
      });
       $('#month-btn').on('click', function () {
        renderGraph((currentDay - (604800000 * 4)),currentDay);
        });
        $('#date-range').click(function () {
         $(this).removeClass('active');
         });
                   //date picker
        $('#date-range').dateRangePicker(
          {
            startOfWeek: 'monday',
            separator: ' <b>to</b> ',
            format: 'YYYY-MM-DD HH:mm',
            autoClose: false,
            time: {
                enabled: true
                  },
            shortcuts: 'hide',
             endDate: currentDay
          })
           .bind('datepicker-apply', function (event, obj) {
                 btnActiveToggle(this);
                 from = convertDate(obj.date1);
                 to = convertDate(obj.date2);
                 renderGraph(from, to);
                        });
});

var populateAPIList = function(){
           jagg.post("/site/blocks/listing/ajax/item-list.jag", { action: "getAllAPIs"},
        function (json) {
        if (!json.error) {
                if (json.apis && json.apis.length>0) {                   
                    var apiList = json.apis;
                    for (var i in apiList) {
                        var apiname = apiList[i].name;
                        var versionList = apiNameVersionMap[apiname];
                        if (!versionList) {
                            versionList = [];
                            }
                            versionList.push(apiList[i].version);
                            apiNameVersionMap[apiname]=versionList;
                        }
                }
                var i=0;
                $('#apiSelect').empty();
                for (var name in apiNameVersionMap) {
                    if (i==0) {
                    $('#apiSelect').append('<option selected="selected" value'+name+'>' + name + '</option>');
                }else{
                    $('#apiSelect').append('<option value='+name+'>' + name+ '</option>');                    
                }
                i++;
            }
            $('#apiSelect').trigger('change');
            }
        });
};
var populateVersionList = function(apiName){
        var i=0;
       $('#versionSelect').empty();
        for (var version in apiNameVersionMap[apiName]) {
            var tempVersion = apiNameVersionMap[apiName][version];
                    if (i==0) {
                    $('#versionSelect').append('<option selected="selected" value'+tempVersion+'>' + tempVersion + '</option>');
                }else{
                    $('#versionSelect').append('<option value='+tempVersion+'>' + tempVersion+ '</option>');                    
                }
                i++;
        }
          $('#versionSelect').trigger('change');
};
function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/api-latencytime/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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
function renderGraph(fromDate,toDate){
   var to = convertTimeString(toDate);
    var from = convertTimeString(fromDate);
           jagg.post("/site/blocks/stats/api-latencytime/ajax/stats.jag", { action : "getExecutionTimeOfAPI" , apiName : apiName , apiVersion : version , fromDate : from , toDate : to },
        function (json) {
            if (!json.error) {
            var data1 = [];
                if (json.usage && json.usage.length > 0) {
                  for(var usage1 in json.usage ){
                    var mediationName = json.usage[usage1].values.mediationName;
                    var tempdata = data1[mediationName];
                    if (!tempdata) {
                        tempdata = [];
                    }
                    var d = new Date(json.usage[usage1].values.year, json.usage[usage1].values.month, json.usage[usage1].values.day, json.usage[usage1].values.hour,json.usage[usage1].values.minutes,"00","00");
                    tempdata.push({x:d,y:json.usage[usage1].values.executionTime});
                     data1[mediationName] = tempdata;
                  }
                    drawGraphInArea(data1);
                }
                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#temploadinglatencytTime').html('');
                    $('#temploadinglatencytTime').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
                    $('#chartContainer').hide();

                }
                else {
                    $('#middle').html("");
                    $('#middle').append($('<div class="errorWrapper"><span class="label top-level-warning"><i class="icon-warning-sign icon-white"></i>'
                        + i18n.t('errorMsgs.checkBAMConnectivity') + '</span><br/><img src="../themes/default/templates/stats/api-last-access-times/images/statsThumb.png" alt="Smiley face"></div>'));
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
function drawGraphInArea(rdata){
    $('#chartContainer').show();
    $('#chartContainer').empty();
    $('#temploadinglatencytTime').empty();
    var renderdata = [];
    var dateFormat = '%d/%m %H:%M';
    for(var legand in rdata){
        renderdata.push({values: rdata[legand],key: legand,color: pickLegandColor(legand)});
    }
nv.addGraph(function() {
  var chart = nv.models.lineChart()
                .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                .transitionDuration(350)  //how fast do you want the lines to transition?
                .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
                .showYAxis(true)        //Show the y-axis
                .showXAxis(true)        //Show the x-axis
  ;
  chart.xAxis.axisLabel('Time (minutes)')
        .tickFormat(function (d) {
        return d3.time.format(dateFormat)(new Date(d))});


  chart.yAxis     //Chart y-axis settings
      .axisLabel('Execution Time (ms)')
      .tickFormat(d3.format(',r'));
  d3.select('#latencytTime svg')    //Select the <svg> element you want to render the chart in.   
      .datum(renderdata)         //Populate the <svg> element with chart data...
      .call(chart);          //Finally, render the chart!

  //Update the chart when window resizes.
  nv.utils.windowResize(function() { chart.update() });
  return chart;
});
$('#chartContainer').append($('<div id="latencytTime"><svg style="height:600px;"></svg></div>'));
$('#latencytTime svg').show();
}
var pickLegandColor = function(legand){
 switch (legand) {
    case "BackEnd":
        return "#0B38AF";
        break;
    case "Throttling":
          return "#FF0000";
          break;
    case "Authentication":
          return "#008000";
          break;
    case "Total Time":
          return "#DA4806";
          break;
    case "CORS":
          return "#06DA0A";
        break;
} 
}