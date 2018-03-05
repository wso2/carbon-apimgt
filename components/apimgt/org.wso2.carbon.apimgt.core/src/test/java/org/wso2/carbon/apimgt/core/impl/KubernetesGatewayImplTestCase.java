/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KubernetesGatewayImplTestCase {

    private static final String NAMESPACE = "default";
    private static final String LABEL_SUFFIX = "1234";

    @Test
    public void testInitImplForGivenToken() throws Exception {

        try {
            KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
            Map<String, String> implParameters = new HashMap<>();
            implParameters.put(ContainerBasedGatewayConstants.MASTER_URL, "https://localhost:8443/");
            implParameters.put(ContainerBasedGatewayConstants.SA_TOKEN_FILE_NAME, "kubsatoken");
            kubernetesGateway.initImpl(implParameters);
            Assert.fail("Exception is not thrown when initializing the Openshift client");
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes
                    .ERROR_INITIALIZING_DEDICATED_CONTAINER_BASED_GATEWAY);
        }
    }

    @Test
    public void testInitImplWhenMasterURLIsMissing() throws Exception {

        try {
            KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
            Map<String, String> implParameters = new HashMap<>();
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
        Mockito.when(openShiftClient.services().inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withLabel(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(baseOperation);
        Mockito.when(baseOperation.delete()).thenReturn(true);

        Mockito.when(openShiftClient.extensions().deployments().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperation);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        kubernetesGateway.removeContainerBasedGateway("label", api);
        Mockito.verify(openShiftClient, Mockito.times(2)).services();
        Mockito.verify(openShiftClient, Mockito.times(4)).extensions();
    }

    @Test(expected = ContainerBasedGatewayException.class)
    public void testRemoveContainerBasedGatewayForException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = new KubernetesGatewayImpl();
        kubernetesGateway.setClient(openShiftClient);
        kubernetesGateway.setValues(createImplParametersMap());
        Mockito.when(openShiftClient.services().inNamespace(NAMESPACE)).thenThrow(KubernetesClientException.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        kubernetesGateway.removeContainerBasedGateway("label", api);
    }

    @Test
    public void testCreateContainerGateway() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(),
                getDeploymentResources(), getIngressResources());

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);
        Mockito.when(scalableResource.get()).thenReturn(null);

        Service service = createService(openShiftClient, nonNamespaceOperation);
        Deployment deployment = createDeployment(openShiftClient, nonNamespaceOperation, scalableResource);
        Ingress ingress = createIngress(openShiftClient, nonNamespaceOperation, scalableResource);

        Mockito.when(nonNamespaceOperation.create(Mockito.any())).thenReturn(service, deployment, ingress);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX,
                api);
        Mockito.verify(openShiftClient, Mockito.times(4)).load(Mockito.any());
        Mockito.verify(openShiftClient, Mockito.times(3)).services();
        Mockito.verify(openShiftClient, Mockito.times(6)).extensions();
    }

    @Test
    public void testGetResourcesFromTemplateWhenResourceIsEmpty() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        List<HasMetadata> serviceResources = new ArrayList<>();
        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(serviceResources);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
        }
    }

    @Test
    public void testGetResourcesFromTemplateWhenResourceIsNull() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.NO_RESOURCE_LOADED_FROM_DEFINITION);
        }
    }

    @Test
    public void testCreateServiceResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        HasMetadata invalidMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> serviceResources = new ArrayList<>();
        serviceResources.add(invalidMetadata);
        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(serviceResources);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateContainerGatewayForAlreadyAvailableResources() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(),
                getDeploymentResources(), getIngressResources());

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);

        BaseOperation baseOperation = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(serviceName)).thenReturn(baseOperation);
        Service service = Mockito.mock(Service.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(baseOperation.get()).thenReturn(service);

        String deploymentName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(deploymentName)).thenReturn(scalableResource);
        Deployment deployment = Mockito.mock(Deployment.class, Mockito.RETURNS_DEEP_STUBS);

        String ingressName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(ingressName)).thenReturn(scalableResource);
        Ingress ingress = Mockito.mock(Ingress.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.when(scalableResource.get()).thenReturn(deployment, ingress);

        API api = SampleTestObjectCreator.createDefaultAPI().context("/test/context/").build();

        kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX,
                api);
        Mockito.verify(openShiftClient, Mockito.times(4)).load(Mockito.any());
        Mockito.verify(openShiftClient, Mockito.times(2)).services();
        Mockito.verify(openShiftClient, Mockito.times(4)).extensions();
    }

    @Test
    public void testCreateServiceResourceForKubernetesClientException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources());

        NonNamespaceOperation nonNamespaceOperationService = Mockito.mock(NonNamespaceOperation.class);
        BaseOperation baseOperationService = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperationService);
        Mockito.when(nonNamespaceOperationService.withName(serviceName)).thenReturn(baseOperationService);
        Mockito.when(baseOperationService.get()).thenThrow(KubernetesClientException.class);
        API api = SampleTestObjectCreator.createDefaultAPI().context("").build();

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    @Test
    public void testCreateDeploymentResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        HasMetadata invalidMetadata = Mockito.mock(Service.class);
        List<HasMetadata> deploymentResources = new ArrayList<>();
        deploymentResources.add(invalidMetadata);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(), deploymentResources);

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);
        Mockito.when(scalableResource.get()).thenReturn(null);

        Service service = createService(openShiftClient, nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.create(Mockito.any())).thenReturn(service);

        API api = SampleTestObjectCreator.createDefaultAPI().build();

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateDeploymentResourceForKubernetesException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(),
                getDeploymentResources());

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);

        Service service = createService(openShiftClient, nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.create(Mockito.any())).thenReturn(service);

        String deploymentName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(deploymentName)).thenReturn(scalableResource);
        Mockito.when(scalableResource.get()).thenThrow(KubernetesClientException.class);

        API api = SampleTestObjectCreator.createDefaultAPI().build();

        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
        }
    }

    @Test
    public void testCreateIngressResourceForInvalidResource() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);
        Mockito.when(scalableResource.get()).thenReturn(null);

        HasMetadata invalidMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> ingressResources = new ArrayList<>();
        ingressResources.add(invalidMetadata);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(),
                getDeploymentResources(), ingressResources);

        Service service = createService(openShiftClient, nonNamespaceOperation);
        Deployment deployment = createDeployment(openShiftClient, nonNamespaceOperation, scalableResource);
        Mockito.when(nonNamespaceOperation.create(Mockito.any())).thenReturn(service, deployment);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
        } catch (ContainerBasedGatewayException e) {
            Assert.assertEquals(e.getErrorHandler(), ExceptionCodes.LOADED_RESOURCE_DEFINITION_IS_NOT_VALID);
        }
    }

    @Test
    public void testCreateIngressResourceForKubernetesException() throws Exception {

        OpenShiftClient openShiftClient = Mockito.mock(OpenShiftClient.class, Mockito.RETURNS_DEEP_STUBS);
        KubernetesGatewayImpl kubernetesGateway = getKubernetesGatewayImpl(openShiftClient);

        Mockito.when(openShiftClient.load(Mockito.any()).get()).thenReturn(getServiceResources(),
                getDeploymentResources(), getIngressResources());

        NonNamespaceOperation nonNamespaceOperation = Mockito.mock(NonNamespaceOperation.class);
        ScalableResource scalableResource = Mockito.mock(ScalableResource.class);
        Mockito.when(scalableResource.get()).thenReturn(null);

        Service service = createService(openShiftClient, nonNamespaceOperation);
        Deployment deployment = createDeployment(openShiftClient, nonNamespaceOperation, scalableResource);
        Mockito.when(nonNamespaceOperation.create(Mockito.any())).thenReturn(service, deployment);

        ScalableResource scalableResourceIngress = Mockito.mock(ScalableResource.class);
        String ingressName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(ingressName)).thenReturn(scalableResourceIngress);
        Mockito.when(scalableResourceIngress.get()).thenThrow(KubernetesClientException.class);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        try {
            kubernetesGateway.createContainerGateway(ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX
                    + LABEL_SUFFIX, api);
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
     * @param openShiftClient       Openshift client
     * @param nonNamespaceOperation NonNamespaceOperation instance
     * @param scalableResource      ScalableResource instance
     */
    private Ingress createIngress(OpenShiftClient openShiftClient, NonNamespaceOperation nonNamespaceOperation,
                                  ScalableResource scalableResource) {
        String ingressName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_INGRESS_SUFFIX;
        Mockito.when(openShiftClient.extensions().ingresses().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(ingressName)).thenReturn(scalableResource);
        Ingress ingress = Mockito.mock(Ingress.class, Mockito.RETURNS_DEEP_STUBS);
        return ingress;
    }

    /**
     * Create Deployment resource
     *
     * @param openShiftClient       Openshift client
     * @param nonNamespaceOperation NonNamespaceOperation instance
     * @param scalableResource      ScalableResource instance
     */
    private Deployment createDeployment(OpenShiftClient openShiftClient, NonNamespaceOperation nonNamespaceOperation,
                                        ScalableResource scalableResource) {
        String deploymentName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_DEPLOYMENT_SUFFIX;
        Mockito.when(openShiftClient.extensions().deployments().inNamespace(NAMESPACE))
                .thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(deploymentName)).thenReturn(scalableResource);
        Deployment deployment = Mockito.mock(Deployment.class, Mockito.RETURNS_DEEP_STUBS);
        return deployment;
    }

    /**
     * Create Service resource
     *
     * @param openShiftClient       Openshift client
     * @param nonNamespaceOperation NonNamespaceOperation instance
     */
    private Service createService(OpenShiftClient openShiftClient, NonNamespaceOperation nonNamespaceOperation) {
        BaseOperation baseOperation = Mockito.mock(BaseOperation.class);
        String serviceName = ContainerBasedGatewayConstants.PRIVATE_JET_API_PREFIX + LABEL_SUFFIX
                + ContainerBasedGatewayConstants.CMS_SERVICE_SUFFIX;
        Mockito.when(openShiftClient.services().inNamespace(NAMESPACE)).thenReturn(nonNamespaceOperation);
        Mockito.when(nonNamespaceOperation.withName(serviceName)).thenReturn(baseOperation);
        Mockito.when(baseOperation.get()).thenReturn(null);
        Service service = Mockito.mock(Service.class, Mockito.RETURNS_DEEP_STUBS);
        return service;
    }

    /**
     * Get ingress resources
     *
     * @return List<HasMetadata> list of ingress resources
     */
    private List<HasMetadata> getIngressResources() {
        HasMetadata ingressMetadata = Mockito.mock(Ingress.class);
        List<HasMetadata> ingressResources = new ArrayList<>();
        ingressResources.add(ingressMetadata);

        return ingressResources;
    }

    /**
     * Get service resources
     *
     * @return List<HasMetadata> list of service resources
     */
    private List<HasMetadata> getServiceResources() {

        HasMetadata serviceMetadata = Mockito.mock(Service.class);
        List<HasMetadata> serviceResources = new ArrayList<>();
        serviceResources.add(serviceMetadata);

        return serviceResources;
    }

    /**
     * Get deployment resources
     *
     * @return List<HasMetadata> list of deployment resources
     */
    private List<HasMetadata> getDeploymentResources() {
        HasMetadata deploymentMetadata = Mockito.mock(Deployment.class);
        List<HasMetadata> deploymentResources = new ArrayList<>();
        deploymentResources.add(deploymentMetadata);

        return deploymentResources;
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
        return implParameters;
    }
}
