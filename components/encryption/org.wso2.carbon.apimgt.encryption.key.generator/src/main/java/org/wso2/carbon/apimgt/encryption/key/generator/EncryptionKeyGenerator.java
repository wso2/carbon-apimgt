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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ensures deployment.toml has an active [encryption] key value.
 */
public final class EncryptionKeyGenerator {

    private static final String DEPLOYMENT_TOML_RELATIVE_PATH = "repository" + File.separator + "conf" +
            File.separator + "deployment.toml";
    private static final String ENCRYPTION_SECTION = "[encryption]";
    private static final String SYSTEM_PARAMETER_SECTION = "[system.parameter]";
    private static final String KEY_PROPERTY_NAME = "key";
    private static final String CIPHER_TRANSFORMATION_PROPERTY = "org.wso2.CipherTransformation";
    private static final String RSA_TRANSFORMATION_PREFIX = "RSA/";
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final int KEY_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int EXIT_SUCCESS_NO_CHANGE = 0;
    private static final int EXIT_SUCCESS_GENERATED = 10;
    private static final int EXIT_ERROR = 1;

    /**
     * Prevents instantiation of utility class.
     */
    private EncryptionKeyGenerator() {
    }

    /**
     * Application entry point.
     *
     * @param args arguments containing CARBON_HOME path
     */
    public static void main(String[] args) {

        execute(args);
    }

    /**
     * Executes encryption key generation flow.
     *
     * @param args arguments containing CARBON_HOME path
     * @return process exit code
     */
    static int execute(String[] args) {

        if (args.length != 1) {
            logFailureWithManualAction("Invalid arguments. Expected CARBON_HOME path.",
                    null, null);
            return EXIT_ERROR;
        }
        Path deploymentToml;
        try {
            deploymentToml = resolveDeploymentToml(args[0]);
        } catch (IOException e) {
            logFailureWithManualAction("Invalid CARBON_HOME path.", args[0], e);
            return EXIT_ERROR;
        }

        if (!Files.exists(deploymentToml, LinkOption.NOFOLLOW_LINKS)
                || !Files.isRegularFile(deploymentToml, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(deploymentToml)) {
            logFailureWithManualAction("deployment.toml file not found under CARBON_HOME.",
                    deploymentToml.toAbsolutePath().toString(), null);
            return EXIT_ERROR;
        }
        try {
            Path securedDeploymentTomlPath = deploymentToml.toRealPath(LinkOption.NOFOLLOW_LINKS);
            String content = readFileContent(securedDeploymentTomlPath);
            String newline = content.contains("\r\n") ? "\r\n" : "\n";
            List<String> lines = new ArrayList<>(Arrays.asList(content.split("\\r?\\n", -1)));
            if (isRsaCipherTransformationConfigured(lines)) {
                return EXIT_SUCCESS_NO_CHANGE;
            }
            if (hasActiveEncryptionKey(lines)) {
                return EXIT_SUCCESS_NO_CHANGE;
            }
            String key = generateHexKey();
            int sectionIndex = findFirstEncryptionSection(lines);
            if (sectionIndex >= 0) {
                upsertEncryptionKey(lines, sectionIndex, key);
            } else {
                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                    lines.add("");
                }
                lines.add(ENCRYPTION_SECTION);
                lines.add(toKeyLine(key));
            }
            writeFileContent(securedDeploymentTomlPath, joinLines(lines, newline));
            logGeneratedKeyMessage(securedDeploymentTomlPath.toString());
            return EXIT_SUCCESS_GENERATED;
        } catch (IOException e) {
            logFailureWithManualAction("Automatic encryption key generation failed.",
                    deploymentToml.toAbsolutePath().toString(), e);
            return EXIT_ERROR;
        } catch (Exception e) {
            logFailureWithManualAction("Unexpected error during automatic encryption key generation.",
                    deploymentToml.toAbsolutePath().toString(), e);
            return EXIT_ERROR;
        }
    }

