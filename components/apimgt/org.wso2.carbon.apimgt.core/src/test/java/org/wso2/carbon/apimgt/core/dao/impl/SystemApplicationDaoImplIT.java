/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.junit.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;

import java.util.UUID;


public class SystemApplicationDaoImplIT extends DAOIntegrationTestBase {
    @Test
    public void testAddAndGetApplicationKey() throws Exception {
        SystemApplicationDao systemApplicationDao = DAOFactory.getSystemApplicationDao();
        String consumerKey = UUID.randomUUID().toString();
        String appName = "publisher";
        systemApplicationDao.addApplicationKey(appName, consumerKey);
        String retrievedConsumerKey = systemApplicationDao.getConsumerKeyForApplication(appName);
        Assert.assertNotNull(retrievedConsumerKey);
        Assert.assertEquals(retrievedConsumerKey, consumerKey);
        Assert.assertTrue(systemApplicationDao.isConsumerKeyExistForApplication(appName));
        systemApplicationDao.removeConsumerKeyForApplication(appName);
        Assert.assertFalse(systemApplicationDao.isConsumerKeyExistForApplication(appName));
        try {
            systemApplicationDao.getConsumerKeyForApplication(appName);
            Assert.fail("System application exist");
        } catch (APIMgtDAOException e) {
            Assert.assertTrue(true);
        }
    }

}
