/**
 * 
 */
package org.wso2.carbon.apimgt.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Usage;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.api.model.policy.GlobalPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;

/**
 * 
 *
 */
public class APIProviderImpl extends AbstractAPIManager implements APIProvider  {

    public APIProviderImpl(String username) throws APIManagementException {
        super(username);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void addAPI(API arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String addBlockCondition(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addDocumentation(APIIdentifier arg0, Documentation arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addDocumentationContent(API arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addFileToDocumentation(APIIdentifier arg0, Documentation arg1, String arg2, InputStream arg3,
            String arg4) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addPolicy(Policy arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addTier(Tier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void callStatUpdateService(String arg0, String arg1, String arg2, boolean arg3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean changeAPILCCheckListItems(APIIdentifier arg0, int arg1, boolean arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void changeAPIStatus(API arg0, APIStatus arg1, String arg2, boolean arg3)
            throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean changeLifeCycleStatus(APIIdentifier arg0, String arg1)
            throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkAndChangeAPILCCheckListItem(APIIdentifier arg0, String arg1, boolean arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkIfAPIExists(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void copyAllDocumentation(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createNewAPIVersion(API arg0, String arg1) throws DuplicateAPIException, APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteAPI(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean deleteBlockCondition(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteBlockConditionByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void deletePolicy(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, Object> getAPILifeCycleData(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAPILifeCycleStatus(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public APIPolicy getAPIPolicy(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public APIPolicy getAPIPolicyByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getAPISubscriptionCountByAPI(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<SubscribedAPI> getAPIUsageByAPIId(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usage getAPIUsageBySubscriber(APIIdentifier arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usage getAPIUsageByUsers(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<API> getAPIsByProvider(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAllPaginatedAPIs(String arg0, int arg1, int arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Provider> getAllProviders() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationPolicy getApplicationPolicy(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationPolicy getApplicationPolicyByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockConditionsDTO getBlockCondition(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockConditionsDTO getBlockConditionByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<BlockConditionsDTO> getBlockConditions() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getConsumerKeys(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomApiFaultSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomApiInSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomApiOutSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomFaultSequences() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomFaultSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomInSequences() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomInSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomOutSequences() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getCustomOutSequences(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefaultVersion(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<APIStore> getExternalAPIStores(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GlobalPolicy getGlobalPolicy(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GlobalPolicy getGlobalPolicyByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLifecycleConfiguration(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getPolicyNames(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Provider getProvider(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<APIStore> getPublishedExternalAPIStores(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Subscriber> getSubscribersOfProvider(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicyByUUID(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set getThrottleTierPermissions() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set getTierPermissions() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Usage getUsageByAPI(APIIdentifier arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasAttachments(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAPIUpdateValid(API arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGlobalPolicyKeyTemplateExists(GlobalPolicy arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSynapseGateway() throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void makeAPIKeysForwardCompatible(API arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void manageAPI(API arg0) throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, String> propergateAPIStatusChangeToGateways(APIIdentifier arg0, APIStatus arg1)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void publishToExternalAPIStores(API arg0, Set<APIStore> arg1, boolean arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeDocumentation(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeDocumentation(APIIdentifier arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeTier(Tier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void saveSwagger20Definition(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<API> searchAPIs(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Documentation, API> searchAPIsByDoc(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateAPI(API arg0) throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean updateAPIStatus(APIIdentifier arg0, String arg1, boolean arg2, boolean arg3, boolean arg4)
            throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateAPIforStateChange(APIIdentifier arg0, APIStatus arg1, Map<String, String> arg2)
            throws APIManagementException, FaultGatewaysException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateAPIsInExternalAPIStores(API arg0, Set<APIStore> arg1, boolean arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateBlockCondition(int arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateBlockConditionByUUID(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateDocumentation(APIIdentifier arg0, Documentation arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updatePolicy(Policy arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateSubscription(SubscribedAPI arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateSubscription(APIIdentifier arg0, String arg1, int arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateThrottleTierPermissions(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateTier(Tier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateTierPermissions(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

}
