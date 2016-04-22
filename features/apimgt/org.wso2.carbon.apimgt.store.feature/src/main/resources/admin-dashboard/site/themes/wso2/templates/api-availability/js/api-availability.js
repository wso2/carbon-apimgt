$( document ).ready(function() {
    var statsEnabled = isDataPublishingEnabled();
    var table = null;

    if(statsEnabled){
        drawTable();
    } else {
        $('.content-data').empty();
        $('.content-data').append('<h2> Stats are disabled.</h2>');
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


    function drawTable() {

        table =$('#apiAvailabilityTable').datatables_extended({
            //"processing": true,
            "serverSide": true,
            "columns" : [
                { title: "Api Version", "orderable": false},
                { title: "Status" , "orderable": false },
            ],
            ajax: {
                "url" : "site/blocks/api-availability/ajax/api-availability.jag",
                "type": "POST",
                "data" : function (d) {
                    d.action = "getDataFromTable";
                    d.searchQuery = null;
                    d.entriesPerPage = $("#apiAvailabilityTable_length option:selected" ).val();
                }
            }
        });

    }
});