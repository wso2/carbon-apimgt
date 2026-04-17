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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

/**
 * MinHash implementation for computing approximate Jaccard similarity.
 * Uses random hash functions to create compact signatures that preserve similarity.
 */
public class MinHashGenerator implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int numHashFunctions;
    private final long[] coeffA;
    private final long[] coeffB;
    private final long largePrime;

    /**
     * Creates a MinHash generator with the specified number of hash functions.
     *
     * @param numHashFunctions Number of hash functions to use
     * @param seed             Random seed for reproducibility
     */
    public MinHashGenerator(int numHashFunctions, long seed) {
        this.numHashFunctions = numHashFunctions;
        this.coeffA = new long[numHashFunctions];
        this.coeffB = new long[numHashFunctions];

        // Use a large prime for modulo operations
        this.largePrime = 2147483647L; // 2^31 - 1 (Mersenne prime)

        // Initialize random coefficients for hash functions
        Random random = new Random(seed);
        for (int i = 0; i < numHashFunctions; i++) {
            // Coefficients must be positive and less than largePrime
            coeffA[i] = Math.abs(random.nextLong()) % (largePrime - 1) + 1;
            coeffB[i] = Math.abs(random.nextLong()) % largePrime;
        }
    }

    /**
     * Creates a MinHash generator with default settings.
     */
    public MinHashGenerator() {
        this(GatekeeperConstants.DEFAULT_NUM_HASH_FUNCTIONS, 42L);
    }

    /**
     * Computes the MinHash signature for a set of shingles.
     *
     * @param shingles Set of hashed shingles
     * @return MinHash signature array
     */
    public int[] computeSignature(Set<Long> shingles) {
        int[] signature = new int[numHashFunctions];
        Arrays.fill(signature, Integer.MAX_VALUE);

        if (shingles == null || shingles.isEmpty()) {
            return signature;
        }

        for (Long shingle : shingles) {
            for (int i = 0; i < numHashFunctions; i++) {
                // Compute hash: h(x) = (a*x + b) mod p
                long hashValue = ((coeffA[i] * shingle + coeffB[i]) % largePrime);
                // Take absolute value to avoid negative values
                hashValue = Math.abs(hashValue);

                if (hashValue < signature[i]) {
                    signature[i] = (int) hashValue;
                }
            }
        }

        return signature;
    }

    /**
     * Computes the MinHash signature from a set of string shingles.
     *
     * @param shingles Set of string shingles
     * @return MinHash signature array
     */
    public int[] computeSignatureFromStrings(Set<String> shingles) {
        return computeSignature(ShinglingUtil.hashShingles(shingles));
    }

    /**
     * Estimates Jaccard similarity between two MinHash signatures.
     *
     * @param sig1 First signature
     * @param sig2 Second signature
     * @return Estimated Jaccard similarity (0.0 to 1.0)
     */
    public double estimateSimilarity(int[] sig1, int[] sig2) {
        if (sig1 == null || sig2 == null) {
            return 0.0;
        }

        if (sig1.length != sig2.length) {
            throw new IllegalArgumentException("Signatures must have the same length");
        }

        int matches = 0;
        for (int i = 0; i < sig1.length; i++) {
            if (sig1[i] == sig2[i]) {
                matches++;
            }
        }

        return (double) matches / sig1.length;
    }

    /**
     * Gets the number of hash functions used.
     *
     * @return Number of hash functions
     */
    public int getNumHashFunctions() {
        return numHashFunctions;
    }

    /**
     * Converts a signature to a byte array for storage.
     *
     * @param signature The MinHash signature
     * @return Byte array representation
     */
    public static byte[] signatureToBytes(int[] signature) {
        byte[] bytes = new byte[signature.length * 4];
        for (int i = 0; i < signature.length; i++) {
            int val = signature[i];
            bytes[i * 4] = (byte) (val >>> 24);
            bytes[i * 4 + 1] = (byte) (val >>> 16);
            bytes[i * 4 + 2] = (byte) (val >>> 8);
            bytes[i * 4 + 3] = (byte) val;
        }
        return bytes;
    }

    /**
     * Converts a byte array back to a signature.
     *
     * @param bytes The byte array
     * @return MinHash signature
     */
    public static int[] bytesToSignature(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Byte array length must be a multiple of 4");
        }

        int[] signature = new int[bytes.length / 4];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = ((bytes[i * 4] & 0xFF) << 24)
                    | ((bytes[i * 4 + 1] & 0xFF) << 16)
                    | ((bytes[i * 4 + 2] & 0xFF) << 8)
                    | (bytes[i * 4 + 3] & 0xFF);
        }
        return signature;
    }
}
