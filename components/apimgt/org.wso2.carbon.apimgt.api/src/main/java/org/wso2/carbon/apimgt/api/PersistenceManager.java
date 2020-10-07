package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.*;

import java.util.List;
import java.util.Map;

public interface PersistenceManager {
    void updateWsdlFromResourceFile();

    // ==== API update   ====
    API getAPI(APIIdentifier identifier);
    API getAPI(String apiUUID);
    void updateApi(API api);
    void updateWsdlFromUrl(APIIdentifier apiIdentifier, String wsdlUrl); // for updateWsdlFromUrl()
    void updateWsdlFromWsdlFile(APIIdentifier apiIdentifier, ResourceFile wsdlResourceFile);
    void updateWsdlFromUrl(API api);
    void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile);
    void updateDocVisibility(APIIdentifier apiIdentifier, String visibility, String visibleRoles,
                                    Documentation documentation); // can be called from within updateApi() function

    // ==== Create API ====

    void addLifeCycle(API api);
    void createAPI(API api);


    // ==== get APIs ===== (search apis >> identify differences of these apis)

    Map<String,Object> searchPaginatedAPIs(String searchQuery, String orgName,int start,int end,
                                    boolean limitAttributes);
    Map<String, Object> searchPaginatedAPIs(String searchQuery, String tenantDomain, int start, int end,
                                    boolean limitAttributes, boolean isPublisherListing);

    Map<String, Object> searchPaginatedAPIsByContent(int tenantId, String searchQuery, int start, int end,
                                    boolean limitAttributes);


    // ==== GraphQL ====

    String getGraphqlSchema(APIIdentifier apiId);

    void saveGraphqlSchemaDefinition(API api, String schemaDefinition);

    void saveGraphqlSchemaDefinition(APIIdentifier apiIdentifier, String visibleRoles, String schemaDefinition);


    String getAPIDefinition(Identifier apiIdentifier);

    void deleteAPI(APIIdentifier identifier);

    // ==== Documentation  ====
    Documentation getDocumentation(String docId, String orgName);

    //APIUtil function getDocument(..) will need to use this
    Map<String, Object> getDocumentContent(String userName, String orgName);

    /**
     * Removes a given documentation
     *
     * @param id   Identifier (Identifier of API or APIProduct)
     * @param docId UUID of the doc
     */
    void removeDocumentation(Identifier id, String docId);

    /**
     * Updates a given documentation
     *
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     */
    void updateDocumentation(APIIdentifier apiId, Documentation documentation);

    /**
     * Returns a list of documentation attached to a particular API/API Product
     *
     * @param id Identifier
     * @return List<Documentation>
     */
    List<Documentation> getAllDocumentation(Identifier id);

    /**
     * Add documentation to an API
     *
     * @param api           API
     * @param documentation Documentation
     */
    void addDocumentation(API api, Documentation documentation);


    /**
     * Get minimal details of API by registry artifact id
     *
     * @param uuid API artifact id
     * @param requestedOrg Name of the organization the API consists
     * @return API of the provided artifact id
     */
    API getLightweightAPIByUUID(String uuid, String requestedOrg);

    /*
     * This method returns the current lifecycle state and all other possible lifecycle states of an API.
     *
     * @param apiId APIIdentifier
     * @return Map<String,Object> a map with lifecycle data
     */
    Map<String, Object> getAPILifeCycleData(APIIdentifier apiId);

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId API Id
     * @return API Lifecycle state information
     */
    // >> need  to re-define LifecycleStateDTO in apimgt.api module. (simply importing org.wso2.carbon.apimgt.rest
    // .api.publisher.v1.dto.LifecycleStateDTO will cause  cyclic dependency issue)
    // LifecycleStateDTO getLifecycleState(String apiId);

}
