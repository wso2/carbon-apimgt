package org.wso2.carbon.apimgt.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.SubscriptionResponse;
import org.wso2.carbon.apimgt.api.model.Tag;

public class APIConsumerImpl extends AbstractAPIManager implements APIConsumer {

    public APIConsumerImpl(String username) throws APIManagementException {
        super(username);
        // TODO Auto-generated constructor stub
    }

    @Override
    public int addApplication(Application arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addComment(APIIdentifier arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SubscriptionResponse addSubscription(APIIdentifier arg0, String arg1, int arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cleanUpApplicationRegistration(String arg0, String arg1, String arg2, String arg3)
            throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map<String, String> completeApplicationRegistration(String arg0, String arg1, String arg2, String arg3,
            String arg4) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteOAuthApplication(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Set<APIIdentifier> getAPIByConsumerKey(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getAPIsWithTag(String arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAllPaginatedAPIsByStatus(String arg0, int arg1, int arg2, String arg3, boolean arg4)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAllPaginatedAPIsByStatus(String arg0, int arg1, int arg2, String[] arg3, boolean arg4)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getAllPaginatedPublishedAPIs(String arg0, int arg1, int arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getAllPublishedAPIs(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tag> getAllTags(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application getApplicationById(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getApplicationStatusById(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application[] getApplications(Subscriber arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application getApplicationsByName(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Application[] getApplicationsWithPagination(Subscriber arg0, String arg1, int arg2, int arg3, String arg4,
            String arg5, String arg6) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comment[] getComments(APIIdentifier arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getDeniedTiers() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getGroupIds(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getPaginatedAPIsWithTag(String arg0, int arg1, int arg2, String arg3)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SubscribedAPI> getPaginatedSubscribedAPIs(Subscriber arg0, String arg1, int arg2, int arg3, String arg4)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getPublishedAPIsByProvider(String arg0, int arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getPublishedAPIsByProvider(String arg0, String arg1, int arg2, String arg3, String arg4)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getRecentlyAddedAPIs(int arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Scope> getScopesByScopeKeys(String arg0, int arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Scope> getScopesBySubscribedAPIs(List<APIIdentifier> arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getScopesByToken(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber arg0, String arg1, String arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SubscribedAPI> getSubscribedIdentifiers(Subscriber arg0, APIIdentifier arg1, String arg2)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Subscriber getSubscriber(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SubscribedAPI getSubscriptionById(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSubscriptionCount(Subscriber arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSubscriptionStatusById(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Tag> getTagsWithAttributes(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> getTopRatedAPIs(int arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getUserRating(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isMonetizationEnabled(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscribed(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTierDeneid(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<String, Object> mapExistingOAuthClient(String arg0, String arg1, String arg2, String arg3, String arg4)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void rateAPI(APIIdentifier arg0, APIRating arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAPIRating(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeApplication(Application arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeSubscriber(APIIdentifier arg0, String arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeSubscription(SubscribedAPI arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeSubscription(APIIdentifier arg0, String arg1, int arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AccessTokenInfo renewAccessToken(String arg0, String arg1, String arg2, String arg3, String[] arg4,
            String arg5) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> requestApprovalForApplicationRegistration(String arg0, String arg1, String arg2,
            String arg3, String[] arg4, String arg5, String arg6, String arg7, String arg8)
            throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<API> searchAPI(String arg0, String arg1, String arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> searchPaginatedAPIs(String arg0, String arg1, String arg2, int arg3, int arg4,
            boolean arg5) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateApplication(Application arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public OAuthApplicationInfo updateAuthClient(String arg0, String arg1, String arg2, String arg3, String[] arg4,
            String arg5, String arg6, String arg7, String arg8) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateSubscriptions(APIIdentifier arg0, String arg1, int arg2) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public JSONObject resumeWorkflow(Object[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
