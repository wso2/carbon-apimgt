/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.solr.common.SolrException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.registry.extensions.indexers.RXTIndexer;

import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.indexing.AsyncIndexer;
import org.wso2.carbon.registry.indexing.IndexingManager;
import org.wso2.carbon.registry.indexing.solr.IndexDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is the document indexer introduced to index document artifacts for unified content search.
 */
public class DocumentIndexer extends RXTIndexer {
    public static final Log log = LogFactory.getLog(DocumentIndexer.class);

    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {
        IndexDocument indexDocument = super.getIndexedDocument(fileData);
        IndexDocument newIndexDocument = indexDocument;

        Registry registry = GovernanceUtils
                .getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
        String documentResourcePath = fileData.path.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());

        if (log.isDebugEnabled()) {
            log.debug("Executing document indexer for resource at " + documentResourcePath);
        }

        Resource documentResource = null;
        Map<String, List<String>> fields = indexDocument.getFields();

        if (registry.resourceExists(documentResourcePath)) {
            documentResource = registry.get(documentResourcePath);
        }

        if (documentResource != null) {
            try {
                fetchRequiredDetailsFromAssociatedAPI(registry, documentResource, fields);
                StringBuilder stringBuilder = new StringBuilder();
                String documentContent = fetchDocumentContent(registry, documentResource);
                if (documentContent != null) {
                    stringBuilder.append(fetchDocumentContent(registry, documentResource));
                    if (fields.get(APIConstants.DOC_NAME) != null) {
                        stringBuilder.append(APIConstants.DOC_NAME + "=" + StringUtils
                                .join(fields.get(APIConstants.DOC_NAME), ","));
                    }
                    if (fields.get(APIConstants.DOC_SUMMARY) != null) {
                        stringBuilder.append(APIConstants.DOC_SUMMARY + "=" + StringUtils
                                .join(fields.get(APIConstants.DOC_SUMMARY), ","));
                    }
                }
                newIndexDocument =
                        new IndexDocument(fileData.path, "", stringBuilder.toString(), indexDocument.getTenantId());
                fields.put(APIConstants.DOCUMENT_INDEXER_INDICATOR, Arrays.asList("true"));
                newIndexDocument.setFields(fields);
            } catch (APIManagementException e) {
                //error occured while fetching details from API, but continuing document indexing
                log.error("Error while updating indexed document.", e);
            } catch (IOException e) {
                //error occured while fetching document content, but continuing document indexing
                log.error("Error while getting document content.", e);
            }
        }

