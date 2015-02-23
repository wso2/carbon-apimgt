/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.apache.synapse.securevault.tool;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.securevault.*;
import org.apache.synapse.securevault.definition.CipherInformation;
import org.apache.synapse.securevault.definition.IdentityKeyStoreInformation;
import org.apache.synapse.securevault.definition.TrustKeyStoreInformation;
import org.apache.synapse.securevault.keystore.IdentityKeyStoreWrapper;
import org.apache.synapse.securevault.keystore.KeyStoreWrapper;
import org.apache.synapse.securevault.keystore.TrustKeyStoreWrapper;
import org.apache.synapse.securevault.secret.SecretInformation;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;

/**
 * Tool for encrypting and decrypting. <br>
 * <br>
 * Arguments and their meanings:
 * <ul>
 * <li>source       Either cipher or plain text as an in-lined form
 * <li>sourceFile   Source from a file
 * <li>passphrase   if a simple symmetric encryption using a pass phrase shall be used
 * <li>keystore     If keys are in a store, it's location
 * <li>storepass    Password for access keyStore
 * <li>keypass      To get private key
 * <li>alias        Alias to identify key owner
 * <li>storetype    Type of keyStore
 * <li>keyfile      If key is in a file
 * <li>trusted      Is KeyStore a trusted store ? . if presents this , consider as a  trusted store
 * <li>opmode       encrypt or decrypt
 * <li>algorithm    encrypt or decrypt algorithm (default RSA)
 * <li>outencode    Currently BASE64 or BIGINTEGER16
 * <li>inencode     Currently BASE64 or BIGINTEGER16
 * <p/>
 * <ul>
 */
public final class CipherTool {

    /* The cipher or plain text as an in-lined */
    private static final String SOURCE_IN_LINED = "source";

    /* The the source from a file*/
    private static final String SOURCE_FILE = "sourcefile";

    /* Pass phrase to use for en- or decryption. */
    private static final String PASSPHRASE = "passphrase";

    /* The argument name for KeyStore location */
    private static final String KEY_STORE = "keystore";

    /* The KeyStore type*/
    private static final String STORE_TYPE = "storetype";

    /* The argument name for password to access KeyStore*/
    private static final String STORE_PASS = "storepass";

    /* The argument name for password for access private key */
    private static final String KEY_PASS = "keypass";

    /* The alias to identify key owner */
    private static final String ALIAS = "alias";

    /* If the key is from a file , then it's location*/
    private static final String KEY_FILE = "keyfile";

    /* The algorithm for encrypting or decrypting */
    private static final String ALGORITHM = "algorithm";

    /* The operation mode of cihper - encrypt or decrypt */
    private static final String OP_MODE = "opmode";

    /* The cipher type - asymmetric , symmetric */
    private static final String CIPHER_TYPE = "ciphertype";

    /* If  the target has to be written to a file*/
    private static final String TARGET_FILE = "targetfile";

    /* If  the output of cipher operation need to be encode - only base64*/
    private static final String OUT_TYPE = "outencode";

    /* If  the encode of the input type base64*/
    private static final String IN_TYPE = "inencode";

    /* Is this keyStore a trusted one */
    private static final String TRUSTED = "trusted";

    private static Log log = LogFactory.getLog(CipherTool.class);

    private CipherTool() {
    }

    public static void main(String[] args) throws Exception {

        // loads the options
        Options options = getOptions();

        // create the command line parser
        CommandLineParser parser = new GnuParser();

        // parse the command line arguments
        try {
            CommandLine cmd = parser.parse(options, args);

            // Loads the cipher relate information
            CipherInformation cipherInformation = getCipherInformation(cmd);

            // Source  as an in-lined
            String source = getArgument(cmd, SOURCE_IN_LINED, null);
            assertEmpty(source, SOURCE_IN_LINED);

            Key key = findKey(cmd, cipherInformation);

            boolean isEncrypt = (cipherInformation.getCipherOperationMode() ==
                    CipherOperationMode.ENCRYPT);

            EncryptionProvider encryptionProvider = null;
            DecryptionProvider decryptionProvider = null;

            if (key != null) {

                if (isEncrypt) {
                    encryptionProvider = CipherFactory.createCipher(cipherInformation, key);
                } else {
                    decryptionProvider = CipherFactory.createCipher(cipherInformation, key);
                }

            } else {

                boolean isTrusted = isArgumentPresent(cmd, TRUSTED);

                KeyStoreWrapper keyStoreWrapper;

                if (isTrusted) {
                    keyStoreWrapper = new TrustKeyStoreWrapper();
                    ((TrustKeyStoreWrapper) keyStoreWrapper).init(getTrustKeyStoreInformation(cmd));
                } else {
                    keyStoreWrapper = new IdentityKeyStoreWrapper();
                    //Password for access private key
                    String keyPass = getArgument(cmd, KEY_PASS, null);
                    assertEmpty(keyPass, KEY_PASS);
                    ((IdentityKeyStoreWrapper) keyStoreWrapper).init(
                            getIdentityKeyStoreInformation(cmd), keyPass);
                }

                if (isEncrypt) {
                    encryptionProvider = CipherFactory.createCipher(cipherInformation,
                            keyStoreWrapper);
                } else {
                    decryptionProvider = CipherFactory.createCipher(cipherInformation,
                            keyStoreWrapper);
                }
            }

            PrintStream out = System.out;
            if (isEncrypt) {
                out.println("Output : " + new String(encryptionProvider.encrypt(source.getBytes())));
            } else {
                out.println("Output : " + new String(decryptionProvider.decrypt(source.getBytes())));
            }

        } catch (ParseException e) {
            handleException("Error passing arguments ", e);
        }
    }

