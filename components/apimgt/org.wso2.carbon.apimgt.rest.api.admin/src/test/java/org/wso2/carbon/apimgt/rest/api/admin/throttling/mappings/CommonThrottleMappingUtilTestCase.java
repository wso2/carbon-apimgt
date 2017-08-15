/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.throttling.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CommonThrottleMappingUtil;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.IP_RANGE_TYPE;
import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.KB;

public class CommonThrottleMappingUtilTestCase {

    @Test(description = "Convert Bandwidth Throttle Limit DTO to Quota Policy")
    public void fromBandwidthThrottleLimitDtoToQuotaPolicyTest () throws Exception {
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType(PolicyConstants.BANDWIDTH_LIMIT_TYPE);
        BandwidthLimitDTO bandwidthLimitDTO = new BandwidthLimitDTO();
        bandwidthLimitDTO.setDataAmount(10);
        bandwidthLimitDTO.setDataUnit(KB);
        throttleLimitDTO.setBandwidthLimit(bandwidthLimitDTO);
        throttleLimitDTO.setTimeUnit("min");
        throttleLimitDTO.setUnitTime(1);
        QuotaPolicy policy = CommonThrottleMappingUtil.fromDTOToQuotaPolicy(throttleLimitDTO);
        Assert.assertNotNull(policy);
        assertEquals(policy.getType(), PolicyConstants.BANDWIDTH_TYPE);
        BandwidthLimit bandwidthLimit = (BandwidthLimit) policy.getLimit();
        assertEquals(bandwidthLimit.getDataAmount(), 10);
        assertEquals(bandwidthLimit.getDataUnit(), KB );
        assertEquals(bandwidthLimit.getTimeUnit(), "min");
        assertEquals(bandwidthLimit.getUnitTime(), 1);
    }

    @Test(description = "Convert Request Count Throttle Limit DTO to Quota Policy")
    public void fromRequestCountThrottleLimitDtoToQuotaPolicyTest () throws Exception {
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType(PolicyConstants.REQUEST_COUNT_LIMIT_TYPE);
        RequestCountLimitDTO requestCountLimitDTO = new RequestCountLimitDTO();
        requestCountLimitDTO.setRequestCount(100);
        throttleLimitDTO.setRequestCountLimit(requestCountLimitDTO);
        throttleLimitDTO.setTimeUnit("sec");
        throttleLimitDTO.setUnitTime(1);
        QuotaPolicy policy = CommonThrottleMappingUtil.fromDTOToQuotaPolicy(throttleLimitDTO);
        Assert.assertNotNull(policy);
        RequestCountLimit limit = (RequestCountLimit) policy.getLimit();
        Assert.assertNotNull(limit);
        assertEquals(limit.getRequestCount(), 100);
        assertEquals(limit.getTimeUnit(), "sec");
        assertEquals(limit.getUnitTime(), 1);

    }

    @Test(description = "Convert Invalid Throttle Limit DTO to Quota Policy")
    public void fromInvalidThrottleLimitDtoToQuotaPolicyTest () throws Exception {
        ThrottleLimitDTO throttleLimitDTO = new ThrottleLimitDTO();
        throttleLimitDTO.setType("UnsupportedType");
        throttleLimitDTO.setTimeUnit("sec");
        throttleLimitDTO.setUnitTime(1);
        try {
            CommonThrottleMappingUtil.fromDTOToQuotaPolicy(throttleLimitDTO);
        } catch (UnsupportedThrottleLimitTypeException e) {
            Assert.assertTrue(ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE.getErrorCode() ==
                    e.getErrorHandler().getErrorCode());
        }
    }

