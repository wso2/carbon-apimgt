/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.configuration.models.FileEncryptionConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.kernel.securevault.SecureVault;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileEncryptionUtilityTestCase {

    private static final String testDir = File.separator + "src" + File.separator + "test";
    private static final String securityDir = File.separator + "resources" + File.separator + "security";

    private static final String testFileToEncrypt = "testFileToEncrypt";
    private static final String someText = "someText";

    private String securityDirAbsolutePath;
    private String encryptedFilePath;
    private String originalFilePath;

    @BeforeTest
    public void createFileToEncrypt() throws Exception {
        System.setProperty("carbon.home", System.getProperty("user.dir") + testDir);
        securityDirAbsolutePath = System.getProperty("carbon.home") + securityDir + File.separator;
        encryptedFilePath = securityDirAbsolutePath + "encrypted" + testFileToEncrypt;
        originalFilePath =  securityDirAbsolutePath + testFileToEncrypt;
        APIFileUtils.createFile(originalFilePath);
        APIFileUtils.writeToFile(originalFilePath, someText);
    }

    @Test (priority = 0, expectedExceptions = APIManagementException.class)
    public void testInitWhileSecureVaultNotAvailable() throws Exception {
        FileEncryptionUtility fileEncryptionUtility = FileEncryptionUtility.getInstance();
        fileEncryptionUtility.init();
    }

    @Test (priority = 1, expectedExceptions = APIManagementException.class)
    public void testStoringAesKeyWhileSecureVaultNotSet() throws Exception {
        FileEncryptionUtility fileEncryptionUtility = FileEncryptionUtility.getInstance();
        fileEncryptionUtility.createAndStoreAESKey();
    }

    @Test (priority = 2)
    public void testEncryptFiles() throws Exception {
        FileEncryptionConfigurations config = new FileEncryptionConfigurations();
        List<String> filesToEncrypt = new ArrayList<>();
        filesToEncrypt.add(testFileToEncrypt);
        config.setFilesToEncrypt(filesToEncrypt);
        SecureVault secureVault = Mockito.mock(SecureVault.class);

        FileEncryptionUtility fileEncryptionUtility = FileEncryptionUtility.getInstance();
        fileEncryptionUtility.setConfig(config);
        fileEncryptionUtility.setAesKeyFileLocation();
        fileEncryptionUtility.setSecureVault(secureVault);

        Answer nonEncryptedAesKey = invocation -> {
            Object[] args = invocation.getArguments();
            return args[0];
        };
        Mockito.when(secureVault.encrypt(Mockito.anyString().getBytes())).thenAnswer(nonEncryptedAesKey);
        Mockito.when(secureVault.decrypt(Mockito.anyString().getBytes())).thenAnswer(nonEncryptedAesKey);

        fileEncryptionUtility.createAndStoreAESKey();
        fileEncryptionUtility.encryptFiles();
        Assert.assertTrue(Files.notExists(Paths.get(originalFilePath)));
        Assert.assertEquals(fileEncryptionUtility.readFromEncryptedFile(encryptedFilePath), someText);
    }



    @Test (priority = 3, expectedExceptions = APIMgtDAOException.class)
    public void testEncryptingNonExistentFile() throws Exception {
        FileEncryptionUtility fileEncryptionUtility = FileEncryptionUtility.getInstance();
        fileEncryptionUtility.encryptFile(securityDirAbsolutePath + "NonExistentFile",
                securityDirAbsolutePath + "encryptedNonExistentFile");
    }

    @Test (priority = 4, expectedExceptions = APIManagementException.class)
    public void testDecryptingNonExistentFile() throws Exception {
        FileEncryptionUtility fileEncryptionUtility = FileEncryptionUtility.getInstance();
        fileEncryptionUtility.readFromEncryptedFile(securityDirAbsolutePath + "NonExistentFile");
    }

    @AfterTest
    public void removeCreatedFiles() throws Exception {
        String encryptedAesKeyPath = securityDirAbsolutePath + "encryptedAESKeyFile";
        Files.deleteIfExists(Paths.get(encryptedFilePath));
        Files.deleteIfExists(Paths.get(encryptedAesKeyPath));
        System.clearProperty("carbon.home");
    }


}
