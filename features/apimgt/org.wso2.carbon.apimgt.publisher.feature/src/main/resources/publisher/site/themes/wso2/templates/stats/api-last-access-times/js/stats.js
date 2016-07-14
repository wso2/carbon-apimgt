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
                }

                else if (json.usage && json.usage.length == 0 && statsEnabled) {
                    $('.stat-page').html("");
                    showNoDataAnalyticsMsg();
                }

                else{
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
    ]
    } );
}