    @Test(description = "Convert IP specific IPCondition DTO to IPCondition Model object")
    public void fromSpecificIPConditionDtoToIPConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.IP_CONDITION_TYPE);
        IPConditionDTO ipConditionDTO = new IPConditionDTO();
        ipConditionDTO.setIpConditionType(PolicyConstants.IP_SPECIFIC_TYPE);
        ipConditionDTO.setSpecificIP("10.100.0.168");
        throttleConditionDTO.setIpCondition(ipConditionDTO);
        IPCondition condition = (IPCondition) CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        Assert.assertNotNull(condition.getCondition());
        assertEquals(condition.getSpecificIP(), "10.100.0.168");
    }

    @Test(description = "Convert IP range IPCondition DTO to IPCondition Model object")
    public void fromIPRangeConditionDtoToIPConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.IP_CONDITION_TYPE);
        IPConditionDTO ipConditionDTO = new IPConditionDTO();
        ipConditionDTO.setIpConditionType(IP_RANGE_TYPE);
        ipConditionDTO.setStartingIP("10.100.0.158");
        ipConditionDTO.setEndingIP("10.100.0.178");
        throttleConditionDTO.setIpCondition(ipConditionDTO);
        IPCondition condition = (IPCondition) CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        assertEquals(condition.getStartingIP(), "10.100.0.158");
        assertEquals(condition.getEndingIP(), "10.100.0.178");
        assertEquals(condition.getType(), IP_RANGE_TYPE);
    }

    @Test(description = "Convert IP range IPCondition DTO to IPCondition Model object")
    public void fromInvalidIPConditionDtoToIPConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.IP_CONDITION_TYPE);
        IPConditionDTO ipConditionDTO = new IPConditionDTO();
        ipConditionDTO.setIpConditionType("InvalidIPCondition");
        throttleConditionDTO.setIpCondition(ipConditionDTO);
        try {
            CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        } catch (UnsupportedThrottleConditionTypeException e) {
            Assert.assertTrue(ExceptionCodes.UNSUPPORTED_THROTTLE_CONDITION_TYPE.getErrorCode() ==
                    e.getErrorHandler().getErrorCode());
        }
    }

    @Test(description = "Convert Header Condition DTO to HeaderCondition Model object")
    public void fromHeaderConditionDtoToHeaderConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.HEADER_CONDITION_TYPE);
        HeaderConditionDTO headerConditionDTO = new HeaderConditionDTO();
        headerConditionDTO.setHeaderName("testHeader");
        headerConditionDTO.setHeaderValue("testHeaderValue");
        throttleConditionDTO.setHeaderCondition(headerConditionDTO);
        HeaderCondition condition = (HeaderCondition) CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        assertEquals(condition.getHeaderName(), "testHeader");
        assertEquals(condition.getValue(), "testHeaderValue");
    }

    @Test(description = "Convert Query param Condition DTO to QueryParamCondition Model object")
    public void fromQueryParamsConditionDtoToQueryParamsConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.QUERY_PARAMS_CONDITION_TYPE);
        QueryParameterConditionDTO queryParameterConditionDTO = new QueryParameterConditionDTO();
        queryParameterConditionDTO.setParameterName("testParam");
        queryParameterConditionDTO.setParameterValue("testParamValue");
        throttleConditionDTO.setQueryParameterCondition(queryParameterConditionDTO);
        QueryParameterCondition condition = (QueryParameterCondition) CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        assertEquals(condition.getParameter(), "testParam");
        assertEquals(condition.getValue(), "testParamValue");


    }

    @Test(description = "Convert JWT Condition DTO to JWTCondition Model object")
    public void fromJWTConditionDtoToJWTConditionModelTest () throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType(PolicyConstants.JWT_CLAIMS_CONDITION_TYPE);
        JWTClaimsConditionDTO jwtClaimsConditionDTO = new JWTClaimsConditionDTO();
        jwtClaimsConditionDTO.setAttribute("testAttribute");
        jwtClaimsConditionDTO.setClaimUrl("http://wso2.org/claims");
        throttleConditionDTO.setJwtClaimsCondition(jwtClaimsConditionDTO);
        JWTClaimsCondition condition = (JWTClaimsCondition) CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        assertEquals(condition.getAttribute(), "testAttribute");
        assertEquals(condition.getClaimUrl(), "http://wso2.org/claims");
    }

    @Test(description = "Convert Invalid Condition DTO to Model object")
    public void fromInvalidConditionDtoToInvalidConditionModelTest ()  throws Exception {
        ThrottleConditionDTO throttleConditionDTO = new ThrottleConditionDTO();
        throttleConditionDTO.setType("InvalidCondition");
        try {
            CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        } catch (UnsupportedThrottleConditionTypeException e) {
            Assert.assertTrue(ExceptionCodes.UNSUPPORTED_THROTTLE_CONDITION_TYPE.getErrorCode() ==
                    e.getErrorHandler().getErrorCode());
        }
    }

    @Test(description = "Converts a list of Conditional Group DTOs into a list of Pipeline objects and vice versa")
    public void fromConditionalGroupDTOListToPipelineListTestAndViceVersa ()  throws Exception {
        List<ConditionalGroupDTO> conditionalGroupDTOs = new ArrayList<>();

        ConditionalGroupDTO headerConditionGroup = new ConditionalGroupDTO();
        headerConditionGroup.setDescription("Test Description");
        ThrottleLimitDTO headerLimitDTO = new ThrottleLimitDTO();
        headerLimitDTO.setType("RequestCountLimit");
        headerLimitDTO.setTimeUnit("s");
        headerLimitDTO.setUnitTime(1);
        RequestCountLimitDTO headerRequestCountLimitDTO = new RequestCountLimitDTO();
        headerRequestCountLimitDTO.setRequestCount(2);
        headerLimitDTO.setRequestCountLimit(headerRequestCountLimitDTO);

        headerConditionGroup.setLimit(headerLimitDTO);

        ThrottleConditionDTO headerThrottleConditionDTO = new ThrottleConditionDTO();
        HeaderConditionDTO headerConditionDTO = new HeaderConditionDTO();
        headerConditionDTO.setHeaderName("Header1");
        headerConditionDTO.setHeaderValue("HeaderVal1");
        headerThrottleConditionDTO.setHeaderCondition(headerConditionDTO);
        headerThrottleConditionDTO.setType("HeaderCondition");
        headerConditionGroup.addConditionsItem(headerThrottleConditionDTO);

        ConditionalGroupDTO queryParamConditionGroup = new ConditionalGroupDTO();
        queryParamConditionGroup.setDescription("Test Description");

        ThrottleLimitDTO queryParamLimitDTO = new ThrottleLimitDTO();
        queryParamLimitDTO.setType("RequestCountLimit");
        queryParamLimitDTO.setTimeUnit("d");
        queryParamLimitDTO.setUnitTime(10);
        RequestCountLimitDTO requestCountLimitDTO1 = new RequestCountLimitDTO();
        requestCountLimitDTO1.setRequestCount(2);
        queryParamLimitDTO.setRequestCountLimit(requestCountLimitDTO1);

        queryParamConditionGroup.setLimit(queryParamLimitDTO);

        ThrottleConditionDTO queryParamThrottleConditionDTO = new ThrottleConditionDTO();
        QueryParameterConditionDTO queryParamCondition = new QueryParameterConditionDTO();
        queryParamCondition.setParameterName("Query1");
        queryParamCondition.setParameterValue("QueryVal1");
        queryParamThrottleConditionDTO.setQueryParameterCondition(queryParamCondition);
        queryParamThrottleConditionDTO.setType("QueryParameterCondition");
        queryParamConditionGroup.addConditionsItem(queryParamThrottleConditionDTO);

        ConditionalGroupDTO jwtClaimConditionGroup = new ConditionalGroupDTO();
        jwtClaimConditionGroup.setDescription("Test Description");

        ThrottleLimitDTO jwtClaimLimit = new ThrottleLimitDTO();
        jwtClaimLimit.setType("RequestCountLimit");
        jwtClaimLimit.setTimeUnit("h");
        jwtClaimLimit.setUnitTime(11);
        RequestCountLimitDTO jwtRequestCountLimitDTO = new RequestCountLimitDTO();
        jwtRequestCountLimitDTO.setRequestCount(2);
        jwtClaimLimit.setRequestCountLimit(jwtRequestCountLimitDTO);

        jwtClaimConditionGroup.setLimit(jwtClaimLimit);

        ThrottleConditionDTO jwtThrottleConditionDTO = new ThrottleConditionDTO();
        JWTClaimsConditionDTO jwtCondition = new JWTClaimsConditionDTO();
        jwtCondition.setClaimUrl("claimUrl1");
        jwtCondition.setAttribute("claimUrlVal1");
        jwtThrottleConditionDTO.setJwtClaimsCondition(jwtCondition);
        jwtThrottleConditionDTO.setType("JWTClaimsCondition");
        jwtClaimConditionGroup.addConditionsItem(jwtThrottleConditionDTO);

        ConditionalGroupDTO ipConditionGroup = new ConditionalGroupDTO();
        ipConditionGroup.setDescription("Test Description");

        ThrottleLimitDTO ipConditionLimit = new ThrottleLimitDTO();
        ipConditionLimit.setType("BandwidthLimit");
        ipConditionLimit.setTimeUnit("m");
        ipConditionLimit.setUnitTime(1);
        BandwidthLimitDTO bandwidthLimit = new BandwidthLimitDTO();
        bandwidthLimit.setDataAmount(12);
        bandwidthLimit.setDataUnit("mb");
        ipConditionLimit.setBandwidthLimit(bandwidthLimit);

        ipConditionGroup.setLimit(ipConditionLimit);

        ThrottleConditionDTO ipConditionThrottleConditionDTO = new ThrottleConditionDTO();
        IPConditionDTO ipCondition = new IPConditionDTO();
        ipCondition.setIpConditionType("IPSpecific");
        ipCondition.setSpecificIP("10.100.5.81");
        ipConditionThrottleConditionDTO.setIpCondition(ipCondition);
        ipConditionThrottleConditionDTO.setType("IPCondition");
        ipConditionGroup.addConditionsItem(ipConditionThrottleConditionDTO);

        conditionalGroupDTOs.add(headerConditionGroup);
        conditionalGroupDTOs.add(queryParamConditionGroup);
        conditionalGroupDTOs.add(jwtClaimConditionGroup);
        conditionalGroupDTOs.add(ipConditionGroup);

        List<Pipeline> pipelines = CommonThrottleMappingUtil.fromConditionalGroupDTOListToPipelineList(conditionalGroupDTOs);
        assertEquals(conditionalGroupDTOs.size(), pipelines.size());

        assertEquals(pipelines.get(0).getDescription(), headerConditionGroup.getDescription());
        assertEquals(((HeaderCondition)pipelines.get(0).getConditions().get(0)).getHeaderName(),
                                                                                    headerConditionDTO.getHeaderName());
        assertEquals(((HeaderCondition)pipelines.get(0).getConditions().get(0)).getValue(),
                headerConditionDTO.getHeaderValue());

        assertEquals(pipelines.get(0).getQuotaPolicy().getLimit().getTimeUnit(), headerLimitDTO.getTimeUnit());
        assertEquals((Integer) pipelines.get(0).getQuotaPolicy().getLimit().getUnitTime(),  headerLimitDTO.getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(0).getQuotaPolicy().getLimit()).getRequestCount(),
                                                                headerLimitDTO.getRequestCountLimit().getRequestCount());

        assertEquals(((QueryParameterCondition)pipelines.get(1).getConditions().get(0)).getParameter(),
                queryParamCondition.getParameterName());
        assertEquals(((QueryParameterCondition)pipelines.get(1).getConditions().get(0)).getValue(),
                queryParamCondition.getParameterValue());

        assertEquals(pipelines.get(1).getQuotaPolicy().getLimit().getTimeUnit(), queryParamLimitDTO.getTimeUnit());
        assertEquals((Integer) pipelines.get(1).getQuotaPolicy().getLimit().getUnitTime(),  queryParamLimitDTO.getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(1).getQuotaPolicy().getLimit()).getRequestCount(),
                queryParamLimitDTO.getRequestCountLimit().getRequestCount());

        assertEquals(((JWTClaimsCondition)pipelines.get(2).getConditions().get(0)).getClaimUrl(),
                jwtCondition.getClaimUrl());
        assertEquals(((JWTClaimsCondition)pipelines.get(2).getConditions().get(0)).getAttribute(),
                jwtCondition.getAttribute());

        assertEquals(pipelines.get(2).getQuotaPolicy().getLimit().getTimeUnit(), jwtClaimLimit.getTimeUnit());
        assertEquals((Integer) pipelines.get(2).getQuotaPolicy().getLimit().getUnitTime(),  jwtClaimLimit.getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(2).getQuotaPolicy().getLimit()).getRequestCount(),
                jwtClaimLimit.getRequestCountLimit().getRequestCount());


        assertEquals(((IPCondition)pipelines.get(3).getConditions().get(0)).getSpecificIP(),
                ipCondition.getSpecificIP());

        assertEquals(pipelines.get(3).getQuotaPolicy().getLimit().getTimeUnit(), ipConditionLimit.getTimeUnit());
        assertEquals((Integer) pipelines.get(3).getQuotaPolicy().getLimit().getUnitTime(),  ipConditionLimit.getUnitTime());
        assertEquals((Integer)((BandwidthLimit)pipelines.get(3).getQuotaPolicy().getLimit()).getDataAmount(),
                ipConditionLimit.getBandwidthLimit().getDataAmount());

        List<ConditionalGroupDTO> mappedConditionalGroups =
                                        CommonThrottleMappingUtil.fromPipelineListToConditionalGroupDTOList(pipelines);

        assertEquals(mappedConditionalGroups.size(), pipelines.size());

        assertEquals(pipelines.get(0).getDescription(), mappedConditionalGroups.get(0).getDescription());
        assertEquals(((HeaderCondition)pipelines.get(0).getConditions().get(0)).getHeaderName(),
                mappedConditionalGroups.get(0).getConditions().get(0).getHeaderCondition().getHeaderName());
        assertEquals(((HeaderCondition)pipelines.get(0).getConditions().get(0)).getValue(),
                mappedConditionalGroups.get(0).getConditions().get(0).getHeaderCondition().getHeaderValue());

        assertEquals(pipelines.get(0).getQuotaPolicy().getLimit().getTimeUnit(),
                                                                mappedConditionalGroups.get(0).getLimit().getTimeUnit());
        assertEquals((Integer) pipelines.get(0).getQuotaPolicy().getLimit().getUnitTime(),
                                                                 mappedConditionalGroups.get(0).getLimit().getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(0).getQuotaPolicy().getLimit()).getRequestCount(),
                mappedConditionalGroups.get(0).getLimit().getRequestCountLimit().getRequestCount());

        assertEquals(pipelines.get(1).getDescription(), mappedConditionalGroups.get(0).getDescription());
        assertEquals(((QueryParameterCondition)pipelines.get(1).getConditions().get(0)).getParameter(),
                mappedConditionalGroups.get(1).getConditions().get(0).getQueryParameterCondition().getParameterName());
        assertEquals(((QueryParameterCondition)pipelines.get(1).getConditions().get(0)).getValue(),
                mappedConditionalGroups.get(1).getConditions().get(0).getQueryParameterCondition().getParameterValue());

        assertEquals(pipelines.get(1).getQuotaPolicy().getLimit().getTimeUnit(),
                mappedConditionalGroups.get(1).getLimit().getTimeUnit());
        assertEquals((Integer) pipelines.get(1).getQuotaPolicy().getLimit().getUnitTime(),
                mappedConditionalGroups.get(1).getLimit().getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(1).getQuotaPolicy().getLimit()).getRequestCount(),
                mappedConditionalGroups.get(1).getLimit().getRequestCountLimit().getRequestCount());

        assertEquals(pipelines.get(2).getDescription(), mappedConditionalGroups.get(0).getDescription());
        assertEquals(((JWTClaimsCondition)pipelines.get(2).getConditions().get(0)).getClaimUrl(),
                mappedConditionalGroups.get(2).getConditions().get(0).getJwtClaimsCondition().getClaimUrl());
        assertEquals(((JWTClaimsCondition)pipelines.get(2).getConditions().get(0)).getAttribute(),
                mappedConditionalGroups.get(2).getConditions().get(0).getJwtClaimsCondition().getAttribute());

        assertEquals(pipelines.get(2).getQuotaPolicy().getLimit().getTimeUnit(),
                mappedConditionalGroups.get(2).getLimit().getTimeUnit());
        assertEquals((Integer) pipelines.get(2).getQuotaPolicy().getLimit().getUnitTime(),
                mappedConditionalGroups.get(2).getLimit().getUnitTime());
        assertEquals((Integer)((RequestCountLimit)pipelines.get(2).getQuotaPolicy().getLimit()).getRequestCount(),
                mappedConditionalGroups.get(2).getLimit().getRequestCountLimit().getRequestCount());

        assertEquals(pipelines.get(3).getDescription(), mappedConditionalGroups.get(0).getDescription());
        assertEquals(((IPCondition)pipelines.get(3).getConditions().get(0)).getSpecificIP(),
                mappedConditionalGroups.get(3).getConditions().get(0).getIpCondition().getSpecificIP());

        assertEquals(pipelines.get(3).getQuotaPolicy().getLimit().getTimeUnit(),
                mappedConditionalGroups.get(3).getLimit().getTimeUnit());
        assertEquals((Integer) pipelines.get(3).getQuotaPolicy().getLimit().getUnitTime(),
                mappedConditionalGroups.get(3).getLimit().getUnitTime());
        assertEquals((Integer)((BandwidthLimit)pipelines.get(3).getQuotaPolicy().getLimit()).getDataAmount(),
                mappedConditionalGroups.get(3).getLimit().getBandwidthLimit().getDataAmount());
    }
}
