var currentLocation;
var apiFilter = "allAPIs";
var subscribedApi = "All";
//setting default date
var fromDate;
var toDate;

currentLocation = window.location.pathname;

$(document).ready(function(){

        var d = new Date();
        //var firstAccessDay = new Date(json[0].year, json[0].month - 1, json[0].day);
        var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());

        //day picker
        $('#today-btn').on('click',function(){
            toDate = convertTimeString(currentDay);
            fromDate = convertTimeString(currentDay-86400000);
            drawSubscriptionTime();
        });

        //hour picker
        $('#hour-btn').on('click',function(){
            toDate = convertTimeString(currentDay);
            fromDate = convertTimeString(currentDay-3600000);
            drawSubscriptionTime();
        })

        //week picker
        $('#week-btn').on('click',function(){
            toDate = convertTimeString(currentDay);
            fromDate = convertTimeString(currentDay-604800000);
            drawSubscriptionTime();
        })

        //month picker
        $('#month-btn').on('click',function(){
            toDate = convertTimeString(currentDay);
            fromDate = convertTimeString(currentDay-(604800000*4));
            drawSubscriptionTime();
        });

        $('#date-range').click(function(){
             $(this).removeClass('active');
        });

        //date picker
        $('#date-range').daterangepicker({
              timePicker: true,
              timePickerIncrement: 30,
              format: 'YYYY-MM-DD HH:mm:ss',
              startDate: moment().subtract(1, 'month'),
              endDate: moment().add(1, 'day').format('YYYY-MM-DD  HH:mm:ss'),
              opens: 'left',
        });

        fromDate = $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD HH:mm:ss');
        toDate = $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD HH:mm:ss');

        $('#date-range').on('apply.daterangepicker', function(ev, picker) {
           btnActiveToggle(this);
           var from = convertTimeString(picker.startDate);
           var to = convertTimeString(picker.endDate);
           var fromStr = from.split(" ");
           var toStr = to.split(" ");
           var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
           $("#date-range span").html(dateStr);
           fromDate = $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD HH:mm:ss');
           toDate = $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD HH:mm:ss');
           drawSubscriptionTime();
        });

        drawSubscriptionTime();

        $('#date-range').click(function (event) {
        event.stopPropagation();
        });

        $('body').on('click', '.btn-group button', function (e) {
            $(this).addClass('active');
            $(this).siblings().removeClass('active');
        });

        $("#apiFilter").change(function (e) {
            apiFilter = this.value;
            drawSubscriptionTime();
        });
});

