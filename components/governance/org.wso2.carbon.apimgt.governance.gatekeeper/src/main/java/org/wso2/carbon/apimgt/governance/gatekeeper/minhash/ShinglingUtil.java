/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.minhash;

import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for N-gram shingling operations.
 * Creates overlapping character or word-level shingles from text for MinHash computation.
 */
public final class ShinglingUtil {

    private ShinglingUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates word-level n-grams from input text.
     * Example: "get user data" with n=3 produces {"get user", "user data"}
     *
     * @param text The input text
     * @param n    The n-gram size (default: 3)
     * @return Set of n-gram shingles
     */
    public static Set<String> createWordNGrams(String text, int n) {
        Set<String> shingles = new HashSet<>();

        if (text == null || text.trim().isEmpty()) {
            return shingles;
        }

        // Normalize text: lowercase, remove extra whitespace
        String normalizedText = text.toLowerCase().trim().replaceAll("\\s+", " ");

        // Split into words
        String[] words = normalizedText.split("\\s+");

        if (words.length < n) {
            // If fewer words than n, use the entire text as one shingle
            shingles.add(normalizedText);
            return shingles;
        }

        // Create n-grams
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder shingle = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) {
                    shingle.append(" ");
                }
                shingle.append(words[i + j]);
            }
            shingles.add(shingle.toString());
        }

        return shingles;
    }

    /**
     * Creates word-level n-grams using default n-gram size.
     *
     * @param text The input text
     * @return Set of n-gram shingles
     */
    public static Set<String> createWordNGrams(String text) {
        return createWordNGrams(text, GatekeeperConstants.NGRAM_SIZE);
    }

    /**
     * Creates character-level n-grams from input text.
     *
     * @param text The input text
     * @param n    The n-gram size
     * @return Set of character n-gram shingles
     */
    public static Set<String> createCharNGrams(String text, int n) {
        Set<String> shingles = new HashSet<>();

        if (text == null || text.isEmpty()) {
            return shingles;
        }

        // Normalize: lowercase and remove non-alphanumeric except spaces
        String normalizedText = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "");

        if (normalizedText.length() < n) {
            shingles.add(normalizedText);
            return shingles;
        }

        for (int i = 0; i <= normalizedText.length() - n; i++) {
            shingles.add(normalizedText.substring(i, i + n));
        }

        return shingles;
    }

    /**
     * Creates shingles from a list of features (paths, schemas, etc.).
     * Each feature is treated as a potential shingle, plus n-grams are created
     * from longer features.
     *
     * @param features List of feature strings
     * @param n        The n-gram size for word splitting
     * @return Set of all shingles
     */
    public static Set<String> createShinglesFromFeatures(List<String> features, int n) {
        Set<String> allShingles = new HashSet<>();

        for (String feature : features) {
            // Add the feature itself as a shingle
            allShingles.add(feature.toLowerCase());

            // Also create word n-grams from the feature
            allShingles.addAll(createWordNGrams(feature, n));
        }

        return allShingles;
    }

    /**
     * Creates shingles from features using default n-gram size.
     *
     * @param features List of feature strings
     * @return Set of all shingles
     */
    public static Set<String> createShinglesFromFeatures(List<String> features) {
        return createShinglesFromFeatures(features, GatekeeperConstants.NGRAM_SIZE);
    }

    /**
     * Converts shingles to their hash values for MinHash computation.
     * Uses a fast hash function for better performance.
     *
     * @param shingles Set of shingle strings
     * @return Set of hash values
     */
    public static Set<Long> hashShingles(Set<String> shingles) {
        Set<Long> hashedShingles = new HashSet<>();

        for (String shingle : shingles) {
            hashedShingles.add(hash(shingle));
        }

        return hashedShingles;
    }

    /**
     * Fast hash function for shingle strings.
     * Uses FNV-1a hash for good distribution and speed.
     *
     * @param str The string to hash
     * @return The hash value
     */
    public static long hash(String str) {
        // FNV-1a hash
        long hash = 0xcbf29ce484222325L;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            hash ^= b;
            hash *= 0x100000001b3L;
        }

        return hash;
    }

    /**
     * Creates an MD5-based hash for longer strings (for signatures).
     *
     * @param str The string to hash
     * @return Array of hash bytes
     */
    public static byte[] md5Hash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            long simpleHash = hash(str);
            return new byte[] {
                (byte) (simpleHash >>> 56),
                (byte) (simpleHash >>> 48),
                (byte) (simpleHash >>> 40),
                (byte) (simpleHash >>> 32),
                (byte) (simpleHash >>> 24),
                (byte) (simpleHash >>> 16),
                (byte) (simpleHash >>> 8),
                (byte) simpleHash
            };
        }
    }
}
