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

package org.wso2.carbon.apimgt.internal.service.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Builds a TAR.GZ archive from deployment ID to YAML content map.
 * Uses minimal TAR format (USTAR) and Java standard library only.
 */
public final class DeploymentTarGzBuilder {

    private static final int TAR_BLOCK_SIZE = 512;

    private DeploymentTarGzBuilder() {
    }

    /**
     * Build TAR bytes from entries (no gzip). Path in archive: {deploymentId}/api-{apiId}.yaml (platform contract).
     * The JAX-RS GZIPOutInterceptor compresses the response once; we must not gzip here to avoid double compression.
     *
     * @param entries for each deployment: (deploymentId, artifactId, yamlContent)
     * @return raw TAR bytes
     */
    public static byte[] buildTar(List<DeploymentEntry> entries) throws IOException {
        ByteArrayOutputStream tarOut = new ByteArrayOutputStream();
        byte[] buffer = new byte[TAR_BLOCK_SIZE];
        final byte[] zeroBlock = new byte[TAR_BLOCK_SIZE];

        for (DeploymentEntry e : entries) {
            if (e.getContent() == null) {
                continue;
            }
            String fileName = e.getDeploymentId() + "/api-" + e.getArtifactId() + ".yaml";
            byte[] content = e.getContent().getBytes(StandardCharsets.UTF_8);
            writeTarEntry(tarOut, buffer, fileName, content);
        }
        // TAR end-of-archive: two 512-byte zero blocks (buffer may still hold last entry header)
        tarOut.write(zeroBlock, 0, TAR_BLOCK_SIZE);
        tarOut.write(zeroBlock, 0, TAR_BLOCK_SIZE);
        return tarOut.toByteArray();
    }

    /**
     * Build TAR.GZ from entries (for use when no outbound gzip interceptor is applied).
     *
     * @param entries for each deployment: (deploymentId, artifactId, yamlContent)
     * @return gzipped TAR bytes
     */
    public static byte[] buildTarGz(List<DeploymentEntry> entries) throws IOException {
        byte[] tarBytes = buildTar(entries);
        ByteArrayOutputStream gzipOut = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(gzipOut)) {
            gzip.write(tarBytes);
        }
        return gzipOut.toByteArray();
    }

    private static void writeTarEntry(ByteArrayOutputStream out, byte[] blockBuffer,
            String fileName, byte[] content) throws IOException {
        byte[] nameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > 99) {
            throw new IOException("File name too long: " + fileName);
        }
        // TAR header: 0-99 filename, 100-107 mode, 108-115 uid, 116-123 gid, 124-135 size (12 octal), 136-147 mtime, 148-155 checksum, 156 typeflag
        Arrays.fill(blockBuffer, (byte) 0);
        System.arraycopy(nameBytes, 0, blockBuffer, 0, nameBytes.length);
        // mode 100644
        writeOctal(blockBuffer, 100, 8, 0100644);
        writeOctal(blockBuffer, 108, 8, 0);
        writeOctal(blockBuffer, 116, 8, 0);
        writeOctal(blockBuffer, 124, 12, content.length);
        writeOctal(blockBuffer, 136, 12, 0); // mtime 0 for deterministic
        blockBuffer[156] = '0'; // typeflag: normal file
        System.arraycopy("ustar\0".getBytes(StandardCharsets.US_ASCII), 0, blockBuffer, 257, 6);
        // USTAR checksum: sum of 512 header bytes with bytes 148-155 treated as spaces
        for (int i = 148; i < 156; i++) {
            blockBuffer[i] = ' ';
        }
        int sum = 0;
        for (int i = 0; i < TAR_BLOCK_SIZE; i++) {
            sum += blockBuffer[i] & 0xff;
        }
        writeOctal(blockBuffer, 148, 8, sum);
        out.write(blockBuffer, 0, TAR_BLOCK_SIZE);
        out.write(content, 0, content.length);
        int pad = (TAR_BLOCK_SIZE - (content.length % TAR_BLOCK_SIZE)) % TAR_BLOCK_SIZE;
        for (int i = 0; i < pad; i++) {
            out.write(0);
        }
    }

    private static void writeOctal(byte[] block, int offset, int len, long value) {
        String octal = Long.toOctalString(value);
        int start = offset + len - octal.length();
        if (start < offset) {
            start = offset;
        }
        for (int i = offset; i < offset + len; i++) {
            block[i] = (byte) (i >= start && i - start < octal.length() ? octal.charAt(i - start) : ' ');
        }
    }

    public static final class DeploymentEntry {
        private final String deploymentId;
        private final String artifactId;
        private final String content;

        public DeploymentEntry(String deploymentId, String artifactId, String content) {
            this.deploymentId = deploymentId;
            this.artifactId = artifactId;
            this.content = content;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getContent() {
            return content;
        }
    }
}
