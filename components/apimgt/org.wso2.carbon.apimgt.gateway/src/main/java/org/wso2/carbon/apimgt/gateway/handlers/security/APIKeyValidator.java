/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;


import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.WSAPIKeyDataStore;
import org.wso2.carbon.apimgt.gateway.handlers.security.thrift.ThriftAPIDataStore;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import javax.cache.Cache;
import javax.cache.Caching;
import java.util.*;

/**
 * This class is used to validate a given API key against a given API context and a version.
 * Actual validation operations are carried out by invoking back-end authentication and
 * key validation services. In order to minimize the network overhead, this implementation
 * caches some API key authentication information in memory. This implementation and the
 * underlying caching implementation are thread-safe. An instance of this class must not be
 * shared among multiple APIs, API handlers or authenticators.
 */
public class APIKeyValidator {

    private APIKeyDataStore dataStore;

    private AxisConfiguration axisConfig;

    private boolean gatewayKeyCacheEnabled = true;

    private boolean isGatewayAPIResourceValidationEnabled = true;

    protected Log log = LogFactory.getLog(getClass());

    public APIKeyValidator(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;

        //check the client type from config
        String keyValidatorClientType = APISecurityUtils.getKeyValidatorClientType();
        if (APIConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(keyValidatorClientType)) {
            this.dataStore = new WSAPIKeyDataStore();
        } else if (APIConstants.API_KEY_VALIDATOR_THRIFT_CLIENT.equals(keyValidatorClientType)) {
            this.dataStore = new ThriftAPIDataStore();
        }

        this.gatewayKeyCacheEnabled = isAPIKeyValidationEnabled();

        this.isGatewayAPIResourceValidationEnabled = isAPIResourceValidationEnabled();

        this.getGatewayKeyCache();

        this.getResourceCache();
    }

    protected Cache getGatewayKeyCache() {
        return Caching.getCacheManager(
                APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.GATEWAY_KEY_CACHE_NAME);
        //return PrivilegedCarbonContext.getCurrentContext(axisConfig).getCache("keyCache");
    }

    protected Cache getResourceCache() {
        return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache(APIConstants.RESOURCE_CACHE_NAME);
        //return PrivilegedCarbonContext.getCurrentContext(axisConfig).getCache("resourceCache");
    }

