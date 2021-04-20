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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import org.apache.poi.ss.formula.functions.T;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPIInfo;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPI;
import org.wso2.carbon.apimgt.persistence.dto.PublisherAPIInfo;

//@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
@Mapper
public interface APIMapper {
    APIMapper INSTANCE = Mappers.getMapper(APIMapper.class);

    @Mapping(source = "providerName", target = "id.providerName")
    @Mapping(source = "apiName", target = "id.apiName")
    @Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "thumbnail", target = "thumbnailUrl")
    @Mapping(source = "availableTierNames", target = "availableTiers")
    @Mapping(source = "visibleOrganizations", target = "visibleTenants")
    @Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    API toApi(PublisherAPI api);

    @Mapping(source = "id.providerName", target = "providerName")
    @Mapping(source = "id.apiName", target = "apiName")
    @Mapping(source = "id.version", target = "version")
    @Mapping(source = "thumbnailUrl", target = "thumbnail")
    @Mapping(source = "availableTiers", target = "availableTierNames")
    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "visibleTenants", target = "visibleOrganizations")
    @Mapping(source = "subscriptionAvailableTenants", target = "subscriptionAvailableOrgs")
    PublisherAPI toPublisherApi(API api);

    @Mapping(source = "providerName", target = "id.providerName")
    @Mapping(source = "apiName", target = "id.apiName")
    @Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "thumbnail", target = "thumbnailUrl")
    @Mapping(source = "context", target = "contextTemplate")
    @Mapping(source = "updatedTime", target = "lastUpdated")
    API toApi(PublisherAPIInfo api);

    @Mapping(source = "providerName", target = "id.providerName")
    @Mapping(source = "apiName", target = "id.apiName")
    @Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "thumbnail", target = "thumbnailUrl")
    @Mapping(source = "context", target = "contextTemplate")
    @Mapping(source = "availableTierNames", target = "availableTiers")
    @Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    API toApi(DevPortalAPIInfo api);
    
    @Mapping(source = "providerName", target = "id.providerName")
    @Mapping(source = "apiName", target = "id.apiName")
    @Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")
    //@Mapping(source = "thumbnail", target = "thumbnailUrl")
    @Mapping(source = "availableTierNames", target = "availableTiers")
    //@Mapping(source = "visibleOrganizations", target = "visibleTenants")
    @Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    //@Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    //@Mapping(source = "environments", target = "environmentList")
    API toApi(DevPortalAPI api);
    
    @Mapping(source = "id.providerName", target = "providerName")
    @Mapping(source = "id.apiName", target = "apiName")
    @Mapping(source = "id.version", target = "version")
    @Mapping(source = "thumbnailUrl", target = "thumbnail")
    @Mapping(source = "availableTiers", target = "availableTierNames")
    @Mapping(source = "uuid", target = "id")
    //@Mapping(source = "visibleTenants", target = "visibleOrganizations")
    @Mapping(source = "subscriptionAvailableTenants", target = "subscriptionAvailableOrgs")
    //@Mapping(source = "environmentList", target = "environments")
    DevPortalAPI toDevPortalApi(API api);
    
    //@Mapping(source = "providerName", target = "id.providerName")
    //@Mapping(source = "apiName", target = "id.apiName")
    //@Mapping(source = "version", target = "id.version")
    @Mapping(source = "id", target = "uuid")
    //@Mapping(source = "thumbnail", target = "thumbnailUrl")
    @Mapping(source = "availableTierNames", target = "availableTiers")
    //@Mapping(source = "visibleOrganizations", target = "visibleTenants")
    @Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    //@Mapping(source = "subscriptionAvailableOrgs", target = "subscriptionAvailableTenants")
    //@Mapping(source = "environments", target = "environmentList")
    @Mapping(source = "status", target = "state")
    @Mapping(source = "swaggerDefinition", target = "definition")
    APIProduct toApiProduct(DevPortalAPI api);

    default JSONObject mapJSONMapToJSONObject(Map<String,String> jsonMap) throws ParseException {
        if (jsonMap != null) {
            JSONParser parser = new JSONParser();
            String jsonText = JSONValue.toJSONString(jsonMap);
            JSONObject jsonObject = (JSONObject) parser.parse(jsonText);
            return jsonObject;
        }
        return null;
    }

    default Map<String, Object> JSONObjectToJSONMap(JSONObject jsonObject){
        Gson gson = new Gson();
        if (jsonObject != null) {
            jsonObject.toJSONString();
            Map<String,Object> fromJson = gson.fromJson(jsonObject.toJSONString(), Map.class);

            return fromJson;
        }
        return null;
    }

    default String mapAccessControlRolesToString(Set<String> accessControlRoles) {
        if (accessControlRoles != null) {
            return String.join(",", accessControlRoles);
        } else {
            return null;
        }
    }

    default Set<String> mapAccessControlRolesToSet(String accessControlRoles){
        if (accessControlRoles != null && !"null".equalsIgnoreCase(accessControlRoles)) {
            return  new HashSet<>(Arrays.asList(accessControlRoles.split(",")));
        } else {
            return null;
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

    default Date mapStringToDate(String dateString) {
        if (dateString != null) {
            return new Date(Long.parseLong(dateString));
        }
        return null;
    }

    default String mapDateToString(Date date) {
        if (date != null) {
            return Long.toString(date.getTime());
        }
        return null;
    }

    default Set<Tier> mapStringToSet(Set<String> tierSet) {

        HashSet<Tier> mappedTiers = new HashSet<Tier>();
        if (tierSet != null) {
            for (String tierName : tierSet) {
                mappedTiers.add(new Tier(tierName));
            }
        }
        return mappedTiers;
    }

    default Set<String> mapTierToSet(Set<Tier> tierSet) {

        HashSet<String> mappedTiers = new HashSet<String>();
        if (tierSet != null) {
            for (Tier tier : tierSet) {
                mappedTiers.add(tier.getName());
            }
        }
        return mappedTiers;
    }
}
