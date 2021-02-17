package org.wso2.carbon.apimgt.persistence.mongodb.mappers;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;
import org.wso2.carbon.apimgt.persistence.mapper.APIMapper;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBPublisherAPI;

public class MongoDBAPIMapperTestCase {


    @Test
    public void testPublisherAPIToMongoDBAPI() {
        PublisherAPI publisherAPI = new PublisherAPI();
        publisherAPI.setApiName("TestAPI");
        publisherAPI.setVersion("1.0");
        publisherAPI.setType("API");
        publisherAPI.setStatus("PUBLISHED");
        publisherAPI.setProviderName("admin");
        publisherAPI.setContext("/test");
        MongoDBPublisherAPI mongoDBPublisherAPI = MongoAPIMapper.INSTANCE.toMongoDBPublisherApi(publisherAPI);

        Assert.assertEquals("Mapped api name does not match", mongoDBPublisherAPI.getApiName(), publisherAPI.getApiName());
        Assert.assertEquals("Mapped api version does not match", mongoDBPublisherAPI.getVersion(), publisherAPI.getVersion());
        Assert.assertEquals("Mapped api state does not match", mongoDBPublisherAPI.getStatus(), publisherAPI.getStatus());
        Assert.assertEquals("Mapped api state does not match", mongoDBPublisherAPI.getContext(), publisherAPI.getContext());

        PublisherAPI toPublisher = MongoAPIMapper.INSTANCE.toPublisherApi(mongoDBPublisherAPI);
    }

    public void testMongoDBAPIToPublisherApi() {
    }
}
