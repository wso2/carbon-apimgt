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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.containermgt.k8scrd.*;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param parameterMap parameters that describe above attributes
     */
    @Override
    public void initManager(Map parameterMap) {

        setValues(parameterMap);
        setClient();
    }

    /**
     * Initial publish of the API in cluster
     * @param api API
     * @param apiIdentifier API Identifier
     * @throws RegistryException
     * @throws ParseException
     * @throws APIManagementException
     */
    @Override
    public void changeLCStateCreatedToPublished(API api, APIIdentifier apiIdentifier)
            throws RegistryException, ParseException, APIManagementException {

        if (!saToken.equals("") && !masterURL.equals("")) {

            String configmapName = deployConfigMap(api, apiIdentifier, openShiftClient, jwtSecurityCRName,
                    oauthSecurityCRName, basicAuthSecurityCRName, false);

            applyAPICustomResourceDefinition(openShiftClient, configmapName, replicas, apiIdentifier, true);
            log.info("Successfully deployed the [API] " + apiIdentifier.getApiName() + " in Kubernetes");


        } else {
            log.warn("Master URL and/or Service-account Token hasn't been Provided."
                    + " The [API] " + apiIdentifier.getApiName() + " will not be Published in Kubernetes");
        }
    }

    /**
     * Deletes the API from all the clusters it had been deployed
     * @param apiId API Identifier
     * @param clusterProperties Clusters which the API has published
     */
    @Override
    public void deleteAPI(APIIdentifier apiId, Map<String, String> clusterProperties) {

        String apiName = apiId.getApiName();

        Config config = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
                .withOauthToken(clusterProperties.get(SATOKEN)).withNamespace(clusterProperties.get(NAMESPACE)).build();

        OpenShiftClient client = new DefaultOpenShiftClient(config);
        CustomResourceDefinition apiCRD = client.customResourceDefinitions().withName(API_CRD_NAME).get();

        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, apiCRD);

        crdClient.withName(apiName.toLowerCase()).delete();

        log.info("Successfully deleted the [API] " + apiName);
    }

    /**
     * Represents the LC change "Demote to created"
     * Deletes the API from clusters
     * @param apiId API Identifier
     * @param clusterProperties Clusters which the API has published
     */
    @Override
    public void changeLCStatePublishedToCreated(APIIdentifier apiId, Map<String, String> clusterProperties) {

        deleteAPI(apiId, clusterProperties);
    }

    /**
     * Re-deploy an API
     * @param apiId API Identifier
     * @param clusterProperties Clusters which the API has published
     */
    @Override
    public void apiRepublish(API api, APIIdentifier apiId, Map<String, String> clusterProperties)
            throws ParseException, RegistryException, APIManagementException {

        String apiName = apiId.getApiName();

        Config config = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
                .withOauthToken(clusterProperties.get(SATOKEN)).withNamespace(clusterProperties.get(NAMESPACE)).build();

        OpenShiftClient client = new DefaultOpenShiftClient(config);

        String configMapName = deployConfigMap(api, apiId, client, clusterProperties.get(JWT_SECURITY_CR_NAME),
                clusterProperties.get(OAUTH2_SECURITY_CR_NAME), clusterProperties.get(BASICAUTH_SECURITY_CR_NAME),
                true);

        CustomResourceDefinition crd = client.customResourceDefinitions().withName(API_CRD_NAME).get();

        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, crd);

        APICustomResourceDefinition apiCustomResourceDefinition = crdClient.withName(apiName.toLowerCase()).get();

        apiCustomResourceDefinition.getSpec().setUpdateTimeStamp(getTimeStamp());
        apiCustomResourceDefinition.getSpec().getDefinition().setconfigmapName(configMapName);

        crdClient.createOrReplace(apiCustomResourceDefinition);
        log.info("Successfully Re-deployed the [API] " + apiName);


    }

    /**
     * Represent sthe LC change Block
     * Deletes the API from the clusters
     * @param apiId API Identifier
     * @param clusterProperties Clusters which the API has published
     */
    @Override
    public void changeLCStateToBlocked(APIIdentifier apiId, Map<String, String> clusterProperties) {

        deleteAPI(apiId, clusterProperties);
    }

    /**
     * Represents the LC change Blocked --> Republish
     * Redeploy the API CR with "override : false"
     * @param apiId API Identifier
     * @param clusterProperties Clusters which the API has published
     * @param configMapName Name of the Config Map
     */
    @Override
    public void changeLCStateBlockedToRepublished(APIIdentifier apiId, Map<String, String> clusterProperties,
                                                  String configMapName) {

        String apiName = apiId.getApiName();


        Config config = new ConfigBuilder().withMasterUrl(clusterProperties.get(MASTER_URL))
                .withOauthToken(clusterProperties.get(SATOKEN)).withNamespace(clusterProperties.get(NAMESPACE)).build();

        OpenShiftClient client = new DefaultOpenShiftClient(config);
        applyAPICustomResourceDefinition(client, configMapName, Integer.parseInt(clusterProperties.get(REPLICAS))
                , apiId, false);
        CustomResourceDefinition crd = client.customResourceDefinitions().withName(API_CRD_NAME).get();

        NonNamespaceOperation<APICustomResourceDefinition, APICustomResourceDefinitionList,
                DoneableAPICustomResourceDefinition, Resource<APICustomResourceDefinition,
                DoneableAPICustomResourceDefinition>> crdClient = getCRDClient(client, crd);

        List<APICustomResourceDefinition> apiCustomResourceDefinitionList = crdClient.list().getItems();

        for (APICustomResourceDefinition apiCustomResourceDefinition : apiCustomResourceDefinitionList) {

            if (apiCustomResourceDefinition.getMetadata().getName().equals(apiName.toLowerCase())) {

                apiCustomResourceDefinition.getSpec().setOverride(false);
                crdClient.createOrReplace(apiCustomResourceDefinition);
                log.info("Successfully Re-Published the [API] " + apiName);
                return;
            }
        }

        log.error("The requested custom resource for the [API] " + apiName + " was not found");


    }

    /**
     * To access the registry
     * @return
     */
    protected RegistryService getRegistryService() {
        return ServiceReferenceHolder.getInstance().getRegistryService();
    }

    /**
     * Deploys the API Custom resource kind in kubernetes
     * @param client , Openshift client
     * @param configmapName , Name of the configmap
     * @param replicas , number of replicas
     * @param apiIdentifier , APIIdentifier
     * @param override , Checks whether the API CR needs to be overrode or not
     */
    private void applyAPICustomResourceDefinition(OpenShiftClient client, String configmapName, int replicas,
                                                  APIIdentifier apiIdentifier, Boolean override) {

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

        Definition definition = new Definition();
        definition.setType(SWAGGER);
        definition.setconfigmapName(configmapName);

        APICustomResourceDefinitionSpec apiCustomResourceDefinitionSpec = new APICustomResourceDefinitionSpec();
        apiCustomResourceDefinitionSpec.setDefinition(definition);
        apiCustomResourceDefinitionSpec.setMode(MODE);
        apiCustomResourceDefinitionSpec.setReplicas(replicas);
        apiCustomResourceDefinitionSpec.setInterceptorConfName("");
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
    }

    /**
     * Deploys the configmap in the Kubernetes cluster
     * @param api , API
     * @param apiIdentifier , APIIdentifier
     * @param client , Openshift client
     * @param jwtSecurityCRName , Security kind name related to JWT
     * @param oauthSecurityCRName , Security kind name related to OAuth2
     * @param basicAuthSecurityCRName , Security kind name related to BasicAuth
     * @param update , checks whether the configmap needs to be updated or deploy independently
     * @return
     * @throws RegistryException
     * @throws APIManagementException
     * @throws ParseException
     */
    private String deployConfigMap(API api, APIIdentifier apiIdentifier, OpenShiftClient client, String jwtSecurityCRName,
                                   String oauthSecurityCRName, String basicAuthSecurityCRName, Boolean update)
            throws RegistryException, APIManagementException, ParseException {

        Registry registry = getRegistryService().getGovernanceUserRegistry();

        SwaggerCreator swaggerCreator = new SwaggerCreator(basicAuthSecurityCRName, jwtSecurityCRName,
                oauthSecurityCRName);
        String swagger = swaggerCreator.
                getOASDefinitionForPrivateJetMode(api, OASParserUtil.getAPIDefinition(apiIdentifier, registry));

        String configmapName = apiIdentifier.getApiName().toLowerCase() + apiIdentifier.getVersion();

        if (update) {

            configmapName = configmapName + "-up-" + getTimeStamp();
        }

        io.fabric8.kubernetes.client.dsl.Resource<ConfigMap, DoneableConfigMap> configMapResource = client
                .configMaps().inNamespace(client.getNamespace()).withName(configmapName);

        ConfigMap configMap = configMapResource.createOrReplace(new ConfigMapBuilder().withNewMetadata().
                withName(configmapName).withNamespace(namespace).endMetadata().
                withApiVersion(V1).addToData(apiIdentifier.getApiName() + ".json", swagger).build());

        log.info("Created [ConfigMap] " + configMap.getMetadata().getName() + " for [API] " + apiIdentifier.getApiName() +
                " in Kubernetes");

        if (swaggerCreator.isSecurityOauth2() && oauthSecurityCRName.equals("")) {
            log.warn("OAuth2 security custom resource name has not been provided,"
                    + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via OAuth2 tokens");
        }

        if (swaggerCreator.isSecurityOauth2() && jwtSecurityCRName.equals("")) {
            log.warn("JWT security custom resource name has not been provided,"
                    + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via JWT tokens");
        }

        if (swaggerCreator.isSecurityBasicAuth() && basicAuthSecurityCRName.equals("")) {
            log.warn("Basic-Auth security custom resource name has not been provided,"
                    + " The [API] " + apiIdentifier.getApiName() + " may not be able to invoke via BasicAuth tokens");
        }
        return configmapName;
    }

    /**
     * Sets the attributes of the object
     * @param parameterMap Cluster info in Map<clusterName, clusterInfo> format
     */
    private void setValues(Map<String, Map<String, String>> parameterMap) {

        String clusterName = parameterMap.keySet().iterator().next();
        Map<String, String> clusterInfo = parameterMap.get(clusterName);

        this.masterURL = clusterInfo.get(MASTER_URL);
        this.saToken = clusterInfo.get(SATOKEN);
        this.namespace = clusterInfo.get(NAMESPACE);
        this.replicas = Integer.parseInt(clusterInfo.get(REPLICAS));
        this.clusterName = clusterName;
        this.jwtSecurityCRName = clusterInfo.get(JWT_SECURITY_CR_NAME);
        this.oauthSecurityCRName = clusterInfo.get(OAUTH2_SECURITY_CR_NAME);
        this.basicAuthSecurityCRName = clusterInfo.get(BASICAUTH_SECURITY_CR_NAME);
    }

    /**
     * Sets the openshift client( This supprots both the Openshift and Kubernetes)
     */
    private void setClient() {

        Config config = new ConfigBuilder().withMasterUrl(masterURL).withOauthToken(saToken).withNamespace(namespace)
                //Get keystore password to connect with local clusters
                .withClientKeyPassphrase(System.getProperty(CLIENT_KEY_PASSPHRASE)).build();

        this.openShiftClient = new DefaultOpenShiftClient(config);

    }

    /**
     * Gets the current timestamp in dd/mm/yyyy format
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
     * @param client , openshift client
     * @param crd , CustomResourceDefinition
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