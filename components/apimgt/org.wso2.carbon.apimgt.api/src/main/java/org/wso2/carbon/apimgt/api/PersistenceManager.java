package org.wso2.carbon.apimgt.api;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.*;

import java.util.List;
import java.util.Map;

public interface PersistenceManager {
    void updateWsdlFromResourceFile();

    // ======= API update   =======
    API getAPI(APIIdentifier identifier);

    API getAPI(String apiUUID);

    void updateApi(API api);

    void updateWsdlFromUrl(APIIdentifier apiIdentifier, String wsdlUrl); // for updateWsdlFromUrl()

    void updateWsdlFromWsdlFile(APIIdentifier apiIdentifier, ResourceFile wsdlResourceFile);

    void updateWsdlFromUrl(API api);

    void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile);

    void updateDocVisibility(APIIdentifier apiIdentifier, String visibility, String visibleRoles,
                                    Documentation documentation); // can be called from within updateApi() function

    // ======= Create API =======

    void addLifeCycle(API api);

    void createAPI(API api);

    // ======= get APIs ======== (search apis >> identify differences of these apis)

    Map<String, Object> searchPaginatedAPIs(String searchQuery, String orgName, int start, int end,
                                    boolean limitAttributes);

    Map<String, Object> searchPaginatedAPIs(String searchQuery, String orgName, int start, int end,
                                    boolean limitAttributes, boolean isPublisherListing);

    Map<String, Object> searchPaginatedAPIsByContent(int orgId, String searchQuery, int start, int end,
                                    boolean limitAttributes);

    // ======= GraphQL =======

    String getGraphqlSchema(APIIdentifier apiId);

    void saveGraphqlSchemaDefinition(API api, String schemaDefinition);

    void saveGraphqlSchemaDefinition(APIIdentifier apiIdentifier, String visibleRoles, String schemaDefinition);

    void deleteAPI(APIIdentifier identifier);


    // ======= Documentation  =======
    Documentation getDocumentation(String docId, String orgName);

    //APIUtil function getDocument(..) will need to use this
    Map<String, Object> getDocumentContent(String userName, String orgName);

    /**
     * Removes a given documentation
     *
     * @param id    Identifier (Identifier of API or APIProduct)
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
     * Get minimal details of API by api artifact id
     *
     * @param uuid         API artifact id
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


    // ======= Mediation Policy ========

    /**
     * Returns a list of API specific mediation policies
     *
     * @param apiIdentifier API identifier
     * @return List of api specific mediation objects available
     */
    List<Mediation> getAllApiSpecificMediationPolicies(APIIdentifier apiIdentifier);

    /**
     * Returns Mediation policy specified by given identifiers
     *
     * @param identifier          API or Product identifier
     * @param mediationPolicyUUID mediation policy identifier
     * @return Mediation object contains details of the mediation policy or null
     */
    Mediation getApiSpecificMediationPolicyFromUUID(Identifier identifier, String mediationPolicyUUID);

    /**
     * Returns Mediation policy specify by given mediationPolicyUUID
     *
     * @param mediationPolicyUUID mediation policy identifier
     * @return Mediation object contains details of the mediation policy or null
     */
    Mediation getApiSpecificMediationPolicyFromUUID(String mediationPolicyUUID);

    /**
     * Delete existing API specific mediation policy
     *
     * @param identifier        API or Product identifier
     * @param mediationPolicyId mediation policy identifier
     */
    void updateApiSpecificMediationPolicy(Identifier identifier, String mediationPolicyId);

    /**
     * Delete existing API specific mediation policy
     *
     * @param identifier        API or Product identifier
     * @param mediationPolicyId mediation policy identifier
     */
    void deleteApiSpecificMediationPolicy(Identifier identifier, String mediationPolicyId);

    boolean checkIfMediationPolicyExists(String mediationPolicyId);

    /**
     * @param identifier  API or Product identifier
     * @param type        Type of the mediation policy
     * @param contentFile Mediation policy ResourceFile
     */
    void addApiSpecificMediationPolicy(Identifier identifier, String type, ResourceFile contentFile);

    /**
     * Configure monetization in the API
     *
     * @param api api to configure monetization
     */
    void configureMonetizationInAPI(API api);

    /**
     * Configure monetization in the API
     *
     * @param apiIdentifier          API identifier
     * @param monetizationProperties Monetization related properties
     * @param isMonetizationEnabled  Whether to eable or disable monetization
     */
    void configureMonetizationInAPI(APIIdentifier apiIdentifier, JSONObject monetizationProperties,
                                    boolean isMonetizationEnabled);

    /**
     * Checks the api is a soap to rest converted one or a soap pass through
     *
     * @param apiIdentifier API identifier
     * @return true if the api is soap to rest converted one. false if the user have a pass through
     */
    boolean isSOAPToRESTApi(APIIdentifier apiIdentifier);

    /**
     * Get the resource policies(inflow/outflow).
     *
     * @param apiIdentifier API or Product identifier
     * @param seqType       Sequence type('in' or 'out')
     * @return Converted sequence strings for a given operation
     */
    String getRestToSoapConvertedSequence(APIIdentifier apiIdentifier, String seqType);

    String getResourcePolicyFromResourceId(APIIdentifier identifier, String resourceId);

    void updateResourcePolicyFromResourceId(APIIdentifier identifier, String resourceId, String content);

    
    // ======= API Definition =======
    /**
     * This method returns swagger definition json of a given api/api product
     *
     * @param identifier api/api product identifier
     * @return api/api product swagger definition json as json string
     */
    String getAPIDefinition(Identifier identifier);

    /**
     * This method saves api definition json
     *
     * @param api               API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     */
    void saveAPIDefinition(API api, String apiDefinitionJSON);

    /**
     * This method saves api definition json
     *
     * @param apiIdentifier     Identifier of the API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     */
    void saveAPIDefinition(APIIdentifier apiIdentifier, String visibleRoles, String apiDefinitionJSON);

    
    // ======= API Thumbnail Icon =======
    
    /**
     * Retrieves the icon image associated with a particular API as a stream.
     *
     * @param identifier ID representing the API
     * @return an Icon containing image content and content type information
     */
    ResourceFile getIcon(APIIdentifier identifier);
}
