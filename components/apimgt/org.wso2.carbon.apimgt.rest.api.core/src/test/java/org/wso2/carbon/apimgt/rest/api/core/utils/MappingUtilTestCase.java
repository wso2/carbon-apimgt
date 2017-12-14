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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.PolicyDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationSummaryDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.UriTemplateDTO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Test cases for MappingUtils class.
 */
public class MappingUtilTestCase {

    @Test
    public void convertToSubscriptionListDTOTest() {
        List<SubscriptionValidationData> validationDataList = new ArrayList<>();
        validationDataList.add(SampleTestObjectCreator.createSubscriptionValidationData());
        validationDataList.add(SampleTestObjectCreator.createSubscriptionValidationData());
        validationDataList.add(SampleTestObjectCreator.createSubscriptionValidationData());
        validationDataList.add(SampleTestObjectCreator.createSubscriptionValidationData());
        List<SubscriptionDTO> subscriptionListDTO = MappingUtil.convertToSubscriptionListDto(validationDataList);
        Assert.assertNotNull(subscriptionListDTO);
        Assert.assertEquals(validationDataList.size(), subscriptionListDTO.size());
        for (int i = 0; i < subscriptionListDTO.size(); i++) {
            Assert.assertEquals(subscriptionListDTO.get(i).getApiName(), validationDataList.get(i).getApiName());
            Assert.assertEquals(subscriptionListDTO.get(i).getApiContext(), validationDataList.get(i).getApiContext());
            Assert.assertEquals(subscriptionListDTO.get(i).getApiProvider(),
                    validationDataList.get(i).getApiProvider());
            Assert.assertEquals(subscriptionListDTO.get(i).getApiVersion(), validationDataList.get(i).getApiVersion());
        }
    }


