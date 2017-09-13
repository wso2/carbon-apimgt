/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ContainerBasedGatewayGenerator;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.template.ContainerBasedGatewayTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is responsible to handle the auto-created gateways in Openshift
 *
 */
public class OpenShiftGatewayImpl implements ContainerBasedGatewayGenerator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

    private static APIMConfigurations apimConfigurations = new APIMConfigurations();
    private String masterURL = apimConfigurations.getContainerGatewayConfigs().getOpenshiftGatewayConfigurations()
            .getMasterURL();
    private String certFile = apimConfigurations.getContainerGatewayConfigs()
            .getOpenshiftGatewayConfigurations().getCertFile();
    private static String saTokenFile = apimConfigurations.getContainerGatewayConfigs()
            .getKubernetesGatewayConfigurations().getSaTokenFile();

    /**
     * @see ContainerBasedGatewayGenerator#createContainerBasedService(String gatewayServiceTemplate, String apiId,
     * String serviceName, String namespace, String label)
     */
    @Override
    public void createContainerBasedService(String serviceTemplate, String apiId, String serviceName, String namespace,
                                            String label) throws GatewayException {
        if (masterURL != null) {

            List<HasMetadata> resources;
            System.setProperty("kubernetes.auth.tryKubeConfig", "false");
            System.setProperty("kubernetes.auth.tryServiceAccount", "true");

            Config config = new ConfigBuilder().withMasterUrl(masterURL)
                        .withClientCertFile(certFile).withOauthToken(ContainerBasedGatewayUtils
                            .getServiceAccountAccessToken()).build();

            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
            OpenShiftClient client = kubernetesClient.adapt(OpenShiftClient.class);

            try (InputStream inputStream = IOUtils.toInputStream(serviceTemplate)) {

                resources = client.load(inputStream).get();
                if (resources.get(0) == null) {
                    throw new GatewayException("No resources loaded from Service Definition: " +
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
                }
                HasMetadata resource = resources.get(0);
                if (resource instanceof Service) {

                    Service result = client.services().inNamespace(namespace).load(inputStream).create();
                    log.info("Created Service : " + result.getMetadata().getName());

                    //todo : return the Https and https URLs form here as an array.

                } else {
                    throw new GatewayException("Loaded Resource is not a Kubernetes Service ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
                }
            } catch (IOException e) {
                throw new GatewayException("Client cannot load the file for service " + serviceName + "as an " +
                        "InputStream", e, ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);

            }
        }
    }

    /**
     * @see ContainerBasedGatewayGenerator#createContainerBasedDeployment(String deploymentTemplate, String apiId,
     * String deploymentName, String namespace, String label)
     */
    @Override
    public void createContainerBasedDeployment(String deploymentTemplate, String apiId, String deploymentName,
                                               String namespace, String label) throws GatewayException {

        if (masterURL != null) {

            System.setProperty("kubernetes.auth.tryKubeConfig", "false");
            System.setProperty("kubernetes.auth.tryServiceAccount", "true");

            Config config = new ConfigBuilder().withMasterUrl(masterURL)
                    .withClientCertFile(certFile).withOauthToken(ContainerBasedGatewayUtils
                            .getServiceAccountAccessToken()).build();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
            OpenShiftClient client = kubernetesClient.adapt(OpenShiftClient.class);
             List<HasMetadata> resources;

            try (InputStream inputStream = IOUtils.toInputStream(deploymentTemplate)) {
                resources = client.load(inputStream).get();

                if (resources.get(0) == null) {
                    client.close();
                    throw new GatewayException("No resources loaded from definition : ",
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
                }

                HasMetadata resource = resources.get(0);
                if (resource instanceof Deployment) {
                    Deployment result =
                            client.extensions().deployments().inNamespace(namespace).load(inputStream).create();
                    log.info("Created deployment : ", result.getMetadata().getName());

                } else {
                    throw new GatewayException("Loaded Resource is not a Kubernetes Deployment ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
                }

            } catch (KubernetesClientException | IOException e) {
                throw new GatewayException("Client cannot load the file for deployment " + deploymentName + "as an " +
                        "InputStream", e, ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);

            }
        }
    }

    /**
     * @see ContainerBasedGatewayGenerator#removeContainerBasedGateway(String label, String apiId, String namespace)
     */
    @Override
    public void removeContainerBasedGateway(String label, String apiId, String namespace) throws GatewayException {

        if (masterURL != null) {
            System.setProperty("kubernetes.auth.tryKubeConfig", "false");
            System.setProperty("kubernetes.auth.tryServiceAccount", "true");

            Config config = new ConfigBuilder().withMasterUrl(masterURL)
                    .withClientCertFile(certFile).withOauthToken(getServiceAccountAccessToken()).build();
            KubernetesClient client = new DefaultKubernetesClient(config);;
            try {
                client.services().inNamespace(namespace).withName(label +
                        ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX).delete();
                client.extensions().deployments().inNamespace(namespace).withName(label +
                        ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX).delete();
                //remove the configuration files from the product as well
                //ContainerBasedGatewayUtils.deleteKubeConfig(new File(directory));
                log.info("Completed Deleting the Gateway deployment and service from Container");
            } catch (KubernetesClientException e) {
                throw new GatewayException("Error in Removing the Container based Gateway Resources",
                        ExceptionCodes.CONTAINER_GATEWAY_REMOVAL_FAILED);

            }
        }

    }

    @Override
    public void createContainerGateway(String apiId, String label) throws GatewayException {

        Map<String , String> templateValues = new HashMap<>();
        templateValues.put(ContainerBasedGatewayConstants.NAMESPACE,
                apimConfigurations.getContainerGatewayConfigs().getKubernetesGatewayConfigurations().getNamespace());
        templateValues.put(ContainerBasedGatewayConstants.GATEWAY_LABEL, label);
        templateValues.put(ContainerBasedGatewayConstants.SERVICE_NAME, label +
                ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX);
        templateValues.put(ContainerBasedGatewayConstants.DEPLOYMENT_NAME, label + "-deployment");
        templateValues.put(ContainerBasedGatewayConstants.CONTAINER_NAME, label + "-container");
        templateValues.put(ContainerBasedGatewayConstants.DOCKER_IMAGE, apimConfigurations.getContainerGatewayConfigs()
                .getKubernetesGatewayConfigurations().getImage());
        templateValues.put(ContainerBasedGatewayConstants.API_CORE_URL, apimConfigurations.getContainerGatewayConfigs()
                .getKubernetesGatewayConfigurations().getApiCoreURL());
        templateValues.put(ContainerBasedGatewayConstants.BROKER_HOST, apimConfigurations.getContainerGatewayConfigs()
                .getKubernetesGatewayConfigurations().getBrokerHost());
        templateValues.put(ContainerBasedGatewayConstants.SERVICE_ACCOUNT_TOKEN_FILE,
                apimConfigurations.getContainerGatewayConfigs().getKubernetesGatewayConfigurations().getSaTokenFile());
        templateValues.put(ContainerBasedGatewayConstants.CERT_FILE_LOCATION,
                apimConfigurations.getContainerGatewayConfigs().getKubernetesGatewayConfigurations().getCertFile());

        ContainerBasedGatewayTemplateBuilder builder = new ContainerBasedGatewayTemplateBuilder();
        //Create gateway Service
        createContainerBasedService(builder.getGatewayServiceTemplate(templateValues), apiId,
                templateValues.get(ContainerBasedGatewayConstants.SERVICE_NAME),
                templateValues.get(ContainerBasedGatewayConstants.NAMESPACE), label);

        // Create gateway deployment
        createContainerBasedDeployment(builder.getGatewayDeploymentTemplate(templateValues),
                apiId, templateValues.get(ContainerBasedGatewayConstants.DEPLOYMENT_NAME),
                templateValues.get(ContainerBasedGatewayConstants.NAMESPACE), label);

        // todo : need to update the labels as well with the access URLs
        // todo : Check whether we can do this at the creation stage of labels - After testing kube
    }

    @Override
    public String getServiceAccountAccessToken() throws GatewayException {
        //todo : get this read from the kube secret
        String token;
        File tokenFile = new File(saTokenFile);
        try (InputStream inputStream = new FileInputStream(tokenFile);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            token = bufferedReader.readLine();

        } catch (FileNotFoundException e) {
            throw new GatewayException("Service Account Access Token file is not found in the given location", e,
                    ExceptionCodes.FILE_NOT_FOUND_IN_LOCATION);
        } catch (IOException e) {
            throw new GatewayException("Error in Reading Access Token File. " + saTokenFile, e,
                    ExceptionCodes.FILE_READING_EXCEPTION);
        }
        return token;
    }
}
