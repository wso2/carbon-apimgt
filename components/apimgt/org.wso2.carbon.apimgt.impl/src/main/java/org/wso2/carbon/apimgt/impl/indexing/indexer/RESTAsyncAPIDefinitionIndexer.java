/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.IndexerUtil;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.AsyncIndexer.File2Index;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is indexer introduced to index swagger,async api artifacts for unified content search.
 */
public class RESTAsyncAPIDefinitionIndexer extends PlainTextIndexer {
    public static final Log log = LogFactory.getLog(RESTAsyncAPIDefinitionIndexer.class);

    @Override
    public IndexDocument getIndexedDocument(File2Index fileData) throws SolrException, RegistryException {
        Registry registry = GovernanceUtils
                .getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
        String definitionResourcePath = fileData.path
                .substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());

        // If the file is not a def file in provider path, do not index
        if (!definitionResourcePath.contains(APIConstants.APPLICATION_DATA_RESOURCE_URL_PREFIX)
                || !(definitionResourcePath.contains(APIConstants.OPENAPI_MASTER_JSON)
                || definitionResourcePath.contains(APIConstants.API_ASYNCAPI_DEFINITION_RESOURCE_NAME))) {
            return null;
        }

        // Filter out only values from the swagger, async json files for indexing
        String jsonAsString = RegistryUtils.decodeBytes(fileData.data);
        String valuesString = IndexerUtil.getValuesFromJsonString(jsonAsString);
        fileData.data = valuesString.getBytes();
        fileData.mediaType = "application/json";

        IndexDocument indexDocument = super.getIndexedDocument(fileData);
        IndexDocument newIndexDocument = indexDocument;

        if (log.isDebugEnabled()) {
            log.debug("Executing json api definition indexer for resource at " + definitionResourcePath);
        }

        Resource resource = null;
        Map<String, List<String>> fields = indexDocument.getFields();

        if (registry.resourceExists(definitionResourcePath)) {
            resource = registry.get(definitionResourcePath);
        }

        if (resource != null) {
            try {
                IndexerUtil.fetchRequiredDetailsFromAssociatedAPI(registry, resource, fields);
                newIndexDocument =
                        new IndexDocument(fileData.path, null,
                                indexDocument.getRawContent(), indexDocument.getTenantId());
                fields.put(APIConstants.DOCUMENT_INDEXER_INDICATOR, Arrays.asList("true"));
                newIndexDocument.setFields(fields);
            } catch (APIManagementException e) {
                //error occurred while fetching details from API, but continuing indexing
                log.error("Error while updating indexed document.", e);
            }
        }

        return newIndexDocument;
    }

}