    /**
     * Resolves deployment.toml from CARBON_HOME directory path.
     *
     * @param inputPath CARBON_HOME directory path
     * @return resolved deployment.toml file
     */
    private static Path resolveDeploymentToml(String inputPath) throws IOException {

        try {
            Path carbonHome = Paths.get(inputPath).toAbsolutePath().normalize();
            if (!Files.exists(carbonHome, LinkOption.NOFOLLOW_LINKS)
                    || !Files.isDirectory(carbonHome, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(carbonHome)) {
                throw new IOException("CARBON_HOME path is not a valid non-symlink directory: " + carbonHome);
            }
            return carbonHome.resolve(DEPLOYMENT_TOML_RELATIVE_PATH).normalize();
        } catch (InvalidPathException e) {
            throw new IOException("Invalid CARBON_HOME path: " + inputPath, e);
        }
    }

    /**
     * Reads a UTF-8 file fully.
     *
     * @param file source file
     * @return full file content
     * @throws IOException if file cannot be read
     */
    private static String readFileContent(Path file) throws IOException {

        StringBuilder content = new StringBuilder();
        char[] buffer = new char[2048];
        try (Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }
        }
        return content.toString();
    }

    /**
     * Writes content to the target file using a temporary file and move.
     *
     * @param file target file
     * @param content file content
     * @throws IOException if writing fails
     */
    private static void writeFileContent(Path file, String content) throws IOException {

        Path absoluteTargetPath = file.toAbsolutePath().normalize();
        Path parent = absoluteTargetPath.getParent();
        if (parent == null) {
            parent = absoluteTargetPath.getRoot();
        }
        if (parent == null) {
            throw new IOException("Unable to determine parent directory for deployment.toml path: " + file);
        }

        Path realParent = parent.toRealPath(LinkOption.NOFOLLOW_LINKS);
        Path fileName = absoluteTargetPath.getFileName();
        if (fileName == null) {
            throw new IOException("Invalid deployment.toml path without file name: " + absoluteTargetPath);
        }
        Path targetInRealParent = realParent.resolve(fileName).normalize();
        if (Files.isSymbolicLink(targetInRealParent)) {
            throw new IOException("Refusing to write to symlink deployment.toml path: " + targetInRealParent);
        }
        if (Files.exists(targetInRealParent, LinkOption.NOFOLLOW_LINKS)
                && !Files.isRegularFile(targetInRealParent, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Refusing to write non-regular deployment.toml path: " + targetInRealParent);
        }

        Path tempFile = Files.createTempFile(realParent, fileName.toString(), ".tmp");

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(content);
        }

        try {
            Files.move(tempFile, targetInRealParent, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, targetInRealParent, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // Ignore cleanup failure to avoid masking a prior move exception.
            }
        }
    }

