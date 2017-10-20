package org.wso2.carbon.apimgt.core.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.FileEncryptionConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.kernel.securevault.SecureVault;
import org.wso2.carbon.kernel.securevault.SecureVaultUtils;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
 *  Util class for File Encryption Related functions
 */
public class FileEncryptionUtil {
    private static final Logger log = LoggerFactory.getLogger(FileEncryptionUtil.class);
    private static final int AES_Key_Size = 128;


    private static FileEncryptionConfigurations config;
    private static String aesKeyFileLocation;
    private static SecureVault secureVault;
    private static Cipher aesCipher;

    public static void init() throws APIManagementException {
        try {
            config = ServiceReferenceHolder.getInstance().getAPIMConfiguration().getFileEncryptionConfigurations();
            aesKeyFileLocation = config.getDestinationDirectory() + "/" + EncryptionConstants.ENCRYPTED_AES_KEY_FILE;
            secureVault = ServiceReferenceHolder.getInstance().getSecureVault();
            if (secureVault == null) {
                throw new APIManagementException("Secure vault OSGi service cannot be accessed");
            }
            aesCipher = Cipher.getInstance(EncryptionConstants.AES);
            createAndStoreAESKey();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            String msg = "Error occurred while initializing AES cipher for File Encryption";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    private static void createAndStoreAESKey() throws APIManagementException {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(EncryptionConstants.AES);
            kgen.init(AES_Key_Size);
            byte[] aesKey = kgen.generateKey().getEncoded();

            //store key => encrypt -> encode -> string
            byte[] encryptedKeyBytes = SecureVaultUtils.base64Encode(secureVault.encrypt(aesKey));
            String encryptedKeyString = new String(SecureVaultUtils.toChars(encryptedKeyBytes));
            APIFileUtils.createDirectory(aesKeyFileLocation);
            APIFileUtils.writeToFile(aesKeyFileLocation, encryptedKeyString);
            log.debug("AES key successfully created and stored");
        } catch (NoSuchAlgorithmException e) {
            String msg = "Error while creating AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (SecureVaultException | NullPointerException | APIMgtDAOException e) {
            String msg = "Error while storing created AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    private static byte[] getAESKey() throws SecureVaultException, APIManagementException {
        byte[] encryptedAesKeyB;
        try {
            String encryptedAesKeyStr = APIFileUtils.readFileContentAsText(aesKeyFileLocation);
            encryptedAesKeyB = SecureVaultUtils.base64Decode(SecureVaultUtils.toBytes(encryptedAesKeyStr));
        } catch (APIMgtDAOException e) {
            String msg = "Error while retrieving stored AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return secureVault.decrypt(encryptedAesKeyB);
    }

    public static void encryptFile(Path inputFilePath, Path outputFilePath) throws APIManagementException {
        try {
            log.info("Encrypting file using stored AES key");
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec);

            InputStream inputStream = APIFileUtils.readFileContentAsStream(inputFilePath.toString());
            CipherOutputStream cipherOutStream = new CipherOutputStream(
                    new FileOutputStream(outputFilePath.toString()), aesCipher);
            IOUtils.copy(inputStream, cipherOutStream);
            inputStream.close();
            cipherOutStream.close();
        } catch (IOException | InvalidKeyException | SecureVaultException e) {
            String msg = "Error while encrypting file using AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    public static String readFromEncryptedFile(Path inputFilePath) throws APIManagementException {
        try {
            log.info("Decrypting file using stored AES key");
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec);

            CipherInputStream cipherInStream = new CipherInputStream(
                    APIFileUtils.readFileContentAsStream(inputFilePath.toString()), aesCipher);
            ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
            IOUtils.copy(cipherInStream, byteArrayOutStream);
            byte[] outByteArray = byteArrayOutStream.toByteArray();
            cipherInStream.close();
            byteArrayOutStream.close();
            return new String(SecureVaultUtils.toChars(outByteArray));
        } catch (IOException | InvalidKeyException | SecureVaultException e) {
            String msg = "Error while decrypting file using AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    public static void encryptFiles() throws APIManagementException {
        List<String> filesToEncrypt = config.getFilesToEncrypt();
        for (String file: filesToEncrypt) {
            Path originalFile = Paths.get(config.getSourceDirectory() + "/" + file);
            Path encryptedFile = Paths.get(config.getDestinationDirectory() + "/encrypted" + file);
            encryptFile(originalFile, encryptedFile);
        }
    }

}
