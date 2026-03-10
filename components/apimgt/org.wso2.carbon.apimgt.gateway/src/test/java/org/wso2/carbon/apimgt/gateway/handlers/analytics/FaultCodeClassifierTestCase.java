/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategories;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;

import java.util.HashMap;
import java.util.Map;

public class FaultCodeClassifierTestCase {

    /**
     * Verifies REQUEST direction guardrail errors are classified as REQUEST_GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailRequestSubCategoryClassification() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{\"direction\":\"REQUEST\"}"));

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.REQUEST_GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies RESPONSE direction guardrail errors are classified as RESPONSE_GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailResponseSubCategoryClassification() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{\"direction\":\"RESPONSE\"}"));

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.RESPONSE_GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies guardrail errors without a direction in the message default to GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailDefaultSubCategoryClassification() {
        MessageContext messageContext = mockMessageContext(new HashMap<>());

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies unknown error codes in guardrail category map to GuardrailViolation.OTHER.
     */
    @Test
    public void testGuardrailOtherSubCategoryForUnknownErrorCode() {
        MessageContext messageContext = mockMessageContext(new HashMap<>());

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, 999999);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.OTHER, subCategory);
    }

    /**
     * Verifies unrecognized direction values still fall back to GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailDefaultSubCategoryWhenDirectionIsUnknown() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{\"direction\":\"UPSTREAM\"}"));

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies direct invocation of getGuardrailViolationFaultSubCategory returns expected mapping.
     */
    @Test
    public void testGetGuardrailViolationFaultSubCategoryDirectly() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{\"direction\":\"REQUEST\"}"));
        TestableFaultCodeClassifier classifier = new TestableFaultCodeClassifier(messageContext);

        FaultSubCategory subCategory = classifier.callGetGuardrailViolationFaultSubCategory(
                Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.REQUEST_GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies a plain-text error message (no direction field) defaults to GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailSubCategoryWithPlainTextErrorMessage() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "Guardrail intervention occurred"));

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies an empty JSON error message (no direction field) defaults to GUARDRAIL_HIT.
     */
    @Test
    public void testGuardrailSubCategoryWithEmptyJsonErrorMessage() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{}"));

        FaultCodeClassifier classifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory subCategory = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(FaultSubCategories.GuardrailViolation.GUARDRAIL_HIT, subCategory);
    }

    /**
     * Verifies getFaultSubCategory routes GUARDRAIL_FAULT requests through guardrail-specific classification logic.
     */
    @Test
    public void testGuardrailCategoryRouteInGetFaultSubCategory() {
        MessageContext messageContext = mockMessageContext(singleProperty(
                SynapseConstants.ERROR_MESSAGE, "{\"direction\":\"RESPONSE\"}"));
        TestableFaultCodeClassifier classifier = new TestableFaultCodeClassifier(messageContext);

        FaultSubCategory routed = classifier.getFaultSubCategory(
                FaultCategory.GUARDRAIL_FAULT, Constants.GUARDRAIL_ERROR_CODE);
        FaultSubCategory direct = classifier.callGetGuardrailViolationFaultSubCategory(
                Constants.GUARDRAIL_ERROR_CODE);

        Assert.assertEquals(direct, routed);
        Assert.assertEquals(FaultSubCategories.GuardrailViolation.RESPONSE_GUARDRAIL_HIT, routed);
    }

    private static MessageContext mockMessageContext(Map<String, Object> properties) {
        MessageContext messageContext = Mockito.mock(MessageContext.class);
        Mockito.when(messageContext.getProperty(Mockito.anyString()))
                .thenAnswer(invocation -> properties.get((String) invocation.getArguments()[0]));
        Mockito.when(messageContext.getPropertyKeySet()).thenAnswer(invocation -> properties.keySet());
        return messageContext;
    }

    private static Map<String, Object> singleProperty(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private static class TestableFaultCodeClassifier extends FaultCodeClassifier {

        TestableFaultCodeClassifier(MessageContext messageContext) {
            super(messageContext);
        }

        FaultSubCategory callGetGuardrailViolationFaultSubCategory(int errorCode) {
            return getGuardrailViolationFaultSubCategory(errorCode);
        }
    }
}
