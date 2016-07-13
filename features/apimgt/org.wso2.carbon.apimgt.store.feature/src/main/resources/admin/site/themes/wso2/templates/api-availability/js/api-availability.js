$( document ).ready(function() {
    var statsEnabled = isDataPublishingEnabled();
    var table = null;

    if(statsEnabled){
        drawTable();
    } else {
        $('.content-data').empty();
        $('.content-data').append('<h2>' + i18n.t("Stats are disabled.") + '</h2>');
    }

    function isDataPublishingEnabled() {
        var isStatsEnabled;

        jagg.syncPost("/site/blocks/api-availability/ajax/api-availability.jag", { action: "isDataPublishingEnabled"},
            function (json) {
                if (!json.error) {
                    isStatsEnabled = json.usage;
                } else {
                    if (json.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content: json.message, type: "error"});
                    }
                }
            }, "json");

        return isStatsEnabled;
    }

    $('#apiAvailabilityTable_filter input').unbind();
    $('#apiAvailabilityTable_filter input').bind('keyup', function(e) {
        if(e.keyCode == 13) {
            table.search( this.value ).draw();
        }});



    function drawTable() {

        table =$('#apiAvailabilityTable').datatables_extended({
            //"processing": true,
            "serverSide": true,
            "columns" : [
                { title: "Api Version"},
                { title: "Status" , "orderable": false , "render":function(data){
                    if(data == "Available"){
                        return '<i class="icon fw fw-success text-success add-margin-right-1x"></i>' + data;
                    } else {
                        return '<i class="icon fw fw-disabled text-muted add-margin-right-1x"></i>' + data;
                    }
                }
                },
            ],
            ajax: {
                "url" : "site/blocks/api-availability/ajax/api-availability.jag",
                "type": "POST",
                "data" : function (d) {
                    d.action = "getDataFromTable";
                    d.searchQuery = null;
                    d.entriesPerPage = $("#apiAvailabilityTable_length option:selected" ).val();
                },
                error:function(xhr,status,error){
                    console.log('Error while trying to connect to the DAS endpoint');
                }
            }

        });

    }
});