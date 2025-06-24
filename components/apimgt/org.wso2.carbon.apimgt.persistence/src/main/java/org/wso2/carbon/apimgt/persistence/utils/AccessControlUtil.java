package org.wso2.carbon.apimgt.persistence.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.PersistenceConstants;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;

import java.util.List;

public class AccessControlUtil {
    private static final Log log = LogFactory.getLog(AccessControlUtil.class);

    private AccessControlUtil() {
        // Prevent instantiation
    }

    public static boolean hasPublisherAccess(UserContext userContext, Organization org, String accessControl, List<String> accessControlRoles) {
        if (userContext == null || org == null || accessControl == null || accessControlRoles == null) {
            log.error("User context, organization or roles cannot be null");
            return false;
        }

        if (userContext.getOrganization().equals(org)) {
            for (String role : userContext.getRoles()) {
                if (accessControlRoles.stream().anyMatch(r -> r.trim().equalsIgnoreCase(role.trim()))) {
                    return true;
                }
            }
        }

       return false;
    }
}
