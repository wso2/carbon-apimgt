/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.plugin;

import org.ballerinalang.BLangCompiler;
import org.ballerinalang.util.BLangConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to generate ballerina archives form the bal file.
 */
public class GenerateArchives {
    private static final Logger log = LoggerFactory.getLogger(GenerateArchives.class);

    public static void main(String[] args) {
        String srcDir = args[0];
        String packagePath = args[1];
        String targetName = args[2];
        new GenerateArchives().execute(srcDir, packagePath, targetName);
    }

    /**
     * Generate archives
     *
     * @param packagePath package path to the bal
     * @param targetName  target file name
     */
    public void execute(String srcDir, String packagePath, String targetName) {
        Path sourcePath = Paths.get(packagePath);
        try {
            Path realPath = Paths.get(srcDir + File.separator + packagePath).toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!Files.isDirectory(realPath, LinkOption.NOFOLLOW_LINKS) && !realPath.toString()
                    .endsWith(BLangConstants.BLANG_SRC_FILE_SUFFIX)) {
                log.error("invalid file or package '" + sourcePath + "'");
                throw new IllegalArgumentException("invalid file or package '" + sourcePath + "'");
            }
        } catch (IOException e) {
            log.error("error reading from file: " + sourcePath + " reason: " + e.getMessage(), e);
            throw new RuntimeException("error reading from file: " + sourcePath + " reason: " + e.getMessage(), e);
        }
        Path programDirPath = Paths.get(srcDir);
        BLangCompiler.compileAndWrite(programDirPath, Paths.get(packagePath), Paths.get(targetName));
    }
}
