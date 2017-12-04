/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.FileEncryptionConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.SecureVaultUtils;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.EncryptionConstants;

/**
 *  Class for File Encryption Related functions
 */
public class FileEncryptionUtility {
    private static final Logger log = LoggerFactory.getLogger(FileEncryptionUtility.class);
    private static final FileEncryptionUtility instance = new FileEncryptionUtility();
    private static final int AES_Key_Size = 128;

    public static final String CARBON_HOME = "carbon.home";
    public static final String SECURITY_DIR = File.separator + "resources" + File.separator + "security";

    private FileEncryptionConfigurations config;
    private String aesKeyFileLocation;
    private SecureVault secureVault;

    /**
     * Gives FileEncryptionUtility instance
     *
     * @return FileEncryptionUtility object
     */
    public static FileEncryptionUtility getInstance() {
        return instance;
    }

    /**
     * Sets the location to store the encrypted AES key file
     * Sets secure vault instance to encrypt AES key
     * Calls createAndStoreAESKey() method
     *
     * @throws APIManagementException if an error occurs while initializing the file encryption
     */
    public void init() throws APIManagementException {
        setConfig(ServiceReferenceHolder.getInstance().getAPIMConfiguration().getFileEncryptionConfigurations());
        setAesKeyFileLocation();
        setSecureVault(ServiceReferenceHolder.getInstance().getSecureVault());
        if (secureVault == null) {
            throw new APIManagementException("Secure vault OSGi service cannot be accessed");
        }
        createAndStoreAESKey();
    }

    /**
     * Encrypts the contents of a file and stores it in a new file
     *
     * @param inputFilePath    absolute path of the file to encrypt
     * @param outputFilePath   expected absolute path of the new encrypted file
     * @throws APIManagementException  if an error occurs encrypting the file
     */
    public void encryptFile(String inputFilePath, String outputFilePath) throws APIManagementException {
        InputStream inputStream = null;
        CipherOutputStream cipherOutStream = null;
        try {
            Cipher aesCipher = Cipher.getInstance(EncryptionConstants.AES);
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec);

            Files.deleteIfExists(Paths.get(outputFilePath));
            inputStream = APIFileUtils.readFileContentAsStream(inputFilePath);
            cipherOutStream = new CipherOutputStream(
                    new FileOutputStream(outputFilePath), aesCipher);
            IOUtils.copy(inputStream, cipherOutStream);
            APIFileUtils.deleteFile(inputFilePath);
            log.debug("Successfully encrypted file using stored AES key");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            String msg = "Error while encrypting the file at " + inputFilePath;
            throw new APIManagementException(msg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(cipherOutStream);
        }
    }

    /**
     * Decrypts the content of an encrypted file and returns the text
     *
     * @param inputFilePath  absolute path of the encrypted file
     * @return content of the file after decryption
     * @throws APIManagementException if an error occurs while reading from the encrypted file
     */
    public String readFromEncryptedFile(String inputFilePath) throws APIManagementException {
        CipherInputStream cipherInStream = null;
        ByteArrayOutputStream byteArrayOutStream = null;
        try {
            if (!Files.exists(Paths.get(inputFilePath))) {
                throw new APIManagementException("File to decrypt does not exist");
            }
            Cipher aesCipher = Cipher.getInstance(EncryptionConstants.AES);
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec);

            cipherInStream = new CipherInputStream(
                    APIFileUtils.readFileContentAsStream(inputFilePath), aesCipher);
            byteArrayOutStream = new ByteArrayOutputStream();
            IOUtils.copy(cipherInStream, byteArrayOutStream);
            byte[] outByteArray = byteArrayOutStream.toByteArray();
            log.debug("Successfully decrypted file using stored AES key");
            return new String(SecureVaultUtils.toChars(outByteArray));
        } catch (IOException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            String msg = "Error while decrypting file " + inputFilePath;
            throw new APIManagementException(msg, e);
        } finally {
            IOUtils.closeQuietly(cipherInStream);
            IOUtils.closeQuietly(byteArrayOutStream);
        }
    }

    /**
     * Encrypts the list of files given in the configuration, which are located /resources/security directory.
     * Encrypted files will have "encrypted" before its original name.
     *
     * @throws APIManagementException if an error occurs while encrypting the list of files
     */
    public void encryptFiles() throws APIManagementException {
        List<String> namesOfFilesToEncrypt = getConfig().getFilesToEncrypt();
        String encryptedFilesLocation = System.getProperty(CARBON_HOME) + SECURITY_DIR + File.separator;
        for (String filename : namesOfFilesToEncrypt) {
            String originalFile = encryptedFilesLocation + filename;
            String encryptedFile = encryptedFilesLocation + "encrypted" + filename;
            encryptFile(originalFile, encryptedFile);
        }
    }

    /**
     * Creates and stores an AES key
     *
     * @throws APIManagementException if an error occurs while creating or storing AES key
     */
    void createAndStoreAESKey() throws APIManagementException {
        try {
            //create a new AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(EncryptionConstants.AES);
            keyGenerator.init(AES_Key_Size);
            byte[] aesKey = keyGenerator.generateKey().getEncoded();

            //store key => encrypt -> encode -> chars -> string
            byte[] encryptedKeyBytes = SecureVaultUtils.base64Encode(getSecureVault().encrypt(aesKey));
            String encryptedKeyString = new String(SecureVaultUtils.toChars(encryptedKeyBytes));

            Files.deleteIfExists(Paths.get(getAesKeyFileLocation()));
            APIFileUtils.createFile(getAesKeyFileLocation());
            APIFileUtils.writeToFile(getAesKeyFileLocation(), encryptedKeyString);
            log.debug("AES key successfully created and stored");
        } catch (NoSuchAlgorithmException | SecureVaultException  |
                APIMgtDAOException | IOException e) {
            String msg = "Error while creating or storing created AES key";
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Decrypts the AES key using secure vault and returns it as a byte array
     *
     * @return AES key as a byte array
     * @throws APIManagementException if an error occurs while reading or decrypting the AES key file
     */
    private byte[] getAESKey() throws APIManagementException {
        byte[] encryptedAesKeyB;
        byte[] aesKey;
        try {
            String encryptedAesKeyStr = APIFileUtils.readFileContentAsText(getAesKeyFileLocation());
            encryptedAesKeyB = SecureVaultUtils.base64Decode(SecureVaultUtils.toBytes(encryptedAesKeyStr));
            aesKey = getSecureVault().decrypt(encryptedAesKeyB);
        } catch (APIMgtDAOException e) {
            String msg = "Error while retrieving stored AES key";
            throw new APIManagementException(msg, e);
        } catch (SecureVaultException e) {
            String msg = "Error while decrypting AES key";
            throw new APIManagementException(msg, e);
        }
        return aesKey;
    }

    public FileEncryptionConfigurations getConfig() {
        return config;
    }

    public void setConfig(FileEncryptionConfigurations config) {
        this.config = config;
    }

    String getAesKeyFileLocation() {
        return aesKeyFileLocation;
    }

    void setAesKeyFileLocation() {
        this.aesKeyFileLocation = System.getProperty(CARBON_HOME) + SECURITY_DIR + File.separator
                + EncryptionConstants.ENCRYPTED_AES_KEY_FILE;
    }

    SecureVault getSecureVault() {
        return secureVault;
    }

    void setSecureVault(SecureVault secureVault) {
        this.secureVault = secureVault;
    }
}
