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
package org.wso2.carbon.apimgt.impl.ai;

import org.wso2.carbon.apimgt.api.AbstractAIRequestPropertyEnricher;

/**
 * Enricher used when no implementation is configured through {@code [apim.ai] propertyEnricherImpl},
 * and the fallback when a configured implementation cannot be loaded.
 * <p>
 * It adds no properties, so the payloads sent to the AI services stay exactly as the product builds
 * them. Deployments that need extra attributes such as the invoking user name configure their own
 * implementation of {@link org.wso2.carbon.apimgt.api.AIRequestPropertyEnricher}.
 */
public class DefaultAIRequestPropertyEnricher extends AbstractAIRequestPropertyEnricher {

}
