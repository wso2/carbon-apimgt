/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
$(document).ready(function () {
     $("#subscribe-button").click( function()
           {
    var applicationId = $("#application-list").val();
    var applicationName = $("#application-list option:selected").text();
    var apiName=$("#apiname").val();
    var version=$("#version").val();
    var provider=$("#provider").val();
    if (applicationId == "-" || applicationId == "createNewApp") {
        BootstrapDialog.show({
                                 type: BootstrapDialog.TYPE_DANGER,
                                 title: 'Error',
                                 message: 'Select an application.',
                                 buttons: [{
                                               label: 'OK',
                                               action: function(dialogRef){
                                                   dialogRef.close();
                                               }
                                           }

                                 ]
                             });
        return;
    }
     var tier = $("#tiers-list").val();
    $.ajax({
    type: "POST",
    url: caramel.context + '/apis/apisubscriptions',
    data: {
        action:"addSubscription",
        applicationId:applicationId,
        name:apiName,
        version:version,
        provider:provider,
        tier:tier
    },
    success: function (result) {
        $("#application-list option[value='"+ $("#application-list").val() +"']").remove();
        $("#subscribe-button").html('Subscribe');
        $("#subscribe-button").removeAttr('disabled');
        if (result.data.error == false) {

            BootstrapDialog.show({
                                     type: BootstrapDialog.TYPE_SUCCESS,
                                     title: 'Success',
                                     message: 'Congratulations! You have successfully subscribed to the API. Please go to "My Subscriptions" page to review your subscription and generate keys.',
                                     buttons: [{
                                                   label: 'Go to My Subscriptions',
                                                   action: function(dialogRef){
                                                       window.location.href =caramel.context+ '/asts/api/my_subscriptions';
                                                   }
                                               },{
                                         label: 'Stay on this page',
                                         action: function(dialogRef){
                                             window.location.reload();
                                         }
                                     }

                                     ]
                                 });
              
        } else {
            BootstrapDialog.show({
                                     type: BootstrapDialog.TYPE_DANGER,
                                     title: 'Error',
                                     message: 'API subscribe process failed.',
                                     buttons: [{
                                                   label: 'OK',
                                                   action: function(dialogRef){
                                                       dialogRef.close();
                                                   }
                                               }

                                     ]
                                 });
        }
          
        },
    dataType: "json"
    });   


    }
);
$('#application-list').change(
            function(){
                if($(this).val() == "createNewApp"){                   
                    window.location.href =caramel.context+ '/asts/api/my_applications';
                }
            }
            );

});
