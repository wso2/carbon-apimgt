package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;

import org.wso2.carbon.apimgt.api.APIManagementException;

public interface OrganizationPurge {
      void deleteOrganization (String organization) throws APIManagementException;
}
