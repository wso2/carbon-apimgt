package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchResult;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.Mediation;
import org.wso2.carbon.apimgt.persistence.dto.MediationInfo;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPISearchResult;
import org.wso2.carbon.apimgt.persistence.dto.ResourceFile;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.GraphQLPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.MediationPolicyPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.ThumbnailPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.WSDLPersistenceException;

import java.util.List;

public class MongoDBPersistenceImpl implements APIPersistence {

    public MongoDBPersistenceImpl(String userName) {

    }

    @Override public PublisherAPI addAPI(Organization org, PublisherAPI publisherAPI) throws APIPersistenceException {
        return null;
    }

    @Override public PublisherAPI updateAPI(Organization org, PublisherAPI publisherAPI)
                                    throws APIPersistenceException {
        return null;
    }

    @Override public PublisherAPI getPublisherAPI(Organization org, String apiId) throws APIPersistenceException {
        return null;
    }

    @Override public DevPortalAPI getDevPortalAPI(Organization org, String apiId) throws APIPersistenceException {
        return null;
    }

    @Override public void deleteAPI(Organization org, String apiId) throws APIPersistenceException {

    }

    @Override public PublisherAPISearchResult searchAPIsForPublisher(Organization org, String searchQuery, int start,
                                    int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override public DevPortalAPISearchResult searchAPIsForDevPortal(Organization org, String searchQuery, int start,
                                    int offset, UserContext ctx) throws APIPersistenceException {
        return null;
    }

    @Override public void changeAPILifeCycle(Organization org, String apiId, String status)
                                    throws APIPersistenceException {

    }

    @Override public void saveWSDL(Organization org, String apiId, ResourceFile wsdlResourceFile)
                                    throws WSDLPersistenceException {

    }

    @Override public ResourceFile getWSDL(Organization org, String apiId) throws WSDLPersistenceException {
        return null;
    }

    @Override public void saveOASDefinition(Organization org, String apiId, String apiDefinition)
                                    throws OASPersistenceException {

    }

    @Override public String getOASDefinition(Organization org, String apiId) throws OASPersistenceException {
        return null;
    }

    @Override public void saveGraphQLSchemaDefinition(Organization org, String apiId, String schemaDefinition)
                                    throws GraphQLPersistenceException {

    }

    @Override public String getGraphQLSchema(Organization org, String apiId) throws GraphQLPersistenceException {
        return null;
    }

    @Override public Documentation addDocumentation(Organization org, String apiId, Documentation documentation)
                                    throws DocumentationPersistenceException {
        return null;
    }

    @Override public Documentation updateDocumentation(Organization org, String apiId, Documentation documentation)
                                    throws DocumentationPersistenceException {
        return null;
    }

    @Override public Documentation getDocumentation(Organization org, String apiId, String docId)
                                    throws DocumentationPersistenceException {
        return null;
    }

    @Override public DocumentContent getDocumentationContent(Organization org, String apiId, String docId)
                                    throws DocumentationPersistenceException {
        return null;
    }

    @Override public DocumentSearchResult searchDocumentation(Organization org, String apiId, int start, int offset,
                                    String searchQuery, UserContext ctx) throws DocumentationPersistenceException {
        return null;
    }

    @Override public void deleteDocumentation(Organization org, String apiId, String docId)
                                    throws DocumentationPersistenceException {

    }

    @Override public Mediation addMediationPolicy(Organization org, String apiId, Mediation mediation)
                                    throws MediationPolicyPersistenceException {
        return null;
    }

    @Override public Mediation updateMediationPolicy(Organization org, String apiId, Mediation mediation)
                                    throws MediationPolicyPersistenceException {
        return null;
    }

    @Override public Mediation getMediationPolicy(Organization org, String apiId, String mediationPolicyId)
                                    throws MediationPolicyPersistenceException {
        return null;
    }

    @Override public List<MediationInfo> getAllMediationPolicies(Organization org, String apiId)
                                    throws MediationPolicyPersistenceException {
        return null;
    }

    @Override public void deleteMediationPolicy(Organization org, String apiId, String mediationPolicyId)
                                    throws MediationPolicyPersistenceException {

    }

    @Override public void saveThumbnail(Organization org, String apiId, ResourceFile resourceFile)
                                    throws ThumbnailPersistenceException {

    }

    @Override public ResourceFile getThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {
        return null;
    }

    @Override public void deleteThumbnail(Organization org, String apiId) throws ThumbnailPersistenceException {

    }
}
