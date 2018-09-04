var currentLocation;
currentLocation=window.location.pathname;
var statsEnabled = isDataPublishingEnabled();

    currentLocation=window.location.pathname;

    jagg.post("/site/blocks/stats/faultCount/ajax/stats.jag", { action:"getFirstAccessTime",currentLocation:currentLocation  },
        function (json) {
            if (!json.error) {
                if( json.usage && json.usage.length > 0){
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month, json.usage[0].day);
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

                    $('#date-range').on('apply.daterangepicker', function(ev, picker) {
                       btnActiveToggle(this);
                       var from = convertTimeString(picker.startDate);
                       var to = convertTimeString(picker.endDate);
                       var fromStr = from.split(" ");
                       var toStr = to.split(" ");
                       var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>" + i18n.t("to") + "</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                       $("#date-range span").html(dateStr);
                       drawAPIResponseFaultCountTable(from,to);
                    });

                    //setting default date
                    var to = new Date();
                    var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                    getDateTime(to,firstAccessDay);

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
                    jagg.message({content:json.message,type:"error"});
                }
            }
        }, "json");

$(document).ready(function(){
    $(document).scroll(function(){
        var top=$(document).scrollTop();
        var width = $("#rangeSliderWrapper").width();
        if(top > 180){
            $("#rangeSliderWrapper").css("position","fixed").css("top","50px").width(width);
        }else{
           $("#rangeSliderWrapper").css({ "position": "relative", "top": "0px" }); 
        }
    })
})

var dt = false;
var drawAPIResponseFaultCountTable = function(from,to){

    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/faultCount/ajax/stats.jag", { action:"getPerAppAPIFaultCount",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {

                $('#PerAppAPIFaultCountTable').find("tr:gt(0)").remove();
                var length = json.usage.length;
                if( length > 0){

                    $('#noData').addClass('hide');
                    $('#tableContainer').removeClass('hide');
                    $('#PerAppAPIFaultCountTable').removeClass('hide');
                    $('#PerAppAPIFaultCountTable_wrapper').removeClass('hide');

                    for (var i = 0; i < json.usage.length; i++) {
                        var k = json.usage[i].apiCountArray[0].apiName;
                        $('#PerAppAPIFaultCountTable').append($('<tr><td>' + json.usage[i].appName + '</td><td>' +json.usage[i].apiCountArray[0].apiName + '</td><td class="tdNumberCell">' + json.usage[i].apiCountArray[0].count + '</td></tr>'));
                         if(json.usage[i].apiCountArray.length > 1){
                             for (var j =1 ; j < json.usage[i].apiCountArray.length; j++) {
                                 $('#PerAppAPIFaultCountTable').append($('<tr><td>' + json.usage[i].appName + '</td><td>' + json.usage[i].apiCountArray[j].apiName + '</td><td class="tdNumberCell">' + json.usage[i].apiCountArray[j].count + '</td></tr>'));
                             }
                         }
                    }
                    if(dt != false) {
                        dt.destroy();
                    }
                    dt = $('#PerAppAPIFaultCountTable').datatables_extended();
                }
                else if(length == 0) {
                    $('#noData').removeClass('hide');
                    $('#PerAppAPIFaultCountTable').addClass('hide');
                    $('#PerAppAPIFaultCountTable_wrapper').addClass('hide');
                }

            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content:json.message,type:"error"});
                }
            }
        }, "json");
}

function isDataPublishingEnabled(){
    jagg.post("/site/blocks/stats/faultCount/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

var convertTimeStringPlusDay = function(date){
    var d = new Date(date);
    var formattedDate = d.getFullYear() + "-" + formatTimeChunk((d.getMonth()+1)) + "-" + formatTimeChunk(d.getDate()+1);
    return formattedDate;
};

var formatTimeChunk = function (t){
    if (t<10){
        t="0" + t;
    }
    return t;
};

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0] + " <i>" + fromDate[1] + "</i> <b>to</b> " + toDate[0] + " <i>" + toDate[1] + "</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawAPIResponseFaultCountTable(from,to);
}

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}