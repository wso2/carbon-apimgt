/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Limit;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BandwidthLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.HeaderConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.JWTClaimsConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.QueryParameterConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.RequestCountLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleConditionTypeException;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Common Throttling related models and their sub components into REST API DTOs
 * and vice-versa
 */
public class CommonThrottleMappingUtil {

    private static final Logger log = LoggerFactory.getLogger(CommonThrottleMappingUtil.class);

    /**
     * Converts a list of Conditional Group DTOs into a list of Pipeline objects
     *
     * @param conditionalGroupDTOs a list of Conditional Group DTOs
     * @return Derived list of Pipelines from list of Conditional Group DTOs
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static List<Pipeline> fromConditionalGroupDTOListToPipelineList(
            List<ConditionalGroupDTO> conditionalGroupDTOs)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        List<Pipeline> pipelines = new ArrayList<>();
        for (ConditionalGroupDTO dto : conditionalGroupDTOs) {
            pipelines.add(fromConditionalGroupDTOToPipeline(dto));
        }
        return pipelines;
    }

    /**
     * Converts a list of Pipeline objects into a list of Conditional Group DTO objetcs
     *
     * @param pipelines A list of pipeline objects
     * @return Derived list of DTO objects from Pipeline list
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static List<ConditionalGroupDTO> fromPipelineListToConditionalGroupDTOList(List<Pipeline> pipelines)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        List<ConditionalGroupDTO> groupDTOs = new ArrayList<>();
        if (pipelines != null) {
            for (Pipeline pipeline : pipelines) {
                groupDTOs.add(fromPipelineToConditionalGroupDTO(pipeline));
            }
        }
        return groupDTOs;
    }

    /**
     * Converts a single Conditional Group DTO into a Pipeline object
     *
     * @param dto Conditional Group DTO
     * @return Derived Pipeline object from Conditional Group DTO
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static Pipeline fromConditionalGroupDTOToPipeline(ConditionalGroupDTO dto)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        Pipeline pipeline = new Pipeline();
        pipeline.setDescription(dto.getDescription());
        pipeline.setEnabled(true);
        pipeline.setQuotaPolicy(fromDTOToQuotaPolicy(dto.getLimit()));

        List<Condition> conditions = fromDTOListToConditionList(dto.getConditions());
        pipeline.setConditions(conditions);
        return pipeline;
    }

    /**
     * Converts a single Pipeline object into a Conditional Group DTO object
     *
     * @param pipeline Pipeline object
     * @return Derived DTO object from Pipeline object
     * @throws UnsupportedThrottleLimitTypeException
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static ConditionalGroupDTO fromPipelineToConditionalGroupDTO(Pipeline pipeline)
            throws UnsupportedThrottleLimitTypeException, UnsupportedThrottleConditionTypeException {
        ConditionalGroupDTO groupDTO = new ConditionalGroupDTO();
        groupDTO.setDescription(pipeline.getDescription());
        groupDTO.setLimit(fromQuotaPolicyToDTO(pipeline.getQuotaPolicy()));

        List<ThrottleConditionDTO> conditionDTOList = fromConditionListToDTOList(pipeline.getConditions());
        groupDTO.setConditions(conditionDTOList);
        return groupDTO;
    }

    /**
     * Converts a list of Throttle Condition DTOs into a list of model Condition objects
     *
     * @param throttleConditionDTOs list of Throttle Condition DTOs
     * @return Derived list of model Condition objects from Throttle Condition DTOs
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static List<Condition> fromDTOListToConditionList(List<ThrottleConditionDTO> throttleConditionDTOs)
            throws UnsupportedThrottleConditionTypeException {
        List<Condition> conditions = new ArrayList<>();
        if (throttleConditionDTOs != null) {
            for (ThrottleConditionDTO dto : throttleConditionDTOs) {
                conditions.add(fromDTOToCondition(dto));
            }
        }
        return conditions;
    }

    /**
     * Converts a list of Condition objects into a list of Throttle Condition DTO objects
     *
     * @param conditions List of Condition objects
     * @return a list of Throttle Condition DTO objects derived from a list of model Condition objects
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static List<ThrottleConditionDTO> fromConditionListToDTOList(List<Condition> conditions)
            throws UnsupportedThrottleConditionTypeException {
        List<ThrottleConditionDTO> dtoList = new ArrayList<>();
        if (conditions != null) {
            for (Condition condition : conditions) {
                dtoList.add(fromConditionToDTO(condition));
            }
        }
        return dtoList;
    }

    /**
     * Converts a Throttle Condition DTO object into a Condition model object
     *
     * @param dto Throttle Condition DTO object
     * @return Derived Condition model object from DTO
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static Condition fromDTOToCondition(ThrottleConditionDTO dto)
            throws UnsupportedThrottleConditionTypeException {
        if (dto instanceof IPConditionDTO) {
            return fromDTOToIPCondition((IPConditionDTO) dto);
        } else if (dto instanceof HeaderConditionDTO) {
            return fromDTOToHeaderCondition((HeaderConditionDTO) dto);
        } else if (dto instanceof QueryParameterConditionDTO) {
            return fromDTOToQueryParameterCondition((QueryParameterConditionDTO) dto);
        } else if (dto instanceof JWTClaimsConditionDTO) {
            return fromDTOToJWTClaimsCondition((JWTClaimsConditionDTO) dto);
        } else {
            String msg = "Throttle Condition type " + dto.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleConditionTypeException(msg);
        }
    }

    /**
     * Converts a Throttle Condition model object into a DTO
     *
     * @param condition Throttle condition model object
     * @return Derived DTO object from the model object
     * @throws UnsupportedThrottleConditionTypeException
     */
    public static ThrottleConditionDTO fromConditionToDTO(Condition condition)   //.................
            throws UnsupportedThrottleConditionTypeException {
        if (condition instanceof IPCondition) {
            return fromIPConditionToDTO((IPCondition) condition);
        } else if (condition instanceof HeaderCondition) {
            return fromHeaderConditionToDTO((HeaderCondition) condition);
        } else if (condition instanceof QueryParameterCondition) {
            return fromQueryParameterConditionToDTO((QueryParameterCondition) condition);
        } else if (condition instanceof JWTClaimsCondition) {
            return fromJWTClaimsConditionToDTO((JWTClaimsCondition) condition);
        } else {
            String msg = "Throttle Condition type " + condition.getClass().getName() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleConditionTypeException(msg);
        }
    }

