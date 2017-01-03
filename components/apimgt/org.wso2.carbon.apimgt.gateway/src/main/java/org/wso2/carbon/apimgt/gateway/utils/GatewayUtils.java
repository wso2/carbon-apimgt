/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class GatewayUtils {

    private static final Log log = LogFactory.getLog(GatewayUtils.class);

    public static boolean isClusteringEnabled() {
        ClusteringAgent agent = ServiceReferenceHolder.getInstance().getServerConfigurationContext().
                                                                            getAxisConfiguration().getClusteringAgent();
        if(agent != null) {
            return true;
        }
        return false;
    }

    public static <T> Map<String, T> generateMap(Collection<T> list) {
        Map<String, T> map = new HashMap<String, T>();
        for (T el : list) {
            map.put(el.toString(), el);
        }
        return map;
    }

    /**
     * Extracts the IP from Message Context.
     * @param messageContext Axis2 Message Context.
     * @return IP as a String.
     */
    public static String getIp(MessageContext messageContext){

        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) messageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        // Assigning an Empty String so that when doing comparisons, .equals method can be used without explicitly
        // checking for nullity.
        String remoteIP = "";
        //Check whether headers map is null and x forwarded for header is present
        if (transportHeaderMap != null) {
            remoteIP = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client by looking at x forded for header and  if it's empty get remote address
        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) messageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        return remoteIP;
    }

    /**
     * Can be used to extract Query Params from {@code org.apache.axis2.context.MessageContext}.
     * @param messageContext The Axis2 MessageContext
     * @return A Map with Name Value pairs.
     */
    public static Map<String,String> getQueryParams(MessageContext messageContext){
        String queryString = (String) messageContext.getProperty(NhttpConstants.REST_URL_POSTFIX);
        if (!StringUtils.isEmpty(queryString)) {
            if (queryString.indexOf("?") > -1) {
                queryString = queryString.substring(queryString.indexOf("?") + 1);
            }
            String[] queryParams = queryString.split("&");
            Map<String, String> queryParamsMap = new HashMap<String, String>();
            String[] queryParamArray;
            String queryParamName, queryParamValue = "";
            for (String queryParam : queryParams) {
                queryParamArray = queryParam.split("=");
                if (queryParamArray.length == 2) {
                    queryParamName = queryParamArray[0];
                    queryParamValue = queryParamArray[1];
                } else {
                    queryParamName = queryParamArray[0];
                }
                queryParamsMap.put(queryParamName, queryParamValue);
            }

           return queryParamsMap;
        }
        return null;
    }

    public static Map getJWTClaims(AuthenticationContext authContext){
        String[] jwtTokenArray = authContext.getCallerToken().split(Pattern.quote("."));
        // decoding JWT
        try {
            byte[] jwtByteArray = Base64.decodeBase64(jwtTokenArray[1].getBytes("UTF-8"));
            String jwtAssertion = new String(jwtByteArray, "UTF-8");
            JSONParser parser = new JSONParser();
            return  (Map) parser.parse(jwtAssertion);
        } catch (UnsupportedEncodingException e) {
            log.error("Error while decoding jwt header", e);
        } catch (ParseException e) {
            log.error("Error while parsing jwt header", e);
        }
        return null;
    }
    
    /**
     * Get the config system registry for tenants
     *
     * @param tenantDomain
     *            - The tenant domain
     * @return - A UserRegistry instance for the tenant
     * @throws APIManagementException
     */
    public static UserRegistry getRegistry(String tenantDomain) throws APIManagementException {
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                   .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry registry;
        try {
            registry = RegistryServiceHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            String msg = "Failed to get registry instance for the tenant : " + tenantDomain + e.getMessage();
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return registry;
    }

    /**
     * Delete the given registry property from the given tenant registry path
     * 
     * @param propertyName
     *            property name
     * @param path
     *            resource path
     * @param tenantDomain
     * @throws APIManagementException
     */
    public static void deleteRegistryProperty(String propertyName, String path, String tenantDomain)
                                                                                        throws APIManagementException {
        UserRegistry registry = getRegistry(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                   .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }
        try {
            Resource resource = registry.get(path);
            if (resource != null && resource.getProperty(propertyName) != null) {
                resource.removeProperty(propertyName);
                registry.put(resource.getPath(), resource);
                resource.discard();
            }
        } catch (RegistryException e) {
            throw new APIManagementException("Error while reading registry resource " + path + " for tenant " +
                                             tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add/Update the given registry property from the given tenant registry
     * path
     * 
     * @param propertyName
     *            property name
     * @param propertyValue
     *            property value
     * @param path
     *            resource path
     * @param tenantDomain
     * @throws APIManagementException
     */
    public static void setRegistryProperty(String propertyName, String propertyValue, String path, String tenantDomain)
                                                                                       throws APIManagementException {
        UserRegistry registry = getRegistry(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                   .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }
        try {
            Resource resource = registry.get(path);
            // add or update property
            if (resource.getProperty(propertyName) != null) {
                resource.setProperty(propertyName, propertyValue);
            } else {
                resource.addProperty(propertyName, propertyValue);
            }
            registry.put(resource.getPath(), resource);
            resource.discard();
        } catch (RegistryException e) {
            throw new APIManagementException("Error while reading registry resource " + path + " for tenant " +
                                             tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Returns the alias string of API endpoint security password
     * 
     * @param apiProviderName
     * @param apiName
     * @param version
     * @return
     */
    public static String getAPIEndpointSecretAlias(String apiProviderName, String apiName, String version) {
        String secureVaultAlias = apiProviderName + "--" + apiName + version;
        return secureVaultAlias;
    }
}
