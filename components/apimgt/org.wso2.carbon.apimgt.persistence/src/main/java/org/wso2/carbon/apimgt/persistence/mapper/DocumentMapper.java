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
 *
 */
package org.wso2.carbon.apimgt.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.persistence.dto.DocumentContent;

@Mapper
public interface DocumentMapper {
    DocumentMapper INSTANCE = Mappers.getMapper(DocumentMapper.class);
    
    Documentation toDocumentation(org.wso2.carbon.apimgt.persistence.dto.Documentation doc);

    DocumentationContent toDocumentationContent(DocumentContent content);

    org.wso2.carbon.apimgt.persistence.dto.Documentation toDocumentation(Documentation documentation);

    DocumentContent toDocumentContent(DocumentationContent content);
}
