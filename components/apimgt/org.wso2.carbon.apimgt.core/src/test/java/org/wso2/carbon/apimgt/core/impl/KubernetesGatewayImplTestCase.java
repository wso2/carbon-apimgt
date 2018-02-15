/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ScalableResource;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FileEncryptionUtility.class, IOUtils.class})
public class KubernetesGatewayImplTestCase {

    private String namespace = "default";
    private static final String TEMPLATE_BASE_PATH = "resources/template/container_gateway_templates/";
    private static final String SERVICE_TEMPLATE_NAME = "container_service_template.yaml";
    private static final String DEPLOYMENT_TEMPLATE_NAME = "container_deployment_template.yaml";
    private static final String INGRESS_TEMPLATE_NAME = "container_ingress_template.yaml";

    @Test
    public void testInitImplForGivenToken() throws Exception {

        FileEncryptionUtility fileEncryptionUtility = Mockito.mock(FileEncryptionUtility.class);
        PowerMockito.mockStatic(FileEncryptionUtility.class);
        PowerMockito.when(FileEncryptionUtility.getInstance()).thenReturn(fileEncryptionUtility);
        Mockito.when(fileEncryptionUtility.readFromEncryptedFile(Mockito.anyString())).thenReturn("Token");

        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        try {
            kubernetesGateway.initImpl(createImplParametersMap());
            Assert.fail("Exception is not thrown when initializing the Openshift client");
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    @Test
    public void testInitImplForException() throws Exception {

        FileEncryptionUtility fileEncryptionUtility = Mockito.mock(FileEncryptionUtility.class);
        PowerMockito.mockStatic(FileEncryptionUtility.class);
        PowerMockito.when(FileEncryptionUtility.getInstance()).thenReturn(fileEncryptionUtility);
        Mockito.when(fileEncryptionUtility.readFromEncryptedFile(Mockito.anyString()))
                .thenThrow(ContainerBasedGatewayException.class);

        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        try {
            kubernetesGateway.initImpl(createImplParametersMap());
            Assert.fail("Exception is not thrown when initializing the Openshift client");
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    @Test
    public void testInitImplWhenMasterURLIsMissing() throws Exception {

        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        Map<String, String> implParameters = new HashMap<>();
        try {
            kubernetesGateway.initImpl(implParameters);
            Assert.fail("Exception is not thrown when initializing the Openshift client");
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    @Test
    public void testInitImplWhenTokenIsMissing() throws Exception {

        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        Map<String, String> implParameters = new HashMap<>();
        implParameters.put(ContainerBasedGatewayConstants.MASTER_URL, "https://localhost:8443/");
        try {
            kubernetesGateway.initImpl(implParameters);
            Assert.fail("Exception is not thrown when initializing the Openshift client");
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    @Test
    public void testRemoveContainerBasedGateway() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperation = Mockito.mock(BaseOperation.class);
        Mockito.when(openShiftClient.services().inNamespace(namespace)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withLabel(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(baseOperation);
        Mockito.when(baseOperation.delete()).thenReturn(true);

        Mockito.when(openShiftClient.extensions().deployments().inNamespace(namespace))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(namespace))
                .thenReturn(nonNamespaceOperation);

        kubernetesGateway.removeContainerBasedGateway("label");
        Mockito.verify(openShiftClient, Mockito.times(2)).services();
        Mockito.verify(openShiftClient, Mockito.times(4)).extensions();
    }

    @Test(expected = ContainerBasedGatewayException.class)
    public void testRemoveContainerBasedGatewayForException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        kubernetesGateway.setClient(openShiftClient);
        kubernetesGateway.setValues(createImplParametersMap());
        Mockito.when(openShiftClient.services().inNamespace(namespace)).thenThrow(KubernetesClientException.class);
        kubernetesGateway.removeContainerBasedGateway("label");
    }

    @Test
    public void testCreateContainerGateway() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        String ingressTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + INGRESS_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);
        createService(openShiftClient, labelSuffix);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        loadDeploymentResource(openShiftClient, deploymentTemplate, inputStreamDeployment);
        createDeployment(openShiftClient, labelSuffix);

        InputStream inputStreamIngress = Mockito.mock(InputStream.class);
        loadIngressResource(openShiftClient, ingressTemplate, inputStreamIngress);
        createIngress(openShiftClient, labelSuffix);

        kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamService);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamDeployment);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamIngress);
        Mockito.verify(openShiftClient, Mockito.times(3)).services();
        Mockito.verify(openShiftClient, Mockito.times(6)).extensions();
    }

    @Test
    public void testGetResourcesFromTemplateWhenResourceIsEmpty() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));