    /**
     * Converts a Throttle Limit DTO object into a Quota Policy object
     *
     * @param dto Throttle limit DTO object
     * @return Derived Quota policy object from DTO
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static QuotaPolicy fromDTOToQuotaPolicy(ThrottleLimitDTO dto) throws UnsupportedThrottleLimitTypeException {
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setLimit(fromDTOToLimit(dto));
        quotaPolicy.setType(mapQuotaPolicyTypeFromDTOToModel(dto.getType()));
        return quotaPolicy;
    }

    /**
     * Converts a Quota Policy object into a Throttle Limit DTO object
     *
     * @param quotaPolicy Quota Policy object
     * @return Throttle Limit DTO object derived from the Quota Policy object
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static ThrottleLimitDTO fromQuotaPolicyToDTO(QuotaPolicy quotaPolicy)
            throws UnsupportedThrottleLimitTypeException {
        Limit limit = quotaPolicy.getLimit();
        String throttleLimitType = quotaPolicy.getType();
        if (PolicyConstants.REQUEST_COUNT_TYPE.equals(throttleLimitType)) {
            if (limit instanceof RequestCountLimit) {
                RequestCountLimit requestCountLimit = (RequestCountLimit) limit;
                return fromRequestCountLimitToDTO(requestCountLimit);
            } else {
                String msg = "Throttle limit type " + throttleLimitType + " is not of type RequestCountLimit";
                log.error(msg);
                throw new UnsupportedThrottleLimitTypeException(msg, ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
            }
        } else if (PolicyConstants.BANDWIDTH_TYPE.equals(throttleLimitType)) {
            if (limit instanceof BandwidthLimit) {
                BandwidthLimit bandwidthLimit = (BandwidthLimit) limit;
                return fromBandwidthLimitToDTO(bandwidthLimit);
            } else {
                String msg = "Throttle limit type " + throttleLimitType + " is not of type BandwidthLimit";
                log.error(msg);
                throw new UnsupportedThrottleLimitTypeException(msg, ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
            }
        } else {
            String msg = "Throttle limit type " + throttleLimitType + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleLimitTypeException(msg, ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
        }
    }

    /**
     * Converts a Throttle Limit DTO object into a Limit object
     *
     * @param dto Throttle Limit DTO object
     * @return Limit object derived from DTO
     * @throws UnsupportedThrottleLimitTypeException
     */

