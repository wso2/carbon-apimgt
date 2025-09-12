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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dto.UserContext;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;


public class PersistenceUtil {
    private static final Log log = LogFactory.getLog(PersistenceUtil.class);

    public static void handleException(String msg, Exception e) throws APIManagementException {
        throw new APIManagementException(msg, e);
    }

    public static void handleException(String msg) throws APIManagementException {
        throw new APIManagementException(msg);
    }


    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     *
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {

        if (input != null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                            APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    public static String extractPDFText(InputStream inputStream) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting text from PDF document");
        }
        PDFParser parser = new PDFParser(inputStream);
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(new PDDocument(cosDoc));
        cosDoc.close();
        return text;
    }

    public static String extractDocXText(InputStream inputStream) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting text from DOCX document");
        }
        XWPFDocument doc = new XWPFDocument(inputStream);
        XWPFWordExtractor msWord2007Extractor = new XWPFWordExtractor(doc);
        return msWord2007Extractor.getText();
    }

    public static String extractDocText(InputStream inputStream) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting text from DOC document");
        }
        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
        WordExtractor msWord2003Extractor = new WordExtractor(fs);
        return msWord2003Extractor.getText();
    }

    public static String extractPlainText(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static File writeStream(InputStream uploadedInputStream, String fileName)
            throws PersistenceException {
        if (log.isDebugEnabled()) {
            log.debug("Writing stream to file: " + fileName);
        }
        String randomFolderName = RandomStringUtils.randomAlphanumeric(10);
        String tmpFolder = System.getProperty(APIConstants.JAVA_IO_TMPDIR) + File.separator
                + APIConstants.DOC_UPLOAD_TMPDIR + File.separator + randomFolderName;
        File docFile = new File(tmpFolder);
        FileOutputStream outFileStream = null;

        boolean folderCreated = docFile.mkdirs();
        if (!folderCreated) {
            log.error("Failed to create temporary folder for document upload at path: " + tmpFolder);
            throw new PersistenceException("Failed to create temporary folder for document upload ");
        }

        try {
            outFileStream = new FileOutputStream(new File(docFile.getAbsolutePath(), fileName));
            int read;
            byte[] bytes = new byte[1024];
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outFileStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            String errorMessage = "Error in transferring files.";
            log.error(errorMessage, e);
            throw new PersistenceException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(outFileStream);
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully wrote stream to file: " + fileName + " at path: " + docFile.getAbsolutePath());
        }
        return docFile;
    }

    public static InputStream readStream(File docFile, String fileName) throws PersistenceException {
        if (log.isDebugEnabled()) {
            log.debug("Reading stream from file: " + fileName + " at path: " + 
                    (docFile != null ? docFile.getAbsolutePath() : "null"));
        }
        try {
            InputStream newInputStream = new FileInputStream(docFile.getAbsolutePath() + File.separator + fileName);
            return newInputStream;
        } catch (FileNotFoundException e) {
            log.error("Failed to open file: " + fileName + " at path: " + 
                    (docFile != null ? docFile.getAbsolutePath() : "null"), e);
            throw new PersistenceException("Failed to open file ");
        }
    }


      public static boolean isAdminUser(UserContext userContext) {
        boolean isAdmin = false;
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_ISADMIN)) {
            isAdmin = (Boolean) properties.get(APIConstants.USER_CTX_PROPERTY_ISADMIN);
        }
        return isAdmin;
    }

    public static String getSkipRoles(UserContext userContext) {
        String skipRoles = "";
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES)) {
            skipRoles = (String) properties.get(APIConstants.USER_CTX_PROPERTY_SKIP_ROLES);
        }
        return skipRoles;
    }
    
    public static boolean areOrganizationsRegistered(UserContext userContext) {
        boolean orgAvailable = false;
        Map<String, Object> properties = userContext.getProperties();
        if (properties != null && properties.containsKey(APIConstants.USER_CTX_PROPERTY_ORGS_AVAILABLE)) {
            orgAvailable = (Boolean) properties.get(APIConstants.USER_CTX_PROPERTY_ORGS_AVAILABLE);
        }
        return orgAvailable;
    }
}
