$( document ).ready(function() {      

    //entriesPerPage default value
    var count = 10;
    var searchEntry = null;
    var startIndex = 0;
    
    loadAlertTable(); 
    $('#alertSelected').change(function(){
        loadAlertTable(); 
    });

 
    $('#entriesPerPage').change(function(){
        count = $('#entriesPerPage option:selected').text();
        loadAlertTable(); 
    });

    $("#search_field").on("keydown",function search(e) {
        if(e.keyCode == 13) {
            var lucenePrefix = "";
            searchEntry = $(this).val();
            if(searchEntry.indexOf(":") == -1 && searchEntry !=""){
                lucenePrefix = "msg:";
            }
            searchEntry = lucenePrefix+searchEntry;
            loadAlertTable();
        }
    });

    
    if (($.cookie("selectedTab") != null)) {
        $.cookie("selectedTab", null);
    }

    $('.pagination a').click(function(ex){
      ex.preventDefault();
      var navigatedPage = parseInt(getParameterByName('page', this));
      startIndex = ((navigatedPage-1)*count) + 1;
      loadAlertTable();
    });
    

    function getParameterByName(name, url) {
       if (!url) url = window.location.href;
       name = name.replace(/[\[\]]/g, "\\$&");
       var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)", "i"),
           results = regex.exec(url);
       if (!results) return null;
       if (!results[2]) return '';
       return decodeURIComponent(results[2].replace(/\+/g, " "));
    }

    //jagg.post("/site/blocks/alert-table/ajax/alert-table.jag",{action:"getCountFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM",searchQuery:searchEntry}, function(json){},"json");
  	
    function loadAlertTable(){
    	
       	var selectedOption = $("#alertSelected option:selected" ).val();
        
        if(selectedOption == 1){
        	//result = loadData("ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM"); 
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM", entriesPerPage: count , searchQuery:searchEntry, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
                
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
                
                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["consumerKey"]+'</td><td>'+result["userid"]+'</td><td>'+ result["msg"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
                  
        } else if(selectedOption == 2){
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ALERT_UNUSUALIPACCESS", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
                
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">IP</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
            
                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];;
                    $('#alertsHistory').append('<tr><td>'+ result["consumerKey"]+'</td><td>'+result["userId"]+'</td><td>'+ result["msg"]+'</td><td>'+ result["ip"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }
            }
            ,"json");
        } else if(selectedOption == 3){
             jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ABNORMALREQUESTSPERMINALERTSTREAM", entriesPerPage: count,startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
                
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="userSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Resouce Template</th><th class="userSpecialCell">Method</th><th class="userSpecialCell">RequestPerMin</th><th class="userSpecialCell">Upper Percantile</th><<th class="userSpecialCell">Lower Percentile</th><th class="userSpecialCell">Reason</th>th class="appSpecialCell">Message</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
                
                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["consumerKey"]+'</td><td>'+result["userId"]+'</td><td>'+ result["msg"]+'</td><td>'+ result["api"]+'</td><td>'+ result["resourceTemplate"]+'</td><td>'+ result["method"]+'</td><td>'+ result["requestPerMin"]+'</td><td>'+ result["requestsPerMinUpperPercentile"]+'</td><td>'+ result["requestsPerMinLowerPercentile"]+'</td><td>'+ result["reason"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        } else if(selectedOption == 4){
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ABNORMALRESPONSETIMEALERTSTREAM", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;

                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Message</th><th class="appSpecialCell">API</th><th class="appSpecialCell">Tenant Domain</th><th class="userSpecialCell">Resource Template</th><th class="userSpecialCell">Method</th><th class="userSpecialCell">Response Time</th><th class="userSpecialCell">Response Percentile</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["msg"]+'</td><td>'+result["api"]+'</td><td>'+ result["tenantDomain"]+'</td><td>'+ result["resourceTemplate"]+'</td><td>'+ result["method"]+'</td><td>'+ result["responseTime"]+'</td><td>'+ result["responsePercentile"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        } else if(selectedOption == 5){
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 6){
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTOKENREFRESH", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
               
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Client ID</th><th class="userSpecialCell">Scope</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["user"]+'</td><td>'+result["msg"]+'</td><td>'+ result["clientId"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        } else if(selectedOption == 7){
             jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTOKENREFRESH", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
            
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Application ID</th><th class="userSpecialCell">Application Name</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["userId"]+'</td><td>'+result["message"]+'</td><td>'+ result["api"]+'</td><td>'+ result["applicationId"]+'</td><td>'+ result["applicationName"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        } else if(selectedOption == 8){
             jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_ABNORMALBACKENDTIMEALERTSTREAM", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
                
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="appSpecialCell">Tenant Domain</th><th class="appSpecialCell">Resource Template</th><th class="appSpecialCell">Method</th><th class="appSpecialCell">Backend Time</th><th class="appSpecialCell">Backend Percentile</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["msg"]+'</td><td>'+result["api"]+'</td><td>'+ result["tenantDomain"]+'</td><td>'+ result["resourceTemplate"]+'</td><td>'+ result["method"]+'</td><td>'+ result["backendTime"]+'</td><td>'+ result["backendPercentile"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        } else if(selectedOption == 9){

            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:"ORG_WSO2_ANALYTICS_APIM_APIHEALTHMONITORSTREAM", entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
            
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">API Version</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["api_version"]+'</td><td>'+result["msg"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
        }else {
            $('#alertsHistory').empty();
        }


       // var heightOfTable = $('#table_div').css('height');

       // console.log(heightOfTable);

   }

  
   

 
	        
});

