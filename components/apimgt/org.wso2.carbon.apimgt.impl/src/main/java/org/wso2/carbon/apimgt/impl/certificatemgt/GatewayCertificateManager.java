/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.certificatemgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles importing, deleting server certificates to Gateway environments.
 */
public class GatewayCertificateManager {

    private static GatewayCertificateManager instance = null;
    private static Log log = LogFactory.getLog(GatewayCertificateManager.class);
    private static APIGatewayAdminClient apiGatewayAdminClient = null;
    private static Map<String, Environment> environmentMap = null;
    private List<Environment> failedGateways = new ArrayList<Environment>();
    private List<Environment> successGateways = new ArrayList<Environment>();
    private Map<String, List<Environment>> envList = new HashMap<String, List<Environment>>();

    /**
     * To get the instance of GatewayCertificate Manager.
     *
     * @return instance of gateway certificate manager.
     */
    public static GatewayCertificateManager getInstance() {
        if (instance == null) {
            synchronized (GatewayCertificateManager.class) {
                if (instance == null) {
                    instance = new GatewayCertificateManager();
                }
            }
        }
        return instance;
    }

    private GatewayCertificateManager() {
        try {
            environmentMap = APIUtil.getEnvironments();
        } catch (APIManagementException e) {
            log.error("Error when reading configured Gateway Environments", e);
        }
    }

    /**
     * This method will add the given certificate to all the given gateway environments.
     *
     * @param certificate : The certificate that should be added to the trust store.
     * @param alias       : The certificate alias for the certificate.
     * @return : Map of failed and success gateway environments.
     */
    public Map<String, List<Environment>> addToGateways(String certificate, String alias) {
        for (Environment environment : environmentMap.values()) {
            try {
                apiGatewayAdminClient = new APIGatewayAdminClient(environment);
                boolean result = apiGatewayAdminClient.addCertificate(certificate, alias);
                if (result) {
                    successGateways.add(environment);
                } else {
                    log.warn("Failed to add the certificate for Alias '" + alias + "' from the Gateway " +
                            "Environment '" + environment.getName() + "'");
                    failedGateways.add(environment);
                }
            } catch (AxisFault axisFault) {
                log.error("Error when initializing the Gateway Admin Service.", axisFault);
            }
        }
        envList.put("Failed", failedGateways);
        envList.put("Success", successGateways);
        return envList;
    }

    /**
     * This method will remove the certificate defined by the given alias from all the environments.
     *
     * @param alias : The alias of the certificate that should be removed.
     * @return : Map of failed and success gateway environments.
     */
    public Map<String, List<Environment>> removeFromGateways(String alias) {
        for (Environment environment : environmentMap.values()) {
            try {
                apiGatewayAdminClient = new APIGatewayAdminClient(environment);
                boolean result = apiGatewayAdminClient.deleteCertificate(alias);
                if (result) {
                    successGateways.add(environment);
                } else {
                    log.warn("Failed to remove the certificate for Alias '" + alias + "' from the Gateway " +
                            "Environment '" + environment.getName() + "'");
                    failedGateways.add(environment);
                }
            } catch (AxisFault axisFault) {
                log.error("Error when initializing the Gateway Admin Service.", axisFault);
            }
        }
        envList.put("Failed", failedGateways);
        envList.put("Success", successGateways);
        return envList;
    }
}
