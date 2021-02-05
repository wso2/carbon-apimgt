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

package org.wso2.carbon.apimgt.persistence.mongodb.dto;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;

public class MongoDBDevPortalAPI extends DevPortalAPI {

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
