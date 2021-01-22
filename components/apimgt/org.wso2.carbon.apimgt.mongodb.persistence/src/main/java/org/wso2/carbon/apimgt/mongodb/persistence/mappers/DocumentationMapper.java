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

package org.wso2.carbon.apimgt.mongodb.persistence.mappers;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.mongodb.persistence.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;

@Mapper
public interface DocumentationMapper {
    DocumentationMapper INSTANCE = Mappers.getMapper(DocumentationMapper.class);

    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "id", target = "id")
    Documentation toDocumentation(APIDocumentation apiDoc);

    @Mapping(source = "sourceType", target = "sourceType")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "id", target = "id")
    APIDocumentation toAPIDocumentation(Documentation doc);

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