    /**
     * Utility method to extract command line arguments
     *
     * @param cmd          Command line which capture all command line arguments
     * @param argName      Name of the argument to be extracted
     * @param defaultValue The default value
     * @return value of the argument if there is , o.w null
     */
    private static String getArgument(CommandLine cmd, String argName, String defaultValue) {

        if (cmd == null) {
            handleException("CommandLine is null");
        }

        if (argName == null || "".equals(argName)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided argument name is null. Returning null as value");
            }
            return defaultValue;
        }

        if (cmd.hasOption(argName)) {
            return cmd.getOptionValue(argName);
        }
        return defaultValue;
    }

    /**
     * Utility method to find boolean argument
     *
     * @param cmd     Command line which capture all command line arguments
     * @param argName Name of the argument to be extracted
     * @return True if presents
     */
    private static boolean isArgumentPresent(CommandLine cmd, String argName) {
        if (cmd == null) {
            handleException("CommandLine is null");
        }

        if (argName == null || "".equals(argName)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided argument name is null. Returning null as value");
            }
            return false;
        }

        return cmd.hasOption(argName);
    }

    /**
     * Factory method to construct @see CipherInformation from command line options
     *
     * @param cmd Command line which capture all command line arguments
     * @return CipherInformation object
     */
    private static CipherInformation getCipherInformation(CommandLine cmd) {

        CipherInformation information = new CipherInformation();

        information.setAlgorithm(getArgument(cmd, ALGORITHM, CipherInformation.DEFAULT_ALGORITHM));

        information.setCipherOperationMode(CipherOperationMode.valueOf(
                getArgument(cmd, OP_MODE, CipherOperationMode.ENCRYPT.toString()).toUpperCase()));

        String encInType = getArgument(cmd, IN_TYPE, null);
        if (encInType != null) {
            information.setInType(EncodingType.valueOf(encInType.toUpperCase()));
        }

        String encOutType = getArgument(cmd, OUT_TYPE, null);
        if (encOutType != null) {
            information.setOutType(EncodingType.valueOf(encOutType.toUpperCase()));
        }

        information.setType(getArgument(cmd, CIPHER_TYPE, null));

        return information;
    }

    /**
     * Factory method to create a @see keyStoreInformation from command line options
     *
     * @param cmd Command line which capture all command line arguments
     * @return KeyStoreInformation object
     */
    private static IdentityKeyStoreInformation getIdentityKeyStoreInformation(CommandLine cmd) {

        IdentityKeyStoreInformation information = new IdentityKeyStoreInformation();
        String alias = getArgument(cmd, ALIAS, null);
        assertEmpty(alias, ALIAS);
        information.setAlias(alias);
        String keyStore = getArgument(cmd, KEY_STORE, null);
        assertEmpty(keyStore, KEY_STORE);
        information.setLocation(keyStore);
        information.setStoreType(getArgument(cmd, STORE_TYPE, KeyStoreType.JKS.toString()));
        String storePass = getArgument(cmd, STORE_PASS, null);
        assertEmpty(storePass, STORE_PASS);
        SecretInformation secretInformation = new SecretInformation();
        secretInformation.setAliasSecret(storePass);
        information.setKeyStorePasswordProvider(secretInformation);

        return information;
    }

    /**
     * Factory method to create a @see keyStoreInformation from command line options
     *
     * @param cmd Command line which capture all command line arguments
     * @return KeyStoreInformation object
     */
    private static TrustKeyStoreInformation getTrustKeyStoreInformation(CommandLine cmd) {

        TrustKeyStoreInformation information = new TrustKeyStoreInformation();
        information.setAlias(getArgument(cmd, ALIAS, null));
        String keyStore = getArgument(cmd, KEY_STORE, null);
        assertEmpty(keyStore, KEY_STORE);
        information.setLocation(keyStore);
        information.setStoreType(getArgument(cmd, STORE_TYPE, KeyStoreType.JKS.toString()));
        String storePass = getArgument(cmd, STORE_PASS, null);
        assertEmpty(storePass, STORE_PASS);
        SecretInformation secretInformation = new SecretInformation();
        secretInformation.setAliasSecret(storePass);
        information.setKeyStorePasswordProvider(secretInformation);

        return information;
    }

    /**
     * Factory method to create options
     *
     * @return Options object
     */
    private static Options getOptions() {

        Options options = new Options();

        Option source = new Option(SOURCE_IN_LINED, true, "Plain text in-lined");
        Option sourceFile = new Option(SOURCE_FILE, true, "Plain text from a file");

        Option passphrase = new Option(PASSPHRASE, true,
                "Passphrase to use for symmetric en- or decryption.");

        Option keyStore = new Option(KEY_STORE, true, "Private key entry KeyStore");
        Option storeType = new Option(STORE_TYPE, true, " KeyStore type");
        Option storePassword = new Option(STORE_PASS, true, "Password for keyStore access");
        Option keyPassword = new Option(KEY_PASS, true, "Password for access private key entry");
        Option alias = new Option(ALIAS, true, "Alias name for identify key owner");
        Option trusted = new Option(TRUSTED, false, "Is this KeyStore trusted one");

        Option keyFile = new Option(KEY_FILE, true, "Private key from a file");
        Option cipherType = new Option(CIPHER_TYPE, true, "Cipher type");
        Option opMode = new Option(OP_MODE, true, "encrypt or decrypt");

        Option algorithm = new Option(ALGORITHM, true, "Algorithm to be used");
        Option targetFile = new Option(TARGET_FILE, true, "Target file");
        Option outType = new Option(OUT_TYPE, true, "Encode type for output");
        Option intType = new Option(IN_TYPE, true, "Encode type of input source");

        options.addOption(source);
        options.addOption(sourceFile);

        options.addOption(passphrase);
        options.addOption(keyStore);
        options.addOption(storeType);
        options.addOption(storePassword);
        options.addOption(keyPassword);
        options.addOption(alias);
        options.addOption(trusted);

        options.addOption(keyFile);

        options.addOption(algorithm);
        options.addOption(cipherType);
        options.addOption(opMode);

        options.addOption(targetFile);
        options.addOption(outType);
        options.addOption(intType);

        return options;
    }

    /**
     * Factory method to retrieve a previously stored key in a file
     *
     * @param filePath Path to file
     * @return Retrieved key
     */
    private static Key getKey(String filePath) {

        if (filePath == null || "".equals(filePath)) {
            handleException("File path cannot be empty or null");
        }

        File keyFile = new File(filePath);
        if (!keyFile.exists()) {
            handleException("File cannot be found in : " + filePath);
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(keyFile));
            Object object = in.readObject();
            if (object instanceof Key) {
                return (Key) object;
            }

        } catch (IOException e) {
            handleException("Error reading key from given path : " + filePath, e);
        } catch (ClassNotFoundException e) {
            handleException("Cannot load a key from the file : " + filePath, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Find the key based on the given command line arguments
     *
     * @param cmd               command line arguments
     * @param cipherInformation cipher information
     * @return an valid <code>Key</code> if found , otherwise
     */
    private static Key findKey(CommandLine cmd, CipherInformation cipherInformation) {
        // if pass phrase is specified, use simple symmetric en-/decryption
        String passPhrase = getArgument(cmd, PASSPHRASE, null);

        Key key = null;

        if (passPhrase != null) {
            key = new SecretKeySpec(passPhrase.getBytes(), cipherInformation.getAlgorithm());

        } else {
            // Key information must not contain any password
            // If Key need to be loaded from a file
            String keyFile = getArgument(cmd, KEY_FILE, null);

            if (keyFile != null) {
                key = getKey(keyFile);
            }
        }
        return key;
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SecureVaultException(msg, e);
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SecureVaultException(msg);
    }

    private static void assertEmpty(String value, String key) {
        if (value == null || "".equals(value)) {
            handleException("The argument : " + key + " : cannot be null or empty.");
        }
    }
}
