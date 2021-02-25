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

package org.wso2.carbon.apimgt.persistence.mongodb.mappers;

import junit.framework.Assert;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalSearchContent;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherSearchContent;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBPublisherAPI;

public class MongoDBAPIMapperTestCase {

    @Test
    public void testPublisherAPIToMongoDBAPIAndBack() {
        PublisherAPI publisherAPI = new PublisherAPI();
        ObjectId apiId = new ObjectId();
        publisherAPI.setId(apiId.toHexString());
        publisherAPI.setApiName("TestAPI");
        publisherAPI.setVersion("1.0");
        publisherAPI.setType("API");
        publisherAPI.setStatus("PUBLISHED");
        publisherAPI.setProviderName("admin");
        publisherAPI.setContext("/test");
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);

        Assert.assertEquals("Mapped api name does not match", mongoDBPublisherAPI.getApiName(),
                publisherAPI.getApiName());
        Assert.assertEquals("Mapped api version does not match", mongoDBPublisherAPI.getVersion(),
                publisherAPI.getVersion());
        Assert.assertEquals("Mapped api state does not match", mongoDBPublisherAPI.getStatus(),
                publisherAPI.getStatus());
        Assert.assertEquals("Mapped api context does not match", mongoDBPublisherAPI.getContext(),
                publisherAPI.getContext());
        Assert.assertEquals("Mapped api id does not match", mongoDBPublisherAPI.getMongodbUuId().toHexString(),
                publisherAPI.getId());

