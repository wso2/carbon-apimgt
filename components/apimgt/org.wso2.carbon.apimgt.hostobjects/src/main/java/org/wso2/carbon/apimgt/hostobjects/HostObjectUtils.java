/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.hostobjects;

import java.io.IOException;
import java.util.Comparator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;

public class HostObjectUtils {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    private static ConfigurationContextService configContextService = null;

    public static void setConfigContextService(ConfigurationContextService configContext) {
        HostObjectUtils.configContextService = configContext;
    }

    public static ConfigurationContext getConfigContext() throws APIManagementException {
        if (configContextService == null) {
            throw new APIManagementException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (APIManagementException e) {
            log.error(e.getMessage());
            return null;

        }
    }





    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static class RequiredUserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (!filed1.getRequired() && filed2.getRequired()){
            	return 1;
            }

            if (filed1.getRequired() && filed2.getRequired()){
            	return 0;
            }

            if (filed1.getRequired() && !filed2.getRequired()){
            	return -1;
            }

            return 0;
        }

    }
    public static class UserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed1.getDisplayOrder() < filed2.getDisplayOrder()) {
                return -1;
            }
            if (filed1.getDisplayOrder() == filed2.getDisplayOrder()) {
                return 0;
            }
            if (filed1.getDisplayOrder() > filed2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }

    /**
    *This methods is to check whether stat publishing is enabled
    * @return boolean
     */
    protected static boolean checkDataPublishingEnabled() {
        return APIUtil.isAnalyticsEnabled();
    }

    /**
     * This method will clear recently added API cache.
     * @param username
     */
    public static void invalidateRecentlyAddedAPICache(String username){
        try{
            PrivilegedCarbonContext.startTenantFlow();
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            boolean isRecentlyAddedAPICacheEnabled =
                  Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE));

            if (username != null && isRecentlyAddedAPICacheEnabled) {
                String tenantDomainFromUserName = MultitenantUtils.getTenantDomain(username);
                if (tenantDomainFromUserName != null &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomainFromUserName)) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainFromUserName,
                                                                                          true);
                } else {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                           .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache("RECENTLY_ADDED_API")
                       .remove(username + ":" + tenantDomainFromUserName);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    protected static boolean isUsageDataSourceSpecified() {
        try {
            return (null != HostObjectComponent.getDataSourceService().
                    getDataSource(APIConstants.API_USAGE_DATA_SOURCE_NAME));
        } catch (DataSourceException e) {
            return false;
        }
    }

    protected static boolean isStatPublishingEnabled() {
            return APIUtil.isAnalyticsEnabled();
    }

    public static NativeObject sendHttpHEADRequest(String urlVal, String invalidStatusCodesRegex) {
        boolean isConnectionError = true;
        String response = null;
        NativeObject data = new NativeObject();
        //HttpClient client = new DefaultHttpClient();
        HttpHead head = new HttpHead(urlVal);
        //Change implementation to use http client as default http client do not work properly with mutual SSL.
        org.apache.commons.httpclient.HttpClient clientnew = new org.apache.commons.httpclient.HttpClient();
        // extract the host name and add the Host http header for sanity
        head.addHeader("Host", urlVal.replaceAll("https?://", "").replaceAll("(/.*)?", ""));
        clientnew.getParams().setParameter("http.socket.timeout", 4000);
        clientnew.getParams().setParameter("http.connection.timeout", 4000);
        HttpMethod method = new HeadMethod(urlVal);

        if (System.getProperty(APIConstants.HTTP_PROXY_HOST) != null &&
                System.getProperty(APIConstants.HTTP_PROXY_PORT) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Proxy configured, hence routing through configured proxy");
            }
            String proxyHost = System.getProperty(APIConstants.HTTP_PROXY_HOST);
            String proxyPort = System.getProperty(APIConstants.HTTP_PROXY_PORT);
            clientnew.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
        }

        try {
            int statusCodeNew = clientnew.executeMethod(method);
            //Previous implementation
            // HttpResponse httpResponse = client.execute(head);
            String statusCode = String.valueOf(statusCodeNew);//String.valueOf(httpResponse.getStatusLine().getStatusCode());
            String reasonPhrase = String.valueOf(statusCodeNew);//String.valueOf(httpResponse.getStatusLine().getReasonPhrase());
            //If the endpoint doesn't match the regex which specify the invalid status code, it will return success.
            if (!statusCode.matches(invalidStatusCodesRegex)) {
                if (log.isDebugEnabled() && statusCode.equals(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED))) {
                    log.debug("Endpoint doesn't support HTTP HEAD");
                }
                response = "success";
                isConnectionError = false;

            } else {
                //This forms the real backend response to be sent to the client
                data.put("statusCode", data, statusCode);
                data.put("reasonPhrase", data, reasonPhrase);
                response = "";
                isConnectionError = false;
            }
        } catch (IOException e) {
            // sending a default error message.
            log.error("Error occurred while connecting to backend : " + urlVal + ", reason : " + e.getMessage(), e);
            String[] errorMsg = e.getMessage().split(": ");
            if (errorMsg.length > 1) {
                response = errorMsg[errorMsg.length - 1]; //This is to get final readable part of the error message in the exception and send to the client
                isConnectionError = false;
            }
        } finally {
            method.releaseConnection();
        }
        data.put("response", data, response);
        data.put("isConnectionError", data, isConnectionError);
        return data;
    }
}
