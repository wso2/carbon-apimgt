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

var removeAPISubscription;
var getAPIUrl;

/*
 This js function will populate the UI after metadata generation in pages/my_subscription.jag
 */
$(function () {
  var client = new ZeroClipboard($('.curl-copybtn'));

  client.on("aftercopy", function(event) {
  $(event.target).siblings('.curl-copyinfo').css('display', 'inline-block').delay(500).queue(function(callback){
          $('.curl-copyinfo').fadeOut();
      callback();
    });
  });

    /*
     The containers in which the UI components will be rendered
     */
    var CONTROL_CONTAINER = '#subscription-control-panel';
    var SUBS_LIST_CONTAINER = '#subscription-list';

    var PROD_KEYS_CONTAINER = '#prod-token-view';
    var PROD_DOMAIN_CONTAINER = '#prod-domain-view';
    var SAND_KEYS_CONTAINER = '#sand-token-view';
    var SAND_DOMAIN_CONTAINER = '#sand-domain-view';


    var EV_APP_SELECT = 'eventAppSelection';
    var EV_SHOW_KEYS = 'eventShowKeys';
    var EV_HIDE_KEYS = 'eventHideKeys';
    var EV_REGENERATE_TOKEN = 'eventRegenerateToken';
    var EV_UPDATE_DOMAIN = 'eventUpdateDomain';
    var EV_GENERATE_PROD_TOKEN = 'eventGenerateProductionToken';
    var EV_GENERATE_SAND_TOKEN = 'eventGenerateSandboxToken';
    var EV_RGEN_PROD_TOKEN = 'eventRegenerateProductionToken';
    var EV_RGEN_SAND_TOKEN = 'eventRegenerateSandboxToken';
    var EV_SUB_DELETE = 'eventSubscriptionDelete';

    var APP_STORE = {};

    /*
     This function generate the location of the templates used in the rendering
     */
    var getSubscriptionAPI = function (appName, action) {
        return caramel.context + '/apis/application/' + appName + '/' + action + '/subscriptions';
    };

    /*
     The function returns the curent subscriptions for the given application
     */
    var findSubscriptionDetails = function (appName) {
        var apps = metadata.appsWithSubs;
        var app;
        for (var appIndex in apps) {
            app = apps[appIndex];

            if (app.name == appName) {
                return app.subscriptions;
            }
        }
        return [];
    };

    /*
     The function returns details of scopes for the given application
     */
    var findAppDetails = function (appName) {
        var apps = metadata.appsWithSubs;
        var app;
        for (var appIndex in apps) {
            app = apps[appIndex];
            if (app.name == appName) {
                return app;
            }
        }
        return null;
    };

    /*
     The function update the details of the given application
     */
    var updateMetadata = function (appName, newDetails, environment, action) {
        var appData = findAppDetails(appName);
        if (action == 'new') {
            if (environment == 'Production') {

                appData.prodValidityTime = newDetails.validityTime;
                appData.prodAuthorizedDomains = newDetails.accessallowdomains;
                appData.prodKey = newDetails.accessToken;
                appData.prodConsumerKey = newDetails.consumerKey;
                appData.prodRegenarateOption = newDetails.enableRegenarate;
                appData.prodConsumerSecret = newDetails.consumerSecret;
                var prodScope=hideDefaultAppScopeVals(newDetails.tokenScope);
                if(prodScope){
                    appData.keyScopeExist=true;
                    appData.keyScopeValue=prodScope ;
                }else{
                    appData.keyScopeExist=false;
                }
                appData.scopes = findAppDetails(appName).scopes;
                // Checking whether scopes are available.
                if (appData.scopes && appData.scopes.length > 0) {
                    appData.isScopeAvailable = true;
                    var scopesArr=prodScope.split(" ");
                    for(var j=0;j<appData.scopes.length;j++){
                        for(var i=0;i<scopesArr.length;i++){
                            if(scopesArr[i]==appData.scopes[j].scopeName){
                                appData.scopes[j].checked=true}
                        }
                    }
                    // Checking whether scopes are available.

                } else {
                    appData.isScopeAvailable = false;
                }
            } else if (environment == 'Sandbox') {

                appData.sandKeyScope = newDetails.tokenScope;
                appData.sandValidityTime = newDetails.validityTime;
                appData.sandboxAuthorizedDomains = newDetails.accessallowdomains;
                appData.sandboxKey = newDetails.accessToken;
                appData.sandboxConsumerKey = newDetails.consumerKey;
                appData.sandRegenarateOption = newDetails.enableRegenarate;
                appData.sandboxConsumerSecret = newDetails.consumerSecret;
                var sandScope1=hideDefaultAppScopeVals(newDetails.tokenScope);
                if(sandScope1){
                    appData.keyScopeExist=true;
                    appData.keyScopeValue=sandScope1 ;
                }else{
                    appData.keyScopeExist=false;
                }
                appData.scopes = findAppDetails(appName).scopes;
                // Checking whether scopes are available.
                if (appData.scopes && appData.scopes.length > 0) {
                    appData.isScopeAvailable = true;
                    var scopesArr4=sandScope1.split(" ");
                    for(var t=0;t<appData.scopes.length;t++){
                        for(var s=0;s<scopesArr4.length;s++){
                            if(scopesArr4[s]==appData.scopes[t].scopeName){
                                appData.scopes[t].checked=true}
                        }
                    }
                    // Checking whether scopes are available.

                } else {
                    appData.isScopeAvailable = false;
                }
            }
        } else if (action == 'refresh') {
            if (environment == 'Production') {

                appData.prodKeyScope = newDetails.tokenScope;
                appData.prodValidityTime = newDetails.validityTime;
                appData.prodAuthorizedDomains = newDetails.accessallowdomains;
                appData.prodKey = newDetails.accessToken;
                appData.prodRegenarateOption = newDetails.enableRegenarate;
                var prodScope1=hideDefaultAppScopes(newDetails.tokenScope).trim();
                if(prodScope1){
                    appData.keyScopeExist=true;
                    appData.keyScopeValue=prodScope1 ;
                }else{
                    appData.keyScopeExist=false;
                }
                appData.scopes = findAppDetails(appName).scopes;
                // Checking whether scopes are available.
                if (appData.scopes && appData.scopes.length > 0) {
                    appData.isScopeAvailable = true;
                    var scopesArr1=prodScope1.split(" ");
                    for(var m=0;m<appData.scopes.length;m++){
                        for(var n=0;n<scopesArr1.length;n++){
                            if(scopesArr1[n]==appData.scopes[m].scopeName){
                                appData.scopes[m].checked=true}
                        }
                    }
                    // Checking whether scopes are available.

                } else {
                    appData.isScopeAvailable = false;
                }
            } else if (environment == 'Sandbox') {

                appData.sandKeyScope = newDetails.tokenScope;
                appData.sandValidityTime = newDetails.validityTime;
                appData.sandboxAuthorizedDomains = newDetails.accessallowdomains;
                appData.sandboxKey = newDetails.accessToken;
                appData.sandRegenarateOption = newDetails.enableRegenarate;
                var sandScope=hideDefaultAppScopes(newDetails.tokenScope).trim();
                if(sandScope){
                    appData.keyScopeExist=true;
                    appData.keyScopeValue=sandScope ;
                }else{
                    appData.keyScopeExist=false;
                }
                appData.scopes = findAppDetails(appName).scopes;
                // Checking whether scopes are available.
                if (appData.scopes && appData.scopes.length > 0) {
                    appData.isScopeAvailable = true;
                    var scopesArr3=sandScope.split(" ");
                    for(var p=0;p<appData.scopes.length;p++){
                        for(var q=0;q<scopesArr3.length;q++){
                            if(scopesArr3[q]==appData.scopes[p].scopeName){
                                appData.scopes[p].checked=true}
                        }
                    }
                    // Checking whether scopes are available.

                } else {
                    appData.isScopeAvailable = false;
                }
            }
        } else if(action == 'ProvideKeys'){
            if(environment == 'Production'){
                appData.prodValidityTime = newDetails.validityTime;
                appData.prodKey = newDetails.accessToken;
                appData.prodConsumerKey = newDetails.consumerKey;
                appData.prodConsumerSecret = newDetails.consumerSecret;

            }else if(environment == 'Sandbox'){
                appData.sandValidityTime = newDetails.validityTime;
                appData.sandboxKey = newDetails.accessToken;
                appData.sandboxConsumerKey = newDetails.consumerKey;
                appData.sandboxConsumerSecret = newDetails.consumerSecret;
            }

        }
        updateAppDetails(appName, appData);
    };

    /*
     The function update metadata.appsWithSubs for the given application
     */
    var updateAppDetails = function (appName, newAppData) {
        var apps = metadata.appsWithSubs;
        var app;
        for (var appIndex in apps) {
            app = apps[appIndex];

            if (app.name == appName) {
                metadata.appsWithSubs[appIndex] = newAppData;
            }
        }
    };

    /*
     The function delete subscription metadata
     */
    var deleteSubscriptionMetadata = function (appName, apiName, apiProvider, apiVersion, action) {
        var subscriptionDetails = findSubscriptionDetails(appName);
        if (action == 'deleteSubscription') {
            var sub;
            for (var subIndex in subscriptionDetails) {
                sub = subscriptionDetails[subIndex];
                if (sub.name == apiName && sub.provider == apiProvider && sub.version == apiVersion) {
                    subscriptionDetails.splice(subIndex, 1);
                }
            }
        }
    };

    /*
     *This function bind the data and get the UUID needed to return API URL.
     */
    getAPIUrl = function (apiProvider, apiName, apiVersion) {
        var apiData = {};
        var appName = $('#subscription_selection').val();
        apiData.apiName = apiName;
        apiData.apiVersion = apiVersion;
        apiData.apiProvider = apiProvider;
        apiData.appName = appName;
        $.ajax({
                   type: 'POST',
                   url: getSubscriptionAPI(appName, 'getUUID'),
                   data: apiData,
                   success: function (responseData) {
                       var data = responseData.data;
                       if (!data.error) {
                           var uuid = data.response;
                           window.location = 'details/' + uuid;
                       } else {
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_DANGER,
                                                    title: 'Error',
                                                    message: '<div><i class="icon-briefcase"></i> Unable to locate the artifact</div>',
                                                    buttons: [{
                                                                  label: 'Close',
                                                                  action: function (dialogItself) {
                                                                      dialogItself.close();
                                                                  }

                                                              }]

                                                });
                       }
                   }, error : function(data) {
                                    BootstrapDialog.show({
                                         type: BootstrapDialog.TYPE_DANGER,
                                         title: 'Fail to Delete Subscription!',
                                         message: '<div><i class="icon-briefcase"></i> Unable to locate the artifact</div>',
                                         buttons: [{
                                                       label: 'Ok',
                                                       action: function(dialogItself){
                                                           dialogItself.close();
                                                       }
                                                   }]
                                     });
                }
               });
    };

    /*
     The function invokes when generating fresh production tokens
     */
    var attachGenerateProdToken = function () {
        ///We need to prevent the afterRender function from been inherited by child views
        //otherwise this method will be invoked by child views
        $('#btnProvideKeyProduction').on('click', function () {
            $('.cDivParentOfManualAuthAppCreateProduction').show();
            $('.defaultBtnSetForProduction').hide();
        });

        $('#btnProvideKeyProductionCancel').on('click', function () {
            $('.cDivParentOfManualAuthAppCreateProduction').hide();
            $('.defaultBtnSetForProduction').show();
        });

        $("#btnProvideKeyProductionSave").on('click', function () {
            mapExistingOauthClient($(this));
        });

        $('#btn-generate-Production-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var selectedScopes='';
            $("input[name='chk_group_Production']:checked").each(function() {
                selectedScopes+=$(this).val()+" ";
            });
            var tokenRequestData = {};
            tokenRequestData['appName'] = appName;
            tokenRequestData['keyType'] = 'Production';
            tokenRequestData['accessAllowDomains'] = $('#input-Production-allowedDomains').val() || 'ALL';
            tokenRequestData['callbackUrl'] = appDetails.callbackUrl || '';
            tokenRequestData['validityTime'] = $('#input-Production-validityTime').val();
            tokenRequestData['tokenScope'] = selectedScopes;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'new'),
                       data: tokenRequestData,
                       success: function (responseData) {
                           var data = responseData.data;
                           APP_STORE.productionKeys = data;
                           updateMetadata(appName, data, 'Production', 'new');
                           events.publish(EV_GENERATE_PROD_TOKEN, findAppDetails(appName));
                       },
                       error: function (data) {
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_DANGER,
                                                    title: 'Failure',
                                                    message: '<div><i class="icon-briefcase"></i>Unable to generate Production key</div>',
                                                    buttons: [{
                                                                  label: 'Ok',
                                                                  action: function (dialogItself) {
                                                                      dialogItself.close();
                                                                  }
                                                              }]
                                                });
                       }
                   });
        });
    };

    /*
     The function invokes when generating fresh sandbox tokens
     */
    var attachGenerateSandToken = function () {

        $('#btnProvideKeySandbox').on('click', function () {
            $('.cDivParentOfManualAuthAppCreateSandbox').show();
            $('.defaultBtnSetForSandbox').hide();
        });

        $('#btnProvideKeySandboxCancel').on('click', function () {
            $('.cDivParentOfManualAuthAppCreateSandbox').hide();
            $('.defaultBtnSetForSandbox').show();
        });

        $("#btnProvideKeySandboxSave").on('click', function () {
            mapExistingOauthClient($(this));
        });


        $('#btn-generate-Sandbox-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var selectedScopes='';
            $("input[name='chk_group_Sandbox']:checked").each(function() {
                selectedScopes+=$(this).val()+" ";
            });
            var tokenRequestData = {};
            tokenRequestData['appName'] = appName;
            tokenRequestData['keyType'] = 'Sandbox';
            tokenRequestData['accessAllowDomains'] = $('#input-Sandbox-allowedDomains').val() || 'ALL';
            tokenRequestData['callbackUrl'] = appDetails.callbackUrl || '';
            tokenRequestData['validityTime'] = $('#input-Sandbox-validityTime').val();
            tokenRequestData['tokenScope'] = selectedScopes;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'new'),
                       data: tokenRequestData,
                       success: function (responseData) {
                           var data = responseData.data;
                           APP_STORE.sandboxKeys = data;
                           updateMetadata(appName, data, 'Sandbox', 'new');
                           events.publish(EV_GENERATE_SAND_TOKEN, findAppDetails(appName));
                       },
                       error: function (data) {
                                BootstrapDialog.show({
                                             type: BootstrapDialog.TYPE_DANGER,
                                             title: 'Failure',
                                             message: '<div><i class="icon-briefcase"></i>Unable to generate Sandbox key</div>',
                                             buttons: [{
                                                           label: 'Ok',
                                                           action: function (dialogItself) {
                                                               dialogItself.close();
                                                           }
                                                       }]
                                         });
                       }
                   });

        });
    };

    /*
     The function sets up the production domain update button to
     send an update request to the remote api
     */
    var attachUpdateProductionDomains = function () {

        $('#btn-Production-updateDomains').on('click', function () {
            var allowedDomains = $('#input-Production-allowedDomains').val();
            var appName = $('#subscription_selection').val();
            var domainUpdateData = {};
            domainUpdateData['accessToken'] = APP_STORE.productionKeys.accessToken;
            domainUpdateData['accessAllowedDomains'] = allowedDomains;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'updateDomain'),
                       data: domainUpdateData,
                       success: function (responseData) {
                               var data = responseData.data;
                               BootstrapDialog.show({
                                                        type: BootstrapDialog.TYPE_SUCCESS,
                                                        title: 'Success',
                                                        message: "Production domain updated successfully",
                                                        buttons: [{
                                                                      label: 'Ok',
                                                                      action: function(dialogItself){
                                                                          dialogItself.close();
                                                                      }
                                                                  }]
                                                    });
                       },
                       error : function(data) {
                                            BootstrapDialog.show({
                                             type: BootstrapDialog.TYPE_DANGER,
                                             title: 'Error!',
                                             message: 'Error while updating production domain',
                                             buttons: [{
                                                           label: 'Ok',
                                                           action: function(dialogItself){
                                                               dialogItself.close();
                                                           }
                                                       }]
                                         });
                       }
                   });
        });
    };

    var attachUpdateSandboxDomains = function () {

        $('#btn-Sandbox-updateDomains').on('click', function () {
            var allowedDomains = $('#input-Sandbox-allowedDomains').val();
            var appName = $('#subscription_selection').val();
            var domainUpdateData = {};
            domainUpdateData['accessToken'] = APP_STORE.sandboxKeys.accessToken;
            domainUpdateData['accessAllowedDomains'] = allowedDomains;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'updateDomain'),
                       data: domainUpdateData,
                       success: function (responseData) {
                           var data = responseData.data;
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_SUCCESS,
                                                    title: 'Error',
                                                    message: "Sandbox domain updated successfully.",
                                                    buttons: [{
                                                                  label: 'Ok',
                                                                  action: function(dialogItself){
                                                                      dialogItself.close();
                                                                  }
                                                              }]
                                                });
                       },
                       error : function(data) {
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_DANGER,
                                                    title: 'Error!',
                                                    message: 'Error while updating sandbox domain',
                                                    buttons: [{
                                                                  label: 'Ok',
                                                                  action: function(dialogItself){
                                                                      dialogItself.close();
                                                                  }
                                                              }]
                                                });
                       }
                   });
        });
    };

    /*
     The function listens for the user to check and uncheck the show keys checkbox
     and then broadcasts the appropriate event
     */
    var attachShowCheckbox = function () {

        $('#input-checkbox-showkeys').change(function () {
            var isChecked = $('#input-checkbox-showkeys').prop('checked');
            APP_STORE['showKeys'] = isChecked;
            if (isChecked) {
                events.publish(EV_SHOW_KEYS);
            }
            else {
                events.publish(EV_HIDE_KEYS);
            }
        });
    };

    /*
     The function is used to attach the logic which will regenerate the Production token
     */
    var attachRegenerateProductionToken = function () {
        $('#btn-refresh-Production-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var selectedScopes='';
            $("input[name='chk_group_Production']:checked").each(function() {
                selectedScopes+=$(this).val()+" ";
            });
            var tokenRequestData = {};
            tokenRequestData.appName = appName;
            tokenRequestData.keyType = 'Production';
            tokenRequestData.accessAllowDomains = $('#input-Production-allowedDomains').val() || 'ALL';
            tokenRequestData.validityTime = $('#input-Production-validityTime').val();
            tokenRequestData.accessToken = appDetails.prodKey;
            tokenRequestData.consumerKey = appDetails.prodConsumerKey;
            tokenRequestData.consumerSecret = appDetails.prodConsumerSecret;
            tokenRequestData.tokenScope = selectedScopes;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'refresh'),
                       data: tokenRequestData,
                       success: function (responseData) {
                           var data = responseData.data;
                           data.consumerKey = APP_STORE.productionKeys.consumerKey;
                           data.consumerSecret = APP_STORE.productionKeys.consumerSecret;
                           var jsonData = data;
                           APP_STORE.productionKeys = jsonData;
                           updateMetadata(appName, jsonData, 'Production', 'refresh');
                           events.publish(EV_GENERATE_PROD_TOKEN, findAppDetails(appName));
                       },
                       error : function(data) {
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_DANGER,
                                                    title: 'Error',
                                                    message: "Error while generating the Production token",
                                                    buttons: [{
                                                                  label: 'Ok',
                                                                  action: function(dialogItself){
                                                                      dialogItself.close();
                                                                  }
                                                              }]
                                                });
                       }
                   });
        });
    };

    /*
     The function is used to attach the logic which will regenerate the Sandbox token
     */
    var attachRegenerateSandboxToken = function () {
        $('#btn-refresh-Sandbox-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var selectedScopes='';
            $("input[name='chk_group_Sandbox']:checked").each(function() {
                selectedScopes+=$(this).val()+" ";
            });
            var tokenRequestData = {};
            tokenRequestData.appName = appName;
            tokenRequestData.keyType = 'Sandbox';
            tokenRequestData.accessAllowDomains = $('#input-Sandbox-allowedDomains').val() || 'ALL';
            tokenRequestData.validityTime = $('#input-Sandbox-validityTime').val();
            tokenRequestData.accessToken = appDetails.sandboxKey;
            tokenRequestData.consumerKey = appDetails.sandboxConsumerKey;
            tokenRequestData.consumerSecret = appDetails.sandboxConsumerSecret;
            tokenRequestData.tokenScope = selectedScopes;
            $.ajax({
                       type: 'POST',
                       url: getSubscriptionAPI(appName, 'refresh'),
                       data: tokenRequestData,
                       success: function (responseData) {
                           var data = responseData.data;
                           data.consumerKey = APP_STORE.sandboxKeys.consumerKey;
                           data.consumerSecret = APP_STORE.sandboxKeys.consumerSecret;
                           var jsonData = data;
                           APP_STORE.sandboxKeys = jsonData;
                           updateMetadata(appName, jsonData, 'Sandbox', 'refresh');
                           events.publish(EV_GENERATE_SAND_TOKEN, findAppDetails(appName));
                       }, error : function(data) {
                           BootstrapDialog.show({
                                                    type: BootstrapDialog.TYPE_DANGER,
                                                    title: 'Error',
                                                    message: "Error while generating the sandbox token",
                                                    buttons: [{
                                                                  label: 'Ok',
                                                                  action: function(dialogItself){
                                                                      dialogItself.close();
                                                                  }
                                                              }]
                                                });
                       }
                   });
        });
    };

    removeAPISubscription = function (apiName, apiVersion, apiProvider) {
        BootstrapDialog.show({
                                 type: BootstrapDialog.TYPE_WARNING,
                                 title: 'Warning',
                                 message: '<div><i class="fw fw-warning"></i>Are you sure you want to remove the subscription for ' +
                                          apiName + '? </div>',
                                 buttons: [{
                                               label: 'Yes',
                                               action: function (dialogItself) {
                                                   var appName = $('#subscription_selection').val();
                                                   var appDetails = findAppDetails(appName);
                                                   var deleteAPISubscriptionData = {};
                                                   deleteAPISubscriptionData.apiName = apiName;
                                                   deleteAPISubscriptionData.apiVersion = apiVersion;
                                                   deleteAPISubscriptionData.apiProvider = apiProvider;
                                                   deleteAPISubscriptionData.appId = appDetails.id;
                                                   deleteAPISubscriptionData.appTier = appDetails.tier;
                                                   $.ajax({
                                                              type: 'POST',
                                                              url: getSubscriptionAPI(appName, 'deleteSubscription'),
                                                              data: deleteAPISubscriptionData,
                                                              success: function (responseData) {
                                                                  var data = responseData.data;
                                                                  if (data.success) {
                                                                      deleteSubscriptionMetadata(appName, apiName, apiProvider,
                                                                                                 apiVersion, 'deleteSubscription');
                                                                      var subs = findSubscriptionDetails(appName);
                                                                      if (subs.length != 0) {
                                                                          events.publish(EV_SUB_DELETE, {appName: appName});
                                                                      } else {
                                                                          location.reload();
                                                                      }
                                                                  } else {
                                                                      BootstrapDialog.show({
                                                                                               type: BootstrapDialog.TYPE_DANGER,
                                                                                               title: 'Fail to Delete Subscription!',
                                                                                               message: '<div><i class="fw fw-warning"></i> API : ' +
                                                                                                        appName + ' subscription could not be deleted.</div>',
                                                                                               buttons: [{
                                                                                                             label: 'Close',
                                                                                                             action: function (dialogItself) {
                                                                                                                 dialogItself.close();
                                                                                                             }
                                                                                                         }]
                                                                                           });
                                                                  }
                                                              }, error : function(data) {
                                                                  BootstrapDialog.show({
                                                                                           type: BootstrapDialog.TYPE_DANGER,
                                                                                           title: 'Fail to Delete Subscription!',
                                                                                           message: '<div><i class="fw fw-warning"></i> API : ' +
                                                                                                    appName + ' subscription could not be deleted.</div>',
                                                                                           buttons: [{
                                                                                                         label: 'Ok',
                                                                                                         action: function(dialogItself){
                                                                                                             dialogItself.close();
                                                                                                         }
                                                                                                     }]
                                                                                       });
                                                              }
                                                          });
                                                   dialogItself.close();
                                               }
                                           }, {
                                               label: 'No',
                                               action: function (dialogItself) {
                                                   dialogItself.close();
                                               }
                                           }]
                             });
    };

    events.register(EV_APP_SELECT);
    events.register(EV_SHOW_KEYS);
    events.register(EV_REGENERATE_TOKEN);
    events.register(EV_GENERATE_PROD_TOKEN);
    events.register(EV_UPDATE_DOMAIN);
    events.register(EV_GENERATE_SAND_TOKEN);
    events.register(EV_HIDE_KEYS);
    events.register(EV_RGEN_PROD_TOKEN);
    events.register(EV_RGEN_SAND_TOKEN);
    events.register(EV_SUB_DELETE);

    /*
     Keys View
     The default view which prompts the user to generate a key
     */

    //Production view
    Views.extend('view', {
        id: 'defaultProductionKeyView',
        container: PROD_KEYS_CONTAINER,
        partial: 'sub-keys-generate-prod',
        beforeRender: function (data) {
            data.environment = Views.translate('Production');
            data.mapExistingAuthApps = metadata.mapExistingAuthApps;

            // Checking whether application name is selected.
            if (data.appName == null) {
                data.isDataNotAvailable = true;
            } else {
                data.isDataNotAvailable = false;
                // retrieving scopes if available.
                var appD=findAppDetails($('#subscription_selection').val());
                var prodScope=hideDefaultAppScopes(appD.prodKeyScopeValue);
                if(prodScope){
                    data.keyScopeExist=true;
                    data.keyScopeValue=prodScope ;
                }else{
                    data.keyScopeExist=false;
                }
                data.scopes = appD.scopes;
                data.scopes=appD.scopes;
                // Checking whether scopes are available.
                if (data.scopes && data.scopes.length > 0) {
                    data.isScopeAvailable = true;
                    var scopesArr=prodScope.split(" ");
                    for(var j=0;j<data.scopes.length;j++){
                        for(var i=0;i<scopesArr.length;i++){
                            if(scopesArr[i]==data.scopes[j].scopeName){
                                data.scopes[j].checked=true}
                        }
                    }
                    // Checking whether scopes are available.

                } else {
                    data.isScopeAvailable = false;
                }
            }
        },
        resolveRender: function () {
            if (APP_STORE.productionKeys) {
                return false;
            }
            return true;
        },
        afterRender: attachGenerateProdToken,
        subscriptions: [EV_APP_SELECT]
    });

    Views.extend('defaultProductionKeyView', {
        id: 'visibleProductionKeyView',
        partial: 'sub-keys-visible-prod',
        subscriptions: [EV_APP_SELECT, EV_SHOW_KEYS, EV_GENERATE_PROD_TOKEN],
        resolveRender: function (data) {
            if (!APP_STORE.showKeys) {
                return false;
            }

            if (!APP_STORE.productionKeys) {
                return false;
            }

            //Determine if the keys need to be visible
            //if (APP_STORE.showKeys) {
            Views.mirror(APP_STORE.productionKeys, data);
            return true;
            //}
            //return false;
        },
        afterRender: attachRegenerateProductionToken
    });

    Views.extend('defaultProductionKeyView', {
        id: 'hiddenProductionKeyView',
        subscriptions: [EV_APP_SELECT, EV_SHOW_KEYS, EV_HIDE_KEYS, EV_GENERATE_PROD_TOKEN],
        partial: 'sub-keys-hidden-prod',
        resolveRender: function (data) {
            //Determine if the keys need to be visible
            if (APP_STORE.showKeys) {
                return false;
            }

            if (!APP_STORE.productionKeys) {
                return false;
            }

            Views.mirror(APP_STORE.productionKeys, data);
            return true;
        },
        afterRender: attachRegenerateProductionToken
    });


    //Sandbox view
    Views.extend('defaultSandboxKeyView', {
        id: 'defaultSandboxKeyView',
        container: SAND_KEYS_CONTAINER,
        partial: 'sub-keys-generate-prod',
        beforeRender: function (data) {
            data.environment = Views.translate('Sandbox');
            data.mapExistingAuthApps = metadata.mapExistingAuthApps;

            // Checking whether application name is selected.
            if (data.appName == null) {
                data.isDataNotAvailable = true;
            } else {
                data.isDataNotAvailable = false;
                // retrieving scopes if available.
                var appD=findAppDetails($('#subscription_selection').val());
                var sandScope=hideDefaultAppScopes(appD.sandKeyScopeValue);
                if(sandScope){
                    data.keyScopeExist=true;
                    data.keyScopeValue= sandScope;
                }else{
                    data.keyScopeExist=false;
                }
                data.scopes=appD.scopes;
                // Checking whether scopes are available.
                if (data.scopes && data.scopes.length > 0) {
                    data.isScopeAvailable = true;
                    var scopesArr=sandScope.split(" ");
                    for(var j=0;j<data.scopes.length;j++){
                        for(var i=0;i<scopesArr.length;i++){
                            if(scopesArr[i]==data.scopes[j].scopeName){
                                data.scopes[j].checked=true;                        }
                        }
                    }
                } else {
                    data.isScopeAvailable = false;
                }
            }
        },
        resolveRender: function () {
            if (APP_STORE.sandboxKeys) {
                return false;
            }
            return true;
        },
        afterRender: attachGenerateSandToken,
        subscriptions: [EV_APP_SELECT]
    });

    var hideDefaultAppScopes=function(scopes){
        var scopesList='';
        if(scopes!=null){
            var scopesArr=scopes.split(" ");
            for(var i=0;i<scopesArr.length;i++){
                if(scopesArr[i]!="am_application_scope" && scopesArr[i]!="default"){
                    scopesList+=scopesArr[i]+" ";
                }
            }
        }
        return scopesList;

    };

    var hideDefaultAppScopeVals=function(scopes){
        var scopesList='';
        if(scopes!=null){
            var scopesArr=scopes.split(",");
            for(var i=0;i<scopesArr.length;i++){
                if(scopesArr[i]!="am_application_scope" && scopesArr[i]!="default"){
                    scopesList+=scopesArr[i]+" ";
                }
            }
        }
        return scopesList;

    };

    Views.extend('defaultSandboxKeyView', {
        id: 'visibleSandboxKeyView',
        partial: 'sub-keys-visible-prod',
        resolveRender: function (data) {

            if (!APP_STORE.sandboxKeys) {
                return false;
            }
            if (!APP_STORE.showKeys) {
                return false;
            }
            //Only render the view if sandbox keys are present
            // if ((APP_STORE.sandboxKeys) && (APP_STORE.showKeys)) {
            Views.mirror(APP_STORE.sandboxKeys, data);
            return true;
            //}
        },
        afterRender: attachRegenerateSandboxToken,
        subscriptions: [EV_APP_SELECT, EV_SHOW_KEYS, EV_GENERATE_SAND_TOKEN]
    });

    Views.extend('defaultSandboxKeyView', {
        id: 'hiddenSandboxKeyView',
        subscriptions: [EV_APP_SELECT, EV_SHOW_KEYS, EV_HIDE_KEYS, EV_GENERATE_SAND_TOKEN],
        partial: 'sub-keys-hidden-prod',
        resolveRender: function (data) {
            //Determine if the keys need to be visible
            if (APP_STORE.showKeys) {
                return false;
            }

            if (!APP_STORE.sandboxKeys) {
                return false;
            }

            Views.mirror(APP_STORE.sandboxKeys, data);
            return true;
        },
        afterRender: attachRegenerateSandboxToken
    });

    /*
     End of Keys View
     */

    /*
     Domain View
     */

    //Production view
    Views.extend('view', {
        id: 'defaultProductionDomainView',
        container: PROD_DOMAIN_CONTAINER,
        partial: 'sub-domain-token-prod',
        beforeRender: function (data) {
            if (data.appName != null && !APP_STORE.productionKeys) {
                data.isDataNotAvailable = false;
                data.environment = Views.translate('Production');
                data.allowedDomains = Views.translate('ALL');
                data.validityTime = Views.translate('3600');
            } else if (typeof(data.appName) == "undefined" || typeof(data.appName) == "null" || data.appName == null) {
                data.isDataNotAvailable = true;
            }
        },
        subscriptions: [EV_APP_SELECT],
        afterRender: function () {
        }
    });

    Views.extend('defaultProductionDomainView', {
        id: 'updateProductionDomainView',
        partial: 'sub-domain-update-prod',
        resolveRender: function (data) {
            if (!APP_STORE.productionKeys) {
                data.isDataNotAvailable = true;
                return false;
            } else {
                data.environment = Views.translate('Production');
                data.allowedDomains = APP_STORE.productionKeys.allowedDomains;
                data.isDataNotAvailable = false;
                return true;
            }
        },
        subscriptions: [EV_APP_SELECT, EV_GENERATE_PROD_TOKEN],
        afterRender: attachUpdateProductionDomains
    });

    //Sandbox view
    Views.extend('defaultProductionDomainView', {
        id: 'defaultSandboxDomainView',
        container: SAND_DOMAIN_CONTAINER,
        beforeRender: function (data) {
            if (data.appName != null && !APP_STORE.sandboxKeys) {
                data.isDataNotAvailable = false;
                data.environment = Views.translate('Sandbox');
                data.allowedDomains = Views.translate('ALL');
                data.validityTime = Views.translate('3600');
            } else if (typeof(data.appName) == "undefined" || typeof(data.appName) == "null" || data.appName == null) {
                data.isDataNotAvailable = true;
            }
        },
        subscriptions: [EV_APP_SELECT],
        afterRender: function () {
        }
    });

    Views.extend('defaultSandboxDomainView', {
        id: 'updateSandboxDomainVIew',
        partial: 'sub-domain-update-prod',
        resolveRender: function (data) {
            if (!APP_STORE.sandboxKeys) {
                data.isDataNotAvailable = true;
                return false;
            } else {
                data.environment = Views.translate('Sandbox');
                data.allowedDomains = APP_STORE.sandboxKeys.allowedDomains;
                data.isDataNotAvailable = false;
                return true;
            }
        },
        subscriptions: [EV_APP_SELECT, EV_GENERATE_SAND_TOKEN],
        afterRender: attachUpdateSandboxDomains
    });
    /*
     End of Domain View
     */

    /*
     API Subscription listing view
     */
    Views.extend('view', {
        id: 'defaultAPISubscriptionsView',
        container: SUBS_LIST_CONTAINER,
        partial: 'sub-listing',
        beforeRender: function (data) {
            var appName = data.appName;
            if (appName != null) {
                data.isDataNotAvailable = false;
                data.subscriptions = findSubscriptionDetails(appName);
            } else {
                data.isDataNotAvailable = true;
            }
        },
        subscriptions: [EV_APP_SELECT, EV_SUB_DELETE],
        afterRender: function () {
        }
    });


    /*
     Control panel containing the Show Keys checkbox
     -Rendered when the user selects an application
     */

    Views.extend('view', {
        id: 'keyControlPanelView',
        container: CONTROL_CONTAINER,
        partial: 'sub-control-panel',
        subscriptions: [EV_APP_SELECT],
        afterRender: attachShowCheckbox
    });

    /*
     The function returns the Production Keys of the given application
     */
    var findProdKeys = function (details) {
        if (!details.prodConsumerKey &&  !details.prodConsumerSecret) {
            return null;
        }
        var keys = {};
        keys.accessToken = details.prodKey;
        keys.environment = 'Production';
        keys.validityTime = details.prodValidityTime;
        keys.consumerKey = details.prodConsumerKey;
        keys.consumerSecret = details.prodConsumerSecret;
        keys.allowedDomains = details.prodAuthorizedDomains;

        return keys;
    };

    /*
     The function returns the Sandbox Keys of the given application
     */
    var findSandKeys = function (details) {
        if (!details.sandboxConsumerKey &&  !details.sandboxConsumerSecret) {
            return null;
        }
        var keys = {};
        keys.accessToken = details.sandboxKey;
        keys.environment = 'Sandbox';
        keys.validityTime = details.sandValidityTime;
        keys.consumerKey = details.sandboxConsumerKey;
        keys.consumerSecret = details.sandboxConsumerSecret;
        keys.allowedDomains = details.sandboxAuthorizedDomains;

        return keys;
    };

    /*
     The function sets the APP_STORE each and every time,
     when page gets refresh and change the application selected(#subscription_selection)
     */
    var populateAppStore = function (appName) {
        var details = findAppDetails(appName);
        APP_STORE.appName = appName;
        APP_STORE.appDetails = details;
        APP_STORE.productionKeys = findProdKeys(details);
        APP_STORE.sandboxKeys = findSandKeys(details);
        APP_STORE.showKeys = true;
    };

    var defaultAppName = $('#subscription_selection').val();
    populateAppStore(defaultAppName);
    events.publish(EV_APP_SELECT, {appName: defaultAppName});

    /*
     The function publish the token data when ever user change the selected application(#subscription_selection)
     */
    $('#subscription_selection').on('change', function () {
        var appName = $('#subscription_selection').val();
        APP_STORE = {};
        populateAppStore(appName);
        events.publish(EV_APP_SELECT, {appName: appName});
    });

    var mapExistingOauthClient=function(oBtnElement){
        var appName = $('#subscription_selection').val();
        var elem = oBtnElement;
        var i = elem.attr("iteration");
        var keyType = elem.attr("data-keytype");
        var authoDomains;
        var clientId;
        var clientSecret;
        //var userName = elem.attr("data-username");
        var userName = "admin";
        var validityTime;
        if (keyType == 'Production') {
            authoDomains = $('#input-Production-allowedDomains').val();
            validityTime = $('#input-Production-validityTime').val();
            clientId = $('#inputConsumerKeyProduction').val();
            clientSecret = $('#inputConsumerSecretProduction').val();
        } else {
            authoDomains = $('#input-Sandbox-allowedDomains').val();
            validityTime = $('#input-Sandbox-validityTime').val();
            clientId = $('#inputConsumerKeySandbox').val();
            clientSecret = $('#inputConsumerSecretSandbox').val();
        }

        /*
         if we have additional parameters we can pass them as a json object.
         */
        var oJsonParams = {
            "username" : userName,
            "key_type" : keyType,
            "client_secret":clientSecret,
            "applicationName" : appName
        };
        console.log(oJsonParams);
        var saveAuthAppParams={};
        saveAuthAppParams.applicationName = appName;
        saveAuthAppParams.keytype = keyType;
        saveAuthAppParams.authorizedDomains = authoDomains;
        saveAuthAppParams.validityTime = validityTime;
        saveAuthAppParams.callbackUrl = "dummyUrl";
        saveAuthAppParams.jsonParams = JSON.stringify(oJsonParams);
        saveAuthAppParams.client_id = clientId;

        $.ajax({
            type: 'POST',
            url: getSubscriptionAPI(appName, 'mapExistingOauthClient'),
            data: saveAuthAppParams,
            success: function (responseData) {
                if(keyType == 'Production'){
                    var data = responseData.data.response;
                    APP_STORE.productionKeys = data;
                    updateMetadata(appName, data, keyType, 'ProvideKeys');
                    events.publish(EV_GENERATE_PROD_TOKEN, findAppDetails(appName));
                }
                else{
                    var data = responseData.data.response;
                    APP_STORE.sandboxKeys = data;
                    updateMetadata(appName, data, keyType, 'ProvideKeys');
                    events.publish(EV_GENERATE_SAND_TOKEN, findAppDetails(appName));
                }

            }, error : function(data) {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_DANGER,
                    title: 'Error',
                    message: "Error while generating the "+keyType+" token",
                    buttons: [{
                        label: 'Ok',
                        action: function(dialogItself){
                            dialogItself.close();
                        }
                    }]
                });
            }
        });

    }

});