    /**
     * Get the API key validated against the specified API
     *
     * @param context    API context
     * @param apiKey     API key to be validated
     * @param apiVersion API version number
     * @return An APIKeyValidationInfoDTO object
     * @throws APISecurityException If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getKeyValidationInfo(String context, String apiKey,
                                                        String apiVersion, String authenticationScheme, String clientDomain,
                                                        String matchingResource, String httpVerb, boolean defaultVersionInvoked) throws APISecurityException {

        String prefixedVersion = apiVersion;
        //Check if client has invoked the default version API.
        if(defaultVersionInvoked){
            //Prefix the version so that it looks like _default_1.0 (_default_<version>)).
            //This is so that the Key Validator knows that this request is coming through a default api version
            prefixedVersion = APIConstants.DEFAULT_VERSION_PREFIX.concat(prefixedVersion);
        }

        String cacheKey = APIUtil.getAccessTokenCacheKey(apiKey, context, prefixedVersion, matchingResource,
                                                         httpVerb, authenticationScheme);
        if (gatewayKeyCacheEnabled) {
            APIKeyValidationInfoDTO info = (APIKeyValidationInfoDTO) getGatewayKeyCache().get(cacheKey);

            if (info != null) {
                if (APIUtil.isAccessTokenExpired(info)) {
                    info.setAuthorized(false);
                 // in cache, if token is expired  remove cache entry.
                    getGatewayKeyCache().remove(cacheKey);
                }
                return info;
            }
        }

        //synchronized (apiKey.intern()) {
        // We synchronize on the API key here to allow concurrent processing
        // of different API keys - However when a burst of requests with the
        // same key is encountered, only one will be allowed to execute the logic,
        // and the rest will pick the value from the cache.
        //   info = (APIKeyValidationInfoDTO) infoCache.get(cacheKey);
        // if (info != null) {
        //   return info;
        //}
        APIKeyValidationInfoDTO info = doGetKeyValidationInfo(context, prefixedVersion, apiKey, authenticationScheme, clientDomain,
                                                              matchingResource, httpVerb);
        if (info != null) {
            if (gatewayKeyCacheEnabled && clientDomain == null) { //save into cache only if, validation is correct and api is allowed for all domains
                getGatewayKeyCache().put(cacheKey, info);
            }
            return info;
        } else {
        	String warnMsg = "API key validation service returns null object";
        	log.warn(warnMsg);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    warnMsg);
        }
        //}
    }

    protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, String apiKey,
                                                             String authenticationScheme, String clientDomain,
                                                             String matchingResource, String httpVerb) throws APISecurityException {


        return dataStore.getAPIKeyData(context, apiVersion, apiKey, authenticationScheme, clientDomain,
                                       matchingResource, httpVerb);
    }

    public void cleanup() {
        dataStore.cleanup();
    }

    public boolean isAPIKeyValidationEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.API_GATEWAY_KEY_CACHE_ENABLED);
            return Boolean.parseBoolean(serviceURL);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. Use default configuration" + e);
        }
        return true;
    }
    public boolean isAPIResourceValidationEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.API_GATEWAY_RESOURCE_CACHE_ENABLED);
            return Boolean.parseBoolean(serviceURL);
        } catch (Exception e) {
            log.error("Did not found valid API Resource Validation Information cache configuration. Use default configuration" + e);
        }
        return true;
    }

    public String getResourceAuthenticationScheme(MessageContext synCtx) throws APISecurityException{

        VerbInfoDTO verb = null;
        try {
            verb = findMatchingVerb(synCtx);
        } catch (ResourceNotFoundException e) {
            log.error("Could not find matching resource for request");
            return APIConstants.NO_MATCHING_AUTH_SCHEME;
        }

        if(verb != null){
            return verb.getAuthType();
        }
        else{
            //No matching resource found. return the highest level of security
            return APIConstants.NO_MATCHING_AUTH_SCHEME;
        }

        //Match the case where the direct selectedApi context is matched
        /*if ("/".equals(requestPath)) {
            String requestCacheKey = apiContext + "/" + apiVersion + requestPath + ":" + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (gatewayKeyCacheEnabled) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }
            //On a cache hit
            if (matchingVerb != null) {
                return matchingVerb.getAuthType();
            } else {
                for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                    String urlPattern = resourceInfoDTO.getUrlPattern();

                    //If the request patch is '/', it can only be matched with a resource whose url-context is '*//*'
                    if ("*//*".equals(urlPattern)) {
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
        requestPath = RESTUtils.trimTrailingSlashes(requestPath);

        while (requestPath.length() > 1) {

            String requestCacheKey = apiContext + "/" + apiVersion + requestPath + ":" + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (gatewayKeyCacheEnabled) {
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
                    if (urlPattern.endsWith("*//*")) {
                        //Remove the ending '*//*'
                        urlPattern = urlPattern.substring(0, urlPattern.length() - 2);
                    }
                    //If the urlPattern ends with a '/', remove that as well.
                    urlPattern = RESTUtils.trimTrailingSlashes(urlPattern);

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
        return APIConstants.NO_MATCHING_AUTH_SCHEME;*/
    }

    public VerbInfoDTO findMatchingVerb(MessageContext synCtx) throws ResourceNotFoundException, APISecurityException {

        VerbInfoDTO verb = null;

        //This function is used by more than one handler. If on one execution of this function, it has found and placed
        //the matching verb in the cache, the same can be re-used from all handlers since all handlers share the same
        //MessageContext. The API_RESOURCE_CACHE_KEY property will be set in the MessageContext to indicate that the
        //verb has been put into the cache.
        String resourceCacheKey = (String)synCtx.getProperty(APIConstants.API_RESOURCE_CACHE_KEY);
        if(resourceCacheKey != null){
            verb = (VerbInfoDTO) getResourceCache().get(resourceCacheKey);
            //Cache hit
            if(verb != null){
                if(log.isDebugEnabled()){
                    log.debug("Found resource in Cache for key: ".concat(resourceCacheKey));
                }
                return verb;
            }
            if(log.isDebugEnabled()){
                log.debug("Resource not found in cache for key: ".concat(resourceCacheKey));
            }
        }

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String fullRequestPath = (String)synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);

        String requestPath = fullRequestPath.substring((apiContext + apiVersion).length() + 1);
        if ("".equals(requestPath)) {
            requestPath = "/";
        }

        if(log.isDebugEnabled()){
            log.debug("Setting REST_SUB_REQUEST_PATH in msg context: ".concat(requestPath));
        }
        synCtx.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, requestPath);

        String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);

        API selectedApi = null;
        Resource selectedResource = null;

        for(API api : synCtx.getConfiguration().getAPIs()){
            if(apiContext.equals(api.getContext()) && apiVersion.equals(api.getVersion())){
                if(log.isDebugEnabled()){
                    log.debug("Selected API: ".concat(apiContext).concat(", Version: ").concat(apiVersion));
                }
                selectedApi = api;
                break;
            }
        }

        if (selectedApi.getResources().length > 0) {
            for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                Resource resource = dispatcher.findResource(synCtx, Arrays.asList(selectedApi.getResources()));
                if (resource != null) {
                    selectedResource = resource;
                    break;
                }
            }
        }

        if(selectedResource == null){
            //No matching resource found.
            log.error("Could not find matching resource for " + requestPath);
            throw new ResourceNotFoundException("Could not find matching resource for " + requestPath);
        }

        String resourceString = selectedResource.getDispatcherHelper().getString();
        resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion, resourceString, httpMethod);

        if(log.isDebugEnabled()){
            log.debug("Selected Resource: ".concat(resourceString));
        }
        //Set the elected resource
        synCtx.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);

        verb = (VerbInfoDTO) getResourceCache().get(resourceCacheKey);

        //Cache hit
        if(verb != null){
            if(log.isDebugEnabled()){
                log.debug("Got Resource from cache for key: ".concat(resourceCacheKey));
            }
            //Set cache key in the message context so that it can be used by the subsequent handlers.
            synCtx.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
            return verb;
        }

        if(log.isDebugEnabled()){
            log.debug("Cache miss for Resource for key: ".concat(resourceCacheKey));
        }

        String apiCacheKey = APIUtil.getAPIInfoDTOCacheKey(apiContext, apiVersion);
        APIInfoDTO apiInfoDTO = null;
        apiInfoDTO = (APIInfoDTO) getResourceCache().get(apiCacheKey);

        //Cache miss
        if (apiInfoDTO == null) {
            if(log.isDebugEnabled()){
                log.debug("Could not find API object in cache for key: ".concat(apiCacheKey));
            }
            apiInfoDTO = doGetAPIInfo(apiContext, apiVersion);
            getResourceCache().put(apiCacheKey, apiInfoDTO);
        }
        if(apiInfoDTO.getResources()!=null){
            for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                if ((resourceString.trim()).equalsIgnoreCase(resourceInfoDTO.getUrlPattern().trim())) {
                    for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                        if (verbDTO.getHttpVerb().equals(httpMethod)) {
                            if(log.isDebugEnabled()){
                                log.debug("Putting resource object in cache with key: ".concat(resourceCacheKey));
                            }
                            //Store verb in cache
                            getResourceCache().put(resourceCacheKey, verbDTO);
                            //Set cache key in the message context so that it can be used by the subsequent handlers.
                            synCtx.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
                            verbDTO.setRequestKey(resourceCacheKey);
                            return verbDTO;
                        }
                    }
                }
        }
        }
        return null;
    }

    private APIInfoDTO doGetAPIInfo(String context, String apiVersion) throws APISecurityException{
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        
        ArrayList<URITemplate> uriTemplates = getAllURITemplates(context, apiVersion);

        apiInfoDTO.setApiName(context);
        apiInfoDTO.setContext(context);
        apiInfoDTO.setVersion(apiVersion);
        apiInfoDTO.setResources(new LinkedHashSet<ResourceInfoDTO>());

        ResourceInfoDTO resourceInfoDTO = null;
        VerbInfoDTO verbInfoDTO = null;
        int i = 0;
        for (URITemplate uriTemplate : uriTemplates) {
        	if (resourceInfoDTO != null && resourceInfoDTO.getUrlPattern().equalsIgnoreCase(uriTemplate.getUriTemplate())) {
                LinkedHashSet<VerbInfoDTO> verbs = (LinkedHashSet<VerbInfoDTO>) resourceInfoDTO.getHttpVerbs();
                verbInfoDTO = new VerbInfoDTO();
                verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
                verbInfoDTO.setAuthType(uriTemplate.getAuthType());
                verbInfoDTO.setThrottling(uriTemplate.getThrottlingTier());
                verbs.add(verbInfoDTO);
                resourceInfoDTO.setHttpVerbs(verbs);
                apiInfoDTO.getResources().add(resourceInfoDTO);
             } else {
            	 resourceInfoDTO = new ResourceInfoDTO();
                 resourceInfoDTO.setUrlPattern(uriTemplate.getUriTemplate());
                 verbInfoDTO = new VerbInfoDTO();
                 verbInfoDTO.setHttpVerb(uriTemplate.getHTTPVerb());
                 verbInfoDTO.setAuthType(uriTemplate.getAuthType());
                 verbInfoDTO.setThrottling(uriTemplate.getThrottlingTier());
                 LinkedHashSet<VerbInfoDTO> httpVerbs2 = new LinkedHashSet();
                 httpVerbs2.add(verbInfoDTO);
                 resourceInfoDTO.setHttpVerbs(httpVerbs2);
                 apiInfoDTO.getResources().add(resourceInfoDTO);
              }
            }
        return apiInfoDTO;
    }


    /**
     * @param context     API context of API
     * @param apiVersion  Version of API
     * @param requestPath Incoming request path
     * @param httpMethod  http method of request
     * @return verbInfoDTO which contains throttling tier for given resource and verb+resource key
     */
    public VerbInfoDTO getVerbInfoDTOFromAPIData(String context, String apiVersion, String requestPath, String httpMethod) 
    		throws APISecurityException {

        String cacheKey = context + ":" + apiVersion;
        APIInfoDTO apiInfoDTO = null;
        if (isGatewayAPIResourceValidationEnabled) {
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
            if (isGatewayAPIResourceValidationEnabled) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }
            //On a cache hit
            if (matchingVerb != null) {
                matchingVerb.setRequestKey(requestCacheKey);
                return matchingVerb;
            } else {
                if(apiInfoDTO.getResources()!=null){
                for (ResourceInfoDTO resourceInfoDTO : apiInfoDTO.getResources()) {
                    String urlPattern = resourceInfoDTO.getUrlPattern();

                    //If the request patch is '/', it can only be matched with a resource whose url-context is '/*'
                    if ("/*".equals(urlPattern)) {
                        for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                            if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                //Store verb in cache
                                getResourceCache().put(requestCacheKey, verbDTO);
                                verbDTO.setRequestKey(requestCacheKey);
                                return verbDTO;
                            }
                        }
                    }
                }
                }
            }
        }

        //Remove the ending '/' from request
        requestPath = RESTUtils.trimTrailingSlashes(requestPath);

        while (requestPath.length() > 1) {

            String requestCacheKey = context + "/" + apiVersion + requestPath + ":" + httpMethod;

            //Get decision from cache.
            VerbInfoDTO matchingVerb = null;
            if (isGatewayAPIResourceValidationEnabled) {
                matchingVerb = (VerbInfoDTO) getResourceCache().get(requestCacheKey);
            }

            //On a cache hit
            if (matchingVerb != null) {
                matchingVerb.setRequestKey(requestCacheKey);
                return matchingVerb;
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
                    urlPattern = RESTUtils.trimTrailingSlashes(urlPattern);

                    if (requestPath.endsWith(urlPattern)) {

                        for (VerbInfoDTO verbDTO : resourceInfoDTO.getHttpVerbs()) {
                            if (verbDTO.getHttpVerb().equals(httpMethod)) {
                                //Store verb in cache
                                getResourceCache().put(requestCacheKey, verbDTO);
                                verbDTO.setRequestKey(requestCacheKey);
                                return verbDTO;
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
        return null;
    }




    private ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion)
            throws APISecurityException {
        return dataStore.getAllURITemplates(context, apiVersion);
    }
}
