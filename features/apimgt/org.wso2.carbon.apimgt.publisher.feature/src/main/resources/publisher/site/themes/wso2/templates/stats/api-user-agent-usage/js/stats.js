var currentLocation = "allAPIs";$('.stat-page').append($('<br><div class="errorWrapper"><img src="../themes/wso2/images/statsEnabledThumb.png" alt="Thumbnail image when stats are enabled"></div>'));
var statsEnabled = isDataPublishingEnabled();
var apiNameVersionMap = {};
var apiName;
var width;
var height;
var padding_horizontal;
var padding_top;
var drilldown = "ALL";
var version = "ALL";
var ratio = 3/4;
var enableVersion = false; 
var d = new Date();
var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes(),d.getSeconds());
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);
$( document ).ready(function() {
   populateAPIList();
    width = $("#chartContainer").width();
    padding_horizontal = width/9;
    padding_top = width/3;
    height = width * ratio;
    $(window).resize(function() {
    width = $("#chartContainer").width();
    padding_horizontal = width/9;
    padding_top = width/3;
    height = width * ratio;
    renderGraph(from,to);        
    });
   $("#apiSelect").change(function (e) {
       apiName = this.value;
       populateVersionList(apiName,false);
    });
   $("#apiFilter").change(function (e) {
      currentLocation = this.value;
      populateAPIList();
    });

     $('#select-version').on('click', function () {
        enableVersion = true;
         populateVersionList(apiName,false);
       $("#select-version-div-label").css('display','none');
       $("#select-version-btn").css('display','none');
       $("#version-select").css('display','inline');
       $("#version-label").css('display','inline');
    });
      $('#button-clear').on('click', function () {
       $("#select-version-div-label").css('display','inline');
       $("#select-version-btn").css('display','inline');
       $("#version-select").css('display','none');
       $("#version-label").css('display','none');
       version="ALL";
       enableVersion = false;
       renderGraph(from,to);        
    });
    $("#versionSelect").change(function (e) {
      if (enableVersion) {
      version = this.value;        
      }
      renderGraph(from,to);        
    });
    $('#today-btn').on('click', function () {
      from = currentDay - 86400000;
      to = currentDay;
      renderGraph(from,to);
      btnActiveToggle(this);
    });
       $('#week-btn').on('click', function () {
        from = currentDay - 604800000;
        to = currentDay;
        renderGraph(from,to);         
       btnActiveToggle(this);
      });
       $('#month-btn').on('click', function () {
        from = currentDay - (604800000 * 4);
        to = currentDay;
        renderGraph(from,to);        
        btnActiveToggle(this);
        });
        $('#date-range').click(function () {
         $(this).removeClass('active');
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
                        from = picker.startDate;
                        to = picker.endDate;
                        var fromStr = convertTimeString(from).split(" ");
                        var toStr = convertTimeString(to).split(" ");
                        var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                        $("#date-range span").html(dateStr);
                        renderGraph(from, to);
                         });
});
var populateAPIList = function(){
           jagg.post("/site/blocks/stats/ajax/stats.jag", { action : "getAPIList" ,currentLocation:currentLocation},
        function (json) {
        if (!json.error) {
              apiNameVersionMap = json.apiNameVersionMap;
                var i=0;
               var apis = '';
                for (var name in apiNameVersionMap) {
                    if (i==0) {
                    apis += '<option selected="selected" value'+name+'>' + name + '</option>';
                }else{
                    apis+= '<option value='+name+'>' + name+ '</option>';
                }
                i++;
            }
        $('#apiSelect')
                    .empty()
                    .append(apis)
                    .selectpicker('refresh')                    
                    .trigger('change');
                        }
        else {
            if (json.message == "AuthenticateError") {
                jagg.showLogin();
            }
        }

        });
};
var populateVersionList = function(apiName,compare){
        var i=0;
       var selectVersion = '';
        for (var version in apiNameVersionMap[apiName]) {
            var tempVersion = apiNameVersionMap[apiName][version];
                    if (i==0) {
                    selectVersion += '<option selected="selected" value='+tempVersion+'>' + tempVersion + '</option>';
                }else{
                    selectVersion +='<option value='+tempVersion+'>' + tempVersion+ '</option>';
                }
                i++;
}
        $('#versionSelect')
                    .empty()
                    .append(selectVersion)
                    .selectpicker('refresh')                    
                    .trigger('change');
        };

function renderGraph(fromDate,toDate){
  if (statsEnabled) {
   var toDateString = convertTimeString(toDate);
    var fromDateString = convertTimeString(fromDate);
    getDateTime(toDate,fromDate);
    var data = [];
           jagg.post("/site/blocks/stats/api-user-agent-usage/ajax/stats.jag", { action : "getUserAgentUsageByAPI" , apiName : apiName , apiVersion : version , fromDate : fromDateString , toDate : toDateString,drilldown:drilldown},
        function (json) {
            if (!json.error) {
                if (json.usage && json.usage.length > 0) {
                  for(var usage1 in json.usage ){
                  var values = json.usage[usage1].values;
                  var count = values.count;
                  var agent;
                  if (drilldown != "ALL") {
                   agent = values.key_os_browser_facet[1];
                  }else{
                   agent = values.key_os_browser_facet[0];
                  }
                  data.push([count,agent]);
                  }
                    drawGraphInArea(data);
                }
                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                          $('#noData').empty();
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="No Stats"></i>'+i18n.t("No Data Available")+'</h4></div></div>'));
                    $('#chartContainer').hide();
                }
                else {
                          $('#noData').empty();
                         $('.stat-page').html("");
                    showEnableAnalyticsMsg();
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
                          $('#noData').empty();
                    $('.stat-page').html("");
                    showEnableAnalyticsMsg();
  }
}

function drawGraphInArea(rdata){
      $('#noData').empty();
      $('#chartContainer').show();
      $('#chartContainer').empty()
      var pieChart ;
  var data =  [
        {
            "metadata" : {
                "names" : ["count","agent"],
                "types" : ["linear", "ordinal"]
            },
            "data": rdata
        }
    ];
        var configOs = {
            charts : [{type: "arc",  x : "count", color : "agent", mode: "pie"}],
            width: 400,
            height: 300
        }
        var configAgent = {
            charts : [{type: "arc",  x : "count", color : "agent", mode: "pie"}],
            width: 400,
            height: 300
        }

    var callbackmethod = function(event, item) { 

        if (item != null) {
           var tempItem = item.datum.agent;
            if (drilldown == "ALL") {
              drilldown = tempItem;
            }else{
              drilldown = "ALL";
            }
            renderGraph(from, to);
          }
          }
    if (drilldown == "ALL") {
        pieChart = new vizg(data, configOs);
    }else{
        pieChart = new vizg(data, configAgent);
    }
    pieChart.draw("#chartContainer", [{type:"click", callback:callbackmethod}]);
    $('#chartContainer').show();
        };
function getDateTime(currentDay,fromDay){
    toDateString = convertTimeString(currentDay);
    fromDateString = convertTimeString(fromDay);
    var toDate = toDateString.split(" ");
    var fromDate = fromDateString.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
}