    /**
     * Joins list of lines using the given separator.
     *
     * @param lines lines to join
     * @param separator line separator
     * @return joined content
     */
    private static String joinLines(List<String> lines, String separator) {

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                output.append(separator);
            }
            output.append(lines.get(i));
        }
        return output.toString();
    }

    /**
     * Finds first active [encryption] section.
     *
     * @param lines deployment.toml lines
     * @return section index, or -1 when absent
     */
    private static int findFirstEncryptionSection(List<String> lines) {

        for (int i = 0; i < lines.size(); i++) {
            String normalized = normalizedLineWithoutComment(lines.get(i));
            if (normalized.isEmpty()) {
                continue;
            }
            if (ENCRYPTION_SECTION.equals(extractSectionHeader(normalized))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks whether an active key exists in [encryption] section.
     *
     * @param lines deployment.toml lines
     * @return true if active key exists
     */
    private static boolean hasActiveEncryptionKey(List<String> lines) {

        boolean inEncryptionSection = false;
        for (String rawLine : lines) {
            String normalized = normalizedLineWithoutComment(rawLine);
            if (normalized.isEmpty()) {
                continue;
            }
            String section = extractSectionHeader(normalized);
            if (section != null) {
                inEncryptionSection = ENCRYPTION_SECTION.equals(section);
                continue;
            }
            if (inEncryptionSection && isActiveKeyLine(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether RSA cipher transformation is explicitly configured.
     *
     * @param lines deployment.toml lines
     * @return true if configured cipher transformation starts with RSA/
     */
    private static boolean isRsaCipherTransformationConfigured(List<String> lines) {

        boolean inSystemParameterSection = false;
        for (String rawLine : lines) {
            String normalized = normalizedLineWithoutComment(rawLine);
            if (normalized.isEmpty()) {
                continue;
            }
            String section = extractSectionHeader(normalized);
            if (section != null) {
                inSystemParameterSection = SYSTEM_PARAMETER_SECTION.equals(section);
                continue;
            }
            if (!inSystemParameterSection) {
                continue;
            }
            String keyName = normalizePropertyName(extractKeyName(normalized));
            if (!CIPHER_TRANSFORMATION_PROPERTY.equals(keyName)) {
                continue;
            }
            String value = normalizePropertyValue(extractValue(normalized));
            return value.regionMatches(true, 0, RSA_TRANSFORMATION_PREFIX, 0,
                    RSA_TRANSFORMATION_PREFIX.length());
        }
        return false;
    }

    /**
     * Removes trailing carriage return from a line when present.
     *
     * @param line input line
     * @return line without trailing carriage return
     */
    private static String stripCarriageReturn(String line) {

        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    /**
     * Normalizes a line by trimming and removing inline comments.
     *
     * @param line input line
     * @return normalized line, or empty string if blank/comment
     */
    private static String normalizedLineWithoutComment(String line) {

        String stripped = stripCarriageReturn(line).trim();
        if (stripped.isEmpty() || stripped.startsWith("#")) {
            return "";
        }
        return stripInlineComment(stripped).trim();
    }

    /**
     * Extracts section header token from a normalized line.
     *
     * @param normalizedLine normalized line
     * @return section header token or null
     */
    private static String extractSectionHeader(String normalizedLine) {

        if (!normalizedLine.startsWith("[") || normalizedLine.startsWith("[[")) {
            return null;
        }
        int delimiterIndex = normalizedLine.lastIndexOf(']');
        if (delimiterIndex <= 0 || delimiterIndex != normalizedLine.length() - 1) {
            return null;
        }
        return normalizedLine.substring(0, delimiterIndex + 1);
    }

    /**
     * Checks whether a normalized line defines active key property.
     *
     * @param normalizedLine normalized line
     * @return true if key property has non-empty value
     */
    private static boolean isActiveKeyLine(String normalizedLine) {

        String keyName = normalizePropertyName(extractKeyName(normalizedLine));
        if (!KEY_PROPERTY_NAME.equals(keyName)) {
            return false;
        }
        String value = extractValue(normalizedLine);
        return !value.isEmpty() && !isEmptyQuotedValue(value);
    }

    /**
     * Extracts key/property name from an assignment line.
     *
     * @param normalizedLine normalized line
     * @return property name, or null if not an assignment
     */
    private static String extractKeyName(String normalizedLine) {

        int delimiterIndex = normalizedLine.indexOf('=');
        if (delimiterIndex <= 0) {
            return null;
        }
        return normalizedLine.substring(0, delimiterIndex).trim();
    }

    /**
     * Extracts value segment from an assignment line.
     *
     * @param normalizedLine normalized line
     * @return trimmed value, or empty string if not an assignment
     */
    private static String extractValue(String normalizedLine) {

        int delimiterIndex = normalizedLine.indexOf('=');
        if (delimiterIndex <= 0) {
            return "";
        }
        return normalizedLine.substring(delimiterIndex + 1).trim();
    }

    /**
     * Normalizes property name by trimming and removing surrounding quotes.
     *
     * @param keyName extracted key/property name
     * @return normalized property name
     */
    private static String normalizePropertyName(String keyName) {

        if (keyName == null) {
            return null;
        }
        return stripSurroundingQuotes(keyName.trim());
    }

    /**
     * Normalizes property value by trimming and removing surrounding quotes.
     *
     * @param value extracted property value
     * @return normalized property value
     */
    private static String normalizePropertyValue(String value) {

        return stripSurroundingQuotes(value.trim());
    }

    /**
     * Removes matching surrounding single or double quotes.
     *
     * @param value input text
     * @return text without surrounding quotes when present
     */
    private static String stripSurroundingQuotes(String value) {

        if (value.length() < 2) {
            return value;
        }
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Checks whether value is an empty quoted string.
     *
     * @param value value text
     * @return true for empty single- or double-quoted values
     */
    private static boolean isEmptyQuotedValue(String value) {

        return "\"\"".equals(value) || "''".equals(value);
    }

    /**
     * Removes inline comment marker while respecting quoted strings.
     *
     * @param line input line
     * @return line content without inline comment
     */
    private static String stripInlineComment(String line) {

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '\\' && inDoubleQuotes && !escaped) {
                escaped = true;
                continue;
            }
            if (current == '"' && !inSingleQuotes && !escaped) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (current == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (current == '#' && !inSingleQuotes && !inDoubleQuotes) {
                return line.substring(0, i);
            }
            escaped = false;
        }
        return line;
    }

    /**
     * Inserts or replaces encryption key property within [encryption] section.
     *
     * @param lines deployment.toml lines
     * @param sectionIndex index of [encryption] section
     * @param key generated key
     */
    private static void upsertEncryptionKey(List<String> lines, int sectionIndex, String key) {

        List<Integer> existingKeyIndexes = new ArrayList<>();
        int sectionEnd = lines.size();
        for (int i = sectionIndex + 1; i < lines.size(); i++) {
            String normalized = normalizedLineWithoutComment(lines.get(i));
            if (normalized.isEmpty()) {
                continue;
            }
            if (extractSectionHeader(normalized) != null) {
                sectionEnd = i;
                break;
            }
            if (KEY_PROPERTY_NAME.equals(normalizePropertyName(extractKeyName(normalized)))) {
                existingKeyIndexes.add(i);
            }
        }

        if (existingKeyIndexes.isEmpty()) {
            lines.add(sectionIndex + 1, toKeyLine(key));
            return;
        }

        int firstKeyIndex = existingKeyIndexes.get(0);
        String indentation = extractIndentation(lines.get(firstKeyIndex));
        lines.set(firstKeyIndex, indentation + toKeyLine(key));

        for (int i = existingKeyIndexes.size() - 1; i >= 1; i--) {
            int removeIndex = existingKeyIndexes.get(i);
            if (removeIndex < sectionEnd) {
                lines.remove(removeIndex);
            }
        }
    }

    /**
     * Extracts leading whitespace from a line.
     *
     * @param line input line
     * @return indentation prefix
     */
    private static String extractIndentation(String line) {

        int index = 0;
        while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
            index++;
        }
        return line.substring(0, index);
    }

    /**
     * Builds TOML key assignment line.
     *
     * @param key generated key
     * @return key assignment line
     */
    private static String toKeyLine(String key) {
        return KEY_PROPERTY_NAME + " = \"" + key + "\"";
    }

    /**
     * Generates random hex key string.
     *
     * @return 64-character lowercase hex key
     */
    private static String generateHexKey() {

        byte[] keyBytes = new byte[KEY_BYTES];
        SECURE_RANDOM.nextBytes(keyBytes);
        char[] hexBuffer = new char[KEY_BYTES * 2];
        for (int i = 0; i < keyBytes.length; i++) {
            int value = keyBytes[i] & 0xFF;
            hexBuffer[i * 2] = HEX_DIGITS[value >>> 4];
            hexBuffer[i * 2 + 1] = HEX_DIGITS[value & 0x0F];
        }
        return new String(hexBuffer);
    }

    /**
     * Logs warning after auto-generating encryption key.
     *
     * @param deploymentTomlPath deployment.toml absolute path
     */
    private static void logGeneratedKeyMessage(String deploymentTomlPath) {

        System.err.print(
                    System.lineSeparator() +
                    "##################################  ALERT  ##################################" +
                    System.lineSeparator() +
                    "[WARNING]: A random encryption key has been created and added to deployment.toml at" +
                    System.lineSeparator()
                    + deploymentTomlPath + "." +
                    System.lineSeparator() +
                    "Please modify this [encryption] key and follow the production guidelines in the documentation" +
                            " for a safe production deployment." +
                    System.lineSeparator() +
                    "#############################################################################" +
                    System.lineSeparator() +
                    System.lineSeparator());
    }

    /**
     * Logs failure details and manual action instructions.
     *
     * @param message failure message
     * @param deploymentTomlPath deployment.toml path if known
     * @param e optional exception
     */
    private static void logFailureWithManualAction(String message, String deploymentTomlPath, Exception e) {

        StringBuilder logMessage = new StringBuilder()
                .append(System.lineSeparator())
                .append("##################################  ALERT  ##################################")
                .append(System.lineSeparator())
                .append("[ERROR]: ")
                .append(message)
                .append(System.lineSeparator());

        if (deploymentTomlPath != null) {
            logMessage
                    .append("No encryption key was added to deployment.toml at ")
                    .append(deploymentTomlPath)
                    .append(".")
                    .append(System.lineSeparator());
        }

        logMessage
                .append("Please add [encryption] key manually and follow the guidelines in the documentation for a" +
                        " safe production deployment.")
                .append(System.lineSeparator())
                .append("#############################################################################")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        System.err.print(logMessage.toString());
        if (e != null) {
            e.printStackTrace(System.err);
        }
    }
}
