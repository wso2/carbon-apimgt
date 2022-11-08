/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.internal;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationConfigurationDto;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;

/**
 * A holder class to hold monetization feature related data objects.
 */
public class MonetizationDataHolder {

    private static final MonetizationDataHolder Instance = new MonetizationDataHolder();
    private AccessTokenGenerator monetizationAccessTokenGenerator;

    private MonetizationDataHolder() {

    }

    public static MonetizationDataHolder getInstance() {

        return Instance;
    }

    public void init() {

        initializeMonetizationAccessTokenGenerator();
    }

    public AccessTokenGenerator getMonetizationAccessTokenGenerator() {

        return monetizationAccessTokenGenerator;
    }

    public void setMonetizationAccessTokenGenerator(AccessTokenGenerator monetizationAccessTokenGenerator) {

        this.monetizationAccessTokenGenerator = monetizationAccessTokenGenerator;
    }

    /**
     * This method will read the monetization configurations and if a token url and consumer key, secret pair is defined,
     * create a new AccessTokenGenerator instance for monetization strip client. This token generator will be used to
     * obtain a valid access token to query choreo insight API.
     */
    public void initializeMonetizationAccessTokenGenerator() {

        MonetizationConfigurationDto configDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getMonetizationConfigurationDto();

        if (configDto != null) {
            String choreoTokenUrl = configDto.getChoreoTokenEndpoint();
            String insightAppConsumerKey = configDto.getInsightAppConsumerKey();
            String insightAppConsumerSecret = configDto.getInsightAppConsumerSecret();
            if (!StringUtils.isEmpty(choreoTokenUrl) && !StringUtils.isEmpty(insightAppConsumerKey) &&
                    !StringUtils.isEmpty(insightAppConsumerSecret)) {
                monetizationAccessTokenGenerator =
                        new AccessTokenGenerator(choreoTokenUrl, null, insightAppConsumerKey, insightAppConsumerSecret);
            }
        }
    }
}
