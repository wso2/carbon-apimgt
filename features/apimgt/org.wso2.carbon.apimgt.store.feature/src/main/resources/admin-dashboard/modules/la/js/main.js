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
var gatewayPort = location.port -9443 + 8243; //Calculate the port offset based gateway port.
var serverUrl = "https://"+location.hostname +":"+ gatewayPort+"/LogAnalyzerRestApi/1.0";
var client = new AnalyticsClient().init(null, null, serverUrl);
var dataM = [];
var filterdMessage;
var template1 = "<ul class='template3'>{{#arr}}<li class='class'>{{class}}</li>{{/arr}}</ul>";



$(document).ready(function () {
    fetch();
var interval = setInterval(fetch, 5000);
});

function fetch() {
    dataM.length = 0;
    var queryInfo;
    queryInfo = {
        tableName: "LOGANALYZER",
        searchParams: {
            query: "logstream:\"" + tenantId + "\"",
            start: 0, //starting index of the matching record set
            count: 100, //page size for pagination
            sortBy : [
                    {
                        field : "_timestamp",
                        sortType : "DESC", // This can be ASC, DESC
                        reversed : "false" //optional
                    }
                ]
        }
    };
    console.log(queryInfo);
    client.search(queryInfo, function (d) {
        var obj = JSON.parse(d["message"]);
        if (d["status"] === "success") {

            for (var i = 0; i < obj.length; i++) {
            var tempDay = new Date(parseInt(obj[i].values._eventTimeStamp)).toUTCString()

                dataM.push([{
                    class: tempDay +  "  " + obj[i].values._class + "-" + obj[i].values._content + "-" + obj[i].values._trace
                }]);
            }
            writeToLogViewer();
        }
    }, function (error) {
        console.log("error occured: " + error);
    });
}

function writeToLogViewer() {
    $("#logViewer").empty();
    for (var i=0;i<dataM.length;i++)
    {
   
            $('#logViewer').append(Mustache.to_html(template1, {arr:dataM[i]}));

    }
}