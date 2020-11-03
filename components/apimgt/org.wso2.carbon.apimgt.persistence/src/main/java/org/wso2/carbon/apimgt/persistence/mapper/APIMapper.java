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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;

@Mapper
public interface APIMapper {
    APIMapper INSTANCE = Mappers.getMapper(APIMapper.class);

    @Mapping(source = "apiOwner", target = "id.providerName")
    @Mapping(source = "apiName", target = "id.apiName")
    @Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")  
    API toApi(PublisherAPI api);
    
    @Mapping(source = "id.providerName", target = "apiOwner")
    @Mapping(source = "id.apiName", target = "apiName")
    @Mapping(source = "id.version", target = "version")
    @Mapping(source = "uuid", target = "id")
    PublisherAPI toPublisherApi(API api);

    default List<Label> mapLabelToList(Set<String> labelSet) {
        List<Label> labels = new ArrayList<Label>();
        if (labelSet != null) {
            for (String labelName : labelSet) {
                Label label = new Label();
                label.setName(labelName);
                labels.add(label);
            }
        }
        return labels;

    }
    
    default Set<String> mapLabelToSet(List<Label> labelList) {
        Set<String> labelSet = new HashSet<String>();
        if (labelList != null) {
            for (Label label : labelList) {
                labelSet.add(label.getName());
            }
        }

        return labelSet;
    }

    default String mapAccessControlRolesToString(Set<String> accessControlRoles) {
        if (accessControlRoles != null) {
            return String.join(",", accessControlRoles);
        } else {
            return "";
        }
    }

    default Set<String> mapAccessControlRolesToSet(String accessControlRoles){
        if(accessControlRoles != null) {
            return  new HashSet<>(Arrays.asList(accessControlRoles.split(",")));
        } else {
            return new HashSet<String>();
        }
    }
    
    default List<APICategory> mapAPICategoriesToList(Set<String> apiCategories) {
        List<APICategory> categoryList = new ArrayList<APICategory>();
        if (apiCategories != null) {
            for (String category : apiCategories) {
                APICategory apiCategory = new APICategory();
                apiCategory.setName(category);
                categoryList.add(apiCategory);
            }
        }
        return categoryList;
    }
    default Set<String> mapAPICategoriesToSet(List<APICategory> apiCategories){
        Set<String> categorySet = new HashSet<String>();
        if (apiCategories != null) {
            for (APICategory category : apiCategories) {
                categorySet.add(category.getName());
            }
        }
        return categorySet;
        
    }
    
    /*
     * this method is created to create compatibility with json-simple 1.1 
     */
    default JSONObject mapJson(JSONObject json) {
        return json;
    }
}

