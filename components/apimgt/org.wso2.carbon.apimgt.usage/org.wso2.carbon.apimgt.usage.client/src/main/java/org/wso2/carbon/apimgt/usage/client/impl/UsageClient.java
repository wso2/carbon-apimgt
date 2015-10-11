package org.wso2.carbon.apimgt.usage.client.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.APIUsageStatisticsClient;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.util.RESTClientConstant;
import org.wso2.carbon.apimgt.usage.client.util.RestClientUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rukshan on 10/6/15.
 */
public class UsageClient {
    private static final Log log = LogFactory.getLog(UsageClient.class);

    public static void initializeDataSource(int dataSourceType) throws APIMgtUsageQueryServiceClientException {
        if (dataSourceType == RESTClientConstant.DATASOURCE_REST_TYPE) {
            APIUsageStatisticsRestClientImpl.initializeDataSource();
            log.info("Initializing REST Usage Statistics Client");
        } else if (dataSourceType == RESTClientConstant.DATASOURCE_RDBMS_TYPE) {
            APIUsageStatisticsRdbmsClientImpl.initializeDataSource();
            log.info("Initializing RDBMS Usage Statistics Client");
        } else {
            log.error("Unknown Statistic client information found");
        }
    }

    public static APIUsageStatisticsClient getClient() {
        if (isDataPublishingEnabled()) {
            try {

                if (getClientType() == RESTClientConstant.DATASOURCE_REST_TYPE) {
                    return new APIUsageStatisticsRestClientImpl("");
                } else if (getClientType() == RESTClientConstant.DATASOURCE_RDBMS_TYPE) {
                    return new APIUsageStatisticsRdbmsClientImpl("");
                } else {
                    log.error("Unknown Statistic client information found");
                    return null;
                }
            } catch (APIMgtUsageQueryServiceClientException e) {
                log.error("Error instantiating Statistic Client", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean isDataPublishingEnabled() {
        APIManagerAnalyticsConfiguration con = APIManagerAnalyticsConfiguration.getInstance();
        return con.isAnalyticsEnabled();
    }

    public static String getSubscriberCountByAPIs(String loggedUser) throws APIManagementException {
        String providerName = null;

        APIProvider apiProvider; //= getAPIProvider(thisObj);
        apiProvider = APIManagerFactory.getInstance().getAPIProvider(loggedUser);

        List<SubscriberCountByAPIs> list = new ArrayList<SubscriberCountByAPIs>();
        boolean isTenantFlowStarted = false;
        try {
            providerName = APIUtil.replaceEmailDomain(loggedUser);
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(providerName));
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }

            if (providerName != null) {
                List<API> apiSet;
                if (providerName.equals("__all_providers__")) {
                    apiSet = apiProvider.getAllAPIs();
                } else {
                    apiSet = apiProvider.getAPIsByProvider(APIUtil.replaceEmailDomain(providerName));
                }

                //                List<SubscriberCountByAPIs> subscriptionData = new ArrayList<SubscriberCountByAPIs>();
                //                Map<String, Long> subscriptions = new TreeMap<String, Long>();

                for (API api : apiSet) {
                    if (api.getStatus() == APIStatus.CREATED) {
                        continue;
                    }
                    long count = apiProvider.getAPISubscriptionCountByAPI(api.getId());
                    if (count == 0) {
                        continue;
                    }

                    SubscriberCountByAPIs apiSub = new SubscriberCountByAPIs();
                    apiSub.apiName.add(api.getId().getApiName());
                    apiSub.apiName.add(api.getId().getVersion());
                    apiSub.apiName.add(api.getId().getProviderName());

                    apiSub.count = count;
                    list.add(apiSub);
                }

            }
        } catch (Exception e) {
            //handleException("Error while getting subscribers of the provider: " + providerName, e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return new Gson().toJson(list);
    }

    public static int getClientType() {
        Registry registry = CarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.valueOf(RegistryType.LOCAL_REPOSITORY.toString()));

        int val = 0;
        try {
            Resource orderRes = registry.get(RESTClientConstant.DATASOURCE_TYPE_REG_LOCATION);
            byte[] st = (byte[]) orderRes.getContent();
            val = (Integer) RestClientUtil.deserialize(st);
        } catch (RegistryException e) {
            log.error("DataSource type is not set", e);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return val;
    }

    public static void setClientType(String sourceType) throws RegistryException, IOException {
        Integer type = Integer.parseInt(sourceType);
        Registry registry = CarbonContext.getThreadLocalCarbonContext()
                .getRegistry(RegistryType.valueOf(RegistryType.LOCAL_REPOSITORY.toString()));

        Resource orderRes = registry.newResource();
        orderRes.setContent(RestClientUtil.serialize(type));
        registry.put(RESTClientConstant.DATASOURCE_TYPE_REG_LOCATION, orderRes);

    }

}

class SubscriberCountByAPIs {
    List<String> apiName = new ArrayList<String>();
    long count;
}