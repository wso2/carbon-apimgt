/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.deployer;

import com.google.gson.JsonObject;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;

import java.util.List;
import java.util.Map;

/**
 * This class controls the API artifact deployments on the External gateways
 */
public interface ExternalGatewayDeployer {

    /**
     * Deploy API artifact to provided environment in the external gateway
     *
     * @param api API to be deployed into in the external gateway
     * @param environment Environment to be deployed
     * @param referenceArtifact Reference API artifact
     * @throws DeployerException if error occurs when deploying APIs to in the external gateway
     */
    public String deploy(API api, Environment environment, String referenceArtifact) throws DeployerException;

    /**
     * Undeploy API artifact from provided environment
     *
     * @param apiID API ID to be undeployed from the external gateway
     * @param apiName     Name of the API to be undeployed from Solace broker
     * @param apiVersion  Version of the API to be undeployed from Solace broker
     * @param apiContext  Context of the API to be undeployed from Solace broker
     * @param environment Environment needed to be undeployed API from
     * @param referenceArtifact Reference API artifact
     * @throws DeployerException if error occurs when undeploying APIs from Solace broker
     */
    public boolean undeploy(String apiID, String apiName, String apiVersion, String apiContext,
                            Environment environment, String referenceArtifact) throws DeployerException;

    /**
     * Undeploy API artifact from provided environment in the external gateway when Api is retired
     *
     * @param api API to be undeployed from the external gateway
     * @param environment Environment needed to be undeployed API from the external gateway
     * @param referenceArtifact Reference API artifact
     * @throws DeployerException if error occurs when undeploying APIs from the external gateway
     */
    public boolean undeployWhenRetire(API api, Environment environment, String referenceArtifact)
            throws DeployerException;

    /**
     * Get vendor type of the external gateway
     *
     * @return String vendor name
     */
    public String getType();

    /**
     * This method returns the Configurations related to external gateway
     *
     * @return  List<ConfigurationDto> connectionConfigurations
     */
    public List<ConfigurationDto> getConnectionConfigurations();

    /**
     * This method returns the Gateway Feature Catalog
     *
     * @return JSON object Gateway Feature Catalog
     */
    public JsonObject getGatewayFeatureCatalog() throws DeployerException;

    /**
     * This method returns the validation result of a given API with the external gateway
     *
     * @return List<String> validation result
     */
    public List<String> validateApi(API api) throws DeployerException;

    /**
     * This method returns the resolved API execution URL by replacing all placeholders appropriately
     * @param url Existing Execution URL
     * @param environment Gateway Environment
     * @param referenceArtifact Reference API artifact
     *
     * @return String api execution url
     */
    public String getAPIExecutionURL(String url, Environment environment, String referenceArtifact)
            throws DeployerException;

    /**
     * This method returns refined API by manipulating the API object according to the external gateway requirements
     *
     * @param api API object
     *
     * @return API api object
     */
    public void transformAPI(API api) throws DeployerException;

    /**
     * This method returns the default hostname template of the external gateway
     *
     * @return String default hostname template
     */
    public String getDefaultHostnameTemplate();
}
