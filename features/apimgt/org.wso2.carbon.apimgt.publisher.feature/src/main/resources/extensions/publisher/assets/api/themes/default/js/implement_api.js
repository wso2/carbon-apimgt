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
                /*if($('input:radio:checked').val() == 'inline') {
                    $('.implementation_method_endpoint').css({'display':'none'});
                    $('.api-implement-resources').css({'display':'block'});
                } else {*/
                    $('.implementation_method_endpoint').css({'display':'block'});
                    $('.api-implement-resources').css({'display':'none'}); 
                //}
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

        var v = $("#implement_form").validate({
                                                     contentType : "application/x-www-form-urlencoded;charset=utf-8",
                                                     dataType: "json",
                                                     onkeyup: false,
                                                     submitHandler: function(form) {
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