        PublisherAPI toPublisher = MongoAPIMapper.INSTANCE.toPublisherApi(mongoDBPublisherAPI);
        Assert.assertEquals("Mapped api name does not match", toPublisher.getApiName(),
                mongoDBPublisherAPI.getApiName());
        Assert.assertEquals("Mapped api version does not match", toPublisher.getVersion(),
                mongoDBPublisherAPI.getVersion());
        Assert.assertEquals("Mapped api state does not match", toPublisher.getStatus(),
                mongoDBPublisherAPI.getStatus());
        Assert.assertEquals("Mapped api context does not match", toPublisher.getContext(),
                mongoDBPublisherAPI.getContext());
        Assert.assertEquals("Mapped api id does not match", toPublisher.getId(), mongoDBPublisherAPI
                .getMongodbUuId().toHexString());
    }

    @Test
    public void testDevportalAPIToMongoDBAPIAndBack() {
        DevPortalAPI devPortalAPI = new DevPortalAPI();
        ObjectId apiId = new ObjectId();
        devPortalAPI.setId(apiId.toHexString());
        devPortalAPI.setApiName("TestAPI");
        devPortalAPI.setVersion("1.0");
        devPortalAPI.setType("API");
        devPortalAPI.setStatus("PUBLISHED");
        devPortalAPI.setProviderName("admin");
        devPortalAPI.setContext("/test");
        MongoDBDevPortalAPI mongoDBDevPortalAPI = MongoAPIMapper.INSTANCE.toMongoDBDevPortalApi(devPortalAPI);

        Assert.assertEquals("Mapped api name does not match", mongoDBDevPortalAPI.getApiName(),
                devPortalAPI.getApiName());
        Assert.assertEquals("Mapped api version does not match", mongoDBDevPortalAPI.getVersion(),
                devPortalAPI.getVersion());
        Assert.assertEquals("Mapped api state does not match", mongoDBDevPortalAPI.getStatus(),
                devPortalAPI.getStatus());
        Assert.assertEquals("Mapped api context does not match", mongoDBDevPortalAPI.getContext(),
                devPortalAPI.getContext());
        Assert.assertEquals("Mapped api id does not match", mongoDBDevPortalAPI.getMongodbUuId().toHexString(),
                devPortalAPI.getId());

        DevPortalAPI toDevPortalApi = MongoAPIMapper.INSTANCE.toDevPortalApi(mongoDBDevPortalAPI);
        Assert.assertEquals("Mapped api name does not match", toDevPortalApi.getApiName(),
                mongoDBDevPortalAPI.getApiName());
        Assert.assertEquals("Mapped api version does not match", toDevPortalApi.getVersion(),
                mongoDBDevPortalAPI.getVersion());
        Assert.assertEquals("Mapped api state does not match", toDevPortalApi.getStatus(),
                mongoDBDevPortalAPI.getStatus());
        Assert.assertEquals("Mapped api context does not match", toDevPortalApi.getContext(),
                mongoDBDevPortalAPI.getContext());
        Assert.assertEquals("Mapped api id does not match", toDevPortalApi.getId(),
                mongoDBDevPortalAPI.getMongodbUuId().toHexString());
    }

    @Test
    public void testMongoDBPublisherAPIToPublisherContent() {
        MongoDBPublisherAPI mongoDBPublisherAPI = new MongoDBPublisherAPI();
        ObjectId apiId = new ObjectId();
        mongoDBPublisherAPI.setMongodbUuId(apiId);
        mongoDBPublisherAPI.setId(apiId.toHexString());
        mongoDBPublisherAPI.setApiName("TestAPI");
        mongoDBPublisherAPI.setVersion("1.0.");
        mongoDBPublisherAPI.setType("API");
        mongoDBPublisherAPI.setStatus("PUBLISHED");
        mongoDBPublisherAPI.setProviderName("admin");
        mongoDBPublisherAPI.setContext("/test");
        PublisherSearchContent publisherSearchContent = MongoAPIMapper.INSTANCE.toPublisherContentApi(mongoDBPublisherAPI);
        Assert.assertEquals("Mapped api name does not match", mongoDBPublisherAPI.getApiName(),
                publisherSearchContent.getName());
        Assert.assertEquals("Mapped api version does not match", mongoDBPublisherAPI.getVersion(),
                publisherSearchContent.getVersion());
        Assert.assertEquals("Mapped api status does not match", mongoDBPublisherAPI.getStatus(),
                publisherSearchContent.getStatus());
        Assert.assertEquals("Mapped api context does not match", mongoDBPublisherAPI.getContext(),
                publisherSearchContent.getContext());
        Assert.assertEquals("Mapped api id does not match", mongoDBPublisherAPI.getMongodbUuId().toHexString(),
                publisherSearchContent.getId());
    }

    @Test
    public void testMongoDBDevportalAPIToPublisherContent() {
        MongoDBDevPortalAPI mongoDBDevPortalAPI = new MongoDBDevPortalAPI();
        ObjectId apiId = new ObjectId();
        mongoDBDevPortalAPI.setMongodbUuId(apiId);
        mongoDBDevPortalAPI.setId(apiId.toHexString());
        mongoDBDevPortalAPI.setApiName("TestAPI");
        mongoDBDevPortalAPI.setVersion("1.0.");
        mongoDBDevPortalAPI.setType("API");
        mongoDBDevPortalAPI.setStatus("PUBLISHED");
        mongoDBDevPortalAPI.setProviderName("admin");
        mongoDBDevPortalAPI.setContext("/test");
        DevPortalSearchContent devPortalSearchContent =
                MongoAPIMapper.INSTANCE.toDevportalContentApi(mongoDBDevPortalAPI);
        Assert.assertEquals("Mapped api name does not match", mongoDBDevPortalAPI.getApiName(),
                devPortalSearchContent.getName());
        Assert.assertEquals("Mapped api version does not match", mongoDBDevPortalAPI.getVersion(),
                devPortalSearchContent.getVersion());
        Assert.assertEquals("Mapped api status does not match", mongoDBDevPortalAPI.getStatus(),
                devPortalSearchContent.getStatus());
        Assert.assertEquals("Mapped api context does not match", mongoDBDevPortalAPI.getContext(),
                devPortalSearchContent.getContext());
        Assert.assertEquals("Mapped api id does not match", mongoDBDevPortalAPI.getMongodbUuId().toHexString(),
                devPortalSearchContent.getId());
    }
}
