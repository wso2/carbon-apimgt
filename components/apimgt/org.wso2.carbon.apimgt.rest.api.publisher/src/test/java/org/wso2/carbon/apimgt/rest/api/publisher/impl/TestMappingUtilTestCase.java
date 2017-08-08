/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.publisher.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;


public class TestMappingUtilTestCase {

    @Test(description = "APIDTO mapping test case")
    void testAPItoAPIDTOMappingAndAPIDTOtoAPIMapping() throws IOException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();

        List<String> userSpecificPermission = new ArrayList<>();
        userSpecificPermission.add("userPermissions");

        api.setUserSpecificApiPermissions(userSpecificPermission);

        //Test mappings from API to APIDTO
        APIDTO apidto = MappingUtil.toAPIDto(api);
        assertEquals(api.getId(), apidto.getId());
        assertEquals(api.getName(), apidto.getName());
        assertEquals(api.getVersion(), apidto.getVersion());
        assertEquals(api.getContext(), apidto.getContext());
        assertEquals(api.getDescription(), apidto.getDescription());
        assertEquals((Boolean)api.isDefaultVersion(), apidto.getIsDefaultVersion());
        assertEquals(api.getVisibility().name(), apidto.getVisibility().name());
        assertEquals(api.isResponseCachingEnabled(), Boolean.parseBoolean(apidto.getResponseCaching()));
        assertEquals((Integer)api.getCacheTimeout(), apidto.getCacheTimeout());
        assertEquals(api.getVisibleRoles().size(), apidto.getVisibleRoles().size());
        assertEquals(api.getProvider(), apidto.getProvider());
        assertEquals(api.getApiPermission(), apidto.getPermission());
        assertEquals(api.getLifeCycleStatus(), apidto.getLifeCycleStatus());
        assertEquals(api.getWorkflowStatus(), apidto.getWorkflowStatus());
        assertEquals(api.getTags().size(), apidto.getTags().size());
        assertEquals(api.getLabels().size(), apidto.getLabels().size());
        assertEquals(api.getTransport().size(), apidto.getTransport().size());
        assertEquals(api.getUserSpecificApiPermissions().size(), apidto.getUserPermissionsForApi().size());
        assertEquals(api.getPolicies().size(), apidto.getPolicies().size());
        assertEquals(api.getBusinessInformation().getBusinessOwner(),
                                                        apidto.getBusinessInformation().getBusinessOwner());
        assertEquals(api.getBusinessInformation().getBusinessOwnerEmail(),
                apidto.getBusinessInformation().getBusinessOwnerEmail());
        assertEquals(api.getBusinessInformation().getTechnicalOwner(),
                apidto.getBusinessInformation().getTechnicalOwner());
        assertEquals(api.getBusinessInformation().getTechnicalOwnerEmail(),
                apidto.getBusinessInformation().getTechnicalOwnerEmail());
        assertEquals((Boolean)api.getCorsConfiguration().isAllowCredentials(),
                apidto.getCorsConfiguration().getAccessControlAllowCredentials());
        assertEquals((Boolean)api.getCorsConfiguration().isEnabled(),
                apidto.getCorsConfiguration().getCorsConfigurationEnabled());
        assertEquals(api.getCorsConfiguration().getAllowHeaders(),
                apidto.getCorsConfiguration().getAccessControlAllowHeaders());
        assertEquals(api.getCorsConfiguration().getAllowMethods(),
                apidto.getCorsConfiguration().getAccessControlAllowMethods());
        assertEquals(api.getCorsConfiguration().getAllowOrigins(),
                apidto.getCorsConfiguration().getAccessControlAllowOrigins());

        assertEquals(api.getEndpoint().get("TestEndpoint").getId(),
                apidto.getEndpoint().get(0).getKey());

