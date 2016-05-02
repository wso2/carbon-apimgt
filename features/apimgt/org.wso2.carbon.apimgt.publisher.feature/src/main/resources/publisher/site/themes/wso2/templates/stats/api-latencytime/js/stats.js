var currentLocation;
currentLocation = window.location.pathname;
var statsEnabled = isDataPublishingEnabled();
var apiNameVersionMap = {};
var mediationName;
var apiName;
var version;
var comparedVersion = {};
var versionComparison = false;
var d = new Date();
var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(),d.getSeconds());
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24);
var depth ="HOUR";
$( document ).ready(function() {
    populateAPIList();
   $("#apiSelect").change(function (e) {
       apiName = this.value;
       populateVersionList(apiName,false);
    });
    $("#versionSelect").change(function (e) {
      version = this.value;
      comparedVersion[version] = version;
      if (versionComparison) {
             populateVersionList(apiName,true);  
      }else{
      renderGraph(from,to,depth);        
      }
    });
    $("#mediationType").change(function (e) {
      mediationName = this.value;
      versionComparison = true;
      renderCompareGraph(from,to,depth,encodeURIComponent(mediationName));      
    });
    $("#compareVersion").change(function (e) {
      var tempArray = {};
       var tempVersion = $('#compareVersion option:selected');
        $(tempVersion).each(function(index, tempVersion){

      tempArray[$(this).val()] = $(this).val();
        });
      tempArray[version] = version;
      comparedVersion = tempArray;
      $('#mediationType').trigger('change');
    });
    $('#today-btn').on('click', function () {
      from = currentDay - 86400000;
      to = currentDay;
      renderGraph(from,to,"HOUR");
      versionComparison = false;
      depth = "HOUR";
      btnActiveToggle(this);
    });
       $('#hour-btn').on('click', function () {
        from = currentDay - 3600000;
        to = currentDay;
        depth = "MINUTES";
        versionComparison = false;
        renderGraph(from,to,depth);     
        btnActiveToggle(this);
      });
      $('#clear-btn').on('click', function () {
         versionComparison = false;
         renderGraph(from,to,depth);  
         $('#compare-selection').css("display", "none");
         $('#compare-selection-label').css("display", "none");
         $('#compare-version-btn').css("display", "inline");
         $('#compare-btn-label').css("display", "inline");
         });
       $('#week-btn').on('click', function () {
        from = currentDay - 604800000;
        to = currentDay;
        depth = "DAY";
        versionComparison = false;
        renderGraph(from,to,depth);
        btnActiveToggle(this);
      });
       $('#month-btn').on('click', function () {
        from = currentDay - (604800000 * 4);
        to = currentDay;
        depth = "DAY";
        versionComparison = false;
        renderGraph(from,to,depth);
        btnActiveToggle(this);
        });
        $('#date-range').click(function () {
         $(this).removeClass('active');
         });
        $('#compare-btn').on('click', function () {
          populateVersionList(apiName,true);
               $('#compare-selection').css("display", "inline");
               $('#compare-selection-label').css("display", "inline");
               $('#compare-version-btn').css("display", "none");
               $('#compare-btn-label').css("display", "none");
        });
                   //date picker
        $('#date-range').daterangepicker({
                        timePicker: true,
                        timePickerIncrement: 30,
                        format: 'YYYY-MM-DD h:mm',
                        opens: 'left'
                    });
        $('#date-range').on('apply.daterangepicker', function (ev, picker) {
                        btnActiveToggle(this);
                        from = convertDate(picker.startDate);
                        to = convertDate(picker.endDate);
                        var fromStr = from.split(" ");
                        var toStr = to.split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        $("#date-range span").html(dateStr);
                        if ((to-from)>(3600000*24*2)) {
                           depth = "DAY";
                           renderGraph(from, to,depth);                        
                           }else{
                           depth = "HOUR";
                           renderGraph(from, to,depth);
                           }
                         });
});

