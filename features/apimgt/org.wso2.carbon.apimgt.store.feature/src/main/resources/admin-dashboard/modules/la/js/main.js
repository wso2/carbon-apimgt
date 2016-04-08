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
var client = new AnalyticsClient().init();
var from = new Date(moment().subtract(1, 'year')).getTime();
var to = new Date(moment()).getTime();
var dataM = [];
var filterdMessage;
var template1 = "<ul class='template3'>{{#arr}}<li class='class'>{{class}}</li>{{/arr}}</ul>";
function initialize() {
    //fetch();
    //$("#tblArtifactDeleted").html(getDefaultText());
}

function getDefaultText() {
    return '<div class="status-message">' +
        '<div class="message message-info">' +
        '<h4><i class="icon fw fw-info"></i>No content to display</h4>' +
        '<p>Please select a date range to view stats.</p>' +
        '</div>' +
        '</div>';
};

function getEmptyRecordsText() {
    return '<div class="status-message">' +
        '<div class="message message-info">' +
        '<h4><i class="icon fw fw-info"></i>No records found</h4>' +
        '<p>Please select a date range to view stats.</p>' +
        '</div>' +
        '</div>';
}

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
            query: "*:*",
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
                dataM.push([{           
                    class: obj[i].values._class + "-" + obj[i].values._content        
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


