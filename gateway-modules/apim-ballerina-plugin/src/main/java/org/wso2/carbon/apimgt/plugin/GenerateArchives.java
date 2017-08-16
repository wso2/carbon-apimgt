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

import org.ballerinalang.BLangProgramArchiveBuilder;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.program.BLangPrograms;
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
    public static final String MAIN_TYPE = "main";
    public static final String SERVICE_TYPE = "service";
    private static final Logger log = LoggerFactory.getLogger(GenerateArchives.class);

    public static void main(String[] args) {
        String type = args[0];
        String srcDir = args[1];
        String packagePath = args[2];
        String targetName = args[3];
        new GenerateArchives().execute(type, srcDir, packagePath, targetName);
    }

    /**
     * Generate archives
     *
     * @param type        type of the bal file
     * @param packagePath package path to the bal
     * @param targetName  target file name
     */
    public void execute(String type, String srcDir, String packagePath, String targetName) {
        Path sourcePath = Paths.get(packagePath);
        try {
            Path realPath = Paths.get(srcDir + File.separator + packagePath).toRealPath(LinkOption.NOFOLLOW_LINKS);
            if (!Files.isDirectory(realPath, LinkOption.NOFOLLOW_LINKS) && !realPath.toString()
                    .endsWith(BLangPrograms.BSOURCE_FILE_EXT)) {
                log.error("invalid file or package '" + sourcePath + "'");
                throw new IllegalArgumentException("invalid file or package '" + sourcePath + "'");
            }
        } catch (IOException e) {
            log.error("error reading from file: " + sourcePath + " reason: " + e.getMessage(), e);
            throw new RuntimeException("error reading from file: " + sourcePath + " reason: " + e.getMessage(), e);
        }

        Path programDirPath = Paths.get(srcDir);
        BLangProgram bLangProgram = null;
        if (MAIN_TYPE.equals(type)) {
            bLangProgram = new BLangProgramLoader().loadMain(programDirPath, sourcePath);
        } else if (SERVICE_TYPE.equals(type)) {
            bLangProgram = new BLangProgramLoader().loadService(programDirPath, sourcePath);
        } else {
            log.error("source type '" + type + "' not supported");
            throw new RuntimeException("source type '" + type + "' not supported");
        }

        new BLangProgramArchiveBuilder().build(bLangProgram, targetName.trim());

    }
}