        PowerMockito.mockStatic(IOUtils.class);
        InputStream inputStreamService = Mockito.mock(InputStream.class);
        PowerMockito.when(IOUtils.toInputStream(serviceTemplate)).thenReturn(inputStreamService);
        List<HasMetadata> serviceResources = new ArrayList<>();
        Mockito.when(openShiftClient.load(inputStreamService).get()).thenReturn(serviceResources);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
        }
    }

    @Test
    public void testGetResourcesFromTemplateWhenResourceIsNull() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));

        PowerMockito.mockStatic(IOUtils.class);
        InputStream inputStreamService = Mockito.mock(InputStream.class);
        PowerMockito.when(IOUtils.toInputStream(serviceTemplate)).thenReturn(inputStreamService);
        Mockito.when(openShiftClient.load(inputStreamService).get()).thenReturn(null);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
        }
    }

    @Test
    public void testGetResourcesFromTemplateForIOException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));

        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.when(IOUtils.toInputStream(serviceTemplate)).thenThrow(IOException.class);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);
        }
    }

    @Test
    public void testCreateServiceResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));

        PowerMockito.mockStatic(IOUtils.class);
        InputStream inputStreamService = Mockito.mock(InputStream.class);
        PowerMockito.when(IOUtils.toInputStream(serviceTemplate)).thenReturn(inputStreamService);
        HasMetadata invalidMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> serviceResources = new ArrayList<>();
        serviceResources.add(invalidMetadata);
        Mockito.when(openShiftClient.load(inputStreamService).get()).thenReturn(serviceResources);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateContainerGatewayForAlreadyAvailableResources() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        String ingressTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + INGRESS_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);

        NonNamespaceOperation nonNamespaceOperationService = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperationService = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(namespace)).thenReturn(nonNamespaceOperationService);
        Mockito.when(nonNamespaceOperationService.withName(serviceName)).thenReturn(baseOperationService);
        Service service = new Service();
        Mockito.when(baseOperationService.get()).thenReturn(service);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        loadDeploymentResource(openShiftClient, deploymentTemplate, inputStreamDeployment);

        NonNamespaceOperation nonNamespaceOperationDeployment = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceDeployment = Mockito.mock(ScalableResource.class);
        String deploymentName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(Mockito.anyString()))
                .thenReturn(nonNamespaceOperationDeployment);
        Mockito.when(nonNamespaceOperationDeployment.withName(deploymentName)).thenReturn(scalableResourceDeployment);
        Deployment deployment = new Deployment();
        Mockito.when(scalableResourceDeployment.get()).thenReturn(deployment);

        InputStream inputStreamIngress = Mockito.mock(InputStream.class);
        loadIngressResource(openShiftClient, ingressTemplate, inputStreamIngress);

        NonNamespaceOperation nonNamespaceOperationIngress = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceIngress = Mockito.mock(ScalableResource.class);
        String ingressName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(namespace))
                .thenReturn(nonNamespaceOperationIngress);
        Mockito.when(nonNamespaceOperationIngress.withName(ingressName)).thenReturn(scalableResourceIngress);
        Ingress ingress = new Ingress();
        Mockito.when(scalableResourceIngress.get()).thenReturn(ingress);

        kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamService);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamDeployment);
        Mockito.verify(openShiftClient, Mockito.times(2)).load(inputStreamIngress);
        Mockito.verify(openShiftClient, Mockito.times(2)).services();
        Mockito.verify(openShiftClient, Mockito.times(4)).extensions();
    }

    @Test
    public void testCreateServiceResourceForKubernetesClientException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);

        NonNamespaceOperation nonNamespaceOperationService = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperationService = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(namespace)).thenReturn(nonNamespaceOperationService);
        Mockito.when(nonNamespaceOperationService.withName(serviceName)).thenReturn(baseOperationService);
        Mockito.when(baseOperationService.get()).thenThrow(KubernetesClientException.class);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    @Test
    public void testCreateDeploymentResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);
        createService(openShiftClient, labelSuffix);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        PowerMockito.when(IOUtils.toInputStream(deploymentTemplate)).thenReturn(inputStreamDeployment);
        HasMetadata invalidMetadata = Mockito.mock(Service.class);
        List<HasMetadata> deploymentResources = new ArrayList<>();
        deploymentResources.add(invalidMetadata);
        Mockito.when(openShiftClient.load(inputStreamDeployment).get()).thenReturn(deploymentResources);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateDeploymentResourceForKubernetesException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);
        createService(openShiftClient, labelSuffix);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        loadDeploymentResource(openShiftClient, deploymentTemplate, inputStreamDeployment);

        NonNamespaceOperation nonNamespaceOperationDeployment = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceDeployment = Mockito.mock(ScalableResource.class);
        String deploymentName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(Mockito.anyString()))
                .thenReturn(nonNamespaceOperationDeployment);
        Mockito.when(nonNamespaceOperationDeployment.withName(deploymentName)).thenReturn(scalableResourceDeployment);
        Mockito.when(scalableResourceDeployment.get()).thenThrow(KubernetesClientException.class);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    @Test
    public void testCreateIngressResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        String ingressTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + INGRESS_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);
        createService(openShiftClient, labelSuffix);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        loadDeploymentResource(openShiftClient, deploymentTemplate, inputStreamDeployment);
        createDeployment(openShiftClient, labelSuffix);

        InputStream inputStreamIngress = Mockito.mock(InputStream.class);
        PowerMockito.when(IOUtils.toInputStream(ingressTemplate)).thenReturn(inputStreamIngress);
        HasMetadata invalidMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> ingressResources = new ArrayList<>();
        ingressResources.add(invalidMetadata);
        Mockito.when(openShiftClient.load(inputStreamIngress).get()).thenReturn(ingressResources);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateIngressResourceForKubernetesException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);
        String labelSuffix = "1234";

        String serviceTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + SERVICE_TEMPLATE_NAME));
        String deploymentTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + DEPLOYMENT_TEMPLATE_NAME));
        String ingressTemplate = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(TEMPLATE_BASE_PATH + INGRESS_TEMPLATE_NAME));
        PowerMockito.mockStatic(IOUtils.class);

        InputStream inputStreamService = Mockito.mock(InputStream.class);
        loadServiceResource(openShiftClient, serviceTemplate, inputStreamService);
        createService(openShiftClient, labelSuffix);

        InputStream inputStreamDeployment = Mockito.mock(InputStream.class);
        loadDeploymentResource(openShiftClient, deploymentTemplate, inputStreamDeployment);
        createDeployment(openShiftClient, labelSuffix);

        InputStream inputStreamIngress = Mockito.mock(InputStream.class);
        loadIngressResource(openShiftClient, ingressTemplate, inputStreamIngress);

        NonNamespaceOperation nonNamespaceOperationIngress = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceIngress = Mockito.mock(ScalableResource.class);
        String ingressName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(namespace))
                .thenReturn(nonNamespaceOperationIngress);
        Mockito.when(nonNamespaceOperationIngress.withName(ingressName)).thenReturn(scalableResourceIngress);
        Mockito.when(scalableResourceIngress.get()).thenThrow(KubernetesClientException.class);

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX
                    + labelSuffix);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    /**
     * Get Kubernetes Gateway Implementation
     *
     * @param openShiftClient Openshift client
     * @return kubernetesGateway
     */
    private KubernetesGatewayImpl getKubernetesGatewayImpl(OpenShiftClient openShiftClient) {
        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        kubernetesGateway.setClient(openShiftClient);
        kubernetesGateway.setValues(createImplParametersMap());
        return kubernetesGateway;
    }

    /**
     * Create Ingress resource
     *
     * @param openShiftClient Openshift client
     * @param labelSuffix     label suffix
     */
    private void createIngress(OpenShiftClient openShiftClient, String labelSuffix) {
        NonNamespaceOperation nonNamespaceOperationIngress = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceIngress = Mockito.mock(ScalableResource.class);
        String ingressName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(namespace))
                .thenReturn(nonNamespaceOperationIngress);
        Mockito.when(nonNamespaceOperationIngress.withName(ingressName)).thenReturn(scalableResourceIngress);
        Mockito.when(scalableResourceIngress.get()).thenReturn(null);
        Ingress ingress = Mockito.mock(Ingress.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(nonNamespaceOperationIngress.create(Mockito.any())).thenReturn(ingress);
    }

    /**
     * Create Deployment resource
     *
     * @param openShiftClient Openshift client
     * @param labelSuffix     label suffix
     */
    private void createDeployment(OpenShiftClient openShiftClient, String labelSuffix) {
        NonNamespaceOperation nonNamespaceOperationDeployment = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResourceDeployment = Mockito.mock(ScalableResource.class);
        String deploymentName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(Mockito.anyString()))
                .thenReturn(nonNamespaceOperationDeployment);
        Mockito.when(nonNamespaceOperationDeployment.withName(deploymentName)).thenReturn(scalableResourceDeployment);
        Mockito.when(scalableResourceDeployment.get()).thenReturn(null);
        Deployment deployment = Mockito.mock(Deployment.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(nonNamespaceOperationDeployment.create(Mockito.any())).thenReturn(deployment);
    }

    /**
     * Create Service resource
     *
     * @param openShiftClient Openshift client
     * @param labelSuffix     label suffix
     */
    private void createService(OpenShiftClient openShiftClient, String labelSuffix) {
        NonNamespaceOperation nonNamespaceOperationService = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperationService = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PER_API_GATEWAY_PREFIX + labelSuffix
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(namespace)).thenReturn(nonNamespaceOperationService);
        Mockito.when(nonNamespaceOperationService.withName(serviceName)).thenReturn(baseOperationService);
        Mockito.when(baseOperationService.get()).thenReturn(null);
        Service service = Mockito.mock(Service.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(nonNamespaceOperationService.create(Mockito.any())).thenReturn(service);
    }

    /**
     * load Ingress resource definition from the template
     *
     * @param openShiftClient    Openshift client
     * @param ingressTemplate    template value
     * @param inputStreamIngress input stream
     */
    private void loadIngressResource(OpenShiftClient openShiftClient, String ingressTemplate, InputStream
            inputStreamIngress) {
        PowerMockito.when(IOUtils.toInputStream(ingressTemplate)).thenReturn(inputStreamIngress);
        HasMetadata ingressMetadata = Mockito.mock(Ingress.class);
        List<HasMetadata> ingressResources = new ArrayList<>();
        ingressResources.add(ingressMetadata);
        Mockito.when(openShiftClient.load(inputStreamIngress).get()).thenReturn(ingressResources);
    }

    /**
     * load Service resource definition from the template
     *
     * @param openShiftClient    Openshift client
     * @param serviceTemplate    template value
     * @param inputStreamService input stream
     */
    private void loadServiceResource(OpenShiftClient openShiftClient, String serviceTemplate, InputStream
            inputStreamService) {
        PowerMockito.when(IOUtils.toInputStream(serviceTemplate)).thenReturn(inputStreamService);
        HasMetadata serviceMetadata = Mockito.mock(Service.class);
        List<HasMetadata> serviceResources = new ArrayList<>();
        serviceResources.add(serviceMetadata);
        Mockito.when(openShiftClient.load(inputStreamService).get()).thenReturn(serviceResources);
    }

    /**
     * load Deployment resource definition from the template
     *
     * @param openShiftClient       Openshift client
     * @param deploymentTemplate    template value
     * @param inputStreamDeployment input stream
     */
    private void loadDeploymentResource(OpenShiftClient openShiftClient, String deploymentTemplate, InputStream
            inputStreamDeployment) {
        PowerMockito.when(IOUtils.toInputStream(deploymentTemplate)).thenReturn(inputStreamDeployment);
        HasMetadata deploymentMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> deploymentResources = new ArrayList<>();
        deploymentResources.add(deploymentMetadata);
        Mockito.when(openShiftClient.load(inputStreamDeployment).get()).thenReturn(deploymentResources);
    }

    /**
     * Creates an implParameters with relevant details
     *
     * @return implParameters map
     */
    private Map<String, String> createImplParametersMap() {
        Map<String, String> implParameters = new HashMap<>();
        implParameters.put(ContainerBasedGatewayConstants.MASTER_URL, "https://localhost:8443/");
        implParameters.put(ContainerBasedGatewayConstants.NAMESPACE, "default");
        implParameters.put(ContainerBasedGatewayConstants.API_CORE_URL, "https://localhost:9443");
        implParameters.put(ContainerBasedGatewayConstants.BROKER_HOST, "https://localhost:5672");
        implParameters.put(ContainerBasedGatewayConstants.CMS_TYPE, ContainerBasedGatewayConstants.KUBERNETES);
        implParameters.put(ContainerBasedGatewayConstants.SA_TOKEN_FILE_NAME, "token_file");
        return implParameters;
    }
}
