$(function () {

    /*
     The location of the templates used in the rendering
     */
    // var API_SUBS_URL = '/store/resources/api/v1/subscription/';
    // var API_TOKEN_URL = '/store/resources/api/v1/apptoken/a';
    //var API_TOKEN_URL = caramel.context+'/apis/applications';
    // var API_DOMAIN_URL = '/store/resources/api/v1/domain/a';
    //var API_SUBSCRIPTION = caramel.context+'/apis/application/subscriptions'

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

    var APP_STORE = {};

    /*
     This function generate the location of the templates used in the rendering
     */
    var getSubscriptionAPI = function (appName) {
        return caramel.context + '/apis/application/' + appName + '/subscriptions';
    };

    /*
     The function returns the curent subscriptions for the given application
     */
    var findSubscriptionDetails = function (appName) {
        //log.info("appsWithSubs :: " + appsWithSubs);
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
     The function returns the details of the given application
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
    var updateMetadata = function (appName, newDetails, environment) {
        var appData = findAppDetails(appName);
        if (environment == 'Production') {

            appData.prodKeyScope = newDetails.tokenScope;
            appData.prodValidityTime = newDetails.validityTime;
            appData.prodAuthorizedDomains = newDetails.accessallowdomains;
            appData.prodKey = newDetails.accessToken;
            appData.prodConsumerKey = newDetails.consumerKey;
            appData.prodRegenarateOption = newDetails.enableRegenarate;
            appData.prodConsumerSecret = newDetails.consumerSecret;
        } else if (environment == 'Sandbox') {

            appData.sandKeyScope = newDetails.tokenScope;
            appData.sandValidityTime = newDetails.validityTime;
            appData.sandboxAuthorizedDomains = newDetails.accessallowdomains;
            appData.sandboxKey = newDetails.accessToken;
            appData.sandboxConsumerKey = newDetails.consumerKey;
            appData.sandRegenarateOption = newDetails.enableRegenarate;
            appData.sandboxConsumerSecret = newDetails.consumerSecret;
        }
        updateAppDetails(appName, appData);
    };

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

    var attachGenerateProdToken = function () {
        ///We need to prevent the afterRender function from been inherited by child views
        //otherwise this method will be invoked by child views
        //console.info('Attaching generate button');
        $('#btn-generate-Production-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var tokenRequestData = {};
            tokenRequestData['appName'] = appName;
            tokenRequestData['keyType'] = 'Production';
            tokenRequestData['accessAllowDomains'] = $('#input-Production-allowedDomains').val() || 'ALL';
            tokenRequestData['callbackUrl'] = appDetails.callbackUrl || '';
            tokenRequestData['validityTime'] = $('#input-Production-validityTime').val();
            $.ajax({
                type: 'POST',
                url: getSubscriptionAPI(appName),
                data: tokenRequestData,
                success: function (data) {
                    var jsonData = data;
                    APP_STORE.productionKeys = jsonData;
                    updateMetadata(appName, jsonData, 'Production');
                    events.publish(EV_GENERATE_PROD_TOKEN, jsonData);
                }
            });
        });
    };

    var attachGenerateSandToken = function () {

        $('#btn-generate-Sandbox-token').on('click', function () {
            var appName = $('#subscription_selection').val();
            var appDetails = findAppDetails(appName);
            var tokenRequestData = {};
            tokenRequestData['appName'] = appName;
            tokenRequestData['keyType'] = 'Sandbox';
            tokenRequestData['accessAllowDomains'] = $('#input-Sandbox-allowedDomains').val() || 'ALL';
            tokenRequestData['callbackUrl'] = appDetails.callbackUrl || '';
            tokenRequestData['validityTime'] = $('#input-Sandbox-validityTime').val();
            $.ajax({
                type: 'POST',
                url: getSubscriptionAPI(appName),
                data: tokenRequestData,
                success: function (data) {
                    var jsonData = data;
                    APP_STORE.sandboxKeys = jsonData;
                    updateMetadata(appName, jsonData, 'Sandbox');
                    events.publish(EV_GENERATE_SAND_TOKEN, jsonData);
                }
            });

        });
    };

    /*
     The function sets up the production domain update button to
     send an update request to the remote api
     */
    var attachUpdateProductionDomains = function () {

        $('#btn-production-updateDomains').on('click', function () {
            var allowedDomains = $('#input-Production-allowedDomains').val();
            console.info(JSON.stringify(APP_STORE.productionKeys));
            var domainUpdateData = {};
            domainUpdateData['accessToken'] = APP_STORE.productionKeys.accessToken;
            domainUpdateData['accessAllowedDomains'] = allowedDomains;

            console.info('***Domain Update Data****');
            console.info(JSON.stringify(domainUpdateData));
            $.ajax({
                type: 'PUT',
                url: API_DOMAIN_URL,
                contentType: 'application/json',
                data: JSON.stringify(domainUpdateData),
                success: function (data) {
                    console.info('Domain updated successfully');
                }
            });
        });
    };

    var attachUpdateSandboxDomains = function () {

        $('#btn-sandbox-updateDomains').on('click', function () {
            var allowedDomains = $('#input-Sandbox-allowedDomains').val();
            console.info(JSON.stringify(APP_STORE.productionKeys));
            var domainUpdateData = {};
            domainUpdateData['accessToken'] = APP_STORE.sandboxKeys.accessToken;
            domainUpdateData['accessAllowedDomains'] = allowedDomains;

            console.info('***Sanbox Domain Update Data***');
            console.info(JSON.stringify(domainUpdateData));
            $.ajax({
                type: 'PUT',
                url: API_DOMAIN_URL,
                contentType: 'application/json',
                data: JSON.stringify(domainUpdateData),
                success: function (data) {
                    console.info('Domain updated successfully');
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
            console.info('Show Keys: ' + isChecked);
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
            console.info('The user wants to regenerate the Production token');
        });
    };

    /*
     The function is used to attach the logic which will regenerate the Sandbox token
     */
    var attachRegenerateSandboxToken = function () {
        $('#btn-refresh-Sandbox-token').on('click', function () {
            console.info('The user wants to regenerate the Sandbox token');
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
            if (data.appName == null) {
                data.isAppNameAvailable = Views.translate('false');
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
            //alert('Resolve rendering!');
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
            if (data.appName == null) {
                data.isAppNameAvailable = Views.translate('false');
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
            console.info(APP_STORE.sandboxKeys);
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
            data.environment = Views.translate('Production');
            data.allowedDomains = Views.translate('ALL');
            data.validityTime = Views.translate('3600');
            if (data.appName == null) {
                data.isAppNameAvailable = Views.translate('false');
            }
        },
        subscriptions: [EV_APP_SELECT],
        afterRender: function () {
        }
    });

    Views.extend('defaultProductionDomainView', {
        id: 'updateProductionDomainView',
        partial: 'sub-domain-update-prod',
        subscriptions: [EV_GENERATE_PROD_TOKEN],
        afterRender: attachUpdateProductionDomains
    });

    //Sandbox view
    Views.extend('defaultProductionDomainView', {
        id: 'defaultSandboxDomainView',
        container: SAND_DOMAIN_CONTAINER,
        beforeRender: function (data) {
            data.environment = Views.translate('Sandbox');
            data.allowedDomains = Views.translate('ALL');
            data.validityTime = Views.translate('3600');
            if (data.appName == null) {
                data.isAppNameAvailable = Views.translate('false');
            }
        },
        afterRender: function () {
        }
    });

    Views.extend('defaultSandboxDomainView', {
        id: 'updateSandboxDomainVIew',
        partial: 'sub-domain-update-prod',
        afterRender: attachUpdateSandboxDomains,
        subscriptions: [EV_GENERATE_SAND_TOKEN]
    });

    /*
     API Subscription listing view
     */
    Views.extend('view', {
        id: 'defaultAPISubscriptionsView',
        container: SUBS_LIST_CONTAINER,
        partial: 'sub-listing',
        beforeRender: function (data) {
            var appName = data.appName;
            data.subscriptions = findSubscriptionDetails(appName);
        },
        subscriptions: [EV_APP_SELECT],
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
        if (!details || !details.prodKey) {
            return null;
        }
        var keys = {};
        keys.accessToken = details.prodKey;
        keys.environment = 'Production';
        keys.validityTime = details.prodValidityTime;
        keys.consumerKey = details.prodConsumerKey;
        keys.consumerSecret = details.prodConsumerSecret;

        return keys;
    };

    /*
     The function returns the Sandbox Keys of the given application
     */
    var findSandKeys = function (details) {
        if (!details || !details.sandboxKey) {
            return null;
        }
        var keys = {};
        keys.accessToken = details.sandboxKey;
        keys.environment = 'Sandbox';
        keys.validityTime = details.sandValidityTime;
        keys.consumerKey = details.sandboxConsumerKey;
        keys.consumerSecret = details.sandboxConsumerSecret;

        return keys;
    };

    var populateAppStore = function (appName) {
        var details = findAppDetails(appName);
        APP_STORE.appName = appName;
        APP_STORE.appDetails = details;
        APP_STORE.productionKeys = findProdKeys(details);
        APP_STORE.sandboxKeys = findSandKeys(details);
        //APP_STORE.showKeys = false;
    };

    var defaultAppName = $('#subscription_selection').val();
    populateAppStore(defaultAppName);
    events.publish(EV_APP_SELECT, {appName: defaultAppName});

    //Connect the events
    $('#subscription_selection').on('change', function () {
        var appName = $('#subscription_selection').val();
        APP_STORE = {};
        populateAppStore(appName);
        events.publish(EV_APP_SELECT, {appName: appName});
    });


});