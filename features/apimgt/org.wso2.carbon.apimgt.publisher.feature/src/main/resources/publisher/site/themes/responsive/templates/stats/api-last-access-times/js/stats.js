var currentLocation;
var statsEnabled = isDataPublishingEnabled();
    currentLocation = window.location.pathname;


    jagg.post("/site/blocks/stats/api-last-access-times/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {
                if (json.usage && json.usage.length > 0) {
                    var d = new Date();
                    var firstAccessDay = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
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
                           var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                           $("#date-range span").html(dateStr);
                           drawProviderAPIVersionUserLastAccess(from,to);
                        });

                        //setting default date
                        var to = new Date();
                        var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

                        getDateTime(to,from);


                        $('#date-range').click(function (event) {
                        event.stopPropagation();
                        });

                        $('body').on('click', '.btn-group button', function (e) {
                            $(this).addClass('active');
                            $(this).siblings().removeClass('active');
                        });

                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><img src="../themes/responsive/templates/stats/images/statsEnabledThumb.png" alt="Stats Enabled"></div>'));
                }

                else{
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><span class="top-level-warning"><span class="glyphicon glyphicon-warning-sign blue"></span>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/responsive/templates/stats/images/statsThumb.png" alt="Smiley face"></div>'));
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


var drawProviderAPIVersionUserLastAccess = function(from,to){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-last-access-times/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate  },
        function (json) {
            $('#spinner').hide();
            if (!json.error) {
                var length = json.usage.length;
                $('#noData').empty();
                $('div#lastAccessTable_wrapper.dataTables_wrapper.no-footer').remove();

                var $dataTable =$('<table class="display table table-striped table-bordered" width="100%" cellspacing="0" id="lastAccessTable"></table>');

                //getting timezone value
                var date=new Date();
                var offset = date.getTimezoneOffset();

                function convertToHHMM(info) {
                   var hrs = parseInt(Number(info));
                   var min = Math.round((Number(info)-hrs) * 60);
                   return (('' + hrs).length < 2 ? '0' : '') + hrs+':'+(('' + min).length < 2 ? '0' : '')+min;
                }

                var timezone;
                if(offset>=(-840) && offset<=720){
                     if(offset==0 || offset<0){
                        timezone=" (GMT+"+convertToHHMM(Math.abs(offset)/60)+")";
                     }
                     else{
                        timezone=" (GMT-"+ convertToHHMM(Math.abs(offset)/60)+")";
                     }
                }else{
                   timezone=" ";
                }

                $dataTable.append($('<thead class="tableHead"><tr>'+
                                        '<th width="20%">API</th>'+
                                         '<th  width="15%">Version</th>'+
                                        '<th  width="15%">Subscriber</th>'+
                                        '<th  style="text-align:right" width="30%">Access Time'+ timezone+'</th>'+
                                    '</tr></thead>'));

                for (var i = 0; i < json.usage.length; i++) {
                    $dataTable.append($('<tr><td>' + json.usage[i].api_name + '</td><td>' + json.usage[i].api_version + '</td><td>' + json.usage[i].user + '</td><td style="text-align:right" >' + jagg.getDate(json.usage[i].lastAccess)+ '</td></tr>'));
                }
                if (length == 0) {
                    $('#lastAccessTable').hide();
                    $('#noData').html('');
                    $('#noData').append($('<h3 class="no-data-heading center-wrapper">No Data Available</h3>'));

                }else{
                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();
                    $('#lastAccessTable').dataTable({
                         "order": [[ 3, "desc" ]],
                          "fnDrawCallback": function(){
                            if(this.fnSettings().fnRecordsDisplay()<=$("#lastAccessTable_length option:selected" ).val()
                          || $("#lastAccessTable_length option:selected" ).val()==-1)
                            $('#lastAccessTable_paginate').hide();
                            else $('#lastAccessTable_paginate').show();
                          }
                    });
                    $('select').css('width','80px');
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
    jagg.post("/site/blocks/stats/api-last-access-times/ajax/stats.jag", { action: "isDataPublishingEnabled"},
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

function btnActiveToggle(button){
    $(button).siblings().removeClass('active');
    $(button).addClass('active');
}

function getDateTime(currentDay,fromDay){
    var to = convertTimeString(currentDay);
    var from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-range span").html(dateStr);
    $('#date-range').data('daterangepicker').setStartDate(from);
    $('#date-range').data('daterangepicker').setEndDate(to);
    drawProviderAPIVersionUserLastAccess(from,to);
}