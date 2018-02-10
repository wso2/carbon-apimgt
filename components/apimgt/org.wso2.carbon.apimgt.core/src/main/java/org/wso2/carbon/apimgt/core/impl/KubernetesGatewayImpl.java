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
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.ConfigBuilder;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.template.ContainerBasedGatewayTemplateBuilder;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is responsible to handle the gateways created in Kubernetes and Openshift
 */
public class KubernetesGatewayImpl extends ContainerBasedGatewayGenerator {

    private static final Logger log = LoggerFactory.getLogger(KubernetesGatewayImpl.class);
    private static final String TRY_KUBE_CONFIG = "kubernetes.auth.tryKubeConfig";
    private static final String TRY_SERVICE_ACCOUNT = "kubernetes.auth.tryServiceAccount";
    private String cmsType;
    private String masterURL;
    private String namespace;
    private String apiCoreUrl;
    private String brokerHost;
    private String saTokenFile;
    private OpenShiftClient client;

    /**
     * @see ContainerBasedGatewayGenerator#initImpl(Map)
     */
    @Override
    void initImpl(Map<String, String> implParameters) throws ContainerBasedGatewayException {
        masterURL = implParameters.get(ContainerBasedGatewayConstants.MASTER_URL);
        saTokenFile = implParameters.get(ContainerBasedGatewayConstants.SA_TOKEN_FILE);
        namespace = implParameters.get(ContainerBasedGatewayConstants.NAMESPACE);
        apiCoreUrl = implParameters.get(ContainerBasedGatewayConstants.API_CORE_URL);
        brokerHost = implParameters.get(ContainerBasedGatewayConstants.BROKER_HOST);
        cmsType = implParameters.get(ContainerBasedGatewayConstants.CMS_TYPE);

        configureClient();
    }

