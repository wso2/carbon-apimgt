package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTenantConfig;

public class DomainMappingUtils {
    private static final Log log = LogFactory.getLog(APIUtil.class);

    /**
     * Returns a map of publisher / gateway / store domains for the tenant
     *
     * @return a Map of domain names for tenant
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if an error occurs when loading tiers from the AM_SYSTEM_CONFIGS table
     */
    public static Map<String, String> getDomainMappings(String tenantDomain, String appType)
            throws APIManagementException {

        Map<String, String> domains = new HashMap<String, String>();
        try {
            JSONObject tenantConfig = getTenantConfig(tenantDomain);
            if (tenantConfig.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS)) {
                JSONObject customUrlsJsonObject = (JSONObject) tenantConfig.get(
                        APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS);
                if (appType.equals(APIConstants.API_DOMAIN_MAPPINGS_STORE)) {
                    appType = APIConstants.API_DOMAIN_MAPPINGS_DEVPORTAL;
                }
                if (customUrlsJsonObject.containsKey(appType)) {
                    JSONObject appTypeJsonObject = (JSONObject) customUrlsJsonObject.get(appType);
                    if (appTypeJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN)) {
                        domains.put(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URL,
                                (String) appTypeJsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN));
                    }
                }
            }
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the tenant domain mappings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return domains;
    }

    public static String getTenantBasedDevPortalContext(String tenantDomain) throws APIManagementException {

        String context = null;
        try {
            JSONObject tenantConfig = getTenantConfig(tenantDomain);
            if (tenantConfig.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS)) {
                JSONObject customUrlsJsonObject = (JSONObject) tenantConfig.get(
                        APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS);
                if (customUrlsJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_DEVPORTAL)) {
                    JSONObject appTypeJsonObject = (JSONObject) customUrlsJsonObject.get(
                            APIConstants.API_DOMAIN_MAPPINGS_DEVPORTAL);
                    if (appTypeJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT)) {
                        context = (String) appTypeJsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
                    } else {
                        context = "";
                    }
                }
            }
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the devPortal tenant context mappings";
            throw new APIManagementException(msg, e);
        }
        return context;
    }

    public static Map getTenantBasedStoreDomainMapping(String tenantDomain) throws APIManagementException {

        try {
            JSONObject tenantConfig = getTenantConfig(tenantDomain);
            if (tenantConfig.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS)) {
                JSONObject customUrlsJsonObject = (JSONObject) tenantConfig.get(
                        APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS);
                if (customUrlsJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_DEVPORTAL)) {
                    JSONObject appTypeJsonObject = (JSONObject) customUrlsJsonObject.get(
                            APIConstants.API_DOMAIN_MAPPINGS_DEVPORTAL);
                    if (appTypeJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN)) {
                        Map<String, String> domain = new HashMap<String, String>();
                        domain.put(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URL,
                                (String) appTypeJsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN));
                        return domain;
                    }
                }
            }
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the devPortal tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    public static Map getTenantBasedPublisherDomainMapping(String tenantDomain) throws APIManagementException {

        try {
            JSONObject tenantConfig = getTenantConfig(tenantDomain);
            if (tenantConfig.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS)) {
                JSONObject customUrlsJsonObject = (JSONObject) tenantConfig.get(
                        APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS);
                if (customUrlsJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER)) {
                    JSONObject appTypeJsonObject = (JSONObject) customUrlsJsonObject.get(
                            APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER);
                    if (appTypeJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN)) {
                        Map<String, String> domain = new HashMap<String, String>();
                        domain.put(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URL,
                                (String) appTypeJsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_DOMAIN));
                        return domain;
                    }
                }
            }
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the publisher tenant domain mappings";
            throw new APIManagementException(msg, e);
        }
        return null;
    }

    public static String getTenantBasedPublisherContext(String tenantDomain) throws APIManagementException {

        String context = null;
        try {
            JSONObject tenantConfig = getTenantConfig(tenantDomain);
            if (tenantConfig.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS)) {
                JSONObject customUrlsJsonObject = (JSONObject) tenantConfig.get(
                        APIConstants.API_DOMAIN_MAPPINGS_CUSTOM_URLS);
                if (customUrlsJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER)) {
                    JSONObject appTypeJsonObject = (JSONObject) customUrlsJsonObject.get(
                            APIConstants.API_DOMAIN_MAPPINGS_PUBLISHER);
                    if (appTypeJsonObject.containsKey(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT)) {
                        context = (String) appTypeJsonObject.get(APIConstants.API_DOMAIN_MAPPINGS_CONTEXT);
                    } else {
                        context = "";
                    }
                }
            }
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the publisher tenant context mappings";
            throw new APIManagementException(msg, e);
        }
        return context;
    }
}
