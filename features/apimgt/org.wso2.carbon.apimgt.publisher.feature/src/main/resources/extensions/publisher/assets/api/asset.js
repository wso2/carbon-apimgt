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
asset.server = function(ctx) {
    return {
        endpoints: {
            pages: [{
                title: 'Implement an API',
                url: 'implement',
                path: 'implement.jag'
            }, {
                title: 'Manage an API',
                url: 'manage',
                path: 'manage.jag'
            },{
                title:'Documents',
                url:'documents',
                path:'documents.jag'
            },{
                title:'Tier Permissions',
                url:'tier_permissions',
                path:'tier_permissions.jag'
            }],
            apis: [{
                url: 'endpoints',
                path: 'endpoints.jag'
            }, {
                url: 'prototype',
                path: 'prototype.jag'
            },{
                url:'tiers',
                path:'tiers.jag'
            }]
        }
    }
};
asset.configure = function(ctx) {
    return {
        table: {
            overview: {
                thumbnail: {
                    type: 'file'
                }
            }
        },
        meta: {
            thumbnail: 'overview_thumbnail'
        }
    };
};
asset.renderer = function(ctx) {
    var type = ctx.assetType;
    var listLinks = function(ribbon, utils) {
        ribbon.enabled = false;
        ribbon.list = [];
    };
    var apiLinks = function(ribbon, utils) {
        var navList = utils.navList();
        navList.push('Overview', 'icon-list-alt', utils.buildUrl('overview'));
        navList.push('Lifecycle', 'icon-list-alt', utils.buildUrl('lifecycle'));
        navList.push('Version', 'icon-list-alt', utils.buildUrl('version'));
        ribbon.enabled = true;
        ribbon.list = navList;
    };
    var buildAddLeftNav = function (page, util) {
        return [];
    };
    var buildListLeftNav = function(page, util) {
        var navList = util.navList();
        navList.push('Add ' + type,'fa-plus', util.buildUrl('create'));
        navList.push('All Statistics', 'fa-area-chart', '/asts/' + type + '/statistics');
	navList.push('Subscriptions', 'fa fa-bookmark', '/asts/' + type + '/statistics');
	navList.push('Statistics', 'fa-area-chart', '/asts/' + type + '/statistics');
	navList.push('Tier Permissions', 'fa fa-cog', '/asts/' + type + '/statistics');
        //navList.push('Configuration', 'icon-dashboard', util.buildUrl('configuration'));
        return navList.list();
    };
    var buildDefaultLeftNav = function(page, util) {
        var id = page.assets.id;
        var navList = util.navList();
        navList.push('Edit', 'fa-pencil', util.buildUrl('update') + '/' + id);
        navList.push('Overview', 'fa-list-alt', util.buildUrl('details') + '/' + id);
        navList.push('Life Cycle' , 'fa-recycle', util.buildUrl('lifecycle') + '/' + id);
        return navList.list();
    };
    return {
        pageDecorators: {
            ribbon: function(page) {
                var ribbon = page.ribbon = {};
                ribbon.isSearchEnabled = true;
                switch (page.meta.pageName) {
                    case 'list':
                        listLinks(ribbon, this);
                        break;
                    case 'create':
                        listLinks(ribbon, this);
                        break;
                    case 'implement':
                        listLinks(ribbon, this);
                        break;
                    case 'manage':
                        listLinks(ribbon, this);
                        break;
                    default:
                        apiLinks(ribbon, this);
                        break;
                }
                return page;
            },
            leftNav: function (page) {
                if(log.isDebugEnabled()){
                    log.debug('Using default leftNav');
                }
                switch (page.meta.pageName) {
                    case 'list':
                        page.leftNav = buildListLeftNav(page, this);
                        break;
                    case 'create':
                        page.leftNav = buildListLeftNav(page, this);
                        break;
                    case 'statistics':
                        page.leftNav = buildListLeftNav(page, this);
                        break;
                    default:
                        page.leftNav = buildListLeftNav(page, this);
                        break;
                }
                return page;
            }
        }
    };
};