var drawSubscriptionTime = function () {
    jagg.post("/site/blocks/stats/subscriptions-time/ajax/stats.jag",
        {
            currentLocation: currentLocation,
            apiFilter: apiFilter,
            fromDate: fromDate,
            toDate: toDate
        },

        function (json) {
            $('#spinner').hide();
            if (!json.error) {

                    var length = json.length, s1 = [];
                    var data = [];
                    if (length > 0) {
                        $('#noData').empty();
                        $('#chartContainer').show();
                        $('.filters').css('display','block');
                        $('#chartContainer').empty();

                        var groupData = [];

                        var origin_json = json;
                        var graph_data = [];

                        for(var i=0; i<origin_json.length; i++){
                        var d = new Date(origin_json[i].created_time);
                        var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth() + 1)) + "-" + formatTimeChunk(d.getDate())
                                            + " " + formatTimeChunk(d.getHours())+ ":" + formatTimeChunk(d.getMinutes()) + ":" + formatTimeChunk(d.getMinutes());
                        	graph_data.push(
                        		{
                                "api_name": origin_json[i].api_name,
                                "versions": [
                                        {	"version": origin_json[i].api_version ,
                                            "time": [
                                                    {	"subscription_count": origin_json[i].subscription_count,
                                                        "created_time": d.toUTCString()
                                                    }
                                                ]
                                        }],
                                }
                        	);
                        }

                        var temp_array = [];
                        graph_data.map(function(element){
                            var outerElement = element;
                            var found = false;
                            for (var i = 0; i < temp_array.length; i++)
                            {
                                if (temp_array[i].api_name == outerElement.api_name)
                                {
                                   found = temp_array[i];
                                   break;
                                }
                            };

                            if (found)
                            {
                               if (found.versions != outerElement.versions)
                               {
                                  found.versions.push(outerElement.versions[0]);
                               }
                            }
                            else
                            {
                              outerElement.versions = outerElement.versions;
                              temp_array.push(outerElement);
                            }

                        });

                        for(var j=0; j < temp_array.length; j++){
                        	var temp_innrer_array = [];
                        	temp_array[j]['versions'].map(function(element){
                            var outerElement = element;
                            var found = false;
                            for (var i = 0; i < temp_innrer_array.length; i++)
                            {
                                if (temp_innrer_array[i].version == outerElement.version )
                                {
                                   found = temp_innrer_array[i];
                                   break;
                                }
                            };

                            if (found)
                            {
                               if (found.time != outerElement.time)
                               {

                                  found.time.push(outerElement.time[0]);
                               }
                            }
                            else
                            {
                              outerElement.time = outerElement.time;
                              temp_innrer_array.push(outerElement);
                            }

                        	});

                        	for(var item in temp_innrer_array) {
                                var count = 0;
                                for(var item2 in temp_innrer_array[item]['time']) {
                                    count += parseInt(temp_innrer_array[item]['time'][item2]['subscription_count']);
                                    temp_innrer_array[item]['time'][item2]['subscription_count'] = count;
                                }
                            }

                        	temp_array[j]['versions'] = [];
                        	temp_array[j]['versions']=temp_innrer_array;
                        }

                        var apiName ='';
                        for ( var i=0; i<temp_array.length ; i++){
                            apiName += '<option>'+ temp_array[i].api_name+'</option>'
                        }

                        $('#apiSelect')
                           .html(apiName)
                           .selectpicker('refresh');

                        $('#apiSelect').on('change', function() {
                        var chartValues = [];
                        for ( var i=0; i<temp_array.length ; i++){

                            if(temp_array[i].api_name==this.value ){

                                for ( var j=0; j<temp_array[i].versions.length ; j++){

                                    var dataStructure = [];
                                    for ( var k=0; k<temp_array[i].versions[j].time.length ; k++){
                                            dataStructure.push({
                                                'x':Date.parse(temp_array[i].versions[j].time[k].created_time),
                                                'y':parseInt(temp_array[i].versions[j].time[k].subscription_count)

                                            });
                                    }
                                    chartValues.push({
                                            'values':dataStructure,
                                            'key': temp_array[i].versions[j].version
                                    });
                                }
                            }
                        }

                        (function (data) {
                            nv.addGraph(function () {
                            var chart = nv.models.lineChart()
                                .margin({right: 40, left: 75})
                                .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
                                .transitionDuration(350)  //how fast do you want the lines to transition?
                                .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
                                .showYAxis(true)        //Show the y-axis
                                .showXAxis(true) ;       //Show the x-axis

                            chart.xAxis.axisLabel('Time')
                            .tickFormat(function (d) {
                                 return d3.time.format('%d/%m %H:%M:%S')(new Date(d)) });

                            chart.yAxis.axisLabel('Subscriber Count')
                                .tickFormat(d3.format('d'));

                            d3.select('#subscriberTimeChart svg')
                                .datum(data)
                                .call(chart);

                            nv.utils.windowResize(chart.update);

                            return chart;
                        });
                        })(chartValues);
                    });
                        $('#chartContainer').append($('<div id="subscriberTimeChart"><svg style="height:600px;"></svg></div>'));
                        $('#subscriberTimeChart svg').show();

                    }else if(length == 0) {
                        $('.filters').css('display','none');
                        $('#chartContainer').hide();
                        $('#tableContainer').hide();
                        $('#noData').html('');
                        $('#noData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));
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

var convertTimeString = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate())+" "
                            + formatTimeChunk(d.getHours()) + ":" + formatTimeChunk(d.getMinutes()) + ":" + formatTimeChunk(d.getSeconds());
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

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}


