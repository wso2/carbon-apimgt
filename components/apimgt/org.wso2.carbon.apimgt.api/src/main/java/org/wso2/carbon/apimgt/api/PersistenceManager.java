package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;

import java.util.Map;

public interface PersistenceManager {
    void updateWsdlFromResourceFile();

    // API update related operations
    API getAPI(APIIdentifier identifier);
    API getAPI(String apiUUID);
    void updateApi(API api);
    void updateWsdlFromUrl(APIIdentifier apiIdentifier, String wsdlUrl); // for updateWsdlFromUrl()
    void updateWsdlFromWsdlFile(APIIdentifier apiIdentifier, ResourceFile wsdlResourceFile);
    void updateWsdlFromUrl(API api);
    void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile);
    void updateDocVisibility(APIIdentifier apiIdentifier, String visibility, String visibleRoles,
                                    Documentation documentation); // can be called from within updateApi() function

    // void addLifeCycle(API api);
    // void createAPI(API api);
    void addLifeCycle(API api);
    void createAPI(API api);

    //get APIs (search apis >> identify differences of these apis)
    Map<String,Object> searchPaginatedAPIs(String searchQuery, String orgName,int start,int end,
                                    boolean limitAttributes);
    Map<String, Object> searchPaginatedAPIs(String searchQuery, String tenantDomain, int start, int end,
                                    boolean limitAttributes, boolean isPublisherListing);

    Map<String, Object> searchPaginatedAPIsByContent(int tenantId, String searchQuery, int start, int end,
                                    boolean limitAttributes);


}
