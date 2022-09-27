package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.NewPostLoginExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.user.exceptions.UserException;
import org.wso2.carbon.apimgt.user.mgt.internal.UserManagerHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class DefaultGroupIDExtractorImpl implements NewPostLoginExecutor {

    private static final Log log = LogFactory.getLog(DefaultGroupIDExtractorImpl.class);

    public String getGroupingIdentifiers(String loginResponse) {

        JSONObject obj;
        String username = null;
        Boolean isSuperTenant;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
        if (StringUtils.isBlank(claim)) {
            claim = "http://wso2.org/claims/organization";
        }
        String organization = null;
        try {
            obj = new JSONObject(loginResponse);
            username = (String) obj.get("user");
            isSuperTenant = (Boolean) obj.get("isSuperTenant");

            //if the user is not in the super tenant domain then find the domain name and tenant id.
            if (!isSuperTenant) {
                tenantDomain = MultitenantUtils.getTenantDomain(username);
                tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            }
            organization = UserManagerHolder.getUserManager()
                    .getUserClaimValue(tenantId, MultitenantUtils.getTenantAwareUsername(username), claim, null);
            if (organization != null) {
                organization = tenantDomain + "/" + organization.trim();
            }
        } catch (JSONException e) {
            log.error("Exception occured while trying to get group Identifier from login response", e);
        } catch (UserException e) {
            log.error("Error while checking user existence for " + username, e);
        }

        return organization;
    }

    @Override
    public String[] getGroupingIdentifierList(String loginResponse) {
        JSONObject obj;
        String username = null;
        Boolean isSuperTenant;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String claim = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_CLAIM_URI);
        if (StringUtils.isBlank(claim)) {
            claim = "http://wso2.org/claims/organization";
        }
        String organization = null;
        String[] groupIdArray = null;
        try {
            obj = new JSONObject(loginResponse);
            username = (String) obj.get("user");
            isSuperTenant = (Boolean) obj.get("isSuperTenant");

            //if the user is not in the super tenant domain then find the domain name and tenant id.
            if (!isSuperTenant) {
                tenantDomain = MultitenantUtils.getTenantDomain(username);
                tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
            }
            organization = UserManagerHolder.getUserManager()
                    .getUserClaimValue(tenantId, MultitenantUtils.getTenantAwareUsername(username), claim, null);
            if (organization != null) {
                if (organization.contains(",")) {
                    groupIdArray = organization.split(",");
                    for (int i = 0; i < groupIdArray.length; i++) {
                        groupIdArray[i] = groupIdArray[i].toString().trim();
                    }
                } else {
                    organization = organization.trim();
                    groupIdArray = new String[] {organization};
                }
            } else {
                // If claim is null then returning a empty string
                groupIdArray = new String[] {};
            }
        } catch (JSONException e) {
            log.error("Exception occured while trying to get group Identifier from login response", e);
        } catch (UserException e) {
            log.error("Error while checking user existence for " + username, e);
        }

        return groupIdArray;
    }

}
