/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.service.thrift.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.thrift.APIKeyValidationServiceImpl;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.apimgt.keymgt.service.thrift.APIKeyValidationService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @scr.component name="api.keymgt.component" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 * @scr.reference name="org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService"
 * cardinality="1..1" policy="dynamic" bind="setThriftAuthenticationService"  unbind="unsetThriftAuthenticationService"
 */
public class APIKeyMgtServiceComponent {

    private static Log log = LogFactory.getLog(APIKeyMgtServiceComponent.class);
    private ThriftAuthenticatorService thriftAuthenticationService;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private boolean isThriftServerEnabled;

    protected void activate(ComponentContext ctxt) {
        try {
            APIKeyMgtDataHolder.initData();
            //Based on configuration we have to decide thrift server run or not
            if (APIKeyMgtDataHolder.getThriftServerEnabled()) {
                APIKeyValidationServiceImpl.init(thriftAuthenticationService);
                startThriftService();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("API key validation thrift server is disabled");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Identity API Key Mgt Bundle is started.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize key management service.", e);
        }
    }
    protected void setRegistryService(RegistryService registryService) {
        APIKeyMgtDataHolder.setRegistryService(registryService);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        APIKeyMgtDataHolder.setRegistryService(null);
        if (log.isDebugEnabled()) {
            log.debug("Registry Service is unset in the API KeyMgt bundle.");
        }
    }

    protected void setRealmService(RealmService realmService) {
        APIKeyMgtDataHolder.setRealmService(realmService);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is set in the API KeyMgt bundle.");
        }
    }

    protected void unsetRealmService(RealmService realmService) {
        APIKeyMgtDataHolder.setRealmService(null);
        if (log.isDebugEnabled()) {
            log.debug("Realm Service is unset in the API KeyMgt bundle.");
        }
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        APIKeyMgtDataHolder.setAmConfigService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        APIKeyMgtDataHolder.setAmConfigService(null);
    }

    /**
     * set Thrift authentication service
     *
     * @param authenticationService <code>ThriftAuthenticatorService</code>
     */
    protected void setThriftAuthenticationService(
            ThriftAuthenticatorService authenticationService) {
        if (log.isDebugEnabled()) {
            log.debug("ThriftAuthenticatorService set in Entitlement bundle");
        }
        this.thriftAuthenticationService = authenticationService;
        //log.info("STUBHUB " + authenticationService + " received.");

    }

    /**
     * un-set Thrift authentication service
     *
     * @param //authenticationService <code>ThriftAuthenticatorService</code>
     */
    protected void unsetThriftAuthenticationService(
            ThriftAuthenticatorService authenticationService) {
        if (log.isDebugEnabled()) {
            log.debug("ThriftAuthenticatorService unset in Entitlement bundle");
        }
        this.thriftAuthenticationService = null;
    }

    private void startThriftService() throws Exception {
        try {
            TSSLTransportFactory.TSSLTransportParameters transportParam =
                    new TSSLTransportFactory.TSSLTransportParameters();

            //read the keystore and password used for ssl communication from config
            String keyStorePath = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
            String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");

            String thriftPortString =
                    APIKeyMgtDataHolder.getAmConfigService().getAPIManagerConfiguration().getFirstProperty(
                            APIConstants.API_KEY_MANGER_THRIFT_SERVER_PORT);

            if (thriftPortString == null) {
                thriftPortString = "10398";
                log.info("Setting default port for thrift key management service: " + "10398");
            }

            String thriftHostString =
                    APIKeyMgtDataHolder.getAmConfigService().getAPIManagerConfiguration().getFirstProperty(
                    APIConstants.API_KEY_MANGER_THRIFT_SERVER_HOST);

            if(thriftHostString == null){
                thriftHostString = NetworkUtils.getLocalHostname();
                log.info("Setting default carbon host for thrift key management service: " + thriftHostString);
            }

            String thriftClientTimeOut =
                    APIKeyMgtDataHolder.getAmConfigService().getAPIManagerConfiguration().getFirstProperty(
                    APIConstants.API_KEY_MANGER_CONNECTION_TIMEOUT);
            if (thriftPortString == null || thriftClientTimeOut == null) {
                throw new APIKeyMgtException("Port and Connection timeout not provided to start thrift key mgt service.");
            }

            int receivePort = Integer.parseInt(thriftPortString);
            int clientTimeOut = Integer.parseInt(thriftClientTimeOut);
            //set it in parameters
            transportParam.setKeyStore(keyStorePath, keyStorePassword);
            
            TServerSocket serverTransport =
                    TSSLTransportFactory.getServerSocket(receivePort,
                                                         clientTimeOut,
                                                         getHostAddress(thriftHostString),
                                                         transportParam);


            APIKeyValidationService.Processor processor = new APIKeyValidationService.Processor(
                    new APIKeyValidationServiceImpl());

            //TODO: have to decide on the protocol.
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                    processor(processor));
            //TServer server = new TThreadPoolServer(new TThreadPoolServer.Args())

            /*TServer server = new TThreadPoolServer(processor, serverTransport,
            new TCompactProtocol.Factory());*/
            Runnable serverThread = new ServerRunnable(server);
            executor.submit(serverThread);

            log.info("Started thrift key mgt service at port:" + receivePort);
        } catch (TTransportException
                e) {
            String transportErrorMsg = "Error in initializing thrift transport";
            log.error(transportErrorMsg, e);
            throw new Exception(transportErrorMsg);
        } catch (UnknownHostException
                e) {
            String hostErrorMsg = "Error in obtaining host name";
            log.error(hostErrorMsg, e);
            throw new Exception(hostErrorMsg);
        }
    }

    /**
     * Thread that starts thrift server
     */
    private class ServerRunnable implements Runnable {
        TServer server;

        public ServerRunnable(TServer server) {
            this.server = server;
        }

        public void run() {
            server.serve();
        }
    }

    /**
     * Get INetAddress by host name or  IP Address
     *
     * @param host name or host IP String
     * @return InetAddress
     * @throws java.net.UnknownHostException
     */

    private InetAddress getHostAddress(String host) throws UnknownHostException {
        String[] splittedString = host.split("\\.");
        boolean value = checkIfIP(splittedString);
        if (!value) {
            return InetAddress.getByName(host);
        }

        byte[] byteAddress = new byte[4];
        for (int i = 0; i < splittedString.length; i++) {
            if (Integer.parseInt(splittedString[i]) > 127) {
                byteAddress[i] = new Integer(Integer.parseInt(splittedString[i]) - 256).byteValue();
            } else {
                byteAddress[i] = Byte.parseByte(splittedString[i]);
            }
        }
        return InetAddress.getByAddress(byteAddress);
    }

    /**
     * Check the hostname is IP or String
     *
     * @param ip IP
     * @return true/false
     */
    private boolean checkIfIP(String ip[]) {
        for (int i = 0; i < ip.length; i++) {
            try {
                Integer.parseInt(ip[i]);
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }
}
