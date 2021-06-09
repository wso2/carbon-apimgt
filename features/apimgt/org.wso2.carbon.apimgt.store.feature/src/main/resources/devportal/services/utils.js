/* eslint-disable*/
// Disable eslint check since this is only used in jaggeryjs codes
/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
var app = require('/site/public/theme/settings.js').Settings.app;

var utils = Packages.org.wso2.carbon.apimgt.impl.utils.APIUtil;
include("/services/constants.jag");

var getLoopbackOrigin = function() {
    var mgtTransportPort = utils.getCarbonTransportPort("https");
    var origin = 'https://' + app.origin.host + ":" + mgtTransportPort;
    return origin;
};

function getIDPOrigin() {
    return utils.getExternalIDPOrigin();
}

function getIDPCheckSessionEndpoint() {
    return utils.getExternalIDPCheckSessionEndpoint();
}

var getTenantBaseStoreContext = function() {
    var tenantDomain = getTenantDomain();
    var tenantContext = utils.getTenantBasedDevPortalContext(tenantDomain);
    if (tenantContext != null) {
        return tenantContext
    } else {
        return app.context;
    }
};

var getTenantBasedLoginCallBack = function() {
    var tenantDomain = getTenantDomain();
    var storeDomainMapping = utils.getTenantBasedStoreDomainMapping(tenantDomain);
    if (storeDomainMapping != null) {
       if (storeDomainMapping.get('login') != null) {
        return storeDomainMapping.get('login');
       }
        return "https://"+storeDomainMapping.get('customUrl')+LOGIN_CALLBACK_URL_SUFFIX;
    }else{
    return null;
    }
};

var getTenantBasedLogoutCallBack = function() {
    var tenantDomain = getTenantDomain();
    var storeDomainMapping = utils.getTenantBasedStoreDomainMapping(tenantDomain);
    if (storeDomainMapping != null) {
       if (storeDomainMapping.get('logout') != null) {
        return storeDomainMapping.get('logout');
       }
        return "https://"+storeDomainMapping.get('customUrl')+LOGOUT_CALLBACK_URL_SUFFIX;
    } else {
    return null;
    }
};

var isPerTenantServiceProviderEnabled = function() {
    var tenantDomain = getTenantDomain();

    var perTenantServiceProviderEnabled = utils.isPerTenantServiceProviderEnabled(tenantDomain);
    return perTenantServiceProviderEnabled;
};

var getTenantDomain = function() {

    var tenantDomain = request.getParameter("tenant");
    if (tenantDomain == null) {
        tenantDomain = request.getHeader("X-WSO2-Tenant");
        if (tenantDomain == null) {
            tenantDomain = "carbon.super";
        }
    }
    return tenantDomain;
};

var getCustomUrlEnabledDomain = function() {
    var tenantDomain = request.getHeader("X-WSO2-Tenant");
    return tenantDomain;
};

var getTenantBasedCustomUrl = function() {
    var tenantDomain = getTenantDomain();
    var storeDomainMapping = utils.getTenantBasedStoreDomainMapping(tenantDomain);
    if (storeDomainMapping != null) {
        return "https://" + storeDomainMapping.get('customUrl');
    } else {
        return null;
    }
};

var getServiceProviderTenantDomain = function() {
    var tenantDomain = getTenantDomain();
    if (isPerTenantServiceProviderEnabled()) {
        return tenantDomain;
    } else {
        return "carbon.super";
    }
};

var isEnableEmailUserName = function() {
    var CarbonUtils = Packages.org.wso2.carbon.utils.CarbonUtils;
    var carbonUtils = new CarbonUtils();
    var isEnableEmailUserName = carbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName");
    if (isEnableEmailUserName != null)
        return isEnableEmailUserName;
    else
        return false;
};

