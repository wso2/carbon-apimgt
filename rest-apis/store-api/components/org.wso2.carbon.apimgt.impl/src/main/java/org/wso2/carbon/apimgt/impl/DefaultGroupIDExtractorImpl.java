package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.LoginPostExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class DefaultGroupIDExtractorImpl implements LoginPostExecutor {
    
    private static final Log log = LogFactory.getLog(DefaultGroupIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse){
        
        JSONObject obj;
        String username  = null;
        Boolean isSuperTenant;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        String claim = "http://wso2.org/claims/organization";
        String organization = null;
        try {
             obj = new JSONObject(loginResponse);
             username = (String)obj.get("user");
             isSuperTenant= (Boolean)obj.get("isSuperTenant");
      
                 RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
                 
                 //if the user is not in the super tenant domain then find the domain name and tenant id.
                 if(!isSuperTenant){
                     tenantDomain = MultitenantUtils.getTenantDomain(username);
                     tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                             .getTenantId(tenantDomain);
                 }
              
                 UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
                 UserStoreManager manager = realm.getUserStoreManager();
            organization =
                    manager.getUserClaimValue(MultitenantUtils.getTenantAwareUsername(username), claim, null);
            if (organization != null) {
                organization = tenantDomain + "/" + organization.trim();
            }
        } catch (JSONException e) {
            log.error("Exception occured while trying to get group Identifier from login response", e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while checking user existence for " + username, e);
        }

        return organization;
    }

}
