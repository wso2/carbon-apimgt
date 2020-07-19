/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.containermgt;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.DeploymentStatus;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.*;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.Status;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants.*;

public class K8sManager implements ContainerManager {

    private static final Logger log = LoggerFactory.getLogger(K8sManager.class);

    private String masterURL;
    private String saToken;
    private String namespace;
    private int replicas;
    private String clusterName;
    private String jwtSecurityCRName;
    private String oauthSecurityCRName;
    private String basicAuthSecurityCRName;
    private OpenShiftClient openShiftClient;

    /**
     * This would initialize the class
     *
     * @param containerMgtInfoDetails parameters that describe above attributes
     */
    @Override
    public void initManager(JSONObject containerMgtInfoDetails) {

        setValues(containerMgtInfoDetails);
        setClient();
    }

    /**
     * Initial publish of the API in cluster
     *
     * @param api           API
     * @param apiIdentifier API Identifier
     * @throws RegistryException
     * @throws ParseException
     * @throws APIManagementException error while deploying API in Kubernetes
     */
    @Override
    public void changeLCStateCreatedToPublished(API api, APIIdentifier apiIdentifier, Registry registry)
            throws ParseException, APIManagementException {

        if (masterURL != null && saToken != null && !"".equals(saToken) && !"".equals(masterURL)) {

            String[] configmapNames = deployConfigMap(api, apiIdentifier, registry, openShiftClient, jwtSecurityCRName,
                    oauthSecurityCRName, basicAuthSecurityCRName, false);

            if (configmapNames != null) {
                applyAPICustomResourceDefinition(openShiftClient, configmapNames, replicas, apiIdentifier, true);
                log.info("Successfully deployed the [API] " + apiIdentifier.getApiName() + " in Kubernetes");
            }

        } else {
            try {
                //handling scenario APIM deployed in Kubernetes cluster
                String[] configmapNames = deployConfigMap(api, apiIdentifier, registry, openShiftClient, jwtSecurityCRName,
                        oauthSecurityCRName, basicAuthSecurityCRName, false);

                if (configmapNames != null) {
                    applyAPICustomResourceDefinition(openShiftClient, configmapNames, replicas, apiIdentifier, true);
                    log.info("Successfully deployed the [API] " + apiIdentifier.getApiName() + " in Kubernetes");
                }

            } catch (KubernetesClientException e) {
                log.error("Master URL and/or Service-account and/or Token hasn't been Provided."
                        + " The [API] " + apiIdentifier.getApiName() + " will not be Published in Kubernetes", e);
            }
        }
//        getPodStatus(openShiftClient,apiIdentifier);
    }

