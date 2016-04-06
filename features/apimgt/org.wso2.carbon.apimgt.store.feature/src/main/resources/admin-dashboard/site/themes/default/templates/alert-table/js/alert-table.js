$( document ).ready(function() {  

    changeSelectedOption();
    
    //default values
    var count = 10;
    var searchEntry = null;
    var startIndex = 0;
    var currentPage = 1;
    
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
    
    function changeSelectedOption(){
        var tableName = getParameterByName('tableName');
        if(tableName != null){
            if(tableName == "RequestPatternChanged"){
                $('#alertSelected').val(1).change();
            } else if(tableName == "UnusualIPAccessAlert"){
                 $('#alertSelected').val(2).change();
            } else if(tableName == "abnormalRequestsPerMin"){
                 $('#alertSelected').val(3).change();
            } else if(tableName == "abnormalResponseTime"){
                 $('#alertSelected').val(4).change();
            } else if(tableName == "AbnormalTierUsage"){
                 $('#alertSelected').val(5).change();
            } else if(tableName == "AbnormalRefreshAlert"){
                 $('#alertSelected').val(6).change();
            } else if(tableName == "FrequentTierHittingAlert"){
                 $('#alertSelected').val(7).change();
            } else if(tableName == "abnormalBackendTime"){
                 $('#alertSelected').val(8).change();
            } else if(tableName == "healthAvailabilityPerMin"){
                 $('#alertSelected').val(9).change();
            }      
        }   
    }

    function getParameterByName(name, url) {
       if (!url) url = window.location.href;
       name = name.replace(/[\[\]]/g, "\\$&");
       var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)", "i"),
           results = regex.exec(url);
       if (!results) return null;
       if (!results[2]) return '';
       return decodeURIComponent(results[2].replace(/\+/g, " "));
    }


    function generatePagination(rowCount,count){

        numberOfPages = Math.ceil(rowCount / count );     
          $.ajax({
            url: "site/blocks/alert-table/ajax/alert-table-pagination.jag",
            data : { currentPage : currentPage, numberOfPages : numberOfPages }, 
            success: function(result){
            $('#pagination-div').html(result);
            $('.pagination a').click(function(ex){
              ex.preventDefault();
              var navigatedPage = parseInt(getParameterByName('page', this));
              startIndex = ((navigatedPage-1)*count) + 1;
              currentPage = navigatedPage;
              loadAlertTable();
            });
          }});        
    }

    function getRowCountFromTable(tableName,searchQuery){
        jagg.post("/site/blocks/alert-table/ajax/alert-table.jag",{action:"getCountFromAlertTable", tableName:tableName,searchQuery:searchQuery}, function(json){
            var tempResult = json;
            var rowCount = tempResult["values"];
            generatePagination(rowCount,count);
        },"json");
    }
  	
    function loadAlertTable(){

       	var selectedOption = $("#alertSelected option:selected" ).val();
        
        if(selectedOption == 1){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count , searchQuery:searchEntry, startIndex:startIndex}, function(json){ 
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
            getRowCountFromTable(tableName,searchEntry);
                  
        } else if(selectedOption == 2){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_UNUSUALIPACCESS";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
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
             getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 3){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALREQUESTSPERMINALERTSTREAM";
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
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 4){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALRESPONSETIMEALERTSTREAM";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
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
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 5){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTIERUSAGEALERT";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">Userid</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API Version</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                 for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["userId"]+'</td><td>'+result["msg"]+'</td><td>'+ result["api_version"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 6){
            
            var tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTOKENREFRESH";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
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
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 7){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_TIERLIMITHITTINGALERT";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
                var tempResult = json;
                var rowCount = tempResult["values"].length;
            
                $('#alertsHistory').empty();
                $('#alertsHistory').append('<thead id="alertsHistoryHeader"> <tr><th class="appSpecialCell">API Publisher</th><th class="appSpecialCell">Message</th><th class="userSpecialCell">API</th><th class="userSpecialCell">Application ID</th><th class="userSpecialCell">Application Name</th><th class="userSpecialCell">Alert Timestamp</th></tr>  </thead> ');

                for(i=0;i<rowCount;i++){
                    var result = tempResult["values"][i]["values"];
                    $('#alertsHistory').append('<tr><td>'+ result["apiPublisher"]+'</td><td>'+result["msg"]+'</td><td>'+ result["api"]+'</td><td>'+ result["applicationId"]+'</td><td>'+ result["applicationName"]+'</td><td>'+ new Date(result["alertTimestamp"])+'</td></tr>');
                }                           
            }
            ,"json");
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 8){

            var tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALBACKENDTIMEALERTSTREAM";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
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
            getRowCountFromTable(tableName,searchEntry);

        } else if(selectedOption == 9){
            
            var tableName = "ORG_WSO2_ANALYTICS_APIM_APIHEALTHMONITORSTREAM";
            jagg.post("/site/blocks/alert-table/ajax/alert-table.jag", { action: "getDataFromAlertTable", tableName:tableName, entriesPerPage: count, startIndex:startIndex}, function(json){ 
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
            getRowCountFromTable(tableName,searchEntry);

        }else {
            $('#alertsHistory').empty();
        }
   }        
});