    @Test
    public void toAPIListDTOTest() {
        List<API> apiList = new ArrayList<>();
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());
        apiList.add(SampleTestObjectCreator.createUniqueAPI().build());
        APIListDTO apDTOList = MappingUtil.toAPIListDTO(apiList);
        Assert.assertEquals(apiList.size(), apDTOList.getList().size());
    }

    @Test
    public void convertToLabelsTest() {
        List<LabelDTO> labelDTOList = new ArrayList<>();
        labelDTOList.add(SampleTestObjectCreator.createUniqueLabelDTO());
        labelDTOList.add(SampleTestObjectCreator.createUniqueLabelDTO());
        labelDTOList.add(SampleTestObjectCreator.createUniqueLabelDTO());
        labelDTOList.add(SampleTestObjectCreator.createUniqueLabelDTO());
        List<Label> labelList = MappingUtil.convertToLabels(labelDTOList);
        Assert.assertEquals(labelList.size(), labelDTOList.size());
        for (int i = 0; i < labelDTOList.size(); i++) {
            Assert.assertEquals(labelList.get(i).getName(), labelDTOList.get(i).getName());
            Assert.assertEquals(labelList.get(i).getAccessUrls(), labelDTOList.get(i).getAccessUrls());
        }

    }

    @Test
    public void convertToResourceListDtoTest() {
        List<UriTemplate> uriTemplateList = new ArrayList<>();
        uriTemplateList.add(SampleTestObjectCreator.createUniqueUriTemplate());
        uriTemplateList.add(SampleTestObjectCreator.createUniqueUriTemplate());
        uriTemplateList.add(SampleTestObjectCreator.createUniqueUriTemplate());
        uriTemplateList.add(SampleTestObjectCreator.createUniqueUriTemplate());
        List<UriTemplateDTO> uriTemplateDTOList = MappingUtil.convertToResourceListDto(uriTemplateList);
        Assert.assertEquals(uriTemplateDTOList.size(), uriTemplateList.size());
        for (int i = 0; i < uriTemplateDTOList.size(); i++) {
            Assert.assertEquals(uriTemplateList.get(i).getUriTemplate(), uriTemplateDTOList.get(i).getUriTemplate());
            Assert.assertEquals(uriTemplateList.get(i).getAuthType(), uriTemplateDTOList.get(i).getAuthType());
            Assert.assertEquals(uriTemplateList.get(i).getHttpVerb(), uriTemplateDTOList.get(i).getHttpVerb());
            Assert.assertEquals(uriTemplateList.get(i).getPolicy().getUuid(), uriTemplateDTOList.get(i).getPolicy());
        }
    }

    @Test
    public void convertToApplicationDtoListTest() {
        List<Application> applicationList = new ArrayList<>();
        applicationList.add(SampleTestObjectCreator.createRandomApplication());
        applicationList.add(SampleTestObjectCreator.createRandomApplication());
        applicationList.add(SampleTestObjectCreator.createRandomApplication());
        List<ApplicationDTO> applicationDTOList = MappingUtil.convertToApplicationDtoList(applicationList);
        Assert.assertEquals(applicationList.size(), applicationDTOList.size());
        for (int i = 0; i < applicationList.size(); i++) {
            Assert.assertEquals(applicationList.get(i).getName(), applicationDTOList.get(i).getName());
            Assert.assertEquals(applicationList.get(i).getId(), applicationDTOList.get(i).getApplicationId());
            Assert.assertEquals(applicationList.get(i).getPolicy().getUuid(), applicationDTOList.get(i).
                    getThrottlingTier());
            Assert.assertEquals(applicationList.get(i).getCreatedUser(), applicationDTOList.get(i).getSubscriber());
        }
    }

    @Test
    public void toRegistrationSummaryDTOTest() {
        RegistrationSummary registrationSummary = SampleTestObjectCreator.createUniqueRegistrationSummary();
        RegistrationSummaryDTO registrationSummaryDTO = MappingUtil.toRegistrationSummaryDTO(registrationSummary);
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getCredentials().getPassword(),
                registrationSummaryDTO.getKeyManagerInfo().getCredentials().getPassword());
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getCredentials().getUsername(),
                registrationSummaryDTO.getKeyManagerInfo().getCredentials().getUsername());
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getDcrEndpoint(),
                registrationSummaryDTO.getKeyManagerInfo().getDcrEndpoint());
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getIntrospectEndpoint(),
                registrationSummaryDTO.getKeyManagerInfo().getIntrospectEndpoint());
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getRevokeEndpoint(),
                registrationSummaryDTO.getKeyManagerInfo().getRevokeEndpoint());
        Assert.assertEquals(registrationSummary.getKeyManagerInfo().getTokenEndpoint(),
                registrationSummaryDTO.getKeyManagerInfo().getTokenEndpoint());
        Assert.assertEquals(registrationSummary.getAnalyticsInfo().getDasServerCredentials().getUsername(),
                registrationSummaryDTO.getAnalyticsInfo().getCredentials().getUsername());
        Assert.assertEquals(registrationSummary.getAnalyticsInfo().getDasServerCredentials().getPassword(),
                registrationSummaryDTO.getAnalyticsInfo().getCredentials().getPassword());
        Assert.assertEquals(registrationSummary.getAnalyticsInfo().getDasServerURL(),
                registrationSummaryDTO.getAnalyticsInfo().getServerURL());
        Assert.assertEquals(Boolean.valueOf(registrationSummary.getAnalyticsInfo().isEnabled()),
                Boolean.valueOf(registrationSummaryDTO.getAnalyticsInfo().getEnabled()));
        Assert.assertEquals(registrationSummary.getJwtInfo().getJwtHeader(),
                registrationSummaryDTO.getJwTInfo().getJwtHeader());
        Assert.assertEquals(Boolean.valueOf(registrationSummary.getJwtInfo().isEnableJWTGeneration()),
                Boolean.valueOf(registrationSummaryDTO.getJwTInfo().getEnableJWTGeneration()));
        Assert.assertEquals(registrationSummary.getThrottlingInfo().getDataPublisher().getReceiverURL(),
                registrationSummaryDTO.getThrottlingInfo().getServerURL());
        Assert.assertEquals(registrationSummary.getThrottlingInfo().getDataPublisher().getCredentials().getPassword(),
                registrationSummaryDTO.getThrottlingInfo().getCredentials().getPassword());
        Assert.assertEquals(registrationSummary.getThrottlingInfo().getDataPublisher().getCredentials().getUsername(),
                registrationSummaryDTO.getThrottlingInfo().getCredentials().getUsername());
    }

    @Test
    public void convertToPolicyDtoListTest() {
        Set<PolicyValidationData> policyValidationDataList = new LinkedHashSet<>();
        policyValidationDataList.add(SampleTestObjectCreator.createUniquePolicyValidationDataObject());
        policyValidationDataList.add(SampleTestObjectCreator.createUniquePolicyValidationDataObject());
        policyValidationDataList.add(SampleTestObjectCreator.createUniquePolicyValidationDataObject());
        List<PolicyDTO> policyDTOList = MappingUtil.convertToPolicyDtoList(policyValidationDataList);
        Assert.assertEquals(policyDTOList.size(), policyValidationDataList.size());

    }

    @Test
    public void toEndpointListDtoTest() {
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(SampleTestObjectCreator.createUniqueEndpoint());
        endpointList.add(SampleTestObjectCreator.createUniqueEndpoint());
        endpointList.add(SampleTestObjectCreator.createUniqueEndpoint());
        List<EndPointDTO> endPointDTOList = MappingUtil.toEndpointListDto(endpointList);
        Assert.assertEquals(endPointDTOList.size(), endpointList.size());
        for (int i = 0; i < endpointList.size(); i++) {
            Assert.assertEquals(endpointList.get(i).getEndpointConfig(), endPointDTOList.get(i).getEndpointConfig());
            Assert.assertEquals(endpointList.get(i).getId(), endPointDTOList.get(i).getId());
            Assert.assertEquals(endpointList.get(i).getType(), endPointDTOList.get(i).getType());
            Assert.assertEquals(endpointList.get(i).getName(), endPointDTOList.get(i).getName());
            Assert.assertEquals(endpointList.get(i).getSecurity(), endPointDTOList.get(i).getSecurity());
        }
    }

    @Test
    public void fromBlockConditionListToListDTOTest() {
        List<BlockConditions> blockConditionsList = new ArrayList<>();
        blockConditionsList.add(SampleTestObjectCreator.
                createUniqueBlockConditions(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE));
        blockConditionsList.add(SampleTestObjectCreator.
                createUniqueBlockConditions(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_API));
        blockConditionsList.add(SampleTestObjectCreator.
                createUniqueBlockConditions(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP));
        BlockingConditionListDTO blockingConditionDTOList = MappingUtil.
                fromBlockConditionListToListDTO(blockConditionsList);
        Assert.assertEquals(blockingConditionDTOList.getList().size(), blockConditionsList.size());
    }

    @Test
    public void fromBlockingConditionToDTOTest() {
        BlockConditions blockCondition = SampleTestObjectCreator.
                createUniqueBlockConditions(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP);
        BlockingConditionDTO blockingConditionDTo = MappingUtil.fromBlockingConditionToDTO(blockCondition);
        Assert.assertEquals(blockCondition.getUuid(), blockingConditionDTo.getUuid());
        Assert.assertEquals(blockCondition.getConditionType(), blockingConditionDTo.getConditionType());
        Assert.assertEquals(blockCondition.getConditionValue(), blockingConditionDTo.getConditionValue());
        Assert.assertEquals(Boolean.valueOf(blockCondition.isEnabled()),
                Boolean.valueOf(blockingConditionDTo.getEnabled()));
        Assert.assertEquals(Long.valueOf(APIUtils.ipToLong(blockCondition.getConditionValue())),
                Long.valueOf(blockingConditionDTo.getFixedIp()));

    }

    @Test
    public void fromBlockingConditionToDTOIPRangeTest() {
        BlockConditions blockConditionIPRANGE = SampleTestObjectCreator.
                createUniqueBlockConditions(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE);
        BlockingConditionDTO blockingConditionDTo = MappingUtil.fromBlockingConditionToDTO(blockConditionIPRANGE);
        Assert.assertEquals(blockConditionIPRANGE.getUuid(), blockingConditionDTo.getUuid());
        Assert.assertEquals(blockConditionIPRANGE.getConditionType(), blockingConditionDTo.getConditionType());
        Assert.assertEquals(blockConditionIPRANGE.getConditionValue(), blockingConditionDTo.getConditionValue());
        Assert.assertEquals(Boolean.valueOf(blockConditionIPRANGE.isEnabled()),
                Boolean.valueOf(blockingConditionDTo.getEnabled()));
        Assert.assertEquals(Long.valueOf(APIUtils.ipToLong(blockConditionIPRANGE.getEndingIP())),
                Long.valueOf(blockingConditionDTo.getEndingIP()));
        Assert.assertEquals(Long.valueOf(APIUtils.ipToLong(blockConditionIPRANGE.getStartingIP())),
                Long.valueOf(blockingConditionDTo.getStartingIP()));

        //Test for null handling
        blockConditionIPRANGE.setUuid(null);
        Assert.assertNull(MappingUtil.fromBlockingConditionToDTO(blockConditionIPRANGE));
    }
}
