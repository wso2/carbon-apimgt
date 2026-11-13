/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.carbon.apimgt.impl.dto.DistributedThrottleConfig;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Tests the batch sync settings of distributed throttling: [apim.distributed_throttling] enable_batch_sync and
 * batch_size in deployment.toml, rendered as EnableBatchSync and BatchSize under DistributedThrottleConfig in
 * api-manager.xml.
 */
public class DistributedThrottleBatchSyncConfigTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /** Loads an api-manager.xml whose DistributedThrottleConfig element has the given children. */
    private DistributedThrottleConfig load(String distributedThrottleChildren) throws Exception {
        File file = folder.newFile();
        String xml = "<APIManager><ThrottlingConfigurations><DistributedThrottleConfig>"
                + "<Enabled>true</Enabled><Type>redis</Type>" + distributedThrottleChildren
                + "</DistributedThrottleConfig></ThrottlingConfigurations></APIManager>";
        Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        APIManagerConfiguration configuration = new APIManagerConfiguration();
        configuration.load(file.getAbsolutePath());
        return configuration.getDistributedThrottleConfig();
    }

    @Test
    public void testDefaults() {
        DistributedThrottleConfig config = new DistributedThrottleConfig();
        Assert.assertTrue("batch sync is on by default", config.isBatchSyncEnabled());
        Assert.assertEquals(500, config.getBatchSize());
        Assert.assertEquals(500, DistributedThrottleConfig.DEFAULT_BATCH_SIZE);
    }

    @Test
    public void testDefaultsApplyWhenTheSettingsAreAbsent() throws Exception {
        DistributedThrottleConfig config = load("");
        Assert.assertTrue(config.isEnabled());
        Assert.assertTrue(config.isBatchSyncEnabled());
        Assert.assertEquals(DistributedThrottleConfig.DEFAULT_BATCH_SIZE, config.getBatchSize());
    }

    @Test
    public void testConfiguredValuesAreRead() throws Exception {
        DistributedThrottleConfig config = load("<EnableBatchSync>false</EnableBatchSync><BatchSize>250</BatchSize>");
        Assert.assertFalse(config.isBatchSyncEnabled());
        Assert.assertEquals(250, config.getBatchSize());
    }

    @Test
    public void testValuesAreTrimmedAndCaseInsensitive() throws Exception {
        DistributedThrottleConfig config = load("<EnableBatchSync> FALSE </EnableBatchSync><BatchSize> 1000 </BatchSize>");
        Assert.assertFalse(config.isBatchSyncEnabled());
        Assert.assertEquals(1000, config.getBatchSize());
    }

    @Test
    public void testInvalidBatchSizesFallBackToTheDefault() throws Exception {
        for (String invalid : new String[]{"0", "-5", "abc", "", "   "}) {
            DistributedThrottleConfig config = load("<BatchSize>" + invalid + "</BatchSize>");
            Assert.assertEquals("BatchSize '" + invalid + "'", DistributedThrottleConfig.DEFAULT_BATCH_SIZE,
                    config.getBatchSize());
        }
    }

    @Test
    public void testBlankEnableBatchSyncKeepsTheDefault() throws Exception {
        Assert.assertTrue(load("<EnableBatchSync></EnableBatchSync>").isBatchSyncEnabled());
    }
}
