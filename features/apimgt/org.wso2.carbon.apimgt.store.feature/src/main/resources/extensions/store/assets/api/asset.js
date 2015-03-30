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
        endpoints: {
            pages: [{
                title: 'Prototyped APIs',
                url: 'prototyped_apis',
                path: 'prototyped_apis.jag'
            }, {
                title: 'My Applications',
                url: 'my_applications',
                path: 'my_applications.jag'
            }, {
                title: 'My Subscriptions',
                url: 'my_subscriptions',
                path: 'my_subscriptions.jag'
            }, {
                title: 'Forum',
                url: 'forum',
                path: 'forum.jag'
            }, {
                title: 'Statistics',
                url: 'statistics',
                path: 'statistics.jag'
            }, {
                title: 'Tools',
                url: 'tools',
                path: 'tools.jag'
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
        pageDecorators: {
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
            }
        }
    };
}