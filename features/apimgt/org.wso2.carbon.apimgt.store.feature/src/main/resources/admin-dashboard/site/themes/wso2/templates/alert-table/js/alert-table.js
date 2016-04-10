$( document ).ready(function() { 

    var statsEnabled = isDataPublishingEnabled();

    
    var selectedOptionKey = $("#alertSelected option:selected" ).val();
    var tableName = null;
    var table = null;
    
    changeActiveTableName();

    $("#alertSelected").change(function(){
         selectedOptionKey = $("#alertSelected option:selected" ).val();
         changeActiveTableName();
         if(statsEnabled){
            table.ajax.reload(null, true);
         } else{
            alert('stats not enabled');
         } 
    })
    

    function changeActiveTableName(){
        if(selectedOptionKey == 1){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALLAPIMALERTSSTREAM"
        } else if(selectedOptionKey== 2){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_UNUSUALIPACCESS";
        } else if(selectedOptionKey== 3){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALREQUESTSPERMINALERTSTREAM";
        } else if(selectedOptionKey== 4){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALRESPONSETIMEALERTSTREAM";
        } else if(selectedOptionKey== 5){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTIERUSAGEALERT";
        } else if(selectedOptionKey== 6){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTOKENREFRESH";
        } else if(selectedOptionKey== 7){
           tableName = "ORG_WSO2_ANALYTICS_APIM_TIERLIMITHITTINGALERT";
        } else if(selectedOptionKey== 8){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALBACKENDTIMEALERTSTREAM";
        } else if(selectedOptionKey== 9){
            tableName = "ORG_WSO2_ANALYTICS_APIM_APIHEALTHMONITORALERTSTREAM";
        } else if(selectedOptionKey== 10){
            tableName = "ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM";
        } 

    }

    function isDataPublishingEnabled(){
    jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "isDataPublishingEnabled"},
        function (json) {
            if (!json.error) {
                statsEnabled = json.usage;
                if(statsEnabled){
                    drawTable();
                } else {
                    alert('stats not enabled');
                }
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

    function changeSelectedAlertType(alertType){
        //var tableName = getParameterByName('tableName');
        if(alertType != null){
            if(alertType == "Unusual IP Access"){
                 $('#alertSelected').val(2).change();
            } else if(alertType == "Abnormal Request Count"){
                 $('#alertSelected').val(3).change();
            } else if(alertType == "Abnormal Response Time"){
                 $('#alertSelected').val(4).change();
            } else if(alertType == "Abnormal Tier Usage"){
                 $('#alertSelected').val(5).change();
            } else if(alertType == "Abnormal Token Refresh"){
                 $('#alertSelected').val(6).change();
            } else if(alertType == "Tier Crossing"){
                 $('#alertSelected').val(7).change();
            } else if(alertType == "Abnormal Backend Time"){
                 $('#alertSelected').val(8).change();
            } else if(alertType == "API Health Monitor"){
                 $('#alertSelected').val(9).change();
            } else if(alertType == "Request Pattern Change"){
                $('#alertSelected').val(10).change();
            }     
        }   
    }

    function drawTable(){
    
        table =$('#alertHistoryTable').DataTable({
                        //"processing": true,
                        "serverSide": true,
                        "columns" : [
                                { title: "Alert Timestamp" },
                                { title: "Type", "orderable": false },
                                { title: "Message" , "orderable": false },
                            ],                       
                        ajax: {
                            "url" : "site/blocks/alert-table/ajax/alert-table.jag",
                            "type": "POST",
                            "data" : function (d) {
                                d.action = "getDataFromAlertTable",
                                d.tableName = tableName;
                                d.searchQuery = null;
                                d.entriesPerPage = $("#alertHistoryTable_length option:selected" ).val();
                            }
                        },
                        "drawCallback": function(){
                                $("thead").addClass("tableHead");
                                $('.alertTypeLink').click(function(){
                                    changeSelectedAlertType(this.text);
                                });
  
                        }
                    });

        }
    
   
});
     