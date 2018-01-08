var chart;
var chartData;
var apiFilter = "allAPIs";
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
        drawApiTime(from,to);
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
        $('#developerSelect').empty();
        $('#developerSelect').append('<option> All </option>');
        apiFilter = this.value;
        developerFilter();
    });

    $('body').on('click', '.btn-group button', function (e) {
        $(this).addClass('active');
        $(this).siblings().removeClass('active');
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
        .rotateLabels(-20)
        .tickFormat(function (d) {
             return d3.time.format('%m/%d %H:%M')(new Date(d)) });

        chart.yAxis.axisLabel(i18n.t('Created API Count'))
            .tickFormat(d3.format('d'));

        chart.tooltipContent(function(key, x, y, e, graph) {
            return '<p><b>'+x+'</b> -' + key + ': ' + y + '</p>'
        });

        // Assign the SVG selction
        chartData = d3.select('#chartContainer svg').datum([]);
        chartData.transition().duration(500).call(chart);

        nv.utils.windowResize(chart.update);
        developerFilter();
        return chart;
    });

});

function developerFilter(){
    jagg.post("/site/blocks/stats/creator-list/ajax/stats.jag",
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
                getDateTime(to, from);

                $('#developerSelect').on('change', function() {
                    selectedDeveloper = this.value;//selected value
                    drawApiTime(from, to);
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

var drawApiTime = function(from, to) {
    jagg.post("/site/blocks/stats/apis-time/ajax/stats.jag" + window.location.search,
        {
           "fromDate": from,
           "toDate": to,
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
};

function getDateTime(currentDay, fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <b>to</b> " + toDate[0];
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawApiTime(from,to);
}
