/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;

/**
 * Interface through which API consumers are authenticated. An implementation of this interface
 * is used by the APIAuthenticationHandler to validate incoming requests. Implementations of this
 * interface never returns false when an authentication failure occurs. All authentication errors
 * are signaled by throwing an APISecurityException.
 */
public interface Authenticator {

    /**
     * Initializes this authenticator instance.
     *
     * @param env Current SynapseEnvironment instance containing the global/tenant configuration
     */
    public void init(SynapseEnvironment env);

    /**
     * Destroys this authenticator and releases any resources allocated to it.
     */
    public void destroy();

    /**
     * Authenticates the given request to see if an API consumer is allowed to access
     * a particular API or not. If the request can be properly authenticated, this method
     * should return true. However implementations of this method must never return
     * false. For all authentication errors and other unexpected error conditions, this
     * method must throw an APISecurityException. Implementations of this method should
     * also try to add some AuthenticationContext information into the successfully
     * authenticated requests, so that other components processing the same requests
     * can access user information easily.
     *
     * @param synCtx The message to be authenticated
     * @return true if the authentication is successful (never returns false)
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    public boolean authenticate(MessageContext synCtx) throws APISecurityException;

    /**
     * Returns a string representation of the authentication challenge imposed by this
     * authenticator. In case of an authentication failure this value will be sent back
     * to the API consumer in the form of a WWW-Authenticate header.
     *
     * @return A string representation of the authentication challenge
     */
    public String getChallengeString();
    
    public String getRequestOrigin();
    
}
