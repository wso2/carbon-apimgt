var currentLocation;
var apiFilter = "allAPIs";
var statsEnabled = isDataPublishingEnabled();
currentLocation = window.location.pathname;

//setting default date
var to = new Date();
var from = new Date(to.getTime() - 1000 * 60 * 60 * 24 * 30);

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

                        $('#date-pick').click(function(){
                             $(this).removeClass('active');
                        });

                        //date picker
                        $('#date-pick').daterangepicker({
                              timePicker: true,
                              timePickerIncrement: 30,
                              format: 'YYYY-MM-DD h:mm',
                              opens: 'left',
                        });
                        
                        $("#apiFilter").change(function (e) {
                        	apiFilter = this.value;
                        	drawProviderAPIVersionUserLastAccess(from,to,apiFilter);
                        });

                        $('#date-pick').on('apply.daterangepicker', function(ev, picker) {
                           btnActiveToggle(this);
                           from = convertTimeString(picker.startDate);
                           to = convertTimeString(picker.endDate);
                           var fromStr = from.split(" ");
                           var toStr = to.split(" ");
                           var dateStr = fromStr[0] + " <i>" + fromStr[1] + "</i> <b>to</b> " + toStr[0] + " <i>" + toStr[1] + "</i>";
                           $("#date-pick span").html(dateStr);
                           drawProviderAPIVersionUserLastAccess(from,to,apiFilter);
                        });                        

                        getDateTime(to,from);


                        $('#date-pick').click(function (event) {
                        event.stopPropagation();
                        });

                        $('body').on('click', '.btn-group button', function (e) {
                            $(this).addClass('active');
                            $(this).siblings().removeClass('active');
                        });

                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><img src="../themes/wso2/templates/images/statsEnabledThumb.png" alt="Stats Enabled"></div>'));
                }

                else{
                    $('.stat-page').html("");
                    $('.stat-page').append($('<br><div class="errorWrapper"><span class="top-level-warning"><span class="glyphicon glyphicon-warning-sign blue"></span>'
                        +i18n.t('errorMsgs.checkBAMConnectivity')+'</span><br/><img src="../themes/wso2/images/statsThumb.png" alt="Smiley face"></div>'));
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


var drawProviderAPIVersionUserLastAccess = function(from,to,apiFilter){
    var fromDate = from;
    var toDate = to;
    jagg.post("/site/blocks/stats/api-last-access-times/ajax/stats.jag", { action:"getProviderAPIVersionUserLastAccess",currentLocation:currentLocation,fromDate:fromDate,toDate:toDate,apiFilter:apiFilter  },
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
                    $dataTable.append($('<tr><td>' + json.usage[i].apiName + '</td><td>' + json.usage[i].apiVersion + '</td><td>' + json.usage[i].user + '</td><td style="text-align:right" >' + jagg.getDate(json.usage[i].lastAccessTime)+ '</td></tr>'));
                }
                if (length == 0) {
                    $('#lastAccessTable').hide();
                    $('div#lastAccessTable_wrapper.dataTables_wrapper.no-footer').remove();
                    $('#noData').html('');
                    $('#noData').append($('<div class="center-wrapper"><div class="col-sm-4"/><div class="col-sm-4 message message-info"><h4><i class="icon fw fw-info"></i>No Data Available.</h4></div></div>'));

                }else{
                    $('#tableContainer').append($dataTable);
                    $('#tableContainer').show();
                    $('#lastAccessTable').datatables_extended({
                         "order": [[ 3, "desc" ]],
                          "fnDrawCallback": function(){
                            if(this.fnSettings().fnRecordsDisplay()<=$("#lastAccessTable_length option:selected" ).val()
                          || $("#lastAccessTable_length option:selected" ).val()==-1)
                            $('#lastAccessTable_paginate').hide();
                            else $('#lastAccessTable_paginate').show();
                          }
                    });
                    //$('select').css('width','80px');
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
function getDateTime(currentDay,fromDay){
    to = convertTimeString(currentDay);
    from = convertTimeString(fromDay);
    var toDate = to.split(" ");
    var fromDate = from.split(" ");
    var dateStr= fromDate[0]+" <i>"+fromDate[1]+"</i> <b>to</b> "+toDate[0]+" <i>"+toDate[1]+"</i>";
    $("#date-pick span").html(dateStr);
    $('#date-pick').data('daterangepicker').setStartDate(from);
    $('#date-pick').data('daterangepicker').setEndDate(to);
    drawProviderAPIVersionUserLastAccess(from,to,apiFilter);
}