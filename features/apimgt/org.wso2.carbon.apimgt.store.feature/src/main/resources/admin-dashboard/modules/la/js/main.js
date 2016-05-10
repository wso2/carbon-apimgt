/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var serverUrl = "https://"+location.hostname +":"+ location.port +"/admin-dashboard/modules/la/log-analyzer-proxy.jag";
var client = new AnalyticsClient().init(null, null, serverUrl);
var logLineArray = [];
var template = "<ul class='template3' style='list-style-type:none' >{{#arr}}<li class='log'>{{log}}</li>{{/arr}}</ul>";
var initialRecordCount = -1;
var currentRecordCount;

$(document).ready(function () {
     if(analyticsEnabled){
        fetchInitialRecordCount();
     }
});

/**
 * This method fetches the initial record count in the LOGANALYZER table and if it is successful,
 * sets the data fetching operation
 *
 */
function fetchInitialRecordCount(){
        var countQueryInfo = {
                tableName: "LOGANALYZER",
                searchParams: {
                    query: "logstream:\"" + tenantId + "\"",
                }
            };
        client.searchCount(countQueryInfo, function(count) {
              if (count["status"] === "success"){
                initialRecordCount = count["message"];
                if(initialRecordCount >= 0){
                    setInterval(fetchCurrentRecordCount, 5000);
                }
              }
              return initialRecordCount;
        }, function(error) {
              console.log("error occured: " + error);
        });
}

/**
 * This method fetches the current record count in the LOGANALYZER table and if there are any
 * new records, fetches the excess data.
 *
 */
function fetchCurrentRecordCount() {

    var countQueryInfo = {
            tableName: "LOGANALYZER",
            searchParams: {
                query: "logstream:\"" + tenantId + "\"",
            }
        };
    client.searchCount(countQueryInfo, function(count) {
          currentRecordCount = count["message"];
          var logCountDifference = currentRecordCount - initialRecordCount;
          if(logCountDifference > 0){
            fetchRecords(logCountDifference);
          }
    }, function(error) {
          console.log("error occured: " + error);
    });
}

/**
 * This method fetches the newly added data to the table
 *
 */
function fetchRecords(logCountDifference){
    initialRecordCount = currentRecordCount;
    logLineArray.length = 0;
    var queryInfo;
    queryInfo = {
        tableName: "LOGANALYZER",
        searchParams: {
            query: "logstream:\"" + tenantId + "\"",
            start: 0,
            count: logCountDifference,
            sortBy : [
                    {
                        field : "_timestamp",
                        sortType : "DESC",
                        reversed : "true"
                    }
                ]
        }
    };

    client.search(queryInfo, function (data) {
    var obj = JSON.parse(data["message"]);
    if (data["status"] === "success") {
        for (var i = 0; i < obj.length; i++) {
            var tempDay = new Date(parseInt(obj[i].values._eventTimeStamp)).toUTCString();
            var logLine;
            if(obj[i].values._trace == null){
                logLine = tempDay +  "  " + obj[i].values._class + "-" + obj[i].values._content;
            }else{
                logLine = tempDay +  "  " + obj[i].values._class + "-" + obj[i].values._content + "-" + obj[i].values._trace;
            }

            logLineArray.push([{
                log: logLine
            }]);
            }
            writeToLogViewer();
            window.scrollTo(0,document.body.scrollHeight);
        }
    }, function (error) {
        console.log("error occurred: " + error);
    });
}

/**
 * This method writes the data to the log viewer
 *
 */
function writeToLogViewer() {
    for (var i=0;i<logLineArray.length;i++){
       $('#logViewer').append(Mustache.to_html(template, {arr:logLineArray[i]}));
    }
}