    /**
     * Deletes the API from all the clusters it had been deployed
     *
     * @param apiId                   API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    @Override
    public void deleteAPI(APIIdentifier apiId, JSONObject containerMgtInfoDetails) {

        String apiName = apiId.getApiName();

        JSONObject properties = (JSONObject) containerMgtInfoDetails.get(PROPERTIES);

        //get openshiftClient
        OpenShiftClient client = getClient(properties);
        if (client != null) {
            try {
                CustomResourceDefinition apiCRD = client.customResourceDefinitions().withName(API_CRD_NAME).get();

                NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                        DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                        DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, apiCRD);

                crdClient.withName(apiName.toLowerCase()).cascading(true).delete();

                log.info("Successfully deleted the [API] " + apiName);
            } catch (KubernetesClientException e) {
                log.error("Error occurred while deleting the API from kubernetes cluster.", e);
            }
        } else {
            log.error("Error occurred while establishing the connection to the Kubernetes cluster");
        }
    }

    /**
     * Represents the LC change "Demote to created"
     * Deletes the API from clusters
     *
     * @param apiId                   API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    @Override
    public void changeLCStatePublishedToCreated(APIIdentifier apiId, JSONObject containerMgtInfoDetails) {

        deleteAPI(apiId, containerMgtInfoDetails);
    }

    /**
     * Re-deploy an API
     *
     * @param apiId                   API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    @Override
    public void apiRepublish(API api, APIIdentifier apiId, Registry registry, JSONObject containerMgtInfoDetails)
            throws ParseException, APIManagementException {

        String apiName = apiId.getApiName();
        JSONObject propreties = (JSONObject) containerMgtInfoDetails.get(PROPERTIES);
        String jwtSecurity = "";
        String basicSecurity = "";
        String oauthSecurity = "";

        //get Openshift client object
        OpenShiftClient client = getClient(propreties);
        if (client != null) {
            if (propreties.get(JWT_SECURITY_CR_NAME) != null) {
                jwtSecurity = propreties.get(JWT_SECURITY_CR_NAME).toString();
            }
            if (propreties.get(OAUTH2_SECURITY_CR_NAME) != null) {
                oauthSecurity = propreties.get(OAUTH2_SECURITY_CR_NAME).toString();
            }
            if (propreties.get(BASICAUTH_SECURITY_CR_NAME) != null) {
                basicSecurity = propreties.get(BASICAUTH_SECURITY_CR_NAME).toString();
            }
            try {
                String[] configMapNames = deployConfigMap(api, apiId, registry, client,
                        jwtSecurity, oauthSecurity, basicSecurity, true);

                CustomResourceDefinition crd = client.customResourceDefinitions().withName(API_CRD_NAME).get();

                NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                        DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                        DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, crd);

                APICustomResourceDefinition apiCustomResourceDefinition = crdClient.withName(apiName.toLowerCase()).get();

                // if API CR does not exists in the cluster, publish the API instead of re-publish
                // (when the API is published in other deployment, select this cluster and re-publish)
                if (apiCustomResourceDefinition == null) {
                    applyAPICustomResourceDefinition(client, configMapNames, replicas, api.getId(), true);
                    log.info("Successfully deployed the [API] " + api.getId().getApiName() + " in Kubernetes");
                } else {
                    // re-publish if API CR is exists
                    apiCustomResourceDefinition.getSpec().setUpdateTimeStamp(getTimeStamp());
                    apiCustomResourceDefinition.getSpec().getDefinition().setSwaggerConfigmapNames(configMapNames);
                    //update with interceptors
                    Interceptors interceptors = new Interceptors();
                    interceptors.setBallerina(new String[]{});
                    interceptors.setJava(new String[]{});
                    apiCustomResourceDefinition.getSpec().getDefinition().setInterceptors(interceptors);

                    crdClient.createOrReplace(apiCustomResourceDefinition);
                    log.info("Successfully Re-deployed the [API] " + apiName);
                }
            } catch (KubernetesClientException e) {
                log.error("Error occurred while re-deploying the API in Kubernetes cluster", e);
            }

        } else {
            log.error("Error occurred while re-deploying the API in Kubernetes cluster. " +
                    "could not establish the connection to the cluster");
        }
    }

    /**
     * Represents the LC change Block
     * Deletes the API from the clusters
     *
     * @param apiId                   API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    @Override
    public void changeLCStateToBlocked(APIIdentifier apiId, JSONObject containerMgtInfoDetails) {

        deleteAPI(apiId, containerMgtInfoDetails);
    }

    /**
     * Represents the LC change Blocked --> Republish
     * Redeploy the API CR with "override : false"
     *
     * @param apiId                   API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     * @param configMapName           Name of the Config Map
     */
    @Override
    public void changeLCStateBlockedToRepublished(APIIdentifier apiId, JSONObject containerMgtInfoDetails,
                                                  String[] configMapName) {

        String apiName = apiId.getApiName();

        JSONObject propreties = (JSONObject) containerMgtInfoDetails.get(ContainerBasedConstants.PROPERTIES);
        //get openShiftClient Object
        OpenShiftClient client = getClient(propreties);
        if (client != null) {
            try {
                applyAPICustomResourceDefinition(client, configMapName, Integer.parseInt(propreties.get(REPLICAS).toString())
                        , apiId, false);
                CustomResourceDefinition crd = client.customResourceDefinitions().withName(API_CRD_NAME).get();

                NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                        DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                        DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, crd);

                List<APICustomResourceDefinition> apiCustomResourceDefinitionList = crdClient.list().getItems();

                for (APICustomResourceDefinition apiCustomResourceDefinition : apiCustomResourceDefinitionList) {

                    if (apiCustomResourceDefinition.getMetadata().getName().equals(apiName.toLowerCase())) {

                        apiCustomResourceDefinition.getSpec().setOverride(false);
                        //update with interceptors
                        Interceptors interceptors = new Interceptors();
                        interceptors.setBallerina(new String[]{});
                        interceptors.setJava(new String[]{});
                        apiCustomResourceDefinition.getSpec().getDefinition().setInterceptors(interceptors);
                        crdClient.createOrReplace(apiCustomResourceDefinition);
                        log.info("Successfully Re-Published the [API] " + apiName);
                        return;
                    }
                }
                log.error("The requested custom resource for the [API] " + apiName + " was not found");
            } catch (KubernetesClientException e) {
                log.error("Error occurred while Re-Publishing the [API] " + apiName, e);
            }
        } else {
            log.error("Error occurred while establishing the connection to the Kubernetes cluster");
        }
    }

    /**
     * To access the registry
     *
     * @return RegistryService
     */
    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    /**
     * Deploys the API Custom resource kind in kubernetes
     *
     * @param client         , Openshift client
     * @param configmapNames , Name of the configmap
     * @param replicas       , number of replicas
     * @param apiIdentifier  , APIIdentifier
     * @param override       , Checks whether the API CR needs to be overrode or not
     */
    private void applyAPICustomResourceDefinition(OpenShiftClient client, String[] configmapNames, int replicas,
                                                  APIIdentifier apiIdentifier, Boolean override) {

        if (client != null) {
            CustomResourceDefinitionList customResourceDefinitionList = client.customResourceDefinitions().list();
            List<CustomResourceDefinition> customResourceDefinitionItems = customResourceDefinitionList.getItems();
            CustomResourceDefinition apiCustomResourceDefinition = null;

            for (CustomResourceDefinition crd : customResourceDefinitionItems) {
                ObjectMeta metadata = crd.getMetadata();

                if (metadata != null && metadata.getName().equals(API_CRD_NAME)) {

                    apiCustomResourceDefinition = crd;
                }
            }

            if (apiCustomResourceDefinition != null) {
                log.info("Found [CRD] " + apiCustomResourceDefinition.getMetadata().getSelfLink());
            } else {

                log.error("Custom resource definition apis.wso2.com was not found in the specified cluster");
                return;
            }

            NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                    DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                    DoneableAPICustomResourceDefinition>> apiCrdClient = getCRDClient(client, apiCustomResourceDefinition);

            // assigning values and creating API cr
            Definition definition = new Definition();
            Interceptors interceptors = new Interceptors();
            interceptors.setBallerina(new String[]{});
            interceptors.setJava(new String[]{});
            definition.setType(SWAGGER);
            definition.setSwaggerConfigmapNames(configmapNames);
            definition.setInterceptors(interceptors);

            APICustomResourceDefinitionSpec apiCustomResourceDefinitionSpec = new APICustomResourceDefinitionSpec();
            apiCustomResourceDefinitionSpec.setDefinition(definition);
            apiCustomResourceDefinitionSpec.setMode(MODE);
            apiCustomResourceDefinitionSpec.setReplicas(replicas);
            apiCustomResourceDefinitionSpec.setOverride(override);
            apiCustomResourceDefinitionSpec.setUpdateTimeStamp("");

            Status status = new Status();

            APICustomResourceDefinition apiCustomResourceDef = new APICustomResourceDefinition();
            apiCustomResourceDef.setSpec(apiCustomResourceDefinitionSpec);
            apiCustomResourceDef.setStatus(status);
            apiCustomResourceDef.setApiVersion(API_VERSION);
            apiCustomResourceDef.setKind(CRD_KIND);
            ObjectMeta meta = new ObjectMeta();
            meta.setName(apiIdentifier.getApiName().toLowerCase());
            meta.setNamespace(client.getNamespace());
            apiCustomResourceDef.setMetadata(meta);
            apiCrdClient.createOrReplace(apiCustomResourceDef);

            log.info("Created [API-CR] apis.wso2.com/" + apiCustomResourceDef.getMetadata().getName() + " for the "
                    + "[API] " + apiIdentifier.getApiName());
        } else {
            log.error("Error occurred while creating [API-CR]. could not establish the connection to the cluster");
        }
    }

    /**
     * Deploys the configmap in the Kubernetes cluster
     *
     * @param api                     , API
     * @param apiIdentifier           , APIIdentifier
     * @param client                  , Openshift client
     * @param jwtSecurityCRName       , Security kind name related to JWT
     * @param oauthSecurityCRName     , Security kind name related to OAuth2
     * @param basicAuthSecurityCRName , Security kind name related to BasicAuth
     * @param update                  , checks whether the configmap needs to be updated or deploy independently
     * @return swaggerConfigmapNames
     * @throws APIManagementException
     * @throws ParseException
     */
    private String[] deployConfigMap(API api, APIIdentifier apiIdentifier, Registry registry, OpenShiftClient client, String jwtSecurityCRName,
                                     String oauthSecurityCRName, String basicAuthSecurityCRName, Boolean update)
            throws APIManagementException, ParseException {

        SwaggerCreator swaggerCreator = new SwaggerCreator(basicAuthSecurityCRName, jwtSecurityCRName,
                oauthSecurityCRName);
        String swagger = swaggerCreator.
                getOASDefinitionForPrivateJetMode(api, OASParserUtil.getAPIDefinition(apiIdentifier, registry));

        String configmapName = apiIdentifier.getApiName().toLowerCase() + "-swagger";

        if (update) {

            configmapName = configmapName + "-up-" + getTimeStamp();
        }

        String[] swaggerConfigmapNames = new String[]{configmapName};

        if (client != null) {
            io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource = client
                    .configMaps().inNamespace(client.getNamespace()).withName(configmapName);

            ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                    withName(configmapName).withNamespace(namespace).endMetadata().
                    withApiVersion(V1).addToData(apiIdentifier.getApiName() + ".json", swagger).build());

            log.info("Created [ConfigMap] " + configMap.getMetadata().getName() + " for [API] " + apiIdentifier.getApiName() +
                    " in Kubernetes");
            if (oauthSecurityCRName != null && swaggerCreator.isSecurityOauth2() && oauthSecurityCRName.equals("")) {
                log.warn("OAuth2 security custom resource name has not been provided,"
                        + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via OAuth2 tokens");
            }

            if (jwtSecurityCRName != null && swaggerCreator.isSecurityOauth2() && jwtSecurityCRName.equals("")) {
                log.warn("JWT security custom resource name has not been provided,"
                        + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via JWT tokens");
            }

            if (basicAuthSecurityCRName != null && swaggerCreator.isSecurityBasicAuth() && basicAuthSecurityCRName.equals("")) {
                log.warn("Basic-Auth security custom resource name has not been provided,"
                        + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via BasicAuth tokens");
            }
            return swaggerConfigmapNames;
        } else {
            log.error("Error occurred while deploying configmaps. could not establish the connection to the cluster");
        }

        return null;
    }

    /**
     * Gets the deployment status information of an API and sets details to DeploymentStatus Object
     *
     * @param apiIdentifier , APIIdentifier
     * @param clusterName   , name of the cluster that the API is deployed
     */
    public DeploymentStatus getPodStatus(APIIdentifier apiIdentifier, String clusterName) {
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        List<Map<String, String>> podsInfo = new ArrayList<>();
        int numberOfRunningPods = 0;

        Map<String, String> lablesMap = new HashMap<String, String>() {{
            put("app", apiIdentifier.getApiName().toLowerCase());
        }};

        PodList podList = openShiftClient.pods().inNamespace(openShiftClient.getNamespace()).withLabels(lablesMap).list();
        for (Pod pod : podList.getItems()) {
            Map<String, String> podStatus = new HashMap<>();
            podStatus.put("podName", pod.getMetadata().getName());
            podStatus.put("status", pod.getStatus().getPhase());
            podStatus.put("creationTimestamp", pod.getMetadata().getCreationTimestamp());
            if (pod.getStatus().getPhase().equals("Running")) {
                numberOfRunningPods++;
            }
            int numberOfContainersPerPod = 0;
            List<ContainerStatus> podStatuses = pod.getStatus().getContainerStatuses();
            for (ContainerStatus containerStatus : podStatuses) {
                if (containerStatus.getReady()) {
                    numberOfContainersPerPod++;
                }
                String ready = numberOfContainersPerPod + "/" + podStatuses.size();
                podStatus.put("ready", ready);
            }
            podsInfo.add(podStatus);
        }

        deploymentStatus.setPodsRunning(numberOfRunningPods);
        deploymentStatus.setPodStatus(podsInfo);
        deploymentStatus.setClusterName(clusterName);

        return deploymentStatus;
    }

    /**
     * Sets the attributes of the object
     *
     * @param containerMgtInfoDetails Cluster info in Map<clusterName, clusterInfo> format
     */
    private void setValues(JSONObject containerMgtInfoDetails) {

        this.clusterName = containerMgtInfoDetails.get(CLUSTER_NAME).toString();
        JSONObject properties = (JSONObject) containerMgtInfoDetails.get(ContainerBasedConstants.PROPERTIES);
        if (properties != null) {
            if (properties.get(MASTER_URL) != null && !"".equals(properties.get(MASTER_URL).toString())) {
                this.masterURL = properties.get(MASTER_URL).toString().replace("\\", "");
            }
            if (properties.get(SATOKEN) != null && !"".equals(properties.get(SATOKEN).toString())) {
                this.saToken = properties.get(SATOKEN).toString();
            }
            if (properties.get(NAMESPACE) != null && !"".equals(properties.get(NAMESPACE).toString())) {
                this.namespace = properties.get(NAMESPACE).toString();
            }
            if (properties.get(REPLICAS) != null) {
                this.replicas = Integer.parseInt(properties.get(REPLICAS).toString());
            }
            if (properties.get(JWT_SECURITY_CR_NAME) != null) {
                this.jwtSecurityCRName = properties.get(JWT_SECURITY_CR_NAME).toString();
            }
            if (properties.get(BASICAUTH_SECURITY_CR_NAME) != null) {
                this.basicAuthSecurityCRName = properties.get(BASICAUTH_SECURITY_CR_NAME).toString();
            }
            if (properties.get(OAUTH2_SECURITY_CR_NAME) != null) {
                this.oauthSecurityCRName = properties.get(OAUTH2_SECURITY_CR_NAME).toString();
            }
        }
    }

    /**
     * Sets the openshift client( This supprots both the Openshift and Kubernetes)
     */
    private void setClient() {
        if (masterURL != null && saToken != null && namespace != null &&
                !"".equals(saToken) && !"".equals(masterURL) && !"".equals(namespace)) {
            Config config = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withNamespace(namespace)
                    //Get keystore password to connect with local clusters
                    .withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
            this.openShiftClient = new DefaultOpenShiftClient(config);
        } else if ((masterURL == null && saToken == null &&  namespace != null) ||
                ("".equals(saToken) && "".equals(masterURL) && !"".equals(namespace))) {
            //When APIM deployed in kubernetes cluster
            Config config = new ConfigBuilder().withNamespace(namespace)
                    //Get keystore password to connect with local clusters
                    .withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();
            this.openShiftClient = new DefaultOpenShiftClient(config);
            log.info("creating client without satoken and mater URLs");
        } else {
            log.error("Failed to make the connection to the cluster");
        }
    }


    /**
     * Returns an OpenshiftClient Object
     */
    OpenShiftClient getClient(JSONObject properties) {
        if (properties.get(MASTER_URL) != null && properties.get(SATOKEN) != null && properties.get(NAMESPACE) != null &&
                !"".equals(properties.get(MASTER_URL)) &&
                !"".equals(properties.get(SATOKEN)) &&
                !"".equals(properties.get(NAMESPACE))) {
            Config config = new ConfigBuilder()
                    .withMasterUrl(properties.get(MASTER_URL).toString().replace("\\", ""))
                    .withOauthToken(properties.get(SATOKEN).toString())
                    .withNamespace(properties.get(NAMESPACE).toString())
                    .withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();

            return new DefaultOpenShiftClient(config);
        } else if ((properties.get(MASTER_URL) == null && properties.get(SATOKEN) == null && properties.get(NAMESPACE) != null) ||
                ("".equals(properties.get(MASTER_URL)) && "".equals(properties.get(SATOKEN))
                        && !"".equals(properties.get(NAMESPACE)))){
            Config config = new ConfigBuilder()
                    .withNamespace(properties.get(NAMESPACE).toString())
                    .withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();

            return new DefaultOpenShiftClient(config);
        }
        return null;
    }

    /**
     * Gets the current timestamp in dd/mm/yyyy format
     *
     * @return , formatted timestamp
     */
    private String getTimeStamp() {

        Date date = new Date();
        long time = date.getTime();
        Timestamp timestamp = new Timestamp(time);
        date.setTime(timestamp.getTime());
        String formattedDate = new SimpleDateFormat("ddMMyyyy").format(date);
        String formattedTime = new SimpleDateFormat("HH.mm").format(date);

        return (formattedDate + "-" + formattedTime);
    }

    /**
     * Creates the client for deploying custom resources
     *
     * @param client , openshift client
     * @param crd    , CustomResourceDefinition
     * @return , Custom resource client
     */
    private NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
            DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
            DoneableAPICustomResourceDefinition>> getCRDClient(OpenShiftClient client, CustomResourceDefinition crd) {

        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>> crdClient = client
                .customResources(crd, APICustomResourceDefinition.class, APICustomResourceDefinitionList.class,
                        DoneableAPICustomResourceDefinition.class);

        crdClient = ((MixedOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>>) crdClient).inNamespace(client.getNamespace());

        return crdClient;
    }
}
