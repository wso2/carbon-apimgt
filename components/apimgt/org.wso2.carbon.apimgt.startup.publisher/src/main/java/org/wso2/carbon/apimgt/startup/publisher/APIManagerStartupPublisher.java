/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.startup.publisher;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.startup.publisher.internal.DataHolder;
import org.wso2.carbon.apimgt.startup.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.common.CommonConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.FileTypeMap;
import javax.cache.Cache;
import javax.xml.namespace.QName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class APIManagerStartupPublisher implements ServerStartupHandler {
	private static final Log log = LogFactory
			.getLog(APIManagerStartupPublisher.class);
	Cache contextCache = APIUtil.getAPIContextCache();
	APIProvider provider;
	protected Registry registry;
	private static final String httpPort = "mgt.transport.http.port";
	private static final String hostName = "carbon.local.ip";

	@Override
	public void invoke() {
		if (log.isDebugEnabled()) {
			log.info("Startup Publisher Invoked");
		}
		
		APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
												.getAPIManagerConfiguration();
		
        if (Boolean.parseBoolean(
                configuration.getFirstProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_ENABLED))) {
            List<String> apiContexts = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_CONTEXT);
            List<String> apiProviders = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_PROVIDER);
            List<String> apiVersions = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_VERSION);
            List<String> apiEndpoints = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_ENDPOINT);
            List<String> apiIconPaths = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_ICONPATH);
            List<String> apiDocumentURLs = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_DOCUMENTURL);
            List<String> apiAuthTypes = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_AUTHYTPE);

            List<String> localAPIContexts = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_CONTEXT);
            List<String> localAPIProviders = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_PROVIDER);
            List<String> localAPIVersions = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_VERSION);
            List<String> localAPIIconPaths = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_ICONPATH);
            List<String> localAPIDocumentURLs = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_DOCUMENTURL);
            List<String> localAPIAuthTypes = configuration
                    .getProperty(APIStartupPublisherConstants.API_STARTUP_PUBLISHER_API_LOCAL_AUTHYTPE);

            if (!isValidLocalAPIConfig(localAPIContexts, localAPIProviders, localAPIVersions, localAPIIconPaths,
                    localAPIDocumentURLs, localAPIAuthTypes)
                    && !isValidAPIConfig(apiProviders, apiVersions, apiEndpoints, apiContexts, apiIconPaths,
                            apiDocumentURLs, apiAuthTypes)) {
                log.error("Invalid StartupAPIPublisher configuration");
                return;
            }
			
			if (apiContexts != null) {
				/* Create APIs*/
				for (int i = 0; i < apiContexts.size(); i++) {
					try {
						String apiContext = apiContexts.get(i);
						String apiProvider = apiProviders.get(i);
						String apiVersion = apiVersions.get(i);
						String apiEndpoint = apiEndpoints.get(i);
						String iconPath = apiIconPaths.get(i);
						String documentURL = apiDocumentURLs.get(i);
						String authType = apiAuthTypes.get(i);
						
						String apiName;

						if (apiProvider == null || apiVersion == null || apiContext == null || apiEndpoint == null
								|| iconPath == null || documentURL == null || authType == null) {
							log.error("Invalid StartupAPIPublisher configuration");
							return;
						}

						/*
						 * API Context validations and initialize apiName to context
						 * without slash
						 */
						if (!apiContext.startsWith("/")) {
							apiName = apiContext;
							apiContext = "/" + apiContext;
						} else {
							apiName = apiContext.substring(1);
						}

						createAPIAtServerStartup(apiName, apiProvider, apiVersion,
								apiEndpoint, apiContext, iconPath, documentURL, authType);
					} catch (IndexOutOfBoundsException e) {
						log.error("Invalid StartupAPIPublisher configuration", e);
					}
				}
			}
			
			if (localAPIContexts != null) {
				/* Create LocalAPIs*/
				for (int i = 0; i < localAPIContexts.size(); i++) {
					try {
						String apiContext = localAPIContexts.get(i);
						String apiProvider = localAPIProviders.get(i);
						String apiVersion = localAPIVersions.get(i);
						String iconPath = localAPIIconPaths.get(i);
						String documentURL = localAPIDocumentURLs.get(i);
						String authType = localAPIAuthTypes.get(i);
						
						String apiName;

						if (apiProvider == null || apiVersion == null || apiContext == null || iconPath == null
								|| documentURL == null || authType == null) {
							log.error("Invalid StartupAPIPublisher configuration");
							return;
						}

						/*
						 * API Context validations and initialize apiName to context
						 * without slash
						 */
						if (!apiContext.startsWith("/")) {
							apiName = apiContext;
							apiContext = "/" + apiContext;
						} else {
							apiName = apiContext.substring(1);
						}

						/* This is an internal API. So we will compute the Endpoint. */
						String apiEndpoint = "http://" + System.getProperty(hostName)
									+ ":" + System.getProperty(httpPort) + apiContext;
						
						createAPIAtServerStartup(apiName, apiProvider, apiVersion,
								apiEndpoint, apiContext, iconPath, documentURL, authType);
					} catch (IndexOutOfBoundsException e) {
						log.error("Invalid StartupAPIPublisher configuration", e);
					}
				}
			}
		}
	}

    private void createAPIAtServerStartup(String apiName, String apiProvider, String apiVersion, String apiEndpoint,
            String apiContext, String iconPath, String documentURL, String authType) {
		/* Check whether API already published */
		if (contextCache.get(apiContext) != null
				|| ApiMgtDAO.getInstance().isContextExist(apiContext)) {
			if (log.isDebugEnabled()) {
				log.info("API Context " + apiContext + " already exists");
			}
			return;
		}

		try {
			API api = createAPIModel(apiName, apiProvider, apiVersion,
					apiEndpoint, apiContext, iconPath, documentURL, authType);
			if (api != null) {
				addAPI(api, documentURL);
				log.info("Successfully Created API " + apiName + "-" + apiVersion);
			}
		} catch (APIManagementException e) {
			log.error(e);
		} catch (RegistryException e) {
			log.error(e);
		}
	}

    private API createAPIModel(String apiName, String apiProvider, String apiVersion, String apiEndpoint,
            String apiContext, String iconPath, String documentURL, String authType) throws APIManagementException {
		API api = null;
		RandomAccessFile randomAccessFile = null;
        FileInputStream fileInputStream = null;
		try {
			provider = APIManagerFactory.getInstance().getAPIProvider(
					apiProvider);
			APIIdentifier identifier = new APIIdentifier(apiProvider, apiName,
					apiVersion);
			api = new API(identifier);
			api.setContext(apiContext);
			api.setUrl(apiEndpoint);
			api.setUriTemplates(getURITemplates(apiEndpoint, authType));
			api.setVisibility(APIConstants.API_GLOBAL_VISIBILITY);
			api.addAvailableTiers(provider.getTiers());
			api.setEndpointSecured(false);
			api.setStatus(APIStatus.PUBLISHED);
            api.setTransports(Constants.TRANSPORT_HTTP+","+Constants.TRANSPORT_HTTPS);
            
            /* Adding Icon*/
            File file ;
           
            if (!APIStartupPublisherConstants.API_ICON_PATH_AND_DOCUMENT_URL_DEFAULT.equals(iconPath)) {
                file = new File(iconPath);
                String absolutePath = file.getAbsolutePath();
                randomAccessFile = new RandomAccessFile(absolutePath, "r");
                fileInputStream = new FileInputStream(randomAccessFile.getFD());
                
                ResourceFile icon = new ResourceFile(fileInputStream,
                        getImageContentType(absolutePath));
                String thumbPath = APIUtil.getIconPath(identifier);
                String thumbnailUrl = provider.addResourceFile(thumbPath, icon);
                api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, apiProvider));
                
                /*Set permissions to anonymous role for thumbPath*/
                APIUtil.setResourcePermissions(apiProvider, null, null, thumbPath);
                
            }                        
        
		} catch (APIManagementException e) {
			handleException("Error while initializing API Provider", e);
		} catch (IOException e) {
			handleException("Error while reading image from icon path", e);
		} finally {
		         
		    if(randomAccessFile != null){		        
                try {
                    randomAccessFile.close();
                } catch (IOException ignored) {
                    
                }              
		    }
		    
		    if(fileInputStream != null){
		        try {
                    fileInputStream.close();
                } catch (IOException ignored) {
                    
                }
		    }
		}
		return api;
	}

	private void addAPI(API api, String documentURL) throws RegistryException,
			APIManagementException {
		ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
		try {
			this.registry = DataHolder.getRegistryService()
					.getGovernanceSystemRegistry();
			createAPIArtifact(api);

			int tenantId = -1234;
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
            tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);

			apiMgtDAO.addAPI(api, tenantId);
			
			
			/* Adding Document URL*/
			if (!APIStartupPublisherConstants.API_ICON_PATH_AND_DOCUMENT_URL_DEFAULT.equals(documentURL)) {
				Documentation doc = new Documentation(DocumentationType.HOWTO, 
						APIStartupPublisherConstants.API_DOCUMENTATION_NAME);
				doc.setSourceType(Documentation.DocumentSourceType.URL);
				doc.setSourceUrl(documentURL);
				createDocumentation(api, doc);
			}
			
			if (APIUtil.isAPIManagementEnabled()) {
				Boolean apiContext = null;
				if (contextCache.get(api.getContext()) != null) {
					apiContext = Boolean.parseBoolean(contextCache.get(
							api.getContext()).toString());
				}
				if (apiContext == null) {
					contextCache.put(api.getContext(), true);
				}
			}
		} catch (APIManagementException e) {
			throw new APIManagementException("Error in adding API :"
					+ api.getId().getApiName(), e);
		} catch (RegistryException e) {
			throw e;
        } catch (UserStoreException e) {
            throw new APIManagementException(
                    "Error in retrieving Tenant Information while adding api :" + api.getId().getApiName(), e);
        }
	}

	/**
	 * Create an Api
	 * 
	 * @param api
	 *            API
	 * @throws APIManagementException
	 *             if failed to create API
	 */
	private void createAPIArtifact(API api) throws APIManagementException {
		GenericArtifactManager artifactManager = APIUtil.getArtifactManager(
				registry, APIConstants.API_KEY);
		try {
			registry.beginTransaction();
			GenericArtifact genericArtifact = artifactManager
					.newGovernanceArtifact(new QName(api.getId().getApiName()));
			GenericArtifact artifact = APIUtil.createAPIArtifactContent(
					genericArtifact, api);
			artifactManager.addGenericArtifact(artifact);
			String artifactPath = GovernanceUtils.getArtifactPath(registry,
					artifact.getId());
			String providerPath = APIUtil.getAPIProviderPath(api.getId());
			// provider ------provides----> API
			registry.addAssociation(providerPath, artifactPath,
					APIConstants.PROVIDER_ASSOCIATION);
			Set<String> tagSet = api.getTags();
			if (tagSet != null) {
				for (String tag : tagSet) {
					registry.applyTag(artifactPath, tag);
				}
			}

			if (api.getUrl() != null && !"".equals(api.getUrl())) {
				String path = APIUtil.createEndpoint(api.getUrl(), registry);
				if (path != null) {
					registry.addAssociation(artifactPath, path,
							CommonConstants.ASSOCIATION_TYPE01);
				}
			}
			// write API Status to a separate property. This is done to support
			// querying APIs using custom query (SQL)
			// to gain performance
			String apiStatus = api.getStatus().getStatus();
			saveAPIStatus(artifactPath, apiStatus);
			String visibleRolesList = api.getVisibleRoles();
			String[] visibleRoles = new String[0];
			if (visibleRolesList != null) {
				visibleRoles = visibleRolesList.split(",");
			}
			APIUtil.setResourcePermissions(api.getId().getProviderName(),
					api.getVisibility(), visibleRoles, artifactPath, registry);
			registry.commitTransaction();

			// Generate API Definition for Swagger. 
			//TO DO: Need to re-write this method to generate swagger 2.0 resource

		} catch (RegistryException e) {
			try {
				registry.rollbackTransaction();
			} catch (RegistryException re) {
				handleException(
						"Error while rolling back the transaction for API: "
								+ api.getId().getApiName(), re);
			}
			handleException(
					"Error while performing registry transaction operation", e);
		}

	}
	
	/**
	 * Create Documentation
	 * 
	 * @param api
	 *            API
	 * @throws APIManagementException
	 *             if failed to create API
	 */
	private void createDocumentation(API api, Documentation documentation)
            throws APIManagementException {
        try {
        	APIIdentifier apiId = api.getId();
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry,
                                                                                APIConstants.DOCUMENTATION_KEY);
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName(documentation.getName()));
            artifactManager.addGenericArtifact(
                    APIUtil.createDocArtifactContent(artifact, apiId, documentation));
            String apiPath = APIUtil.getAPIPath(apiId);
            
            //Adding association from api to documentation . (API -----> doc)
            registry.addAssociation(apiPath, artifact.getPath(),
                                    APIConstants.DOCUMENTATION_ASSOCIATION);
            
            String visibleRolesList = api.getVisibleRoles();
			String[] visibleRoles = new String[0];
			if (visibleRolesList != null) {
				visibleRoles = visibleRolesList.split(",");
			}
            
            APIUtil.setResourcePermissions(apiId.getProviderName(), 
            		api.getVisibility(), visibleRoles, artifact.getPath(), registry);
            
        } catch (RegistryException e) {
            handleException("Failed to add documentation", e);
        } 
    }

	/**
	 * Persist API Status into a property of API Registry resource
	 * 
	 * @param artifactId
	 *            API artifact ID
	 * @param apiStatus
	 *            Current status of the API
	 * @throws APIManagementException
	 *             on error
	 */
	private void saveAPIStatus(String artifactId, String apiStatus)
			throws APIManagementException {
		try {
			Resource resource = registry.get(artifactId);
			if (resource != null) {
				String propValue = resource
						.getProperty(APIConstants.API_STATUS);
				if (propValue == null) {
					resource.addProperty(APIConstants.API_STATUS, apiStatus);
				} else {
					resource.setProperty(APIConstants.API_STATUS, apiStatus);
				}
				registry.put(artifactId, resource);
			}
		} catch (RegistryException e) {
			handleException("Error while adding API", e);
		}
	}

	private Set<URITemplate> getURITemplates(String endpoint, String authType) {
		Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();
		String[] httpVerbs = { "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS" };
		
		if (authType.equals(APIConstants.AUTH_NO_AUTHENTICATION)) {
			for (int i = 0; i < 5; i++) {
				URITemplate template = new URITemplate();
				template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
				template.setHTTPVerb(httpVerbs[i]);
				template.setResourceURI(endpoint);
				template.setUriTemplate("/*");
				uriTemplates.add(template);
			}
		} else {
			for (int i = 0; i < 5; i++) {
				URITemplate template = new URITemplate();
				if (i != 4) {
					template.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
				} else {
					template.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
				}
				template.setHTTPVerb(httpVerbs[i]);
				template.setResourceURI(endpoint);
				template.setUriTemplate("/*");
				uriTemplates.add(template);
			}
		}
		
		return uriTemplates;
	}

	private void handleException(String msg, Exception e)
			throws APIManagementException {
		log.error(msg, e);
		throw new APIManagementException(msg, e);
	}
	
	
	private String getImageContentType(String imageAbsolutePath) {
		String fileName = new File(imageAbsolutePath).getName();
        return FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
	}
	
    private boolean isValidLocalAPIConfig(List<String> localAPIContexts, List<String> localAPIProviders,
            List<String> localApiVersions, List<String> localAPIIconPaths, List<String> localAPIDocumentURLs,
            List<String> localAPIAuthTypes) {

        return !(localAPIContexts == null || localAPIProviders == null || localApiVersions == null
                || localAPIIconPaths == null || localAPIDocumentURLs == null || localAPIAuthTypes == null);
    }

    private boolean isValidAPIConfig(List<String> apiProviders, List<String> apiVersions, List<String> apiEndpoints,
            List<String> apiContexts, List<String> apiIconPaths, List<String> apiDocumentURLs,
            List<String> apiAuthTypes) {  
        
        return !(apiProviders == null || apiVersions == null || apiEndpoints == null || apiContexts == null
                || apiIconPaths == null || apiDocumentURLs == null || apiAuthTypes == null);
    }
	
	private static DocumentationType getDocType(String docType) {
        DocumentationType docsType = null;
        for (DocumentationType type : DocumentationType.values()) {
            if (type.getType().equalsIgnoreCase(docType)) {
                docsType = type;
            }
        }
        return docsType;
    }

}
