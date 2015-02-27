package org.apache.synapse.securevault.keystore;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.IKeyStoreLoader;
import org.apache.synapse.securevault.SecureVaultException;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Constructs a KeyStore instance of type JKS from a pkcs8 private key and certificate.
 */
public class PKCS8KeyStoreLoader implements IKeyStoreLoader {

    private static Log log = LogFactory.getLog(PKCS8KeyStoreLoader.class);
    private String pkPath;
    private String certPath;
    private String keyPassword;
    private String entryAlias;

    private static final String HEADER = "-----BEGIN PRIVATE KEY-----\n";
    private static final String FOOTER = "-----END PRIVATE KEY-----";

    /**
     * constructs an instance of KeyStoreLoader
     *
     * @param pkcs8PrivateKeyPath - path to a private key file.  Key must be in PKCS8 format,
     *                            PEM encoded and unencrypted.
     * @param certFilePath        - path to certificate file.  File must be PEM encoded.
     * @param keyPass             - password to secure the private key within the keystore.
     *                            This will be required later to retrieve the private key
     *                            back from the keystore.
     * @param entryAlias          - alias for the given entry within the keystore.
     */
    public PKCS8KeyStoreLoader(String pkcs8PrivateKeyPath, String certFilePath,
                               String keyPass,
                               String entryAlias) {
        pkPath = pkcs8PrivateKeyPath;
        certPath = certFilePath;
        keyPassword = keyPass;
        this.entryAlias = entryAlias;
    }

    /**
     * Returns a JKS keyStore from the given private key, certificate path, key password and alias.
     */
    public KeyStore getKeyStore() {

        File file = new File(pkPath);
        if (!file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no private key in the given path : " + pkPath);
            }
            return null;
        }

        File certFile = new File(certPath);
        if (!certFile.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("There is no certificate in the given path : " + certPath);
            }
            return null;
        }

        try {

            if (log.isDebugEnabled()) {
                log.debug("Reading a private key(unencrypted) from given path : " + pkPath);
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = bufferedInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                handleException("IOError reading from file :  " + pkPath, e);
            } finally {
                try {
                    bufferedInputStream.close();
                    fileInputStream.close();
                    outStream.close();
                } catch (IOException ignored) {

                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Creating a private key in PKCS8Encoded using given" +
                        " (unencrypted) RSA private key ");
            }
            PrivateKey key = createPrivateKey(outStream.toByteArray());

            if (log.isDebugEnabled()) {
                log.debug("Generating a X509 certificate form given certificate file");
            }

            FileInputStream certInputStream = new FileInputStream(certFile);
            BufferedInputStream certBufferedInputStream = new BufferedInputStream(certInputStream);

            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            Certificate cert = certFactory.generateCertificate(certBufferedInputStream);

            certBufferedInputStream.close();
            certInputStream.close();


            if (log.isDebugEnabled()) {
                log.debug("Creating a KeyStore instance of type JKS from a" +
                        " PKCS8 private key and X509 certificate");
            }

            KeyStore newKeyStore = KeyStore.getInstance("JKS");
            newKeyStore.load(null, null);

            newKeyStore.setCertificateEntry("server Cert", cert);

            Certificate[] certChain = new Certificate[1];
            certChain[0] = cert;

            newKeyStore.setKeyEntry(entryAlias, key, keyPassword.toCharArray(), certChain);

            return newKeyStore;
        } catch (FileNotFoundException e) {
            handleException("IOError", e);
        } catch (IOException e) {
            handleException("IOError", e);
        } catch (NoSuchAlgorithmException e) {
            handleException("Error creating KeyStore", e);
        } catch (KeyStoreException e) {
            handleException("Error creating KeyStore", e);
        } catch (CertificateException e) {
            handleException("Error creating KeyStore", e);
        }
        return null;


    }


    /**
     * Takes the (unencrypted) RSA private key in pkcs8 format, and creates a private key out of it
     *
     * @param keyBytes Byte Array of the private key
     * @return PKCS8Encoded PrivateKey
     */
    private PrivateKey createPrivateKey(byte[] keyBytes) {

        int dataStart = HEADER.length();
        int dataEnd = keyBytes.length - FOOTER.length() - 1;
        int dataLength = dataEnd - dataStart;
        byte[] keyContent = new byte[dataLength];

        System.arraycopy(keyBytes, dataStart, keyContent, 0, dataLength);

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                new Base64().decode(keyContent));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException e) {
            handleException("Error getting a KeyFactory instance", e);
        } catch (InvalidKeySpecException e) {
            handleException("Error generating a private key", e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SecureVaultException(msg, e);
    }
}
