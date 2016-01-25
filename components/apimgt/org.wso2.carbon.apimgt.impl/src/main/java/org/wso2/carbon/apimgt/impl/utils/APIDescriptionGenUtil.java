/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class APIDescriptionGenUtil {
    /**
     * Class Logger
     */
    private static Log log = LogFactory.getLog(APIDescriptionGenUtil.class);

    private static final String DESCRIPTION = "Allows [1] request(s) per minute.";

    public static String generateDescriptionFromPolicy(OMElement policy) throws APIManagementException {
        //Here as the method is about extracting some info from the policy. And it's not concern on compliance to
        // specification. So it just extract the required element.
        OMElement maxCount;
        OMElement timeUnit;
        long requestPerMinute;
        try {
            maxCount = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).getFirstChildWithName
                    (APIConstants
                             .THROTTLE_CONTROL_ELEMENT).getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT);
            timeUnit = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).getFirstChildWithName
                    (APIConstants
                             .THROTTLE_CONTROL_ELEMENT).getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_UNIT_TIME_ELEMENT);
            //Here we will assume time unit provided as milli second and do calculation to get requests per minute.
            if (maxCount.getText().isEmpty() || timeUnit.getText().isEmpty()) {
                String msg = APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT + " or "
                             + APIConstants.THROTTLE_UNIT_TIME_ELEMENT + " element data found empty in " +
                             "the policy.";
                log.warn(msg);
                throw new APIManagementException(msg);
            }
            requestPerMinute = (Long.parseLong(maxCount.getText().trim()) * 60000) / (Long.parseLong(timeUnit.getText().trim()));
            if (requestPerMinute >= 1) {
                return DESCRIPTION.replaceAll("\\[1\\]", Long.toString(requestPerMinute));
            }
            return DESCRIPTION;
        } catch (NullPointerException npe) {
            String msg = "Policy could not be parsed correctly based on http://schemas.xmlsoap.org/ws/2004/09/policy " +
                         "specification";
            log.error(msg, npe);
            throw new APIManagementException(msg, npe);
        }
    }

    /**
     * This method is used to get Allowed Requests count for a tier
     *
     * @param policy tier policy
     * @return Allowed Requests Count
     * @throws APIManagementException if error occurs when processing XML
     */
    public static long getAllowedRequestCount(OMElement policy) throws APIManagementException {
        try {
            OMElement maxCount = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_CONTROL_ELEMENT).
                    getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT);

            if (maxCount.getText().isEmpty()) {
                String message = APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT.getLocalPart()
                                 + " element data not found empty in the policy.";
                log.warn(message);
                throw new APIManagementException(message);
            }
            return Long.parseLong(maxCount.getText().trim());
        } catch (OMException e) {
            //We capture the runtime exception here.
            String errorMessage = "Policy could not be parsed correctly based on " + 
                    "http://schemas.xmlsoap.org/ws/2004/09/policy specification";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage + e.getMessage());
        } catch (NumberFormatException e) {
            log.error("Error in retrieving request count in tier xml.", e);
            throw new APIManagementException("Error in retrieving request count in tier xml." + e.getMessage());
        }
    }

    /**
     * The method to extract the tier attributes from each tier level policy definitions
     * @param policy  Tier level policy
     * @return Attributes map
     * @throws APIManagementException
     */
    public static Map<String, Object> getTierAttributes(OMElement policy) throws APIManagementException {
        Map<String, Object> attributesMap = new HashMap<String, Object>();
        OMElement attributes = null;

        try {
            OMElement tier = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).getFirstChildWithName
                    (APIConstants.THROTTLE_CONTROL_ELEMENT).getFirstChildWithName(APIConstants.POLICY_ELEMENT)
                    .getFirstChildWithName(APIConstants.POLICY_ELEMENT);
            if (tier != null) {
                attributes = tier.getFirstChildWithName(APIConstants.THROTTLE_ATTRIBUTES_ELEMENT);
            }
            if (attributes == null) {
                return attributesMap;
            } else {
                for (Iterator childElements = attributes.getChildElements(); childElements.hasNext(); ) {
                    OMElement element = (OMElement) childElements.next();
                    String displayName = element.getAttributeValue(
                            new QName(APIConstants.THROTTLE_ATTRIBUTE_DISPLAY_NAME));
                    String localName = element.getLocalName();
                    String attrName = (displayName != null ? displayName : localName); //If displayName not defined,
                    // use the attribute name
                    String attrValue = element.getText();
                    attributesMap.put(attrName, attrValue);
                }
            }
        } catch (NullPointerException e) {
            String errorMessage = "Policy could not be parsed correctly based on " +
                    "http://schemas.xmlsoap.org/ws/2004/09/policy specification";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage + e.getMessage());
        }
        return attributesMap;
    }

    /**
     * This method gives the allowed request count for a minute
     *
     * @param policy The tier level policy
     * @return The request count for a minute((maxCount * 60000)/timeDuration)
     * @throws APIManagementException if policy or parsing error occurs
     */
    public static long getAllowedCountPerMinute(OMElement policy) throws APIManagementException {
        //Here as the method is about extracting some info from the policy. And it's not concern on compliance to
        // specification. So it just extract the required element.
        OMElement maxCount;
        OMElement timeUnit;
        long requestPerMinute;
        try {
            maxCount = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).getFirstChildWithName
                    (APIConstants
                            .THROTTLE_CONTROL_ELEMENT).getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT);
            timeUnit = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).getFirstChildWithName
                    (APIConstants
                            .THROTTLE_CONTROL_ELEMENT).getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_UNIT_TIME_ELEMENT);
            //Here we will assume time unit provided as milli second and do calculation to get requests per minute.
            if (maxCount.getText().isEmpty() || timeUnit.getText().isEmpty()) {
                String errorMessage = APIConstants.THROTTLE_MAXIMUM_COUNT_ELEMENT + "or"
                        + APIConstants.THROTTLE_UNIT_TIME_ELEMENT + " element data found empty in " +
                        "the policy.";
                log.warn(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            return (Long.parseLong(maxCount.getText().trim()) * 60000) /
                                                                (Long.parseLong(timeUnit.getText().trim()));
        } catch (NullPointerException e) {
            String errorMessage = "Policy could not be parsed correctly based on " +
                    "http://schemas.xmlsoap.org/ws/2004/09/policy specification";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage + e.getMessage());
        }
    }

    /**
     * This method is used to get time duration of a tier
     *
     * @param policy tier policy
     * @return time duration for requests
     * @throws APIManagementException if error occurs when processing XML
     */
    public static long getTimeDuration(OMElement policy) throws APIManagementException {
        try {
            OMElement duration = policy.getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_CONTROL_ELEMENT).
                    getFirstChildWithName(APIConstants.POLICY_ELEMENT).
                    getFirstChildWithName(APIConstants.THROTTLE_UNIT_TIME_ELEMENT);

            if (duration.getText().isEmpty()) {
                String message = APIConstants.THROTTLE_UNIT_TIME_ELEMENT.getLocalPart() +
                                 " element data not found empty in the policy.";
                log.warn(message);
                throw new APIManagementException(message);
            }
            // We return the milliseconds value as it is.
            // Reason - We need the ability to do fine grained throttling configurations.
            return Long.parseLong(duration.getText().trim());
        } catch (OMException e) {
            String errorMessage = "Policy could not be parsed correctly based on " +
                                  "http://schemas.xmlsoap.org/ws/2004/09/policy specification";
            log.error(errorMessage, e);
            throw new APIManagementException(errorMessage, e);
        } catch (NumberFormatException e) {
            String message = "Error in retrieving time duration from the tiers xml";
            log.error(message, e);
            throw new APIManagementException(message, e);
        }
    }
}
