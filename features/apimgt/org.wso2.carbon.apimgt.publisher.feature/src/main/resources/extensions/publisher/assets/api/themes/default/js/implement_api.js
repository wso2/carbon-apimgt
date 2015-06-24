/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
$(function() {
    $(document).ready(function() {

        var failoverepcount = 1;
        var failoversandboxepcount = 1;
        var loadbalanceepcount = 1;
        var loadbalancesandboxepcount = 1;


        $('#form-api-endpoints').ajaxForm({
            success: function() {
                var options = obtainFormMeta('#form-api-endpoints');
                notify({
                    text: 'API endpoint details saved successfully'
                });
            },
            error: function() {
                notify({
                    text: 'Unable to save endpoint details'
                });
            }
        });

        $('input:radio').change(
            function(){
                if($('input:radio:checked').val() == 'inline') {
                    $('.implementation_method_endpoint').css({'display':'none'});
                    $('.api_designer').css({'display':'block'});
                    //$('#prototyped_api').removeClass('hide');
                } else {
                    $('.implementation_method_endpoint').css({'display':'block'});
                    $('.api_designer').css({'display':'none'}); 
                    //$('#prototyped_api').addClass('hide');
                }
            }
        );  

        $('.more-options').click(function(){
            $('.show-more').css({'display':'none'});
            $('.show-less').css({'display':'block'});
            $('#more-options-endpoints').css({'display':'block'});
       });

        $('.less-options').click(function(){
            $('.show-more').css({'display':'block'});
            $('.show-less').css({'display':'none'});
            $('#more-options-endpoints').css({'display':'none'});
       });

        $( "#api-implement-more-security" ).change(function() {
          if($(this).val() == "secured"){
            $('#api-implement-more-credentials').css({'display':'block'});
          }else{
            $('#api-implement-more-credentials').css({'display':'none'});
          }
        });

        var previousClicked = "";
        $('.api-implement-type').click(function(){
            $($(this).attr('value')).slideToggle();
            $($(this).attr('value')).removeClass('hide');
            if(previousClicked !="" && previousClicked != $(this).attr('value')){
                $(previousClicked).slideUp();
            }
            previousClicked=$(this).attr('value');
        });

        $(".implementation_methods").change(function(event){
            //$(".implementation_method").hide();
            $(".implementation_method_"+$(this).val()).show();
        });

        $( "#api-implement-endpoint-type" ).change(function() {
            var endpointType = $(this).val();
          if(endpointType === "http"){
            $('#api-implement-endpoints-http').css({'display':'block'});
            $('#api-implement-endpoints-address').css({'display':'none'});
            $('#api-implement-endpoints-wsdl').css({'display':'none'});
            $('#api-implement-endpoints-failover').css({'display':'none'});
            $('#api-implement-endpoints-load_balance').css({'display':'none'});
          }else if (endpointType === "address"){
            $('#api-implement-endpoints-http').css({'display':'none'});
            $('#api-implement-endpoints-address').css({'display':'block'});
            $('#api-implement-endpoints-wsdl').css({'display':'none'});
            $('#api-implement-endpoints-failover').css({'display':'none'});
            $('#api-implement-endpoints-load_balance').css({'display':'none'});
          }else if (endpointType === "wsdl"){
            $('#api-implement-endpoints-http').css({'display':'none'});
            $('#api-implement-endpoints-address').css({'display':'none'});
            $('#api-implement-endpoints-wsdl').css({'display':'block'});
            $('#api-implement-endpoints-failover').css({'display':'none'});
            $('#api-implement-endpoints-load_balance').css({'display':'none'});
          }else if (endpointType === "failover"){
            $('#api-implement-endpoints-http').css({'display':'none'});
            $('#api-implement-endpoints-address').css({'display':'none'});
            $('#api-implement-endpoints-wsdl').css({'display':'none'});
            $('#api-implement-endpoints-failover').css({'display':'block'});
            $('#api-implement-endpoints-load_balance').css({'display':'none'});
          }else if (endpointType === "load_balance"){
            $('#api-implement-endpoints-http').css({'display':'none'});
            $('#api-implement-endpoints-address').css({'display':'none'});
            $('#api-implement-endpoints-wsdl').css({'display':'none'});
            $('#api-implement-endpoints-failover').css({'display':'none'});
            $('#api-implement-endpoints-load_balance').css({'display':'block'});
          }
        });

        $( "#api-implement-endpoints-load_balance-algoritm" ).change(function() {
          if($(this).val() == "other"){
            $('#api-implement-endpoints-load_balance-algo-div').css({'display':'block'});
          }else{
            $('#api-implement-endpoints-load_balance-algo-div').css({'display':'none'});
          }
        });

        //Failover EP related- START
        $('#prod-failover-endpoint-add-btn').click(function(){
            failoverepcount ++;
            $( "#prod-failover-endpoints" ).append('<span id= "prod-failover-endpoint-' +failoverepcount+'"><br/>Endpoint '+ failoverepcount +') <br/> <input type ="text" id= "api-implement-endpoints-loadbalance-production-loadbalance-'+ failoverepcount +'"><br/><span>');
       });

        $('#prod-failover-endpoint-remove-btn').click(function(){
            if(failoverepcount >=1){        
               $( "#prod-failover-endpoint-" + failoverepcount ).remove();
               failoverepcount --;
            }
       });

        $('#sandbox-failover-endpoint-add-btn').click(function(){
            failoversandboxepcount ++;
            $( "#sandbox-failover-endpoints" ).append('<span id= "sandbox-failover-endpoint-' +failoversandboxepcount+'"><br/>Endpoint '+ failoversandboxepcount +') <br/> <input type ="text" id= "api-implement-endpoints-failover-sandbox-endpoint-'+ failoversandboxepcount +'"><br/><span>');
       });

        $('#sandbox-failover-endpoint-remove-btn').click(function(){
            if(failoversandboxepcount >=1){        
                $( "#sandbox-failover-endpoint-" + failoversandboxepcount ).remove();
                failoversandboxepcount --;
            }
       });
        //Failover EP related- END
        //LB EP related- START
        $('#prod-loadbalance-endpoint-add-btn').click(function(){
            loadbalanceepcount ++;
            $( "#prod-loadbalance-endpoints" ).append('<span id= "prod-loadbalance-endpoint-' +loadbalanceepcount+'"><br/>Endpoint '+ loadbalanceepcount +') <br/> <input type ="text" id= "api-implement-endpoints-loadbalance-production-loadbalance-'+ loadbalanceepcount +'"><br/><span>');
       });

        $('#prod-loadbalance-endpoint-remove-btn').click(function(){
            if(loadbalanceepcount >=1){        
               $( "#prod-loadbalance-endpoint-" + loadbalanceepcount ).remove();
               loadbalanceepcount --;
            }
       });

        $('#sandbox-loadbalance-endpoint-add-btn').click(function(){
            loadbalancesandboxepcount ++;
            $( "#sandbox-loadbalance-endpoints" ).append('<span id= "sandbox-loadbalance-endpoint-' +loadbalancesandboxepcount+'"><br/>Endpoint '+ loadbalancesandboxepcount +') <br/> <input type ="text" id= "api-implement-endpoints-loadbalance-sandbox-endpoint-'+ loadbalancesandboxepcount +'"><br/><span>');
       });

        $('#sandbox-loadbalance-endpoint-remove-btn').click(function(){
            if(loadbalancesandboxepcount >=1){        
                $( "#sandbox-loadbalance-endpoint-" + loadbalancesandboxepcount ).remove();
                loadbalancesandboxepcount --;
            }
       });

        $('#go_to_manage').click(function(e){
            $("body").unbind("api_saved");
            $("body").on("api_saved" , function(e){
              location.href = caramel.context + "/asts/api/manage/"+store.publisher.api.id;
            });
            $("#implement_form").submit();
        });
        //LB EP related- END
        var v = $("#prototype_form").validate({
        submitHandler: function(form) {        
        var designer = APIMangerAPI.APIDesigner();
        var endpoint_config = {"production_endpoints":{"url": $("#prototype_endpoint").val(),"config":null},"endpoint_type":"http"}
        $('.swagger').val(JSON.stringify(designer.api_doc));
        $('.prototype_config').val(JSON.stringify(endpoint_config));        

        //$('#'+thisID).buttonLoader('start');

        $(form).ajaxSubmit({
            success:function(responseText, statusText, xhr, $form) {
             if (!responseText.error) {
                var designer = APIMangerAPI.APIDesigner();
                designer.saved_api = {};
                designer.saved_api.name = responseText.data.apiName;
                designer.saved_api.version = responseText.data.version;
                designer.saved_api.provider = responseText.data.provider;
               // $('#'+thisID).buttonLoader('stop');
                $( "body" ).trigger( "prototype_saved" );                             
             } else {
                 if (responseText.message == "timeout") {
                     if (ssoEnabled) {
                         var currentLoc = window.location.pathname;
                         if (currentLoc.indexOf(".jag") >= 0) {
                             location.href = "index.jag";
                         } else {
                             location.href = 'site/pages/index.jag';
                         }
                     } else {
                         jagg.showLogin();
                     }
                 } else {
                     jagg.message({content:responseText.message,type:"error"});
                 }
                 $('#'+thisID).buttonLoader('stop');
             }
            }, dataType: 'json'
        });
        }
    });
    
    $("#prototyped_api").click(function(e){
      
        $("body").on("prototype_saved", function(e){
            $("body").unbind("prototype_saved");
                var designer = APIMangerAPI.APIDesigner();
                console.log("prototyped_api pressed body on!!");

                data = {
                    action: "updateStatus",
                    name: designer.saved_api.name,
                    version: designer.saved_api.version,
                    provider: designer.saved_api.provider,
                    status: "PROTOTYPED",
                    publishToGateway: true,
                    requireResubscription: true
                };
                $.ajax({
                           url: caramel.context + '/asts/api/apis/lifecycle?type=api',
                           type: 'POST',
                           data: JSON.stringify(data),
                           contentType: 'application/json',
                           success: function (data) {
                               //BootstrapDialog.alert('Successfully Changed the life cycle state');
                                BootstrapDialog.show({
                                type: BootstrapDialog.TYPE_INFO,
                                title: 'Success',
                                message: 'Successfully deployed as a prototype.',
                                buttons: [{
                                            label: 'OK',
                                            action: function(dialogRef){
                                            dialogRef.close();
                                          }
                                          }]             
                                });  
                           },
                           error: function (data) {
                               //BootstrapDialog.alert('Error while changing the life cycle state');
                            BootstrapDialog.show({
                                                  type: BootstrapDialog.TYPE_DANGER,
                                                  title: 'Error',
                                                  message: 'Error while deploying as a prototype.',
                                                  buttons: [{
                                                             label: 'OK',
                                                             action: function(dialogRef){
                                                             dialogRef.close();
                                                            }
                                                            }]             
                                                  });   
                           }
                       });
                /*$.ajax({
                    type: "POST",
                    url: jagg.site.context + "/site/blocks/life-cycles/ajax/life-cycles.jag",
                    data: {
                        action :"updateStatus",
                        name:designer.saved_api.name,
                        version:designer.saved_api.version,
                        provider: designer.saved_api.provider,
                        status: "PROTOTYPED",
                        publishToGateway:true,
                        requireResubscription:true
                    },
                    success: function(responseText){
                        if (!responseText.error) {
                             $("#prototype-success").modal('show');
                        }else{
                             if (responseText.message == "timeout") {
                                 if (ssoEnabled) {
                                     var currentLoc = window.location.pathname;
                                     if (currentLoc.indexOf(".jag") >= 0) {
                                         location.href = "index.jag";
                                     } else {
                                       location.href = 'site/pages/index.jag';
                                     }
                                 } else {
                                     jagg.showLogin();
                                 }
                             } else {
                                 jagg.message({content:responseText.message,type:"error"});
                             }
                        }
                    },
                    dataType: "json"
                });             */  
            });
       // $("#prototype_form").submit();                        
    });

        var v = $("#implement_form").validate({
                                                     contentType : "application/x-www-form-urlencoded;charset=utf-8",
                                                     dataType: "json",
                                                     onkeyup: false,
                                                     submitHandler: function(form) {
                                                         var designer = APIMangerAPI.APIDesigner();
                                                         $('#swagger').val(JSON.stringify(designer.api_doc));
                                                         $('#saveMessage').show();
                                                         $('#saveButtons').hide();

                                                         $(form).ajaxSubmit({
                                                                                success:function(responseText, statusText, xhr, $form){
                                                                                    $('#saveMessage').hide();
                                                                                    $('#saveButtons').show();
                                                                                    if (!responseText.error) {
                                                                                        $( "body" ).trigger( "api_saved" );
                                                                                    } else {
                                                                                        if (responseText.message == "timeout") {
                                                                                            if (ssoEnabled) {
                                                                                                var currentLoc = window.location.pathname;
                                                                                                if (currentLoc.indexOf(".jag") >= 0) {
                                                                                                    location.href = "index.jag";
                                                                                                } else {
                                                                                                    location.href = 'site/pages/index.jag';
                                                                                                }
                                                                                            } else {
                                                                                                //jagg.showLogin();
                                                                                            }
                                                                                        } else {
                                                                                            //jagg.message({content:responseText.message,type:"error"});
                                                                                        }
                                                                                    }
                                                                                }, dataType: 'json'
                                                                            });
                                                     }
                                                 });





    });
});

var thisID='';
$('#saveBtn').click(function(e){
    thisID = $(this).attr('id');
});

$('#savePrototypeBtn').click(function(e){
    thisID = $(this).attr('id');
});

/*$('#prototyped_api').click(function(e){
    thisID = $(this).attr('id');
});*/

$('#go_to_manage').click(function(e){
    thisID = $(this).attr('id');
});

