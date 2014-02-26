/*
*Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.impl.token;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.SortedMap;

/**
 * This interface encapsulates a user claims retriever.
 * The retrieved claims are encoded to the JWT during subscriber validation
 * in the order defined by the SortedMap.
 * Anyone trying to add custom user properties to the JWT should implement this interface
 * and mention the fully qualified class name in api-manager.xml ->
 * APIConsumerAuthentication -> ClaimsRetrieverImplClass
 */
public interface ClaimsRetriever {

    String CLAIMS_RETRIEVER_IMPL_CLASS = "APIConsumerAuthentication.ClaimsRetrieverImplClass";

    String CONSUMER_DIALECT_URI = "APIConsumerAuthentication.ConsumerDialectURI";

    String DEFAULT_DIALECT_URI = "http://wso2.org/claims";

  /**
     * Initialization method that runs only once.
     *
     * @throws APIManagementException
     */
    void init() throws APIManagementException;

  /**
     * Method that retrieves user claims
     *
     * @return a sorted map
     *                  keys - claimURIs
     *                  values - claim values.
     * @throws APIManagementException
     */
    SortedMap<String,String> getClaims(String endUserName) throws APIManagementException;

  /**
     * Must return the dialect URI of the user ClaimURIs.
     *
     * @throws APIManagementException
     */
    String getDialectURI(String endUserName);

}
