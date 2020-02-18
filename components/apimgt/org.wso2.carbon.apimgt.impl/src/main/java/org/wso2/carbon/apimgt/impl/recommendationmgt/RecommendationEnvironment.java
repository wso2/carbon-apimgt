/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.recommendationmgt;

public class RecommendationEnvironment {

    private String recommendationServerURL;
    private String oauthURL;
    private String consumerKey;
    private String consumerSecret;
    private String userName;
    private String password;
    private int maxRecommendations = 5;
    private boolean applyForAllTenants = true;

    public String getRecommendationServerURL() {

        return recommendationServerURL;
    }

    public void setRecommendationServerURL(String recommendationServerURL) {

        this.recommendationServerURL = recommendationServerURL;
    }

    public String getOauthURL() {

        return oauthURL;
    }

    public void setOauthURL(String oauthURL) {

        this.oauthURL = oauthURL;
    }

    public String getConsumerKey() {

        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {

        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {

        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {

        this.consumerSecret = consumerSecret;
    }

    public int getMaxRecommendations() {

        return maxRecommendations;
    }

    public void setMaxRecommendations(int maxRecommendations) {

        this.maxRecommendations = maxRecommendations;
    }

    public boolean isApplyForAllTenants() {

        return applyForAllTenants;
    }

    public void setApplyForAllTenants(boolean applyForAllTenants) {

        this.applyForAllTenants = applyForAllTenants;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }
}

