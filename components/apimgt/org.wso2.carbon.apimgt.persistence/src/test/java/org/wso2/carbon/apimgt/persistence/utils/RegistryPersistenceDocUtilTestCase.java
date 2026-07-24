/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.registry.core.RegistryConstants;

@RunWith(JUnit4.class)
public class RegistryPersistenceDocUtilTestCase {

    private static final String API_SOURCE_PATH = "/apimgt/applicationdata/provider/admin/MyAPI/1.0";

    private String expectedDocumentFilePath(String docId, String fileName) {
        return API_SOURCE_PATH + RegistryConstants.PATH_SEPARATOR + APIConstants.DOC_DIR
                + RegistryConstants.PATH_SEPARATOR + APIConstants.DOCUMENT_FILE_DIR
                + RegistryConstants.PATH_SEPARATOR + docId + RegistryConstants.PATH_SEPARATOR + fileName;
    }

    @Test
    public void testGetDocumentFilePath_ValidInputs() {
        String path = RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-123", "sample.pdf");
        Assert.assertEquals(expectedDocumentFilePath("doc-123", "sample.pdf"), path);
    }

    @Test
    public void testGetDocumentFilePath_SameFileNameDifferentDocIdsProduceDistinctPaths() {
        // Two documents sharing a file name must not resolve to the same on-disk path.
        String path1 = RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-1", "readme.txt");
        String path2 = RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-2", "readme.txt");
        Assert.assertFalse(path1.equals(path2));
    }

    @Test
    public void testGetDocumentFilePath_FileNameWithPathTraversalIsSanitized() {
        String path = RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-123",
                "../../../etc/passwd");
        Assert.assertEquals(expectedDocumentFilePath("doc-123", "passwd"), path);
    }

    @Test
    public void testGetDocumentFilePath_DocIdWithPathTraversalIsSanitized() {
        String path = RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "../../secret-doc",
                "file.txt");
        Assert.assertEquals(expectedDocumentFilePath("secret-doc", "file.txt"), path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentFilePath_BlankFileNameThrows() {
        RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-123", "   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentFilePath_DotDotFileNameThrows() {
        RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "doc-123", "..");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDocumentFilePath_BlankDocIdThrows() {
        RegistryPersistenceDocUtil.getDocumentFilePath(API_SOURCE_PATH, "", "file.txt");
    }
}
