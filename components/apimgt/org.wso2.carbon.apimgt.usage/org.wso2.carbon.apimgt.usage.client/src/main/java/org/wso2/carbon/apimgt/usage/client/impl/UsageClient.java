package org.wso2.carbon.apimgt.usage.client.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rukshan on 10/6/15.
 */
public class UsageClient {
    private static final Log log = LogFactory.getLog(UsageClient.class);
    private static APIUsageStatisticsClient usageStatisticsClient;

    public static void initializeDataSource() throws APIMgtUsageQueryServiceClientException {

        try {
            APIUsageStatisticsClient client=getStatisticClient();
            client.initializeDataSource();
        } catch (ClassNotFoundException e) {
            throw new APIMgtUsageQueryServiceClientException("Class not found",e);
        } catch (IllegalAccessException e) {
            throw new APIMgtUsageQueryServiceClientException("error in class",e);
        } catch (InstantiationException e) {
            throw new APIMgtUsageQueryServiceClientException("error in class",e);
        } catch (NoSuchMethodException e) {
            throw new APIMgtUsageQueryServiceClientException("error in class",e);
        } catch (InvocationTargetException e) {
            throw new APIMgtUsageQueryServiceClientException("error in class",e);
        }

        //        if (dataSourceType == RESTClientConstant.DATASOURCE_REST_TYPE) {
//            APIUsageStatisticsRestClientImpl.initializeDataSource();
//            log.info("Initializing REST Usage Statistics Client");
//        } else if (dataSourceType == RESTClientConstant.DATASOURCE_RDBMS_TYPE) {
//            APIUsageStatisticsRdbmsClientImpl.initializeDataSource();
//            log.info("Initializing RDBMS Usage Statistics Client");
//        } else {
//            log.error("Unknown Statistic client information found");
//        }
    }

    public static APIUsageStatisticsClient getClient() throws APIMgtUsageQueryServiceClientException {
        if (isDataPublishingEnabled()) {
            try {
                APIUsageStatisticsClient client=getStatisticClient();
                return client;
            } catch (ClassNotFoundException e) {
                throw new APIMgtUsageQueryServiceClientException("Class not found",e);
            } catch (IllegalAccessException e) {
                throw new APIMgtUsageQueryServiceClientException("error in class",e);
            } catch (InstantiationException e) {
                throw new APIMgtUsageQueryServiceClientException("error in class",e);
            }catch (NoSuchMethodException e) {
                throw new APIMgtUsageQueryServiceClientException("error in class",e);
            } catch (InvocationTargetException e) {
                throw new APIMgtUsageQueryServiceClientException("error in class",e);
            }
        } else {
            return null;
        }
    }

    public static boolean isDataPublishingEnabled() {
        APIManagerAnalyticsConfiguration con = APIManagerAnalyticsConfiguration.getInstance();
        return con.isAnalyticsEnabled();
    }

    public static List<SubscriberCountByAPIs> getSubscriberCountByAPIs(String loggedUser) throws APIManagementException {
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
        return list;
    }

    private static APIUsageStatisticsClient getStatisticClient()
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException,
            InvocationTargetException {

        if(usageStatisticsClient==null) {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            String className = config.getFirstProperty("StatisticClientProvider");
            usageStatisticsClient = (APIUsageStatisticsClient) Class.forName(className).getConstructor(String.class).newInstance("");
        }

        return usageStatisticsClient;
    }


}

class SubscriberCountByAPIs {
    List<String> apiName = new ArrayList<String>();
    long count;
}