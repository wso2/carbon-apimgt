package org.wso2.carbon.apimgt.persistence.dto;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class MongoDBPublisherAPI extends PublisherAPI {

    @BsonProperty(value = "_id")
    @BsonId
    private ObjectId mongodbUuId;

    public ObjectId getMongodbUuId() {
        return mongodbUuId;
    }

    public void setMongodbUuId(ObjectId mongodbUuId) {
        this.mongodbUuId = mongodbUuId;
    }
}
