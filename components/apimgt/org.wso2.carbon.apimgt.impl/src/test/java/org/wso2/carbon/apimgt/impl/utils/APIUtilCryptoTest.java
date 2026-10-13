/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the any-size (chunked) crypto helpers in {@link APIUtil}
 * ({@code encryptAndBase64EncodeAnySize} / {@code base64DecodeAndDecryptAnySize} / {@code isChunkedCipherText}).
 * The kernel {@link CryptoUtil} is replaced with a reversible base64 "cipher" so the tests exercise the
 * chunking/format logic (block splitting, the {@code chunk:v1:} marker, reassembly, and the single-shot
 * fallbacks) independently of any real crypto provider.
 */
public class APIUtilCryptoTest {

    private static final int MAX_PLAINTEXT_CHUNK_SIZE = 126;

    private CryptoUtil cryptoUtil;

    @Before
    public void setUp() throws Exception {

        cryptoUtil = mock(CryptoUtil.class);
        // Reversible stand-in for the real single-shot primitives: encrypt == base64-encode, decrypt == decode.
        when(cryptoUtil.encryptAndBase64Encode(any(byte[].class)))
                .thenAnswer(invocation -> Base64.getEncoder().encodeToString(invocation.getArgument(0)));
        when(cryptoUtil.base64DecodeAndDecrypt(anyString()))
                .thenAnswer(invocation -> Base64.getDecoder().decode((String) invocation.getArgument(0)));
    }

    @Test
    public void testSmallSecretRoundTrips() throws Exception {

        byte[] plain = "{\"type\":\"service_account\"}".getBytes(StandardCharsets.UTF_8);

        String cipher = APIUtil.encryptAndBase64EncodeAnySize(cryptoUtil, plain);

        Assert.assertTrue("A non-empty secret must carry the chunk marker", cipher.startsWith("chunk:v1:"));
        Assert.assertFalse("A single small chunk has no delimiter", cipher.substring("chunk:v1:".length())
                .contains(";"));
        Assert.assertArrayEquals(plain, APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, cipher));
    }

    @Test
    public void testLargeSecretIsChunkedAndRoundTrips() throws Exception {

        // 300 bytes -> ceil(300 / 126) = 3 chunks.
        byte[] plain = new byte[300];
        for (int i = 0; i < plain.length; i++) {
            plain[i] = (byte) (i % 256);
        }

        String cipher = APIUtil.encryptAndBase64EncodeAnySize(cryptoUtil, plain);

        int chunks = cipher.substring("chunk:v1:".length()).split(";", -1).length;
        Assert.assertEquals(3, chunks);
        verify(cryptoUtil, times(3)).encryptAndBase64Encode(any(byte[].class));
        Assert.assertArrayEquals(plain, APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, cipher));
    }

    @Test
    public void testExactBlockBoundaryProducesSingleChunk() throws Exception {

        byte[] plain = new byte[MAX_PLAINTEXT_CHUNK_SIZE];

        String cipher = APIUtil.encryptAndBase64EncodeAnySize(cryptoUtil, plain);

        Assert.assertEquals(1, cipher.substring("chunk:v1:".length()).split(";", -1).length);
        Assert.assertArrayEquals(plain, APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, cipher));
    }

    @Test
    public void testEmptySecretIsEncryptedSingleShotWithoutMarker() throws Exception {

        String cipher = APIUtil.encryptAndBase64EncodeAnySize(cryptoUtil, new byte[0]);

        Assert.assertFalse("An empty secret must not be marked as chunked", APIUtil.isChunkedCipherText(cipher));
        Assert.assertArrayEquals(new byte[0], APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, cipher));
    }

    @Test(expected = CryptoException.class)
    public void testNullPlaintextThrows() throws Exception {

        APIUtil.encryptAndBase64EncodeAnySize(cryptoUtil, null);
    }

    @Test(expected = CryptoException.class)
    public void testNullCipherTextThrows() throws Exception {

        APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, null);
    }

    @Test(expected = CryptoException.class)
    public void testMalformedChunkedValueWithEmptyChunkThrows() throws Exception {

        APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, "chunk:v1:AAAA;;BBBB");
    }

    @Test
    public void testNonChunkedValueIsDecryptedSingleShot() throws Exception {

        // A value without the marker (e.g. a legacy single-shot ciphertext) is decrypted directly.
        String legacy = Base64.getEncoder().encodeToString("legacy".getBytes(StandardCharsets.UTF_8));

        byte[] decrypted = APIUtil.base64DecodeAndDecryptAnySize(cryptoUtil, legacy);

        Assert.assertEquals("legacy", new String(decrypted, StandardCharsets.UTF_8));
        verify(cryptoUtil).base64DecodeAndDecrypt(legacy);
    }

    @Test
    public void testIsChunkedCipherText() {

        Assert.assertTrue(APIUtil.isChunkedCipherText("chunk:v1:AAAA"));
        Assert.assertFalse(APIUtil.isChunkedCipherText("AAAA"));
        Assert.assertFalse(APIUtil.isChunkedCipherText(null));
    }
}