    public static Limit fromDTOToLimit(ThrottleLimitDTO dto) throws UnsupportedThrottleLimitTypeException {

        if (PolicyConstants.BANDWIDTH_LIMIT_TYPE.equals(dto.getType())) {
            // check if all required params are available
            if (dto.getBandwidthLimit() == null || dto.getBandwidthLimit().getDataAmount() == null || dto
                    .getBandwidthLimit().getDataUnit() == null) {
                // can't continue, throw
                String errorMsg = "One or more required params are missing for the ThrottleLimit type: "+ dto.getType();
                log.error(errorMsg);
                throw new UnsupportedThrottleLimitTypeException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
            }
            return fromDTOToBandwidthLimit(dto);
        } else if (PolicyConstants.REQUEST_COUNT_LIMIT_TYPE.equals(dto.getType())) {
            // check if all required params are available
            if (dto.getRequestCountLimit() == null || dto.getRequestCountLimit().getRequestCount() == null) {
                // can't continue, throw
                String errorMsg = "One or more required params are missing for the ThrottleLimit type: "+ dto.getType();
                log.error(errorMsg);
                throw new UnsupportedThrottleLimitTypeException(errorMsg, ExceptionCodes.PARAMETER_NOT_PROVIDED);
            }
            return fromDTOToRequestCountLimit(dto);
        } else {
            String msg = "Throttle limit type " + dto.getType() + " is not supported";
            log.error(msg);
            throw new UnsupportedThrottleLimitTypeException(msg,  ExceptionCodes.UNSUPPORTED_THROTTLE_LIMIT_TYPE);
        }
    }

    /**
     * Converts a Bandwidth Limit DTO object into a Bandwidth Limit model object
     *
     * @param dto Bandwidth Limit DTO object
     * @return Bandwidth Limit model object derived from DTO
     */
    public static BandwidthLimit fromDTOToBandwidthLimit(ThrottleLimitDTO dto) {
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        bandwidthLimit = updateFieldsFromDTOToLimit(dto, bandwidthLimit);
        bandwidthLimit.setDataAmount(dto.getBandwidthLimit().getDataAmount());
        bandwidthLimit.setDataUnit(dto.getBandwidthLimit().getDataUnit());
        return bandwidthLimit;
    }

    /**
     * Converts a Request Count Limit DTO object into a Request Count model object
     *
     * @param dto Request Count Limit DTO object
     * @return Request Count model object derived from DTO
     */
    public static RequestCountLimit fromDTOToRequestCountLimit(ThrottleLimitDTO dto) {
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit = updateFieldsFromDTOToLimit(dto, requestCountLimit);
        requestCountLimit.setRequestCount(dto.getRequestCountLimit().getRequestCount());
        return requestCountLimit;
    }

    /**
     * Converts a Bandwidth Limit model object into a Bandwidth Limit DTO object
     *
     * @param bandwidthLimit Bandwidth Limit model object
     * @return Bandwidth Limit DTO object derived from model
     */
    public static ThrottleLimitDTO fromBandwidthLimitToDTO(BandwidthLimit bandwidthLimit) {  //done
        ThrottleLimitDTO dto = new ThrottleLimitDTO();
        dto = updateFieldsFromLimitToDTO(bandwidthLimit, dto);
        dto.setType(PolicyConstants.BANDWIDTH_LIMIT_TYPE);
        dto.setBandwidthLimit(new BandwidthLimitDTO());
        dto.getBandwidthLimit().setDataAmount(bandwidthLimit.getDataAmount());
        dto.getBandwidthLimit().setDataUnit(bandwidthLimit.getDataUnit());
        return dto;
    }