    /**
     * @see ContainerBasedGatewayGenerator#removeContainerBasedGateway(String)
     */
    @Override
    public void removeContainerBasedGateway(String label) throws ContainerBasedGatewayException {

        try {
            client.services().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY, label).delete();
            client.extensions().deployments().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY,
                    label).delete();
            client.extensions().ingresses().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY,
                    label).delete();

            log.info("Completed Deleting the Gateway deployment, service and ingress resources in " + cmsType);
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while removing container based gateway", e,
                    ExceptionCodes.CONTAINER_GATEWAY_REMOVAL_FAILED);
        }

    }

    /**
     * @see ContainerBasedGatewayGenerator#createContainerGateway(String)
     */
    @Override
    public void createContainerGateway(String label) throws ContainerBasedGatewayException {

        Map<String, String> templateValues = new HashMap<>();
        String serviceName = label + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        String deploymentName = label + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        String ingressName = label + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;

        templateValues.put(ContainerBasedGatewayConstants.NAMESPACE, namespace);
        templateValues.put(ContainerBasedGatewayConstants.GATEWAY_LABEL, label);
        templateValues.put(ContainerBasedGatewayConstants.SERVICE_NAME, serviceName);
        templateValues.put(ContainerBasedGatewayConstants.DEPLOYMENT_NAME, deploymentName);
        templateValues.put(ContainerBasedGatewayConstants.INGRESS_NAME, ingressName);
        templateValues.put(ContainerBasedGatewayConstants.CONTAINER_NAME, label
                + ContainerBasedGatewayConstants.CMS_CONTAINER_SUFFIX);
        templateValues.put(ContainerBasedGatewayConstants.API_CORE_URL, apiCoreUrl);
        templateValues.put(ContainerBasedGatewayConstants.BROKER_HOST, brokerHost);

        ContainerBasedGatewayTemplateBuilder builder = new ContainerBasedGatewayTemplateBuilder();

        // Create gateway service resource
        createServiceResource(builder.generateTemplate(templateValues,
                ContainerBasedGatewayConstants.GATEWAY_SERVICE_TEMPLATE), serviceName);

        // Create gateway deployment resource
        createDeploymentResource(builder.generateTemplate(templateValues,
                ContainerBasedGatewayConstants.GATEWAY_DEPLOYMENT_TEMPLATE), deploymentName);

        // Create gateway ingress resource
        createIngressResource(builder.generateTemplate(templateValues,
                ContainerBasedGatewayConstants.GATEWAY_INGRESS_TEMPLATE), ingressName);
        // todo : need to update the labels as well with the access URLs
        // todo : after configuring load balancer type for K8 and openshift service
    }

    /**
     * Configure Openshift client
     *
     * @throws ContainerBasedGatewayException if failed to configure Openshift client
     */
    private void configureClient() throws ContainerBasedGatewayException {

        System.setProperty(TRY_KUBE_CONFIG, "false");
        System.setProperty(TRY_SERVICE_ACCOUNT, "true");

        ConfigBuilder configBuilder = new ConfigBuilder().withMasterUrl(masterURL);

        if (!StringUtils.isEmpty(saTokenFile)) {
            configBuilder.withOauthToken(resolveToken("encrypted" + saTokenFile));
        }

        try {
            client = new DefaultOpenShiftClient(configBuilder.build());
        } catch (KubernetesClientException e) {
            String msg = "Error occurred while creating Default Openshift Client";
            throw new ContainerBasedGatewayException(msg, e, ExceptionCodes.ERROR_INITIALIZING_CONTAINER_BASED_GATEWAY);
        }

    }

    /**
     * Get resources from template
     *
     * @param template Template as a String
     * @return HasMetadata
     * @throws ContainerBasedGatewayException if failed to load resource from the template
     */
    private HasMetadata getResourcesFromTemplate(String template) throws ContainerBasedGatewayException {

        List<HasMetadata> resources;

        try (InputStream inputStream = IOUtils.toInputStream(template)) {

            resources = client.load(inputStream).get();
            if (resources.get(0) == null) {
                throw new ContainerBasedGatewayException("No resources loaded from the definition provided : ",
                        ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
            }
            return resources.get(0);

        } catch (IOException e) {
            throw new ContainerBasedGatewayException("Client cannot load any resource from the template :  "
                    + template, e, ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);
        }
    }

    /**
     * Create a service in cms
     *
     * @param serviceTemplate Service template as a String
     * @param serviceName     Name of the service
     * @throws ContainerBasedGatewayException if failed to create a service
     */
    private void createServiceResource(String serviceTemplate, String serviceName)
            throws ContainerBasedGatewayException {

        HasMetadata resource = getResourcesFromTemplate(serviceTemplate);

        try {
            if (resource instanceof Service) {
                // check whether there are existing service already
                if (client.services().inNamespace(namespace).withName(serviceName).get() == null) {
                    Service service = (Service) resource;
                    Service result = client.services().inNamespace(namespace).create(service);
                    if (log.isDebugEnabled()) {
                        log.debug("CMS Type: " + cmsType + ". The Service Definition : " + serviceTemplate);
                    }
                    log.info("Created Service : " + result.getMetadata().getName() + " in " + cmsType);
                } else {
                    log.info("There exist a service with the same name in " + cmsType + ". Service name : "
                            + serviceName);
                }
                //todo : return gateway Https and https URLs form here as an array.
            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Service in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway service in "
                    + cmsType + "!", e, ExceptionCodes.CONTAINER_GATEWAY_CREATION_FAILED);
        }

    }

    /**
     * Create a deployment in cms
     *
     * @param deploymentTemplate Deployment template as a String
     * @param deploymentName     Name of the deployment
     * @throws ContainerBasedGatewayException if failed to create a deployment
     */
    private void createDeploymentResource(String deploymentTemplate, String deploymentName)
            throws ContainerBasedGatewayException {

        HasMetadata resource = getResourcesFromTemplate(deploymentTemplate);

        try {
            if (resource instanceof Deployment) {
                // check whether there are existing service already
                if (client.extensions().deployments().inNamespace(namespace).withName(deploymentName).get() == null) {
                    Deployment deployment = (Deployment) resource;
                    Deployment result = client.extensions().deployments().inNamespace(namespace).create(deployment);
                    if (log.isDebugEnabled()) {
                        log.debug("CMS Type: " + cmsType + ". The Deployment Definition : " + deploymentTemplate);
                    }
                    log.info("Created Deployment : " + result.getMetadata().getName() + " in " + cmsType);
                } else {
                    log.info("There exist a deployment with the same name in " + cmsType + ". Deployment name : "
                            + deploymentName);
                }

            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Deployment in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway deployment in "
                    + cmsType + "!", e, ExceptionCodes.CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    /**
     * Create an Ingress resource in cms
     *
     * @param ingressTemplate Ingress template as a String
     * @param ingressName     Name of the ingress
     * @throws ContainerBasedGatewayException if failed to create a service
     */
    private void createIngressResource(String ingressTemplate, String ingressName)
            throws ContainerBasedGatewayException {

        HasMetadata resource = getResourcesFromTemplate(ingressTemplate);

        try {
            if (resource instanceof Ingress) {
                // check whether there are existing service already
                if (client.extensions().ingresses().inNamespace(namespace).withName(ingressName).get() == null) {
                    Ingress ingress = (Ingress) resource;
                    Ingress result = client.extensions().ingresses().inNamespace(namespace).create(ingress);
                    if (log.isDebugEnabled()) {
                        log.debug("CMS Type: " + cmsType + ". The Ingress Definition : " + ingressTemplate);
                    }
                    log.info("Created Ingress : " + result.getMetadata().getName() + " in " + cmsType);
                } else {
                    log.info("There exist an ingress with the same name in " + cmsType + ". Ingress name : "
                            + ingressName);
                }
                //todo : return gateway Https and https URLs form here as an array.
            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Service in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway ingress in "
                    + cmsType + "!", e, ExceptionCodes.CONTAINER_GATEWAY_CREATION_FAILED);
        }

    }

    /**
     * Get the token after decrypting using {@link FileEncryptionUtility#readFromEncryptedFile(java.lang.String)}
     *
     * @return service account token
     * @throws ContainerBasedGatewayException if an error occurs while resolving the token
     */
    private String resolveToken(String encryptedTokenFileName) throws ContainerBasedGatewayException {

        String token;
        try {
            String externalSATokenFilePath = System.getProperty(FileEncryptionUtility.CARBON_HOME)
                    + FileEncryptionUtility.SECURITY_DIR + File.separator + encryptedTokenFileName;
            token = FileEncryptionUtility.getInstance().readFromEncryptedFile(externalSATokenFilePath);
        } catch (APIManagementException e) {
            String msg = "Error occurred while resolving externally stored token";
            throw new ContainerBasedGatewayException(msg, e, ExceptionCodes.ERROR_INITIALIZING_CONTAINER_BASED_GATEWAY);
        }
        return StringUtils.replace(token, "\n", "");
    }
}
