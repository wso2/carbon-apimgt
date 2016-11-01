/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.lifecycle.manager.sql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.config.model.LifecycleConfig;
import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Build Lifecycle Configuration from the YAML file
 */
public class LifecycleConfigBuilder {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleConfigBuilder.class);

    public static LifecycleConfig getLifecycleConfig() {
        return lifecycleConfig;
    }

    private static LifecycleConfig lifecycleConfig;

    public static void build(Supplier<LifecycleConfig> defaultConfig) {
        Optional<String> lifecycleConfigFileContent = readFile("lifecycle.yml");
        if (lifecycleConfigFileContent.isPresent()) {
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(representer);
            lifecycleConfig = yaml.loadAs(lifecycleConfigFileContent.get(), LifecycleConfig.class);
        } else {
            lifecycleConfig = defaultConfig.get();
        }
    }

    /**
     * Read file content to a String. The optional will have the file content from the given file in a {@link String}
     * when the file exists in the system or when it is found in the classpath.
     *
     * @param fileName The file name
     * @return An optional {@link String}
     */
    private static Optional<String> readFile(final String fileName) {
        Optional<File> configFile = getConfigFile(fileName);
        try (final InputStream in = configFile.isPresent() ?
                new FileInputStream(configFile.get()) :
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                        Stream<String> stream = buffer.lines()) {
                    String fileContent = stream.map(Utils::substituteVariables)
                            .collect(Collectors.joining(System.lineSeparator()));
                    return Optional.of(fileContent);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read lines from the file: " + fileName, e);
        }

        return Optional.empty();
    }

    /**
     * Get the configuration file. The optional will have the file only if it exists and if it is a valid file.
     *
     * @param fileName The file name
     * @return An optional {@link File}
     */
    private static Optional<File> getConfigFile(final String fileName) {
        File file = new File(Utils.getCarbonConfigHome().resolve(fileName).toString());

        if (file.exists() && file.isFile()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Configuration file found at {}", file.getAbsolutePath());
            }
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

}
