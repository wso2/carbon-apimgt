/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ScopesDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.notification.NotificationDTO;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIProviderImplWrapper extends APIProviderImpl {

    private API api;
    private Map<String, Map<String, String>> failedGateways;
    private List<Documentation> documentationList;

    public APIProviderImplWrapper(APIPersistence apiPersistenceInstance, ApiMgtDAO apimgtDAO, ScopesDAO scopesDAO,
            List<Documentation> documentationList, Map<String, Map<String,String>> failedGateways) throws APIManagementException {
        super(null);
        this.apiPersistenceInstance = apiPersistenceInstance;
        this.apiMgtDAO = apimgtDAO;
        this.scopesDAO = scopesDAO;
        if (documentationList != null) {
            this.documentationList = documentationList;
        }
        this.failedGateways = failedGateways;
    }

    public APIProviderImplWrapper(APIPersistence apiPersistenceInstance, ApiMgtDAO apimgtDAO,
                                  ImportExportAPI importExportAPI, GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO,
                                  ArtifactSaver artifactSaver) throws APIManagementException {

        super(null);
        this.apiPersistenceInstance = apiPersistenceInstance;
        this.apiMgtDAO = apimgtDAO;
        this.importExportAPI = importExportAPI;
        this.gatewayArtifactsMgtDAO = gatewayArtifactsMgtDAO;
        this.artifactSaver = artifactSaver;
    }

    public APIProviderImplWrapper(ApiMgtDAO apimgtDAO, ScopesDAO scopesDAO) throws APIManagementException {

        super(null);
        this.apiMgtDAO = apimgtDAO;
        this.scopesDAO = scopesDAO;
    }

    public APIProviderImplWrapper(APIPersistence apiPersistenceInstance, ApiMgtDAO apimgtDAO, ScopesDAO scopesDAO)
            throws APIManagementException {

        this(apimgtDAO,scopesDAO);
        this.apiPersistenceInstance = apiPersistenceInstance;
    }

    public APIProviderImplWrapper(ApiMgtDAO apimgtDAO, ScopesDAO scopesDAO, List<Documentation> documentationList)
            throws APIManagementException {

        this(apimgtDAO, scopesDAO);
        this.documentationList = documentationList;
    }

    public int getTenantId() {

        return tenantId;
    }

    @Override
    protected void registerCustomQueries(UserRegistry registry, String username)
            throws RegistryException, APIManagementException {
        // do nothing
    }

    @Override
    protected String createAPI(API api) throws APIManagementException {

        this.api = api;
        return super.createAPI(api);
    }

    @Override
    public void makeAPIKeysForwardCompatible(API api) throws APIManagementException {
        //do nothing
    }

    @Override
    public List<Documentation> getAllDocumentation(Identifier apiId) throws APIManagementException {

        return documentationList;
    }

    @Override
    protected int getTenantId(String tenantDomain) {

        return -1234;
    }

    @Override
    public String addResourceFile(Identifier identifier, String resourcePath, ResourceFile resourceFile)
            throws APIManagementException {

        return null;
    }

    @Override
    protected void sendAsncNotification(NotificationDTO notificationDTO) throws NotificationException {
        //do nothing
    }

    @Override
    protected void invalidateResourceCache(String apiContext, String apiVersion, Set<URITemplate> uriTemplates) {
        //do nothing
    }

    @Override
    public JSONObject getSecurityAuditAttributesFromConfig(String userId) throws APIManagementException {

        return super.getSecurityAuditAttributesFromConfig(userId);
    }

    @Override
    public boolean hasValidLength(String field, int maxLength) {

        return true;
    }

    @Override
    public void updateWsdlFromUrl(API api) throws APIManagementException {
        // do nothing
    }

}
