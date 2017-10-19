/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.apimgt.ballerina.caching.util;

import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.util.codegen.ProgramFile;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for Ballerina Parser.
 *
 * @since 0.10-SNAPSHOT
 */
public class BTestUtils {

    private BTestUtils() {
    }

    /**
     * Get parsed, analyzed and linked Ballerina object model.
     *
     * @param sourceFilePath Path to Bal file.
     * @return BLangProgram instance.
     */
    public static ProgramFile parseBalFile(String sourceFilePath) {
        Path programPath;
        try {
            programPath = Paths.get(BTestUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return new BLangProgramLoader().loadMainProgramFile(programPath, Paths.get(sourceFilePath));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("error while running test: " + e.getMessage());
        }
    }
}

