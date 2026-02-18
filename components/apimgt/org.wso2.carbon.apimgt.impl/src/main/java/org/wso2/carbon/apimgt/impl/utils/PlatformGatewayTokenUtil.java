/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;

import com.fasterxml.uuid.Generators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for platform gateway registration token: generate, hash (deterministic, no salt), verify.
 * Aligned with API Platform: SHA-256(plainToken) only, enabling direct DB lookup by TOKEN_HASH.
 * Supports combined format {@code tokenId.plainToken} for direct lookup by token row ID.
 */
public final class PlatformGatewayTokenUtil {

    private static final Log log = LogFactory.getLog(PlatformGatewayTokenUtil.class);
    private static final int TOKEN_BYTES = 32;
    private static final String SHA_256 = "SHA-256";

    /** Separator for combined format: tokenId.plainToken (enables lookup by ID). */
    public static final String COMBINED_TOKEN_SEPARATOR = ".";

    /** Maximum length for api-key value: combined format uuid7.plainToken = 36+1+43 = 80; small headroom for DoS safety. */
    private static final int MAX_PLAIN_TOKEN_LENGTH = 128;

    private PlatformGatewayTokenUtil() {
    }

    /**
     * Generate a time-ordered token row ID (UUIDv7, RFC 9562). Sortable by creation time, good for DB indexes.
     */
    public static String generateTokenId() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }

    /**
     * Generate a plain registration token (32 bytes random, base64url no padding).
     * Returned only in the create response; never stored in DB.
     */
    public static String generateToken() {
        if (log.isDebugEnabled()) {
            log.debug("Generating new platform gateway registration token");
        }
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash token deterministically: SHA-256(plainToken) only, hex-encoded.
     * No salt — same as API Platform; allows direct DB lookup by TOKEN_HASH.
     */
    public static String hashToken(String plainToken) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA_256);
        byte[] hash = md.digest(plainToken.getBytes(StandardCharsets.UTF_8));
        return org.apache.commons.codec.binary.Hex.encodeHexString(hash);
    }

    /**
     * Verify the api-key value (plain token or combined tokenId.plainToken).
     * Uses single-row lookup: by TOKEN_HASH (if plain token) or by token ID (if combined).
     *
     * @param apiKeyValue either the plain token, or combined format {@code tokenId.plainToken}
     * @return the associated platform gateway, or null if invalid/not found
     */
    public static PlatformGatewayDAO.PlatformGateway verifyToken(String apiKeyValue)
            throws APIManagementException, NoSuchAlgorithmException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying platform gateway token");
        }
        if (apiKeyValue == null || apiKeyValue.length() > MAX_PLAIN_TOKEN_LENGTH) {
            if (log.isDebugEnabled()) {
                log.debug("Token verification skipped: null or length exceeds " + MAX_PLAIN_TOKEN_LENGTH);
            }
            return null;
        }

        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        PlatformGatewayDAO.TokenWithGateway tokenRow;

        if (apiKeyValue.contains(COMBINED_TOKEN_SEPARATOR)) {
            // Combined format: tokenId.plainToken — lookup by ID then verify hash
            int firstDot = apiKeyValue.indexOf(COMBINED_TOKEN_SEPARATOR);
            String tokenId = apiKeyValue.substring(0, firstDot);
            String plainToken = apiKeyValue.substring(firstDot + 1);
            if (tokenId.isEmpty() || plainToken.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Token verification skipped: invalid combined format");
                }
                return null;
            }
            tokenRow = dao.getActiveTokenById(tokenId);
            if (tokenRow == null) {
                return null;
            }
            String computedHash = hashToken(plainToken);
            byte[] computedBytes;
            byte[] storedBytes;
            try {
                computedBytes = Hex.decodeHex(computedHash.toCharArray());
                storedBytes = Hex.decodeHex(tokenRow.tokenHash.toCharArray());
            } catch (DecoderException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Token verification failed: invalid hash encoding for token id=" + tokenId);
                }
                return null;
            }
            if (computedBytes.length != storedBytes.length || !MessageDigest.isEqual(computedBytes, storedBytes)) {
                if (log.isDebugEnabled()) {
                    log.debug("Token verification failed: hash mismatch for token id=" + tokenId);
                }
                return null;
            }
        } else {
            // Plain token — lookup by hash
            String tokenHash = hashToken(apiKeyValue);
            tokenRow = dao.getActiveTokenByHash(tokenHash);
            if (tokenRow == null) {
                log.warn("Failed to verify platform gateway token - no matching token found");
                return null;
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Platform gateway token verified successfully for gateway: " + tokenRow.gatewayId);
        }
        return dao.getGatewayById(tokenRow.gatewayId);
    }
}
