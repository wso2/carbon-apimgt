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
 * Interface which defines methods to support JWT and to get public, private keys from keystore
 */
public interface JWTWithRSASignature {

    PrivateKey getPrivateKey(String keyStoreFilePath, String keyStorePassword, String alias, String aliasPassword)
            throws APIManagementException;

    String rsaSignAndSerialize(RSAPrivateKey rsaPrivateKey, JWTClaimsSet claimsSet) throws APIManagementException;

    PublicKey getPublicKey(String keyStoreFilePath, String keyStorePassword, String alias)
            throws APIManagementException;

    boolean verifyRSASignature(String token, RSAPublicKey rsaPublicKey) throws APIManagementException;
}
