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


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ContainerBasedGatewayGenerator;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.template.ContainerBasedGatewayTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;


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
 * This is responsible to handle the gateways created in Kubernetes
 */
public class KubernetesGatewayImpl implements ContainerBasedGatewayGenerator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

  //  private static String carbonHome = System.getProperty(Constants.CARBON_HOME);
    private static APIMConfigurations apimConfigurations
            = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
    private String masterURL = apimConfigurations.getContainerGatewayConfigs().getKubernetesGatewayConfigurations()
            .getMasterURL();
    private String certFile = apimConfigurations.getContainerGatewayConfigs().getKubernetesGatewayConfigurations()
            .getCertFile();
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

            /*Though it is in the cluster or outside by reading access token from an accessible file can get the access
            token. so, handled It in a single place.*/
            Config config = new ConfigBuilder().withMasterUrl(masterURL)
                    .withClientCertFile(certFile).withOauthToken(getServiceAccountAccessToken()).build();

            KubernetesClient client = new DefaultKubernetesClient(config);

            try (InputStream inputStream = IOUtils.toInputStream(serviceTemplate)) {

                // todo : check whether this is working. If not use a fileInputStream
                resources = client.load(inputStream).get();
                if (resources.get(0) == null) {
                    throw new GatewayException("No resources loaded from service definition provided : ",
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
                }
                HasMetadata resource = resources.get(0);

                if (resource instanceof Service) {
                    // check whether there are existing service already
                    if (client.services().inNamespace(namespace).withLabel(label) != null) {
                        Service result = client.services().inNamespace(namespace).load(inputStream).create();
                        if (log.isDebugEnabled()) {
                            log.debug("The Kubernetes Service Definition : " + serviceTemplate);
                        }
                        log.info("Created Service : " + result.getMetadata().getName());
                    } else {
                        log.info("There exist a kubernetes service with the same name." + serviceName);
                    }
                    //todo : return gateway Https and https URLs form here as an array.
                } else {
                    throw new GatewayException("Loaded Resource is not a Kubernetes Service ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
                }
            } catch (IOException e) {
                throw new GatewayException("Client cannot load the service :  " + serviceName + "as an " +
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
                    .withClientCertFile(certFile).withOauthToken(getServiceAccountAccessToken()).build();
            KubernetesClient client = new DefaultKubernetesClient(config);
            List<HasMetadata> resources;

            try (InputStream inputStream = IOUtils.toInputStream(deploymentTemplate)) {

                resources = client.load(inputStream).get();

                if (resources.get(0) == null) {
                    client.close();
                    throw new GatewayException("No resources loaded from deployment definition provided : ",
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
                }
                HasMetadata resource = resources.get(0);
                if (resource instanceof Deployment) {

                    // check whether there are existing service already
                    if (client.extensions().deployments().inNamespace(namespace).withLabel(label) != null) {
                        Deployment result =
                                client.extensions().deployments().inNamespace(namespace).load(inputStream).create();
                        if (log.isDebugEnabled()) {
                            log.debug("The Kubernetes Deployment Definition : " + deploymentTemplate);
                        }
                        log.info("Created deployment : ", result.getMetadata().getName());
                    } else {
                        log.info("There exist a kubernetes deployment with the same name." + deploymentName);
                    }

                } else {
                    throw new GatewayException("Loaded Resource is not a Kubernetes Deployment ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
                }

            } catch (KubernetesClientException | IOException e) {
                throw new GatewayException("Client cannot load the deployment " + deploymentName + "as an " +
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
            KubernetesClient client = new DefaultKubernetesClient(config);
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

    /**
     * @see ContainerBasedGatewayGenerator#createContainerGateway(String, String)
     */
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
        // todo : after configuring load balancer type for K8 and openshift service
    }

    /**
     * @see ContainerBasedGatewayGenerator#getServiceAccountAccessToken()
     */
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
