/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.persistence.mongodb.mappers;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBDevPortalAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;

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
