/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */ 
package org.wso2.carbon.apimgt.persistence.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.dto.DocumentSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationType;
import org.wso2.carbon.apimgt.persistence.dto.Documentation.DocumentVisibility;
import org.wso2.carbon.apimgt.persistence.dto.DocumentationInfo.DocumentSourceType;
import org.wso2.carbon.apimgt.persistence.exceptions.DocumentationPersistenceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;

public class RegistryPersistenceDocUtil {
    private static final Log log = LogFactory.getLog(RegistryPersistenceDocUtil.class);
    public static Documentation getDocumentation(GenericArtifact artifact) throws DocumentationPersistenceException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setId(artifact.getId());
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));
            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;

            if (visibilityAttr != null) {
                if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                    documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                    documentVisibility = Documentation.DocumentVisibility.PRIVATE;
                } else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                    documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
                }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (Documentation.DocumentSourceType.URL.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.URL;
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            } else if (Documentation.DocumentSourceType.FILE.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.FILE;
                documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(APIConstants.DOC_FILE_PATH)));
            } else if (Documentation.DocumentSourceType.MARKDOWN.name().equals(artifactAttribute)) {
                docSourceType = Documentation.DocumentSourceType.MARKDOWN;
            }
            documentation.setSourceType(docSourceType);
            if (documentation.getType() == DocumentationType.OTHER) {
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new DocumentationPersistenceException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }

    public static String prependWebContextRoot(String postfixUrl) {

        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (webContext != null && !"/".equals(webContext)) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }
    
    public static GenericArtifactManager getDocumentArtifactManager(Registry registry)
            throws DocumentationPersistenceException {

        GenericArtifactManager artifactManager = null;
        String key = "document";
        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if (GovernanceUtils.findGovernanceArtifactConfiguration(key, registry) != null) {
                artifactManager = new GenericArtifactManager(registry, key);
            } else {
                log.warn("Couldn't find GovernanceArtifactConfiguration of RXT: " + key
                        + ". Tenant id set in registry : " + ((UserRegistry) registry).getTenantId()
                        + ", Tenant domain set in PrivilegedCarbonContext: "
                        + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new DocumentationPersistenceException(msg, e);
        }
        return artifactManager;
    }
    
    /**
     * Get Document collection location path
     * @param provider provider 
     * @param apiName api name  
     * @param version version
     * @return
     */
    public static String getDocumentPath(String provider, String apiName, String version) {

        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                RegistryPersistenceUtil.replaceEmailDomain(provider) + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR +
                version + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
    }
    
    /**
     * Get file type content location
     * @param provider provider 
     * @param apiName api name  
     * @param version version
     * @param fileName file name
     * @return
     */
    public static String getDocumentFilePath(String provider, String apiName, String version, String fileName) {

        return getDocumentPath(provider, apiName, version) + APIConstants.DOCUMENT_FILE_DIR
                + RegistryConstants.PATH_SEPARATOR + fileName;
    }
    
    public static String getDocumentContentPath(String provider, String apiName, String version, String fileName) {

        return getDocumentPath(provider, apiName, version) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR
                + RegistryConstants.PATH_SEPARATOR + fileName;
    }
    
    public static GenericArtifact createDocArtifactContent(GenericArtifact artifact, String apiName, String apiVersion,
            String apiProvider, Documentation documentation) throws DocumentationPersistenceException {

        try {
            artifact.setAttribute(APIConstants.DOC_NAME, documentation.getName());
            artifact.setAttribute(APIConstants.DOC_SUMMARY, documentation.getSummary());
            artifact.setAttribute(APIConstants.DOC_TYPE, documentation.getType().getType());
            artifact.setAttribute(APIConstants.DOC_VISIBILITY, documentation.getVisibility().name());

            Documentation.DocumentSourceType sourceType = documentation.getSourceType();

            switch (sourceType) {
            case INLINE:
                sourceType = Documentation.DocumentSourceType.INLINE;
                break;
            case MARKDOWN:
                sourceType = Documentation.DocumentSourceType.MARKDOWN;
                break;
            case URL:
                sourceType = Documentation.DocumentSourceType.URL;
                break;
            case FILE: {
                sourceType = Documentation.DocumentSourceType.FILE;
            }
                break;
            default:
                throw new DocumentationPersistenceException("Unknown sourceType " + sourceType + " provided for documentation");
            }
            // Documentation Source URL is a required field in the documentation.rxt for migrated setups
            // Therefore setting a default value if it is not set.
            if (documentation.getSourceUrl() == null) {
                documentation.setSourceUrl(" ");
            }
            artifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, sourceType.name());
            artifact.setAttribute(APIConstants.DOC_SOURCE_URL, documentation.getSourceUrl());
            artifact.setAttribute(APIConstants.DOC_FILE_PATH, documentation.getFilePath());
            artifact.setAttribute(APIConstants.DOC_OTHER_TYPE_NAME, documentation.getOtherTypeName());
            String basePath = RegistryPersistenceUtil.replaceEmailDomain(apiProvider) + RegistryConstants.PATH_SEPARATOR
                    + apiName + RegistryConstants.PATH_SEPARATOR + apiVersion;
            artifact.setAttribute(APIConstants.DOC_API_BASE_PATH, basePath);
        } catch (GovernanceException e) {
            String msg = "Failed to create doc artifact content from :" + documentation.getName();
            log.error(msg, e);
            throw new DocumentationPersistenceException(msg, e);
        }
        return artifact;
    }
    
    public DocumentSearchContent getPublisherDocSearchResult(GenericArtifact docArtifact, GenericArtifact apiArtifact)
            throws DocumentationPersistenceException {
        DocumentSearchContent doc = new DocumentSearchContent();
        try {
            doc.setApiName(apiArtifact.getAttribute(""));
            doc.setApiProvider("admin");
            doc.setApiVersion("2");
            doc.setAssociatedType("API");
            doc.setApiUUID(apiArtifact.getId());
            
            
            doc.setDocType(DocumentationType.HOWTO);
            doc.setId(docArtifact.getId());
            doc.setSourceType(DocumentSourceType.INLINE);
            doc.setVisibility(DocumentVisibility.API_LEVEL);
            doc.setName(docArtifact.getAttribute(APIConstants.DOC_NAME));
            
            
            
        } catch (GovernanceException e) {
            throw new DocumentationPersistenceException("Error while retrieving artifact attributes", e);
        }

        return doc;
    }
    
    public DocumentSearchContent getDevPortalDocSearchResult(Documentation document) {
        DocumentSearchContent doc = new DocumentSearchContent();
        doc.setApiName(document.getName());
        doc.setApiProvider("admin");
        doc.setApiVersion("2");
        doc.setAssociatedType("API");
        doc.setDocType(DocumentationType.HOWTO);
        doc.setId("yyyyyyyyyyy");
        doc.setSourceType(DocumentSourceType.INLINE);
        doc.setVisibility(DocumentVisibility.API_LEVEL);
        doc.setName("Mydoc");
        return doc;
    }
}
