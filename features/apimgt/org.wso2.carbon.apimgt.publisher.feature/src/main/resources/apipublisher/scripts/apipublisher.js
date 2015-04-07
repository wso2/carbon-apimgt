/*
* Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

//TODO add proper introduction to the module
var apipublisher = {};

(function (apipublisher) {


    var log = new Log("jaggery-modules.api-manager.publisher");


    apipublisher.getAllProviders = function (username) {
        var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
        var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
        return APIProvider.getAllProviders();
    };


    apipublisher.designAPI = function (api,username) {
        var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
        var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
        return APIProvider.designAPI(api);
    };
    apipublisher.implementAPI = function (api,username) {
        var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
        var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
        return APIProvider.implementAPI(api);
    };
    apipublisher.manageAPI = function (api,username) {
        var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
        var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
        return APIProvider.manageAPI(api);
    };
    apipublisher.updateDesignAPI = function (api,username) {
        var APIManagerFactory = Packages.org.wso2.carbon.apimgt.impl.APIManagerFactory;
        var APIProvider =  APIManagerFactory.getInstance().getAPIProvider(username);
        return APIProvider.updateDesignAPI(api);
    };
           
})(apipublisher);