function generateCurl( granttype, id){
    var apps = metadata.appsWithSubs;
    var gatewayurlendpoint = '';
    if(metadata.gatewayEndpoint.error == false){
      gatewayurlendpoint = metadata.gatewayEndpoint.endPoint;
    }

    var appName = $('#subscription_selection').val();
    var consumerKey = {};
    var consumerSecret = {};
    var keys = {};
    for (i = 0; i < apps.length; i++) { 
      var app = apps[i] ;
      if(app.name == appName){
        consumerKey = app.prodConsumerKey;
        consumerSecret = app.prodConsumerSecret;
        keys = consumerKey+":"+consumerSecret;
        break;
      }
    }

      var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
          var encodedString = Base64.encode(keys);
      var grantTypeVal = '';

      if (granttype == 'password'){
        grantTypeVal = 'password&username=<span>&lt;USER&gt;</span>&password=<span>&lt;PASSWORD&gt;</span>';
      }
      else if (granttype == 'client_credentials'){
        grantTypeVal = 'client_credentials';
      }

      $('#'+id).attr('data-granttype', granttype);
      $('#'+id).html('curl -k -d "grant_type='+grantTypeVal+'" -H "Authorization: Basic '+encodedString+', Content-Type: application/x-www-form-urlencoded" '+gatewayurlendpoint+'/token');
      $('#'+id).closest('.row-fluid').show();
      $('#'+id+ 'Copy').show();
}



