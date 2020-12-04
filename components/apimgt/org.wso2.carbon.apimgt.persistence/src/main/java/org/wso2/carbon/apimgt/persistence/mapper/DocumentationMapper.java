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

package org.wso2.carbon.apimgt.persistence.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.persistence.dto.APIDocumentation;
import org.wso2.carbon.apimgt.persistence.dto.Documentation;

@Mapper
public interface DocumentationMapper {
    DocumentationMapper INSTANCE = Mappers.getMapper(DocumentationMapper.class);

    Documentation toDocumentation(APIDocumentation apiDoc);

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

//    default String mapVisibilityToString(Documentation.DocumentVisibility visibility){
//        return visibility.name();
//    }

//    default Documentation.DocumentVisibility mapVisibilityToString(String visibility){
//        Documentation.DocumentVisibility[] visibilities = Documentation.DocumentVisibility.values();
//        for (Documentation.DocumentVisibility item:visibilities) {
//            if(item.name().equals(visibility)){
//                return item;
//            }
//        }
//        return null;
//    }
//
//    default String mapTypeToString(Documentation.DocumentVisibility visibility){
//        return visibility.name();
//    }
//
//    default Documentation.DocumentVisibility mapVisibilityToString(String visibility){
//        Documentation.DocumentVisibility[] visibilities = Documentation.DocumentVisibility.values();
//        for (Documentation.DocumentVisibility item:visibilities) {
//            if(item.name().equals(visibility)){
//                return item;
//            }
//        }
//        return null;
//    }
}
