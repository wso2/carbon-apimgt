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

public class DocumentIndexer extends RXTIndexer {
    public static final Log log = LogFactory.getLog(DocumentIndexer.class);

    public IndexDocument getIndexedDocument(AsyncIndexer.File2Index fileData) throws SolrException, RegistryException {
        IndexDocument indexDocument = super.getIndexedDocument(fileData);
        IndexDocument newIndexDocument = indexDocument;

        Registry registry = GovernanceUtils
                .getGovernanceSystemRegistry(IndexingManager.getInstance().getRegistry(fileData.tenantId));
        String documentResourcePath = fileData.path.substring(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH.length());
        Resource documentResource = null;
        Map<String, List<String>> fields = indexDocument.getFields();

        if (registry.resourceExists(documentResourcePath)) {
            documentResource = registry.get(documentResourcePath);
        }

        if (documentResource != null) {
            try {
                fetchRequiredDetailsFromAssociatedAPI(registry, documentResource, fields);
                String content = fetchDocumentContent(registry, documentResource);
                newIndexDocument = new IndexDocument(fileData.path, "", content, indexDocument.getTenantId());
                newIndexDocument.setFields(fields);
            } catch (APIManagementException e) {
                log.error("Error while updating indexed document.", e);
            } catch (IOException e) {
                log.error("Error while getting document content.", e);
            }
        }

        log.info("Running Document Indexer...");
        return newIndexDocument;
    }

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
            if (log.isDebugEnabled()) {
                log.debug("API does not exist at " + apiPath);
            }
        }
    }

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

            //a file document can have one file association
            if (fileAssociations.length == 1) {
                fileAssociation = fileAssociations[0];
                String contentPath = fileAssociation.getDestinationPath();

                if (registry.resourceExists(contentPath)) {
                    Resource contentResource = registry.get(contentPath);

                    String fileName = ((ResourceImpl) contentResource).getName();
                    String extension = FilenameUtils.getExtension(fileName);
                    InputStream inputStream = contentResource.getContentStream();

                    if (APIConstants.PDF_EXTENSION.equals(extension)) {
                        PDFParser pdfParser = new PDFParser(inputStream);
                        pdfParser.parse();
                        COSDocument cosDocument = pdfParser.getDocument();
                        PDFTextStripper stripper = new PDFTextStripper();
                        contentString = stripper.getText(new PDDocument(cosDocument));
                    } else if (APIConstants.DOC_EXTENSION.equals(extension)) {
                        POIFSFileSystem pfs = new POIFSFileSystem(inputStream);
                        WordExtractor msWord2003Extractor = new WordExtractor(pfs);
                        contentString = msWord2003Extractor.getText();
                    } else if (APIConstants.DOCX_EXTENSION.equals(extension)) {
                        XWPFDocument doc = new XWPFDocument(inputStream);
                        XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
                        contentString = msWord2007Extractor.getText();
                    } else if (APIConstants.XLS_EXTENSION.equals(extension)) {
                        POIFSFileSystem pfs = new POIFSFileSystem(inputStream);
                        ExcelExtractor extractor = new ExcelExtractor(pfs);
                        contentString = extractor.getText();
                    } else if (APIConstants.XLSX_EXTENSION.equals(extension)) {
                        XSSFWorkbook xssfSheets = new XSSFWorkbook(inputStream);
                        XSSFExcelExtractor xssfExcelExtractor = new XSSFExcelExtractor(xssfSheets);
                        contentString = xssfExcelExtractor.getText();
                    } else if (APIConstants.PPT_EXTENSION.equals(extension)) {
                        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
                        PowerPointExtractor extractor = new PowerPointExtractor(fs);
                        contentString = extractor.getText();
                    } else if (APIConstants.PPTX_EXTENSION.equals(extension)) {
                        XMLSlideShow xmlSlideShow = new XMLSlideShow(inputStream);
                        XSLFPowerPointExtractor xslfPowerPointExtractor = new XSLFPowerPointExtractor(xmlSlideShow);
                        contentString = xslfPowerPointExtractor.getText();
                    } else if (APIConstants.TXT_EXTENSION.equals(extension) || APIConstants.WSDL_EXTENSION
                            .equals(extension) || APIConstants.XML_DOC_EXTENSION.equals(extension)) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder contentBuilder = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            contentBuilder.append(line);
                        }
                        contentString = contentBuilder.toString();
                    }
                }
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

                    InputStream instream = contentResource.getContentStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    String line;
                    StringBuilder contentBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line);
                    }
                    contentString = contentBuilder.toString();
                }
            }
        }
        return contentString;
    }
}