var populateAPIList = function(){
           jagg.post("/site/blocks/stats/ajax/stats.jag", { action : "getAPIList" ,currentLocation:currentLocation},
        function (json) {
        if (!json.error) {
              apiNameVersionMap = json.apiNameVersionMap;
                var i=0;
                var apis= '';
                for (var name in apiNameVersionMap) {
                    if (i==0) {
                    apis+='<option selected="selected" value='+name+'>' + name + '</option>';
                }else{
                    apis+='<option value='+name+'>' + name+ '</option>';
                }
                i++;
            }
           $('#apiSelect')
                    .empty()
                    .append(apis)
                    .selectpicker('refresh')                    
                    .trigger('change');
            }
        });
};
var populateVersionList = function(apiName,compare){
        var i=0;
        if (compare) {
          $('#compareVersion').multiselect();      
           $('#compareVersion option').each(function(index, option) {
              $(option).remove();
            });
        for (var ver in apiNameVersionMap[apiName]) {
            var tempVersion = apiNameVersionMap[apiName][ver];
            if (tempVersion != $('#versionSelect option:selected').val()) {
                    if (i==0) {
                    $('#compareVersion').append('<option selected="selected" value'+tempVersion+'>' + tempVersion + '</option>');
                }else{
                    $('#compareVersion').append('<option value='+tempVersion+'>' + tempVersion+ '</option>');
                }
                i++;
              }
        }
        $('#compareVersion').multiselect('rebuild');          
        $('#compareVersion').trigger('change');
        }else{
          var selectVersions = '';
        for (var version in apiNameVersionMap[apiName]) {
            var tempVersion = apiNameVersionMap[apiName][version];
                    if (i==0) {
                    selectVersions += '<option selected="selected" value='+tempVersion+'>' + tempVersion + '</option>';
                }else{
                    selectVersions += '<option value='+tempVersion+'>' + tempVersion+ '</option>';
                }
                i++;
        }
if (versionComparison){
        if (apiNameVersionMap[apiName].length == 1) {
            $('#clear-btn').trigger('click');
        }  
}
           $('#versionSelect')
                    .empty()
                    .append(selectVersions)
                    .selectpicker('refresh')                    
                    .trigger('change');
        }
};

