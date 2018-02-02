/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.indexing.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.SolrException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.indexers.RXTIndexer;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.util.*;

import static org.wso2.carbon.apimgt.impl.APIConstants.CUSTOM_API_INDEXER_PROPERTY;
import static org.wso2.carbon.apimgt.impl.APIConstants.OVERVIEW_PREFIX;

/**
 * This is the custom indexer to add the API properties, to existing APIs.
 */
@SuppressWarnings("unused")
public class CustomAPIIndexer extends RXTIndexer {
    public static final Log log = LogFactory.getLog(CustomAPIIndexer.class);

    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {
        Registry registry = GovernanceUtils
                .getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
        String resourcePath = fileData.path.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
        Resource resource = null;

        if (registry.resourceExists(resourcePath)) {
            resource = registry.get(resourcePath);
        }
        if (log.isDebugEnabled()) {
            log.debug("CustomAPIIndexer is currently indexing the api at path " + resourcePath);
        }
        if (resource != null) {
            String publisherAccessControl = resource.getProperty(APIConstants.PUBLISHER_ROLES);
            API api = null;
            String storeVisibility = null;
            String storeVisibleRoles = null;
            try {
                api = APIUtil.getAPI(APIUtil.getArtifactManager(registry, "api").getGenericArtifact(resource.getUUID()), registry);
                storeVisibility = api.getVisibility();
                storeVisibleRoles = api.getVisibleRoles();
            } catch (APIManagementException e) {
                // We need to continue default indexing process although access control extension faces an error, so not throwing an exception here.
                log.error("Error while retrieving API", e);
            }

            if (publisherAccessControl == null || publisherAccessControl.trim().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("API at " + resourcePath + "did not have property : " + APIConstants.PUBLISHER_ROLES
                            + ", hence adding the null value for that API resource.");
                }
                resource.setProperty(APIConstants.PUBLISHER_ROLES, "null");
                resource.setProperty(APIConstants.ACCESS_CONTROL, APIConstants.NO_ACCESS_CONTROL);
                resource.setProperty(CUSTOM_API_INDEXER_PROPERTY, "true");
                registry.put(resourcePath, resource);
            }

            if (storeVisibility != null) {
                if (log.isDebugEnabled()) {
                    log.debug("API at " + resourcePath + "did not have property : " + APIConstants.STORE_VIEW_ROLES
                            + ", hence adding the values for that API resource.");
                }
                if (storeVisibility.equals(APIConstants.PUBLIC_STORE_VISIBILITY)) {
                    resource.setProperty(APIConstants.STORE_VIEW_ROLES, "null");
                } else if (storeVisibility.equals(APIConstants.RESTRICTED_STORE_VISIBILITY)){
                    resource.setProperty(APIConstants.STORE_VIEW_ROLES, storeVisibleRoles + "," + publisherAccessControl);
                }
                resource.setProperty(CUSTOM_API_INDEXER_PROPERTY, "true");
                registry.put(resourcePath, resource);
            }
        }

        // Here we are adding properties as fields, so that we can search the properties as we do for attributes.
        IndexDocument indexDocument = super.getIndexedDocument(fileData);
        Map<String, List<String>> fields = indexDocument.getFields();
        if (resource != null) {
            Properties properties = resource.getProperties();
            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String property = (String) propertyNames.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug("API at " + resourcePath + " has " + property + " property");
                }
                if (property.startsWith(APIConstants.API_RELATED_CUSTOM_PROPERTIES_PREFIX)) {
                    fields.put((OVERVIEW_PREFIX + property), getLowerCaseList(resource.getPropertyValues(property)));
                    if (log.isDebugEnabled()) {
                        log.debug(property + " is added as " + (OVERVIEW_PREFIX + property) + " field for indexing");
                    }
                }
            }
            indexDocument.setFields(fields);
        }
        return indexDocument;
    }

    /**
     * To get the list with lower case letters. This is needed as whenever we do a attribute search it will be
     * converted back to lower case.
     *
     * @param propertyValues List of properties that need to be converted to lowercase.
     * @return lower case list of properties.
     */
    private List<String> getLowerCaseList(List<String> propertyValues) {
        List<String> lowerCasedProperties = new ArrayList<>();
        if (propertyValues == null) {
            return null;
        }
        for (String property : propertyValues) {
            lowerCasedProperties.add(property.toLowerCase());
        }
        return lowerCasedProperties;
    }
}
