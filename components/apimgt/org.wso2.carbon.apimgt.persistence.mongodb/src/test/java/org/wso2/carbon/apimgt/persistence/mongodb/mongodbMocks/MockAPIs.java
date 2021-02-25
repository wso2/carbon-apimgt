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

package org.wso2.carbon.apimgt.persistence.mongodb.mongodbMocks;

import org.bson.types.ObjectId;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBPublisherAPI;
import org.wso2.carbon.apimgt.persistence.mongodb.dto.MongoDBThumbnail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockAPIs {

    private List<MongoDBPublisherAPI> publisherAPIList = new ArrayList<>();
    private static final String definition =
            "{\"swagger\":\"2.0\",\"info\":{\"description\":\"This is a sample server Petstore server\"}}";

    public MockAPIs() {
        for (int i = 0; i < 10; i++) {
            MongoDBPublisherAPI mongoDBPublisherAPI = new MongoDBPublisherAPI();
            Set<APIDocumentation> apiDocumentationSet = new HashSet<>();
            ObjectId apiId = new ObjectId();
            mongoDBPublisherAPI.setMongodbUuId(apiId);
            mongoDBPublisherAPI.setId(apiId.toHexString());
            mongoDBPublisherAPI.setApiName("TestAPI_" + i);
            mongoDBPublisherAPI.setVersion("1.0." + i);
            mongoDBPublisherAPI.setType("API");
            mongoDBPublisherAPI.setStatus("PUBLISHED");
            mongoDBPublisherAPI.setProviderName("admin");
            mongoDBPublisherAPI.setContext("/test" + i);
            mongoDBPublisherAPI.setSwaggerDefinition(definition);
            mongoDBPublisherAPI.setMongoDBThumbnail(new MongoDBThumbnail());
            mongoDBPublisherAPI.setDocumentationList(apiDocumentationSet);
            publisherAPIList.add(mongoDBPublisherAPI);
        }
    }

    public List<MongoDBPublisherAPI> getPublisherAPIList() {
        return publisherAPIList;
    }

    public MongoDBPublisherAPI getSingleMongoDBAPI() {
        return publisherAPIList.get(0);
    }

    public PublisherAPI getSinglePublisherAPI() {
        PublisherAPI publisherAPI = new PublisherAPI();
        ObjectId apiId = new ObjectId();
        publisherAPI.setId(apiId.toHexString());
        publisherAPI.setApiName("TestAPI");
        publisherAPI.setVersion("1.0");
        publisherAPI.setType("API");
        publisherAPI.setStatus("PUBLISHED");
        publisherAPI.setProviderName("admin");
        publisherAPI.setContext("/test");
        publisherAPI.setSwaggerDefinition(definition);
        return publisherAPI;
    }
}
