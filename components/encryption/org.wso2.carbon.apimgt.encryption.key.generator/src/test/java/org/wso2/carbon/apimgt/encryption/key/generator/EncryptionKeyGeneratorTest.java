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
import static org.junit.Assert.assertTrue;

public class EncryptionKeyGeneratorTest {

    private static final Pattern KEY_LINE_PATTERN = Pattern.compile("(?m)^\\s*key\\s*=\\s*\"([0-9a-f]{64})\"\\s*$");

    /**
     * Verifies an existing [encryption] section with an empty double-quoted key remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeForExistingEncryptionSectionWithEmptyDoubleQuotedKey() throws IOException {

        String content = "[encryption]\nkey = \"\"\nname = \"default\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies an existing [encryption] section with an empty single-quoted key remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeForExistingEncryptionSectionWithEmptySingleQuotedKey() throws IOException {

        String content = "[encryption]\nkey = ''\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies an inline-comment [encryption] header remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeWhenEncryptionSectionExistsWithInlineComment() throws IOException {

        String content = "[encryption] # initial section\n# key is assumed to exist by contract\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
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
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies config directory input resolves deployment.toml and generates a key.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testGenerateWhenConfigDirectoryPathIsProvided() throws IOException {

        Path[] paths = createConfigDirectoryWithDeployment("[server]\nhostname = \"localhost\"\n");
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertTrue(updated.contains("[encryption]"));
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies a normalized existing [encryption] section prevents key generation.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeWhenEncryptionSectionIsIndented() throws IOException {

        String content = "  [encryption]\nkey = \"\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies a commented [encryption] token does not prevent generation.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testGenerateWhenEncryptionSectionIsOnlyInComment() throws IOException {

        String content = "# [encryption]\n[server]\nhostname = \"localhost\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(10, exitCode);
        assertSingleGeneratedKey(updated);
    }

    /**
     * Verifies config directory input returns error when deployment.toml is missing.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testConfigDirectoryPathReturnsErrorWhenDeploymentTomlMissing() throws IOException {

        Path configDirectory = Files.createTempDirectory("config-directory-missing");
        configDirectory.toFile().deleteOnExit();

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
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
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies an existing [encryption] section with quoted key syntax remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeWhenEncryptionSectionContainsQuotedKeyName() throws IOException {

        String key = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String content = "[encryption]\n\"key\" = \"" + key + "\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies an existing [encryption] section with a quoted empty key name remains unchanged.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoChangeForExistingEncryptionSectionWithQuotedEmptyKey() throws IOException {

        String content = "[encryption]\n\"key\" = \"\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
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

        Path configDirectory = Files.createTempDirectory("config-directory-symlink");
        configDirectory.toFile().deleteOnExit();

        Path externalFile = Files.createTempFile("external-deployment", ".toml");
        externalFile.toFile().deleteOnExit();
        writeFile(externalFile, "[encryption]\n");

        Path deploymentToml = configDirectory.resolve("deployment.toml");
        try {
            Files.createSymbolicLink(deploymentToml, externalFile);
        } catch (UnsupportedOperationException e) {
            Assume.assumeNoException("Symlinks are not supported in this environment", e);
        } catch (IOException e) {
            Assume.assumeNoException("Symlink creation is not permitted in this environment", e);
        }

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        assertEquals(1, exitCode);
    }

    /**
     * Verifies an indented [system.parameter] section with RSA transformation still disables generation.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testNoActionWhenRsaCipherTransformationConfiguredInIndentedSystemParameterSection()
            throws IOException {

        String content = "  [system.parameter]\n"
                + "\"org.wso2.CipherTransformation\"=\"RSA/ECB/OAEPwithSHA1andMGF1Padding\"\n";
        Path[] paths = createConfigDirectoryWithDeployment(content);
        Path configDirectory = paths[0];
        Path deploymentToml = paths[1];

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectory.toString()});
        String updated = readFile(deploymentToml);

        assertEquals(0, exitCode);
        assertEquals(content, updated);
    }

    /**
     * Verifies non-directory config directory path is rejected.
     *
     * @throws IOException if temporary file operations fail
     */
    @Test
    public void testRejectNonDirectoryConfigDirectoryPath() throws IOException {

        Path configDirectoryFile = Files.createTempFile("config-directory-file", ".tmp");
        configDirectoryFile.toFile().deleteOnExit();

        int exitCode = EncryptionKeyGenerator.execute(new String[] {configDirectoryFile.toString()});
        assertEquals(1, exitCode);
    }

    /**
     * Creates a config directory with deployment.toml and returns both paths.
     *
     * @param content deployment.toml content
     * @return array with [0] = config directory, [1] = deployment.toml path
     * @throws IOException if file operations fail
     */
    private static Path[] createConfigDirectoryWithDeployment(String content) throws IOException {

        Path configDirectory = Files.createTempDirectory("config-directory");
        configDirectory.toFile().deleteOnExit();
        Path deploymentToml = configDirectory.resolve("deployment.toml");
        writeFile(deploymentToml, content);
        deploymentToml.toFile().deleteOnExit();
        return new Path[] {configDirectory, deploymentToml};
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
}
