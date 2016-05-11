var chart;
var chartData;
var apiFilter = "allAPIs";

function update_chart(data) {
    // Update the SVG with the new data and call chart
    chartData.datum(data).transition().duration(500).call(chart);
    nv.utils.windowResize(chart.update);
};

$(document).ready(function(){
    chart = "";
    chartData = "";
    /* Load date range picker */
    $('#date-range').daterangepicker({
        timePicker: true,
        timePickerIncrement: 30,
        format: 'YYYY-MM-DD h:mm',
        startDate: moment().subtract(1, 'month'),
        endDate: moment().add(1, 'day').format('YYYY-MM-DD  h:mm'),
        opens: 'left',
    });

    /***
     * Filter controllers.
     */
    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
       $("body").trigger("update_chart");
    });

    //day picker
    $('#today-btn').on('click',function(){
        $('#date-range').data('daterangepicker').setStartDate(moment().startOf('day'));
        $('#date-range').data('daterangepicker').setEndDate(moment().format('YYYY-MM-DD  h:mm'));
        $("body").trigger("update_chart");
    });

    //hour picker
    $('#hour-btn').on('click',function(){
        $('#date-range').data('daterangepicker').setStartDate(moment().startOf('hour'));
        $('#date-range').data('daterangepicker').setEndDate(moment().format('YYYY-MM-DD  h:mm'));
        $("body").trigger("update_chart");
    })

    //week picker
    $('#week-btn').on('click',function(){
        $('#date-range').data('daterangepicker').setStartDate(moment().startOf('week'));
        $('#date-range').data('daterangepicker').setEndDate(moment().format('YYYY-MM-DD  h:mm'));
        $("body").trigger("update_chart");
    })

    //month picker
    $('#month-btn').on('click',function(){
        $('#date-range').data('daterangepicker').setStartDate(moment().startOf('month'));
        $('#date-range').data('daterangepicker').setEndDate(moment().format('YYYY-MM-DD  h:mm'));
        $("body").trigger("update_chart");
    });

    $("#apiFilter").change(function (e) {
        $('#developerSelect').empty();
        $('#developerSelect').append('<option> All </option>');
        apiFilter = this.value;
        developerFilter();
    });

    nv.addGraph(function () {
        chart = nv.models.lineChart()
            .margin({right: 40, left: 75})
            //.useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
            .transitionDuration(350)  //how fast do you want the lines to transition?
            .showLegend(true)       //Show the legend, allowing users to turn on/off line series.
            .showYAxis(true)        //Show the y-axis
            .showXAxis(true) ;       //Show the x-axis

        chart.xAxis.axisLabel('Time')
        .tickFormat(function (d) {
             return d3.time.format('%m/%d %H:%M')(new Date(d)) });

        chart.yAxis.axisLabel('Application Count')
            .tickFormat(d3.format('d'));

        chart.tooltipContent(function(key, x, y, e, graph) {
            return '<p><b>'+x+'</b> -' + key + ': ' + y + '</p>'
        });

         chart.lines.dispatch.on('elementClick', function(e) {
             //alert("You've clicked on " + e.series.key + " - " + e.point.x);
             window.location = "/analytics/site/pages/all-statistics.jag?page=apis-list&stat=all-stat";
         });

        // Assign the SVG selction
        chartData = d3.select('#chartContainer svg').datum([]);
        chartData.transition().duration(500).call(chart);

        nv.utils.windowResize(chart.update);
        return chart;
    });

    function developerFilter(){
        jagg.post("/site/blocks/stats/developers-list/ajax/stats.jag",
            {
                "apiFilter": apiFilter
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
                $("body").trigger("update_chart");

                $('#developerSelect').on('change', function() {
                    selectedDeveloper = this.value;//selected value
                    $("body").trigger("update_chart");
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

    //update developer list
    developerFilter();

    $("body").on("update_chart",function(){
        jagg.post("/site/blocks/stats/apis-time/ajax/stats.jag" + window.location.search,
            {
               "fromDate": $('#date-range').data('daterangepicker').startDate.format('YYYY-MM-DD'),
               "toDate": $('#date-range').data('daterangepicker').endDate.format('YYYY-MM-DD'),
               "developer": $('#developerSelect').val(),
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
    });
});