    /**
     * Converts a Request Count Limit model object into a Request Count Limit DTO object
     *
     * @param requestCountLimit Request Count Limit model object
     * @return Request Count DTO object derived from model
     */
    public static ThrottleLimitDTO fromRequestCountLimitToDTO(RequestCountLimit requestCountLimit) { //done
        ThrottleLimitDTO dto = new ThrottleLimitDTO();
        dto = updateFieldsFromLimitToDTO(requestCountLimit, dto);
        dto.setType("RequestCountLimit");
        dto.setRequestCountLimit(new RequestCountLimitDTO());
        dto.getRequestCountLimit().setRequestCount(requestCountLimit.getRequestCount());
        return dto;
    }

    /**
     * Converts a IP Condition DTO object into a model object
     *
     * @param dto IP Condition DTO object
     * @return IP Condition model object derived from DTO
     */
    public static IPCondition fromDTOToIPCondition(IPConditionDTO dto) {
        String ipConditionType = mapIPConditionTypeFromDTOToModel(dto.getIpConditionType());
        IPCondition ipCondition = new IPCondition(ipConditionType);
        ipCondition = updateFieldsFromDTOToCondition(dto, ipCondition);
        ipCondition.setSpecificIP(dto.getSpecificIP());
        ipCondition.setStartingIP(dto.getStartingIP());
        ipCondition.setEndingIP(dto.getEndingIP());
        return ipCondition;
    }

