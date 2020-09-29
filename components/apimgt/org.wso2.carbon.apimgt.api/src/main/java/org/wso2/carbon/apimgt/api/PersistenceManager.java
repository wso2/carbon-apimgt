package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;

public interface PersistenceManager {
    void updateWsdlFromResourceFile();

    //void addLifeCycle(API api);
    // void createAPI(API api);
    API getAPI(APIIdentifier identifier) throws APIManagementException;
    void updateApi(API api);
    void updateWsdl();

    void addLifeCycle(API api);

    void createAPI(API api);
}