        assertEquals(api.getUriTemplates().get("getApisApiIdGet").getTemplateId(),
                apidto.getOperations().get(0).getId());
        assertEquals(api.getUriTemplates().get("getApisApiIdGet").getAuthType(),
                apidto.getOperations().get(0).getAuthType());
        assertEquals(api.getUriTemplates().get("getApisApiIdGet").getHttpVerb(),
                apidto.getOperations().get(0).getHttpVerb());
        assertEquals(api.getUriTemplates().get("getApisApiIdGet").getUriTemplate(),
                apidto.getOperations().get(0).getUritemplate());
        assertEquals(api.getUriTemplates().get("getApisApiIdGet").getPolicy().getPolicyName(),
                apidto.getOperations().get(0).getPolicy());

        assertEquals(api.getApiPolicy().getPolicyName(), apidto.getApiPolicy());
        assertEquals(api.getCreatedTime().toString(), apidto.getCreatedTime());
        assertEquals(api.getLastUpdatedTime().toString(), apidto.getLastUpdatedTime());

        //Test mappings from APIDTO to API
        API mappedAPI = MappingUtil.toAPI(apidto).build();
        assertEquals(apidto.getId(), mappedAPI.getId());
        assertEquals(apidto.getName(), mappedAPI.getName());
        assertEquals(apidto.getVersion(), mappedAPI.getVersion());
        assertEquals(apidto.getContext(), mappedAPI.getContext());
        assertEquals(apidto.getDescription(), mappedAPI.getDescription());
        assertEquals(apidto.getIsDefaultVersion(), (Boolean)mappedAPI.isDefaultVersion());
        assertEquals(apidto.getVisibility().name(), mappedAPI.getVisibility().name());
        assertEquals(Boolean.parseBoolean(apidto.getResponseCaching()), mappedAPI.isResponseCachingEnabled());
        assertEquals(apidto.getCacheTimeout(), (Integer)mappedAPI.getCacheTimeout());
        assertEquals(apidto.getVisibleRoles().size(), mappedAPI.getVisibleRoles().size());
        assertEquals(apidto.getProvider(), mappedAPI.getProvider());
        assertEquals(apidto.getPermission(), mappedAPI.getApiPermission());
        assertEquals(apidto.getLifeCycleStatus(), mappedAPI.getLifeCycleStatus());
        //Npt mapped it's intentional
        assertNull(mappedAPI.getWorkflowStatus());
        assertEquals(apidto.getTags().size(), mappedAPI.getTags().size());
        assertEquals(apidto.getLabels().size(), mappedAPI.getLabels().size());
        assertEquals(apidto.getTransport().size(), mappedAPI.getTransport().size());
        //Npt mapped it's intentional
        assertNull(mappedAPI.getUserSpecificApiPermissions());
        assertEquals(apidto.getPolicies().size(), mappedAPI.getPolicies().size());
        assertEquals(apidto.getBusinessInformation().getBusinessOwner(),
                mappedAPI.getBusinessInformation().getBusinessOwner());
        assertEquals(apidto.getBusinessInformation().getBusinessOwnerEmail(),
                mappedAPI.getBusinessInformation().getBusinessOwnerEmail());
        assertEquals(apidto.getBusinessInformation().getTechnicalOwner(),
                mappedAPI.getBusinessInformation().getTechnicalOwner());
        assertEquals(apidto.getBusinessInformation().getTechnicalOwnerEmail(),
                mappedAPI.getBusinessInformation().getTechnicalOwnerEmail());
        assertEquals(apidto.getCorsConfiguration().getAccessControlAllowCredentials(),
                (Boolean)mappedAPI.getCorsConfiguration().isAllowCredentials());
        assertEquals(apidto.getCorsConfiguration().getCorsConfigurationEnabled(),
                (Boolean)mappedAPI.getCorsConfiguration().isEnabled());
        assertEquals(apidto.getCorsConfiguration().getAccessControlAllowHeaders(),
                mappedAPI.getCorsConfiguration().getAllowHeaders());
        assertEquals(apidto.getCorsConfiguration().getAccessControlAllowMethods(),
                mappedAPI.getCorsConfiguration().getAllowMethods());
        assertEquals(apidto.getCorsConfiguration().getAccessControlAllowOrigins(),
                mappedAPI.getCorsConfiguration().getAllowOrigins());

