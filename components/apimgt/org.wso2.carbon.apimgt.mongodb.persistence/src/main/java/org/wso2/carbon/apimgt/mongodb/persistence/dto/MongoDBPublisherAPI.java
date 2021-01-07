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

package org.wso2.carbon.apimgt.mongodb.persistence.dto;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.persistence.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;

import java.util.Set;

public class MongoDBPublisherAPI extends PublisherAPI {

    @BsonProperty(value = "_id")
    @BsonId
    private ObjectId mongodbUuId;

    private Set<APIDocumentation> documentationList;

    public Set<APIDocumentation> getDocumentationList() {
        return documentationList;
    }

    public void setDocumentationList(Set<APIDocumentation> documentationList) {
        this.documentationList = documentationList;
    }

    public ObjectId getMongodbUuId() {
        return mongodbUuId;
    }

    public void setMongodbUuId(ObjectId mongodbUuId) {
        this.mongodbUuId = mongodbUuId;
    }
}
