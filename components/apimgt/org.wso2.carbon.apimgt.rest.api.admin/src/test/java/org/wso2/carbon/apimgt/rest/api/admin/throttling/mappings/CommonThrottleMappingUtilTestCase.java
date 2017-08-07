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
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.CommonThrottleMappingUtil;

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
        Assert.assertEquals(policy.getType(), PolicyConstants.BANDWIDTH_TYPE);
        BandwidthLimit bandwidthLimit = (BandwidthLimit) policy.getLimit();
        Assert.assertEquals(bandwidthLimit.getDataAmount(), 10);
        Assert.assertEquals(bandwidthLimit.getDataUnit(), KB );
        Assert.assertEquals(bandwidthLimit.getTimeUnit(), "min");
        Assert.assertEquals(bandwidthLimit.getUnitTime(), 1);
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
        Assert.assertEquals(limit.getRequestCount(), 100);
        Assert.assertEquals(limit.getTimeUnit(), "sec");
        Assert.assertEquals(limit.getUnitTime(), 1);

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
        Condition condition = CommonThrottleMappingUtil.fromDTOToCondition(throttleConditionDTO);
        Assert.assertNotNull(condition);
        Assert.assertNotNull(condition.getCondition());
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
        Assert.assertEquals(condition.getStartingIP(), "10.100.0.158");
        Assert.assertEquals(condition.getEndingIP(), "10.100.0.178");
        Assert.assertEquals(condition.getType(), IP_RANGE_TYPE);
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
        Assert.assertEquals(condition.getHeaderName(), "testHeader");
        Assert.assertEquals(condition.getValue(), "testHeaderValue");
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
        Assert.assertEquals(condition.getParameter(), "testParam");
        Assert.assertEquals(condition.getValue(), "testParamValue");


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
        Assert.assertEquals(condition.getAttribute(), "testAttribute");
        Assert.assertEquals(condition.getClaimUrl(), "http://wso2.org/claims");
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
}
