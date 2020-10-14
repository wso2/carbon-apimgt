package org.wso2.carbon.apimgt.api;

import org.apache.axiom.om.OMElement;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface APIPersistence {

    /* ======= API update   =======
       =========================== */
    API updateApi(API api);


    /* =========== WSDL ============
       =========================== */

    String createWsdl(API api, InputStream wsdlContent, OMElement wsdlContentEle);

    /**
     * Returns the wsdl content of the given API
     *
     * @param apiId Id of API
     * @return wsdl content matching if exist, else null
     */
    ResourceFile getWSDL(String apiId);

    void updateWsdlFromUrl(String apiId, String wsdlUrl);

    void updateWsdlFromWsdlFile(API api, ResourceFile wsdlResourceFile);

    /**
     *
     * @param apiId
     * @param wsdlResourceFile
     * @return registry path of wsdl file
     * @throws APIManagementException
     */
    String updateWsdlFromWsdlFile(String apiId, ResourceFile wsdlResourceFile)
                                    throws APIManagementException;

    void updateWsdlFromUrl(API api);



    /* ===== API Life Cycle =====
    ============================= */

    void addLifeCycle(API api);

    /*
     * This method returns the current lifecycle state and all other possible lifecycle states of an API.
     *
     * @param apiId Id of API
     * @return Map<String,Object> a map with lifecycle data
     */
    Map<String, Object> getAPILifeCycleData(String apiId);

    void changeAPILifeCycle(String apiId, String status); // need to change for reg impl

    /**
     * Retrieves API Lifecycle state information
     *
     * @param //apiId API Id
     * @return API Lifecycle state information
     */
    // >> need  to re-define LifecycleStateDTO in apimgt.api module. (simply importing org.wso2.carbon.apimgt.rest
    // .api.publisher.v1.dto.LifecycleStateDTO will cause  cyclic dependency issue)
    // LifecycleStateDTO getLifecycleState(String apiId);



    /* ======= Create API =======
    =========================== */

    void createAPI(API api) throws APIManagementException;
    void createAPI(API api, List<Label> gatewayLabelList) throws APIManagementException;
    /**
     * Create a new version of the api with the specified new version
     *
     * @param api           API to create new version
     * @param newVersion    New version
     * @return              ID of the API created
     */
    int createNewAPIVersion(API api, String newVersion);

    /**
     * Check whether an API with given identifiers (name, version, provider exists)
     * @param apiIdentifier Identifier of API
     * @return
     */
    boolean isApiExists(APIIdentifier apiIdentifier);


    /* ======= get/search APIs ======== (search apis >> identify differences of these apis)
    =================================== */

    /**
     * Get minimal details of API by api artifact id
     *
     * @param uuid         API artifact id
     * @param requestedOrg Name of the organization the API consists
     * @return API of the provided artifact id
     */
    API getLightweightAPIByUUID(String uuid, String requestedOrg);

    API getAPIbyId(String id, String requestedTenantDomain) throws APIManagementException;

    //API getAPI(String apiId);

    Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start, int end,
                                    boolean limitAttributes);
    Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start, int end,
                                    boolean limitAttributes, boolean isPublisherListing);
    Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery, int start, int end,
                                    boolean limitAttributes);



    /* ========== GraphQL ==========
     =============================== */

    String getGraphqlSchema(String apiId);

    void saveGraphqlSchemaDefinition(String apiId, String schemaDefinition);

    void deleteAPI(String apiId);



    /* ======= Documentation  =======
    ================================ */

    Documentation getDocumentation(String apiId, String docId, Organization requestedOrg) throws APIManagementException;

    /**
     * Get an api product documentation by artifact Id
     *
     * @param docId                 artifact id of the document
     * @param requestedOrg tenant domain of the registry where the artifact is located
     * @return Document object which represents the artifact id
     */
    Documentation getProductDocumentation(String productId, String docId, Organization requestedOrg);

    Map<String, Object> getDocumentContent(String apiId, String docId, Organization requestedOrg);

    /**
     * Removes a given documentation
     *
     * @param apiOrProductId    ID of API or APIProduct
     * @param docId ID of the doc
     */
    void removeDocumentation(String apiOrProductId, String docId);

    /**
     * Updates a given documentation
     *
     * @param apiId         Id of API
     * @param documentation Documentation
     */
    void updateDocumentation(String apiId, Documentation documentation);

    /**
     * Returns a list of documentation attached to a particular API/API Product
     *
     * @param apiOrProductId        Id of API/API Product
     * @return List<Documentation>  List of Documentation of the API or Product
     */
    List<Documentation> getAllDocumentation(String apiOrProductId);

    /**
     * Add documentation to an API
     *
     * @param apiId           ID of API
     * @param documentation Documentation
     */
    void addDocumentation(String apiId, Documentation documentation);

    /**
     * Checks whether the given document already exists for the given api/product
     *
     * @param apiOrProductId Id of API/Product
     * @param docName    Name of the document
     * @return true if document already exists for the given api/product
     */
    boolean isDocumentationExists(String apiOrProductId, String docName);

    void updateDocVisibility(String apiId, String visibility, String visibleRoles,
                                    Documentation documentation); // can be called from within updateApi() function


    /* ======= Mediation Policy ========
     =================================== */

    /**
     * Returns a list of API specific mediation policies
     *
     * @param apiId  Id of API
     * @return List of api specific mediation objects available
     */
    List<Mediation> getAllApiSpecificMediationPolicies(String apiId);

    /**
     * Returns Mediation policy specified by given identifiers
     *
     * @param apiOrProductId          Id of API/API Product
     * @param mediationPolicyUUID mediation policy identifier
     * @return Mediation object contains details of the mediation policy or null
     */
    Mediation getApiSpecificMediationPolicyFromUUID(String apiOrProductId, String mediationPolicyUUID);

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
     * @param apiOrProductId        API or Product ID
     * @param mediationPolicyId mediation policy identifier
     */
    void updateApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId);

    /**
     * Delete existing API specific mediation policy
     *
     * @param apiOrProductId        API or Product ID
     * @param mediationPolicyId mediation policy identifier
     */
    void deleteApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId);

    /**
     * Check the existence of the mediation policy
     */
    boolean isMediationPolicyExists(String mediationPolicyId);
    // This method ca throw an exception if the given mediation policy does not exist
    // i.e. throw new APIManagementException(ExceptionCodes.MEDIATION_POLICY_API_ALREADY_EXISTS);

    /**
     * @param apiOrProductId  API or Product ID
     * @param type        Type of the mediation policy
     * @param contentFile Mediation policy ResourceFile
     */
    void addApiSpecificMediationPolicy(String apiOrProductId, String type, ResourceFile contentFile);



    /* ===== Monetization ======
    =========================== */

    /**
     * Configure monetization in the API
     *
     * @param api api to configure monetization
     */
   void configureMonetizationInAPI(API api);

    /**
     * Configure monetization in the API
     *
     * @param apiId                  API ID
     * @param monetizationProperties Monetization related properties
     * @param isMonetizationEnabled  Whether to eable or disable monetization
     */
   void configureMonetizationInAPI(String apiId, JSONObject monetizationProperties, boolean isMonetizationEnabled);



    /* ===== SOAP to REST related =====
    ================================== */

    /**
     * Checks the api is a soap to rest converted one or a soap pass through
     *
     * @param apiOrProductId Id of API/API Product
     * @return true if the api is soap to rest converted one. false if the user have a pass through
     */
    boolean isSOAPToRESTApi(String apiOrProductId);

    /**
     * Get the resource policies(inflow/outflow).
     *
     * @param apiOrProductId API or Product ID
     * @param seqType       Sequence type('in' or 'out')
     * @return Converted sequence strings for a given operation
     */
    String getRestToSoapConvertedSequence(String apiOrProductId, String seqType);

    String getResourcePolicyFromResourceId(String apiId, String resourceId);

    void updateResourcePolicyFromResourceId(String apiId, String resourceId, String content);

    
    /* ======= API Definition =======
    ================================= */


    /*
     * This method returns swagger definition json of a given api/api product
     *
     * @param apiOrProductId Id of API/API Product
     * @return api/api product swagger definition json as json string
     */
    String getOASDefinitionOfAPI(String apiOrProductId);

    /**
     * This method saves api definition json
     *
     * @param apiId               ID of the API to be saved
     * @param apiDefinitionJSON API definition as JSON string
     */
   void saveOASAPIDefinition(String apiId, String apiDefinitionJSON);

    
    /* ======= API Thumbnail Icon =======
    ==================================== */

    /**
     * Retrieves the icon image associated with a particular API as a stream.
     *
     * @param apiId ID representing the API
     * @return an Icon containing image content and content type information
     */
    ResourceFile getIcon(String apiId);

    /**
     * Update the thumbnail image icon of an API
     * @param apiId             ID representing the API
     * @param fileInputStream   File stream of the thumbnail icon
     * @param fileDetail        Attachment file
     */
    void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail);



    /* Add interface methods for API/Product import export related methods in org.wso2.carbon.apimgt.impl
    .importexport.utils.APIAndAPIProductCommonUtil
     */


    /* =========== Analyzing -ApiProductsApiServiceImpl- ============
    ===============================================================*/

    /**
     * Get API Product by registry artifact id
     *
     * @param uuid                  API Product uuid
     * @param requestedOrg tenantDomain that the API Product exists
     * @return                      API Product of the provided artifact id
     */
    //  requestedTenantDomain
    APIProduct getAPIProductbyUUID(String uuid, Organization requestedOrg);

    /**
     * Get API Product by product identifier
     *
     * @param apiProductId    ID of API Product
     * @return API product    identified by product identifier
     */
    APIProduct getAPIProduct(String apiProductId);

    /**
     * Delete the API Product
     * @param apiProductId        ID of API Product
     */
    void deleteAPIProduct(String apiProductId);

}