var populateMediations = function(data){
        var i=0;
        var mediations = '';
        for (var mediationName in data) {
                    if (i==0) {
                    mediations +='<option selected="selected" value'+mediationName+'>' + mediationName + '</option>';
                }else{
                    mediations +='<option value='+encodeURIComponent(mediationName)+'>' + mediationName+ '</option>';
                }
                i++;
              }
                $('#mediationType')
                    .empty()
                    .append(mediations)
                    .selectpicker('refresh');
};
function renderGraph(fromDate,toDate,drillDown){
   var to = convertTimeString(toDate);
    var from = convertTimeString(fromDate);
        getDateTime(to,from);
    if (statsEnabled) {
        jagg.post("/site/blocks/stats/api-latencytime/ajax/stats.jag", { action : "getExecutionTimeOfAPI" , apiName : apiName , apiVersion : version , fromDate : from , toDate : to,drilldown:drillDown},
        function (json) {
            if (!json.error) {
            var data1 = {};
                if (json.usage && json.usage.length > 0) {
                  for(var usage1 in json.usage ){
                    var mediationName = json.usage[usage1].values.mediationName;
                    var tempdata = data1[mediationName];
                    if (!tempdata) {
                        tempdata = [];
                    }
                    var d = new Date(json.usage[usage1].values.year, (json.usage[usage1].values.month -1), json.usage[usage1].values.day, json.usage[usage1].values.hour,json.usage[usage1].values.minutes,json.usage[usage1].values.seconds,"00");
                    tempdata.push({x:d,y:json.usage[usage1].values.executionTime});
                     data1[mediationName] = tempdata;
                  }
                    populateMediations(data1);
                    drawGraphInArea(data1,drillDown);
                }
                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('#noData').html('');
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>No Data Available.</h4></div></div>'));
                    $('#chartContainer').hide();

                }
                else {
                  $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><span class="top-level-warning"><span class="glyphicon glyphicon-warning-sign blue"></span>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/wso2/images/statsEnabledThumb.png" alt="Thumbnail image when stats enabled"></div>'));
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
    }else{
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><span class="top-level-warning"><span class="glyphicon glyphicon-warning-sign blue"></span>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/wso2/images/statsThumb.png" alt="Thumbnail image when stats not configured"></div>'));
    }
}
function renderCompareGraph(fromDate,toDate,drillDown,mediationName){
   var to = convertTimeString(toDate);
    var from = convertTimeString(fromDate);
    getDateTime(to,from);
           jagg.post("/site/blocks/stats/api-latencytime/ajax/stats.jag", { action : "getComparisonData" , apiName : apiName , fromDate : from , toDate : to,drilldown:drillDown,versionArray:JSON.stringify(comparedVersion),mediationName:decodeURIComponent(mediationName)},
        function (json) {
            if (!json.error) {
                  drawGraphInArea(json.usage,drillDown);                    
          }
        }, "json");
}

function drawGraphInArea(rdata,drilldown){
    $('#chartContainer').show();
    $('#chartContainer').empty();
    $('#noData').empty();
    $('#temploadinglatencytTime').empty();
    var renderdata = [];
    var dateFormat;
    var xAxisLabel;
    if (drilldown == "DAY") {
      dateFormat = '%d/%m';
      xAxisLabel = 'Time (Days)';
    }else if (drilldown == "HOUR") {
      dateFormat = '%d/%m %H';
      xAxisLabel = 'Time (Hours)';
    }else if (drilldown == "MINUTES") {
      dateFormat = '%d/%m %H:%M';
      xAxisLabel = 'Time (Minutes)';
    }else if (drilldown == "SECONDS") {
      dateFormat = '%d/%m %H:%M:%S';
      xAxisLabel = 'Time (Seconds)';
    }
    for(var legand in rdata){
        renderdata.push({values: rdata[legand],key: legand,color: pickLegandColor(legand)});
    }
nv.addGraph(function() {
  var chart = nv.models.lineChart()
                .margin({left: 100, bottom: 100})
                .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                .transitionDuration(350)  //how fast do you want the lines to transition?
                .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
                .showYAxis(true)        //Show the y-axis
                .showXAxis(true)        //Show the x-axis
            ;
  chart.xAxis.axisLabel(xAxisLabel).rotateLabels(-45)
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
 d3.selectAll(".nv-point").on("click", function (e) {
    var date = new Date(e.x);
    if (depth == "DAY"){
     from = new Date(e.x).setDate(date.getDate()-1);
     to = new Date(e.x).setDate(date.getDate()+1);
    depth = "HOUR";
    }else if (depth == "HOUR"){
     from = new Date(e.x).setHours(date.getHours()-1);
     to = new Date(e.x).setHours(date.getHours()+1);
    depth = "MINUTES";
    }else if (depth == "MINUTES"){
     from = new Date(e.x).setMinutes(date.getMinutes()-1);
     to = new Date(e.x).setMinutes(date.getMinutes()+1);
    depth = "SECONDS";
    }else if (depth == "SECONDS"){
    depth = "HOUR";
     from = new Date(e.x).setHours(date.getHours()-1);
     to = new Date(e.x).setHours(date.getHours()+1);
    }
    if (versionComparison) {
       renderCompareGraph(from,to,depth,encodeURIComponent(mediationName));      
    }else{
    renderGraph(from,to,depth);
    }
  });
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
    case "1.0.0":
         return "#DA4806";
          break;
    case "2.0.0":
          return "#06DA0A";
        break;
}
}
function getDateTime(currentDay,fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
}