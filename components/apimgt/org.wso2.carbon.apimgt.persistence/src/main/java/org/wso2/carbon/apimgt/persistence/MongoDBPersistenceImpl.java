package org.wso2.carbon.apimgt.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIPersistence;
import org.wso2.carbon.apimgt.api.model.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MongoDBPersistenceImpl implements APIPersistence {

    public MongoDBPersistenceImpl(String userName) {

    }

    @Override public API updateApi(API api) {
        return null;
    }

    @Override public String createWsdl(API api, InputStream wsdlContent, OMElement wsdlContentEle) {
        return null;
    }

    @Override public void updateWsdlFromUrl(String apiId, String wsdlUrl) {

    }

    @Override public void updateWsdlFromUrl(API api) {

    }

    @Override public void updateDocVisibility(String apiId, String visibility, String visibleRoles,
                                    Documentation documentation) {

    }

    @Override public void updateWsdlFromWsdlFile(API api, ResourceFile resourceFile) {

    }

    @Override public String updateWsdlFromWsdlFile(String apiId, ResourceFile wsdlResourceFile)
                                    throws APIManagementException {
        return null;
    }

    @Override public void addLifeCycle(API api) {

    }

    @Override public API createAPI(API api) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                    int end, boolean limitAttributes) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIs(String searchQuery, Organization requestedOrg, int start,
                                    int end, boolean limitAttributes, boolean isPublisherListing) {
        return null;
    }

    @Override public Map<String, Object> searchPaginatedAPIsByContent(Organization requestedOrg, String searchQuery,
                                    int start, int end, boolean limitAttributes) {
        return null;
    }

    @Override public String getGraphqlSchema(String apiId) {
        return null;
    }

    @Override public void saveGraphqlSchemaDefinition(String apiId, String schemaDefinition) {

    }

    @Override public void deleteAPI(String apiId) {

    }

    @Override public Documentation getDocumentation(String apiId, String docId, Organization requestedOrg)
                                    throws APIManagementException {
        return null;
    }

    @Override public void updateDocumentation(String apiId, Documentation documentation) {

    }

    @Override public List<Documentation> getAllDocumentation(String apiOrProductId) {
        return null;
    }

    @Override public void addDocumentation(String apiId, Documentation documentation) {

    }

    @Override public API getLightweightAPIByUUID(String uuid, String requestedOrg) {
        return null;
    }

    @Override public API getAPIbyId(String id, String requestedTenantDomain) throws APIManagementException {
        return null;
    }

    @Override public Map<String, Object> getAPILifeCycleData(String apiId) {
        return null;
    }

    @Override public List<Mediation> getAllApiSpecificMediationPolicies(String apiId) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String apiOrProductId,
                                    String mediationPolicyUUID) {
        return null;
    }

    @Override public Mediation getApiSpecificMediationPolicyFromUUID(String mediationPolicyUUID) {
        return null;
    }

    @Override public void updateApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public void deleteApiSpecificMediationPolicy(String apiOrProductId, String mediationPolicyId) {

    }

    @Override public boolean isMediationPolicyExists(String mediationPolicyId) {
        return false;
    }

    @Override public void addApiSpecificMediationPolicy(String apiOrProductId, String type, ResourceFile contentFile) {

    }

    @Override public void configureMonetizationInAPI(API api) {

    }

    @Override public void configureMonetizationInAPI(String apiId, JSONObject monetizationProperties,
                                    boolean isMonetizationEnabled) {

    }

    @Override public boolean isSOAPToRESTApi(String apiOrProductId) {
        return false;
    }

    @Override public String getRestToSoapConvertedSequence(String apiOrProductId, String seqType) {
        return null;
    }

    @Override public String getResourcePolicyFromResourceId(String apiId, String resourceId) {
        return null;
    }

    @Override public void updateResourcePolicyFromResourceId(String apiId, String resourceId, String content) {

    }

    @Override public String getOASDefinitionOfAPI(String apiOrProductId) {
        return null;
    }

    @Override public void saveOASAPIDefinition(String apiId, String apiDefinitionJSON) {

    }

    @Override public ResourceFile getIcon(String apiId) {

        return null;
    }

    @Override
    public void saveAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail) {

    }

    @Override public boolean isDocumentationExists(String apiOrProductId, String docName) {
        return false;
    }

    @Override public ResourceFile getWSDL(String apiId) {
        return null;
    }

    @Override public void changeAPILifeCycle(String apiId, String status) {

    }

    @Override public int createNewAPIVersion(API api, String newVersion) {
        return 0;
    }



    @Override public void deleteAPIProduct(String apiProductId) {

    }

    @Override public Documentation getProductDocumentation(String productId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public Map<String, Object> getDocumentContent(String apiId, String docId, Organization requestedOrg) {
        return null;
    }

    @Override public void removeDocumentation(String apiOrProductId, String docId) {

    }

    @Override public boolean isApiExists(APIIdentifier apiIdentifier) {
        return false;
    }
}
