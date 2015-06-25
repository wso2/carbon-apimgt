/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
app.dependencies = ['store-common'];
app.server = function(ctx) {
    return {
        endpoints: {
            apis: [{
                url: 'widget',
                path: 'widget.jag',
                secured:false
            }, {
                url: 'applications',
                path: 'applications.jag',
                secured:true
            }, {
                url: 'application',
                path: 'application.jag',
                secured:true
            }, {
                url: 'document/download',
                path: 'download.jag',
                secured:true
            },{
                url: 'apisubscriptions',
                path: 'api-subscriptions.jag',
                secured:false
            }]
        }
    }
};

app.apiHandlers = function(ctx) {
    return {
        onApiLoad: function() {
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                //ctx.res.status='401';//sendRedirect(ctx.appContext+'/login');
                print('{ error:"Authentication error" }'); //TODO: Fix this to return a proper status code
                return false;
            }
            return true;
        }
    };
};
