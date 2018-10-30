/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.UserNameMapper;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;

/**
 * Creates API Producers and API Consumers.
 */
public class APIManagerFactory {

    private static final Logger log = LoggerFactory.getLogger(APIManagerFactory.class);

    private static final String ANONYMOUS_USER = "__wso2.am.anon__";

    private static final APIManagerFactory instance = new APIManagerFactory();

    private APIMgtAdminService apiMgtAdminService;
    private IdentityProvider identityProvider;
    private KeyManager keyManager;
    private APIGateway apiGateway;
    private APILifecycleManager apiLifecycleManager;
    private UserNameMapper userNameMapper;

    private APIManagerFactory() {

    }

    /**
     * Get APIManagerFactory instance
     *
     * @return APIManagerFactory object
     */
    public static APIManagerFactory getInstance() {
        return instance;
    }

    private APIPublisher newProvider(String username) throws APIManagementException {
        try {
            APIPublisherImpl apiPublisher = new APIPublisherImpl(username, getIdentityProvider(), getKeyManager(),
                    new DAOFactory(), geApiLifecycleManager(), new GatewaySourceGeneratorImpl(), new
                    APIGatewayPublisherImpl(), new UserNameMapperImpl());

            // Register all the observers which need to observe 'Publisher' component
            apiPublisher.registerObserver(new EventLogger());
            apiPublisher.registerObserver(new FunctionTrigger(new DAOFactory().getFunctionDAO(),
                    new RestCallUtilImpl()));

            return apiPublisher;
        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Provider", e);
            throw new APIMgtDAOException("Couldn't Create API Provider", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    private APIMgtAdminServiceImpl newAPIMgtAdminService() throws APIManagementException {
        return new APIMgtAdminServiceImpl(new DAOFactory(), new APIGatewayPublisherImpl());
    }

    private APIStore newConsumer(String username) throws APIManagementException {
        // if (username.equals(ANONYMOUS_USER)) {
        // username = null;
        // }
        try {
            APIStoreImpl userAwareAPIStore = new APIStoreImpl(username, getIdentityProvider(), getKeyManager(),
                    new DAOFactory(), new GatewaySourceGeneratorImpl(), new APIGatewayPublisherImpl(), new
                    UserNameMapperImpl());

            // Register all the observers which need to observe 'Store' component
            userAwareAPIStore.registerObserver(new EventLogger());
            userAwareAPIStore.registerObserver(new FunctionTrigger(new DAOFactory().getFunctionDAO(),
                    new RestCallUtilImpl()));

            return userAwareAPIStore;
        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Consumer", e);
            throw new APIMgtDAOException("Couldn't Create API Consumer", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Get API Publisher object for a particular user
     *
     * @param username The username of user who's requesting the object
     * @return APIPublisher object
     * @throws APIManagementException if error occurred while initializing API Publisher
     */
    public APIPublisher getAPIProvider(String username) throws APIManagementException {

        return newProvider(username);
    }

    /**
     * Get Analyzer for a particular user
     *
     * @param username The username of user who's requesting the object
     * @return APIPublisher object
     * @throws APIManagementException if error occurred while initializing Analytics object
     */
    public Analyzer getAnalyzer(String username) throws APIManagementException {

        return newAnalyzer(username);
    }

    private Analyzer newAnalyzer(String username) throws APIMgtDAOException {
        try {
            AnalyzerImpl analyzer = new AnalyzerImpl(username, new DAOFactory().getAnalyticsDAO());
            return analyzer;
        } catch (APIMgtDAOException e) {
            throw new APIMgtDAOException("Couldn't Create Analyzer", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Get API Manager Admin Service object
     *
     * @return APIMgtAdminService object
     * @throws APIManagementException if error occurred while initializing API Manager Admin Service
     */
    public APIMgtAdminService getAPIMgtAdminService() throws APIManagementException {
        if (apiMgtAdminService == null) {
            synchronized (this) {
                apiMgtAdminService = newAPIMgtAdminService();
            }
        }
        return apiMgtAdminService;
    }

    /**
     * Get API Store object by anonymous user
     *
     * @return APIStore object
     * @throws APIManagementException if error occurred while initializing API Store
     */
    public APIStore getAPIConsumer() throws APIManagementException {
        return getAPIConsumer(ANONYMOUS_USER);
    }

    /**
     * Get API Store object for a particular user
     *
     * @param username The username of user who's requesting the objecusernamet
     * @return APIStore object
     * @throws APIManagementException if error occurred while initializing API Store
     */
    public APIStore getAPIConsumer(String username) throws APIManagementException {
        return newConsumer(username);
    }

    /**
     * Get Identity Provider object
     *
     * @return Identity Provider object
     * @throws IdentityProviderException if error occurred while initializing Identity Provider
     */
    public IdentityProvider getIdentityProvider() throws IdentityProviderException {
        if (identityProvider == null) {
            try {
                identityProvider = (IdentityProvider) Class.forName(ServiceReferenceHolder.getInstance()
                        .getAPIMConfiguration().getIdentityProviderConfigs().getIdentityProviderImplClass())
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new IdentityProviderException("Error occurred while initializing identity provider", e,
                        ExceptionCodes.IDP_INITIALIZATION_FAILED);
            }
        }
        return identityProvider;
    }

    /**
     * Get Key Manager object
     *
     * @return Key Manager object
     * @throws KeyManagementException if error occurred while initializing key manager
     */
    public KeyManager getKeyManager() throws KeyManagementException {
        if (keyManager == null) {
            try {
                keyManager = (KeyManager) Class.forName(ServiceReferenceHolder.getInstance().getAPIMConfiguration()
                        .getKeyManagerConfigs().getKeyManagerImplClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new KeyManagementException("Error occurred while initializing key manager", e,
                        ExceptionCodes.KEY_MANAGER_INITIALIZATION_FAILED);
            }
        }
        return keyManager;
    }

    /**
     * Get API gateway publisher implementation object
     *
     * @return APIGateway impl object
     */
    public APIGateway getApiGateway() {

        if (apiGateway == null) {
            apiGateway = new APIGatewayPublisherImpl();
        }
        return apiGateway;
    }

    /**
     * Get API Lifecycle Manager implementation object
     *
     * @return APILifecycleManager impl object
     */
    public APILifecycleManager geApiLifecycleManager() {

        if (apiLifecycleManager == null) {
            apiLifecycleManager = new APILifeCycleManagerImpl();
        }
        return apiLifecycleManager;
    }

    /**
     * Get User Name Mapper Implementation object
     *
     * @return UserNameMapper implementation object
     */
    public UserNameMapper getUserNameMapper() {
        if (userNameMapper == null) {
            userNameMapper = new UserNameMapperImpl();
        }
        return userNameMapper;
    }
}
