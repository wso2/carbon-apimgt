/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * Utility for platform gateway registration token: generate (plain + salt + hash), verify.
 * Same semantics as API Platform (Bijira): SHA-256(plainToken + salt), constant-time compare.
 */
public final class PlatformGatewayTokenUtil {

    private static final int TOKEN_BYTES = 32;
    private static final int SALT_BYTES = 32;
    private static final String SHA_256 = "SHA-256";

    private PlatformGatewayTokenUtil() {
    }

    /**
     * Generate a plain registration token (32 bytes random, base64url no padding).
     * Returned only in the create response; never stored in DB.
     */
    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generate a salt (32 bytes random). Store in DB as hex.
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hash token: SHA-256(plainToken UTF-8 bytes + salt bytes), return hex.
     */
    public static String hashToken(String plainToken, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA_256);
        md.update(plainToken.getBytes(StandardCharsets.UTF_8));
        md.update(salt);
        byte[] hash = md.digest();
        return Hex.encodeHexString(hash);
    }

    /**
     * Verify a plain token against stored tokens. Loads all active tokens, computes hash for each
     * and constant-time compares. Returns the gateway (id, organizationId) if match, else null.
     */
    public static PlatformGatewayDAO.PlatformGateway verifyToken(String plainToken)
            throws APIManagementException, NoSuchAlgorithmException, DecoderException {
        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        List<PlatformGatewayDAO.TokenWithGateway> tokens = dao.getActiveTokensWithGateway();
        byte[] inputBytes = plainToken.getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance(SHA_256);

        for (PlatformGatewayDAO.TokenWithGateway t : tokens) {
            byte[] salt = Hex.decodeHex(t.salt.toCharArray());
            md.reset();
            md.update(inputBytes);
            md.update(salt);
            byte[] computedHash = md.digest();
            byte[] storedHash = Hex.decodeHex(t.tokenHash.toCharArray());
            if (computedHash.length == storedHash.length && MessageDigest.isEqual(computedHash, storedHash)) {
                return dao.getGatewayById(t.gatewayId);
            }
        }
        return null;
    }
}
