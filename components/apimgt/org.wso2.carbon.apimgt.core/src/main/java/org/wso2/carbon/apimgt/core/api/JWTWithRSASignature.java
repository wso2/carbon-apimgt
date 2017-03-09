/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.api;

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Interface which defines methods to support JW Tokens and to get public, private keys from keystore.
 */
public interface JWTWithRSASignature {

    /**
     * To get private key from the keystore.
     *
     * @param keyStoreFilePath File path of the keystore
     * @param keyStorePassword Password of the keystore
     * @param alias            Alias of the required private key
     * @param aliasPassword    Password of the alias
     * @return Corresponding private key
     * @throws APIManagementException In case of any failures, when trying to get the private key from the given
     *                                keystore and alias.
     */
    PrivateKey getPrivateKey(String keyStoreFilePath, String keyStorePassword, String alias, String aliasPassword)
            throws APIManagementException;

    /**
     * To sign the given JWT claims set using the RSA private key provided.
     *
     * @param rsaPrivateKey Private key which is used to sign
     * @param claimsSet     ClaimsSet which needs to be signed
     * @return signed and serialized JWT String.
     * @throws APIManagementException In case of any failures, when trying to sign and serialize.
     */
    String rsaSignAndSerialize(RSAPrivateKey rsaPrivateKey, JWTClaimsSet claimsSet) throws APIManagementException;

    /**
     * To get the public key from the keystore.
     *
     * @param keyStoreFilePath File path of the keystore
     * @param keyStorePassword Password of the keystore
     * @param alias            Alias of the required public key
     * @return Corresponding public key
     * @throws APIManagementException In case of any failures, when trying to get the public key from the given
     *                                keystore and alias.
     */
    PublicKey getPublicKey(String keyStoreFilePath, String keyStorePassword, String alias)
            throws APIManagementException;

    /**
     * To parse and verify a given JWT String using RSA public key to ensure the integrity.
     *
     * @param token        JWT String which needs to be verified after parsing.
     * @param rsaPublicKey Public key used to verify the digital signature.
     * @return True if signature matches, False otherwise.
     * @throws APIManagementException In case of any failures, when trying to parse and verify the signature.
     */
    boolean verifyRSASignature(String token, RSAPublicKey rsaPublicKey) throws APIManagementException;
}
