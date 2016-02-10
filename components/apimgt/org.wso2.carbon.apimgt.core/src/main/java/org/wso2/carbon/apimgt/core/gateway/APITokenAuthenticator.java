/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.gateway;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.ArrayList;
import java.util.HashSet;

public class APITokenAuthenticator {

    private static final Log log = LogFactory.getLog(APITokenAuthenticator.class);



    public APIInfoDTO doGetAPIInfo(String context, String apiVersion) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        try {
            ArrayList<URITemplate> uriTemplates = getAllURITemplates(context, apiVersion);

            apiInfoDTO.setApiName(context);
            apiInfoDTO.setContext(context);
            apiInfoDTO.setVersion(apiVersion);
            apiInfoDTO.setResources(new HashSet<ResourceInfoDTO>());

            ResourceInfoDTO resourceInfoDTO = null;
            VerbInfoDTO verbInfoDTO;
            for (URITemplate uriTemplate : uriTemplates) {
                if (resourceInfoDTO != null && resourceInfoDTO.getUrlPattern().equalsIgnoreCase(uriTemplate.getUriTemplate())) {
                    HashSet<VerbInfoDTO> verbs = (HashSet<VerbInfoDTO>) resourceInfoDTO.getHttpVerbs();
                    verbInfoDTO = new VerbInfoDTO();
                    verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
                    verbInfoDTO.setAuthType(uriTemplate.getAuthType());
                    verbs.add(verbInfoDTO);
                    resourceInfoDTO.setHttpVerbs(verbs);
                    apiInfoDTO.getResources().add(resourceInfoDTO);
                } else {
                    resourceInfoDTO = new ResourceInfoDTO();
                    resourceInfoDTO.setUrlPattern(uriTemplate.getUriTemplate());
                    verbInfoDTO = new VerbInfoDTO();
                    verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
                    verbInfoDTO.setAuthType(uriTemplate.getAuthType());
                    HashSet<VerbInfoDTO> httpVerbs2 = new HashSet<VerbInfoDTO>();
                    httpVerbs2.add(verbInfoDTO);
                    resourceInfoDTO.setHttpVerbs(httpVerbs2);
                    apiInfoDTO.getResources().add(resourceInfoDTO);
                }
            }
        } catch (APIManagementException e) {
            log.error("Loading URI templates for " + context + ":" + apiVersion + " failed", e);
        }

        return apiInfoDTO;
    }

    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion) throws APIManagementException {
        try {
            return ApiMgtDAO.getInstance().getAllURITemplates(context, apiVersion);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while looking up API resource URI templates in the database", e);
        }
    }

    public String getResourceAuthenticationScheme(String context, String apiVersion,
                                                  String requestPath, String httpMethod) {

        String cacheKey = context + ":" + apiVersion;
        APIInfoDTO apiInfoDTO = null;
        if (ServiceReferenceHolder.getInstance().isGatewayAPIKeyValidationEnabled()) {
            apiInfoDTO = (APIInfoDTO) getResourceCache().get(cacheKey);
        }

        if (apiInfoDTO == null) {
            apiInfoDTO = doGetAPIInfo(context, apiVersion);
            getResourceCache().put(cacheKey, apiInfoDTO);
        }

        //Match the case where the direct api context is matched
        if ("/".equals(requestPath)) {
            String requestCacheKey = context + "/" + apiVersion + requestPath + ":" + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (ServiceReferenceHolder.getInstance().isGatewayAPIKeyValidationEnabled()) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }
            //On a cache hit
            if (matchingVerb != null) {
                return matchingVerb.getAuthType();
            } else {
                for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                    String urlPattern = resourceInfoDTO.getUrlPattern();

                    //If the request patch is '/', it can only be matched with a resource whose url-context is '/*'
                    if ("/*".equals(urlPattern)) {
                        for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                            if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                //Store verb in cache
                                getResourceCache().put(requestCacheKey, verbDTO);
                                return verbDTO.getAuthType();
                            }
                        }
                    }
                }
            }
        }
        //Remove the ending '/' from request
        if (requestPath != null && requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }

        while (requestPath != null && requestPath.length() > 1) {
            String requestCacheKey = context + "/" + apiVersion + requestPath + ":" + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (ServiceReferenceHolder.getInstance().isGatewayAPIKeyValidationEnabled()) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }

            //On a cache hit
            if (matchingVerb != null) {
                return matchingVerb.getAuthType();
            }
            //On a cache miss
            else {
                for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                    String urlPattern = resourceInfoDTO.getUrlPattern();
                    if (urlPattern.endsWith("/*")) {
                        //Remove the ending '/*'
                        urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
                    }
                    //If the urlPattern ends with a '/', remove that as well.
                    //urlPattern = RESTUtils.trimTrailingSlashes(urlPattern);
                    if (urlPattern.endsWith("/")) {
                        urlPattern = urlPattern.substring(0, urlPattern.length() - 1);
                    }

                    if (requestPath.endsWith(urlPattern)) {
                        for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                            if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                //Store verb in cache
                                getResourceCache().put(requestCacheKey, verbDTO);
                                return verbDTO.getAuthType();
                            }
                        }
                    }
                }
            }
            //Remove the section after the last occurrence of the '/' character
            int index = requestPath.lastIndexOf("/");
            requestPath = requestPath.substring(0, index <= 0 ? 0 : index);
        }
        //nothing found. return the highest level of security
        return APIConstants.NO_MATCHING_AUTH_SCHEME;
    }

    protected Cache getResourceCache(){
        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.RESOURCE_CACHE_NAME);
    }
}
