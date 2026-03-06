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

package org.wso2.carbon.apimgt.encryption.key.generator;

import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EncryptionKeyGeneratorTest {

    private static final Pattern KEY_LINE_PATTERN = Pattern.compile("(?m)^\\s*key\\s*=\\s*\"([0-9a-f]{64})\"\\s*$");
    private static final Pattern ENCRYPTION_SECTION_PATTERN = Pattern.compile("(?m)^\\[encryption\\](?:\\s*#.*)?$");

    /**
     * Verifies empty double-quoted key is treated as missing and replaced.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testGenerateForEmptyDoubleQuotedKey() throws IOException {

        String content = "[encryption]\nkey = \"\"\nname = \"default\"\n";
        Path[] paths = createCarbonHomeWithDeployment(content);
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertFalse(updated.contains("key = \"\""));
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies empty single-quoted key is treated as missing and replaced.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testGenerateForEmptySingleQuotedKey() throws IOException {

        String content = "[encryption]\nkey = ''\n";
        Path[] paths = createCarbonHomeWithDeployment(content);
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertFalse(updated.contains("key = ''"));
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies an inline-comment section header does not create duplicate sections.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testInlineCommentSectionHeaderUsesExistingSection() throws IOException {

        String content = "[encryption] # initial section\n# key will be generated\n";
        Path[] paths = createCarbonHomeWithDeployment(content);
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertEquals(1, countMatches(ENCRYPTION_SECTION_PATTERN, updated));
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies active key with inline comment remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeWhenActiveKeyExistsWithInlineComment() throws IOException {

        String key = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String content = "[encryption] # active key is present\nkey = \"" + key + "\" # keep\n";
        Path[] paths = createCarbonHomeWithDeployment(content);
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies CARBON_HOME input resolves deployment.toml and generates a key.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testGenerateWhenCarbonHomePathIsProvided() throws IOException {

        Path[] paths = createCarbonHomeWithDeployment("[server]\nhostname = \"localhost\"\n");
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertTrue(updated.contains("[encryption]"));
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies CARBON_HOME input returns error when deployment.toml is missing.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testCarbonHomePathReturnsErrorWhenDeploymentTomlMissing() throws IOException {

        Path carbonHome = Files.createTempDirectory("carbon-home-missing");
        carbonHome.toFile().deleteOnExit();

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        assertEquals(1, exitCode);
    }

    /**
     * Verifies RSA cipher transformation in system parameters disables key generation.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoActionWhenRsaCipherTransformationConfigured() throws IOException {

        String content = "[system.parameter]\n"
                + "\"org.wso2.CipherTransformation\"=\"RSA/ECB/OAEPwithSHA1andMGF1Padding\"\n";
        Path[] paths = createCarbonHomeWithDeployment(content);
        Path carbonHome = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies symlinked deployment.toml is rejected.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testRejectSymlinkDeploymentToml() throws IOException {

        Path carbonHome = Files.createTempDirectory("carbon-home-symlink");
        carbonHome.toFile().deleteOnExit();
        Path confDir = carbonHome.resolve("repository").resolve("conf");
        Files.createDirectories(confDir);

        Path externalFile = Files.createTempFile("external-deployment", ".toml");
        externalFile.toFile().deleteOnExit();
        writeFile(externalFile, "[encryption]\n");

        Path deploymentToml = confDir.resolve("deployment.toml");
        try {
            Files.createSymbolicLink(deploymentToml, externalFile);
        } catch (UnsupportedOperationException e) {
            Assume.assumeNoException("Symlinks are not supported in this environment", e);
        } catch (IOException e) {
            Assume.assumeNoException("Symlink creation is not permitted in this environment", e);
        }

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHome.toString()});
        assertEquals(1, exitCode);
    }

    /**
     * Verifies non-directory CARBON_HOME path is rejected.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testRejectNonDirectoryCarbonHomePath() throws IOException {

        Path carbonHomeFile = Files.createTempFile("carbon-home-file", ".tmp");
        carbonHomeFile.toFile().deleteOnExit();

        int exitCode = EncryptionKeyGenerator.execute(new String[] {carbonHomeFile.toString()});
        assertEquals(1, exitCode);
    }

    /**
     * Creates CARBON_HOME structure with deployment.toml and returns both paths.
     *
     * @param content deployment.toml content
     * @return array with [0] = CARBON_HOME, [1] = deployment.toml path
     * @throws IOException if file operations fail
     */
    private static Path[] createCarbonHomeWithDeployment(String content) throws IOException {

        Path carbonHome = Files.createTempDirectory("carbon-home");
        carbonHome.toFile().deleteOnExit();
        Path deploymentToml = carbonHome.resolve("repository").resolve("conf").resolve("deployment.toml");
        Files.createDirectories(deploymentToml.getParent());
        writeFile(deploymentToml, content);
        deploymentToml.toFile().deleteOnExit();
        return new Path[] {carbonHome, deploymentToml};
    }

    /**
     * Writes UTF-8 content to file.
     *
     * @param file file path
     * @param content content to write
     * @throws IOException if writing fails
     */
    private static void writeFile(Path file, String content) throws IOException {

        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads UTF-8 file content.
     *
     * @param file file path
     * @return file content
     * @throws IOException if reading fails
     */
    private static String readFile(Path file) throws IOException {

        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    /**
     * Asserts exactly one generated key assignment exists.
     *
     * @param updatedContent updated deployment.toml content
     */
    private static void assertSingleGeneratedKey(String updatedContent) {

        Matcher keyMatcher = KEY_LINE_PATTERN.matcher(updatedContent);
        int keyCount = 0;
        while (keyMatcher.find()) {
            keyCount++;
        }
        assertEquals(1, keyCount);
    }

    /**
     * Counts regex matches in content.
     *
     * @param pattern regex pattern
     * @param content text content
     * @return number of matches
     */
    private static int countMatches(Pattern pattern, String content) {

        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
