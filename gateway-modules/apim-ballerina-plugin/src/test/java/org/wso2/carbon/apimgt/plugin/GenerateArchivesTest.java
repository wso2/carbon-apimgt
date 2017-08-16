package org.wso2.carbon.apimgt.plugin;
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

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for ballerina source build and archives generation
 */
public class GenerateArchivesTest {
    private static final Logger log = LoggerFactory.getLogger(GenerateArchivesTest.class);
    private File temDir;
    private GenerateArchives generateArchives;
    private String srcDir;

    @BeforeClass
    void init() {
        Path resourceDirectory = Paths.get("src/test/resources");
        srcDir = resourceDirectory.toAbsolutePath() + File.separator + "configs";
        temDir = Files.createTempDir();
        temDir.deleteOnExit();
        generateArchives = new GenerateArchives();
    }

    @Test
    public void testGenerateMainArchives() {
        String type = "main";
        String packagePath = "org/wso2/carbon/apimgt/gateway/auth";
        String targetName = temDir.getAbsolutePath() + File.separator + "auth";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
        } catch (Exception e) {
            //catching Exception class to catch all the runtime exceptions
            log.error("error generating archives ", e);
            Assert.fail("Exception occurred while building ballerina sources");
        }
        File authBMZ = new File(temDir + File.separator + "auth.bmz");
        Assert.assertTrue(authBMZ.exists(), "auth.bmz archive generation failed");
    }

    @Test
    public void testGenerateServiceArchives() {
        String type = "service";
        String packagePath = "org/wso2/carbon/apimgt/gateway/services";
        String targetName = temDir.getAbsolutePath() + File.separator + "services";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
        } catch (Exception e) {
            //catching Exception class to catch all the runtime exceptions
            log.error("error generating archives ", e);
            Assert.fail("Exception occurred while building ballerina sources");
        }
        File authBMZ = new File(temDir + File.separator + "services.bsz");
        Assert.assertTrue(authBMZ.exists(), "service.bsz archive generation failed");
    }

    @Test
    public void testGenerateArchivesFromInvalidFile() {
        String type = "main";
        String packagePath = "org/wso2/carbon/apimgt/gateway/invalidfile/invalidfile.notbal";
        String targetName = temDir.getAbsolutePath() + File.separator + "invalidfile";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
            Assert.fail("Exception should be occurred for non ballerina files");
        } catch (IllegalArgumentException e) {
            log.error("error generating archives ", e);
        }
    }

    @Test
    public void testGenerateArchivesFromValidBalFile() {
        String type = "main";
        String packagePath = "org/wso2/carbon/apimgt/gateway/bal/validfile.bal";
        String targetName = temDir.getAbsolutePath() + File.separator + "validfile";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
        } catch (IllegalArgumentException e) {
            log.error("error generating archives ", e);
            Assert.fail("Exception should not be occurred for valid ballerina files");
        }

    }

    @Test
    public void testGenerateArchivesFromInvalidPath() {
        String type = "main";
        String packagePath = "org/wso2/carbon/apimgt/gateway/nopackage";
        String targetName = temDir.getAbsolutePath() + File.separator + "nopackage";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
            Assert.fail("Exception should be occurred for invalid source path");
        } catch (RuntimeException e) {
            log.error("error generating archives ", e);
        }
    }

    @Test
    public void testGenerateArchivesForNonSupportedType() {
        String type = "notype";
        String packagePath = "org/wso2/carbon/apimgt/gateway/auth";
        String targetName = temDir.getAbsolutePath() + File.separator + "auth";
        try {
            generateArchives.main(new String[] { type, srcDir, packagePath, targetName });
            Assert.fail("Exception should be occurred for non supported archive type");
        } catch (RuntimeException e) {
            log.error("error generating archives ", e);
        }
    }
}