        assertEquals(apidto.getEndpoint().get(0).getKey(),
                mappedAPI.getEndpoint().get("TestEndpoint").getId());

        assertEquals(apidto.getOperations().get(0).getId(),
                mappedAPI.getUriTemplates().get("getApisApiIdGet").getTemplateId());
        assertEquals(apidto.getOperations().get(0).getAuthType(),
                mappedAPI.getUriTemplates().get("getApisApiIdGet").getAuthType());
        assertEquals(apidto.getOperations().get(0).getHttpVerb(),
                mappedAPI.getUriTemplates().get("getApisApiIdGet").getHttpVerb());
        assertEquals(apidto.getOperations().get(0).getUritemplate(),
                mappedAPI.getUriTemplates().get("getApisApiIdGet").getUriTemplate());
        assertEquals(apidto.getOperations().get(0).getPolicy(),
                mappedAPI.getUriTemplates().get("getApisApiIdGet").getPolicy().getPolicyName());

        assertEquals(apidto.getApiPolicy(), mappedAPI.getApiPolicy().getPolicyName());
    }

    @Test(description = "API list to API list info mapping")
    void testAPIListToAPIListInfoMapping() {
        API api1 = SampleTestObjectCreator.createDefaultAPI().id("newId1").name("newName1").context("newContext1")
                 .description("newDesc1").provider("newProvider1")
                 .lifeCycleStatus("newStatus1").version("newVersion1")
                 .workflowStatus("newWorkflowStatus1").build();
        API api2 = SampleTestObjectCreator.createDefaultAPI().id("newId2").name("newName2").context("newContext2")
                .description("newDesc2").provider("newProvider2")
                .lifeCycleStatus("newStatus2").version("newVersion2")
                .workflowStatus("newWorkflowStatus2").build();
        List<API> apis = new ArrayList<>();
        apis.add(api1);
        apis.add(api2);
        APIListDTO apiListDTO = MappingUtil.toAPIListDTO(apis);
        assertEquals((Integer) apis.size(), apiListDTO.getCount());
        assertEquals(api1.getId(), apiListDTO.getList().get(0).getId());
        assertEquals(api1.getName(), apiListDTO.getList().get(0).getName());
        assertEquals(api1.getContext(), apiListDTO.getList().get(0).getContext());
        assertEquals(api1.getDescription(), apiListDTO.getList().get(0).getDescription());
        assertEquals(api1.getProvider(), apiListDTO.getList().get(0).getProvider());
        assertEquals(api1.getLifeCycleStatus(), apiListDTO.getList().get(0).getLifeCycleStatus());
        assertEquals(api1.getVersion(), apiListDTO.getList().get(0).getVersion());
        assertEquals(api1.getWorkflowStatus(), apiListDTO.getList().get(0).getWorkflowStatus());

        assertEquals(api2.getId(), apiListDTO.getList().get(1).getId());
        assertEquals(api2.getName(), apiListDTO.getList().get(1).getName());
        assertEquals(api2.getContext(), apiListDTO.getList().get(1).getContext());
        assertEquals(api2.getDescription(), apiListDTO.getList().get(1).getDescription());
        assertEquals(api2.getProvider(), apiListDTO.getList().get(1).getProvider());
        assertEquals(api2.getLifeCycleStatus(), apiListDTO.getList().get(1).getLifeCycleStatus());
        assertEquals(api2.getVersion(), apiListDTO.getList().get(1).getVersion());
        assertEquals(api2.getWorkflowStatus(), apiListDTO.getList().get(1).getWorkflowStatus());
    }

    @Test(description = "DocumentInfo to DocumentDTO mapping and vice versa")
    void testDocumentInfoToDocumentDTOInfoMappingAndViceVersa() {
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo().build();
        DocumentDTO documentDTO = MappingUtil.toDocumentDTO(documentInfo);
        //Test DocumentInfo to DocumentDTO mapping
        assertEquals(documentInfo.getName(), documentDTO.getName());
        assertEquals(documentInfo.getId(), documentDTO.getDocumentId());
        assertEquals(documentInfo.getOtherType(), documentDTO.getOtherTypeName());
        assertEquals(documentInfo.getSourceType().getType(), documentDTO.getSourceType().name());
        assertEquals(documentInfo.getSourceURL(), documentDTO.getSourceUrl());
        assertEquals(documentInfo.getFileName(), documentDTO.getFileName());
        assertEquals(documentInfo.getSummary(), documentDTO.getSummary());
        assertEquals(documentInfo.getVisibility().toString(), documentDTO.getVisibility().name());
        assertEquals(documentInfo.getType().toString(), documentDTO.getType().name());

        //Test DocumentDTO to DocumentInfo mapping
        DocumentInfo mappedDocumentInfo = MappingUtil.toDocumentInfo(documentDTO);
        assertEquals(mappedDocumentInfo.getName(), documentDTO.getName());
        assertEquals(mappedDocumentInfo.getId(), documentDTO.getDocumentId());
        assertEquals(mappedDocumentInfo.getOtherType(), documentDTO.getOtherTypeName());
        assertEquals(mappedDocumentInfo.getSourceType().getType(), documentDTO.getSourceType().name());
        assertEquals(mappedDocumentInfo.getSourceURL(), documentDTO.getSourceUrl());
        assertEquals(mappedDocumentInfo.getFileName(), documentDTO.getFileName());
        assertEquals(mappedDocumentInfo.getSummary(), documentDTO.getSummary());
        assertEquals(mappedDocumentInfo.getVisibility().toString(), documentDTO.getVisibility().name());
        assertEquals(mappedDocumentInfo.getType().toString(), documentDTO.getType().name());
    }

    @Test(description = "Document Info list to Document DTO list mapping")
    void testDocumentInfoListToDocumentDTOMapping() {
        DocumentInfo documentInfo1 = SampleTestObjectCreator.createDefaultDocumentationInfo()
                                    .id("newId1").name("newName1").fileName("newFile1")
                                    .summary("newSum1").build();
        DocumentInfo documentInfo2 = SampleTestObjectCreator.createDefaultDocumentationInfo()
                                    .id("newId2").name("newName2").fileName("newFile2")
                                    .summary("newSum2").build();
        List<DocumentInfo> documentInfos = new ArrayList<>();
        documentInfos.add(documentInfo1);
        documentInfos.add(documentInfo2);
        DocumentListDTO documentListDTO = MappingUtil.toDocumentListDTO(documentInfos);
        assertEquals((Integer) documentInfos.size(), documentListDTO.getCount());
        assertEquals(documentInfo1.getName(), documentListDTO.getList().get(0).getName());
        assertEquals(documentInfo1.getId(), documentListDTO.getList().get(0).getDocumentId());
        assertEquals(documentInfo1.getOtherType(), documentListDTO.getList().get(0).getOtherTypeName());
        assertEquals(documentInfo1.getSourceType().getType(), documentListDTO.getList().get(0).getSourceType().name());
        assertEquals(documentInfo1.getSourceURL(), documentListDTO.getList().get(0).getSourceUrl());
        assertEquals(documentInfo1.getFileName(), documentListDTO.getList().get(0).getFileName());
        assertEquals(documentInfo1.getSummary(), documentListDTO.getList().get(0).getSummary());
        assertEquals(documentInfo1.getVisibility().toString(), documentListDTO.getList().get(0).getVisibility().name());
        assertEquals(documentInfo1.getType().toString(), documentListDTO.getList().get(0).getType().name());

        assertEquals(documentInfo2.getName(), documentListDTO.getList().get(1).getName());
        assertEquals(documentInfo2.getId(), documentListDTO.getList().get(1).getDocumentId());
        assertEquals(documentInfo2.getOtherType(), documentListDTO.getList().get(1).getOtherTypeName());
        assertEquals(documentInfo2.getSourceType().getType(), documentListDTO.getList().get(1).getSourceType().name());
        assertEquals(documentInfo2.getSourceURL(), documentListDTO.getList().get(1).getSourceUrl());
        assertEquals(documentInfo2.getFileName(), documentListDTO.getList().get(1).getFileName());
        assertEquals(documentInfo2.getSummary(), documentListDTO.getList().get(1).getSummary());
        assertEquals(documentInfo2.getVisibility().toString(), documentListDTO.getList().get(1).getVisibility().name());
        assertEquals(documentInfo2.getType().toString(), documentListDTO.getList().get(1).getType().name());
    }

    @Test(description = "Application to Application DTO mapping")
    void testApplicationToApplicationDTOMapping() {
        Application application = SampleTestObjectCreator.createDefaultApplication();
        ApplicationDTO applicationDTO = MappingUtil.toApplicationDto(application);
        assertEquals(application.getId(), applicationDTO.getApplicationId());
        assertEquals(application.getDescription(), applicationDTO.getDescription());
        assertEquals(application.getName(), applicationDTO.getName());
        assertEquals(application.getCreatedUser(), applicationDTO.getSubscriber());
        assertEquals(application.getPolicy().getPolicyName(), applicationDTO.getThrottlingTier());
    }

    @Test(description = "Subscription to Subscription DTO mapping")
    void testSubscriptionToSubscriptionDTOMapping() {
        Policy subscriptionPolicy = SampleTestObjectCreator.goldSubscriptionPolicy;
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Application application = SampleTestObjectCreator.createDefaultApplication();
        String uuid = UUID.randomUUID().toString();
        Subscription subscription = new Subscription(uuid, application, api, subscriptionPolicy);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);
        SubscriptionDTO subscriptionDTO = MappingUtil.fromSubscription(subscription);
        assertEquals(subscription.getId(), subscriptionDTO.getSubscriptionId());
        assertEquals(subscription.getStatus().name(), subscriptionDTO.getSubscriptionStatus().name());
        assertEquals(subscription.getApplication().getId(), subscriptionDTO.getApplicationInfo().getApplicationId());
        assertEquals(subscription.getPolicy().getPolicyName(), subscriptionDTO.getPolicy());
    }

    @Test(description = "Subscription list to Subscription DTO list mapping")
    void testSubscriptionListToSubscriptionListDTOMapping() {
        Policy subscriptionPolicy1 = SampleTestObjectCreator.goldSubscriptionPolicy;
        API api1 = SampleTestObjectCreator.createDefaultAPI().name("newName1").build();
        Application application1 = SampleTestObjectCreator.createDefaultApplication();
        application1.setName("newNameApp1");
        String uuid1 = UUID.randomUUID().toString();
        Subscription subscription1 = new Subscription(uuid1, application1, api1, subscriptionPolicy1);
        subscription1.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);

        Policy subscriptionPolicy2 = SampleTestObjectCreator.silverSubscriptionPolicy;
        API api2 = SampleTestObjectCreator.createDefaultAPI().name("newName2").build();
        Application application2 = SampleTestObjectCreator.createDefaultApplication();
        application1.setName("newNameApp2");
        String uuid2 = UUID.randomUUID().toString();
        Subscription subscription2 = new Subscription(uuid2, application2, api2, subscriptionPolicy2);
        subscription2.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);

        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(subscription1);
        subscriptions.add(subscription2);

        SubscriptionListDTO subscriptionListDTO = MappingUtil.fromSubscriptionListToDTO(subscriptions, 10, 0);
        assertEquals((Integer)subscriptions.size(), subscriptionListDTO.getCount());
        assertEquals(subscription1.getId(), subscriptionListDTO.getList().get(0).getSubscriptionId());
        assertEquals(subscription1.getStatus().name(), subscriptionListDTO.getList()
                                                    .get(0).getSubscriptionStatus().name());
        assertEquals(subscription1.getApplication().getId(), subscriptionListDTO.getList().get(0)
                                                        .getApplicationInfo().getApplicationId());
        assertEquals(subscription1.getPolicy().getPolicyName(), subscriptionListDTO.getList().get(0).getPolicy());

        assertEquals(subscription2.getId(), subscriptionListDTO.getList().get(1).getSubscriptionId());
        assertEquals(subscription2.getStatus().name(), subscriptionListDTO.getList().get(1).
                                                            getSubscriptionStatus().name());
        assertEquals(subscription2.getApplication().getId(), subscriptionListDTO.getList().get(1)
                                                            .getApplicationInfo().getApplicationId());
        assertEquals(subscription2.getPolicy().getPolicyName(), subscriptionListDTO.getList().get(1).getPolicy());
    }

    @Test(description = "Endpoint to Endpoint DTO mapping and vice versa")
    void testEndpointToEndpointDTOMappingAndViceVersa() throws IOException {
        Endpoint endpoint = SampleTestObjectCreator.createMockEndpoint();
        EndPointDTO endPointDTO = MappingUtil.toEndPointDTO(endpoint);
        assertEquals(endpoint.getId(), endPointDTO.getId());
        assertEquals(endpoint.getName(), endPointDTO.getName());
        assertEquals(endpoint.getType(), endPointDTO.getType());
        assertEquals(endpoint.getMaxTps(), endPointDTO.getMaxTps());
        assertTrue(endpoint.getSecurity().contains(endPointDTO.getEndpointSecurity().getEnabled().toString()));

        Endpoint mappedEndpoint = MappingUtil.toEndpoint(endPointDTO);
        assertEquals(mappedEndpoint.getId(), endPointDTO.getId());
        assertEquals(mappedEndpoint.getName(), endPointDTO.getName());
        assertEquals(mappedEndpoint.getType(), endPointDTO.getType());
        assertEquals(mappedEndpoint.getMaxTps(), endPointDTO.getMaxTps());
        assertTrue(mappedEndpoint.getSecurity().contains(endPointDTO.getEndpointSecurity().getEnabled().toString()));
    }

    @Test(description = "Workflow response to Workflow response DTO mapping ")
    void testWorkflowResponseToWorkflowResponseDTOMapping() {
        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);
        WorkflowResponseDTO workflowResponseDTO = MappingUtil.toWorkflowResponseDTO(workflowResponse);
        assertEquals(workflowResponse.getWorkflowStatus().name(), workflowResponseDTO.getWorkflowStatus().name());
    }

    @Test(description = "Label list to Label list DTO mapping ")
    void testLabelsListToLabelListDTOMapping() {
        Label label1 = SampleTestObjectCreator.createLabel("label1").build();
        Label label2 = SampleTestObjectCreator.createLabel("label1").build();
        List<Label> labels = new ArrayList<>();
        labels.add(label1);
        labels.add(label2);
        LabelListDTO labelListDTO = MappingUtil.toLabelListDTO(labels);
        assertEquals((Integer)labels.size(), labelListDTO.getCount());
        assertEquals(label1.getId(), labelListDTO.getList().get(0).getLabelId());
        assertEquals(label1.getName(), labelListDTO.getList().get(0).getName());
        assertEquals(label2.getId(), labelListDTO.getList().get(1).getLabelId());
        assertEquals(label2.getName(), labelListDTO.getList().get(1).getName());
    }

}
