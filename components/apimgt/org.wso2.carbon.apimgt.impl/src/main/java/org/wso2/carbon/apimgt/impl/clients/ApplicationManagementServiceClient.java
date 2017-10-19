/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;

import java.rmi.RemoteException;

@SuppressWarnings("unused")
public class ApplicationManagementServiceClient {

    private static final Log log = LogFactory.getLog(ApplicationManagementServiceClient.class);
    boolean debugEnabled = log.isErrorEnabled();
    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private IdentityApplicationManagementServiceStub identityApplicationManagementServiceStub;

    public ApplicationManagementServiceClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        //username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);

        if (serviceURL == null) {
            throw new APIManagementException("Required connection details for the key management server not provided");
        }

        try {

            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
            identityApplicationManagementServiceStub = getIdentityApplicationManagementServiceStub(serviceURL, ctx);
            ServiceClient client = identityApplicationManagementServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setCallTransportCleanup(true);
            options.setManageSession(true);

        } catch (AxisFault axisFault) {
            throw new APIManagementException("Error while initializing the OAuth admin service stub", axisFault);
        }
    }

    protected IdentityApplicationManagementServiceStub getIdentityApplicationManagementServiceStub(String serviceURL,
            ConfigurationContext ctx) throws AxisFault {
        return new IdentityApplicationManagementServiceStub(ctx, serviceURL
                + "IdentityApplicationManagementService");
    }

    /**
     * This method is used to create application according to the given sp and user
     *
     * @param serviceProvider Service Provider
     * @param username        UserName of the user
     * @throws Exception
     */
    public void createApplication(ServiceProvider serviceProvider, String username) throws Exception {
        try {
            if (debugEnabled) {
                log.debug("Registering Service Provider " + serviceProvider.getApplicationName());
            }
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            identityApplicationManagementServiceStub.createApplication(serviceProvider);
        } catch (RemoteException  e ) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This method is used to get application details for given application name
     *
     * @param applicationName Name of the application
     * @param username        UserName of the user
     * @return service provider
     * @throws Exception
     */
    public ServiceProvider getApplication(String applicationName, String username) throws Exception {
        try {
            if (debugEnabled) {
                log.debug("Loading Service Provider " + applicationName);
            }
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            return identityApplicationManagementServiceStub.getApplication(applicationName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }

    }

    /**
     * This method is used to get the application information of a given user
     *
     * @param username UserName of the user
     * @return Basic information of the application as an array
     * @throws Exception
     */
    public ApplicationBasicInfo[] getAllApplicationBasicInfo(String username) throws Exception {
        try {
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            return identityApplicationManagementServiceStub.getAllApplicationBasicInfo();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This method is used to update the application data
     *
     * @param serviceProvider Service Provider
     * @param username        UserName of the user
     * @throws Exception
     */
    public void updateApplicationData(ServiceProvider serviceProvider, String username) throws Exception {
        try {
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            identityApplicationManagementServiceStub.updateApplication(serviceProvider);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * This method is used to delete a given application
     *
     * @param applicationID application id
     * @param username      UserName of the user
     * @throws Exception
     */
    public void deleteApplication(String applicationID, String username) throws Exception {
        try {
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            identityApplicationManagementServiceStub.deleteApplication(applicationID);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }

    }

    /**
     * This method is ued to retrieve federated identity providers
     *
     * @param identityProviderName Name of the idp
     * @param username             UserName of the user
     * @return Identity Provider
     * @throws Exception
     */
    public IdentityProvider getFederatedIdentityProvider(String identityProviderName, String username)
            throws Exception {
        Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
        return identityApplicationManagementServiceStub.getIdentityProvider(identityProviderName);
    }

    /**
     * This method is used to get all request path authenticators
     *
     * @param username UserName of the user
     * @return request path authenticators
     * @throws Exception
     */
    public RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators(String username) throws Exception {
        Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
        return identityApplicationManagementServiceStub.getAllRequestPathAuthenticators();
    }

    /**
     * This method is used to get all local authenticators
     *
     * @param username UserName of the user
     * @return local authenticators
     * @throws Exception
     */
    public LocalAuthenticatorConfig[] getAllLocalAuthenticators(String username) throws Exception {
        Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
        return identityApplicationManagementServiceStub.getAllLocalAuthenticators();
    }

    /**
     * This method is used to get all federated identity providers
     *
     * @param username UserName of the user
     * @return federated identity providers
     * @throws Exception
     */
    public IdentityProvider[] getAllFederatedIdentityProvider(String username) throws Exception {
        IdentityProvider[] identityProviders;

        try {
            Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
            identityProviders = identityApplicationManagementServiceStub.getAllIdentityProviders();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
        return identityProviders;
    }

    /**
     * This method is used to get all claim uris
     *
     * @param username UserName of the user
     * @return all claim uris
     * @throws Exception
     */
    public String[] getAllClaimUris(String username) throws Exception {
        Util.setAuthHeaders(identityApplicationManagementServiceStub._getServiceClient(), username);
        return identityApplicationManagementServiceStub.getAllLocalClaimUris();
    }

}
