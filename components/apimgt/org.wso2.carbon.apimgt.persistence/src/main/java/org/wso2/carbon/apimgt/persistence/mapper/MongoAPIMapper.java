package org.wso2.carbon.apimgt.persistence.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;

@Mapper
public interface MongoAPIMapper {
    MongoAPIMapper INSTANCE = Mappers.getMapper(MongoAPIMapper.class);

    @Mapping(source = "id", target = "mongodbUuId")
    MongoDBPublisherAPI toMongoDBPublisherApi(PublisherAPI api);

    @Mapping(source = "mongodbUuId", target = "id")
    PublisherAPI toPublisherApi(MongoDBPublisherAPI api);

    @Mapping(source = "id", target = "mongodbUuId")
    MongoDBDevPortalAPI toMongoDBDevPortalApi(DevPortalAPI api);

    @Mapping(source = "mongodbUuId", target = "id")
    DevPortalAPI toDevPortalApi(MongoDBDevPortalAPI api);

    default ObjectId mapStringIdToObjectId(String id) {
        if (id != null) {
            return new ObjectId(id);
        }
        return null;
    }

    default String mapObjectIdToString(ObjectId id) {
        if (id != null) {
            return id.toString();
        }
        return null;
    }
}