    /**
     * Converts an IP Condition model object into a DTO
     *
     * @param ipCondition IP Condition model object
     * @return DTO object derived from model object
     */
    public static IPConditionDTO fromIPConditionToDTO(IPCondition ipCondition) {
        IPConditionDTO.IpConditionTypeEnum ipConditionType = mapIPConditionTypeFromModelToDTO(ipCondition.getType());
        IPConditionDTO dto = new IPConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.IPCONDITION);
        dto = updateFieldsFromConditionToDTO(ipCondition, dto);
        dto.setIpConditionType(ipConditionType);
        dto.setSpecificIP(ipCondition.getSpecificIP());
        dto.setStartingIP(ipCondition.getStartingIP());
        dto.setEndingIP(ipCondition.getEndingIP());
        return dto;
    }

    /**
     * Converts a Header Condition DTO object into a model object
     *
     * @param dto Header Condition DTO object
     * @return Header Condition model object derived from Header Condition DTO
     */
    public static HeaderCondition fromDTOToHeaderCondition(HeaderConditionDTO dto) {
        HeaderCondition headerCondition = new HeaderCondition();
        headerCondition = updateFieldsFromDTOToCondition(dto, headerCondition);
        headerCondition.setHeader(dto.getHeaderName());
        headerCondition.setValue(dto.getHeaderValue());
        return headerCondition;
    }

    /**
     * Converts a Header Condition model object into a DTO
     *
     * @param headerCondition Header Condition model object
     * @return DTO object that was derived from Header Condition model object
     */
    public static HeaderConditionDTO fromHeaderConditionToDTO(HeaderCondition headerCondition) {
        HeaderConditionDTO dto = new HeaderConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.HEADERCONDITION);
        dto = updateFieldsFromConditionToDTO(headerCondition, dto);
        dto.setHeaderName(headerCondition.getHeaderName());
        dto.setHeaderValue(headerCondition.getValue());
        return dto;
    }

    /**
     * Converts a Query Parameter Condition DTO object into a model object
     *
     * @param dto Query Parameter Condition DTO object
     * @return Query Parameter Condition model object derived from Query Parameter Condition DTO
     */
    public static QueryParameterCondition fromDTOToQueryParameterCondition(QueryParameterConditionDTO dto) {
        QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
        queryParameterCondition = updateFieldsFromDTOToCondition(dto, queryParameterCondition);
        queryParameterCondition.setParameter(dto.getParameterName());
        queryParameterCondition.setValue(dto.getParameterValue());
        return queryParameterCondition;
    }

    /**
     * Converts a Query Parameter Condition model object into a DTO
     *
     * @param condition Query Parameter Condition model object
     * @return DTO object that was derived from Query Parameter Condition model object
     */
    public static QueryParameterConditionDTO fromQueryParameterConditionToDTO(QueryParameterCondition condition) {
        QueryParameterConditionDTO dto = new QueryParameterConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.QUERYPARAMETERCONDITION);
        dto = updateFieldsFromConditionToDTO(condition, dto);
        dto.setParameterName(condition.getParameter());
        dto.setParameterValue(condition.getValue());
        return dto;
    }

    /**
     * Converts a JWT Claims Condition DTO object into a model object
     *
     * @param dto JWT Claims Condition DTO object
     * @return JWT Claims Condition model object derived from JWT Claims Condition DTO
     */
    public static JWTClaimsCondition fromDTOToJWTClaimsCondition(JWTClaimsConditionDTO dto) {
        JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
        jwtClaimsCondition = updateFieldsFromDTOToCondition(dto, jwtClaimsCondition);
        jwtClaimsCondition.setAttribute(dto.getAttribute());
        jwtClaimsCondition.setClaimUrl(dto.getClaimUrl());
        return jwtClaimsCondition;
    }

    /**
     * Converts a JWT Claims Condition model object into a DTO
     *
     * @param condition JWT Claims Condition model object
     * @return DTO object that was derived from JWT Claims Condition model object
     */
    public static JWTClaimsConditionDTO fromJWTClaimsConditionToDTO(JWTClaimsCondition condition) {
        JWTClaimsConditionDTO dto = new JWTClaimsConditionDTO();
        dto.setType(ThrottleConditionDTO.TypeEnum.JWTCLAIMSCONDITION);
        dto = updateFieldsFromConditionToDTO(condition, dto);
        dto.setClaimUrl(condition.getClaimUrl());
        dto.setAttribute(condition.getAttribute());
        return dto;
    }

    /**
     * Update common fields of Condition model object using Condition DTO object
     * Fields update: conditionEnabled, invertCondition
     *
     * @param dto       Throttle Condition DTO object
     * @param condition Condition model object
     * @param <T>       The type of Condition
     * @return Condition object with common fields updated
     */
    public static <T extends Condition> T updateFieldsFromDTOToCondition(ThrottleConditionDTO dto, T condition) {
        condition.setConditionEnabled(Boolean.TRUE.toString());
        condition.setInvertCondition(dto.getInvertCondition());
        return condition;
    }

    /**
     * Updates common fields of Throttle Condition DTO object using the Condition model object
     * Fields update: conditionEnabled, invertCondition
     *
     * @param condition Condition model object
     * @param dto       Throttle Condition DTO object
     * @param <T>       The type of Condition DTO object
     * @return Condition DTO object with common fields updated
     */
    public static <T extends ThrottleConditionDTO> T updateFieldsFromConditionToDTO(Condition condition, T dto) {
        dto.setInvertCondition(condition.isInvertCondition());
        return dto;
    }

    /**
     * Update common fields of Limit model object using Throttle Limit DTO object
     * Fields update: timeUnit, unitTime
     *
     * @param dto   Throttle limit DTO object
     * @param limit Limit object
     * @param <T>   Type of Limit
     * @return Limit model object with common fields updated
     */
    public static <T extends Limit> T updateFieldsFromDTOToLimit(ThrottleLimitDTO dto, T limit) {
        limit.setTimeUnit(dto.getTimeUnit());
        limit.setUnitTime(dto.getUnitTime());
        return limit;
    }

    /**
     * Update common fields of Throttle Limit DTO object using Limit model object
     * Fields update: timeUnit, unitTime
     *
     * @param limit Limit model object
     * @param dto   Throttle Limit DTO object
     * @param <T>   Type of Throttle Limit object
     * @return Throttle Limit DTO object with common fields updated
     */
    public static <T extends ThrottleLimitDTO> T updateFieldsFromLimitToDTO(Limit limit, T dto) {
        dto.setTimeUnit(limit.getTimeUnit());
        dto.setUnitTime(limit.getUnitTime());
        return dto;
    }

    /**
     * Update common fields of Policy model object using Throttle Policy DTO object
     *
     * @param dto    Throttle Policy DTO object
     * @param policy Policy model object
     * @param <T>    Type of Policy model
     * @return Updated Policy object with common fields
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static <T extends Policy> T updateFieldsFromDTOToPolicy(ThrottlePolicyDTO dto, T policy)
            throws UnsupportedThrottleLimitTypeException {
        policy.setDisplayName(dto.getDisplayName());
        policy.setDescription(dto.getDescription());
        policy.setPolicyName(dto.getPolicyName());
        return policy;
    }

    /**
     * Update common fields of Throttle Policy DTO object using Policy model object
     *
     * @param dto    Throttle Policy DTO object
     * @param policy Policy model object
     * @param <T>    Type of Throttle Policy DTO object model
     * @return Updated Throttle Policy DTO object with common fields
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static <T extends ThrottlePolicyDTO> T updateFieldsFromToPolicyToDTO(Policy policy, T dto)
            throws UnsupportedThrottleLimitTypeException {

        dto.setPolicyId(policy.getUuid());
        dto.setDisplayName(policy.getDisplayName());
        dto.setIsDeployed(policy.isDeployed());
        dto.setDescription(policy.getDescription());
        dto.setPolicyName(policy.getPolicyName());
        return dto;
    }

    /**
     * Create a custom attribute using the given name and value
     *
     * @param name  Name of the attribute
     * @param value Value of the attribute
     * @return Custom Attribute object containing the given name and value
     */
    public static CustomAttributeDTO getCustomAttribute(String name, String value) {
        CustomAttributeDTO customAttributeDTO = new CustomAttributeDTO();
        customAttributeDTO.setName(name);
        customAttributeDTO.setValue(value);
        return customAttributeDTO;
    }

    /**
     * Maps Throttle Limit DTO's Type Enum to Quota Policy Type
     *
     * @param throttleLimitType Throttle Limit DTO's Type
     * @return Mapped Quota Policy Type
     */
    private static String mapQuotaPolicyTypeFromDTOToModel(String throttleLimitType) {
        switch (throttleLimitType) {
        case PolicyConstants.BANDWIDTH_LIMIT_TYPE:
            return PolicyConstants.BANDWIDTH_TYPE;
        case PolicyConstants.REQUEST_COUNT_LIMIT_TYPE:
            return PolicyConstants.REQUEST_COUNT_TYPE;
        default:
            return null;
        }
    }

    /**
     * Maps IP Condition Type from IP Condition DTO into IP Condition model type
     *
     * @param typeEnum Type from IP Condition DTO
     * @return Mapped IP Condition model type
     */
    private static String mapIPConditionTypeFromDTOToModel(IPConditionDTO.IpConditionTypeEnum typeEnum) {
        switch (typeEnum) {
        case IPRANGE:
            return PolicyConstants.IP_RANGE_TYPE;
        case IPSPECIFIC:
            return PolicyConstants.IP_SPECIFIC_TYPE;
        default:
            return null;
        }
    }

    /**
     * Map IP Condition model type into DTO's type
     *
     * @param ipConditionType IP Condition model type
     * @return Mapped IP Condition DTO type
     */
    private static IPConditionDTO.IpConditionTypeEnum mapIPConditionTypeFromModelToDTO(String ipConditionType) {
        switch (ipConditionType) {
        case PolicyConstants.IP_RANGE_TYPE:
            return IPConditionDTO.IpConditionTypeEnum.IPRANGE;
        case PolicyConstants.IP_SPECIFIC_TYPE:
            return IPConditionDTO.IpConditionTypeEnum.IPSPECIFIC;
        default:
            return null;
        }
    }

}
