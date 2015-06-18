/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.jaggeryjs.scriptengine.exceptions.ScriptException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.doc.model.APIDefinition;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.doc.model.Operation;
import org.wso2.carbon.apimgt.api.doc.model.Parameter;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIPublisher;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.APISubscription;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.FileData;
import org.wso2.carbon.apimgt.api.model.Icon;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.Provider;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.clients.ApplicationManagementServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.client.ProviderKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.services.ServiceDataPublisherAdmin;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.ExceptionException;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfo;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.PermissionUpdateUtil;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.indexer.IndexerException;
import org.wso2.carbon.registry.indexing.solr.SolrClient;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

/**
 * This class contains the utility methods used by the implementations of APIManager, APIProvider
 * and APIConsumer interfaces.
 */
public final class APIUtil {

    private static final Log log = LogFactory.getLog(APIUtil.class);

    private static boolean isContextCacheInitialized = false;

    private static final String DESCRIPTION = "Allows [1] request(s) per minute.";

    private static Set<Integer> registryInitializedTenants = new HashSet<Integer>();
    private static GenericArtifactManager genericArtifactManager;

    private static ConfigurationContextService configContextService = null;

	private static String VERSION_PARAM="{version}";

    /**
     * This method is used to set config context service
     *
     * @param configContext
     */
	public static void setConfigContextService(ConfigurationContextService configContext) {
        APIUtil.configContextService = configContext;
    }

