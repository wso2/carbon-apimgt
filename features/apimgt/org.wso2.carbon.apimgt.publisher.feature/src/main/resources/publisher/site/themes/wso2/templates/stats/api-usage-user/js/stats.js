var currentLocation;
var apiFilter = "allAPIs";
var statsEnabled = isDataPublishingEnabled();

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);


    currentLocation = window.location.pathname;

    jagg.post("/site/blocks/stats/api-usage-user/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {

                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    from = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
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

                    $('#date-range').click(function(){
                         $(this).removeClass('active');
                    });

                    //date picker
                    $('#date-range').daterangepicker({
                          timePicker: true,
                          timePickerIncrement: 30,
                          format: 'YYYY-MM-DD h:mm',
                          opens: 'left',
                    });

                    $("#apiFilter").change(function (e) {
                    	apiFilter = this.value;
                    	drawAPIUsage(from,to,apiFilter);
                    });

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       from = convertTimeString(picker.startDate);
                       to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawAPIUsage(from,to,apiFilter);
                    });

                    getDateTime(to,from);

                    $('#date-range').click(function (event) {
                    event.stopPropagation();
                    });

                    $('body').on('click', '.btn-group button', function (e) {
                        $(this).addClass('active');
                        $(this).siblings().removeClass('active');
                    });

                } else {
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


var subscriberDetails=[];
var groupData = [];

var drawAPIUsage = function (from,to,apiFilter) {

    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-subscriptions/ajax/stats.jag", { action: "getSubscriberCountByAPIs", currentLocation: currentLocation, apiFilter: apiFilter },
                function (json) {
    				$('#spinner').hide();
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
                        		$('#chartContainer').empty();
                        		$('div#apiSelectTable_wrapper.dataTables_wrapper.no-footer').remove();
                        		$('#apiUsageByUserTable').hide();
                                $('#noData').html('');
                                $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="'+ i18n.t('No Stats') + '"></i>'+ i18n.t('No Data Available') + '</h4></div></div>'));

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

    jagg.post("/site/blocks/stats/api-usage-user/ajax/stats.jag", { action: "getAPIUsageByUser", currentLocation: currentLocation, fromDate: fromDate, toDate: toDate, apiFilter: apiFilter},
        function (json) {
            if (!json.error) {
                $('#spinner').hide();
                $('#tooltipTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                $('#noData').empty();
                $('#chartContainer').empty();
                $('div#apiSelectTable_wrapper.dataTables_wrapper.no-footer').remove();

                if (length == 0){
                    $('#apiUsageByUserTable').hide();
                    $('#noData').html('');
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info" title="'+ i18n.t('No Stats') + '"></i>'+ i18n.t('No Data Available') + '</h4></div></div>'));

                } else {
                    $('#apiUsage_note').removeClass('hide');
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
                    s.getTooltipText = function (e) {
                        return []; //removes the default tooltip text
                    };
                    s.addEventHandler("mousedown", function (e){
                        var apiName = e.xValue;
                        window.location = "all-statistics.jag?page=api-top-users&stat=all-stat&apiName=" + apiName;
                    });
                    var div = d3.select("body").append("div").attr("class", "toolTip");
                    var filterValues = dimple.getUniqueValues(data, "API");
                    var state_array = [];
                    var defaultFilterValues=[];
                    var sortData=[];
                    var chartData=[];

                    var $dataTable =$('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="apiSelectTable"></table>');

                    $dataTable.append($('<thead class="tableHead"><tr>'+
                                            '<th width="2%">'+
                                            '<input name="mainCheckBox" id="mainCheckBox" type=checkbox checked class="mainCheckBox"/>'+
                                            '</th>'+
                                            '<th width="38%">API</th>'+
                                            '<th style="text-align:right" width="20%" >'+ i18n.t('Subscriber Count') + '</th>'+
                                            '<th class="details-control sorting_disabled" width="40%"></th>'+
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
                                                +'<td class="details-control" style="text-align:right;padding-right:30px;">'+ i18n.t('Show more details') + '<div style="display :inline"class="showDetail"></div></td></tr>'));
                            state_array.push(true);
                            defaultFilterValues.push(sortData[n].API_name);
                            chartData.push(sortData[n].API_name);
                        }else{
                            $dataTable.append($('<tr><td >'
                                                +'<input name="item_checkbox'+n+'"  id='+n+'  type="checkbox"  data-item='+sortData[n].API_name +' class="inputCheckbox"/>'
                                                +'</td>'
                                                +'<td style="text-align:left;"><label for='+n+'>'+sortData[n].API_name +'</label></td>'
                                                +'<td style="text-align:right;"><label for='+n+'>'+sortData[n].SubscriberCount +'</label></td>'
                                                +'<td class="details-control" style="text-align:right;padding-right:30px;">'+ i18n.t('Show more details') + '<div style="display :inline"class="showDetail"></div></td></tr>'));
                            state_array.push(false);
                            chartData.push(sortData[n].API_name);
                        }
                    }

                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();

                    var table = $('#apiSelectTable').datatables_extended({
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
                        { "bSortable": false },
                        ],
                    });
                    $('.details-control').removeClass('sorting');

                    // show more detail table display
                    var detailRows = [];
                    $('#apiSelectTable tbody').on( 'click', 'tr td.details-control', function () {
                       var tr = $(this).closest('tr');
                               var row = table.row(tr);
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

                    //$('select').css('width','80px');
                    chart.data = dimple.filterData(data, "API", defaultFilterValues);

                    $(document).on("click",".paginate_button", function () {
                        checkAllButtonStateChange();
                    });

                    //on main checkbox check and uncheck event
                    $('#apiSelectTable').on( 'change', 'input.mainCheckBox', function () {
                        var rowCount = $('#apiSelectTable tr').length - 1;
                        var pageNumber = document.getElementsByClassName("paginate_button active")[0].
                                                                    children[0].getAttribute('data-dt-idx') - 1;
                        if (pageNumber == "0") {
                            pageNumber = "";
                        }
                        while (rowCount!= 0) {
                            var id = rowCount - 1;
                            $("#"+pageNumber+id.toString()).prop("checked", false);
                            state_array[pageNumber+id.toString()] = false;
                            rowCount--;
                        }
                        var check=$(this).is(':checked');
                        var draw_chart=[];

                        if (check) {
                            var n = pageNumber+"0";
                            rowCount = $('#apiSelectTable tr').length - 1;
                            rowCount = parseInt(n) + rowCount;
                            for (;n < rowCount;n++) {
                                var id = n;
                                state_array[id] = true;
                                $("#"+n).prop("checked", true);
                                $('#displayMsg').html('');
                            }
                        }

                        $.each(chartData, function (index, value) {
                                if (state_array[index]){
                                    draw_chart.push(value);
                                }
                        });
                        chart.data = dimple.filterData(data, "API", draw_chart);
                        chart.draw();
                    });

                    //on checkbox check and uncheck event
                    $('#apiSelectTable').on( 'change', 'input.inputCheckbox', function () {
                          var id =  $(this).attr('id');
                          var check=$(this).is(':checked');
                          var draw_chart=[];
                          $("#mainCheckBox").prop("checked", false);
                          if (check) {
                                $('#displayMsg').html('');
                                state_array[id] = true;
                                checkAllButtonStateChange();
                          } else {
                                $('#displayMsg').html('');
                                state_array[id] = false;
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

                            circle.on("mouseenter", function(d){
                            //circle on mouse enter
                            $(this).css('cursor', 'pointer');
                            
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
                                div.html('<table class="table"><tbody><tr><td><div style="color:#555; text-align:left">'+ i18n.t("API") + '</td><td>' + app + '</td></tr><tr><td><div style="color:#555; text-align:left">'+ i18n.t('Subscriptions') + '</td><td>' + data[i].SubscriberCount + '</td></tr><tr><td><div style="color:#555; text-align:left">'+ i18n.t('Total Hits') + '</td><td>' + data[i].Hits + '</td></tr></tbody></table><table class="table" id="tooltipTable"><thead><tr><th>'+ i18n.t('Version') + '</th><th>'+ i18n.t('Hits') + '</th></tr></thead><tbody></tbody></table>');
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
        }, "json");
}

function format ( d ) {
    var subTable=$('<div class="slider pull-right " ><table style="padding-left:50px;"></table></div>');
    subTable.append($('<thead><tr><th>'+ i18n.t('Version') + '</th><th>'+ i18n.t('Subscriber Count') + '</th><th>'+ i18n.t('Hit Count') + '</th></tr></thead>'));

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
                subTable.append($('<tr><td >'+versions[k].version +'</td><td style="text-align:left">'+versions[k].SubCount+'</td><td style="text-align:left">'+versions[k].hitCount+'</td></tr>'));
            }
        }
    }

    for ( var l = 0; l < versions.length; l++) {
        if($(d[0]).attr('data-item')==versions[l].apiName){
            if(versions[l].isChecked==false){
                   subTable.append($('<tr ><td style="text-align:left">'+versions[l].version +'</td><td >'+versions[l].SubCount+'</td><td style="text-align:left">'+versions[l].hitCount+'</td></tr>'));
            }
        }
    }
    return subTable;
}

function getDateTime(currentDay,fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr = fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawAPIUsage(from,to,apiFilter);
}

function checkAllButtonStateChange() {
    var rowCount = $('#apiSelectTable tr').length - 1;
    var pageNumber = document.getElementsByClassName("paginate_button active")[0].
                                                children[0].getAttribute('data-dt-idx') - 1;
    var n = pageNumber+"0";
    rowCount = parseInt(n) + rowCount;
    var checkCount = 0;
    for (;n < rowCount;n++) {
        var id = n;
        if ($("#"+parseInt(id)).prop("checked")) {
            checkCount++;
        }
    }
    n = pageNumber+"0";
    if (checkCount == rowCount - parseInt(n)) {
        $("#mainCheckBox").prop("checked", true);
    }
    else {
        $("#mainCheckBox").prop("checked", false);
    }
}
