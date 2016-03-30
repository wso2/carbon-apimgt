$( document ).ready(function() {      

    loadAlertTable(); 
    $('#alertSelected').change(function(){
        loadAlertTable(); 
    });
 
    
 
  	
    function loadAlertTable(){
    	
       	var selectedOption = $("#alertSelected option:selected" ).val();
        
        if(selectedOption == 1){
        	//ajaxResult = loadData("ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM");  
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable",}, function(json){ 
              var  ajaxResulttemp = json;
              console.log(ajaxResulttemp["values"].length);
              ajaxResult = ajaxResulttemp["values"][0]["values"];
                console.log(ajaxResult);
            $('#alertsHistory').empty();
            $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
            $('#alertsHistory').append('<tr><td>'+ ajaxResult["consumerKey"]+'</td><td>'+ajaxResult["userid"]+'</td><td>'+ ajaxResult["message"]+'</td><td>'+ ajaxResult["alertTimestamp"]+'</td></tr>');
           
            }
  ,"json");
             
           
        } else if(selectedOption == 2){
            $('#alertsHistory').empty();
            $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">IP</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
            
        } else if(selectedOption == 3){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Consumer Key</th><th class="appSpecialCell">Userid</th><th class="userSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Resouce Template</th><th class="userSpecialCell">Method</th><th class="userSpecialCell">RequestPerMin</th><th class="userSpecialCell">Upper Percantile</th><<th class="userSpecialCell">Lower Percentile</th><th class="userSpecialCell">Reason</th>th class="appSpecialCell">Message</th><th class="userSpecialCell">IP</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 4){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="userSpecialCell">Message</th><th class="appSpecialCell">API</th><th class="appSpecialCell">Tenant Domain</th><th class="userSpecialCell">Resource Template</th><th class="userSpecialCell">Method</th><th class="userSpecialCell">Response Time</th><th class="userSpecialCell">Response Percentile</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 5){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 6){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Client ID</th><th class="userSpecialCell">Client ID</th><th class="userSpecialCell">Scope</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 7){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Application ID</th><th class="userSpecialCell">Application Name</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 8){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="appSpecialCell">Tenant Domain</th><th class="appSpecialCell">Resource Template</th><th class="appSpecialCell">Method</th><th class="appSpecialCell">Backend Time</th><th class="appSpecialCell">Backend Percentile</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        } else if(selectedOption == 9){
            $('#alertsHistory').empty();
             $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">API Version</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');
        }else {
            $('#alertsHistory').empty();
        }

   }

 
	        
});