    /**
     * This method is used to get config context
     *
     * @return configContextService.getServerConfigContext()
     * @throws APIManagementException
     */
    public static ConfigurationContext getConfigContext() throws APIManagementException {
        if (configContextService == null) {
            handleException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    private static Pattern pathParamExtractorPattern=Pattern.compile("\\{.*?\\}");
    private static Pattern pathParamValidatorPattern=Pattern.compile("\\{uri\\.var\\.[\\w]+\\}");

    /**
     * This method used to get API from governance artifact
     *
     * @param artifact API artifact
     * @param registry Registry
     * @return API
     * @throws APIManagementException if failed to get API from artifact
     */
    public static API getAPI(GovernanceArtifact artifact, Registry registry)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            int apiId = ApiMgtDAO.getAPIID(apiIdentifier, null);

            if(apiId == -1){
                return null;
            }
            api = new API(apiIdentifier);
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            // BigDecimal bigDecimal = new BigDecimal(getAverageRating(apiId));
            //BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);
            api.setRating(getAverageRating(apiId));
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            // set url
            api.setUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_URL));
            api.setSandboxUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_SANDBOX_URL));
            api.setStatus(getApiStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {		
            	cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch(NumberFormatException e) {
            	//ignore
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setDestinationStatsEnabled(artifact.getAttribute(APIConstants.API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED));

            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

            Set<Tier> availableTier = new HashSet<Tier>();
            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            Map<String, Tier> definedTiers = getTiers(tenantId);
            if (tiers != null && !"".equals(tiers)) {
                String[] tierNames = tiers.split("\\|\\|");
                for (String tierName : tierNames) {
                    Tier definedTier = definedTiers.get(tierName);
                    if (definedTier != null) {
                        availableTier.add(definedTier);
                    } else {
                        log.warn("Unknown tier: " + tierName + " found on API: " + apiName);
                    }
                }
            }
            api.addAvailableTiers(availableTier);
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));


            Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
            List<String> uriTemplateNames = new ArrayList<String>();

            Set<Scope> scopes = ApiMgtDAO.getAPIScopes(api.getId());
            api.setScopes(scopes);

            HashMap<String, String> urlPatternsSet;
            urlPatternsSet = ApiMgtDAO.getURITemplatesPerAPIAsString(api.getId());

            HashMap<String, String> resourceScopes;
            resourceScopes = ApiMgtDAO.getResourceToScopeMapping(api.getId());

            Set<String> urlPatternsKeySet = urlPatternsSet.keySet();
            String resourceScopeKey;
            for (String urlPattern : urlPatternsKeySet) {
                URITemplate uriTemplate = new URITemplate();
                String[] urlPatternComponents = urlPattern.split("::");
                String uTemplate = (urlPatternComponents.length >= 1) ? urlPatternComponents[0] : null;
                String method = (urlPatternComponents.length >= 2) ? urlPatternComponents[1] : null;
                String authType = (urlPatternComponents.length >= 3) ? urlPatternComponents[2] : null;
                String throttlingTier = (urlPatternComponents.length >= 4) ? urlPatternComponents[3] : null;
                String mediationScript = (urlPatternComponents.length >= 5) ? urlPatternComponents[4] : null;
                uriTemplate.setHTTPVerb(method);
                uriTemplate.setAuthType(authType);
                uriTemplate.setThrottlingTier(throttlingTier);
                uriTemplate.setHttpVerbs(method);
                uriTemplate.setAuthTypes(authType);
                uriTemplate.setUriTemplate(uTemplate);
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
                uriTemplate.setThrottlingTiers(throttlingTier);
                uriTemplate.setMediationScript(mediationScript);
                resourceScopeKey = APIUtil.getResourceKey(api.getContext(), apiVersion, uTemplate, method);
                uriTemplate.setScopes(findScopeByKey(scopes, resourceScopes.get(resourceScopeKey)));
                //Checking for duplicate uri template names
                if (uriTemplateNames.contains(uTemplate)) {
                    for (URITemplate tmp : uriTemplates) {
                        if (uTemplate.equals(tmp.getUriTemplate())) {
                            tmp.setHttpVerbs(method);
                            tmp.setAuthTypes(authType);
                            tmp.setThrottlingTiers(throttlingTier);
                            resourceScopeKey = APIUtil.getResourceKey(api.getContext(), apiVersion, uTemplate, method);
                            tmp.setScopes(findScopeByKey(scopes, resourceScopes.get(resourceScopeKey)));
                            break;
                        }
                    }
                } else {
                    uriTemplates.add(uriTemplate);
                }
                uriTemplateNames.add(uTemplate);
            }
            api.setUriTemplates(uriTemplates);
            api.setAsDefaultVersion(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Provider";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * This Method is different from getAPI method, as this one returns
     * URLTemplates without aggregating duplicates. This is to be used for building synapse config.
     *
     * @param artifact
     * @param registry
     * @return API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static API getAPIForPublishing(GovernanceArtifact artifact, Registry registry)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            int apiId = ApiMgtDAO.getAPIID(apiIdentifier, null);

            if (apiId == -1) {
                return null;
            }

            api = new API(apiIdentifier);
            // set rating
            String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
            // BigDecimal bigDecimal = new BigDecimal(getAverageRating(apiId));
            //BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);
            api.setRating(getAverageRating(apiId));
            //set description
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            //set last access time
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            // set url
            api.setUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_URL));
            api.setSandboxUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_SANDBOX_URL));
            api.setStatus(getApiStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
            api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
            api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
            api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
            api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
            api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
            api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
            api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
                String strCacheTimeout = artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT);
                if (strCacheTimeout != null && !strCacheTimeout.isEmpty()) {
                    cacheTimeout = Integer.parseInt(strCacheTimeout);
                }
            } catch (NumberFormatException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error while retrieving cache timeout from the registry for " + apiIdentifier);
                }
                // ignore the exception and use default cache timeout value
            }

            api.setCacheTimeout(cacheTimeout);

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setDestinationStatsEnabled(artifact.getAttribute(APIConstants.API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED));

            String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);

            Set<Tier> availableTier = new HashSet<Tier>();
            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            Map<String, Tier> definedTiers = getTiers(tenantId);
            if (tiers != null && !"".equals(tiers)) {
                String[] tierNames = tiers.split("\\|\\|");
                for (String tierName : tierNames) {
                    Tier definedTier = definedTiers.get(tierName);
                    if (definedTier != null) {
                        availableTier.add(definedTier);
                    } else {
                        log.warn("Unknown tier: " + tierName + " found on API: " + apiName);
                    }
                }
            }
            api.addAvailableTiers(availableTier);
            // This contains the resolved context
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            // We set the context template here
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setLatest(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));


            Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
            List<String> uriTemplateNames = new ArrayList<String>();

            Set<Scope> scopes = ApiMgtDAO.getAPIScopes(api.getId());
            api.setScopes(scopes);

            HashMap<String, String> urlPatternsSet;
            urlPatternsSet = ApiMgtDAO.getURITemplatesPerAPIAsString(api.getId());
            HashMap<String, String> resourceScopes;
            resourceScopes = ApiMgtDAO.getResourceToScopeMapping(api.getId());

            Set<String> urlPatternsKeySet = urlPatternsSet.keySet();
            String resourceScopeKey;
            for (String urlPattern : urlPatternsKeySet) {
                URITemplate uriTemplate = new URITemplate();
                String[] urlPatternComponents = urlPattern.split("::");
                String uTemplate = (urlPatternComponents.length >= 1) ? urlPatternComponents[0] : null;
                String method = (urlPatternComponents.length >= 2) ? urlPatternComponents[1] : null;
                String authType = (urlPatternComponents.length >= 3) ? urlPatternComponents[2] : null;
                String throttlingTier = (urlPatternComponents.length >= 4) ? urlPatternComponents[3] : null;
                String mediationScript = (urlPatternComponents.length >= 5) ? urlPatternComponents[4] : null;
                uriTemplate.setHTTPVerb(method);
                uriTemplate.setAuthType(authType);
                uriTemplate.setThrottlingTier(throttlingTier);
                uriTemplate.setHttpVerbs(method);
                uriTemplate.setAuthTypes(authType);
                uriTemplate.setUriTemplate(uTemplate);
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());
                uriTemplate.setThrottlingTiers(throttlingTier);
                uriTemplate.setMediationScript(mediationScript);
                uriTemplate.setMediationScripts(method, mediationScript);
                resourceScopeKey = APIUtil.getResourceKey(api.getContext(), apiVersion, uTemplate, method);
                uriTemplate.setScopes(findScopeByKey(scopes, resourceScopes.get(resourceScopeKey)));
                //Checking for duplicate uri template names

                if (uriTemplateNames.contains(uTemplate)) {
                    for (URITemplate tmp : uriTemplates) {
                        if (uTemplate.equals(tmp.getUriTemplate())) {
                            tmp.setHttpVerbs(method);
                            tmp.setAuthTypes(authType);
                            tmp.setThrottlingTiers(throttlingTier);
                            tmp.setMediationScripts(method, mediationScript);
                            resourceScopeKey = APIUtil.getResourceKey(api.getContext(), apiVersion, uTemplate, method);
                            tmp.setScopes(findScopeByKey(scopes, resourceScopes.get(resourceScopeKey)));
                            break;
                        }
                    }
                } else {
                    uriTemplates.add(uriTemplate);
                }
                uriTemplateNames.add(uTemplate);
            }

            if (api.getImplementation().equalsIgnoreCase(APIConstants.IMPLEMENTATION_TYPE_INLINE)) {
                for (URITemplate template : uriTemplates) {
                    template.setMediationScript(template.getAggregatedMediationScript());
                }
            }

            api.setUriTemplates(uriTemplates);
            api.setAsDefaultVersion(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            Set<String> tags = new HashSet<String>();
            Tag[] tag = registry.getTags(artifactPath);
            for (Tag tag1 : tag) {
                tags.add(tag1.getTagName());
            }
            api.addTags(tags);
            api.setLastUpdated(registry.get(artifactPath).getLastModified());
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));

        } catch (GovernanceException e) {
            String msg = "Failed to get API for artifact ";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "Failed to get LastAccess time or Rating";
            throw new APIManagementException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Failed to get User Realm of API Provider";
            throw new APIManagementException(msg, e);
        }
        return api;
    }



    public static API getAPI(GovernanceArtifact artifact)
            throws APIManagementException {

        API api;
        try {
            String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
            String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
            String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, apiVersion);
            api = new API(apiIdentifier);
            int apiId = ApiMgtDAO.getAPIID(apiIdentifier, null);
            if (apiId == -1) {
                return null;
            }
            api.setRating(getAverageRating(apiId));
            api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
            api.setStatus(getApiStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)));
            api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
            api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
            api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
            api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
            api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
            api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
            api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
            api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
            api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
            api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
            api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));

            int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
            try {
            	cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
            } catch(NumberFormatException e) {
            	//ignore
            }
            api.setCacheTimeout(cacheTimeout);

            Set<Tier> availableTier = new HashSet<Tier>();
            String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
            if (tiers != null) {
                String[] tierNames = tiers.split("\\|\\|");
                for (String tierName : tierNames) {
                    Tier tier = new Tier(tierName);
                    availableTier.add(tier);

                }

                api.addAvailableTiers(availableTier);
            }

            api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
            api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
            api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

            api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

            api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
            api.setSubscriptionAvailableTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

            api.setDestinationStatsEnabled(artifact.getAttribute(APIConstants.API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED));
            api.setAsDefaultVersion(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));
            api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
            ArrayList<URITemplate> urlPatternsList;
            urlPatternsList = ApiMgtDAO.getAllURITemplates(api.getContext(), api.getId().getVersion());
            Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);

            for (URITemplate uriTemplate : uriTemplates) {
                uriTemplate.setResourceURI(api.getUrl());
                uriTemplate.setResourceSandboxURI(api.getSandboxUrl());

            }
            api.setUriTemplates(uriTemplates);
            String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
            api.setEnvironments(extractEnvironmentsForAPI(environments));
        } catch (GovernanceException e) {
            String msg = "Failed to get API from artifact ";
            throw new APIManagementException(msg, e);
        }
        return api;
    }

    /**
     * This method used to get Provider from provider artifact
     *
     * @param artifact provider artifact
     * @return Provider
     * @throws APIManagementException if failed to get Provider from provider artifact.
     */
    public static Provider getProvider(GenericArtifact artifact) throws APIManagementException {
        Provider provider;
        try {
            provider =
                    new Provider(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_NAME));
            provider.setDescription(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_DESCRIPTION));
            provider.setEmail(artifact.getAttribute(APIConstants.PROVIDER_OVERVIEW_EMAIL));

        } catch (GovernanceException e) {
            String msg = "Failed to get provider ";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return provider;
    }

    /**
     * Returns a list of scopes when passed the Provider Name and Scope Key
     * @param scopeKey
     * @param provider
     * @return
     * @throws APIManagementException
     */
    public static Set<Scope> getScopeByScopeKey(String scopeKey, String provider) throws APIManagementException {
        Set<Scope> scopeList = null;
        provider = APIUtil.replaceEmailDomain(provider);
        String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(provider));
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomainName);
            scopeList = ApiMgtDAO.getAPIScopesByScopeKey(scopeKey,tenantId);
        } catch (UserStoreException e) {
            handleException("Error while retrieving Scopes");
        }
        return scopeList;
    }

    /**
     * Create Governance artifact from given attributes
     *
     * @param artifact initial governance artifact
     * @param api      API object with the attributes value
     * @return GenericArtifact
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to create API
     */
    public static GenericArtifact createAPIArtifactContent(GenericArtifact artifact, API api)
            throws APIManagementException {
        try {
            String apiStatus = api.getStatus().getStatus();
            artifact.setAttribute(APIConstants.API_OVERVIEW_NAME, api.getId().getApiName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION, api.getId().getVersion());

            artifact.setAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION, String.valueOf(api.isDefaultVersion()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT, api.getContext());
            artifact.setAttribute(APIConstants.API_OVERVIEW_PROVIDER, api.getId().getProviderName());
            artifact.setAttribute(APIConstants.API_OVERVIEW_DESCRIPTION, api.getDescription());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_URL, api.getUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_SANDBOX_URL, api.getSandboxUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WSDL, api.getWsdlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_WADL, api.getWadlUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL, api.getThumbnailUrl());
            artifact.setAttribute(APIConstants.API_OVERVIEW_STATUS, apiStatus);
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER, api.getTechnicalOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL, api.getTechnicalOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER, api.getBusinessOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL, api.getBusinessOwnerEmail());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBILITY, api.getVisibility());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES, api.getVisibleRoles());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS, api.getVisibleTenants());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED,Boolean.toString(api.isEndpointSecured()));
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME, api.getEndpointUTUsername());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD, api.getEndpointUTPassword());
            artifact.setAttribute(APIConstants.API_OVERVIEW_TRANSPORTS, api.getTransports());
            artifact.setAttribute(APIConstants.API_OVERVIEW_INSEQUENCE, api.getInSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE, api.getOutSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE, api.getFaultSequence());
            artifact.setAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING, api.getResponseCache());
            artifact.setAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT, Integer.toString(api.getCacheTimeout()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL, api.getRedirectURL());
            artifact.setAttribute(APIConstants.API_OVERVIEW_OWNER, api.getApiOwner());
            artifact.setAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY, Boolean.toString(api.isAdvertiseOnly()));

            artifact.setAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG, api.getEndpointConfig());

            artifact.setAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY, api.getSubscriptionAvailability());
            artifact.setAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS, api.getSubscriptionAvailableTenants());

            artifact.setAttribute(APIConstants.API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED, api.getDestinationStatsEnabled());

			artifact.setAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION, api.getImplementation());

            // This is to support the pluggable version strategy.
            artifact.setAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE, api.getContextTemplate());
            artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context"); // TODO: check whether this is
            // correct

            String tiers = "";
            for (Tier tier : api.getAvailableTiers()) {
                tiers += tier.getName() + "||";
            }
            if (!"".equals(tiers)) {
                tiers = tiers.substring(0, tiers.length() - 2);
                artifact.setAttribute(APIConstants.API_OVERVIEW_TIER, tiers);
            }
            if (APIConstants.PUBLISHED.equals(apiStatus)) {
                artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true");
            }
            String[] keys = artifact.getAttributeKeys();
            for (String key : keys) {
                if (key.contains("URITemplate")) {
                    artifact.removeAttribute(key);
                }
            }

            Set<URITemplate> uriTemplateSet = api.getUriTemplates();
            int i = 0;
            for (URITemplate uriTemplate : uriTemplateSet) {
                artifact.addAttribute(APIConstants.API_URI_PATTERN + i,
                        uriTemplate.getUriTemplate());
                artifact.addAttribute(APIConstants.API_URI_HTTP_METHOD + i,
                        uriTemplate.getHTTPVerb());
                artifact.addAttribute(APIConstants.API_URI_AUTH_TYPE + i,
                        uriTemplate.getAuthType());
//                artifact.addAttribute(APIConstants.API_URI_MEDIATION_SCRIPT + i,
//                        uriTemplate.getMediationScript());
                i++;

            }
            artifact.setAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS, writeEnvironmentsToArtifact(api));

        } catch (GovernanceException e) {
            String msg = "Failed to create API for : " + api.getId().getApiName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact)
            throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));
            String visibilityAttr = artifact.getAttribute(APIConstants.DOC_VISIBILITY);
            Documentation.DocumentVisibility documentVisibility = Documentation.DocumentVisibility.API_LEVEL;
            if(visibilityAttr!=null){
            if (visibilityAttr.equals(Documentation.DocumentVisibility.API_LEVEL.name())) {
                documentVisibility= Documentation.DocumentVisibility.API_LEVEL;
            } else if (visibilityAttr.equals(Documentation.DocumentVisibility.PRIVATE.name())) {
                documentVisibility = Documentation.DocumentVisibility.PRIVATE;
            }else if (visibilityAttr.equals(Documentation.DocumentVisibility.OWNER_ONLY.name())) {
                documentVisibility = Documentation.DocumentVisibility.OWNER_ONLY;
            }
            }
            documentation.setVisibility(documentVisibility);

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (artifactAttribute.equals(Documentation.DocumentSourceType.URL.name())) {
                docSourceType = Documentation.DocumentSourceType.URL;
            } else if (artifactAttribute.equals(Documentation.DocumentSourceType.FILE.name())) {
                docSourceType = Documentation.DocumentSourceType.FILE;
            }

            documentation.setSourceType(docSourceType);
            if (artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE).equals("URL")) {
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            }

            if (docSourceType == Documentation.DocumentSourceType.FILE) {
                documentation.setFilePath(prependWebContextRoot(artifact.getAttribute(APIConstants.DOC_FILE_PATH)));
            }

            if(documentation.getType() == DocumentationType.OTHER){
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }

    /**
     * Create the Documentation from artifact
     *
     * @param artifact Documentation artifact
     * @return Documentation
     * @throws APIManagementException if failed to create Documentation from artifact
     */
    public static Documentation getDocumentation(GenericArtifact artifact,String docCreatorName)
            throws APIManagementException {

        Documentation documentation;

        try {
            DocumentationType type;
            String docType = artifact.getAttribute(APIConstants.DOC_TYPE);

            if (docType.equalsIgnoreCase(DocumentationType.HOWTO.getType())) {
                type = DocumentationType.HOWTO;
            } else if (docType.equalsIgnoreCase(DocumentationType.PUBLIC_FORUM.getType())) {
                type = DocumentationType.PUBLIC_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.SUPPORT_FORUM.getType())) {
                type = DocumentationType.SUPPORT_FORUM;
            } else if (docType.equalsIgnoreCase(DocumentationType.API_MESSAGE_FORMAT.getType())) {
                type = DocumentationType.API_MESSAGE_FORMAT;
            } else if (docType.equalsIgnoreCase(DocumentationType.SAMPLES.getType())) {
                type = DocumentationType.SAMPLES;
            } else {
                type = DocumentationType.OTHER;
            }
            documentation = new Documentation(type, artifact.getAttribute(APIConstants.DOC_NAME));
            documentation.setSummary(artifact.getAttribute(APIConstants.DOC_SUMMARY));

            Documentation.DocumentSourceType docSourceType = Documentation.DocumentSourceType.INLINE;
            String artifactAttribute = artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE);

            if (artifactAttribute.equals(Documentation.DocumentSourceType.URL.name())) {
                docSourceType = Documentation.DocumentSourceType.URL;
            } else if (artifactAttribute.equals(Documentation.DocumentSourceType.FILE.name())) {
                docSourceType = Documentation.DocumentSourceType.FILE;
            }

            documentation.setSourceType(docSourceType);
            if (artifact.getAttribute(APIConstants.DOC_SOURCE_TYPE).equals("URL")) {
                documentation.setSourceUrl(artifact.getAttribute(APIConstants.DOC_SOURCE_URL));
            }

            if (docSourceType == Documentation.DocumentSourceType.FILE) {
                String filePath=prependTenantPrefix(artifact.getAttribute(APIConstants.DOC_FILE_PATH),docCreatorName);
                documentation.setFilePath(prependWebContextRoot(filePath));
            }

            if(documentation.getType() == DocumentationType.OTHER){
                documentation.setOtherTypeName(artifact.getAttribute(APIConstants.DOC_OTHER_TYPE_NAME));
            }

        } catch (GovernanceException e) {
            throw new APIManagementException("Failed to get documentation from artifact", e);
        }
        return documentation;
    }

    public static APIStatus getApiStatus(String status) throws APIManagementException {
        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
                break;
            }
        }
        return apiStatus;

    }

    /**
     * Prepends the Tenant Prefix to a registry path. ex: /t/test1.com
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he Tenant domain prefix.
     */
    public static String prependTenantPrefix(String postfixUrl, String username) {
    	String tenantDomain = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(username));
    	if (!(tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))) {
    		String tenantPrefix = "/t/";
            if (tenantDomain != null) {

                postfixUrl = tenantPrefix + tenantDomain + postfixUrl;
            }
        }

        return postfixUrl;
    }

    /**
     * Prepends the webcontextroot to a registry path.
     * @param postfixUrl path to be prepended.
     * @return Path prepended with he WebContext root.
     */
    public static String prependWebContextRoot(String postfixUrl) {
        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (webContext != null && !webContext.equals("/")) {

            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }

    /**
     * Utility method for creating storage path for an icon.
     *
     * @param identifier APIIdentifier
     * @return Icon storage path.
     */
    public static String getIconPath(APIIdentifier identifier) {
        String artifactPath = APIConstants.API_IMAGE_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR + identifier.getVersion();
        return artifactPath + RegistryConstants.PATH_SEPARATOR + APIConstants.API_ICON_IMAGE;
    }

    /**
     * Utility method to generate the path for a file.
     *
     * @param identifier APIIdentifier
     * @return Generated path.
     * @fileName File name.
     */
    public static String getDocumentationFilePath(APIIdentifier identifier, String fileName) {
        String contentPath = APIUtil.getAPIDocPath(identifier) + APIConstants.DOCUMENT_FILE_DIR +
                RegistryConstants.PATH_SEPARATOR + fileName;
        return contentPath;
    }

    //remove getSwagger12DefinitionFilePath once getSwagger20DefinitionFilePath operates
    public static String getSwagger12DefinitionFilePath(String apiName, String apiVersion, String apiProvider) {
    	String resourcePath = APIConstants.API_DOC_LOCATION + RegistryConstants.PATH_SEPARATOR +
    			apiName +"-"  + apiVersion + "-" + apiProvider + RegistryConstants.PATH_SEPARATOR + APIConstants.API_DOC_1_2_LOCATION;

    	return resourcePath;
    }

    public static String getSwagger20DefinitionFilePath(String apiName, String apiVersion, String apiProvider) {
        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR + apiProvider + RegistryConstants.PATH_SEPARATOR +
                apiName + RegistryConstants.PATH_SEPARATOR + apiVersion + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Utility method to get api path from APIIdentifier
     *
     * @param identifier APIIdentifier
     * @return API path
     */
    public static String getAPIPath(APIIdentifier identifier) {
        return APIConstants.API_ROOT_LOCATION + RegistryConstants.PATH_SEPARATOR +
                identifier.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getApiName() + RegistryConstants.PATH_SEPARATOR +
                identifier.getVersion() + APIConstants.API_RESOURCE_NAME;
    }

    /**
     * Utility method to get API provider path
     *
     * @param identifier APIIdentifier
     * @return API provider path
     */
    public static String getAPIProviderPath(APIIdentifier identifier) {
        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR
                + identifier.getProviderName();
    }

    /**
     * Utility method to get documentation path
     *
     * @param apiId APIIdentifier
     * @return Doc path
     */
    public static String getAPIDocPath(APIIdentifier apiId) {
        return APIConstants.API_LOCATION + RegistryConstants.PATH_SEPARATOR +
                apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                apiId.getApiName() + RegistryConstants.PATH_SEPARATOR +
                apiId.getVersion() + RegistryConstants.PATH_SEPARATOR +
                APIConstants.DOC_DIR + RegistryConstants.PATH_SEPARATOR;
    }

    /**
     * Utility method to get documentation content file path
     *
     * @param apiId APIIdentifier
     * @param documentationName String
     * @return Doc content path
     */
    public static String getAPIDocContentPath(APIIdentifier apiId, String documentationName) {
        return getAPIDocPath(apiId) + APIConstants.INLINE_DOCUMENT_CONTENT_DIR +
        		RegistryConstants.PATH_SEPARATOR + documentationName;
    }

    /**
     * This utility method used to create documentation artifact content
     *
     * @param artifact      GovernanceArtifact
     * @param apiId         APIIdentifier
     * @param documentation Documentation
     * @return GenericArtifact
     * @throws APIManagementException if failed to get GovernanceArtifact from Documentation
     */
    public static GenericArtifact createDocArtifactContent(GenericArtifact artifact,
                                                           APIIdentifier apiId,
                                                           Documentation documentation)
            throws APIManagementException {
        try {
            artifact.setAttribute(APIConstants.DOC_NAME, documentation.getName());
            artifact.setAttribute(APIConstants.DOC_SUMMARY, documentation.getSummary());
            artifact.setAttribute(APIConstants.DOC_TYPE, documentation.getType().getType());
            artifact.setAttribute(APIConstants.DOC_VISIBILITY, documentation.getVisibility().name());

            Documentation.DocumentSourceType sourceType = documentation.getSourceType();

            switch (sourceType) {
                case INLINE:
                    sourceType = Documentation.DocumentSourceType.INLINE;
                    break;
                case URL:
                    sourceType = Documentation.DocumentSourceType.URL;
                    break;
                case FILE: {
                    sourceType = Documentation.DocumentSourceType.FILE;
                    setFilePermission(documentation.getFilePath());
                }
                break;
            }
            artifact.setAttribute(APIConstants.DOC_SOURCE_TYPE, sourceType.name());
            artifact.setAttribute(APIConstants.DOC_SOURCE_URL, documentation.getSourceUrl());
            artifact.setAttribute(APIConstants.DOC_FILE_PATH, documentation.getFilePath());
            artifact.setAttribute(APIConstants.DOC_OTHER_TYPE_NAME,documentation.getOtherTypeName());
            String basePath = apiId.getProviderName() + RegistryConstants.PATH_SEPARATOR +
                    apiId.getApiName() + RegistryConstants.PATH_SEPARATOR +
                    apiId.getVersion();
            artifact.setAttribute(APIConstants.DOC_API_BASE_PATH, basePath);
        } catch (GovernanceException e) {
            String msg = "Filed to create doc artifact content from :" + documentation.getName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifact;
    }

    /**
     * this method used to initialized the ArtifactManager
     *
     * @param registry Registry
     * @param key      , key name of the key
     * @return GenericArtifactManager
     * @throws APIManagementException if failed to initialized GenericArtifactManager
     */
    public static GenericArtifactManager getArtifactManager(Registry registry, String key)
            throws APIManagementException {
        GenericArtifactManager artifactManager = null;

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            if(GovernanceUtils.findGovernanceArtifactConfiguration(key, registry)!=null){
            artifactManager = new GenericArtifactManager(registry, key);
            }
        } catch (RegistryException e) {
            String msg = "Failed to initialize GenericArtifactManager";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return artifactManager;
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    public static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static SubscriberKeyMgtClient getKeyManagementClient() throws APIManagementException {

        KeyManagerConfiguration configuration = KeyManagerHolder.getKeyManagerInstance().getKeyManagerConfiguration();
        String serverURL = configuration.getParameter(APIConstants.AUTHSERVER_URL);
        String username = configuration.getParameter(APIConstants.KEY_MANAGER_USERNAME);
        String password = configuration.getParameter(APIConstants.KEY_MANAGER_PASSWORD);

        if (serverURL == null) {
            handleException("API key manager URL unspecified");
        }

        if (username == null || password == null) {
            handleException("Authentication credentials for API key manager unspecified");
        }

        try {
            return new SubscriberKeyMgtClient(serverURL, username, password);
        } catch (Exception e) {
            handleException("Error while initializing the subscriber key management client", e);
            return null;
        }
    }

	/**
	 * Used to get instance of ProviderKeyMgtClient
	 * @return ProviderKeyMgtClient
	 * @throws APIManagementException
	 */
	public static ProviderKeyMgtClient getProviderClient() throws APIManagementException {
		APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
																.getAPIManagerConfiguration();
		String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
		if (url == null) {
			handleException("API key manager URL unspecified");
		}

		String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
		String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
		if (username == null || password == null) {
			handleException("Authentication credentials for API Provider manager unspecified");
		}

		try {
			return new ProviderKeyMgtClient(url, username, password);
		} catch (APIManagementException e) {
			handleException("Error while initializing the provider  management client", e);
			return null;
		}
	}

    public static OAuthAdminClient getOauthAdminClient() throws APIManagementException {

        try {
            return new OAuthAdminClient();
        } catch (Exception e) {
            handleException("Error while initializing the OAuth admin client", e);
            return null;
        }
    }


    public static ApplicationManagementServiceClient getApplicationManagementServiceClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        try {
            return new ApplicationManagementServiceClient();
        } catch (Exception e) {
            handleException("Error while initializing the Application Management Service client", e);
            return null;
        }
    }


    /**
     * Crate an WSDL from given wsdl url. Reset the endpoint details to gateway node
     ** 
     * @param registry - Governance Registry space to save the WSDL
     * @param api      -API instance
     * @return Path of the created resource
     * @throws APIManagementException If an error occurs while adding the WSDL
     */

    public static String createWSDL(Registry registry, API api) throws RegistryException, APIManagementException {

    	try {
    		String wsdlResourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + api.getId().getProviderName() +
                    "--" + api.getId().getApiName() + api.getId().getVersion()+".wsdl";
			String absoluteWSDLResourcePath = RegistryUtils.getAbsolutePath(
                    RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)) +
                    wsdlResourcePath;

			APIMWSDLReader wsdlreader = new APIMWSDLReader(api.getWsdlUrl());
            OMElement wsdlContentEle = null;
            String wsdRegistryPath = null;

            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (tenantDomain.equalsIgnoreCase(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                wsdRegistryPath = RegistryConstants.PATH_SEPARATOR + "registry"
                                  + RegistryConstants.PATH_SEPARATOR + "resource"
                                  + absoluteWSDLResourcePath;
            } else {
                wsdRegistryPath = "/t/" + tenantDomain + RegistryConstants.PATH_SEPARATOR + "registry"
                                  + RegistryConstants.PATH_SEPARATOR + "resource"
                                  + absoluteWSDLResourcePath;
            }

            Resource wsdlResource = registry.newResource();
            if (!api.getWsdlUrl().matches(wsdRegistryPath)) {
                if (isWSDL2Document(api.getWsdlUrl())) {
                    wsdlContentEle = wsdlreader.readAndCleanWsdl2(api);
                    wsdlResource.setContent(wsdlContentEle.toString());
                } else {
                    wsdlContentEle = wsdlreader.readAndCleanWsdl(api);
                    wsdlResource.setContent(wsdlContentEle.toString());
                }

                registry.put(wsdlResourcePath, wsdlResource);
                //set the anonymous role for wsld resource to avoid basicauth security.
                String visibleRoles[] = null;
                if (api.getVisibleRoles() != null) {
                    visibleRoles = api.getVisibleRoles().split(",");
                }
                setResourcePermissions(api.getId().getProviderName(), api.getVisibility(), visibleRoles, wsdlResourcePath);
            }

			//set the wsdl resource permlink as the wsdlURL.
			api.setWsdlUrl(getRegistryResourceHTTPPermlink(absoluteWSDLResourcePath));

            return wsdlResourcePath;

        } catch (RegistryException e) {
            String msg = "Failed to add WSDL " + api.getWsdlUrl() + " to the registry";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } catch (APIManagementException e) {
	        String msg = "Failed to process the WSDL : " + api.getWsdlUrl() ;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Given a URL, this method checks if the underlying document is a WSDL2
     * @param url
     * @return
     * @throws Exception
     */
    private static boolean isWSDL2Document(String url) throws APIManagementException{
        URL wsdl = null;
        boolean isWsdl2 = false;
        try {
            wsdl = new URL(url);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Malformed URL encountered", e);
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(wsdl.openStream()));

        String inputLine;
        StringBuilder urlContent = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
            urlContent.append(inputLine);
            isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
        }
        in.close();
        if (isWsdl2) {
            WSDLReader wsdlReader20 = null;
            try {
                wsdlReader20 = WSDLFactory.newInstance().newWSDLReader();
                wsdlReader20.readWSDL(url);
            } catch (WSDLException e) {
                throw new APIManagementException("Error while reading WSDL Document from " + url, e);
            }
        }
        } catch (IOException e) {
            throw new APIManagementException("Error Reading Input from Stream from " + url, e);
        }
        return isWsdl2;
    }

    /**
     * Read the GateWay Endpoint from the APIConfiguration. If multiple Gateway
     * environments defined,
     * take only the production node's Endpoint.
     * Else, pick what is available as the gateway node.
     *
     * @return {@link String} - Gateway URL
     */

    public static String getGatewayendpoint(String transports) {

        String gatewayURLs = null;
        String gatewayURL = null;

        Map<String, Environment> gatewayEnvironments = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService()
                .getAPIManagerConfiguration()
                .getApiGatewayEnvironments();
        if (gatewayEnvironments.size() > 1) {
            for (Environment environment : gatewayEnvironments.values()) {
                if (APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(environment.getType())) {
                    gatewayURLs = environment.getApiGatewayEndpoint(); // This might have http,https
                    // endpoints
                    gatewayURL = APIUtil.extractHTTPSEndpoint(gatewayURLs, transports);
                    break;
                }
            }
        } else {
            gatewayURLs = ((Environment) gatewayEnvironments.values().toArray()[0]).getApiGatewayEndpoint();
            gatewayURL = extractHTTPSEndpoint(gatewayURLs, transports);
        }

        return gatewayURL;
    }

    /**
     * Gateway endpoint  has HTTP and HTTPS endpoints.
     * If both are defined pick HTTPS only. Else, pick whatever available.
     * eg: <GatewayEndpoint>http://${carbon.local.ip}:${http.nio.port},
     * 		https://${carbon.local.ip}:${https.nio.port}</GatewayEndpoint>
     *
     * @param gatewayURLs - String contains comma separated gateway urls.
     * @return {@link String} - Returns HTTPS gateway endpoint
     */

    private static String extractHTTPSEndpoint(String gatewayURLs, String transports) {
        String gatewayURL = null;
        String gatewayHTTPURL = null;
        String gatewayHTTPSURL = null;
        boolean httpsEnabled = false;
        String[] gatewayURLsArray = gatewayURLs.split(",");
        String[] transportsArray = transports.split(",");
        for (int j = 0; j < transportsArray.length; j++) {
            if (transportsArray[j].toString().startsWith("https")) {
                httpsEnabled = true;
            }
        }
        if (gatewayURLsArray.length > 1) {
            for (int j = 0; j < gatewayURLsArray.length; j++) {
                if (gatewayURLsArray[j].toString().startsWith("https:")) {
                    gatewayHTTPSURL = gatewayURLsArray[j].toString();
                }else {
                	gatewayHTTPURL = gatewayURLsArray[j].toString();
                }
            }
            if (httpsEnabled) {
                gatewayURL = gatewayHTTPSURL;
            } else {
                gatewayURL = gatewayHTTPURL;
            }
        } else {
            gatewayURL = gatewayURLs;
        }
        return gatewayURL;
    }

    /**
     * Create an Endpoint
     *
     * @param endpointUrl Endpoint url
     * @param registry    Registry space to save the endpoint
     * @return Path of the created resource
     * @throws APIManagementException If an error occurs while adding the endpoint
     */
    public static String createEndpoint(String endpointUrl, Registry registry) throws APIManagementException {
        try {
            EndpointManager endpointManager = new EndpointManager(registry);
            Endpoint endpoint = endpointManager.newEndpoint(endpointUrl);
            endpointManager.addEndpoint(endpoint);
            return GovernanceUtils.getArtifactPath(registry, endpoint.getId());
        } catch (RegistryException e) {
            String msg = "Failed to import endpoint " + endpointUrl + " to registry ";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
    }

    /**
     * Returns a map of API availability tiers as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getTiers() throws APIManagementException {
        Map<String, Tier> tiers = new TreeMap<String, Tier>();
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TIER_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement assertion = element.getFirstChildWithName(APIConstants.ASSERTION_ELEMENT);
                Iterator policies = assertion.getChildrenWithName(APIConstants.POLICY_ELEMENT);

                while (policies.hasNext()) {
                    OMElement policy = (OMElement) policies.next();
                    OMElement id = policy.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT);
                    String displayName=null;
                    if(id.getAttribute(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT)!=null){
                    displayName=id.getAttributeValue(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
                    }
                    if(displayName==null){
                    displayName=id.getText();
                    }
                    Tier tier = new Tier(id.getText());
                    tier.setPolicyContent(policy.toString().getBytes());
                    tier.setDisplayName(displayName);
                    // String desc = resource.getProperty(APIConstants.TIER_DESCRIPTION_PREFIX + id.getText());

                    String desc;
                    try {
                        long requestPerMin = APIDescriptionGenUtil.getAllowedCountPerMinute(policy);
                        tier.setRequestsPerMin(requestPerMin);
                        if(requestPerMin >= 1){
                            desc = DESCRIPTION.replaceAll("\\[1\\]", Long.toString(requestPerMin));
                        }
                        else{
                            desc = DESCRIPTION;
                        }
                    } catch (APIManagementException ex) {
                        desc = APIConstants.TIER_DESC_NOT_AVAILABLE;
                    }
                    Map<String,Object> tierAttributes=APIDescriptionGenUtil.getTierAttributes(policy);
                    if(tierAttributes!=null && tierAttributes.size()!=0){
                    tier.setTierAttributes(APIDescriptionGenUtil.getTierAttributes(policy));
                    }
                    tier.setDescription(desc);
                    if (!tier.getName().equalsIgnoreCase("Unauthenticated")) {
                        tiers.put(tier.getName(), tier);
                    }
                }
            }

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            if (Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENABLE_UNLIMITED_TIER))) {
                Tier tier = new Tier(APIConstants.UNLIMITED_TIER);
                tier.setDescription(APIConstants.UNLIMITED_TIER_DESC);
                tier.setDisplayName(APIConstants.UNLIMITED_TIER);
                tier.setRequestsPerMin(Long.MAX_VALUE);
                tiers.put(tier.getName(), tier);
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving API tiers from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the API tier policy resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return tiers;
    }

    /**
     * Sorts the list of tiers according to the number of requests allowed per minute in each tier in descending order.
     * @param tiers - The list of tiers to be sorted
     * @return - The sorted list.
     */
    public static List<Tier> sortTiers(Set<Tier> tiers){
        List<Tier> tierList = new ArrayList<Tier>();
        tierList.addAll(tiers);
        Collections.sort(tierList);
        return tierList;
    }

    /**
     * Returns a set of External API Stores as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Set<APIStore> getExternalStores(int tenantId) throws APIManagementException {
        // First checking if ExternalStores are defined in api-manager.xml
        Set<APIStore> externalAPIStores = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getExternalAPIStores();
        // If defined, return Store Config provided there.
        if (externalAPIStores != null && !externalAPIStores.isEmpty()) {
            return externalAPIStores;
        }
        // Else Read the config from Tenant's Registry.
        externalAPIStores = new HashSet<APIStore>();
        try {
    		UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                Iterator apistoreIterator = element.getChildrenWithLocalName("ExternalAPIStore");

                while(apistoreIterator.hasNext()){
                    APIStore store=new APIStore();
                    OMElement storeElem = (OMElement)apistoreIterator.next();
                    String type=storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_TYPE));
                    String className =
                            storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_CLASS_NAME));
                    store.setPublisher((APIPublisher) Class.forName(className).newInstance());
                    store.setType(type); //Set Store type [eg:wso2]
                    String name=storeElem.getAttributeValue(new QName(APIConstants.EXTERNAL_API_STORE_ID));
                    if (name == null) {
                        try {
                            throw new APIManagementException("The ExternalAPIStore name attribute is not defined in api-manager.xml.");
                        } catch (APIManagementException e) {
                            //ignore
                        }
                    }
                    store.setName(name); //Set store name
                    OMElement configDisplayName = storeElem.getFirstChildWithName(new QName(APIConstants.EXTERNAL_API_STORE_DISPLAY_NAME));
                    String displayName = (configDisplayName != null) ? replaceSystemProperty(
                            configDisplayName.getText()) : name;
                    store.setDisplayName(displayName);//Set store display name
                    store.setEndpoint(replaceSystemProperty(
                            storeElem.getFirstChildWithName(new QName(
                                    APIConstants.EXTERNAL_API_STORE_ENDPOINT)).getText())); //Set store endpoint,which is used to publish APIs
                    store.setPublished(false);
                    if (APIConstants.WSO2_API_STORE_TYPE.equals(type)) {
                        OMElement password = storeElem.getFirstChildWithName(new QName(
                                APIConstants.EXTERNAL_API_STORE_PASSWORD));
                        if (password != null) {
                            String key = APIConstants.EXTERNAL_API_STORES + "." + APIConstants.EXTERNAL_API_STORE + "." + APIConstants.EXTERNAL_API_STORE_PASSWORD + '_' + name;//Set store login password [optional]
                            String value = password.getText();

                    store.setPassword(replaceSystemProperty(value));
                    store.setUsername(replaceSystemProperty(
                            storeElem.getFirstChildWithName(new QName(
                                    APIConstants.EXTERNAL_API_STORE_USERNAME)).getText())); //Set store login username [optional]
                    }else{
                        try {
                            throw new APIManagementException("The user-credentials of API Publisher is not defined in the <ExternalAPIStore> config of api-manager.xml.");
                        } catch (APIManagementException e) {
                            //ignore
                        }
                    }
                    }
                    externalAPIStores.add(store);
                }

            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the External Stores Configuration resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ClassNotFoundException e) {
            log.error("Requested APIPublisher Class couldn't found", e);
        } catch (InstantiationException e) {
            log.error("Requested APIPublisher Class couldn't load", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return externalAPIStores;
    }


    /**
     * Returns the External API Store Configuration with the given Store Name
     * @param apiStoreName
     * @return
     * @throws APIManagementException
     */
    public static APIStore getExternalAPIStore(String apiStoreName, int tenantId) throws APIManagementException {
    	Set<APIStore> externalAPIStoresConfig = APIUtil.getExternalStores(tenantId);
        APIStore apiStore = null;
        for (APIStore apiStoreConfig : externalAPIStoresConfig) {
            if (apiStoreConfig.getName().equals(apiStoreName)) {
            	apiStore = apiStoreConfig;
            }
        }
        return apiStore;
    }

    /**
     * Returns a map of API availability tiers of the tenant as defined in the underlying governance
     * registry.
     *
     * @return a Map of tier names and Tier objects - possibly empty
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, Tier> getTiers(int tenantId) throws APIManagementException {
        Map<String, Tier> tiers = new TreeMap<String, Tier>();
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TIER_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement assertion = element.getFirstChildWithName(APIConstants.ASSERTION_ELEMENT);
                Iterator policies = assertion.getChildrenWithName(APIConstants.POLICY_ELEMENT);
                while (policies.hasNext()) {
                    OMElement policy = (OMElement) policies.next();
                    OMElement id = policy.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT);
                    String displayName=null;
                    if(id.getAttribute(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT)!=null){
                    displayName=id.getAttributeValue(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
                    }
                    if(displayName==null){
                    displayName=id.getText();
                    }
                    Tier tier = new Tier(id.getText());
                    tier.setPolicyContent(policy.toString().getBytes());
                    tier.setDisplayName(displayName);
                    // String desc = resource.getProperty(APIConstants.TIER_DESCRIPTION_PREFIX + id.getText());
                    String desc;
                    try {
                        long requestPerMin = APIDescriptionGenUtil.getAllowedCountPerMinute(policy);
                        tier.setRequestsPerMin(requestPerMin);

                        if(requestPerMin >= 1){
                            desc = DESCRIPTION.replaceAll("\\[1\\]", Long.toString(requestPerMin));
                        }
                        else{
                            desc = DESCRIPTION;
                        }
                    } catch (APIManagementException ex) {
                        desc = APIConstants.TIER_DESC_NOT_AVAILABLE;
                    }
                    Map<String,Object> tierAttributes=APIDescriptionGenUtil.getTierAttributes(policy);
                    if(tierAttributes!=null && tierAttributes.size()!=0){
                    tier.setTierAttributes(APIDescriptionGenUtil.getTierAttributes(policy));
                    }
                    tier.setDescription(desc);
                    if (!tier.getName().equalsIgnoreCase("Unauthenticated")) {
                        tiers.put(tier.getName(), tier);
                    }
                }
            }

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();
            if (Boolean.parseBoolean(config.getFirstProperty(APIConstants.ENABLE_UNLIMITED_TIER))) {
                Tier tier = new Tier(APIConstants.UNLIMITED_TIER);
                tier.setDescription(APIConstants.UNLIMITED_TIER_DESC);
                tier.setDisplayName(APIConstants.UNLIMITED_TIER);
                tier.setRequestsPerMin(Long.MAX_VALUE);
                tiers.put(tier.getName(), tier);
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving API tiers from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the API tier policy resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return tiers;
    }

    /**
     * Returns the tier display name for a particular tier
     *
     * @return the relevant tier display name
     * @throws APIManagementException if an error occurs when loading tiers from the registry
     */
    public static String getTierDisplayName(int tenantId,String tierName) throws APIManagementException {
        String displayName = null;
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TIER_LOCATION);
                String content = new String((byte[]) resource.getContent());
                OMElement element = AXIOMUtil.stringToOM(content);
                OMElement assertion = element.getFirstChildWithName(APIConstants.ASSERTION_ELEMENT);
                Iterator policies = assertion.getChildrenWithName(APIConstants.POLICY_ELEMENT);

                while (policies.hasNext()) {
                    OMElement policy = (OMElement) policies.next();
                    OMElement id = policy.getFirstChildWithName(APIConstants.THROTTLE_ID_ELEMENT);
                    if(id.getText().equals(tierName)) {
                    	if(id.getAttribute(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT) != null) {
                    		displayName = id.getAttributeValue(APIConstants.THROTTLE_ID_DISPLAY_NAME_ELEMENT);
                    	} else if(displayName==null) {
                            displayName = id.getText();
                        }
                    } else if(APIConstants.UNLIMITED_TIER.equals(tierName)){
                    	displayName=APIConstants.UNLIMITED_TIER;
                    }
                }

        } }catch (RegistryException e) {
            String msg = "Error while retrieving API tiers from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Malformed XML found in the API tier policy resource";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return displayName;
    }

    /**
     * Checks whether the specified user has the specified permission.
     *
     * @param username   A username
     * @param permission A valid Carbon permission
     * @throws APIManagementException If the user does not have the specified permission or if an error occurs
     */
    public static void checkPermission(String username, String permission)
            throws APIManagementException {
        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                                             " the anonymous user");
        }

        if (isPermissionCheckDisabled()) {
            log.debug("Permission verification is disabled by APIStore configuration");
            return;
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        boolean authorized;
        try {
            if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                AuthorizationManager manager = ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(tenantId).
                        getAuthorizationManager();
                authorized = manager.isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username), permission,
                                                      CarbonConstants.UI_PERMISSION_ACTION);
            } else {
                RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
                authorized = authorizationManager.isUserAuthorized(username, permission);
            }
            if (!authorized) {
                throw new APIManagementException("User '" + username + "' does not have the " +
                                                 "required permission: " + permission);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while checking the user:"+username+ " authorized or not",e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Checks whether the disablePermissionCheck parameter enabled
     *
     * @return boolean
     */
    public static boolean isPermissionCheckDisabled() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String disablePermissionCheck = config.getFirstProperty(APIConstants.API_STORE_DISABLE_PERMISSION_CHECK);
        if (disablePermissionCheck == null) {
            return false;
        }

        return Boolean.parseBoolean(disablePermissionCheck);
    }

    /**
     * Checks whether the specified user has the specified permission without throwing
     * any exceptions.
     *
     * @param username   A username
     * @param permission A valid Carbon permission
     * @return true if the user has the specified permission and false otherwise
     */
    public static boolean checkPermissionQuietly(String username, String permission) {
        try {
            checkPermission(username, permission);
            return true;
        } catch (APIManagementException e) {
            return false;
        }
    }

    /**
     * Gets the information of the logged in User.
     *
     * @param cookie Cookie of the previously logged in session.
     * @param serviceUrl Url of the authentication service.
     * @return LoggedUserInfo object containing details of the logged in user.
     */
    public static LoggedUserInfo getLoggedInUserInfo(String cookie,String serviceUrl) throws RemoteException, ExceptionException {
        LoggedUserInfoAdminStub stub = new LoggedUserInfoAdminStub(null,
                serviceUrl + "LoggedUserInfoAdmin");
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
        LoggedUserInfo userInfo = stub.getUserInfo();
        return userInfo;
    }

    /**
     * Retrieves the role list of a user
     *
     * @param username   A username
     * @throws APIManagementException If an error occurs
     */
    public static String[] getListOfRoles(String username) throws APIManagementException {
        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                    " the anonymous user");
        }

        RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
        return authorizationManager.getRolesOfUser(username);
    }

    /**
     * Retrieves the list of user roles without throwing any exceptions.
     *
     * @param username   A username
     * @return the list of roles to which the user belongs to.
     */
    public static String[] getListOfRolesQuietly(String username) {
        try {
            return getListOfRoles(username);
        } catch (APIManagementException e) {
            return new String[0];
        }
    }

    /**
     * Sets permission for uploaded file resource.
     *
     * @param filePath Registry path for the uploaded file
     * @throws APIManagementException
     */

    private static void setFilePermission(String filePath) throws APIManagementException {
        try {
            filePath = filePath.replaceFirst("/registry/resource/", "");
            AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
                    getAuthorizationManager();
            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    filePath, ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        filePath, ActionConstants.GET);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while setting up permissions for file location", e);
        }
    }

      /**
        * This method used to get API from governance artifact specific to copyAPI
        *
        * @param artifact API artifact
        * @param registry Registry
        * @return API
        * @throws APIManagementException if failed to get API from artifact
        */
       public static API getAPI(GovernanceArtifact artifact, Registry registry,APIIdentifier oldId, String oldContext)
               throws APIManagementException {

           API api;
           try {
               String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
               String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
               String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
               api = new API(new APIIdentifier(providerName, apiName, apiVersion));
               // set rating
               String artifactPath = GovernanceUtils.getArtifactPath(registry, artifact.getId());
               BigDecimal bigDecimal = new BigDecimal(registry.getAverageRating(artifactPath));
               BigDecimal res = bigDecimal.setScale(1, RoundingMode.HALF_UP);
               api.setRating(res.floatValue());
               //set description
               api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
               //set last access time
               api.setLastUpdated(registry.get(artifactPath).getLastModified());
               // set url
               api.setUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_URL));
               api.setSandboxUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_SANDBOX_URL));
               api.setStatus(getApiStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)));
               api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
               api.setWsdlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WSDL));
               api.setWadlUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_WADL));
               api.setTechnicalOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER));
               api.setTechnicalOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_TEC_OWNER_EMAIL));
               api.setBusinessOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER));
               api.setBusinessOwnerEmail(artifact.getAttribute(APIConstants.API_OVERVIEW_BUSS_OWNER_EMAIL));
               api.setEndpointSecured(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_SECURED)));
               api.setEndpointUTUsername(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_USERNAME));
               api.setEndpointUTPassword(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_PASSWORD));
               api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));

               api.setEndpointConfig(artifact.getAttribute(APIConstants.API_OVERVIEW_ENDPOINT_CONFIG));

               api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
               api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
               api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));

               api.setSubscriptionAvailability(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY));
               api.setSubscriptionAvailableTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS));

               api.setResponseCache(artifact.getAttribute(APIConstants.API_OVERVIEW_RESPONSE_CACHING));
               api.setImplementation(artifact.getAttribute(APIConstants.PROTOTYPE_OVERVIEW_IMPLEMENTATION));
               api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
               int cacheTimeout = APIConstants.API_RESPONSE_CACHE_TIMEOUT;
               try {
               	cacheTimeout = Integer.parseInt(artifact.getAttribute(APIConstants.API_OVERVIEW_CACHE_TIMEOUT));
               } catch(NumberFormatException e) {
               	//ignore
               }

               api.setDestinationStatsEnabled(artifact.getAttribute(APIConstants.API_OVERVIEW_DESTINATION_BASED_STATS_ENABLED));

               String tenantDomainName = MultitenantUtils.getTenantDomain(replaceEmailDomainBack(providerName));
               int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                .getTenantId(tenantDomainName);

               Set<Tier> availableTier = new HashSet<Tier>();
               String tiers = artifact.getAttribute(APIConstants.API_OVERVIEW_TIER);
               Map<String, Tier> definedTiers = getTiers(tenantId);
               if (tiers != null && !"".equals(tiers)) {
                   String[] tierNames = tiers.split("\\|\\|");
                   for (String tierName : tierNames) {
                       Tier definedTier = definedTiers.get(tierName);
                       if (definedTier != null) {
                           availableTier.add(definedTier);
                       } else {
                           log.warn("Unknown tier: " + tierName + " found on API: " + apiName);
                       }
                   }
               }
               api.addAvailableTiers(availableTier);
               api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
               api.setContextTemplate(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT_TEMPLATE));
               api.setLatest(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_LATEST)));
               ArrayList<URITemplate> urlPatternsList;

               urlPatternsList = ApiMgtDAO.getAllURITemplates(oldContext, oldId.getVersion());
               Set<URITemplate> uriTemplates = new HashSet<URITemplate>(urlPatternsList);

               for (URITemplate uriTemplate : uriTemplates) {
                   uriTemplate.setResourceURI(api.getUrl());
                   uriTemplate.setResourceSandboxURI(api.getSandboxUrl());

               }
               api.setUriTemplates(uriTemplates);

               Set<String> tags = new HashSet<String>();
               Tag[] tag = registry.getTags(artifactPath);
               for (Tag tag1 : tag) {
                   tags.add(tag1.getTagName());
               }
               api.addTags(tags);
               api.setLastUpdated(registry.get(artifactPath).getLastModified());
               api.setAsDefaultVersion(Boolean.valueOf(artifact.getAttribute(APIConstants.API_OVERVIEW_IS_DEFAULT_VERSION)));

               String environments = artifact.getAttribute(APIConstants.API_OVERVIEW_ENVIRONMENTS);
               api.setEnvironments(extractEnvironmentsForAPI(environments));

           } catch (GovernanceException e) {
               String msg = "Failed to get API fro artifact ";
               throw new APIManagementException(msg, e);
           } catch (RegistryException e) {
               String msg = "Failed to get LastAccess time or Rating";
               throw new APIManagementException(msg, e);
           } catch (UserStoreException e){
               String msg = "Failed to get User Realm of API Provider";
               throw new APIManagementException(msg, e);
           }
           return api;
       }


    /**
     * Gets the List of Authorized Domains by consumer key.
     * @param consumerKey
     * @return
     * @throws APIManagementException
     */
    public static List<String> getListOfAuthorizedDomainsByConsumerKey(String consumerKey)
            throws APIManagementException {
        String list = ApiMgtDAO.getAuthorizedDomainsByConsumerKey(consumerKey);
        if(list != null || !list.isEmpty()){
            return Arrays.asList(list.split(","));
        }

        return null;
    }
    
    public static boolean checkAccessTokenPartitioningEnabled() {
        return OAuthServerConfiguration.getInstance().isAccessTokenPartitioningEnabled();
    }

    public static boolean checkUserNameAssertionEnabled() {
        return OAuthServerConfiguration.getInstance().isUserNameAssertionEnabled();
    }

    public static String[] getAvailableKeyStoreTables() throws APIManagementException {
        String[] keyStoreTables = new String[0];
        Map<String, String>  domainMappings = getAvailableUserStoreDomainMappings();
        if (domainMappings != null) {
            keyStoreTables = new String[domainMappings.size()];
            int i = 0;
            for (Entry<String, String> e : domainMappings.entrySet()) {
                String value = e.getValue();
                keyStoreTables[i] = APIConstants.ACCESS_TOKEN_STORE_TABLE + "_" + value.trim();
                i++;
            }
        }
        return keyStoreTables;
    }

    public static Map<String, String> getAvailableUserStoreDomainMappings() throws
            APIManagementException {
        Map<String, String> userStoreDomainMap = new HashMap<String, String>();
        String domainsStr = OAuthServerConfiguration.getInstance().getAccessTokenPartitioningDomains();
        if (domainsStr != null) {
            String[] userStoreDomainsArr = domainsStr.split(",");
            for (String anUserStoreDomainsArr : userStoreDomainsArr) {
                String[] mapping = anUserStoreDomainsArr.trim().split(":"); //A:foo.com , B:bar.com
                if (mapping.length < 2) {
                    throw new APIManagementException("Domain mapping has not defined");
                }
                userStoreDomainMap.put(mapping[1].trim(), mapping[0].trim()); //key=domain & value=mapping
            }
        }
        return userStoreDomainMap;
    }

    public static String getAccessTokenStoreTableFromUserId(String userId)
            throws APIManagementException {
        String accessTokenStoreTable = APIConstants.ACCESS_TOKEN_STORE_TABLE;
        String userStore;
         if(userId != null) {
            String[] strArr = userId.split("/");
            if (strArr != null && strArr.length > 1) {
                userStore = strArr[0];
                Map<String, String> availableDomainMappings = getAvailableUserStoreDomainMappings();
                if (availableDomainMappings != null &&
                        availableDomainMappings.containsKey(userStore)) {
                    accessTokenStoreTable = accessTokenStoreTable + "_" +
                            availableDomainMappings.get(userStore);
                }
            }
         }
        return accessTokenStoreTable;
    }

    public static String getAccessTokenStoreTableFromAccessToken(String apiKey)
            throws APIManagementException {
        String userId = getUserIdFromAccessToken(apiKey); //i.e: 'foo.com/admin' or 'admin'
        return getAccessTokenStoreTableFromUserId(userId);
    }

    public static String getUserIdFromAccessToken(String apiKey) {
        String userId = null;
        String decodedKey = new String(Base64.decodeBase64(apiKey.getBytes()));
        String[] tmpArr = decodedKey.split(":");
        if (tmpArr != null && tmpArr.length == 2) { //tmpArr[0]= userStoreDomain & tmpArr[1] = userId
            userId = tmpArr[1];
        }
        return userId;
    }

    /**
     * validates if an accessToken has expired or not
     * @param accessTokenDO
     * @return true if token has expired else false
     */
    public static boolean isAccessTokenExpired(APIKeyValidationInfoDTO accessTokenDO) {
        long validityPeriod = accessTokenDO.getValidityPeriod();
        long issuedTime = accessTokenDO.getIssuedTime();
        long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
        long currentTime = System.currentTimeMillis();

        //If the validity period is not an never expiring value
        if (validityPeriod != Long.MAX_VALUE) {
            //check the validity of cached OAuth2AccessToken Response

            if ((currentTime - timestampSkew) > (issuedTime + validityPeriod)) {
                accessTokenDO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return true;
            }
        }

        return false;
    }

    /**
     *  When an input is having '@',replace it with '-AT-' [This is required to persist API data in registry,as registry paths don't allow '@' sign.]
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomain(String input){
        if(input!=null&& input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR) ){
            input=input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR,APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return input;
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to persist API data between registry and database]
     * @param input inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {
        if (input!=null && input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
            input = input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                  APIConstants.EMAIL_DOMAIN_SEPARATOR);
        }
        return input;
    }

    public static void copyResourcePermissions(String username, String sourceArtifactPath, String targetArtifactPath)
            throws APIManagementException {
        String sourceResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                  APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                         RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                                                  + sourceArtifactPath);

        String targetResourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                  APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                         RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                                                  + targetArtifactPath);

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));

        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(
		            tenantDomain);
            AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getAuthorizationManager();
            String[] allowedRoles = authManager.getAllowedRolesForResource(sourceResourcePath, ActionConstants.GET);

            if (allowedRoles != null) {

                for (String allowedRole : allowedRoles) {
                    authManager.authorizeRole(allowedRole, targetResourcePath, ActionConstants.GET);
                }
            }

        } catch (UserStoreException e) {
            throw new APIManagementException("Error while adding role permissions to API", e);
        }
    }


    /**
     * This function is to set resource permissions based on its visibility
     *
     * @param visibility   API visibility
     * @param roles        Authorized roles
     * @param artifactPath API resource path
     * @throws APIManagementException Throwing exception
     */
    public static void setResourcePermissions(String username, String visibility, String[] roles, String artifactPath)
            throws APIManagementException {
        try {
        	String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
        	        APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                    + artifactPath);

        	String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
        	if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.
        			MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
        		int tenantId = ServiceReferenceHolder.getInstance().getRealmService().
        				getTenantManager().getTenantId(tenantDomain);
                // calculate resource path
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                        (ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);
                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(tenantId).getAuthorizationManager();
                if (visibility != null && visibility.equalsIgnoreCase(APIConstants.API_RESTRICTED_VISIBILITY)) {
                    boolean isRoleEveryOne = false;
                    /*If no roles have defined, authorize for everyone role */
                    if (roles != null && roles.length == 1 && roles[0].equals("")) {
                        authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath,
                                                  ActionConstants.GET);
                        isRoleEveryOne = true;
                    } else {
                        for (String role : roles) {
                            if (role.equalsIgnoreCase(APIConstants.EVERYONE_ROLE)) {
                                isRoleEveryOne = true;
                            }
                            authManager.authorizeRole(role, resourcePath, ActionConstants.GET);

                        }
                    }
                    if (!isRoleEveryOne) {
                        authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    }
                    authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && visibility.equalsIgnoreCase(APIConstants.API_PRIVATE_VISIBILITY)) {
                    authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && visibility.equalsIgnoreCase(APIConstants.DOC_OWNER_VISIBILITY)) {

                    /*If no roles have defined, deny access for everyone & anonymous role */
                    if (roles == null) {
                        authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                        authManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    } else {
                        for (String role : roles) {
                            authManager.denyRole(role, resourcePath, ActionConstants.GET);

                        }
                    }
                } else {
                    authManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath,
                                              ActionConstants.GET);
                    authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath,
                                              ActionConstants.GET);
                }
            } else {
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                        (ServiceReferenceHolder.getUserRealm());

                if (visibility != null && visibility.equalsIgnoreCase(APIConstants.API_RESTRICTED_VISIBILITY)) {
                    boolean isRoleEveryOne = false;
                    for (String role : roles) {
                        if (role.equalsIgnoreCase(APIConstants.EVERYONE_ROLE)) {
                            isRoleEveryOne = true;
                        }
                        authorizationManager.authorizeRole(role, resourcePath, ActionConstants.GET);

                    }
                    if (!isRoleEveryOne) {
                        authorizationManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    }
                    authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);

                } else if (visibility != null && visibility.equalsIgnoreCase(APIConstants.API_PRIVATE_VISIBILITY)) {
                    authorizationManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                    authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                } else if (visibility != null && visibility.equalsIgnoreCase(APIConstants.DOC_OWNER_VISIBILITY)) {
                     /*If no roles have defined, deny access for everyone & anonymous role */
                    if (roles == null) {
                        authorizationManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
                        authorizationManager.denyRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    } else {
                        for (String role : roles) {
                            authorizationManager.denyRole(role, resourcePath, ActionConstants.GET);

                        }
                    }
                } else {
                    authorizationManager.authorizeRole(APIConstants.EVERYONE_ROLE, resourcePath,
                                                       ActionConstants.GET);
                    authorizationManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath,
                                                       ActionConstants.GET);
                }
        	}


        } catch (UserStoreException e) {
        	throw new APIManagementException("Error while adding role permissions to API", e);
        }
    }

	/**
	 * Load the throttling policy  to the registry for tenants
	 *
	 * @param tenant
	 * @param tenantID
	 * @throws APIManagementException
	 */

	public static void loadTenantAPIPolicy(String tenant, int tenantID)
	                                                                   throws APIManagementException {
		try {
			RegistryService registryService =
			                                  ServiceReferenceHolder.getInstance()
			                                                        .getRegistryService();
			//UserRegistry govRegistry = registryService.getGovernanceUserRegistry(tenant, tenantID);
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

			if (govRegistry.resourceExists(APIConstants.API_TIER_LOCATION)) {
				if (log.isDebugEnabled()) {
					log.debug("Tier policies already uploaded to the tenant's registry space");
				}
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug("Adding API tier policies to the tenant's registry");
			}
			InputStream inputStream =
			                          APIManagerComponent.class.getResourceAsStream("/tiers/default-tiers.xml");
			byte[] data = IOUtils.toByteArray(inputStream);
			Resource resource = govRegistry.newResource();
			resource.setContent(data);
			govRegistry.put(APIConstants.API_TIER_LOCATION, resource);

		} catch (RegistryException e) {
			throw new APIManagementException(
			                                 "Error while saving policy information to the registry",
			                                 e);
		} catch (IOException e) {
			throw new APIManagementException("Error while reading policy file content", e);
		}
    }

    /**
     * Load the External API Store Configuration  to the registry
     *
     * @param tenantID
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */

	public static void loadTenantExternalStoreConfig(int tenantID)
	                                                                   throws APIManagementException {
		try {
			RegistryService registryService =
			                                  ServiceReferenceHolder.getInstance()
			                                                        .getRegistryService();
			//UserRegistry govRegistry = registryService.getGovernanceUserRegistry(tenant, tenantID);
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }
			if (log.isDebugEnabled()) {
				log.debug("Adding External Stores configuration to the tenant's registry");
			}
			InputStream inputStream =
			                          APIManagerComponent.class.getResourceAsStream("/externalstores/default-external-api-stores.xml");
			byte[] data = IOUtils.toByteArray(inputStream);
			Resource resource = govRegistry.newResource();
			resource.setContent(data);
			govRegistry.put(APIConstants.EXTERNAL_API_STORES_LOCATION, resource);

			/*set resource permission*/
            AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantID).getAuthorizationManager();
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                    APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                    + APIConstants.EXTERNAL_API_STORES_LOCATION);
            authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);

		} catch (RegistryException e) {
            throw new APIManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading External Stores configuration file content", e);
        } catch (UserStoreException e) {
        	throw new APIManagementException("Error while setting permission to External Stores configuration file", e);
		}
    }

	/**
	 * Load the Google Analytics Configuration  to the registry
	 *
	 * @param tenantID
	 * @throws APIManagementException
	 */

	public static void loadTenantGAConfig(int tenantID) throws APIManagementException {
        InputStream inputStream = null;
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            //UserRegistry govRegistry = registryService.getGovernanceUserRegistry(tenant, tenantID);
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(APIConstants.GA_CONFIGURATION_LOCATION)) {
                log.debug("Google Analytics configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding Google Analytics configuration to the tenant's registry");
            }
            inputStream = APIManagerComponent.class.getResourceAsStream("/statistics/default-ga-config.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            govRegistry.put(APIConstants.GA_CONFIGURATION_LOCATION, resource);

			/*set resource permission*/
            AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantID).getAuthorizationManager();
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                       RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                                                + APIConstants.GA_CONFIGURATION_LOCATION);
            authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving Google Analytics configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading Google Analytics configuration file content", e);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while setting permission to Google Analytics configuration file", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error while closing the input stream");
                }
            }
        }
    }

    public static void loadTenantWorkFlowExtensions(int tenantID)
            throws APIManagementException {
        // TODO: Merge different resource loading methods and create a single method.
        try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            //UserRegistry govRegistry = registryService.getGovernanceUserRegistry(tenant, tenantID);
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

            if (govRegistry.resourceExists(APIConstants.WORKFLOW_EXECUTOR_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding External Stores configuration to the tenant's registry");
            }
            InputStream inputStream =
                    APIManagerComponent.class.getResourceAsStream("/workflowextensions/default-workflow-extensions.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            resource.setMediaType(APIConstants.WORKFLOW_MEDIA_TYPE);
            govRegistry.put(APIConstants.WORKFLOW_EXECUTOR_LOCATION, resource);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading External Stores configuration file content", e);
        }
    }
    
    public static void loadTenantAPILifecycle(int tenantID)throws APIManagementException {
        try {
            UserRegistry govRegistry = getSystemConfigRegistry(tenantID);

            if (govRegistry.resourceExists(APIConstants.API_LIFE_CYCLE_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding External Stores configuration to the tenant's registry");
            }
            String relativePath = "/lifecycle/APILifeCycle.xml";
            Resource resource = govRegistry.newResource();
            byte[] data = getResourceData(relativePath);
            resource.setContent(data);
            resource.setMediaType(APIConstants.XML_MEDIA_TYPE);
            String content = new String(data);
            UserRegistry rootRegistry = getRootRegistry();
            CommonUtil.addLifecycle(content, govRegistry, rootRegistry);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading External Stores configuration file content", e);
        } catch (XMLStreamException e) {
        	throw new APIManagementException("Error while generate  External Stores configuration file content", e);
		}
    }
    
    

    /**
     *
     * @param tenantId
     * @throws APIManagementException
     */
    public static void loadTenantSelfSignUpConfigurations(int tenantId)
    		throws APIManagementException {
    	try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantId);

            if (govRegistry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                log.debug("Self signup configuration already uploaded to the registry");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding Self signup configuration to the tenant's registry");
            }
            InputStream inputStream;
            if(tenantId==org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID){
            inputStream=
                    APIManagerComponent.class.getResourceAsStream("/signupconfigurations/default-sign-up-config.xml");
            }else{
            inputStream=
                        APIManagerComponent.class.getResourceAsStream("/signupconfigurations/tenant-sign-up-config.xml");
            }
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = govRegistry.newResource();
            resource.setContent(data);
            resource.setMediaType(APIConstants.SELF_SIGN_UP_CONFIG_MEDIA_TYPE);
            govRegistry.put(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION, resource);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving Self signup configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading Self signup configuration file content", e);
        }
    }

    /**
     *
     * @param tenantId
     * @throws APIManagementException
     */
    public static void createSelfSignUpRoles(int tenantId)
    		throws APIManagementException {
    	try {
            RegistryService registryService =
                    ServiceReferenceHolder.getInstance()
                            .getRegistryService();
            UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantId);
            if (govRegistry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                Resource resource = govRegistry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION);
                InputStream content=resource.getContentStream();
                DocumentBuilderFactory factory
                        = DocumentBuilderFactory.newInstance();
                DocumentBuilder parser = factory.newDocumentBuilder();
                Document dc= parser.parse(content);
                boolean enableSignup=Boolean.parseBoolean(dc.getElementsByTagName(APIConstants.SELF_SIGN_UP_REG_ENABLED).item(0).getFirstChild().getNodeValue());
                String signUpDomain=dc.getElementsByTagName(APIConstants.SELF_SIGN_UP_REG_DOMAIN_ELEM).item(0).getFirstChild().getNodeValue();
                if(enableSignup){
                int roleLength=dc.getElementsByTagName(APIConstants.SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT).getLength();
                for(int i=0;i<roleLength;i++){
                String roleName=dc.getElementsByTagName(APIConstants.SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT).item(i).getFirstChild().getNodeValue();
                boolean isExternalRole=Boolean.parseBoolean(dc.getElementsByTagName(APIConstants.SELF_SIGN_UP_REG_ROLE_IS_EXTERNAL).item(i).getFirstChild().getNodeValue());
                if(roleName!=null){
                // If isExternalRole==false ;create the subscriber role as an internal role
                if(isExternalRole && signUpDomain!=null){
                roleName=signUpDomain.toUpperCase()+CarbonConstants.DOMAIN_SEPARATOR+roleName;
                }else{
                roleName= UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR+roleName;
                }
                createSubscriberRole(roleName,tenantId);
                }
                }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Adding Self signup configuration to the tenant's registry");
            }



        } catch (RegistryException e) {
            throw new APIManagementException("Error while getting Self signup role information from the registry", e);
        } catch (ParserConfigurationException e) {
            throw new APIManagementException("Error while getting Self signup role information from the registry", e);
        } catch (SAXException e) {
            throw new APIManagementException("Error while getting Self signup role information from the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while getting Self signup role information from the registry", e);
        }
    }

	 /**
     * Add BAM Server Profile Configuration which is used for southbound statistics
     * publishing
     * @throws APIManagementException
     */
    public static void addBamServerProfile(String bamServerURL, String bamServerUser, 
    		String bamServerPassword, int tenantId) throws APIManagementException {
    	RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getConfigSystemRegistry(tenantId);
            log.debug("Adding Bam Server Profile to the registry");
            InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/bam/profile/bam-profile.xml");
            String bamProfile = IOUtils.toString(inputStream);
                        
            String bamProfileConfig = bamProfile.replaceAll("\\[1\\]", bamServerURL).
            		replaceAll("\\[2\\]", bamServerUser).
            		replaceAll("\\[3\\]", bamServerPassword);

            Resource resource = registry.newResource();
            resource.setContent(bamProfileConfig);
            registry.put(APIConstants.BAM_SERVER_PROFILE_LOCATION, resource);

        } catch(RegistryException e) {
        	throw new APIManagementException("Error while adding BAM Server Profile configuration " +
        			"information to the registry", e);
        } catch (IOException e) {
        	throw new APIManagementException("Error while reading BAM Server Profile configuration " +
        			"configuration file content", e);
		}
	}

    public static boolean isAnalyticsEnabled() {
     ServiceDataPublisherAdmin serviceDataPublisherAdmin = APIManagerComponent.getDataPublisherAdminService();
        if (serviceDataPublisherAdmin != null){
            return serviceDataPublisherAdmin.getEventingConfigData().isServiceStatsEnable();
        }
        return false;
    }

    public static Map<String, String> getAnalyticsConfigFromRegistry() {

        Map<String,String> propertyMap = new HashMap<String, String>();
        EventingConfigData eventingConfigData = APIManagerComponent.
                getDataPublisherAdminService().getEventingConfigData();
        propertyMap.put(APIConstants.API_USAGE_BAM_SERVER_URL_GROUPS, eventingConfigData.getUrl());
        propertyMap.put(APIConstants.API_USAGE_BAM_SERVER_USER, eventingConfigData.getUserName());
        propertyMap.put(APIConstants.API_USAGE_BAM_SERVER_PASSWORD, eventingConfigData.getPassword());
        return propertyMap;
    }

	public static void addDefinedAllSequencesToRegistry(UserRegistry registry,
	                                                    String customSequenceType)
			throws APIManagementException {

		InputStream inSeqStream = null;
		String seqFolderLocation =
				APIConstants.API_CUSTOM_SEQUENCES_FOLDER_LOCATION + File.separator +
				customSequenceType;

		try {
			File inSequenceDir = new File(seqFolderLocation);
			File[] sequences;
			sequences = inSequenceDir.listFiles();

			if (sequences != null) {
				for (File sequenceFile : sequences) {
					String sequenceFileName = sequenceFile.getName();
					String regResourcePath =
							APIConstants.API_CUSTOM_SEQUENCE_LOCATION + "/" +
							customSequenceType + "/" + sequenceFileName;
					if (registry.resourceExists(regResourcePath)) {
						if (log.isDebugEnabled()) {
							log.debug("Defined sequences have already been added to the registry");
						}
					} else {
						if (log.isDebugEnabled()) {
							log.debug("Adding defined sequences to the registry.");
						}

						inSeqStream =
								new FileInputStream(sequenceFile);
						byte[] inSeqData = IOUtils.toByteArray(inSeqStream);
						Resource inSeqResource = registry.newResource();
						inSeqResource.setContent(inSeqData);

						registry.put(regResourcePath, inSeqResource);
					}
				}
			} else {
				log.error(
						"Custom sequence template location unavailable for custom sequence type " +
						customSequenceType + " : " + seqFolderLocation
				);
			}

		} catch (RegistryException e) {
			throw new APIManagementException(
					"Error while saving defined sequences to the registry ", e);
		} catch (IOException e) {
			throw new APIManagementException("Error while reading defined sequence ", e);
		} finally {
			if (inSeqStream != null) {
				try {
					inSeqStream.close();
				} catch (IOException e) {
					log.error(
							"Error while closing input stream in path " + seqFolderLocation + " " +
							e);
				}
			}
		}

	}

	public static void writeDefinedSequencesToTenantRegistry(int tenantID)
			throws APIManagementException {
		try {

			RegistryService registryService =
					ServiceReferenceHolder.getInstance()
					                      .getRegistryService();
			UserRegistry govRegistry = registryService.getGovernanceSystemRegistry(tenantID);

			//Add all custom in,out and fault sequences to tenant registry
			APIUtil.addDefinedAllSequencesToRegistry(govRegistry,
			                                         APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
			APIUtil.addDefinedAllSequencesToRegistry(govRegistry,
			                                         APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT);
			APIUtil.addDefinedAllSequencesToRegistry(govRegistry,
			                                         APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT);

		} catch (RegistryException e) {
			throw new APIManagementException(
					"Error while saving defined sequences to the tenant's registry ", e);
		}
	}

	/**
	 * Load the  API RXT to the registry for tenants
	 *
	 * @param tenant
	 * @param tenantID
	 * @throws APIManagementException
	 */

    public static void loadloadTenantAPIRXT(String tenant, int tenantID) throws APIManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry registry = null;
        try {
            //registry = registryService.getRegistry(tenant, tenantID);
            registry = registryService.getGovernanceSystemRegistry(tenantID);
        } catch (RegistryException e) {
            throw new APIManagementException("Error when create registry instance ", e);
        }

        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" +
                        File.separator + "rxts";
        File file = new File(rxtDir);
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        for (String rxtPath : rxtFilePaths) {
            String resourcePath =
                    GovernanceConstants.RXT_CONFIGS_PATH +
                    RegistryConstants.PATH_SEPARATOR + rxtPath;

            //This is  "registry" is a governance registry instance, therefore calculate the relative path to governance.
            String govRelativePath = RegistryUtils.getRelativePathToOriginal(resourcePath,
                                                                             APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                                    RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH));
            try {
                // calculate resource path
                RegistryAuthorizationManager authorizationManager = new RegistryAuthorizationManager
                        (ServiceReferenceHolder.getUserRealm());
                resourcePath = authorizationManager.computePathOnMount(resourcePath);

                AuthorizationManager authManager = ServiceReferenceHolder.getInstance().getRealmService().
                        getTenantUserRealm(tenantID).getAuthorizationManager();

                if (registry.resourceExists(govRelativePath)) {
                    // set anonymous user permission to RXTs
                    authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);
                    continue;
                }

                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                registry.put(govRelativePath, resource);


                authManager.authorizeRole(APIConstants.ANONYMOUS_ROLE, resourcePath, ActionConstants.GET);

            } catch (UserStoreException e) {
                throw new APIManagementException("Error while adding role permissions to API", e);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }

    }

    /**
     * Converting the user store domain name to uppercase.
     *
     * @param username Username to be modified
     * @return Username with domain name set to uppercase.
     */
    public static String setDomainNameToUppercase(String username) {
        if (username != null) {
            String[] nameParts = username.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (nameParts.length > 1) {
                username = nameParts[0].toUpperCase() + CarbonConstants.DOMAIN_SEPARATOR + nameParts[1];
            }
        }

        return username;
    }

    public static void createSubscriberRole(String roleName,int tenantId) throws APIManagementException {

        String[] permissions = new String[]{
                "/permission/admin/login",
                APIConstants.Permissions.API_SUBSCRIBE
        };
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm;
            org.wso2.carbon.user.api.UserRealm tenantRealm;
            UserStoreManager manager;

            if (tenantId < 0) {
                realm = realmService.getBootstrapRealm();
                manager = realm.getUserStoreManager();
            } else {
                tenantRealm = realmService.getTenantUserRealm(tenantId);
                manager = tenantRealm.getUserStoreManager();
            }
            if (!manager.isExistingRole(roleName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating subscriber role: " + roleName);
                }
                Permission[] subscriberPermissions = new Permission[]{new Permission("/permission/admin/login", UserMgtConstants.EXECUTE_ACTION),
                        new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
                String tenantAdminName = ServiceReferenceHolder.getInstance()
                        .getRealmService().getTenantUserRealm(tenantId).
                                getRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{tenantAdminName};
                manager.addRole(roleName, userList, subscriberPermissions);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while creating subscriber role: " + roleName+ " - " +
                    "Self registration might not function properly.", e);
        }
    }

    public void setupSelfRegistration(APIManagerConfiguration config,int tenantId)
            throws APIManagementException {
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            return;
        }
        //Create the subscriber role as an internal role
        String role = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR+config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role.equals(UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
            // Required parameter missing - Throw an exception and interrupt startup
            throw new APIManagementException("Required subscriber role parameter missing " +
                                             "in the self sign up configuration");
        }
        String[] permissions = new String[]{
                "/permission/admin/login",
                APIConstants.Permissions.API_SUBSCRIBE
        };
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm;
            org.wso2.carbon.user.api.UserRealm tenantRealm;
            UserStoreManager manager;

            if (tenantId < 0) {
                realm = realmService.getBootstrapRealm();
                manager = realm.getUserStoreManager();
            } else {
                tenantRealm = realmService.getTenantUserRealm(tenantId);
                manager = tenantRealm.getUserStoreManager();
            }
            if (!manager.isExistingRole(role)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating subscriber role: " + role);
                }
                Permission[] subscriberPermissions = new Permission[]{new Permission("/permission/admin/login", UserMgtConstants.EXECUTE_ACTION),
                        new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
                String tenantAdminName = ServiceReferenceHolder.getInstance()
                        .getRealmService().getTenantUserRealm(tenantId).
                                getRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{tenantAdminName};
                manager.addRole(role, userList, subscriberPermissions);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while creating subscriber role: " + role + " - " +
                    "Self registration might not function properly.", e);
        }
    }

    public static String removeAnySymbolFromUriTempate(String uriTemplate) {
    	if (uriTemplate != null) {
    		int anySymbolIndex = uriTemplate.indexOf("/*");
    		if (anySymbolIndex != -1) {
    			return uriTemplate.substring(0, anySymbolIndex);
    		}
    	}
    	return uriTemplate;
    }

    public static float getAverageRating(APIIdentifier apiId) throws APIManagementException {
        return ApiMgtDAO.getAverageRating(apiId);
    }

    public static float getAverageRating(int apiId) throws APIManagementException {
        return ApiMgtDAO.getAverageRating(apiId);
    }

    public static List<Tenant> getAllTenantsWithSuperTenant() throws UserStoreException {
        Tenant[] tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        ArrayList<Tenant> tenantArrayList=new ArrayList<Tenant>();
        for(Tenant t:tenants){
            tenantArrayList.add(t);
        }
        Tenant superAdminTenant=new Tenant();
        superAdminTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superAdminTenant.setId(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID);
        superAdminTenant.setAdminName(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
        tenantArrayList.add(superAdminTenant);
        return tenantArrayList;
    }

    /**
     * In multi tenant environment, publishers should allow only to revoke the tokens generated within his domain.
     * Super tenant should not see the tenant created tokens and vise versa. This method is used to check the logged in
     * user have permissions to revoke a given users tokens.
     * @param loggedInUser current logged in user to publisher
     * @param authorizedUser access token owner
     * @return
     */
    public static boolean isLoggedInUserAuthorizedToRevokeToken(String loggedInUser, String authorizedUser) {
        String loggedUserTenantDomain = MultitenantUtils.getTenantDomain(loggedInUser);
        String authorizedUserTenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);

        if (loggedUserTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) &&
                authorizedUserTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            return true;
        } else if (loggedUserTenantDomain.equals(authorizedUserTenantDomain)) {
            return true;
        }

        return false;
    }
    public static int getApplicationId(String appName,String userId) throws APIManagementException {
        return new ApiMgtDAO().getApplicationId(appName,userId);
    }

    public static boolean isAPIManagementEnabled() {
        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("APIManagement.Enabled"));
    }

    public static boolean isLoadAPIContextsAtStartup() {
        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("APIManagement.LoadAPIContextsInServerStartup"));
    }

    public static Set<APIStore> getExternalAPIStores(int tenantId) throws APIManagementException {
        SortedSet<APIStore> apistoreSet=new TreeSet<APIStore>(new APIStoreNameComparator());
        apistoreSet.addAll(getExternalStores(tenantId));
        if (apistoreSet.size() != 0) {
            return apistoreSet;
        } else {
            return null;
        }

    }

    public static boolean isAllowDisplayAPIsWithMultipleStatus() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String displayAllAPIs = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_ALL_APIS);
        if (displayAllAPIs == null) {
            log.warn("The configurations related to show deprecated APIs in APIStore " +
                    "are missing in api-manager.xml.");
            return false;
        }
        return Boolean.parseBoolean(displayAllAPIs);
    }

    public static boolean isAllowDisplayMultipleVersions() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String displayMultiVersions = config.getFirstProperty(APIConstants.API_STORE_DISPLAY_MULTIPLE_VERSIONS);
        if (displayMultiVersions == null) {
            log.warn("The configurations related to show multiple versions of API in APIStore " +
                    "are missing in api-manager.xml.");
            return false;
        }
        return Boolean.parseBoolean(displayMultiVersions);
    }

    public static Set<APIStore> getExternalAPIStores(Set<APIStore> inputStores, int tenantId)
            throws APIManagementException {
        SortedSet<APIStore> apiStores = new TreeSet<APIStore>(new APIStoreNameComparator());
        apiStores.addAll(getExternalStores(tenantId));
        boolean exists = false;
        if (apiStores.size() != 0) {
            for (APIStore store : apiStores) {
                for (APIStore inputStore : inputStores) {
                    if (inputStore.getName().equals(store.getName())) { //If the configured apistore already stored in db,ignore adding it again
                        exists = true;
                    }
                }
                if (!exists) {
                    inputStores.add(store);
                }
                exists=false;
            }

        }
        return inputStores;


    }

    public static boolean isAPIsPublishToExternalAPIStores(int tenantId)
            throws APIManagementException {

        return getExternalStores(tenantId).size() != 0;


    }

    public static boolean isAPIGatewayKeyCacheEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String serviceURL = config.getFirstProperty(APIConstants.API_GATEWAY_KEY_CACHE_ENABLED);
            return Boolean.parseBoolean(serviceURL);
        } catch (Exception e) {
            log.error("Did not found valid API Validation Information cache configuration. Use default configuration" + e);
        }
        return true;
    }


    public static Cache getAPIContextCache() {
        CacheManager contextCacheManager = Caching.getCacheManager(APIConstants.API_CONTEXT_CACHE_MANAGER).
                getCache(APIConstants.API_CONTEXT_CACHE).getCacheManager();
        if (!isContextCacheInitialized) {
            isContextCacheInitialized = true;
            return contextCacheManager.<String, Boolean>createCacheBuilder(APIConstants.API_CONTEXT_CACHE_MANAGER).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.DAYS,
                            APIConstants.API_CONTEXT_CACHE_EXPIRY_TIME_IN_DAYS)).setStoreByValue(false).build();
        } else {
            return Caching.getCacheManager(APIConstants.API_CONTEXT_CACHE_MANAGER).getCache(APIConstants.API_CONTEXT_CACHE);
        }
    }

    /**
     * Get active tenant domains
     *
     * @return
     * @throws UserStoreException
     */
    public static Set<String> getActiveTenantDomains() throws UserStoreException {
        Set<String> tenantDomains = null;
        Tenant[] tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        if (tenants == null || tenants.length == 0) {
            tenantDomains = Collections.<String>emptySet();
        } else {
            tenantDomains = new HashSet<String>();
            for (Tenant tenant : tenants) {
                if (tenant.isActive()) {
                    tenantDomains.add(tenant.getDomain());
                }
            }
            if (tenantDomains.size() > 0) {
                tenantDomains.add(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }
        }
        return tenantDomains;
    }


    /**
     * Retrieves the role list of system

     * @throws APIManagementException If an error occurs
     */
    public static String[] getRoleNames(String username) throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        try {
            if (!tenantDomain.equals(org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
                UserStoreManager manager = ServiceReferenceHolder.getInstance().
                        getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();

                return manager.getRoleNames();
            } else {
                RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
                return authorizationManager.getRoleNames();
            }
        } catch (UserStoreException e) {
            log.error("Error while getting all the roles", e);
            return null;

        }

    }

    /**
     * Create API Definition in JSON
     *
     * @param api API
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to generate the content and save
     */
    @Deprecated
    public static String createSwaggerJSONContent(API api) throws APIManagementException {
    	APIIdentifier identifier = api.getId();

		APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        Environment environment = (Environment) config.getApiGatewayEnvironments().values().toArray()[0];
        String endpoints = environment.getApiGatewayEndpoint();
        String[] endpointsSet = endpoints.split(",");
        String apiContext = api.getContext();
        String version = identifier.getVersion();
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        String description = api.getDescription();

        // With the new context version strategy, the URL prefix is the apiContext. the verison will be embedded in
        // the apiContext.
        String urlPrefix = apiContext;
//        String urlPrefix = apiContext + "/" +version;

        if (endpointsSet.length < 1) {
        	throw new APIManagementException("Error in creating JSON representation of the API" + identifier.getApiName());
        }
    	if (description == null || description.equals("")) {
    		description = "";
    	} else {
    		description = description.trim();
    	}

    	Map<String, List<Operation>> uriTemplateDefinitions = new HashMap<String, List<Operation>>();
    	List<APIResource> apis = new ArrayList<APIResource>();
    	for (URITemplate template : uriTemplates) {
    		List<Operation> ops;
    		List<Parameter> parameters = null;
    		String path = urlPrefix +
    				APIUtil.removeAnySymbolFromUriTempate(template.getUriTemplate());
    		/* path exists in uriTemplateDefinitions */
    		if (uriTemplateDefinitions.get(path) != null) {
    			ops = uriTemplateDefinitions.get(path);
    			parameters = new ArrayList<Parameter>();

    			String httpVerb = template.getHTTPVerb();
    			/* For GET and DELETE Parameter name - Query Parameters*/
    			if (httpVerb.equals(Constants.Configuration.HTTP_METHOD_GET) ||
    					httpVerb.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
    				Parameter queryParam = new Parameter(APIConstants.OperationParameter.QUERY_PARAM_NAME,
    						APIConstants.OperationParameter.QUERY_PARAM_DESCRIPTION, APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
    				parameters.add(queryParam);
    			} else {/* For POST and PUT Parameter name - Payload*/
    				Parameter payLoadParam = new Parameter(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME,
    						APIConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION, APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
    				parameters.add(payLoadParam);
    			}

    			Parameter authParam = new Parameter(APIConstants.OperationParameter.AUTH_PARAM_NAME,
						APIConstants.OperationParameter.AUTH_PARAM_DESCRIPTION, APIConstants.OperationParameter.AUTH_PARAM_TYPE, false, false, "String");
    			parameters.add(authParam);
    			if (!httpVerb.equals("OPTIONS")) {
    				Operation op = new Operation(httpVerb, description, description, parameters);
    				ops.add(op);
    			}
    		} else {/* path not exists in uriTemplateDefinitions */
    			ops = new ArrayList<Operation>();
    			parameters = new ArrayList<Parameter>();

				String httpVerb = template.getHTTPVerb();
				/* For GET and DELETE Parameter name - Query Parameters*/
    			if (httpVerb.equals(Constants.Configuration.HTTP_METHOD_GET) ||
    					httpVerb.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
    				Parameter queryParam = new Parameter(APIConstants.OperationParameter.QUERY_PARAM_NAME,
    						APIConstants.OperationParameter.QUERY_PARAM_DESCRIPTION, APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
    				parameters.add(queryParam);
    			} else {/* For POST and PUT Parameter name - Payload*/
    				Parameter payLoadParam = new Parameter(APIConstants.OperationParameter.PAYLOAD_PARAM_NAME,
    						APIConstants.OperationParameter.PAYLOAD_PARAM_DESCRIPTION, APIConstants.OperationParameter.PAYLOAD_PARAM_TYPE, false, false, "String");
    				parameters.add(payLoadParam);
    			}
    			Parameter authParam = new Parameter(APIConstants.OperationParameter.AUTH_PARAM_NAME,
						APIConstants.OperationParameter.AUTH_PARAM_DESCRIPTION, APIConstants.OperationParameter.AUTH_PARAM_TYPE, false, false, "String");
    			parameters.add(authParam);
    			if (!httpVerb.equals("OPTIONS")) {
    				Operation op = new Operation(httpVerb, description, description, parameters);
    				ops.add(op);
    			}
    			uriTemplateDefinitions.put(path, ops);
    		}
    	}

    	Set<String> resPaths = uriTemplateDefinitions.keySet();

		for (String resPath: resPaths) {
			APIResource apiResource = new APIResource(resPath, description, uriTemplateDefinitions.get(resPath));
			apis.add(apiResource);
    	}

		APIDefinition apidefinition = new APIDefinition(version, APIConstants.SWAGGER_VERSION, endpointsSet[0], apiContext, apis);

    	Gson gson = new Gson();
    	return gson.toJson(apidefinition);
     }

    /**
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     * @throws APIManagementException
     */
    public static int getTenantId(String userName){
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if(realmService == null){
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            return tenantId;
        } catch (UserStoreException e) {

            log.error(e);
        }

        return -1;
    }

    /**
     * Helper method to get username with tenant domain.
     *
     * @param userName
     * @return  userName with tenant domain
     */
    public static String getUserNameWithTenantSuffix(String userName) {
        String userNameWithTenantPrefix = userName;
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        if (userName != null && !userName.contains("@")
                  && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
              userNameWithTenantPrefix = userName + "@" + tenantDomain;
        }

        return userNameWithTenantPrefix;

    }

    /**
     * Build OMElement from inputstream
     * @param inputStream
     * @return  OMElement
     * @throws Exception
     * @return
     */
    public static OMElement buildOMElement(InputStream inputStream) throws Exception {
        XMLStreamReader parser;
        StAXOMBuilder builder;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
             builder = new StAXOMBuilder(parser);
        }
        catch (XMLStreamException e) {
            String msg = "Error in initializing the parser.";
            log.error(msg, e);
            throw new Exception(msg, e);
        }

        return builder.getDocumentElement();
    }


	/**
	 * Get stored in sequences, out sequences and fault sequences from the governanceSystem registry
	 *
	 * @param sequenceName
	 *            -The sequence to be retrieved
	 * @param tenantId
	 * @param direction
	 *            - Direction indicates which sequences to fetch. Values would be
	 *             "in", "out" or "fault"
	 * @return
	 * @throws APIManagementException
	 */
	public static OMElement getCustomSequence(String sequenceName, int tenantId,
	                                                 String direction)
	                                                                  throws APIManagementException {
		org.wso2.carbon.registry.api.Collection seqCollection = null;

		try {
			UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
			                                              .getGovernanceSystemRegistry(tenantId);
			if ("in".equals(direction)) {
				seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION);
			}
			else if ("out".equals(direction)) {
				seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION);
			}
            else if("fault".equals(direction)) {
                seqCollection = (org.wso2.carbon.registry.api.Collection) registry.get(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION);
            }

			if (seqCollection != null) {
				String[] childPaths = seqCollection.getChildren();

				for (int i = 0; i < childPaths.length; i++) {
					Resource sequence = registry.get(childPaths[i]);
					OMElement seqElment = APIUtil.buildOMElement(sequence.getContentStream());
					if (sequenceName.equals(seqElment.getAttributeValue(new QName("name")))) {
						return seqElment;
					}
				}
			}

		} catch (Exception e) {
			String msg = "Issue is in accessing the Registry";
			log.error(msg);
			throw new APIManagementException(msg, e);
		}
		return null;
	}

	/**
	 * Return the sequence extension name.
	 * eg: admin--testAPi--v1.00
	 *
	 * @param api
	 * @return
	 */
	public static String getSequenceExtensionName(API api) {

		String seqExt = api.getId().getProviderName() + "--" + api.getId().getApiName() + ":v" +
		                        api.getId().getVersion();

		return seqExt;

	}

    /**
     *
     * @param token
     * @return
     */
    public static String decryptToken(String token) throws CryptoException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if(Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENCRYPT_TOKENS))){
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(token));
        }
        return token;
    }

    /**
     *
     * @param token
     * @return
     */
    public static String encryptToken(String token) throws CryptoException{
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if(Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_ENCRYPT_TOKENS))){
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(token.getBytes());
        }
        return token;
    }

    public static void loadTenantRegistry(int tenantId)throws org.wso2.carbon.registry.core.exceptions.RegistryException{
        TenantRegistryLoader tenantRegistryLoader = APIManagerComponent.getTenantRegistryLoader();
        ServiceReferenceHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
        tenantRegistryLoader.loadTenantRegistry(tenantId);
     }

    /**
     * This is to get the registry resource's HTTP permlink path.
     * Once this issue is fixed (https://wso2.org/jira/browse/REGISTRY-2110),
     * we can remove this method, and get permlink from the resource.
     * @param path - Registry resource path
     * @return {@link String} -HTTP permlink
     */
    public static String getRegistryResourceHTTPPermlink(String path){
        String schemeHttp = "http";
        String schemeHttps = "https";

        ConfigurationContextService contetxservice = ServiceReferenceHolder.getContextService();
        //First we will try to generate http permalink and if its disabled then only we will consider https
        int port = CarbonUtils.getTransportProxyPort(contetxservice.getServerConfigContext(), schemeHttp);
        if (port == -1) {
            port = CarbonUtils.getTransportPort(contetxservice.getServerConfigContext(), schemeHttp);
        }
        //getting https parameters if http is disabled. If proxy port is not present we will go for default port
        if (port == -1) {
            port = CarbonUtils.getTransportProxyPort(contetxservice.getServerConfigContext(), schemeHttps);
        }
        if (port == -1) {
            port = CarbonUtils.getTransportPort(contetxservice.getServerConfigContext(), schemeHttps);
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");

        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }
        RegistryService registryService =ServiceReferenceHolder.getInstance().getRegistryService();
        String version = "";
        if (registryService == null) {
            log.error("Registry Service has not been set.");
        } else if (path != null) {
            try {
                String[] versions = registryService.getRegistry(
                        CarbonConstants.REGISTRY_SYSTEM_USERNAME,
                        CarbonContext.getThreadLocalCarbonContext().getTenantId()).getVersions(path);
                if (versions != null && versions.length > 0) {
                    version = versions[0].substring(versions[0].lastIndexOf(";version:"));
                }
            } catch (RegistryException e) {
                log.error("An error occurred while determining the latest version of the " +
                        "resource at the given path: " + path, e);
            }
        }
        if (port != -1 && path != null) {
            String tenantDomain =
            		PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            return webContext +
            		( (tenantDomain != null &&
            		!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
            			"/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain :
            			"") +
                    "/registry/resource" +
                    org.wso2.carbon.registry.app.Utils.encodeRegistryPath(path) + version;
        }
        return null;
    }


    public static boolean isSandboxEndpointsExists(API api){
        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(api.getEndpointConfig());

            if(config.containsKey("sandbox_endpoints")){
                return true;
            }
        } catch (ParseException e) {
            log.error("Unable to parse endpoint config JSON", e);
        } catch (ClassCastException e){
            log.error("Unable to parse endpoint config JSON", e);
        }
        return false;
    }

    public static boolean isProductionEndpointsExists(API api){
        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(api.getEndpointConfig());

            if(config.containsKey("production_endpoints")){
                return true;
            }
        } catch (ParseException e) {
            log.error("Unable to parse endpoint config JSON", e);
        } catch (ClassCastException e){
            log.error("Unable to parse endpoint config JSON", e);
        }
        return false;
    }

     /**
       * This method used to get API minimum information from governance artifact
       *
       * @param artifact API artifact
       * @param registry Registry
       * @return API
       * @throws APIManagementException if failed to get API from artifact
     */
     public static API getAPIInformation(GovernanceArtifact artifact, Registry registry)
                                        throws APIManagementException {
        API api;
        try {
        String providerName = artifact.getAttribute(APIConstants.API_OVERVIEW_PROVIDER);
        String apiName = artifact.getAttribute(APIConstants.API_OVERVIEW_NAME);
        String apiVersion = artifact.getAttribute(APIConstants.API_OVERVIEW_VERSION);
        api = new API(new APIIdentifier(providerName, apiName, apiVersion));
        api.setThumbnailUrl(artifact.getAttribute(APIConstants.API_OVERVIEW_THUMBNAIL_URL));
        api.setStatus(getApiStatus(artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS)));
        api.setContext(artifact.getAttribute(APIConstants.API_OVERVIEW_CONTEXT));
        api.setVisibility(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBILITY));
        api.setVisibleRoles(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_ROLES));
        api.setVisibleTenants(artifact.getAttribute(APIConstants.API_OVERVIEW_VISIBLE_TENANTS));
        api.setTransports(artifact.getAttribute(APIConstants.API_OVERVIEW_TRANSPORTS));
        api.setInSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_INSEQUENCE));
        api.setOutSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_OUTSEQUENCE));
        api.setFaultSequence(artifact.getAttribute(APIConstants.API_OVERVIEW_FAULTSEQUENCE));
        api.setDescription(artifact.getAttribute(APIConstants.API_OVERVIEW_DESCRIPTION));
        api.setRedirectURL(artifact.getAttribute(APIConstants.API_OVERVIEW_REDIRECT_URL));
        api.setApiOwner(artifact.getAttribute(APIConstants.API_OVERVIEW_OWNER));
        api.setAdvertiseOnly(Boolean.parseBoolean(artifact.getAttribute(APIConstants.API_OVERVIEW_ADVERTISE_ONLY)));
        } catch (GovernanceException e) {
        String msg = "Failed to get API fro artifact ";
        throw new APIManagementException(msg, e);
        }
        return api;
     }
    /**
     * Get the cache key of the ResourceInfoDTO
     * @param apiContext - Context of the API
     * @param apiVersion - API Version
     * @param resourceUri - The resource uri Ex: /name/version
     * @param httpMethod - The http method. Ex: GET, POST
     * @return - The cache key
     */
    public static String getResourceInfoDTOCacheKey(String apiContext, String apiVersion,
                                                    String resourceUri, String httpMethod){
        return apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;
    }


    /**
     * Get the key of the Resource ( used in scopes)
     * @param api - API
     * @param template - URI Template
     * @return - The resource key
     */
    public static String getResourceKey(API api, URITemplate template){
        return APIUtil.getResourceKey(api.getContext(),api.getId().getVersion(),template.getUriTemplate(),template.getHTTPVerb());
    }

    /**
     * Get the key of the Resource ( used in scopes)
     * @param apiContext - Context of the API
     * @param apiVersion - API Version
     * @param resourceUri - The resource uri Ex: /name/version
     * @param httpMethod - The http method. Ex: GET, POST
     * @return - The resource key
     */
    public static String getResourceKey(String apiContext, String apiVersion,
                                                    String resourceUri, String httpMethod){
        return apiContext + "/" + apiVersion + resourceUri + ":" + httpMethod;
    }

    /**
     * Find scope object in a set based on the key
     * @param scopes - Set of scopes
     * @param key - Key to search with
     * @return Scope - scope object
     */
    public static Scope findScopeByKey(Set<Scope> scopes,String key){
        for(Scope scope:scopes){
            if(scope.getKey().equals(key)){
                return scope;
            }
        }
        return null;
    }

    /**
     * Get the cache key of the APIInfoDTO
     * @param apiContext - Context of the API
     * @param apiVersion - API Version
     * @return - The cache key of the APIInfoDTO
     */
    public static String getAPIInfoDTOCacheKey(String apiContext, String apiVersion){
        return apiContext + "/" + apiVersion;
    }

    /**
     * Get the cache key of the Access Token
     * @param accessToken - The access token which is cached
     * @param apiContext - The context of the API
     * @param apiVersion - The version of the API
     * @param resourceUri - The value of the resource url
     * @param httpVerb - The http method. Ex: GET, POST
     * @param authLevel - Required Authentication level. Ex: Application/Application User
     * @return - The Key which will be used to cache the access token
     */
    public static String getAccessTokenCacheKey(String accessToken, String apiContext, String apiVersion,
                                                String resourceUri, String httpVerb, String authLevel){
        return accessToken + ":" + apiContext + "/" + apiVersion + resourceUri + ":" + httpVerb + ":" + authLevel;
    }

    private static String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
            // property
            // used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals("carbon.home") && propValue != null
                    && propValue.equals(".")) {

                text = new File(".").getAbsolutePath() + File.separator + text;

            }
        }
        return text;
    }

    public static String encryptPassword(String plainTextPassword) throws APIManagementException {
        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(plainTextPassword.getBytes());
        } catch (CryptoException e) {
            String errorMsg = "Error while encrypting the password. " + e.getMessage();
            throw new APIManagementException(errorMsg, e);
        }
    }

    public static Map<Documentation, API> searchAPIsByDoc(Registry registry, int tenantID, String username, String searchTerm, String searchType) throws APIManagementException {
    	List<API> apiSortedList = new ArrayList<API>();

    	Map<Documentation, API> apiDocMap = new HashMap<Documentation, API>();

    	try {
	    	GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
	                APIConstants.API_KEY);
	    	GenericArtifactManager docArtifactManager = APIUtil.getArtifactManager(registry,
	                APIConstants.DOCUMENTATION_KEY);
			SolrClient client =  SolrClient.getInstance();
	    	Map<String, String> fields = new HashMap<String, String>();
			fields.put(APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, "*" + APIConstants.API_ROOT_LOCATION + "*");
			fields.put(APIConstants.DOCUMENTATION_SEARCH_MEDIA_TYPE_FIELD, "*");

			//PaginationContext.init(0, 10000, "ASC", APIConstants.DOCUMENTATION_SEARCH_PATH_FIELD, Integer.MAX_VALUE);

			SolrDocumentList documentList = client.query(searchTerm, tenantID, fields);

			AuthorizationManager manager = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(tenantID).
                    getAuthorizationManager();

			username = MultitenantUtils.getTenantAwareUsername(username);

			for (SolrDocument document : documentList) {
	    		String filePath= (String) document.getFieldValue("path_s");
	    		int index = filePath.indexOf(APIConstants.APIMGT_REGISTRY_LOCATION);
	    		filePath = filePath.substring(index);
	    		Association[] associations = registry.getAllAssociations(filePath);
	    		API api = null;
	    		Documentation doc = null;
	    		for (Association association : associations) {
	    			boolean isAuthorized = false;
	    			String documentationPath = association.getSourcePath();
                    String path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                       RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + documentationPath);
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
		    			isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
		    		} else {
		    			isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
		    		}

		    		if(isAuthorized) {
		    			Resource docResource = registry.get(documentationPath);
			    		String docArtifactId = docResource.getUUID();
			    		if (docArtifactId != null) {
			    			GenericArtifact docArtifact = docArtifactManager.getGenericArtifact(docArtifactId);
			    			doc = APIUtil.getDocumentation(docArtifact);
			    		}

			    		Association[] docAssociations = registry.getAssociations(documentationPath, APIConstants.DOCUMENTATION_ASSOCIATION);
			    		/* There will be only one document association, for a document path which is by its owner API*/
			    		if (docAssociations.length > 0) {
			    			isAuthorized = false;
			    			String apiPath = docAssociations[0].getSourcePath();
                            path = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                 APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                        RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + apiPath);
                            if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(username)) {
				    			isAuthorized = manager.isRoleAuthorized(APIConstants.ANONYMOUS_ROLE, path, ActionConstants.GET);
				    		} else {
				    			isAuthorized = manager.isUserAuthorized(username, path, ActionConstants.GET);
				    		}

				    		if (isAuthorized) {
				                   Resource resource = registry.get(apiPath);
				                   String apiArtifactId = resource.getUUID();
				                   if (apiArtifactId != null) {
				                       GenericArtifact apiArtifact = artifactManager.getGenericArtifact(apiArtifactId);
				                       api = APIUtil.getAPI(apiArtifact, registry);
				                       apiSortedList.add(api);
				                   } else {
				                       throw new GovernanceException("artifact id is null of " + apiPath);
				                   }
				    		}
			    		}
		    		}

	    			if (doc != null && api != null) {
	    				apiDocMap.put(doc, api);
	    			}
	    		}
	    	}
    	} catch(IndexerException e) {
    		handleException("Failed to search APIs with type Doc", e);
    	} catch (RegistryException e) {
			handleException("Failed to search APIs with type Doc", e);
		} catch (UserStoreException e) {
			handleException("Failed to search APIs with type Doc", e);
		}
    	return apiDocMap;
    }


    public static Map<String,Object>  searchAPIsByURLPattern(Registry registry,String searchTerm,int start,int end) throws APIManagementException {
        SortedSet<API> apiSet = new TreeSet<API>(new APINameComparator());
        List<API> apiList = new ArrayList<API>();
        final String searchValue=searchTerm.trim();
        Map<String,Object> result=new HashMap<String, Object>();
        int totalLength=0;
        String criteria;
        Map<String, List<String>> listMap = new HashMap<String, List<String>>();
        GenericArtifact[] genericArtifacts = new GenericArtifact[0];
        GenericArtifactManager artifactManager = null;
        try {
        artifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
        PaginationContext.init(0, 10000, "ASC", APIConstants.API_OVERVIEW_NAME, Integer.MAX_VALUE);
        if (artifactManager != null) {
        for(int i=0;i<20;i++){ //This need to fix in future.We don't have a way to get max value of "url_template" entry stores in registry,unless we search in each API
            criteria=APIConstants.API_URI_PATTERN+i;
            listMap.put(criteria, new ArrayList<String>() {{
                add(searchValue);
            }});
            genericArtifacts = (GenericArtifact[]) ArrayUtils.addAll(genericArtifacts, artifactManager.findGenericArtifacts(listMap));

        }

        if (genericArtifacts == null || genericArtifacts.length == 0) {

            result.put("apis",apiSet);
            result.put("length",0);
            return result;
        }
        totalLength = genericArtifacts.length;
        StringBuilder apiNames=new StringBuilder();
        for (GenericArtifact artifact : genericArtifacts) {
            if(apiNames.indexOf(artifact.getAttribute(APIConstants.API_OVERVIEW_NAME))<0){
            String status = artifact.getAttribute(APIConstants.API_OVERVIEW_STATUS);
            if (isAllowDisplayAPIsWithMultipleStatus()) {
                if (status.equals(APIConstants.PUBLISHED) || status.equals(APIConstants.DEPRECATED)) {
                    API api=APIUtil.getAPI(artifact, registry);
                    apiList.add(api);
                    apiNames.append(api.getId().getApiName());
                }
            } else {
                if (status.equals(APIConstants.PUBLISHED)) {
                    API api=APIUtil.getAPI(artifact, registry);
                    apiList.add(api);
                    apiNames.append(api.getId().getApiName());
                }
            }
            }
            totalLength=apiList.size();

        }
        if(totalLength<=((start+end)-1)){
            end=totalLength;
        }
        for(int i=start;i<end;i++){
            apiSet.add(apiList.get(i));


        }

        }
        } catch (APIManagementException e) {
            handleException("Failed to search APIs with input url-pattern", e);
        } catch (GovernanceException e) {
            handleException("Failed to search APIs with input url-pattern", e);
        }
        result.put("apis",apiSet);
        result.put("length",totalLength);
        return result;

    }



    /**
     * This method will check the validity of given url. WSDL url should be
     * contain http, https or file system patch
     * otherwise we will mark it as invalid wsdl url. How ever here we do not
     * validate wsdl content.
     * 
     * @param wsdlURL
     *            wsdl url tobe tested
     * @return true if its valid url else fale
     */
    public static boolean isValidWSDLURL(String wsdlURL, boolean required) {
        if ((wsdlURL != null && !"".equals(wsdlURL))) {
            if ((wsdlURL.contains("http:") | wsdlURL.contains("https:") | wsdlURL.contains("file:"))) {
                return true;
            }
        } else if (!required) {
            // If the WSDL in not required and URL is empty, then we don't need
            // to add debug log.
            // Hence returning.
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("WSDL url validation failed. Provided wsdl url is not valid url: " + wsdlURL);
        }
        return false;

    }

    /**
     * load tenant axis configurations.
     * @param tenantDomain
     */
	public static void loadTenantConfig(String tenantDomain) {

		try {
			ConfigurationContext ctx = ServiceReferenceHolder.getContextService().getServerConfigContext();
			TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, ctx);
		} catch (Exception e) {
			log.error("Error while creating axis configuration for tenant " + tenantDomain, e);
		}

	}

    public static void checkClientDomainAuthorized(APIKeyValidationInfoDTO apiKeyValidationInfoDTO, String clientDomain)
            throws APIManagementException {
        if (clientDomain != null) {
            clientDomain = clientDomain.trim();
        }
        List<String> authorizedDomains = apiKeyValidationInfoDTO.getAuthorizedDomains();
        if (authorizedDomains != null && !(authorizedDomains.contains("ALL") || authorizedDomains.contains(clientDomain)
        )) {
            log.error("Unauthorized client domain :" + clientDomain +
                      ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
            throw new APIManagementException("Unauthorized client domain :" + clientDomain +
                                             ". Only \"" + authorizedDomains + "\" domains are authorized to access the API.");
        }

    }

    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     *
     * @param registryContext Registry Context instance which holds path mappings
     * @param path            default path of the registry
     * @return mounted path or path
     */
    public static String extractCustomerKeyFromAuthHeader(Map headersMap) {

        //From 1.0.7 version of this component onwards remove the OAuth authorization header from
        // the message is configurable. So we dont need to remove headers at this point.
        String authHeader = (String) headersMap.get(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return null;
        }

        if (authHeader.startsWith("OAuth ") || authHeader.startsWith("oauth ")) {
            authHeader = authHeader.substring(authHeader.indexOf("o"));
        }

        String[] headers = authHeader.split(APIConstants.OAUTH_HEADER_SPLITTER);
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split(APIConstants.CONSUMER_KEY_SEGMENT_DELIMITER);
                if (elements != null && elements.length > 1) {
                    int j = 0;
                    boolean isConsumerKeyHeaderAvailable = false;
                    for (String element : elements) {
                        if (!"".equals(element.trim())) {
                            if (APIConstants.CONSUMER_KEY_SEGMENT.equals(elements[j].trim())) {
                                isConsumerKeyHeaderAvailable = true;
                            } else if (isConsumerKeyHeaderAvailable) {
                                return removeLeadingAndTrailing(elements[j].trim());
                            }
                        }
                        j++;
                    }
                }
            }
        }
        return null;
    }
    private static String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }
    
    /**
     * This method will return mounted path of the path if the path
     * is mounted. Else path will be returned.
     * 
     * @param registryContext
     *            Registry Context instance which holds path mappings
     * @param path
     *            default path of the registry
     * @return mounted path or path
     */
    public static String getMountedPath(RegistryContext registryContext, String path) {
        if (registryContext != null && path != null) {
            List<Mount> mounts = registryContext.getMounts();
            if (mounts != null) {
                for (Mount mount : mounts) {
                    if (path.equals(mount.getPath())) {
                        return mount.getTargetPath();
                    }
                }
            }
        }
        return path;
    }

    /**
     * Returns a map of gateway domains for the tenant
     *
     * @return a Map of domain names for tenant
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if an error occurs when loading tiers from the registry
     */
    public static Map<String, String> getDomainMappings(String tenantDomain) throws APIManagementException {
        Map<String, String> domains = new HashMap<String, String>();
        String resourcePath;
        try {
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().
                    getGovernanceSystemRegistry();
            resourcePath = APIConstants.API_DOMAIN_MAPPINGS.replace("<tenant-id>",tenantDomain);
            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                String content = new String((byte[]) resource.getContent());
                JSONParser parser = new JSONParser();
                JSONObject mappings = (JSONObject) parser.parse(content);
                if(mappings.get("gateway") != null) {
                    mappings = (JSONObject) mappings.get("gateway");
                    Iterator entries = mappings.entrySet().iterator();
                    while (entries.hasNext()) {
                        Entry thisEntry = (Entry) entries.next();
                        String key = (String) thisEntry.getKey();
                        String value = (String) thisEntry.getValue();
                        domains.put(key, value);
                    }
                }
            }
        } catch (RegistryException e) {
            String msg = "Error while retrieving gateway domain mappings from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ClassCastException e) {
            String msg = "Invalid JSON found in the gateway tenant domain mappings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "Malformed JSON found in the gateway tenant domain mappings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return domains;
    }

    /**
     * This method used to Downloaded Uploaded Documents from publisher
     *
     * @param userName     logged in username
     * @param resourceUrl  resource want to download
     * @param tenantDomain loggedUserTenantDomain
     * @return map that contains Data of the resource
     * @throws APIManagementException
     */

    public static Map<String, Object> getDocument(String userName, String resourceUrl)
            throws APIManagementException {
        Map<String, Object> documentMap = new HashMap<String, Object>();

        InputStream inStream = null;
        String[] resourceSplitPath =
                resourceUrl.split(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
        if (resourceSplitPath.length == 2) {
            resourceUrl = resourceSplitPath[1];
        } else {
            handleException("Invalid resource Path " + resourceUrl);
        }
        Resource apiDocResource;
        Registry registryType;
        int tenantId;
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        try {
           
                tenantId = ServiceReferenceHolder
                        .getInstance().getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
                userName = MultitenantUtils.getTenantAwareUsername(userName);
            registryType = ServiceReferenceHolder
                    .getInstance().
                            getRegistryService().getGovernanceUserRegistry(userName, tenantId);
            if (registryType.resourceExists(resourceUrl)) {
                apiDocResource = registryType.get(resourceUrl);
                inStream = apiDocResource.getContentStream();
                documentMap.put("Data", inStream);
                documentMap.put("contentType", apiDocResource.getMediaType());
                String[] content = apiDocResource.getPath().split("/");
                documentMap.put("name", content[content.length - 1]);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Couldn't retrieve Tenant Domain for User " + userName, e);
            handleException("Couldn't retrieve Tenant Domain for User " + userName, e);

        } catch (RegistryException e) {
            log.error("Couldn't retrieve registry for User " + userName + " Tenant " + tenantDomain,
                      e);
            handleException(
                    "Couldn't retrieve registry for User " + userName + " Tenant " + tenantDomain,
                    e);
        }
        return documentMap;
    }

    /**
     * this method used to set environments values to api object.
     *
     * @param environments environments values in json format
     * @return set of environments that Published
     */
    public static Set<String> extractEnvironmentsForAPI(String environments) {
        Set<String> environmentStringSet = null;
        if (environments == null) {
            environmentStringSet = new HashSet<String>(
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                          .getAPIManagerConfiguration().getApiGatewayEnvironments().keySet());
        } else {
            //handle not to publish to any of the gateways
            if ("none".equals(environments)) {
                environmentStringSet = new HashSet<String>();
            }
            //handle to set published gateways nto api object
            else if (!"".equals(environments)) {
                String[] publishEnvironmentArray = environments.split(",");
                environmentStringSet = new HashSet<String>(Arrays.asList(publishEnvironmentArray));
                environmentStringSet.remove("none");
            }
            //handle to publish to any of the gateways when api creating stage
            else if ("".equals(environments)) {
                environmentStringSet = new HashSet<String>(
                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                              .getAPIManagerConfiguration().getApiGatewayEnvironments().keySet());
            }
        }
        return environmentStringSet;
    }

    /**
     * This method used to set environment values to governance artifact of API .
     *
     * @param api      API object with the attributes value
     * @throws GovernanceException
     */
    public static String writeEnvironmentsToArtifact( API api){
        StringBuilder publishedEnvironments = new StringBuilder();
        Set<String> apiEnvironments = api.getEnvironments();
        if (apiEnvironments != null) {

            if (apiEnvironments != null) {
                for (String environmentName : apiEnvironments) {
                    publishedEnvironments.append(environmentName + ",");
                }
                if(apiEnvironments.isEmpty()) {
                    publishedEnvironments.append("none,");
                }
            }
            if (!publishedEnvironments.toString().isEmpty()) {
                publishedEnvironments.deleteCharAt(publishedEnvironments.length() - 1);
            }
        }
        return publishedEnvironments.toString();
    }
  /**
     * Given the apps and the application name to check for, it will check if the application already exists.
     * 
     * @param apps The collection of applications
     * @param name The application to be checked if exists
     * @return true - if an application of the name <name> already exists in the collection <apps>
     *         false-  if an application of the name <name>  does not already exists in the collection <apps> 
     */
    public static boolean doesApplicationExist(Application[] apps, String name){
    	boolean doesApplicationExist = false;
        if (apps != null) {
            for (Application app : apps) {
                if (app.getName().equals(name)) {
                	doesApplicationExist = true;
                }
            }
        }
        return doesApplicationExist;
    }

    /**
     * Read the group id extractor class reference from api-manager.xml.
     *
     * @return group id extractor class reference.
     */
    public static String getGroupingExtractorImplementation() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();

        String gropingExtractorClass = config.getFirstProperty(APIConstants.API_STORE_GROUP_EXTRACTOR_IMPLEMENTATION);
        return gropingExtractorClass;
	}
    /**
     * This method will update the permission cache of the tenant which is related to the given usename
     *
     * @param username User name to find the relevant tenant
     * @throws UserStoreException if the permission update failed
     */
    public static void updatePermissionCache(String username) throws UserStoreException {
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getTenantId(tenantDomain);
        PermissionUpdateUtil.updatePermissionTree(tenantId);
    }

	/**
     * Check whether given application name is available under current subscriber or group
     *
     * @param subscriber      subscriber name
     * @param applicationName application name
     * @param groupId         group of the subscriber
     * @return true if application is available for the subscriber
     * @throws APIManagementException if failed to get applications for given subscriber
     */
    public static boolean isApplicationExist(String subscriber, String applicationName, String groupId)
            throws APIManagementException {
        return ApiMgtDAO.isApplicationExist(applicationName, subscriber, groupId);
    }

    // Start of APIProvider related methods.

    /**
     * Get Documentation type
     *
     * @return docType DocumentationType
     */
    public static DocumentationType getDocType(String docType) {
        DocumentationType docsType = null;
        for (DocumentationType type : DocumentationType.values()) {
            if (type.getType().equalsIgnoreCase(docType)) {
                docsType = type;
            }
        }
        return docsType;
    }

    /**
     * Get WebContext Root
     *
     * @return postfixUrl String
     */
    public static String getWebContextRoot(String postfixUrl) {
        String webContext = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
        if (StringUtils.isNotBlank(postfixUrl) && StringUtils.isNotBlank(webContext) && !webContext.equals("/")) {
            postfixUrl = webContext + postfixUrl;
        }
        return postfixUrl;
    }

    /**
     * Is API Doc can show
     *
     * @return bool
     */
    // TODO: method signature should changed as isAPIDocCanShow()
    public boolean showAPIDocVisibility() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            return Boolean
                    .parseBoolean(config.getFirstProperty(APIConstants.API_PUBLISHER_ENABLE_API_DOC_VISIBILITY_LEVELS));
        } else {
            return false;
        }
    }

    /**
     * Is API store URL can show
     *
     * @return bool
     */
    // TODO: method signature should changed as isAPIStoreURLCanShow()
    public static boolean showAPIStoreURL() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            return Boolean.parseBoolean(config.getFirstProperty(APIConstants.SHOW_API_STORE_URL_FROM_PUBLISHER));
        } else {
            return false;
        }
    }

    /**
     * Get the API store URL
     *
     * @return url String
     */
    public static String getAPIStoreURL() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            return config.getFirstProperty(APIConstants.API_STORE_URL);
        } else {
            return null;
        }
    }

    /**
     * Get the authenticator server URL
     *
     * @return url String
     * @throws APIManagementException If API key manager URL unspecified
     */
    public static String getAuthServerURL() throws APIManagementException {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        if (StringUtils.isBlank(url)) {
            handleException("API key manager URL unspecified");
        }
        return url;
    }

    /**
     * This method will return gateway endpoints in a comma separated string.
     *
     * @param api API will used to get published environments
     * @return endpoints comma separated string
     * @throws APIManagementException If API key manager URL unspecified
     */
    public static String getGatewayEndpoints(API api) throws APIManagementException {
        StringBuilder endpoints = new StringBuilder();
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String publishedEnvironments = writeEnvironmentsToArtifact(api);
        String[] publishedEnvironmentSet = publishedEnvironments.split(",");
        for (int i = 0; i < config.getApiGatewayEnvironments().keySet().toArray().length; i++) {
            for (int j = 0; j < publishedEnvironmentSet.length; j++) {
                if (config.getApiGatewayEnvironments().keySet().toArray()[i].toString()
                        .equals(publishedEnvironmentSet[j])) {
                    Environment environment = (Environment) config.getApiGatewayEnvironments().values().toArray()[i];
                    endpoints.append(publishedEnvironmentSet[j] + ","+ environment.getApiGatewayEndpoint()+ ",");
                    break;
                }
            }
        }
        if (endpoints.toString().isEmpty()) {
            handleException("Endpoints are unspecified or No matching environments found!");
        } else {
            endpoints.deleteCharAt(endpoints.length() - 1);
        }
        return endpoints.toString();
    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port int
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (APIManagementException e) {
            log.error("Error occurred while getting config context.", e);
            return null;

        }
    }

    /**
     * Get the running HTTPS URL
     *
     * @return "https://" + hostName + ":" + backendHttpsPort String
     */
    public static String getHTTPsURL() {
        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty(APIConstants.PROPERTY_HOSTNAME);
        String backendHttpsPort = getBackendPort(APIConstants.PROTOCOL_HTTPS);
        if (hostName == null) {
            hostName = System.getProperty(APIConstants.CARBON_LOCALIP);
        }
        return "https://" + hostName + ":" + backendHttpsPort;

    }

    /**
     * Evaluate HTTP end-point URI to validate path parameter and query
     * parameter formats<br>
     * Sample URI format<br>
     * http[s]//[www.]anyhost[.com][:port]/{uri.var.param}?param1=value&param2={uri.var.value}
     *
     * @param endpointConfig JSON representation of end-point configuration.
     * @return true if valid URI
     * @throws APIManagementException If the endpointConfig is invalid or URI is invalid
     */
    public static boolean validateEndpointURI(String endpointConfig) throws APIManagementException {
	    if (endpointConfig != null) {
		    try {
			    JSONParser parser = new JSONParser();
			    JSONObject jsonObject = (JSONObject) parser.parse(endpointConfig);
			    Object epType = jsonObject.get("endpoint_type");
			    if (epType instanceof String && "http".equals(epType)) {
				    // extract production uri from config
				    Object prodEPs = (JSONObject) jsonObject.get("production_endpoints");
				    if (prodEPs instanceof JSONObject) {
					    Object url = ((JSONObject) prodEPs).get("url");
					    if (url instanceof String && !isValidURI(url.toString())) {
						    handleException("Invalid Production Endpoint URI. Please refer HTTP Endpoint " +
						                    "documentation of the WSO2 ESB for details.");
					    }
				    }
				    // extract sandbox uri from config
				    Object sandEPs = (JSONObject) jsonObject.get("sandbox_endpoints");
				    if (sandEPs instanceof JSONObject) {
					    Object url = ((JSONObject) sandEPs).get("url");
					    if (url instanceof String && !isValidURI(url.toString())) {
						    handleException("Invalid Sandbox Endpoint URI. Please refer HTTP Endpoint " +
						                    "documentation of the WSO2 ESB for details.");
					    }
				    }
			    }
		    } catch (ParseException e) {
			    handleException("Invalid Endpoint config", e);
		    }
	    }
	    return true;
    }

    /**
     * This method returns whether the given url is contain valid uri params or not
     *
     * @param url URL to be validated
     * @return true if URI doesn't contain params or contains valid params
     */
    private static boolean isValidURI(String url) {
	    boolean isInvalid = false;
	    // validate only if uri contains { or }
	    if (url != null && (url.contains("{") || url.contains("}"))) {
		    // check { and } are matched or not. otherwise invalid
		    int startCount = 0, endCount = 0;
		    for (char c : url.toCharArray()) {
			    if (c == '{') {
				    startCount++;
			    } else if (c == '}') {
				    endCount++;
			    }
			    // this check guarantee the order of '{' and '}'. Ex: {uri.var.name} not }uri.var.name{
			    if (endCount > startCount) {
				    isInvalid = true;
				    break;
			    }
		    }
		    // continue only if the matching no of brackets are found. otherwise invalid
		    if (startCount == endCount) {
			    // extract content including { } brackets
			    Matcher pathParamMatcher = pathParamExtractorPattern.matcher(url);
			    while (pathParamMatcher.find()) {
				    // validate the format of { } content
				    Matcher formatMatcher = pathParamValidatorPattern.matcher(pathParamMatcher.group());
				    if (!formatMatcher.matches()) {
					    isInvalid = true;
					    break;
				    }
			    }
		    } else {
			    isInvalid = true;
		    }
	    }
	    return !isInvalid;
    }

    // End of APIProvider related methods.

    // Start of APIStore related methods.

    /**
     * This method returns application access token validity period in seconds
     *
     * @return Access token validity period in seconds, this is a long value
     */
    public static long getApplicationAccessTokenValidityPeriodInSeconds() {
        return OAuthServerConfiguration.getInstance().getApplicationAccessTokenValidityPeriodInSeconds();
    }

    /**
     * This method returns bool expression whether access token get expired later or not
     *
     * @return boolean
     */
    public static boolean isApplicationAccessTokenNeverExpire(long validityPeriod) {
        return validityPeriod == Long.MAX_VALUE;
    }

    /**
     * This method returns filtered URL list by transport
     *
     * @param apiData
     * @param transports
     * @return urlString or apiData if apiData == null
     */
    public static String filterUrls(String apiData, String transports) {
        if (apiData != null && transports != null) {
            List<String> urls = new ArrayList<String>();
            List<String> transportList = new ArrayList<String>();
            urls.addAll(Arrays.asList(apiData.split(",")));
            transportList.addAll(Arrays.asList(transports.split(",")));
            urls = filterUrlsByTransport(urls, transportList, APIConstants.PROTOCOL_HTTPS);
            urls = filterUrlsByTransport(urls, transportList, APIConstants.PROTOCOL_HTTP);
            String urlString = urls.toString();
            return urlString.substring(1, urlString.length() - 1);
        }
        return apiData;
    }

    /**
     * This method returns filtered URL list by transport
     * this is for use in filterUrls(String apiData, String transports) function
     *
     * @param urlsList
     * @param transportList
     * @return urlsList
     */
    private static List<String> filterUrlsByTransport(List<String> urlsList, List<String> transportList,
            String transportName) {
        if (!transportList.contains(transportName)) {
            ListIterator<String> it = urlsList.listIterator();
            while (it.hasNext()) {
                String url = it.next();
                if (url.startsWith(transportName + ":")) {
                    it.remove();
                }
            }
            return urlsList;
        }
        return urlsList;
    }

    /**
     * This method returns app key of the relevant app and key type.
     *
     * @param app
     * @param keyType
     * @return getKeyOfType(apiKeys, keyType)
     */
    public static APIKey getAppKey(Application app, String keyType) {
        List<APIKey> apiKeys = app.getKeys();
        return getKeyOfType(apiKeys, keyType);
    }

    /**
     * This method returns the key of the relevant key type.
     *
     * @param apiKeys
     * @param keyType
     * @return key
     */
    private static APIKey getKeyOfType(List<APIKey> apiKeys, String keyType) {
        for (APIKey key : apiKeys) {
            if (keyType.equals(key.getType())) {
                return key;
            }
        }
        return null;
    }

    /**
     * This method returns the allowed scopes for user application.
     *
     * @param username
     * @param reqScopeSet
     * @return authorizedScopes
     */
    public static List<Scope> getAllowedScopesForUserApplication(String username, Set<Scope> reqScopeSet) {
        String[] userRoles = null;
        List<Scope> authorizedScopes = new ArrayList<Scope>();
        List<String> userRoleList = null;
        try {
            userRoles = getListOfRoles(username);
        } catch (APIManagementException e) {
            log.error("Error while getting the roles for user", e);
        }

        if (userRoles != null && userRoles.length > 0) {
            userRoleList = new ArrayList<String>(Arrays.asList(userRoles));
        } else {
            log.error("No user roles were defined for " + username + "!");
        }

        // Iterate the requested scopes list.
        for (Scope scope : reqScopeSet) {
            // Get the set of roles associated with the requested scope.
            String roles = scope.getRoles();

            // If the scope has been defined in the context of the App and if roles have been defined for the scope
            if (roles != null && roles.length() != 0) {
                List<String> roleList = new ArrayList<String>(Arrays.asList(roles.replaceAll(" ", "").split(",")));
                //Check if user has at least one of the roles associated with the scope
                roleList.retainAll(userRoleList);
                if (!roleList.isEmpty()) {
                    authorizedScopes.add(scope);
                }
            }
        }

        return authorizedScopes;
    }

    /**
     * This method returns the scope names by key.
     *
     * @param scopeKey
     * @param availableScopeSet
     * @return prodKeyScope
     */
    public static String getScopeNamesByKey(String scopeKey, Set<Scope> availableScopeSet) {
        //convert scope keys to names
        StringBuilder scopeBuilder = new StringBuilder("");
        String prodKeyScope;

        if (APIConstants.OAUTH2_DEFAULT_SCOPE.equals(scopeKey)) {
            scopeBuilder.append("Default  ");
        } else {
            List<String> inputScopeList = new ArrayList<String>(Arrays.asList(scopeKey.split(" ")));
            String scopeName = "";
            for (String inputScope : inputScopeList) {
                for (Scope availableScope : availableScopeSet) {
                    if (availableScope.getKey().equals(inputScope)) {
                        scopeName = availableScope.getName();
                        break;
                    }
                }
                scopeBuilder.append(scopeName);
                scopeBuilder.append(", ");
            }
        }
        prodKeyScope = scopeBuilder.toString();
        prodKeyScope = prodKeyScope.substring(0, prodKeyScope.length() - 2);
        return prodKeyScope;
    }

    /**
     * This method is to check whether billing enabled or not.
     *
     * @return true if billing enabled else false
     */
    public static boolean isBillingEnabled() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String billingConfig = config.getFirstProperty(APIConstants.BILLING_AND_USAGE_CONFIGURATION);
        return Boolean.parseBoolean(billingConfig);
    }

    /**
     * Get the API publisher URL
     *
     * @return url String
     */
    public static String getAPIPublisherURL() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            return config.getFirstProperty(APIConstants.API_PUBLISHER_URL);
        }
        return null;
    }

    /**
     * This method check email username is enable or not
     *
     * @return bool
     */
    public static boolean isEnableEmailUsername() {
        return Boolean.parseBoolean(CarbonUtils.getServerConfiguration().getFirstProperty("EnableEmailUserName"));
    }

    public static boolean hasPublisherAccess(String username) {
        String usernameWithDomain = getUserNameWithTenantSuffix(username);
        String tenantDomain = MultitenantUtils.getTenantDomain(usernameWithDomain);
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            usernameWithDomain = usernameWithDomain + APIConstants.EMAIL_DOMAIN_SEPARATOR + tenantDomain;
        }
        boolean displayPublishUrlFromStore = false;
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (config != null) {
            displayPublishUrlFromStore = Boolean
                    .parseBoolean(config.getFirstProperty(APIConstants.SHOW_API_PUBLISHER_URL_FROM_STORE));
        }
        boolean loginUserHasPublisherAccess = false;
        if (displayPublishUrlFromStore) {
            loginUserHasPublisherAccess =
                    checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_CREATE)
                            || checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_PUBLISH);
        }
        return loginUserHasPublisherAccess;
    }

    /**
     * This method check whether user permissions
     *
     * @return bool
     */
    public static boolean hasUserPermissions(String username) {
        String usernameWithDomain = getUserNameWithTenantSuffix(username);
        return checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_CREATE)
                || checkPermissionQuietly(usernameWithDomain, APIConstants.Permissions.API_PUBLISH);
    }

    // End of APIStore related methods.

    public static String checkValue(String input) {
        return input != null ? input : "";
    }

    public static String checkTransport(String compare, String transport)
            throws APIManagementException {
        if (transport != null) {
            List<String> transportList = new ArrayList<String>();
            transportList.addAll(Arrays.asList(transport.split(",")));
            if (transportList.contains(compare)) {
                return "checked";
            } else {
                return "";
            }

        } else {
            return "";
        }
    }

	public static String getTransports(JSONObject apiData) {
		String transportStr = String.valueOf(apiData.get("transports"));
		String transport  = transportStr;
		if (transportStr != null) {
			if ((transportStr.indexOf(",") == 0) || (transportStr.indexOf(",") == (transportStr.length()-1))) {
				transport =transportStr.replace(",","");
			}
		}
		return transport;
	}
	
	public static UserRegistry getRootRegistry() throws RegistryException{
	   	 RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();	   	
	     UserRegistry govRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
	     return govRegistry;
	}
	   
	
	public static UserRegistry getSystemConfigRegistry(int tenantID) throws RegistryException{
   	 RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry govRegistry = registryService.getConfigSystemRegistry(tenantID);
        return govRegistry;
   }
   
   private static byte[] getResourceData(final String relativePath) throws IOException{
   	InputStream inputStream = APIManagerComponent.class.getResourceAsStream(relativePath);
       byte[] data = IOUtils.toByteArray(inputStream);
       return data;
   }

	/**
	 *This methos is to check whether stat publishing is enabled
	 * @return boolean
	 */
	public static boolean checkDataPublishingEnabled() {
		APIManagerAnalyticsConfiguration analyticsConfiguration =
				ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIAnalyticsConfiguration();
		return analyticsConfiguration.isAnalyticsEnabled();
	}

	/**
	 * This method will clear recently added API cache.
	 * @param username
	 */
	public static void invalidateRecentlyAddedAPICache(String username){
		try{
			PrivilegedCarbonContext.startTenantFlow();
			APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
																	getAPIManagerConfiguration();
			boolean isRecentlyAddedAPICacheEnabled =
					Boolean.parseBoolean(config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE));

			if (username != null && isRecentlyAddedAPICacheEnabled) {
				String tenantDomainFromUserName = MultitenantUtils.getTenantDomain(username);
				if (tenantDomainFromUserName != null &&
				    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomainFromUserName)) {
					PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomainFromUserName,
							true);
				} else {
					PrivilegedCarbonContext.getThreadLocalCarbonContext()
							.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
				}
				Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache("RECENTLY_ADDED_API")
						.remove(username + ":" + tenantDomainFromUserName);
			}
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	public static  boolean isUsageDataSourceSpecified() {
		APIManagerConfiguration configuration =
				ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();

		return (null != configuration.getFirstProperty(APIConstants.API_USAGE_DATA_SOURCE_NAME));
	}

	public static boolean isStatPublishingEnabled() {
		return ServiceReferenceHolder.getInstance().
				getAPIManagerConfigurationService().getAPIAnalyticsConfiguration().isAnalyticsEnabled();
	}

	/**
	 * Validate the backend by sending HTTP HEAD
	 *
	 * @param urlVal - backend URL
	 * @return - status of HTTP HEAD Request to backend
	 */
	public static String sendHttpHEADRequest(String urlVal) {

		String response = "error while connecting";

		HttpClient client = new DefaultHttpClient();
		HttpHead head = new HttpHead(urlVal);
		client.getParams().setParameter("http.socket.timeout", 4000);
		client.getParams().setParameter("http.connection.timeout", 4000);


		if (System.getProperty(APIConstants.HTTP_PROXY_HOST) != null &&
		    System.getProperty(APIConstants.HTTP_PROXY_PORT) != null) {
			if (log.isDebugEnabled()) {
				log.debug("Proxy configured, hence routing through configured proxy");
			}
			String proxyHost = System.getProperty(APIConstants.HTTP_PROXY_HOST);
			String proxyPort = System.getProperty(APIConstants.HTTP_PROXY_PORT);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					new HttpHost(proxyHost, new Integer(proxyPort)));
		}

		try {
			HttpResponse httpResponse = client.execute(head);
			int statusCode = httpResponse.getStatusLine().getStatusCode();

			//If the endpoint doesn't support HTTP HEAD or if status code is < 400
			if (statusCode == 405 || statusCode / 100 < 4) {
				if (log.isDebugEnabled() && statusCode == 405) {
					log.debug("Endpoint doesn't support HTTP HEAD");
				}
				response = "success";
			}
		} catch (IOException e) {
			// sending a default error message.
			log.error("Error occurred while connecting backend : " + urlVal + ", reason : " + e.getMessage());
		} finally {
			client.getConnectionManager().shutdown();
		}
		return response;
	}

	public static void validateWsdl(String url) throws Exception {

		URL wsdl = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(wsdl.openStream()));
		String inputLine;
		boolean isWsdl2 = false;
		boolean isWsdl10 = false;
		StringBuilder urlContent = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
			String wsdl10NameSpace = "http://schemas.xmlsoap.org/wsdl/";
			urlContent.append(inputLine);
			isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
			isWsdl10 = urlContent.indexOf(wsdl10NameSpace) > 0;
		}
		in.close();
		if (isWsdl10) {
			javax.wsdl.xml.WSDLReader wsdlReader11 = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
			wsdlReader11.readWSDL(url);
		} else if (isWsdl2) {
			WSDLReader wsdlReader20 = WSDLFactory.newInstance().newWSDLReader();
			wsdlReader20.readWSDL(url);
		} else {
			handleException("URL is not in format of wsdl1/wsdl2");
		}

	}

	public boolean resourceMethodMatches(String[] resourceMethod1,
	                                      String[] resourceMethod2) {
		for (String m1 : resourceMethod1) {
			for (String m2 : resourceMethod2) {
				if (m1.equals(m2)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param failedGateways map of failed environments
	 * @return json string of input map
	 */
	public static String createFailedGatewaysAsJsonString(Map<String, List<String>> failedGateways) {
		String failedJson = "{\"PUBLISHED\" : \"\" ,\"UNPUBLISHED\":\"\"}";
		if (failedGateways != null) {
			if (!failedGateways.isEmpty()) {
				StringBuilder failedToPublish = new StringBuilder();
				StringBuilder failedToUnPublish = new StringBuilder();
				for (String environmentName : failedGateways.get("PUBLISHED")) {
					failedToPublish.append(environmentName + ",");
				}
				for (String environmentName : failedGateways.get("UNPUBLISHED")) {
					failedToUnPublish.append(environmentName + ",");
				}
				if (!"".equals(failedToPublish.toString())) {
					failedToPublish.deleteCharAt(failedToPublish.length() - 1);
				}
				if (!"".equals(failedToUnPublish.toString())) {
					failedToUnPublish.deleteCharAt(failedToUnPublish.length() - 1);
				}
				failedJson = "{\"PUBLISHED\" : \"" + failedToPublish.toString() + "\" ,\"UNPUBLISHED\":\"" +
				             failedToUnPublish.toString() + "\"}";
			}
		}
		return failedJson;
	}

	public static String userAgentParser(String userAgent){
		String userBrowser;
		if(userAgent.contains("Chrome")){
			userBrowser = "Chrome";
		}
		else if(userAgent.contains("Firefox")){
			userBrowser = "Firefox";
		}
		else if(userAgent.contains("Opera")){
			userBrowser = "Opera";
		}
		else if(userAgent.contains("MSIE")){
			userBrowser = "Internet Explorer";
		}
		else{
			userBrowser = "Other";
		}
		return userBrowser;
	}

	public static boolean isStringValues(Object[] args) {
		int i = 0;
		for (Object arg : args) {

			if (!(arg instanceof String)) {
				return false;

			}
			i++;
		}
		return true;
	}

	public static int getSubscriberCount(Set<Subscriber> subs)
			throws APIManagementException {
		Set<String> subscriberNames = new HashSet<String>();
		if (subs != null) {
			for (Subscriber sub : subs) {
				subscriberNames.add(sub.getName());
			}
			return subscriberNames.size();
		} else {
			return 0;
		}
	}

	public static String getTransports(String transportStr) {
		String transport  = transportStr;
		if (transportStr != null) {
			if ((transportStr.indexOf(",") == 0) || (transportStr.indexOf(",") == (transportStr.length()-1))) {
				transport =transportStr.replace(",","");
			}
		}
		return transport;
	}

	public static String checkAndSetVersionParam(String context) {
		// This is to support the new Pluggable version strategy
		// if the context does not contain any {version} segment, we use the default version strategy.
		if(!context.contains(VERSION_PARAM)){
			if(!context.endsWith("/")){
				context = context + "/";
			}
			context = context + VERSION_PARAM;
		}
		return context;
	}

	private static void checkFileSize(File file)
			throws ScriptException, APIManagementException {
		if (file != null) {
			long length = file.length();
			if (length / 1024.0 > 1024) {
				handleException("Image file exceeds the maximum limit of 1MB");
			}
		}
	}

	public static String updateContextWithVersion(String version, String contextVal, String context) {
		// This condition should not be true for any occasion but we keep it so that there are no loopholes in
		// the flow.
		if (version == null) {
			// context template patterns - /{version}/foo or /foo/{version}
			// if the version is null, then we remove the /{version} part from the context
			context = contextVal.replace("/" + VERSION_PARAM, "");
		}else{
			context = context.replace(VERSION_PARAM, version);
		}
		return context;
	}

	public static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

    public JSONObject stringifyAPISubscriptions(Map<String, Object> subscriptions) {
        List<APISubscription> subs = (List<APISubscription>) subscriptions.get("applications");
        int subscriptionCount = (Integer) subscriptions.get("totalLength");
        JSONArray applicationList = new JSONArray();
        JSONObject result = new JSONObject();
        if (subs != null) {
            for (APISubscription sub : subs) {
                JSONObject appObj = new JSONObject();
                appObj.put("id", sub.getAppId());
                appObj.put("name", sub.getAppName());
                appObj.put("callbackUrl", sub.getCallbackUrl());
                appObj.put("prodKey", sub.getProdKey());
                appObj.put("prodKeyScope", sub.getProdKeyScope());
                appObj.put("prodKeyScopeValue", sub.getProdKeyScopeValue());
                appObj.put("prodConsumerKey", sub.getProdConsumerKey());
                appObj.put("prodConsumerSecret", sub.getProdConsumerSecret());
                appObj.put("prodJsonString", sub.getProdJsonString());
                appObj.put("prodAuthorizedDomains", sub.getProdAuthorizedDomains());
                appObj.put("prodValidityTime", sub.getProdValidityTime());
                appObj.put("prodRegenerateOption", sub.isProdRegenerateOption());
                appObj.put("prodKeyState", sub.getProdKeyState());
                appObj.put("sandboxKey", sub.getSandKey());
                appObj.put("sandKeyScope", sub.getSandKeyScope());
                appObj.put("sandKeyScopeValue", sub.getSandKeyScopeValue());
                appObj.put("sandboxConsumerKey", sub.getSandConsumerKey());
                appObj.put("sandboxConsumerSecret", sub.getSandConsumerSecret());
                appObj.put("sandboxKeyState", sub.getSandKeyState());
                appObj.put("sandboxJsonString", sub.getSandJsonString());
                appObj.put("sandboxAuthorizedDomains", sub.getSandAuthorizedDomains());
                appObj.put("sandValidityTime", sub.getSandValidityTime());
                appObj.put("sandRegenarateOption", sub.isSandRegenerateOption());
                Set<Scope> scopeSet=sub.getScopes();
                Set<Map<String,Object>> apisubs=sub.getSubscriptions();
                JSONArray scopesArray=new JSONArray();
                for (Scope scope : scopeSet) {
                    JSONObject scopeObj = new JSONObject();
                    scopeObj.put("scopeKey", scope.getKey());
                    scopeObj.put("scopeName", scope.getName());
                    scopesArray.add(scopeObj);
                }
                JSONArray apisArray=new JSONArray();
                for(Map<String,Object> api:apisubs){
                    JSONObject apiObj = new JSONObject();
                    apiObj.put("name", api.get("name"));
                    apiObj.put("provider", api.get("provider"));
                    apiObj.put("version", api.get("version"));
                    apiObj.put("status", api.get("status"));
                    apiObj.put("tier", api.get("tier"));
                    apiObj.put("subStatus", api.get("subStatus"));
                    apiObj.put("thumburl", api.get("thumburl"));
                    apiObj.put("context", api.get("context"));
                    apiObj.put("prodKey", api.get("prodKey"));
                    apiObj.put("prodConsumerKey", api.get("prodConsumerKey"));
                    apiObj.put("prodConsumerSecret", api.get("prodConsumerSecret"));
                    apiObj.put("prodAuthorizedDomains", api.get("prodAuthorizedDomains"));
                    apiObj.put("prodValidityTime", api.get("prodValidityTime"));
                    apiObj.put("sandboxKey", api.get("sandboxKey"));
                    apiObj.put("sandboxConsumerKey", api.get("sandboxConsumerKey"));
                    apiObj.put("sandboxConsumerSecret", api.get("sandboxConsumerSecret"));
                    apiObj.put("sandAuthorizedDomains", api.get("sandAuthorizedDomains"));
                    apiObj.put("sandValidityTime", api.get("sandValidityTime"));
                    apiObj.put("hasMultipleEndpoints", api.get("hasMultipleEndpoints"));
                    apisArray.add(apiObj);
                }
                appObj.put("subscriptions",apisArray);
                appObj.put("scopes",scopesArray);
                applicationList.add(appObj);
                result.put("applications", applicationList);
                result.put("totalLength", subscriptionCount);

            }
        }
        return result;

    }
	public String isURLValid(String type, String urlVal) throws APIManagementException {

		String response = "";

		if (urlVal != null && !urlVal.isEmpty()) {
			URLConnection conn = null;
			try {
				URL url = new URL(urlVal);
				if (type != null && type.equals("wsdl")) {
					validateWsdl(urlVal);
					response = "success";
				}
				// checking http,https endpoints up to resource level by doing
				// http HEAD. And other end point
				// validation do through basic url connect
				else if (url.getProtocol().matches("https")) {
					ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
					String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
					String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
					System.setProperty("javax.net.ssl.trustStore", trustStorePath);
					System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

					return sendHttpHEADRequest(urlVal);
				} else if (url.getProtocol().matches("http")) {
					return sendHttpHEADRequest(urlVal);
				} else {
					return "error while connecting";
				}
			} catch (Exception e) {
				response = e.getMessage();
			} finally {
				if (conn != null) {
					conn = null;
				}
			}
		}
		return response;
	}

	/**
	 * This method is to functionality of get list of environments that list in api-manager.xml
	 *
	 * @return list of environments with details of environments
	 */
	public static Map<String, Environment> getEnvironments() {
		APIManagerConfiguration config =
				ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
						.getAPIManagerConfiguration();
		Map<String, Environment> environments = config.getApiGatewayEnvironments();
		return environments;
	}

	public static Map getRegisteredResourceByAPIIdentifier(APIIdentifier identifier) throws APIManagementException {
		//get new key manager
		KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
		Map registeredResource = keyManager.getResourceByApiId(identifier.toString());
		return registeredResource;
	}
	
    public static String convertToString(Object obj) {
	Gson gson = new Gson();
	String json = gson.toJson(obj);
	return json;
    }
	
    public static Documentation populateDocument(String docType, String sourceType, String sourceURL, String summary,
	    String docName, String otherTypeName, String visibility) {
	Documentation doc = new Documentation(getDocType(docType), docName);

	doc.setSummary(summary);
	doc.setSourceUrl(sourceURL);

	if (doc.getType() == DocumentationType.OTHER) {
	    doc.setOtherTypeName(otherTypeName);
	}

	if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.URL.toString())) {
	    doc.setSourceType(Documentation.DocumentSourceType.URL);
	} else if (sourceType.equalsIgnoreCase(Documentation.DocumentSourceType.FILE.toString())) {
	    doc.setSourceType(Documentation.DocumentSourceType.FILE);
	} else {
	    doc.setSourceType(Documentation.DocumentSourceType.INLINE);
	}

	if (visibility == null) {
	    visibility = APIConstants.DOC_API_BASED_VISIBILITY;
	}
	if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.API_LEVEL.toString())) {
	    doc.setVisibility(Documentation.DocumentVisibility.API_LEVEL);
	} else if (visibility.equalsIgnoreCase(Documentation.DocumentVisibility.PRIVATE.toString())) {
	    doc.setVisibility(Documentation.DocumentVisibility.PRIVATE);
	} else {
	    doc.setVisibility(Documentation.DocumentVisibility.OWNER_ONLY);
	}

	return doc;
    }
	
    public static FileData populateFileData(byte[] content, String fileName, String contentType, String filePath) {
	FileData file = new FileData(new ByteArrayInputStream(content), fileName, contentType, filePath);
	return file;
    }

	public static String getLifeCycleTransitionAction(String currentStatus, String nextStatu) {
		//TODO Implement
		return "Publish";
	}
}
