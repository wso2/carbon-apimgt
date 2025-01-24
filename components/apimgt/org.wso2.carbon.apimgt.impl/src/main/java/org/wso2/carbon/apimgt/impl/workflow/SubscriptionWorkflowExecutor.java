package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.monetization.DefaultMonetizationImpl;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationSubscription;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This is the class that should be extended by each Subscription workflow implementation.
 */
public abstract class SubscriptionWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(SubscriptionWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return "SUBSCRIPTION_SUPER";
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * Returns an instance of AbstractMonetization to be called within getMonetizationSubscriptionClass
     *
     * @return an instance of AbstractMonetization
     * @throws APIManagementException if the action fails
     */
    public Monetization getMonetizationImplClass() throws APIManagementException {

        APIManagerConfiguration configuration = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.
                getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        Monetization monetizationImpl = null;
        if (configuration == null) {
            log.error("API Manager configuration is not initialized.");
        } else {
            String monetizationImplClass = configuration.getMonetizationConfigurationDto().getMonetizationImpl();
            if (monetizationImplClass == null) {
                monetizationImpl = new DefaultMonetizationImpl();
            } else {
                try {
                    monetizationImpl = (Monetization) APIUtil.getClassInstance(monetizationImplClass);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    APIUtil.handleException("Failed to load monetization implementation class.", e);
                }
            }
        }
        return monetizationImpl;
    }

    /**
     * Returns an instance of MonetizationSubscription to be called within complete for adding or removing monetized subscriptions
     *
     * @return an instance of MonetizationSubscription
     * @throws APIManagementException due to it calling the getMonetizationImplClass method
     */
    public abstract MonetizationSubscription getMonetizationSubscriptionClass() throws APIManagementException;

    /**
     * Returns an instance of ApiTypeWrapper that contains either an instance of API or APIProduct to be used for monetized subscriptions
     *
     * @return an instance of ApiTypeWrapper
     */
    public ApiTypeWrapper getAPIorAPIProductwithWorkflowDTO(WorkflowDTO workflowDTO){
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        String UUID = workflowDTO.getMetadata(WorkflowConstants.PayloadConstants.API_UUID);
        String tenantDomain;
        String apiProviderName;
        String apiName;
        String apiVersion;
        APIProvider apiProvider;
        try {
            tenantDomain = workflowDTO.getTenantDomain();
            apiName = workflowDTO.getMetadata(WorkflowConstants.PayloadConstants.VARIABLE_APINAME);
            apiVersion = workflowDTO.getMetadata(WorkflowConstants.PayloadConstants.VARIABLE_APIVERSION);;
            apiProviderName = apiMgtDAO.getAPIProviderByNameAndVersion(apiName, apiVersion, tenantDomain);
            apiProvider = APIManagerFactory.getInstance().getAPIProvider(apiProviderName);
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(UUID, tenantDomain);
            return apiTypeWrapper;
        } catch (APIManagementException e) {
            log.error("An error occurred while retrieving API or API Product with UUID: " + UUID, e);
        }
        return null;
    }

    public Tier getAPIorAPIProductTier(ApiTypeWrapper apiTypeWrapper, WorkflowDTO workflowDTO){
        API api = null;
        APIProduct product = null;
        String tierName = null;
        boolean isApiProduct = apiTypeWrapper.isAPIProduct();
        if (isApiProduct) {
            product = apiTypeWrapper.getApiProduct();
        } else {
            api = apiTypeWrapper.getApi();
        }
        Tier tier = null;
        tierName = workflowDTO.getMetadata(WorkflowConstants.PayloadConstants.TIER_NAME);
        Set<Tier> policies = Collections.emptySet();
        if (!isApiProduct) {
            policies = api.getAvailableTiers();
        } else {
            policies = product.getAvailableTiers();
        }
        for (Tier policy : policies) {
            if (policy.getName() != null && (policy.getName()).equals(tierName)) {
                tier = policy;
            }
        }
        return tier;
    }
}
