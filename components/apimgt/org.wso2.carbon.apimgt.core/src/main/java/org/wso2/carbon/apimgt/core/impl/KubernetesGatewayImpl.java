/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.fabric8.kubernetes.client.Config;
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
import org.wso2.carbon.apimgt.core.models.API;
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
    private static final String DASH = "-";
    private static final String FORWARD_SLASH = "/";
    private String cmsType;
    private String masterURL;
    private String namespace;
    private String apiCoreUrl;
    private String brokerHost;
    private String saTokenFileName;
    private String gatewayHostname;
    private OpenShiftClient client;

    /**
     * @see ContainerBasedGatewayGenerator#initImpl(Map)
     */
    @Override
    void initImpl(Map<String, String> implParameters) throws ContainerBasedGatewayException {
        try {
            setValues(implParameters);
            setClient(new DefaultOpenShiftClient(buildConfig()));
        } catch (KubernetesClientException e) {
            String msg = "Error occurred while creating Default Openshift Client";
            throw new ContainerBasedGatewayException(msg, e,
                    ExceptionCodes.ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    /**
     * Set values for Openshift client
     */
    void setValues(Map<String, String> implParameters) {
        masterURL = implParameters.get(ContainerBasedGatewayConstants.MASTER_URL);
        saTokenFileName = implParameters.get(ContainerBasedGatewayConstants.SA_TOKEN_FILE_NAME);
        namespace = implParameters.get(ContainerBasedGatewayConstants.NAMESPACE);
        apiCoreUrl = implParameters.get(ContainerBasedGatewayConstants.API_CORE_URL);
        brokerHost = implParameters.get(ContainerBasedGatewayConstants.BROKER_HOST);
        cmsType = implParameters.get(ContainerBasedGatewayConstants.CMS_TYPE);
        gatewayHostname = implParameters.get(ContainerBasedGatewayConstants.GATEWAY_HOSTNAME);
        log.debug("master url: {} saTokenFileName: {} namespace: {} apiCoreUrl: {} brokerHost: {} cmsType: {} " +
                        "gatewayHostname: {}", masterURL, saTokenFileName, namespace, apiCoreUrl, brokerHost,
                cmsType, gatewayHostname);
    }

    /**
     * @see ContainerBasedGatewayGenerator#removeContainerBasedGateway(String, API) (String)
     */
    @Override
    public void removeContainerBasedGateway(String label, API api) throws ContainerBasedGatewayException {

        try {
            client.services().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY, label).delete();
            client.extensions().deployments().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY,
                    label).delete();
            client.extensions().ingresses().inNamespace(namespace).withLabel(ContainerBasedGatewayConstants.GATEWAY,
                    label).delete();

            log.info(String.format("Completed deleting the container gateway related %s deployment, service and " +
                    "ingress resources.", cmsType));
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while removing container based gateway", e,
                    ExceptionCodes.CONTAINER_GATEWAY_REMOVAL_FAILED);
        }

    }

    /**
     * @see ContainerBasedGatewayGenerator#createContainerGateway(String, API)
     */
    @Override
    public void createContainerGateway(String label, API api) throws ContainerBasedGatewayException {

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
        templateValues.put(ContainerBasedGatewayConstants.GATEWAY_HOSTNAME, generateSubDomain(api) + "."
                + gatewayHostname);

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
    }

    /**
     * Generate a sub domain for the Ingress based on the API's context
     *
     * @param api API
     * @return subDomain
     */
    private String generateSubDomain(API api) {

        String context = api.getContext();
        String[] contextValues = context.split(FORWARD_SLASH);

        StringBuffer stringBuffer = new StringBuffer();
        for (String contextValue : contextValues) {
            if (!contextValue.isEmpty()) {
                stringBuffer.append(contextValue + DASH);
            }
        }

        String subDomain = stringBuffer.toString();
        if (subDomain.endsWith(DASH)) {
            subDomain = subDomain.substring(0, subDomain.length() - 1);
        }
        log.debug("Sub domain: {} generated for the api: {}", subDomain, api.getName());
        return subDomain;
    }

    /**
     * Build configurations for Openshift client
     *
     * @throws ContainerBasedGatewayException if failed to configure Openshift client
     */
    private Config buildConfig() throws ContainerBasedGatewayException {

        System.setProperty(TRY_KUBE_CONFIG, "false");
        System.setProperty(TRY_SERVICE_ACCOUNT, "true");
        ConfigBuilder configBuilder;

        if (masterURL != null) {
            configBuilder = new ConfigBuilder().withMasterUrl(masterURL);
        } else {
            throw new ContainerBasedGatewayException("Kubernetes Master URL is not provided!", ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }

        if (!StringUtils.isEmpty(saTokenFileName)) {
            configBuilder.withOauthToken(resolveToken("encrypted" + saTokenFileName));
        }

        return configBuilder.build();
    }

    /**
     * Set Default Openshift client
     *
     * @param openShiftClient Openshift client
     */
    void setClient(OpenShiftClient openShiftClient) {
        this.client = openShiftClient;
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
            if (resources == null || resources.isEmpty()) {
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
                    log.debug("Deploying in CMS type: {} and the Service resource definition: {} ", cmsType,
                            serviceTemplate);
                    Service service = (Service) resource;
                    Service result = client.services().inNamespace(namespace).create(service);
                    log.info("Created Service : " + result.getMetadata().getName() + " in Namespace : "
                            + result.getMetadata().getNamespace() + " in " + cmsType);
                } else {
                    log.info("There exist a service with the same name in " + cmsType + ". Service name : "
                            + serviceName);
                }
            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Service in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway service in "
                    + cmsType + "!", e, ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
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
                    log.debug("Deploying in CMS type: {} and the Deployment resource definition: {} ", cmsType,
                            deploymentTemplate);
                    Deployment deployment = (Deployment) resource;
                    Deployment result = client.extensions().deployments().inNamespace(namespace).create(deployment);
                    log.info("Created Deployment : " + result.getMetadata().getName() + " in Namespace : "
                            + result.getMetadata().getNamespace() + " in " + cmsType);
                } else {
                    log.info("There exist a deployment with the same name in " + cmsType + ". Deployment name : "
                            + deploymentName);
                }

            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Deployment in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway deployment in "
                    + cmsType + "!", e, ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
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
                    log.debug("Deploying in CMS type: {} and the Ingress resource definition: {} ", cmsType,
                            ingressTemplate);
                    Ingress ingress = (Ingress) resource;
                    Ingress result = client.extensions().ingresses().inNamespace(namespace).create(ingress);
                    log.info("Created Ingress : " + result.getMetadata().getName() + " in Namespace : "
                            + result.getMetadata().getNamespace() + " in " + cmsType);
                } else {
                    log.info("There exist an ingress with the same name in " + cmsType + ". Ingress name : "
                            + ingressName);
                }
            } else {
                throw new ContainerBasedGatewayException("Loaded Resource is not a Service in " + cmsType + "! " +
                        resource, ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
            }
        } catch (KubernetesClientException e) {
            throw new ContainerBasedGatewayException("Error while creating container based gateway ingress in "
                    + cmsType + "!", e, ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
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
            throw new ContainerBasedGatewayException(msg, e,
                    ExceptionCodes.ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
        return StringUtils.replace(token, "\n", "");
    }
}
