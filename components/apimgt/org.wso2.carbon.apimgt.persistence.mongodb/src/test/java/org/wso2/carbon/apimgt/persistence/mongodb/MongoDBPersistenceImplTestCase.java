/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.mongodb;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.Organization;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.persistence.exceptions.OASPersistenceException;
import org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks.MockAPIs;
import org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks.MockDB;
import org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks.MockMongoDBCollection;
import org.wso2.carbon.apimgt.persistence.mongodb.utils.MongoDBConnectionUtil;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MongoDBConnectionUtil.class})
public class MongoDBPersistenceImplTestCase {

    private MongoDBPersistenceImpl apiPersistenceInstance;
    private Organization organization;
    MockMongoDBCollection mockMongoDBCollection;
    MockDB mockDB;
    MockAPIs mockAPIs;

    @Before
    public void init() {
        apiPersistenceInstance = new MongoDBPersistenceImpl();
        organization = new Organization("test_org");
        mockMongoDBCollection = new MockMongoDBCollection();
        mockDB = Mockito.mock(MockDB.class);
        mockStatic(MongoDBConnectionUtil.class);
        when(MongoDBConnectionUtil.getDatabase()).thenReturn(mockDB);
        when(MongoDBConnectionUtil.getPublisherCollection(anyString())).thenReturn(mockMongoDBCollection);
        when(MongoDBConnectionUtil.getDevPortalCollection(anyString())).thenReturn(mockMongoDBCollection);
        mockAPIs = new MockAPIs();
    }

    @Test
    public void testGetPublisherAPI() throws APIPersistenceException {
        ObjectId apiId = new ObjectId();
        PublisherAPI publisherAPI = apiPersistenceInstance.getPublisherAPI(organization, apiId.toHexString());
        Assert.assertNotNull(publisherAPI);
    }

    @Test
    public void testAddAPI() throws APIPersistenceException {
        PublisherAPI singleAPI = mockAPIs.getSinglePublisherAPI();
        PublisherAPI publisherAPI = apiPersistenceInstance.addAPI(organization, singleAPI);
        Assert.assertNotNull(publisherAPI);
    }

    @Test
    public void testSwaggerGet() throws OASPersistenceException {
        ObjectId apiId = new ObjectId();
        String oasDefinition = apiPersistenceInstance.getOASDefinition(organization, apiId.toHexString());
        Assert.assertNotNull(oasDefinition);
    }

    @Test
    public void testUpdateAPI() throws APIPersistenceException {
        PublisherAPI singleAPI = mockAPIs.getSinglePublisherAPI();

        PublisherAPI publisherAPI = apiPersistenceInstance.updateAPI(organization, singleAPI);
        Assert.assertNotNull(publisherAPI);

        //When swagger is null
        singleAPI.setSwaggerDefinition(null);
        PublisherAPI publisherAPI2 = apiPersistenceInstance.updateAPI(organization, singleAPI);
        Assert.assertNotNull(publisherAPI2);
        Assert.assertNotNull(publisherAPI2.getSwaggerDefinition());
    }

}
