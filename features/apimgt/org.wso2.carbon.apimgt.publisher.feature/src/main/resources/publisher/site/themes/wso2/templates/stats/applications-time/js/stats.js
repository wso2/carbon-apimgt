var chart;
var chartData;
var apiFilter = "allAPIs";
var selectedDeveloper = "All";
var selectedCreator = "All";
var subscribedApi = "All";

//setting default date
var to = new Date();
var from = new Date(2012, 1, 1);

function update_chart(data) {
    //clear previous data
    d3.select("svg").selectAll("*").remove();
    // Update the SVG with the new data and call chart
    chartData.datum(data).transition().duration(500).call(chart);
    nv.utils.windowResize(chart.update);
};

$(document).ready(function(){
    chart = "";
    chartData = "";
    var d = new Date();
    var currentDay = new Date(d.getFullYear(), d.getMonth(), d.getDate(),d.getHours(),d.getMinutes());

    $('#date-range').click(function(){
        $(this).removeClass('active');
    });

    /* Load date range picker */
    $('#date-range').daterangepicker({
        timePicker: false,
        timePickerIncrement: 30,
        format: 'YYYY-MM-DD HH:mm:ss',
        startDate: moment().subtract(1, 'month'),
        endDate: moment().add(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        opens: 'left',
    });

    /***
     * Filter controllers.
     */
     $('#date-range').on('apply.daterangepicker', function(ev, picker) {
        btnActiveToggle(this);
        from = convertTimeString(picker.startDate);
        to = convertTimeString(picker.endDate);
        var fromStr = from.split(" ");
        var toStr = to.split(" ");
        var dateStr = fromStr[0] + " <b>to</b> " + toStr[0];
        $("#date-range span").html(dateStr);
        drawAppTime(from,to);
     });
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

    $("#apiFilter").change(function (e) {
    	$('#apiSelect').empty();
    	$('#apiSelect').append('<option>'+ i18n.t('All') +'</option>');
    	$('#developerSelect').empty();
    	$('#developerSelect').append('<option>'+ i18n.t('All') +'</option>');
        apiFilter = this.value;
        apiFilterList();
        developerFilter();
        drawAppTime(from, to);
    });

    nv.addGraph(function () {
        chart = nv.models.lineChart()
            .margin({right: 30, left: 75})
            //.useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
            .transitionDuration(350)  //how fast do you want the lines to transition?
            .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
            .showYAxis(true)        //Show the y-axis
            .showXAxis(true);       //Show the x-axis



        chart.xAxis.axisLabel(i18n.t('Time'))
        .rotateLabels(-20)
        .tickFormat(function (d) {
             return d3.time.format('%m/%d %H:%M')(new Date(d)) });

        chart.yAxis.axisLabel(i18n.t('Application Count'))
            .tickFormat(d3.format('d'));

        chart.tooltipContent(function(key, x, y, e, graph) {
            return '<p><b>'+x+'</b> -' + key + ': ' + y + '</p>'
        });

        // Assign the SVG selction
        chartData = d3.select('#chartContainer svg').datum([]);
        chartData.transition().duration(500).call(chart);

        nv.utils.windowResize(chart.update);
        //update api list
        apiFilterList();
        //update developer list
        developerFilter();
        //load graph
        getDateTime(to, from);
        return chart;
    });

});

function developerFilter(){
    jagg.post("/site/blocks/stats/developers-list/ajax/stats.jag",
        {
            "apiFilter" : apiFilter
        },
        function (json) {
        if (!json.error) {
            var developerName = '';
            for (var i = 0; i < json.data.length; i++) {
                developerName += '<option>'+ json.data[i].userId+'</option>'
            }
            $('#developerSelect')
               .append(developerName)
               .selectpicker('refresh');

            $('#developerSelect').on('change', function() {
                selectedDeveloper = this.value;//selected value
                drawAppTime(from, to);
            });
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

function apiFilterList(){
    jagg.post("/site/blocks/stats/apis-list/ajax/stats.jag",
        {
            "apiFilter" : apiFilter
        },
        function (json) {
            if (!json.error) {
                var  apiName = '';
                for ( var i=0; i< json.data.length ; i++){
                    apiName += '<option>'+ json.data[i].apiName+'</option>'
                }
                $('#apiSelect')
                   .append(apiName)
                   .selectpicker('refresh');

                $('#apiSelect').on('change', function() {
                    subscribedApi = this.value;//selected value
                    drawAppTime(from, to);
                });
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

var drawAppTime = function(from, to) {
    jagg.post("/site/blocks/stats/applications-time/ajax/stats.jag" + window.location.search,
        {
            "fromDate": from,
            "toDate": to,
            "developer": selectedDeveloper,
            "subscribedApi": subscribedApi,
            "apiFilter": apiFilter
        },
        function (json) {
        if (!json.error) {
            update_chart(json.data);
        }
        else {
            if (json.message == "AuthenticateError") {
                jagg.showLogin();
            } else {
                jagg.message({content: json.message, type: "error"});
            }
        }
    }, "json");
};

function getDateTime(currentDay, fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + "  <b>to</b> " + toDate[0];
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawAppTime(from,to);
}