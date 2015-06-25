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
asset.manager = function(ctx){

    var buildPublishedQuery = function(query) {
        query = query || {};
        var isLCEnabled = ctx.rxtManager.isLifecycleEnabled(ctx.assetType);
        //If lifecycles are not enabled then do nothing
        if(!isLCEnabled){
            log.warn('lifecycles disabled,not adding published states to search query');
            return query;
        }
        //Get all of the published assets
        var publishedStates = ctx.rxtManager.getPublishedStates(ctx.assetType) || [];
        //Determine if there are any published states
        if (publishedStates.length == 0) {
            return query;
        }
        //TODO: Even though an array is sent in only the first search value is accepted
        query.lcState=[publishedStates[0]];
        return query;
    };

    return {
        search: function(query, paging) {
            query=buildPublishedQuery(query);
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get:function(id){
            // log.info('Calling custom get of asset');
            return this._super.get.call(this,id);
        }
        /*search : function(query, paging) {
         var carbonAPI = require('carbon');
         var tenantDomain = carbonAPI.server.tenantDomain({
         tenantId : ctx.tenantId
         });
         var server = require('store').server;
         var user = server.current(ctx.session);
         var userName = '__wso2.am.anon__';

         if (user != null) {
         userName = user.username;
         }
         log.info('============== user name ==========' + userName);
         var apistore = require('apistore').apistore.instance(userName);
         var assetApi = apistore.getAllPaginatedAPIsByStatus(tenantDomain,
         0, 100, '');
         log.info('This is the custom APIM search method');
         log.info('============== api json  ==========' + assetApi);
         // return assetApi;

         var json = JSON.parse(assetApi);

         var apisArray = [];
         if (json.apis.length > 0) {
         apisArray = json.apis;
         }
         log.info('============== array ===========' + apisArray);
         return apisArray;
         }*/
    };
};

asset.configure = function(ctx) {
    return {
        meta: {
            search: {
                searchableFields: ['overview_provider', 'overview_name']
            }
        }
    }
};
asset.server = function(ctx) {
    return {
        onUserLoggedIn : function(){
            var userName=ctx.username;
            var apistore = require('apistore').apistore.instance(userName);
            var subscriber=apistore.getSubscriber(userName);
            if(!subscriber){
                apistore.addSubscriber(userName,ctx.tenantId);
            }
        },
        endpoints: {
            pages: [{
                        title: 'Prototyped APIs',
                        url: 'prototyped_apis',
                        path: 'prototyped_apis.jag'
                    }, {
                        title: 'My Applications',
                        url: 'my_applications',
                        path: 'my_applications.jag',
                        secured: true
                    },{
                        title: 'My Subscriptions',
                        url: 'my_subscriptions',
                        path: 'my_subscriptions.jag',
                        secured: true
                    },{
                        title: 'View Document',
                        url: 'document/view',
                        path: 'view_document.jag',
                        secured: true
                    },{
                        title: 'Forum',
                        url: 'forum',
                        path: 'forum.jag'
                    }, {
                        title: 'Statistics',
                        url: 'statistics',
                        path: 'statistics.jag'
                    }]
        }
    };
};
asset.renderer = function(ctx) {
    var twitterLink = function(assetUrl, asset) {
        var attr = asset.attributes || {};
        var description = attr.overview_description ? attr.overview_description : 'No description';
        return 'https://twitter.com/share?text=' + description + '&url=' + assetUrl;
    };
    var facebookLink = function(assetUrl, asset) {
        return 'https://facebook.com/sharer.php?u=' + assetUrl;
    };
    var gplusLink = function(assetUrl, asset) {
        return 'https://plus.google.com/share?url=' + assetUrl;
    };
    var diggitLink = function(assetUrl, asset) {
        return 'https://digg.com/submit?url=' + assetUrl;
    };
    return {
        details:function(page){
            // log.info('Details page rendered!!!');

            //=================== Getting subscription details ========================

            var carbonAPI = require('carbon');
            var server = require('store').server;
            var user = server.current(ctx.session);
            var tenantId = null, tenantDomain = null;

            var userName = '__wso2.am.anon__';

            if (user != null) {
                tenantId = user.tenantId;
                tenantDomain = carbonAPI.server.tenantDomain({
                                                                 tenantId : user.tenantId
                                                             });
                userName = user.username;
            }
            var lenI=0,lenJ=0,i,j,result,apidata,deniedTiers,tiers,appsList=[],subscribedToDefault=false,showSubscribe=false,status,selectedDefault=false;
            var apistore = require('apistore').apistore.instance(userName);

            var asset = page.assets;
            if (asset != null) {
                status = asset.lifecycleState;
                if (status != null) {
                    status = status.toUpperCase()
                }
            }
            var resultapi = apistore.getAPI(asset.attributes.overview_provider,
                asset.name, asset.attributes.overview_version);
            var apidata=resultapi.api;
            if (apidata != null) {
                tiers = apidata.tiers;
                if (status == "PUBLISHED" && user) {
                    showSubscribe = true;
                }
            }

            if (userName != '__wso2.am.anon__') {
                var applications = JSON.parse(apistore.getApplications(userName));

                var subscriptions = JSON.parse(apistore.getAPISubscriptions(asset.attributes.overview_provider,
                    asset.name, asset.attributes.overview_version, userName));
                if (applications) {
                    lenI = applications.length;
                }
                if (subscriptions) {
                    lenJ = subscriptions.length;
                }
                Label1:
                    for (i = 0; i < lenI; i++) {
                        var application = applications[i];
                        for (j = 0; j < lenJ; j++) {
                            var subscription = subscriptions[j];
                            if (subscription.applicationId == application.id) {
                                if (application.name == "DefaultApplication") {
                                    selectedDefault = true;
                                }
                                continue Label1;
                            } else {
                                if (application.name == "DefaultApplication") {
                                    subscribedToDefault = true;
                                }
                            }
                        }

                        if (application.status == "APPROVED") {
                            application.selectedDefault = selectedDefault;
                            appsList.push(application);
                        }
                    }

                result = apistore.getDeniedTiers();
                deniedTiers = result.tiers;
                var k, m, allowedTiers = [], denied = false, tiersAvailable = false;
                if (tiers) {
                    var tiersVal = tiers.split(",");
                    for (var m = 0; m < tiersVal.length; m++) {
                        if (deniedTiers) {
                            var deniedTiersVal = deniedTiers.split(",");
                            for (var k = 0; k < deniedTiersVal.length; k++) {
                                if (tiersVal[m].tierName == deniedTiersVal[k].tierName) {
                                    denied = true;
                                }
                            }
                        }
                        if (!denied) {
                            allowedTiers.push(tiersVal[m]);
                            tiersAvailable = true;
                        }
                        denied = false;
                    }
                }

                page.applications = appsList;
                page.tiersAvailable = tiersAvailable;
                page.tiers = allowedTiers;
                page.subscribedToDefault = subscribedToDefault;
            }

            page.showSubscribe = showSubscribe;
            page.api = apidata;
            page.status = status;

            //Setting throttling infomation
            var uriTemplates = apidata.templates;
            var tiersList = apistore.getTiers().tiers;
            var templateRepresentation =  [];
            if (uriTemplates != null && uriTemplates.length > 0) {

                for (var i = 0; i < uriTemplates.length; i++) {
                    var methods = uriTemplates[i][1].split(",");
                    var authTypes = uriTemplates[i][2].split(",");
                    var throttlingTiers = uriTemplates[i][3].split(",");
                    var getThrottlingTier, postThrottlingTier, deleteThrottlingTier, optionsThrottlingTier, putThrottlingTier;
                    var method;
                    var tierLimitDescriptions = [];
                    var tierLimitDes;

                    for (var n = 0; n < methods.length; n++) {
                        method = methods[n];
                        if (authTypes[n] == 'Application_User') {
                            authTypes[n] = 'Application User';
                        } else if (method == "GET") {
                            getThrottlingTier = throttlingTiers[n];
                        } else if (method == "POST") {
                            postThrottlingTier = throttlingTiers[n];
                        } else if (method == "PUT") {
                            putThrottlingTier = throttlingTiers[n];
                        } else if (method == "DELETE") {
                            deleteThrottlingTier = throttlingTiers[n];
                        } else if (method == "OPTIONS") {
                            optionsThrottlingTier = throttlingTiers[n];
                        }

                        for (var k = 0; k < tiersList.length; k++) {
                            if (tiersList[k].tierName == getThrottlingTier) {
                                tierLimitDes = "GET " + tiersList[k].tierDescription;
                                break;
                            } else if (tiersList[k].tierName == postThrottlingTier) {
                                tierLimitDes = "POST " + tiersList[k].tierDescription;
                                break;
                            } else if (tiersList[k].tierName == putThrottlingTier) {
                                tierLimitDes = "PUT " + tiersList[k].tierDescription;
                                break;
                            } else if (tiersList[k].tierName == deleteThrottlingTier) {
                                tierLimitDes = "DELETE " + tiersList[k].tierDescription;
                                break;
                            } else if (tiersList[k].tierName == optionsThrottlingTier) {
                                tierLimitDes = "OPTIONS " + tiersList[k].tierDescription;
                                break;
                            }
                        }
                        tierLimitDescriptions.push({"description" : tierLimitDes})
                    }

                    templateRepresentation.push({
                                                    "templateName": uriTemplates[i][0],
                                                    "tierLimitDescriptions": tierLimitDescriptions
                                                });
                }
            }
            page.templateRepresentation = templateRepresentation;
            //Set language specific helper messages and names
            var locale = require('/extensions/assets/api/locale/locale_default.json');
            page.locale = locale;
            var documents = apistore.getAllDocumentation(asset.attributes.overview_provider,
                                                         asset.name, asset.attributes.overview_version, userName).documents;
            var apiIdentifier = {};
            apiIdentifier.name = asset.attributes.overview_name;
            apiIdentifier.version = asset.name;
            apiIdentifier.provider = asset.attributes.overview_provider;
            page.apiIdentifier = apiIdentifier;

            var processedDocuments = [];
            var type, index;
            var docs, empty = true;
            var processedTypes = [], processed;
            for(var i = 0; i < documents.length; i++) {
                empty = false;
                docs = [], index = 0;
                processed = false;
                type = documents[i].type;
                for(var k = 0 ; k < processedTypes.length; k++) {
                    if (type == processedTypes[k]) {
                        processed = true;
                    }
                }

                if(processed) {
                    continue;
                }

                for(var j = 0; j <documents.length; j++) {
                    if(type == documents[j].type) {
                        docs[index] = documents[j];
                        index++;
                    }
                }

                processedDocuments.push({
                                            "type" : type,
                                            "docs" : docs
                                        });
                processedTypes.push(type);
            }
            processedDocuments.empty = empty;
            page.processedDocuments = processedDocuments;
            page.tenant = {"tenantId" : tenantId, "tenantDomain" : tenantDomain};
        },
        pageDecorators: {
            populateEndPoints : function(page){
                if (page.assets && page.assets.id) {
                    var httpEndpoint,httpsEndpoint;
                    if (page.api.serverURL.split(",")[0] == 'Production and Sandbox') {
                        httpEndpoint = page.api.serverURL.split(",")[1];
                        httpsEndpoint = page.api.serverURL.split(",")[2];
                    }
                    var isDefaultVersion=page.api.isDefaultVersion;

                    page.assets.httpEndpoint = httpEndpoint;
                    page.assets.httpsEndpoint = httpsEndpoint;
                    page.assets.isDefaultVersion = isDefaultVersion;

                    //var prodEps = parse(page.assets.attributes.overview_endpointConfig).production_endpoints;
                    //var sandBoxEps = parse(page.assets.attributes.overview_endpointConfig).sandbox_endpoints;
                    //
                    //if(prodEps != null){
                    //    var prodEpArry = [];
                    //
                    //    for(var i = 0; prodEps.length > i; i++){
                    //        prodEpArry.push(prodEps[i].url);
                    //    }
                    //
                    //    page.assets.production_endpoint = prodEpArry.join(',');
                    //
                    //}else {
                    //    page.assets.production_endpoint = parse(page.assets.attributes.overview_endpointConfig).production_endpoints.url;
                    //}
                    //
                    //if(sandBoxEps != null){
                    //    var sandBoxEpArry = [];
                    //
                    //    for(var i = 0; prodEps.length > i; i++){
                    //        sandBoxEpArry.push(sandBoxEps[i].url);
                    //    }
                    //
                    //    page.assets.sandbox_endpoint = sandBoxEpArry.join(',');
                    //
                    //} else if(parse(page.assets.attributes.overview_endpointConfig).sandbox_endpoints != null && parse(page.assets.attributes.overview_endpointConfig).sandbox_endpoints.url != null) {
                    //    page.assets.sandbox_endpoint = parse(page.assets.attributes.overview_endpointConfig).sandbox_endpoints.url;
                    //}
                }
            },
            socialSitePopulator: function(page, meta) {
                var utils = require('utils');
                //If the
                if (!utils.reflection.isArray(page.assets || [])) {
                    var asset = page.assets;
                    var assetUrl = this.buildAssetPageUrl('api', '/details/' + asset.id);
                    page.social_sites = {};
                    var facebook = page.social_sites.facebook = {};
                    var gplus = page.social_sites.gplus = {};
                    var twitter = page.social_sites.twitter = {};
                    var diggit = page.social_sites.diggit = {};
                    facebook.href = facebookLink(assetUrl, asset);
                    gplus.href = gplusLink(assetUrl, asset);
                    twitter.href = twitterLink(assetUrl, asset);
                    diggit.href = diggitLink(assetUrl, asset);
                }
            },
            embedLinkPopulator: function(page, meta) {
                var utils = require('utils');
                if (!utils.reflection.isArray(page.assets || [])) {
                    var asset = page.assets;
                    var attributes = asset.attributes || {};
                    var widgetLink = '/apis/widget';
                    var assetUrl = widgetLink+'?name=' + asset.name + '&version=' + attributes.overview_version + '&provider=' + attributes.overview_provider;
                    page.api_embed_links = '<iframe width="450" height="120" src="' + assetUrl + '" frameborder="0" allowfullscreen></iframe>';
                }
            },  populateApiActionBar: function(page,meta){
                var action = {};
                action.url = '/asts/api/list';
                action.iconClass ='fa-cogs';
                action.name ='APIs';
                page.actionBar.actions.push(action);
                action = {};
                action.url = '/asts/api/prototyped_apis';
                action.iconClass ='fa-cog';
                action.name ='Prototyped APIs';
                page.actionBar.actions.push(action);
                action = {};
                action.url = '/asts/api/my_applications';
                action.iconClass ='fa-briefcase';
                action.name ='My Applications';
                page.actionBar.actions.push(action);
                action = {};
                action.url = '/asts/api/my_subscriptions';
                action.iconClass ='fa-tags';
                action.name ='My Subscriptions';
                page.actionBar.actions.push(action);
                action = {};
                action.url = '/asts/api/forum';
                action.iconClass ='fa-comment-o';
                action.name ='Forum';
                page.actionBar.actions.push(action);
                action = {};
                action.url = '/asts/api/statistics';
                action.iconClass ='fa-line-chart';
                action.name ='Statistics';
                page.actionBar.actions.push(action);
        }
        }
    }
}
