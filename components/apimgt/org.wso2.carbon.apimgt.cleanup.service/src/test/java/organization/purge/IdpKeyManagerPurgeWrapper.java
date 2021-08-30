package organization.purge;

import org.wso2.carbon.apimgt.cleanup.service.IdpKeyMangerPurge;
import org.wso2.carbon.apimgt.cleanup.service.OrganizationPurgeDAO;

public class IdpKeyManagerPurgeWrapper extends IdpKeyMangerPurge {

    public IdpKeyManagerPurgeWrapper(String username, OrganizationPurgeDAO organizationPurgeDAO) {
        super(username, organizationPurgeDAO);
    }
}