        return newIndexDocument;
    }

    /**
     * Fetch api status and access control details to document artifacts
     *
     * @param registry
     * @param documentResource
     * @param fields
     * @throws RegistryException
     * @throws APIManagementException
     */
    private void fetchRequiredDetailsFromAssociatedAPI(Registry registry, Resource documentResource,
            Map<String, List<String>> fields) throws RegistryException, APIManagementException {
        Association apiAssociations[] = registry
                .getAssociations(documentResource.getPath(), APIConstants.DOCUMENTATION_ASSOCIATION);

        //a document can have one api association
        Association apiAssociation = apiAssociations[0];
        String apiPath = apiAssociation.getSourcePath();

        if (registry.resourceExists(apiPath)) {
            Resource apiResource = registry.get(apiPath);
            GenericArtifactManager apiArtifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiResource.getUUID());
            String apiStatus = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS).toLowerCase();
            String publisherRoles = apiResource.getProperty(APIConstants.PUBLISHER_ROLES);
            fields.put(APIConstants.API_OVERVIEW_STATUS, Arrays.asList(apiStatus));
            fields.put(APIConstants.PUBLISHER_ROLES, Arrays.asList(publisherRoles));
        } else {
            log.warn("API does not exist at " + apiPath);
        }
    }

    /**
     * Write document content to document artifact as its raw content
     *
     * @param registry
     * @param documentResource
     * @return
     * @throws RegistryException
     * @throws IOException
     * @throws APIManagementException
     */
    private String fetchDocumentContent(Registry registry, Resource documentResource)
            throws RegistryException, IOException, APIManagementException {
        GenericArtifactManager docArtifactManager = APIUtil
                .getArtifactManager(registry, APIConstants.DOCUMENTATION_KEY);
        GenericArtifact documentArtifact = docArtifactManager.getGenericArtifact(documentResource.getUUID());
        String sourceType = documentArtifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

        String contentString = null;
        if (Documentation.DocumentSourceType.FILE.name().equals(sourceType)) {
            Association fileAssociations[] = registry
                    .getAssociations(documentResource.getPath(), APIConstants.DOCUMENTATION_FILE_ASSOCIATION);
            Association fileAssociation;

            if (fileAssociations.length < 1) {
                String error = "No document associated to API";
                if (log.isDebugEnabled()) {
                    log.debug(error);
                }
                return null;
            }

            //a file document can have one file association
            fileAssociation = fileAssociations[0];
            String contentPath = fileAssociation.getDestinationPath();

            if (!registry.resourceExists(contentPath)) {
                String error = "API not found at " + contentPath;
                log.error(error);
                throw new APIManagementException(error);
            }

            Resource contentResource = registry.get(contentPath);

            String fileName = ((ResourceImpl) contentResource).getName();
            String extension = FilenameUtils.getExtension(fileName);
            InputStream inputStream = null;
            try {
                inputStream = contentResource.getContentStream();
                switch (extension) {
                case APIConstants.PDF_EXTENSION:
                    PDFParser pdfParser = new PDFParser(inputStream);
                    pdfParser.parse();
                    COSDocument cosDocument = pdfParser.getDocument();
                    PDFTextStripper stripper = new PDFTextStripper();
                    contentString = stripper.getText(new PDDocument(cosDocument));
                    break;
                case APIConstants.DOC_EXTENSION: {
                    POIFSFileSystem pfs = new POIFSFileSystem(inputStream);
                    WordExtractor msWord2003Extractor = new WordExtractor(pfs);
                    contentString = msWord2003Extractor.getText();
                    break;
                }
                case APIConstants.DOCX_EXTENSION:
                    XWPFDocument doc = new XWPFDocument(inputStream);
                    XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
                    contentString = msWord2007Extractor.getText();
                    break;
                case APIConstants.XLS_EXTENSION: {
                    POIFSFileSystem pfs = new POIFSFileSystem(inputStream);
                    ExcelExtractor extractor = new ExcelExtractor(pfs);
                    contentString = extractor.getText();
                    break;
                }
                case APIConstants.XLSX_EXTENSION:
                    XSSFWorkbook xssfSheets = new XSSFWorkbook(inputStream);
                    XSSFExcelExtractor xssfExcelExtractor = new XSSFExcelExtractor(xssfSheets);
                    contentString = xssfExcelExtractor.getText();
                    break;
                case APIConstants.PPT_EXTENSION: {
                    POIFSFileSystem fs = new POIFSFileSystem(inputStream);
                    PowerPointExtractor extractor = new PowerPointExtractor(fs);
                    contentString = extractor.getText();
                    break;
                }
                case APIConstants.PPTX_EXTENSION:
                    XMLSlideShow xmlSlideShow = new XMLSlideShow(inputStream);
                    XSLFPowerPointExtractor xslfPowerPointExtractor = new XSLFPowerPointExtractor(xmlSlideShow);
                    contentString = xslfPowerPointExtractor.getText();
                    break;
                case APIConstants.TXT_EXTENSION:
                case APIConstants.WSDL_EXTENSION:
                case APIConstants.XML_DOC_EXTENSION:
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder contentBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line);
                    }
                    contentString = contentBuilder.toString();
                    break;
                }
            } finally {
                IOUtils.closeQuietly(inputStream);
            }


        } else if (Documentation.DocumentSourceType.INLINE.name().equals(sourceType)) {
            Association contentAssociations[] = registry
                    .getAssociations(documentResource.getPath(), APIConstants.DOCUMENTATION_CONTENT_ASSOCIATION);
            Association contentAssociation;

            //an inline document can have one or no content associations
            if (contentAssociations.length == 1) {
                contentAssociation = contentAssociations[0];
                String contentPath = contentAssociation.getDestinationPath();

                if (registry.resourceExists(contentPath)) {
                    Resource contentResource = registry.get(contentPath);

                    InputStream instream = null;
                    BufferedReader reader = null;
                    String line;
                    try {
                        instream = contentResource.getContentStream();
                        reader = new BufferedReader(new InputStreamReader(instream));
                        StringBuilder contentBuilder = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            contentBuilder.append(line);
                        }
                        contentString = contentBuilder.toString();
                    } finally {
                        if (reader != null) {
                            IOUtils.closeQuietly(reader);
                        }
                    }
                }
            }
        }
        return contentString;
    }
}
