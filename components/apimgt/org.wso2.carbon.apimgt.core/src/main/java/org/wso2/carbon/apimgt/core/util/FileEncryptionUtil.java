package org.wso2.carbon.apimgt.core.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.configuration.models.FileEncryptionConfigurations;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.secvault.MasterKeyReader;
import org.wso2.carbon.secvault.SecureVaultConstants;
import org.wso2.carbon.secvault.SecureVaultUtils;
import org.wso2.carbon.secvault.exception.SecureVaultException;
import org.wso2.carbon.secvault.internal.SecureVaultDataHolder;
import org.wso2.carbon.secvault.model.SecretRepositoryConfiguration;
import org.wso2.carbon.secvault.repository.DefaultSecretRepository;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
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
    private static DefaultSecretRepository defaultSecretRepository;
    private static Path secretPropertiesFilePath;
    private static Properties secretsProperties;
    private static Cipher aesCipher;

    public static void init() throws APIManagementException {
        try {
            config = ServiceReferenceHolder.getInstance().getAPIMConfiguration().getFileEncryptionConfigurations();

            SecureVaultDataHolder secureVaultDataHolder = SecureVaultDataHolder.getInstance();
            SecretRepositoryConfiguration secretRepositoryConfig = secureVaultDataHolder.getSecureVaultConfiguration()
                    .orElseThrow(() -> new APIManagementException("Error in getting secure vault configuration"))
                    .getSecretRepositoryConfig();
            MasterKeyReader masterKeyReader = secureVaultDataHolder.getMasterKeyReader()
                    .orElseThrow(() -> new APIManagementException("Error in getting secure vault configuration"));
            defaultSecretRepository = new DefaultSecretRepository();
            defaultSecretRepository.init(secretRepositoryConfig, masterKeyReader);

            secretPropertiesFilePath = Paths.get(secretRepositoryConfig
                    .getParameter(SecureVaultConstants.SECRET_PROPERTIES_CONFIG_PROPERTY)
                    .orElseThrow(() -> new SecureVaultException("Secret properties path not found")));
            secretsProperties = SecureVaultUtils.loadSecretFile(secretPropertiesFilePath);

            aesCipher = Cipher.getInstance(EncryptionConstants.AES);
            createAndStoreAESKey();
        } catch (APIManagementException | SecureVaultException e) {
            String msg = "Error occurred while initializing File Encryption";
            log.error(msg, e);
            throw new APIManagementException(msg);
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
            byte[] encryptedKeyBytes = SecureVaultUtils.base64Encode(defaultSecretRepository.encrypt(aesKey));
            String encryptedKeyString = new String(SecureVaultUtils.toChars(encryptedKeyBytes));
            secretsProperties.setProperty(EncryptionConstants.ENCRYPTED_AES_KEY,
                    SecureVaultConstants.CIPHER_TEXT + " " + encryptedKeyString);
            SecureVaultUtils.updateSecretFile(secretPropertiesFilePath, secretsProperties);
            log.debug("AES key successfully created and stored");
        } catch (NoSuchAlgorithmException e) {
            String msg = "Error while creating AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (SecureVaultException | NullPointerException e) {
            String msg = "Error while storing created AES key";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    private static byte[] getAESKey() throws SecureVaultException {
        String encryptedAesKeyStr = secretsProperties.getProperty(EncryptionConstants.ENCRYPTED_AES_KEY)
                .trim().split(SecureVaultConstants.SPACE)[1];
        byte[] encryptedAesKeyB = SecureVaultUtils.base64Decode(SecureVaultUtils.toBytes(encryptedAesKeyStr));
        return defaultSecretRepository.decrypt(encryptedAesKeyB);
    }

    public static void encryptFile(Path inputFilePath, Path outputFilePath) throws APIManagementException {
        try {
            log.info("Encrypting file using stored AES key");
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec);

            InputStream inputStream = getResourceAsStream(inputFilePath);
            CipherOutputStream cipherOutStream = new CipherOutputStream(
                    new FileOutputStream(outputFilePath.toString()), aesCipher);
            IOUtils.copy(inputStream, cipherOutStream);
            inputStream.close();
            cipherOutStream.close();
        } catch (IOException | InvalidKeyException | SecureVaultException e) {
            String msg = "Error while encrypting file using AES key";
            log.error(msg, e);
            throw new APIManagementException(msg);
        }
    }

    public static String readFromEncryptedFile(Path inputFilePath) throws APIManagementException {
        try {
            log.info("Decrypting file using stored AES key");
            SecretKeySpec aesKeySpec = new SecretKeySpec(getAESKey(), EncryptionConstants.AES);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec);

            CipherInputStream cipherInStream = new CipherInputStream(getResourceAsStream(inputFilePath), aesCipher);
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

    private static InputStream getResourceAsStream(Path path) {
        return FileEncryptionUtil.class.getResourceAsStream(path.toString());
    }
}
