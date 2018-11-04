var currentLocation;
var apiFilter = "allAPIs";
var statsEnabled = isDataPublishingEnabled();
currentLocation = window.location.pathname;

//setting default date
var to;
var from;

jagg.post("/site/blocks/stats/api-last-access-times/ajax/stats.jag", { action: "getFirstAccessTime", currentLocation: currentLocation  },
        function (json) {

            if (!json.error) {
                if (json.usage && json.usage.length > 0) {
                    from = new Date(json.usage[0].year, json.usage[0].month - 1, json.usage[0].day);
                    to = new Date();

                        $("#apiFilter").change(function (e) {
                        	apiFilter = this.value;
                            var table = $('#lastAccessTable').DataTable();
                            table.ajax.reload();
                        });
                        $('body').on('click', '.btn-group button', function (e) {
                            $(this).addClass('active');
                            $(this).siblings().removeClass('active');
                        });
                        drawProviderAPIVersionUserLastAccess();
                } else {
                    $('.stat-page').html("");
                    showEnableAnalyticsMsg();
                }
            } else {
                if (json.message == "AuthenticateError") {
                    jagg.showLogin();
                } else {
                    jagg.message({content: json.message, type: "error"});
                }
            }

        }, "json");


var drawProviderAPIVersionUserLastAccess = function() {
    $('#spinner').hide();
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
    $('#tableContainer').append($dataTable);

    $('#lastAccessTable').DataTable( {
    "processing": true,
    "serverSide": true,
    "searching": false,
    "ajax": {
        "url": "../../site/blocks/stats/api-last-access-times/ajax/stats.jag",
        "data": function ( d ) {
            d.action = "getProviderAPIVersionUserLastAccess";
            d.currentLocation = currentLocation;
            d.fromDate = from;
            d.toDate = to;
            d.apiFilter = apiFilter;
        }
    },
    "columns": [
        { "data": "apiName" },
        { "data": "apiVersion" },
        { "data": "user" },
        { "data": "lastAccessTime" }
    ],
        "columnDefs": [{
            "targets": 3,
            "render": function (data, type, full, meta) {
                var accessTimeInUTC = new Date(Number(data)); // conversion assumes server time is in current time zone
                var accessTime = formatTimeIn12HourFormat(accessTimeInUTC);
                return accessTime;
            }
        }]
    } );

    /**
     * Format Time into MM/DD/YY hh:mm format
     * @param date
     * @returns {string}
     */
    function formatTimeIn12HourFormat(date) {
        var year = date.getFullYear();
        year = year < 2000 ? (year + 100) : year;
        var hours = date.getHours();
        var minutes = date.getMinutes();
        var ampm = hours >= 12 ? 'pm' : 'am';
        hours = hours % 12;
        hours = hours ? hours : 12; // the hour '0' should be '12'
        minutes = minutes < 10 ? '0' + minutes : minutes;
        var strTime = formatTimeChunk((date.getMonth() + 1)) + "/" + formatTimeChunk(date.getDate()) + "/"
            + year + ", " + hours + ':' + minutes + ' ' + ampm;
        return strTime;
    }
}
